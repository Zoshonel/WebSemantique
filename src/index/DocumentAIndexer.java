package index;

import java.sql.Date;
import java.util.Vector;
import java.util.Enumeration;

/**
 * Les objets DocumentAIndexer contiennnent les unit�s d'indexation
 *
 * Un DocumentAIndexer est d�fini par son nom, et un vecteur de termes -
 * <i>ContentVector</i>-. Il est aussi decrit par le nombre de termes, Les
 * objets DocumentAIndexer sont crees pendant la phase d'indexation.
 * */

public final class DocumentAIndexer {

	/** Identifiant du document */
	public int id;

	/** Nom du document */
	public String name = null;

	/**
	 * Vecteur contenant tous les termes du document
	 * 
	 * @see Content
	 */
	// public Vector ContentVector;

	/** Nombre de termes dans le document */
	// public int term_count;

	/** Constuit un nouveau document. */
	public DocumentAIndexer() {
	}

	/**
	 * Construit un nouveau document � indexer
	 * 
	 * @param identifiant
	 *            identifiant de document
	 * @param name
	 *            nom du document
	 **/
	public DocumentAIndexer(int identifiant, String name) {

		this.id = identifiant;
		this.name = name;

		// this.ContentVector=new Vector();
	}

	/** Affiche le document */
	public void PrintDocument() {

		System.out.println("Id :" + this.id + "\tDocument :" + this.name);

	} // PrintDocument()

} // DocumentAIndexer.java
