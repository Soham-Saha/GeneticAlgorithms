//
//STABLE VERSION LOCATED AT MONALISA.JAVA
//
/*-import java.awt.Color;
import java.awt.Graphics;
import java.awt.Polygon;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Scanner;
import java.util.Vector;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;

public class Monalisa2 {
	public static BufferedImage target;
	public static JFrame frm;
	public static JLabel lbl = new JLabel();

	public static double mutation_prob = 0.02;
	public static int deltax = 150;
	public static int deltay = 150;
	public static int deltac = 150;
	public static int population_size = 50;
	public static double error_prob = 0.0001;
	public static int mutation_per_gen = 10;

	public static void main(String[] args) throws IOException, ClassNotFoundException {
		target = ImageIO.read(new File("C:\\Users\\raja\\Desktop\\target2.PNG"));
		frm = new JFrame("Genetic Algorithm");
		frm.setResizable(false);
		frm.setSize(target.getWidth() + 17, target.getHeight() + 40);
		frm.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frm.setLocation(Toolkit.getDefaultToolkit().getScreenSize().width / 2 - frm.getSize().width / 2,
				Toolkit.getDefaultToolkit().getScreenSize().height / 2 - frm.getSize().height / 2);
		frm.setAlwaysOnTop(true);
		Scanner scan = new Scanner(System.in);
		System.out.println("Read old?(Y/N):");
		Population population;
		if (scan.nextLine().equalsIgnoreCase("y")) {
			ObjectInputStream in = new ObjectInputStream(new FileInputStream("data.txt"));
			population = new Population(((Population) in.readObject()).pop);
			in.close();
		} else {
			population = new Population(new Vector<>());
			for (int i = 0; i < population_size; i++) {
				int[] xpt = new int[3];
				int[] ypt = new int[3];
				for (int v = 0; v < 3; v++) {
					xpt[v] = (int) (Math.random() * target.getWidth());
					ypt[v] = (int) (Math.random() * target.getHeight());
				}
				Color col = new Color((int) (Math.random() * 0x1000000) | 0x70000000, true);
				population.pop.add(new Shape(new Polygon(xpt, ypt, 3), col));
			}
		}
		scan.close();
		frm.setVisible(true);
		BufferedImage img = new BufferedImage(target.getWidth(), target.getHeight(), BufferedImage.TYPE_INT_ARGB);
		Graphics g = img.getGraphics();
		g.setColor(Color.black);
		g.fillRect(0, 0, img.getWidth(), img.getHeight());
		boolean updated = true;
		int pop_fitness = calculateFitness(population.pop);
		final int init_fitness = pop_fitness;
		refresh(getImageOf(population.pop));
		int sgen = 0;
		int tgen = 0;
		int written = 68;
		while (true) {
			tgen++;
			if (updated) {
				sgen++;
				pop_fitness = calculateFitness(population.pop);
				System.out.println("f=" + pop_fitness + " in sgen=" + sgen + " tgen=" + tgen + " f(delta)="
						+ (int) ((double) (init_fitness - pop_fitness) / (double) sgen) + "(/sgen), "
						+ (int) ((double) (init_fitness - pop_fitness) / (double) tgen) + "(/tgen)");
				updated = false;
			}
			Vector<Shape> pop2;
			pop2 = mutate(population.pop);
			int new_fitness = calculateFitness(pop2);
			if (new_fitness < pop_fitness || Math.random() < error_prob) {
				population.pop = pop2;
				updated = true;
				refresh(getImageOf(population.pop));
			}
			if (pop_fitness < 3200000) {
				mutation_per_gen = 20;
				deltax = 60;
				deltay = 60;
				deltac = 30;
			}
			if (pop_fitness < 2600000) {
				deltax = 30;
				deltay = 30;
				deltac = 30;
			}
			if (pop_fitness < 2300000) {
				deltax = 10;
				deltay = 10;
				deltac = 10;
			}
			if (pop_fitness / 25000 < written) {
				System.out.println("written");
				ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream("data.txt"));
				out.writeObject(population);
				out.close();
				written = pop_fitness / 25000;
			}

		}

	}

	private static BufferedImage getImageOf(Vector<Shape> pop) {
		BufferedImage img = new BufferedImage(target.getWidth(), target.getHeight(), BufferedImage.TYPE_INT_ARGB);
		Graphics g = img.getGraphics();
		g.setColor(Color.black);
		g.fillRect(0, 0, img.getWidth(), img.getHeight());
		for (Shape x : pop) {
			g.setColor(x.col);
			g.fillPolygon(x.poly);
		}
		return img;
	}

	private static Vector<Shape> mutate(Vector<Shape> pop) {
		Vector<Shape> pop2 = new Vector<>();
		int mutated = 0;
		for (Shape x : pop) {
			if (mutated < mutation_per_gen && Math.random() < mutation_prob) {
				int r = x.col.getRed();
				int g = x.col.getGreen();
				int b = x.col.getBlue();
				int[] xpt = new int[3];
				int[] ypt = new int[3];
				mutated++;
				int rand = (int) (Math.random() * 9);
				switch (rand) {
				case 0:
					xpt[0] = (int) (Math.random() * deltax - deltax / 2);
					break;
				case 1:
					xpt[1] = (int) (Math.random() * deltax - deltax / 2);
					break;
				case 2:
					xpt[2] = (int) (Math.random() * deltax - deltax / 2);
					break;
				case 3:
					ypt[0] = (int) (Math.random() * deltay - deltay / 2);
					break;
				case 4:
					ypt[1] = (int) (Math.random() * deltay - deltay / 2);
					break;
				case 5:
					ypt[2] = (int) (Math.random() * deltay - deltay / 2);
					break;
				case 6:
					r += (int) (Math.random() * deltac - deltac / 2);
					if (r < 0)
						r = 0;
					if (r > 255)
						r = 255;
					break;
				case 7:
					g += (int) (Math.random() * deltac - deltac / 2);
					if (g < 0)
						g = 0;
					if (g > 255)
						g = 255;
					break;
				case 8:
					b += (int) (Math.random() * deltac - deltac / 2);
					if (b < 0)
						b = 0;
					if (b > 255)
						b = 255;
					break;
				default:
					break;
				}
				for (int i = 0; i < 3; i++) {
					xpt[i] += x.poly.xpoints[i];
					ypt[i] += x.poly.ypoints[i];
				}
				Color newCol;
				newCol = new Color(r, g, b, x.col.getAlpha());
				pop2.add(new Shape(new Polygon(xpt, ypt, 3), newCol));
			} else {
				pop2.add(x);
			}
		}
		return pop2;
	}

	private static int calculateFitness(Vector<Shape> pop) {
		BufferedImage img = getImageOf(pop);
		int fitness = 0;
		for (int i = 0; i < target.getWidth(); i++) {
			for (int j = 0; j < target.getHeight(); j++) {
				Color c1 = new Color(img.getRGB(i, j));
				Color c2 = new Color(target.getRGB(i, j));
				fitness += Math.abs(c1.getRed() - c2.getRed()) + Math.abs(c1.getGreen() - c2.getGreen())
						+ Math.abs(c1.getBlue() - c2.getBlue());
			}
		}
		return fitness;
	}

	private static void refresh(BufferedImage img) {
		frm.remove(lbl);
		lbl = new JLabel(new ImageIcon(img));
		frm.add(lbl);
		frm.invalidate();
		frm.validate();
		frm.repaint();
	}

}

@SuppressWarnings("serial")
class Shape implements Serializable {
	Polygon poly;
	Color col;

	public Shape(Polygon poly, Color col) {
		this.poly = poly;
		this.col = col;
	}
}

@SuppressWarnings("serial")
class Population implements Serializable {
	Vector<Shape> pop;

	public Population(Vector<Shape> pop) {
		this.pop = pop;
	}
}*/