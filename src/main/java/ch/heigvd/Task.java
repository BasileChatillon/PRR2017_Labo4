package ch.heigvd;

import ch.heigvd.utils.Message;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

/**
 * Classe qui représente le traitement d'une tâche
 */
public class Task extends Thread {

    private final int timeToWaitMin = 10000; // Le temps qu'une tache va dormir au minimum
    private final int timeToWaitMax = 20000; // Le temps qu'une tache va dormir au maximum

    private final double P = 0.3; // La probabilité qu'un tache en crée une autre

    private Gestionnaire gestionnaire; // Le gestionnaire pour qui on execute la tache
    private int numberOfSite; // Le nombre de site présent dans l'anneau
    private int ourNumber; // Notre numéro de site
    private Random random;


    public Task(Gestionnaire gestionnaire, int numberOfSite, int ourNumber) {
        this.gestionnaire = gestionnaire;
        this.numberOfSite = numberOfSite;
        this.ourNumber = ourNumber;
        this.random = new Random();
    }

    public void run() {
        try {

            sleep(random.nextInt(timeToWaitMax - timeToWaitMin) + timeToWaitMin);
            
            int siteToCreateTask; // Variable qui va potentiellement recueuillir le site sur lequel on va créer une tache
            byte[] messageTask; // Le message qui sera potentiellement envoyé au site qui doit créer la tache

            while (Math.random() < P) {

                // Pour tirer un site aléatoire sauf nous, on tire un numéro entre 0 et n-2. Si le numéro est le notre
                // alors on dit que c'est le dernier site
                siteToCreateTask = random.nextInt(numberOfSite - 1);
                if(siteToCreateTask == ourNumber) {
                    siteToCreateTask = numberOfSite - 1;
                }

                // Création du message et envoie de celui-ci via notre gestionnaire (vu que le gestionnaire
                // connait le voisin, c'est plus simple)
                System.out.println("Task.run : Création d'une nouvelle tache sur le site n°" + siteToCreateTask);
                messageTask = Message.createTask(siteToCreateTask);
                gestionnaire.sendMessage(messageTask);


                sleep(random.nextInt(timeToWaitMax - timeToWaitMin) + timeToWaitMin);
            }
        } catch (Exception e) {
            System.err.println("Task.run : Erreur lors du sleep de la tache");
            e.printStackTrace();
        }

        // Une fois que la tâche est terminée on va notifier le gestionnaire pour qu'il decrémente une tache
        gestionnaire.endTask();
    }
}
