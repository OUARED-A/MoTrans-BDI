package Environnement;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JOptionPane;

import org.jgap.IChromosome;
import org.jgap.impl.CompositeGene;
import org.jgap.impl.IntegerGene;

import Environnement.AGEN.AGexemple;

public class FH {
	private Compte CompteUser = null;
	private Vector<Requete> req = null;
	private Vector<Vector<Fragment>> PredFrags = null;
	private Vector<Table> tablesCandidates = null;
	private Vector<Attribut> AttrSelections = null;
    private Vector<Vector> souDomaines = null;
    private Vector<Integer> nbrSsDomaines = null;
    public Vector<Vector<Double>> SelDim = null;
	public Vector<Vector<Double>> SelFait = null;	
	private Vector<String> indexTables = null; 
	private Schema SCH = null;
	public static Vector<Integer> nbrAttrParTable = null;
	private Vector<HashSet<String>> FragTables = null;
	private IChromosome BestofBest = null;
	private Vector<Integer> iAttrsFH = null;
	private Vector<Integer> iAttrsIJB = null;
	private Vector<Integer> iAttrsVM = null;
	private Vector<Table> tabsFH = null;
	private Vector<Attribut> AttrDeFrag = null;
    
	/** constructeur dans le cas automatique **/
	public FH(Compte c , Vector<Requete> RQ, boolean FragH, boolean IJB, boolean VM, int getTpopInit, int getnbGen,
		     int getTCrois, int getTMut,  int getTPS, int getTB, int getnbW,
		     int getTSI, int getTVM, int getTRowID) 
	{	
		this.nbrSsDomaines = new Vector<Integer>();
		this.souDomaines = new Vector<Vector>();
		
		this.req = RQ;
		this.CompteUser = c;
		indexTables = new Vector<String>();
		extraireAttrDeFrag();
		extraireTablesCandidates();
		for(int i=0; i<AttrSelections.size(); i++)
			extraireSousDomaines(AttrSelections.elementAt(i).GetNomAtt());
	
	ChangerReste();
	ReordonnerAttributs();
	afficherInfosFH();
	extraireSelectiviteDim();
	extraireSelectiviteFait();
	
	//g�n�r� les indices des attrs utilis�s par les techniques
	Vector<Integer> iAttrsFH = new Vector<Integer>();
	Vector<Integer> iAttrsIJB = new Vector<Integer>();
	Vector<Integer> iAttrsVM = new Vector<Integer>();
	for(int i=0; i<AttrSelections.size(); i++)
	{
		iAttrsFH.add(i);
		iAttrsIJB.add(i);
		iAttrsVM.add(i);
	}	
	CalculNbrAttrParTable();
	AGexemple AG = new AGexemple(this.CompteUser, this.req, this.tablesCandidates, this.AttrSelections, 
              					this.nbrSsDomaines, this.souDomaines, 
              					this.SelDim, this.SelFait, iAttrsFH, iAttrsIJB, iAttrsVM, FragH, IJB, VM,
              					getTpopInit, getnbGen, getTCrois, getTMut,  getTPS, getTB, getnbW, getTSI, getTVM,
              					getTRowID);
   this.SCH = AG.getSCHoptimale();
   this.BestofBest = SCH.codage; 
   
   definirTablesAFragmentees();
   extraireFragmentsTables();  
   identifFrags();
}

/** constructeur dans le ca manuel **/
public FH(	Compte c , Vector<Requete> RQ, Vector<String> tablesCandidates, Vector<String> AttrSelections,
			Vector<Vector> souDomaines, Vector<String> AttrsFH, Vector<String> AttrsIJB, Vector<String> AttrsVM,
			boolean FragH, boolean IJB, boolean VM, int getTpopInit, int getnbGen,
		     int getTCrois, int getTMut,  int getTPS, int getTB, int getnbW,
		     int getTSI, int getTVM, int getTRowID ) 
{	
	System.out.println();
	System.out.println("FH cas manuele");
	System.out.println();
	
	this.req = RQ;
	this.CompteUser = c;
	
	this.tablesCandidates = new Vector<Table>(tablesCandidates.size());
	for(int i=0; i<tablesCandidates.size(); i++)
		this.tablesCandidates.add(c.getTables().elementAt(c.indexTables.indexOf(tablesCandidates.elementAt(i))));
	
	this.AttrSelections = new Vector<Attribut>(AttrSelections.size());
	for(int i=0; i<AttrSelections.size(); i++)
		for(int j=0; j<c.getTabEDD().size(); j++)
			if(c.getTabEDD().elementAt(j).indexAttributs.contains(AttrSelections.elementAt(i)))
				this.AttrSelections.add(c.getTabEDD().elementAt(j).getV_Attributs()
						.elementAt(c.getTabEDD().elementAt(j).indexAttributs.indexOf(AttrSelections.elementAt(i))));
	
	this.souDomaines = new Vector<Vector>();
	this.souDomaines = souDomaines;
	this.nbrSsDomaines = new Vector<Integer>();
	for(int i=0; i<this.souDomaines.size(); i++)
		nbrSsDomaines.add(this.souDomaines.elementAt(i).size());
	
	this.indexTables = tablesCandidates;
	
	ChangerReste();
	ReordonnerAttributs();
	afficherInfosFH();
	extraireSelectiviteDim();
	extraireSelectiviteFait();
	
	//g�n�r� les indices des attrs utilis�s par les techniques
	iAttrsFH = new Vector<Integer>();
	iAttrsIJB = new Vector<Integer>();
	iAttrsVM = new Vector<Integer>();
	for(int i=0; i<AttrSelections.size(); i++)
	{
		if(AttrsFH.contains(AttrSelections.elementAt(i))) iAttrsFH.add(i);
		if(AttrsIJB.contains(AttrSelections.elementAt(i))) iAttrsIJB.add(i);
		if(AttrsVM.contains(AttrSelections.elementAt(i))) iAttrsVM.add(i);
	}
	
	CalculNbrAttrParTable();
   AGexemple AG = new AGexemple	(	this.CompteUser, this.req, this.tablesCandidates, this.AttrSelections, 
	       	                     	this.nbrSsDomaines, this.souDomaines, this.SelDim, this.SelFait,
	       	                     	iAttrsFH, iAttrsIJB, iAttrsVM, FragH, IJB, VM,getTpopInit, getnbGen, getTCrois, 
	       	                     	getTMut,  getTPS, getTB, getnbW, getTSI, getTVM, getTRowID);
   this.SCH = AG.getSCHoptimale();
   this.BestofBest = SCH.codage;   
   
   definirTablesAFragmentees();
   extraireFragmentsTables();  
   identifFrags();
}

private void extraireAttrDeFrag() {
	System.out.println();
	System.out.println("*********Extraire les attributs de fragmentations*********");
	System.out.println();
	
	Statement st =null; ResultSet rs; Connection cn = this.CompteUser.getConnection();
	this.AttrSelections = new Vector<Attribut>();
	
	// on parcours les requetes
	for(int i=0; i<this.req.size(); i++) {
		Requete requete = this.req.elementAt(i);
		System.out.println();
		//on parcours les blocs de OR de la requette
		for(int h=0; h<requete.getBlocsOR().size(); h++) {
			
			Vector<Attribut> BlocPred = requete.getBlocsOR().elementAt(h);
		
			//on parcourt les attributs des blocs de OR
		for(int j=0; j<BlocPred.size(); j++) {
			
			Attribut at = BlocPred.elementAt(j);
			Attribut attribut = new Attribut(at.GetTableAtt().toUpperCase(), at.GetNomAtt().toUpperCase());
			attribut.valeurs = new Vector<Object>();
			attribut.operateurs = new Vector<String>();
			attribut.valeurs.addAll((Vector<Object>) at.valeurs.clone());
			attribut.operateurs.addAll((Vector<String>) at.operateurs.clone());
			
			// on verifie; si l'attribut existe deja dans le vecteur on ajoute seulemet les predicats; 
			// Sinon on ajoute l'attribut 			
			int g;
			for(g=0; g < AttrSelections.size() 
		&& !AttrSelections.elementAt(g).GetNomAtt().equals(attribut.GetNomAtt()); g++);
			
			if (g==AttrSelections.size()) {
				//on recupere les infos sur l'attribut
				int idTab = this.CompteUser.indexTables.indexOf(attribut.GetTableAtt()); 
				int m;
				for(m= 0; m<this.CompteUser.getTables().elementAt(idTab).getV_Attributs().size(); m++)
				if(this.CompteUser.getTables().elementAt(idTab).getV_Attributs().elementAt(m).GetNomAtt().equals(attribut.GetNomAtt()))
					break;
			    Attribut aa =  this.CompteUser.getTables().elementAt(idTab).getV_Attributs().elementAt(m);
			    attribut.setCardinalite(aa.getCardinalite());
				AttrSelections.addElement(attribut);
				System.out.println("attribut : "+attribut.GetNomAtt()+". ajout�");
				
				Attribut a = AttrSelections.lastElement();
				//on parcours les predicats et on supprime les non existants dans la BD
				for(int y=0; y<attribut.valeurs.size(); y++)
				{ int n = 0; String str = "";				  
				String op = a.operateurs.get(y);
		        String val = (String) a.valeurs.get(y);
		        
		        //on verifie la valeur du diff�rent dans la BD
		        if (op.equals("IN")) str = " "+op+" ("+(String) val+")"; 
		        else if(op.equals("<>")) str = "=" +" "+ val;
		        else str = op +" "+ val;
		         		          
				try {
	    				st = cn.createStatement();
	    				rs=st.executeQuery("select count(*) from "+a.GetTableAtt()+" where "+
	    						            attribut.GetNomAtt()+str);
	    				if(rs.next()) { n = Integer.parseInt(rs.getString("count(*)"));}
	    				
	    				}catch(SQLException e) {e.printStackTrace();}
	    				
	    			if(n==0) {AttrSelections.lastElement().valeurs.removeElementAt(y);
	    			          AttrSelections.lastElement().operateurs.removeElementAt(y);
	    			          System.out.println("predicat : "+op+val+" supprim�"); y--; }
				}				
				 if(AttrSelections.lastElement().valeurs.size()==0) AttrSelections.removeElementAt(AttrSelections.size()-1);
			                                }
			
		            //attribut existant
			else { //on ajoute les predicats (valeurs et operateurs) seulement
		    	int nbVals = attribut.valeurs.size();
		    	//System.out.println("attribut : "+attribut.GetNomAtt()+". non ajout�");
		    	System.out.println("attribut existant: "+AttrSelections.elementAt(g).GetNomAtt()+".");
		    	
		    	Vector<Object> Vvaleurs = AttrSelections.elementAt(g).valeurs;
		    	Vector<String> Voperateurs = AttrSelections.elementAt(g).operateurs;
		    	//System.out.print("taille vals = "+Vvaleurs.size());
		    	//System.out.println(" taille op = "+Voperateurs.size());
		    	
		    	for(int k=0; k < nbVals; k++) {
		        	Object val = attribut.valeurs.elementAt(k);
		        	String op  = attribut.operateurs.elementAt(k);
		        	
		        	//on verifie si le predicat existe deja(valeur + operateur) pour l'ajouter ou non
		        	int t;
		        	for(t=0; t<Vvaleurs.size(); t++)
		        		if (Vvaleurs.elementAt(t).equals(val) && Voperateurs.elementAt(t).equals(op)) break;
		        		else if(op.equals("=") && Voperateurs.elementAt(t).equals("IN")) 
		        		{String s = (String)Vvaleurs.elementAt(t), s1 = (String)val;
		        		 if(s.indexOf(s1)!=-1) break;	
		        		}
		        		else if(op.equals("IN")) 
		        		{ int n,p; String s = (String)Vvaleurs.elementAt(t), s1 = (String)val; 
		        		  p = s1.indexOf("'"); n = s1.indexOf(","); 
		        		  while (n!=-1) { String s2 = s1.substring(p,n);
		        		  if(s.indexOf(s2)!=-1) s1=s1.substring(0,p)+s1.substring(n+1);
		        		  else p=n+1; 
		        		  n = s1.indexOf(",",p); 
		        		                 }
		                //cas last elmt
		                if(n==-1) if(s.indexOf(s1.substring(p))!=-1) 
		                	      if(p==0) s1="";
		                	      else s1=s1.substring(0,p-1);
		                if(s1.isEmpty()) break;
		                val = s1;
		                }
		        	
		        	// predicat non existant alors on l'ajoute
		        	if(t==Vvaleurs.size()) 
		        	   { 
		        	   //On verifie si la valeur existe dans la base de donn�es
		        	   if(op.equals("<>")) op = "="; int n = 0;
		             try {String str = op +" "+(String) val; 
		        	          if (op.equals("IN")) str = " "+op +" ("+(String) val+")"; 
		    				st = cn.createStatement();
		    				rs=st.executeQuery("select count(*) from "+attribut.GetTableAtt()+" where "+
		    						            attribut.GetNomAtt()+str);
		    				if(rs.next()) {n = Integer.parseInt(rs.getString("count(*)"));}
		    				
		    			   	  }catch(SQLException e) {e.printStackTrace();}
		    				
		    			if(n>0) {	
		        		System.out.println("pr�dicat "+op+" "+val+" ajout�");
		        		 AttrSelections.elementAt(g).valeurs.add(val);
		        	     AttrSelections.elementAt(g).operateurs.add(op);
		    			         }
		    			else System.out.println("Valeurs "+op+" "+val+" innexistantes dans la Base Donn�es");
		        	   }
		        	else System.out.println("pr�dicat "+op+" "+val+" existant");
		                              }
		    	Vvaleurs = null; Voperateurs=null;
		          }
			
		}
		BlocPred=null;
	}
		requete=null;
	}
	System.gc();
}

private void extraireTablesCandidates() {
	HashSet<Table> ensTables = new HashSet<Table>(); int k;
	indexTables = new Vector<String>();
	
	for(int i=0; i<req.size(); i++) //parcours des requetes 
		
		for(int j=0; j<req.elementAt(i).getBlocsOR().size(); j++) //parcours des blocs de la requete
	       
			for(int y=0; y<req.elementAt(i).getBlocsOR().elementAt(j).size(); y++)
		    { 
		      Attribut A = req.elementAt(i).getBlocsOR().elementAt(j).elementAt(y);
		
	          k=this.CompteUser.indexTables.indexOf(A.GetTableAtt().toUpperCase());
	     
			  ensTables.add(this.CompteUser.getTables().elementAt(k));
	        }	
	tablesCandidates = new Vector<Table>();
	tablesCandidates.addAll(ensTables);
	for(int i=0; i<tablesCandidates.size(); i++) indexTables.add(tablesCandidates.elementAt(i).getNomTable().toUpperCase());
	ensTables=null;
}

private void extraireSousDomaines(String NomAttrribut) {

	this.nbrSsDomaines.setSize(this.AttrSelections.size());
	
	int i;
	for(i=0; i<AttrSelections.size() && !AttrSelections.elementAt(i).GetNomAtt().equals(NomAttrribut); i++);

	Attribut a = AttrSelections.elementAt(i);
	
	String str = "", type="entier"; Statement st =null; ResultSet rs;
	Connection cn = this.CompteUser.getConnection(); int nbVals=0;
	
	
	// on calcul le nombre de valeurs distincts de l'attribut
	try {
		st = cn.createStatement();
		rs=st.executeQuery("select count(distinct "+a.GetNomAtt()+") from "+a.GetTableAtt());
		if(rs.next()) {
		nbVals = Integer.parseInt(rs.getString(1));
		
		System.out.println("le nombre de valeurs de l'attribut "+a.GetNomAtt()+" = "+nbVals);
		}
		}catch(SQLException e) {e.printStackTrace();}
		
	    // Verification du type de valeurs de l'attribut
	try {
		for(int k=0; k<a.valeurs.size(); k++) 
	    Integer.parseInt((String) a.valeurs.elementAt(k));
	    } catch (NumberFormatException e) {type = "reel";}
	    
	    try {
	    if(type.equals("reel")) Double.parseDouble((String) a.valeurs.firstElement());
	    } catch (NumberFormatException e) {type = "string";}
	    	 
	 a.type = type; 
	    	    
    // Si les valeurs sont des chaines de caract�res
	if(type.equals("string")) {
		
		Vector<String> SD = new Vector<String>();
		this.nbrSsDomaines.setElementAt(0,i);
		
		//dans le cas du string chaque valeur de predicat represente un fragment 
		//puis le reste des valeurs sera mis dans un fragment 
		//System.out.println("Valeurs string :");
		int inc = 0;		
		for(int j=0; j<a.valeurs.size(); j++) { 
			if(a.operateurs.elementAt(j).equals("IN"))
			   {str = "IN"+" "+(String)a.valeurs.elementAt(j); 
			    inc=1; 
			    int n = str.indexOf(","); System.out.println("str = "+str);
			    while(n != -1) {			    
				inc++; //calcul le nbr de vals de ce SD
				n = str.indexOf(",",n+1);
			                    }
			   }
			else str = "="+" "+a.valeurs.elementAt(j);			
			if(!SD.contains(str)) {
			SD.add(str); 
			this.nbrSsDomaines.setElementAt(this.nbrSsDomaines.elementAt(i)+1,i);
			                      }
		                                      }
		//on ajoute le SD Reste sil existe
		 if(this.nbrSsDomaines.elementAt(i)+inc < nbVals) {
		 SD.add("Reste"); 
		 this.nbrSsDomaines.setElementAt(this.nbrSsDomaines.elementAt(i)+1,i);
		                                             }
		//on ajoute les ss domaine de cet attribut au vecteur des ss Domaines
		souDomaines.add(SD); SD=null;		
	}
	
	//si les valeurs sont des entiers
	else if (type.equals("entier")) {
		System.out.println("Valeurs enti�res :");
		
		Vector<Intervalle> SD = new Vector<Intervalle>();
		
		this.nbrSsDomaines.setElementAt(1,i);
		
		//on recupere le MAX de l'attribut
		int max = 0;
		try {System.out.println(a.GetNomAtt().toUpperCase());
			st = cn.createStatement();
			rs=st.executeQuery("select MAX("+a.GetNomAtt().toUpperCase()+") " +
					           "from "+a.GetTableAtt().toUpperCase()+" ");
			if(rs.next()) {
			max = Integer.parseInt(rs.getString("MAX("+a.GetNomAtt().toUpperCase()+")"));
			
			System.out.println("le MAX de "+a.GetNomAtt()+" = "+max);
			}
			}catch(SQLException e) {e.printStackTrace();}
			
			//on initialise le sous domaines de cet attribut
			Intervalle I = new Intervalle(0,max);
	        SD.add(I);
	        this.nbrSsDomaines.setElementAt(1,i);
	        
	        //on parcours la liste des predicats(valeurs et operateurs)
	        for(int j=0; j<a.valeurs.size(); j++) {
	        	
	        	int val = Integer.parseInt((String)a.valeurs.elementAt(j));
	        	int k;
	        	
	        	//si l'operateur est > ou <=
	        	if(a.operateurs.elementAt(j).equals(">") || a.operateurs.elementAt(j).equals("<=")) 
	        	{
	        		//on recherche l'intervalle k correspendant � la valeure
	        		for(k=0; k<SD.size(); k++) if(SD.elementAt(k).inf <= val && SD.elementAt(k).sup >= val) break;

	        		//1er cas : val <> borne sup et val <> borne inf
	        		if(val != SD.elementAt(k).inf && val != SD.elementAt(k).sup) 
	        			{	
	        				I = new Intervalle(val+1, SD.elementAt(k).sup);
	        				SD.elementAt(k).sup = val;
	        				if(k < SD.size()-1) SD.insertElementAt(I, k+1);
	        				else SD.add(I);
	 
	        				this.nbrSsDomaines.setElementAt(this.nbrSsDomaines.elementAt(i)+1,i);
	        			}
	
	        		//2eme cas : val = borne inf et borne inf < borne sup
	        		else if(val == SD.elementAt(k).inf && SD.elementAt(k).inf < SD.elementAt(k).sup) 
	        		{
	        			I = new Intervalle(val, val);
	        			SD.elementAt(k).inf = val+1;
	        			SD.add(I);
		 
	        			this.nbrSsDomaines.setElementAt(this.nbrSsDomaines.elementAt(i)+1,i);
	        		}  
	        	}	        	
	        	
	        	//si l'operateur est >= ou <
	        	else if(a.operateurs.elementAt(j).equals(">=") || a.operateurs.elementAt(j).equals("<")) {
	        		
	        		for(k=0; k<SD.size(); k++) if(SD.elementAt(k).inf <= val && SD.elementAt(k).sup >= val) break;
    
	        		//1er cas : val < borne sup et val > borne inf
	        		if(val > SD.elementAt(k).inf && val < SD.elementAt(k).sup) 
	        		{	
	        			I = new Intervalle(val, SD.elementAt(k).sup);
	        			SD.elementAt(k).sup = val-1;
	        			if(k < SD.size()-1) SD.insertElementAt(I, k+1);
	        			else SD.add(I);
		 
	        			this.nbrSsDomaines.setElementAt(this.nbrSsDomaines.elementAt(i)+1,i);
	        		}
		
	        		//2eme cas : val = borne sup et borne inf < borne sup
	        		else if(val == SD.elementAt(k).sup && SD.elementAt(k).inf < SD.elementAt(k).sup) 
	        		{
	        			I = new Intervalle(val, val);
	        			SD.elementAt(k).sup = val-1;
	        			SD.add(I);
			 
	        			this.nbrSsDomaines.setElementAt(this.nbrSsDomaines.elementAt(i)+1,i);
	        		}
	        			        	}

					//si l'operateur est = ou <>
					else if(a.operateurs.elementAt(j).equals("=") || a.operateurs.elementAt(j).equals("<>")) {
						
					for(k=0; k<SD.size(); k++) if(SD.elementAt(k).inf <= val && SD.elementAt(k).sup >= val) break;
					    System.out.println("val = "+val+" "+"SD = "+k);
						//1er cas : val < borne sup et val > borne inf
						if(val > SD.elementAt(k).inf && val < SD.elementAt(k).sup) 
						    {	
							 I = new Intervalle(val+1, SD.elementAt(k).sup);
							 SD.elementAt(k).sup = val-1;
							 if(k < SD.size()-1) SD.insertElementAt(I, k+1);
							 else SD.add(I);
							 I = new Intervalle(val , val);
							 SD.add(I);
							 
							 this.nbrSsDomaines.setElementAt(this.nbrSsDomaines.elementAt(i)+2,i);
						    }
							
						//2eme cas : val = borne inf et borne inf < borne sup
					    else if(val == SD.elementAt(k).inf && SD.elementAt(k).inf < SD.elementAt(k).sup) 
					    {
					    		 I = new Intervalle(val, val);
								 SD.elementAt(k).inf = val+1;
								 SD.add(I);
						
						    this.nbrSsDomaines.setElementAt(this.nbrSsDomaines.elementAt(i)+1,i);
						}
						
						//3eme cas : val = borne sup et borne inf < borne sup
					    else if(val == SD.elementAt(k).sup && SD.elementAt(k).inf < SD.elementAt(k).sup) 
					    {
					    		 I = new Intervalle(val, val);
								 SD.elementAt(k).sup = val-1;
								 SD.add(I);
								 
							  this.nbrSsDomaines.setElementAt(this.nbrSsDomaines.elementAt(i)+1,i);
						}
						        			        	}	        	
						        
						        }
						        
					   //on ajoute les sous domaines de cet attribut dans le Vecteur des sous domaines        
						souDomaines.add(SD); SD=null;
						}
	
	//si les valeurs sont des Reels
	else if (type.equals("reel")) {
		System.out.println("Valeurs Reels :");
		
		Vector<IReel> SD = new Vector<IReel>();
		
		this.nbrSsDomaines.setElementAt(1,i);
		
		//on recupere le MAX de l'attribut
		double max = 0;
		try {
			st = cn.createStatement();
			rs=st.executeQuery("select MAX("+a.GetNomAtt().toUpperCase()+") " +
					           "from "+a.GetTableAtt().toUpperCase()+" ");
			if(rs.next()) {
			max = Double.parseDouble(rs.getString("MAX("+a.GetNomAtt().toUpperCase()+")"));
			
			System.out.println("le MAX de "+a.GetNomAtt()+" = "+max);
			}
			}catch(SQLException e) {e.printStackTrace();}
			
			//on initialise le sous domaines de cet attribut
			IReel R = new IReel(0,max);
	        SD.add(R);
	        this.nbrSsDomaines.setElementAt(1,i);
	        
	        //on parcours la liste des predicats(valeurs et operateurs)
	        for(int j=0; j<a.valeurs.size(); j++) {
	        	
	        	double val = Double.parseDouble((String)a.valeurs.elementAt(j));
	        	int k;
	        	
			//si l'operateur est > ou <=
			if(a.operateurs.elementAt(j).equals(">") || a.operateurs.elementAt(j).equals("<=")) {
			
			//on recherche l'intervalle k correspendant � la valeure
			for(k=0; k<SD.size(); k++) if(SD.elementAt(k).inf <= val && SD.elementAt(k).sup >= val) break;
			
			//1er cas : val <> borne sup et val <> borne inf
			if(k<SD.size() && val != SD.elementAt(k).inf && val != SD.elementAt(k).sup) 
			    {	
				 R = new IReel(Math.nextUp(val), SD.elementAt(k).sup);
				 SD.elementAt(k).sup = val;
				 if(k < SD.size()-1) SD.insertElementAt(R, k+1);
				 else SD.add(R);
				 
				 this.nbrSsDomaines.setElementAt(this.nbrSsDomaines.elementAt(i)+1,i);
			    }
				
			//2eme cas : val = borne inf et borne inf < borne sup
				 else if(k<SD.size() && val == SD.elementAt(k).inf && SD.elementAt(k).inf < SD.elementAt(k).sup) {
					 R = new IReel(val, val);
					 SD.elementAt(k).inf = Math.nextUp(val);
					 SD.add(R);
					 
					 this.nbrSsDomaines.setElementAt(this.nbrSsDomaines.elementAt(i)+1,i);
				    }  
			}	        	
				        	
			//si l'operateur est >= ou <
			else if(a.operateurs.elementAt(j).equals(">=") || a.operateurs.elementAt(j).equals("<")) {
			
				for(k=0; k<SD.size(); k++) if(SD.elementAt(k).inf <= val && SD.elementAt(k).sup >= val) break;
			    
				//1er cas : val < borne sup et val > borne inf
				if(k<SD.size() && val > SD.elementAt(k).inf && val < SD.elementAt(k).sup) 
				    {	
					 R = new IReel(val, SD.elementAt(k).sup);
					 SD.elementAt(k).sup = (val-Math.nextUp(val-1))+(val-1); //le num direct en arriere
					 if(k < SD.size()-1) SD.insertElementAt(R, k+1);
					 else SD.add(R);
					 
					 this.nbrSsDomaines.setElementAt(this.nbrSsDomaines.elementAt(i)+1,i);
				    }
					
				//2eme cas : val = borne sup et borne inf < borne sup
					 else if(k<SD.size() && val == SD.elementAt(k).sup && SD.elementAt(k).inf < SD.elementAt(k).sup) 
					 {
						 R = new IReel(val, val);
						 SD.elementAt(k).sup = (val-Math.nextUp(val-1))+(val-1);
						 SD.add(R);
						 
						 this.nbrSsDomaines.setElementAt(this.nbrSsDomaines.elementAt(i)+1,i);
				     }
				        			        	}
			
			//si l'operateur est = ou <>
			else if(a.operateurs.elementAt(j).equals("=") || a.operateurs.elementAt(j).equals("<>")) {
				
			for(k=0; k<SD.size(); k++) if(SD.elementAt(k).inf <= val && SD.elementAt(k).sup >= val) break;
			    System.out.println("val = "+val+" "+"SD = "+k);
			    
				//1er cas : val < borne sup et val > borne inf
				if(k<SD.size() && val > SD.elementAt(k).inf && val < SD.elementAt(k).sup) 
				    {	
					 R = new IReel (Math.nextUp(val), SD.elementAt(k).sup);
					 SD.elementAt(k).sup = (val-(Math.nextUp(val-1))+(val-1));
					 if(k < SD.size()-1) SD.insertElementAt(R, k+1);
					 else SD.add(R);
					 R = new IReel(val , val);
					 SD.add(R);
					 
					 this.nbrSsDomaines.setElementAt(this.nbrSsDomaines.elementAt(i)+2,i);
				    }
					
				//2eme cas : val = borne inf et borne inf < borne sup
			    else if(k<SD.size() && val == SD.elementAt(k).inf && SD.elementAt(k).inf < SD.elementAt(k).sup) 
			    {
			    		 R = new IReel(val, val);
						 SD.elementAt(k).inf = Math.nextUp(val);
						 SD.add(R);
				
				    this.nbrSsDomaines.setElementAt(this.nbrSsDomaines.elementAt(i)+1,i);
				}
				
				//3eme cas : val = borne sup et borne inf < borne sup
			    else if(k<SD.size() && val == SD.elementAt(k).sup && SD.elementAt(k).inf < SD.elementAt(k).sup) 
			    {
			    		 R = new IReel(val, val);
						 SD.elementAt(k).sup = (val-(Math.nextUp(val-1))+(val-1));
						 SD.add(R);
						 
					  this.nbrSsDomaines.setElementAt(this.nbrSsDomaines.elementAt(i)+1,i);
				}
				        			        	}	        	
				        
				        }
				        
			   //on ajoute les sous domaines de cet attribut dans le Vecteur des sous domaines        
				souDomaines.add(SD);
				SD=null;
				}
				a=null;
}

@SuppressWarnings("unchecked")
private void afficherInfosFH() {
	System.out.println();
	System.out.println("*********INFOS FH**************************");
	System.out.println();
    System.out.println("Les Attributs de Fragmentations :");
    System.out.println();
	for (int p=0; p< AttrSelections.size(); p++){
        Attribut a = AttrSelections.elementAt(p);
   
        System.out.println("   ---------- ");
        System.out.println("attribut: "+a.GetNomAtt());
        /*System.out.println("\t\tsa table : "+ a.GetTableAtt());
        System.out.println("   Ses Valeurs :");
        for(int cp=0;cp<a.valeurs.size(); cp++) 
        System.out.println("pridicat "+cp+": "+a.operateurs.elementAt(cp)+" "+a.valeurs.elementAt(cp));
        */System.out.println("   ---------- ");
                                                           }
	System.out.println("-----Les tables candidates------"+ tablesCandidates.size());
	for(int i=0; i<tablesCandidates.size(); i++) 
		System.out.println("nom table : "+tablesCandidates.elementAt(i).getNomTable());

	
	System.out.println("-----Les sous Domaines------");
	for(int cpt=0; cpt<souDomaines.size(); cpt++) {
	     
	System.out.println("\n Le nombre de sous domaines de "
	+AttrSelections.elementAt(cpt).GetNomAtt()+" = "+this.nbrSsDomaines.elementAt(cpt));
	     
		//on verifie le type de l'attribut	
	
	//si le type de l'attribut est entier
   if(AttrSelections.elementAt(cpt).type.equals("entier")) 	    
	 
		 for(int i=0; i<souDomaines.elementAt(cpt).size(); i++) 
		 { Intervalle I = (Intervalle) souDomaines.elementAt(cpt).elementAt(i);
	       System.out.print(" ["+I.inf+"-"+I.sup+"] ");
	       if(i==souDomaines.elementAt(cpt).size()-1) System.out.println();
		 } 
   
   else //si le type de l'attribut est reel
	   if(AttrSelections.elementAt(cpt).type.equals("reel")) 	    
			 
			 for(int i=0; i<souDomaines.elementAt(cpt).size(); i++) 
			 { IReel I = (IReel) souDomaines.elementAt(cpt).elementAt(i);
		       System.out.print(" ["+I.inf+"-"+I.sup+"] ");
		       if(i==souDomaines.elementAt(cpt).size()-1) System.out.println();
			 } 
	
   //si le type de l'attribut est string
    else if(AttrSelections.elementAt(cpt).type.equals("string"))    	
    		for(int i=0; i<souDomaines.elementAt(cpt).size(); i++)
    			System.out.println(souDomaines.elementAt(cpt).elementAt(i));
		    
	}
	
	/*for(int j=0; j<this.nbrSsDomaines.size();j++) 
		System.out.print(nbrSsDomaines.elementAt(j));
	    System.out.println();*/
}

private void extraireSelectiviteDim () {
	
		Statement st =null; ResultSet rs; String str="";
		this.CompteUser.fermerConnexion(); this.CompteUser.seConnecter();
		Connection cn = this.CompteUser.getConnection();
		Long nbTSD, nbTtab; 
		this.SelDim = new Vector<Vector<Double>>();
		
		System.out.println();
		System.out.println("*****************Selectivit� table de dimension********************");
		System.out.println();
		
		try {
		for(int cpt=0; cpt<souDomaines.size(); cpt++) {
			
			Vector<Double> sel = new Vector<Double> ();
			Attribut a = AttrSelections.elementAt(cpt);
			Intervalle I = null;
			IReel R = null;
			
			//on calcul le nombre de tuples pour chaque table 
			st = cn.createStatement();
			rs=st.executeQuery("select COUNT(*) from "+this.AttrSelections.elementAt(cpt).GetTableAtt().toUpperCase());
			if(rs.next()) str = rs.getString("COUNT(*)");
			nbTtab = Long.parseLong(str);
			System.out.println("nb tuples de "+	this.AttrSelections.elementAt(cpt).GetTableAtt()+" = "+nbTtab);
		
	    //on calcul la selectivit� de chaque ss domaine dans sa dimension
 			for(int i=0; i<souDomaines.elementAt(cpt).size(); i++) 
			{
				st = cn.createStatement();
				
		if(a.type.equals("string")) {
		String s = (String) souDomaines.elementAt(cpt).elementAt(i);
		
		//si SD IN alors on ajoute les parenth�ses
		if(s.indexOf("IN ")!=-1) s = s.substring(0,s.indexOf("'"))+"("+s.substring(s.indexOf("'"))+")";
		
		System.out.println("select COUNT(*) from "
		+this.AttrSelections.elementAt(cpt).GetTableAtt().toUpperCase()+
	    " where "+this.AttrSelections.elementAt(cpt).GetNomAtt().toUpperCase()+" "+s);
		
		rs=st.executeQuery("select COUNT(*) from "
		+this.AttrSelections.elementAt(cpt).GetTableAtt().toUpperCase()+
	    " where "+this.AttrSelections.elementAt(cpt).GetNomAtt().toUpperCase()+
	    " "+s);		      
		                            }
		
		else if(a.type.equals("entier")) {//entier
			I = (Intervalle) souDomaines.elementAt(cpt).elementAt(i);
			
			System.out.println("select COUNT(*) from "+a.GetTableAtt().toUpperCase()+
				    " where "+a.GetNomAtt().toUpperCase()+" >= "+I.inf+
				      " and "+a.GetNomAtt().toUpperCase()+" <= "+I.sup);
			
			rs=st.executeQuery("select COUNT(*) from "+a.GetTableAtt().toUpperCase()+
				    " where "+a.GetNomAtt().toUpperCase()+" >= "+I.inf+
				      " and "+a.GetNomAtt().toUpperCase()+" <= "+I.sup);
		     }
		
		else {//reel
			R = (IReel) souDomaines.elementAt(cpt).elementAt(i);
			rs=st.executeQuery("select COUNT(*) from "+a.GetTableAtt().toUpperCase()+
				    " where "+a.GetNomAtt().toUpperCase()+" >= "+R.inf+
				      " and "+a.GetNomAtt().toUpperCase()+" <= "+R.sup);
		     }
		
		if(rs.next()) str = rs.getString("COUNT(*)");
		
		nbTSD = Long.parseLong(str);
		
		if(nbTSD > 0) 
		{
		double n = (double)nbTSD/nbTtab;
		sel.add(n);
		
		if(a.type.equals("string"))
		System.out.println("nb tuples "+souDomaines.elementAt(cpt).elementAt(i)+" de "+
				a.GetNomAtt()+" = "+str+"  FSelectivit� = "+sel.lastElement());
		
		else if(a.type.equals("entier"))
		System.out.println("nb tuples ["+I.inf+"-"+I.sup+"] de "+
					a.GetNomAtt()+" = "+str+"  FSelectivit� = "+sel.lastElement());
		
		else
			System.out.println("nb tuples ["+R.inf+"-"+R.sup+"] de "+
					a.GetNomAtt()+" = "+str+"  FSelectivit� = "+sel.lastElement());
		}
		
		else {  
			if(a.type.equals("string"))
			System.out.println("Le SD "+souDomaines.elementAt(cpt).elementAt(i)+" de "+
					a.GetNomAtt()+" a �t� supprim�");
			
			else if(a.type.equals("entier")) {
				for(int k=i+1; k<souDomaines.elementAt(cpt).size() ; k++) {
				Intervalle I2 = (Intervalle) souDomaines.elementAt(cpt).elementAt(k);
				 if(I2.inf==I.sup+1) {I2.inf = I.inf; break;}
				                                                           }			    
				System.out.println("Le SD ["+I.inf+"-"+I.sup+"] de "+
		        a.GetNomAtt()+" a �t� joint au suivant ");
			                                  }

			else {IReel R2 = null;
			      for(int k=i+1; k<souDomaines.elementAt(cpt).size() ; k++) {
				   R2 = (IReel) souDomaines.elementAt(cpt).elementAt(k);
				  if(R2.inf==Math.nextUp(R.sup)) {R2.inf = R.inf; break;}
				                                                           }			    
				 System.out.println("Le SD ["+R.inf+"-"+R.sup+"] de "+
		         a.GetNomAtt()+" a �t� joint au suivant ");
			}
		
		souDomaines.elementAt(cpt).removeElementAt(i); i--;
		int nb = nbrSsDomaines.elementAt(cpt);
		nbrSsDomaines.setElementAt(nb-1, cpt);
			  }
		}			
			
			this.SelDim.add(sel);
			System.out.println();			
		}
		
		} catch(SQLException e) {e.printStackTrace();}
}

private void extraireSelectiviteFait () {
	Statement st =null; ResultSet rs; String str="";
	Connection cn = this.CompteUser.getConnection();
	Long nbTSD, nbTtab;
	this.SelFait = new Vector<Vector<Double>>();
	
	//on recupere la table de fait
	Table fait = this.CompteUser.getTableFaits();
	System.out.println();
	System.out.println("*****************selectivit� du fait********************");
	System.out.println();
	System.out.println("table de fait = "+fait.getNomTable());
	
	nbTtab = fait.getNb_Tuple();
	System.out.println("Nbr de tuples de "+fait.getNomTable()+" = "+nbTtab);
	
 try {
		for(int cpt=0; cpt<souDomaines.size(); cpt++)
		{			
			Vector<Double> sel = new Vector<Double> ();
			Attribut a = this.AttrSelections.elementAt(cpt);
			
			//on calcul le facteur de selectivit� pour chaque ss domaine
			for(int i=0; i<souDomaines.elementAt(cpt).size(); i++) 
		{	
		    st = cn.createStatement();
		    Intervalle I = null; IReel R = null;
		    
		    int iT = indexTables.indexOf(this.AttrSelections.elementAt(cpt).GetTableAtt().toUpperCase());
		    
		//on recupere la cle primaire de la dimension
		String PKdim="",FKfait=""; 
		 PKdim = tablesCandidates.elementAt(iT).getPK(); 
		
		//on recupere la cl� etrang�re de la cl� primaire de la dim dans le fait
		FKfait = tablesCandidates.elementAt(iT).getFKfait();
		
		//on calcul le nombre de tuples du SD de la dimension dans le fait
		if(a.type.equals("string")) {
		String s = (String) souDomaines.elementAt(cpt).elementAt(i);
		
		//si SD IN alors on ajoute les parenth�ses
		if(s.indexOf("IN ")!=-1) s = s.substring(0,s.indexOf("'"))+"("+s.substring(s.indexOf("'"))+")";
		
		System.out.println("select COUNT(*) from "
                +fait.getNomTable()+" F, "
                +this.AttrSelections.elementAt(cpt).GetTableAtt().toUpperCase()+" T"
                +" where "+"F."+FKfait+" = T."+PKdim
                +" and "+this.AttrSelections.elementAt(cpt).GetNomAtt().toUpperCase()
                +" "+s);
		
		rs=st.executeQuery("select COUNT(*) from "
		                   +fait.getNomTable()+" F, "
		                   +this.AttrSelections.elementAt(cpt).GetTableAtt().toUpperCase()+" T"
		                   +" where "+"F."+FKfait+" = T."+PKdim
		                   +" and ("+this.AttrSelections.elementAt(cpt).GetNomAtt().toUpperCase()
		                   +" "+s+")");		       
		                             }
		
		else  if(a.type.equals("entier"))	{//entier
			  I = (Intervalle) souDomaines.elementAt(cpt).elementAt(i);
			  System.out.println("select COUNT(*) from "
					+fait.getNomTable()+" F, "+a.GetTableAtt().toUpperCase()+" T"+ 
				    " where "+"F."+FKfait+" = T."+PKdim
					+" and "+a.GetNomAtt().toUpperCase()+" >= "+I.inf
				    +" and "+a.GetNomAtt().toUpperCase()+" <= "+I.sup);
			  
		      rs=st.executeQuery("select COUNT(*) from "
					+fait.getNomTable()+" F, "+a.GetTableAtt().toUpperCase()+" T"+ 
				    " where "+"F."+FKfait+" = T."+PKdim
					+" and "+a.GetNomAtt().toUpperCase()+" >= "+I.inf
				    +" and "+a.GetNomAtt().toUpperCase()+" <= "+I.sup);
		                                     }
		
		else {
			R = (IReel) souDomaines.elementAt(cpt).elementAt(i);
		      rs=st.executeQuery("select COUNT(*) from "
					+fait.getNomTable()+" F, "+a.GetTableAtt().toUpperCase()+" T"+ 
				    " where "+"F."+FKfait+" = T."+PKdim
					+" and "+a.GetNomAtt().toUpperCase()+" >= "+R.inf
				    +" and "+a.GetNomAtt().toUpperCase()+" <= "+R.sup);
		      }
		
		if(rs.next()) str = rs.getString("COUNT(*)");
		
		//on calcul la selectivit� du SD
		nbTSD = Long.parseLong(str);
		
		double n = (float)nbTSD/nbTtab;
		sel.add(n);
		
		if(a.type.equals("string"))
			System.out.println("nb tuples "+souDomaines.elementAt(cpt).elementAt(i)+" de "+
					a.GetNomAtt()+" = "+str+"  FSelectivit� = "+sel.lastElement());
			
			else if(a.type.equals("entier"))
			System.out.println("nb tuples ["+I.inf+"-"+I.sup+"] de "+
						a.GetNomAtt()+" = "+str+"  FSelectivit� = "+sel.lastElement());
			
			else
				System.out.println("nb tuples ["+R.inf+"-"+R.sup+"] de "+
						a.GetNomAtt()+" = "+str+"  FSelectivit� = "+sel.lastElement());
		}
					
			this.SelFait.add(sel);
		}
		} catch(SQLException e) {e.printStackTrace();}
}

private void ChangerReste() {
	   
	Statement st =null; ResultSet rs; String result="";
	Connection cn = this.CompteUser.getConnection();
	
	   for(int i=0; i<souDomaines.size(); i++)
		   if(AttrSelections.elementAt(i).type.equals("string"))
			   if(souDomaines.elementAt(i).lastElement().equals("Reste"))
			   {
				   String Reste ="";
				   for(int n=0; n<souDomaines.elementAt(i).size()-1; n++)   
				   {
					String s = (String) souDomaines.elementAt(i).elementAt(n);   
					if(n>=1) Reste+="and ";
					if(s.indexOf("IN ")==-1)
	                   Reste+=AttrSelections.elementAt(i).GetNomAtt()+
	                          " <>"+ s.substring(s.indexOf("=")+1)+" ";		
					else { int p = s.indexOf("'"), v = s.indexOf(",");
						   while(v!=-1) {
							              Reste+=AttrSelections.elementAt(i).GetNomAtt()+
							                     " <> "+s.substring(p,v)+" and ";
							              p=v+1; v=s.indexOf(",",p);
						                 }
						   Reste+=AttrSelections.elementAt(i).GetNomAtt()+
		                          " <> "+s.substring(p)+" ";
					      }
	               }
				   
				   try {
               	    st=cn.createStatement();
						rs = st.executeQuery("select distinct("+AttrSelections.elementAt(i).GetNomAtt()
								             +") from "+AttrSelections.elementAt(i).GetTableAtt()
								             +" where "+Reste);
						int cp = 0;
						while(rs.next()) 
						{ if(cp>=1)	result+=",";
						  result+=" = '"+rs.getString(AttrSelections.elementAt(i).GetNomAtt())+"'";
						  cp++;
					     }
				       } catch (SQLException e) {e.printStackTrace();}
				       
				       //on modifie le reste par l'ajout du nom de l'attribut
				       int p=result.indexOf(",");;
  	        	     while(p!=-1)
  	        	     {
  	        	    	result=result.substring(0,p)+" OR "+AttrSelections.elementAt(i).GetNomAtt()+result.substring(p+1);
  	        	    	p=result.indexOf(",",p+1);
  	        	     }
				   
				   souDomaines.elementAt(i).setElementAt(result, souDomaines.elementAt(i).size()-1);
				   result="";
			   }
      }
    
private void CalculNbrAttrParTable() {
	
	nbrAttrParTable = new Vector<Integer>();
	FragTables = new Vector<HashSet<String>>();
	
	/** Initialisation des vecteurs nbrAttrParTab et FragTables **/
	for (int i = 0; i < tablesCandidates.size(); i++) 
	{
		HashSet<String> H = new HashSet<String>();
		FragTables.add(H);
		nbrAttrParTable.add(0);
	}

	/** On calcul le nombre d'attribut pour chaque table **/
	for (int i = 0; i < this.AttrSelections.size(); i++) 
	{
		for (int k = 0; k < tablesCandidates.size(); k++)
			if (tablesCandidates.elementAt(k).getNomTable().toUpperCase()
					.equals(this.AttrSelections.elementAt(i).GetTableAtt().toUpperCase())) 
			{
				int g = nbrAttrParTable.elementAt(k);
				nbrAttrParTable.setElementAt(g + 1, k);
				break;
			}
	}

	/** affichage du nombre d'attribut par table **/
	System.out.println();
	System.out.print("Nombre Attribut par tables : ");
	for (int i = 0; i < nbrAttrParTable.size(); i++)
		System.out.print(" " + nbrAttrParTable.elementAt(i));
	System.out.println();
	
}

public void extraireFragmentsTables() {

	HashSet<Integer> HS = new HashSet<Integer>();
	int ind;
	int nbFrag = 1;
	Vector<Integer> VMaxAtt = new Vector<Integer>();
	Vector<Integer> itChange = new Vector<Integer>();
	Vector<Integer> itCour = new Vector<Integer>();
	Vector<Integer> valCour = new Vector<Integer>();
	CompositeGene comp;
	Vector<Integer> ligne = new Vector<Integer>();

	/**calcul du nombre de fragments de chaque attribut**/
	for (int i = 0; i < BestofBest.size(); i++) {
		comp = (CompositeGene) BestofBest.getGene(i);
		HS.clear();
		for (int j = 0; j < this.nbrSsDomaines.elementAt(i); j++) {
			ind = ((Integer) ((IntegerGene) comp.geneAt(j)).getAllele()).intValue();
			HS.add(ind);
		}
		VMaxAtt.add(HS.size());
		nbFrag *= HS.size();
	}
     HS=null;
	/**calcul pour chaque attribut le nbr d'iterations a faire avant de changer de valeur**/
	for (int i = 0; i < BestofBest.size(); i++) {
		nbFrag = 1;
		for (int j = i + 1; j < BestofBest.size(); j++)
			nbFrag *= VMaxAtt.elementAt(j);
		itChange.add(nbFrag);
	}

	/**on initialise les valeurs des iterations courantes et des vals courantes**/
	for (int i = 0; i < BestofBest.size(); i++) {
		itCour.add(0);
		valCour.add(1);
	}

	boolean Fin = false;
	int sc = 0;
	/** affichage des series de valeurs des sous schemas **/
	while (!Fin) 
	{
		ligne.clear();
		for (int i = 0; i < BestofBest.size(); i++) {

			/** on incremente l'iteration courante **/
			int it = itCour.elementAt(i);
			it++;
			itCour.setElementAt(it, i);

			if (itCour.elementAt(i) <= itChange.elementAt(i)) 
			{
				ligne.add(valCour.elementAt(i));
				//System.out.print(" " + valCour.elementAt(i));
			} else if (i == 0 && valCour.elementAt(i) == VMaxAtt.elementAt(i)) 
					{
						Fin = true;
						break;
					} 
				else if (i > 0 && valCour.elementAt(i) == VMaxAtt.elementAt(i)) 
				{
					itCour.setElementAt(1, i);
					valCour.setElementAt(1, i);
					ligne.add(valCour.elementAt(i));
					//System.out.print(" " + valCour.elementAt(i));
				} 
				else{
						itCour.setElementAt(1, i);
						int val = valCour.elementAt(i);
						val++;
						valCour.setElementAt(val, i);
						ligne.add(valCour.elementAt(i));
						//System.out.print(" " + valCour.elementAt(i));
					}
		}
		if (Fin)break;
		//System.out.println();
		
		/** On enregistre les ss Schemas **/
		int decal = 0;
		for (int i = 0; i < tablesCandidates.size(); i++) 
		{
			String str = "";
			for (int j = decal; j < decal + nbrAttrParTable.elementAt(i); j++)
				str += ligne.elementAt(j);
			decal += nbrAttrParTable.elementAt(i);
			FragTables.elementAt(i).add(str);
		}
	}
		VMaxAtt=null; itChange=null; itCour=null; valCour=null; ligne=null; 
		String str = "";
		/** Affichage des Fragments de chaque table **/
		Iterator<String> I = null; System.out.println();
		for (int i = 0; i < FragTables.size(); i++) 
		{
			System.out.println("Fragment de la table "+tablesCandidates.elementAt(i).getNomTable()+" : ");
			I = FragTables.elementAt(i).iterator();
			while (I.hasNext())
				System.out.print(" " + I.next());
			System.out.println();
		}
}	

private void definirTablesAFragmentees()
{
	int decal = 0; this.tabsFH = new Vector<Table>();
	this.AttrDeFrag = new Vector<Attribut>();
	//parcours des tables candidates
	for(int i=0; i<tablesCandidates.size(); i++)
	{ boolean FH = false;
		//parcours des attributs de chaque table
		int j;
		for(j=decal; j<decal+nbrAttrParTable.elementAt(i); j++)
			{	
				CompositeGene comp = (CompositeGene) this.BestofBest.getGene(j);
				HashSet<Integer> HS = new HashSet<Integer>(nbrSsDomaines.elementAt(j));
				//parcours des sous domaines de chaque attribut
				for(int k=0; k<nbrSsDomaines.elementAt(j) && HS.size()<=1; k++)
					HS.add(((Integer)((IntegerGene)comp.geneAt(k)).getAllele()).intValue());
				if(HS.size()>=2) {FH=true; this.AttrDeFrag.add(this.AttrSelections.elementAt(j));} 
			}
		if(FH) tabsFH.add(tablesCandidates.elementAt(i));
		decal+=nbrAttrParTable.elementAt(i);
	}
}

private void identifFrags() {
	
	PredFrags = new Vector<Vector<Fragment>>();
	
	/** Construire les fragments des tables candidates **/
	int decal = 0; CompositeGene comp=null;
	Fragment Frag = new Fragment();
	Vector<Fragment> VFrag = new Vector<Fragment>();
	Iterator<String> I = null;
	Attribut att = null; int ValSD, indAttr;
	System.out.println("Identif Frags :");
	
	for (int i = 0; i < tablesCandidates.size(); i++) 
	{
		VFrag = new Vector<Fragment>();
		I = FragTables.elementAt(i).iterator();
		while (I.hasNext()) {
			Frag = new Fragment();
			String str = I.next();
			for (int j = 0; j < str.length(); j++) 
			{				
				ValSD = Integer.parseInt(str.substring(j,j+1));
				//System.out.println("ValSD = "+ValSD);
				indAttr = decal + j;
				comp = (CompositeGene) BestofBest.getGene(indAttr);
				String Tab = AttrSelections.elementAt(indAttr).GetTableAtt();
				String Attr = AttrSelections.elementAt(indAttr).GetNomAtt();
				att = new Attribut(Tab, Attr);
				att.valeurs = new Vector<Object>();
				att.type = AttrSelections.elementAt(indAttr).type;

				for (int k = 0; k < souDomaines.elementAt(indAttr).size(); k++)
					if (((Integer)((IntegerGene)comp.geneAt(k)).getAllele()).intValue() == ValSD ) 
					{
						//System.out.print(" "+SouDomAttrCod.elementAt(indAttr).elementAt(k));				
						att.valeurs.add(souDomaines.elementAt(indAttr).elementAt(k));							
					}
				//System.out.println();
				Frag.predicats.add(att);
			}				
			    VFrag.add(Frag);
		}
		PredFrags.add(VFrag);
		decal += nbrAttrParTable.elementAt(i);		
	}		
	
	/**Affichage des Fragments de chaque table**/
	for(int i=0; i< PredFrags.size(); i++)
	{   System.out.println("Frgaments de "+tablesCandidates.elementAt(i).getNomTable()+" :");
		for(int j=0; j<PredFrags.elementAt(i).size(); j++) 
		{ System.out.println("Frgament N� "+(j+1)+" :");
		  for(int k=0; k<PredFrags.elementAt(i).elementAt(j).predicats.size(); k++) 
		  {
			att = PredFrags.elementAt(i).elementAt(j).predicats.elementAt(k);
			System.out.println("Attribut = "+att.GetNomAtt());
			System.out.println("Ses Valeurs :");
			if(att.type.equals("entier"))
				for(int y=0; y<att.valeurs.size(); y++) 
				 { Intervalle I2 = (Intervalle) att.valeurs.elementAt(y);
			      // str+=" [ "+I2.inf+" - "+I2.sup+" ] ";
			       System.out.print(" [ "+I2.inf+" - "+I2.sup+" ] ");
			       if(y==att.valeurs.size()-1) {System.out.println();}
				 } 
			
			else if(att.type.equals("reel"))
					for(int y=0; y<att.valeurs.size(); y++) 
					{ 	IReel I2 = (IReel) att.valeurs.elementAt(y);
						//str += " [ "+I2.inf+" - "+I2.sup+" ] ";
						System.out.print(" [ "+I2.inf+" - "+I2.sup+" ] ");
						if(y == att.valeurs.size()-1) {/*str+="\n";*/ System.out.println();}
					}			
				//si le type de l'attribut est string
		    	else if(att.type.equals("string"))    	
		    			for(int y=0; y<att.valeurs.size(); y++)
		    				{/*str += att.valeurs.elementAt(y)+"\n";*/ System.out.println(att.valeurs.elementAt(y)+"\n");}
		  }
		}
	}
}

/*public String identifFrags() {
	
	PredFrags = new Vector<Vector<Fragment>>();
	
	/** Construire les fragments des tables candidates **
	int decal = 0; CompositeGene comp=null;
	Fragment Frag = null;
	Vector<Fragment> VFrag = new Vector<Fragment>();
	Iterator<String> I = null;
	Attribut att = null; int ValSD,indAttr,x;
	System.out.println("Identif Frags :");
	
	for (int i = 0; i < tabsFH.size(); i++) 
	{
		VFrag = new Vector<Fragment>();
		I = FragTables.elementAt(i).iterator();
		while (I.hasNext()) {
			Frag = new Fragment();
			String str = I.next();
			for (int j = 0; j < str.length(); j++) 
			{				
			  indAttr = decal + j;
			  //verifi� si on fragmente sur cet attribut avant de l'ecrire dans le fragment
			  if(this.AttrDeFrag.contains(this.AttrSelections.elementAt(indAttr)))
			  {
				ValSD = Integer.parseInt(str.substring(j,j+1));
				//System.out.println("ValSD = "+ValSD);				
				comp = (CompositeGene) BestofBest.getGene(indAttr);
				String Tab = this.AttrSelections.elementAt(indAttr).GetTableAtt();
				String Attr = this.AttrSelections.elementAt(indAttr).GetNomAtt();
				att = new Attribut(Tab, Attr);
				att.valeurs = new Vector<Object>();
				att.type = this.AttrSelections.elementAt(indAttr).type;

				for (int k = 0; k < this.nbrSsDomaines.elementAt(indAttr); k++)
					if (((Integer)((IntegerGene)comp.geneAt(k)).getAllele()).intValue() == ValSD ) 
					{
						//System.out.print(" "+SouDomAttrCod.elementAt(indAttr).elementAt(k));
						if(!this.souDomaines.elementAt(indAttr).elementAt(k).equals("Reste"))
							att.valeurs.add(this.souDomaines.elementAt(indAttr).elementAt(k));
						else { 
							   String Reste ="";
							   for(int n=0; n<k; n++)   
							   {
								String s = (String)this.souDomaines.elementAt(indAttr).elementAt(n);   
								if(n>=1) Reste+="and ";
								if(s.indexOf("IN")==-1)
				                   Reste+=this.AttrSelections.elementAt(indAttr).GetNomAtt()+
				                          " <>"+ s.substring(s.indexOf("=")+1)+" ";		
								else { int p = s.indexOf("'"), v = s.indexOf(",");
									   while(v!=-1) {
										              Reste+=this.AttrSelections.elementAt(indAttr).GetNomAtt()+
										                     " <> "+s.substring(p,v)+" and ";
										              p=v+1; v=s.indexOf(",",p);
									                 }
									   Reste+=this.AttrSelections.elementAt(indAttr).GetNomAtt()+
					                          " <> "+s.substring(p)+" ";
								      }
				               }
							   att.valeurs.add(Reste);
						     }
					}
				//System.out.println();
				
				Frag.predicats.add(att);//enreg des predicats de chaque fragments
				
			}//fin verif si attr de frag
			}//fin d'un fragment	
			
			    VFrag.add(Frag); //enreg du fragment
		}
		PredFrags.add(VFrag); //enreg des fragments de chaque tables � fragment�es
		decal += nbrAttrParTable.elementAt(i);		
	}		
	String str="";
	/**Affichage des Fragments de chaque table**
	for(int i=0; i< PredFrags.size(); i++)
	{   str+="\nFrgaments de "+tabsFH.elementAt(i).getNomTable()+" :";
		System.out.println("Frgaments de "+tabsFH.elementAt(i).getNomTable()+" :");
		for(int j=0; j<PredFrags.elementAt(i).size(); j++) 
		{ str += "\nFrgament N� "+(j+1)+" :";
		System.out.println("Frgament N� "+(j+1)+" :");
		for(int k=0; k<PredFrags.elementAt(i).elementAt(j).predicats.size(); k++) 
		{
			att = PredFrags.elementAt(i).elementAt(j).predicats.elementAt(k);
			str+="\nAttribut = "+att.GetNomAtt();
			System.out.println("Attribut = "+att.GetNomAtt());
			str+="\nSes Valeurs :";
			System.out.println("Ses Valeurs :");
			
			if(att.type.equals("entier"))
				for(int y=0; y<att.valeurs.size(); y++) 
				 { Intervalle I2 = (Intervalle) att.valeurs.elementAt(y);
			       str+=" [ "+I2.inf+" - "+I2.sup+" ] ";
			       System.out.print(" [ "+I2.inf+" - "+I2.sup+" ] ");
			       if(y==att.valeurs.size()-1) {str+="\n"; System.out.println();}
				 } 
			
			else if(att.type.equals("reel"))
					for(int y=0; y<att.valeurs.size(); y++) 
					{ 	IReel I2 = (IReel) att.valeurs.elementAt(y);
						str += " [ "+I2.inf+" - "+I2.sup+" ] ";
						System.out.print(" [ "+I2.inf+" - "+I2.sup+" ] ");
						if(y == att.valeurs.size()-1) {str+="\n"; System.out.println();}
					}			
				//si le type de l'attribut est string
		    	else if(att.type.equals("string"))    	
		    			for(int y=0; y<att.valeurs.size(); y++)
		    				{str += att.valeurs.elementAt(y)+"\n"; System.out.println(att.valeurs.elementAt(y)+"\n");}
		}
		}
	}
	
	return str;
}*/

private void ReordonnerAttributs()
{
	// on reordonne les attributs et la taille de leurs ss domaines
	Vector<Attribut> att = new Vector<Attribut>(AttrSelections.size());
	Vector<Integer> nbSD = new Vector<Integer>(nbrSsDomaines.size());
	Vector<Vector> SD = new Vector<Vector>(this.souDomaines.size());
	for(int i=0; i<tablesCandidates.size(); i++)
		for(int j=0; j<AttrSelections.size(); j++) 
		if (tablesCandidates.elementAt(i).getNomTable().equals(AttrSelections.elementAt(j).GetTableAtt().toUpperCase()))
			{ nbSD.add(nbrSsDomaines.elementAt(j));
		      att.add(AttrSelections.elementAt(j));
		      SD.add(souDomaines.elementAt(j));
		     }
	nbrSsDomaines.removeAllElements(); AttrSelections.removeAllElements(); 
	souDomaines.removeAllElements();
	nbrSsDomaines.addAll(nbSD);
	AttrSelections.addAll(att);
	souDomaines.addAll(SD);
}

public Schema getSchema(){
	return this.SCH;
}

public Vector<Table> getTabsC(){return this.tablesCandidates;}

public Vector<Table> getTabsFH(){return this.tabsFH;}

public Vector<Attribut> getAttrsC(){return this.AttrSelections;}

public Vector<Attribut> getAttrsFH(){return this.AttrDeFrag;}

public Vector<Integer> getnbAttrsTab(){return this.nbrAttrParTable;}

public Vector<Integer> getnbSdAttr(){return this.nbrSsDomaines;}

public Vector<Vector<Double>> getSelFait(){return this.SelFait;}

public Vector<Vector> getSsDomaines(){return this.souDomaines;}

public Compte getCompte(){return this.CompteUser;}

public Vector<Requete> getRequetes(){return this.req;}
}
