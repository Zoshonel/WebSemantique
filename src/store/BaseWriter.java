package store;

import index.DocumentAIndexer;
import index.Term;
import index.TermFrequency;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;

/**
 * Cette classe permet de creer une BD relationnelle Postgres et de stocker des
 * donnees dans la base
 *
 * Tables creees : Document, Termes, TermesDoc
 */

// Notice, do not import com.mysql.jdbc.*
// or you will have problems!

public class BaseWriter {

	static Connection conn;

	/**
	 * Constructeur. <br>
	 * Effectue une connexion a la base
	 * 
	 * @param ConnectURL
	 *            String contenant l'URL pour la connection.
	 * @param login
	 *            Login pour la connection a la base
	 * @param pass
	 *            Mot de passe pour la collection a la base
	 */
	public BaseWriter(String ConnectURL, String login, String pass) {

		try {

			Class.forName("org.postgresql.Driver").newInstance();
			System.out.println("Driver charge.");
		} catch (Exception ex) {
			// handle the error
		}

		try {
			System.out.println("Tentative de connection..." + ConnectURL + " "
					+ login);
			conn = DriverManager.getConnection(ConnectURL, login, pass);
			System.out.println("Connection etablie!");

		} catch (SQLException ex) {
			// handle any errors
			System.out.println("SQLException: " + ex.getMessage());
			System.out.println("SQLState: " + ex.getSQLState());
			System.out.println("VendorError: " + ex.getErrorCode());
		}

	} // BaseWriter()

	/**
	 * Creee toutes les tables de l'index:
	 * 
	 * Efface les tables si elles existent deja.
	 */
	public static void create() throws SQLException {

		// assume conn is an already created JDBC connection
		Statement stmt = null;

		stmt = conn.createStatement();
		stmt.addBatch("drop table if exists TermesDoc, Documents, Termes;");
		stmt.addBatch("create table Documents (doc_id int primary key, document varchar(300)) ;");
		System.out.println("Table Documents");
		stmt.addBatch("create table Termes (term_id int primary key, term varchar NOT NULL, nb int NOT NULL DEFAULT 0 );");
		System.out.println("Table Termes");
		stmt.addBatch("create table TermesDoc (term_id int, doc_id int, poids real,position int, PRIMARY KEY(term_id,doc_id));");
		System.out.println("Table TermesDoc");

		if (stmt != null) {
			try {
				stmt.executeBatch();
				System.out.println("Table creees");
				stmt.close();
			} catch (SQLException sqlEx) { // ignore
			}

			stmt = null;
		}

	} // create()

	/**
	 * Insere le contenu d'un vecteur de DocumentAIndexer dans la table
	 * Documents.
	 *
	 * @see DocumentAIndexer
	 * @param myDocumentVector
	 *            Vecteur d'objets DocumentAIndexer
	 */
	public static void insertDocument(Vector myDocumentVector)
			throws SQLException {
		StringBuilder listReq = new StringBuilder();
		Statement stmt = conn.createStatement();
		// assume conn is an already created JDBC connection
		// PreparedStatement pstmt = conn
		// .prepareStatement("insert into Documents (doc_id, document) values (?,?)");
		// pour chaque DocumentAIndexer du vecteur
		for (Enumeration e = myDocumentVector.elements(); e.hasMoreElements();) {

			DocumentAIndexer tempDocument = new DocumentAIndexer();
			tempDocument = (DocumentAIndexer) e.nextElement();
			stmt.addBatch("insert into Documents (doc_id, document) values ("
					+ tempDocument.id + ",\'" + tempDocument.name + "\');");
			// listReq.append(\n");
			// pstmt.setObject(1, tempDocument.id);
			// pstmt.setObject(2, tempDocument.name);
			// try {
			// pstmt.executeUpdate();
			// } catch (SQLException sqlEx) {
			// System.out.println("Erreur dans l'insertion dans Documents : "
			// + sqlEx.getMessage());
			// }

		}
		// if (pstmt != null) {
		// try {
		// pstmt.close();
		// } catch (SQLException sqlEx) {
		// System.out.println("Erreur dans l'insertion dans Documents : "
		// + sqlEx.getMessage());
		// }
		// }

		stmt.executeBatch();
		System.out.println("Insertion dans la table Documents : ok");

	} // insertDocument()

	/**
	 * Insere le contenu d'une hashtable de termes (objets Term) dans la table
	 * Termes et dans la table TermesDoc
	 * 
	 * @see Term
	 * @param myPostingTable
	 *            Hashtable d'objets Term
	 * @param tailleMoy
	 * @param docTaille
	 */
	public static void insertPosting(Hashtable myPostingTable,
			Hashtable<Integer, Integer> docTaille, double tailleMoy)
			throws SQLException {

		// assume conn is an already created JDBC connection
		PreparedStatement pstmt = conn
				.prepareStatement("insert into Termes (term_id,term,nb) values (?,?,?)");
		PreparedStatement pstmt2 = conn
				.prepareStatement("insert into TermesDoc (term_id,doc_id,poids) values (?,?,?)");
		// for each Term in the hashtable
		double pourcentIndex = 0;
		for (Enumeration e = myPostingTable.elements(); e.hasMoreElements();) {
			Term tempTerm = new Term();
			tempTerm = (Term) e.nextElement();
			boolean rs;
			tempTerm.nb = tempTerm.frequency.size();
			// System.out.println("j essaie :"+tempTerm.text);
			pstmt.setObject(1, tempTerm.term_id);
			pstmt.setObject(2, tempTerm.text);
			pstmt.setObject(3, tempTerm.frequency.size(), Types.INTEGER);
			try {
				pstmt.addBatch();
			} catch (SQLException sqlEx) {
				System.out.println("Attention le terme suivant est très long "
						+ tempTerm.term_id + " " + tempTerm.text);
			}

			for (Iterator it = tempTerm.frequency.keySet().iterator(); it
					.hasNext();) {
				TermFrequency tempTermFrequency = new TermFrequency();
				tempTermFrequency = (TermFrequency) tempTerm.frequency.get(it
						.next());
				pstmt2.setObject(1, tempTerm.term_id);
				pstmt2.setObject(2, tempTermFrequency.doc_id);
				pstmt2.setObject(
						3,
						calcTermFreqsuency(tempTermFrequency.frequency,
								docTaille.get(tempTermFrequency.doc_id),
								tailleMoy), Types.INTEGER);
				pstmt2.addBatch();

			}
		} // enumeration
		if (pstmt != null) {
			try {
				pstmt.executeBatch();
				System.out.println("Insertion dans la table Termes : ok");
				pstmt.close();
			} catch (SQLException sqlEx) { // ignore
			}
			pstmt = null;
		}
		if (pstmt2 != null) {
			try {
				pstmt2.executeBatch();
				System.out.println("Insertion dans la table TermesDoc : ok");
				pstmt2.close();
			} catch (SQLException sqlEx) { // ignore
			}
			pstmt2 = null;
		}

	} // insertPosting()

	/**
	 * Ferme la connection au serveur mySQL
	 */
	public static void close() {
		try {
			conn.close();

		} catch (SQLException ex) {
			// handle any errors
			System.out.println("SQLException: " + ex.getMessage());
			System.out.println("SQLState: " + ex.getSQLState());
			System.out.println("VendorError: " + ex.getErrorCode());
		}
	} // close()

	private static int calcTermFreqsuency(int freq, int tailleDoc,
			double tailleMoy) {
		// return (int) ((int) 100 * (freq / (freq + 0.5 + (1.5 * (tailleDoc /
		// tailleMoy)))));
		return freq;
	}

} // BaseWriter.java
