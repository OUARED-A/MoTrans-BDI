package Environnement;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Observable;
import java.util.Vector;

public class Servers extends Observable {

	public final static String AJOUT_Server = "Ajout d'un serveur";
    public final static String SUPPRIMER_Server = "Suppression d'un serveur";
	public final static String AJOUT_CONNE = "Ajout d'une connexion";

    String ligne;
	FileReader fr ;
    BufferedReader br;
    int len;
	public Vector<String> v_url=new Vector<String>();
	public Vector<String> v_SGBD=new Vector<String>();
	public Vector<String> v_connex=new Vector<String>();
	public Vector<String> v_ConnexSG=new Vector<String>();
	

	ServeurBD base;
	
	public Servers(){
		chargement();
	}
    
    public void chargement(){
			try {
				fr = new FileReader("C:\\N\\serveurs.txt");
				br = new BufferedReader(fr);
				ligne = br.readLine(); 

			while((ligne != null))
		{
				len = ligne.length();
				if(len>0){
					int i = ligne.indexOf("\t");
					int k;
					//for(k=0;((k<v_url.size())&&(!ligne.subSequence(0,i).equals(v_url.elementAt(k))));k++);
					//if(k==v_url.size())
					if(!v_url.contains(ligne.substring(0,i).trim())){ 
						v_url.add(ligne.substring(0,i).trim()); 
						v_SGBD.add(ligne.substring(i+1).trim());
						//System.out.println(ligne.substring(i+1,len));
					                   }
					//System.out.println(v_url.elementAt(i));
				         }
        	ligne = br.readLine();
        }
    	br.close();
        fr.close();
			} catch (IOException e) {e.printStackTrace();}
    }

    public void AddServer(ServeurBD base){
    	this.base=base;
    	FileWriter writer = null;
		String Chemin = "C:\\N\\serveurs.txt";
		
		try {
			writer = new FileWriter(Chemin, true);
			BufferedWriter output = new BufferedWriter(writer);
			output.write(base.getURL()+"\t"+base.getTypeSGBD());
			output.newLine();
			output.flush();
			output.close();
			
			hasChanged();
			notifyObservers(AJOUT_Server);
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }

    public void RemoveServer(ServeurBD base){
    	
    }
}
