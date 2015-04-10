package sparqlclient;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class SparqlClientExample {

    /**
     * @param args the command line arguments
     */
    public void main(String args[]) {
        SparqlClient sparqlClient = new SparqlClient("localhost:8080/sparql.tpl");

        String query = "ASK WHERE { ?s ?p ?o }";
        boolean serverIsUp = sparqlClient.ask(query);
        if (serverIsUp) {
            System.out.println("server is UP");

            nbPersonnesParPiece(sparqlClient);

//            System.out.println("ajout d'une personne dans le bureau:");
//            query = "PREFIX : <http://www.lamaisondumeurtre.fr#>\n"
//                    + "PREFIX instances: <http://www.lamaisondumeurtre.fr/instances#>\n"
//                    + "INSERT DATA\n"
//                    + "{\n"
//                    + "  instances:Bob :personneDansPiece instances:Bureau.\n"
//                    + "}\n";
//            sparqlClient.update(query);

//            System.out.println("suppression d'une personne du bureau:");
//            query = "PREFIX : <http://www.lamaisondumeurtre.fr#>\n"
//                    + "PREFIX instances: <http://www.lamaisondumeurtre.fr/instances#>\n"
//                    + "DELETE DATA\n"
//                    + "{\n"
//                    + "  instances:Bob :personneDansPiece instances:Bureau.\n"
//                    + "}\n";
//            sparqlClient.update(query);
        
        } else {
            System.out.println("service is DOWN");
        }
    }
    
    
    public ArrayList<String> callSparql(String terms){
    	SparqlClient sparqlClient = new SparqlClient("localhost:3030/space");

        String query = "ASK WHERE { ?s ?p ?o }";
        boolean serverIsUp = sparqlClient.ask(query);
        if (serverIsUp) {
            System.out.println("server is UP");

            return getSynonymesOfTerms(sparqlClient);
        
        } else {
            System.out.println("service is DOWN");
            return null ;
        }
    }
    
    
    public Map<String,String> callSparqlProps(String terms){
    	SparqlClient sparqlClient = new SparqlClient("localhost:3030/space");

        String query = "ASK WHERE { ?s ?p ?o }";
        boolean serverIsUp = sparqlClient.ask(query);
        if (serverIsUp) {
            System.out.println("server is UP");

            return getAllProperties(sparqlClient);
        
        } else {
            System.out.println("service is DOWN");
            return null ;
        }
    }
    
    
    public ArrayList<String> getSynonymesOfTerms(SparqlClient sparqlClient) {
        String query = "PREFIX filmo: <http://www.irit.fr/recherches/MELODI/ontologies/FilmographieV1.owl#> "
						+ "PREFIX rdf:  <http://www.w3.org/1999/02/22-rdf-syntax-ns#> "
						+ "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> "
						+ "PREFIX owl:  <http://www.w3.org/2002/07/owl#> "
						+ "PREFIX xsd:  <http://www.w3.org/2001/XMLSchema#> "
						+ " "
						+ "SELECT ?sousClasses "
						+ "WHERE { "
						+ " 	?a rdfs:subClassOf filmo:Ceremonie . "
						+ " 	?a rdfs:label ?sousClasses "
						+ "}";
            Iterable<Map<String, String>> results = sparqlClient.select(query);
            System.out.println("nombre de personnes par pièce:");
            System.out.println(query);
            
            ArrayList<String> synonymes = new ArrayList<String>() ;
            for (Map<String, String> result : results) {
//                System.err.println(result.get("sousClasses"));
                synonymes.add(result.get("sousClasses"));
            }
            return synonymes ;
    }  
    
    
    
    
    public Map<String, String> getAllProperties(SparqlClient sparqlClient) {
        String query = "PREFIX filmo: <http://www.irit.fr/recherches/MELODI/ontologies/FilmographieV1.owl#> "
							+ "PREFIX rdf:  <http://www.w3.org/1999/02/22-rdf-syntax-ns#> "
							+ "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> "
							+ "PREFIX owl:  <http://www.w3.org/2002/07/owl#> "
							+ "PREFIX xsd:  <http://www.w3.org/2001/XMLSchema#> "
							+ " "
							+ "SELECT ?subject ?a "
							+ "WHERE { "
							+ "   ?subject ?property ?object. "
							+ "   ?subject rdfs:label ?a "
							+ "} ";
            Iterable<Map<String, String>> results = sparqlClient.select(query);
            System.out.println(query);
            
            Map<String, String> properties = new HashMap< String, String >();
            for (Map<String, String> result : results) {
//                System.err.println(result.get("a"));
                properties.put(result.get("subject"),result.get("a"));
            }
            return properties ;
    }  
    
    
    
    
    
    public void nbPersonnesParPiece(SparqlClient sparqlClient) {
        String query = "PREFIX : <http://www.lamaisondumeurtre.fr#>\n"
                    + "SELECT ?piece (COUNT(?personne) AS ?nbPers) WHERE\n"
                    + "{\n"
                    + "    ?personne :personneDansPiece ?piece.\n"
                    + "}\n"
                    + "GROUP BY ?piece\n";
            Iterable<Map<String, String>> results = sparqlClient.select(query);
            System.out.println("nombre de personnes par pièce:");
            for (Map<String, String> result : results) {
                System.out.println(result.get("piece") + " : " + result.get("nbPers"));
            }
    }    
}
