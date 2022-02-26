
package Environnement;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Observable;
import java.util.Vector;
import javax.swing.JOptionPane;

public class Chargement extends Observable{
    final static String EXTRACTION_DES_REQUETES = "extraction des requetes";

    private String path = null;
    private Compte compte = null;
    private Vector<Requete> requetes = new Vector<Requete>(); //vecteur qui stockera les requ�tes candidates
    private Vector<Requete> VRQ = null; //vecteur qui va stock� les req correctes et incorrectes

    public Chargement(String path, Compte con) throws IOException{
        this.path = path;
        this.compte = con;

        this.extraireCodeSql();
        System.out.println("\n\n\t*** code sql des requetes r�cup�r�  ***\n");
        
        extraireRequete();
        assigneTable();
        
        /** L'ajout du type pour chaque attribut **/
        Vector<Table> tablesEDD = this.getCompte().getTables();
        for(int u=0; u< this.VRQ.size(); u++)
        {
            
            Requete r = this.VRQ.elementAt(u);
             
            if(r.Validite && r.getBlocsOR()!=null)
            for (int p=0; p< r.getBlocsOR().size(); p++) {            	
            	for (int h=0; h< r.getBlocsOR().elementAt(p).size(); h++) {
                Attribut a = r.getBlocsOR().elementAt(p).elementAt(h);
                int indT = this.getCompte().indexTables.indexOf(a.GetTableAtt());
                int indAtt = tablesEDD.elementAt(indT).indexAttributs.indexOf(a.GetNomAtt());
                a.type = tablesEDD.elementAt(indT).getV_Attributs().elementAt(indAtt).type;                
                                                                            }
                                                          }
         }  

        this.hasChanged();
        this.notifyObservers(EXTRACTION_DES_REQUETES);
        

      //On Affiche les requetes
        for(int u=0; u< this.requetes.size(); u++){
            Requete r = this.requetes.elementAt(u);
            System.out.println();
            System.out.println("Num REquete = "+r.getNumReq());
            System.out.println();
            
            for (int p=0; p< r.getBlocsOR().size(); p++) {
            	System.out.println("                   Bloc OR n� : "+p);
            	for (int h=0; h< r.getBlocsOR().elementAt(p).size(); h++) {
                Attribut a = r.getBlocsOR().elementAt(p).elementAt(h);
                
                System.out.println("table: "+ a.GetTableAtt());
                System.out.println("\tattribut: "+a.GetNomAtt()+"\t    Ses Valeurs :");
                for(int cp=0; cp<a.valeurs.size(); cp++) 
        System.out.println("val "+a.operateurs.elementAt(cp)+" "+a.valeurs.elementAt(cp));
                                                                            }
                                                          }
                                                      }

    }

    private void lancerCompil() throws IOException
	{
     try {
            FileWriter writer = null;
            Runtime r = Runtime.getRuntime();
            String Chemin = "C:\\grammaire\\lancer.bat";
            /*System.out.println("#############################");
            System.out.println("LE CHEMIN EST \n\t\t"+Chemin);
            System.out.println("Path : "+this.path);
            */
            writer = new FileWriter(Chemin, false);
            BufferedWriter tamponEcriture = new BufferedWriter(writer);

            String infoInsert = "CD\\";
            tamponEcriture.write(infoInsert); ///ajout ligne1
            tamponEcriture.newLine();
            
            infoInsert = "CD grammaire\\";
            tamponEcriture.write(infoInsert); ///ajout ligne1
            tamponEcriture.newLine();

            infoInsert = "DEL out.txt";
            tamponEcriture.write(infoInsert); ///ajout ligne1
            tamponEcriture.newLine();

            infoInsert = "compil.exe";
            tamponEcriture.write(infoInsert); ///ajout ligne1
            tamponEcriture.newLine();

            tamponEcriture.flush();
            tamponEcriture.close();
            writer.close();
            Process p = r.exec(Chemin);
            p.waitFor(); 
            
            System.out.println("Terminer !");
            
        } catch (InterruptedException ex) 	{System.out.println("Erreur");}
    }
	
