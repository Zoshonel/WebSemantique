package search;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import store.BaseReader;

/**
 * Classe permettant de traiter les requetes : recherche des termes dans
 * l'index, calcul des scores des elements
 */
final public class TermQuery {

	// vecteur contenant les termes de la requetes (objets Term)
	private ArrayList<ArrayList<String>> terms;

	/**
	 * Constructeur : construit le vecteur des termes de la requete
	 */
	public TermQuery(String query) {
		// elimination des termes stopable
		List<String> stopables = new ArrayList<>();
		for (String stopword : index.IndexWriter.STOP_WORDS) {
			stopables.add(stopword);
		}
		System.out.println("La requete est:" + query);

		String[] termstable = query.split(", ");
		ArrayList<String> termstableOk = new ArrayList<String>();
		ArrayList<String> subTermstableOk = null;
		String[] subtermstable = null;
		for (String string : termstable) {
			if (!(stopables.contains(string))) {
				termstableOk.add(string);
			}
		}

		terms = new ArrayList<ArrayList<String>>();
		Iterator<String> termIterator = termstableOk.iterator();
		while (termIterator.hasNext()) {
			String term = termIterator.next();
			ArrayList<String> subTermList = new ArrayList<String>();
			if (term.contains(" ")) {
				subTermstableOk = new ArrayList<String>();
				subtermstable = term.split(" ");
				for (String string : subtermstable) {
					if (!stopables.contains(string)) {
						subTermstableOk.add(truncateToSeven(string));
					}
				}
				Iterator<String> subTermIterator = subTermstableOk.iterator();
				while (subTermIterator.hasNext()) {
					subTermList.add(subTermIterator.next());
				}
			} else {
				subTermList.add(truncateToSeven(term));
			}
			terms.add(subTermList);
		}

		System.out.println("Fin de la requete");
	}

	public static String truncateToSeven(String base) {
		if (base.length() > 7 && !Character.isUpperCase(base.charAt(0))) {
			base = Normalizer.normalize(base, Normalizer.Form.NFD);
			base = base.replaceAll("[^\\p{ASCII}]", "");
			return base.substring(0, 7).toLowerCase();
		}
		return base.toLowerCase();
	}

	/**
	 * Calcule les scores des documents contenant au moins un terme de la
	 * requete
	 */
	public List<Integer> score(BaseReader reader) throws IOException {

		List<Integer> result = new ArrayList<Integer>();
		try {
			String query = " select doc_id, SUM(nbterm) as \"nbtermTotal\",SUM(poids) as \"poidsTotal\" from ( ";
			Iterator<ArrayList<String>> iteratorTerms = terms.iterator();
			while (iteratorTerms.hasNext()) {
				ArrayList<String> subTermsList = iteratorTerms.next();
				if (subTermsList.size() == 1) {
					query += "(select doc_id,count(t.term) as \"nbterm\",SUM(poids)as \"poids\" from termes t,termesdoc td where t.term_id = td.term_id and (t.term like '"
							+ subTermsList.get(0)
							+ "') GROUP BY doc_id order by \"nbterm\" DESC,\"poids\" DESC)";
				} else {
					query += "(select * from ( select doc_id,count(t.term) as \"nbterm\",SUM(poids)as \"poids\"  from termes t,termesdoc td where t.term_id = td.term_id and ( ";
					Iterator<String> iteratorSubTerms = subTermsList.iterator();
					while (iteratorSubTerms.hasNext()) {
						String term = iteratorSubTerms.next();
						query += "t.term like '" + term + "'";
						if (iteratorSubTerms.hasNext())
							query += " or ";
					}
					query += ") GROUP BY doc_id) as \"r\" where nbterm = "
							+ subTermsList.size() + " )";
				}
				if (iteratorTerms.hasNext())
					query += " UNION ";
			}
			query += " ) as \"r\" GROUP BY doc_id order by \"nbtermTotal\" DESC,\"poidsTotal\" DESC ";

			System.out.println(query);
			ResultSet rs = null;
			Statement stmt = reader.conn.createStatement();
			rs = stmt.executeQuery(query);
			while (rs.next()) {
				int doc_id = rs.getInt("doc_id");
				result.add(doc_id);
			}
		} catch (SQLException ex) {
			System.out
					.println("Erreur de recuperation du terme ou de calcul du poids");
			System.out.println("SQLException: " + ex.getMessage());
			System.out.println("SQLState: " + ex.getSQLState());
			System.out.println("VendorError: " + ex.getErrorCode());
		}

		// System.out.println(result.size());
		return result;

	} // scorer
} // termQuery.java
