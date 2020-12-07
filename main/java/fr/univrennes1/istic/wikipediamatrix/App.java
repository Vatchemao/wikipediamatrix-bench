package fr.univrennes1.istic.wikipediamatrix;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import scala.Array;

public class App {

	private static String BASE_WIKIPEDIA_URL = "https://en.wikipedia.org/wiki/";
	private static String outputDirHtml = "output" + File.separator + "html" + File.separator;
	private static String outputDirWikitext = "output" + File.separator + "wikitext" + File.separator;
	private static File file = new File("inputdata" + File.separator + "wikiurls.txt");
	private static String url;
	private static int nurl = 0;
	private static int nbEchecs = 0;
	private static RetourExtraction retour = null;

	private static final Logger logger = LogManager.getLogger(App.class);

    public static void main( String[] args ) throws IOException {
		logger.debug("Début du main");
		List<String> listeEchecs = new ArrayList<String>();
        WikipediaHTMLExtractor extracteur = new WikipediaHTMLExtractor(BASE_WIKIPEDIA_URL, outputDirHtml, outputDirWikitext, file);
		BufferedReader br = new BufferedReader(new FileReader(file));
//		while ((url = br.readLine()) != null) {
//	    	logger.debug("On extrait les tableaux de l'url n° " + String.valueOf(nurl));
//	    	retour = extracteur.extraire(url);
//	    	if (retour.isEnEchec()) {
//	    		nbEchecs += 1;
//	    		listeEchecs.add(url);
//	    	}
//	    	nurl ++;
//	    }
//		logger.warn("Il y a " + nbEchecs + " échecs d'intégration. Les voici :");
//		for (String echec : listeEchecs) {
//			logger.warn("    - " + echec);
//		}
//	    logger.debug("FIN DU TRAITEMENT");
		url = "Comparison_of_World_War_I_tanks";
		extracteur.extraire(url);
	    br.close();
    }
    
    

}
