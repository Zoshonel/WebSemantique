package evaluateur;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class evaluateurReq {

	private List<String> res_poids;
	private HashMap<String, Float> attendus_poids;

	public evaluateurReq(String requete, String qrel, int num_req) {
		String namefic = "res_req/out" + num_req + ".txt";
		new test.TestQuery("config.txt", requete, namefic);

		// construction liste obtenue
		res_poids = new ArrayList<String>();
		try {
			BufferedReader outputs = new BufferedReader(new FileReader(namefic));

			String ligne;
			while ((ligne = outputs.readLine()) != null) {
				String[] l = ligne.split("\t");
				res_poids.add(l[0]);
			}
			outputs.close();

		} catch (IOException e) {
			e.printStackTrace();
		}

		// création liste qrel
		attendus_poids = new HashMap<String, Float>();
		try {
			BufferedReader attendus = new BufferedReader(new FileReader(qrel));

			String ligne;
			while ((ligne = attendus.readLine()) != null) {
				String[] l = ligne.split("\t");
				l[1] = l[1].replace(',', '.');
				float p = Float.valueOf(l[1]);
				if (p > 0) {
					attendus_poids.put(l[0], p);
				}
			}
			attendus.close();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public Map<String, Float> evaluer() {
		int nbPertinent = attendus_poids.size();
		int trouves = res_poids.size();
		System.out.println("Documents trouvés : " + trouves
				+ " | Documents pertinents : " + nbPertinent);

		HashMap<String, Float> precision_rappel = new HashMap<String, Float>();
		int i = 0;
		float nbtrouves = 0;
		// calcul précision
		for (String res : res_poids) {
			if (attendus_poids.containsKey(res))
				nbtrouves++;

			i++;
			if (i == 5)
				precision_rappel.put("P5", nbtrouves / 5);
			else if (i == 10)
				precision_rappel.put("P10", nbtrouves / 10);
			else if (i >= nbPertinent) {
				precision_rappel.put("P25", nbtrouves / i);
				break;
			} else if (i == 25) {
				precision_rappel.put("P25", nbtrouves / 25);
				break;
			}

		}

		// calcul rappel
		float docsrappeles = 0;
		for (String attendu : attendus_poids.keySet()) {
			if (res_poids.contains(attendu))
				docsrappeles++;
		}
		precision_rappel.put("Rappel", docsrappeles / nbPertinent);

		return precision_rappel;
	}

}
