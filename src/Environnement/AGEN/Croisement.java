package Environnement.AGEN;
import java.util.List;
import java.util.Vector;

import org.jgap.GeneticOperator;
import org.jgap.IChromosome;
import org.jgap.Population;
import org.jgap.impl.CompositeGene;
import org.jgap.impl.IntegerGene;

/**
 * 
 * @author SAMY
 * classe qui fait un croisement multilignes sur deux
 * chromosomes selectionn�s par Roulette , avec un 
 * taux de croisement defini par l'utilisateur
 */
public class Croisement {
	
	public void operate(Population pop, int taux) {

 int ichrom1=-1 , ichrom2=-1, x; 
 CompositeGene comp2,comp; 
 Vector<IChromosome> V = new Vector<IChromosome>();
 Vector<Integer> tab = null;
 Selection select = new Selection();
  
 while (V.size() < pop.size()) { 
	 
	 /** selection de deux chromosomes par roulette **/
	 tab = new Vector<Integer>();	 
     tab = select.Roulette(pop, 2); 
     
    /* System.out.print("Selection = ");
     for(int i=0;i<tab.size();i++) System.out.print(" "+tab.elementAt(i)); 
     System.out.println();*/
 
    ichrom1 = tab.firstElement(); ichrom2 = tab.elementAt(1);
 
	IChromosome chrom1 = (IChromosome) pop.getChromosome(ichrom1).clone();
        
	IChromosome chrom2 = (IChromosome) pop.getChromosome(ichrom2).clone();
        
        
	
	/** tirer un nombre entre 0 et 100, si ce nombre est inf au taux de croisement alors effectu� croisement, 
	 * sinon ajout� les deux chromosomes telle qu'ils sont s'ils n'existent pas deja**/
	if( (int)(Math.random() *100) <=taux) {
	
	//affichage des chromosomes avant croisement
	/* System.out.println("croisement "+ichrom1+" "+ichrom2);
	 System.out.println("Avant croisement : ");
	 //affichage de chrome1
	 for (int i = 0; i < chrom1.size(); i++) {
	        comp = (CompositeGene)chrom1.getGene(i);
	        System.out.print((i+1)+") ");
	        for (int j=0; j< comp.size()-1; j++) 
	        	System.out.print(" "+((Integer)((IntegerGene)comp.geneAt(j)).getAllele()).intValue());
	        System.out.println();
	        //affichage de la case du Frag vertical
	            System.out.println("\t\t"+((Integer)((IntegerGene)comp.geneAt(comp.size()-1)).getAllele()).intValue());
	                                                }
	    System.out.println("*****************"); 
	    
	    //affichage de chrom2
		 for (int i = 0; i < chrom2.size(); i++) {
		        comp = (CompositeGene)chrom2.getGene(i);
		        System.out.print((i+1)+") ");
		        for (int j=0; j< comp.size()-1; j++) 
		        	System.out.print(" "+((Integer)((IntegerGene)comp.geneAt(j)).getAllele()).intValue());
		        System.out.println();
		        //affichage de la case du Frag vertical
		            System.out.println("\t\t"+((Integer)((IntegerGene)comp.geneAt(comp.size()-1)).getAllele()).intValue());
		                                                }
		    System.out.println("*****************");*/
	
	/** croisement de l'horizental **/
	if(AGexemple.FragH)
	for (int i = 0; i <chrom1.size(); i++) {
        comp = (CompositeGene)chrom1.getGene(i);
        comp2 = (CompositeGene)chrom2.getGene(i);
        int colonne = (int)(Math.random() * AGexemple.nbrSsDomParAttr.elementAt(i));
        //System.out.print(" "+colonne);
        for (int j=colonne; j< AGexemple.nbrSsDomParAttr.elementAt(i); j++) 
        { 
        	x=((Integer)((IntegerGene)comp.geneAt(j)).getAllele()).intValue();
            comp.geneAt(j).setAllele(((Integer)((IntegerGene)comp2.geneAt(j)).getAllele()).intValue());
            comp2.geneAt(j).setAllele(x);
        }
	                                       }
	//System.out.println();
	
	/** croisement de l'indexation **/
	int ligne = (int)(Math.random() * chrom1.size());
	if(AGexemple.IJB)
	for (int i = ligne; i <chrom1.size(); i++) {
        comp = (CompositeGene)chrom1.getGene(i);
        comp2 = (CompositeGene)chrom2.getGene(i);
        x= ((Integer)((IntegerGene)comp.geneAt(AGexemple.nbrSsDomParAttr.elementAt(i))).getAllele()).intValue();
        comp.geneAt(AGexemple.nbrSsDomParAttr.elementAt(i)).setAllele
                   (((Integer)((IntegerGene)comp2.geneAt(AGexemple.nbrSsDomParAttr.elementAt(i))).getAllele()).intValue());
        comp2.geneAt(AGexemple.nbrSsDomParAttr.elementAt(i)).setAllele(x);
	                                            }

	/** croisement des conf des vues materialis�es **/
	if(AGexemple.VM)
	for (int i = 0; i <chrom1.size(); i++) {
        comp = (CompositeGene)chrom1.getGene(i);
        comp2 = (CompositeGene)chrom2.getGene(i);
        int colonne = (int)(Math.random()*AGexemple.nbrSsDomParAttr.elementAt(i))+AGexemple.nbrSsDomParAttr.elementAt(i)+1;
        //System.out.print(" "+colonne);
        for (int j=colonne; j< AGexemple.nbrSsDomParAttr.elementAt(i)*2+1; j++) 
        { 
        	x=((Integer)((IntegerGene)comp.geneAt(j)).getAllele()).intValue();
            comp.geneAt(j).setAllele(((Integer)((IntegerGene)comp2.geneAt(j)).getAllele()).intValue());
            comp2.geneAt(j).setAllele(x);
        }
	                                       }	
	//Renumeroter les deux chromosomes
    RenumeroterH(chrom1);
    RenumeroterH(chrom2);
    RenumeroterV(chrom1);
    RenumeroterV(chrom2);
    RenumeroterVues(chrom1);
    RenumeroterVues(chrom2);
    
	//affichage du resultat du croisement effectuer
	/* System.out.println("Apr�s croisement : pointV = "+ligne);
	 
	 //affichage de chrome1
	 for (int i = 0; i < chrom1.size(); i++) {
	        comp = (CompositeGene)chrom1.getGene(i);
	        System.out.print((i+1)+") ");
	        for (int j=0; j< comp.size()-1; j++) 
	        	System.out.print(" "+((Integer)((IntegerGene)comp.geneAt(j)).getAllele()).intValue());
	        System.out.println();
	        //affichage de la case du Frag vertical
	            System.out.println("\t\t"+((Integer)((IntegerGene)comp.geneAt(comp.size()-1)).getAllele()).intValue());
	                                                }
	    System.out.println("*****************"); 
	    
	    //affichage de chrom2
		 for (int i = 0; i < chrom2.size(); i++) {
		        comp = (CompositeGene)chrom2.getGene(i);
		        System.out.print((i+1)+") ");
		        for (int j=0; j< comp.size()-1; j++) 
		        	System.out.print(" "+((Integer)((IntegerGene)comp.geneAt(j)).getAllele()).intValue());
		        System.out.println();
		        //affichage de la case du Frag vertical
		            System.out.println("\t\t"+((Integer)((IntegerGene)comp.geneAt(comp.size()-1)).getAllele()).intValue());
		                                                }
		    System.out.println("*****************");  */
	}
	
	//on insere les deux chromosomes dans la generation suivante
	if (!V.contains(chrom1)) 
	                 {V.add(chrom1);}//System.out.println("chrom1 ajout�"+V.size());}
	if (!V.contains(chrom2) && V.size() < pop.size()) 
		             {V.add(chrom2);}//System.out.println("chrom2 ajout�"+V.size());}
 }
 pop.clear();
 pop.setChromosomes(V);
	    }
	
