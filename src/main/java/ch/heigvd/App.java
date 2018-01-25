package ch.heigvd;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Scanner;

/**
 * MARCHE A SUIVRE :
 * Pour lancer l'application, il faut lancer 4 fois le programme en lui passant en
 * paramètre le numéro du site (de 0 à 3)
 *
 *
 * CONCEPTION :
 * Pour faire ce programme, nous sommes partis du principe que chaque site connait son
 * voisin directement. Pour cela, il va parcourir le fichier de propriétés pour y récupérer
 * toutes les adresses de tous les sites et d'y extraire son voisin directe.
 *
 * Nous avons créé trois types de messages:
 * - Le message JETON : Il permet d'annoncer que le site doit commencer à terminer.
 *   Il est composé d'un unique byte qui représente le type du message.
 *
 * - Le message FIN : permet d'annoncer que tous les sites ont terminé leur traitement.
 *   Il est composé d'un unique byte qui représente le type du message
 *
 * - Le message TACHE : permet de créer sur un site une tâche. Vu que les sites ne
 *   peuvent communiquer qu'à leur voisin, il faut qu'un site puisse savoir s'il doit lancer
 *   une tache. C'est pour cela que le message est composé de 5 bytes : 1 pour le type du message
 *   et 4 byte qui représente un int qui est le site sur lequel doit être lancé la tache.
 *   Lorsqu'un site reçoit un message de tache, il extrait le numéro, si c'est le sien il
 *   crée une tache, sinon il forward le message à son voisin.
 *
 * Pour remplir les conditions de ce laboratoire nous avons 2 threads:
 * Le premier est le "main" qui va s'occuper de faire la gestion des entrées de l'utilisateur
 * Le deuxième est le "gestionnaire" qui lui va s'occuper de lire les messages et les
 * traiter en fonction du type de message.
 * Le gestionnaire est également capable de créer des threads "tâches".
 *
 * Pour que le gestionnaire connaisse le nombre de tâche en cours, nous avons décidé
 * d'utiliser une variable qui ressence le nombre de tache en cours. A chaque fois que
 * le gestionnaire crée une tâche, il va incrémenter ce compteur. Lorsqu'une tâche se
 * finit, elle va notifier le gestionnaire et il décrémentera le compteur.
 * Compte tenu que ce compteur est donc une variable partagée, nous utilisons un objet
 * pour garantir l'exclusion mutuelle au travers du synchronized.
 *
 * Une des spécialités est que le gestionnaire recoit un message JETON et qu'il est actif
 * alors il va attendre que toutes les tâches soient terminées. Pour cela, on fait un wait
 * sur l'objet pour ne pas faire de l'attente active. Lorsqu'une tâche se termine et que
 * c'est la dernière en cours, alors nous notifions l'objet.
 *
 * Bien que nous possédions une variable qui compte le nombre de tâche en cours, nous sommes
 * obligés d'avoir un booléan "actif" qui détermine si le site est actif ou non. En effet,
 * si un site inactif reçoit un message TACHE et que ce n'est pas lui qui doit créer la TACHE,
 * alors il faut quand même qu'il redevienne actif pour satisfaire la spéc. Le compteur ne
 * suffit donc pas.
 *
 * Dès que le gestionnaire reçoit un JETON, il empêchera l'utilisateur de créer de
 * nouvelle tâche.
 *
 * Une fois que le gestionnaire reçoit le message de fin, il va terminer le thread de son
 * site et il va sortir de sa boucle. Cela permet de bien terminer l'application.
 * Pour terminer le site, nous faisons un System.exit(0). Nous aurions pu faire en sorte
 * de sortir de la boucle while qui demande à l'utilisateur de finir, mais pour cela,
 * il devait encore entrer une mot pour passer l'attente du scanner.nextLine(), ce que
 * nous ne trouvions pas ergonomique.
 *
 *
 * TESTS :
 * Pour tester l'application, nous avons premièrement vérifier que les tâches se créent
 * bien sur le site ainsi que les tâches se créent bien sur les autres sites.
 *
 * Nous avons ensuite testé la terminaison sans avoir créé de tache au préalable.
 *
 * Nous avons également lancer des tâches avec un grand temps de calcul (elles dorment longtemps)
 * sur un site, puis nous avons lancé la terminaison sur ce même site. On a pu constater
 * qu'il attend bien avant d'envoyer le JETON. Nous avons également constaté que
 * l'utilisateur ne peut plus créer de tâche sur le site, ce qui est correcte. Nous avons
 * également pu constater que le site suivant attend également de finir ses tâches (s'il
 * en a) avant d'envoyer le JETON.
 *
 * Le dernier test que nous avons effectué est les tours mutltiples du JETON. Pour se faire
 * nous avons lanceé une tâche sur le site 0, et tout de suite après, lancé la terminaison
 * depuis celui-ci. Nous avons fait en sorte de pouvoir quand même lancer des taches en tant
 * qu'utilisateur, cela nous a permis de créer une tache après que celui-ci aie envoyé le JETON
 * Une fois qu'il reçoit le JETON, il le ré-envoie car il n'était plus inactif
 */
