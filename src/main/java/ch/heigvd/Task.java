package ch.heigvd;

import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.List;

public class Task extends Thread {

    private Gestionnaire gestionnaire;
    private final double P = 0.3;
    private List<Site> sites;
    private DatagramSocket datagramSocket;


    public Task(Gestionnaire gestionnaire, List<Site> sites) {
        this.gestionnaire = gestionnaire;
        this.sites = sites;

        try {
            this.datagramSocket = new DatagramSocket();
        } catch (SocketException e) {
            e.printStackTrace();
        }
    }

    public void run() {

        try {
            sleep(5000);
        } catch (Exception e) {
            e.printStackTrace();
        }

        while (Math.random() < P) {
            try {
                sleep(5000);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        gestionnaire.endTask();
    }
}
