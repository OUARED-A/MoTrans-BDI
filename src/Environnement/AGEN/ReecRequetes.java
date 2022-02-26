package Environnement.AGEN;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Vector;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.jgap.IChromosome;
import org.jgap.impl.CompositeGene;
import org.jgap.impl.IntegerGene;

import Environnement.Attribut;
import Environnement.Compte;
import Environnement.Fragment;
import Environnement.IReel;
import Environnement.Intervalle;
import Environnement.Requete;
import Environnement.Table;


public class ReecRequetes {
	
	private Vector<Integer> nbrAttrParTable = new Vector<Integer>();
	private Vector<HashSet<String>> FragTables = new Vector<HashSet<String>>();
	private Vector<Vector<Fragment>> PredFrags = new Vector<Vector<Fragment>>();
	private Vector<Table> tablesCandidates = null;
	public Vector<String> indexTables = null;
	private Vector<Attribut> AttributCodage = null;
	private Vector<Vector> SouDomAttrCod = null;
	private Vector<String> SousSchBD = null;
	private IChromosome BestofBest = null;
	private Vector<Vector<String>> WhereFragments = null;
	private Vector<Vector<Integer>> iSouSch = null;
	private Vector<Requete> requetes = null;
	private Vector<String> SQLrequetes = null;
	private Vector<Integer> BlocsValides = null;
	private Vector<String> ClePrim = null;
	private Vector<String> CleEtrang = null;
	private int cptCloseConnect=0;
	private Compte cmpt = null; 

	public ReecRequetes(Compte cmpt, IChromosome chrom, Vector<Requete> VRQ, Vector<Table> tablesCandidates, 
			Vector<Attribut> AttributCodage, Vector<Vector> SouDomAttrCod) 
	{	
		this.BestofBest = chrom;
		this.cmpt = cmpt;
		cmpt.fermerConnexion(); cmpt.seConnecter();
		this.requetes = VRQ;
		this.tablesCandidates = tablesCandidates;
		this.AttributCodage = AttributCodage;
		this.SouDomAttrCod = SouDomAttrCod;
		//Modifi� Chrom
	    /*for (int i = 0; i < this.BestofBest.size(); i++) {
	        CompositeGene comp = (CompositeGene)this.BestofBest.getGene(i); 
	        for (int j=0; j< comp.size()-1; j++) 
	        if(i==0 || i==1) comp.geneAt(j).setAllele(1);
	        else if(i==2 && (j==1||j==2))comp.geneAt(j).setAllele(2);
	             else if(i==2) comp.geneAt(j).setAllele(1);
	             else if(i==3 && j!=1) comp.geneAt(j).setAllele(1);
	             else if(i==3 && j==1) comp.geneAt(j).setAllele(2);
	                                                 }*/
		indexTables = new Vector<String>(tablesCandidates.size()); 
    	for(int i=0; i<tablesCandidates.size(); i++)
    		indexTables.add(tablesCandidates.elementAt(i).getNomTable().toUpperCase());		
		
		CalculNbrAttrParTable();
		extraireFragmentsTables();
		identifFrags();
		extraireSousSchBD();
		AjoutColBD();
		FragmenterED();
		ReecritureRequetes();
	}
	
