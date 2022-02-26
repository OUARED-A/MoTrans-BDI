package Environnement;

import java.awt.Component;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Observable;
import java.util.Vector;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JOptionPane;

//import Control.EDDCtrl;
import Environnement.Table;


public class Compte extends Observable{
	
	public final static String CONNECTION_OUVRIR_MESSAGE = "Connecxion";
	public final static String EXTRACTION_MESSAGE = "Extraction des donn�es";
	public final static String CONNECTION_FERMER_MESSAGE = "D�connexion";
	public final static String AJOUT_REQUETES_UTILISATEUR_MESSAGE = "Ajout des requ�tes";
	public final static String EXTRACTION_FIN_MESSAGE = "Extraction termin�e";
	public final static String CONFIG_EDD="Configuration de l'entrepot";
	public final static String CHOIX_TECH="Choix des techniques";
	public final static String Extraire_Vals_Distincts ="extraire les valeurs dictinct d'un attributs e parir de l'ED";
	public final static String MAX_VAL ="extraire la valeur Max d'une valeur e partir de l'ED";
    public final static String Enreg_TabsC_AttrsC ="enregistrer les tables candidates et les attributs candidats";
    public final static String Extraire_Tabs_ED ="extraire les tables de l'ED s�lection�";
    
	private User user = null;
    private String login = null;
    private ServeurBD base = null;
    private Connection connect = null;
    private DatabaseMetaData dma= null;
    private Vector<Table> tables = null;
    private Table faits=null;
    private Vector<Table> TabEDD=null;
    public Vector<String> indexTables = null;
    private Vector<String> ListeEDs = null;
    private Vector<String> ListeEDsCharges = new Vector<String>();
    private Vector<Vector<Table>> DimsEDs = new Vector<Vector<Table>>();
   

    public Vector<Table> getTabEDD() {
		return TabEDD;
	}

	public Table GetFaitsEDD(){
		Table t; int k;
		for(k=0;((k<TabEDD.size())&&(!TabEDD.elementAt(k).getTypeTable().equals("Fait")));k++);
		t=TabEDD.elementAt(k);
		return(t);
	}
	
	public Compte(User user, ServeurBD base) {
      this.user = user;
      this.base= base;
      this.login = this.user.getLogin();
    }
  
  public Compte(){

  }

 //permet de passer en param�tre :le login et le mot de passe (de l'interface)
  public void setUser(User user){
        this.user = user;
        this.login = this.user.getLogin();
    }