    public void extraireCodeSql() throws IOException{
    	FileReader fr, Or = null;
    	VRQ = new Vector<Requete>();
        String str = "";
        Requete req = null;
        int pos,j=0, k=0; String temp="";
        FileWriter fo = new FileWriter("C:\\grammaire\\out2.txt");
		BufferedWriter ow = new BufferedWriter(fo);
        
            fr = new FileReader(path);
            BufferedReader br = new BufferedReader(fr);
            
            while (str != null)
            {
                str = br.readLine();
                if(str != null) {
                  j = str.length();
                  if(str.contains("FROM")) 
                	    str=str.substring(0, str.indexOf("FROM"))+" "+str.substring(str.indexOf("FROM"))+" ";
                  if(str.contains("from")) 
              	    str=str.substring(0, str.indexOf("from"))+" "+str.substring(str.indexOf("from"))+" ";
                  if(str.contains("AND")) 
              	    str=str.substring(0, str.indexOf("AND"))+" "+str.substring(str.indexOf("AND"))+" ";
                  if(str.contains("and")) 
                	    str=str.substring(0, str.indexOf("and"))+" "+str.substring(str.indexOf("and"))+" ";
                  if(str.contains(" OR ")) 
              	    str=str.substring(0, str.indexOf(" OR "))+" "+str.substring(str.indexOf(" OR "))+" ";
                  if(str.contains("GROUP BY")) 
                	    str=str.substring(0, str.indexOf("GROUP BY"))+" "+str.substring(str.indexOf("GROUP BY"))+" ";
                  if(str.contains("group by")) 
              	    str=str.substring(0, str.indexOf("group by"))+" "+str.substring(str.indexOf("group by"))+" ";
                  if(str.contains("ORDER BY")) 
              	    str=str.substring(0, str.indexOf("ORDER BY"))+" "+str.substring(str.indexOf("ORDER BY"))+" ";
                  if(str.contains("order by")) 
            	    str=str.substring(0, str.indexOf("order by"))+" "+str.substring(str.indexOf("order by"))+" ";
                  
                  temp = temp + str;
                  pos = temp.indexOf(";");
                //System.out.println("pos = "+pos);
                if (pos != -1) 
                {
                	req = new Requete(k, null , null, null);
                    req.setCodeSQL(temp.substring(0, pos));
                    VRQ.add(req);
                    //ecriture de la requ�te ds un fichier vide
                    FileWriter fw = new FileWriter("C:\\grammaire\\in.txt");
    				BufferedWriter bw = new BufferedWriter(fw);
    				bw.write(req.getCodeSQL()+";");
    				bw.close();
    				//compil� la requ�te ecrite ds le fichier vide
    				lancerCompil();
    				//verifier si requ�te correcte syntaxiquement
    				Or = new FileReader("C:\\grammaire\\out.txt");
                    BufferedReader bf = new BufferedReader(Or);
                    String l="", rs="";
                    while(l!=null)
                    {
                    	l=bf.readLine();
                    	if(l!=null)
                    	{
                    		if(l.equals("Erreur lexicale") || l.equals("Erreur syntaxique")) req.Validite = false;
                    		if(!rs.equals("")) rs +="\r\n"; 
                    		if(l.length()==1) rs+=k;
                    		else rs+=l;
                    	}
                    }
                    bf.close();
                    if(req.Validite) ow.append(rs+"\r\n\r\n");
                    k++;
                    //System.out.println(temp);
                    if(j >= (pos+1)) temp = str.substring(pos+1);
                    else temp ="";
                }
                                 }
           }
            br.close();  ow.close();         
    }

