package index;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.TreeMap;
import java.util.Vector;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
//import org.w3c.dom.*;
//import org.xml.sax.*;
//import javax.xml.parsers.*;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import search.TermQuery;
import store.BaseWriter;
import utils.FileList;

/**
 * IndexWriter ecrit l'index (c'est a dire les 3 tables de la BD). Les documents
 * sont parses avec Jsoup.
 *
 */

public final class IndexWriter {

	/**
	 * Vecteur contenant des objets Document
	 * 
	 * @see DocumentAIndexer
	 */
	public Vector documentVector;

	/**
	 * Vecteur contenant des objets Noeud
	 * 
	 * @see NodeAIndexer
	 */
	public Vector pathTable;

	/**
	 * Hashtable contenant des objets Term
	 * 
	 * @see Term
	 */
	public Hashtable<TextObject, Term> postingTable;
	private Hashtable<Integer, Integer> DocTaille;

	// compteur pour l'identifiant du terme
	protected int count_id_term;
	// compteur pour l'identifiant de document
	protected int count_id_doc;

	// nombre de termes dans un document
	protected int term_count;

	protected BaseWriter maBase;

	// liste des fichiers a indexer
	protected Vector fileList;

	private double tailleMoy;

	// mots vides du français
	public static final String[] STOP_WORDS = { "a", "à", "afin", "ai", "aie",
			"aient", "aient", "ainsi", "ais", "ait", "alors", "as", "assez",
			"au", "auquel", "auquelle", "aussi", "aux", "auxquelles",
			"auxquels", "avaient", "avais", "avait", "avant", "avec", "avoir",
			"beaucoup", "ca", "ça", "car", "ce", "cela", "celle", "celles",
			"celui", "certain", "certaine", "certaines", "certains", "ces",
			"cet", "cette", "ceux", "chacun", "chacune", "chaque", "chez",
			"ci", "comme", "comment", "concern", "concernant", "connait",
			"connaît", "conseil", "contre", "d", "dans", "de", "des",
			"desquelles", "desquels", "differe", "different", "différent",
			"differente", "différente", "differentes", "différentes",
			"differents", "différents", "dois", "doit", "doivent", "donc",
			"dont", "du", "dû", "duquel", "dus", "e", "elle", "elles", "en",
			"encore", "ensuite", "entre", "es", "est", "et", "etai", "etaient",
			"étaient", "etais", "étais", "etait", "était", "etant", "étant",
			"etc", "ete", "été", "etiez", "étiez", "etion", "etions", "étions",
			"etre", "être", "eu", "eux", "evidenc", "evidence", "évidence",
			"expliqu", "explique", "fai", "faire", "fais", "fait", "faite",
			"faites", "faits", "fera", "feras", "fini", "finie", "finies",
			"finis", "finit", "font", "grace", "grâce", "ici", "il", "ils",
			"intere", "interessant", "intéressant", "interesse", "intéressé",
			"j", "jamais", "je", "l", "la", "laquell", "laquelle", "le",
			"lequel", "les", "lesquelles", "lesquels", "leur", "leurs", "lors",
			"lorsque", "lui", "m", "ma", "mainten", "maintenant", "mais",
			"mal", "me", "meme", "même", "memes", "mêmes", "mes", "mettre",
			"moi", "moins", "mon", "n", "ne", "ni", "no", "non", "nos",
			"notre", "nôtre", "notres", "nôtres", "nou", "nous", "obtenu",
			"obtenue", "obtenues", "obtenus", "on", "ont", "or", "ou", "où",
			"par", "parfois", "parle", "pars", "part", "pas", "permet", "peu",
			"peut", "peuvent", "peux", "plus", "pour", "pourquo", "pourquoi",
			"pouvez", "pouvons", "prendre", "pres", "près", "princip",
			"principal", "principaux", "qu", "quand", "que", "quel", "quelle",
			"quelles", "quelques", "quels", "qui", "quoi", "sa", "savoir",
			"se", "seront", "ses", "seul", "seuls", "si", "soient", "soit",
			"son", "sont", "sous", "souvent", "sui", "suis", "sur", "t", "ta",
			"te", "tel", "telle", "telleme", "tellement", "telles", "tels",
			"tes", "ton", "toujour", "toujours", "tous", "tout", "toute",
			"toutes", "traite", "tres", "très", "trop", "tu", "un", "une",
			"unes", "uns", "utilise", "utilisé", "utilisee", "utilisée",
			"utilisées", "utilisees", "uilisés", "utilises", "va", "venir",
			"vers", "veut", "veux", "vont", "voulez", "voulu", "vous" };

