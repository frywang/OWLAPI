package updateOwl;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.formats.RDFXMLDocumentFormat;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.util.DefaultPrefixManager;
import java.io.File;
import java.util.HashSet;
import java.util.Set;

/**
 * 对本体添加三元组，此时的三元组是限定型的三元组
 * 例如：牛and颜色值-红色&黑色or咬对象-猪or食物对象-河马
 * 这样用于处理两个三元组问题，如“狗-包含-腿，腿-颜色值-白色”
 * main函数给出具体实例演示
 */
public class AddNestedTripple {
    private OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
    private String filePath;
    private final String base = "http://qieyin/ontologies/child#";
    private OWLOntology ontology;
    private OWLDataFactory factory;
    private PrefixManager pm;
    // 定义类的初始结构
    public AddNestedTripple(String filePath) {
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
        if(ontology.containsClassInSignature(ClsIri)){
            // 定义概念
            OWLClass owlClass = factory.getOWLClass(ClsIri);

            // 这个采用先删后添
            if (!ontology.getSubClassAxiomsForSubClass(owlClass).isEmpty()) {
                for (OWLSubClassOfAxiom ax : ontology.getSubClassAxiomsForSubClass(owlClass)) {
                    if (ax.toString().contains(P)){
                        manager.removeAxiom(ontology, ax);
                        //System.out.println(P+"#原概念中存在#删除");
                    }
                }
            }

            // 针对属性是对象属性进行处理
            if (ontology.containsObjectPropertyInSignature(PropIri)){
                // 数据属性
                OWLObjectProperty owlProperty = factory.getOWLObjectProperty(PropIri);
                if(value.contains("and") && value.contains("-")){

                    Set<OWLClassExpression> allClass = new HashSet<OWLClassExpression>();

                    String[] someProperty = value.split("and");
                    // 对部件处理
                    IRI VCls01Iri = IRI.create(base + someProperty[0]);
                    allClass.add(factory.getOWLClass(VCls01Iri));

                    // 对限制做处理
                    Set<OWLClassExpression> allRestiction01 = new HashSet<OWLClassExpression>();
                    String[] restrict = someProperty[1].split("or");
                    for (int i = 0 ; i < restrict.length ; i++) {
                        String[] pv = restrict[i].split("-");
                        IRI pIri = IRI.create(base + pv[0]);
                        if (ontology.containsObjectPropertyInSignature(pIri)){
                            OWLObjectProperty Pro01 = factory.getOWLObjectProperty(pIri);
                            System.out.println(pv[1]);
                            if (pv[1].contains("or")){
                                String[] minMax = pv[1].split("or");
                                Set<OWLClass> name = new HashSet<OWLClass>();
                                for (int h = 0 ; h < minMax.length ; h++) {
                                    IRI VClsIri = IRI.create(base + minMax[h]);

                                    if (ontology.containsClassInSignature(VClsIri)) {
                                        name.add(factory.getOWLClass(VClsIri));
                                    } else {
                                        System.out.println(E+"#"+ P +"#" +value + "\t"+ minMax[h] + "#概念不存在");
                                    }
                                }
                                if(!name.isEmpty()) {
                                    OWLClassExpression sd = factory.getOWLObjectUnionOf(name);
                                    OWLObjectRestriction EqualToD = factory.getOWLObjectSomeValuesFrom(Pro01, sd);
                                    allRestiction01.add(EqualToD);
                                }
                            }else {
                                IRI vIri = IRI.create(base + pv[1]);
                                OWLClass Cls01 = factory.getOWLClass(vIri);
                                OWLObjectRestriction addPValue = factory.getOWLObjectSomeValuesFrom(Pro01,Cls01);
                                allRestiction01.add(addPValue);
                            }
                        }
                    }
                    if (!allRestiction01.isEmpty()){
                        OWLObjectUnionOf owlObjectUnionOf = factory.getOWLObjectUnionOf(allRestiction01);
                        allClass.add(owlObjectUnionOf);
                        OWLClassExpression sd = factory.getOWLObjectIntersectionOf(allClass);
                        OWLClassExpression EqualToD = factory.getOWLObjectSomeValuesFrom(owlProperty,sd);
                        OWLSubClassOfAxiom ax = factory.getOWLSubClassOfAxiom(owlClass, EqualToD);
                        AddAxiom addAx = new AddAxiom(ontology, ax);
                        System.out.println(E+":"+P+":"+value+"#添加成功");
                        manager.applyChange(addAx);
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
        AddNestedTripple d = new AddNestedTripple("datas/qieyinChild.owl");
//      添加的格式如“天牛#包含#翅膀and颜色值-枯黄色”
        d.addThreeTuple("儿子","气味值","牛and颜色值-红色&黑色or咬对象-猪or食物对象-河马");

        d.saveOnt("datas/ontowl.owl");
    }
}
