package test;

import index.IndexWriter;
import store.BaseWriter;

import java.io.*;
import java.sql.SQLException;
import java.util.Date;

/**
 * 
 * Cette classe permet d'indexer une simple collection de documents HTML.<br>
 * L'index est stocke dans une base de donn�es relationnelle POstGres.
 *
 * Utilisation: java test/TestIndexer dir config <br>
 * o� dir est le repertoire contenant les documents, et config est le fichier de
 * configuration pour la connexion à la base.
 */

public class TestIndexer {

	public static void main(String argv[]) throws IOException, SQLException {

		Date datedebut = new Date();
		System.out.println("Indexation de la collection");
		// connection à la base
		BufferedReader config = new BufferedReader(new FileReader("config.txt"));
		String ConnectURL;
		String login = "";
		String pass = "";
		ConnectURL = config.readLine();
		if (ConnectURL != null) {
			login = config.readLine();
		}
		if (login != null) {
			pass = config.readLine();
		}
		config.close();

		BaseWriter maBase = new BaseWriter(ConnectURL, login, pass);

		System.out.println("Creation de la base");
		maBase.create();

		// Parcours des documents avec le parseur Jsoup et construction des
		// structures de stockage
		System.out.println("Parcours des documents");
		IndexWriter monIndexWriter = new IndexWriter(argv[0], maBase);
		monIndexWriter.construct();

		// fermeture de la base
		maBase.close();
		Date datefin = new Date();
		long duree = (datefin.getTime() - datedebut.getTime()) / 1000;

		System.out.println("That's all ! en :" + duree + " secondes");

	} // main(String[])

} // class TestIndexer.java
