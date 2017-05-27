package updateOwl;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.formats.RDFXMLDocumentFormat;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.util.DefaultPrefixManager;
import org.semanticweb.owlapi.util.OWLEntityRemover;

import java.io.File;

import static java.util.Collections.singleton;

/**
 * 对本体的删除的操作
 *   ：删除概念
 *   ：删除属性
 *   ：删除实例
 *   ：删除三元组
 *   main函数有具体的实例演示
 */
public class DeleteConceptEntityPropertyTripple {

    private OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
    private String filePath;
    private final String base = "http://qieyin/ontologies/child#";
    private OWLOntology ontology;
    private OWLDataFactory factory;
    private PrefixManager pm;
    // 定义类的初始结构
    public DeleteConceptEntityPropertyTripple(String filePath) {
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
    public void saveOnt(String filepath) throws Exception {
        // 保存model
        RDFXMLDocumentFormat rdfxmlFormat = new RDFXMLDocumentFormat();
        File file = new File(filepath);
        manager.saveOntology(ontology, rdfxmlFormat, IRI.create(file.toURI()));
    }

    /*
     * 针对提供的概念E-属性P,对其进行删除
     */
    public void delEP(String E, String P) throws Exception {
        // 定义对象IRI
        IRI ClsIri = IRI.create(base+E);
        IRI PropIri = IRI.create(base+P);

        // 判断是否有该概念
        if(ontology.containsClassInSignature(ClsIri)){
            // 获取概念对象
            OWLClass owlClass = factory.getOWLClass(ClsIri);
            OWLProperty owlProperty = null;
            // 判断数据属性中是否有该属性
            if (ontology.containsDataPropertyInSignature(PropIri)){
                // 获取---数据属性---删除
                owlProperty = factory.getOWLDataProperty(PropIri);
            }else if (ontology.containsObjectPropertyInSignature(PropIri)){
                // 获取对象属性
                owlProperty = factory.getOWLObjectProperty(PropIri);
            }else {
                System.out.println("没有该属性");
            }
            // 获取概念下对应属性的subclass
            if (!ontology.getSubClassAxiomsForSubClass(owlClass).isEmpty()) {
                for (OWLSubClassOfAxiom ax : ontology.getSubClassAxiomsForSubClass(owlClass)) {
                    OWLClassExpression clsEx = ax.getSuperClass();
                    if (clsEx.isAnonymous()) {
                        String type = clsEx.getClassExpressionType().getName();
                        if (type.equalsIgnoreCase("DataSomeValuesFrom")) {
                            OWLDataSomeValuesFrom someValuesFrom = (OWLDataSomeValuesFrom) clsEx;
                            if (someValuesFrom.getProperty().asOWLDataProperty().equals(owlProperty)) {
                                manager.removeAxiom(ontology, ax);
                                System.out.println(P+"#DataSomeValuesFrom#删除成功");
                            }
                        }else if(type.equalsIgnoreCase("DataHasValue")){
                            OWLDataHasValue hasValue = (OWLDataHasValue) clsEx;
                            if (hasValue.getProperty().asOWLDataProperty().equals(owlProperty)) {
                                manager.removeAxiom(ontology, ax);
                                System.out.println(P+"#DataHasValue#删除成功");
                            }
                        }else if(type.equalsIgnoreCase("ObjectHasValue")){
                            OWLObjectHasValue objectHasValue = (OWLObjectHasValue) clsEx;
                            if (objectHasValue.getProperty().asOWLObjectProperty().equals(owlProperty)) {
                                manager.removeAxiom(ontology, ax);
                                System.out.println(P+"#ObjectHasValue#删除成功");
                            }
                        }else if (type.equalsIgnoreCase("ObjectSomeValuesFrom")){
                            OWLObjectSomeValuesFrom someValuesFrom = (OWLObjectSomeValuesFrom) clsEx;
                            if (someValuesFrom.getProperty().asOWLObjectProperty().equals(owlProperty)) {
                                manager.removeAxiom(ontology, ax);
                                System.out.println(P+"#ObjectHasValue#删除成功");
                            }
                        }else if (type.equalsIgnoreCase("ObjectAllValuesFrom")){
                            OWLObjectAllValuesFrom allValuesFrom = (OWLObjectAllValuesFrom) clsEx;
                            if (allValuesFrom.getProperty().asOWLObjectProperty().equals(owlProperty)) {
                                manager.removeAxiom(ontology, ax);
                                System.out.println(P+"#ObjectHasValue#删除成功");
                            }
                        }
                    }
                }
            }
        }else {
            System.out.println("没有该概念");
        }
    }

    /*
     * 针对概念E 进行删除
     */
    public void delE(String E) throws Exception{
        // 定义对象IRI
        IRI ClsIri = IRI.create(base+E);

        // 判断是否有该概念
        if(ontology.containsClassInSignature(ClsIri)){
            OWLClass owlClass = factory.getOWLClass(ClsIri);
            // 定义删除对象
            OWLEntityRemover remover = new OWLEntityRemover(singleton(ontology));
            owlClass.accept(remover);
            manager.applyChanges(remover.getChanges());
            System.out.println(E+"#概念"+"#删除成功");
        }else {
            System.out.println(E+"#概念#不存在");
        }
    }

    /*
     * 针对属性P 进行删除
     */
    public void delP(String P) throws Exception{
        // 定义对象IRI
        IRI PropIri = IRI.create(base + P);
        // 定义删除对象
        OWLEntityRemover remover = new OWLEntityRemover(singleton(ontology));
        // 判断是否有该属性
        if (ontology.containsDataPropertyInSignature(PropIri)){
            // 获取---数据属性---删除
            OWLDataProperty owlProperty = factory.getOWLDataProperty(PropIri);
            owlProperty.accept(remover);
            manager.applyChanges(remover.getChanges());
            System.out.println(P+"#数据属性"+"#删除成功");

        }else if (ontology.containsObjectPropertyInSignature(PropIri)){
            // 获取---对象属性---删除
            OWLObjectProperty owlProperty = factory.getOWLObjectProperty(PropIri);
            owlProperty.accept(remover);
            manager.applyChanges(remover.getChanges());
            System.out.println(P+"#概念属性"+"#删除成功");
        }else {
            System.out.println(P+"#属性#不存在");
        }
    }

    /*
     * 针对个体I 进行删除
     */
    public void delI(String I) throws Exception{
        // 定义对象IRI
        IRI IndIri = IRI.create(base + I);
        // 定义删除对象
        OWLEntityRemover remover = new OWLEntityRemover(singleton(ontology));
        // 判断是否存在该实例
        if (ontology.containsIndividualInSignature(IndIri)){
            OWLNamedIndividual owlNamedIndividual = factory.getOWLNamedIndividual(IndIri);
            owlNamedIndividual.accept(remover);
            manager.applyChanges(remover.getChanges());
            System.out.println(I+"#实例"+"#删除成功");
        }else {
            System.out.println(I+"#实例#不存在");
        }
    }


    public static void main(String[] args)throws Exception{
        DeleteConceptEntityPropertyTripple d = new DeleteConceptEntityPropertyTripple("OWLAPI/datas/qieyinChild.owl");
        d.delEP("Test", "价格值");
        //d.delE("Test");
        //d.delP("长度值");
        //d.delI("孙悟空");
        d.saveOnt("OWLAPI/datas/ontowl.owl");
    }
}
