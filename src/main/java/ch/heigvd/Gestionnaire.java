package ch.heigvd;

import ch.heigvd.utils.Message;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.List;

public class Gestionnaire extends Thread {

    private App app; // Lien vers l'applicatif
    private int numberOfSite; // La liste de tous les autres sites
    private Site us; // Notre site
    private Site neighbour; // Le site voisin

    private DatagramSocket socket; // Le socket que l'on va utiliser pour recevoir et envoyer des messages
    private boolean mustTerminate; // Définit si le site à reçe le jeton
    private boolean hasInitializedEnd; // Définit si le site à lancer la terminaison des sites
    private boolean isRunning; // Détérmine si le thread est actif (utile pour finir le thread)
    private boolean actif; // Détermine si le gestionnaire est toujour actif vis-à-vis de la procédure de terminaison

    private Object object; // Object pour faire de l'exculsion mutuelle
    private int numberTasksRunning; // Le nombre de tache en cours sur le site


    public Gestionnaire(App app, List<Site> sites, int ourNumber) {
        this.app = app;
        this.numberOfSite = sites.size();
        this.us = sites.get(ourNumber);
        this.neighbour = sites.get((us.getNumber() + 1) % numberOfSite);
        this.mustTerminate = false;
        this.hasInitializedEnd = false;
        this.actif = false;

        // Ouverture du socket
        try {
            this.socket = new DatagramSocket(us.getPort());
        } catch (SocketException e) {
            System.err.println("App.App : Erreur lors de la création du socket");
            e.printStackTrace();
        }
        this.isRunning = true;

        this.object = new Object();
        this.numberTasksRunning = 0;
    }

    @Override
    public void run() {
        // Représente la taille maximale d'un message pouvant être reçu
        int sizeMessageMax = 5;

        DatagramPacket packetReceived = new DatagramPacket(new byte[sizeMessageMax], sizeMessageMax);

        // Attente de réception d'un message
        try {
            while (isRunning) {
                socket.receive(packetReceived);

                // Récupération du message
                byte[] message = new byte[packetReceived.getLength()];
                System.arraycopy(packetReceived.getData(), packetReceived.getOffset(), message, 0, packetReceived.getLength());

                // Traitement du message selon la spéc fournie
                switch (Message.getTypeOfMessage(message)) {
                    case TASK:
                        // Extraction du numéro de site contenu dans le message
                        int siteNumber = Message.extractSiteNumberFromTaskMessage(message);
                        actif = true;

                        // Si on est le site, alors on doit créer une tache, sinon on forward le message à notre voisin
                        if (siteNumber == us.getNumber()) {
                            _createTask();
                        } else {
                            sendMessage(message);
                        }
                        break;

                    case JETON:
                        // Une fois le Jeton reçu, on met que le site doit terminer pour empéccher l'utilisateur du site de créer d'autres taches
                        mustTerminate = true;
                        System.out.println("Gestionnaire.run : Récéption d'un jeton");
                        byte[] newMessage;

                        // Si on est le site qui a initialisé la terminaison et qu'on est inactif alors on débute la fin
                        if (hasInitializedEnd && !actif) {
                            // On crée le message de fin
                            newMessage = Message.createEnd();
                        } else {
                            // Si commence par attendre que toutes les taches soient terminée.
                            waitForTaskToEnd();

                            newMessage = message;
                        }

                        // Envoie du message de fin ou forward du jeton
                        sendMessage(newMessage);
                        break;

                    case END:
                        System.out.println("Gestionnaire.run : Récéption d'un la fin");
                        // Dans le cas ou le message de fin à fait le tour alors, on ne le renvoie pas
                        if (hasInitializedEnd) {
                            System.out.println("Gestionnaire.run : Tout est terminé!");
                        } else {
                            System.out.println("Gestionnaire.run : On a terminé!");
                            sendMessage(message);
                        }

                        // On termine le thread et on demande au site de se terminer
                        isRunning = false;
                        app.terminate();

                        break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Permet d'envoyer un message à notre voisin.
     *
     * @param message Le message à envoyer
     */
    void sendMessage(byte[] message) {
        DatagramPacket packetQuittance = new DatagramPacket(message, message.length, neighbour.getIp(), neighbour.getPort());

        try {
            socket.send(packetQuittance);
        } catch (IOException e) {
            System.err.println("GestionnaireElection:: Echec d'envoi de la quittance");
            e.printStackTrace();
        }
    }

    /**
     * Permet au site de créer une tache tant que le jeton n'a pas encore été reçu.
     *
     * @return True si on a pu lancer une tache
     */
    public boolean createTask() {
        if (!mustTerminate) {
            _createTask();
            actif = true;
        }

        return !mustTerminate;
    }

    /**
     * Méthode privée qui va permettre la création d'une tache. On incrémente également le nombre de tache en cours.
     */
    private void _createTask() {

        new Task(this, numberOfSite, us.getNumber()).start();
        synchronized (object) {
            numberTasksRunning += 1;
            System.out.println("Gestionnaire.createTask : nouvelle tache (en cours : " + numberTasksRunning + ")");
        }
    }

    /**
     * Méthode qui va permettre de commencer la fin! Soit d'envoyer le jeton ainsi que de changer les bons params.
     */
    public void beginEnding() {
        hasInitializedEnd = true;
        mustTerminate = true;

        waitForTaskToEnd();

        byte[] message = Message.createJeton();
        sendMessage(message);
    }

    /**
     * Méthode qui permet de faire attendre jusqu'à la fin de toutes taches
     */
    private void waitForTaskToEnd(){

        synchronized (object) {
            if (numberTasksRunning != 0) {
                try {
                    System.out.println("Gestionnaire.run : Attente de la terminaison des tâches");
                    object.wait();
                    System.out.println("Gestionnaire.run : Fin de l'attente");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        actif = false;
    }

    /**
     * Méthode qu'une tache va appeler une fois que celle-ci est terminée.
     * S'il n'y a plus de tâche en cours, alors on va tenter de notifier le gestionnaire qui attend potentiellement que
     * le site ne devienne inactif.
     */
    public void endTask() {
        synchronized (object) {
            numberTasksRunning -= 1;
            System.out.println("Gestionnaire.endTask : Fin de la tache (restante : " + numberTasksRunning + ")");
            if (numberTasksRunning == 0) {
                object.notifyAll();
            }
        }
    }
}
