import java.util.Vector;

public class TextCopy {
	public static double mutation_probability = 0.01;
	public static int population_size = 100;
	public static final String genes = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890, .-;:_!\"'#%&/()=?@${[]}";
	public static double prog;
	public static final String target = "To optimize mutation rate for genetic algorithms (GA) using adaptive change, To optimize mutation rate for genetic algorithms (GA) using adaptive change, using adaptive change, To optimize mutation rate for genetic algorithms (GA) using adaptive change, using adaptive change, To optimize mutation rate for genetic algorithms (GA) using adaptive change,";

	public static void main(String[] args) {
		GA data = new GA(genes, target.length(), mutation_probability, population_size) {

			@Override
			void displayStats(Vector<Individual> population, int gen) {
				prog = (target.length() - population.firstElement().fitness) * 100 / target.length();
				System.out.println("In generation: " + gen + "   string: " + population.firstElement().chromosome
						+ "\nprogress: " + prog + "%");

			}

			@Override
			int calculateFitness(String chromosome) {
				int fitness = 0;
				for (int i = 0; i < target.length(); i++) {
					if (chromosome.charAt(i) != target.charAt(i))
						fitness++;
				}
				return fitness;
			}

			@Override
			void updateMutationRate() {

			}
		};
		data.train();

	}

}
