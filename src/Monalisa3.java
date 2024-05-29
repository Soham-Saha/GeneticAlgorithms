import java.awt.Color;
import java.awt.Graphics;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Scanner;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

public class Monalisa3 {
	public static BufferedImage target;
	public static JFrame frm;
	public static JLabel imgDisplay;
	public static JTextArea stat;
	public static boolean useMem = false;
	public static int[] mem = { -1, 0, 0 };
	public static int mutate = 0;
	public static int rand = 0;
	public static boolean pos = false;

	// HYPER-PARAMETERS
	public static int u = 4;
	public static Color init_bg_color = Color.black;
	public static int population_size = 500;
	public static double error_prob = 0.0001;

	public static void main(String[] args) throws IOException, ClassNotFoundException {
		target = ImageIO.read(new File("d:\\main_drive\\alldata\\Mine\\Others\\me.jpg"));
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
		Scanner scan = new Scanner(System.in);
		System.out.println("Read old?(Y/N):");
		ArrayList<Rect> rectSet;
		if (scan.nextLine().equalsIgnoreCase("y")) {
			ObjectInputStream in = new ObjectInputStream(new FileInputStream("datarect.txt"));
			rectSet = ((RectSet) in.readObject()).rectSet;
			in.close();
		} else {
			rectSet = new ArrayList<Rect>();
			for (int i = 0; i < population_size; i++) {
				rectSet.add(randomRect());
			}
		}
		scan.close();
		frm.setVisible(true);
		boolean updated = true;
		double unfitness = calculateUnfitness(rectSet);
		final double init_unfitness = unfitness;
		refresh(getImageOf(rectSet));
		int sgen = 0;
		int tgen = 0;
		boolean written = false;
		while (true) {
			adaptToUnfitness(unfitness);
			tgen++;
			if (updated) {
				sgen++;
				unfitness = calculateUnfitness(rectSet);
				stat.append("\nuf=" + (int) unfitness + " in sgen=" + sgen + " tgen=" + tgen + " uf(delta)="
						+ (int) ((init_unfitness - unfitness) / sgen) + "(/sgen), "
						+ (int) ((init_unfitness - unfitness) / tgen) + "(/tgen)");
				stat.setCaretPosition(stat.getDocument().getLength());
				updated = false;
			}
			ArrayList<Rect> rectSet2;
			rectSet2 = mutate(rectSet);
			double new_unfitness = calculateUnfitness(rectSet2);
			if (new_unfitness < unfitness || Math.random() < error_prob) {
				useMem = true;
				rectSet = rectSet2;
				updated = true;
				refresh(getImageOf(rectSet));
			} else {
				useMem = false;
			}
			// System.out.println(mem[0] + " " + mem[1] + " " + mem[2] + " " + pos);
			if (unfitness < 1430000 && !written) {
				written = true;
				write(rectSet);
			}
		}

	}

	public static Rect randomRect() {
		Color col = new Color((int) (Math.random() * 0x1000000) | 0x80000000, true);
		int x = (int) (Math.random() * Monalisa3.target.getWidth());
		int y = (int) (Math.random() * Monalisa3.target.getHeight());
		int width = (int) (Math.random() * 10);
		int height = (int) (Math.random() * 10);
		return new Rect(x, y, width, height, col);
	}

	private static void write(ArrayList<Rect> rectSet) throws FileNotFoundException, IOException {
		System.out.println("written");
		ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream("datarect.txt"));
		out.writeObject(new RectSet(rectSet, Color.black));
		out.close();
	}

	private static void adaptToUnfitness(double unfitness) {
		if (unfitness < 1470000) {
			u = 2;
		}
	}

	private static BufferedImage getImageOf(ArrayList<Rect> rectSet) {
		BufferedImage img = new BufferedImage(target.getWidth(), target.getHeight(), BufferedImage.TYPE_INT_ARGB);
		Graphics g = img.getGraphics();
		g.setColor(init_bg_color);
		g.fillRect(0, 0, img.getWidth(), img.getHeight());
		for (Rect x : rectSet) {
			g.setColor(x.col);
			g.fillRect(x.x, x.y, x.width, x.height);
		}
		return img;
	}

	public static ArrayList<Rect> mutate(ArrayList<Rect> rectSet) {
		ArrayList<Rect> rectSet2 = new ArrayList<>();
		mutate = useMem ? mem[2] : (rand == 6 ? mutate += 1 : mutate);
		mutate %= rectSet.size();
		int ct = 0;
		for (Rect x : rectSet) {
			if (ct == mutate) {
				int newX = x.x;
				int newY = x.y;
				int newWidth = x.width;
				int newHeight = x.height;
				int r = x.col.getRed();
				int g = x.col.getGreen();
				int b = x.col.getBlue();
				if (!useMem) {
					pos = !pos;
					if (pos) {
						rand += 1;
						rand %= 7;
					}
					mem[0] = rand;
					mem[2] = ct;
					switch (rand) {
					case 0:
						mem[1] = pos ? -u : u;
						newX += mem[1];
						break;
					case 1:
						mem[1] = pos ? -u : u;
						newY += mem[1];
						break;
					case 2:
						mem[1] = pos ? -u : u;
						newWidth += mem[1];
						break;
					case 3:
						mem[1] = pos ? -u : u;
						newHeight += mem[1];
						break;
					case 4:
						mem[1] = pos ? -u : u;
						r += mem[1];
						if (r < 0)
							r = 0;
						if (r > 255)
							r = 255;
						break;
					case 5:
						mem[1] = pos ? -u : u;
						g += mem[1];
						if (g < 0)
							g = 0;
						if (g > 255)
							g = 255;
						break;
					case 6:
						mem[1] = pos ? -u : u;
						b += mem[1];
						if (b < 0)
							b = 0;
						if (b > 255)
							b = 255;
						break;
					default:
						break;
					}
				} else {
					switch (mem[0]) {
					case 0:
						newX += mem[1];
						break;
					case 1:
						newY += mem[1];
						break;
					case 2:
						newWidth += mem[1];
						break;
					case 3:
						newHeight += mem[1];
						break;
					case 4:
						r += mem[1];
						if (r < 0)
							r = 0;
						if (r > 255)
							r = 255;
						break;
					case 5:
						g += mem[1];
						if (g < 0)
							g = 0;
						if (g > 255)
							g = 255;
						break;
					case 6:
						b += mem[1];
						if (b < 0)
							b = 0;
						if (b > 255)
							b = 255;
						break;
					default:
						break;
					}
				}
				Color newCol;
				newCol = new Color(r, g, b, x.col.getAlpha());
				rectSet2.add(new Rect(newX, newY, newWidth, newHeight, newCol));
			} else {
				rectSet2.add(x);
			}
			ct++;
		}
		return rectSet2;
	}

	private static double calculateUnfitness(ArrayList<Rect> rectSet) {
		BufferedImage img = getImageOf(rectSet);
		double unfitness = 0;
		for (int i = 0; i < target.getWidth(); i++) {
			for (int j = 0; j < target.getHeight(); j++) {
				Color c1 = new Color(img.getRGB(i, j));
				Color c2 = new Color(target.getRGB(i, j));
				unfitness += Math.sqrt(Math.pow(c1.getRed() - c2.getRed(), 2)
						+ Math.pow(c1.getGreen() - c2.getGreen(), 2) + Math.pow(c1.getBlue() - c2.getBlue(), 2));
			}
		}
		return unfitness;
	}

	private static void refresh(BufferedImage img) {
		imgDisplay.setIcon(new ImageIcon(img));
	}

}