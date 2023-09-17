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

	private static boolean isValidIpAddress(String ipAddress) {

		String[] addressArr = ipAddress.replace(".", ",").split(",");
		if (addressArr.length != 4)
			return false;
		else {
			for (String octet : addressArr) {
				try {
					int integerOctet = Integer.parseInt(octet);
					if (integerOctet < 0 || integerOctet > 255)
						return false;
				} catch (Exception e) {
					return false;
				}
			}
		}
		return true;
	}

	private static void setServerAddress() {
		boolean isValidAddress = false;
		String address;
		while (!isValidAddress) {
			System.out.println("Entrer l'adresse du server :\t");
			address = scanner.nextLine();
			if (isValidIpAddress(address)) {
				serverAddress = address;
				isValidAddress = true;
			} else
				System.out.println(
						"L'adresse saisie est invalide veuillez entrer une adresse au format X.X.X.X\nOù X est entre 0 et 255");

		}
	}

	private static void setServerPort() {
		boolean isInvalidPort = true;
		int port;
		while (isInvalidPort) {
			System.out.println("Entrer le port du server :\t");
			port = scanner.nextInt();
			if (port >= 5000 && port <= 5050) {
				serverPort = port;
				isInvalidPort = false;
			} else
				System.out.println("Le numéro de port entre est invalide veuillez entrer un numéro entre 5000 et 5050");
		}
	}

	private static void createServer() throws IOException {
		setServerAddress();
		setServerPort();
		listener = new ServerSocket();
		listener.setReuseAddress(true);
		boolean serverNotCreated = true;
		while (serverNotCreated) {
			try {
				listener.bind(new InetSocketAddress(serverAddress, serverPort));
				serverNotCreated = false;
			} catch (Exception e) {
				System.out.println("L'adresse ip fournie n'est pas celle de votre ordinateur");
				setServerAddress();
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

		public ClientHandler(Socket socket) {
			this.clientSocket = socket;
			System.out.println("New connection with client#" + "at" + clientSocket);
		}

		private BufferedImage sobelFilter(BufferedImage image) {
			int x = image.getWidth();
			int y = image.getHeight();
			int[][] edgeColors = new int[x][y];
			int maxGradient = -1;

			for (int i = 1; i < x - 1; i++) {
				for (int j = 1; j < y - 1; j++) {
					int val00 = getGrayScale(image.getRGB(i - 1, j - 1));
					int val01 = getGrayScale(image.getRGB(i - 1, j));
					int val02 = getGrayScale(image.getRGB(i - 1, j + 1));

					int val10 = getGrayScale(image.getRGB(i, j - 1));
					int val11 = getGrayScale(image.getRGB(i, j));
					int val12 = getGrayScale(image.getRGB(i, j + 1));

					int val20 = getGrayScale(image.getRGB(i + 1, j - 1));
					int val21 = getGrayScale(image.getRGB(i + 1, j));
					int val22 = getGrayScale(image.getRGB(i + 1, j + 1));

					int gx = ((-1 * val00) + (0 * val01) + (1 * val02))
							+ ((-2 * val10) + (0 * val11) + (2 * val12))
							+ ((-1 * val20) + (0 * val21) + (1 * val22));

					int gy = ((-1 * val00) + (-2 * val01) + (-1 * val02))
							+ ((0 * val10) + (0 * val11) + (0 * val12))
							+ ((1 * val20) + (2 * val21) + (1 * val22));

					double gval = Math.sqrt((gx * gx) + (gy * gy));
					int g = (int) gval;

					if (maxGradient < g) {
						maxGradient = g;
					}

					edgeColors[i][j] = g;
				}
			}

			double scale = 255.0 / maxGradient;

			for (int i = 1; i < x - 1; i++) {
				for (int j = 1; j < y - 1; j++) {
					int edgeColor = edgeColors[i][j];
					edgeColor = (int) (edgeColor * scale);
					edgeColor = 0xff000000 | (edgeColor << 16) | (edgeColor << 8) | edgeColor;

					image.setRGB(i, j, edgeColor);
				}
			}
			return image;
		}

		private static int getGrayScale(int rgb) {
			int r = (rgb >> 16) & 0xff;
			int g = (rgb >> 8) & 0xff;
			int b = (rgb) & 0xff;

			// from https://en.wikipedia.org/wiki/Grayscale, calculating luminance
			int gray = (int) (0.2126 * r + 0.7152 * g + 0.0722 * b);
			// int gray = (r + g + b) / 3;

			return gray;
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

		private void sendImage(BufferedImage image) throws Exception{
			DataOutputStream out = new DataOutputStream(this.clientSocket.getOutputStream());	
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ImageIO.write(image, "png", baos);
			byte[] imageBytes = baos.toByteArray();
			out.writeInt(imageBytes.length);
			out.write(imageBytes);
		}

		private void createImage(BufferedImage image) throws Exception{
			File outputfile = new File("sobel.png");
				ImageIO.write(image, "png", outputfile);
		}

		public void run() {
			try {
				try {
					sendImage(sobelFilter(getImage()));
				 } catch (Exception e) {
					System.out.println("Une erreur est survenue lors du traitement de l'image:\n" + e);
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
