package ch.heigvd.utils;

/**
 * Classe qui regroupe et implémente les différentes méthodes utiles à la création de message ou à la lecture des
 * messages échangés dans la procédure.
 */
public class Message {
    /**
     * Permet de créer un message de Task
     *
     * @return Un tableau de byte représentant le message
     */
    public static byte[] createTache() {
        // On crée un buffer de la bonne taille
        byte[] message = new byte[1];

        // Ajout du type de message au début du buffer
        message[0] = TypeMessage.TASK.getValueMessage();

        return message;
    }

    /**
     * Permet de créer un message de jeton
     *
     * @return Un tableau de byte représentant le message
     */
    public static byte[] createJeton() {
        // On crée un buffer de la bonne taille
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
