/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package Opérations;



import java.sql.DatabaseMetaData;
import java.awt.Component;
import java.sql.Connection;
import java.lang.String.*;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Observable;
import java.util.Vector;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;

import Environnement.Table;

import Environnement.Attribut;
import Environnement.Compte;

//import smahouse.Table;






 

public class DataExtraction extends Observable{
private String login = null;
private DatabaseMetaData dma= null;




// vecteurs des objets de type attrubuts
public  Vector<Attribut> GetListAttr(String nomTable)

{
   Attribut Att;
   Vector<Attribut> Vattr =new Vector<Attribut>();
   Vattr=null;
   Attribut attr;



int i;
int j;
Vector<String> vvv=new Vector<String>();
vvv=null;

vvv=GetTable();

for(i=0;i<GetTable().size();i++)
{
  Vector<String> vv=new Vector<String>();
  vv=null;
  vv=GetAttrTable(GetTable().elementAt(i));
  attr=null;
   Vector <String> Vtailleattr =new Vector <String>();
   Vtailleattr=null;
   Vtailleattr=GetTailleAttr1(GetTable().elementAt(i));


   Vector <String> Vtypeattr =new Vector <String>();
   Vtypeattr=null;
   Vtypeattr=GetTypeAttr(GetTable().elementAt(i));




Vector<Attribut> attRempTable=new Vector<Attribut>();
attRempTable=null;

   for( j=0;j<vv.size();j++)
{
   String NatureAttr=null;
   NatureAttr=RecupNatureAttributs(GetTable().elementAt(i),vv.elementAt(j));

   String CardAttr=null;
   CardAttr=GetCardVector(GetTable().elementAt(i),vv.elementAt(j));


   attr=null;
   //attr.SetNomAtt(vv.elementAt(j));
   attr.SetNatureAtt(NatureAttr);
   attr.setCardinalite(Long.parseLong(CardAttr));
   attr.SetTabAtt(GetTable().elementAt(i));
   attr.SetTypeAtt(Vtypeattr.elementAt(j));
   attr.setAttrSelect(false);
   //attr.setOperateurs("");
   //attr.setValeurs(VTable);


   attr.SetTailleAtt(Long.parseLong(Vtailleattr.elementAt(j)));
    attRempTable.addElement(attr);

    Vattr.addElement(attr);
   }
}


   return Vattr;
}

// vecteurs des objets de type Table
public  Vector<Table> GetListTable()

{
   Attribut attr;


   Vector<Table> VTable=new Vector<Table>();
   VTable=null;
   Table tt=null;
int i;
int j;
Vector<String> vvv=new Vector<String>();
vvv=null;

vvv=GetTable();

for(i=0;i<vvv.size();i++)
{
  Vector<String> vv=new Vector<String>();
  vv=null;
  vv=GetAttrTable(GetTable().elementAt(i));
  attr=null;
   Vector <String> Vtailleattr =new Vector <String>();
   Vtailleattr=null;
   Vtailleattr=GetTailleAttr1(GetTable().elementAt(i));
   
   
   Vector <String> Vtypeattr =new Vector <String>();
   Vtypeattr=null;
   Vtypeattr=GetTypeAttr(GetTable().elementAt(i));




Vector<Attribut> attRempTable=new Vector<Attribut>();
attRempTable=null;

   for( j=0;j<vv.size();j++)
{
   String NatureAttr=null;
   NatureAttr=RecupNatureAttributs(GetTable().elementAt(i),vv.elementAt(j));

   String CardAttr=null;
   CardAttr=GetCardVector(GetTable().elementAt(i),vv.elementAt(j));


   attr=null;
   //attr.SetNomAtt(vv.elementAt(j));
   attr.SetNatureAtt(NatureAttr);
   attr.setCardinalite(Long.parseLong(CardAttr));
   attr.SetTabAtt(GetTable().elementAt(i));
   attr.SetTypeAtt(Vtypeattr.elementAt(j));
   attr.setAttrSelect(false);
   //attr.setOperateurs("");
   //attr.setValeurs(VTable);


   attr.SetTailleAtt(Long.parseLong(Vtailleattr.elementAt(j)));
    attRempTable.addElement(attr);

    //System.out.print(attr.GetNomAtt()+"  "+attr.GetTableAtt()+"  "+attr.GetNatureAtt());
   }


   //tt.SetNomTable(GetTable().elementAt(i));

   if(GetTable().elementAt(i).equals("ACTVARS"))
   tt.SetTypeTable("Dimension");
   else tt.SetTypeTable("Fais");

   
   tt.SetNb_Tuple(calcul_NbTuple(GetTable().elementAt(i)));
   tt.setInfoFrag(determineFrag(GetTable().elementAt(i)));
   tt.setTailleTuple(j);

   if(GetTable().elementAt(i).equals("ACTVARS"))
   {
   tt.setPK("CUSTOMER_LEVEL");
   tt.setFKfait("CUSTOMER_LEVEL");
   }
     if(GetTable().elementAt(i).equals("CHANLEVEL"))
     {
   tt.setPK("BASE_LEVEL");
   tt.setFKfait("BASE_LEVEL");
   }


     if(GetTable().elementAt(i).equals("CUSTLEVEL"))
         {
          tt.setPK("STORE_LEVEL");
          tt.setFKfait("STORE_LEVEL");
      }
     if(GetTable().elementAt(i).equals("PRODLEVEL"))
     {
   tt.setPK("CODE_LEVEL");
    tt.setFKfait("CODE_LEVEL");
    }

     if(GetTable().elementAt(i).equals("TIMELEVEL"))
         {
   tt.setPK("TID");
    tt.setPK("TID");
     }
 tt.SetV_Attributs(attRempTable);

   VTable.addElement(tt);


}
   
   return VTable;
}

public int CalculTailleTuple(String NomT1){

        Vector<String> VV=new Vector<String>();
        VV=GetTailleAttr(NomT1);
		int T=0;
		for(int i=0;i<VV.size();i++)
        T=T+ Integer.parseInt(VV.elementAt(i));

        return T;
	}


public  Vector<String> GetTable()
{
 Vector<String> VTable2 =new Vector<String>();

 try
 {
     

VTable2=null;
VTable2.addElement("ACTVARS");
VTable2.addElement("CHANLEVEL");
VTable2.addElement("CUSTLEVEL");
VTable2.addElement("PRODLEVEL");
VTable2.addElement("TIMELEVEL");
}

 finally {
    // return(VTable);
  }
 return(VTable2);
  }

// la taille de l'attrubut
public  Vector<String> GetTailleAttr1(String NomT)
{
   Vector<String> TailleAttr =new Vector<String>();
TailleAttr=null;

if(NomT.equals("ACTVARS"))
    {
try
{

TailleAttr.addElement("12");
TailleAttr.addElement("12");
TailleAttr.addElement("12");
TailleAttr.addElement("12");
TailleAttr.addElement("126");
TailleAttr.addElement("126");
TailleAttr.addElement("126");


    }
finally {
       //return(TailleAttr);
 //System.out.println("gg");
}
return(TailleAttr);
}


if(NomT.equals("CHANLEVEL"))
    {
try
{

TailleAttr.addElement("12");
TailleAttr.addElement("12");
    }
finally {
       //return(TailleAttr);
 //System.out.println("gg");
}
return(TailleAttr);
}


if(NomT.equals("CUSTLEVEL"))
    {
try
{

TailleAttr.addElement("12");
TailleAttr.addElement("12");

    }
finally {
     //  return(Atttable);
 //System.out.println("gg");
}
return(TailleAttr);
 }

if(NomT.equals("PRODLEVEL"))
    {
try
{

TailleAttr.addElement("12");
TailleAttr.addElement("12");
TailleAttr.addElement("12");
TailleAttr.addElement("12");
TailleAttr.addElement("12");
TailleAttr.addElement("12");



    }
finally {
       //return(TailleAttr);
 //System.out.println("gg");
}
return(TailleAttr);
}

if(NomT.equals("TIMELEVEL"))
    {
try
{

TailleAttr.addElement("12");
TailleAttr.addElement("4");
TailleAttr.addElement("6");
TailleAttr.addElement("2");
TailleAttr.addElement("2");
TailleAttr.addElement("2");


    }
finally {
      // return(TailleAttr);
 //System.out.println("gg");
}
return(TailleAttr);
}
return(TailleAttr);


}

//Fin de


// type Attrubut





public  Vector<String> GetAttrTable(String NomT)
{

Vector<String> AttrTable =new Vector<String>();
AttrTable=null;

if(NomT.equals("ACTVARS"))
    {
try
{

AttrTable.addElement("CUSTOMER_LEVEL");
AttrTable.addElement("PRODUCT_LEVEL");
AttrTable.addElement("CHANNEL_LEVEL");
AttrTable.addElement("TIME_LEVEL");
AttrTable.addElement("UNITSSOLD");
AttrTable.addElement("DOLLARSALES");
AttrTable.addElement("DOLLARCOST");
}

finally {
      // return(TailleAttr);
 System.out.println("gg");
}
}
if(NomT.equals("CHANLEVEL"))
    {
try
{

AttrTable.addElement("BASE_LEVEL");
AttrTable.addElement("ALL_LEVEL");
}

finally {
      // return(TailleAttr);
 System.out.println("gg");
}
}
if(NomT.equals("CUSTLEVEL"))
    {
try
{

AttrTable.addElement("STORE_LEVEL");
AttrTable.addElement("RETAILER_LEVEL");
}

finally {
      // return(TailleAttr);
 System.out.println("gg");
}
}
if(NomT.equals("PRODLEVEL"))
    {
try
{

AttrTable.addElement("CODE_LEVEL");
AttrTable.addElement("CLASS_LEVEL");

AttrTable.addElement("GROUP_LEVEL");
AttrTable.addElement("FAMILY_LEVEL");

AttrTable.addElement("LINE_LEVEL");
AttrTable.addElement("DIVISION_LEVEL");

}

finally {
      // return(TailleAttr);
 System.out.println("gg");
}
}

if(NomT.equals("TIMELEVEL"))
    {
try
{

AttrTable.addElement("TID");
AttrTable.addElement("YEAR_LEVEL");

AttrTable.addElement("QUARTER_LEVEL");
AttrTable.addElement("MONTH_LEVEL");

AttrTable.addElement("WEEK_LEVEL");
AttrTable.addElement("DAY_LEVEL");

}

finally {
      // return(TailleAttr);
 System.out.println("gg");
}


}
return AttrTable;

}







public  Vector<String> GetTailleAttr(String NomT)
{
    

   Vector<String> TailleAttr =new Vector<String>();
TailleAttr=null;

if(NomT.equals("ACTVARS"))
    {
try
{

TailleAttr.addElement("12");
TailleAttr.addElement("12");
TailleAttr.addElement("12");
TailleAttr.addElement("12");
TailleAttr.addElement("126");
TailleAttr.addElement("126");
TailleAttr.addElement("126");


    }
finally {
      // return(TailleAttr);
 //System.out.println("gg");
}
return(TailleAttr);
}


if(NomT.equals("CHANLEVEL"))
    {
try
{

TailleAttr.addElement("12");
TailleAttr.addElement("12");
    }
finally {
       //return(TailleAttr);
 //System.out.println("gg");
}
return(TailleAttr);
}


if(NomT.equals("CUSTLEVEL"))
    {
try
{

TailleAttr.addElement("12");
TailleAttr.addElement("12");

    }
finally {
     //  return(Atttable);
 //System.out.println("gg");
}
return(TailleAttr);
 }

if(NomT.equals("PRODLEVEL"))
    {
try
{

TailleAttr.addElement("12");
TailleAttr.addElement("12");
TailleAttr.addElement("12");
TailleAttr.addElement("12");
TailleAttr.addElement("12");
TailleAttr.addElement("12");



    }
finally {
       //return(TailleAttr);
 //System.out.println("gg");
}
return(TailleAttr);
}

if(NomT.equals("TIMELEVEL"))
    {
try
{

TailleAttr.addElement("12");
TailleAttr.addElement("4");
TailleAttr.addElement("6");
TailleAttr.addElement("2");
TailleAttr.addElement("2");
TailleAttr.addElement("2");


    }
finally {
       //return(TailleAttr);
 //System.out.println("gg");
}
return(TailleAttr);
}

return(TailleAttr);



}



public  Vector <String> GetTypeAttr(String NomT)
{

Vector<String> TypeAttr =new Vector<String>();
TypeAttr=null;

if(NomT.equals("ACTVARS"))
    {
try
{

TypeAttr.addElement("string");
TypeAttr.addElement("string");
TypeAttr.addElement("string");
TypeAttr.addElement("string");
TypeAttr.addElement("reel");
TypeAttr.addElement("reel");
TypeAttr.addElement("reel");


    }
finally {
//       return(TypeAttr);
 //System.out.println("gg");
}
return(TypeAttr);
}




if(NomT.equals("CHANLEVEL"))
    {
try
{

TypeAttr.addElement("string");
TypeAttr.addElement("string");

    }
finally {
     //  return(Atttable);
 //System.out.println("gg");
}
return(TypeAttr);
 }

if(NomT.equals("CUSTLEVEL"))
    {
try
{

TypeAttr.addElement("string");
TypeAttr.addElement("string");

    }
finally {
       //return(TypeAttr);
 //System.out.println("gg");
}
return(TypeAttr);
}

if(NomT.equals("PRODLEVEL"))
    {
try
{

TypeAttr.addElement("string");
TypeAttr.addElement("entier");
TypeAttr.addElement("string");
TypeAttr.addElement("entier");
TypeAttr.addElement("entier");
TypeAttr.addElement("entier");

    }
finally {
       //return(TypeAttr);
 //System.out.println("gg");
}
return(TypeAttr);
}


if(NomT.equals("TIMELEVEL"))
    {
try
{

TypeAttr.addElement("string");
TypeAttr.addElement("string");
TypeAttr.addElement("string");
TypeAttr.addElement("string");
TypeAttr.addElement("string");
TypeAttr.addElement("string");

    }
finally {
//       return(TypeAttr);
 //System.out.println("gg");
}
return(TypeAttr);
}
return(TypeAttr);

}





//pour utliser cette methode je dois appeler intialiser
  

// recupere nature attr dans un objet de type Vector

public String RecupNatureAttributs(String nomTable,String nomAttribut)
{
	  boolean pk=false, fk=false;
      

          String jdbcUri = "jdbc:oracle:thin:@localhost:1521:orcl1";
          String jdbcUsername = "dw";
          String jdbcPassword = "dw";



        try {
            Statement st =null;
            ResultSet rs = null;
            String str="";
        	//r�cup�ration des cl�s primaires de la table et comparaison avec l'att
        	Connection con = DriverManager.getConnection(jdbcUri, jdbcUsername, jdbcPassword);
            st = con.createStatement();
            ResultSet resultat = st.executeQuery("select U1.CONSTRAINT_TYPE " +
            		                             "from user_constraints U1, user_cons_columns U2 "+
                                                 "where U1.CONSTRAINT_NAME = U2.CONSTRAINT_NAME "+
            		                             "and U2.COLUMN_NAME='"+nomAttribut+"'"+
            		                             " and U2.Table_name='"+nomTable+"'");

            while(resultat.next()) {
                if (resultat.getString("CONSTRAINT_TYPE").equals("P"))
                    pk=true;

                else if (resultat.getString("CONSTRAINT_TYPE").equals("R"))
                    fk = true;
            }
            st.close(); resultat.close();
            } catch (SQLException ex) {
                                       Logger.getLogger(Compte.class.getName()).log(Level.SEVERE, null, ex);
                                       return(null);
                                      }
        if(pk && fk) return "PF";
        else  if(pk) return "PK";
        else if(fk) return "FK";
        return "non_clé";
    }


public long calcul_NbTuple(String nomTable)
  {
    long nb_T= 0;
    Statement st =null; ResultSet rs = null; String str="";
      try {

          String jdbcUri = "jdbc:oracle:thin:@localhost:1521:orcl1";
          String jdbcUsername = "dw";
          String jdbcPassword = "dw";

          Connection con = DriverManager.getConnection(jdbcUri, jdbcUsername, jdbcPassword);
          st = con.createStatement();
          rs = 	st.executeQuery("select count(*) from "+nomTable);
      		if(rs.next())
              nb_T = Long.parseLong(rs.getString("count(*)"));
      		
          } catch (SQLException ex)
 {Logger.getLogger(Compte.class.getName()).log(Level.SEVERE, null, ex); return(-1);}

      return (nb_T);
  }




public String determineFrag(String nomTab)
  {
          String jdbcUri = "jdbc:oracle:thin:@localhost:1521:orcl1";
          String jdbcUsername = "dw";
          String jdbcPassword = "dw";

	      Statement st =null; ResultSet rs = null; String str="";
	  try {
		  Connection con = DriverManager.getConnection(jdbcUri, jdbcUsername, jdbcPassword);
          st = con.createStatement();
		rs  =  st.executeQuery(	"select Partitioned from USER_TABLES"+
				  				" where TABLE_NAME='"+nomTab+"'");

		if(rs.next()) str = rs.getString("Partitioned");
	      } catch (SQLException e) {e.printStackTrace();}

return str;
  }

// recupere nature attr dans un objet de type Vector

public String GetCardVector(String NOMTABLE,String NOMATTRUBUT )
 {

 String card=null;
 Statement st =null; ResultSet rs = null; String str="";
 try
{
String jdbcUri = "jdbc:oracle:thin:@localhost:1521:orcl1";
String jdbcUsername = "dw";
String jdbcPassword = "dw";


Connection con = DriverManager.getConnection(jdbcUri, jdbcUsername, jdbcPassword);
 st = con.createStatement();

 rs = st.executeQuery("SELECT COUNT(DISTINCT("+NOMATTRUBUT+")) AS cardinalite FROM "+NOMTABLE);


 while (rs.next())
            {
                card=rs.getString(1);


             }
    

   }
catch(Exception e)
{
   System.out.println(" Exception");
   System.err.println(e.getMessage());
   
}

 return card;



}

 public String getPK(Table T)
  {     String s="";
		for(int i=0; i<T.getV_Attributs().size(); i++)
			if(T.getV_Attributs().elementAt(i).GetNatureAtt().equals("PK")
					                                      || T.getV_Attributs().elementAt(i).GetNatureAtt().equals("PF"))
			{ if(i>=1) s+=", "; s+=T.getV_Attributs().elementAt(i).GetNomAtt();}
		return s;
	}





//Connection con=null;


//return con;
   

}
