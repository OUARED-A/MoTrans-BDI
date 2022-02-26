/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Agents;
import java.lang.String;
import java.util.Vector;
/**
 *
 * @author USER
 */
public class APIIJB {
    
String    cBit= new String();

//int  cBitV[]= new int [11];

public Vector<String> ListIJB = new Vector<String>();

int nIboucle;
int ValX;
int ValY;
int k ;
boolean drap=false; 
int nbsolution =0;


int[][] matijb =new int [2048][11];

char  car1;
int j;
int nligne=-1;
int i;
int n =1;
int l=1;
int l1=1;
double espaceconfig =0.0;



double coutconfig =0;
double MinCout=0.0;

//tabTtt = new boolean[11];
static String  NomattrSel=null;


String Nomattr[]={"Family","Year","Class","City","Retailar","Month","Division","Gender","Group","All","Quarter"};
double StockageIJB[] = {30,31 , 19,41 , 109,45 , 19,56 , 33,89 , 20,90 , 19,71 ,19,41 , 19,71 , 19,71, 19,71};
double CoutIJB[] = {112, 15, 10, 112, 15,18,17,145,11,122,145,144};
boolean tabTtt[]= new boolean[11];
int tttt[];

 char hhhhhhhhh;


 
public boolean isselected(int NumAttr,int s,int w)
{ 
    boolean T=false ; 
ModeSelect ModeSelect1=new ModeSelect();
if(ModeSelect1.SelTO(s, w)==0)
    T= false;
if(ModeSelect1.SelTO(s, w)==2)
   T= false;

if(ModeSelect1.SelTO(s, w)==-1)
if(NumAttr==1 || NumAttr==3 || NumAttr==4|| NumAttr==5 || NumAttr==7)    
    T= true;
else  T= false;

if(ModeSelect1.SelTO(s, w)==1)
    T= true;



return T;
    
}        
        
        
public  Vector <String> getConfigIJB( int s,int w)
        
{   
   for(k = 0; k < 11; k++)
{
    tabTtt[k]=true;	
}
 
  n=1;
  
  for(k = 0; k < 11; k++)
   {
       if(isselected(k,s,w)==false) 
           {
             tabTtt[k]=false;
             
           }
       else
		n=n*2;	
		
       
       
       
   }
  System.out.println(" debug ************* **************************** "+n);



for(i=0; i<n; i++) {
        for(j=0; j<11; j++) {
           matijb[i][j]=0;
        }
}






 for(k = 0; k < n-1; k++)
    {
       
        ValX =k;
	cBit="";
        while (ValX > 1) 
        {
        ValY = (int) Math.floor(ValX / 2);
        cBit = String.valueOf( ValX % 2) + cBit;
        ValX = ValY;

        }
          
        cBit = String.valueOf(ValX) + cBit;
        
        
        for(i = 0; i < cBit.length(); i++)
            {   
                car1= cBit.charAt(i);  
                
                matijb[k][i]= Character.getNumericValue(car1);
               
                System.out.println(k+" "+cBit+"***********************" +i+"***");
              // String  StrMKcz= String.valueOf(hhhhhhhhh); 
              //  matijb[i][k]= Integer.parseInt(StrMKcz);
                
               //l1= Integer.parseInt(cBit);
               
              //  matijb[i][k]= l1;
                        
                
            }
        }
        
        
 for(i = 0; i < n-1; i++)     
               {   
 
         espaceconfig =0.0;
	 coutconfig =0;
        
        for(j=0;j<11;j++) 
                        {   

	if(tabTtt[j]==true)
            
                if(matijb[i][j]==1) 
                   { 
            
		    espaceconfig =espaceconfig + StockageIJB[j];
                    coutconfig=coutconfig+ CoutIJB[j];

                  }
        

             if(MinCout < coutconfig) {
             MinCout=coutconfig;
	     nligne=i;
	     drap=true;     
             espaceconfig=0;
             }
             }
        }
 
             
             
             if(nligne!=-1) {
	       if(drap==true)
                   {
                   for(j=0;j<11;j++)
                       {System.out.println("hhhhhhhhhhhhh"+nligne+"  "+j);
                       if(matijb[nligne][j]==1) 
                           {
                           espaceconfig=espaceconfig+StockageIJB[j];
                           //TableAjouteLigne(TABLE_Table1,IJB.index)
             
                          }
                       }
             
             

   //SAI_ES=PartieEntière(MinCout/100000)
	     //SAI_espaceIJB=espaceconfig
  
                   }

             }
           NomattrSel=""; 
          i=0; 
      for(j=0;j<11;j++)
          {
       if(matijb[nligne][j]==1) 
           {
           NomattrSel=NomattrSel+Nomattr[j];
           ListIJB.add(i,Nomattr[j]);
           System.out.println("*****======"+ListIJB.elementAt(i));  
           i++;
           }      
      
             }
      
  return ListIJB;  
  
             }
             }

       













    
    
    
    
    
    
