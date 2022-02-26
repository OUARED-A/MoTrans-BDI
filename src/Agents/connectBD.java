/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Agents;

import java.awt.*;
import java.io.*;
import java.awt.event.*;
import javax.swing.*;
import java.sql.*;
import javax.sql.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.SQLSyntaxErrorException;
import java.util.Vector;
/**
 *
 * @author djilali
 */
public class connectBD {

public class MetaBD {  
    String NomT;
    String Nbtyple;
    String typeTable;
    String attributs;
    
      }  
    
private Vector<MetaBD> MetaBD1 = new Vector<MetaBD>(); //vecteur qui stockera 

public Vector<MetaBD> Chargement()
        {
            try {
            
            
        
           int port = 3306;
    String login = "root", pwd = "", url = "com.mysql.jdbc.Driver", host = "localhost", base = "ed1";
    //String driver = "jdbc:mysql" + host + ":" + port + "/" + "mysql";
    String driver ="jdbc:mysql://localhost:3306/ed1";
            
            
        
            Class.forName("com.mysql.jdbc.Driver");//pour enregistrer le driver odbcjdbcdriver
            Connection conn = DriverManager.getConnection(driver,login,pwd);
            Statement state=null;
            connect();
           
         
            state = conn.createStatement();

           ResultSet rsa6=state.executeQuery("SELECT * FROM tables");
           
           tabSchema[0]="Actvars";
           rsa6.first();
           int i=1;
             
           while(rsa6.next()) {
               
           	MetaBD1.elementAt(i).NomT=rsa6.getString(1);
                MetaBD1.elementAt(i).Nbtyple=rsa6.getString(2);
                MetaBD1.elementAt(i).typeTable=rsa6.getString(3);
                MetaBD1.elementAt(i).attributs=rsa6.getString(4);
                
                System.out.println("kkkkk"+MetaBD1.get(i).Nbtyple);
                
                
                
                
              
                i++;
           }  
       
           state.close();
          
           
           
            return MetaBD1;
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return MetaBD1;
        }
        }


    
private String tabSchema[]=new String [5];
private String tabSchema1[]=new String [5];
private String tabSchema2[]=new String [5];
private String tabSchema3[]=new String [5];




   



    
 public   String[] connect()
 {
        try {
                int port = 3306;
    String login = "root", pwd = "", url = "com.mysql.jdbc.Driver", host = "localhost", base = "ed1";
    //String driver = "jdbc:mysql" + host + ":" + port + "/" + "mysql";
    String driver ="jdbc:mysql://localhost:3306/ed1";
            
            
        
            Class.forName("com.mysql.jdbc.Driver");//pour enregistrer le driver odbcjdbcdriver
            Connection conn = DriverManager.getConnection(driver,login,pwd);
            Statement state=null;
         
            
           
         
            state = conn.createStatement();

           ResultSet rsa6=state.executeQuery("SELECT * FROM tables");
           
           tabSchema[0]="Actvars";
           rsa6.first();
           int i=1;
             
           while(rsa6.next()) {
               
           	tabSchema[i]=rsa6.getString(1);
                
                
                i++;
           }  
       
           state.close();
          
           
           
            return tabSchema;
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return tabSchema;
        }
    }
 
 
 public   String[] gettyple()
 {
       try {
                int port = 3306;
    String login = "root", pwd = "", url = "com.mysql.jdbc.Driver", host = "localhost", base = "ed1";
    //String driver = "jdbc:mysql" + host + ":" + port + "/" + "mysql";
    String driver ="jdbc:mysql://localhost:3306/ed1";
            
            
        
            Class.forName("com.mysql.jdbc.Driver");//pour enregistrer le driver odbcjdbcdriver
            Connection conn = DriverManager.getConnection(driver,login,pwd);
            Statement state=null;
         
            
           
         
            state = conn.createStatement();

           ResultSet rsa6=state.executeQuery("SELECT * FROM tables");
           
           tabSchema1[0]="2478600";
           rsa6.first();
           int i=1;
             
           while(rsa6.next()) {
               
           	tabSchema1[i]=rsa6.getString(3);
                
                
                i++;
           }  
       
           state.close();
          
           
           
            return tabSchema1;
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return tabSchema1;
        }
    }
 
 
 public   String[] gettype()
 {
       try {
                int port = 3306;
    String login = "root", pwd = "", url = "com.mysql.jdbc.Driver", host = "localhost", base = "ed1";
    //String driver = "jdbc:mysql" + host + ":" + port + "/" + "mysql";
    String driver ="jdbc:mysql://localhost:3306/ed1";
            
            
        
            Class.forName("com.mysql.jdbc.Driver");//pour enregistrer le driver odbcjdbcdriver
            Connection conn = DriverManager.getConnection(driver,login,pwd);
            Statement state=null;
         
            
           
         
            state = conn.createStatement();

           ResultSet rsa6=state.executeQuery("SELECT * FROM tables");
           
           tabSchema2[0]="Fait";
           rsa6.first();
           int i=1;
             
           while(rsa6.next()) {
               
           	tabSchema2[i]=rsa6.getString(2);
                
                
                i++;
           }  
       
           state.close();
          
           
           
            return tabSchema2;
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return tabSchema2;
        }
    }
 
 
 public   String[] getAttr()
 {
       try {
                int port = 3306;
    String login = "root", pwd = "", url = "com.mysql.jdbc.Driver", host = "localhost", base = "ed1";
    //String driver = "jdbc:mysql" + host + ":" + port + "/" + "mysql";
    String driver ="jdbc:mysql://localhost:3306/ed1";
            
            
        
            Class.forName("com.mysql.jdbc.Driver");//pour enregistrer le driver odbcjdbcdriver
            Connection conn = DriverManager.getConnection(driver,login,pwd);
            Statement state=null;
         
            
           
         
            state = conn.createStatement();

           ResultSet rsa6=state.executeQuery("SELECT * FROM tables");
           
           tabSchema3[0]="CUSTOMER_LEVEL,PRODUCT_LEVEL,CHANNEL_LEVEL,TIME_LEVEL,UNITSSOLD,DOLLARSALES,DOLLARCOST";
           rsa6.first();
           int i=1;
             
           while(rsa6.next()) {
               
           	tabSchema3[i]=rsa6.getString(4);
                
                
                i++;
           }  
       
           state.close();
          
           
           
            return tabSchema3;
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return tabSchema3;
        }
    }
 
 
}
/*

package paieptt;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;


public class connexion extends Thread {

    public connexion() {
    }
    static int port = 3306;
    static String login = "root",  pwd = "",  url = "org.gjt.mm.mysql.Driver",  host = "localhost",  base = "PaiePTT";
    Connection conn;
    Statement stmt;
    ResultSet rs;

    boolean login(String log, String pwd) {
        //  Principale p = new Principale();
        //p.setVisible(true);
        String driver = "jdbc:mysql://" + host + ":" + port + "/" + "mysql";
        try {
            Class.forName("org.gjt.mm.mysql.Driver");
            System.out.println("vous éte connecté" + login + " " + pwd);
            conn = DriverManager.getConnection(
                    driver,
                    login,
                    pwd);
            stmt = conn.createStatement();
            rs = stmt.executeQuery(
                    "select User,password from user where User='" + log + "' and password='" + pwd + "'");
            System.out.println("req");
            if (!rs.wasNull()) {
                stmt.close();
                conn.close();
                // run();
                return true;
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return false;
        }
        return false;

    }

    public boolean conect(String driver, String login, String pwd) {
        try {
            Class.forName(url);
            conn = DriverManager.getConnection(
                    driver,
                    login,
                    pwd);
            return true;
        // System.out.println("vous éte connecté");
        } catch (Exception e) {
            System.out.println("erreur " + e.getMessage());
            return false;
        // JOptionPane.showMessageDialog(Main.p, "Verifier vos informations");
        }

    }

    public void run() {
        int i = 0;
        while (true) {
            conect("jdbc:mysql://" + host + ":" + port + "/" + base, login, pwd);
            try {
                conn.close();
                System.out.print(i++);
                Thread.sleep(1000);
            } catch (Exception a) {
            }
        }
    }
}

 */