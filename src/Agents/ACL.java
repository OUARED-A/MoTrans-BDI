/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package Agents;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.io.*;



/**
 *
 * @author USER
 */



public class ACL {

    public long TempsExe ()
     {

        long beginTime = System.currentTimeMillis()/1000;
        return beginTime;

     }

       public String CapturerQ ()
  {




      String chaine="";
		String fichier ="D:\\SMAHOUSE\\charge.txt";

		//lecture du fichier texte
		try{
			InputStream ips=new FileInputStream(fichier);
			InputStreamReader ipsr=new InputStreamReader(ips);
			BufferedReader br=new BufferedReader(ipsr);
			String ligne;
			while ((ligne=br.readLine())!=null){
				//System.out.println(ligne);
				chaine+=ligne+"\n";
			}
			br.close();
		}
		catch (Exception e){
			System.out.println(e.toString());
		}


        return chaine;

  }
    public String capturer()
{
   String text1="";
text1 = text1+"Réveillez les agents  "+"\n";
text1 = text1+"   Agent extracteur ….> prêt   "+"\n";
text1 = text1+"   Agent préférence … > prêt    "+"\n";
text1 = text1+"   Agent IJB … > prêt     "+"\n";
text1 = text1+"   Agent FH … > prêt      "+"\n";
text1 = text1+"   Agent Evaluateur … > prêt      "+"\n";
text1 = text1+"   Agent Négociateur … > prêt       "+"\n";
text1 = text1+"--------------------------------------------------------------------"+"\n";

SimpleDateFormat formater = null;
formater = new SimpleDateFormat("'le' dd MMMM yyyy 'à' hh:mm:ss");
Date aujourdhui = new Date();



text1 = text1+"Lancement de l’agent extracteur à heure "+formater.format(aujourdhui)+"\n";

text1 = text1+"   Connexion à l' ED    …………OK "+"\n";
text1 = text1+"   Extraction des métadonnées  "+"\n";
text1 = text1+"   Extraction des la charge de requêtes Q  "+"\n";
text1 = text1+"   Extravtion terminée > envoyé à l'agent préférence  "+"\n";
text1 = text1+"--------------------------------------------------------------------"+"\n";








formater = null;
formater = new SimpleDateFormat("'le' dd MMMM yyyy 'à' hh:mm:ss");
 aujourdhui = new Date();

text1 = text1+"Lancement de l’agent préférence à heure "+formater.format(aujourdhui)+"\n";
text1 = text1+"Analyse la charge Q "+"\n";
text1 = text1+"Analyse des parametres W,S"+"\n";
text1 = text1+"Choix de mode de selection"+"\n";
text1 = text1+"Reveuiller les agents selectionneur "+"\n";

text1 = text1+"Selection des structures d'optmisation "+"\n";
text1 = text1+"Envoyer le resultat à l'agent négociateur "+"\n";
text1 = text1+"Analyse de resultat final "+"\n";
text1 = text1+"Affichage de resultat final "+"\n";

return text1;




}


}