  //etablir la connexion selon le SGBD
  public void seConnecter(){
     if(this.base.getTypeSGBD().equals("Oracle")){
            try {
                Class.forName("oracle.jdbc.driver.OracleDriver");
            } catch (ClassNotFoundException ex) {
                Logger.getLogger(Compte.class.getName()).log(Level.SEVERE, null, ex);
                JOptionPane.showMessageDialog(null, ex.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
                return;
            }
            }
      else if(this.base.getTypeSGBD().equals("MySQL")){
           try {
                Class.forName("com.mysql.jdbc.Driver");
            } catch (ClassNotFoundException ex) {
                Logger.getLogger(Compte.class.getName()).log(Level.SEVERE, null, ex);
                JOptionPane.showMessageDialog(null, ex.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
                return;
            }
      }
      else if(this.base.getTypeSGBD().equals("SQL Server")){
    	  try {
              Class.forName("a refaire");
          } catch (ClassNotFoundException ex) {
              Logger.getLogger(Compte.class.getName()).log(Level.SEVERE, null, ex);
              JOptionPane.showMessageDialog(null, ex.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
              return;
          }
      }
     		//r�cup�ration de l'URL
            String url = this.base.recupURL();
            
            //r�cup�ration des infos de l'utilisateur
            String utilisateur = this.user.getLogin();
            char[] mp = this.user.getPassword();
           
            String code = "";
            for(int j=0;j< mp.length; j++) code = code + mp[j];
            
            try {
                this.connect = DriverManager.getConnection(url, utilisateur, code);
                this.dma = this.connect.getMetaData();
                
            } catch (SQLException ex) {
                System.out.println("erreur: getConnection");
                Logger.getLogger(Compte.class.getName()).log(Level.SEVERE, null, ex);
                JOptionPane.showMessageDialog(null, ex.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
                return;
            }
  }

  //extraction des informations n�cessaires de la M�tadonn�e
  public void extraire(String ED)
  {
      if(this.connect!=null)
      if(!this.ListeEDsCharges.contains(ED))
      {
    	  System.out.println("extraction de l'ED : "+ED);
    	  
    	  if(this.tables==null) this.tables = new Vector<Table>();
    	  /**extraction des tables ainsi que les att's ac ttes les infos**/
    	  Statement st;
		try {
			st = this.connect.createStatement();
		
		//verif si l'ED existe dans la base
          ResultSet resultat = st.executeQuery( "select count(*) from Entrepots "+
          										"where Nom = '"+ED+"'");
          if(resultat.next() && resultat.getInt("count(*)")>0)
        	  {System.out.println("extraction de BDD Infos"); this.tables.addAll(extraireTablesBD(ED));}
          else {
        	  		st.executeQuery("INSERT INTO Entrepots VALUES (sq_Entrepots.NEXTVAL, '"+ED+"')");
        	  		System.out.println("extraction nouvelle de l'ED");
        	  		this.tables.addAll(extraireTables(ED));        	  		
          		}
          } catch (SQLException e) {e.printStackTrace();}
          
          this.ListeEDsCharges.add(ED);//enreg de l'ED charg�
      } 
      else DefTablesED(ED);
  }
 
  private Vector<Table> extraireTablesBD(String ED) {
	  Vector<Table> mesTables = new Vector<Table>();
      this.TabEDD = new Vector<Table>();
      if(indexTables==null) indexTables = new Vector<String>();
      try {
            Statement st = this.connect.createStatement();
            ResultSet resultat = st.executeQuery("select nom,TypeTable,FKFAIT,PK,NbFK,NbTuples,TailleTuple,Fragment�e "
            									+"from Tables "+"where NOMED = '"+ED+"'");
            
            Table tab = null;
            String NomT;
            //r�cup�ration des noms de tables
            while(resultat.next()) 
            {
                NomT= resultat.getString("nom");
            //sauvegarde des tables
            if(NomT!=null){
            	tab = new Table(NomT);
            	this.indexTables.add(NomT);
            	
            	//r�cup�ration des attributs
            	Vector<Attribut> att = extraireAttributsBD(NomT);
                tab.SetV_Attributs(att); 
                
                tab.set_NbFK(Integer.parseInt(resultat.getString("nbFK"))); 
                tab.SetNb_Tuple(Integer.parseInt(resultat.getString("nbTuples")));                
                tab.setTailleTuple(Integer.parseInt(resultat.getString("TailleTuple"))); 
                tab.setInfoFrag(resultat.getString("Fragment�e"));
                tab.SetTypeTable(resultat.getString("TypeTable"));
                //determiner la table des faits 
                if(tab.getTypeTable().equals("Fait")) {setTypeTableFait(tab); this.faits = tab;}
                                
                /**D�finir la cl� primaire**/
                tab.setPK(resultat.getString("PK"));
                
                /**D�finir la cl� �trang�re dans le fait **/
                tab.setFKfait(resultat.getString("FKFAIT"));
                
                System.out.println("Tab nom : "+NomT+" nbFK : "+tab.get_NbFK()+" nbTup : "+tab.getNb_Tuple()+" TailleTup : "+
                tab.getTailleTuple()+" fragment�e : "+tab.getInfoFrag()+" TypeTable : "+tab.getTypeTable());
                
                this.TabEDD.add(tab); //enregistrement des tables de l'ED
                
                mesTables.addElement(tab);
               }
            else  break;
            }
                DimsEDs.add(TabEDD);
                
          } catch (SQLException ex) {
            System.err.println(ex.getMessage());
            JOptionPane.showMessageDialog(null, ex.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE); return(null);
        						  	}
        return(mesTables);
  }
  
  private Vector<Attribut> extraireAttributsBD(String nomTable)
  {
	  Vector<Attribut> V = new Vector<Attribut>();
      
      try {
    	  	Statement st = connect.createStatement();
            ResultSet resultat = st.executeQuery("select nom,TypeCl�,Taille,TypeBD,TypeAdmin,cardinalite "
					+"from Attributs "+"where NOMTABLE = '"+nomTable+"'");
            
            Attribut att = null;
            String NomAtt; 
            String TypeAtt;
            long taille = 0;
            //r�cup�ration des att's
            while (resultat.next()) 
            {
                NomAtt = resultat.getString("nom");
                    
                taille = Long.parseLong(resultat.getString("Taille"));
                //sauvegarde des att's: nom, taille, type, nature
                att = new Attribut(nomTable,NomAtt);
                
                att.SetTailleAtt(taille);

                TypeAtt = resultat.getString("TYPEBD");
                att.SetTypeAtt(TypeAtt);
                
                att.type=resultat.getString("TYPEADMIN");;
                                                
                att.SetNatureAtt(resultat.getString("TypeCl�"));
                att.setCardinalite(resultat.getLong("cardinalite"));
                
                System.out.println("att : "+att.GetNomAtt()+" "+att.GetNatureAtt()+" "+att.GetTailleAtt()+" "+att.type);
                
                V.addElement(att);
            }
        } catch (SQLException ex) 
        	{Logger.getLogger(Compte.class.getName()).log(Level.SEVERE, null, ex);}
        
        return V;
  }
  
  private void DefTablesED(String ED)
  { 
	  this.TabEDD = DimsEDs.elementAt(ListeEDsCharges.indexOf(ED));
	  
	  this.faits = this.getTables().elementAt(this.indexTables.indexOf(ED));
      if(faits!=null) setTypeTableFait(faits);
      System.out.println("Fait : "+this.faits.getNomTable());
  }
  
  public Connection getConnection() {
	  return this.connect;
  }

  private Vector<Table> extraireTables(String ED){
      Vector<Table> mesTables = new Vector<Table>();
      this.TabEDD = new Vector<Table>();
      if(indexTables==null) indexTables = new Vector<String>();
        try {
            //ResultSet resultat = this.dma.getTables(null,this.login,"%",types);
            Statement st = this.connect.createStatement(), st2 = this.connect.createStatement();
            
            Table tab = null;
            String NomT;
            
            /** Ajout de la table de fait de l'entrep�t **/
            NomT = ED;
            //sauvegarde des tables
            if(NomT!=null){
            	tab = new Table(NomT);
            	this.indexTables.add(NomT);    
            	
            	//r�cup�ration des attributs
            	Vector<Attribut> att = extraireAttributs(NomT);
                tab.SetV_Attributs(att);                
                tab.Calcul_NbFK();                
                long nbtup = this.calcul_NbTuple(NomT);
                tab.SetNb_Tuple(nbtup);                
                tab.CalculTailleTuple();   
                tab.setInfoFrag(determineFrag(NomT.toUpperCase()));
                
                /**D�finir la cl� primaire**/
                tab.setPK(this.getPK(tab));
                
                /**determiner la table des faits**/
                setTypeTableFait(tab);
                this.faits = tab;
                System.out.println("extr Fait : "+tab.getNomTable());
                
                st.executeQuery("INSERT INTO Tables VALUES (sq_Tables.NEXTVAL, '"+NomT+"','"+ED+"','"+tab.getTypeTable()+
                		"','"+tab.getFKfait()+"','"+tab.getPK()+"','"+tab.get_NbFK()+"','"+tab.getNb_Tuple()+"','"+
                		tab.getTailleTuple()+"','"+tab.getInfoFrag()+"')");
                
                this.TabEDD.add(tab); //enreg le fait ds l'ens des tables de l'ED
                mesTables.addElement(tab);
            }
            
            ResultSet resultat = st.executeQuery("select a.table_name from user_cons_columns a, user_constraints b "+
            								"where b.r_constraint_name = a.constraint_name and b.table_name = '"+ED+"'");
            
            /** Ajout des tables de dimensions **/
            while(resultat.next()) 
            {
                NomT= resultat.getString("table_name");
                System.out.println("Nom nouv Tab "+NomT);
            //sauvegarde des tables
            if(NomT!=null){
            	tab = new Table(NomT);
            	this.indexTables.add(NomT);
            	
            	//r�cup�ration des attributs
            	Vector<Attribut> att = extraireAttributs(NomT);
                tab.SetV_Attributs(att);                
                tab.Calcul_NbFK();                
                long nbtup = this.calcul_NbTuple(NomT);
                tab.SetNb_Tuple(nbtup);                
                tab.CalculTailleTuple(); 
                tab.setInfoFrag(determineFrag(NomT.toUpperCase()));
                /**D�finir la cl� primaire**/
                tab.setPK(this.getPK(tab));
                
                /**D�finir la cl� �trang�re dans le fait **/
                tab.setFKfait(getFkfait(tab));
                
                st2.executeQuery("INSERT INTO Tables VALUES (sq_Tables.NEXTVAL, '"+NomT+"', '"+ED+"','"+tab.getTypeTable()+
                		"','"+tab.getFKfait()+"','"+tab.getPK()+"','"+tab.get_NbFK()+"','"+tab.getNb_Tuple()+"','"+
                		tab.getTailleTuple()+"','"+tab.getInfoFrag()+"')");
                
                System.out.println("ExtrNouv nom : "+NomT+" nbFK : "+tab.get_NbFK()+" nbTup : "+tab.getNb_Tuple()+" TailleTup : "+
                        tab.getTailleTuple()+" fragment�e : "+tab.getInfoFrag()+" TypeTable : "+tab.getTypeTable());
                
                this.TabEDD.add(tab); //enregistrement des tables de l'ED
                mesTables.addElement(tab);
               }
            else  break;
            }           
        } catch (SQLException ex) {
            System.err.println(ex.getMessage());
            JOptionPane.showMessageDialog(null, ex.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
            return(null);
        						  }
        DimsEDs.add(TabEDD);
        
        return(mesTables);
  }
  
  //extraction des attributs de chaque table de la m�tadonn�e
  private Vector<Attribut> extraireAttributs(String nomTable){
      Vector<Attribut> V = new Vector<Attribut>();
      
      try {
    	  	Statement st = connect.createStatement();
            ResultSet resultat = this.dma.getColumns(null, this.login, nomTable, "%");
            
            Attribut att = null;
            String NomAtt; 
            String TypeAtt;
            long taille = 0;
            //r�cup�ration des att's
            while (resultat.next()) 
            {
                NomAtt = resultat.getString(4);
                    
                taille = Long.parseLong(resultat.getString("CHAR_OCTET_LENGTH"));
                //sauvegarde des att's: nom, taille, type, nature
                att = new Attribut(nomTable,NomAtt);
                
                att.SetTailleAtt(taille);

                TypeAtt = resultat.getString("TYPE_NAME");
                att.SetTypeAtt(TypeAtt);
                
                ResultSet rs = st.executeQuery(	"select Data_scale from user_tab_columns" +
                        						" where table_name = '"+nomTable+"' and column_name ='"+NomAtt+"'");
                //ajout� le type e chaque attribut                
                if(rs.next() && rs.getString("Data_scale")!=null && !rs.getString("Data_scale").equals("0")) att.type="reel";
                else if(TypeAtt.equals("VARCHAR2") || TypeAtt.equals("VARCHAR") || TypeAtt.equals("CHAR"))
                		att.type = "string";
                	else if (TypeAtt.equals("NUMBER")) att.type="entier";
                     	else if (TypeAtt.equals("FLOAT")||TypeAtt.equals("REAL")) att.type="reel";
                                                
                String nature = Recup_NatureAttributs(nomTable.toUpperCase(),NomAtt.toUpperCase());
                att.SetNatureAtt(nature);
                
                System.out.println("att : "+att.GetNomAtt()+" "+att.GetNatureAtt()+" "+att.GetTailleAtt()+" "+att.type);
                
                String requete ="SELECT COUNT(DISTINCT("+NomAtt+")) AS cardinalite FROM "+nomTable;
                ResultSet res = st.executeQuery(requete);
                if(res.next()) {
                   long val = res.getInt("cardinalite");
                    att.setCardinalite(val);
                }
                
                st.executeQuery("INSERT INTO Attributs VALUES (sq_Attributs.NEXTVAL, '"+NomAtt+"','"+nature+"','"+nomTable+
                		"','"+taille+"','"+TypeAtt+"','"+att.type+"','"+att.getCardinalite()+"')");
                V.addElement(att);
            }
        } catch (SQLException ex) 
        	{Logger.getLogger(Compte.class.getName()).log(Level.SEVERE, null, ex);}
        
        return V;
  }

  //r�cup�ration des types d'attributs
  private String Recup_NatureAttributs(String nomTable, String nomAttribut) {
	  boolean pk=false, fk=false;
        try {
        	//r�cup�ration des cl�s primaires de la table et comparaison avec l'att
        	Statement st = connect.createStatement();
            ResultSet resultat = st.executeQuery("select U1.CONSTRAINT_TYPE " +
            		                             "from user_constraints U1, user_cons_columns U2 "+
                                                 "where U1.CONSTRAINT_NAME = U2.CONSTRAINT_NAME "+
            		                             "and U2.COLUMN_NAME='"+nomAttribut+"'"+
            		                             " and U2.Table_name='"+nomTable+"'");
            
            while(resultat.next()) {
                if (resultat.getString("CONSTRAINT_TYPE").equals("P")) 
                    pk=true;                
            
                else if (resultat.getString("CONSTRAINT_TYPE").equals("R")) 
                    fk = true;
            }
            st.close(); resultat.close();
            } catch (SQLException ex) {
                                       Logger.getLogger(Compte.class.getName()).log(Level.SEVERE, null, ex);
                                       return(null);
                                      }
        if(pk && fk) return "PF";
        else  if(pk) return "PK";
        else if(fk) return "FK";
        return "non_cl�";
    }

  
  private void calculCard() {
      String nomTable, nomAttribut;
      java.sql.Statement stm=null;
      
      for(int i=0; i<this.tables.size(); i++)
    	  //on verifie si elle n'est pas calcul�e avant
    	  if(this.tables.elementAt(i).getV_Attributs().elementAt(0).getCardinalite()==0)
          for(int j=0; j<this.tables.elementAt(i).getV_Attributs().size(); j++){
                  nomTable = this.tables.elementAt(i).getNomTable();
                  nomAttribut = this.tables.elementAt(i).getV_Attributs().elementAt(j).GetNomAtt();
                  try {
                      String requete ="SELECT COUNT(DISTINCT("+nomAttribut+")) AS cardinalite FROM "+nomTable;
                      stm = connect.createStatement();
                      ResultSet res = stm.executeQuery(requete);
                      if(res.next()) {
                          long val = res.getInt("cardinalite");
                          Attribut t =this.tables.elementAt(i).getV_Attributs().elementAt(j);
                          t.setCardinalite(val);
                      }
                  } catch (SQLException ex) {
                      Logger.getLogger(Compte.class.getName()).log(Level.SEVERE, null, ex);
                      System.out.println("erreur requete cardinalité");
                  }
              }
  }
  
  private String determineFrag(String nomTab)
  {
	  Statement st =null; ResultSet rs = null; String str="";
	  try {
		  st = (Statement) this.connect.createStatement();
		rs  =  st.executeQuery(	"select Partitioned from USER_TABLES"+ 
				  				" where TABLE_NAME='"+nomTab+"'");
		
		if(rs.next()) str = rs.getString("Partitioned");		
	      } catch (SQLException e) {e.printStackTrace();}
	      
return str;
  }
  
  public Table calculTableFaits(){
      Table Tmax = null; 
      int max = 0;
      int size = this.tables.size();
      Table t = null;
      for(int i=0; i<size; i++){
          t =  this.tables.elementAt(i);
          if(t.get_NbFK()> max) {max = t.get_NbFK(); Tmax = t;}
      }
      return(Tmax);
  }
  
  public void setTypeTableFait(Table t){
	  t.SetTypeTable("Fait");
  }
  
  private long calcul_NbTuple(String nomTable)
  {
      long nb_T= 0;
      try {
      		Statement st = this.getConnection().createStatement();
      		ResultSet rs = 	st.executeQuery("select count(*) from "+nomTable);	
      		if(rs.next())
              nb_T = Long.parseLong(rs.getString("count(*)"));
      		st.close(); rs.close();
          } catch (SQLException ex) 
 {Logger.getLogger(Compte.class.getName()).log(Level.SEVERE, null, ex); return(-1);}
           
      return (nb_T);
  }
  
  public void setTables(Vector<Table> tables) {
	this.tables = tables;
}

  public Vector<Table> getTables(){return(this.tables);}

  public void afficherTables(){
       int nb = this.tables.size();Table t = null;
       for(int i =0; i< nb; i++) {
           t = this.tables.elementAt(i);
           System.out.println("###################################################################################");           
           System.out.println("table : "+t.getNomTable()+"/ nombre FK = "+t.get_NbFK()+"/ type T: "+t.getTypeTable());           
           System.out.println("Nombre de tuple= "+t.getNb_Tuple()+" /taille tuple = "+t.getTailleTuple());           
           System.out.println("les attributs sont: ");
           t.afficherAttributs();
       }
   }

  public DatabaseMetaData getDatabaseMetaData(){
      return(this.dma);
  }
  
  public void fermerConnexion(){
      try {
          this.connect.close();
          this.hasChanged();
          this.notifyObservers(CONNECTION_FERMER_MESSAGE);
      } catch (SQLException ex) {
          Logger.getLogger(Compte.class.getName()).log(Level.SEVERE, null, ex);
          return;
      }
}
  
  public ServeurBD getBase(){
       return(this.base);
   }

  public String getUser(){
       return(this.user.getLogin());
   }
  
  public Table getTableFaits(){
      return(this.faits);
  }

  public User getUtilisateur(){
      return(this.user);
  }

  public void setServeurBD(ServeurBD s){
      this.base = s;
  }
  
  public Table rechercheTable(String nomTable){
      int j = 0; boolean b = false; Table t = null;String str;
      while((j<this.tables.size())&&(b==false)){
          str = ((Table)(this.tables.elementAt(j))).getNomTable();
         if(str.toUpperCase().equals(nomTable)){
             b= true;
             t = (Table) this.tables.elementAt(j);
         }
        j++;
      }
     return(t);
  }
  
  private String getFkfait(Table T){
	  Statement st =null; ResultSet rs = null; String str="";
		  try {
			  st = (Statement) this.connect.createStatement();
			rs  =  st.executeQuery("select COLUMN_NAME from USER_CONS_COLUMNS"+ 
					  " where TABLE_NAME='"+this.faits.getNomTable()+"'"+
					  " and CONSTRAINT_NAME = (select CONSTRAINT_NAME from user_constraints"+ 
					  " where table_name ='"+faits.getNomTable()+"'"+ 
					  " and R_CONSTRAINT_NAME IN (select CONSTRAINT_NAME from USER_CONS_COLUMNS"+
					  " where TABLE_NAME='"+T.getNomTable().toUpperCase()+"'"+
					  " and  COLUMN_NAME = '"+T.getPK()+"'))");
			
			if(rs.next()) str = rs.getString("COLUMN_NAME");
			
		      } catch (SQLException e) {e.printStackTrace();}  
    return str;
     }
  
  public String getPK(Table T) 
  {     String s="";
		for(int i=0; i<T.getV_Attributs().size(); i++)
			if(T.getV_Attributs().elementAt(i).GetNatureAtt().equals("PK") 
					                                      || T.getV_Attributs().elementAt(i).GetNatureAtt().equals("PF")) 
			{ if(i>=1) s+=", "; s+=T.getV_Attributs().elementAt(i).GetNomAtt();}
		return s; 
	}
  
  public void extraireListeEDs(){
	 try {  Statement st = connect.createStatement();
      		ResultSet resultat = st.executeQuery("select distinct(table_name) from user_constraints where constraint_type='R'");
      
      ListeEDs = new Vector<String>();      
      while(resultat.next()) ListeEDs.add(resultat.getString("table_name"));
      
		} catch (SQLException e) {e.printStackTrace();}
  }
  
  public Vector<String> getFaits(){ return ListeEDs; }
}