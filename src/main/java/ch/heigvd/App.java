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
    private final String propertiesFileName = "site.properties";
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
        this.gestionnaire = new Gestionnaire(this, sites, siteNumber);
        this.gestionnaire.start();

        demarrer();
    }

    private void demarrer() {

        // Gestion de la GUI en ligne de commande
        Scanner scanner = new Scanner(System.in);
        while (true) {
            printInfo();
            String input = scanner.nextLine();
            input = input.toLowerCase();

            if (input.contains("tache"))
            {
                gestionnaire.createTask();
            }
            else if (input.contains("fin")) {
                System.out.println("Début de la fin...");
                gestionnaire.beginEnding();
            }
        }

    }

    /**
     * Méthode privée permettant d'afficher les commandes disponibles
     */
    private static void printInfo() {
        System.out.println();
        System.out.println("Entrez les différentes valeurs :");
        System.out.println("TACHE pour démarrer une nouvelle tache");
        System.out.println("FIN pour démarrer la terminaison");
        System.out.print("> ");
    }

    /**
     * Fonction qui permet de récupérer tous les sites contenu dans le ficher site.properties.
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

    public static void main(String[] args) {
        // Vérification que l'utilisateur a bien entré un paramètre
        if (args.length != 1) {
            System.err.println("main : Invalid argument, you need to pass a site number");
            System.exit(1);
        }
        int number = Integer.parseInt(args[0]);

        new App(number);
    }
}
