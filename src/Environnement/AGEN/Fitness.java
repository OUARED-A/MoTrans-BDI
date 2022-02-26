package Environnement.AGEN;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Vector;

import org.jgap.FitnessFunction;
import org.jgap.IChromosome;
import org.jgap.impl.CompositeGene;
import org.jgap.impl.IntegerGene;
import Environnement.*;


 public class Fitness extends FitnessFunction{
	
	private Compte compte = null; 
	private IChromosome codage;
	private Vector<Attribut> AttributCodage;
	private Vector<Vector> SouDomAttrCod;
	private Vector<Vector<Double>> SelDim = null;
	private Vector<Vector<Double>> SelFait = null;
	private Vector<Requete> requetes = new Vector<Requete>(); 
	private Vector<Table> tablesCandidates = null;
	private Vector<String> indexAttributs = null;
	private Vector<String> indexTables = null;
	private Vector<String> indexTablesFragmentees = null;
	private Vector<Integer> BlocsValides = null;
	private double CoutInit = 0, coutSCH = 0;
	private String ModelCout = "";
	private int cptCloseConnect = 0;
	private Vector<Integer> nbrSsDomParAttr = AGexemple.nbrSsDomParAttr;
	private int W = 0, SI=0, SVM=0, RowID=0; //contrainte du nombre de fragments fait max
	private boolean IJB = false, VM = false, FragH = false;
	private Vector<Table> tabsFH = null;
	private Vector<Attribut> AttrDeTablesDeFrag = null;

	@SuppressWarnings("static-access")
	Fitness(int W, int SI, int SVM, int RowID, Compte compte, Vector<Requete> req, Vector<Attribut> AttributCodage, 
			Vector<Vector> SouDomAttrCod, Vector<Vector<Double>> SDim, Vector<Vector<Double>> SFait, 
			Vector<Table> tablesCandidates, boolean FragH, boolean IJB, boolean VM ) 
	{
		this.W = W;	this.SI = SI/*octects*/; this.SVM = SVM/*octects*/; this.RowID = RowID/*octects*/;
		this.IJB = IJB;	this.VM = VM; this.FragH = FragH;
		this.AttributCodage = AttributCodage;
		this.SouDomAttrCod = SouDomAttrCod;
		this.SelDim = SDim;
		this.SelFait = SFait;
		this.requetes = req; 
		this.tablesCandidates = tablesCandidates;
		this.compte = compte;
		System.gc();
		//vecteur contenant les noms des attributs 
		indexAttributs = new Vector<String>(AttributCodage.size());
		for(int i=0; i<this.AttributCodage.size(); i++) 
			indexAttributs.add(AttributCodage.elementAt(i).GetNomAtt().toUpperCase());
		
		//vecteur contennats les nonms des tables candidates
		indexTables = new Vector<String>(tablesCandidates.size());
		for(int i=0; i<this.tablesCandidates.size(); i++) 
			indexTables.add(tablesCandidates.elementAt(i).getNomTable().toUpperCase());
		
		/** calcul du cout des requetes sur le shema globale (sans fragmentation) **/
		Vector<Vector<Integer>> Cinitiale = new Vector<Vector<Integer>>();
		Vector<Integer> L = new Vector<Integer>();
		
		for (int i = 0; i < SelDim.size(); i++) { 
	        for (int j=0; j< SelDim.elementAt(i).size(); j++) 
	        L.add(1);
	        Cinitiale.add(L); L = new Vector<Integer>();
		                                         }
		
		this.AttrDeTablesDeFrag = new Vector<Attribut>();
		this.tabsFH = new Vector<Table>();
		
		for(int i=0; i<this.requetes.size(); i++) {
	    	/*System.out.println();
	    	System.out.println("Cout Initiale Requete num : "+i+" :");
	    	System.out.println();*/
	    if(Valide(Cinitiale,this.requetes.elementAt(i)))
			 { 
				  Requete Q = new Requete(requetes.elementAt(i));
				  ReecRequete(Cinitiale, Q);
				  CoutInit += CoutSsSchema(Cinitiale, Q) * Q.getFrequence();
			 }
	                                               }
		
		System.out.println();
		System.out.println("Cout du Schema Initiale = "+CoutInit);
    	System.out.println();    	
    	
    	//Affichage de la matrice de selectivit� des ss dom du frag fait
		System.out.println("************Selectivit� du Fait************");
		for(int i=0; i<SelFait.size(); i++) {
			for(int j = 0; j < SelFait.elementAt(i).size(); j++)
				System.out.print(" "+SelFait.elementAt(i).elementAt(j));
			    System.out.println();
		                                     }
		System.out.println("**********************************");
	}
	
	@Override
	public double evaluate(IChromosome individu) 
	{
		// TODO Auto-generated method stub
		this.codage = individu;
		definirTablesAFragmentees();
		/*
		//Modifi� Chrom
	    for (int i = 0; i < this.codage.size(); i++) {
	        CompositeGene comp = (CompositeGene)this.codage.getGene(i); 
	        for (int j=0; j< comp.size()-1; j++) 
	        comp.geneAt(j).setAllele(j+1); 
	                                                 }*/
		/** affichage du chromosome **/
		System.out.println();				
		    for (int i = 0; i < this.codage.size(); i++) 
		    {
		    	CompositeGene comp = (CompositeGene) this.codage.getGene(i);
				System.out.print((i + 1) + ") ");
				for (int j = 0; j < nbrSsDomParAttr.elementAt(i); j++)
					System.out.print(" "+((Integer) ((IntegerGene) comp.geneAt(j)).getAllele()).intValue());
				    System.out.println();
				// affichage de la case du Frag vertical
				System.out.println("\t\t"+ ((Integer) ((IntegerGene) comp.geneAt
						           (nbrSsDomParAttr.elementAt(i))).getAllele()).intValue());
				//affichage de la conf des vues materialis�es
				System.out.print("\t\t  ");
				for (int j = nbrSsDomParAttr.elementAt(i)+1; j < (nbrSsDomParAttr.elementAt(i)*2)+1; j++)
					System.out.print(" "+((Integer) ((IntegerGene) comp.geneAt(j)).getAllele()).intValue());
				    System.out.println();
		    }
		System.out.println();
		
		for(int i=0; i<nbrSsDomParAttr.size(); i++) System.out.print(" "+nbrSsDomParAttr.elementAt(i));
		System.out.println();
		
		/** generer les sous schemas **/
		Vector<Vector<Integer>> ensSCH = extraireSChemas();
		
			
		/** calcul du cout du schema avec FH ou IJB**/
		double CoutSchema = 0, Gain=0, Cost1 = 0, Cost2=0; 
		int sc=0; 
		Vector<Object> ensMB = new Vector<Object>(ensSCH.size());
		
	//parcours de l'ens des sous schemas
	for(int k=0; k<ensSCH.size(); k++) 
	{
			Vector<Integer> ligne = ensSCH.elementAt(k);
			
			//on construit la matrice binaire qui represente le ss schema
		    Vector<Vector<Integer>> CODE = new Vector<Vector<Integer>>(codage.size());
		    for(int i=0; i<ligne.size(); i++)
		    {   
		    	CompositeGene comp = (CompositeGene)codage.getGene(i);
		    	Vector<Integer> v = new Vector<Integer>();
		    	for(int j=0; j<nbrSsDomParAttr.elementAt(i); j++) 
	              if (((Integer)((IntegerGene)comp.geneAt(j)).getAllele()).intValue() == ligne.elementAt(i))
		            	v.add(1);
	              else  v.add(0);
		    	CODE.add(v);
		    }
		    			
		    ensMB.add(CODE);
	    
			//System.out.println();
			String SCH="";
			for(int n=0; n<ligne.size(); n++) SCH+=ligne.elementAt(n)+"-";
			sc++; //System.out.println("sous Schema n� : "+sc);*/
			
			/** parcours de la charge de requetes **/
			for(int i=0; i<this.requetes.size(); i++) 
			{
				System.out.println();
				System.out.println("Requete num : "+i+" SCH : "+SCH);
				System.out.println();
	    	
				if(Valide(CODE,this.requetes.elementAt(i))) 
					{ 	    	
						System.out.println("sous Schema "+SCH+" n� : "+sc+" est valide pour la requete "+i);
						Requete Q = new Requete(this.requetes.elementAt(i));	    	
						ReecRequete(CODE, Q);
					if(FragH)
						{	
							System.out.println();
							System.out.println("Calcul du cout de FH : ");
							System.out.println();	    	
						
							Cost1 = CoutSsSchema(CODE, Q) * Q.getFrequence(); //cout FH	 
							System.out.println("Cout FH de la requete "+i+" = "+Cost1);
						}
						
					if(IJB)
				    	{
				    		System.out.println();
				    		System.out.println("Calcul du cout de IJB : ");
				    		System.out.println();
				    	
				    		Cost2= CoutIndex(CODE, Q) * Q.getFrequence(); //cout IJB et FH
				    		System.out.println("Cout IJB de la requete "+i+" = "+Cost2);
				    	}
						if(IJB && FragH)
				    		if(Cost1 <= Cost2) {	CoutSchema+=Cost1;
				    								System.out.println("Cout FH de la requete ");
				    	                    	}
				    							    	
				    		else	{ 	
				    					CoutSchema+=Cost2;
				    					System.out.println("Cout IJB de la requete ");
			            		 	}							
				    	
					else if(FragH)
							{	/** Cas FH sans IJB **/
								CoutSchema += Cost1;
								System.out.println("Cout FH seule de la requete ");
							}
						else if(IJB)
							{ /** Cas IJB sans FH **/ 	
								CoutSchema += Cost2;
								System.out.println("Cout IJB seule de la requete ");
        		 			}					
					}//fin verif validit�
				
				else System.out.println("sous Schema non valide pour la requete "+i);
			}
	}	
	    /** ajout du cout des jointures du select avec les dims non fragment�es utilis�es que pr le select**/
		//parcours de la charge de requetes
	if(IJB || FragH)
		for(int i=0; i<this.requetes.size(); i++) 
		{				
				double selReq = SelReqED(this.requetes.elementAt(i)); //sel requete ds l'entrepot
				CoutSchema+= coutJoinSelect(this.requetes.elementAt(i), selReq);
		}				
		System.out.println();
		System.out.println("Cout du schema = "+CoutSchema);
		      
		Gain = CoutInit - CoutSchema;
		System.out.println("Gain = "+Gain);
		      
		double PenaliteFH = 0, PenaliteIJB = 0, FxFH_IJB = Gain, FxVM = 0.0;
		HashSet<Integer> HS = new HashSet<Integer>(); 
		int ind; int nbFrag=1;
		CompositeGene comp;
		
        /** calcul du nombre de fragments de ce shema **/
		for (int i = 0; i < codage.size(); i++) 
		{
		    comp = (CompositeGene)codage.getGene(i);
		    HS.clear();
		    for (int j=0; j< nbrSsDomParAttr.elementAt(i); j++)
		    {
		    	ind = ((Integer)((IntegerGene)comp.geneAt(j)).getAllele()).intValue(); 	
		    	HS.add(ind); 
		    }
		    nbFrag *= HS.size();                  
		}	   
		
	       	PenaliteFH = (double) this.W / (double) nbFrag;
	       	PenaliteIJB = (double) SI / TailleConfIJB(codage); 
                
                
	       	if(FragH)
                    
	       		if(Gain < 0.0 ) 
	       		{
	       			Gain = Gain - ((PenaliteFH * Gain) - Gain);
	       		}
	       		else Gain = Gain * PenaliteFH;
	       	if(IJB)
	       		if(Gain < 0.0 ) 
	       		{
	       			Gain = Gain - ((PenaliteIJB * Gain) - Gain);
	       		}
	       		else Gain = Gain * PenaliteIJB;
	    
	      FxFH_IJB = Gain; 	
	       	
	      System.out.println();
		  System.out.println(" nbr Fragments = "+nbFrag+" Pen = "+PenaliteFH);		
	      System.out.println("Cout renvoy� par la fonction avec Pen "+PenaliteFH+" = "+FxFH_IJB);
	      
	      /*****************************************************************************/
	      double CoutVM = 0.0;  
if(VM)
 {
	    System.out.println();
	  	System.out.println("Calcul du cout avec VM : ");
	  	System.out.println();
	  	
	  	/** construction du vecteur des vecteurs des vuesM du codage **/
		Vector<Vector<Integer>> RepVM = new Vector<Vector<Integer>>();
		for(int i=0; i<this.codage.size(); i++)
		{
			Vector<Integer> Vint = new Vector<Integer>();
			comp = (CompositeGene) this.codage.getGene(i);
			for(int j=nbrSsDomParAttr.elementAt(i)+1; j<comp.size(); j++)
				Vint.add(((Integer)((IntegerGene)comp.geneAt(j)).getAllele()).intValue()); 
			RepVM.add(Vint);
		}
	  	
	  	/** construction du schema initiale (sans fragmentation) **/
	  	Vector<Vector<Integer>> Cinitiale = new Vector<Vector<Integer>>();
	  	Vector<Integer> L = new Vector<Integer>();
	  	
	  	for (int i = 0; i < SelDim.size(); i++) { 
	          for (int j=0; j< SelDim.elementAt(i).size(); j++) 
	          L.add(1);
	          Cinitiale.add(L); L = new Vector<Integer>();
	  	                                         }
	  	
	  	for(int i=0; i<this.requetes.size(); i++)	  	
	  		if(Valide(Cinitiale, this.requetes.elementAt(i)))
	  		{	
	  			Requete Q = new Requete(requetes.elementAt(i));
	  			ReecRequete(Cinitiale, Q);
                                
	  			
	  			//Affichage de chaque requete
	  			System.out.println();
	  			for (int p=0; p< Q.getBlocsOR().size(); p++) 
	  			{
	              	System.out.println("                   Bloc OR n� : "+p);
	              	for (int h=0; h< Q.getBlocsOR().elementAt(p).size(); h++) 
	              	{
	              		Attribut a = Q.getBlocsOR().elementAt(p).elementAt(h);
	                  
	              		System.out.println("table: "+ a.GetTableAtt());
	              		System.out.println("\tattribut: "+a.GetNomAtt()+"\t    Ses Valeurs :");
	              		for(int cp=0; cp<a.valeurs.size(); cp++) 
	              		System.out.println("val "+a.operateurs.elementAt(cp)+" "+a.valeurs.elementAt(cp));
	                 }
	             }					
	  			System.out.println();
	  			
	  			double costVM = CoutVM(RepVM, Q, ensMB);
	  			CoutVM += costVM;
	  			
	  			System.out.println("Cout de la requete "+i+" = "+costVM);
	  		}
	  	System.out.println();
	  	System.out.println("Cout avec VM = "+CoutVM);
	  	
	  	Gain = CoutInit - CoutVM;
	  	double penVM = (double) SVM / TailleConfVM(codage);
	  	if(Gain < 0.0 ) 
       	{
    	   FxVM = Gain - ((penVM * Gain) - Gain);
       	}
       else FxVM = Gain * penVM;
	  	
		System.out.println("Gain VM = "+Gain);	
 }	       
double Fx=0.0;
if(VM) 	if(IJB || FragH)
	   		if(FxFH_IJB >= FxVM) {Fx = FxFH_IJB; coutSCH = CoutSchema; ModelCout = "FH_IJB";}
	   		else {Fx = FxVM; coutSCH = CoutVM; ModelCout = "VM";}
		else {Fx = FxVM; coutSCH = CoutVM; ModelCout = "VM";}
else {Fx = FxFH_IJB; coutSCH = CoutSchema; ModelCout = "FH_IJB";}
		
		return (Fx);		
	}

	public double getCoutInit() {
		return this.CoutInit;
	}
	
	public double getCoutSCH() {
		return this.coutSCH;
	}
	
	public String getModelCout() {return this.ModelCout;}
	
	private void definirTablesAFragment�es()
	{
		int decal = 0; this.tabsFH = new Vector<Table>();
		this.AttrDeTablesDeFrag = new Vector<Attribut>();
		//parcours des tables candidates
		for(int i=0; i<tablesCandidates.size(); i++)
		{ boolean Fh = false;
			//parcours des attributs de chaque table
			int j;
			for(j=decal; j<decal+FH.nbrAttrParTable.elementAt(i); j++)
				{	
					CompositeGene comp = (CompositeGene) this.codage.getGene(j);
					HashSet<Integer> HS = new HashSet<Integer>(this.nbrSsDomParAttr.elementAt(j));
					//parcours des sous domaines de chaque attribut
					for(int k=0; k<this.nbrSsDomParAttr.elementAt(j) && HS.size()<=1; k++)
						HS.add(((Integer)((IntegerGene)comp.geneAt(k)).getAllele()).intValue());
					if(HS.size()>=2) {Fh=true; break;} 
				}
			if(Fh) tabsFH.add(tablesCandidates.elementAt(i));
			decal+=FH.nbrAttrParTable.elementAt(i);
		}
		for(int i=0; i<AttributCodage.size(); i++)
			{	
				int j=0;
				for(j=0; j<tabsFH.size(); j++)
					if(tabsFH.elementAt(j).getNomTable().equals(AttributCodage.elementAt(i).GetTableAtt()))
						break;
				if(j<tabsFH.size()) this.AttrDeTablesDeFrag.add(this.AttributCodage.elementAt(i));
			}
	}
	
	private Vector<Vector<Integer>> extraireSChemas() {
		
		HashSet<Integer> HS = new HashSet<Integer>(); int ind; int nbFrag=1;
	    Vector<Integer> VMaxAtt = new Vector<Integer>();
	    Vector<Integer> itChange = new Vector<Integer>();
	    Vector<Integer> itCour = new Vector<Integer>();
	    Vector<Integer> valCour = new Vector<Integer>();
	    CompositeGene comp;
  	    Vector<Integer> ligne = new Vector<Integer>(); 
  	    Vector<Vector<Integer>> ensSCH = new Vector<Vector<Integer>>();
	    
	    /** calcul du nombre de fragments de chaque attribut **/
	    for (int i = 0; i < this.codage.size(); i++) {
	        comp = (CompositeGene)this.codage.getGene(i);
	        HS.clear();
	        for (int j=0; j< nbrSsDomParAttr.elementAt(i); j++){
	        ind = ((Integer)((IntegerGene)comp.geneAt(j)).getAllele()).intValue(); 	
	        HS.add(ind); 
	                                             }
	        //System.out.print(HS.size()+" ");
	        VMaxAtt.add(HS.size());
	        nbFrag *= HS.size();
	}
	    /*System.out.println("Nb Fragments : "+nbFrag);
	    System.out.println("Vecteur nbr frag attribut : ");
	    for(int i=0; i<VMaxAtt.size(); i++) System.out.print(" "+VMaxAtt.elementAt(i));
	    System.out.println();*/
	    
	  //calcul pour chaque attribut le nbr d'iterations a faire avant de changer de valeur
	    for(int i=0; i<this.codage.size(); i++) {
	    	nbFrag=1;
	    	for(int j=i+1; j<this.codage.size(); j++) nbFrag*=VMaxAtt.elementAt(j);
	    	itChange.add(nbFrag);
	    }
	 /*   System.out.println("Vecteur des iterations de changement :");
	    for(int i=0; i<itChange.size(); i++) System.out.print(" "+itChange.elementAt(i));
	    System.out.println();
	    System.out.println("****************************");*/
	    
	  //on initialise les valeurs des iterations courantes et des vals courantes
	    for(int i=0; i<this.codage.size(); i++) {
	    	itCour.add(0);
	    	valCour.add(1);
	    }
	    
	    boolean Fin = false;
	    int sc = 0;
	    //affichage des series de valeurs des sous schemas
	    while (!Fin) 
	    {
	    	  ligne = new Vector<Integer>();
	    	  for(int i=0; i<this.codage.size(); i++) {	    
	    
	    	//on incremente l'iteration courante
	    	int it = itCour.elementAt(i);
	    	it++;
    		itCour.setElementAt(it, i);
	   
    		if(itCour.elementAt(i) <= itChange.elementAt(i) )
	    	 {	    		
    			ligne.add(valCour.elementAt(i));
	    		//System.out.print(" "+valCour.elementAt(i));	    		
	       	  }
    		else if(i==0 && valCour.elementAt(i)==VMaxAtt.elementAt(i)) 
    		     {Fin = true; break;}
    		else if(i>0 && valCour.elementAt(i)== VMaxAtt.elementAt(i))
    		{
    			itCour.setElementAt(1, i); 
 		        valCour.setElementAt(1, i);
 		        ligne.add(valCour.elementAt(i));
 		     // System.out.print(" "+valCour.elementAt(i));
    		}
    		else { itCour.setElementAt(1, i); 
    		       int val = valCour.elementAt(i); 
    		       val++;
    		       valCour.setElementAt(val, i);
    		       ligne.add(valCour.elementAt(i));
    		      // System.out.print(" "+valCour.elementAt(i));
    		     }
	    }
	    if(Fin) break;    	
	    	    
	    ensSCH.add(ligne);  
	    
	    }//fin while
	    
	      return ensSCH;
	}
	
	private boolean Valide(Vector<Vector<Integer>> C, Requete Q) {
		
		boolean valide = false;	BlocsValides = new Vector<Integer>();
		
		//on parcours les blocs OR de la requette 
		for(int h=0; h<Q.getBlocsOR().size(); h++) 
		{		
			Vector<Attribut> Bloc = Q.getBlocsOR().elementAt(h);
			int k;
			
		//si le Bloc est valide on recherche les attributs de fragmentations correspondants aux attributs du bloc OR 
		if(existPredSsSchema(Bloc, C))
	   {			
			/** si le ss shema contient les valeurs des predicats d'un bloc de la requete alors il est valide **/			
			//System.out.println("Bloc "+h+" Valide");	     
			valide = true;
			BlocsValides.add(h);        
			
		//parcours des attributs du bloc	
		for(k=0; k < Bloc.size(); k++)
		{   
			Attribut attribut = Bloc.elementAt(k);
			
			//on recupere l'indice de l'attribut de fragmentation ( j )
		int j = indexAttributs.indexOf(attribut.GetNomAtt().toUpperCase());
		attribut.L = j;	attribut.SDvals = new Vector<Integer>();
		
		/*System.out.println("Valide Attribut = "+attribut.GetNomAtt()+" "+attribut.operateurs.firstElement()+" "
							+attribut.valeurs.firstElement());*/
		
		if(j!=-1) {/**c'est un attribut de fragmentation **/  
	
	/** si l'attribut est de type string **/
	if(AttributCodage.elementAt(j).type.equals("string")) 
	{			
		int i; attribut.type = "string";
		
		/**on recherche le numero du ss domaine correspendant � chaque valeur de l'attribut du bloc OR**/
		for(i=0; i<attribut.valeurs.size(); i++)
		{
			Vector<Integer> isd = new Vector<Integer>();	
		    String str = attribut.operateurs.elementAt(i) +" "+ attribut.valeurs.elementAt(i);
		    
		    if(attribut.operateurs.elementAt(i).equals("=")) 
		  { 
		    int iSD = 0;		    
		    for(iSD=0; iSD < SouDomAttrCod.elementAt(j).size(); iSD++){
		    	String s = (String) SouDomAttrCod.elementAt(j).elementAt(iSD);
		        if (s.indexOf((String)attribut.valeurs.elementAt(i))!=-1) break;			
		                                                               }
			if(iSD < SouDomAttrCod.elementAt(j).size())
			{ // System.out.println("SD "+SouDomAttrCod.elementAt(j).elementAt(iSD));
			  isd.add(iSD);		
			  attribut.SDvals = (Vector<Integer>) isd.clone();
		    }
			
		 }/** fin du cas operateur (=) **/ 
		    
		    else if(attribut.operateurs.elementAt(i).equals("<>"))
		         {//cas du operateur (<>)	    	
		    	//on remplie les ss domaines valides des valeurs
		    	   for(int t=0; t<SouDomAttrCod.elementAt(j).size(); t++){
		    		   String s = (String) SouDomAttrCod.elementAt(j).elementAt(t);
		    		   if(s.indexOf((String)attribut.valeurs.elementAt(i))==-1 || str.indexOf(",")!=-1 || str.indexOf("and")!=-1)   
		    			   isd.add(t);
		    	                                                           }
		    	   if(isd.size()>0) attribut.SDvals = (Vector<Integer>) isd.clone();
		    	  }
		    
		    else {//Cas op predicat requete IN 		    	  
		    	  //on remplie les ss domaines valides des valeurs
		    	  for(int t=0; t < SouDomAttrCod.elementAt(j).size(); t++)
		    	  {    
		        	   String sd = (String) SouDomAttrCod.elementAt(j).elementAt(t);
		        	   String predicat = (String)attribut.valeurs.elementAt(i);
		        	   
		        	   int n=predicat.indexOf(","), p=predicat.indexOf("'");
		        	   while(n!=-1) {
		        		   if(sd.contains(predicat.substring(p,n))) {isd.add(t); break;}
		        		   p=n+1; n=predicat.indexOf(",",n+1);
		        	                  }
		        	   if(n==-1 && sd.contains(predicat.substring(p))) isd.add(t);   
   	               }
		    	  
		    	   if(isd.size()>0) attribut.SDvals = (Vector<Integer>) isd.clone();
		          }   	  		    		   
		} //fin parcours des valeurs de l'attribut	
	
	 }//fin verif = string				
	
	 else  if(AttributCodage.elementAt(j).type.equals("entier")) 
	        {//entier
		                   Bloc.elementAt(k).type = "entier";
		                   
				/** on remplie les num�ros des SDs de chaque valeur de l'attribut **/ 
				for(int i=0; i<attribut.valeurs.size(); i++)
				{
					Vector<Integer> vi  = new Vector<Integer>();
					System.out.println("att : "+attribut.GetNomAtt()+" "+attribut.valeurs.elementAt(i));
					int val = Integer.parseInt(String.valueOf(attribut.valeurs.elementAt(i)));
					
				  for(int y=0; y<SouDomAttrCod.elementAt(j).size(); y++)
				  {					
					Intervalle I = (Intervalle) SouDomAttrCod.elementAt(j).elementAt(y);
										
					//on verifie les diffs types d'operateurs			
					if(attribut.operateurs.elementAt(i).equals(">")) 
					{if(I.sup > val) vi.add(y);}
					
					if(attribut.operateurs.elementAt(i).equals("<")) 
					{if(I.inf < val) vi.add(y);}
					
					if(attribut.operateurs.elementAt(i).equals(">=")) 
					{if(I.sup >= val) vi.add(y);}
					
					if(attribut.operateurs.elementAt(i).equals("<=")) 
					{if(I.inf <= val) vi.add(y);}
					
					if(attribut.operateurs.elementAt(i).equals("=")) 
					{if(I.inf <= val && I.sup>=val) vi.add(y);}
					
					if(attribut.operateurs.elementAt(i).equals("<>")) 
					{if(I.inf != val || I.sup!=val) vi.add(y);}
				   }
				  if(vi.size()>0) 
					if(i==0)  attribut.SDvals = (Vector<Integer>) vi.clone();
	                else
	                    { //on fait l'intersection entre les deux vecteurs de ss doms valides
		 			 	 for(int v=0; v<vi.size(); v++) if(!attribut.SDvals.contains(vi.elementAt(v)))
		 			 	                                  {vi.removeElementAt(v); v--;}
		                 attribut.SDvals = (Vector<Integer>) vi.clone();
	                    }
				else {attribut.SDvals = new Vector<Integer>(); break;} 
				}
				
			}//fin verif entier
	
	else {/** Reel **/
	         Bloc.elementAt(k).type = "reel";
			//on remplie les num�ros des SDs de chaque valeur de l'attribut 
			for(int i=0; i<attribut.valeurs.size(); i++)
			{
				Vector<Integer> vi  = new Vector<Integer>();
				double val = Double.parseDouble(String.valueOf(attribut.valeurs.elementAt(i)));
				
			for(int y=0; y<SouDomAttrCod.elementAt(j).size(); y++)
			  {					
				IReel R = (IReel) SouDomAttrCod.elementAt(j).elementAt(y);
									
				//on verifie les diffs types d'operateurs
				//on enregistre les ss doms correspendants � chaque valeure 
				if(attribut.operateurs.elementAt(i).equals(">")) 
				{if(R.sup > val) vi.add(y);}
				
				if(attribut.operateurs.elementAt(i).equals("<")) 
				{if(R.inf < val) vi.add(y);}
				
				if(attribut.operateurs.elementAt(i).equals(">=")) 
				{if(R.sup >= val) vi.add(y);}
				
				if(attribut.operateurs.elementAt(i).equals("<=")) 
				{if(R.inf <= val) vi.add(y);}
				
				if(attribut.operateurs.elementAt(i).equals("=")) 
				{if(R.inf <= val && R.sup>=val) vi.add(y);}
				
				if(attribut.operateurs.elementAt(i).equals("<>")) 
				{if(R.inf != val || R.sup!=val) vi.add(y);}
			  }
			if(vi.size()>0) if(i==0)  attribut.SDvals = (Vector<Integer>) vi.clone();
			                else
			                    { //on fait l'intersection entre les deux vecteurs de ss doms valides
				 			 	 for(int v=0; v<vi.size(); v++) if(!attribut.SDvals.contains(vi.elementAt(v)))
				 			 	                                  {vi.removeElementAt(v); v--;}
				                 attribut.SDvals = (Vector<Integer>) vi.clone();
			                    }
			else {attribut.SDvals = new Vector<Integer>(); break;}  
			}
	  }/** fin verif reel **/
	
		}/** fin verif indice attribut j **/		
				
		}/**fin parcours des predicats du bloc **/
		
	    }/** fin test validit� du bloc **/
		else {/*System.out.println("Bloc "+h+" non Valide");
        System.out.println();*/}
		
		}/** fin parcours des blocs de la requete **/
		
		/*System.out.println();
		if(!valide) System.out.println("Sous Schema non Valide");
		if( valide)  System.out.println("Sous Schema Valide");
		System.out.println();*/
		
		return valide;
	}

	@SuppressWarnings({ "unused", "unchecked" })
	private double CoutSsSchema(Vector<Vector<Integer>> C, Requete Q){
		
		Compte cmpt = this.compte; String str = "";		
		Vector<Double> SommeLigneJoinF = new Vector<Double>();
		Vector<Vector<Double>> selFragFait = new Vector<Vector<Double>>();
		
		/*System.out.println();
		System.out.println("**********Le Fragment Fait***********");
		System.out.println();*/
		
		double selF = selFragFait(C);
			
			
		  /*System.out.println("SelF = "+selF);
		  System.out.println("-------------------------");*/
		
		/** construction de la matrice de selectivit� des ss dom du frag fait **/
		for(int i=0; i<SelFait.size(); i++) 
		{
			Vector<Double> V = new Vector<Double>();
			for(int j = 0; j < SelFait.elementAt(i).size(); j++)
			{				
			   if(C.elementAt(i).elementAt(j)==0) V.add(0.0);
			   else {
				     double sel = SelFait.elementAt(i).elementAt(j);
				     for(int c=0; c<SelFait.size(); c++)
				    	 if(c!=i) {
				    		       double sumLigne=0;
				    		       for(int k=0; k<SelFait.elementAt(c).size(); k++)
				    		    	   if(C.elementAt(c).elementAt(k)==1) sumLigne+=SelFait.elementAt(c).elementAt(k);
				    		       sel*=sumLigne;
				    	          }
				         V.add(sel / selF);
			        }
			}
			selFragFait.add(V);
			SommeLigneJoinF.add(0.0);
		}
		
		//Affichage de la matrice de selectivit� des ss dom du frag fait
		/*System.out.println("************Selectivit� du Frag Fait************");
		for(int i=0; i<selFragFait.size(); i++) {
			for(int j = 0; j < selFragFait.elementAt(i).size(); j++)
				System.out.print(" "+selFragFait.elementAt(i).elementAt(j));
			    System.out.println();
		                                     }
		System.out.println("**********************************");*/
		
		/*//affichage de la matrice du sous schema 		
            for(int i=0; i<C.size(); i++) {		
			for(int j = 0; j < C.elementAt(i).size(); j++) 
				System.out.print(" "+C.elementAt(i).elementAt(j));
				System.out.println();
				}		*/
		
	/*	Vector<Double> SelDimFait = new Vector<Double>(); 
		for(int i = 0; i<tablesCandidates.size(); i++) SelDimFait.add(1.0);
		
		//calcul de la selectivit� de chaque fragment de dimension dans le fait
		for(int i=0; i<C.size(); i++) {
			double selLigne = 0;
			
			for(int j = 0; j < C.elementAt(i).size(); j++) 
			 if(C.elementAt(i).elementAt(j)==1) selLigne+=this.SelFait.elementAt(i).elementAt(j);	
			
			
			//recherche de l'indice de la table correspendante � l'attribut n� i
			int k;
            for(k=0; k<tablesCandidates.size() && 
           !AttributCodage.elementAt(i).GetTableAtt().toUpperCase().equals
           (tablesCandidates.elementAt(k).getNomTable()); k++);
	 
			    SelDimFait.setElementAt(SelDimFait.elementAt(k) * selLigne, k);
		}	*/
		
		//on recupere la table de fait
		Table fait = cmpt.getTableFaits();
		//on recupere le nbr de tuples de la table de fait
		long nbTupFait = fait.getNb_Tuple();
		//System.out.println("nb tuples fait = "+ nbTupFait);
		//on calcul le nbr de tuples du fragment fait du sous schema
		long nbTupFragFait = (long) Math.ceil(selF * nbTupFait);
		//System.out.println("nb de tuples du fragment fait de ce sous schema = "+ nbTupFragFait);
		//on calcul la taille du fragment fait
		double tailleTuples = nbTupFragFait * fait.getTailleTuple();
		//System.out.println("taille frag fait = "+ tailleTuples);
		//on calcul la taille du fragment fait en Pages Systems 
		long TailleFaitPS = (long) Math.ceil(tailleTuples / AGexemple.PS);
		//System.out.println("taille frag fait en PS = "+ TailleFaitPS);
		
		//Calcul du nombre d'attributs de fragmentation pr chaque table
		Vector<Integer> nbrAttrParTable = new Vector<Integer>();
		for(int i=0; i<tablesCandidates.size(); i++) { 
		int nb = 0;		
		for(int j = 0; j < AttributCodage.size(); j++)
			if(tablesCandidates.elementAt(i).getNomTable().equals(AttributCodage.elementAt(j).GetTableAtt().toUpperCase()))
		        nb++; 
			    nbrAttrParTable.add(nb);
		                                              }
		
		/** calcul de la selectivit� de chaque attribut des blocs valides de la requete dans le fait **/
		Vector<String> V = new Vector<String>();
		Vector<String> TableNonC = new Vector<String>();
		Vector<Double> SelAttrNonCTabNonC = new Vector<Double>();
		Vector<String> AttrNonCTabNonC = new Vector<String>();
		Vector<Double> SelAttrNonCTabC = new Vector<Double>(tablesCandidates.size());
		Vector<String> AttrNonCTabC = new Vector<String>();
		Vector<Vector<Object>> HistPredAttFrag = new Vector<Vector<Object>>(this.AttributCodage.size()); 
		for(int i =0; i<AttributCodage.size(); i++) HistPredAttFrag.add(null);
		Vector<Object> HistPredAttNonFragTabNonC = new Vector<Object>();
		Vector<Object> HistPredAttNonFragTabC = new Vector<Object>();
		Vector<String> indexAttrTablesDeFrag = new Vector<String>();
		for(int i=0; i<AttrDeTablesDeFrag.size(); i++) 
			indexAttrTablesDeFrag.add(AttrDeTablesDeFrag.elementAt(i).GetNomAtt());
				
		//parcours des blocs OR
		for(int o=0; o<Q.getBlocsOR().size(); o++)
		{	
		 //System.out.println("bloc : "+o+" / "+Q.getBlocsOR().size());
		 Vector<Attribut> Bloc = Q.getBlocsOR().elementAt(o);
		 /*Vector<Double> VerifBlocPertinant = new Vector<Double>();
		 Vector<String> AttrNonC = new Vector<String>();
		 for(int i=0; i< AttributCodage.size(); i++) VerifBlocPertinant.add(0.0);*/
		 
		 //parcours des attributs du bloc
			for(int u=0; u<Bloc.size(); u++) 
			{   double sommel = 0;
				Attribut a = Bloc.elementAt(u);
				//System.out.println("attr a analys� : "+a.GetNomAtt());
					//verf si c un attribut d'une table fragment�e
				    if(indexAttrTablesDeFrag.indexOf(a.GetNomAtt())!=-1)//verif si c un attr du codage
				    	 if(a.type.equals("string")) 
				    		 for(int i=0; i<a.SDvals.size(); i++) //parcours des ss Doms valides du predicat
				    		 	{   
				    			 int isd = a.SDvals.elementAt(i);
				    			 if(C.elementAt(a.L).elementAt(isd) == 1 && !V.contains(a.L+" "+isd) 
								          && (isd < selFragFait.elementAt(a.L).size()))//pr le cas d reste sil n'esxiste pas 
				    			 	{
				    				 sommel = selFragFait.elementAt(a.L).elementAt(isd);	
				    				 SommeLigneJoinF.setElementAt(SommeLigneJoinF.elementAt(a.L)+sommel, a.L);
							    
				    				 /*VerifBlocPertinant.setElementAt(VerifBlocPertinant.elementAt(a.L)+sommel, a.L);
				    				 System.out.println(a.GetNomAtt()+" : ");
				    				 System.out.println(" sommel = "+sommel+" L:"+a.L+" SD:"+isd);
				    				 System.out.println("SommeLF = "+SommeLigneJoinF.elementAt(a.L));*/
				    				 String sd = a.L+" "+isd; 
				    				 V.add(sd);
				    			 	}
				    		 	}
				    	 else //cas type = reel ou entier et attr de Fragmentation
				    	 {   Vector<Object> Iatt = ConstrSDpred(a);
				    	 	 Vector<Object>	Hist = VectHistPred(HistPredAttFrag.elementAt(a.L), Iatt, a.type);
				    		 sommel = (double) nbTupVectHistPred(C, a, Hist)/ (double) nbTupFragFait;	
		    				 SommeLigneJoinF.setElementAt(sommel, a.L);
		    				 
		    				 HistPredAttFrag.setElementAt((Vector<Object>) Hist.clone(), a.L);
		    				 
		    				 /*VerifBlocPertinant.setElementAt(VerifBlocPertinant.elementAt(a.L)+sommel, a.L);
		    				 System.out.println(a.GetNomAtt()+" : ");
		    				 System.out.println("SommeLF = "+SommeLigneJoinF.elementAt(a.L));*/
				    	 }
				     
				     /**attribut non de frag: calculer sa selectivit� dans le frag fait **/
				     else {	 				    	   
				    //attribut non de frag ayant table de frag
				    if(indexTables.contains(a.GetTableAtt().toUpperCase()))//verif si table utilis� dans le codage
				    	//verif si c une table fragment�e
				       if(tabsFH.contains(tablesCandidates.elementAt(indexTables.indexOf(a.GetTableAtt().toUpperCase()))))
				    	     { 
				    		   if(AttrNonCTabC.contains(a.GetNomAtt()))
				    			   if(a.type.equals("string"))
				    			   		{   
				    				        int indAtt = AttrNonCTabC.indexOf(a.GetNomAtt());
				    				        double sel=(double)nbTupAttrStr(C,(Vector<String>)HistPredAttNonFragTabC.elementAt(indAtt),a)/nbTupFragFait;
				    				    	double d = SelAttrNonCTabC.elementAt(indAtt);
				    				   	 	SelAttrNonCTabC.setElementAt(sel+d, indAtt);
				    				   	 	
				    				   	 	if(SelAttrNonCTabC.elementAt(indAtt)>1)
				    				   	 	SelAttrNonCTabC.setElementAt(1.0, indAtt);
				    				   	 		
				    				   	 	System.out.println(" selAtt = "+sel);				    				   			     
				    		                System.out.println("TabC att : "+a.GetNomAtt());
				    		            }
				    		   
				    			   else { //type = reel ou entier attr de Frag
				    				     int indAtt = AttrNonCTabC.indexOf(a.GetNomAtt());
				    				   	 Vector<Object> Iatt = ConstrSDpred(a);
							    	 	 Vector<Object>	Hist = VectHistPred((Vector<Object>)HistPredAttNonFragTabC.elementAt(indAtt), Iatt, a.type);
							    		 sommel = (double)nbTupVectHistPred(C, a, Hist)/(double)nbTupFragFait;	
							    		 SelAttrNonCTabC.setElementAt(sommel, indAtt);
							    		 HistPredAttNonFragTabC.setElementAt((Vector<Object>) Hist.clone(), indAtt);
	    				   				 System.out.println(" selAtt = "+sommel);
				    			        }
				    		   //cas nouv attribut
				    		   else if(a.type.equals("string")) 
				    		          { 
				    			   		Vector<String> H = new Vector<String>();
				    			   		double sel = (double)nbTupAttrStr(C, H, a)/nbTupFragFait;
				    			        AttrNonCTabC.add(a.GetNomAtt()); 
				    			        SelAttrNonCTabC.add(sel);
				    			        HistPredAttNonFragTabC.add(H);
				    			        System.out.println("TabC nouv att nonFH str : "+a.GetNomAtt()+" selAtt = "+sel);
				    			      }
				    		        
				    		        else { //cas type = "entier" ou "reel"
				    		        		Vector<Object> Iatt = ConstrSDpred(a);
				    		        		Vector<Object>	Hist = Iatt;
				    		        		sommel = (double)nbTupVectHistPred(C, a, Hist)/(double)nbTupFragFait;
				    		        		AttrNonCTabC.add(a.GetNomAtt());
				    		        		SelAttrNonCTabC.add(sommel);
				    		        		HistPredAttNonFragTabC.add((Vector<Object>) Hist.clone());
				    		        		System.out.println("TabC nouv att nonFH num : "+a.GetNomAtt()+" selAtt = "+sommel);
				    		             }			    		   				    		   				    		                                             
				    	     }
				    	   
                           // Tables et attrs non de fragmentation				    	   
				    	   else  if(!TableNonC.contains(a.GetTableAtt().toUpperCase()))
				    		   if(a.type.equals("string"))
				    	           { 
				    			   	 TableNonC.add(a.GetTableAtt().toUpperCase());
				    	           	 AttrNonCTabNonC.add(a.GetNomAtt());
				    	           	 Vector<String> H = new Vector<String>();
				    	           	 double sel = (double) nbTupAttrStr(C, H, a)/nbTupFragFait;
				    	             SelAttrNonCTabNonC.add(sel);
				    	             HistPredAttNonFragTabNonC.add(H);
				    	             System.out.println("TabNonC Nouv att : "+a.GetNomAtt()+" selAtt = "+sel);
				    	           }
				    		   else { //type = reel ou entier
		    		        			Vector<Object> Iatt = ConstrSDpred(a);
		    		        			Vector<Object>	Hist = Iatt;
		    		        			sommel = (double)nbTupVectHistPred(C, a, Hist)/(double)nbTupFragFait;
		    		        			TableNonC.add(a.GetTableAtt().toUpperCase());
					    	            AttrNonCTabNonC.add(a.GetNomAtt());
		    		        			SelAttrNonCTabNonC.add(sommel);
		    		        			HistPredAttNonFragTabNonC.add((Vector<Object>) Hist.clone());
		    		        			System.out.println("TabNonC nouv att : "+a.GetNomAtt()+" selAtt = "+sommel);
		    		             	}  
				    
				    			//table de l'attribut existente
				    	         else if(a.type.equals("string"))
				    	         {
				    	        	    System.out.println("nom attribut nonFH "+a.GetNomAtt());
				    	        	  	int indAtt = AttrNonCTabNonC.indexOf(a.GetNomAtt());
				    	        	  	if(indAtt==-1) 
				    	        	  		{	
				    	        	  		 AttrNonCTabNonC.add(a.GetNomAtt());
						    	           	 Vector<String> H = new Vector<String>();
						    	           	 double sel = (double) nbTupAttrStr(C, H, a)/nbTupFragFait;
						    	             SelAttrNonCTabNonC.add(sel);
						    	             HistPredAttNonFragTabNonC.add(H);
						    	             System.out.println("TabNonC Nouv att : "+a.GetNomAtt()+" selAtt = "+sel);
				    	        	  		}
				    	        	  	
				    	        	  	else{
				    	 double sel=(double)nbTupAttrStr(C,(Vector<String>) HistPredAttNonFragTabNonC.elementAt(indAtt), a)/nbTupFragFait;
			    		 double d = SelAttrNonCTabNonC.elementAt(indAtt);
			    				   	 	SelAttrNonCTabNonC.setElementAt(sel+d, indAtt);
			    				   	 	
			    				   	 	if(SelAttrNonCTabNonC.elementAt(indAtt)>1)
			    				   	 		SelAttrNonCTabNonC.setElementAt(1.0, indAtt);
				    	               System.out.println("TabNonC att nonFH : "+a.GetNomAtt()+" sel = "+sel+" selAtt = "+(d+sel));
				    	        	  		}
				    	         }
				    	             else //cas type = reel ou entier
				    	             {
				    	            	 int indAtt = AttrNonCTabNonC.indexOf(a.GetNomAtt());
				    	            	 if(indAtt==-1) 
				    	        	  		{
				    	            		 	Vector<Object> Iatt = ConstrSDpred(a);
				    		        			Vector<Object>	Hist = Iatt;
				    		        			sommel = (double)nbTupVectHistPred(C, a, Hist)/(double)nbTupFragFait;
							    	            AttrNonCTabNonC.add(a.GetNomAtt());
				    		        			SelAttrNonCTabNonC.add(sommel);
				    		        			HistPredAttNonFragTabNonC.add((Vector<Object>) Hist.clone());
				    		        			System.out.println("TabNonC nouv att : "+a.GetNomAtt()+" selAtt = "+sommel);
				    	        	  		}				    	            	 
				    	            	 else 	{
				    	            	 Vector<Object> Iatt = ConstrSDpred(a);
			    		        		 Vector<Object>	Hist = VectHistPred((Vector<Object>) HistPredAttNonFragTabNonC.elementAt(indAtt), Iatt, a.type);;
			    		        		 sommel = (double)nbTupVectHistPred(C, a, Hist)/(double)nbTupFragFait;
			    		        		 SelAttrNonCTabNonC.setElementAt(sommel, indAtt);
			    		        		 HistPredAttNonFragTabNonC.setElementAt((Vector<Object>) Hist.clone(), indAtt);
			    		        		 System.out.println("TabNonC att nonFH : "+a.GetNomAtt()+" sel = "+sommel);
				    	            	 		}
				    	             }
				          }
			}//fin parcours attributs du blocs
			}//fin parcours des blocs OR
		
		for(int p=0; p<SommeLigneJoinF.size(); p++) {if(SommeLigneJoinF.elementAt(p)==0) SommeLigneJoinF.setElementAt(1.0, p);
		                                            //System.out.println("attr "+p+" = "+SommeLigneJoinF.elementAt(p));
		                                            }
		
		//Les tables de dimensions
		/*System.out.println();
		System.out.println("**********Les Fragments des Dimensions***********");
		System.out.println();*/
		
		Vector<Double> SelJoinDimFait = new Vector<Double>();
		Vector<Double> selD = new Vector<Double>();		
		
		/** Calcul de la selectivit� de la jointure de chaque frag de dimension candidat avec le frag fait **/
		for(int i=0; i<tablesCandidates.size(); i++){ 
		double s=1; selD.add(1.0); //on initialise selD
		
		for(int j = 0; j < AttributCodage.size(); j++)
		    if(tablesCandidates.elementAt(i).indexAttributs.contains(AttributCodage.elementAt(j).GetNomAtt()))
		        s*=SommeLigneJoinF.elementAt(j);
		for(int j = 0; j < AttrNonCTabC.size(); j++)
			if(tablesCandidates.elementAt(i).indexAttributs.contains(AttrNonCTabC.elementAt(j)))
		        s*=SelAttrNonCTabC.elementAt(j);
		
			     SelJoinDimFait.add(s); //on insere la select join de cette dimension
			     //System.out.println("Sel Join TabC "+tablesCandidates.elementAt(i).getNomTable()+" Fait = "+s);
		                                             }
		
		Vector<Double> VD = new Vector<Double>();
		for(int i=0; i<tablesCandidates.size(); i++)
			if(tabsFH.contains(tablesCandidates.elementAt(i))) VD.add(SelJoinDimFait.elementAt(i));
		SelJoinDimFait.removeAllElements();
		SelJoinDimFait.addAll(VD);
		VD=null;
		
		/** Calcul de la selectivit� de la jointure de chaque frag de dimension non candidat avec le frag fait **/
		Vector<Double> SelJoinTabNonC = new Vector<Double>();
		for(int i=0; i<TableNonC.size(); i++) { 
		double s=1;
		//on recupere la table corresp de l'entrepot
        Table T = cmpt.getTables().elementAt(cmpt.indexTables.indexOf(TableNonC.elementAt(i)));
        
		for(int j = 0; j < AttrNonCTabNonC.size(); j++)
			if(T.indexAttributs.contains(AttrNonCTabNonC.elementAt(j)))
		        s*=SelAttrNonCTabNonC.elementAt(j);
			     SelJoinTabNonC.add(s);
	    //System.out.println("Sel Join TabNonC "+TableNonC.elementAt(i)+" Fait = "+s);
		                                       }
		
        //calcul de la selectivit� de chaque fragment de dimension par rapport � sa table de dimension
		for(int i=0; i<C.size(); i++) 
		{
			double selLigne = 0;
			
			for(int j = 0; j < C.elementAt(i).size(); j++)				
				if(C.elementAt(i).elementAt(j)==1) selLigne+=this.SelDim.elementAt(i).elementAt(j);	
						
			//recherche la table correspendante � l'attribut n� i
			int k=indexTables.indexOf(AttributCodage.elementAt(i).GetTableAtt().toUpperCase()); 
			
			    selD.setElementAt(selD.elementAt(k) * selLigne, k);
		 }	
			
			VD = new Vector<Double>();
			for(int i=0; i<tablesCandidates.size(); i++)
				if(tabsFH.contains(tablesCandidates.elementAt(i))) VD.add(selD.elementAt(i));
			selD.removeAllElements();	selD.addAll(VD);	VD=null;
		
			this.indexTablesFragment�es = new Vector<String>();
			for(int i=0; i<tabsFH.size(); i++) indexTablesFragment�es.add(tabsFH.elementAt(i).getNomTable());
		
		//Affichage de la matrice de selectivit� des attributs de Dimensions
		/*System.out.println("/////////////Selectivit� des attributs de dimensions dans leurs dimensions///////////");
		for(int i=0; i<SelDim.size(); i++) {
			for(int j = 0; j < SelDim.elementAt(i).size(); j++)
				System.out.print(" "+SelDim.elementAt(i).elementAt(j));
			System.out.println();
		}
		//System.out.println("///////////////////////////////////////");
		
		//affichage des valeurs de selectivit�s des tables de dimensions
		System.out.println();
		for(int i=0; i<selD.size(); i++) System.out.print(" selDim "+tablesCandidates.elementAt(i).getNomTable()+" = "+
				                                           selD.elementAt(i));
		System.out.println();*/
		 
		Vector<Long> TdimPS = new Vector<Long>();
		Vector<Long> nbTupFragDim = new Vector<Long>();
		Vector<Double> FS = new Vector<Double>();
		Vector<String> TablesSelect = new Vector<String>();		
		Vector<String> tab = new Vector<String>();
		for(int i=0; i<Q.getTables().size(); i++) tab.add(Q.getTables().elementAt(i).toUpperCase());
		
		/** calcul de la taille des attributs du select pour les tables candidates **/
		Vector<Long> TailleSelectTab = new Vector<Long>();
		
		//System.out.println("les attributs du Select sont :");
		if(Q.Select!=null)
			for(int r=0; r<Q.Select.size(); r++)
				for(int h=0; h<cmpt.getTables().size(); h++)
			    if(cmpt.getTables().elementAt(h).indexAttributs.contains(Q.Select.elementAt(r).toUpperCase())
			       && (indexTablesFragment�es.contains(cmpt.getTables().elementAt(h).getNomTable()) 
			    	   || TableNonC.contains(cmpt.getTables().elementAt(h).getNomTable()) ) )
					  			    	
			    	if(!TablesSelect.contains(cmpt.getTables().elementAt(h).getNomTable().toUpperCase()))
				    { 	
			    		TablesSelect.add(cmpt.getTables().elementAt(h).getNomTable().toUpperCase());			    		
				        int g = cmpt.getTables().elementAt(h).indexAttributs.indexOf(Q.Select.elementAt(r).toUpperCase());
				        TailleSelectTab.add(cmpt.getTables().elementAt(h).getV_Attributs().elementAt(g).GetTailleAtt());
					 }
			    	else{
			    			int ind = TablesSelect.indexOf(cmpt.getTables().elementAt(h).getNomTable().toUpperCase());
			    			int g=cmpt.getTables().elementAt(h).indexAttributs.indexOf(Q.Select.elementAt(r).toUpperCase());
			    			long tailleAtt = cmpt.getTables().elementAt(h).getV_Attributs().elementAt(g).GetTailleAtt();
			   				TailleSelectTab.setElementAt(TailleSelectTab.elementAt(ind)+tailleAtt, ind);			    
				       	}
				/*System.out.println("la taille de "+cmpt.getTables().elementAt(h).getV_Attributs().elementAt(g).GetNomAtt()+
						" = "+cmpt.getTables().elementAt(h).getV_Attributs().elementAt(g).GetTailleAtt());*/
				  
		//System.out.println();
		long nbTupFragD = 0, TailleDimPS;
		Vector<Long> TailleSelect = new Vector<Long>();
		
		/** Calcul des infos des tables de dimensions **/
		for(int i=0; i<tab.size(); i++) 
		if(	indexTablesFragment�es.contains(tab.elementAt(i).toUpperCase()) 
			|| 	TableNonC.contains(tab.elementAt(i).toUpperCase()))
		{   
			//on recupere la table de dimension
			int index = cmpt.indexTables.indexOf(tab.elementAt(i).toUpperCase()); 
			Table Dim = cmpt.getTables().elementAt(index);			
			int id = indexTablesFragment�es.indexOf(Dim.getNomTable().toUpperCase());
			
			//on recupere le nbr de tuples de la table de dimension
			long nbTupDim = Dim.getNb_Tuple();
			//System.out.println("nb tuples de "+Dim.getNomTable()+" initiale = "+ nbTupDim);
			
			//on calcul le nbr de tuples du fragment de la table de dimension
			if(indexTablesFragment�es.contains(Dim.getNomTable().toUpperCase()))         
			{ nbTupFragD = (long) Math.ceil(selD.get(id) * nbTupDim);
			//System.out.println("nb tuples du fragment de "+Dim.getNomTable()+" = "+nbTupFragD);
			}
			
			else nbTupFragD = nbTupDim;	        
			
	        //on calcul la taille du fragment de la table de dimension
			double tailleFragDim = nbTupFragD * Dim.getTailleTuple();
			//System.out.println("taille frag Dimension = "+ tailleFragDim);
			
			//on calcul la taille du fragment de dimension en Pages Ssystems 
			TailleDimPS = (long) Math.ceil(tailleFragDim / AGexemple.PS);
			//System.out.println("taille frag dimension en PS = "+ TailleDimPS);	
		
	//Calcul du facteur de selectivit� de chaque table de dimension
	//on divise le nbr de tuples selectionn�s de la jointure entre la dimension et le fait sur le nb tup Dim * nb Tup Fait
			
			double selDF = 0.0;
			if(indexTablesFragment�es.contains(Dim.getNomTable().toUpperCase())) selDF = SelJoinDimFait.elementAt(id);
			else if(TableNonC.contains(Dim.getNomTable())) 
				    selDF = SelJoinTabNonC.elementAt(TableNonC.indexOf(Dim.getNomTable()));
			
			System.out.println("Dim : "+Dim.getNomTable()+" sel join avc frag fait "+selDF+" indTFrag : "+id);
			if( selDF < 1) {
				nbTupFragDim.add(nbTupFragD); TdimPS.add(TailleDimPS); 
			double fs = (selDF * (double) nbTupFragFait) / (double)(nbTupFragD * nbTupFragFait);
			System.out.println("Facteur de selectivit� du fragment de "+Dim.getNomTable()+" = "+fs);
			FS.add(fs);
			if(TablesSelect.contains(Dim.getNomTable())) 
				     TailleSelect.add(TailleSelectTab.elementAt(TablesSelect.indexOf(Dim.getNomTable())));
			else TailleSelect.add((long)0);
			}
	    	else if(!TablesSelect.contains(Dim.getNomTable().toUpperCase()));
			     //System.out.println("Table "+Dim.getNomTable()+" extraite de la jointure"); //cas res tab pr�calcul�
				
			     else{
				//System.out.println("Contient des attributs dans le Select");
				nbTupFragDim.add(nbTupFragD); TdimPS.add(TailleDimPS);
				double fs = (selDF * (double) nbTupFragFait) / (double)(nbTupFragD * nbTupFragFait);
				//System.out.println("Facteur de selectivit� du fragment de "+Dim.getNomTable()+" = "+fs);
				FS.add(fs);
				TailleSelect.add(TailleSelectTab.elementAt(TablesSelect.indexOf(Dim.getNomTable())));
			          }
			/*System.out.println();
			System.out.println("*****************************");
			System.out.println();*/
		}		
		
		if(FS.size()==0) {/*System.out.println();
		                  System.out.println("Cout de la requete Join=0 sur se Sous Schema = "+ TailleFaitPS);
		                  System.out.println();*/
		                  return(TailleFaitPS);
		                 }
		
		/** Cout de la lere jointure **/		
		
		//on cherche la dimension ayant le facteur de selectivit� minimum
		int iMin = 0; double Min = FS.elementAt(0);
		for(int j=1; j<FS.size(); j++)
			if (FS.elementAt(j) < Min) {Min = FS.get(j); iMin=j;}
		
		//on calcul le cout de la 1ere jointure : taille PS du fait + taille PS de Dmin
		double Cpj = 3 * (TailleFaitPS + TdimPS.elementAt(iMin));
		//System.out.println("Cout de la 1ere jointure = "+ Cpj);
		
		//Cout du resultat intermediaire de la 1ere jointure
		Long PS = fait.getTailePS(); long B = 60;
		
		Long nbTupRES = (long) Math.ceil(Min * nbTupFragFait * nbTupFragDim.elementAt(iMin));
		Long TailleSel = fait.getTailleTuple() + TailleSelect.elementAt(iMin);
		Long TailleResPS = nbTupRES * (TailleSel) / PS; 
		
		FS.removeElementAt(iMin);
		TdimPS.removeElementAt(iMin);
		nbTupFragDim.removeElementAt(iMin);
		TailleSelect.removeElementAt(iMin);
		
		//calcul du co�t du resultat intermediaire
		double Cri = 0;
		while( FS.size() > 0 ) 
		{			
			//on cherche la dimension ayant le FS minimum
			iMin = 0; Min = FS.elementAt(0);
			for(int j=1; j<FS.size(); j++) 
	        if (FS.elementAt(j) < Min) {Min = FS.get(j); iMin = j;}
			
			Cri += ( 2 * TDisp(B, TailleResPS) * (TailleResPS - (B+1)) ) + 3 * TdimPS.elementAt(iMin);
			nbTupRES = (long) Math.ceil(Min * nbTupRES * nbTupFragDim.elementAt(iMin));
			TailleSel+=TailleSelect.elementAt(iMin);
			TailleResPS = nbTupRES * (TailleSel) / PS;
			
			FS.removeElementAt(iMin);
			TdimPS.removeElementAt(iMin);
			nbTupFragDim.removeElementAt(iMin);
			TailleSelect.removeElementAt(iMin);
		}
		//System.out.println("Cout du Resultat intermediare = "+Cri);
		
		//Calcul du cout des groupements et des agregations
		double Cga = 0;
		if(Q.AG && Q.Group) {//System.out.println("existe AG et Group");
		                     Cga = 4 * TDisp(B, TailleResPS) * (TailleResPS - (B+1)) ;
		                     }
		else if(Q.AG || Q.Group) {//System.out.println("existe AG ou Group");
		                          Cga = 2 * TDisp(B, TailleResPS) * (TailleResPS - (B+1)) ;
		                          }
		//System.out.println("Cout du groupement et des agregations = "+ Cga);
		
		double cost = Cpj + Cri+ Cga;
		/*System.out.println();
		System.out.println("Cout de la requete sur se Sous Schema = "+ cost);
		System.out.println();*/
		
		return (cost);
}
	
	private double CoutIndex(Vector<Vector<Integer>> C, Requete Q) {
		
		int RowID = this.RowID*8;//22*8; //en bits
		long cardAtt=0; double TailleIJB=0;
		boolean scenario1=false, scenario2=false;
		
		/** on verifie si la requete utilise l'index **/
		HashSet<Integer> HistIndex = new HashSet<Integer>();
		int g;
		for(g=0; g<Q.getBlocsOR().size(); g++)
		{
			Vector<Attribut> Bloc = Q.getBlocsOR().elementAt(g);
			int j;
			for(j=0; j< Bloc.size(); j++)
			{
				Attribut att = Bloc.elementAt(j);
				int indAtt = indexAttributs.indexOf(att.GetNomAtt().toUpperCase());
				/*System.out.println("IJB Attr a verifi�: "+att.GetNomAtt()+" "+att.operateurs.firstElement()
									+att.valeurs.firstElement());*/
				
				if(indAtt != -1) {
					               CompositeGene comp = (CompositeGene) this.codage.getGene(indAtt);
					               int numIndex=((Integer)((IntegerGene)comp.geneAt(nbrSsDomParAttr.elementAt(indAtt)))
					            		         .getAllele()).intValue();
					               if(numIndex>0) {HistIndex.add(numIndex); scenario1=true;}
					               else scenario2=true;
				                 }
				else scenario2=true;
			}
		}		
		
		/** si on utilise l'index alors on calcul son cout **/
		if(scenario1)
	{
			                              /* if(!scenario2) System.out.println("scenario 1");
			                               else System.out.println("scenario 2");*/
		/** calcul cardinalit� du fait **/
		long CardFragFait=0;

		double selF = selFragFait(C);	//System.out.println("SelF = "+selF);
		
		CardFragFait = (long) Math.ceil(selF * this.compte.getTableFaits().getNb_Tuple());
		
		/** calcul taille des IJBs **/
		/** calcul de la cardinalit� des attributs de chaque index **/
		Iterator I = HistIndex.iterator();
		while(I.hasNext()) 
		{    int numI = (Integer) I.next(); cardAtt=0;
		     for(int k=0; k<this.codage.size(); k++)
		     {
              CompositeGene comp = (CompositeGene) this.codage.getGene(k);
              int numIndex=((Integer)((IntegerGene)comp.geneAt(nbrSsDomParAttr.elementAt(k))).getAllele()).intValue();
              if(numIndex==numI) cardAtt += this.AttributCodage.elementAt(k).getCardinalite();
              /*System.out.println("Att = "+AttributCodage.elementAt(k).GetNomAtt()
            		             +" card = "+AttributCodage.elementAt(k).getCardinalite());*/
             }		
		//System.out.println("card att IJB "+numI+" : "+cardAtt);
		TailleIJB += ((RowID + cardAtt) * CardFragFait) / 8; //en octects
		}
		
		/** calcul du co�t de chargement de l'index **/
		long CC = (long) Math.ceil(TailleIJB / AGexemple.PS);
		if(CC==0) CC = 1;
		/** calcul du nombre de tuple selectionn�s **/
		long Nt = (long) Math.ceil(SelTupSelectFait_IJB(Q, selF, C) * CardFragFait);
		
		/**calcul du cout d'acc�s au n-uplets **/
		long TailleFragFait = (long) Math.ceil(CardFragFait * this.compte.getTableFaits().getTailleTuple() / AGexemple.PS);
		//System.out.println(" Card Fait = "+CardFragFait+" TailleFait : "+TailleFragFait+" Nt : "+Nt);
		
		long CA = Nt;
		
		double CJ = 0.0;
		if(scenario2) CJ = coutSCH_IJB(C, Q, Nt, selF);     	    
		
		/*System.out.println();*/
	    //System.out.println(" CJ = "+CJ+" TailleIJB = "+TailleIJB+" CC = "+CC+" CA = "+CA);
		return (CJ+CA+CC);		               
	}		
		else { //System.out.println("scenario3"); 
			    double CJ = CoutSsSchema(C, Q); 
		       //System.out.println("Cout requete = "+CJ);
               return (CJ);
		      }
}
	
	private double SelTupSelectFait_IJB (Requete Q ,double selF, Vector<Vector<Integer>> C) {
		
		Vector<Vector<Double>> selFragFait = new Vector<Vector<Double>>();
		Vector<Double> SommeLigneJoinF = new Vector<Double>();
		
		/** construction de la matrice de selectivit� des ss dom du frag fait **/
		for(int i=0; i<SelFait.size(); i++) 
		{
			Vector<Double> V = new Vector<Double>();
			for(int j = 0; j < SelFait.elementAt(i).size(); j++){
				
			   if(C.elementAt(i).elementAt(j)==0) V.add(0.0);
			   else {
				     double sel = SelFait.elementAt(i).elementAt(j);
				     for(int c=0; c<SelFait.size(); c++)
				    	 if(c!=i) {
				    		       double sumLigne=0;
				    		       for(int k=0; k<SelFait.elementAt(c).size(); k++)
				    		    	   if(C.elementAt(c).elementAt(k)==1) sumLigne+=SelFait.elementAt(c).elementAt(k);
				    		       sel*=sumLigne;
				    	          }
				         V.add(sel / selF);
			        }
			}
			selFragFait.add(V);
			SommeLigneJoinF.add(0.0);
		}
		
		/** calcul de la selectivit� de chaque attribut index� des blocs valides de la requete dans le fait **/
		Vector<String> V = new Vector<String>();
		
		for(int o=0; o<Q.getBlocsOR().size(); o++)
		{	
		 Vector<Attribut> Bloc = Q.getBlocsOR().elementAt(o);
			
		 /** parcours des attributs du bloc **/
			for(int u=0; u<Bloc.size(); u++) 
			{   double sommel = 0;
				Attribut a = Bloc.elementAt(u);
				/*System.out.println("Selectiv Att : "+a.GetNomAtt()+" "+a.operateurs.firstElement()
						           +" "+a.valeurs.firstElement());*/
                int indAtt = indexAttributs.indexOf(a.GetNomAtt().toUpperCase()); int numIndex=0;
                
				if(indAtt != -1) {
					               CompositeGene comp = (CompositeGene) this.codage.getGene(indAtt);
					               numIndex =((Integer)((IntegerGene)comp.geneAt(nbrSsDomParAttr.elementAt(indAtt)))
					            		      .getAllele()).intValue();        	   					                               
				                 }
				       //si c un attribut index� alors on calcul sa selectivit�
				     if(a.SDvals.size()>0 
				    		 && numIndex>0)
					   for(int i=0; i<a.SDvals.size(); i++) //parcours des SD valides du predicat
					   {   
						   int isd = a.SDvals.elementAt(i);
						   if(C.elementAt(a.L).elementAt(isd) == 1 && !V.contains(a.L+" "+isd)) 
						       {
							    sommel = selFragFait.elementAt(a.L).elementAt(isd);	
								SommeLigneJoinF.setElementAt(SommeLigneJoinF.elementAt(a.L)+sommel, a.L);
								String sd = a.L+" "+isd; 
								V.add(sd);
						       }
					   }				     
			}			
		}
		
		for(int p=0; p<SommeLigneJoinF.size(); p++) if(SommeLigneJoinF.elementAt(p)==0) SommeLigneJoinF.setElementAt(1.0, p);
		
		/** calcul de la selectivit� des attributs index�s de la requete dans le fait **/ 
		double selTupSel = 1;
		for(int p=0; p<SommeLigneJoinF.size(); p++) selTupSel*=SommeLigneJoinF.elementAt(p);
		
		return selTupSel;
	}
	
	private double coutSCH_IJB(Vector<Vector<Integer>> C, Requete Q , Long Nt, double selF) {
		
		Compte cmpt = this.compte;		
		Vector<Double> SommeLigneJoinF = new Vector<Double>();
		Vector<Vector<Double>> selFragFait = new Vector<Vector<Double>>();
		
		/*System.out.println();
		System.out.println("**********Le Fragment Fait***********");
		System.out.println();*/
		
		/** construction de la matrice de selectivit� des ss dom du frag fait **/
		for(int i=0; i<SelFait.size(); i++) 
		{
			Vector<Double> V = new Vector<Double>();
			for(int j = 0; j < SelFait.elementAt(i).size(); j++)
			{				
			   if(C.elementAt(i).elementAt(j)==0) V.add(0.0);
			   else {
				     double sel = SelFait.elementAt(i).elementAt(j);
				     for(int c=0; c<SelFait.size(); c++)
				    	 if(c!=i) {
				    		       double sumLigne=0;
				    		       for(int k=0; k<SelFait.elementAt(c).size(); k++)
				    		    	   if(C.elementAt(c).elementAt(k)==1) sumLigne+=SelFait.elementAt(c).elementAt(k);
				    		       sel*=sumLigne;
				    	          }
				         V.add(sel / selF);
			        }
			}
			selFragFait.add(V);
			SommeLigneJoinF.add(0.0);
		}
		
		/*System.out.println("************Selectivit� du Frag Fait************");
		for(int i=0; i<selFragFait.size(); i++) {
			for(int j = 0; j < selFragFait.elementAt(i).size(); j++)
				System.out.print(" "+selFragFait.elementAt(i).elementAt(j));
			    System.out.println();
		                                     }
		System.out.println("**********************************");*/
		
		//on recupere la table de fait
		Table fait = cmpt.getTableFaits();
		
		//on recupere le nbr de tuples de la table de fait
		long nbTupFait = fait.getNb_Tuple();
		
		//on calcul le nbr de tuples du fragment fait du sous schema
		long nbTupFragFait = (long) Math.ceil(selF * nbTupFait);
		
		//on calcul la taille du fragment fait
		double tailleTuples = nbTupFragFait * fait.getTailleTuple();
		
		//on calcul la taille du fragment fait en Pages Systems 
		long TailleFaitPS = (long) Math.ceil(tailleTuples / AGexemple.PS);
		
		Vector<Boolean> TabAjoindre = new Vector<Boolean>(this.tablesCandidates.size());
		
		/** Calcul du nombre d'attributs de fragmentation pr chaque table **/
		Vector<Integer> nbrAttrParTable = new Vector<Integer>();
		for(int i=0; i<tablesCandidates.size(); i++){ 
		int nb = 0; TabAjoindre.add(false);		
		for(int j = 0; j < AttributCodage.size(); j++)
			if(tablesCandidates.elementAt(i).getNomTable().equals(AttributCodage.elementAt(j).GetTableAtt().toUpperCase()))
		        nb++; 
			    nbrAttrParTable.add(nb);
		                                             }		
		
		/** calcul de la selectivit� de chaque attribut des blocs valides de la requete dans le fait **/
		Vector<String> V = new Vector<String>();
		Vector<String> TableNonC = new Vector<String>();
		Vector<Double> SelAttrNonCTabNonC = new Vector<Double>();
		Vector<String> AttrNonCTabNonC = new Vector<String>();
		Vector<Double> SelAttrNonCTabC = new Vector<Double>(tablesCandidates.size());
		Vector<String> AttrNonCTabC = new Vector<String>();
		Vector<Vector<String>> HistAttrStrNonCTabC = new Vector<Vector<String>>(); 
		Vector<Vector<String>> HistAttrStrNonCTabNonC = new Vector<Vector<String>>();
		Vector<Vector<Object>> HistPredAttFrag = new Vector<Vector<Object>>(this.AttributCodage.size()); 
		for(int i =0; i<AttributCodage.size(); i++) HistPredAttFrag.add(null);
		Vector<Object> HistPredAttNonFragTabNonC = new Vector<Object>();
		Vector<Object> HistPredAttNonFragTabC = new Vector<Object>();
		Vector<String> indexAttrTablesDeFrag = new Vector<String>();
		for(int i=0; i<AttrDeTablesDeFrag.size(); i++) 
			indexAttrTablesDeFrag.add(AttrDeTablesDeFrag.elementAt(i).GetNomAtt());
		
		//parcours des blocs OR 
		for(int o=0; o<Q.getBlocsOR().size(); o++)
		{
			//System.out.println("bloc : "+o);
			Vector<Attribut> Bloc = Q.getBlocsOR().elementAt(o);
			
		 /** parcours des attributs du bloc **/
			for(int u=0; u<Bloc.size(); u++) 
			{   
				double sommel = 0;
				Attribut a = Bloc.elementAt(u);
                int indAtt = indexAttributs.indexOf(a.GetNomAtt().toUpperCase()); 
                int numIndex=0;
                
				if(indAtt != -1) { //si attr candidat alors voir s'il est ind�x�
					               CompositeGene comp = (CompositeGene) this.codage.getGene(indAtt);
					               numIndex=((Integer)((IntegerGene)comp.geneAt(nbrSsDomParAttr.elementAt(indAtt)))
					            		     .getAllele()).intValue();        	   					                               
				                 }
				     //verif si c un attribut candidat			       
				     if(indAtt!= -1)
				     {
				    	 //enregistr� la table dont l'attribut n'est pas ind�x�
				    	 if(numIndex==0)
				    		 TabAjoindre.setElementAt(true, indexTables.indexOf(a.GetTableAtt().toUpperCase()));
				    	 
				    	 if(a.type.equals("string")) 
				    		 for(int i=0; i<a.SDvals.size(); i++) //parcours des ss Doms valides du predicat
				    		 	{   
				    			 	int isd = a.SDvals.elementAt(i);
				    			 	if(C.elementAt(a.L).elementAt(isd) == 1 && !V.contains(a.L+" "+isd) 
								       && (isd < selFragFait.elementAt(a.L).size()))//pr le cas d reste sil n'esxiste pas 
				    			 	{
				    				 sommel = selFragFait.elementAt(a.L).elementAt(isd);	
				    				 SommeLigneJoinF.setElementAt(SommeLigneJoinF.elementAt(a.L)+sommel, a.L);
							    
				    				 /*System.out.println("att de frag : "+a.GetNomAtt()+" : ");
				    				 System.out.println(" sommel = "+sommel+" L:"+a.L+" SD:"+isd);
				    				 System.out.println("SommeLF = "+SommeLigneJoinF.elementAt(a.L)); */
				    				 String sd = a.L+" "+isd; 
				    				 V.add(sd);
				    			 	}
				    		 	}
				    	 else //cas type = reel ou entier et attr de Fragmentation
				    	 {   Vector<Object> Iatt = ConstrSDpred(a);
				    	 	 Vector<Object>	Hist = VectHistPred(HistPredAttFrag.elementAt(a.L), Iatt, a.type);
				    		 sommel = (double)nbTupVectHistPred(C, a, Hist)/(double)nbTupFragFait;	
		    				 SommeLigneJoinF.setElementAt(sommel, a.L);
		    				 
		    				 HistPredAttFrag.setElementAt((Vector<Object>) Hist.clone(), a.L);
				    	 }
				       }
				     /**attribut non de frag: calculer sa selectivit� dans le frag fait **/
				     else {	 				    	   
				    //attribut non de frag ayant table de frag
				    if(indexTables.contains(a.GetTableAtt().toUpperCase()))//verif si table utilis� dans le codage
				    	//verif si c une table fragment�e
				       if(tabsFH.contains(tablesCandidates.elementAt(indexTables.indexOf(a.GetTableAtt().toUpperCase()))))
				    	     { 
				    		   if(AttrNonCTabC.contains(a.GetNomAtt()))
				    			   if(a.type.equals("string"))
				    			   		{   
				    				        indAtt = AttrNonCTabC.indexOf(a.GetNomAtt());
				    				        double sel=(double)nbTupAttrStr(C,(Vector<String>)HistPredAttNonFragTabC.elementAt(indAtt),a)/nbTupFragFait;
				    				    	double d = SelAttrNonCTabC.elementAt(indAtt);
				    				   	 	SelAttrNonCTabC.setElementAt(sel+d, indAtt);
				    				   	 	
				    				   	 	if(SelAttrNonCTabC.elementAt(indAtt)>1)
				    				   	 	SelAttrNonCTabC.setElementAt(1.0, indAtt);
				    				   	 		
				    				   	 	System.out.println(" selAtt = "+sel);				    				   			     
				    		                System.out.println("TabC att : "+a.GetNomAtt());
				    		            }
				    		   
				    			   else { //type = reel ou entier attr de Frag
				    				     indAtt = AttrNonCTabC.indexOf(a.GetNomAtt());
				    				   	 Vector<Object> Iatt = ConstrSDpred(a);
							    	 	 Vector<Object>	Hist = VectHistPred((Vector<Object>)HistPredAttNonFragTabC.elementAt(indAtt), Iatt, a.type);
							    		 sommel = (double)nbTupVectHistPred(C, a, Hist)/(double)nbTupFragFait;	
							    		 SelAttrNonCTabC.setElementAt(sommel, indAtt);
							    		 HistPredAttNonFragTabC.setElementAt((Vector<Object>) Hist.clone(), indAtt);
	    				   				 System.out.println(" selAtt = "+sommel);
				    			        }
				    		   //cas nouv attribut
				    		   else if(a.type.equals("string")) 
				    		          { 
				    			   		Vector<String> H = new Vector<String>();
				    			   		double sel = (double)nbTupAttrStr(C, H, a)/nbTupFragFait;
				    			        AttrNonCTabC.add(a.GetNomAtt()); 
				    			        SelAttrNonCTabC.add(sel);
				    			        HistPredAttNonFragTabC.add(H);
				    			        System.out.println("TabC nouv att nonFH str : "+a.GetNomAtt()+" selAtt = "+sel);
				    			      }
				    		        
				    		        else { //cas type = "entier" ou "reel"
				    		        		Vector<Object> Iatt = ConstrSDpred(a);
				    		        		Vector<Object>	Hist = Iatt;
				    		        		sommel = (double)nbTupVectHistPred(C, a, Hist)/(double)nbTupFragFait;
				    		        		AttrNonCTabC.add(a.GetNomAtt());
				    		        		SelAttrNonCTabC.add(sommel);
				    		        		HistPredAttNonFragTabC.add((Vector<Object>) Hist.clone());
				    		        		System.out.println("TabC nouv att nonFH num : "+a.GetNomAtt()+" selAtt = "+sommel);
				    		             }			    		   				    		   				    		                                             
				    	     }
				    	   
                           // Tables et attrs non de fragmentation				    	   
				    	   else  if(!TableNonC.contains(a.GetTableAtt().toUpperCase()))
				    		   if(a.type.equals("string"))
				    	           { 
				    			   	 TableNonC.add(a.GetTableAtt().toUpperCase());
				    	           	 AttrNonCTabNonC.add(a.GetNomAtt());
				    	           	 Vector<String> H = new Vector<String>();
				    	           	 double sel = (double) nbTupAttrStr(C, H, a)/nbTupFragFait;
				    	             SelAttrNonCTabNonC.add(sel);
				    	             HistPredAttNonFragTabNonC.add(H);
				    	             System.out.println("TabNonC Nouv att : "+a.GetNomAtt()+" selAtt = "+sel);
				    	           }
				    		   else { //type = reel ou entier
		    		        			Vector<Object> Iatt = ConstrSDpred(a);
		    		        			Vector<Object>	Hist = Iatt;
		    		        			sommel = (double)nbTupVectHistPred(C, a, Hist)/(double)nbTupFragFait;
		    		        			TableNonC.add(a.GetTableAtt().toUpperCase());
					    	            AttrNonCTabNonC.add(a.GetNomAtt());
		    		        			SelAttrNonCTabNonC.add(sommel);
		    		        			HistPredAttNonFragTabNonC.add((Vector<Object>) Hist.clone());
		    		        			System.out.println("TabNonC nouv att : "+a.GetNomAtt()+" selAtt = "+sommel);
		    		             	}  
				    
				    			//table de l'attribut existente
				    	         else if(a.type.equals("string"))
				    	         {
				    	        	    System.out.println("nom attribut nonFH "+a.GetNomAtt());
				    	        	  	indAtt = AttrNonCTabNonC.indexOf(a.GetNomAtt());
				    	        	  	if(indAtt==-1) 
				    	        	  		{	
				    	        	  		 AttrNonCTabNonC.add(a.GetNomAtt());
						    	           	 Vector<String> H = new Vector<String>();
						    	           	 double sel = (double) nbTupAttrStr(C, H, a)/nbTupFragFait;
						    	             SelAttrNonCTabNonC.add(sel);
						    	             HistPredAttNonFragTabNonC.add(H);
						    	             System.out.println("TabNonC Nouv att : "+a.GetNomAtt()+" selAtt = "+sel);
				    	        	  		}
				    	        	  	
				    	        	  	else{
				    	 double sel=(double)nbTupAttrStr(C,(Vector<String>) HistPredAttNonFragTabNonC.elementAt(indAtt), a)/nbTupFragFait;
			    		 double d = SelAttrNonCTabNonC.elementAt(indAtt);
			    				   	 	SelAttrNonCTabNonC.setElementAt(sel+d, indAtt);
			    				   	 	
			    				   	 	if(SelAttrNonCTabNonC.elementAt(indAtt)>1)
			    				   	 		SelAttrNonCTabNonC.setElementAt(1.0, indAtt);
				    	               System.out.println("TabNonC att nonFH : "+a.GetNomAtt()+" sel = "+sel+" selAtt = "+(d+sel));
				    	        	  		}
				    	         }
				    	             else //cas type = reel ou entier
				    	             {
				    	            	 indAtt = AttrNonCTabNonC.indexOf(a.GetNomAtt());
				    	            	 if(indAtt==-1) 
				    	        	  		{
				    	            		 	Vector<Object> Iatt = ConstrSDpred(a);
				    		        			Vector<Object>	Hist = Iatt;
				    		        			sommel = (double)nbTupVectHistPred(C, a, Hist)/(double)nbTupFragFait;
							    	            AttrNonCTabNonC.add(a.GetNomAtt());
				    		        			SelAttrNonCTabNonC.add(sommel);
				    		        			HistPredAttNonFragTabNonC.add((Vector<Object>) Hist.clone());
				    		        			System.out.println("TabNonC nouv att : "+a.GetNomAtt()+" selAtt = "+sommel);
				    	        	  		}				    	            	 
				    	            	 else 	{
				    	            	 Vector<Object> Iatt = ConstrSDpred(a);
			    		        		 Vector<Object>	Hist = VectHistPred((Vector<Object>) HistPredAttNonFragTabNonC.elementAt(indAtt), Iatt, a.type);;
			    		        		 sommel = (double)nbTupVectHistPred(C, a, Hist)/(double)nbTupFragFait;
			    		        		 SelAttrNonCTabNonC.setElementAt(sommel, indAtt);
			    		        		 HistPredAttNonFragTabNonC.setElementAt((Vector<Object>) Hist.clone(), indAtt);
			    		        		 System.out.println("TabNonC att nonFH : "+a.GetNomAtt()+" sel = "+sommel);
				    	            	 		}
				    	             }
				          }
			}//fin parcours attributs du blocs
			}//fin parcours des blocs OR
		
		for(int p=0; p<SommeLigneJoinF.size(); p++) {if(SommeLigneJoinF.elementAt(p)==0) SommeLigneJoinF.setElementAt(1.0, p);
		                                            }
		
		//Les tables de dimensions
		/*System.out.println();
		System.out.println("**********Les Fragments des Dimensions***********");
		System.out.println();*/
		
		Vector<Double> SelJoinDimFait = new Vector<Double>();
		Vector<Double> selD = new Vector<Double>();		
		
		/** Calcul de la selectivit� de la jointure de chaque frag de dimension candidat avec le frag fait **/
		for(int i=0; i<tablesCandidates.size(); i++){ 
		double s=1; selD.add(1.0); //on initialise selD
		
		for(int j = 0; j < AttributCodage.size(); j++)
		    if(tablesCandidates.elementAt(i).indexAttributs.contains(AttributCodage.elementAt(j).GetNomAtt()))
		        s*=SommeLigneJoinF.elementAt(j);
		for(int j = 0; j < AttrNonCTabC.size(); j++)
			if(tablesCandidates.elementAt(i).indexAttributs.contains(AttrNonCTabC.elementAt(j)))
		        s*=SelAttrNonCTabC.elementAt(j);
		
			     SelJoinDimFait.add(s); //on insere la select join de cette dimension
			     //System.out.println("Sel Join TabC "+tablesCandidates.elementAt(i).getNomTable()+" Fait = "+s);
		                                             }
		
		/** Calcul de la selectivit� de la jointure de chaque frag de dimension non candidat avec le frag fait **/
		Vector<Double> SelJoinTabNonC = new Vector<Double>();
		for(int i=0; i<TableNonC.size(); i++) { 
		double s=1;
		//on recupere la table corresp de l'entrepot
        Table T = cmpt.getTables().elementAt(cmpt.indexTables.indexOf(TableNonC.elementAt(i)));
		for(int j = 0; j < AttrNonCTabNonC.size(); j++)
			if(T.indexAttributs.contains(AttrNonCTabNonC.elementAt(j)))
		        s*=SelAttrNonCTabNonC.elementAt(j);
			     SelJoinTabNonC.add(s);
	    //System.out.println("Sel Join TabNonC "+TableNonC.elementAt(i)+" Fait = "+s);
		                                       }
		
        /** calcul de la selectivit� de chaque fragment de dimension par rapport � sa table de dimension **/
		for(int i=0; i<C.size(); i++) {
			double selLigne = 0;
			
			for(int j = 0; j < C.elementAt(i).size(); j++) {				
		 if(C.elementAt(i).elementAt(j)==1) selLigne+=this.SelDim.elementAt(i).elementAt(j);	
			}
			
			//recherche la table correspendante � l'attribut n� i
			int k=indexTables.indexOf(AttributCodage.elementAt(i).GetTableAtt().toUpperCase()); 
	 
			    selD.setElementAt(selD.elementAt(k) * selLigne, k);
		}
		
		//Affichage de la matrice de selectivit� des attributs de Dimensions
		/*System.out.println("/////////////Selectivit� des attributs de dimensions dans leurs dimensions///////////");
		for(int i=0; i<SelDim.size(); i++) {
			for(int j = 0; j < SelDim.elementAt(i).size(); j++)
				System.out.print(" "+SelDim.elementAt(i).elementAt(j));
			System.out.println();
		}
		//System.out.println("///////////////////////////////////////");
		
		//affichage des valeurs de selectivit�s des tables de dimensions
		System.out.println();
		for(int i=0; i<selD.size(); i++) System.out.print(" selDim "+tablesCandidates.elementAt(i).getNomTable()+" = "+
				                                           selD.elementAt(i));
		System.out.println();*/
		 
		Vector<Long> TdimPS = new Vector<Long>();
		Vector<Long> nbTupFragDim = new Vector<Long>();
		Vector<Double> FS = new Vector<Double>();
		Vector<String> TablesSelect = new Vector<String>();		
		Vector<String> tab = new Vector<String>();
		for(int i=0; i<Q.getTables().size(); i++) tab.add(Q.getTables().elementAt(i).toUpperCase());
		
		/** on enleve les tables candidates qui ne vont pas etres jointes **/
		Vector<String> TabCandAjoindre = (Vector<String>) indexTables.clone();
		for(int i=0; i<TabAjoindre.size(); i++)
		  if(TabAjoindre.elementAt(i).equals(false))
		  {	
			//System.out.println("tab "+TabCandAjoindre.elementAt(i)+" suppr");
		    TabCandAjoindre.removeElementAt(i); TabAjoindre.removeElementAt(i); i--;
		  }
		
		/** calcul de la taille des attributs du select pour les tables candidates **/
		Vector<Long> TailleSelectTab = new Vector<Long>();
		
		//System.out.println("les attributs du Select sont :");
		if(Q.Select!=null)			
			for(int r=0; r<Q.Select.size(); r++)
				for(int h=0; h<Q.getTables().size(); h++)
				{
				Table table = cmpt.getTables().elementAt(cmpt.indexTables.indexOf(Q.getTables().elementAt(h).toUpperCase()));
			    if(	table.indexAttributs.contains(Q.Select.elementAt(r).toUpperCase())
			    		&&	!cmpt.getTableFaits().equals(table)
			    		&& (TabCandAjoindre.contains(table) || TableNonC.contains(table) ) )
					  			    	
			    	if(!TablesSelect.contains(cmpt.getTables().elementAt(h).getNomTable().toUpperCase()))
				    { 	
			    		TablesSelect.add(cmpt.getTables().elementAt(h).getNomTable().toUpperCase());			    		
				        int g = cmpt.getTables().elementAt(h).indexAttributs.indexOf(Q.Select.elementAt(r).toUpperCase());
				        TailleSelectTab.add(cmpt.getTables().elementAt(h).getV_Attributs().elementAt(g).GetTailleAtt());
				        break;
					 }
			    	else{
			    			int ind = TablesSelect.indexOf(cmpt.getTables().elementAt(h).getNomTable().toUpperCase());
			    			int g=cmpt.getTables().elementAt(h).indexAttributs.indexOf(Q.Select.elementAt(r).toUpperCase());
			    			long tailleAtt = cmpt.getTables().elementAt(h).getV_Attributs().elementAt(g).GetTailleAtt();
			   				TailleSelectTab.setElementAt(TailleSelectTab.elementAt(ind)+tailleAtt, ind);	
			   				break;
				       	}
				}
					/*System.out.println("la taille de "+cmpt.getTables().elementAt(h).getV_Attributs().elementAt(g).GetNomAtt()+
							" = "+cmpt.getTables().elementAt(h).getV_Attributs().elementAt(g).GetTailleAtt());*/
					  
		//System.out.println();
		
		long nbTupFragD = 0, TailleDimPS;
		Vector<Long> TailleSelect = new Vector<Long>();
		
		//Calcul des infos des tables de dimensions
		for(int i=0; i<tab.size(); i++) 
		if(TabCandAjoindre.contains(tab.elementAt(i).toUpperCase()) || TableNonC.contains(tab.elementAt(i).toUpperCase()))
		{   
			//on recupere la table de dimension
			int index = cmpt.indexTables.indexOf(tab.elementAt(i).toUpperCase()); 
			Table Dim = cmpt.getTables().elementAt(index);			
			int id = indexTables.indexOf(Dim.getNomTable().toUpperCase());
			
			//on recupere le nbr de tuples de la table de dimension
			long nbTupDim = Dim.getNb_Tuple();
			//System.out.println("nb tuples de "+Dim.getNomTable()+" initiale = "+ nbTupDim);
			
			//on calcul le nbr de tuples du fragment de la table de dimension
			if(indexTables.contains(Dim.getNomTable().toUpperCase()))         
			{ nbTupFragD = (long) Math.ceil(selD.get(id) * nbTupDim);
			//System.out.println("nb tuples du fragment de "+Dim.getNomTable()+" = "+nbTupFragD);
			}
			
			else nbTupFragD = nbTupDim;	        
			
	        //on calcul la taille du fragment de la table de dimension
			double tailleFragDim = nbTupFragD * Dim.getTailleTuple();
			//System.out.println("taille frag Dimension = "+ tailleFragDim);
			
			//on calcul la taille du fragment de dimension en Pages Ssystems 
			TailleDimPS = (long) Math.ceil(tailleFragDim / AGexemple.PS);
			//System.out.println("taille frag dimension en PS = "+ TailleDimPS);	
		
			//Calcul du facteur de selectivit� de chaque table de dimension
	//on divise le nbr de tuples selectionn�s de la jointure entre la dimension et le fait sur le nb tup Dim * nb Tup Fait
			double selDF = 0.0;
			if(TabCandAjoindre.contains(Dim.getNomTable().toUpperCase())) selDF = SelJoinDimFait.elementAt(id);
			else if(TableNonC.contains(Dim.getNomTable().toUpperCase())) 
				    selDF = SelJoinTabNonC.elementAt(TableNonC.indexOf(Dim.getNomTable().toUpperCase()));
			     else selDF = 1; //cas table contient des attrs ds le select c'est tt
			//System.out.println("sel join avc frag fait "+selDF);
			if( selDF < 1) 	{
				nbTupFragDim.add(nbTupFragD); TdimPS.add(TailleDimPS); 
			double fs = (selDF * (double) nbTupFragFait) / (double)(nbTupFragD * nbTupFragFait);
			//System.out.println("Facteur de selectivit� du fragment de "+Dim.getNomTable()+" = "+fs);
			FS.add(fs);
			if(TablesSelect.contains(Dim.getNomTable())) 
			     TailleSelect.add(TailleSelectTab.elementAt(TablesSelect.indexOf(Dim.getNomTable())));
		    else TailleSelect.add((long)0);
							}
	    	else if(!TablesSelect.contains(Dim.getNomTable().toUpperCase()))
			     //System.out.println("Table "+Dim.getNomTable()+" extraite de la jointure")
				;
			     else{
				//System.out.println("Contient des attributs dans le Select");
				nbTupFragDim.add(nbTupFragD); TdimPS.add(TailleDimPS);
				double fs = (selDF * (double) nbTupFragFait) / (double)(nbTupFragD * nbTupFragFait);
				//System.out.println("Facteur de selectivit� du fragment de "+Dim.getNomTable()+" = "+fs);
				FS.add(fs);
				TailleSelect.add(TailleSelectTab.elementAt(TablesSelect.indexOf(Dim.getNomTable())));
			          }
			/*System.out.println();
			System.out.println("*****************************");
			System.out.println();*/
		}		
		
		if(FS.size()==0) {/*System.out.println();
        				  System.out.println("Cout de selection de la requete Join=0 sur se Sous Schema = "+ 0);
        				  System.out.println();*/
        				  return(0);
						 } //si ya pa de tables a joindres
		
		int iMin = 0; double Min = FS.elementAt(0);
		
		/** Cout de la lere jointure *	 	
		
		//on cherche la dimension ayant le facteur de selectivit� minimum		
		for(int j=1; j<FS.size(); j++)
			if (FS.elementAt(j) < Min) {Min = FS.get(j); iMin=j;}
		
		on calcul le cout de la 1ere jointure : taille PS du fait + taille PS de Dmin
		double Cpj = 3 * (TailleFaitPS + TdimPS.elementAt(iMin));
		//System.out.println("Cout de la 1ere jointure = "+ Cpj); */
		
		//Resultat intermediaire des n-uplets selectionn�s du frag fait
		Long PS = AGexemple.PS; long B = AGexemple.B;
		
		Long nbTupRES = Nt;
		Long TailleSel = fait.getTailleTuple();
		Long TailleResPS = nbTupRES * TailleSel / PS; 
		
		/*FS.removeElementAt(iMin);
		TdimPS.removeElementAt(iMin);
		nbTupFragDim.removeElementAt(iMin);*/
		
		//calcul du co�t du resultat intermediaire
		double Cri = 0;
		while( FS.size() > 0 ) 
		{			
			//on cherche la dimension ayant le FS minimum
			iMin = 0; Min = FS.elementAt(0);
			for(int j=1; j<FS.size(); j++) 
	        if (FS.elementAt(j) < Min) {Min = FS.get(j); iMin = j;}
			
			Cri += ( 2 * TDisp(B, TailleResPS) * (TailleResPS - (B+1)) ) + 3 * TdimPS.elementAt(iMin);
			nbTupRES = (long) Math.ceil(Min * nbTupRES * nbTupFragDim.elementAt(iMin));
			TailleSel += TailleSelect.elementAt(iMin);
			TailleResPS = nbTupRES * (TailleSel) / PS;
			
			FS.removeElementAt(iMin);
			TdimPS.removeElementAt(iMin);
			nbTupFragDim.removeElementAt(iMin);
			TailleSelect.removeElementAt(iMin);
		}
		//System.out.println("Cout du Resultat intermediare = "+Cri);
		
		/** Calcul du cout des groupements et des agregations **/
		double Cga = 0;
		if(Q.AG && Q.Group) {//System.out.println("existe AG et Group");
		                     Cga = 4 * TDisp(B, TailleResPS) * (TailleResPS - (B+1)) ;
		                     }
		else if(Q.AG || Q.Group) {//System.out.println("existe AG ou Group");
		                          Cga = 2 * TDisp(B, TailleResPS) * (TailleResPS - (B+1)) ;
		                          }
		//System.out.println("Cout du groupement et des agregations = "+ Cga);
		
		double cost = Cri + Cga;
		/*System.out.println();
		System.out.println("Cout de la requete sur se Sous Schema = "+ cost);
		System.out.println();*/
		
		return (cost);
	}
	
	private double CoutVM(Vector<Vector<Integer>> codage, Requete Q, Vector<Object> ensMB) 
	{					
		double CC = 0, CJ = 0; Table fait = this.compte.getTableFaits();		
		
		Requete ReqSelect = new Requete(0, new Vector<Vector<Attribut>>(), new Vector<String>(), new Vector<String> ());
		ReqSelect.Select = Q.Select;
		
		/** verif l'exist d'Attrs des tabs Cand dans le select **/
		boolean ExistJoinVuesED = false;
		for(int i=0; i<Q.Select.size(); i++)
		{
			String attr = Q.Select.elementAt(i).toUpperCase();
			for(int j=0; j<tablesCandidates.size(); j++)
				if(tablesCandidates.elementAt(j).indexAttributs.contains(attr)) {ExistJoinVuesED = true; break;}
			if(ExistJoinVuesED) break;
		}
		
		/** parcours des blocs OR **/
		for(int i=0; i<Q.getBlocsOR().size(); i++)
		{
			/*System.out.println();
			System.out.println("BLOC OR N� : "+i);
			System.out.println();*/
			boolean scenario2 = false;
			Requete q = new Requete(0, new Vector<Vector<Attribut>>(), new Vector<String>(), null);
			Vector<Requete> VReq = new Vector<Requete>(Q.getBlocsOR().elementAt(i).size());
			Vector<Requete> VReqVMnonPert = new Vector<Requete>();

			Vector<Attribut> bloc = new Vector<Attribut>(Q.getBlocsOR().elementAt(i).size());
			Vector<Attribut> blocVues = new Vector<Attribut>(Q.getBlocsOR().elementAt(i).size());
			Vector<HashSet<Integer>> Vues = new Vector<HashSet<Integer>>(AttributCodage.size());
			Vector<HashSet<Integer>> VuesNonPert = new Vector<HashSet<Integer>>(AttributCodage.size());
			
			//Initialis� l'ens des vues bloc
			for(int h=0; h<AttributCodage.size(); h++)  
			{
				HashSet<Integer> H = new HashSet<Integer>();
				Vues.add(H); VuesNonPert.add(H);
			}
			
			/** parcours des attributs du bloc **/
			for(int j=0; j<Q.getBlocsOR().elementAt(i).size(); j++)
			{
				Attribut a = Q.getBlocsOR().elementAt(i).elementAt(j);
				int indAtt = this.indexAttributs.indexOf(a.GetNomAtt());
							
				if(indAtt!=-1) 
				{ 	
					HashSet<Integer> HistVues = new HashSet<Integer>();
					boolean V = false;
					Attribut A = null; 
					
					if(a.type.equals("string")) 
				{
					//verifi� si existe SD=0 et enregistr� les vues prtinentes
					for(int k=0; k< a.valeurs.size(); k++)
					{
						String Val = (String) a.valeurs.elementAt(k); String Operateur = a.operateurs.elementAt(k);						
						
						if(!Operateur.contains("<>"))
						{	
							int p = Val.indexOf("'"), n = Val.indexOf(",");
								while(n != -1) 
								{
									String pred = Val.substring(p,n);									
									
									//on verifie si le predicat accede a une vue ds le codage et on enregistre sa VueM
									for (int s=0; s<SouDomAttrCod.elementAt(indAtt).size(); s++)
										{ 
										  String sd = (String) SouDomAttrCod.elementAt(indAtt).elementAt(s);
										  if(sd.contains(pred)) 
										     if (codage.elementAt(indAtt).elementAt(s)>0) 
										    	 {
										    	 	HistVues.add(codage.elementAt(indAtt).elementAt(s)); 
										    	 	/*System.out.println("sd : "+sd+" pred : "+pred+" "+
										    		    	           codage.elementAt(indAtt).elementAt(s));*/
										    	 }
										     else //si VM inexist quitter bcle 
										     {V=true; break;}
										}		
									if(V) break;
									p=n+1; n = Val.indexOf(",",p);
								}
								
						        if(n==-1) 
						        {
                                    String pred = Val.substring(p);
									
									//on verifie si le predicat accede a une vue ds le codage et on enregistre sa VueM
									for (int s=0; s<SouDomAttrCod.elementAt(indAtt).size(); s++)
										 { 
										     String sd = (String) SouDomAttrCod.elementAt(indAtt).elementAt(s);
										     if(sd.contains(pred)) 
										     if (codage.elementAt(indAtt).elementAt(s)>0) 
										     { HistVues.add(codage.elementAt(indAtt).elementAt(s)); 
									    	   /*System.out.println("sd : "+sd+" pred : "+pred+" "+
									    			             codage.elementAt(indAtt).elementAt(s));*/
									    	 }
										     else //si VM inexist quitter bcle
										     {V=true; break;}
										}		
						        }
						}
						else if(Operateur.equals("<>"))
						{						
							//on verifie si le predicat accede a une vue ds le codage et on enregistre sa VueM
							for (int s=0; s<SouDomAttrCod.elementAt(indAtt).size(); s++)
								{ 
								  String sd = (String) SouDomAttrCod.elementAt(indAtt).elementAt(s);
								  if(sd.contains(",") || !sd.contains(Val))
									  if (codage.elementAt(indAtt).elementAt(s)>0) 
										  { HistVues.add(codage.elementAt(indAtt).elementAt(s));
								  		    /*System.out.println("sd : "+sd+" pred : "+Val+" "+
								  				  			  codage.elementAt(indAtt).elementAt(s));*/
								  		  }
								  
									  else 	//si VM inexist quitter bcle
									  		{V=true; break;}
								}	
						}
					}//fin parcours valeurs attr requete
					
	if(!V) //si pred n'accede pas a des SD=0 ; Recherche des VM non pertinentes
	{	
		/*System.out.println();
		System.out.println("Recherche des VM non pertinentes String :");
		System.out.println();*/
		
		Iterator I = HistVues.iterator();
		String ValPred = (String) a.valeurs.firstElement();
		String Operateur = a.operateurs.firstElement();
		
		while(I.hasNext())
		{
			int numVM = (Integer) I.next();	boolean pert = true;
			
			for (int s=0; s<SouDomAttrCod.elementAt(indAtt).size(); s++)
				if(codage.elementAt(indAtt).elementAt(s) == numVM)
				{
					String ValSD = (String)SouDomAttrCod.elementAt(indAtt).elementAt(s);
										
						int p = ValSD.indexOf("'"), n = ValSD.indexOf(",");
							while(n != -1) 
							{
								String SD = ValSD.substring(p,n);
								
								if(!Operateur.equals("<>"))  
									{ if(!ValPred.contains(SD)) 
								        { 	pert = false; 
								        	/*System.out.println("Att : "+a.GetNomAtt()+" "+Operateur+" "+ValPred);
											System.out.println(" SD : "+SD);
											System.out.println("VM "+numVM+" non pertinente");*/
											
											HistVues.remove(numVM);	I=HistVues.iterator();											
											
								            VuesNonPert.elementAt(indAtt).add(numVM); break;
								         }
									}
								
								else {	if(ValPred.contains(SD)) 
									 { 	pert = false; 
									 	
									 	/*System.out.println("Att : "+a.GetNomAtt()+" "+Operateur+" "+ValPred);
									 	System.out.println(" SD : "+SD);
									 	System.out.println("VM "+numVM+" non pertinente");*/

									 	HistVues.remove(numVM);	I=HistVues.iterator();                            												
										
										VuesNonPert.elementAt(indAtt).add(numVM); break;
									  }}
								p=n+1; n = ValSD.indexOf(",",p);
							}
							
					        if(n==-1) 
					        {
					        	String SD = ValSD.substring(p);									
					        	if(!Operateur.equals("<>"))  
					        	  { if(!ValPred.contains(SD)) 
	                               { 	pert = false; 
	                               		/*System.out.println("Att : "+a.GetNomAtt()+" "+Operateur+" "+ValPred);
	                               		System.out.println(" SD : "+SD);
	                               		System.out.println("VM "+numVM+" non pertinente");*/
	                               		
	                               		HistVues.remove(numVM);	I=HistVues.iterator();                    												
										
	                               		VuesNonPert.elementAt(indAtt).add(numVM); break;
	                                }}
					        	
					        	else{	if(ValPred.contains(SD)) 
					        		{ 	pert = false; 
		 	
					        			/*System.out.println("Att : "+a.GetNomAtt()+" "+Operateur+" "+ValPred);
					        			System.out.println(" SD : "+SD);
					        			System.out.println("VM "+numVM+" non pertinente");*/
					        											        			
	                               		HistVues.remove(numVM);	I=HistVues.iterator();
					        			
					        			VuesNonPert.elementAt(indAtt).add(numVM); break;
					        		}}
					        }
					        
					        if(!pert) break;
					
					        }//fin parcours des SD
					        
			//si vue non pert pr le pred alors creer nouv pred pr recup vals pert de cette VM
			if(!pert) 
	        {   
	        	String predReq = "", result = "";
	        	int limite=0; 
	        	
	        	if(((String)SouDomAttrCod.elementAt(indAtt).elementAt(SouDomAttrCod.elementAt(indAtt).size()-1)).contains("OR"))
	        		 limite = SouDomAttrCod.elementAt(indAtt).size()-1;
	        	else limite = SouDomAttrCod.elementAt(indAtt).size();
	        	
	        	for(int per=0; per<limite; per++)
	        		if(codage.elementAt(indAtt).elementAt(per)==numVM)
	        			{if(!predReq.equals("")) predReq+=","; predReq += SouDomAttrCod.elementAt(indAtt).elementAt(per);}
	        	
	        	int p = predReq.indexOf("'"), n = predReq.indexOf(",");
	        
	        	if(!predReq.equals("")) //si la vue contient des ss dom avant Reste
	        {	        	
				while(n != -1) 
				{
					String SD = predReq.substring(p,n);
					if(Operateur.equals("<>"))
						{if(!ValPred.contains(SD)) 
		                    { if(!result.equals("")) result+=","; result += SD; }}
					else if(ValPred.contains(SD)) 
							{ if(!result.equals("")) result+=","; result += SD;}
					p=predReq.indexOf("'",n); n = predReq.indexOf(",",p);
				}
				
		        if(n==-1) 
		        {   		        	
		        	String SD = predReq.substring(p);
		        	if(Operateur.equals("<>"))
						{if(!ValPred.contains(SD)) 
	                    	{ if(!result.equals("")) result+=","; result += SD; }}
		        	else if(ValPred.contains(SD)) 
							{ if(!result.equals("")) result+=","; result += SD; }
		        }
	        }//fin verif si existe des sd avant RESTE
		        
	        	//Faire les mm traitements pr le sd Reste s'il existe 
		        if(limite == SouDomAttrCod.elementAt(indAtt).size()-1 && codage.elementAt(indAtt).elementAt(limite)==numVM)
		        {
		        	predReq = (String)SouDomAttrCod.elementAt(indAtt).elementAt(limite);
		        	p = predReq.indexOf("'"); n = predReq.indexOf("OR");
					while(n != -1) 
					{
						String SD = predReq.substring(p,n).trim();
						if(Operateur.equals("<>"))
							{if(!ValPred.contains(SD)) 
			                    { if(!result.equals("")) result+=","; result += SD; }}
						else if(ValPred.contains(SD)) 
								{ if(!result.equals("")) result+=","; result += SD;}
						p=predReq.indexOf("'",n); n = predReq.indexOf("OR",p);
					}
					
			        if(n==-1) 
			        {
			        	String SD = predReq.substring(p);
			        	if(Operateur.equals("<>"))
							{if(!ValPred.contains(SD)) 
		                    	{ if(!result.equals("")) result+=","; result += SD; }}
			        	else if(ValPred.contains(SD)) 
								{ if(!result.equals("")) result+=","; result += SD; }
			        }
		        }
		        if(!result.equals(""))
		        {
		        	Attribut att = new Attribut(a.GetTableAtt(), a.GetNomAtt());
		        	att.valeurs = new Vector<Object>(1); att.operateurs = new Vector<String>(1);
		        	att.valeurs.add(result); att.operateurs.add("IN"); att.type=a.type;

		        	Vector<Attribut> B = new Vector<Attribut>(1); B.add(att);
		        	Requete v = new Requete(0, new Vector<Vector<Attribut>>(1), new Vector<String>(2), null);
		        	v.getTables().add(fait.getNomTable()); v.getTables().add(a.GetTableAtt());
		        	v.getBlocsOR().add(B);
		        	VReqVMnonPert.add(v);
		        }	
		        	
		        	//System.out.println("Attr : "+a.GetNomAtt()+" result : "+result);
	        }//fin verif pertinence			
			//else System.out.println("VM "+numVM+" Pertinente");
		} //fin while
	}//fin verif si existe ss dom absent = 0
				}//fin type = "string"
					
					else if(a.type.equals("entier")) 
					{
						//System.out.println("Attribut entier : "+a.GetNomAtt()+" ind : "+indAtt);
												
						Vector<Object> VI = ConstrSDpred(a);
						
						/** Recherche des vues pouvants etre pertinantes **/
						//parcours des I des attributs requete
						for(int e=0; e<VI.size(); e++) 
						{	
							Intervalle IVal = (Intervalle) VI.elementAt(e);
							//System.out.println(e+" : ["+IVal.inf+"-"+IVal.sup+"]");							
							
							/** parcours des vals des ss domaines **/
							int k;
							for(k=0; k<SouDomAttrCod.elementAt(indAtt).size(); k++)
								{										
									Intervalle ISD = (Intervalle) SouDomAttrCod.elementAt(indAtt).elementAt(k);									
									
									if((IVal.inf>=ISD.inf && IVal.inf<=ISD.sup)||(IVal.sup>=ISD.inf && IVal.sup<=ISD.sup)
											|| (ISD.inf>=IVal.inf && ISD.sup<=IVal.sup) )										
										if(codage.elementAt(indAtt).elementAt(k)>0)					 	
											HistVues.add(codage.elementAt(indAtt).elementAt(k));	
									
										else //si VM inexist quitter bcle
										    { V=true; break;}									   				
									
								} //fin parcours ss dom
				
							if(k<SouDomAttrCod.elementAt(indAtt).size()) break;	//on a trouv� un SD=0	
																				
						}//fin parcours I attribut requete						
							
						//Recherche des VM non pertinentes � partir des VM pertinentes prises 
						Iterator I = HistVues.iterator();
						if(!V)
						while(I.hasNext())
						{	int numVM = (Integer) I.next(); int e=VI.size();
							for(int y=0; y<SouDomAttrCod.elementAt(indAtt).size(); y++)
								{
									Intervalle ISD = (Intervalle) SouDomAttrCod.elementAt(indAtt).elementAt(y);
									
									if(codage.elementAt(indAtt).elementAt(y)==numVM)
										for(e=0; e<VI.size(); e++)
										{										
											Intervalle IVal = (Intervalle) VI.elementAt(e);
									
											if(IVal.inf<=ISD.inf && IVal.sup>=ISD.sup) //SD pertinant inclue dans I pred
												//System.out.println("SD : "+y+" VM N� : "+numVM+" pertinente")
											;
											else //cas SD non pertinant
												{ 	
													HistVues.remove(numVM); I = HistVues.iterator();
													VuesNonPert.elementAt(indAtt).add(numVM);
													//System.out.println("SD : "+y+" VM N� : "+numVM+" non pertinente");
													break;
												}
										}//fin parcours des I pred
									if(e<VI.size()) break;
								}//fin parcours des SD
						}//fin parcours HistVues
					
					//Construction des requetes pr les VM non pertinentes trouv�es
					if(!V && VuesNonPert.elementAt(indAtt).size()>0)
					{ int cp=0; 
						Requete v = new Requete(0, new Vector<Vector<Attribut>>(), new Vector<String>(), null);
							v.getTables().add(fait.getNomTable()); v.getTables().add(a.GetTableAtt());
							VReqVMnonPert.add(v);
							
						/** parcours des I des attributs de la requete **/	
						for(int e=0; e<VI.size(); e++) 
						{						
							/** parcours des vals des ss domaines **/
							int k;
							for(k=0; k<SouDomAttrCod.elementAt(indAtt).size(); k++)								
								if(VuesNonPert.elementAt(indAtt).contains(codage.elementAt(indAtt).elementAt(k)))
								{									
									Intervalle ISD = (Intervalle) SouDomAttrCod.elementAt(indAtt).elementAt(k);
									Intervalle IVal = (Intervalle) VI.elementAt(e);
									
						//on verifie si ISD possede des vals recherch�es			
							/*System.out.println(" Bloc num "+cp); cp++;	
							System.out.println("IVal : ["+IVal.inf+"-"+IVal.sup+"] , ISD : ["+ISD.inf+"-"+ISD.sup+"]");*/
							
							Vector<Attribut> B = new Vector<Attribut>(1); 
									
									A = new Attribut(a.GetTableAtt(), a.GetNomAtt());
									A.valeurs = new Vector<Object>();
									A.operateurs = new Vector<String>();
									A.type = a.type;
									
						if(IVal.inf < ISD.inf && IVal.sup>=ISD.inf && IVal.sup<=ISD.sup)
							{
								A.valeurs.add(ISD.inf); 	A.operateurs.add(">=");
								A.valeurs.add(IVal.sup); 	A.operateurs.add("<=");
								
								//System.out.println("pred : >= "+ISD.inf+" and <= "+IVal.sup+" ajout�");
							}
					
						else if (IVal.sup > ISD.sup && IVal.inf <= ISD.sup && IVal.inf >= ISD.inf) 
								{
									A.valeurs.add(IVal.inf);	A.operateurs.add(">=");
									A.valeurs.add(ISD.sup); 	A.operateurs.add("<=");
									
									//System.out.println("pred : >= "+IVal.inf+" and <= "+ISD.sup+" ajout�");
								}
							else if ((ISD.inf < IVal.inf && ISD.sup >= IVal.sup)||(ISD.inf<=IVal.inf && ISD.sup>IVal.sup))									
									{
										A.valeurs.add(IVal.inf); A.operateurs.add(">=");
										A.valeurs.add(IVal.sup); A.operateurs.add("<=");
										
										//System.out.println("pred : >= "+IVal.inf+" and <= "+IVal.sup+" ajout�");
									}
								else if (IVal.inf <= ISD.inf && IVal.sup>=ISD.sup) //ISD inclue ds IVAL
									{
										A.valeurs.add(ISD.inf); A.operateurs.add(">=");
										A.valeurs.add(ISD.sup); A.operateurs.add("<=");
									
										//System.out.println("pred : >= "+ISD.inf+" and <= "+ISD.sup+" ajout�");
									}
								
						if(A.valeurs.size()>0)	{B.add(A); v.getBlocsOR().add(B);}
						
								}//fin parcours des SD
						}//fin parcours intervalles predicat
					}									
					} //fin type = entier
					
					else if(a.type.equals("reel")) 
					{                         						
						//System.out.println("Attribut Reel : "+a.GetNomAtt()+" ind : "+indAtt);
						
						Vector<Object> VI = ConstrSDpred(a);
						
						/** Recherche des vues pouvants etre pertinantes **/
						//parcours des I des attributs requete
						for(int e=0; e<VI.size(); e++) 
						{	
							IReel IVal = (IReel) VI.elementAt(e);
							//System.out.println(e+" : ["+IVal.inf+"-"+IVal.sup+"]");					
							
							/** parcours des vals des ss domaines **/
							int k;
							for(k=0; k<SouDomAttrCod.elementAt(indAtt).size(); k++)
								{										
									IReel ISD = (IReel) SouDomAttrCod.elementAt(indAtt).elementAt(k);											
									
						if( (IVal.inf>=ISD.inf && IVal.inf<=ISD.sup)||(IVal.sup>=ISD.inf && IVal.sup<=ISD.sup)
							|| (ISD.inf>=IVal.inf && ISD.sup<=IVal.sup) ) 										
						if(codage.elementAt(indAtt).elementAt(k)>0)					 	
							{
								HistVues.add(codage.elementAt(indAtt).elementAt(k));
								//System.out.println("vue succeptible N� : "+codage.elementAt(indAtt).elementAt(k));
							}						
						else //si VM inexist quitter bcle
						    { V=true; break;}								   				
									
								} //fin parcours ss dom
				
							if(k<SouDomAttrCod.elementAt(indAtt).size()) break;	//on a trouv� un SD=0	
																				
						}//fin parcours I attribut requete
					
					//Recherche des VM non pertinentes � partir des VM pertinentes prises 
						Iterator I = HistVues.iterator();
						if(!V)
						while(I.hasNext())
						{ int numVM = (Integer) I.next(); int e=VI.size();
						  //System.out.println("Verif VM N� : "+numVM);
							for(int y=0; y<SouDomAttrCod.elementAt(indAtt).size(); y++)
								{									
									IReel ISD = (IReel) SouDomAttrCod.elementAt(indAtt).elementAt(y);
									
									if(codage.elementAt(indAtt).elementAt(y)==numVM)
										for(e=0; e<VI.size(); e++)
										{										
											IReel IVal = (IReel) VI.elementAt(e);
											//System.out.println(e+" pertinence : ["+IVal.inf+"-"+IVal.sup+"]");
									
											if(IVal.inf<=ISD.inf && IVal.sup>=ISD.sup) //SD pertinant inclue dans I
												//System.out.println("SD : "+y+" VM N� : "+numVM+" pertinente")
											;
											else //cas SD non pertinant
												{ 	
													HistVues.remove(numVM); I = HistVues.iterator();
													VuesNonPert.elementAt(indAtt).add(numVM);
													//System.out.println("SD : "+y+" VM N� : "+numVM+" non pertinente");
													break;
												}
										}//fin parcours des Ipred
									if(e<VI.size()) break;
								}//fin parcours des ISD
						}//fin parcours HistVues
					
					//Construction des requetes pr les VM non pertinentes trouv�es
					if(!V && VuesNonPert.elementAt(indAtt).size()>0)
					{ int cp=0;
						Requete v = new Requete(0, new Vector<Vector<Attribut>>(), new Vector<String>(), null);
							v.getTables().add(fait.getNomTable()); v.getTables().add(a.GetTableAtt()); 
							VReqVMnonPert.add(v);
							
						/** parcours des I des attributs de la requete **/	
						for(int e=0; e<VI.size(); e++) 
						{						
							/** parcours des vals des ss domaines **/
							int k;
							for(k=0; k<SouDomAttrCod.elementAt(indAtt).size(); k++)								
								if(VuesNonPert.elementAt(indAtt).contains(codage.elementAt(indAtt).elementAt(k)))
								{									
									IReel ISD = (IReel) SouDomAttrCod.elementAt(indAtt).elementAt(k);
									IReel IVal = (IReel) VI.elementAt(e);
									
						//on verifie si ISD possede des vals recherch�es			
						if((IVal.inf>=ISD.inf && IVal.inf<=ISD.sup)||(IVal.sup>=ISD.inf && IVal.sup<=ISD.sup))
						{
									Vector<Attribut> B = new Vector<Attribut>(1); 
									//System.out.println(" Bloc num "+cp); cp++;
									
									A = new Attribut(a.GetTableAtt(), a.GetNomAtt());
									A.valeurs = new Vector<Object>();
									A.operateurs = new Vector<String>();
									A.type = a.type;
									
						if((IVal.inf < ISD.inf && IVal.sup>=ISD.inf && IVal.sup<=ISD.sup))
							{
								A.valeurs.add(ISD.inf); 	A.operateurs.add(">=");
								A.valeurs.add(IVal.sup); 	A.operateurs.add("<=");
								
								//System.out.println("pred : >= "+ISD.inf+" and <= "+IVal.sup+" ajout�");
							}
					
						else if (IVal.sup > ISD.sup && IVal.inf <= ISD.sup && IVal.inf >= ISD.inf) 
								{
									A.valeurs.add(IVal.inf);	A.operateurs.add(">=");
									A.valeurs.add(ISD.sup); 	A.operateurs.add("<=");
									
									//System.out.println("pred : >= "+IVal.inf+" and <= "+ISD.sup+" ajout�");
								}
							else if (ISD.inf < IVal.inf && ISD.sup > IVal.sup)									
									{
										A.valeurs.add(IVal.inf); A.operateurs.add(">=");
										A.valeurs.add(IVal.sup); A.operateurs.add("<=");
										
										//System.out.println("pred : >= "+IVal.inf+" and <= "+IVal.sup+" ajout�");
									}
							else if (IVal.inf <= ISD.inf && IVal.sup>=ISD.sup) //ISD inclue ds IVAL
							{
								A.valeurs.add(ISD.inf); A.operateurs.add(">=");
								A.valeurs.add(ISD.sup); A.operateurs.add("<=");
							
								//System.out.println("pred : >= "+ISD.inf+" and <= "+ISD.sup+" ajout�");
							}
								
						if(A.valeurs.size()>0)	{B.add(A); v.getBlocsOR().add(B);} 
						
						}//fin verif si ISD possede des vals recherch�es
								} //fin parcours des SD
						} //fin parcours intervalles predicat
					} //fin verif si exist VM non pertinentes
					} //fin type = reel
					
					if(V) { //si pred acced a des SD non rep par des VM
							HistVues.clear(); VuesNonPert.elementAt(indAtt).clear();
							scenario2 = true; bloc.add(a);
							//System.out.println("att "+a.GetNomAtt()+" ajout� ds bloc des SD=0");
					  	  }
					else{	
							blocVues.add(a);
							//System.out.println("Vue att "+a.GetNomAtt()+" ajout� ds blocVues pr non Abs de SD");
						}
					
					Vues.elementAt(indAtt).clear();					
					Vues.elementAt(indAtt).addAll(HistVues);
        			
				}//fin verif attr de frag
				else{	scenario2 =true;bloc.add(a);
				      	//System.out.println("att "+a.GetNomAtt()+" non frag ajout� ds req des SD=0");
					}		
			}//fin du parcours des attrs du bloc OR
			
			if(bloc.size()>0) //enregistr� les pred qui accedent � des SD VIDES
			{				
				q.getBlocsOR().add(bloc); q.Select = Q.Select;
				for(int it = 0; it<bloc.size(); it++) q.getTables().add(bloc.elementAt(it).GetTableAtt());
				VReq.add(q);
			}		
			
			/** calcul du cout des join des vues non pertinentes **/
			/*System.out.println();
			System.out.println("Calcul du cout de join des VM non pertinentes");
			System.out.println(); */
			double CJnonPert = 0;
			//System.out.println(" Taille Vect Req Non Pert : "+VReqVMnonPert.size());
			for(int h=0; h<VReqVMnonPert.size(); h++)
			for(int j=0; j<ensMB.size(); j++)
		    {
		    	Vector<Vector<Integer>> CODE = (Vector<Vector<Integer>>) ensMB.elementAt(j);
		    	if(Valide(CODE, VReqVMnonPert.elementAt(h)))
		    	{	
		    		/*System.out.println("SCH N�:"+j);
		    		System.out.print("Requ�te initiale : ");
		    		for(int y=0; y<VReqVMnonPert.elementAt(h).getBlocsOR().size(); y++) 
	    			{ 	if(y>=1) System.out.print("  OR  ");
	    				for(int t=0; t<VReqVMnonPert.elementAt(h).getBlocsOR().elementAt(y).size(); t++)
	    					{ 	Attribut atr = VReqVMnonPert.elementAt(h).getBlocsOR().elementAt(y).elementAt(t);
	    				      	for(int v=0; v<atr.valeurs.size(); v++)
	    						System.out.print(" "+atr.GetNomAtt()+" "+atr.operateurs.elementAt(v)
	    								         +" "+atr.valeurs.elementAt(v));
	    					}	    			
	    			} System.out.println();*/
		    		
		    		Requete Q1 = new Requete(VReqVMnonPert.elementAt(h));
		    		ReecRequete(CODE, Q1);
		    		
		    		/*System.out.print("Requ�te reecrite : ");
		    		for(int y=0; y<Q1.getBlocsOR().size(); y++) 
		    			{ 	if(y>=1) System.out.print("  OR  ");
		    				for(int t=0; t<Q1.getBlocsOR().elementAt(y).size(); t++)
		    					{ 	Attribut atr = Q1.getBlocsOR().elementAt(y).elementAt(t);
		    				      	for(int v=0; v<atr.valeurs.size(); v++)
		    						System.out.print(" "+atr.GetNomAtt()+" "+atr.operateurs.elementAt(v)
		    								         +" "+atr.valeurs.elementAt(v));
		    					}
		    			} System.out.println();*/
		    		double cj = CoutIndex(CODE, Q1); 
		    		CJ += cj; CJnonPert+=cj;
		    	}
		    }	
			/*System.out.println("Cout des joins des VM non pertinentes = "+CJnonPert);
			System.out.println();*/
			
			/** calcul du cout de chargement des vues pertinentes **/
			/*System.out.println();
			System.out.println("Cout de chargement des VM");
			System.out.println();*/
			for(int h=0; h<Vues.size(); h++)
			   if(Vues.elementAt(h).size()>0)
			   {
				   //System.out.println("Charg� les VM de l'attribut : "+AttributCodage.elementAt(h).GetNomAtt());
				   Iterator<Integer> I = Vues.elementAt(h).iterator();
				   while(I.hasNext())
				   {					    
					   int VM = I.next(); 
					   //System.out.println("Vue N� : "+VM);
					   
					   /** on calcul la taille de la vue **/
					    //calculer la selectivit� de la vue
					    double selVM = 0;
					   for(int k=0; k<codage.elementAt(h).size(); k++)
						   if(codage.elementAt(h).elementAt(k)==VM) {selVM += this.SelFait.elementAt(h).elementAt(k);
						   //System.out.println("L: "+h+" sd: "+k+" SelVM + "+this.SelFait.elementAt(h).elementAt(k)+" = "+selVM);
						                                              }
					   long nbTupVM = (long) Math.ceil(selVM * fait.getNb_Tuple()); //nbr tuples de la VM
					   long TailleVMPS = (nbTupVM * fait.getTailleTuple()) / AGexemple.PS; //Taille en PS de la VM
					   
					   CC += TailleVMPS;
				   }			   			   
			   }
			
			/** calcul du cout des jointures ds l'entrepots des pred acced a des SD absents = 0**/
			if(scenario2)				
				{ 	
					/*System.out.println();
					System.out.println("VM=0 ; Cout des jointures des pred acced a des SD=0 non rep par des VM");*/
				    for(int j=0; j<ensMB.size(); j++)
				    {
				    	//System.out.println("SCH N�:"+j);
				    	Vector<Vector<Integer>> CODE = (Vector<Vector<Integer>>) ensMB.elementAt(j);
				    	if(Valide(CODE, q))
				    	{	
				    		double cj = CoutIndex(CODE, q); 
				    		CJ += cj;
				    	}
				    }			
			   	}
			
			/** Cout des join entre le RES de l'intersect des VM et le RES des SD=0 obtenue de l'entrepot : 
			 * 	si exist attrs select en plus **/
			/** RES : resultat des pred acced a des SD absents ds les VM **/ 
			if(ExistJoinVuesED) //si on doit faire des joins avc les dims Cands pr recuperer le RES du SELECT
			{				
				//Requete qui calcule l'intersection des VM utilis�es par ce bloc
				Requete ReqVues= new Requete(0, new Vector<Vector<Attribut>>(), new Vector<String>(), null);
				ReqVues.getBlocsOR().add(blocVues);
				if(bloc.size()>0 && blocVues.size()>0) //s'il existe des predicats accedants a l'ED alors join RES et VM
					
			{		/*System.out.println();		
					System.out.println("ExistJoinVuesED = true ; cout de jointure entre RES SD=0 et RES Intersect VM");
					System.out.println();*/
				long nbTupRES = (long) Math.ceil(SelReqED(q) * fait.getNb_Tuple());
				long nbTupVues = (long) Math.ceil(SelReqED(ReqVues) * fait.getNb_Tuple());
				long TailleSelect = 0;
				
				//calculer la taille des attrs des tabs cand
				for(int f=0; f<Q.Select.size(); f++)
				{
					String attr = Q.Select.elementAt(f).toUpperCase();
					for(int j=0; j<tablesCandidates.size(); j++)
						if(tablesCandidates.elementAt(j).indexAttributs.contains(attr)) 
							{ 	
								int indAtt = tablesCandidates.elementAt(j).indexAttributs.indexOf(attr);
							  	TailleSelect+=tablesCandidates.elementAt(j).getV_Attributs().elementAt(indAtt).GetTailleAtt();
							}
				}
				TailleSelect += fait.getTailleTuple(); 
				//calculer taille RES et vues
				long TailleRES = nbTupRES * TailleSelect;
				long TailleVues = nbTupVues * fait.getTailleTuple();
				//calculer le cout de la jointure par hachage entre le RES d la join et le RES des Vues
				CJ+= 3 * (TailleRES + TailleVues);
			}
				else //cas utilisation des VM seulement alors recup RES du SELECT de l'ED
				{	/*System.out.println();
					System.out.println("ExistJoinVuesED = true ; cout de jointure du RES des VM ds l'ED");
					System.out.println();*/
					for(int j=0; j<ensMB.size(); j++)
				    {
				    	Vector<Vector<Integer>> CODE = (Vector<Vector<Integer>>) ensMB.elementAt(j);
				    	if(Valide(CODE, ReqVues))
				    	{	
				    		double cj = CoutIndex(CODE, ReqVues); 
				    		CJ += cj;
				    	}
				    }			
				}//fin cas VM seulement
			}			
		}//fin parcours des blocs OR				
		
		/** calcul du cout du select avec les tab non Cand **/ 
		/*System.out.println();
		System.out.println("Cout du select avec les TabNonC : ");
		System.out.println();*/
		double CS = 0;
		CS = coutJoinSelect(Q, SelReqED(Q));
		
		System.out.println("CC = "+CC+" CJ = "+CJ+" CS_TabNonC = "+CS);
		
		return (CC+CJ+CS);
	}
	
	private double CoutJoinVM(Vector<Vector<Integer>> codage, Requete Q, Vector<Vector<Integer>> C) {
				
		Vector<Double> SommeLigneJoinF = new Vector<Double>();
		Vector<Vector<Double>> selFragFait = new Vector<Vector<Double>>();
		
		/*System.out.println();
		System.out.println("**********Le Fragment Fait***********");
		System.out.println();*/
		
		/** construction de la matrice de selectivit� des ss dom du frag fait **/
		double selF = selFragFait(C);
		
		for(int i=0; i<SelFait.size(); i++) 
		{
			Vector<Double> V = new Vector<Double>();
			for(int j = 0; j < SelFait.elementAt(i).size(); j++)
			{				
			   if(C.elementAt(i).elementAt(j)==0) V.add(0.0);
			   else {
				     double sel = SelFait.elementAt(i).elementAt(j);
				     for(int c=0; c<SelFait.size(); c++)
				    	 if(c!=i) {
				    		       double sumLigne=0;
				    		       for(int k=0; k<SelFait.elementAt(c).size(); k++)
				    		    	   if(C.elementAt(c).elementAt(k)==1) sumLigne+=SelFait.elementAt(c).elementAt(k);
				    		       sel*=sumLigne;
				    	          }
				         V.add(sel / selF);
			        }
			}
			selFragFait.add(V);
			SommeLigneJoinF.add(0.0);
		}
		
		/*System.out.println("************Selectivit� du Frag Fait************");
		for(int i=0; i<selFragFait.size(); i++) {
			for(int j = 0; j < selFragFait.elementAt(i).size(); j++)
				System.out.print(" "+selFragFait.elementAt(i).elementAt(j));
			    System.out.println();
		                                     }
		System.out.println("**********************************");*/
		
		//on recupere la table de fait
		Table fait = this.compte.getTableFaits();
		
		//on recupere le nbr de tuples de la table de fait
		long nbTupFait = fait.getNb_Tuple();
		
		//on calcul le nbr de tuples du fragment fait du sous schema
		long nbTupFragFait = (long) Math.ceil(selF * nbTupFait);
		
		//on calcul la taille du fragment fait
		double tailleTuples = nbTupFragFait * fait.getTailleTuple();
		
		//on calcul la taille du fragment fait en Pages Systems 
		long TailleFaitPS = (long) Math.ceil(tailleTuples / AGexemple.PS);
		
		Vector<Double> SelAttrNonCTabC = new Vector<Double>(tablesCandidates.size());
		Vector<Boolean> TabAjoindre = new Vector<Boolean>(this.tablesCandidates.size());
		
		/** Calcul du nombre d'attributs de fragmentation pr chaque table **/
		Vector<Integer> nbrAttrParTable = new Vector<Integer>();
		for(int i=0; i<tablesCandidates.size(); i++){ 
		int nb = 0; TabAjoindre.add(false);		
		for(int j = 0; j < AttributCodage.size(); j++)
			if(tablesCandidates.elementAt(i).getNomTable().equals(AttributCodage.elementAt(j).GetTableAtt().toUpperCase()))
		        nb++; 
			    nbrAttrParTable.add(nb);
		                                             }		
		
		/** calcul de la selectivit� de chaque attribut des blocs valides de la requete dans le fait **/
		Vector<String> V = new Vector<String>();
		Vector<String> TableNonC = new Vector<String>();
		Vector<Double> SelAttrNonCTabNonC = new Vector<Double>();
		Vector<String> AttrNonCTabNonC = new Vector<String>();		
		Vector<String> AttrNonCTabC = new Vector<String>();
		
		//parcours des blocs OR 
		for(int o=0; o<Q.getBlocsOR().size(); o++)
		{
		 System.out.println("bloc : "+o);
			Vector<Attribut> Bloc = Q.getBlocsOR().elementAt(o);
			
		 /** parcours des attributs du bloc **/
			for(int u=0; u<Bloc.size(); u++) 
			{   
				double sommel = 0;
				Attribut a = Bloc.elementAt(u);
                int indAtt = indexAttributs.indexOf(a.GetNomAtt().toUpperCase());
                
				if(indAtt != -1) { //si attr candidat alors voir s'il est ind�x�
				    	 
					   for(int i=0; i<a.SDvals.size(); i++) //parcours des SDs valides du predicat
					   {   
						   int isd = a.SDvals.elementAt(i);
						   if(C.elementAt(a.L).elementAt(isd) == 1 && !V.contains(a.L+" "+isd) 
							               && (isd < selFragFait.elementAt(a.L).size()))//pr le cas d reste sil n'esxiste pas 
						   if(codage.elementAt(a.L).elementAt(isd)==0)    
						   {
							    /** enregistr� la table dont l'attribut possede un SD sans VM **/						        
						    	TabAjoindre.setElementAt(true, indexTables.indexOf(a.GetTableAtt().toUpperCase()));
							    
						        sommel = selFragFait.elementAt(a.L).elementAt(isd);	
								SommeLigneJoinF.setElementAt(SommeLigneJoinF.elementAt(a.L)+sommel, a.L);
								System.out.println("sel attr "+a.GetNomAtt()+" = "+sommel);
								
								String sd = a.L+" "+isd; 
								V.add(sd);
						    }
					   }
				                         }
				     /** Attribut non de frag: calculer sa selectivit� dans le frag fait **/
				     else {
				    	   double sel = SelAttSsSchema(a, C);
				    	   
				    	      //si attr non Cand mais sa table est Cand
				    	   if(indexTables.contains(a.GetTableAtt().toUpperCase()))
				    	     { 
				    		   if(AttrNonCTabC.contains(a.GetNomAtt())) {
				    		       double d = SelAttrNonCTabC.elementAt(AttrNonCTabC.indexOf(a.GetNomAtt()));
				    		       if(d<sel) {
				    		    	           SelAttrNonCTabC.setElementAt(sel,AttrNonCTabC.indexOf(a.GetNomAtt()));
				    		                   System.out.println(" selAtt = "+sel);
				    		                  }
				    		       System.out.println("TabC att : "+a.GetNomAtt());
				    		                                             }
				    		   else {
				    			      AttrNonCTabC.add(a.GetNomAtt()); SelAttrNonCTabC.add(sel);
				    		          System.out.println("TabC nouv att : "+a.GetNomAtt()+" selAtt = "+sel);
				    		         }
				    	     }
				    	         //cas attr non Cand et table non Cand
				    	   else  if(!TableNonC.contains(a.GetTableAtt().toUpperCase()))
		    	                   { TableNonC.add(a.GetTableAtt().toUpperCase());
		    	                     AttrNonCTabNonC.add(a.GetNomAtt()); SelAttrNonCTabNonC.add(sel);
		    	                     System.out.println("TabNonC Nouv att : "+a.GetNomAtt()+" selAtt = "+sel);
			    			       }
				    	         else 
				    	        	 {
					    	           double d = SelAttrNonCTabNonC.elementAt(AttrNonCTabNonC.indexOf(a.GetNomAtt()));
					    	           if(d<sel)SelAttrNonCTabNonC.setElementAt(sel, AttrNonCTabNonC.indexOf(a.GetNomAtt()));
					    	       	   System.out.println("TabNonC att : "+a.GetNomAtt()+" sel = "+sel+" selAtt = "+(d+sel));
					    	         }		    	        
				          }
			}			
		}
		
		for(int p=0; p<SommeLigneJoinF.size(); p++) {if(SommeLigneJoinF.elementAt(p)==0) SommeLigneJoinF.setElementAt(1.0, p);
		                                            }
		
		//Les tables de dimensions
		/*System.out.println();
		System.out.println("**********Les Fragments des Dimensions***********");
		System.out.println();*/
		
		Vector<Double> SelJoinDimFait = new Vector<Double>();
		Vector<Double> selD = new Vector<Double>();		
		
		/** Calcul de la selectivit� de la jointure de chaque frag de dimension candidat avec le frag fait **/
		for(int i=0; i<tablesCandidates.size(); i++){ 
		double s=1; selD.add(1.0); //on initialise selD
		
		for(int j = 0; j < AttributCodage.size(); j++)
		    if(tablesCandidates.elementAt(i).indexAttributs.contains(AttributCodage.elementAt(j).GetNomAtt()))
		        s*=SommeLigneJoinF.elementAt(j);
		for(int j = 0; j < AttrNonCTabC.size(); j++)
			if(tablesCandidates.elementAt(i).indexAttributs.contains(AttrNonCTabC.elementAt(j)))
		        s*=SelAttrNonCTabC.elementAt(j);
		
			     SelJoinDimFait.add(s); //on insere la select join de cette dimension
			     System.out.println("Sel Join TabC "+tablesCandidates.elementAt(i).getNomTable()+" Fait = "+s);
		                                             }
		
		/** Calcul de la selectivit� de la jointure de chaque frag de dimension non candidat avec le frag fait **/
		Vector<Double> SelJoinTabNonC = new Vector<Double>();
		for(int i=0; i<TableNonC.size(); i++) { 
		double s=1;
		//on recupere la table corresp de l'entrepot
        Table T = this.compte.getTables().elementAt(this.compte.indexTables.indexOf(TableNonC.elementAt(i)));
		for(int j = 0; j < AttrNonCTabNonC.size(); j++)
			if(T.indexAttributs.contains(AttrNonCTabNonC.elementAt(j)))
		        s*=SelAttrNonCTabNonC.elementAt(j);
			     SelJoinTabNonC.add(s);
	    System.out.println("Sel Join TabNonC "+TableNonC.elementAt(i)+" Fait = "+s);
		                                       }
		
        /** calcul de la selectivit� de chaque fragment de dimension par rapport � sa table de dimension **/
		for(int i=0; i<C.size(); i++) {
			double selLigne = 0;
			
			for(int j = 0; j < C.elementAt(i).size(); j++) {				
		 if(C.elementAt(i).elementAt(j)==1) selLigne+=this.SelDim.elementAt(i).elementAt(j);	
			}
			
			//recherche la table correspendante � l'attribut n� i
			int k=indexTables.indexOf(AttributCodage.elementAt(i).GetTableAtt().toUpperCase()); 
	 
			    selD.setElementAt(selD.elementAt(k) * selLigne, k);
		}
		
		//Affichage de la matrice de selectivit� des attributs de Dimensions
		/*System.out.println("/////////////Selectivit� des attributs de dimensions dans leurs dimensions///////////");
		for(int i=0; i<SelDim.size(); i++) {
			for(int j = 0; j < SelDim.elementAt(i).size(); j++)
				System.out.print(" "+SelDim.elementAt(i).elementAt(j));
			System.out.println();
		}
		//System.out.println("///////////////////////////////////////");
		
		//affichage des valeurs de selectivit�s des tables de dimensions
		System.out.println();
		for(int i=0; i<selD.size(); i++) System.out.print(" selDim "+tablesCandidates.elementAt(i).getNomTable()+" = "+
				                                           selD.elementAt(i));
		System.out.println();*/
		 
		Vector<Long> TdimPS = new Vector<Long>();
		Vector<Long> nbTupFragDim = new Vector<Long>();
		Vector<Double> FS = new Vector<Double>();
		//Vector<String> TablesSelect = new Vector<String>();		
		Vector<String> tab = new Vector<String>();
		for(int i=0; i<Q.getTables().size(); i++) tab.add(Q.getTables().elementAt(i).toUpperCase());
		
		//calcul de la taille des attributs du select
		/*Vector<Long> TailleSelectTab = new Vector<Long>();
		
		//System.out.println("les attributs du Select sont :");
		for(int r=0; r<Q.Select.size(); r++) 
		    for(int h=0; h<cmpt.getTables().size(); h++) 
			    for(int g=0; g<cmpt.getTables().elementAt(h).getV_Attributs().size();g++)
			       if(Q.Select.elementAt(r).toUpperCase()
				               	.equals(cmpt.getTables().elementAt(h).getV_Attributs().elementAt(g).GetNomAtt())
				      && !cmpt.getTables().elementAt(h).equals(cmpt.getTableFaits()))
				  {
			    	   if(!TablesSelect.contains(cmpt.getTables().elementAt(h).getNomTable().toUpperCase()))
				        { TablesSelect.add(cmpt.getTables().elementAt(h).getNomTable().toUpperCase());
					      TailleSelectTab.add(cmpt.getTables().elementAt(h).getV_Attributs().elementAt(g).GetTailleAtt());
					    }
				    else {
				    	int ind = TablesSelect.indexOf(cmpt.getTables().elementAt(h).getNomTable().toUpperCase());
				    	long tailleAtt = cmpt.getTables().elementAt(h).getV_Attributs().elementAt(g).GetTailleAtt();
				    	TailleSelectTab.setElementAt(TailleSelectTab.elementAt(ind)+tailleAtt, ind);			    
				         }
				/*System.out.println("la taille de "+cmpt.getTables().elementAt(h).getV_Attributs().elementAt(g).GetNomAtt()+
						" = "+cmpt.getTables().elementAt(h).getV_Attributs().elementAt(g).GetTailleAtt());
				  }*/
		//System.out.println();
		
		/** on enleve les tables candidates qui ne vont pas etres jointes **/
		Vector<String> TabCandAjoindre = (Vector<String>) indexTables.clone();
		for(int i=0; i<TabAjoindre.size(); i++)
		  if(TabAjoindre.elementAt(i).equals(false)){TabCandAjoindre.removeElementAt(i); TabAjoindre.removeElementAt(i); i--;}
		
		long nbTupFragD = 0, TailleDimPS;
		//Vector<Long> TailleSelect = new Vector<Long>();
		
		/** Calcul des infos des tables de dimensions **/
		for(int i=0; i<tab.size(); i++) 
		if(TabCandAjoindre.contains(tab.elementAt(i).toUpperCase()) /*|| TablesSelect.contains(tab.elementAt(i).toUpperCase())
			*/|| TableNonC.contains(tab.elementAt(i).toUpperCase()))
		{   
			//on recupere la table de dimension
			int index = this.compte.indexTables.indexOf(tab.elementAt(i).toUpperCase()); 
			Table Dim = this.compte.getTables().elementAt(index);			
			int id = indexTables.indexOf(Dim.getNomTable().toUpperCase());
			
			//on recupere le nbr de tuples de la table de dimension
			long nbTupDim = Dim.getNb_Tuple();
			//System.out.println("nb tuples de "+Dim.getNomTable()+" initiale = "+ nbTupDim);
			
			//on calcul le nbr de tuples du fragment de la table de dimension
			if(indexTables.contains(Dim.getNomTable().toUpperCase()))         
			{ nbTupFragD = (long) Math.ceil(selD.get(id) * nbTupDim);
			System.out.println("nb tuples du fragment de "+Dim.getNomTable()+" = "+nbTupFragD);
			}
			
			else nbTupFragD = nbTupDim;	        
			
	        //on calcul la taille du fragment de la table de dimension
			double tailleFragDim = nbTupFragD * Dim.getTailleTuple();
			//System.out.println("taille frag Dimension = "+ tailleFragDim);
			
			//on calcul la taille du fragment de dimension en Pages Ssystems 
			TailleDimPS = (long) Math.ceil(tailleFragDim / AGexemple.PS);
			//System.out.println("taille frag dimension en PS = "+ TailleDimPS);	
		
			//Calcul du facteur de selectivit� de chaque table de dimension
	//on divise le nbr de tuples selectionn�s de la jointure entre la dimension et le fait sur le nb tup Dim * nb Tup Fait
			double selDF = 0.0;
			if(TabCandAjoindre.contains(Dim.getNomTable().toUpperCase())) selDF = SelJoinDimFait.elementAt(id);
			else if(TableNonC.contains(Dim.getNomTable().toUpperCase())) 
				    selDF = SelJoinTabNonC.elementAt(TableNonC.indexOf(Dim.getNomTable().toUpperCase()));
			     else selDF = 1; //cas table contient des attrs ds le select c'est tt
			System.out.println("sel join avc frag fait "+selDF);
			if( selDF < 1) {
				nbTupFragDim.add(nbTupFragD); TdimPS.add(TailleDimPS); 
			double fs = (selDF * (double) nbTupFragFait) / (double)(nbTupFragD * nbTupFragFait);
			//System.out.println("Facteur de selectivit� du fragment de "+Dim.getNomTable()+" = "+fs);
			FS.add(fs);
			/*if(TablesSelect.contains(Dim.getNomTable())) 
			     TailleSelect.add(TailleSelectTab.elementAt(TablesSelect.indexOf(Dim.getNomTable())));
		    else TailleSelect.add((long)0);*/
			}
	    	/*else if(!TablesSelect.contains(Dim.getNomTable().toUpperCase()))
			     System.out.println("Table "+Dim.getNomTable()+" extraite de la jointure");
				
			     else{
				System.out.println("Contient des attributs dans le Select");
				nbTupFragDim.add(nbTupFragD); TdimPS.add(TailleDimPS);
				double fs = (selDF * (double) nbTupFragFait) / (double)(nbTupFragD * nbTupFragFait);
				//System.out.println("Facteur de selectivit� du fragment de "+Dim.getNomTable()+" = "+fs);
				FS.add(fs);
				//TailleSelect.add(TailleSelectTab.elementAt(TablesSelect.indexOf(Dim.getNomTable())));
			          }
			/*System.out.println();
			System.out.println("*****************************");
			System.out.println();*/
		}		
		
		if(FS.size()==0) return(0); //si ya pa de tables a joindres
		
        /** Cout de la lere jointure **/		
		
		//on cherche la dimension ayant le facteur de selectivit� minimum
		int iMin = 0; double Min = FS.elementAt(0);
		for(int j=1; j<FS.size(); j++)
			if (FS.elementAt(j) < Min) {Min = FS.get(j); iMin=j;}
		
		//on calcul le cout de la 1ere jointure : taille PS du fait + taille PS de Dmin
		double Cpj = 3 * (TailleFaitPS + TdimPS.elementAt(iMin));
		System.out.println("Cout de la 1ere jointure = "+ Cpj);
		
		//Cout du resultat intermediaire de la 1ere jointure
		Long PS = fait.getTailePS(); long B = 60;
		
		Long nbTupRES = (long) Math.ceil(Min * nbTupFragFait * nbTupFragDim.elementAt(iMin));
		Long TailleResPS = nbTupRES * (fait.getTailleTuple() /*+ TailleSelect.elementAt(iMin)*/) / PS; 
		
		FS.removeElementAt(iMin);
		TdimPS.removeElementAt(iMin);
		nbTupFragDim.removeElementAt(iMin);
		//TailleSelect.removeElementAt(iMin);
		
		//calcul du co�t du resultat intermediaire
		double Cri = 0;
		while( FS.size() > 0 ) 
		{			
			//on cherche la dimension ayant le FS minimum
			iMin = 0; Min = FS.elementAt(0);
			for(int j=1; j<FS.size(); j++) 
	        if (FS.elementAt(j) < Min) {Min = FS.get(j); iMin = j;}
			
			Cri += ( 2 * TDisp(B, TailleResPS) * (TailleResPS - (B+1)) ) + 3 * TdimPS.elementAt(iMin);
			nbTupRES = (long) Math.ceil(Min * nbTupRES * nbTupFragDim.elementAt(iMin));
			TailleResPS = nbTupRES * (fait.getTailleTuple()/*TailleResPS + TailleSelect.elementAt(iMin)*/) / PS;
			
			FS.removeElementAt(iMin);
			TdimPS.removeElementAt(iMin);
			nbTupFragDim.removeElementAt(iMin);
			//TailleSelect.removeElementAt(iMin);
		}
		System.out.println("Cout du Resultat intermediare = "+Cri);
		
		/** Calcul du cout des groupements et des agregations **/
		/*double Cga = 0;
		if(Q.AG && Q.Group) {//System.out.println("existe AG et Group");
		                     Cga = 4 * TDisp(B, TailleResPS) * (TailleResPS - (B+1)) ;
		                     }
		else if(Q.AG || Q.Group) {//System.out.println("existe AG ou Group");
		                          Cga = 2 * TDisp(B, TailleResPS) * (TailleResPS - (B+1)) ;
		                          }
		System.out.println("Cout du groupement et des agregations = "+ Cga);*/
		
		double cost = Cpj+ Cri /*+ Cga*/;
		/*System.out.println();
		System.out.println("Cout de la requete sur se Sous Schema = "+ cost);
		System.out.println();*/
		
		return (cost);
	}
	
	
	private int TDisp(long B, long RES) 
	{
		if(RES < B) return 0;
		else return 1;
    }    
	
	private boolean existPredSsSchema(Vector<Attribut> Bloc, Vector<Vector<Integer>> C) {
    	//System.out.println("ExistPredSsSchema");
    	//System.out.println("attribut : "+attribut.GetNomAtt());
	/** On verifie par une requete l'existence des predicats ds ce ss schema **/
	   
    	cptCloseConnect++;
	   Compte cmpt = this.compte;
	   Table fait = cmpt.getTableFaits();
	   Statement st = null; ResultSet rs;	   
	   Long nbTatt = null; 
	   String s=""; 
	   if(cptCloseConnect==100){ cmpt.fermerConnexion(); cmpt.seConnecter(); cptCloseConnect=0;}
	   Connection cn = cmpt.getConnection(); 
	   
	   //on ecrit les pred de selection concernant le ss schema
	   for(int is=0; is<SouDomAttrCod.size(); is++)
	       {
	    	if(is==0) s+="(";
	    	if(is>= 1) s+= ") and ("; int cp=0;
	    for(int ia=0; ia<SouDomAttrCod.elementAt(is).size(); ia++)
	    	if(C.elementAt(is).elementAt(ia)==1) {	
	    	
	    	 String sd=""; if(cp>=1) s+=" or ";
	    	
	    	if(AttributCodage.elementAt(is).type.equals("string")) 
	    	   {sd =  (String) SouDomAttrCod.elementAt(is).elementAt(ia);
	    	    if(sd.indexOf("IN ")!=-1) sd=sd.substring(0,sd.indexOf("'")).trim()+" ("
	    	                                +sd.substring(sd.indexOf("'")).trim()+")"; 	    	
	    	   }
	    	
	    	else if(AttributCodage.elementAt(is).type.equals("entier")) 
	    		sd = ">="+((Intervalle)SouDomAttrCod.elementAt(is).elementAt(ia)).inf+" and "+
	    		AttributCodage.elementAt(is).GetNomAtt()+"<="+((Intervalle)SouDomAttrCod.elementAt(is).elementAt(ia)).sup;
	    	
	    	else if(AttributCodage.elementAt(is).type.equals("reel")) 
	    		sd = ">="+((IReel)SouDomAttrCod.elementAt(is).elementAt(ia)).inf+" and "+
	    		AttributCodage.elementAt(is).GetNomAtt()+"<="+((IReel)SouDomAttrCod.elementAt(is).elementAt(ia)).sup;
	    	
	    	s+=AttributCodage.elementAt(is).GetNomAtt()+" "+sd;
	    	System.out.println("ecriture sous schema");
	    	System.out.println("att : "+AttributCodage.elementAt(is).GetNomAtt()+" "+sd);
	    	cp++;
	    	                                      }
          }
	   
	   s += ")"; 
	   HashSet<String> TabAjout = new HashSet<String>();
	    
	   //On ecrir les pred du bloc de la requete
	    if(!s.equals("") && Bloc.size()>0) s+=" and ";
	    for(int i=0; i<Bloc.size(); i++) {
	    	System.out.println("Ecriture bloc");
	    	System.out.println(Bloc.elementAt(i).GetNomAtt()+" "+Bloc.elementAt(i).operateurs.firstElement()
	    	        +Bloc.elementAt(i).valeurs.firstElement());
	    	if(i>=1) s+=" and ";
	    for(int is=0; is<Bloc.elementAt(i).valeurs.size(); is++) 
	    { 
	    	if(is>=1) s+=" and ";
	    	if(Bloc.elementAt(i).operateurs.elementAt(is).equals("IN"))
	    	s+= Bloc.elementAt(i).GetNomAtt()+" "+Bloc.elementAt(i).operateurs.elementAt(is)
	    	    +" ("+Bloc.elementAt(i).valeurs.elementAt(is)+")";
	    	else s+= Bloc.elementAt(i).GetNomAtt()+" "+Bloc.elementAt(i).operateurs.elementAt(is)
	    	        +Bloc.elementAt(i).valeurs.elementAt(is);
	    	
	    	//Ajout de la table de l'attribut si elle n'est pas candidate
	    	if(!indexTables.contains(Bloc.elementAt(i).GetTableAtt().toUpperCase())) 
	    	   TabAjout.add(Bloc.elementAt(i).GetTableAtt().toUpperCase());
	    }
	      }
	    
	    /** ecriture des pred de joint entre le fait et les tables candidates**/
	    String join=""; String tables="";
	    for(int y=0; y<tablesCandidates.size(); y++)
	    {   
	    	if(y>=1) join+=" and ";
	      Table T = tablesCandidates.elementAt(y);
	      tables+=", "+T.getNomTable();
	      join+=fait.getNomTable()+"."+T.getFKfait()+"="+T.getNomTable()+"."+T.getPK();	
	    }
	    
	    /**ajout de la join avc les tables des attributs de la requete**/
	    Iterator I = TabAjout.iterator();
	    if(TabAjout.size()>0)
	    	while(I.hasNext())
	    {   String T = (String) I.next();
	        //System.out.println("table = "+T);
            Table TattReq = this.compte.getTables().elementAt(this.compte.indexTables.indexOf(T));
            tables+=", "+TattReq.getNomTable();
	    	join+=" and "+fait.getNomTable()+"."+TattReq.getFKfait()+"="+TattReq.getNomTable()+"."+TattReq.getPK();
	    }
	   
  try {
		st = cn.createStatement();
        
		System.out.println(	"select count(*) from "+fait.getNomTable()+tables+
	             			" where "+join+" and "+s);
		rs = st.executeQuery("select count(*) from "+fait.getNomTable()+tables+
				             " where "+join+" and "+s);
		if(rs.next())
		nbTatt = Long.parseLong(rs.getString("count(*)"));
		if(nbTatt==0) {//System.out.println("ss schema non valide pour "+attribut.GetNomAtt()); 
		               return false;}
		
		st.close(); rs.close();		
	   } catch (SQLException e) {e.printStackTrace();}
	
	return true;
}
	
	private double SelAttSsSchema(Attribut attribut, Vector<Vector<Integer>> C)
    {   //System.out.println("SelAttrSsSchema");
        //System.out.println("attribut : "+attribut.GetNomAtt());
    	/** On verifie par une requete l'existence du predicat ds ce ss schema **/
    	
 	   Table fait = this.compte.getTableFaits();
 	   Statement st = null; ResultSet rs;
 	   cptCloseConnect++;
 	  if(cptCloseConnect==100){ this.compte.fermerConnexion(); this.compte.seConnecter(); cptCloseConnect=0;}
 	   Connection cn = this.compte.getConnection(); 
 	   Long nbTatt = null; 
       //attribut.type = getType(attribut);
 	   String s=""; 
 	   
 	   //on ecrit les pred de selection concernant le ss schema
 	   for(int is=0; is<SouDomAttrCod.size(); is++)
 	       {
 	    	if(is==0) s+="(";
 	    	if(is>= 1) s+= ") and ("; int cp=0;
 	    for(int ia=0; ia<SouDomAttrCod.elementAt(is).size(); ia++)
 	    	if(C.elementAt(is).elementAt(ia)==1) {	String sd=""; if(cp>=1) s+=" or ";
 	    	
 	    	if(AttributCodage.elementAt(is).type.equals("string")) 
 	    	   {sd =  (String) SouDomAttrCod.elementAt(is).elementAt(ia);
 	    	    if(sd.indexOf("IN ")!=-1) sd=sd.substring(0,sd.indexOf("'")).trim()+" ("
 	    	                                +sd.substring(sd.indexOf("'")).trim()+")"; 
 	    	   }
 	    	else if(AttributCodage.elementAt(is).type.equals("entier")) 
 	    		sd = ">="+((Intervalle)SouDomAttrCod.elementAt(is).elementAt(ia)).inf+" and "+
 	    		AttributCodage.elementAt(is).GetNomAtt()+"<="+((Intervalle)SouDomAttrCod.elementAt(is).elementAt(ia)).sup;
 	    	
 	    	else if(AttributCodage.elementAt(is).type.equals("reel")) 
 	    		sd = ">="+((IReel)SouDomAttrCod.elementAt(is).elementAt(ia)).inf+" and "+
 	    		AttributCodage.elementAt(is).GetNomAtt()+"<="+((IReel)SouDomAttrCod.elementAt(is).elementAt(ia)).sup;
 	    	
 	    	s+=AttributCodage.elementAt(is).GetNomAtt()+" "+sd;
 	    	cp++;
 	    	                                      }
           }	
 	   
 	   //on ajoute les predicats du bloc
 	  s+=")";
	    String s2="";
	    for(int is=0; is<attribut.valeurs.size(); is++) 
	    { 
	    	if(is>=1) s2+=" and ";
	    	if(attribut.operateurs.elementAt(is).equals("IN"))
	    	s2+= attribut.GetNomAtt()+" "+attribut.operateurs.elementAt(is)+" ("+attribut.valeurs.elementAt(is)+")";
	    	else s2+= attribut.GetNomAtt()+" "+attribut.operateurs.elementAt(is)+attribut.valeurs.elementAt(is);
	    }
 	    
 	    /** ecriture des pred de joint entre le fait et les tables**/
 	    String join=""; boolean extabAtt=false; String tables="";
 	    for(int y=0; y<tablesCandidates.size(); y++)
 	    {   
 	    	if(y>=1) join+=" and ";
 	      Table T = tablesCandidates.elementAt(y);
 	      tables+=", "+T.getNomTable();
 	      if(attribut.GetTableAtt().toUpperCase().equals(T.getNomTable().toUpperCase())) extabAtt=true;
 	      join+=fait.getNomTable()+"."+T.getFKfait()+"="+T.getNomTable()+"."+T.getPK();	
 	    }
 	    //ajout de la join avc la table de l'attr de requete
 	    if(!join.equals("") && !extabAtt) 
 	    {
 Table TattReq = this.compte.getTables().elementAt(this.compte.indexTables.indexOf(attribut.GetTableAtt().toUpperCase()));
             tables+=", "+TattReq.getNomTable();
 	    	join+=" and "+fait.getNomTable()+"."+TattReq.getFKfait()+"="+TattReq.getNomTable()+"."+TattReq.getPK();
 	    }
 	    
 	   long nbTupFragFait=0;
   try {
 		st = cn.createStatement();
 		/*System.out.println("select count(*) from "+fait.getNomTable()+tables+
	                       " where "+join+" and "+s+" and "+s2);*/
 		
 		rs = st.executeQuery("select count(*) from "+fait.getNomTable()+tables+
 				             " where "+join+" and "+s+" and "+s2);
 		if(rs.next())
 		nbTatt = Long.parseLong(rs.getString("count(*)"));
 		
 		/*System.out.println("select count(*) from "+fait.getNomTable()+tables+
	                       " where "+join+" and "+s);*/
 		
 		rs = st.executeQuery("select count(*) from "+fait.getNomTable()+tables+
	             " where "+join+" and "+s);
 		
 		if(rs.next())
         nbTupFragFait = Long.parseLong(rs.getString("count(*)"));
 		
 		st.close(); rs.close();
 	  } catch (SQLException e) {e.printStackTrace();}
 	  
 	 double sel = (double) nbTatt / (double)nbTupFragFait;
 	
 	return sel;
    }
	
	private String getType(Attribut attribut) 
    {   int i=this.compte.indexTables.indexOf(attribut.GetTableAtt().toUpperCase()); int j=0;
    		for( j=0; j<this.compte.getTables().elementAt(i).getV_Attributs().size() 
    		           && !this.compte.getTables().elementAt(i).getV_Attributs().elementAt(j).GetNomAtt()
    		              .equals(attribut.GetNomAtt().toUpperCase()); j++);
    		//System.out.println("i="+i+" j="+j+"Vsize="+FH.CompteUser.getTables().elementAt(i).getV_Attributs().size() );
    	String type="";
    	if(this.compte.getTables().elementAt(i).getV_Attributs().elementAt(j).GetTypeAtt().contains("VARCHAR2")
    	   || this.compte.getTables().elementAt(i).getV_Attributs().elementAt(j).GetTypeAtt().contains("VARCHAR")
    	   || this.compte.getTables().elementAt(i).getV_Attributs().elementAt(j).GetTypeAtt().contains("CHAR"))
    	         type = "string";
    	else if(this.compte.getTables().elementAt(i).getV_Attributs().elementAt(j).GetTypeAtt().contains("NUMBER")) 
    		    type="entier";
    	else if(this.compte.getTables().elementAt(i).getV_Attributs().elementAt(j).GetTypeAtt().contains("FLOAT")
    			|| this.compte.getTables().elementAt(i).getV_Attributs().elementAt(j).GetTypeAtt().contains("REAL")) 
		    type="reel";
    	return type;
    }


	private void ReecRequete(Vector<Vector<Integer>> C, Requete Q) {
    	
    	//supprimer les blocs non valides de la requete
    	for(int i=0; i < Q.getBlocsOR().size(); i++)
    		if(BlocsValides.indexOf(i)==-1) { Q.getBlocsOR().removeElementAt(i); i--;
    		                                  for(int j=0; j<BlocsValides.size(); j++) 
    		                                	  BlocsValides.setElementAt(BlocsValides.elementAt(j)-1, j);
    		                                 }
    	
    	//parcours des blocs ORs valides
    	for(int i=0; i<Q.getBlocsOR().size(); i++) 
    	{      		
    		//Parcours des attributs de chaque bloc OR 
    		int j;   		
    		for(j=0; j < Q.getBlocsOR().elementAt(i).size(); j++)
    		{   
    			//on reccupere l'attribut de la requete
    			Attribut att = Q.getBlocsOR().elementAt(i).elementAt(j); 
    			int iAtt = indexAttributs.indexOf(att.GetNomAtt().toUpperCase());
    			int k = 0; boolean nonDeFrag =false;
    			
    			/** verif si attribut candidat **/
    			if(iAtt!=-1)
    			{    			    			    	
    			 if(att.type.equals("string")) 
    		 {    			   			
    			//parcours des vals de l'attribut du fragment
				int f;
				for(f=0; f<SouDomAttrCod.elementAt(iAtt).size(); f++)
					if(C.elementAt(iAtt).elementAt(f)==1)
    			{   String s = (String) SouDomAttrCod.elementAt(iAtt).elementAt(f);
    			
    				//parcours des valeur de l'attribut de la requete
    	    		String valAtt="";
    	    		for(k=0; k<att.valeurs.size(); k++)
    			    {
    	    		   valAtt = (String) att.valeurs.elementAt(k);
    	    		   if(!att.operateurs.elementAt(k).equals("<>"))
    	    		   { 
    				      int p=s.indexOf("'"), n=s.indexOf(",");    				   
    				      while(n!=-1) 
    				      {
    					   if(!valAtt.contains(s.substring(p,n))) break;
    					   p=n+1; n=s.indexOf(",",p);
    				      }
    				      
    				      if(n==-1)   
    				    	{if(!valAtt.contains(s.substring(p))) break;}   				                
    				      else break;
    	    		   }
    	    		   else if(s.contains(valAtt)) break;
    	    		   
    			     } //fin parcours valeurs attribut requete
    				
    	    		if(k<att.valeurs.size()) break;
    			
    			} //fin parcours des vals attribut fragment    
				  if(f==SouDomAttrCod.elementAt(iAtt).size())				 
					  {Q.getBlocsOR().elementAt(i).removeElementAt(j); j--; 
					  System.out.println(att.GetNomAtt()+" supprim� ");
					  }
    			            
    	    }//fin type = string
    			
    			else if(att.type.equals("entier")) {
    				                                            // System.out.println("entier : "+att.GetNomAtt());
    				 /** parcours des vals attribut requete **/
				     for(int e=0; e<att.valeurs.size(); e++) {
					   
					   /** parcours des vals des attributs du fragment **/
					   for(k=0; k<SouDomAttrCod.elementAt(iAtt).size(); k++)
						   if(C.elementAt(iAtt).elementAt(k)==1)
    			{	
					int val = Integer.parseInt(String.valueOf(att.valeurs.elementAt(e)));
				    Intervalle I = (Intervalle) SouDomAttrCod.elementAt(iAtt).elementAt(k);
				   				    
					/** on verifie les diffs types d'operateurs **/			
					if(att.operateurs.elementAt(e).equals(">")) 
					if(I.inf <= val) break;
					
					if(att.operateurs.elementAt(e).equals("<")) 
					if(I.sup >= val) break;
					
					if(att.operateurs.elementAt(e).equals(">=")) 
					if(I.inf < val) break;
					
					if(att.operateurs.elementAt(e).equals("<=")) 
					if(I.sup > val) break;
					
					if(att.operateurs.elementAt(e).equals("=")) 
					if(I.inf != val || I.sup!=val) break;
					
					if(att.operateurs.elementAt(e).equals("<>")) 
					if(I.inf <= val && I.sup>=val) break;
    			} //fin parcours ss dom
				       
				     if(k==SouDomAttrCod.elementAt(iAtt).size())				 
	    			   { /*System.out.println("Valeur "+att.operateurs.elementAt(e)+" "
	    					                +att.valeurs.elementAt(e)+" supprim� ");*/
				    	 att.valeurs.removeElementAt(e); att.operateurs.removeElementAt(e); e--;
				        }	    			     
				     }//fin parcours vals attribut requete
				     
				     if(att.valeurs.size()==0) {Q.getBlocsOR().elementAt(i).removeElementAt(j); j--;}
    			} //fin type = entier
    			
    		else if(att.type.equals("reel")) {
    				                                       // System.out.println("att : "+att.GetNomAtt()+" : "+att.type);
   				 /** parcours des vals attribut requete **/
				     for(int e=0; e<att.valeurs.size(); e++) { 
					   
					   /** parcours des vals de l'attribut du fragment **/
				       for(k=0; k<SouDomAttrCod.elementAt(iAtt).size(); k++)
				    	   if(C.elementAt(iAtt).elementAt(k)==1)
   			   {	double val = Double.parseDouble(String.valueOf(att.valeurs.elementAt(e)));
				    IReel R = (IReel) SouDomAttrCod.elementAt(iAtt).elementAt(k);
				    				    
					/** on verifie les diffs types d'operateurs **/			
					if(att.operateurs.elementAt(e).equals(">")) 
					if(R.inf <= val) break;
					
					if(att.operateurs.elementAt(e).equals("<")) 
					if(R.sup >= val) break;
					
					if(att.operateurs.elementAt(e).equals(">=")) 
					if(R.inf < val) break;
					
					if(att.operateurs.elementAt(e).equals("<=")) 
					if(R.sup > val) break;
					
					if(att.operateurs.elementAt(e).equals("=")) 
					if(R.inf != val || R.sup!=val) break;
					
					if(att.operateurs.elementAt(e).equals("<>")) 
					if(R.inf <= val && R.sup>=val) break;
		        } //fin parcours ss dom
				       
				     if(k==SouDomAttrCod.elementAt(iAtt).size())				 
	    			   { /*System.out.println("Valeur "+att.operateurs.elementAt(e)+" "
	    					                +att.valeurs.elementAt(e)+" supprim� ");*/
				    	 att.valeurs.removeElementAt(e); att.operateurs.removeElementAt(e); e--;
				        }	    			     
				     }//fin parcours vals attribut requete
				     
				     if(att.valeurs.size()==0) {Q.getBlocsOR().elementAt(i).removeElementAt(j); j--;}
				     
   			} //fin type = reel
    			
    			}//fin verif si attr de frag
    			else {
    				   double selAttr = SelAttSsSchema(att, C);
    				   if (selAttr==1) { 
    					                 Q.getBlocsOR().elementAt(i).removeElementAt(j); j--; 
 					                     //System.out.println(att.GetNomAtt()+" supprim� ");
 					                   }
    			      }
    		
    		}//fin parcours attributs du bloc
    		
    		if(Q.getBlocsOR().elementAt(i).size()==0) 
    		  { 
    			Q.getBlocsOR().removeAllElements();
    		
    		   //enlever les tables en plus dans le from
    			if(Q.Select != null)
    	 	   for(int k=0; k<Q.getTables().size(); k++)
    	 		  if(!Q.getTables().elementAt(k).toUpperCase().equals(this.compte.getTableFaits().getNomTable()))
    	 	   {
    	 			  System.out.println("table : "+Q.getTables().elementAt(k).toUpperCase());
    	 		   int indT = this.compte.indexTables.indexOf(Q.getTables().elementAt(k).toUpperCase());
    	 		   Table T = this.compte.getTables().elementAt(indT);
    	 		   
    	 		   int t;
    	 		   for(t=0; t<Q.Select.size(); t++) 
    	 			   if(T.indexAttributs.contains(Q.Select.elementAt(t).toUpperCase())) break; //si un attr trouv� on sort
    	 		   
    	 		   if(t==Q.Select.size())  {
    	 		      System.out.println(Q.getTables().elementAt(k)+" supprim� ");
    	 		     Q.getTables().removeElementAt(k); k--;}//si la aucun attr ds select alors suppr
    	 	   }
    		  }
    		
    	}//fin parcours des blocs de la requete/
    }

    
	private double selFragFait(Vector<Vector<Integer>> C)
    {
		double selF=1;

		//calcul de la selctivit� du frag fait
		for(int i=0; i<C.size(); i++) {			
			double selLigne = 0;		
			for(int j = 0; j < C.elementAt(i).size(); j++) 
				if(C.elementAt(i).elementAt(j)==1) 
                  selLigne+=this.SelFait.elementAt(i).elementAt(j);	
			
			selF*=selLigne;            }
		
       return selF;
    }

	private double SelReqED(Requete Q) 
    {
    	//System.out.println("ExistPredSsSchema");
    	//System.out.println("attribut : "+attribut.GetNomAtt());
	/** On verifie par une requete l'existence du predicat ds ce ss schema **/
	   
    	cptCloseConnect++;
	   Compte cmpt = this.compte;
	   Table fait = this.compte.getTableFaits();
	   Statement st = null; ResultSet rs;	   
	   Long nbTupSel = null, nbTupFait = null; 
	   String s=""; 
	   if(cptCloseConnect==100){ cmpt.fermerConnexion(); cmpt.seConnecter(); cptCloseConnect=0;}
	   Connection cn = cmpt.getConnection(); 
	 	   
	   String s2="";
	   if(Q.getBlocsOR().size()>0) s2+=" (";
	   HashSet<String> TabAjout = new HashSet<String>();
	   for(int j=0; j<Q.getBlocsOR().size(); j++)
	    {
	    	Vector<Attribut> Bloc = Q.getBlocsOR().elementAt(j);
	    	if(j>=1) s2+=" OR "; 
	    		
	    	//On ecrir les pred du bloc de la requete
	    	for(int i=0; i<Bloc.size(); i++) 
	    	{
	    		if(i>=1) s2+=" and ";
	    			for(int is=0; is<Bloc.elementAt(i).valeurs.size(); is++) 
	    				{ 
	    					if(is>=1) s2+=" and ";
	    					if(Bloc.elementAt(i).operateurs.elementAt(is).equals("IN"))
	    						s2+= Bloc.elementAt(i).GetNomAtt()+" "+Bloc.elementAt(i).operateurs.elementAt(is)
	    						+" ("+Bloc.elementAt(i).valeurs.elementAt(is)+")";
	    					else s2+= Bloc.elementAt(i).GetNomAtt()+" "+Bloc.elementAt(i).operateurs.elementAt(is)
	    							+Bloc.elementAt(i).valeurs.elementAt(is);
	    	
	    	//Ajout de la table de l'attribut si elle n'est pas candidate 
	    	   TabAjout.add(Bloc.elementAt(i).GetTableAtt().toUpperCase());
	    				}
	    	}
	    }
	    
	   if(Q.getBlocsOR().size()>0) s2+=" )";
	   
	    String join=""; String tables="";
	    
	    /** Ajout de la join avc les tables des attributs de la requete**/
	    Iterator<String> I = TabAjout.iterator(); int cp = 0;
	    if(TabAjout.size()>0)
	    	while(I.hasNext())
	    	{   
	    		if(cp>=1) {tables += ", "; join += " and ";} 
	    		String T = I.next();
	    		//System.out.println("table = "+T);
	    		Table TattReq = this.compte.getTables().elementAt(this.compte.indexTables.indexOf(T));
	    		tables += TattReq.getNomTable();
	    		join += fait.getNomTable()+"."+TattReq.getFKfait()+"="+TattReq.getNomTable()+"."+TattReq.getPK();
	    		cp++;
	    	}	   
  try {
		st = cn.createStatement();
		
		/*System.out.println("select count(*) from "+fait.getNomTable()+tables+
        " where "+join+" and "+s+s2);*/		
		rs = st.executeQuery("select count(*) from "+fait.getNomTable());
		if(rs.next())
			nbTupFait = Long.parseLong(rs.getString("count(*)"));
        
		System.out.println("select count(*) from "+fait.getNomTable()+", "+tables+
				 " where "+join+" and "+s2);
			rs = st.executeQuery("select count(*) from "+fait.getNomTable()+", "+tables+
								 " where "+join+" and "+s2);
			if(rs.next())
			nbTupSel = Long.parseLong(rs.getString("count(*)"));		
			st.close(); rs.close();		
			
	   } catch (SQLException e) {e.printStackTrace();}
	
	   return nbTupSel/nbTupFait;
    }
    
	private double coutJoinSelect(Requete Q, double SelReqED) {
    	String str = "";		
		Vector<Double> SommeLigneJoinF = new Vector<Double>();
		Vector<Vector<Double>> selFragFait = new Vector<Vector<Double>>();
		
		//on recupere la table de fait
		Table fait = this.compte.getTableFaits();
		//on recupere le nbr de tuples de la table de fait
		long nbTupFait = fait.getNb_Tuple();
		//System.out.println("nb tuples fait = "+ nbTupFait);
		//on calcul le nbr de tuples du fragment fait du sous schema
		long nbTupFragFait = (long) Math.ceil(SelReqED * nbTupFait);
		//System.out.println("nb de tuples du fragment fait de ce sous schema = "+ nbTupFragFait);
		//on calcul la taille du fragment fait
		double tailleTuples = nbTupFragFait * fait.getTailleTuple();
		//System.out.println("taille frag fait = "+ tailleTuples);
		//on calcul la taille du fragment fait en Pages Systems 
		long TailleFaitPS = (long) Math.ceil(tailleTuples / AGexemple.PS);
		//System.out.println("taille frag fait en PS = "+ TailleFaitPS);	
		
		//Les tables de dimensions
		/*System.out.println();
		System.out.println("**********Les Fragments des Dimensions***********");
		System.out.println();*/
		
		Vector<Long> TdimPS = new Vector<Long>();
		Vector<Long> VnbTupDim = new Vector<Long>();
		Vector<Double> FS = new Vector<Double>();				
		Vector<String> tab = new Vector<String>();
		for(int i=0; i<Q.getTables().size(); i++) tab.add(Q.getTables().elementAt(i).toUpperCase());
		
		//on enregistre les tables possedants des predicats de jointures
		HashSet<String> TabJointes = new HashSet<String>();
		for(int j=0; j<Q.getBlocsOR().elementAt(0).size(); j++)
			if(!indexTables.contains(Q.getBlocsOR().elementAt(0).elementAt(j).GetTableAtt()))
				TabJointes.add(Q.getBlocsOR().elementAt(0).elementAt(j).GetTableAtt());
		
		for(int i=0; i<Q.getBlocsOR().size(); i++)
		{
			Iterator I = TabJointes.iterator();
			while(I.hasNext())
			{
				String TabCom = (String) I.next(); int j;
				for(j=0; j<Q.getBlocsOR().elementAt(i).size() 
				            && !Q.getBlocsOR().elementAt(i).elementAt(j).GetTableAtt().equals(TabCom); j++);
				//si tab non commune entre les deux blocs alors la supprim�e
				if(j==Q.getBlocsOR().elementAt(i).size()) 
					{ TabJointes.remove(TabCom);  I=TabJointes.iterator(); }
			}
		}
		
		/** calcul de la taille des attributs du select **/
		Vector<Long> TailleSelectTab = new Vector<Long>();
		Vector<String> TablesSelect = new Vector<String>();
		
		for(int r=0; r<Q.Select.size(); r++) 
		    for(int h=0; h<this.compte.getTables().size(); h++) 
			    if(!this.compte.getTables().elementAt(h).equals(this.compte.getTableFaits()) 
			    		             && !TabJointes.contains(this.compte.getTables().elementAt(h).getNomTable())
			    		             && !indexTables.contains(this.compte.getTables().elementAt(h).getNomTable()) )
			    {
		    		int indAtt = this.compte.getTables().elementAt(h).indexAttributs.indexOf(Q.Select.elementAt(r).toUpperCase());
				    if(indAtt!=-1) 
				    	{ 
				    		Attribut att = this.compte.getTables().elementAt(h).getV_Attributs().elementAt(indAtt);				      
				  
				    		if(!TablesSelect.contains(att.GetTableAtt().toUpperCase()))
				    			{ 	TablesSelect.add(att.GetTableAtt().toUpperCase());
				    				System.out.println("TableNonC Select "+att.GetTableAtt().toUpperCase()+" ajout�");
				    				TailleSelectTab.add(att.GetTailleAtt());
				    				break;
				    			}
				    		else{
				    				int ind = TablesSelect.indexOf(att.GetTableAtt().toUpperCase());
				    				long tailleAtt = att.GetTailleAtt();
				    				TailleSelectTab.setElementAt(TailleSelectTab.elementAt(ind)+tailleAtt, ind);
				    				break;
				    			}				
				    	}
			    }		
		
		long TailleDimPS;
		Vector<Long> TailleSelect = new Vector<Long>();
		
		/** Calcul des infos des tables de dimensions **/
		for(int i=0; i<TablesSelect.size(); i++) 
		{   
			//on recupere la table de dimension
			int index = this.compte.indexTables.indexOf(TablesSelect.elementAt(i).toUpperCase()); 
			Table Dim = this.compte.getTables().elementAt(index);
			
			//on recupere le nbr de tuples de la table de dimension
			long nbTupDim = Dim.getNb_Tuple();
			//System.out.println("nb tuples de "+Dim.getNomTable()+" initiale = "+ nbTupDim);
			
	        //on calcul la taille du fragment de la table de dimension
			double tailleDim = nbTupDim * Dim.getTailleTuple();
			//System.out.println("taille frag Dimension = "+ tailleFragDim);
			
			//on calcul la taille du fragment de dimension en Pages Ssystems 
			TailleDimPS = (long) Math.ceil(tailleDim / AGexemple.PS);
			//System.out.println("taille frag dimension en PS = "+ TailleDimPS);	
		
			//Calcul du facteur de selectivit� de chaque table de dimension
	//on divise le nbr de tuples selectionn�s de la jointure entre la dimension et le fait sur le nb tup Dim * nb Tup Fait
				double selDF = 1.0;

				//System.out.println("Contient des attributs dans le Select");
				VnbTupDim.add(nbTupDim); TdimPS.add(TailleDimPS);
				double fs = (selDF * (double) nbTupFragFait) / (double)(nbTupDim * nbTupFragFait);
				//System.out.println("Facteur de selectivit� du fragment de "+Dim.getNomTable()+" = "+fs);
				FS.add(fs);
				System.out.println("Dim Select = "+Dim.getNomTable());
				TailleSelect.add(TailleSelectTab.elementAt(TablesSelect.indexOf(Dim.getNomTable())));
			          
			/*System.out.println();
			System.out.println("*****************************");
			System.out.println();*/
		}		
		
		if(FS.size()==0) {System.out.println();
		                  System.out.println("Cout de selection de la requete Join=0 sur se Sous Schema = "+ 0);
		                  System.out.println();
		                  return(0);
		                 }
		
		/** Cout de la lere jointure **/		
		
		//on cherche la dimension ayant le facteur de selectivit� minimum
		int iMin = 0; double Min = FS.elementAt(0);
		for(int j=1; j<FS.size(); j++)
			if (FS.elementAt(j) < Min) {Min = FS.get(j); iMin=j;}
		
		//on calcul le cout de la 1ere jointure : taille PS du fait + taille PS de Dmin
		double Cpj = 3 * (TailleFaitPS + TdimPS.elementAt(iMin));
		//System.out.println("Cout de la 1ere jointure = "+ Cpj);
		
		//Cout du resultat intermediaire de la 1ere jointure
		Long PS = fait.getTailePS(); long B = 60;
		
		Long nbTupRES = nbTupFragFait;
		Long TailleSel = fait.getTailleTuple() + TailleSelect.elementAt(iMin);
		Long TailleResPS = nbTupRES * TailleSel / PS; 
		
		FS.removeElementAt(iMin);
		TdimPS.removeElementAt(iMin);
		VnbTupDim.removeElementAt(iMin);
		TailleSelect.removeElementAt(iMin);
		
		//calcul du co�t du resultat intermediaire
		double Cri = 0;
		while( FS.size() > 0 ) 
		{			
			//on cherche la dimension ayant le FS minimum
			iMin = 0; Min = FS.elementAt(0);
			for(int j=1; j<FS.size(); j++) 
	        if (FS.elementAt(j) < Min) {Min = FS.get(j); iMin = j;}
			
			Cri += ( 2 * TDisp(B, TailleResPS) * (TailleResPS - (B+1)) ) + 3 * TdimPS.elementAt(iMin);
	
			TailleSel+=TailleSelect.elementAt(iMin);
			TailleResPS = nbTupRES * (TailleSel) / PS;
			
			FS.removeElementAt(iMin);
			TdimPS.removeElementAt(iMin);
			VnbTupDim.removeElementAt(iMin);
			TailleSelect.removeElementAt(iMin);
		}
		//System.out.println("Cout du Resultat intermediare = "+Cri);
		
		//Calcul du cout des groupements et des agregations
		double Cga = 0;
		if(Q.AG && Q.Group) {//System.out.println("existe AG et Group");
		                     Cga = 4 * TDisp(B, TailleResPS) * (TailleResPS - (B+1)) ;
		                     }
		else if(Q.AG || Q.Group) {//System.out.println("existe AG ou Group");
		                          Cga = 2 * TDisp(B, TailleResPS) * (TailleResPS - (B+1)) ;
		                          }
		//System.out.println("Cout du groupement et des agregations = "+ Cga);
		
		double cost = Cpj + Cri+ Cga;
		/*System.out.println();
		System.out.println("Cout de la requete sur se Sous Schema = "+ cost);
		System.out.println();*/
		
		return (cost);
    }

	private Vector<Object> ConstrSDpred(Attribut att) 
    {    	
    	cptCloseConnect++;
  	   Table fait = this.compte.getTableFaits();
  	   Statement st = null; ResultSet rs;	   
  	   Long nbTatt = null; 
  	   String s=""; 
  	   if(cptCloseConnect==100){ this.compte.fermerConnexion(); this.compte.seConnecter(); cptCloseConnect=0;}
  	   Connection cn = this.compte.getConnection(); 
  	   
    	Vector<Object> V = new Vector<Object>();
    	
    /** si les valeurs sont des entiers **/
    if (att.type.equals("entier")) {    		//System.out.println("Valeurs enti�res :");
    
    Vector<Intervalle> SD = new Vector<Intervalle>();
    int max = 0;
    //on recupere le MAX de l'attribut
    int indAtt = this.indexAttributs.indexOf(att.GetNomAtt());
    
    if(indAtt!=-1) //si c un attr de frag
    {
    	max = ((Intervalle)this.SouDomAttrCod.elementAt(indAtt).firstElement()).sup;
    	for(int i=0; i<this.SouDomAttrCod.elementAt(indAtt).size(); i++)
    		if(((Intervalle)this.SouDomAttrCod.elementAt(indAtt).elementAt(i)).sup > max)
    			max= ((Intervalle)this.SouDomAttrCod.elementAt(indAtt).elementAt(i)).sup;
    }
    else try {
    	
    	st = cn.createStatement();
    	rs = st.executeQuery("select max("+att.GetNomAtt()+") from "+att.GetTableAtt());
    	if(rs.next()) max= Integer.parseInt(rs.getString("max("+att.GetNomAtt()+")"));
    	
        		} catch (SQLException e) {e.printStackTrace();}
    	
    //on initialise le sous domaines de cet attribut
    Intervalle I = new Intervalle(0,max);
    SD.add(I);
    	        
    //on parcours la liste des predicats(valeurs et operateurs)
    for(int j=0; j<att.valeurs.size(); j++) 
    {    	        	
    int val = Integer.parseInt(String.valueOf(att.valeurs.elementAt(j)));
    int k;
    	        	
    //si l'operateur est >=
    if(att.operateurs.elementAt(j).equals(">="))

    //on recherche l'intervalle k correspendant � la valeure
    for(k=0; k<SD.size(); k++) 	if(SD.elementAt(k).sup < val) {SD.removeElementAt(k); k--;}
    						   	else if(SD.elementAt(k).sup >= val) SD.elementAt(k).inf = val;
                                  
    
    //si l'operateur est <=
    if(att.operateurs.elementAt(j).equals("<="))

    //on recherche l'intervalle k correspendant � la valeure
    for(k=SD.size()-1; k>=0; k--) 	if(SD.elementAt(k).inf > val) SD.removeElementAt(k);
    								else if(SD.elementAt(k).inf <= val) SD.elementAt(k).sup = val;
                                             	
  //si l'operateur est >
    if(att.operateurs.elementAt(j).equals(">"))

    //on recherche l'intervalle k correspendant � la valeure
    for(k=0; k<SD.size(); k++) 	if(SD.elementAt(k).sup <= val) {SD.removeElementAt(k); k--;}
    							else if(SD.elementAt(k).sup > val) SD.elementAt(k).inf = val+1;
                                
  //si l'operateur est <
    if(att.operateurs.elementAt(j).equals("<"))

    //on recherche l'intervalle k correspendant � la valeure
    for(k=SD.size()-1; k>=0; k--) 	if(SD.elementAt(k).inf >= val) SD.removeElementAt(k);
    								else if(SD.elementAt(k).inf < val) SD.elementAt(k).sup = val-1;
                                    
  //si l'operateur est =
    if(att.operateurs.elementAt(j).equals("="))
    	{
    		//on recherche l'intervalle k correspendant � la valeure
    		SD.removeAllElements();
    		SD.add(new Intervalle(val, val));
    		break;
    	} 
    
  //si l'operateur est <>
	else if(att.operateurs.elementAt(j).equals("<>")) 
	{
		
	for(k=0; k<SD.size(); k++) if(SD.elementAt(k).inf <= val && SD.elementAt(k).sup >= val) break;
	
	if(k<SD.size())    
		//1er cas : val < borne sup et val > borne inf	
		if(val > SD.elementAt(k).inf && val < SD.elementAt(k).sup) 
		    {	
			 I = new Intervalle(val+1, SD.elementAt(k).sup);
			 SD.elementAt(k).sup = val-1;
			 if(k < SD.size()-1) SD.insertElementAt(I, k+1);
			 else SD.add(I);
		    }
			
		//2eme cas : val = borne inf et borne inf < borne sup
	    else if(val == SD.elementAt(k).inf && SD.elementAt(k).inf < SD.elementAt(k).sup) 
	    
				 SD.elementAt(k).inf = val+1;
		
		//3eme cas : val = borne sup et borne inf < borne sup
	    else if(val == SD.elementAt(k).sup && SD.elementAt(k).inf < SD.elementAt(k).sup) 
	    
				 SD.elementAt(k).sup = val-1;
	}
   }//fin parcours des valeurs    	        
      V = (Vector<Object>) SD.clone();
      
    	}//fin verif type = entier
    
  /** si les valeurs sont des reels **/
    if (att.type.equals("reel")) 
    {                                       		//System.out.println("Valeurs reels :");    
    Vector<IReel> SD = new Vector<IReel>();
    double max = 0.0;
    
    //on recupere le MAX de l'attribut
    int indAtt = this.indexAttributs.indexOf(att.GetNomAtt());
    if(indAtt!=-1) 
    {
    	max = ((IReel)this.SouDomAttrCod.elementAt(indAtt).firstElement()).sup;
    	for(int i=0; i<this.SouDomAttrCod.elementAt(indAtt).size(); i++)
    		if(((IReel)this.SouDomAttrCod.elementAt(indAtt).elementAt(i)).sup > max)
    			max= ((IReel)this.SouDomAttrCod.elementAt(indAtt).elementAt(i)).sup;
    }
    else try {    	
    			st = cn.createStatement();
    			rs = st.executeQuery("select max("+att.GetNomAtt()+") from "+att.GetTableAtt());
    			if(rs.next()) max= Double.parseDouble(rs.getString("max("+att.GetNomAtt()+")"));
    	
        		} catch (SQLException e) {e.printStackTrace();}
    	
    //on initialise le sous domaines de cet attribut
    IReel I = new IReel(0.0,max);
    SD.add(I);
    	        
    /** on parcours la liste des predicats(valeurs et operateurs) **/
    for(int j=0; j<att.valeurs.size(); j++) 
    {    	        	
    double val = Double.parseDouble(String.valueOf(att.valeurs.elementAt(j)));
    int k;
    	        	
    //si l'operateur est >=
    if(att.operateurs.elementAt(j).equals(">="))

    //on recherche l'intervalle k correspendant � la valeure
    for(k=0; k<SD.size(); k++) {if(SD.elementAt(k).sup < val) {SD.removeElementAt(k); k--;}
    							else if(SD.elementAt(k).sup >= val) SD.elementAt(k).inf = val;
                               }   
    
    //si l'operateur est <=
    if(att.operateurs.elementAt(j).equals("<="))

    //on recherche l'intervalle k correspendant � la valeure
    for(k=SD.size()-1; k>=0; k--) {	if(SD.elementAt(k).inf > val) SD.removeElementAt(k);
    								else if(SD.elementAt(k).inf <= val) SD.elementAt(k).sup = val;
                                  }           	
  //si l'operateur est >
    if(att.operateurs.elementAt(j).equals(">"))

    //on recherche l'intervalle k correspendant � la valeure
    for(k=0; k<SD.size(); k++) {if(SD.elementAt(k).sup <= val) {SD.removeElementAt(k); k--;}
    							else if(SD.elementAt(k).sup > val) SD.elementAt(k).inf = Math.nextUp(val);
                               } 
  //si l'operateur est <
    if(att.operateurs.elementAt(j).equals("<"))

    //on recherche l'intervalle k correspendant � la valeure
    for(k=SD.size()-1; k>=0; k--) { if(SD.elementAt(k).inf >= val) SD.removeElementAt(k);
    								else if(SD.elementAt(k).inf < val) SD.elementAt(k).sup = (val-Math.nextUp(val-1))+(val-1);
                                  }   
  //si l'operateur est =
    if(att.operateurs.elementAt(j).equals("="))
    	{
    		//on recherche l'intervalle k correspendant � la valeure
    		SD.removeAllElements();
    		SD.add(new IReel(val, val));
    		break;
    	} 
    
  //si l'operateur est <>
	else if(att.operateurs.elementAt(j).equals("<>")) 
	{		
	for(k=0; k<SD.size(); k++) if(SD.elementAt(k).inf <= val && SD.elementAt(k).sup >= val) break;
	
	if(k<SD.size())
		//1er cas : val < borne sup et val > borne inf
		if(val > SD.elementAt(k).inf && val < SD.elementAt(k).sup) 
		    {	
			 I = new IReel(Math.nextUp(val), SD.elementAt(k).sup);
			 SD.elementAt(k).sup = (val-Math.nextUp(val-1))+(val-1);
			 if(k < SD.size()-1) SD.insertElementAt(I, k+1);
			 else SD.add(I);
		    }
			
		//2eme cas : val = borne inf et borne inf < borne sup
	    else if(val == SD.elementAt(k).inf && SD.elementAt(k).inf < SD.elementAt(k).sup) 
	    
				 SD.elementAt(k).inf = Math.nextUp(val);
		
		//3eme cas : val = borne sup et borne inf < borne sup
	    else if(val == SD.elementAt(k).sup && SD.elementAt(k).inf < SD.elementAt(k).sup) 
	    
				 SD.elementAt(k).sup = (val-Math.nextUp(val-1))+(val-1);
		
   }//fin op <>
   }//fin parcours des valeurs de l'attribut 
    
    V = (Vector<Object>) SD.clone();
    	}//fin verif type = reel
    
    return V;
    }

    
	private Vector<Object> VectHistPred(Vector<Object> HistSdAttr, Vector<Object> SDpred, String type)
    {
    	if(HistSdAttr == null) return SDpred; // cas du 1er I pred
    	else if (SDpred.size()==0) return SDpred; //cas pred n'accedant a aucun I
    	else if(type.equals("entier"))    			
    				for(int i=0; i<SDpred.size(); i++)
    				{
    					Intervalle I = (Intervalle) SDpred.elementAt(i);
    					int inf = I.inf, sup = I.sup; boolean modif = false; 
    					for(int j=0; j<HistSdAttr.size(); j++)
    					{
    					  Intervalle I2 = (Intervalle) HistSdAttr.elementAt(j);
    					  if(inf>=I2.inf && inf<=I2.sup) //I.inf inclue ds un I
    						  if(sup<=I2.sup) {modif = true; break;} //cas I inclue dans un I de l'historique
    						  
    						  else if(j<HistSdAttr.size()-1) //s'il ya au moins un I apr�s
    						  	   {
    							  	int s = j+1;   							  	
    							  	do
    							  	  {
    							  		Intervalle I3 = (Intervalle) HistSdAttr.elementAt(s);
    							  		if(I3.sup<=sup) HistSdAttr.removeElementAt(s);
    							  		else if(I3.inf<=sup) {I2.sup = I3.sup; HistSdAttr.removeElementAt(s); 
    							  		                      modif = true; break;
    							  		                      }
    							  		     else {I2.sup = sup; modif = true; break;} 
    							  	   } while(s<HistSdAttr.size());
    							  	if(!modif){I2.sup=sup; modif=true; break;} //si on a fait que supr sans trouv� d'I superieur
    							  	else break;
    						  	   }
    						       
    						       else //si c le derniar sd
    							        {I2.sup = sup; modif = true;}
    					}
    					if(!modif) //si I.inf n'est inclue ds aucun intervalle : cherch le 1er I plus grand
    						for(int j=0; j<HistSdAttr.size(); j++)
        					{
        					  Intervalle I2 = (Intervalle) HistSdAttr.elementAt(j);
        					  if(inf < I2.inf) 
        						  if(j+1<HistSdAttr.size())
        						  for(int h=j+1; h<HistSdAttr.size(); h++)
        						  {
        							  Intervalle I3 = (Intervalle) HistSdAttr.elementAt(h);
                					  if(I3.inf <= sup && sup <= I3.sup) //Interval du sup
                					  if(j>0) //si l'I d'avant suit direct celui ci
                					  {
                						   Intervalle I4 = (Intervalle) HistSdAttr.elementAt(j-1);
                						   if(I4.sup==inf-1) 
                						   { I4.sup = I3.sup;
                						     for(int cpt=1; cpt<=h-(j-1); cpt++) HistSdAttr.removeElementAt(j);
                						     modif = true;
                						   }                						    
                					  }
                					  if(!modif) {
                						           I2.inf = inf; I2.sup = I3.sup;
                						           for(int cpt=1; cpt<=h-j; cpt++) HistSdAttr.removeElementAt(j+1);
                						           modif = true;
                					             }
                					  if(modif) break;
                					  
                					  if(h==HistSdAttr.size()-1) //cas ou I doit etre inser� ds sa pos 
                					  { HistSdAttr.insertElementAt(I, j); modif=true;}
        						  }
        						  else //cas ou c le dernier I ayant I.inf > inf
        							   {
        							    if(I2.inf-1 <= sup) { I2.inf = inf;
        							  	                      if(I2.sup < sup) I2.sup = sup; 
        							    					}
        							    else HistSdAttr.insertElementAt(I, j); //inserer l'I ds sa position 
        							  	modif = true;        							  	
        							   }       					  
        					  
        					  if(modif) break; //si modif apport�e alors quitter la bcle
        					}
    					if(!modif) //c le plus grand I : alors l'ajout� en fin de vecteur
    						HistSdAttr.add(I);
    				}
    	
    	else if(type.equals("reel"))   			
				for(int i=0; i<SDpred.size(); i++)
				{
					IReel I = (IReel) SDpred.elementAt(i);
					double inf = I.inf, sup = I.sup; boolean modif = false; 
					for(int j=0; j<HistSdAttr.size(); j++)
					{
						IReel I2 = (IReel) HistSdAttr.elementAt(j);
					  if(inf>=I2.inf && inf<=I2.sup) //I.inf inclue ds un I
						  if(sup<=I2.sup) {modif = true; break;} //cas I inclue dans un I de l'historique
						  
						  else if(j<HistSdAttr.size()-1) //s'il ya au moins un I apr�s
						  	   {
							  	int s = j+1;   							  	
							  	do
							  	  {
							  		IReel I3 = (IReel) HistSdAttr.elementAt(s);
							  		if(I3.sup<=sup) HistSdAttr.removeElementAt(s);
							  		else if(I3.inf<=sup) {I2.sup = I3.sup; HistSdAttr.removeElementAt(s); 
							  		                      modif = true; break;
							  		                      }
							  		     else {I2.sup = sup; modif = true; break;} 
							  	   } while(s<HistSdAttr.size());
							  	if(!modif){I2.sup=sup; modif=true; break;} //si on a fait que supr sans trouv� d'I superieur
							  	else break;
						  	   }
						       
						       else //si c le derniar sd
							        {I2.sup = sup; modif = true;}
					}
					if(!modif) //si I.inf n'est inclue ds aucun intervalle : cherch le 1er I plus grand
						for(int j=0; j<HistSdAttr.size(); j++)
    					{
							IReel I2 = (IReel) HistSdAttr.elementAt(j);
    					  if(inf < I2.inf) 
    						  if(j+1<HistSdAttr.size())
    						  for(int h=j+1; h<HistSdAttr.size(); h++)
    						  {
    							  IReel I3 = (IReel) HistSdAttr.elementAt(h);
            					  if(I3.inf <= sup && sup <= I3.sup) //Interval du sup
            					  if(j>0) //si l'I d'avant suit direct celui ci
            					  {
            						  IReel I4 = (IReel) HistSdAttr.elementAt(j-1);
            						   if(I4.sup==inf-1) 
            						   { I4.sup = I3.sup;
            						     for(int cpt=1; cpt<=h-(j-1); cpt++) HistSdAttr.removeElementAt(j);
            						     modif = true;
            						   }                						    
            					  }
            					  if(!modif) {
            						           I2.inf = inf; I2.sup = I3.sup;
            						           for(int cpt=1; cpt<=h-j; cpt++) HistSdAttr.removeElementAt(j+1);
            						           modif = true;
            					             }
            					  if(modif) break;
            					  
            					  if(h==HistSdAttr.size()-1) //cas ou I doit etre inser� ds sa pos 
            					  { HistSdAttr.insertElementAt(I, j); modif=true;}
    						  }
    						  else //cas ou c le dernier I ayant I.inf > inf
    							   {
    							    if(I2.inf-1 <= sup) { I2.inf = inf;
    							  	                      if(I2.sup < sup) I2.sup = sup; 
    							    					}
    							    else HistSdAttr.insertElementAt(I, j); //inserer l'I ds sa position 
    							  	modif = true;        							  	
    							   }       					  
    					  
    					  if(modif) break; //si modif apport�e alors quitter la bcle
    					}
					if(!modif) //c le plus grand I : alors l'ajout� en fin de vecteur
						HistSdAttr.add(I);
				}
    	return HistSdAttr;
    }    

    
	private Long nbTupVectHistPred(Vector<Vector<Integer>> C, Attribut a, Vector<Object> HistSdAttr)
    {
    	cptCloseConnect++;
 	   Table fait = this.compte.getTableFaits();
 	   Statement st = null; ResultSet rs;	   
 	   Long nbTatt = null; 
 	   String s=""; 
 	   if(cptCloseConnect==100){ this.compte.fermerConnexion(); this.compte.seConnecter(); cptCloseConnect=0;}
 	   Connection cn = this.compte.getConnection(); 
 	   
 	   /** on ecrit les pred de selection concernant le ss schema **/
	   for(int is=0; is<SouDomAttrCod.size(); is++)
	       {
	    	if(is==0) s+="(";
	    	if(is>= 1) s+= ") and ("; int cp=0;
	    for(int ia=0; ia<SouDomAttrCod.elementAt(is).size(); ia++)
	    	if(C.elementAt(is).elementAt(ia)==1) {	
	    	
	    	 String sd=""; if(cp>=1) s+=" or ";
	    	
	    	if(AttributCodage.elementAt(is).type.equals("string")) 
	    	   {sd =  (String) SouDomAttrCod.elementAt(is).elementAt(ia);
	    	    if(sd.indexOf("IN ")!=-1) sd=sd.substring(0,sd.indexOf("'")).trim()+" ("
	    	                                +sd.substring(sd.indexOf("'")).trim()+")"; 	    	
	    	   }
	    	
	    	else if(AttributCodage.elementAt(is).type.equals("entier")) 
	    		sd = ">="+((Intervalle)SouDomAttrCod.elementAt(is).elementAt(ia)).inf+" and "+
	    		AttributCodage.elementAt(is).GetNomAtt()+"<="+((Intervalle)SouDomAttrCod.elementAt(is).elementAt(ia)).sup;
	    	
	    	else if(AttributCodage.elementAt(is).type.equals("reel")) 
	    		sd = ">="+((IReel)SouDomAttrCod.elementAt(is).elementAt(ia)).inf+" and "+
	    		AttributCodage.elementAt(is).GetNomAtt()+"<="+((IReel)SouDomAttrCod.elementAt(is).elementAt(ia)).sup;
	    	
	    	s+=AttributCodage.elementAt(is).GetNomAtt()+" "+sd;
	    	cp++;
	    	                                      }
          }
	   
	   s += ")";
 	   
 	  /** on ecrit les pred des Intervalles de l'attribut **/
 	  int cp=0; s+=" and (";
 	  for(int is=0; is<HistSdAttr.size(); is++)
 	    	{	 	    	
 		  		String sd=""; if(cp>=1) s+=" or ";
 	    	
 	    	 	if(a.type.equals("entier")) 
 	    	 		sd = ">="+((Intervalle)HistSdAttr.elementAt(is)).inf+" and "+
 	    	 		a.GetNomAtt()+"<="+((Intervalle)HistSdAttr.elementAt(is)).sup;
 	    	
 	    		else if(a.type.equals("reel")) 
 	    				sd = ">="+((IReel)HistSdAttr.elementAt(is)).inf+" and "+
 	    				a.GetNomAtt()+"<="+((IReel)HistSdAttr.elementAt(is)).sup;
 	    	
 	    		s+= a.GetNomAtt()+" "+sd;
 	    		cp++;
 	    	}
 	  s+=" )";	  
 	  
 	 /** ecriture des pred de joint entre le fait et les tables candidates**/
	    String join=""; String tables="";
	    for(int y=0; y<tablesCandidates.size(); y++)
	    {   
	    	if(y>=1) join+=" and ";
	      Table T = tablesCandidates.elementAt(y);
	      tables+=", "+T.getNomTable();
	      join+=fait.getNomTable()+"."+T.getFKfait()+"="+T.getNomTable()+"."+T.getPK();	
	    }
	    
	    /**ajout de la join avc la table de l'attribut de la requete**/	    
        if(!this.indexTables.contains(a.GetTableAtt()))  
	    { Table TattReq = this.compte.getTables().elementAt(this.compte.indexTables.indexOf(a.GetTableAtt()));
          tables+=", "+TattReq.getNomTable();
          join+=" and "+fait.getNomTable()+"."+TattReq.getFKfait()+"="+TattReq.getNomTable()+"."+TattReq.getPK();
        }
 	   
 	  try {
 			st = cn.createStatement();
 	        
 			/*System.out.println("select count(*) from "+fait.getNomTable()+tables+
		             		   " where "+join+" and ("+s+" )");*/
 			rs = st.executeQuery("select count(*) from "+fait.getNomTable()+tables+
          		   				 " where "+join+" and ("+s+" )");
 			if(rs.next())
 			nbTatt = Long.parseLong(rs.getString("count(*)"));
 			
 			st.close(); rs.close();		
 		   } catch (SQLException e) {e.printStackTrace();}
 		 
 	return nbTatt;
    }

    
	private double nbTupAttrStr(Vector<Vector<Integer>> C, Vector<String> Hist, Attribut a)
    {
    	Vector<String> V = new Vector<String>(a.valeurs.size());
    	
    	if(Hist.size()==0) 
    	{    		
    		for(int i=0; i<a.valeurs.size(); i++)
    			if(a.operateurs.elementAt(i).equals("IN"))
    				 V.add(a.operateurs.elementAt(i)+" ("+a.valeurs.elementAt(i)+")");
    			else V.add(a.operateurs.elementAt(i)+" "+a.valeurs.elementAt(i));
    		Hist = (Vector<String>) V.clone();
    	}
    	
    	else //Hist non vide
    	{
    	   if(a.operateurs.firstElement().equals("="))
    		   {
    		   	int i;
    		   	for(i=0; i<Hist.size(); i++) 
    			   if(!Hist.elementAt(i).contains("<>") && Hist.elementAt(i).contains((String)a.valeurs.firstElement()))
    					   return 0.0;
    			if(i==Hist.size()) {Hist.add("="+" "+a.valeurs.firstElement()); V.add(Hist.lastElement());}
    		   }
    	   else if(a.operateurs.firstElement().equals("<>"))
    	   		{
   		   			int i;
   		   			for(i=0; i<Hist.size(); i++) 
   		   			if(Hist.elementAt(i).contains("<>") && Hist.elementAt(i).contains((String)a.valeurs.firstElement()))
   					   return 0.0;
   		   			if(i==Hist.size()) {Hist.add("<>"+" "+a.valeurs.firstElement()); V.add(Hist.lastElement());}
    	   		}
    	   
    	   else if(a.operateurs.firstElement().equals("IN"))
	   			{
    		   		String valAtt = (String) a.valeurs.firstElement();
    		   		int p = valAtt.indexOf("'"), n = valAtt.indexOf(",");
		   			while(n!=-1) 
		   			{
		   				int i; String val = valAtt.substring(p,n);
		   				for(i=0; i<Hist.size(); i++) 
		   				if(!Hist.elementAt(i).contains("<>") && Hist.elementAt(i).contains(val)) break;
		   				if(i==Hist.size()) {Hist.add("="+" "+val); V.add(Hist.lastElement());}
		   				p=n+1; n = valAtt.indexOf(",",p);
		   			}
		   			if(n==-1)
		   			{
		   				int i; String val = valAtt.substring(p,n);
		   				for(i=0; i<Hist.size(); i++) 
		   				if(!Hist.elementAt(i).contains("<>") && Hist.elementAt(i).contains(val)) break;
		   				if(i==Hist.size()) {Hist.add("="+" "+val); V.add(Hist.lastElement());}
		   			}
	   			}
    	}
    	
    	cptCloseConnect++;
  	   Table fait = this.compte.getTableFaits();
  	   Statement st = null; ResultSet rs;	   
  	   Long nbTatt = null; 
  	   String s=""; 
  	   if(cptCloseConnect==100){ this.compte.fermerConnexion(); this.compte.seConnecter(); cptCloseConnect=0;}
  	   Connection cn = this.compte.getConnection(); 
  	   
  	   /** on ecrit les pred de selection concernant le ss schema **/
 	   for(int is=0; is<SouDomAttrCod.size(); is++)
 	       {
 	    	if(is==0) s+="(";
 	    	if(is>= 1) s+= ") and ("; int cp=0;
 	    for(int ia=0; ia<SouDomAttrCod.elementAt(is).size(); ia++)
 	    	if(C.elementAt(is).elementAt(ia)==1) {	
 	    	
 	    	 String sd=""; if(cp>=1) s+=" or ";
 	    	
 	    	if(AttributCodage.elementAt(is).type.equals("string")) 
 	    	   {sd =  (String) SouDomAttrCod.elementAt(is).elementAt(ia);
 	    	    if(sd.indexOf("IN ")!=-1) sd=sd.substring(0,sd.indexOf("'")).trim()+" ("
 	    	                                +sd.substring(sd.indexOf("'")).trim()+")"; 	    	
 	    	   }
 	    	
 	    	else if(AttributCodage.elementAt(is).type.equals("entier")) 
 	    		sd = ">="+((Intervalle)SouDomAttrCod.elementAt(is).elementAt(ia)).inf+" and "+
 	    		AttributCodage.elementAt(is).GetNomAtt()+"<="+((Intervalle)SouDomAttrCod.elementAt(is).elementAt(ia)).sup;
 	    	
 	    	else if(AttributCodage.elementAt(is).type.equals("reel")) 
 	    		sd = ">="+((IReel)SouDomAttrCod.elementAt(is).elementAt(ia)).inf+" and "+
 	    		AttributCodage.elementAt(is).GetNomAtt()+"<="+((IReel)SouDomAttrCod.elementAt(is).elementAt(ia)).sup;
 	    	
 	    	s+=AttributCodage.elementAt(is).GetNomAtt()+" "+sd;
 	    	cp++;
 	    	                                      }
           }
 	   
 	   s += ")";
  	   
  	  /** on ecrit les pred de l'attribut **/
  	  int cp=0; s+=" and (";
  	  for(int is=0; is<V.size(); is++)
  	    	{	 	    	
  		  		if(cp>=1) s+=" or ";
  		  		
  		  		s += a.GetNomAtt()+" "+V.elementAt(is);
  	    		cp++;
  	    	}
  	  s+=" )";
  	   
  	/** ecriture des pred de joint entre le fait et les tables candidates**/
	    String join=""; String tables="";
	    for(int y=0; y<tablesCandidates.size(); y++)
	    {   
	    	if(y>=1) join+=" and ";
	      Table T = tablesCandidates.elementAt(y);
	      tables+=", "+T.getNomTable();
	      join+=fait.getNomTable()+"."+T.getFKfait()+"="+T.getNomTable()+"."+T.getPK();	
	    }
	    
	    /**ajout de la join avc la table de l'attribut de la requete**/	    
	    if(!this.indexTables.contains(a.GetTableAtt()))  
	    { Table TattReq = this.compte.getTables().elementAt(this.compte.indexTables.indexOf(a.GetTableAtt()));
          tables+=", "+TattReq.getNomTable();
          join+=" and "+fait.getNomTable()+"."+TattReq.getFKfait()+"="+TattReq.getNomTable()+"."+TattReq.getPK();
        }
	   
	  try {
			st = cn.createStatement();
	        
			System.out.println("select count(*) from "+fait.getNomTable()+tables+
	   							" where "+join+" and ("+s+" )");
			rs = st.executeQuery("select count(*) from "+fait.getNomTable()+tables+
        		   				 " where "+join+" and ("+s+" )");
  			if(rs.next())
  			nbTatt = Long.parseLong(rs.getString("count(*)"));
  			
  			st.close(); rs.close();		
  		   } catch (SQLException e) {e.printStackTrace();}
  		 
  	return nbTatt;
    }
    
    
	private boolean Validit�PredSD(Vector<Vector<Integer>> C, Attribut a, Object SD)
    {
    	cptCloseConnect++;
  	   Table fait = this.compte.getTableFaits();
  	   Statement st = null; ResultSet rs;	   
  	   Long nbTatt = null; 
  	   String s=""; 
  	   if(cptCloseConnect==100){ this.compte.fermerConnexion(); this.compte.seConnecter(); cptCloseConnect=0;}
  	   Connection cn = this.compte.getConnection(); 
  	   
  	   /** on ecrit les pred de selection concernant le ss schema **/
 	   for(int is=0; is<SouDomAttrCod.size(); is++)
 	       {
 	    	if(is==0) s+="(";
 	    	if(is>= 1) s+= ") and ("; int cp=0;
 	    for(int ia=0; ia<SouDomAttrCod.elementAt(is).size(); ia++)
 	    	if(C.elementAt(is).elementAt(ia)==1) {	
 	    	
 	    	 String sd=""; if(cp>=1) s+=" or ";
 	    	
 	    	if(AttributCodage.elementAt(is).type.equals("string")) 
 	    	   {sd =  (String) SouDomAttrCod.elementAt(is).elementAt(ia);
 	    	    if(sd.indexOf("IN ")!=-1) sd=sd.substring(0,sd.indexOf("'")).trim()+" ("
 	    	                                +sd.substring(sd.indexOf("'")).trim()+")"; 	    	
 	    	   }
 	    	
 	    	else if(AttributCodage.elementAt(is).type.equals("entier")) 
 	    		sd = ">="+((Intervalle)SouDomAttrCod.elementAt(is).elementAt(ia)).inf+" and "+
 	    		AttributCodage.elementAt(is).GetNomAtt()+"<="+((Intervalle)SouDomAttrCod.elementAt(is).elementAt(ia)).sup;
 	    	
 	    	else if(AttributCodage.elementAt(is).type.equals("reel")) 
 	    		sd = ">="+((IReel)SouDomAttrCod.elementAt(is).elementAt(ia)).inf+" and "+
 	    		AttributCodage.elementAt(is).GetNomAtt()+"<="+((IReel)SouDomAttrCod.elementAt(is).elementAt(ia)).sup;
 	    	
 	    	s+=AttributCodage.elementAt(is).GetNomAtt()+" "+sd;
 	    	cp++;
 	    	                                      }
           }
 	   
 	   /**ajout de l'I du SD **/
 	   s += ") and (";
 	   if(a.type.equals("entier"))
 	   {
 		  Intervalle I = (Intervalle)SD;
 		  s+=a.GetNomAtt()+" >= "+I.inf+" and "+a.GetNomAtt()+" <= "+I.sup+")";
 	   }
 	   else if(a.type.equals("reel"))
 	   {
  		  IReel I = (IReel)SD;
  		  s+=a.GetNomAtt()+" >= "+I.inf+" and "+a.GetNomAtt()+" <= "+I.sup+")";
  	   } 
 	   
 	   Vector<Object> SDpred = ConstrSDpred(a);
 	   
  	  /** on ecrit les pred des Intervalles de l'attribut **/
  	  int cp=0; s+=" and (";
  	  for(int is=0; is<SDpred.size(); is++)
  	    	{	 	    	
  		  		String sd=""; if(cp>=1) s+=" or ";
  	    	
  	    	 	if(a.type.equals("entier")) 
  	    	 		sd = ">="+((Intervalle)SDpred.elementAt(is)).inf+" and "+
  	    	 		a.GetNomAtt()+"<="+((Intervalle)SDpred.elementAt(is)).sup;
  	    	
  	    		else if(a.type.equals("reel")) 
  	    				sd = ">="+((IReel)SDpred.elementAt(is)).inf+" and "+
  	    				a.GetNomAtt()+"<="+((IReel)SDpred.elementAt(is)).sup;
  	    	
  	    		s+= a.GetNomAtt()+" "+sd;
  	    		cp++;
  	    	}
  	  s+=" )";	  
  	  
  	 /** ecriture des pred de joint entre le fait et les tables candidates**/
 	    String join=""; String tables="";
 	    for(int y=0; y<tablesCandidates.size(); y++)
 	    {   
 	    	if(y>=1) join+=" and ";
 	      Table T = tablesCandidates.elementAt(y);
 	      tables+=", "+T.getNomTable();
 	      join+=fait.getNomTable()+"."+T.getFKfait()+"="+T.getNomTable()+"."+T.getPK();	
 	    }
 	    
 	    /**ajout de la join avc la table de l'attribut de la requete**/	    
         if(!this.indexTables.contains(a.GetTableAtt()))  
 	    { Table TattReq = this.compte.getTables().elementAt(this.compte.indexTables.indexOf(a.GetTableAtt()));
           tables+=", "+TattReq.getNomTable();
           join+=" and "+fait.getNomTable()+"."+TattReq.getFKfait()+"="+TattReq.getNomTable()+"."+TattReq.getPK();
         }
  	   
  	  try {
  			st = cn.createStatement();
  	        
  			/*System.out.println("select count(*) from "+fait.getNomTable()+tables+
 		             		   " where "+join+" and ("+s+" )");*/
  			rs = st.executeQuery("select count(*) from "+fait.getNomTable()+tables+
           		   				 " where "+join+" and ("+s+" )");
  			if(rs.next())
  			nbTatt = Long.parseLong(rs.getString("count(*)"));
  			
  			if(nbTatt>0) return true;
  			
  			st.close(); rs.close();		
  		   } catch (SQLException e) {e.printStackTrace();}
  		 
  	return false;
    }
    
	private double TailleConfIJB(IChromosome individu) 
	{
		HashSet<Integer> HS = new HashSet<Integer>();
		long RowID = 1;
	    long CardAttrs = 0;
		CompositeGene comp;

		// calcul du nombre d'attributs index�s et d'index du schema 
		for (int i = 0; i < individu.size(); i++) 
		{
			comp = (CompositeGene) individu.getGene(i);
			if(((Integer) ((IntegerGene) comp.geneAt(nbrSsDomParAttr.elementAt(i))).getAllele()).intValue()>0)
			  {	
				CardAttrs += AttributCodage.elementAt(i).getCardinalite(); 
			  	HS.add(((Integer)((IntegerGene)comp.geneAt(nbrSsDomParAttr.elementAt(i))).getAllele()).intValue());
			  }
		}		
			double Taille = ((HS.size() * RowID + CardAttrs) * this.compte.getTableFaits().getNb_Tuple())/ 8;
			if(Taille==0) Taille = 1.0;
			
			System.out.println("Taille conf IJB : "+Taille+" nbCardAttrs = "+CardAttrs);
						
			return Taille;
	}

	private double TailleConfVM(IChromosome individu) 
	{
		double Taille = 0; //en octects
		CompositeGene comp;		 

		// calcul du nombre de fragments de ce shema
		for (int i = 0; i < individu.size(); i++) 
		{
			comp = (CompositeGene) individu.getGene(i);
			int nbr =  nbrSsDomParAttr.elementAt(i);
				
			for (int j = 0; j < nbr; j++) 
			{
				if(((Integer) ((IntegerGene) comp.geneAt(j+nbr+1)).getAllele()).intValue()>0)
				Taille += (this.SelFait.elementAt(i).elementAt(j) * this.compte.getTableFaits().getNb_Tuple())
							* this.compte.getTableFaits().getTailleTuple();
			}
		}
		System.out.println("Taille conf VM : "+Taille);
		
		// System.out.println("Non Admissible : "+nbFrag);
		return Taille;
	}
}