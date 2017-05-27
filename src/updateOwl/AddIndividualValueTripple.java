package updateOwl;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.formats.RDFXMLDocumentFormat;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.util.DefaultPrefixManager;

import java.io.File;

/**
 * 添加三元组的属性值为实例的情况，但是三元组的实例之间有or或and关系的情况下不能添加。
 */
public class AddIndividualValueTripple {

    private OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
    private String filePath;
    private final String base = "http://qieyin/ontologies/child#";
    private OWLOntology ontology;
    private OWLDataFactory factory;
    private PrefixManager pm;
    // 定义类的初始结构
    public AddIndividualValueTripple(String filePath) {
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
    public void saveOnt(String filePath) throws Exception {
        // 保存model
        RDFXMLDocumentFormat rdfxmlFormat = new RDFXMLDocumentFormat();
        File file = new File(filePath);
        manager.saveOntology(ontology, rdfxmlFormat, IRI.create(file.toURI()));
    }


    public void addThreeTuple(String E, String P, String value)throws Exception {
        // 定义对象IRI
        IRI ClsIri = IRI.create(base + E);
        IRI PropIri = IRI.create(base + P);
        if(ontology.containsClassInSignature(ClsIri)) {
            // 定义概念
            OWLClass owlClass = factory.getOWLClass(ClsIri);

            // 这个采用先删后添
            if (!ontology.getSubClassAxiomsForSubClass(owlClass).isEmpty()) {
                for (OWLSubClassOfAxiom ax : ontology.getSubClassAxiomsForSubClass(owlClass)) {
                    if (ax.toString().contains(P)) {
                        manager.removeAxiom(ontology, ax);
                        System.out.println(P + "#原概念中存在#删除");
                    }
                }
            }
            if (ontology.containsObjectPropertyInSignature(PropIri)){
                OWLObjectProperty owlProperty = factory.getOWLObjectProperty(PropIri);
                // 值对象
                IRI VindIri = IRI.create(base+value);
                if (ontology.containsIndividualInSignature(VindIri)){
                    OWLNamedIndividual owlindV = factory.getOWLNamedIndividual(VindIri);
                    OWLObjectRestriction addPValue = factory.getOWLObjectHasValue(owlProperty,owlindV);
                    OWLSubClassOfAxiom ax = factory.getOWLSubClassOfAxiom(owlClass, addPValue);
                    AddAxiom addAx = new AddAxiom(ontology, ax);
                    manager.applyChange(addAx);
                    System.out.println(E+":"+P+":"+value+"#添加成功");
                }else{
                    System.out.println(value+"#实例#不存在");
                }
            } else{
                System.out.println(P+"#属性#不存在");
            }

        }else {
            System.out.println(E + "#概念#不存在");
        }


    }
    public static void main(String[] args)throws Exception{
        AddIndividualValueTripple d = new AddIndividualValueTripple("OWLAPI/datas/qieyinChild.owl");

        d.addThreeTuple("Test","食物对象","a");
        d.saveOnt("OWLAPI/datas/ontowl.owl");
    }


}
