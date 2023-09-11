import java.net.*;
import java.util.Scanner;
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

	private static void createServer(String serverAddress, int serverPort) throws IOException {
		setServerPort();
		setServerAddress();
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
				createServer(serverAddress, serverPort);
			} catch (Exception e) {
				System.out.println("Une erreur inattendue est survenue lors de la création du serveur:\n" + e);
			}

		} catch (Exception e) {
			System.out.println("Une erreur inattendue est survenue:\n" + e);
		}

	}

}
