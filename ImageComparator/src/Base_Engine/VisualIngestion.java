package Base_Engine;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
/*
 * 
 * This class contains the following for use in DoppelBanger
 * -Convert Image to BufferedImage
 * -Convert BufferedImage to ArrayList of Pixels
 * -Given two ArrayLists of Pixel score there similarity, higher scores meaning less similar
 * -Given an image and a folder of images, use a threaded method to find the most similar image within the folder 
 * -ArrayList of pixels to image
 * -All supporting methods for these actions to take place
 * Carson Cummins 2017
 */
public class VisualIngestion {
	//Increase for more speed but more proccessor usage
	public static final int MAX_THREADS = 50;
	//Size to make images (decrease for more speed, increase for more accuracy)
	public static final int SQUARE_SIZE = 50;
	//how important that there is a similar number of pixels in the structure (Balance with color and shape for accuracy improvements)
	public static final double SIZE_IMPORTANCE = 1.0;
	//Importance of similar colors (Balance with shape and size for accuracy increases)
	public static final double COLOR_IMPORTANCE = 1.0;
	//Importance of similar black and white shading of nearby pixels (Balance with color and size for accuracy increase)
	public static final double SHAPE_IMPORTANCE = 1.0;
	//Increase for more speed but less accuracy, Minimum size of a structure
	public static final int MIN_STRUCTURE_SIZE = 100;
	public VisualIngestion() {

	}