	Hashtable<String, String> Stoptable;

	/**
	 * COnstructeur. Met les compteurs a zero et initialise les structures des
	 * stockage, instancie le parseur.
	 */
	public IndexWriter(String direc, BaseWriter base) throws IOException {

		fileList = FileList.list(direc);
		maBase = base;

		documentVector = new Vector();
		pathTable = new Vector();
		postingTable = new Hashtable<TextObject, Term>();

		count_id_doc = 0;
		count_id_term = 0;
		// count_id_node=0;

		term_count = 0;
		// node_count=0;
		// leaf_count=0;

		Stoptable = new Hashtable<String, String>();
		for (int i = 0; i < STOP_WORDS.length; i++) {
			Stoptable.put(STOP_WORDS[i], STOP_WORDS[i]);
		}

	}

	/**
	 * Permet de remplir la base avec toutes les informations contenues dans la
	 * memoire
	 */
	public void construct() {
		DocTaille = new Hashtable<Integer, Integer>();
		System.out.println("Traitement des fichiers ");
		for (int i = 0; i < fileList.size(); i++) {
			String monNom = (String) fileList.elementAt(i);
			File fichier = new File(monNom);
			String Nomfichier = fichier.getName();

			term_count = 0;

			try {
				// parsage du fichier
				Document document = Jsoup.parse(fichier, "UTF-8");
				// on recupère le texte contenu dans le body et on l'index
				Element body = document.body();
				// System.out.println("corps doc "+body.text());
				constructTerme(body.text(), 1);
				for (int j = 1; j < 3; j++) {
					if (body.getElementsByTag("h" + j) != null)
						constructTerme(body.getElementsByTag("h" + j).text(),
								100 - (j * 11));
				}
				if (body.getElementsByTag("strong") != null)
					constructTerme(body.getElementsByTag("strong").text(), 15);
				if (body.getElementsByTag("title") != null)
					constructTerme(body.getElementsByTag("title").text(), 15);
				if (body.getElementsByTag("meta") != null)
					constructTerme(body.getElementsByTag("meta").text(), 50);

			}

			catch (IOException io) {
				System.out.println("Erreur de d'entree/sortie");
			}
			DocumentAIndexer dtoindex = new DocumentAIndexer(count_id_doc,
					Nomfichier);
			documentVector.add(dtoindex);
			DocTaille.put(count_id_doc, term_count);
			count_id_doc++;
		}// on a fini de parcourir tous les documents
		tailleMoy /= fileList.size();
		// on insere les donnees sur les documents dans la base
		try {
			// PrintDocumentTable();
			maBase.insertDocument(documentVector);
		} catch (SQLException sqle) {
			System.out.println("Erreur insertion document et noeuds "
					+ sqle.getMessage());
		}

		// on insere les termes dans la base
		try {
			// PrintPostingTable();
			maBase.insertPosting(postingTable, DocTaille, tailleMoy);
		} catch (SQLException sqle2) {
			System.out.println("Erreur insertion termes " + sqle2.getMessage());
		}

	} // construct()

