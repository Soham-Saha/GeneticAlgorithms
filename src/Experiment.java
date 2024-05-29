import java.awt.Color;
import java.awt.Graphics;
import java.awt.Toolkit;
import java.io.IOException;
import java.util.Vector;

import javax.swing.JFrame;
import javax.swing.JPanel;

public class Experiment {

	public static String genes = "01";
	public static double mutation_probability = 0.0001;
	public static int population_size = 50;
	public static int chromosome_length = 28 * 28;
	public static double prog = 0;
	public static JFrame frm;
	public static int boxs = 3;
	public static JPanel pnl = new JPanel();

	public static void main(String[] args) throws IOException {
		System.out.println("target set");
		initializeDisplay();
		System.out.println("display created\ntraining...");
		GA genAlgo = new GA(genes, chromosome_length, mutation_probability, population_size) {

			@SuppressWarnings("serial")
			@Override
			void displayStats(Vector<Individual> population, int gen) {
				final Individual best = population.firstElement();
				prog = (chromosome_length - best.fitness) * 100 / chromosome_length;
				System.out.println("In generation: " + gen + " progress: " + prog + "%");
				frm.remove(pnl);
				pnl = new JPanel() {
					@Override
					protected void paintComponent(Graphics g) {
						int ct = 0;
						for (int i = 0; i < 28; i++) {
							for (int j = 0; j < 28; j++) {
								g.setColor(best.chromosome.charAt(ct) == '0' ? Color.black : Color.white);
								g.fillRect(j * boxs, i * boxs, boxs, boxs);
								ct++;
							}
						}
					}
				};
				frm.add(pnl);
				frm.invalidate();
				frm.validate();
				frm.repaint();

			}

			@Override
			int calculateFitness(String chromosome) {
				return chromosome.replaceAll("0", "").length();
			}

			@Override
			void updateMutationRate() {
				if (prog >= 90) {
					this.mutation_probability = 0.001;
				}
			}
		};
		genAlgo.train();
		System.out.println("trained");
	}

	private static void initializeDisplay() {
		frm = new JFrame("Genetic Algorithm");
		frm.setResizable(false);
		frm.setSize(28 * boxs + 17, 28 * boxs + 40);
		frm.setLocation(Toolkit.getDefaultToolkit().getScreenSize().width / 2 - frm.getSize().width / 2,
				Toolkit.getDefaultToolkit().getScreenSize().height / 2 - frm.getSize().height / 2);
		frm.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frm.setVisible(true);
	}
}
