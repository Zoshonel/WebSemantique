package search;

/**
 * Les objets TermQ contiennent pour le texte et le poids associé à un terme de
 * la requête
 * 
 * */

public class TermQ {

	/** texte du terme */
	public String text;
	/** poids du terme dans la requête */
	public short weigth;

	/** Default constructor */
	public TermQ() {
	}

	/**
	 * Construit un nouveau terme
	 * 
	 * @param text
	 *            the Term text
	 */
	public TermQ(String text) {
		this.text = text;
	}

	/**
	 * Construuit un nouveau terme
	 * 
	 * @param text
	 *            texte du terme
	 * @param weigth
	 *            poid du terme dans la requête
	 */

	public TermQ(String text, short weigth) {
		this.text = text;
		this.weigth = weigth;

	} // TermQuery()

	/**
	 * Prints a TermQuery object
	 */
	public void PrintTerm() {

		System.out.print("Term Query:");
		System.out.println(this.text + " " + this.weigth);

	} // PrintTerm()

}
