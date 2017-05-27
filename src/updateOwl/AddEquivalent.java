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
public class AddEquivalent{
    private OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
    private String filePath;
    private final String base = "http://qieyin/ontologies/child#";
    private OWLOntology ontology;
    private OWLDataFactory factory;
    private PrefixManager pm;
    // 定义类的初始结构
    public AddEquivalent(String filePath) {
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
    public void addEquivalntThreeTuple(String E, String P, String value)throws Exception {
        // 定义对象IRI，第一个类的IRI
        IRI ClsIri = IRI.create(base + E);
        // 定义属性IRI，第一个属性的IRI
        IRI PropIri = IRI.create(base + P);
        //查看知识库是否包含需要添加属性组的概念
        if(ontology.containsClassInSignature(ClsIri)){
            // 定义概念
            OWLClass owlClass = factory.getOWLClass(ClsIri);
            System.out.println(ontology.getSubClassAxiomsForSubClass(owlClass));
            System.out.println(ontology.getEquivalentClassesAxioms(owlClass));

            // 这个采用先删后添
            if (!ontology.getEquivalentClassesAxioms(owlClass).isEmpty()) {
                for (OWLEquivalentClassesAxiom ax : ontology.getEquivalentClassesAxioms(owlClass)) {
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
                    OWLEquivalentClassesAxiom axiom = factory.getOWLEquivalentClassesAxiom(owlClass, EqualToD);
                    
                    manager.applyChange(new AddAxiom(ontology, axiom));

                }
                // value 中包含数值型的进行处理，即这里采用统一的double类型
                else if (value.matches("^[-+]?(([0-9]+)([.]([0-9]+))?|([.]([0-9]+))?)$")) {
                    // 设置数据属性为double
                    OWLDatatype doubleDatatype = factory.getOWLDatatype(OWL2Datatype.XSD_DOUBLE.getIRI());
                    OWLLiteral literal = factory.getOWLLiteral(value, doubleDatatype);
                    // 封装
                    OWLClassExpression addPValue = factory.getOWLDataHasValue(owlProperty, literal);
                    OWLEquivalentClassesAxiom ax = factory.getOWLEquivalentClassesAxiom(owlClass, addPValue);
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
                    OWLEquivalentClassesAxiom ax = factory.getOWLEquivalentClassesAxiom(owlClass, addPValue);
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
                    OWLEquivalentClassesAxiom ax = factory.getOWLEquivalentClassesAxiom(owlClass, addPValue);
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
                        OWLEquivalentClassesAxiom ax = factory.getOWLEquivalentClassesAxiom(owlClass, EqualToD);
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
                        OWLEquivalentClassesAxiom ax = factory.getOWLEquivalentClassesAxiom(owlClass, EqualToD);
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
                        OWLEquivalentClassesAxiom ax = factory.getOWLEquivalentClassesAxiom(owlClass, EqualToD);
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
                        OWLEquivalentClassesAxiom ax = factory.getOWLEquivalentClassesAxiom(owlClass, addPValue);
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
    
    
    public void addEquivalentNestedThreeTuple(String E, String P, String value)throws Exception {
        // 定义对象IRI，第一个类的IRI
        IRI ClsIri = IRI.create(base + E);
        // 定义属性IRI，第一个属性的IRI
        IRI PropIri = IRI.create(base + P);
        //查看知识库是否包含需要添加属性组的概念
        if(ontology.containsClassInSignature(ClsIri)){
            // 确定要添加属性组的概念
            OWLClass owlClass = factory.getOWLClass(ClsIri);

            // 这个采用先删后添，如果已有EquivalentClass就要先删除
            if (!ontology.getEquivalentClassesAxioms(owlClass).isEmpty()) {
                for (OWLEquivalentClassesAxiom ax : ontology.getEquivalentClassesAxioms(owlClass)) {
                    if (ax.toString().contains(P)){
                        manager.removeAxiom(ontology, ax);
                        //System.out.println(P+"#原概念中存在#删除");
                    }
                }
            }

            // 针对属性是对象属性进行处理
            //首先判断需要添加的属性是否存在
            if (ontology.containsObjectPropertyInSignature(PropIri)){
                // 数据属性
                OWLObjectProperty owlProperty = factory.getOWLObjectProperty(PropIri);
                //System.out.println("这是"+owlProperty.toString());
                //判断需要添加的属性值的内容，and和-用来区分逻辑
                if(value.contains("and") && value.contains("-")){
                    // 建立一个set，set的目的是要添加的三元组。对限制做处理,区分第二属性组的元素
                    Set<OWLClassExpression> allClass = new HashSet<OWLClassExpression>();
                    //用split对两个属性组进行区分
                    String[] someProperty = value.split("and");
//                    System.out.println("这是"+someProperty[1].toString());  
                    // 该IRI地址是第一组属性组的属性值
                    IRI VCls01Iri = IRI.create(base + someProperty[0]);
                    //将确定添加的第一组属性组的属性值加入set
                    allClass.add(factory.getOWLClass(VCls01Iri));

                    // 建立一个set，set的目的是要添加的value三元组。对限制做处理,区分第二属性组的元素
                    Set<OWLClassExpression> allRestiction01 = new HashSet<OWLClassExpression>();
                    //如果第二属性组的属性值里包含or则把它提出来
                    String[] restrict = someProperty[1].split("or");

                    for (int i = 0 ; i < restrict.length ; i++) {
                    	//
//                        System.out.println("这是"+restrict[i].toString());  
                        //把第二属性组的属性和属性值区分开来
                        String[] pv = restrict[i].split("-");
                             
                        //第二个属性组里的属性
                        IRI pIri = IRI.create(base + pv[0]);
//                        System.out.println("这是"+pv[0].toString());  
                        //判断第二属性组需要添加的属性是否存在
                        if (ontology.containsObjectPropertyInSignature(pIri)){
                        	//根据属性IRI得到属性
                            OWLObjectProperty Pro01 = factory.getOWLObjectProperty(pIri);  
                            //判断第二组属性的属性值是否包含or                          
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
                                //如果不包含or,则确定第二属性组的属性值
                            }else {
                                IRI vIri = IRI.create(base + pv[1]);
//                                System.out.println("这是那"+vIri.toString());                            
                                OWLClass Cls01 = factory.getOWLClass(vIri);       
                                //********非常重要，这是添加属性值的方法，对属性值进行限制
                                OWLObjectRestriction addPValue = factory.getOWLObjectSomeValuesFrom(Pro01,Cls01);

                                //向set类型中添加属性组
                                allRestiction01.add(addPValue);
                            }
                        }
                    }
                    if (!allRestiction01.isEmpty()){
                        OWLObjectUnionOf owlObjectUnionOf = factory.getOWLObjectUnionOf(allRestiction01);
                        System.out.println(" owlObjectUnionOf "+owlObjectUnionOf.toString());
                        allClass.add(owlObjectUnionOf);   
                        //ObjectIntersectionOf表示属性值的表达式
                        OWLClassExpression sd = factory.getOWLObjectIntersectionOf(allClass);
                        System.out.println(" ObjectIntersectionOf "+sd.toString());
                        //getOWLObjectSomeValuesFrom表示属性和属性值三元组的表达式
                        OWLClassExpression EqualToD = factory.getOWLObjectSomeValuesFrom(owlProperty,sd);    
                        System.out.println(" OWLObjectSomeValuesFrom "+EqualToD.toString());
                        //getOWLEquivalentClassesAxiom表示最终完整的表达式
                        OWLEquivalentClassesAxiom ax = factory.getOWLEquivalentClassesAxiom(owlClass, EqualToD);
                        //AddAxiom表示在本体上添加最终的表达式
                        AddAxiom addAx = new AddAxiom(ontology, ax);
                        System.out.println(E+":"+P+":"+value+"#添加成功");
                        //提交最终添加的结果
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
    	AddEquivalent d = new AddEquivalent("datas/qieyinChild.owl");
        //d.delEP("河马", "价格值");
        //d.addE("te","Test");
        //d.addP("花心值", "data");
        //d.addI("子墨","Test");
//        d.addEquivalntThreeTuple("Test","手颜色值","熊猫and河马or海牛");
//        d.addEquivalntThreeTuple("Test","手颜色值","熊猫and河马or海牛");
//        d.addEquivalentNestedThreeTuple("Test","气味值","牛and颜色值-红色&黑色or咬对象-猪or食物对象-河马");
        d.addEquivalentNestedThreeTuple("儿子","性别值","男性and颜色值-红色or黑色and咬对象-猪or食物对象-河马");       
        d.saveOnt("datas/ontowl.owl");
    }

}
