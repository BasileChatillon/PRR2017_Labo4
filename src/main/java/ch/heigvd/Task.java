package ch.heigvd;

import ch.heigvd.utils.Message;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.List;
import java.util.Random;

public class Task extends Thread {

    private Gestionnaire gestionnaire;
    private final double P = 0.6;
    private List<Site> sitesWithoutUs;
    private Random random;
    private final int timeToWait = 3000;


    public Task(Gestionnaire gestionnaire, List<Site> sitesWithoutUs, Site us) {
        this.gestionnaire = gestionnaire;
        this.sitesWithoutUs = sitesWithoutUs;
        sitesWithoutUs.remove(us);
        this.random = new Random();
    }

    public void run() {

        DatagramSocket socket;
        DatagramPacket packet;
        try {
            socket = new DatagramSocket();

            sleep(timeToWait);

            Site toSend;
            byte[] message;

            while (Math.random() < P) {
                toSend = sitesWithoutUs.get(random.nextInt(sitesWithoutUs.size()));
                message = Message.createTache();
                System.out.println("Task.run : Création d'une nouvelle tache sur le site n°" + toSend.getNumber());
                packet = new DatagramPacket(message, message.length, toSend.getIp(), toSend.getPort());
                socket.send(packet);
                sleep(timeToWait);
            }
        } catch (SocketException e) {
            System.err.println("Task.run : Erreur lors de la création du socket d'envoie");
            e.printStackTrace();
        } catch (IOException e) {
            System.err.println("Task.run : Erreur lors de l'envoie du paquet");
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("Task.run : Erreur lors du sleep de la tache");
            e.printStackTrace();
        }

        gestionnaire.endTask();
    }
}
