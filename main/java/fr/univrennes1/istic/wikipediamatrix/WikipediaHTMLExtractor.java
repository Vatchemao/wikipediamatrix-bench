package fr.univrennes1.istic.wikipediamatrix;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;


import au.com.bytecode.opencsv.CSVWriter;

public class WikipediaHTMLExtractor {

	private String BASE_WIKIPEDIA_URL;
	private String outputDirHtml;
	private String outputDirWikitext;
	private File file;
	
	private CSVWriter csvWriter;

	private static final Logger logger = LogManager.getLogger(WikipediaHTMLExtractor.class);

	public WikipediaHTMLExtractor(String BASE_WIKIPEDIA_URL, String outputDirHtml, String outputDirWikitext, File file) {
		this.BASE_WIKIPEDIA_URL = BASE_WIKIPEDIA_URL;
		this.outputDirHtml = outputDirHtml;
		this.outputDirWikitext = outputDirWikitext;
		this.file = file;		
	}

	public RetourExtraction extraire(String url) throws IOException {
	   List<Element> listeRetour = new ArrayList<Element>();
	   boolean enEchec = false;
	   logger.debug("Début de l'extraction\n");
	   try {
		   String wurl = BASE_WIKIPEDIA_URL + url;
		   logger.debug("On extrait à présent cette page : " + wurl);
		   Document doc = Jsoup.connect(wurl).get();

	       // On itère sur chaque tableau de la page.
	       List<Element> tables = doc.select("table.wikitable");
	       logger.debug("Il y a " + String.valueOf(tables.size()) + " tableaux dans cette page.");
	       for (int i = 0; i < tables.size(); i++ ) {
	    	   Element table = tables.get(i);
	    	   traiterTableau(table, url, i);
	    	   listeRetour.add(table);
	       }
	   } catch(Exception e) {
		   logger.warn("\nCette url n'est pas accessible. Le traitement va l'ignorer et se poursuivre.");
		   enEchec = true;
	   }
	   logger.debug("Fin de l'extraction");
       return new RetourExtraction(listeRetour, enEchec);
	}

	public String mkCSVFileName(String url, int n) {
		return url.trim() + "-" + n + ".csv";
	}

	public void traiterTableau(Element table, String url, int i) throws IOException {
		logger.debug("Début du traitement du tableau n° " + String.valueOf(i + 1));

		// On initialise le fichier csv.
		String csvFileName = mkCSVFileName(url, i + 1);
		File csvFile = new File(outputDirWikitext + csvFileName);
		logger.debug("Le fichier est créé ici : " + outputDirWikitext + csvFileName);
		FileWriter fileWriter = new FileWriter(csvFile, StandardCharsets.UTF_8);
		csvWriter = new CSVWriter(fileWriter, ';');

		// On initialise la map de rowspans
		Map<Integer, Rowspan> rowspans = new HashMap<Integer, Rowspan>();

		Elements lignes = table.select("tr");
		logger.debug("Il y a " + String.valueOf(lignes.size()) + " lignes dans ce tableau, en-têtes comprises.");
		// On crée les en-tête TODO prendre en compte les en-tête sur deux lignes
		lignes = (Elements) createHeaders(lignes);

		// On crée les lignes TODO prendre en compte les colspans, les en-tête verticaux, les mixes, bref,
		// pas gagné ^^
		int compteur = 0;
		for (Element ligne : lignes) {
			rowspans = traiterLigne(rowspans, ligne);
			compteur++;
		}
        csvWriter.close();
		logger.debug("Fin du traitement du tableau n° " + String.valueOf(i + 1) + "\n");
	}

