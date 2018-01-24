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
 * Hello world!
 */
public class App {
    private final String propertiesFileName = "site.properties"; // Le ficher ou se trouves les différentes propriétés
    private Gestionnaire gestionnaire; // Le gestionnaire du site qui s'occupe de l'échange des messages et de la création de tache

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
     * Méthode qui lance la routine permettant de récupérer l'entrée de l'utilisateur et faire le travail associé.
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
     * Cela permet ensuite de passer cette instance à différentes méthode pour y récupérer différents élements
     * <p>
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
