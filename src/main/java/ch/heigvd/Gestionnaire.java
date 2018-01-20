package ch.heigvd;

import ch.heigvd.utils.Message;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.List;

public class Gestionnaire extends Thread {

    private App app;
    private List<Site> sites;
    private Site us;
    private Site neighbour;

    private DatagramSocket socket;
    private boolean mustTerminate;
    private boolean hasInitializedEnd;

    private Object object;
    private int numberTasksRunning;


    public Gestionnaire(App app, List<Site> sites, int siteNumber) {
        // Extraction d'infos depuis les propriétés
        this.app = app;
        this.sites = sites;
        this.us = sites.get(siteNumber);
        this.neighbour = sites.get((us.getNumber() + 1) % sites.size());
        this.mustTerminate = false;
        this.hasInitializedEnd = false;

        try {
            this.socket = new DatagramSocket(us.getPort());
        } catch (SocketException e) {
            System.err.println("App.App : Erreur lors de la création du socket");
            e.printStackTrace();
        }

        this.object = new Object();
        this.numberTasksRunning = 0;
    }

    @Override
    public void run() {
        int sizeMessageMax = 1;

        DatagramPacket packetReceived = new DatagramPacket(new byte[sizeMessageMax], sizeMessageMax);

        // Attente de réception d'un message
        try {
            socket.receive(packetReceived);

            // Récupération du message
            byte[] message = new byte[packetReceived.getLength()];
            System.arraycopy(packetReceived.getData(), packetReceived.getOffset(), message, 0, packetReceived.getLength());

            switch (Message.getTypeOfMessage(message)) {
                case TASK:
                    createTask();
                    break;

                case JETON:
                    if (hasInitializedEnd && numberTasksRunning == 0) {
                        byte[] newMessage = Message.createEnd();
                        sendMessage(newMessage);
                    } else {

                    }

                    break;

                case END:

                    break;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Permet d'envoyer un message à notre voisin
     *
     * @param message Le message à envoyer
     */
    private void sendMessage(byte[] message) {
        DatagramPacket packetQuittance = new DatagramPacket(message, message.length, neighbour.getIp(), neighbour.getPort());

        try {
            socket.send(packetQuittance);
        } catch (IOException e) {
            System.err.println("GestionnaireElection:: Echec d'envoi de la quittance");
            e.printStackTrace();
        }
    }


    public void createTask() {
        if (!mustTerminate) {
            new Task(this, sites).start();

            synchronized (object) {
                numberTasksRunning++;
            }
        }
    }


    public void beginEnding() {
        hasInitializedEnd = true;
        mustTerminate = true;

        byte[] message = Message.createJeton();
        sendMessage(message);
    }

    public void endTask() {
        synchronized (object) {
            numberTasksRunning--;
        }

        if (numberTasksRunning == 0) {

        }
    }
}
