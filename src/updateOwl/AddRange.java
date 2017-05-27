package updateOwl;
/*
 * 只能添加对象属性的值域，不能添加数据属性的值域。
 */
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.formats.RDFXMLDocumentFormat;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.util.DefaultPrefixManager;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by qieyin on 2017/1/23.
 */
public class AddRange {

    private OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
    private String filePath;
    private final String base = "http://qieyin/ontologies/child#";
    private OWLOntology ontology;
    private OWLDataFactory factory;
    private PrefixManager pm;
    // 定义类的初始结构
    public AddRange(String filePath) {
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
    public void addProDomain(String P, String value)throws Exception {
        // 定义对象IRI
        IRI PropIri = IRI.create(base + P);

        // 针对对象属性进行处理
        if (ontology.containsObjectPropertyInSignature(PropIri)){
            // 对象属性
            OWLObjectProperty owlProperty = factory.getOWLObjectProperty(PropIri);
            // 添加多值型的数据
            if(value.contains("or")){
                Set<OWLClass> allClass = new HashSet<OWLClass>();
                String[] someProperty = value.split("or");
                for (String s1 : someProperty){
                    IRI VClsIri = IRI.create(base + s1);
                    if (ontology.containsClassInSignature(VClsIri)) {
                        allClass.add(factory.getOWLClass(VClsIri));
                    } else {
                        System.out.println(s1 + "#概念不存在");
                    }
                }
                // 添加Domain
                if(!allClass.isEmpty()){
                    OWLObjectUnionOf unionOf = factory.getOWLObjectUnionOf(allClass);
                    OWLAxiom owlAxiom = factory.getOWLObjectPropertyRangeAxiom(owlProperty,unionOf);
                    AddAxiom addAx = new AddAxiom(ontology, owlAxiom);
                    System.out.println(P+":"+value+"#添加成功");
                    manager.applyChange(addAx);
                }
            }
            // 添加单值型的数据
            else{
                IRI VClsIri = IRI.create(base + value);
                OWLClass owlClass = factory.getOWLClass(VClsIri);
                OWLAxiom owlAxiom = factory.getOWLObjectPropertyRangeAxiom(owlProperty,owlClass);
                AddAxiom addAx = new AddAxiom(ontology, owlAxiom);
                System.out.println(P+":"+value+"#添加成功");
                manager.applyChange(addAx);
            }
        }
        // 针对数据属性进行添加值域
        else if (ontology.containsDataPropertyInSignature(PropIri)) {
            // 数据属性
            OWLDataProperty owlProperty = factory.getOWLDataProperty(PropIri);

        }
        else{
            System.out.println(P + "#该属性不存在");
        }

    }
    public static void main(String[] args)throws Exception{
        AddRange d = new AddRange("OWLAPI/datas/qieyinChild.owl");

        d.addProDomain("食物对象","动物or植物");

        d.saveOnt("OWLAPI/datas/ontowl.owl");
    }
}
