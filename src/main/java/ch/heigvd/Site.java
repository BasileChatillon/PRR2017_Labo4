package ch.heigvd;

import java.net.InetAddress;

/**
 * La classe Site représente toutes les informations utiles pour pouvoir localiser un site. (en tout cas son IP
 * et son numéro de port).
 * Elle contient également un numéro qui permet de l'identifier.
 */
public class Site {
    private int number; // Le numéro du site
    private InetAddress ip; // L'adresse IP du site
    private int port; // Le port du site

    // Constructeur
    public Site(int number, InetAddress ip, int port) {
        this.number = number;
        this.ip = ip;
        this.port = port;
    }

    /************ Getter ************/
    public int getNumber() {
        return number;
    }

    public InetAddress getIp() {
        return ip;
    }

    public int getPort() {
        return port;
    }

    @Override
    public String toString() {
        return "Site n°" + number + " : " + ip.toString() + " - port n°" + port;
    }
}
