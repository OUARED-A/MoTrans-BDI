/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package Agents;

import java.sql.*;
import java.sql.DriverManager;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;



/**
 *
 * @author USER
 */
public class ConnectOracle {





public void testConn()
    {
       
try
{
    String jdbcUri = "jdbc:oracle:thin:@localhost:1521:orcl1";
//String jdbcDriverclass = "oracle.jdbc.driver.OracleDriver";
String jdbcUsername = "dw";
String jdbcPassword = "dw";


Connection con = DriverManager.getConnection(jdbcUri, jdbcUsername, jdbcPassword);
//return con;
System.out.println(" you are  connect to");
}
catch(Exception e)

{
//Connection con=null;
System.out.println(" can not connect to");
System.out.println(" C ");

//return con;

}



}

}
