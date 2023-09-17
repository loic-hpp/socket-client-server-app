import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
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
	private static Scanner scanner = new Scanner(System.in);
	private static Validator validator = new Validator(scanner);

	private void createImage(BufferedImage image, String imageName) throws Exception {
		File outputfile = new File(imageName + ".png");
		ImageIO.write(image, "png", outputfile);
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

			scanner.close();
			socket.close();
		} catch (Exception e) {
			System.out.println("Une erreur inattendue est survenue:\n" + e);
		}

	}

}