	private void CalculNbrAttrParTable() {
		
		/** Initialisation des vecteurs nbrAttrParTab et FragTables **/
		for (int i = 0; i < tablesCandidates.size(); i++) 
		{
			HashSet<String> H = new HashSet<String>();
			FragTables.add(H);
			nbrAttrParTable.add(0);
		}

		/** On calcul le nombre d'attribut pour chaque table **/
		for (int i = 0; i < this.AttributCodage.size(); i++) 
		{
			for (int k = 0; k < tablesCandidates.size(); k++)
				if (tablesCandidates.elementAt(k).getNomTable().toUpperCase()
						.equals(AttributCodage.elementAt(i).GetTableAtt().toUpperCase())) 
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
	
	private void extraireFragmentsTables() {

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
			for (int j = 0; j < SouDomAttrCod.elementAt(i).size(); j++) {
				ind = ((Integer) ((IntegerGene) comp.geneAt(j)).getAllele())
						.intValue();
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
		while (!Fin) {
			ligne.clear();
			for (int i = 0; i < BestofBest.size(); i++) {

				/** on incremente l'iteration courante **/
				int it = itCour.elementAt(i);
				it++;
				itCour.setElementAt(it, i);

				if (itCour.elementAt(i) <= itChange.elementAt(i)) {
					ligne.add(valCour.elementAt(i));
					//System.out.print(" " + valCour.elementAt(i));
				} else if (i == 0
						&& valCour.elementAt(i) == VMaxAtt.elementAt(i)) {
					Fin = true;
					break;
				} else if (i > 0
						&& valCour.elementAt(i) == VMaxAtt.elementAt(i)) {
					itCour.setElementAt(1, i);
					valCour.setElementAt(1, i);
					ligne.add(valCour.elementAt(i));
					//System.out.print(" " + valCour.elementAt(i));
				} else {
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
			for (int i = 0; i < tablesCandidates.size(); i++) {
				String str = "";
				for (int j = decal; j < decal + nbrAttrParTable.elementAt(i); j++)
					str += ligne.elementAt(j);
				decal += nbrAttrParTable.elementAt(i);
				FragTables.elementAt(i).add(str);
			}
		}
         VMaxAtt=null; itChange=null; itCour=null; valCour=null; ligne=null; 
		
		/** Affichage des Fragments de chaque table **/
		Iterator<String> I = null; System.out.println();
		for (int i = 0; i < FragTables.size(); i++) 
		{
			System.out.println("Fragment de la table "
					+ tablesCandidates.elementAt(i).getNomTable()+" : ");
			I = FragTables.elementAt(i).iterator();
			while (I.hasNext())
				   System.out.print(" " + I.next());
			System.out.println();
		}
	}	
	
    private void identifFrags() {
		
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
					String Tab = AttributCodage.elementAt(indAttr).GetTableAtt();
					String Attr = AttributCodage.elementAt(indAttr).GetNomAtt();
					att = new Attribut(Tab, Attr);
					att.valeurs = new Vector<Object>();
					att.type = AttributCodage.elementAt(indAttr).type;

					for (int k = 0; k < SouDomAttrCod.elementAt(indAttr).size(); k++)
						if (((Integer)((IntegerGene)comp.geneAt(k)).getAllele()).intValue() == ValSD ) 
						{
							//System.out.print(" "+SouDomAttrCod.elementAt(indAttr).elementAt(k));				
							att.valeurs.add(SouDomAttrCod.elementAt(indAttr).elementAt(k));							
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
					/*if(att.type.equals("entier"))
						for(int y=0; y<att.valeurs.size(); y++) 
						 { Intervalle I2 = (Intervalle) att.valeurs.elementAt(y);
					      // str+=" [ "+I2.inf+" - "+I2.sup+" ] ";
					       //System.out.print(" [ "+I2.inf+" - "+I2.sup+" ] ");
					       if(y==att.valeurs.size()-1) {System.out.println();}
						 } 
					
					else if(att.type.equals("reel"))
							for(int y=0; y<att.valeurs.size(); y++) 
							{ 	IReel I2 = (IReel) att.valeurs.elementAt(y);
								str += " [ "+I2.inf+" - "+I2.sup+" ] ";
								System.out.print(" [ "+I2.inf+" - "+I2.sup+" ] ");
								if(y == att.valeurs.size()-1) {str+="\n"; //System.out.println();}
							}			
						//si le type de l'attribut est string
				    	else if(att.type.equals("string"))    	
				    			for(int y=0; y<att.valeurs.size(); y++)
				    				{str += att.valeurs.elementAt(y)+"\n"; System.out.println(att.valeurs.elementAt(y)+"\n");}
			  */}
			}
		}
    }
    
    private void extraireSousSchBD() {
    	
		int nbFrag = 1;
		Vector<Integer> VMaxAtt = new Vector<Integer>();
		Vector<Integer> itChange = new Vector<Integer>();
		Vector<Integer> itCour = new Vector<Integer>();
		Vector<Integer> valCour = new Vector<Integer>();
		CompositeGene comp;
		Vector<Integer> ligne = new Vector<Integer>();
		SousSchBD = new Vector<String>();
		iSouSch = new Vector<Vector<Integer>>();

		/**calcul du nombre de fragments de chaque attribut**/
		for (int i = 0; i < PredFrags.size(); i++) {
			VMaxAtt.add(PredFrags.elementAt(i).size());
			nbFrag *= PredFrags.elementAt(i).size();
		}
         
		/**calcul pour chaque attribut le nbr d'iterations a faire avant de changer de valeur**/
		for (int i = 0; i < PredFrags.size(); i++) {
			nbFrag = 1;
			for (int j = i + 1; j < PredFrags.size(); j++)
				nbFrag *= VMaxAtt.elementAt(j);
			itChange.add(nbFrag);
		}

		/**on initialise les valeurs des iterations courantes et des vals courantes**/
		for (int i = 0; i < PredFrags.size(); i++) {
			itCour.add(0);
			valCour.add(1);
		}

		boolean Fin = false;
		int sc = 0;
		/** affichage des series de valeurs des sous schemas **/
		while (!Fin) {
			ligne.clear();
			for (int i = 0; i < PredFrags.size(); i++) {

				/** on incremente l'iteration courante **/
				int it = itCour.elementAt(i);
				it++;
				itCour.setElementAt(it, i);

				if (itCour.elementAt(i) <= itChange.elementAt(i)) {
					ligne.add(valCour.elementAt(i));
					//System.out.print(" " + valCour.elementAt(i));
				} else if (i == 0
						&& valCour.elementAt(i) == VMaxAtt.elementAt(i)) {
					Fin = true;
					break;
				} else if (i > 0
						&& valCour.elementAt(i) == VMaxAtt.elementAt(i)) {
					itCour.setElementAt(1, i);
					valCour.setElementAt(1, i);
					ligne.add(valCour.elementAt(i));
					//System.out.print(" " + valCour.elementAt(i));
				} else {
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
			
			String str = ""; Vector<Integer> v = new Vector<Integer>();
			/** On enregistre les ss Schemas **/
			for (int i = 0; i < ligne.size(); i++) {
				if(i>=1) str+="-"; 
				str+=ligne.elementAt(i);
				v.add(ligne.elementAt(i));
			}
			iSouSch.add(v);
			SousSchBD.add(str);
		}
          VMaxAtt=null; itChange=null; itCour=null; valCour=null; ligne=null; 
		
		/** Affichage des sous schema de l'entrepot de donn�es **/
       /* System.out.println("Les Sous Schemas de l'entrep�t de donn�es :");
        for (int i = 0; i < SousSchBD.size(); i++) 
        {
		            System.out.println(SousSchBD.elementAt(i));
        for(int j=0; j<iSouSch.elementAt(i).size(); j++) 
        	System.out.print(" "+iSouSch.elementAt(i).elementAt(j));
        System.out.println();
        }*/
	}	
    
    private void AjoutColBD() {
    	cmpt.fermerConnexion(); cmpt.seConnecter();
    	Statement st =null; ResultSet rs = null; String str="";
		Connection cn = this.cmpt.getConnection();
		int n, nb;
		WhereFragments = new Vector<Vector<String>>(PredFrags.size());
		
		/**parcours des tables**/
		for(int i=0; i< PredFrags.size(); i++)
		{
	   try { //on verifie si la colonne col existe deja ou non
		    st = cn.createStatement();
			rs=st.executeQuery("select count(*) from user_tab_columns where table_name = '"+tablesCandidates.elementAt(i).getNomTable()
					           +"' and Column_name = 'COL'");
			if(rs.next()) str = rs.getString("COUNT(*)");
		    n = Integer.parseInt(str);
		    
		    /**s'il nexiste pas de col alors on l'ajoute**/
		    if(n==0) 
		       rs=st.executeQuery("ALTER TABLE "+ tablesCandidates.elementAt(i).getNomTable()+" ADD COL Number ");
		    
		    } catch (SQLException e) {e.printStackTrace();}	
		    
		    /**parcours des fragments de chaque table**/
			Vector<String> whereFrag = new Vector<String>();
		    for(int j=0; j<PredFrags.elementAt(i).size(); j++) 	
			{ str=""; 
			/**parcours des predicats de chaque fragment**/
			for(int k=0; k<PredFrags.elementAt(i).elementAt(j).predicats.size(); k++) 
			{   if(k>=1) str+=" and ";
				Attribut att = PredFrags.elementAt(i).elementAt(j).predicats.elementAt(k);
				str+="(";
				for(int y=0; y<att.valeurs.size(); y++) 
				{   String c="";
					if(y>=1) str+=" OR ";
					if(att.type.equals("string")) c = (String)att.valeurs.elementAt(y);
					else if(att.type.equals("entier")){Intervalle I = (Intervalle) att.valeurs.elementAt(y);
					                                   c="("+att.GetNomAtt()+">="+I.inf+" and "+att.GetNomAtt()+"<="+I.sup+")";
					                                   }
					     else {IReel R = (IReel) att.valeurs.elementAt(y);
                               c="("+att.GetNomAtt()+">="+R.inf+" and "+att.GetNomAtt()+"<="+R.sup+")";
                              }
					if(c.indexOf("<>")!=-1)	str += c;
					else if(c.indexOf(">=")!=-1) str += c;
					else if(c.indexOf("IN")!=-1) {
						                            c= c.substring(0,c.indexOf("'"))+"("
					                                +c.substring(c.indexOf("'"),c.lastIndexOf("'")+1)+")";
						                            str+=att.GetNomAtt()+" "+c;
					                             }
					else str+=att.GetNomAtt()+c;
				}
				str+=")";
			}
			 /**Remplissage de la col de chaque fragment **/
			try {
			    st = cn.createStatement();
				rs=st.executeQuery("UPDATE "+tablesCandidates.elementAt(i).getNomTable()
						           +" SET COL="+(j+1)+" WHERE "+str);
			    } catch (SQLException e) {e.printStackTrace();}
			    
			whereFrag.add(str);
			System.out.println(str); 
			}
		       WhereFragments.add(whereFrag);
		}
		
		/**Remplir la colonne du Fait **/
		Table fait = this.cmpt.getTableFaits(); 
  try {	st = cn.createStatement();         
		rs=st.executeQuery("select count(*) from user_tab_columns " +
				           "where table_name = '"+fait.getNomTable()+
				           "' and Column_name = 'COLF'");
		if(rs.next()) str = rs.getString("COUNT(*)");
	    n = Integer.parseInt(str);
	    
	    /**s'il nexiste pas de col alors on l'ajoute**/
	    if(n==0) st.executeQuery("ALTER TABLE "+fait.getNomTable()+" ADD COLF VARCHAR2 (700) ");
		
		/**On r�cupere le nom des cl�s prim de chaque dim **/
		ClePrim = new Vector<String>(tablesCandidates.size());
		CleEtrang = new Vector<String>(tablesCandidates.size());			
		
		for(int i=0;i<tablesCandidates.size(); i++) {
		String PKdim="",FKfait=""; 
		
		PKdim = tablesCandidates.elementAt(i).getPK(); 
		ClePrim.add(PKdim);
		
		//on recupere la cl� etrang�re de la cl� primaire de la dim dans le fait
		FKfait=tablesCandidates.elementAt(i).getFKfait();
		CleEtrang.add(FKfait);
		                                             }
		
		     for(int i=0; i<SousSchBD.size(); i++) 
		     {
		    	 String s=""; 
		    	 for(int t=0; t<Cl�Etrang.size(); t++)
		    	 {
		    		 int id = iSouSch.elementAt(i).elementAt(t)-1;
		    		 if(t>=1) s+=" and ";
		    		 s+=Cl�Etrang.elementAt(t)+" IN (Select "+Cl�Prim.elementAt(t)+" from "+
		    		    tablesCandidates.elementAt(t).getNomTable()+
		    		    " Where "+WhereFragments.elementAt(t).elementAt(id)+")";
		    	 }
		     
		    	 st.executeQuery("update vente set COLF = '"+SousSchBD.elementAt(i)+"' "+
                                 "where "+s);
             }	
		
} catch (SQLException e) {e.printStackTrace();}  

}

    private void FragmenterED() {
    	cmpt.fermerConnexion(); cmpt.seConnecter();
    	 ResultSet rs = null; Connection cn = this.cmpt.getConnection(); 
    	 Table fait = this.cmpt.getTableFaits();
    	
    try {   
    		Statement st = cn.createStatement();
    		
    		/** verif si tab Fait existante alors la supprimer **/
    		//si table existante alors la supprimer
			rs = st.executeQuery("Select count(*) from user_tables where "+
					  			 "table_name = '"+fait.getNomTable().toUpperCase()+"F'");
			
			if(rs.next()) if(Integer.parseInt(rs.getString("count(*)"))>0) 
				st.executeQuery("DROP TABLE "+fait.getNomTable().toUpperCase()+"F");
    	
    	/** Creation des Fragments pour chaque table candidate **/		
         for(int i=0; i<tablesCandidates.size(); i++)
         {  
        	Table T = tablesCandidates.elementAt(i);
			String attributs = "", attS="";
			
			//si table candidate existante alors la supprimer
			rs = st.executeQuery("Select count(*) from user_tables where "+
					  			 "table_name = '"+tablesCandidates.elementAt(i).getNomTable().toUpperCase()+"F'");
			
			if(rs.next()) if(Integer.parseInt(rs.getString("count(*)"))>0) 
				st.executeQuery("DROP TABLE "+tablesCandidates.elementAt(i).getNomTable().toUpperCase()+"F");
			
			/** ecriture de l'attribut et de son type **/
			for(int j=0; j<T.getV_Attributs().size(); j++) 
			{
				Attribut a = T.getV_Attributs().elementAt(j);
				if(j>=1) {attributs+=", "; attS+=", ";} attS += a.GetNomAtt();
				attributs += a.GetNomAtt()+" "+a.GetTypeAtt();
				if(a.GetTypeAtt().contains("CHAR")) attributs+="(50)";
			}			
			
			//Ecriture de la cl� Prim
	         String Prim = "CONSTRAINT "+T.getNomTable()+"F_PK PRIMARY KEY ("+T.getPK()+")";
			
			String Partition = "";
			for(int j=0; j<PredFrags.elementAt(i).size();j++)
			{   if(j>=1) Partition+=", ";
				Partition+="Partition "+tablesCandidates.elementAt(i).getNomTable().toUpperCase()+(j+1)
				           +" Values ("+(j+1)+")";
			}
			if(!Partition.equals("")) Partition+=", ";
			Partition+=	"Partition "+tablesCandidates.elementAt(i).getNomTable().toUpperCase()
						+(PredFrags.elementAt(i).size()+1)
						+" Values (DEFAULT)";
			
			//Cr�ation de la tabele fragment�e
			System.out.println();
			System.out.println("CREATE TABLE "+tablesCandidates.elementAt(i).getNomTable().toUpperCase()+"F ("+
					           attributs+",\n"+Prim+")\n"+" PARTITION BY LIST (COL) "+"("+Partition+")");
			st.executeQuery("CREATE TABLE "+tablesCandidates.elementAt(i).getNomTable().toUpperCase()+"F ("+
					              attributs+",\n"+Prim+")\n"+" PARTITION BY LIST (COL) "+"("+Partition+")");
			
			/**remplir la table fragment�e **/
			System.out.println();
			System.out.println("insert into "+tablesCandidates.elementAt(i).getNomTable().toUpperCase()+"F ("+
		              attS+")"+" select "+attS+" from "+tablesCandidates.elementAt(i).getNomTable());
			
			st.executeQuery("insert into "+tablesCandidates.elementAt(i).getNomTable().toUpperCase()+"F ("+
		              attS+")"+" select "+attS+" from "+tablesCandidates.elementAt(i).getNomTable());
         }
         
         /**Creation des fragments pr la table des faits **/
			
         //Ecriture de la cl� Prim
         String Prim = "CONSTRAINT "+fait.getNomTable()+"F_PK PRIMARY KEY ("+fait.getPK()+")";
        	 
         //Ecriture des cl�s etrang�res         
         String Etr = ""; 
            for(int i=0; i<this.cmpt.getTables().size(); i++) {	
            Table T = this.cmpt.getTables().elementAt(i);
            if(T.getFKfait()!= null && !T.getFKfait().equals(""))
            {     	String s="";        
            
            if(indexTables.contains(T.getNomTable().toString()))	
        	s = "CONSTRAINT "+fait.getNomTable()+"F_FK"+(i+1)+" FOREIGN KEY ("+T.getFKfait()+")"+
        	           " References "+T.getNomTable()+"F ("+T.getPK()+")";
            
            else s = "CONSTRAINT "+fait.getNomTable()+"F_FK"+(i+1)+" FOREIGN KEY ("+T.getFKfait()+")"+
	           " References "+T.getNomTable()+" ("+T.getPK()+")";
        	Etr+=s;
        	Etr+=",";
        	if(i>=1) Etr+="\n";
            }
                                                                   }
            Etr=Etr.substring(0, Etr.lastIndexOf(","));
            
            //Ecriture de la requete de fragmentation
            String attributs = "", attS="";
            
            for(int j=0; j<fait.getV_Attributs().size(); j++) {
				Attribut a = fait.getV_Attributs().elementAt(j);
			  if(j>=1) {attributs+=", "; attS+=", ";} attS += a.GetNomAtt();
			  attributs+=a.GetNomAtt()+" "+a.GetTypeAtt();
			  if(a.GetTypeAtt().contains("CHAR")) attributs+="(50)";
			                  }			
			
			String Partition = "";
			for(int j=0; j<SousSchBD.size();j++)
			{   
				Partition+="Partition "+fait.getNomTable().toUpperCase()+(j+1)
				           +" Values ('"+(SousSchBD.elementAt(j))+"')";
				if(j<SousSchBD.size()-1) Partition+=", ";
				Partition+="\n";
			}			
			if(!Partition.equals("")) Partition+=", ";
			Partition+=	"Partition "+fait.getNomTable().toUpperCase()+(SousSchBD.size()+1)
						+" Values (DEFAULT)";
			
			//Creation de la table fragment�e
			System.out.println();
			System.out.println("CREATE TABLE "+fait.getNomTable().toUpperCase()+"F ("+
		              attributs+",\n"+Prim+",\n"+Etr+")"+
		              " PARTITION BY LIST (COLF) "+"("+Partition+")");
			
			st.executeQuery("CREATE TABLE "+fait.getNomTable().toUpperCase()+"F ("+
					              attributs+",\n"+Prim+",\n"+Etr+")"+
					              " PARTITION BY LIST (COLF) "+"("+Partition+")");
			
			/**remplir la table fragment�e **/
			System.out.println();
			System.out.println("insert into "+fait.getNomTable().toUpperCase()+"F ("+
		              attS+")"+" select "+attS+" from "+fait.getNomTable());
			
			st.executeQuery("insert into "+fait.getNomTable().toUpperCase()+"F ("+
		                     attS+")"+" select "+attS+" from "+fait.getNomTable());
            
		} catch (SQLException e) {e.printStackTrace();}
    
    }//fin fction
    
    private void ReecritureRequetes(){
    	this.SQLrequetes = new Vector<String>(this.requetes.size());
    	
    	/**Affichage des Fragments de chaque table*
		for(int h=0; h< PredFrags.size(); h++)
		{   System.out.println("Frgaments de "+tablesCandidates.elementAt(h).getNomTable()+" :");
			for(int j=0; j<PredFrags.elementAt(h).size(); j++) 
			{ System.out.println("Frgament N� "+(j+1)+" :");
			for(int k=0; k<PredFrags.elementAt(h).elementAt(j).predicats.size(); k++) 
			  {
				Attribut att = PredFrags.elementAt(h).elementAt(j).predicats.elementAt(k);
				System.out.println("Attribut = "+att.GetNomAtt());
				System.out.println("Ses Valeurs :");
				for(int y=0; y<att.valeurs.size(); y++)
					System.out.println(" "+att.valeurs.elementAt(y));
				System.out.println();
			   }
			}
		}*/
    	
    	for(int j=0; j<requetes.size(); j++) {
    			
    		/**Ecriture du Select de la requete **/
    		String chSelect = "Select "; 
    		for(int k=0; k<requetes.elementAt(j).Select.size(); k++)
    				{
    					if(k>=1) chSelect+=", ";
    					chSelect+=requetes.elementAt(j).Select.elementAt(k);
    				}
    		//ajout des agregats ds le Select
    		if(requetes.elementAt(j).AG)
    		for(int k=0; k<requetes.elementAt(j).opAG.size(); k++)
    		{
    			if(chSelect.length()>7) chSelect+=", "; //si existe attrs de selections
    			chSelect += requetes.elementAt(j).opAG.elementAt(k)+"("+requetes.elementAt(j).valAG.elementAt(k)+")";
    		}
    		
    		String idfFait = this.cmpt.getTableFaits().getNomTable();
    		Vector<Table> TabNonCSelect = new Vector<Table>();
        	
        	/** Determiner les tables non cand utilis�es que pour le SELECT **/
        	String ChTabNonCSelect ="", ChWhereTabNonCSelect="";
        	for(int i=0; i<requetes.elementAt(j).getTables().size(); i++)
        		if(!this.indexTables.contains(requetes.elementAt(j).getTables().elementAt(i).toUpperCase())
        && !this.cmpt.getTableFaits().getNomTable().equals(requetes.elementAt(j).getTables().elementAt(i).toUpperCase()))
        	{
        		String TabNonC = requetes.elementAt(j).getTables().elementAt(i).toUpperCase();
        		boolean exist = false;
        		
        		for(int b=0; b<requetes.elementAt(j).getBlocsOR().size() && !exist; b++)
        			for(int a=0; a<requetes.elementAt(j).getBlocsOR().elementAt(b).size(); a++)
        			{        				
        				Attribut att = requetes.elementAt(j).getBlocsOR().elementAt(b).elementAt(a);
        				if(att.GetTableAtt().equals(TabNonC)) {exist=true; break;} 
        			}
        		if(!exist) {	
        					if(!ChTabNonCSelect.equals("")) ChTabNonCSelect += ", "; 
        					ChTabNonCSelect += TabNonC;
        					
        					//recup�ration de la table
        					int indTab = this.cmpt.indexTables.indexOf(TabNonC);
        					Table tab = this.cmpt.getTables().elementAt(indTab);
        					TabNonCSelect.add(tab);
        					
        					//Ecriture des jointures des tab Non C utilis�es que pr le SELECT
        					if(!ChWhereTabNonCSelect.equals("")) ChWhereTabNonCSelect += ",\r\n"; 
        					ChWhereTabNonCSelect += idfFait+"."+tab.getFKfait()+" = "+TabNonC+"."+tab.getPK();
        					}
        	}
    		
    		/** Ecriture des sous requ�tes **/
    		String chReqSsSCH =""; int cp=0;
    		/*System.out.println();
    		System.out.println("La requete N�"+this.requetes.elementAt(j).getNumReq()+" :");
    		System.out.println();*/
    	for(int i=0; i<iSouSch.size(); i++) {
    		/*System.out.println();
    		System.out.println("Le sous schema "+SousSchBD.elementAt(i)+" :");
    		System.out.println();  	*/
    		if (Valide(requetes.elementAt(j),iSouSch.elementAt(i))) 
    		  {
    			System.out.println("Le sous schema "+SousSchBD.elementAt(i)+" n�"+(i+1)+" est valide pour la requete N�"+j);
    			if(cp>=1) chReqSsSCH+="\r\nUNION ALL\r\n"; 
    			chReqSsSCH += ReecritureRequeteSousSCH(requetes.elementAt(j), iSouSch.elementAt(i), TabNonCSelect);
    			cp++;
    		  }
    		else //System.out.println("Le sous schema "+SousSchBD.elementAt(i)+" n'est pas valide pour la requete N�"+j)
    		     ;
    	                                      }
    	/**Ecriture des groupement **/
    	String chGroup ="";
    	if(requetes.elementAt(j).Group)
    		chGroup+=requetes.elementAt(j).opGroup+" ("+requetes.elementAt(j).attGroup+")";    	
    	
    	/** Ecriture des jointures des tabNonC utilis�es que pr le SELECT **/	
    	String SQLreq="Resultat requete vide";    	
    	
    	if(ChTabNonCSelect.equals(""))
    		{if(!chReqSsSCH.equals("")) SQLreq = chSelect+" \r\nFROM (\r\n"+chReqSsSCH+"\r\n ) "+chGroup+";";}
    	else {if(!chReqSsSCH.equals("")) SQLreq = chSelect+" \r\nFROM (\r\n"+chReqSsSCH+"\r\n ) "+idfFait+", "+
    		  ChTabNonCSelect+"\r\nWhere "+ChWhereTabNonCSelect+"\r\n"+chGroup+";";}
    	SQLrequetes.add(SQLreq);
    	                                     }//fin parcours requetes
    	
    	//ecriture des requ�tes r�ecrites sur fichier
    	String nom, chemin = null; 
    	JFileChooser chooser = new JFileChooser(new File("C:\\Users\\SAMY\\grammaire\\"));
    	chooser.setDialogTitle("Veuiller indiquer un fichier pour les requ�tes r��crites");
    	chooser.setApproveButtonText("Charger");
    	FileNameExtensionFilter monFiltre = new FileNameExtensionFilter("Fichiers sql (*.txt)","txt");
    	chooser.addChoosableFileFilter(monFiltre);
    	   
    			if (chooser.showOpenDialog(null) == JFileChooser.OPEN_DIALOG)
    			{
    				nom = chooser.getSelectedFile().getName();
    				chemin = chooser.getSelectedFile().getAbsolutePath();
    			}
    			
    			FileWriter fw = null;
				try {fw = new FileWriter(chemin);} catch (IOException e1) {e1.printStackTrace();}
				
    			BufferedWriter bw = new BufferedWriter(fw);
    			
   for(int i=0; i<SQLrequetes.size(); i++) 
   {
	   System.out.println();
	   System.out.println("Affichage du code SQL de la req reecrite N�: "+i);
	   System.out.println();
	   System.out.println(SQLrequetes.elementAt(i));
	   try {bw.write(SQLrequetes.elementAt(i)+"\r\n\r\n\r\n");} catch (IOException e) {e.printStackTrace();}	
	   System.out.println();
    }		
		try {bw.close();} catch (IOException e) {e.printStackTrace();}
    }
    
    private boolean Valide(Requete Q, Vector<Integer> SousSCH) {
    	
    	/*System.out.println();
    	System.out.println("Fonction valide");
    	System.out.println();*/
    	
    	BlocsValides = new Vector<Integer>(); boolean  valide =false;
    	
    	//parcours des blocs ORs
    	for(int i=0; i<Q.getBlocsOR().size(); i++) 
    		if(existPredSsSchema(Q.getBlocsOR().elementAt(i), SousSCH))
    	{
    			//System.out.println("Bloc "+i+" valide"); System.out.println(); 
      	          valide = true; BlocsValides.add(i);
    			
    		//Parcours des attributs de chaque bloc OR 
    		/*int j;   		
    		for(j=0; j < Q.getBlocsOR().elementAt(i).size(); j++)
    		{
    			Attribut att = Q.getBlocsOR().elementAt(i).elementAt(j);
    			//System.out.println("att = "+att.GetNomAtt()+" sa table = "+att.GetTableAtt());
    			
    			int iTable = indexTables.indexOf(att.GetTableAtt().toUpperCase());
    			
    			/** verif si table de frag **
    			if(iTable!=-1) {    				
    			int iFragTable = SousSCH.elementAt(iTable)-1;
    			//System.out.println("Le Frag = "+SousSCH.elementAt(iTable));
    			
    			//recherche de l'attribut correspendant dans le fragment
    			int k;
    			//System.out.println(" iTable = "+iTable+" iFragTable = "+iFragTable);
    			//System.out.println("taille Vpred = "+PredFrags.elementAt(iTable).elementAt(iFragTable).predicats.size());
    			
    			for(k=0; k<PredFrags.elementAt(iTable).elementAt(iFragTable).predicats.size() && 
    			         !PredFrags.elementAt(iTable).elementAt(iFragTable).predicats.elementAt(k).GetNomAtt().toUpperCase()
    			         .equals(att.GetNomAtt().toUpperCase()); k++);
    			
    			/**verif si attr de fragmentation **
    			if(k<PredFrags.elementAt(iTable).elementAt(iFragTable).predicats.size())
    			  {
    			Attribut attFrag = PredFrags.elementAt(iTable).elementAt(iFragTable).predicats.elementAt(k);
    			
    			//Verification de l'existence des predicats de l'attributs de la requete ds l'attribut du frag correspendant 
    			String valAtt="";
    			for(k=0; k<att.valeurs.size(); k++) 
    			{
    				valAtt = (String)att.valeurs.elementAt(k); int g;
    				
    				for(g=0; g < attFrag.valeurs.size() ; g++) 
    				{ 
    					if(att.type.equals("string")) 
    					{
    				       String s = (String) attFrag.valeurs.elementAt(g);
    				       if(att.operateurs.elementAt(k).equals("IN") || att.operateurs.elementAt(k).equals("=")) 
    				       { int p= valAtt.indexOf("'"), n=valAtt.indexOf(",");
    				            while(n!=-1) 
    				            { String val = valAtt.substring(p,n);
    				    	      if(s.contains(val)) {//System.out.println("val "+val+" existentes");
    				    	      		                                     break; 
    				    	      		                                    }
    				    	      p=n+1; n = valAtt.indexOf(",",p); 
    				            }
    				       if(n==-1) { if(s.contains(valAtt.substring(p))) 
    				                     {//System.out.println("val "+valAtt.substring(p)+" existentes");
    				                      break;
    				                     }
    				                 
    				                 }
    				       else break;
    				       }
    				       else if(att.operateurs.elementAt(k).equals("<>") && (s.indexOf(",")!=-1 || s.indexOf((String)valAtt)==-1))     	   
    				            break;
    					}
    					else if(att.type.equals("entier"))
    					{   int val = Integer.parseInt(valAtt);
    					    Intervalle I = (Intervalle) attFrag.valeurs.elementAt(g);
    					    //System.out.println("valeurs entieres :");
    					    
    						//on verifie les diffs types d'operateurs			
    						if(att.operateurs.elementAt(k).equals(">")) 
    						if(I.sup > val) {//System.out.println("val "+att.operateurs.elementAt(k)+val+" existentes"); 
    						                 break;}
    						
    						if(att.operateurs.elementAt(k).equals("<")) 
    						if(I.inf < val) {//System.out.println("val "+att.operateurs.elementAt(k)+val+" existentes"); 
    						                 break;}
    						
    						if(att.operateurs.elementAt(k).equals(">=")) 
    						if(I.sup >= val) {//System.out.println("val "+att.operateurs.elementAt(k)+val+" existentes");
    						                  break;}
    						
    						if(att.operateurs.elementAt(k).equals("<=")) 
    						if(I.inf <= val) {//System.out.println("val "+att.operateurs.elementAt(k)+val+" existentes");
    						                  break;}
    						
    						if(att.operateurs.elementAt(k).equals("=")) 
    						if(I.inf <= val && I.sup>=val) {//System.out.println("val "+att.operateurs.elementAt(k)+val+" existentes");
    						                                break;}
    						
    						if(att.operateurs.elementAt(k).equals("<>")) 
    						if(I.inf != val || I.sup!=val) {//System.out.println("val "+att.operateurs.elementAt(k)+val+" existentes");
    						                                break;}
    			        }
    					
    					else if(att.type.equals("reel"))
					    {   double val = Double.parseDouble(valAtt);
					        IReel R = (IReel) attFrag.valeurs.elementAt(g);
					        //System.out.println("valeurs reelles :"+val);
					        
						    //on verifie les diffs types d'operateurs			
						    if(att.operateurs.elementAt(k).equals(">")) 
						    if(R.sup > val) {//System.out.println("val "+att.operateurs.elementAt(k)+val+" existentes>");
						                     break;}
						
						    if(att.operateurs.elementAt(k).equals("<")) 
						    if(R.inf < val) {//System.out.println("val "+att.operateurs.elementAt(k)+val+" existentes<");
						                     break;}
						
					    	if(att.operateurs.elementAt(k).equals(">=")) 
						    if(R.sup >= val) {//System.out.println("val "+att.operateurs.elementAt(k)+val+" existentes>=");
						                      break;}
						
						    if(att.operateurs.elementAt(k).equals("<=")) 
						    if(R.inf <= val) {//System.out.println("val "+att.operateurs.elementAt(k)+val+" existentes<=");
						                      break;}
						
						    if(att.operateurs.elementAt(k).equals("=")) 
						    if(R.inf <= val && R.sup>=val) {//System.out.println("val "+att.operateurs.elementAt(k)+val+" existentes=");
						                                    break;}
						
						    if(att.operateurs.elementAt(k).equals("<>")) 
						    if(R.inf != val || R.sup!=val) {//System.out.println("val "+att.operateurs.elementAt(k)+val+" existentes<>");
						                                    break;}
					   }
    				}
    					if(g==attFrag.valeurs.size()) break; 
    			 }
    			if(k<att.valeurs.size()) 
    			  {//System.out.println("val "+att.operateurs.elementAt(k)+" "+att.valeurs.elementAt(k)+" non existentes"); 
    			   break;}   
    			  }//verif si attr de frag
    			else nonDeFrag = true;
    		      }//verif si table de frag
    			 
    		    }
    	if(j<Q.getBlocsOR().elementAt(i).size()) {//System.out.println("Bloc "+i+" non valide"); System.out.println();
    		                                      }*/
    	}/** fin parcours des blocs OR **/
    	
    	if(valide) return true;
    	else return false;
    }

    private String ReecritureRequeteSousSCH(Requete Q, Vector<Integer> SousSCH, Vector<Table> TabNonCSelect) {
    	/*indexTables = new Vector<String>(tablesCandidates.size());
    	for(int i=0; i<tablesCandidates.size(); i++)
    		indexTables.add(tablesCandidates.elementAt(i).getNomTable().toUpperCase());*/
    	
    	Vector<Integer> EtatTables = new Vector<Integer>(Q.getTables().size());
    	for(int i=0; i<Q.getTables().size(); i++) EtatTables.add(-1);
    	
    	Vector<Vector<String>> predBloc = new Vector<Vector<String>>();
    	HashSet<String> Htab = new HashSet<String>();
    	Vector<String> HistoriqueAtt = new Vector<String>();
    	Vector<String> HistoriqueTab = new Vector<String>();
    	
    	//parcours des blocs ORs valides
    	for(int i=0; i<BlocsValides.size(); i++) 
    	{    
    		Vector<String> predB = new Vector<String>();
    		//Parcours des attributs de chaque bloc OR 
    		int j;   		
    		for(j=0; j < Q.getBlocsOR().elementAt(BlocsValides.elementAt(i)).size(); j++)
    		{   
    			//on reccupere l'attribut de la requete
    			Attribut att = Q.getBlocsOR().elementAt(BlocsValides.elementAt(i)).elementAt(j);
    			
    			int iT = Q.getTables().indexOf(att.GetTableAtt().toUpperCase());
    			int iTable = indexTables.indexOf(att.GetTableAtt().toUpperCase());
    			int k = 0; int iFragTable=0; boolean nonDeFrag =false;
    			
    			/**verif si table candidate **/
    			if(iTable!=-1)
    			{
    			 iFragTable = SousSCH.elementAt(iTable)-1;
    			
    			//recherche de l'attribut correspendant dans le fragment    			    			
    			for(k=0; k<PredFrags.elementAt(iTable).elementAt(iFragTable).predicats.size() && 
    			         !PredFrags.elementAt(iTable).elementAt(iFragTable).predicats.elementAt(k).GetNomAtt().toUpperCase()
    			         .equals(att.GetNomAtt().toUpperCase()); k++);
    			
    			/** verif si attr candidat**/
    			if(k<PredFrags.elementAt(iTable).elementAt(iFragTable).predicats.size())
    			{
    			//on recupere l'attribut de fragmentation
    			Attribut attFrag = PredFrags.elementAt(iTable).elementAt(iFragTable).predicats.elementAt(k);
    	
    			if(att.type.equals("string")) 
    		 {    			   			
    			//parcours des vals de l'attribut du fragment
				int f;
				for(f=0; f<attFrag.valeurs.size(); f++)
    			{   String s = (String) attFrag.valeurs.elementAt(f);
    			
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
    					   if(!valAtt.contains(s.substring(p,n))) {
    						   if(EtatTables.elementAt(iT)!=0) EtatTables.setElementAt(0, iT);
    						   if(att.operateurs.elementAt(k).equals("IN"))
    						        predB.add(att.GetNomAtt().toUpperCase()+" IN ("+att.valeurs.elementAt(k)+")");
    						   else predB.add(att.GetNomAtt().toUpperCase()+" = "+att.valeurs.elementAt(k));
    						        Htab.add(att.GetTableAtt().toUpperCase());
    						        HistoriqueAtt.add(att.GetNomAtt().toUpperCase()); 
    						        HistoriqueTab.add(att.GetTableAtt().toUpperCase());
    						        break;                        }
    					   
    					   p=n+1; n=s.indexOf(",",p);
    				      }
    				      
    				      if(n==-1) {  
    				    	if(!valAtt.contains(s.substring(p))) {     				    		
     						    if(EtatTables.elementAt(iT)!=0) EtatTables.setElementAt(0, iT);
    				    	    if(att.operateurs.elementAt(k).equals("IN"))
    				    	       predB.add(att.GetNomAtt().toUpperCase()+" IN ("+att.valeurs.elementAt(k)+")");
  						        else predB.add(att.GetNomAtt().toUpperCase()+" = "+att.valeurs.elementAt(k));
    				    	    Htab.add(att.GetTableAtt().toUpperCase());
    				    	    HistoriqueAtt.add(att.GetNomAtt().toUpperCase()); 
						        HistoriqueTab.add(att.GetTableAtt().toUpperCase());
    				    	         break;                       }
    				                }
    				      else break;
    	    		   }
    	    		   else if(s.contains(valAtt)) {predB.add(att.GetNomAtt().toUpperCase()+" <> "+att.valeurs.elementAt(k));
    	    		                                if(EtatTables.elementAt(iT)!=0) EtatTables.setElementAt(0, iT);
    	    		                                Htab.add(att.GetTableAtt().toUpperCase());
    	    		                                HistoriqueAtt.add(att.GetNomAtt().toUpperCase()); 
    	    	    						        HistoriqueTab.add(att.GetTableAtt().toUpperCase());
    	    		                                break; }
    			   } //fin parcours valeurs attribut requete
    				
    	    		if(k<att.valeurs.size()) break;
    			
    			} //fin parcours des vals attribut fragment    
				  if(f==attFrag.valeurs.size())				 
			         if(EtatTables.elementAt(iT)!=0) EtatTables.setElementAt(1, iT);
    			            
    	    }//fin type = string
    			
    			else if(att.type.equals("entier")) {
    				                                            // System.out.println("entier : "+att.GetNomAtt());
    				 /** parcours des vals attribut requete **/
				     for(int e=0; e<att.valeurs.size(); e++) 
					   
					   /** parcours des vals attribut du fragment **/
					   for(k=0; k<attFrag.valeurs.size(); k++)
    			{	int val = Integer.parseInt((String)att.valeurs.elementAt(e));
				    Intervalle I = (Intervalle) attFrag.valeurs.elementAt(k);
				   				    
					/** on verifie les diffs types d'operateurs **/			
					if(att.operateurs.elementAt(e).equals(">")) 
					if(I.inf <= val) {if(EtatTables.elementAt(iT)!=0) EtatTables.setElementAt(0, iT); 
					                  predB.add(att.GetNomAtt().toUpperCase()+" "+att.operateurs.elementAt(e)+" "+val);
					                  Htab.add(att.GetTableAtt().toUpperCase());
					                  HistoriqueAtt.add(att.GetNomAtt().toUpperCase()); 
	    						        HistoriqueTab.add(att.GetTableAtt().toUpperCase());
					                  break;}
					
					if(att.operateurs.elementAt(e).equals("<")) 
					if(I.sup >= val) {if(EtatTables.elementAt(iT)!=0) EtatTables.setElementAt(0, iT); 
	                                  predB.add(att.GetNomAtt().toUpperCase()+" "+att.operateurs.elementAt(e)+" "+val);
	                                  Htab.add(att.GetTableAtt().toUpperCase());
	                                  HistoriqueAtt.add(att.GetNomAtt().toUpperCase()); 
	    						        HistoriqueTab.add(att.GetTableAtt().toUpperCase());
	                                  break;}
					
					if(att.operateurs.elementAt(e).equals(">=")) 
					if(I.inf < val) {if(EtatTables.elementAt(iT)!=0) EtatTables.setElementAt(0, iT); 
                                     predB.add(att.GetNomAtt().toUpperCase()+" "+att.operateurs.elementAt(e)+" "+val);
                                     Htab.add(att.GetTableAtt().toUpperCase());
                                     HistoriqueAtt.add(att.GetNomAtt().toUpperCase()); 
     						        HistoriqueTab.add(att.GetTableAtt().toUpperCase());
                                     break;}
					
					if(att.operateurs.elementAt(e).equals("<=")) 
					if(I.sup > val) {if(EtatTables.elementAt(iT)!=0) EtatTables.setElementAt(0, iT); 
                                     predB.add(att.GetNomAtt().toUpperCase()+" "+att.operateurs.elementAt(e)+" "+val);
                                     Htab.add(att.GetTableAtt().toUpperCase());
                                     HistoriqueAtt.add(att.GetNomAtt().toUpperCase()); 
     						        HistoriqueTab.add(att.GetTableAtt().toUpperCase());
                                     break;}
					
					if(att.operateurs.elementAt(e).equals("=")) 
					if(I.inf != val || I.sup!=val) {if(EtatTables.elementAt(iT)!=0) EtatTables.setElementAt(0, iT); 
                                                    predB.add(att.GetNomAtt().toUpperCase()+" "+att.operateurs.elementAt(e)+" "+val);
                                                    Htab.add(att.GetTableAtt().toUpperCase());
                                                    HistoriqueAtt.add(att.GetNomAtt().toUpperCase()); 
    	    	    						        HistoriqueTab.add(att.GetTableAtt().toUpperCase());
                                                    break;}
					
					if(att.operateurs.elementAt(e).equals("<>")) 
					if(I.inf <= val && I.sup>=val) {if(EtatTables.elementAt(iT)!=0) EtatTables.setElementAt(0, iT); 
                                                    predB.add(att.GetNomAtt().toUpperCase()+" "+att.operateurs.elementAt(e)+" "+val);
                                                    Htab.add(att.GetTableAtt().toUpperCase());
                                                    HistoriqueAtt.add(att.GetNomAtt().toUpperCase()); 
    	    	    						        HistoriqueTab.add(att.GetTableAtt().toUpperCase());
                                                    break;}
		        }
    			if(k==attFrag.valeurs.size())				 
			         if(EtatTables.elementAt(iT)!=0) EtatTables.setElementAt(1, iT);
    			} //fin type = entier
    			
    		else if(att.type.equals("reel")) {
    				                                       // System.out.println("att : "+att.GetNomAtt()+" : "+att.type);
   				 /** parcours des vals attribut requete **/
				     for(int e=0; e<att.valeurs.size(); e++) 
					   
					   /** parcours des vals attribut du fragment **/
					   for(k=0; k<attFrag.valeurs.size(); k++)
   			   {	double val = Double.parseDouble((String)att.valeurs.elementAt(e));
				    IReel R = (IReel) attFrag.valeurs.elementAt(k);
				    				    
					/** on verifie les diffs types d'operateurs **/			
					if(att.operateurs.elementAt(e).equals(">")) 
					if(R.inf <= val) {//System.out.println("ecrit : > "+val);
						              if(EtatTables.elementAt(iT)!=0) EtatTables.setElementAt(0, iT); 
					                  predB.add(att.GetNomAtt().toUpperCase()+" "+att.operateurs.elementAt(e)+" "+val);
					                  Htab.add(att.GetTableAtt().toUpperCase());
					                  HistoriqueAtt.add(att.GetNomAtt().toUpperCase()); 
	    						      HistoriqueTab.add(att.GetTableAtt().toUpperCase());
					                  break;}
					
					if(att.operateurs.elementAt(e).equals("<")) 
					if(R.sup >= val) {if(EtatTables.elementAt(iT)!=0) EtatTables.setElementAt(0, iT); 
	                                  predB.add(att.GetNomAtt().toUpperCase()+" "+att.operateurs.elementAt(e)+" "+val);
	                                  Htab.add(att.GetTableAtt().toUpperCase());
	                                  HistoriqueAtt.add(att.GetNomAtt().toUpperCase()); 
	    						      HistoriqueTab.add(att.GetTableAtt().toUpperCase());
	                                  break;}
					
					if(att.operateurs.elementAt(e).equals(">=")) 
					if(R.inf < val) { //System.out.println("ecrit : >= "+val);
						            if(EtatTables.elementAt(iT)!=0) EtatTables.setElementAt(0, iT); 
                                    predB.add(att.GetNomAtt().toUpperCase()+" "+att.operateurs.elementAt(e)+" "+val);
                                    Htab.add(att.GetTableAtt().toUpperCase());
                                    HistoriqueAtt.add(att.GetNomAtt().toUpperCase()); 
    						        HistoriqueTab.add(att.GetTableAtt().toUpperCase());
                                    break;}
					
					if(att.operateurs.elementAt(e).equals("<=")) 
					if(R.sup > val) {if(EtatTables.elementAt(iT)!=0) EtatTables.setElementAt(0, iT); 
                                    predB.add(att.GetNomAtt().toUpperCase()+" "+att.operateurs.elementAt(e)+" "+val);
                                    Htab.add(att.GetTableAtt().toUpperCase());
                                    HistoriqueAtt.add(att.GetNomAtt().toUpperCase()); 
    						        HistoriqueTab.add(att.GetTableAtt().toUpperCase());
                                    break;}
					
					if(att.operateurs.elementAt(e).equals("=")) 
					if(R.inf != val || R.sup!=val) {if(EtatTables.elementAt(iT)!=0) EtatTables.setElementAt(0, iT); 
                                                   predB.add(att.GetNomAtt().toUpperCase()+" "+att.operateurs.elementAt(e)+" "+val);
                                                   Htab.add(att.GetTableAtt().toUpperCase());
                                                   HistoriqueAtt.add(att.GetNomAtt().toUpperCase()); 
   	    	    						           HistoriqueTab.add(att.GetTableAtt().toUpperCase());
                                                   break;}
					
					if(att.operateurs.elementAt(e).equals("<>")) 
					if(R.inf <= val && R.sup>=val) {if(EtatTables.elementAt(iT)!=0) EtatTables.setElementAt(0, iT); 
                                                   predB.add(att.GetNomAtt().toUpperCase()+" "+att.operateurs.elementAt(e)+" "+val);
                                                   Htab.add(att.GetTableAtt().toUpperCase());
                                                   HistoriqueAtt.add(att.GetNomAtt().toUpperCase()); 
   	    	    						           HistoriqueTab.add(att.GetTableAtt().toUpperCase());
                                                   break;}
		        }
   			if(k==attFrag.valeurs.size())				 
			         if(EtatTables.elementAt(iT)!=0) EtatTables.setElementAt(1, iT);
   			} //fin type = reel
    			}//fin verif si attr de frag
    			else nonDeFrag=true;
    			}//fin verif si table candidate
    		
    		if(iTable ==-1 || nonDeFrag)
    		{
    			for(k=0; k<att.valeurs.size(); k++)
    			if(att.operateurs.elementAt(k).equals("IN"))
    			predB.add(att.GetNomAtt().toUpperCase()+" "+att.operateurs.elementAt(k)+" ("+att.valeurs.elementAt(k)+")");
    			else
    			predB.add(att.GetNomAtt().toUpperCase()+" "+att.operateurs.elementAt(k)+" "+att.valeurs.elementAt(k));
    			Htab.add(att.GetTableAtt().toUpperCase());
    		}
    			
    		}//fin parcours attributs du bloc
    		
    		//si un bloc est pr�calcul� alors on suppr ts les predicats
    		int h;
    		for(h=0; h<EtatTables.size(); h++) 
    			if(EtatTables.elementAt(h)==1 && predB.size()==0) 
    			{ /* EtatTables.setElementAt(2, h);    			
    			   String nomTab = Q.getTables().elementAt(h).toUpperCase();
    			 for(int g=0; g<predBloc.size(); g++)
    				 for(int p=0; p<predBloc.elementAt(g).size(); p++)
    				 {
    					 String pred = predBloc.elementAt(g).elementAt(p);
    					 String nomAtt = pred.substring(0, pred.indexOf(" ")).trim().toUpperCase();
    					 //recherche de la table de l'attribut
    					 int iAtt = HistoriqueAtt.indexOf(nomAtt);
    					 String nomT = HistoriqueTab.elementAt(iAtt);
    					 if (nomTab.equals(nomT)) {//System.out.println("pred "+predBloc.elementAt(g).elementAt(p)+"supprim�");
    					 		                   predBloc.elementAt(g).removeElementAt(p); p--;}
    					 if(predBloc.elementAt(g).size()==0) {predBloc.removeElementAt(g); g--; break;}
    				 }*/
    				predBloc.removeAllElements(); break;
    			}
    			else if(EtatTables.elementAt(h)==0) EtatTables.setElementAt(-1, h);
    		if(h<EtatTables.size()) break;
    		if(predB.size()>0) predBloc.add(predB);
    	
    	}//fin parcours des blocs de la requete
    	
    	/**Ecriture des attributs du select**/
    	String chaineSelect ="";
    	//ajout des cl�s prim du fait ds le select des sous schemas
    	if(TabNonCSelect.size()>0)
    	for(int i=0; i<this.cmpt.getTables().size(); i++)
    		if(this.cmpt.getTables().elementAt(i).getFKfait()!=null
    													&& !this.cmpt.getTables().elementAt(i).getFKfait().equals("")) 
    			{
    				Table tab = this.cmpt.getTables().elementAt(i);
    				int iFait = Q.getTables().indexOf(this.cmpt.getTableFaits().getNomTable());
    				String idfFait = Q.getIDFs().elementAt(iFait);
    				if(!chaineSelect.equals("")) chaineSelect += ", ";
    				
    				if(!idfFait.equals(".")) chaineSelect += idfFait+"."+tab.getFKfait();
    				else chaineSelect += tab.getFKfait();
    			}
    	
    	for(int i=0; i<Q.Select.size(); i++) 
    	{ 	int j;
    		for(j=0; j<TabNonCSelect.size(); j++)
    			if(TabNonCSelect.elementAt(j).indexAttributs.contains(Q.Select.elementAt(i).toUpperCase())) break;
    		if(j==TabNonCSelect.size())
    			{if(!chaineSelect.equals(""))chaineSelect+=", "; chaineSelect+=Q.Select.elementAt(i);}
    	}
    	if(!chaineSelect.equals("") && Q.AG) chaineSelect+=", ";
    	for(int i=0; i<Q.valAG.size(); i++)  {if(i>=1)chaineSelect+=", "; chaineSelect+=Q.valAG.elementAt(i);}
    	
    	//ajout des tables des attributs du select
    	int h;
    	for(int j=0; j<Q.Select.size(); j++){
    	for(h=0; h<tablesCandidates.size() 
    	         && !tablesCandidates.elementAt(h).indexAttributs.contains(Q.Select.elementAt(j).toUpperCase()); h++);
    	if(h<tablesCandidates.size()) Htab.add(tablesCandidates.elementAt(h).getNomTable().toUpperCase());
    	else {	for(h=0; h<this.cmpt.getTables().size() 
             	&& !this.cmpt.getTables().elementAt(h).indexAttributs.contains(Q.Select.elementAt(j).toUpperCase()); h++);
    		Htab.add(this.cmpt.getTables().elementAt(h).getNomTable());
    	     }
    	                                    }    	
    	/** Ecriture des tables du From **/
    	//Recuperation de la table de  fait
    	Table fait = this.cmpt.getTableFaits();
    	
    	System.out.println("fait : "+fait.getNomTable().toUpperCase());
    	System.out.println("Req n� : "+Q.getNumReq()+" ses tables :");
    	for(int i=0; i<Q.getTables().size(); i++)
    		System.out.print("  "+Q.getTables().elementAt(i)+" "+Q.getIDFs().elementAt(i));
    	System.out.println();
    	
    	int iT = Q.getTables().indexOf(fait.getNomTable().toUpperCase());
    	if(iT!=-1) 
    		if(Q.getIDFs().elementAt(iT).equals(".")) Q.getIDFs().setElementAt(fait.getNomTable().substring(0,2), iT); 
    		
    	//Recuperation du numero du Ss Schema
    	int iSsSCH = this.iSouSch.indexOf(SousSCH);
    	
    	String idfFait = Q.getIDFs().elementAt(iT);
    	String chaineT = fait.getNomTable()+"F PARTITION ("+fait.getNomTable()+(iSsSCH+1)+") "+idfFait;
    	String chJoinT = "";
    	
    	/** Ecriture des tables � joindres et des pred de jointures **/
    	Iterator I = Htab.iterator(); int cp = 0;
    	while(I.hasNext())
    	{ 
    	  String s = (String) I.next();
    	  int i;  
    	  for(i=0; i<TabNonCSelect.size() && !TabNonCSelect.elementAt(i).getNomTable().equals(s); i++);
    	  if(i==TabNonCSelect.size())
    	  {
    		  //on recupere l'indice de la tab ds le from de la requete pr recuperer l'idf
    		  int iTQ = Q.getTables().indexOf(s.toUpperCase());
    		  if(Q.getIDFs().elementAt(iTQ).equals(".")) Q.getIDFs().setElementAt(s.substring(0,2), iTQ);
    		  String idfT = Q.getIDFs().elementAt(iTQ);
    	  
    		  //recuperation de l'indice de la table dans le Vect Tab Cand pr recuperer le num du frag corresp
    		  int iTC = indexTables.indexOf(s.toUpperCase());
    		  int iFragT = -1;
    		  if(iTC!=-1) iFragT = SousSCH.elementAt(iTC); 
    		  String ch= "";
    		  if(iFragT!=-1) ch = s+"F PARTITION ("+s+iFragT+") " + idfT;
    		  else ch = s +" "+ Q.getIDFs().elementAt(iTQ);//cas table non fragment�e
    	  
    		  chaineT+=", "+ch;
    		  if(cp>=1) chJoinT+= " and ";
    		  if(iTC!=-1) chJoinT+=idfFait+"."+CleEtrang.elementAt(iTC)+" = "+idfT+"."+ClePrim.elementAt(iTC);
    		  
                  else { 
    			  	int it= this.cmpt.indexTables.indexOf(s.toUpperCase());
    			  	String pk,fk;
    			  	pk = this.cmpt.getTables().elementAt(it).getPK();
    			  	fk = this.cmpt.getTables().elementAt(it).getFKfait();
    			  	chJoinT+=idfFait+"."+fk+" = "+idfT+"."+pk;
    	       		}
    		  cp++;
    	  }
    	}    	
    	//ecriture du where
    	String chaineWhere = chJoinT;
    	for(int i=0; i<predBloc.size(); i++) {
    		if(i==0) chaineWhere+=" and (";
    		if(i>=1) chaineWhere+=" OR ";
    		   for(int j=0; j< predBloc.elementAt(i).size(); j++)
    	   {if(j>=1) chaineWhere+=" and "; chaineWhere+=predBloc.elementAt(i).elementAt(j);}
    	                                      }
    	if(predBloc.size()>0) chaineWhere+=")";
    	
    	String reqSsSCH = "Select "+chaineSelect+"\n"+
    	                  "FROM "+chaineT+"\n"+
    	                  "Where "+chaineWhere;
    	                                          	
    /*	System.out.println();
    	System.out.println("Affichage des preds requetes du Sous SCH : ");
    	System.out.println();
    	System.out.println(reqSsSCH);*/
    	
    	return reqSsSCH;
    }
    
    private boolean existPredSsSchema(Vector<Attribut> Bloc, Vector<Integer> C) 
    {
    	//System.out.println("ExistPredSsSchema");
    	//System.out.println("attribut : "+attribut.GetNomAtt());
	  /** On verifie par une requete l'existence du predicat ds ce ss schema **/
	   
    	cptCloseConnect++;
	   Table fait = this.cmpt.getTableFaits();
	   Statement st = null; ResultSet rs;	   
	   Long nbTatt = null; 
	   String s="", sd=""; 
	   if(cptCloseConnect==100){ cmpt.fermerConnexion(); cmpt.seConnecter(); cptCloseConnect=0;}
	   Connection cn = cmpt.getConnection(); 
	   
	   /** on ecrit les pred de selection concernant le ss schema **/
	   
	   //Parcours des fragments des tables candidates
	   for(int is=0; is<C.size(); is++)
	       {  
		    if(is>=1) s+=" and ";
		    int iF = C.elementAt(is); //indice du fragment de la table correspendante	
		    //System.out.println("iF = "+iF);
		    
		    //parcours des predicats des fragments
	        for(int ia=0; ia<PredFrags.elementAt(is).elementAt(iF-1).predicats.size(); ia++)
	    	   { 
	        	if(ia==0) s+="(";
		    	if(ia>= 1) s+= ") and ("; int cp=0; 
	        	Attribut att = PredFrags.elementAt(is).elementAt(iF-1).predicats.elementAt(ia);	    	      
	    	     
	 	    	 if(att.type.equals("string")) 
	    	       for(int i=0; i<att.valeurs.size(); i++) 
	    	    {  
	    	    	if(cp>=1) s+=" or ";
	    	        sd =  (String) att.valeurs.elementAt(i);
	    	        if(sd.indexOf("IN")!=-1) sd=sd.substring(0,sd.indexOf("'")).trim()+" ("
	    	                                    +sd.substring(sd.indexOf("'")).trim()+")";
	    	        else if(sd.indexOf(",")!=-1) //Cas du Reste chang�
	    	               {	    	        	     
	    	        	     int p=sd.indexOf(",");;
	    	        	     while(p!=-1)
	    	        	     {
	    	        	    	sd=sd.substring(0,p)+" OR "+att.GetNomAtt()+sd.substring(p+1);
	    	        	    	p=sd.indexOf(",",p+1);
	    	        	     }
	    	               }
	    	        cp++;
	    	        s+=att.GetNomAtt()+" "+sd;
	    	     }
	 	    	 
	    	else if(att.type.equals("entier"))	    		
	    		   for(int i=0; i<att.valeurs.size(); i++) {
	    			   if(cp>=1) s+=" or ";
	    			   		sd = ">="+((Intervalle)att.valeurs.elementAt(i)).inf+" and "+
	    			   		att.GetNomAtt()+"<="+((Intervalle)att.valeurs.elementAt(i)).sup;
	    			   		cp++;
	    			   		s+=att.GetNomAtt()+" "+sd;
	    		                                            }
	    	
	    	else if(att.type.equals("reel")) 
	    		   for(int i=0; i<att.valeurs.size(); i++) {
	    			   if(cp>=1) s+=" or ";
	    			   	sd = ">="+((IReel)att.valeurs.elementAt(i)).inf+" and "+
	    			   	att.GetNomAtt()+"<="+((IReel)att.valeurs.elementAt(i)).sup;
	    			   	cp++;
	    			   	s+=att.GetNomAtt()+" "+sd;
	    		                                            }	  	    		    	
	             }//fin parcours predicats fragments	  
	        s+=")";
          }//fin parcours des fragments des tab candidates		    
	   
	    HashSet<String> TabAjout = new HashSet<String>();
	    
		   /** On ecrit les pred du bloc de la requete **/
		    if(!s.equals("")) s+=" and ";
		    for(int i=0; i<Bloc.size(); i++) {
		    	if(i>=1) s+=" and ";
		    for(int is=0; is<Bloc.elementAt(i).valeurs.size(); is++) 
		    { 
		    	if(is>=1) s+=" and ";
		    	if(Bloc.elementAt(i).operateurs.elementAt(is).equals("IN"))
		    		s += Bloc.elementAt(i).GetNomAtt()+" "+Bloc.elementAt(i).operateurs.elementAt(is)
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
		        System.out.println("table = "+T);
	            Table TattReq = this.cmpt.getTables().elementAt(this.cmpt.indexTables.indexOf(T));
	            tables+=", "+TattReq.getNomTable();
		    	join+=" and "+fait.getNomTable()+"."+TattReq.getFKfait()+"="+TattReq.getNomTable()+"."+TattReq.getPK();
		    }
	   
  try {
		st = cn.createStatement();
        
		/*System.out.println("select count(*) from "+fait.getNomTable()+tables+
	                       " where "+join+" and "+s);*/
		rs = st.executeQuery("select count(*) from "+fait.getNomTable()+tables+
				             " where "+join+" and "+s);
		if(rs.next())
		nbTatt = Long.parseLong(rs.getString("count(*)"));
		if(nbTatt==0) {	//System.out.println("ss schema non valide pour "+attribut.GetNomAtt()); 
		               	return false;
		              }
		st.close(); rs.close();
	   } catch (SQLException e) {e.printStackTrace();}
	
	return true;
}
 } 