	public static void RenumeroterH(IChromosome chrom) {
		int sauv = 0;
		 
			for(int j=0; j<chrom.size(); j++) {
			CompositeGene comp = (CompositeGene)chrom.getGene(j);
			int num = 1; 
			int nbr = AGexemple.nbrSsDomParAttr.elementAt(j); boolean[] tab = new boolean[nbr];
			for (int k=0; k<nbr; k++)
			{
				if (tab[k]!=true) {
				sauv = ((Integer)((IntegerGene)comp.geneAt(k)).getAllele()).intValue();
				comp.geneAt(k).setAllele(num);
				                  }
				for(int n=k+1; n<nbr; n++) 
				if (((Integer)((IntegerGene)comp.geneAt(n)).getAllele()).intValue()==sauv && tab[n]!=true)
					{
						comp.geneAt(n).setAllele(num); tab[n]=true;
					}
				if (tab[k]!=true) num++;
			}
		                                          } 		                                
	}

	public static void RenumeroterV(IChromosome chrom) 
	{		
		int sauv = -1; int num = 1; boolean[] tab = new boolean[chrom.size()];
		
		for (int j = 0; j < chrom.size(); j++) {
				CompositeGene comp = (CompositeGene) chrom.getGene(j);
				
				//recuperation du num d'index
				int numI = ((Integer) ((IntegerGene) comp
						.geneAt(AGexemple.nbrSsDomParAttr.elementAt(j))).getAllele()).intValue();
				
				if (tab[j] != true && numI != 0) {
					sauv = ((Integer) ((IntegerGene) comp
							.geneAt(AGexemple.nbrSsDomParAttr.elementAt(j))).getAllele()).intValue();
					comp.geneAt(AGexemple.nbrSsDomParAttr.elementAt(j)).setAllele(num);
				                                  }
				
				if(numI != 0) //si num Index = 0, eviter cette bcle
				for (int n = j + 1; n < chrom.size(); n++) {
					comp = (CompositeGene) chrom.getGene(n);
					if (((Integer) ((IntegerGene) comp.geneAt(AGexemple.nbrSsDomParAttr.elementAt(n)))
							.getAllele()).intValue() == sauv && tab[n] != true) 
					{
						comp.geneAt(AGexemple.nbrSsDomParAttr.elementAt(n)).setAllele(num);
						tab[n] = true;
					}
				                                             }
				if (tab[j] != true && numI != 0) num++;
			                                    }
    }

