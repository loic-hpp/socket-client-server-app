import java.io.File;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;

public class Client {

	private void createImage(BufferedImage image) throws Exception{
			File outputfile = new File("sobel.png");
				ImageIO.write(image, "png", outputfile);
		}

	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
