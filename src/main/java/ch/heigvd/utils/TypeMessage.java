package ch.heigvd.utils;

/**
 * Un type énuméré qui représente les différents types de message utilisés dans notre appliaction
 * Chaque message est représenté par un byte.
 */
public enum TypeMessage {
    TASK((byte) 0), // le message de demande de relancer une tâche
    JETON((byte) 1), // le message pour demander la fin
    END((byte) 2); // Message d'annonce pour le deuxième tour

    // La valeur du message
    private byte valueMessage;

    TypeMessage(byte valueMessage) {
        this.valueMessage = valueMessage;
    }

    public byte getValueMessage() {
        return valueMessage;
    }
}
