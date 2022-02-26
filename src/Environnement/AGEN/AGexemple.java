package Environnement.AGEN;

import java.awt.List;

import java.util.HashSet;
import java.util.Vector;

import org.jgap.Chromosome;
import org.jgap.Configuration;
import org.jgap.FitnessFunction;
import org.jgap.Gene;
import org.jgap.GeneticOperator;
import org.jgap.Genotype;
import org.jgap.IChromosome;
import org.jgap.InvalidConfigurationException;
import org.jgap.NaturalSelector;
import org.jgap.NaturalSelectorExt;
import org.jgap.Population;
import org.jgap.impl.CompositeGene;
import org.jgap.impl.DefaultConfiguration;
import org.jgap.impl.IntegerGene;
import org.jgap.impl.MutationOperator;

import Environnement.*;


public class AGexemple {

	private static int nbr_attr;
	private static int nbr_tables;
	public static Vector<Integer> nbrSsDomParAttr = null;
	private static int MaxEvolve = 1;
	private static int TpopInit = 1;
	private static int TauxCrois = 1;
	private static int TauxMut = 1;
	public static Vector<Double> tabFitness = null;
	public Vector<Schema> VSCH = null;
	public static Vector<Attribut> AttributCodage = null;
	public static Vector<Vector> SouDomAttrCod = null;
	private static Vector<Vector<Double>> SelDim = null;
	private static Vector<Vector<Double>> SelFait = null;
	public static Vector<Table> tablesCandidates = null;
	public static Vector<Integer> iAttrsFH=null;
	public static Vector<Integer> iAttrsIJB = null;
	public static Vector<Integer> iAttrsVM = null;
	public static double sumFitness = 0;
	private Mutation M = new Mutation();
	private Croisement C = new Croisement();
	private int W = 10, SI= 100, SVM = 30000, RowID = 0;
	private IChromosome BestOfBestChrom = null;
	public static long PS = 8, B = 60;
	private Schema SCHoptimale = null;
	private Compte compte = null;
	private Vector<Requete> req = null;
	public static boolean IJB = false, VM = false, FragH = false;

	/**
	 * @param args
	 */
	public AGexemple( 	Compte compte, Vector<Requete> req, Vector<Table> tablesCandidates,
			Vector<Attribut> AttrSelections, Vector<Integer> nbrSsDomaines,
			Vector<Vector> souDomaines, Vector<Vector<Double>> SelDim, Vector<Vector<Double>> SelFait, 
			Vector<Integer> iAttrsFH, Vector<Integer> iAttrsIJB,Vector<Integer> iAttrsVM,boolean FH,boolean IJB,boolean VM,
			int getTpopInit, int getnbGen, int getTCrois, int getTMut,  int getTPS, int getTB, int getnbW,
		    int getTSI, int getTVM, int getTRowID) 
	{
		this.nbr_attr = AttrSelections.size();
		this.nbr_tables = tablesCandidates.size();
		this.SouDomAttrCod = souDomaines;
		this.nbrSsDomParAttr = nbrSsDomaines;
		this.AttributCodage = AttrSelections;
		this.SelDim = SelDim;
		this.SelFait = SelFait;
		this.tablesCandidates = tablesCandidates;
		this.compte = compte; 
		this.req = req;
		this.iAttrsFH = iAttrsFH;
		this.iAttrsIJB = iAttrsIJB;
		this.iAttrsVM = iAttrsVM;
		this.FragH = FH; this.IJB = IJB; this.VM = VM;
		this.TpopInit = getTpopInit; this.MaxEvolve = getnbGen; this.TauxCrois = getTCrois; this.TauxMut = getTMut;
		this.PS = getTPS; this.B = getTB; this.W = getnbW; this.SI = getTSI; this.SVM = getTVM; this.RowID = getTRowID; 
		/*
		 * for(int j=0; j<this.nbrSsDomParAttr.size();j++)
		 * System.out.print(this.nbrSsDomParAttr.elementAt(j));
		 * System.out.println();
		 */		
		/** Executer l'algorithme genetique **/
		try {ConfigureJGAP();} 
		catch (Exception e) {e.printStackTrace();}
		
		//ReecRequetes RR = new ReecRequetes(this.BestOfBestChrom, this.compte, this.req);
		this.compte.fermerConnexion();
	}

