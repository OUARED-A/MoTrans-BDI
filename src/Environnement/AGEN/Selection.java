package Environnement.AGEN;
import java.util.Vector;


import org.jgap.Population;
import java.util.Random;


public class Selection {

	
	public Vector<Integer> Roulette (Population pop, int nbr) {
		// TODO Auto-generated method stub 
		int nbVal = 0;
    double sumFitness = AGexemple.sumFitness;
    Vector<Double> pourcentage = new Vector<Double>();
    
    //Calcul nbr val fitness diff de 1.0
    for(int i=0; i<AGexemple.tabFitness.size(); i++) if(AGexemple.tabFitness.elementAt(i)!=1.0) nbVal++;

  /*  System.out.println("Select Sum Fitness = "+sumFitness);
    System.out.println("Taille pop = "+pop.size()+" Taille tab Fitness = "+AGexemple.tabFitness.size());*/
    
    double sumP=0;
    for(int i=0;i<pop.size(); i++) {    	                            
    	                            pourcentage.add(AGexemple.tabFitness.elementAt(i)*100 / sumFitness); 
                                    sumP+=pourcentage.elementAt(i);
                                    }
    
   /* System.out.println("Sum Pourcentage = "+sumP);  
    for(int k=0; k<pourcentage.size(); k++) System.out.print(" "+pourcentage.elementAt(k));
    System.out.println();*/
    
    Vector<Integer> tabResult = new Vector<Integer>();
    int num, i, j, cpt = 0,x = 0;; double nb;
    
    while (tabResult.size()<nbr) {
    	cpt++;
    	num = (int)(Math.random() * Math.round(sumP));
    	//System.out.println("Num slectionn� = "+num); 
        nb =0; 
        for(i=0;i<pourcentage.size(); i++)  
                                 {if (num <= pourcentage.elementAt(i)+nb) break;
                                  nb+=pourcentage.elementAt(i);
                                 }
    
        if(!tabResult.contains(i)) tabResult.add(i);
        else if(cpt>3) { boolean T = false;
        	        while (!T)
                  { x = (int) (Math.random() * pop.size());
                  //System.out.println(x);
                    if(!tabResult.contains(x)) {tabResult.add(x); T=true;}
        	        cpt = 0;
                   }
                  }
                                  }
    /*for(int k=0; k<tabResult.size(); k++) System.out.print(" Select Result = "+tabResult.elementAt(k));
    System.out.println();*/
    
    return tabResult;
	}
        
public double getresullt(int w,int s)
   {
    
    Random dice =new Random ();
    double k=0;
    if(s <=5)
        k=dice.nextInt(6)*100+2000000;

    
    return k;
    
   }     

        
        
        
}
