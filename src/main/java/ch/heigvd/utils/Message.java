package ch.heigvd.utils;

/**
 * Classe qui regroupe et implémente les différentes méthodes utiles à la
 * création de message ou à la lecture des messages échangés dans la procédure.
 */
public class Message {
    /**
     * Permet de créer un message de Task
     *
     * @param siteNumber Le numéro du site sur lequel on veut créer une tâche
     * @return Un tableau de byte représentant le message
     */
    public static byte[] createTask(int siteNumber) {
        // On crée un buffer de la bonne taille (1 byte pour le type + 4byte pour le numero du site)
        byte[] message = new byte[5];

        // Ajout du type de message au début du buffer
        message[0] = TypeMessage.TASK.getValueMessage();

        // on ajoute le numéro du message à la fin
        for (int i = 3; i > 0; i--) {
            message[i + 1] = (byte) (siteNumber & 0xFF);
            siteNumber >>= 8;
        }

        return message;
    }

    /**
     * Permet de récupérer depuis un message de tâche le numéro du site sur lequel
     * la tâche doit être lancée
     *
     * @param message Le message dont on veut extraire les informations
     * @return Le numéro du site
     */
    public static int extractSiteNumberFromTaskMessage(byte[] message) {
        int siteNumber = 0;
        // Parcours d'un bout du message pour récupérer le numéro du site
        for (int i = 0; i < 4; i++) {
            siteNumber <<= 8;
            siteNumber |= (message[i + 1] & 0xFF);
        }

        return siteNumber;
    }

    /**
     * Permet de créer un message de jeton
     *
     * @return Un tableau de byte représentant le message
     */
    public static byte[] createJeton() {
        // On crée un buffer de la bonne taille (1 byte pour le type + 4byte pour le numero ud site)
        byte[] message = new byte[1];

        // Ajout du type de message au début du buffer
        message[0] = TypeMessage.JETON.getValueMessage();

        return message;
    }

    /**
     * Permet de créer un message de fin
     *
     * @return Un tableau de byte représentant le message
     */
    public static byte[] createEnd() {
        // On crée un buffer de la bonne taille
        byte[] message = new byte[1];

        // Ajout du type de message au début du buffer
        message[0] = TypeMessage.END.getValueMessage();

        return message;
    }

    /**
     * Récupère le type du message
     *
     * @param message Le message dont on veut récupérer le type
     * @return Le type du message
     */
    public static TypeMessage getTypeOfMessage(byte[] message) {
        return TypeMessage.values()[message[0]];
    }
}