 /**
  	* Cette m�thode lit le fichier en sortie de l'analyseur et structure ses �l�ments dans un vecteur d'objet
 * @return Vecteur de requ�te ou chaque �l�ment contient le num�ro requete, tables et les attributs de l'�l�ment
 */
    private void extraireRequete(){
    	
        Vector<Vector<Attribut>> Vatt = new Vector<Vector<Attribut>>();
        Vector<Attribut> v = new Vector<Attribut>();
        Vector<String> AttSelect = new Vector<String>();
        String str, opGroup=""; boolean AG = false, Group = false, etoile = false;
        Vector<String> tables = new Vector<String>();
        Vector<String> idf = new Vector<String>();
        Vector<String> opAg = new Vector<String>();
        Vector<String> valAg = new Vector<String>();
        String attGr = "";
         String s;
        int  j, u, len, taille; Boolean b = false;
        Vector<String> nomTbase = new Vector<String> ();
        
        for(int i=0; i< compte.getTabEDD().size(); i++)
        nomTbase.add(compte.getTabEDD().elementAt(i).getNomTable());
        
        System.out.println("d�marage de l'extraction");
        
        try {
            FileReader fr = new FileReader("C:\\grammaire\\out2.txt");
            BufferedReader br = new BufferedReader(fr);

            str = br.readLine(); //System.out.println(str);

            if(str!=null)
            if(str.length()>0){
                if(str.equals("Erreur lexicale")){
                    JOptionPane.showMessageDialog(null, "Erreur lexicale!", "Erreur", JOptionPane.ERROR_MESSAGE);
                    System.exit(1);
                                                  }
                else if (str.equals("Erreur syntaxique")){
                    JOptionPane.showMessageDialog(null, "Erreur syntaxique!", "Erreur", JOptionPane.ERROR_MESSAGE);
                    System.exit(1);
                                                          }
            }

            while((str != null)){
                
                len = str.length(); //System.out.println("la taille de str = "+len);
                taille = len;
                if(taille> 0){// si ligne non vide on commence les tests
                
                int i = str.indexOf("$"); //System.out.println("i = "+i);
                	
                if(i != -1){  j= str.indexOf("$", i+1); //System.out.println("$: j = "+j);
               
                //recuperation des tables et de leurs IDFs s'il y'en a
                while(j!= -1){
                    u = str.indexOf("+", i+1);//System.out.println("+: u = "+u);
                    if((u<j)&&(u!=-1)){
                        tables.add(str.substring(i+1, u-1).trim().toUpperCase()); 
                        System.out.println("ma table : "+str.substring(i+1, u-1).toUpperCase());
                        idf.add(str.substring(u+1, j-1).trim().toUpperCase()); 
                        System.out.println("son idf : "+str.substring(u+1, j-1).toUpperCase());
                    }
                    else { tables.add(str.substring(i+1, j-1).trim().toUpperCase()); 
                           System.out.println("ma table : "+str.substring(i+1, j-1).toUpperCase());
                           idf.add("."); System.out.println("son idf : "+idf.lastElement());}
                    i= j;
                    j= str.indexOf("$", i+1); //System.out.println("j = "+j);
                    b = true;
                }
                
                //recuperation de la dernniere table avec son IDF s'il existe
                if((j== -1)&&(i+1 < len) && (b== true)){
                    u = str.indexOf("+", i+1);
                    if(u== -1) {tables.add(str.substring(i+1).trim().toUpperCase());
                                idf.add("."); 
                                System.out.println("ma table: "+str.substring(i+1));
                                System.out.println("son idf : "+idf.lastElement());
                               }
                    else{
                        tables.add(str.substring(i+1, u-1).trim().toUpperCase());
                        System.out.println("ma table : "+tables.lastElement());
                        idf.add(str.substring(u+1).trim().toUpperCase());
                        System.out.println("son idf : "+str.substring(u+1));
                         }
                                                        }
                
               // cas : soit ya une seul table o� c'est les attributs avec leurs idfs 
                else if (b == false) { 
           
                    j = str.indexOf("*"); //System.out.println("*: j = "+j);
                    if(j != -1){
                        String temp[] = new String[2];
                        
                        //on recupere l'idf de l'attribut
                        temp[0] = str.substring(i+1, j-1).trim().toUpperCase(); //System.out.println(str.substring(i+1, j-1));
                        i= j;
                        j= str.indexOf("&");//System.out.println("saut: j = "+j);
                        
                        //on recupere le nom de l'attribut
                        temp[1] = str.substring(i+1,j).trim().toUpperCase();//System.out.println(str.substring(i+1, taille));
                      
                        //si nouvel attr on lajoute; si attr existant on lajoute pas                        
                        for(j=0;(j<v.size() && !temp[1].equals(v.elementAt(j).GetNomAtt()));j++);
                        
                        if(j==v.size()) { //attribut non existant
                        //on verifie si l'idf existe pour ajouter le nom de table au nvel attribut	
                        int nu = idf.indexOf(temp[0]);   Attribut a = null;
                        if(nu!=-1) 	a = new Attribut(tables.elementAt(nu), temp[1]);
                        else a = new Attribut(" ", temp[1]);
                        a.operateurs = new Vector<String>(); a.valeurs = new Vector<Object>();                        
                        v.add(a);
                         //System.out.println("nv attribut : "+temp[1]+" "+v.elementAt(j).GetNomAtt()+" "+j);
                                        }
                        
                        //ajout des valeurs a l'attribut existant ou au nvel attribut
                        if(str.indexOf("IN")!= -1) {//ens IN
                        	int p,n; String IN = "";
                        	p = str.indexOf("'"); n = str.indexOf(",");
                        	while(n!=-1) {
                        		//ON recupere la valeure 
                        		IN+=str.substring(p,n+1).trim();
                        		p=n+1; n = str.indexOf(",",p);
                                       	  }
                        	IN+=str.substring(p).trim();
                        	v.elementAt(j).operateurs.add("IN");
                        	v.elementAt(j).valeurs.add(IN);
                        }
                        
                        else if(str.indexOf("#")!= -1) {//valeur numerique
                    	v.elementAt(j).operateurs.add(str.substring(str.indexOf("&")+1,str.indexOf("#")).trim());
                    	v.elementAt(j).valeurs.add(str.substring(str.indexOf("#")+1).trim());
                    	/*System.out.println("num "+temp[1]+" "+j+" "+v.elementAt(j).operateurs.lastElement()+
                    			" "+v.elementAt(j).valeurs.lastElement());*/                           
                    	}
                    	
                    	else if(str.indexOf("'")!= -1) {//chaine de caract�re
                    		v.elementAt(j).operateurs.add(str.substring(str.indexOf("&")+1,str.indexOf("'")).trim());
                    		i=str.indexOf("'");
                    		v.elementAt(j).valeurs.add(str.substring(i,str.indexOf("'", i+1)+1).trim());                    		
                    		/*System.out.println("char "+temp[1]+" "+j+" "+v.elementAt(j).operateurs.lastElement()+
                        			" "+v.elementAt(j).valeurs.lastElement());*/
                    	                               }
                    }
                    
                    else{//cas ou ya une seul table avec idf ou sans idf
                    	j = str.indexOf("+"); 
                    	if(j != -1){ //avec idf
                    		tables.add(str.substring(i+1, j-1).trim().toUpperCase()); 
                    		System.out.println("table: "+str.substring(i+1, j-1).toUpperCase());
                    		idf.add(str.substring(j+1).trim().toUpperCase()); 
                    		System.out.println("idf: "+str.substring(j+1).trim().toUpperCase());
                    	}
                    	// sans idf
                    	else {	tables.add(str.substring(i+1).trim().toUpperCase());
                    			System.out.println("table: "+str.substring(i+1).trim().toUpperCase());
                    			idf.add("."); System.out.println("idf: "+idf.lastElement());
                    	     }
                          }
                }
                
            } else { //pas de $; une etoile cas�d un attribut sans idf
                    i = str.indexOf("*"); //System.out.println("*: i = "+i);
                    
                    if(i != -1 && str.indexOf("�")==-1) {
                    	//si nouvel attr on lajoute; si attr existant on lajoute pas                        
                        String st = str.substring(i+1,str.indexOf("&")).trim().toUpperCase();
                    	for(j=0;(j<v.size() && !st.toUpperCase().equals(v.elementAt(j).GetNomAtt().toUpperCase()));j++);
                        
                        if(j==v.size()) {v.add(new Attribut(" ", st));
                                         v.lastElement().operateurs = new Vector<String>();
                                         v.lastElement().valeurs = new Vector<Object>();
                         //System.out.println("nv attribut : "+st+" "+v.elementAt(j).GetNomAtt()+" "+j);
                                        }
                        
                        if(str.indexOf("IN")!= -1) {//ensemble IN
                        	int p,n; String IN = "";
                        	p = str.indexOf("'"); n = str.indexOf(",");
                        	while(n!=-1) {
                        		//ON recupere la valeure 
                        		IN+=str.substring(p,n+1).trim();
                        		p=n+1; n = str.indexOf(",",p);
                                       	  }
                        	IN+=str.substring(p).trim();
                        	v.elementAt(j).operateurs.add("IN");
                        	v.elementAt(j).valeurs.add(IN);
                        }
                        
                        else if(str.indexOf("#")!= -1) {//valeur numerique
                    	v.elementAt(j).operateurs.add(str.substring(str.indexOf("&")+1,str.indexOf("#")).trim());
                    	v.elementAt(j).valeurs.add(str.substring(str.indexOf("#")+1).trim());
                    	/*System.out.println("num "+temp[1]+" "+j+" "+v.elementAt(j).operateurs.lastElement()+
                    			" "+v.elementAt(j).valeurs.lastElement());*/                           
                    	}
                    	else if(str.indexOf("'")!= -1) {//chaine de caract�re
                    		v.elementAt(j).operateurs.add(str.substring(str.indexOf("&")+1,str.indexOf("'")).trim());
                    		i=str.indexOf("'");
                    		v.elementAt(j).valeurs.add(str.substring(i,str.indexOf("'", i+1)+1).trim());
                    		/*System.out.println("char "+temp[1]+" "+j+" "+v.elementAt(j).operateurs.lastElement()+
                        			" "+v.elementAt(j).valeurs.lastElement());*/
                    	                               }
                                                            }
                    
                    else if (str.equals("OR")) {System.out.println("OR trouv�");
                                                Vatt.add(v);
                                                v = new Vector<Attribut>();
                                                }
                    
                    //recuperation des attributs de selections
                    else if (str.indexOf("�")!=-1) {
                    	if(str.indexOf("�*")!=-1) etoile = true;
                    	else {
                    	i=str.indexOf("�"); j=str.indexOf("�",i+1);
                    	String AttS ="";
                    	
                    	while(j!=-1) {
                    		AttS = str.substring(i+1,j).trim().toUpperCase(); AttSelect.add(AttS);
                    		i=j; j=str.indexOf("�",i+1);
                    	              }
                    	
                    	if(j==-1 && str.indexOf("-")==-1) {	AttS = str.substring(i+1).trim().toUpperCase(); 
                    										AttSelect.add(AttS);}
                    	else if (j==-1 && str.indexOf("-")!=-1)
                    	{ 
                    	AttS = str.substring(i+1,str.indexOf("-")).trim().toUpperCase(); AttSelect.add(AttS); 
                    	AG = true;
                    	i=str.indexOf("!"); 
                    	//recuperation des operateurs d'agregation
                    	while(i!=-1) {	opAg.add(str.substring(i+1,str.indexOf("?",i)).trim().toUpperCase());
                    					i=str.indexOf("!",i+1);}
                    	//recuperation des attribut d'agregations
                    	i=str.indexOf("?"); j=str.indexOf("-",i);
                    	while(j!=-1) {valAg.add(str.substring(i+1,j).trim().toUpperCase()); 
                    	i=str.indexOf("?",j); j=str.indexOf("-",i);}
                    	valAg.add(str.substring(i+1).trim().toUpperCase());
                    	}
                               }
                                                      }
                    //recuperation des infos du groupement
                    else if(str.contains("-Group")|| str.contains("-Order")) 
                    {Group = true;
                     opGroup= str.substring(str.indexOf("-")+1,str.indexOf("?")).trim().toUpperCase();
                     attGr = str.substring(str.indexOf("?")+1).trim().toUpperCase();
                    }
                    
                    else if(str.equals("Erreur lexicale")){
                    JOptionPane.showMessageDialog(null, "Erreur lexicale!", "Erreur", JOptionPane.ERROR_MESSAGE);
                    System.exit(1);
                                                           }
                    
                    else if (str.equals("Erreur syntaxique")){
                    JOptionPane.showMessageDialog(null, "Erreur syntaxique!", "Erreur", JOptionPane.ERROR_MESSAGE);
                    System.exit(1);
                                                              }
                    
                    // cas ou c'est le num�ro de la requete 
                    else //if(str.length()==1)
                       {                       
                        s = str;
 
                        Integer k = Integer.parseInt(s); //eneregistre le num de la req
                        System.out.println("bloc req "+k+"ajout�" );
                        Vatt.add(v); //on ajoute le dernier bloc de OR
                        
                        System.out.println();
                        System.out.print("num�ro requete: "); System.out.println(k);
                        System.out.println();
                        if (etoile) { int h; String nomFait = compte.getTableFaits().getNomTable();
                        	for(h=0; h<tables.size(); h++) 
                        	{
                        		int ind = nomTbase.indexOf(tables.elementAt(h).toUpperCase());
                        		if (!tables.elementAt(h).toUpperCase().equals(nomFait))
                        		for(int y=0; y<compte.getTables().elementAt(ind).getV_Attributs().size(); y++)
                        			AttSelect.add(compte.getTables().elementAt(ind).getV_Attributs().elementAt(y).GetNomAtt());
                        	}
                                    }
                        //Requete req = new Requete(k, Vatt, tables, idf);
                        Requete req = VRQ.elementAt(k);
                        req.setTables(tables); req.setBlocsOR(Vatt); req.setIDFs(idf);
                        req.Select = AttSelect;
                        req.AG = AG; req.Group = Group; 
                        req.opGroup=opGroup; req.attGroup=attGr;
                        req.opAG=opAg; req.valAG=valAg;
      
                        //this.requetes.add(req);
                        //reintitialisation de toutes les variables
                        AttSelect = new Vector<String>(); AG =false; Group = false; etoile=false;
                        opGroup=""; attGr="";
                        opAg= new Vector<String>(); valAg = new Vector<String>();
                        idf = new Vector<String>();
                        tables = new Vector<String>();
                        v = new Vector<Attribut>();
                        Vatt = new Vector<Vector<Attribut>>();
                         }
                   } 
            }                
                str = br.readLine();
                //System.out.println(str);
                b = false;
            }
            br.close();
            fr.close();
        } catch (IOException ex) { System.out.println(ex.getMessage()); }
    }

