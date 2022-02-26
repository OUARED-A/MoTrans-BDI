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
/**
 *
 * @author djilali
 */
public class connect {


    
    int port = 3306;
    String login = "", pwd = "", url = "sun.jdbc.odbc.JdbcOdbcDriver", host = "localhost", base = "ED";
    //String driver = "jdbc:odbc:ED" + host + ":" + port + "/" + "SGBD";
    String driver = "jdbc:odbc:ED" ;

    
 public   String connect() {
        try {
            Statement state=null;
            Class.forName(url);//pour enregistrer le driver odbcjdbcdriver
            Connection conn = DriverManager.getConnection(driver,login,pwd);
            return "ok";
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return "no";
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