package ch.heigvd;

import ch.heigvd.utils.Message;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class Task extends Thread {

    private Gestionnaire gestionnaire;
    private final double P = 0.6;
    private List<Integer> sitesNumberWithoutUs;
    private Site neighbour;
    private Random random;
    private final int timeToWait = 3000;


    public Task(Gestionnaire gestionnaire, List<Site> sites, Site us, Site neighbour) {
        this.gestionnaire = gestionnaire;
        this.sitesNumberWithoutUs = sites.stream()
                .filter(site -> site.getNumber() != us.getNumber())
                .map(site -> site.getNumber())
                .collect(Collectors.toList());

        this.neighbour = neighbour;
        this.random = new Random();
    }

    public void run() {

        DatagramSocket socket;
        DatagramPacket packet;
        try {
            socket = new DatagramSocket();

            sleep(timeToWait);

            int toSend;
            byte[] message;

            while (Math.random() < P) {
                toSend = sitesNumberWithoutUs.get(random.nextInt(sitesNumberWithoutUs.size()));
                message = Message.createTask(toSend);
                System.out.println("Task.run : Création d'une nouvelle tache sur le site n°" + toSend);

                packet = new DatagramPacket(message, message.length, neighbour.getIp(), neighbour.getPort());
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
