package Environnement;


import org.jgap.IChromosome;

public class Schema 
{
	public IChromosome codage = null;
	public double cout = 0.0;
    public double coutInit = 0.0;
    public String ModelCout ="";
	
	public Schema(IChromosome chrom, double cout, double coutInit, String MC)
	{
		this.codage = chrom;
		this.cout = cout;
		this.coutInit = coutInit;
		this.ModelCout = MC;
	}
	
	public double getCoutInit()
	{return this.coutInit;}
	
	public IChromosome getCodage()
	{return this.codage;}
	
	public double getCout()
	{return this.cout;}
}
