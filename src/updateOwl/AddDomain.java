package updateOwl;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.formats.RDFXMLDocumentFormat;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.util.DefaultPrefixManager;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

/**
 * 该部分实现对本体的添加属性定义域
 * addProDomain(String P, String value)为添加函数
 * P：属性
 * value ：定义域，value 格式为 "动物 or植物"
 * main函数给出实例演示
 */
public class AddDomain {
    private OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
    private String filePath;
    private final String base = "http://qieyin/ontologies/child#";
    private OWLOntology ontology;
    private OWLDataFactory factory;
    private PrefixManager pm;
    // 定义类的初始结构
    public AddDomain(String filePath) {
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
                    OWLAxiom owlAxiom = factory.getOWLObjectPropertyDomainAxiom(owlProperty,unionOf);
                    AddAxiom addAx = new AddAxiom(ontology, owlAxiom);
                    System.out.println(P+":"+value+"#添加成功");
                    manager.applyChange(addAx);
                }
            }
            // 添加单值型的数据
            else{
                IRI VClsIri = IRI.create(base + value);
                OWLClass owlClass = factory.getOWLClass(VClsIri);
                OWLAxiom owlAxiom = factory.getOWLObjectPropertyDomainAxiom(owlProperty,owlClass);
                AddAxiom addAx = new AddAxiom(ontology, owlAxiom);
                System.out.println(P+":"+value+"#添加成功");
                manager.applyChange(addAx);
            }
        }
        // 针对数据属性进行添加定义域
        else if (ontology.containsDataPropertyInSignature(PropIri)) {
            // 数据属性
            OWLDataProperty owlProperty = factory.getOWLDataProperty(PropIri);
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
                    OWLAxiom owlAxiom = factory.getOWLDataPropertyDomainAxiom(owlProperty,unionOf);
                    AddAxiom addAx = new AddAxiom(ontology, owlAxiom);
                    System.out.println(P+":"+value+"#添加成功");
                    manager.applyChange(addAx);
                }
            // 添加单值型的数据
            }else {
                IRI VClsIri = IRI.create(base + value);
                OWLClass owlClass = factory.getOWLClass(VClsIri);
                OWLAxiom owlAxiom = factory.getOWLDataPropertyDomainAxiom(owlProperty,owlClass);
                AddAxiom addAx = new AddAxiom(ontology, owlAxiom);
                System.out.println(P+":"+value+"#添加成功");
                manager.applyChange(addAx);
            }
        }
        else{
            System.out.println(P + "#该属性不存在");
        }

    }
    public static void main(String[] args)throws Exception{
        AddDomain d = new AddDomain("./datas/qieyinChild.owl");

        d.addProDomain("食物对象","动物or植物");

        d.saveOnt("./datas/qwe.owl");
    }


}
