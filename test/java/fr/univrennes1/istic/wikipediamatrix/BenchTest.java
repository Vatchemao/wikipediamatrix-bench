package fr.univrennes1.istic.wikipediamatrix;

import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.nio.charset.StandardCharsets;
import au.com.bytecode.opencsv.CSVReader;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.jsoup.nodes.Element;

public class BenchTest {

	private String BASE_WIKIPEDIA_URL = "https://en.wikipedia.org/wiki/";
	private String outputDirHtml = "output" + File.separator + "html" + File.separator;
	private String outputDirWikitextTest = "output" + File.separator + "wikitext" + File.separator + "test" + File.separator;
	private String outputDirWikitextTestNombreFichiers = "output" + File.separator + "wikitext" + File.separator + "test" + File.separator + "testNombreFichiers" + File.separator;
	private File file = new File("inputdata" + File.separator + "wikiurls.txt");
	private String url;
	private int nurl = 0;

	private static final Logger logger = LogManager.getLogger(BenchTest.class);

    private WikipediaHTMLExtractor extracteur = new WikipediaHTMLExtractor(BASE_WIKIPEDIA_URL, outputDirHtml, outputDirWikitextTest, file);

	@Test
	public void testNombreURL() throws Exception {
		logger.debug("Début du test testNombreURL");
		BufferedReader br = new BufferedReader(new FileReader(file));
		while ((url = br.readLine()) != null) {
	    	nurl++;
	    }
	    br.close();
	    assertEquals(336, nurl);
		logger.debug("Fin du test testNombreURL\n");
	}

	@Test
	public void testNombreTableaux() throws IOException {
		logger.debug("\nDébut du test testNombreTableaux");
		url = "Comparison_of_World_War_I_tanks";
    	assertEquals(2, extracteur.extraire(url).getListeTableaux().size());
		logger.debug("Fin du test testNombreTableaux\n");
	}

	@Test
	public void testNombreFichiersCrees() throws IOException {
		logger.debug("\nDébut du test testNombreFichiersCrees");
		// On crée un extracteur spécifique, pour crééer les fichiers dans un répertoire spécifique afin de ne pas brouiller le compte de fichiers
		WikipediaHTMLExtractor extracteurTestNombreFichiersCrees = new WikipediaHTMLExtractor(BASE_WIKIPEDIA_URL, outputDirHtml, outputDirWikitextTestNombreFichiers, file);
		url = "Comparison_between_Esperanto_and_Ido";
		extracteurTestNombreFichiersCrees.extraire(url);
		// On crée une image du répertoire où sont générés les csv pour ce test.
		File repertoireOutput = new File(outputDirWikitextTestNombreFichiers);
    	assertEquals(8, repertoireOutput.listFiles().length);
		logger.debug("Fin du test testNombreFichiersCrees\n");
	}

	@Test 
	public void testNombreLignes() throws IOException {
		logger.debug("Début du test testNombreLigne");
		url = "Comparison_of_World_War_I_tanks";
    	assertEquals(6, extracteur.extraire(url).getListeTableaux().get(1).select("tr").size());
		logger.debug("Début du test testNombreLigne\n");
	}
	
	@Test
	public void testNombreColonnes() throws IOException {
		logger.debug("Début du test testNombreColonnes");
		url = "Comparison_of_World_War_I_tanks";
		assertEquals(12, extracteur.extraire(url).getListeTableaux().get(0).select("tr").get(1).children().size());
		logger.debug("Début du test testNombreColonnes\n");
	}

	@Test
	public void testValeurCellule() throws IOException {
		logger.debug("Début du test testValeurCellule");
		url = "Comparison_of_ICBMs";
		List<Element> liste = extracteur.extraire(url).getListeTableaux();
		// On récupère la valeur de la page wikipédia (premier tableau, deuxième ligne, sixième colonne).
		String valeurCelluleSource = liste.get(0).select("tr").get(3).select("td").get(5).text();
		// On récupère la valeur du fichier csv créé.
		File csvFile = new File(outputDirWikitextTest + extracteur.mkCSVFileName(url, 1));
		FileReader fileReader = new FileReader(csvFile, StandardCharsets.UTF_8);
		CSVReader csvReader = new CSVReader(fileReader);
		// NB : comme il n'est pas possible d'accéder directement à la nième ligne d'un fichier, on doit
		// lire les n-1 premières.
		int i = 0;
		String valeurCelluleCsv = "";
		while(i <4) {
			valeurCelluleCsv = csvReader.readNext()[0].split(";")[5];
			i++;
		}
		// Et on compare les deux (on supprime les guillemets initiaux et finals de valeurCelluleCsv).
		assertEquals(valeurCelluleSource, valeurCelluleCsv.substring(1, valeurCelluleCsv.length()-1));
		csvReader.close();
		logger.debug("Fin du test testValeurCellule\n");
	}

}