public class App {
    /**
     * Le ficher où se trouve les différentes propriétés
     */
    private final String propertiesFileName = "site.properties";
    /**
     * Le gestionnaire du site qui s'occupe de l'échange des messages et de la création de tache
     */
    private Gestionnaire gestionnaire;

    public App(int siteNumber) {
        // Récupération des propriétés dans le but d'y extraire des informations
        Properties properties = new Properties();
        try {
            properties = getSiteProperties();
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }

        // Extraction d'infos depuis les propriétés
        List<Site> sites = getAllSite(properties);

        System.out.println("App.App : Awfichage des sites");
        for (Site site : sites) {
            System.out.println(site);
        }
        // Création et lancement du gestionnaire
        this.gestionnaire = new Gestionnaire(this, sites, siteNumber);
        this.gestionnaire.start();

        demarrer();
    }

    /**
     * Méthode qui lance la routine permettant de récupérer l'entrée de l'utilisateur et faire
     * le travail associé.
     */
    private void demarrer() {

        // Gestion de la GUI en ligne de commande
        Scanner scanner = new Scanner(System.in);
        printInfo();
        while (true) {
            // Récupération de l'entrée
            String input = scanner.nextLine();
            input = input.toLowerCase();

            if (input.contains("tache")) {
                if (gestionnaire.createTask()) {
                    System.out.println("Création d'une tache...");
                } else {
                    System.out.println("Création d'une tache impossible pendant la terminaison");
                }
            } else if (input.contains("fin")) {
                System.out.println("Début de la fin...");
                gestionnaire.beginEnding();
            } else if (input.contains("info")) {
                printInfo();
            }
        }
    }

    /**
     * Méthode privée permettant d'afficher les commandes disponibles
     */
    private static void printInfo() {
        System.out.println();
        System.out.println("Entrez les différentes valeurs :");
        System.out.println("INFO pour afficher les différentes commandes.");
        System.out.println("TACHE pour démarrer une nouvelle tache.");
        System.out.println("FIN pour démarrer la terminaison.");
        System.out.print("> ");
    }

    /**
     * Fonction qui permet de récupérer tous les sites contenus dans le ficher site.properties.
     *
     * @param properties L'instance de properties dans laquelle sont stockées les différentes propriétés à récupérer
     * @return La liste des sites.
     */
    private List<Site> getAllSite(Properties properties) {
        List<Site> sites = new ArrayList<Site>();

        // On récupère le nombre de site total
        String number_site = properties.getProperty("totalSiteNumber");
        System.out.println(number_site);

        String siteAddress;
        InetAddress siteIP;
        int sitePort;

        try {
            // On parcourt ensuite tous les sites dans le fichier de propriétés et on récupère leurs informations
            for (int i = 0; i < Integer.parseInt(number_site); ++i) {
                siteAddress = properties.getProperty(String.valueOf(i));
                String[] values = siteAddress.split(":");
                siteIP = InetAddress.getByName(values[0]);
                sitePort = Integer.parseInt(values[1]);
                sites.add(new Site(i, siteIP, sitePort));
            }
        } catch (Exception e) {
            System.err.println("App.getAllSite : Erreur lors de la récupération des propriétés.");
        }

        return sites;
    }

    /**
     * Permet de récupérer une instance de la class Properties du fichier de propriétés sites.properties.
     * Cela permet ensuite de passer cette instance à différentes méthode pour y récupérer différents
     * élements.
     *
     * Lance une exception si le ficher .properties n'est pas trouvé
     *
     * @return un instance de Properties
     * @throws IOException
     */
    private Properties getSiteProperties() throws IOException {
        Properties properties = new Properties();
        // On récupère un stream
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream(propertiesFileName);

        // Si on a bien récupéré le stream, on tente de charger l'instance Properties
        if (inputStream != null) {
            properties.load(inputStream);
        } else {
            throw new FileNotFoundException("property file '" + propertiesFileName + "' not found in the classpath");
        }

        return properties;
    }

    /**
     * Permet de terminer l'application
     */
    public void terminate() {
        System.out.println("Application terminée");
        System.exit(0);
    }

    public static void main(String[] args) {
        // Vérification que l'utilisateur a bien entré un paramètre
        if (args.length != 1) {
            System.err.println("main : Invalid argument, you need to pass a site number");
            System.exit(1);
        }

        // Extraction du paramètre et lancement de l'application
        int number = Integer.parseInt(args[0]);
        new App(number);
    }
}
