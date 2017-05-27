package workOwl;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.formats.RDFXMLDocumentFormat;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.util.DefaultPrefixManager;

import java.io.File;
import java.util.ArrayList;
import java.util.Set;

/**
 * Created by qieyin on 2017/1/6.
 */
public class readowl {
    private OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
    private String filePath;
    private final String base = "http://www.semanticweb.org/fry/ontologies/2016/10/untitled-ontology-82#";
    private OWLOntology ontology;
    private OWLDataFactory factory;
    private PrefixManager pm;

    // 定义类的初始结构
    public readowl(String filePath) {
        this.filePath = filePath;
        try {
            this.ontology = manager.loadOntologyFromOntologyDocument(new File(filePath));
        } catch (OWLOntologyCreationException e) {
            e.printStackTrace();
        }
        this.factory = manager.getOWLDataFactory();
        this.pm = new DefaultPrefixManager(null, null, base);
    }

    // 本体保存
    public void saveOnt() throws Exception {
        // 保存model
        RDFXMLDocumentFormat rdfxmlFormat = new RDFXMLDocumentFormat();
        File file = new File("D:/JAVA_CODE/OWLAPI/datas/owlapi_saving.owl");
        manager.saveOntology(ontology, rdfxmlFormat, IRI.create(file.toURI()));
    }

    public ArrayList<String> queryE(String E) throws Exception{
        // 定义对象IRI
        IRI ClsIri = IRI.create(base+E);
        ArrayList<String> result = new ArrayList<String>();

        if(ontology.containsClassInSignature(ClsIri)){
            OWLClass owlClass = factory.getOWLClass(ClsIri);
            if (!ontology.getSubClassAxiomsForSubClass(owlClass).isEmpty()) {
                for (OWLSubClassOfAxiom ax : ontology.getSubClassAxiomsForSubClass(owlClass)) {
                    OWLClassExpression clsEx = ax.getSuperClass();
                    if (clsEx.isAnonymous()) {
                        String type = clsEx.getClassExpressionType().getName();
                        if (type.equalsIgnoreCase("ObjectSomeValuesFrom")){
                            OWLObjectSomeValuesFrom someValuesFrom = (OWLObjectSomeValuesFrom) clsEx;
                            //OWLObjectProperty owlDataProperty = someValuesFrom.getProperty().asOWLObjectProperty();
                            OWLClassExpression range = someValuesFrom.getFiller();
                            String mm = range.getClassExpressionType().getName();
                            if (mm.equals("ObjectIntersectionOf")){
                                //System.out.println(range);
                                OWLObjectIntersectionOf pp = (OWLObjectIntersectionOf)range;
                                System.out.println(pp);
                            }
                            //System.out.println(range);
                        }
                        //System.out.println(clsEx);
                    }
                }
            }
        }else {
            System.out.println(E+"#该类不存在");
        }
        return result;
    }

    public static void main(String[] args)throws Exception{

        readowl d = new readowl("OWLAPI/datas/qieyinPropertiesnews.owl");
        ArrayList<String> re = d.queryE("test");
        for(String s : re){
            System.out.println(s);
        }
    }
}
