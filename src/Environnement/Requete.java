package Environnement;

import java.util.HashSet;
import java.util.Vector;


public class Requete{
    private int numReq;
    private String codeSqlReq = null;
    private int frequence = 1;
    private Vector<String> NomTables = null;
    private Boolean elague;
    public Vector<String> Select = null;
    private Vector<Vector<Attribut>> Vect_Attribut = null;
    private Vector<String> idf = null;
    public Vector<String> opAG = null;
    public Vector<String> valAG = null;
    public String opGroup = null;
    public String attGroup;
    Vector<String> v = null;
    public boolean Validite = true;

    public boolean AG = false, Group = false;
    
    
    public Vector<String> getVatt() {
		return v;
	}

	public Requete(){
        this.elague = false;
    }
    
   /* public Requete(int num, Attribut a){
        this.numReq = num;
        this.Vect_Attribut = new Vector<Attribut>();
        this.Vect_Attribut.add(a);
        this.elague = false;
    }
    public Requete(int num, String codeSql, int freq){
        this.numReq = num;
        this.codeSqlReq = codeSql;
        this.frequence = freq;
        this.elague = false;
    }*/
/**
 * Constructeur de la classe Requete
 * @param num Num�ro de la requ�te
 * @param attributs Les attributs utilis�s par la requ�te
 * @param tables Les tables ref�renc�es par la requ�te
 */
    public Requete(int num, Vector<Vector<Attribut>> attributs, Vector<String> tables,	Vector<String> idf){
        this.numReq = num;
        this.Vect_Attribut = attributs;
        this.NomTables = tables;
        this.elague = false;
        this.idf = idf;
        this.v = new Vector<String>();
        HashSet<String> Vatt = new HashSet<String>();
        if(Vect_Attribut!=null)
        for(int i=0; i<Vect_Attribut.size(); i++)
        	for(int j=0; j<Vect_Attribut.elementAt(i).size(); j++)
        		Vatt.add(Vect_Attribut.elementAt(i).elementAt(j).GetNomAtt().toUpperCase());
        this.v.addAll(Vatt);
    }
    
    /** 
     * constructeur qui fait la copie d'une requ�te 
     * @param Q requ�te e partir de laquelle on copie 
     */
    public Requete(Requete Q){
        this.numReq = Q.numReq;
        this.Vect_Attribut = new Vector<Vector<Attribut>>();
        
        for(int i=0; i<Q.getBlocsOR().size(); i++) {
     	   Vector<Attribut> Bloc = new Vector<Attribut>();
     	   
     	   for(int j=0; j<Q.getBlocsOR().elementAt(i).size(); j++)
     	   {
     		Attribut att = new Attribut(Q.getBlocsOR().elementAt(i).elementAt(j));
     		Bloc.add(att);
     	   }
     	   Vect_Attribut.add(Bloc);
        }
        
        if(Q.NomTables != null) this.NomTables = (Vector<String>) Q.NomTables.clone();
        this.elague = Q.isElagage();
        if(Q.getIDFs()!=null) this.idf = (Vector<String>) Q.getIDFs().clone();
        this.frequence = Q.frequence;
        this.codeSqlReq = Q.codeSqlReq;
        if(Q.Select != null) this.Select = (Vector<String>) Q.Select.clone();
        if(Q.opAG!=null) this.opAG = (Vector<String>) Q.opAG.clone(); 
        if(Q.valAG != null) this.valAG = (Vector<String>) Q.valAG.clone();
        this.opGroup = Q.opGroup;
        this.attGroup = Q.attGroup;
        this.AG = Q.AG;
        this.Group = Q.Group;
    }

   public void setTables(Vector<String> tables){
       this.NomTables = tables;
   }

   public Vector<Vector<Attribut>> getBlocsOR(){
       return(this.Vect_Attribut);
   }
   
   public void setBlocsOR(Vector<Vector<Attribut>> VA)
   {
       this.Vect_Attribut = VA;
       this.v = new Vector<String>();
       
       //on enregistre la liste des attributs candidats
       HashSet<String> Vatt = new HashSet<String>();
       if(Vect_Attribut!=null)
           for(int i=0; i<Vect_Attribut.size(); i++)
           	for(int j=0; j<Vect_Attribut.elementAt(i).size(); j++)
           		Vatt.add(Vect_Attribut.elementAt(i).elementAt(j).GetNomAtt().toUpperCase());
           this.v.addAll(Vatt);
   }
   
   public void setCodeSQL(String code){
       this.codeSqlReq = code;
   }
   public Integer getNumReq(){
       return(this.numReq);
   }
   /*
    * Modifie la frequence de la requete
    * @param freq La valeur de frequence de la requete
    */
   public void setFrequence(int freq){
       this.frequence = freq;
   }
   public Vector<String> getTables(){
       return(this.NomTables);
   }
   
   public Vector<String> getIDFs(){
       return(this.idf);
   }
   
   public void setIDFs(Vector<String> idfs){
       this.idf = idfs;
   }

   public String getCodeSQL(){
       return(this.codeSqlReq);
   }

   public int getFrequence(){
       return(this.frequence);
   }

   public void setElagage(Boolean b){
       this.elague = b;
   }

   public Boolean isElagage(){
       return(this.elague);
   }

}
