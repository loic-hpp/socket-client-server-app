import java.net.*;
import java.util.Scanner;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;

import java.io.*;

public class Server {
	private static ServerSocket listener;
	private static int clientNumber = 0;
	private static String serverAddress;
	private static int serverPort;
	private static Scanner scanner = new Scanner(System.in);
	private static Validator validator = new Validator(scanner);
	private static Authenticator authenticator = new Authenticator();

	private static void createServer() throws IOException {
		serverAddress = validator.setServerAddress();
		serverPort = validator.setServerPort();
		listener = new ServerSocket();
		listener.setReuseAddress(true);
		boolean serverNotCreated = true;
		while (serverNotCreated) {
			try {
				listener.bind(new InetSocketAddress(serverAddress, serverPort));
				serverNotCreated = false;
			} catch (Exception e) {
				System.out.println("L'adresse ip fournie n'est pas celle de votre ordinateur ou le port est en cours d'utilisation\n" + e);
				serverAddress = validator.setServerAddress();
				serverPort = validator.setServerPort();
			}
		}

		System.out.format("The server is running %s:%d %n", serverAddress, serverPort);
		scanner.close();
	}

	public static void main(String[] args) {
		try {
			try {
				createServer();
			} catch (Exception e) {
				System.out.println("Une erreur inattendue est survenue lors de la création du serveur:\n" + e);
			}

			try {
				while (true) {
					new ClientHandler(listener.accept()).start();
				}

			} catch (Exception e) {
				System.out.println("Une erreur inattendue est survenue:\n" + e);
			} finally {
				listener.close();
			}

		} catch (Exception e) {
			System.out.println("Une erreur inattendue est survenue:\n" + e);
		}

	}

	private static class ClientHandler extends Thread {
		private Socket clientSocket;
		private boolean isAuthenticated;
		private String username;
		private String password;

		public ClientHandler(Socket socket) {
			this.clientSocket = socket;
			isAuthenticated = false;
			System.out.println("New connection with client#" + "at" + clientSocket);
		}

		private BufferedImage getImage() throws Exception {
			DataInputStream in = new DataInputStream(this.clientSocket.getInputStream());
			// Read the length of the byte array
			int length = in.readInt();
			byte[] imageBytes = new byte[length];
			in.readFully(imageBytes);

			ByteArrayInputStream bais = new ByteArrayInputStream(imageBytes);
			BufferedImage receivedImage = ImageIO.read(bais);
			bais.close();
			return receivedImage;
		}

		private void sendImage(BufferedImage image) throws Exception {
			DataOutputStream out = new DataOutputStream(clientSocket.getOutputStream());
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ImageIO.write(image, "png", baos);
			byte[] imageBytes = baos.toByteArray();
			out.writeInt(imageBytes.length);
			out.write(imageBytes);
		}

		public void run() {
			try {
				DataInputStream in = new DataInputStream(clientSocket.getInputStream());
				DataOutputStream out = new DataOutputStream(clientSocket.getOutputStream());
				while (!isAuthenticated) {
					username = in.readUTF();
					password = in.readUTF();
					System.out.println("username");
					isAuthenticated = authenticator.authenticate(username, password);
					out.writeBoolean(isAuthenticated);
				}
				if (isAuthenticated) {
					try {
						Sobel sobel = new Sobel();
						sendImage(sobel.filterImage(getImage()));
					} catch (Exception e) {
						System.out.println("Une erreur est survenue lors du traitement de l'image:\n" + e);
					}
				}

			} catch (Exception e) {
				System.out.println("Error handling client#" + clientSocket + e);
			} finally {
				try {
					// Fermeture de la connexion avec le client
					clientSocket.close();
				} catch (IOException e) {
					System.out.println("Could not close a socket");
				}
				System.out.println("Connection with client#" + clientSocket + "closed");
			}

		}
	}

}