	@SuppressWarnings( { "deprecation", "static-access" })
	public void ConfigureJGAP() throws Exception {
		Configuration conf = new DefaultConfiguration();
		
		/** Fonction Fitness **/
		Fitness myFunc = new Fitness(W, SI, SVM, RowID, this.compte, this.req, AttributCodage, SouDomAttrCod,
				                     SelDim, SelFait, tablesCandidates, this.FragH, this.IJB, this.VM);
		conf.setFitnessFunction(myFunc);
		conf.setKeepPopulationSizeConstant(true);

		/** on configure notre chromosome (structure de notre codage) **/
		/*
		 * System.out.println("nbrAttr = "+nbr_attr+
		 * " nbrSD = "+nbrSsDomParAttr.elementAt(0));
		 */
		Gene[] sampleGenes = new Gene[nbr_attr];

		for (int i = 0; i < nbr_attr; i++) 
		{
			CompositeGene compositeGene = new CompositeGene(conf);
			// on cr�e le tableau de l'attribut qui representera la conf FH			
			for (int j = 0; j < nbrSsDomParAttr.elementAt(i); j++) {
			IntegerGene gene = null;				
				if(FragH && iAttrsFH.contains(i))
					 gene = new IntegerGene(conf, 1, nbrSsDomParAttr.elementAt(i));
				else gene = new IntegerGene(conf, 1, 1);
				compositeGene.addGene(gene);
			                                                        }
			// on cr�e la case qui representera la  conf d'index de cet attribut
			{	IntegerGene gene =null;
				if(IJB && iAttrsIJB.contains(i))
						gene = new IntegerGene(conf, 0, nbr_attr);
				else 	gene = new IntegerGene(conf, 0, 0);
				compositeGene.addGene(gene);
			}
			//on cr�e le tableau qui representera la conf des vues materialis�es
			for (int j = 0; j < nbrSsDomParAttr.elementAt(i); j++) 
			{
				IntegerGene gene = null;
				if(VM && iAttrsVM.contains(i))
						gene = new IntegerGene(conf, 0, nbrSsDomParAttr.elementAt(i));
				else 	gene = new IntegerGene(conf, 0, 0);
				compositeGene.addGene(gene);
			 }
				sampleGenes[i] = compositeGene;
		  }

		/** Cr�ation du chromosome **/
		IChromosome sampleChromosome = new Chromosome(conf, sampleGenes);
		conf.setSampleChromosome(sampleChromosome);

		/** la taille de la population **/
		conf.setPopulationSize(TpopInit);

		/*
		 * on ajoute l'operateur de mutation conf.addGeneticOperator(new
		 * Mutation(75)); conf.addGeneticOperator(new Croisement(75));
		 */

		/** Create random initial population of Chromosomes **/
		// ------------------------------------------------
		Genotype population = Genotype.randomInitialGenotype(conf);
		Population pop = population.getPopulation();
		System.out.println("taille pop initiale = " + pop.size());

		/*
		 * //affichage initiale sans renumeroter
		 * System.out.println("affichage initiale :"); for (int i = 0; i <
		 * exSolut.size(); i++) { CompositeGene comp =
		 * (CompositeGene)exSolut.getGene(i); System.out.print((i+1)+") "); for
		 * (int j=0; j< nbrSsDomParAttr.elementAt(i); j++)
		 * System.out.print(" "+(
		 * (Integer)((IntegerGene)comp.geneAt(j)).getAllele()).intValue());
		 * System.out.println(); //affichage de la case du Frag vertical
		 * System.out
		 * .println("\t\t"+((Integer)((IntegerGene)comp.geneAt(nbrSsDomParAttr
		 * .elementAt(i))).getAllele()).intValue()); }
		 * System.out.println("*****************");
		 */

		/** Renumeroter la population ***/
		RenumeroterH(pop);
		RenumeroterIJB(pop);
		RenumeroterVues(pop);

		/***** Affichage apr�s Renumeroter sans Reparation **********/
		/*
		 * IChromosome exSolut = pop.getChromosome(1);
		 * System.out.println("affichage apr�s Renumeroter sans Reparation : ");
		 * for (int i = 0; i < exSolut.size(); i++) { CompositeGene comp =
		 * (CompositeGene)exSolut.getGene(i); System.out.print((i+1)+") "); for
		 * (int j=0; j< nbrSsDomParAttr.elementAt(i); j++)
		 * System.out.print(" "+((Integer)((IntegerGene)comp.geneAt(j)).getAllele()).intValue());
		 * System.out.println(); //affichage de la case du Frag vertical
		 * System.out
		 * .println("\t\t"+((Integer)((IntegerGene)comp.geneAt(nbrSsDomParAttr
		 * .elementAt(i))).getAllele()).intValue()); }
		 * System.out.println("*****************");
		 */

		/********** Reparation de la moiti� de la pop initiale **********/
		int nbR = 0;
		for (int i = 0; i < pop.size() && nbR < pop.size() / 2; i++) 
		{
			if (!Admissible(pop.getChromosome(i))) 
			{
				System.out.println("Reparation du Chromosome " + i + ":");
				if(!AdmissibleFH(pop.getChromosome(i))) ReparationFH(pop.getChromosome(i));
				if(!AdmissibleIndex(pop.getChromosome(i))) ReparationIJB(pop.getChromosome(i));
				if(!AdmissibleVM(pop.getChromosome(i))) ReparationVM(pop.getChromosome(i));
				nbR++;
			}
		}
		System.out.println();

		/************* Calcul du nbr de solutions admissibles *****************/
		nbR = 0;
		for (int i = 0; i < pop.size(); i++)
			if (AdmissibleFH(pop.getChromosome(i)))
				nbR++;
		System.out.println("Nombre de Chromosomes admissibles = " + nbR);
		/*
		 * //Apr�s Reparation
		 * /System.out.println("affichage apr�s Reparation : "); for (int i = 0;
		 * i < exSolut.size(); i++) { CompositeGene comp =
		 * (CompositeGene)exSolut.getGene(i); System.out.print((i+1)+") "); for
		 * (int j=0; j< nbrSsDomParAttr.elementAt(i); j++)
		 * System.out.print(" "+(
		 * (Integer)((IntegerGene)comp.geneAt(j)).getAllele()).intValue());
		 * System.out.println(); //affichage de la case du Frag vertical
		 * System.out
		 * .println("\t\t"+((Integer)((IntegerGene)comp.geneAt(nbrSsDomParAttr
		 * .elementAt(i))).getAllele()).intValue()); }
		 * System.out.println("*****************");
		 */

		/**
		 * on cr�� le tableau qui contiendra les valeurs fitness de chaque
		 * generation
		 **/
		sumFitness = 0;
		this.tabFitness = new Vector<Double>(); VSCH = new Vector<Schema>();
		// calcul des vals fitness de la pop initiale
		for (int h = 0; h < pop.size(); h++)
		{ this.tabFitness.add(myFunc.evaluate((IChromosome) pop.getChromosome(h)));
		  Schema sch = new Schema(null, myFunc.getCoutSCH(), 0.0, myFunc.getModelCout());
		  VSCH.add(sch);
		}
		
		// affichage des vals fitness
		System.out.println("les vals fitness :");
		for (int k = 0; k < this.tabFitness.size(); k++) 
		{
			System.out.print(tabFitness.elementAt(k) + " ");
			sumFitness += this.tabFitness.elementAt(k);
		}
		System.out.println();
		
		IChromosome BestChrom = null; 
		Schema BestSCH = new Schema(null, 0.0, 0.0,"");

		double F = GetBestFitnessValue(tabFitness);
		BestChrom = (IChromosome) pop.getChromosome(tabFitness.indexOf(F)).clone();
		BestSCH.cout = VSCH.elementAt(tabFitness.indexOf(F)).cout;
		BestSCH.ModelCout = VSCH.elementAt(tabFitness.indexOf(F)).ModelCout;
		
		if (!Admissible(BestChrom)) 
		{
			if(!AdmissibleFH(BestChrom)) ReparationFH(BestChrom);
			if(!AdmissibleIndex(BestChrom)) ReparationIJB(BestChrom);
			if(!AdmissibleVM(BestChrom)) ReparationVM(BestChrom);
			
			double Max = 0 , fBest;
			IChromosome C = null;
			boolean T = false;

			// on verifie s'il existe au moins une sol admissible
			for (int p = 0; p < tabFitness.size(); p++)
				if (Admissible(pop.getChromosome(p))) {
					T = true;
					Max = tabFitness.elementAt(p);
					C = (IChromosome) pop.getChromosome(p).clone();
					break;
				}

			// si existe chrom admissible : on recherche le meilleur chromosome admissible
			if (T)
				for (int p = 0; p < tabFitness.size(); p++)
					if (Max < tabFitness.elementAt(p) && Admissible(pop.getChromosome(p))) 
					{							
						Max = tabFitness.elementAt(p);
						C = (IChromosome) pop.getChromosome(p).clone();
					}
			
			/** on compare entre BestChrom r�par� et le meilleur chrom admissible **/
			fBest = myFunc.evaluate(BestChrom);
			if (T && fBest < Max) {
				System.out.println("Solution inadmissible, le meilleur Chrom Adm = "+ Max);
				
                                BestChrom = (IChromosome) C.clone();
				F = Max;
				BestSCH.cout = VSCH.elementAt(tabFitness.indexOf(F)).cout;
				BestSCH.ModelCout = VSCH.elementAt(tabFitness.indexOf(F)).ModelCout;
			                      } 
			else {
				System.out.println("Solution inadmissible, chrom repar� = "+ fBest);
				F = fBest;
				BestSCH.cout = myFunc.getCoutSCH();
				BestSCH.ModelCout = myFunc.getModelCout();
			      }
		}

		/*** Variable qui sauvegardera la meilleure solution ***/
		BestOfBestChrom = BestChrom;
		this.SCHoptimale = new Schema(null, BestSCH.cout, 0.0, BestSCH.ModelCout);
		System.out.println("la meilleur valeur fitness avant evolution = " + F);

		// System.out.println("BestOfBest = "+myFunc.getFitnessValue(BestOfBestChrom));

		/*** faire evoluer la population et la renumeroter ***/
		for (int i = 0; i < MaxEvolve; i++) 
		{
			/**** on applique la mutation ***/
			M.operate(pop, TauxMut);
			RenumeroterH(pop);
			System.out.println("evolve3 "+i);
			// RenumeroterV(pop);

			/**** on applique le croisement ****/
			C.operate(pop, TauxCrois);
			RenumeroterH(pop);
			// RenumeroterV(pop);

			/**************
			 * on cr�� le tableau qui contiendra
			 * les valeurs fitness de chaque generation
			 */
			sumFitness = 0;
			this.tabFitness.clear(); VSCH.clear();
			for (int h = 0; h < pop.size(); h++)
			{ this.tabFitness.add(myFunc.evaluate((IChromosome) pop.getChromosome(h)));
			  Schema sch = new Schema(null, myFunc.getCoutSCH(), 0.0, myFunc.getModelCout());
			  VSCH.add(sch);
			}
			
			// affichage des vals fitness
			System.out.println("les vals fitness :");
			for (int k = 0; k < this.tabFitness.size(); k++) 
			{
				System.out.print(tabFitness.elementAt(k) + " ");
				sumFitness += this.tabFitness.elementAt(k);
			}
			System.out.println("taille = " + tabFitness.size());

			/*** recup�rer la meilleure valeure fitness de cette generation ***/
			
			double F2 = GetBestFitnessValue(tabFitness);
			BestChrom = (IChromosome) pop.getChromosome(tabFitness.indexOf(F2)).clone();
			BestSCH.cout = VSCH.elementAt(tabFitness.indexOf(F2)).cout;
			BestSCH.ModelCout = VSCH.elementAt(tabFitness.indexOf(F2)).ModelCout;
			System.out.println();
			// System.out.println("Best Chrom : "+GetBestFitnessValue(tabFitness));
			System.out.println("Le meilleur chromosome de la generation "+ (i + 1) + " :" + F2);
			System.out.println();

			if (!Admissible(BestChrom)) 
			{
				if(!AdmissibleFH(BestChrom)) ReparationFH(BestChrom);
				if(!AdmissibleIndex(BestChrom)) ReparationIJB(BestChrom);
				if(!AdmissibleVM(BestChrom)) ReparationVM(BestChrom);
				
				double Max = 0 , fBest;
				IChromosome C = null;
				boolean T = false;

				// on verifie s'il existe au moins une sol admissible
				for (int p = 0; p < tabFitness.size(); p++)
					if (Admissible(pop.getChromosome(p))) {
						T = true;
						Max = tabFitness.elementAt(p);
						C = (IChromosome) pop.getChromosome(p).clone();
						break;
					}

				// si existe chrom admissible : on recherche le meilleur chromosome admissible
				if (T)
					for (int p = 0; p < tabFitness.size(); p++)
						if (Max < tabFitness.elementAt(p) && Admissible(pop.getChromosome(p))) 
						{							
							Max = tabFitness.elementAt(p);
							C = (IChromosome) pop.getChromosome(p).clone();
						}
				
				/** on compare entre BestChrom r�par� et le meilleur chrom admissible **/
				fBest = myFunc.evaluate(BestChrom);
				if (T && fBest < Max) {
					System.out.println("Solution inadmissible, le meilleur Chrom Adm = "+ Max);
					BestChrom = (IChromosome) C.clone();
					F2 = Max;
					BestSCH.cout = VSCH.elementAt(tabFitness.indexOf(F2)).cout;
					BestSCH.ModelCout = VSCH.elementAt(tabFitness.indexOf(F2)).ModelCout;
				                      } 
				else {
					System.out.println("Solution inadmissible, chrom repar� = "+ fBest);
					F2 = fBest;
					BestSCH.cout = myFunc.getCoutSCH();
					BestSCH.ModelCout = myFunc.getModelCout();
				      }
			}

			if (F < F2) {
				BestOfBestChrom.cleanup();
				F = F2;
				BestOfBestChrom = (IChromosome) BestChrom.clone();
				SCHoptimale.cout = BestSCH.cout;
				SCHoptimale.ModelCout = BestSCH.ModelCout;
			            }
		}

		/*
		 * System.out.println("affichage apr�s evolution et Renumeroter: "); for
		 * (int i = 0; i <exSolut.size(); i++) { CompositeGene comp =
		 * (CompositeGene)exSolut.getGene(i); System.out.print((i+1)+") "); for
		 * (int j=0; j< nbrSsDomParAttr[i]; j++)
		 * System.out.print(" "+((Integer)(
		 * (IntegerGene)comp.geneAt(j)).getAllele()).intValue());
		 * System.out.println(); //affichage de la case du Frag vertical
		 * System.out
		 * .println("\t\t"+((Integer)((IntegerGene)comp.geneAt(nbrSsDomParAttr
		 * [i])).getAllele()).intValue()); }
		 */
		System.out.println();
		System.out.println("**********************************************");
		System.out
				.println("**la valeur fitness BestOfBestChrom = " + F + " **");
		System.out.println("**********************************************");

		System.out.println("affichage du Best of Best Chrom :");
		for (int i = 0; i < BestOfBestChrom.size(); i++) 
		{
			CompositeGene comp = (CompositeGene) BestOfBestChrom.getGene(i);
			System.out.print((i + 1) + ") ");
			for (int j = 0; j < nbrSsDomParAttr.elementAt(i); j++)
				System.out.print(" "+((Integer) ((IntegerGene) comp.geneAt(j)).getAllele()).intValue());
			    System.out.println();
			// affichage de la case du Frag vertical
			System.out.println("\t\t"+ ((Integer) ((IntegerGene) comp.geneAt
					           (nbrSsDomParAttr.elementAt(i))).getAllele()).intValue());
			//affichage de la conf des vues materialis�es
			System.out.print("\t\t  ");
			for (int j = nbrSsDomParAttr.elementAt(i)+1; j < (nbrSsDomParAttr.elementAt(i)*2)+1; j++)
				System.out.print(" "+((Integer) ((IntegerGene) comp.geneAt(j)).getAllele()).intValue());
			    System.out.println();
		}
		System.out.println("*****************");
		System.out.println();
		SCHoptimale.codage = BestOfBestChrom;	
		SCHoptimale.coutInit = myFunc.getCoutInit();
		conf.reset();
		/*System.out.println("test validit�s schemas du Chrom Best of Best :");
		System.out.println();
		myFunc.evaluate(BestOfBestChrom);
		System.out.println();*/
}

