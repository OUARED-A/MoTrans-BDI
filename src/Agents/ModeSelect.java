/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package Agents;

/**
 *
 * @author USER
 */
public class ModeSelect {

public int  SelTO(int w,int s)
  { int k=-1;
    if(w==0 && s!=0)
    k=1;
    if(s==0 &&  w!=0)
    k=0;
    if(s==0 &&  w==0)
    k=2;
    return k;
  }
// classification K-means
public String  Classification(int I)
        { String Listattr[] = {"Family", "Division", "Class", "City", "Retailer" ,"Gender", "Month", "Year", "All", "Quarter", "Group"};
          double Poidattr[] = {0.58,1.65,1.16,2.056,1.51 ,2.874 ,2.425 ,2.356 ,2.874,2.686,065};
          
          
          
        
            String text1="";
        return text1;
        }



}
