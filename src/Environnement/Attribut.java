package Environnement;

import java.util.Vector;

public class Attribut {

	private String NomAtt;
	private String TypeAtt;
	public String type="";
	private long Taille_Att;
	private String table_att;
	private boolean isAttrSelect = false;
	public Vector<Object> valeurs = null;
	public Vector<String> operateurs = null;
	public Vector<Integer> SDvals = null;
	public int L, S; 
	private long cardAttr = 0;
	private String NatureAtt = null;


	
	public Attribut(String tab,String att){
		this.NomAtt=att;
		this.table_att=tab;
	}
	
	public Attribut (Attribut a) 
	{
		this.NomAtt = a.NomAtt;
		this.TypeAtt = a.TypeAtt;
		this.NatureAtt = a.NatureAtt;
		this.Taille_Att = a.Taille_Att;
		this.type = a.type;
		this.table_att = a.table_att;
		this.valeurs = (Vector<Object>) a.valeurs.clone();
		this.operateurs = (Vector<String>) a.operateurs.clone();
		if(a.SDvals!=null)
		this.SDvals = (Vector<Integer>) a.SDvals.clone();
		this.L=a.L;
		this.cardAttr = a.getCardinalite();
	}
	
	public String GetNomAtt(){
		return this.NomAtt;
	}
	
	public String GetTypeAtt(){
		return this.TypeAtt;
	}
	
	public void SetTypeAtt(String TypeAtt){
		this.TypeAtt=TypeAtt;
	}
	
	public long GetTailleAtt(){
		return this.Taille_Att;
	}
	
	public void SetTailleAtt(long Taille_Att)
	{
		this.Taille_Att=Taille_Att;
	}
	
	public String GetTableAtt(){
		return this.table_att;
	}
	
	public void SetTabAtt(String table_att){
		this.table_att=table_att;
	}
	
	public boolean isAttrSelect() {
		return this.isAttrSelect;
	}
	
	public void setAttrSelect(boolean b) {
	 this.isAttrSelect = b;
	}
	
	public void setValeurs(Vector Valeurs) {
		this.valeurs = Valeurs;
	}
	
	public Vector getValeurs() {
		return(this.valeurs);
	}
	
	public void setOperateurs(Vector<String> operateurs) {
		this.operateurs = operateurs;
	}
	
	public Vector<String> setOperateurs() {
		return (this.operateurs);
	}
	
	public Vector getOperateur() {
		return(this.operateurs);
	}

	public Long getCardinalite(){
        return(this.cardAttr);
    }

    public void setCardinalite(Long card){
        this.cardAttr = card;
    }

	public String GetNatureAtt(){
		return this.NatureAtt;
	}
	
	public void SetNatureAtt(String NatAtt){
		this.NatureAtt=NatAtt;
	}
	
	
}