	public List<Element> createHeaders(List<Element> lignes) {
		logger.debug("Début de la création des en-têtes");
		List<Element> listeHeaders = lignes.get(0).children();
		String listeEntetes[] = new String[listeHeaders.size()];
		for (int j = 0; j < listeHeaders.size() ; j++) {
			listeEntetes[j] = listeHeaders.get(j).text();
			}
		csvWriter.writeNext(listeEntetes);
		lignes.remove(0);
		logger.debug("Fin de la création des en-têtes");
		return lignes;
	}

//	public void traiterLigne(Element ligne, CSVWriter csvWriter) {
//		Elements cellules = ligne.children();
//		String listeCellules[] = new String[cellules.size()];
//		for (int k = 0; k < cellules.size(); k++) {
//		   listeCellules[k] = cellules.get(k).text();
//		}
//		csvWriter.writeNext(listeCellules);
//	}

	/**Cette méthode permet de traiter une ligne. Un soin particulier a été apporté à la gestion des rowspans.
	 * Cette méthode prend en paramètre une map <Integer, Rowspan>, et retourne une map du même type, ce qui
	 * lui permet de traiter les rowspans que lui ont légués les lignes précédentes, et d'envoyer aux lignes 
	 * cette map, modifiée et potentiellement complétées des nouveaux rowspans dont elle fait l'objet.**/
	public Map<Integer, Rowspan> traiterLigne(Map<Integer, Rowspan> mapRowspans, Element ligne) {
		Elements cellules = ligne.children();
		
		Map<Integer, Rowspan> retour = new HashMap<Integer, Rowspan>();
		Rowspan rowspan = null;
		
		int compteurAnciensRowspansTraites = 0;
		int tailleTotale = cellules.size() + mapRowspans.size();
		
		// Pour compléter la liste nouvellement créée avec les éventuels rowspans résiduels légués
		// par les lignes précédentes, on parcourt la map rowspans. Lorsqu'on trouve un Rowspan à la 
		// position Integer, on insère une nouvelle cellule dans la liste des cellules nouvellement 
		// créée, à la position Integer.
		String listeCellules[] = new String[tailleTotale];
		for (int k = 0; k < tailleTotale; k++) {
			if (mapRowspans.containsKey(Integer.valueOf(k))) {
				rowspan = mapRowspans.get(k);
				listeCellules[k] = rowspan.getTexte();
				// Et on n'oublie pas de mettre à jour la map, en décrémentant l'attribut rowspanResiduel
				// du rowspan courant (voire supprimant ce rowspan s'il devient nul).
				if (rowspan.getRowspanResiduel() > 1) {
					rowspan.setRowspanResiduel(rowspan.getRowspanResiduel() - 1);
					retour.put(k, rowspan);
				}
				compteurAnciensRowspansTraites += 1;
			} else {
				Element cellule = cellules.get(k - compteurAnciensRowspansTraites);
				listeCellules[k] = cellule.text();
				// Si la cellule contient un rowspan, on le crée et on l'ajoute à la map.
				if (cellule.hasAttr("rowspan")) {
					rowspan = new Rowspan(Integer.valueOf(cellule.attr("rowspan")) - Integer.valueOf(1), cellule.text());
					retour.put(k, rowspan);
				}
			}
		}
		csvWriter.writeNext(listeCellules);
		return retour;
	}

	public String getBASE_WIKIPEDIA_URL() {
		return BASE_WIKIPEDIA_URL;
	}

	public void setBASE_WIKIPEDIA_URL(String bASE_WIKIPEDIA_URL) {
		BASE_WIKIPEDIA_URL = bASE_WIKIPEDIA_URL;
	}

	public String getOutputDirHtml() {
		return outputDirHtml;
	}

	public void setOutputDirHtml(String outputDirHtml) {
		this.outputDirHtml = outputDirHtml;
	}

	public String getOutputDirWikitext() {
		return outputDirWikitext;
	}

	public void setOutputDirWikitext(String outputDirWikitext) {
		this.outputDirWikitext = outputDirWikitext;
	}

	public File getFile() {
		return file;
	}

	public void setFile(File file) {
		this.file = file;
	}

}