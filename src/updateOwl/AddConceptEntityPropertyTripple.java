package updateOwl;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.formats.RDFXMLDocumentFormat;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.util.DefaultPrefixManager;
import org.semanticweb.owlapi.util.OWLEntityRemover;
import org.semanticweb.owlapi.vocab.OWL2Datatype;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static java.util.Collections.singleton;
import static org.semanticweb.owlapi.vocab.OWLFacet.MAX_INCLUSIVE;
import static org.semanticweb.owlapi.vocab.OWLFacet.MIN_INCLUSIVE;

/**
 * 对本体库的添加
 *     ：添加概念，包括概念的层级结构。
 *     ：添加实例
 *     ：添加属性，包括对象属性和数据属性，不包括注释属性。
 *     ：添加三元组(嵌套三元组在AddNestedTripple里添加；注释三元组在jenaAPI下添加，但不包括人工新建的注释属性。
 *       ——属性值为individual类型的三元组在AddIndividualValueTripple下添加)
 * main函数给出实例演示
 */
public class AddConceptEntityPropertyTripple {
    private OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
    private String filePath;
    private final String base = "http://qieyin/ontologies/child#";
    private OWLOntology ontology;
    private OWLDataFactory factory;
    private PrefixManager pm;
    // 定义类的初始结构
    public AddConceptEntityPropertyTripple(String filePath) {
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

    /*
     * 针对提供的概念E,对其进行添加
     */
    public void addE(String E, String SupE) throws Exception{
        // 定义对象IRI
        IRI ClsIri = IRI.create(base+E);
        IRI SupClsIri = IRI.create(base+SupE);
        // 确保父类存在
        if(ontology.containsClassInSignature(SupClsIri)){
            OWLClass supOwlClass = factory.getOWLClass(SupClsIri);
            // 判断是否有该概念
            if(!ontology.containsClassInSignature(ClsIri)){
                OWLClass owlClass = factory.getOWLClass(ClsIri);
                // 创建公理axiom
                OWLAxiom axiom = factory.getOWLSubClassOfAxiom(owlClass, supOwlClass);
                // 保存操作
                AddAxiom addAxiom = new AddAxiom(ontology, axiom);
                manager.applyChange(addAxiom);
                System.out.println(E+"#概念"+"#添加成功");
            }else {
                System.out.println(E+"#概念#已存在");
            }
        }else {
            System.out.println(SupE+"#父类概念#不存在");
        }
    }

    /*
     * 针对提供的属性P,对其进行添加
     * type: data & object
     */
    public void addP(String P, String type) throws Exception{
        // 定义对象IRI
        IRI PropIri = IRI.create(base + P);
        // 判断是否有该属性
        if (!ontology.containsDataPropertyInSignature(PropIri)
                && !ontology.containsObjectPropertyInSignature(PropIri)){
            if (type.equals("data")){
                OWLDataProperty owlProperty = factory.getOWLDataProperty(PropIri);
                OWLDeclarationAxiom declarationAxiom = factory.getOWLDeclarationAxiom(owlProperty);
                manager.addAxiom(ontology, declarationAxiom);
                System.out.println(P+"#DataProperty"+"#添加成功");
            }else if(type.equals("object")){
                OWLObjectProperty owlProperty = factory.getOWLObjectProperty(PropIri);
                OWLDeclarationAxiom declarationAxiom = factory.getOWLDeclarationAxiom(owlProperty);
                manager.addAxiom(ontology, declarationAxiom);
                System.out.println(P+"#ObjectProperty"+"#添加成功");
            }else {
                System.out.println("数据类型错误");
            }
        }else {
            System.out.println(P+"#属性#已存在");
        }
    }

    /*
     * 针对实例I 进行添加
     */
    public void addI(String I, String E) throws Exception{
        // 定义对象IRI
        IRI IndIri = IRI.create(base + I);
        IRI ClsIri = IRI.create(base+E);
        if(ontology.containsClassInSignature(ClsIri)){
            OWLClass owlClass = factory.getOWLClass(ClsIri);
            if (!ontology.containsIndividualInSignature(IndIri)){
                OWLNamedIndividual ind = factory.getOWLNamedIndividual(IndIri);
                OWLClassAssertionAxiom axiom = factory.getOWLClassAssertionAxiom(owlClass, ind);
                // 保存操作
                AddAxiom addAxiom = new AddAxiom(ontology, axiom);
                manager.applyChange(addAxiom);
                System.out.println( I+ "#" + E+ "#" + "#添加成功");
            }else {
                System.out.println(I+"#实例#已存在");
            }
        }else{
            System.out.println(E+"#概念#不存在");
        }
    }

    /*
     * 针对subclass中加入restriction
     */
    public void addThreeTuple(String E, String P, String value)throws Exception {
        // 定义对象IRI
        IRI ClsIri = IRI.create(base + E);
        
        IRI PropIri = IRI.create(base + P);
        if(ontology.containsClassInSignature(ClsIri)){
            // 定义概念
            OWLClass owlClass = factory.getOWLClass(ClsIri);

            // 这个采用先删后添
            if (!ontology.getSubClassAxiomsForSubClass(owlClass).isEmpty()) {
                for (OWLSubClassOfAxiom ax : ontology.getSubClassAxiomsForSubClass(owlClass)) {
                    if (ax.toString().contains(P)){
                        manager.removeAxiom(ontology, ax);
                        System.out.println(P+"#原概念中存在#删除");
                    }
                }
            }

            // 针对属性是数据属性进行对应处理
            if (ontology.containsDataPropertyInSignature(PropIri)) {
                // 数据属性
                OWLDataProperty owlProperty = factory.getOWLDataProperty(PropIri);
                // value 中包含"-"的进行处理，即处理的是范围值
                if (value.contains("-")){
                    String[] minMax = value.split("-");
                    // 定义数据类型
                    OWLDatatype doubleDatatype = factory.getDoubleOWLDatatype();
                    // 设置数据的最小值
                    OWLLiteral doubleMin = factory.getOWLLiteral(Double.parseDouble(minMax[0]));
                    OWLDataRange douMin = factory.getOWLDatatypeRestriction(doubleDatatype, MIN_INCLUSIVE, doubleMin);
                    // 设置数据的最大值
                    OWLLiteral doubleMax = factory.getOWLLiteral(Double.parseDouble(minMax[1]));
                    OWLDataRange douMAX = factory.getOWLDatatypeRestriction(doubleDatatype, MAX_INCLUSIVE, doubleMax);
                    // 封装
                    OWLDataIntersectionOf sd = factory.getOWLDataIntersectionOf(douMin,douMAX);
                    OWLClassExpression EqualToD = factory.getOWLDataSomeValuesFrom(owlProperty, sd);
                    OWLSubClassOfAxiom axiom = factory.getOWLSubClassOfAxiom(owlClass, EqualToD);
                    manager.applyChange(new AddAxiom(ontology, axiom));

                }
                // value 中包含数值型的进行处理，即这里采用统一的double类型
                else if (value.matches("^[-+]?(([0-9]+)([.]([0-9]+))?|([.]([0-9]+))?)$")) {
                    // 设置数据属性为double
                    OWLDatatype doubleDatatype = factory.getOWLDatatype(OWL2Datatype.XSD_DOUBLE.getIRI());
                    OWLLiteral literal = factory.getOWLLiteral(value, doubleDatatype);
                    // 封装
                    OWLClassExpression addPValue = factory.getOWLDataHasValue(owlProperty, literal);
                    OWLSubClassOfAxiom ax = factory.getOWLSubClassOfAxiom(owlClass, addPValue);
                    AddAxiom addAx = new AddAxiom(ontology, ax);
                    manager.applyChange(addAx);
                }
                // value 中true或false字符串
                else if (value.equalsIgnoreCase("true") || value.equalsIgnoreCase("false")) {
                    // 设置数据属性为double
                    OWLDatatype booleanDatatype = factory.getOWLDatatype(OWL2Datatype.XSD_BOOLEAN.getIRI());
                    OWLLiteral literal = factory.getOWLLiteral(value, booleanDatatype);
                    // 设置Restriction
                    OWLClassExpression addPValue = factory.getOWLDataHasValue(owlProperty, literal);
                    // 将Restriction设置为父类
                    OWLSubClassOfAxiom ax = factory.getOWLSubClassOfAxiom(owlClass, addPValue);
                    AddAxiom addAx = new AddAxiom(ontology, ax);
                    manager.applyChange(addAx);
                }
                // value 其他类型都作为字符串加入
                else {
                    // 设置数据的值
                    OWLLiteral literal = factory.getOWLLiteral(value);
                    // 设置Restriction
                    OWLClassExpression addPValue = factory.getOWLDataHasValue(owlProperty, literal);
                    // 将Restriction设置为父类
                    OWLSubClassOfAxiom ax = factory.getOWLSubClassOfAxiom(owlClass, addPValue);
                    AddAxiom addAx = new AddAxiom(ontology, ax);
                    manager.applyChange(addAx);
                }
                System.out.println(E + "#" + P + "#" + value + "#DataProperty#添加成功");

            // 针对属性是对象属性进行处理
            }else if (ontology.containsObjectPropertyInSignature(PropIri)){
                // 数据属性
                OWLObjectProperty owlProperty = factory.getOWLObjectProperty(PropIri);

                if(value.contains("and") && value.contains("or")){
                    String[] minMax = value.split("and");
                    Set<OWLClassExpression> name = new HashSet<OWLClassExpression>();
                    for (int h = 0 ; h < minMax.length ; h++) {
                        if (minMax[h].contains("or")){
                            String[] minMax01 = minMax[h].split("or");
                            Set<OWLClass> name01 = new HashSet<OWLClass>();
                            for (int h1 = 0 ; h1 < minMax.length ; h1++) {

                                IRI VClsIri = IRI.create(base + minMax01[h1]);

                                if (ontology.containsClassInSignature(VClsIri)) {
                                    name01.add(factory.getOWLClass(VClsIri));

                                } else {
                                    System.out.println(E+"#"+ P +"#" +value + "\t"+ minMax01[h1] + "#概念不存在");
                                }
                            }
                            if(!name01.isEmpty()){
                                OWLClassExpression sd = factory.getOWLObjectUnionOf(name01);
                                name.add(sd);
                            }
                        }
                        else {
                            IRI VClsIri = IRI.create(base + minMax[h]);

                            if (ontology.containsClassInSignature(VClsIri)) {
                                name.add(factory.getOWLClass(VClsIri));
                            } else {
                                System.out.println(E+"#"+ P +"#" +value + "\t"+ minMax[h] + "#概念不存在");
                            }
                        }
                    }
                    if(!name.isEmpty()){
                        OWLClassExpression sd = factory.getOWLObjectIntersectionOf(name);
                        //System.out.println(sd);
                        OWLClassExpression EqualToD = factory.getOWLObjectSomeValuesFrom(owlProperty,sd);
                        OWLSubClassOfAxiom ax = factory.getOWLSubClassOfAxiom(owlClass, EqualToD);
                        AddAxiom addAx = new AddAxiom(ontology, ax);
                        System.out.println(E+":"+P+":"+value+"#添加成功");
                        manager.applyChange(addAx);
                    }
                }


                else if(value.contains("and")){
                    String[] minMax = value.split("and");
                    Set<OWLClass> name = new HashSet<OWLClass>();
                    for (int h = 0 ; h < minMax.length ; h++) {

                        //List<OWLClassExpression> name = new ArrayList<OWLClassExpression>();
                        IRI VClsIri = IRI.create(base + minMax[h]);

                        if (ontology.containsClassInSignature(VClsIri)) {
                            name.add(factory.getOWLClass(VClsIri));

                        } else {
                            System.out.println(E+"#"+ P +"#" +value + "\t"+ minMax[h] + "#概念不存在");
                        }
                    }
                    if(!name.isEmpty()){
                        OWLClassExpression sd = factory.getOWLObjectIntersectionOf(name);
                        //System.out.println(sd);
                        OWLClassExpression EqualToD = factory.getOWLObjectSomeValuesFrom(owlProperty,sd);
                        OWLSubClassOfAxiom ax = factory.getOWLSubClassOfAxiom(owlClass, EqualToD);
                        AddAxiom addAx = new AddAxiom(ontology, ax);
                        System.out.println(E+":"+P+":"+value+"#添加成功");
                        manager.applyChange(addAx);
                    }
                }
                else if (value.contains("or")){
                    String[] minMax = value.split("or");
                    Set<OWLClass> name = new HashSet<OWLClass>();
                    for (int h = 0 ; h < minMax.length ; h++) {
                        //List<OWLClassExpression> name = new ArrayList<OWLClassExpression>();
                        IRI VClsIri = IRI.create(base + minMax[h]);

                        if (ontology.containsClassInSignature(VClsIri)) {
                            name.add(factory.getOWLClass(VClsIri));

                        } else {
                            System.out.println(E+"#"+ P +"#" +value + "\t"+ minMax[h] + "#概念不存在");
                        }
                    }
                    if(!name.isEmpty()){
                        OWLObjectUnionOf owlObjectUnionOf = factory.getOWLObjectUnionOf(name);

                        OWLClassExpression EqualToD = factory.getOWLObjectSomeValuesFrom(owlProperty,owlObjectUnionOf);
                        OWLSubClassOfAxiom ax = factory.getOWLSubClassOfAxiom(owlClass, EqualToD);
                        AddAxiom addAx = new AddAxiom(ontology, ax);
                        System.out.println(E+":"+P+":"+value+"#添加成功");
                        manager.applyChange(addAx);
                    }
                }
                else{
                    // 值对象
                    IRI VClsIri = IRI.create(base+value);
                    if (ontology.containsClassInSignature(VClsIri)){
                        OWLClass owlClassV = factory.getOWLClass(VClsIri);
                        OWLObjectRestriction addPValue = factory.getOWLObjectSomeValuesFrom(owlProperty,owlClassV);
                        OWLSubClassOfAxiom ax = factory.getOWLSubClassOfAxiom(owlClass, addPValue);
                        AddAxiom addAx = new AddAxiom(ontology, ax);
                        manager.applyChange(addAx);
                        System.out.println(E+":"+P+":"+value+"#添加成功");
                    }else{
                        System.out.println(E+"#"+ P +"#" +value + "\t"+ value+"#概念#不存在");
                    }
                }
            }else{
                System.out.println(P+"#属性#不存在");
            }

        }else {
            System.out.println(E+"#"+ P +"#" +value + "\t"+ E + "#不存在");
        }
    }
    public static void main(String[] args)throws Exception{
        AddConceptEntityPropertyTripple d = new AddConceptEntityPropertyTripple("datas/qieyinChild.owl");
        //d.delEP("河马", "价格值");
        //d.addE("te","Test");
        //d.addP("花心值", "data");
        //d.addI("子墨","Test");
        d.addThreeTuple("Test","颜色值","狗and河马or海牛");
        d.saveOnt("datas/ontowl.owl");
    }

}
