package evaluateur;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class evaluateurReqs {

	public evaluateurReqs(String fic_query) {
		List<Map<String, Float>> evaluations = new ArrayList<>();
		try {
			BufferedReader config = new BufferedReader(
					new FileReader(fic_query));
			String ligne;
			int i = 1;
			while ((ligne = config.readLine()) != null) {
				evaluateurReq e = new evaluateurReq(ligne, "resources/qrels/qrelQ"
						+ i + ".txt", i++);
				evaluations.add(e.evaluer());
			}

			config.close();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// écriture des evaluations dans un fichier
		try {
			FileWriter fic_evals = new FileWriter(new File("evals.txt"));

			int i = 0;
			float sumRappel = 0;
			float sumP5 = 0;
			float sumP10 = 0;
			float sumP25 = 0;

			for (Map<String, Float> es : evaluations) {
				sumRappel += es.get("Rappel");
				sumP5 += es.get("P5");
				sumP10 += es.get("P10");
				sumP25 += es.get("P25");

				i++;
				fic_evals.write("Q" + i + "\nPrécision à 5 : " + es.get("P5")
						+ "\nPrécision à 10 : " + es.get("P10")
						+ "\nPrécision à 25 : " + es.get("P25") + "\n\n");
			}
			fic_evals.write("MOY" + i + "\nPrécision à 5 : " + (sumP5 / i)
					+ "\nPrécision à 10 : " + (sumP10 / i)
					+ "\nPrécision à 25 : " + (sumP25 / i) + "\n\n");
			fic_evals.close();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public static void main(String[] args) {
		new evaluateurReqs("requetes.txt");
	}
}
