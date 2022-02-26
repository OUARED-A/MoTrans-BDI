package Environnement;

import java.util.Vector;


public class ServeurBD{
  private String nomBD;
  private int port;
  private String host;
  private String typeSGBD;
  private String URL;
  private String nomServeur;

/**
   * Constructeur de la class ServeurBD
   * @param host: le nom ou ip du serveur de la base de donn�es
   * @param port: num�ro du port de la base de donn�es
   * @param nomBD: Nom de la base de donn�es
   * @param typeSGBD: Nom du SGBD
   */
  public ServeurBD(String host, int port, String nomMB, String typeSGBD ){
	  this.nomBD = nomMB;
      this.port= port;
      this.host = host;
      this.typeSGBD = typeSGBD;
      this.nomServeur = " ";
  }
  
  public ServeurBD(String nomServeur, String host, int port, String nomMB, String typeSGBD ){
      this.nomServeur = nomServeur;
      this.nomBD = nomMB;
      this.port= port;
      this.host = host;
      this.typeSGBD = typeSGBD;
  }

  public ServeurBD(String rl){
	  this.URL=rl;
  }
  
  public String recupURL(){
	  return this.URL;
  }
  
  /**
   * @return Renvoie la chaine de connexion a la base de donn�es
   */
  public String getURL(){
        if(this.typeSGBD.equals("Oracle"))
            this.URL ="jdbc:oracle:thin:@"+this.host+":"+this.port+":"+this.nomBD;
        else if(this.typeSGBD.equals("MySQL")) this.URL="jdbc:mysql://"+this.host+":"+this.port+"/"+this.nomBD;
        //faire celui de SQL Server

        return(this.URL);
  }
 
  public String getNomMB(){
      return(this.nomBD);
  }

  public String getHost(){
      return(this.host);
  }

  public Integer getPort(){
	  return(this.port);
  }

  public String getTypeSGBD(){
      return(this.typeSGBD);
  }
  
  public void setTypeSGBD(String typeSGBD) {
		this.typeSGBD = typeSGBD;
	}

  public String getNomServeur(){
      return(this.nomServeur);
  }   
}