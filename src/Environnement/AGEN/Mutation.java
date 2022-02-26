package Environnement.AGEN;
import java.util.Vector;

import org.jgap.IChromosome;
import org.jgap.Population;
import org.jgap.impl.CompositeGene;
import org.jgap.impl.IntegerGene;


public class Mutation {

	public void operate(Population pop,int taux) { 
		// TODO Auto-generated method stub
		Selection sel = new Selection();
		Vector<IChromosome> V = new Vector<IChromosome>();
		
		//System.out.println("Nombre d'individu mut�s = "+nbr);
		while(V.size()<pop.size()) 
		{		
		//on selectionne un chromosome aleatoirement
		int IndiceChrom = sel.Roulette(pop, 1).firstElement();
		System.out.println("Mutation iChrom = "+IndiceChrom);
                
		IChromosome chrom = (IChromosome) pop.getChromosome(IndiceChrom).clone();
                
                
                
		//on tire un nombre aleatoire pour appliquer ou non la mutation pour ce chromosome
		//si le nombre est sup a taux alors on injecte le chrom dans V sinon on mute
		
		if ( (int)(Math.random() * 100) <= taux ) {
			
			int ligne = -1, nb, numGene; CompositeGene comp = null;
			
			/** Mutation rep FH **/
			if(AGexemple.FragH) 
		{
			//on choisit une ligne aleatoire dans ce chromosome			
				while (!AGexemple.iAttrsFH.contains(ligne))
						ligne = (int)(Math.random() * chrom.size());
						
				//on choisit un gene aleatoire dans la ligne
				comp = (CompositeGene)chrom.getGene(ligne);
				numGene = (int)(Math.random() * AGexemple.nbrSsDomParAttr.elementAt(ligne)); //n� du gene a modifi�
				nb = (int)(Math.random() * AGexemple.nbrSsDomParAttr.elementAt(ligne)+1); //la nouvelle valeur a inser�e
				if(nb==0) nb=1;
				//System.out.println("Mutation du chrom : "+IndiceChrom+" "+(ligne)+" "+(numGene)+" "+nb);
		
				/*for (int j=0; j< comp.size(); j++) 
        		System.out.print(" "+((Integer)((IntegerGene)comp.geneAt(j)).getAllele()).intValue());
		    	System.out.println();*/
		
				comp.geneAt(numGene).setAllele(nb);
		
				/*for (int j=0; j< comp.size(); j++) 
        		System.out.print(" "+((Integer)((IntegerGene)comp.geneAt(j)).getAllele()).intValue());
		    	System.out.println();*/
		
				//Renumeroter la ligne chang�e du chromosome
				RenumeroterH(chrom, ligne);
		
				/*for (int j=0; j< comp.size(); j++) 
        		System.out.print(" "+((Integer)((IntegerGene)comp.geneAt(j)).getAllele()).intValue());
				System.out.println();*/
		}
			
		/** Mutation representation index **/
		if(AGexemple.IJB) 
		{
			ligne =-1;
			while(!AGexemple.iAttrsIJB.contains(ligne))
					ligne = (int)(Math.random() * chrom.size()); //choix d'1 ligne aleatoirement
		
			nb = (int)(Math.random() * chrom.size()+1); //choix du nveau num d'index
			comp = (CompositeGene)chrom.getGene(ligne);
			comp.geneAt(AGexemple.nbrSsDomParAttr.elementAt(ligne)).setAllele(nb);
		
			/** Renumeroter la representation d'index du chromosome **/
			RenumeroterV(chrom);		
		}
		
		/** Mutation rep Vues materialis�es **/		
		if(AGexemple.VM) {
			//on choisit une ligne aleatoire dans ce chromosome
			ligne = -1;
			while(!AGexemple.iAttrsVM.contains(ligne))
				ligne = (int)(Math.random() * chrom.size());
		
			//on choisit un gene aleatoire dans la ligne
			comp = (CompositeGene)chrom.getGene(ligne);
			numGene = (int)(Math.random() * AGexemple.nbrSsDomParAttr.elementAt(ligne))
		                +AGexemple.nbrSsDomParAttr.elementAt(ligne)+1; //n� du gene a modifi�
			nb = (int)(Math.random() * AGexemple.nbrSsDomParAttr.elementAt(ligne)+1); //la nouvelle valeur a inser�e		
			comp.geneAt(numGene).setAllele(nb);
		
			/** Renumeroter la representation des vues materialis�es du chromosome **/
			RenumeroterVues(chrom, ligne);
						}
		                                          }
		
		/** on insere le chromosome modifi� dans le vecteur V **/
		if(!V.contains(chrom)) V.add(chrom);
		}
		pop.clear();
		pop.setChromosomes(V);
	}
	
	private static void RenumeroterH(IChromosome chrom, int ligne) {
		int sauv = 0;
		
			//on recupere la ligne correspondante
			CompositeGene comp = (CompositeGene)chrom.getGene(ligne);
			int num = 1; //indice qui repr�sentera le nveau numero a enregistr�
			int nbr = AGexemple.nbrSsDomParAttr.elementAt(ligne); 
			boolean[] tab = new boolean[nbr];
			
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

	public static void RenumeroterV(IChromosome chrom) 
	{		
		int sauv = -1; int num = 1; 	boolean[] tab = new boolean[chrom.size()];
		
		for (int j = 0; j < chrom.size(); j++) {
				CompositeGene comp = (CompositeGene) chrom.getGene(j);
				
				//recuperation du num d'index
				int numI = ((Integer) ((IntegerGene) comp
						.geneAt(AGexemple.nbrSsDomParAttr.elementAt(j))).getAllele()).intValue();
				
				if (tab[j] != true && numI != 0) {
					sauv = numI;
					comp.geneAt(AGexemple.nbrSsDomParAttr.elementAt(j)).setAllele(num);
				                                  }
				if(numI!=0) //si le 1er = 0, eviter cette bcle
				for (int n = j + 1; n < chrom.size(); n++) {
					comp = (CompositeGene) chrom.getGene(n);
					if (((Integer) ((IntegerGene) comp.geneAt(AGexemple.nbrSsDomParAttr.elementAt(n)))
							.getAllele()).intValue() == sauv
							&& tab[n] != true) {
						comp.geneAt(AGexemple.nbrSsDomParAttr.elementAt(n)).setAllele(num);
						tab[n] = true;
					                            }
				                                             }
				if (tab[j] != true && numI !=0)	num++;
			                                    }
    }

	private static void RenumeroterVues(IChromosome chrom, int ligne) 
	{
		int sauv = 0;
		
			//on recupere la ligne correspondante
			CompositeGene comp = (CompositeGene)chrom.getGene(ligne);
			int num = 1; //indice qui repr�sentera le nveau numero a enregistr�
			int nbr = AGexemple.nbrSsDomParAttr.elementAt(ligne); 
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
