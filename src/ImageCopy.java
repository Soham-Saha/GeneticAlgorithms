import java.awt.Color;
import java.awt.Graphics;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Vector;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JPanel;

public class ImageCopy {

	public static String genes = "01";
	public static double mutation_probability = 0.001;
	public static int population_size = 100;
	public static double prog = 0;
	public static JFrame frm;
	public static int boxs = 5;
	public static JPanel pnl = new JPanel();
	public static BufferedImage img;
	public static String target = "";

	public static void main(String[] args) throws IOException {
		System.out.println("Setting target");
		initializeTarget();
		System.out.println("target set");
		initializeDisplay();
		System.out.println("display created\ntraining...");
		GA genAlgo = new GA(genes, img.getWidth() * img.getHeight(), mutation_probability, population_size) {

			@SuppressWarnings("serial")
			@Override
			void displayStats(Vector<Individual> population, int gen) {
				final Individual best = population.firstElement();
				prog = (target.length() - best.fitness) * 100 / target.length();
				System.out.println("In generation: " + gen + " progress: " + prog + "%");
				frm.remove(pnl);
				pnl = new JPanel() {
					@Override
					protected void paintComponent(Graphics g) {
						int ct = 0;
						for (int i = 0; i < img.getHeight(); i++) {
							for (int j = 0; j < img.getWidth(); j++) {
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
		genAlgo.train();
		System.out.println("trained");
	}

	private static void initializeTarget() throws IOException {
		img = ImageIO.read(new File("C:\\Users\\raja\\Desktop\\0.png"));
		for (int i = 0; i < img.getHeight(); i++) {
			for (int j = 0; j < img.getWidth(); j++) {
				target += (new Color(img.getRGB(j, i)).getRed()) > 127.5 ? '1' : '0';
			}
		}

	}

	private static void initializeDisplay() {
		frm = new JFrame("Genetic Algorithm");
		frm.setResizable(false);
		frm.setSize(img.getWidth() * boxs + 17, img.getHeight() * boxs + 40);
		frm.setLocation(Toolkit.getDefaultToolkit().getScreenSize().width / 2 - frm.getSize().width / 2,
				Toolkit.getDefaultToolkit().getScreenSize().height / 2 - frm.getSize().height / 2);
		frm.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frm.setVisible(true);
	}
}
