
/**
 * @author Soham Saha
 */

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;
import java.util.stream.Collectors;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

public class Monalisa {
	public static BufferedImage target;
	public static ArrayList<Rect> currentRectSet;
	public static JFrame frm;
	public static JLabel imgDisplay;
	public static JTextArea stat;
	public static Color bg_color;

	// HYPER-PARAMETERS
	public static int delta_x;
	public static int delta_y;
	public static int delta_w;
	public static int delta_h;
	public static int delta_c;
	public static int population_size;
	public static double error_prob;
	public static int mutation_per_gen;

	public static void main(String[] args) {
		initializeTarget();
		preprocessTarget();
		setHyperparameters();
		initializeDisplay();
		initializePopulation();
		frm.setVisible(true);
		startGeneticAlgorithm();
	}

	private static void initializeTarget() {
		try {
			target = ImageIO.read(new File("D:\\main_drive\\alldata\\Mine\\Others\\monalisa.png"));
		} catch (IOException e) {
			System.out.println("Couldn't locate target. (error: " + e.getMessage() + ")");
			System.exit(0);
		}
	}

	private static void preprocessTarget() {
		Image tmp = target.getScaledInstance(214, 242, Image.SCALE_SMOOTH);
		target = new BufferedImage(tmp.getWidth(null), tmp.getHeight(null), BufferedImage.TYPE_INT_ARGB);
		target.createGraphics().drawImage(tmp, 0, 0, null);
	}

	private static void setHyperparameters() {
		delta_x = 100;
		delta_y = 100;
		delta_w = 100;
		delta_h = 100;
		delta_c = 100;
		population_size = 300;
		error_prob = 0.0001;
		mutation_per_gen = 10;
	}

	/*
	 * private static Color getMeanColor(BufferedImage img) { double[] sum = { 0, 0,
	 * 0 }; for (int i = 0; i < img.getWidth(); i++) { for (int j = 0; j <
	 * img.getHeight(); j++) { Color c = new Color(img.getRGB(i, j)); sum[0] +=
	 * c.getRed() * c.getRed(); sum[1] += c.getBlue() * c.getBlue(); sum[2] +=
	 * c.getGreen() * c.getGreen(); } } for (int i = 0; i < 3; i++) { sum[i] /=
	 * (img.getWidth() * img.getHeight()); } return new Color((int)
	 * Math.sqrt(sum[0]), (int) Math.sqrt(sum[1]), (int) Math.sqrt(sum[2])); }
	 *
	 * private static Color getModalColor(BufferedImage img) { Map<Color, Integer>
	 * colorCounts = new HashMap<>(); int maxCount = 0; Color modalColor = null; for
	 * (int i = 0; i < img.getWidth(); i++) { for (int j = 0; j < img.getHeight();
	 * j++) { Color c = new Color(img.getRGB(i, j)); Color color = new
	 * Color(c.getRed() / 5 * 5, c.getBlue() / 5 * 5, c.getGreen() / 5 * 5); int
	 * count = colorCounts.getOrDefault(color, 0) + 1; colorCounts.put(color,
	 * count); if (count > maxCount) { maxCount = count; modalColor = color; } } }
	 * System.out.println(maxCount); return modalColor; }
	 */

	private static void initializeDisplay() {
		frm = new JFrame("Genetic Algorithm");
		frm.setResizable(false);
		frm.setSize(target.getWidth() + 420 + 17, target.getHeight() + 40);
		frm.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frm.setLocation(Toolkit.getDefaultToolkit().getScreenSize().width / 2 - frm.getSize().width / 2,
				Toolkit.getDefaultToolkit().getScreenSize().height / 2 - frm.getSize().height / 2);
		frm.setAlwaysOnTop(true);
		frm.setLayout(null);
		imgDisplay = new JLabel();
		imgDisplay.setLocation(0, 0);
		imgDisplay.setSize(target.getWidth(), target.getHeight());
		frm.add(imgDisplay);
		stat = new JTextArea("STATISTICS");
		stat.setEditable(false);
		JScrollPane statpane = new JScrollPane(stat);
		statpane.setLocation(imgDisplay.getWidth(), 0);
		statpane.setSize(420, imgDisplay.getHeight());
		statpane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		frm.add(statpane);
	}

