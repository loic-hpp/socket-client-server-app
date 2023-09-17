import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.util.Scanner;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;

public class Client {
	private static Socket socket;
	private static String serverAddress;
	private static int serverPort;
	private static String username;
	private static String password;
	private static boolean isClientAuthenticated = false;
	Sobel sobel = new Sobel();
	private static Scanner scanner = new Scanner(System.in);
	private static Validator validator = new Validator(scanner);

	private static void createImage(BufferedImage image, String imageName, String format) throws Exception {
		File outputfile = new File(imageName + "_with_sobel." + format);
		ImageIO.write(image, format, outputfile);
	}

	private static BufferedImage readImageFile(String imageName) throws IOException {
		File f = new File("./" + imageName);
		BufferedImage image = ImageIO.read(f);
		return image;
	}

	public static void main(String[] args) {
		try {
			boolean isConnectedToServer = false;
			while (!isConnectedToServer) {
				serverAddress = validator.setServerAddress();
				serverPort = validator.setServerPort();
				try {
					socket = new Socket(serverAddress, serverPort);
					isConnectedToServer = true;
				} catch (Exception e) {
					System.out.println("Les informations du serveur sont incorrectes\n" + e);
				}
			}
			DataInputStream in = new DataInputStream(socket.getInputStream());
			DataOutputStream out = new DataOutputStream(socket.getOutputStream());
			while (!isClientAuthenticated) {
				System.out.println("Entrer votre nom de d'utilisateur :\t");
				username = scanner.nextLine();
				out.writeUTF(username);
				System.out.println("Entrer votre mot de passe :\t");
				password = scanner.nextLine();
				out.writeUTF(password);
				isClientAuthenticated = in.readBoolean();
			}

			if (isClientAuthenticated) {
				String imageName = "";
				Sobel sobel = new Sobel();
				String[] imageNameInfo;
				boolean imageNameInvalid = true;
				while (imageNameInvalid) {
					System.out.println("Entrer le nom de l'image");
					imageName = scanner.nextLine();
					try {
						BufferedImage imageBuffer = readImageFile(imageName);
						imageNameInfo = imageName.replace(".", ",").split(",");
						sobel.sendImage(imageBuffer, socket, imageNameInfo[1]);
					} catch (Exception e) {
						System.out.println(
								"La lecture de l'image a échoué veuillez vérifier le nom de l'image et réessayer\n"
										+ e);
					}
					imageNameInvalid = false;
				}
				imageNameInfo = imageName.replace(".", ",").split(",");
				System.out.println("Image envoyée pour traitement");
				out.writeUTF(imageNameInfo[1]);
				createImage(sobel.getImage(socket), imageNameInfo[0], imageNameInfo[1]);
				System.out.println(
						" L'image a été créé a l'emplacement " + System.getProperty("user.dir") + "\\" + imageName);
			}

			// get Byteimage et turn into file

			scanner.close();
			socket.close();
		} catch (Exception e) {
			System.out.println("Une erreur inattendue est survenue:\n" + e);
		}

	}

}
