package utils;

import java.io.File;
import java.util.Vector;

public class FileList {

	public static Vector fileList = new Vector();

	private FileList() {
	} // no public constructor

	/**
	 * Permet de trouver tous les documents � indexer pr�sents dans un
	 * r�pertoire
	 * 
	 * @param direc
	 *            Chemin jusqu'au r�pertoire contenant les documents � indexer
	 * @return Vector Vector contenant tous les noms de documents
	 */
	static public final Vector list(String direc) {

		File f1 = new File(direc);

		if (f1.exists() == false) {
			System.err.println(f1 + " : not found");
		}

		if (f1.isFile() == true) { // if f1 is a file
			String nf = new String(f1.toString());
			fileList.add(nf);
		} else {
			// f1 is a directory
			String tab_fichier[] = f1.list();
			File f2;

			for (int i = 0; i < tab_fichier.length; i++) {
				f2 = new File(direc, tab_fichier[i]);
				String nf2 = f2.toString();
				f2 = null;
				// System.out.println("fichier ajouté :"+nf2);

				list(nf2);

			}
		}

		f1 = null;

		return fileList;

	} // list()

} // FileList.java