	private static void initializePopulation() {
		Scanner scan = new Scanner(System.in);
		System.out.println("Read old?(Y/N):");
		try {
			if (scan.nextLine().equalsIgnoreCase("y")) {
				ObjectInputStream in = new ObjectInputStream(new FileInputStream("datarect.txt"));
				RectSet set = ((RectSet) in.readObject());
				currentRectSet = set.rectSet;
				bg_color = set.bg_color;
				in.close();
			} else {
				createRandomPopulation();
				bg_color = Color.black;
			}
		} catch (IOException | ClassNotFoundException e) {
			System.out.println("Data not available. Using random population. (error: " + e.getMessage() + ")");
			createRandomPopulation();
		}
		scan.close();
	}

	private static void createRandomPopulation() {
		currentRectSet = new ArrayList<Rect>();
		for (int i = 0; i < population_size; i++) {
			currentRectSet.add(Rect.randomRect());
		}
	}

	private static void startGeneticAlgorithm() {
		boolean updated = true;
		double unfitness = calculateUnfitness(currentRectSet);
		final double init_unfitness = unfitness;
		refresh(getImageOf(currentRectSet));
		int sgen = 0;
		int tgen = 0;
		boolean written = false;
		int out = 0;
		while (true) {
			adaptToUnfitness(unfitness);
			tgen++;
			if (updated) {
				sgen++;
				unfitness = calculateUnfitness(currentRectSet);
				stat.append("\nuf=" + (int) unfitness + " in sgen=" + sgen + " tgen=" + tgen + " uf(delta)="
						+ (int) ((init_unfitness - unfitness) / sgen) + "(/sgen), "
						+ (int) ((init_unfitness - unfitness) / tgen) + "(/tgen)");
				if (out == 0) {
					System.out.println(tgen + "\t" + unfitness);
				}
				out = (1 + out) % 4;
				stat.setCaretPosition(stat.getDocument().getLength());
				updated = false;
			}
			ArrayList<Rect> rectSet2;
			rectSet2 = mutate(currentRectSet);
			double new_unfitness = calculateUnfitness(rectSet2);
			if (new_unfitness < unfitness || Math.random() < error_prob) {
				currentRectSet = rectSet2;
				updated = true;
				refresh(getImageOf(currentRectSet));
			}
			Color new_bg_color = mutateBackgroundColor();
			new_unfitness = calculateUnfitness(rectSet2, new_bg_color);
			if (new_unfitness < unfitness || Math.random() < error_prob) {
				bg_color = new_bg_color;
				updated = true;
				refresh(getImageOf(currentRectSet));
			}
			if (unfitness < 1000000 && !written) {
				written = true;
				write(currentRectSet);
			}
		}
	}

	private static Color mutateBackgroundColor() {
		int r = bg_color.getRed();
		int g = bg_color.getGreen();
		int b = bg_color.getBlue();
		int rand = (int) (Math.random() * 3);
		switch (rand) {
		case 0:
			r = mutateValue(r, delta_c, 0, 255);
			break;
		case 1:
			g = mutateValue(g, delta_c, 0, 255);
			break;
		case 2:
			b = mutateValue(b, delta_c, 0, 255);
			break;
		}
		return new Color(r, g, b);
	}

