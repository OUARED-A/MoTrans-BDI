package Environnement;

import java.util.Vector;


public class Table {

	private long PS=8*1024; //taille page systeme
	
	//informations sur la table
	private String NomTable;
	private String TypeTable;
	private long Nb_Tuple;
	private long Taille_Tuple;
	private int nbFK =0;
	private String PK = null;
	private Vector<Attribut> V_Attributs;
	private String FKfait=null;
	public Vector<String> indexAttributs=null;
	private String infofrag="";
	
	public Table(String NomTable){
		this.NomTable=NomTable;
		this.TypeTable="Dimension";
		}
	
	public String getNomTable(){
		return this.NomTable;
	}
	
	public boolean IsAttribut(Attribut att){
		boolean b=false;
		for(int i=0;i<this.V_Attributs.size();i++){
			if(att.GetNomAtt().equals(this.V_Attributs.elementAt(i).GetNomAtt())){
				b=true;
				break;	
			}
		}
		return b;
	}

	public long getTailePS(){
		return this.PS;
	}

	
	public String getTypeTable(){
		return this.TypeTable;
	}
	
	public void SetTypeTable(String TypeTable){
		this.TypeTable=TypeTable;
	}
	
	public long getNb_Tuple(){
		return this.Nb_Tuple;
	}
	
	public void SetNb_Tuple(long Nb_Tuple){
		this.Nb_Tuple=Nb_Tuple;
	}
	
	public long getTailleTuple(){
	    return(this.Taille_Tuple);
	}
	
	public void setTailleTuple(int T){
		this.Taille_Tuple = T;
	}
	
	public void CalculTailleTuple(){
		long T=0;
		for(int i=0;i<this.V_Attributs.size();i++)	T=T+this.V_Attributs.elementAt(i).GetTailleAtt();
		this.Taille_Tuple=T;
	}
	
	public Vector<Attribut> getV_Attributs(){
		return this.V_Attributs;
	}
	
	public void SetV_Attributs(Vector<Attribut> V_Attributs){
		indexAttributs = new Vector<String>();
		for(int i=0; i<V_Attributs.size(); i++)
			indexAttributs.add(V_Attributs.elementAt(i).GetNomAtt().toUpperCase());
		this.V_Attributs=V_Attributs;
	}
    
	public Vector<String> get_indexAttributs(){
		return this.indexAttributs;
	}
	
	public int get_NbFK(){
		return this.nbFK;
	}
	
	public int set_NbFK(int nb){
		return this.nbFK = nb;
	}
	
	public void Calcul_NbFK(){
		int nbr=0;
		if(this.V_Attributs==null) nbr=0;
		else {
			Attribut att=null;
			int size=this.V_Attributs.size();
			for(int i=0;i<size;i++){
				att=this.V_Attributs.elementAt(i);
				if(att.GetNatureAtt().equals("FK")|| att.GetNatureAtt().equals("PF")) nbr++;
			}
		}
		this.nbFK=nbr;
	}

	public void afficherAttributs(){
		Attribut att=null; int nb=this.V_Attributs.size();
	    for(int i=0; i<nb;i++){
	            att = this.V_Attributs.elementAt(i);
	            System.out.println("Attribut : "+att.GetNomAtt()+" / Nature : "+att.GetNatureAtt()+" / taille : "+att.GetTailleAtt()+" / type : "+att.GetTypeAtt());

	        }
	    }

	public String getDescTable(){
        String str;
        str =" Nom: "+this.NomTable+'\n'+'\n'+" Type: "+this.getTypeTable()+'\n'
        +" Nombre de cl� �trang�re : "+this.nbFK+'\n'+" Nombre d'attributs: "+this.getV_Attributs().size()+'\n'+" Nombre de tuples : "+this.Nb_Tuple
        +'\n'+" Fragment�e : "+this.infofrag;
        
        return(str);
    }
	
	public void setInfoFrag(String Info)
	{this.infofrag = Info;}
	
	public String getInfoFrag()
	{return this.infofrag;}
	
	public String getFKfait (){
		return this.FKfait;
	}
	
     public void setFKfait (String Fk){
		this.FKfait = Fk;
	}
	
     public void setPK(String PK) {
     	this.PK = PK;
     }
     
     public String getPK() {	
 		return this.PK; 
 	}
}
