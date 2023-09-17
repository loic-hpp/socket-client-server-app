import java.util.Scanner;

public class Validator {
    private Scanner scanner;

    public Validator(Scanner scannerParam) {
        this.scanner = scannerParam;
    }

    public int setServerPort() {
        boolean isInvalidPort = true;
        int port = 0;
        while (isInvalidPort) {
            System.out.println("Entrer le port du server :\t");
            port = scanner.nextInt();
            if (port >= 5000 && port <= 5050) {
                isInvalidPort = false;
            } else
                System.out.println("Le numéro de port entre est invalide veuillez entrer un numéro entre 5000 et 5050");
        }
        return port;
    }

    private boolean isValidIpAddress(String ipAddress) {

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

    public String setServerAddress() {
        boolean isValidAddress = false;
        String address = "";
        while (!isValidAddress) {
            System.out.println("Entrer l'adresse du server :\t");
            address = scanner.nextLine();
            if (isValidIpAddress(address)) {
                isValidAddress = true;
            } else
                System.out.println(
                        "L'adresse saisie est invalide veuillez entrer une adresse au format X.X.X.X\nOù X est entre 0 et 255");

        }
        return address;
    }

}