	public Schema getSCHoptimale()
	{return this.SCHoptimale;}
	
	public static void RenumeroterH(Population pop) 
	{
		int sauv = 0;
		//parcours des chromosomes de la pop
		for (int i = 0; i < pop.size(); i++) 
		{
			//parcours des lignes(attributs) du chromosome
			for (int j = 0; j < pop.getChromosome(i).size(); j++) 
			{
				CompositeGene comp = (CompositeGene) pop.getChromosome(i).getGene(j);
				int num = 1;
				int nbr = nbrSsDomParAttr.elementAt(j);
				boolean[] tab = new boolean[nbr];
				for (int k = 0; k < nbr; k++) 
				{
					if (tab[k] != true) 
					{
						sauv = ((Integer) ((IntegerGene) comp.geneAt(k)).getAllele()).intValue();
						comp.geneAt(k).setAllele(num);
					}
					for (int n = k + 1; n < nbr; n++)
						if (((Integer) ((IntegerGene)comp.geneAt(n)).getAllele()).intValue() == sauv
							&& tab[n] != true) 
						{
							comp.geneAt(n).setAllele(num);
							tab[n] = true;
						}
					if (tab[k] != true)	num++;
				}
			}
		}
	}

	public static void RenumeroterIJB(Population pop) {
		int sauv = -1;
		//parcours des chromosomes de la pop
		for (int i = 0; i < pop.size(); i++) 
		{
			int num = 1;
			boolean[] tab = new boolean[nbr_attr];
			
			//parcours des attributs du chromosome
			for (int j = 0; j < pop.getChromosome(i).size(); j++) {
				CompositeGene comp = (CompositeGene) pop.getChromosome(i).getGene(j);
				
				//recuperation du num d'index
				int numI = ((Integer) ((IntegerGene) comp
						.geneAt(AGexemple.nbrSsDomParAttr.elementAt(j))).getAllele()).intValue();
				
				if (tab[j] != true && numI != 0) {
					sauv = ((Integer) ((IntegerGene) comp
							.geneAt(AGexemple.nbrSsDomParAttr.elementAt(j))).getAllele()).intValue();
					comp.geneAt(AGexemple.nbrSsDomParAttr.elementAt(j)).setAllele(num);
				                                  }
				
				if(numI != 0) //si num Index = 0, eviter cette bcle
				for (int n = j + 1; n < pop.getChromosome(i).size(); n++) {
					comp = (CompositeGene) pop.getChromosome(i).getGene(n);
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
	}

	public static void RenumeroterVues(Population pop) 
	{
		int sauv = 0;
		//parcours des chromosomes de la pop
		for (int i = 0; i < pop.size(); i++) 
		{
			//parcours des lignes(attributs) du chromosome
			for (int j = 0; j < pop.getChromosome(i).size(); j++) 
			{
				CompositeGene comp = (CompositeGene) pop.getChromosome(i).getGene(j);
				int num = 1;
				int nbr = nbrSsDomParAttr.elementAt(j);
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
	
	public double GetBestFitnessValue(Vector<Double> V) {
		Double Max = V.elementAt(0);
		for (int i = 1; i < V.size(); i++)
			if (Max < V.elementAt(i))
				Max = V.elementAt(i);
		return Max;
	}

	private boolean AdmissibleFH(IChromosome individu) {
		HashSet<Integer> HS = new HashSet<Integer>();
		int ind;
		int nbFrag = 1;
		CompositeGene comp;

		// calcul du nombre de fragments de ce shema
		for (int i = 0; i < individu.size(); i++) {
			comp = (CompositeGene) individu.getGene(i);
			HS.clear();
			for (int j = 0; j < nbrSsDomParAttr.elementAt(i); j++) {
				ind = ((Integer) ((IntegerGene) comp.geneAt(j)).getAllele()).intValue();
				HS.add(ind);
			}
			nbFrag *= HS.size();
		}

		if (this.W >= nbFrag) {// System.out.println("Admissible : "+nbFrag);
			return true;
		}
		// System.out.println("Non Admissible : "+nbFrag);
		return false;
	}

	private boolean AdmissibleIndex(IChromosome individu) 
	{
		HashSet<Integer> HS = new HashSet<Integer>();
		long RowID = 1;
	    long CardAttrs = 0;
		CompositeGene comp;

		// calcul du nombre d'attributs index�s et d'index du schema 
		for (int i = 0; i < individu.size(); i++) 
		{
			comp = (CompositeGene) individu.getGene(i);
			if(((Integer) ((IntegerGene) comp.geneAt(nbrSsDomParAttr.elementAt(i))).getAllele()).intValue()>0)
			  {	
				CardAttrs += AttributCodage.elementAt(i).getCardinalite(); 
			  	HS.add(((Integer)((IntegerGene)comp.geneAt(nbrSsDomParAttr.elementAt(i))).getAllele()).intValue());
			  }
		}		
			double Taille = ((HS.size() * RowID + CardAttrs) * this.compte.getTableFaits().getNb_Tuple())/ 8;
			if(Taille==0) Taille = 1.0;
			
			System.out.println("Taille conf IJB : "+Taille+" CardAttr = "+CardAttrs);
			if(Taille<=SI) return true;
			
			return false;
	}

	private boolean AdmissibleVM(IChromosome individu) 
	{
		double Taille = 0; //en octects
		CompositeGene comp;		 

		// calcul du nombre de fragments de ce shema
		for (int i = 0; i < individu.size(); i++) 
		{
			comp = (CompositeGene) individu.getGene(i);
			int nbr =  nbrSsDomParAttr.elementAt(i);
				
			for (int j = 0; j < nbr; j++) 
			{
				if(((Integer) ((IntegerGene) comp.geneAt(j+nbr+1)).getAllele()).intValue()>0)
				Taille += (this.SelFait.elementAt(i).elementAt(j) * this.compte.getTableFaits().getNb_Tuple())
							* this.compte.getTableFaits().getTailleTuple();
			}
		}
		System.out.println("Taille conf VM : "+Taille);
		
		if (this.SVM >= Taille) {// System.out.println("Admissible : "+nbFrag);
									return true;
								 }
		// System.out.println("Non Admissible : "+nbFrag);
		return false;
	}
	
	private void ReparationFH(IChromosome individu) 
	{
		System.out.println("Reparation FH");
		while (!AdmissibleFH(individu)) 
		{
			boolean T = false;
			int ligne = -1, Max = 0;
			CompositeGene comp = null;
            
			// 1-on recherche une ligne dont le max>1 pour faire une fusion
			while (!T) {
				ligne = (int) (Math.random() * individu.size());
				// System.out.print("L:"+ligne);
				comp = (CompositeGene) individu.getGene(ligne);
				Max = MaxLigne(comp, ligne);
				if (Max > 1)
					T = true;
			            }
			
			// 2-on fusionne deux SDs dont les frags sont diff
			boolean F = false;
			// System.out.println(" Max Av: "+Max);
			while (!F) {
				T = false;
				int sd1 = -1, sd2 = -1;
				// on selectionne deux SDs dont les frags sont diff
				while (!T) 
				{
					sd1 = (int) (Math.random() * nbrSsDomParAttr.elementAt(ligne));
					sd2 = (int) (Math.random() * nbrSsDomParAttr.elementAt(ligne));
					if (comp.geneAt(sd1).getAllele() != comp.geneAt(sd2).getAllele()) T = true;
				}
				// System.out.println(" sd1: "+sd1+" sd2: "+sd2);
				comp.geneAt(sd2).setAllele(comp.geneAt(sd1).getAllele());
				RenumeroterLigne(comp, ligne);
				// System.out.println(" Max Apr�s : "+MaxLigne(comp));
				if (Max > MaxLigne(comp, ligne))
					F = true;
			             }
		}
	}

	private void ReparationIJB(IChromosome individu)
	{
		System.out.println("Reparation IJB");
		while (!AdmissibleIndex(individu)) 
		{				
			//on tire une ligne au hasard
			int ligne = (int)(Math.random() * individu.size());
						
			CompositeGene comp = (CompositeGene) individu.getGene(ligne); 
			if(((Integer) ((IntegerGene) comp.geneAt(nbrSsDomParAttr.elementAt(ligne))).getAllele()).intValue()>0)
				comp.geneAt(nbrSsDomParAttr.elementAt(ligne)).setAllele(0);			
		}
			RenumeroterIJB(individu);
	}

	private void ReparationVM(IChromosome individu)
	{
	 System.out.println("Reparation VM");	
		while (!AdmissibleVM(individu)) 
		{				
			//on tire une ligne au hasard
			int ligne = (int)(Math.random() * individu.size());
			int colonne = (int)(Math.random() * nbrSsDomParAttr.elementAt(ligne));
			int decal = nbrSsDomParAttr.elementAt(ligne)+1;
			
			CompositeGene comp = (CompositeGene) individu.getGene(ligne); 
			if(((Integer) ((IntegerGene) comp.geneAt(decal+colonne)).getAllele()).intValue()>0)
				{comp.geneAt(decal+colonne).setAllele(0); RenumeroterVues(individu, ligne);}
		}
	}
	
	private int MaxLigne(CompositeGene comp, int L) {
		/**calcul le frag max d'un attribut **/
		int Max = ((Integer) ((IntegerGene) comp.geneAt(0)).getAllele()).intValue();
		for (int k = 1; k < nbrSsDomParAttr.elementAt(L); k++)
			if (Max < ((Integer) ((IntegerGene) comp.geneAt(k)).getAllele()).intValue())
				Max = ((Integer) ((IntegerGene) comp.geneAt(k)).getAllele()).intValue();
		return Max;
	}

	private void RenumeroterLigne(CompositeGene comp, int L) 
	{
		int num = 1, sauv = 0;
		int nbr = nbrSsDomParAttr.elementAt(L);
		boolean[] tab = new boolean[nbr];
		for (int k = 0; k < nbr; k++) 
		{
			if (tab[k] != true) 
			{
				sauv = ((Integer) ((IntegerGene) comp.geneAt(k)).getAllele()).intValue();
				comp.geneAt(k).setAllele(num);
			}
			for (int n = k + 1; n < nbr; n++)
				 if (((Integer) ((IntegerGene) comp.geneAt(n)).getAllele())
						         .intValue() == sauv && tab[n] != true ) 
				{
					comp.geneAt(n).setAllele(num);
					tab[n] = true;
				}
			if (tab[k] != true)	num++;
		}
	}
 
	private void RenumeroterVues(IChromosome chrom, int ligne) 
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

	private void RenumeroterIJB(IChromosome chrom) 
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

	private boolean Admissible(IChromosome chrom) 
	{		
		if(AdmissibleFH(chrom) && AdmissibleIndex(chrom) && AdmissibleVM(chrom)) return true;
		return false;
	}
}