	public static void RenumeroterVues(IChromosome chrom) {
		int sauv = 0;
		 
		//parcours des lignes(attributs) du chromosome
		for (int j = 0; j < chrom.size(); j++) 
		{
			CompositeGene comp = (CompositeGene) chrom.getGene(j);
			int num = 1;
			int nbr = AGexemple.nbrSsDomParAttr.elementAt(j);
			boolean[] tab = new boolean[nbr];
			
			//parcours des cellules des ss dom de l'attribut
			for (int k = nbr+1; k < (nbr*2)+1; k++) 
			{
				int numV = ((Integer) ((IntegerGene) comp.geneAt(k)).getAllele()).intValue();
				if (tab[k-(nbr+1)] != true && numV!=0) 
				{
					sauv = numV;
					comp.geneAt(k).setAllele(num);
				}
				if(numV!=0)
				for (int n = k + 1; n < (nbr*2)+1; n++)
					if (((Integer) ((IntegerGene)comp.geneAt(n)).getAllele()).intValue() == sauv
						&& tab[n-(nbr+1)] != true) 
					{
						comp.geneAt(n).setAllele(num);
						tab[n-(nbr+1)] = true;
					}
				if (numV != 0 && tab[k-(nbr+1)] != true)	num++;
			}
		}		                                
	}
}
