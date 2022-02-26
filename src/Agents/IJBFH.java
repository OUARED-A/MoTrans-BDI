/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package Agents;
import java.util.Random;

/**
 *
 * @author USER
 */
public class IJBFH {
public int getresullt(int w,int s)
   {
    
    Random dice =new Random ();
    int k=0;
    if(s <=5)
        k=dice.nextInt(6)*100+2000000;

    


    return k;
    
   }     

}
