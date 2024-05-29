import java.util.Comparator;
import java.util.Vector;

public abstract class GA {

	public String genes;
	public int chromosome_length;
	public double mutation_probability;
	public int population_size;

	public GA(String genes, int chromosome_length, double mutation_probability, int population_size) {
		this.genes = genes;
		this.chromosome_length = chromosome_length;
		this.mutation_probability = mutation_probability;
		this.population_size = population_size;
	}

	public void train() {
		int gen = 0;
		Vector<Individual> population = new Vector<>();
		for (int i = 0; i < population_size; i++) {
			population.add(randomIndividual());
		}
		reorder(population);
		while (true) {
			updateMutationRate();
			if (population.firstElement().fitness <= 0) {
				break;
			}
			Vector<Individual> new_gen = new Vector<>();
			// elitism:
			int s = (10 * population_size) / 100;
			for (int i = 0; i < s; i++)
				new_gen.add(population.get(i));
			// now reproduction:
			s = population_size - s;
			for (int i = 0; i < s; i++) {
				int len = population.size();
				Individual parent1 = population.get((int) (Math.random() * len / 2));
				Individual parent2 = population.get((int) (Math.random() * len / 2));
				new_gen.add(crossOver(parent1, parent2));
			}
			population = new_gen;
			reorder(population);
			displayStats(population, gen);
			gen++;
		}
		displayStats(population, gen);
	}

	private static void reorder(Vector<Individual> population) {
		population.sort(new Comparator<Individual>() {
			@Override
			public int compare(Individual i1, Individual i2) {
				return (int) (i1.fitness - i2.fitness);
			}
		});
	}

	public Individual randomIndividual() {
		String chr = create_gnome();
		return new Individual(chr, calculateFitness(chr));
	}

	public Individual crossOver(Individual p1, Individual p2) {
		double x = mutation_probability;
		String child_chromosome = "";
		int len = p1.chromosome.length();
		for (int i = 0; i < len; i++) {
			double p = Math.random();
			if (p < (1 - x) / 2)
				child_chromosome += p1.chromosome.charAt(i);
			else if (p < 1 - x)
				child_chromosome += p2.chromosome.charAt(i);
			else
				child_chromosome += mutated_genes();
		}
		return new Individual(child_chromosome, calculateFitness(child_chromosome));
	}

	private String create_gnome() {
		String gnome = "";
		for (int i = 0; i < chromosome_length; i++)
			gnome += mutated_genes();
		return gnome;
	}

	private char mutated_genes() {
		return genes.charAt((int) (genes.length() * Math.random()));
	}

	abstract void displayStats(Vector<Individual> population, int gen);

	abstract int calculateFitness(String chromosome);

	abstract void updateMutationRate();
}