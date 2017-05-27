package test;

/*
 * http://blog.csdn.net/tao_sun/article/details/14585719
 */
import java.io.File;
import java.util.Set;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;



public class LoadOWL {
	
	
	public void shouldLoad() {
		
		// we can load a local ontology file

	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		File file = new File("/home/fry/Documents/svn/ai/trunk/ontology/qieyinChild.owl");
		
	    try {
	        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
	        OWLOntology localOntology = manager.loadOntologyFromOntologyDocument(file);
	        System.out.println("afd");
	        //getting all axioms    
	        Set<OWLAxiom> axSet= localOntology.getAxioms();
	        
	        IRI documentIRI = manager.getOntologyDocumentIRI(localOntology);
	        System.out.println(" from:"+documentIRI );

	    } catch (OWLOntologyCreationException e) {
	        e.printStackTrace();
	    }
	    
	    System.out.println("afd");

	}

}
	