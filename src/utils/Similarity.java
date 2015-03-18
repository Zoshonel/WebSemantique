package utils;

import java.io.IOException;
import index.Term;
import store.BaseReader;

import java.sql.SQLException;

/**
 * Classe permettant le calcul de stocres
 */
public final class Similarity {

	private Similarity() {
	} // no public constructor

	/**
	 * Computes the Innerproduct for a TERM find in a doc and in a query
	 */
	public static final float InnerProd(float poidDoc, short poidReq) {

		return (float) (poidDoc * poidReq);

	}

} // Similarity.java