	/**
	 * Permet de remplir la table de posting avec le texte.
	 */
	public final void constructTerme(String texte, int multiplicateur) {

		// il faut traiter tout ce texte...

		// on passe en minuscules
		// texte= texte.toLowerCase();

		// on commence par remplacer
		texte = texte.replaceAll("http://[^ ]*", " ");
		texte = texte.replaceAll(" - ", " ");
		texte = texte.replaceAll("- ", " ");
		texte = texte.replaceAll(" -", " ");
		texte = texte.replaceAll("-{2,}", " ");
		texte = texte.replace('.', ' ');
		texte = texte.replace('/', ' ');
		texte = texte.replace('!', ' ');
		texte = texte.replace(';', ' ');
		texte = texte.replace(',', ' ');
		texte = texte.replace('+', ' ');
		texte = texte.replace('*', ' ');
		texte = texte.replace('?', ' ');
		texte = texte.replace('[', ' ');
		texte = texte.replace(']', ' ');
		texte = texte.replace('(', ' ');
		texte = texte.replace(')', ' ');
		texte = texte.replace('\'', ' ');
		texte = texte.replace('\"', ' ');
		texte = texte.replace(':', ' ');
		texte = texte.replace('\\', ' ');
		texte = texte.replace('}', ' ');
		texte = texte.replace('{', ' ');
		texte = texte.replace('&', ' ');
		texte = texte.replace('©', ' ');
		texte = texte.replace('»', ' ');
		texte = texte.replace('«', ' ');

		// A utiliser pour les query
		Pattern p = Pattern
				.compile("[^a-zA-Z0-9áàâäãåçéèêëíìîïñóòôöõúùûüýÿæœÁÀÂÄÃÅÇÉÈÊËÍÌÎÏÑÓÒÔÖÕÚÙÛÜÝŸÆŒ\'-]");
		String[] mots = p.split(texte);

		for (int j = 0; j < mots.length; j++) {
			String mot = mots[j]; // on pourrair utiliser Porter ou la
									// troncature ...!
			if (mot != null) {
				mot.replaceAll(
						"[^a-zA-Z0-9áàâäãåçéèêëíìîïñóòôöõúùûüýÿæœÁÀÂÄÃÅÇÉÈÊËÍÌÎÏÑÓÒÔÖÕÚÙÛÜÝŸÆŒ\'-]",
						"");
			}
			// on verifie que le mot n'est pas un mot vide ou un mot qui
			// contient un @ ou un %
			if (Stoptable.get(mot) == null && !mot.equals(" ")
					&& !(mot.length() == 0)) {
				mot = TermQuery.truncateToSeven(mot);
				TextObject myTermText = new TextObject(mot);
				term_count++;
				if (postingTable.containsKey(myTermText)) { // si la table de
															// posting contient
															// deja le terme car
															// rencontrer soit
															// dans une autre
															// doc, soit dans le
															// même

					Term myTerm = (Term) postingTable.get(myTermText); // on
																		// récupère
																		// les
																		// infos
																		// qu'on
																		// a
																		// jusqu'ici
					postingTable.remove(myTermText);
					TreeMap freq = new TreeMap();
					freq = myTerm.frequency; // on recupère les occurences dans
												// les autre documents

					if (freq.containsKey(count_id_doc)) { // si le terme a déjà
															// été trouvé pour
															// le document

						TermFrequency myTermFrequency = (TermFrequency) freq
								.get(count_id_doc);
						freq.remove(count_id_doc);
						myTermFrequency.frequency = (short) (myTermFrequency.frequency + multiplicateur);
						freq.put(count_id_doc, myTermFrequency);
						Term myNewTerm = new Term(myTerm.term_id, myTerm.text,
								freq);
						postingTable.put(myTermText, myNewTerm);
					}

					else { // si le terme est trouve dans un nouvel docuemnt
						short un = (short) (1 * multiplicateur);
						TermFrequency myTermFrequency = new TermFrequency(
								count_id_doc, un);
						freq.put(count_id_doc, myTermFrequency);
						Term myNewTerm = new Term(myTerm.term_id, myTerm.text,
								freq);
						postingTable.put(myTermText, myNewTerm);
						Boolean myNewBoolean = new Boolean(false);

					}

				} // if postinTable.containsKey
				else { // si la table de posting ne contient pas le terme, on
						// l'insere!

					short un = 1;
					TermFrequency myTermFrequency = new TermFrequency(
							count_id_doc, un);

					TreeMap freq = new TreeMap();
					freq.put(count_id_doc, myTermFrequency);
					Term myTerm = new Term(count_id_term, mot, freq);
					count_id_term++;
					postingTable.put(myTermText, myTerm);

				} // else

			} // if

		} // for
		tailleMoy += term_count;
	}

	/** Prints the documentVector */
	public final void PrintDocumentTable() {

		for (Enumeration e = documentVector.elements(); e.hasMoreElements();) {
			DocumentAIndexer tempDocument = new DocumentAIndexer();
			tempDocument = (DocumentAIndexer) e.nextElement();
			tempDocument.PrintDocument();
		}

	} // PrintDocumentTable()

	/** Prints the postingTable */
	public final void PrintPostingTable() {

		for (Enumeration e = postingTable.elements(); e.hasMoreElements();) {
			Term tempTerm = new Term();
			tempTerm = (Term) e.nextElement();
			tempTerm.PrintTerm();
		}

	} // PrintPostingTable()

} // IndexWriter.java

