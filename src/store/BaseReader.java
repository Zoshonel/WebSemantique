package store;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.PreparedStatement;
import java.sql.Blob;
import java.util.Vector;
import java.util.Enumeration;
import java.sql.Date;
import java.util.Hashtable;
import java.util.TreeMap;
import java.lang.Integer;
import java.io.*;

import index.DocumentAIndexer;
import index.Term;
import index.TermFrequency;

/**
 * Cette classe permet de lire la BD relationnelle contenant l'index Tables
 * pouvant etre lues: Documents, Noeuds, Termes
 */

public class BaseReader {

	public static Connection conn;

	/**
	 * Constructeur. <br>
	 * Effectue une connection a la base
	 * 
	 * @param ConnectURL
	 *            String contenant l'URL pour la connection. Ex :
	 *            jdbc:mysql://localhost:3306/databasename
	 * @param login
	 *            Login pour la connection a la BD
	 * @param pass
	 *            Password pour la connection a la BD
	 */
	public BaseReader(String ConnectURL, String login, String pass) {

		try {
			// The newInstance() call is a work around for some
			// broken Java implementations
			Class.forName("org.postgresql.Driver").newInstance();
			// System.out.println("Driver charge.");
		} catch (Exception ex) {
			// handle the error
		}

		try {
			// System.out.println("Tentative de connection...");
			conn = DriverManager.getConnection(ConnectURL, login, pass);
			// System.out.println("Connection ok!");

		} catch (SQLException ex) {
			// handle any errors
			System.out.println("SQLException: " + ex.getMessage());
			System.out.println("SQLState: " + ex.getSQLState());
			System.out.println("VendorError: " + ex.getErrorCode());
		}

	} // BaseReader()

	/**
	 * Trouve un objet DocumentAIndexer correspondant a un id
	 * 
	 * @param id
	 *            l'identifiant du doc
	 * @return DocumentAIndexer objet DocumentAIndexer
	 * @see DocumentAIndexer
	 */
	public DocumentAIndexer document(int iddoc) throws SQLException {

		DocumentAIndexer myDocument = new DocumentAIndexer();
		Statement stmt = null;
		ResultSet rs = null;
		stmt = conn.createStatement();
		String query = "select * from Documents where doc_id=" + iddoc;
		rs = stmt.executeQuery(query);
		while (rs.next()) {
			myDocument.name = rs.getString("document");

		}
		if (stmt != null) {
			try {
				stmt.close();
			} catch (SQLException sqlEx) { // ignore
			}

			stmt = null;
		} // stmt

		return myDocument;

	} // document()

	/**
	 * Trouve l'objet Term correspondant a un terme en le cherchant dans la
	 * table Termes
	 * 
	 * @see Term
	 * @param termName
	 *            nom du terme a chercher
	 * @return Term Object Term correspondant a la chaine a rechercher
	 */
	public Term readTerm(String termName) throws SQLException {

		// assume conn is an already created JDBC connection
		Statement stmt = null;
		Statement stmt2 = null;
		ResultSet rs = null;
		stmt = conn.createStatement();
		stmt2 = conn.createStatement();
		String query = "select * from Termes where term=\'" + termName + "\';";
		Term myTerm = null;
		TreeMap tableFrequency = new TreeMap();
		rs = stmt.executeQuery(query);
		while (rs.next()) {
			int term_id = rs.getInt("term_id");

			String query2 = "select * from TermesDoc where term_id=" + term_id
					+ ";";
			ResultSet rs2 = null;
			rs2 = stmt2.executeQuery(query2);
			// for each doc in which the term is
			while (rs2.next()) {
				int doc_id = rs2.getInt("doc_id"); // get the node doc id
				short frequency = rs2.getShort("poids"); // get frequency
				// System.out.println("terme présent dans "+this.document(doc_id).name+" de poids "+frequency);
				TermFrequency myTermFrequency = new TermFrequency(doc_id,
						frequency);
				tableFrequency.put(doc_id, myTermFrequency);

			}

			myTerm = new Term(term_id, termName, tableFrequency);
		} // while rs.next

		if (stmt != null) {
			try {
				stmt.close();
			} catch (SQLException sqlEx) { // ignore
			}
			stmt = null;
		} // stmt
		return myTerm;

	} // readTerm()

	/**
	 * Ferme la connection au serveur MySQL
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

}// BaseReader.java
