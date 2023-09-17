import java.net.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Scanner;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;

import java.io.*;

public class Server {
	private static ServerSocket listener;
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
				System.out.println(
						"L'adresse ip fournie n'est pas celle de votre ordinateur ou le port est en cours d'utilisation\n"
								+ e);
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
			System.out.println("Nouvelle connexion" + " sur " + clientSocket);
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
						// get le format de l'image ici

						BufferedImage imageBuffer = sobel.getImage(clientSocket);
						LocalDateTime currentDateTime = LocalDateTime.now();
						DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd@HH:mm:ss");
						String formattedDateTime = currentDateTime.format(formatter);
						System.out.println("[ " + username + " - " + clientSocket.getInetAddress().getHostAddress()
								+ ":" + clientSocket.getPort() + " - " + formattedDateTime + " ]");
						String imageFormat = in.readUTF();
						sobel.sendImage(sobel.filterImage(imageBuffer), clientSocket, imageFormat);
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
				System.out.println("Connexion sur " + clientSocket + " fermée");
			}

		}
	}

}
