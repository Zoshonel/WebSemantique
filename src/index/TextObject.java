package index;

/**
 * Les objets TextObjects sont utilises comme cles de Hashtable pour l'instant
 * cet objet ne contient que le texte du terme mais ils pourraient contenir le
 * nombre de fois qu'il apparait pour faciliter le clacul de TFidf de robertson
 * */

public final class TextObject {
	String value;

	public TextObject(String value) {
		this.value = value;
	}

	public final boolean equals(Object o) {
		if (o == null)
			return false;
		TextObject other = (TextObject) o;
		return value.equals(other.value);
	}

	public final int hashCode() {
		return value.hashCode();
	}

} // TextObject.java
