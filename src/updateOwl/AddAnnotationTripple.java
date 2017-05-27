package updateOwl;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.formats.RDFXMLDocumentFormat;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.util.DefaultPrefixManager;
import org.semanticweb.owlapi.vocab.OWL2Datatype;

import java.io.File;

/**
 * Created by qieyin on 2017/1/23.
 * 对于owl自带的本体文件，调用OWLAPI自动实现。对于人工添加的，人工定义实现
 * -现在实现的有imp,label和nickname.
 */
public class AddAnnotationTripple {
    private OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
    private String filePath;
    private final String base = "http://qieyin/ontologies/child#";
    private OWLOntology ontology;
    private OWLDataFactory factory;
    private PrefixManager pm;
    // 定义类的初始结构
    public AddAnnotationTripple(String filePath) {
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


    public void addELabel(String E, String index, String value) throws Exception{
        // 定义对象IRI
        IRI ClsIri = IRI.create(base+E);
        // 确保父类存在
        if(ontology.containsClassInSignature(ClsIri)){

            if(index.equalsIgnoreCase("label")){
                OWLAnnotation LabelAnno = factory.getOWLAnnotation(factory.getRDFSLabel(), factory.getOWLLiteral(
                        value, "zh"));

                OWLAxiom ax = factory.getOWLAnnotationAssertionAxiom(ClsIri, LabelAnno);
                manager.applyChange(new AddAxiom(ontology, ax));
                System.out.println(value+"#"+index+"#添加成功");

            }else if(index.equalsIgnoreCase("comment")){
                OWLAnnotation LabelAnno = factory.getOWLAnnotation(factory.getRDFSComment(), factory.getOWLLiteral(
                        value, "zh"));

                OWLAxiom ax = factory.getOWLAnnotationAssertionAxiom(ClsIri, LabelAnno);
                manager.applyChange(new AddAxiom(ontology, ax));
                System.out.println(value+"#"+index+"#添加成功");
            }else if(index.equalsIgnoreCase("isDefinedBy")){
                OWLAnnotation LabelAnno = factory.getOWLAnnotation(factory.getRDFSIsDefinedBy(), factory.getOWLLiteral(
                        value, "zh"));

                OWLAxiom ax = factory.getOWLAnnotationAssertionAxiom(ClsIri, LabelAnno);
                manager.applyChange(new AddAxiom(ontology, ax));
                System.out.println(value+"#"+index+"#添加成功");

            }else if(index.equalsIgnoreCase("imp")){
                IRI annIri = IRI.create(base+index);
                if (ontology.containsAnnotationPropertyInSignature(annIri)){
                    OWLAnnotationProperty annotationProperty = factory.getOWLAnnotationProperty(annIri);
                    OWLAnnotation LabelAnno = factory.getOWLAnnotation(annotationProperty, factory.getOWLLiteral(
                            value, OWL2Datatype.XSD_POSITIVE_INTEGER));
                    OWLAxiom ax = factory.getOWLAnnotationAssertionAxiom(ClsIri, LabelAnno);
                    manager.applyChange(new AddAxiom(ontology, ax));
                    System.out.println(value+"#"+index+"#添加成功");
                }

            }else if (index.equalsIgnoreCase("nickname")){
                IRI annIri = IRI.create(base+index);
                if (ontology.containsAnnotationPropertyInSignature(annIri)){
                    OWLAnnotationProperty annotationProperty = factory.getOWLAnnotationProperty(annIri);
                    OWLAnnotation LabelAnno = factory.getOWLAnnotation(annotationProperty, factory.getOWLLiteral(
                            value, "zh"));

                    OWLAxiom ax = factory.getOWLAnnotationAssertionAxiom(ClsIri, LabelAnno);
                    manager.applyChange(new AddAxiom(ontology, ax));
                    System.out.println(value+"#"+index+"#添加成功");

                }
            }




        }else {
            System.out.println(E+"#概念#不存在");
        }
    }

    public static void main(String[] args)throws Exception{
        AddAnnotationTripple d = new AddAnnotationTripple("OWLAPI/datas/qieyinChild.owl");

        d.addELabel("Test","imp","1");
        d.saveOnt("OWLAPI/datas/ontowl.owl");
    }
}
