package Base_Engine;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

public class RunEnviroment implements Runnable {
double sim = -1.0;
File f;
int i;
BufferedImage img;
	public RunEnviroment(File ff, BufferedImage ii) {
		f=ff;
		img=ii;
	}

	@Override
	public void run() {
		BufferedImage img2 = null;
		VisualIngestion IE = new VisualIngestion();
		try {
			img2 = ImageIO.read(f);
			Image a = img2.getScaledInstance(VisualIngestion.SQUARE_SIZE, VisualIngestion.SQUARE_SIZE, Image.SCALE_SMOOTH);
			img2 = IE.toBufferedImage(a);
			sim = IE.similarity(IE.PicToArray(img),IE.PicToArray(img2));
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}

}