    /**
    * methode qui remplace les idfs du vecteur d'attributs par les noms des tables respectifs
 * @param req : la requette concern�e
 * @param idf : le tableau des idfs enregistr� lors de l'extraction des requetes
 */    
    private void assigneTable(){
        Requete req = null;

        Vector<Table> base = this.compte.getTabEDD();
        if(this.compte.getTabEDD() == null) 
       {JOptionPane.showMessageDialog(null, "nn", "ERROR LORS DE L'EXTRACTION DES TABLES", 
        		                      JOptionPane.ERROR_MESSAGE);return;}
        
        Vector<Attribut> attr, BlocAttr; Table tab; Attribut attReq;
        String ch; Boolean b = false;

        //On parcours les requetes
        for(int i=0; i<this.VRQ.size(); i++){
        	req = this.VRQ.elementAt(i);
            System.out.println("num req"+i+" "+req.Validite+" "+req.getBlocsOR());
            
        	if(req.Validite && req.getBlocsOR()!=null)
            //On parcours les blocs des attributs regroup�s par AND
            for(int h=0; h<req.getBlocsOR().size(); h++) { 
            BlocAttr = req.getBlocsOR().elementAt(h);

            for(int u = 0; u<BlocAttr.size(); u++){
                attReq =  BlocAttr.elementAt(u);
                b=false;
                
                if(attReq.GetTableAtt().equals(" "))        
                    for( int j=0; (j<base.size())&&(b==false); j++){
                        tab = base.elementAt(j);
                        attr = tab.getV_Attributs();

                        for(int k=0; (k<attr.size())&& (b==false); k++)
                        {
                        ch= null;
                        ch = attr.elementAt(k).GetNomAtt().toUpperCase();

                        if ( ch.equals(attReq.GetNomAtt().toUpperCase()) ) 
                        {
                            attReq.SetTabAtt(tab.getNomTable());
                            b=true;
                        }
                        }
                                                                           }
            }
        }
        }  
    }
    