	private static void write(ArrayList<Rect> rectSet) {
		try {
			ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream("datarect.txt"));
			out.writeObject(new RectSet(rectSet, bg_color));
			out.close();
			System.out.println("Written.");
		} catch (IOException e) {
			System.out.println("Couldn't write. Error: " + e.getMessage());
		}
	}

	private static void adaptToUnfitness(double unfitness) {
		if (unfitness < 2500000) {
			mutation_per_gen = 20;
			delta_x = 10;
			delta_y = 10;
			delta_c = 10;
			delta_w = 10;
			delta_h = 10;
		} else if (unfitness < 3500000) {
			delta_x = 50;
			delta_y = 50;
			delta_c = 50;
			delta_w = 50;
			delta_h = 50;
		}

	}

	private static BufferedImage getImageOf(ArrayList<Rect> currentRectSet2, Color bg) {
		BufferedImage img = new BufferedImage(target.getWidth(), target.getHeight(), BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = img.createGraphics();
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
		g.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_SPEED);
		g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED);
		g.setColor(bg);
		g.fillRect(0, 0, img.getWidth(), img.getHeight());
		for (Rect x : currentRectSet2) {
			g.setColor(x.col);
			g.fillRect(x.x, x.y, x.width, x.height);
		}
		g.dispose();
		return img;
	}

	private static BufferedImage getImageOf(ArrayList<Rect> currentRectSet2) {
		return getImageOf(currentRectSet2, bg_color);
	}

	private static int mutateValue(int value, int delta, int minValue, int maxValue) {
		int newValue = value + (int) (Math.random() * delta - delta / 2);
		return Math.min(Math.max(newValue, minValue), maxValue);
	}

	private static ArrayList<Rect> mutate(ArrayList<Rect> currentRectSet2) {
		ArrayList<Rect> rectSet2 = new ArrayList<>();
		List<Integer> randl = new Random().ints(0, currentRectSet2.size()).distinct().limit(mutation_per_gen).boxed()
				.collect(Collectors.toList());
		int mutated = 0;
		for (Rect x : currentRectSet2) {
			if (randl.contains(mutated)) {
				int newX = x.x;
				int newY = x.y;
				int newWidth = x.width;
				int newHeight = x.height;
				int r = x.col.getRed();
				int g = x.col.getGreen();
				int b = x.col.getBlue();
				int rand = (int) (Math.random() * 7);

				switch (rand) {
				case 0:
					newX = mutateValue(newX, delta_x, 0, Integer.MAX_VALUE);
					break;
				case 1:
					newY = mutateValue(newY, delta_y, 0, Integer.MAX_VALUE);
					break;
				case 2:
					newWidth = mutateValue(newWidth, delta_w, 0, Integer.MAX_VALUE);
					break;
				case 3:
					newHeight = mutateValue(newHeight, delta_h, 0, Integer.MAX_VALUE);
					break;
				case 4:
					r = mutateValue(r, delta_c, 0, 255);
					break;
				case 5:
					g = mutateValue(g, delta_c, 0, 255);
					break;
				case 6:
					b = mutateValue(b, delta_c, 0, 255);
					break;
				default:
					break;
				}

				Color newCol = new Color(r, g, b, x.col.getAlpha());
				rectSet2.add(new Rect(newX, newY, newWidth, newHeight, newCol));
			} else {
				rectSet2.add(x);
			}
			mutated++;
		}
		return rectSet2;
	}

	private static double calculateUnfitness(ArrayList<Rect> currentRectSet2) {
		return calculateUnfitness(currentRectSet2, bg_color);
	}

	private static double calculateUnfitness(ArrayList<Rect> currentRectSet2, Color bg) {
		BufferedImage img = getImageOf(currentRectSet2, bg);
		double unfitness = 0;
		for (int i = 0; i < target.getWidth(); i++) {
			for (int j = 0; j < target.getHeight(); j++) {
				int rgb1 = img.getRGB(i, j);
				int rgb2 = target.getRGB(i, j);

				int dr = ((rgb1 >> 16) & 0xFF) - ((rgb2 >> 16) & 0xFF);
				int dg = ((rgb1 >> 8) & 0xFF) - ((rgb2 >> 8) & 0xFF);
				int db = (rgb1 & 0xFF) - (rgb2 & 0xFF);

				unfitness += Math.sqrt(dr * dr + dg * dg + db * db);
			}
		}
		return unfitness;
	}

	private static void refresh(BufferedImage img) {
		imgDisplay.setIcon(new ImageIcon(img));
	}

}

@SuppressWarnings("serial")
class Rect implements Serializable {
	int x;
	int y;
	int width;
	int height;
	Color col;

	public Rect(int x, int y, int width, int height, Color col) {
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
		this.col = col;
	}

	public static Rect randomRect() {
		Color col = new Color((int) (Math.random() * 0x1000000) | 0x80000000, true);
		int x = (int) (Math.random() * Monalisa.target.getWidth());
		int y = (int) (Math.random() * Monalisa.target.getHeight());
		int width = (int) (Math.random() * 10);
		int height = (int) (Math.random() * 10);
		return new Rect(x, y, width, height, col);
	}
}

@SuppressWarnings("serial")
class RectSet implements Serializable {
	Color bg_color;
	ArrayList<Rect> rectSet;

	public RectSet(ArrayList<Rect> rectSet, Color bg_color) {
		this.rectSet = rectSet;
		this.bg_color = bg_color;
	}
}