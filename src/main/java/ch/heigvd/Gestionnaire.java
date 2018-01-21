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
            while (true) {
                socket.receive(packetReceived);

                // Récupération du message
                byte[] message = new byte[packetReceived.getLength()];
                System.arraycopy(packetReceived.getData(), packetReceived.getOffset(), message, 0, packetReceived.getLength());

                switch (Message.getTypeOfMessage(message)) {
                    case TASK:
                        System.out.println("Gestionnaire.run : Création d'une nouvelle tache.");
                        createTask();
                        break;

                    case JETON:
                        mustTerminate = true;
                        System.out.println("Gestionnaire.run : Récéption d'un jeton");
                        byte[] newMessage;

                        synchronized (object) {
                            if (numberTasksRunning != 0) {
                                try {
                                    System.out.println("Gestionnaire.run : Attente de la terminaison des tâches");
                                    object.wait();
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }
                        }

                        if (hasInitializedEnd) {
                            newMessage = Message.createEnd();
                        } else {
                            newMessage = message;
                        }

                        sendMessage(newMessage);
                        break;

                    case END:
                        System.out.println("Gestionnaire.run : Récéption d'un la fin");
                        if (hasInitializedEnd) {
                            System.out.println("Gestionnaire.run : Tout est terminé!");
                        } else {
                            System.out.println("Gestionnaire.run : On a terminé!");
                            sendMessage(message);
                        }

                        break;
                }
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
            new Task(this, sites, us).start();

            synchronized (object) {
                numberTasksRunning++;
                System.out.println("Gestionnaire.createTask : nouvelle tache (en cours : " + numberTasksRunning + ")");
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
            System.out.println("Gestionnaire.endTask : Fin de la tache (restabte : " + numberTasksRunning + ")");
            if (numberTasksRunning == 0) {
                object.notifyAll();
            }
        }
    }
}