	public BufferedImage toBufferedImage(Image img) {
		if (img instanceof BufferedImage) {
			return (BufferedImage) img;
		}

		// Create a buffered image with transparency
		BufferedImage bimage = new BufferedImage(img.getWidth(null), img.getHeight(null), BufferedImage.TYPE_INT_ARGB);

		// Draw the image on to the buffered image
		Graphics2D bGr = bimage.createGraphics();
		bGr.drawImage(img, 0, 0, null);
		bGr.dispose();

		// Return the buffered image
		return bimage;
	}
	public String mostSimilarFolder(BufferedImage in, String fName){
		File[] f = (new File(fName).listFiles());
		String ret = "";
		double lowestScore = Double.MAX_VALUE;
		for (int i = 0; i < f.length; i++) {
			double k = 0.0;
			if((k=avgSimilarityWithFolder(in,f[i].getPath()))<lowestScore){
				ret = f[i].getName();
				lowestScore = k;
			}
		}
		return ret;
	}
public double avgSimilarityWithFolder(BufferedImage imgg, String fName){
	File[] f = (new File(fName).listFiles());
	Image aa = imgg.getScaledInstance(SQUARE_SIZE, SQUARE_SIZE, Image.SCALE_SMOOTH);
	BufferedImage img = toBufferedImage(aa);
	ArrayList<RunEnviroment> enviroments = new ArrayList<RunEnviroment>();
	int left = f.length;
	for (int j = f.length; j > 0; j-=MAX_THREADS) {
		for (int i = 0; i < (MAX_THREADS<left?MAX_THREADS:left); i++) {
			left--;
		RunEnviroment ee = new RunEnviroment(f[j*MAX_THREADS+i], img);
		enviroments.add(ee);
		Thread tt = new Thread(ee);
		tt.start();
	}
	while (enviroments.get(enviroments.size() - 1).sim == -1.0) {
		try {
			Thread.currentThread().wait(2);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}
	double ret = 0.0;
	for (int i = 0; i < enviroments.size(); i++) {
	ret+=enviroments.get(i).sim;
	}
	return ret/enviroments.size();
}
	public String getPicFromFolder(BufferedImage imgg, String fName) {
		File[] f = (new File(fName).listFiles());
		Image aa = imgg.getScaledInstance(SQUARE_SIZE, SQUARE_SIZE, Image.SCALE_SMOOTH);
		BufferedImage img = toBufferedImage(aa);
		String bestFile = "";
		double leastScore = Double.MAX_VALUE;
		ArrayList<RunEnviroment> enviroments = new ArrayList<RunEnviroment>();
		int left = f.length;
		for (int j = f.length; j > 0; j-=MAX_THREADS) {
			for (int i = 0; i < (MAX_THREADS<left?MAX_THREADS:left); i++) {
				left--;
			RunEnviroment ee = new RunEnviroment(f[j*MAX_THREADS+i], img);
			enviroments.add(ee);
			Thread tt = new Thread(ee);
			tt.start();
		}
		while (enviroments.get(enviroments.size() - 1).sim == -1.0) {
			try {
				Thread.currentThread().wait(2);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
		for (int i = 0; i < enviroments.size(); i++) {
			if (enviroments.get(i).sim < leastScore) {
				leastScore = enviroments.get(i).sim;
				bestFile = enviroments.get(i).f.getName();
			}
		}
		return bestFile;
	}

	public double similarity(ArrayList<Pixel> s1, ArrayList<Pixel> s2) {
		ArrayList<ArrayList<Pixel>> Structures1 = struct(s1);
		ArrayList<ArrayList<Pixel>> Structures2 = struct(s2);
		double ret = 0.0;
		Structures1 = trimFat(Structures1);
		Structures2 = trimFat(Structures2);
		if (Structures1.size() < Structures2.size()) {
			for (int i = 0; i < Structures1.size(); i++) {
				ArrayList<Pixel> ii = match(Structures1.get(i), Structures2);
				ret += differences(ii, Structures1.get(i));
				Structures2.remove(Structures2.indexOf(ii));
			}
		} else {
			for (int i = 0; i < Structures2.size(); i++) {
				ArrayList<Pixel> ii = match(Structures2.get(i), Structures1);
				ret += differences(ii, Structures2.get(i));
				Structures1.remove(Structures1.indexOf(ii));
			}
		}
		return ret;
	}

	private ArrayList<ArrayList<Pixel>> trimFat(ArrayList<ArrayList<Pixel>> s) {
		ArrayList<ArrayList<Pixel>> ret = new ArrayList<ArrayList<Pixel>>();
		for (int i = 0; i < s.size(); i++) {
			if(s.get(i).size()>MIN_STRUCTURE_SIZE){
			ret.add(s.get(i));
		}
		}
		return ret;
	}

	public ArrayList<Pixel> match(ArrayList<Pixel> aa, ArrayList<ArrayList<Pixel>> ii) {
		ArrayList<Pixel> bestFit = ii.get(0);
		double bestScore = Double.MAX_VALUE;
		for (int i = 0; i < ii.size(); i++) {
			double b = 0.0;
			if ((b = differences(ii.get(i), aa)) < bestScore) {
				bestFit = ii.get(i);
				bestScore = b;
			}
		}
		return bestFit;
	}

	public ArrayList<ArrayList<Pixel>> struct(ArrayList<Pixel> s) {
		ArrayList<ArrayList<Pixel>> ret = new ArrayList<ArrayList<Pixel>>();
		for (int i = 0; i < s.size(); i++) {
			if (!s.get(i).inStruct) {
				ret.add(s.get(i).Build(s.get(i)));
			}
		}
		return ret;
	}

	private double differences(ArrayList<Pixel> P1, ArrayList<Pixel> P2) {
double SizeDif = SIZE_IMPORTANCE*Math.abs(P1.size()-P2.size());
double ColorDif = COLOR_IMPORTANCE*imageAvgColorDif(P1,P2);
double ShapeDif = SHAPE_IMPORTANCE*avgShapeDif(P1,P2);
		return SizeDif+ColorDif+ShapeDif;
	}

	private double avgShapeDif(ArrayList<Pixel> p, ArrayList<Pixel> k) {
		double ret = 0.0;
		BufferedImage compI = null;
		if(p.size()>k.size()){
			compI = ArrayToImage(p);
			Image a = compI.getScaledInstance(MaxCol(k)+1, MaxRow(k)+1, Image.SCALE_FAST);
			compI = toBufferedImage(a);
			ArrayList<Pixel> comp = PicToArray(compI);
			for (int i = 0; i < k.size(); i++) {
				ret+=shadeDif(k.get(i),closestPixel(k.get(i),comp));
			}
			return ret/k.size();
		}else{
			compI = ArrayToImage(k);
			Image a = compI.getScaledInstance(MaxCol(p)+1, MaxRow(p)+1, Image.SCALE_FAST);
			compI = toBufferedImage(a);
			ArrayList<Pixel> comp = PicToArray(compI);
			for (int i = 0; i < p.size(); i++) {
				
				ret+=shadeDif(p.get(i),closestPixel(p.get(i),comp));

				}
			return ret/k.size();
		}
	
	}

	private double shadeDif(Pixel p, Pixel k) {
		double  f = (p.red+p.blue+p.green)/3;
		double s = (p.red+p.blue+p.green)/3;
		return Math.abs(f-s);
	}

	private Pixel closestPixel(Pixel p, ArrayList<Pixel> comp) {
		double shortest = Double.MAX_VALUE;
		Pixel best = null;
		for (int i = 0; i < comp.size(); i++) {
			double d = 0.0;
			if((d = Math.sqrt(Math.pow((comp.get(i).row-p.row),2)+Math.pow((comp.get(i).column-p.column),2)))<shortest&&comp.get(i).alpha>0){
				shortest = d;
				best = comp.get(i);
			}
		}
		return best;
	}

	private BufferedImage ArrayToImage(ArrayList<Pixel> p) {
		BufferedImage ret = new BufferedImage(MaxCol(p)+1,MaxRow(p)+1,BufferedImage.TYPE_INT_ARGB);
		for (int i = 0; i < ret.getWidth(); i++) {
			for (int k = 0; k < ret.getHeight(); k++) {
				ret.setRGB(i, k, ColorToInt(0,0,0,0));
			}
		}
		for (int i = 0; i < p.size(); i++) {
			
			ret.setRGB(p.get(i).column, p.get(i).row, ColorToInt(p.get(i).alpha,p.get(i).red,p.get(i).green,p.get(i).blue));
			
		}
		return toBufferedImage(ret);
	}

	private int MaxRow(ArrayList<Pixel> p)
	{
		int ret = 0;
		for (int i = 0; i < p.size(); i++) {
			if(p.get(i).row>ret){
				ret = p.get(i).row;
			}
		}
		return ret;
	}

	private int MaxCol(ArrayList<Pixel> p) {
		int ret = 0;
		for (int i = 0; i < p.size(); i++) {
			if(p.get(i).column>ret){
				ret = p.get(i).column;
			}
		}
		return ret;
	}

	private double imageAvgColorDif(ArrayList<Pixel> p, ArrayList<Pixel> k) {
		double red1 = 0.0;
		double green1 = 0.0;
		double blue1 = 0.0;
		for (int i = 0; i < p.size(); i++) {
			red1+=p.get(i).red;
			green1+=p.get(i).green;
			blue1+=p.get(i).blue;
		}
		double red2 = 0.0;
		double green2 = 0.0;
		double blue2 = 0.0;
		for (int i = 0; i < k.size(); i++) {
			red2+=k.get(i).red;
			green2+=k.get(i).green;
			blue2+=k.get(i).blue;
		}
		double r = Math.abs(red2-red1);
		double g = Math.abs(green2-green1);
		double b = Math.abs(blue2-blue1);
		return (r+g+b)/3;
	}
	public int ColorToInt(int alpha, int red, int green, int blue){
		return alpha<<24+red<<16+green<<8+blue;
	}

	public ArrayList<Pixel> PicToArray(BufferedImage img) {
		ArrayList<Pixel> ret = new ArrayList<Pixel>();

		for (int i = 0; i < img.getWidth(); i++) {
			for (int j = 0; j < img.getHeight(); j++) {
				int clr = img.getRGB(i, j);
				int red = (clr & 0x00ff0000) >> 16;
				int green = (clr & 0x0000ff00) >> 8;
				int blue = clr & 0x000000ff;
				int alpha = (clr&0xff000000)>>24;
		Pixel p = new Pixel(i+1, j+1, red, green, blue);
		p.alpha = alpha;
		ret.add(p);
			}
		}
		ret = buildAdjacency(ret);
		return ret;
	}

	private ArrayList<Pixel> buildAdjacency(ArrayList<Pixel> p) {
	ArrayList<Pixel> ret =new ArrayList<Pixel>();
		for (int i = 0; i < p.size(); i++) {
		Pixel a = p.get(i);
		ArrayList<Pixel> adj = new ArrayList<Pixel>();
		for (int j = 0; j < p.size(); j++) {
			int r = p.get(j).row;
			int c = p.get(j).column;
			if(Math.abs(a.row-r)<2&&Math.abs(a.column-c)<2){
				adj.add(p.get(j));
			}
		}
		a.adjacent = adj;
		ret.add(a);
	}
		return ret;
	}

}