    public void affiche(){
    	for(int b=0;b<this.requetes.size();b++) {
    		Requete req=this.requetes.elementAt(b);
			System.out.println("\n************************************");
			System.out.println("les table de la req n�"+req.getNumReq()+" sont : ");
			System.out.println("size  "+req.getTables().size());
			for(int m=0; m<req.getTables().size();m++){
    		System.out.println("la table : "+req.getTables().elementAt(m));
    		}
    	}
    }
    
    public Compte getCompte(){
        return(this.compte);
    }
   
    public Vector<Requete> getRequetes(){

        return (this.VRQ);
    }
    
    public Vector<Requete> getCandRequete(){
        Vector<Requete> r = new Vector<Requete>();
        for(int i=0; i<this.requetes.size(); i++)
            if(!this.requetes.elementAt(i).isElagage()) r.add(this.requetes.elementAt(i));
        return(r);
    }
 
    public static Requete getRequeteAt(Vector req, int ID){
        Requete temp = null;
        Boolean find = false;
        for(int i=0;((i< req.size())&&(find.equals(false))); i++){
            temp = (Requete) req.elementAt(i);
            if(temp.getNumReq()==ID) find = true;
        }
        return(temp);
    }

    public int[] frequences(){
        int f[] = new int[this.requetes.size()];
        Requete r = null;
        for(int i=0; i<this.requetes.size(); i++){
            r = this.requetes.elementAt(i);
            f[i] = r.getFrequence();
        }
        return(f);
    }
  
}
