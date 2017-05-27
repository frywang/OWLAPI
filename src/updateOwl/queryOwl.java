package updateOwl;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.formats.RDFXMLDocumentFormat;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.util.DefaultPrefixManager;

import java.io.File;
import java.util.ArrayList;
import java.util.Set;

/**
 * 查询数据
 *      ：针对--概念查询;查询结果是以概念为subject的三元组。
 *      ：针对--属性查询;查询结果是以该属性为predicate的三元组。
 *      ：针对--概念属性查询;查询具体的三元组。
 * main 函数给出具体实例，查询结果返回到result里的
 */
public class queryOwl {
    private OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
    private String filePath;
    private final String base = "http://qieyin/ontologies/child#";
    private OWLOntology ontology;
    private OWLDataFactory factory;
    private PrefixManager pm;
    // 定义类的初始结构
    public queryOwl(String filePath) {
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

    /*
     * 针对提供的概念E,对其进行查询
     */
    public ArrayList<String> queryE(String E) throws Exception{
        // 定义对象IRI
        IRI ClsIri = IRI.create(base+E);
        ArrayList<String> result = new ArrayList<String>();

        if(ontology.containsClassInSignature(ClsIri)){
            OWLClass owlClass = factory.getOWLClass(ClsIri);
            if (!ontology.getSubClassAxiomsForSubClass(owlClass).isEmpty()) {
                for (OWLSubClassOfAxiom ax : ontology.getSubClassAxiomsForSubClass(owlClass)) {
                    OWLClassExpression clsEx = ax.getSuperClass();
                    // 添加概念
                    String V = E;
                    if (clsEx.isAnonymous()) {
                        String type = clsEx.getClassExpressionType().getName();
                        if (type.equalsIgnoreCase("DataSomeValuesFrom")){
                            OWLDataSomeValuesFrom someValuesFrom = (OWLDataSomeValuesFrom) clsEx;
                            OWLDataProperty owlDataProperty = someValuesFrom.getProperty().asOWLDataProperty();
                            // 添加属性
                            V += "#"+owlDataProperty.getIRI().getShortForm();
                            OWLDataRange range = someValuesFrom.getFiller();
                            if (range.getDataRangeType().getName().equalsIgnoreCase("DataIntersectionOf")) {
                                OWLDataIntersectionOf intersectionOf = (OWLDataIntersectionOf) range;
                                Set<OWLDataRange> ranges = intersectionOf.getOperands();
                                int i = 0;
                                for (OWLDataRange ra : ranges) {
                                    OWLDatatypeRestriction r = (OWLDatatypeRestriction) ra;
                                    Set<OWLFacetRestriction> facetRestrictions = r.getFacetRestrictions();
                                    for (OWLFacetRestriction f : facetRestrictions) {
                                        if (i == 0) {
                                            V += ("#" + f.getFacetValue().parseDouble());
                                        } else {
                                            V += ("-" + f.getFacetValue().parseDouble());
                                        }
                                    }
                                    i++;
                                }
                            }
                            // 保留结果
                            result.add(V);

                        }else if (type.equalsIgnoreCase("DataHasValue")){
                            OWLDataHasValue dataHasValue = (OWLDataHasValue) clsEx;
                            OWLDataProperty owlDataProperty = dataHasValue.getProperty().asOWLDataProperty();
                            // 添加属性
                            V += "#"+owlDataProperty.getIRI().getShortForm();

                            OWLLiteral literal = dataHasValue.getFiller();
                            if (literal.getDatatype().isDouble()) {
                                V += "#" + Double.toString(literal.parseDouble());

                            } else if (literal.getDatatype().isBoolean()) {
                                V += "#" + Boolean.toString(literal.parseBoolean());

                            } else if (literal.getDatatype().isString()) {
                                V += "#" + literal.getLiteral();
                            }
                            // 保留结果
                            result.add(V);

                        }else if (type.equalsIgnoreCase("ObjectHasValue")){
                            OWLObjectHasValue objectHasValue = (OWLObjectHasValue) clsEx;
                            OWLObjectProperty owlObjectProperty = objectHasValue.getProperty().asOWLObjectProperty();
                            // 添加属性
                            V += "#"+owlObjectProperty.getIRI().getShortForm();

                            OWLIndividual owlIndividual = objectHasValue.getFiller();
                            String value = owlIndividual.asOWLNamedIndividual().getIRI().getShortForm();
                            V += "#" + value;
                            // 保留结果
                            result.add(V);

                        }else if (type.equalsIgnoreCase("ObjectSomeValuesFrom")){
                            OWLObjectSomeValuesFrom someValuesFrom = (OWLObjectSomeValuesFrom) clsEx;
                            OWLObjectProperty owlObjectProperty = someValuesFrom.getProperty().asOWLObjectProperty();
                            // 添加属性
                            V += "#"+owlObjectProperty.getIRI().getShortForm();
                            OWLClassExpression owlClassExpression = someValuesFrom.getFiller();

                            if(owlClassExpression.getClassExpressionType().getName().equals("ObjectIntersectionOf")){
                                Set<OWLClass> set = owlClassExpression.getClassesInSignature();
                                //System.out.println(set);
                                V += "#";
                                int imp = 0;
                                for (OWLClass s : set){
                                    imp += 1;
                                    String value = s.getIRI().getShortForm();
                                    if (imp == set.size()){
                                        V += value;
                                    }else{
                                        V += value + "&";
                                    }
                                }
                            }else{
                                //System.out.println(owlClassExpression);
                                String value = owlClassExpression.asOWLClass().getIRI().getShortForm();
                                V += "#" + value;
                            }
                            // 保留结果
                            result.add(V);

                        }else if (type.equalsIgnoreCase("ObjectAllValuesFrom")){
                            OWLObjectAllValuesFrom allValuesFrom = (OWLObjectAllValuesFrom) clsEx;
                            OWLObjectProperty owlObjectProperty = allValuesFrom.getProperty().asOWLObjectProperty();
                            // 添加属性
                            V += "#"+owlObjectProperty.getIRI().getShortForm();
                            OWLClassExpression owlClassExpression = allValuesFrom.getFiller();
                            String value = owlClassExpression.asOWLClass().getIRI().getShortForm();
                            V += "#" + value;
                            // 保留结果
                            result.add(V);
                        }
                    }else{
                        // 处理subclass中的父类
                        OWLClass supCls = clsEx.asOWLClass();
                        V += "#父类#" + supCls.getIRI().getShortForm();
                    }
                }
            }
        }else {
            System.out.println(E+"#该类不存在");
        }
        return result;
    }

    /*
     * 针对提供的属性P,对其进行查询
     */
    public ArrayList<String> queryP(String P) throws Exception{
        // 定义对象IRI
        IRI PropIri = IRI.create(base+P);
        ArrayList<String> result = new ArrayList<String>();
        OWLProperty owlProperty = null;
        if (ontology.containsDataPropertyInSignature(PropIri)){
            owlProperty = factory.getOWLDataProperty(PropIri);
        }else if (ontology.containsObjectPropertyInSignature(PropIri)){
            owlProperty = factory.getOWLObjectProperty(PropIri);
        }

        //
        if(ontology.containsDataPropertyInSignature(PropIri) ||
                ontology.containsObjectPropertyInSignature(PropIri)){
            Set<OWLAxiom> owlAxioms = ontology.getAxioms();
            for (OWLAxiom owlAxiom : owlAxioms){
                if (owlAxiom.getAxiomType().getName().equals("SubClassOf")){
                    OWLSubClassOfAxiom ax = (OWLSubClassOfAxiom)owlAxiom;
                    OWLClassExpression clsEx = ax.getSuperClass();
                    String V = "";
                    if (clsEx.isAnonymous()) {
                        String type = clsEx.getClassExpressionType().getName();
                        if (type.equalsIgnoreCase("DataSomeValuesFrom")){
                            OWLDataSomeValuesFrom someValuesFrom = (OWLDataSomeValuesFrom) clsEx;
                            OWLDataProperty owlDataProperty = someValuesFrom.getProperty().asOWLDataProperty();
                            if (owlDataProperty.equals(owlProperty)){
                                // 添加属性
                                V += ax.getSubClass().asOWLClass().getIRI().getShortForm()+ "#" + P;
                                OWLDataRange range = someValuesFrom.getFiller();
                                if (range.getDataRangeType().getName().equalsIgnoreCase("DataIntersectionOf")) {
                                    OWLDataIntersectionOf intersectionOf = (OWLDataIntersectionOf) range;
                                    Set<OWLDataRange> ranges = intersectionOf.getOperands();
                                    int i = 0;
                                    for (OWLDataRange ra : ranges) {
                                        OWLDatatypeRestriction r = (OWLDatatypeRestriction) ra;
                                        Set<OWLFacetRestriction> facetRestrictions = r.getFacetRestrictions();
                                        for (OWLFacetRestriction f : facetRestrictions) {
                                            if (i == 0) {
                                                V += ("#" + f.getFacetValue().parseDouble());
                                            } else {
                                                V += ("-" + f.getFacetValue().parseDouble());
                                            }
                                        }
                                        i++;
                                    }
                                }
                                // 保留结果
                                result.add(V);
                            }

                        }else if (type.equalsIgnoreCase("DataHasValue")){
                            OWLDataHasValue dataHasValue = (OWLDataHasValue) clsEx;

                            OWLDataProperty owlDataProperty = dataHasValue.getProperty().asOWLDataProperty();
                            if(owlDataProperty.equals(owlProperty)){
                                // 添加属性
                                V += ax.getSubClass().asOWLClass().getIRI().getShortForm()+ "#" + P;

                                OWLLiteral literal = dataHasValue.getFiller();
                                if (literal.getDatatype().isDouble()) {
                                    V += "#" + Double.toString(literal.parseDouble());

                                } else if (literal.getDatatype().isBoolean()) {
                                    V += "#" + Boolean.toString(literal.parseBoolean());

                                } else if (literal.getDatatype().isString()) {
                                    V += "#" + literal.getLiteral();
                                }
                                // 保留结果
                                result.add(V);
                            }

                        }else if (type.equalsIgnoreCase("ObjectHasValue")){
                            OWLObjectHasValue objectHasValue = (OWLObjectHasValue) clsEx;
                            OWLObjectProperty owlObjectProperty = objectHasValue.getProperty().asOWLObjectProperty();
                            if (owlObjectProperty.equals(owlProperty)){
                                // 添加属性
                                V += ax.getSubClass().asOWLClass().getIRI().getShortForm()+ "#" + P;

                                OWLIndividual owlIndividual = objectHasValue.getFiller();
                                String value = owlIndividual.asOWLNamedIndividual().getIRI().getShortForm();
                                V += "#" + value;
                                // 保留结果
                                result.add(V);
                            }

                        }else if (type.equalsIgnoreCase("ObjectSomeValuesFrom")){
                            OWLObjectSomeValuesFrom someValuesFrom = (OWLObjectSomeValuesFrom) clsEx;
                            OWLObjectProperty owlObjectProperty = someValuesFrom.getProperty().asOWLObjectProperty();
                            if(owlObjectProperty.equals(owlProperty)){
                                // 添加属性
                                V += ax.getSubClass().asOWLClass().getIRI().getShortForm()+ "#" + P;
                                OWLClassExpression owlClassExpression = someValuesFrom.getFiller();

                                if(owlClassExpression.getClassExpressionType().getName().equals("ObjectIntersectionOf")){
                                    Set<OWLClass> set = owlClassExpression.getClassesInSignature();
                                    //System.out.println(set);
                                    V += "#";
                                    int imp = 0;
                                    for (OWLClass s : set){
                                        imp += 1;
                                        String value = s.getIRI().getShortForm();
                                        if (imp == set.size()){
                                            V += value;
                                        }else{
                                            V += value + "&";
                                        }
                                    }
                                }else{
                                    //System.out.println(owlClassExpression);
                                    String value = owlClassExpression.asOWLClass().getIRI().getShortForm();
                                    V += "#" + value;
                                }
                                // 保留结果
                                result.add(V);
                            }

                        }else if (type.equalsIgnoreCase("ObjectAllValuesFrom")){
                            OWLObjectAllValuesFrom allValuesFrom = (OWLObjectAllValuesFrom) clsEx;
                            OWLObjectProperty owlObjectProperty = allValuesFrom.getProperty().asOWLObjectProperty();
                            if(owlObjectProperty.equals(owlProperty)){
                                // 添加属性
                                V += ax.getSubClass().asOWLClass().getIRI().getShortForm()+ "#" + P;
                                OWLClassExpression owlClassExpression = allValuesFrom.getFiller();
                                if(owlClassExpression.getClassExpressionType().getName().equals("ObjectIntersectionOf")){
                                    Set<OWLClass> set = owlClassExpression.getClassesInSignature();
                                    //System.out.println(set);
                                    V += "#";
                                    int imp = 0;
                                    for (OWLClass s : set){
                                        imp += 1;
                                        String value = s.getIRI().getShortForm();
                                        if (imp == set.size()){
                                            V += value;
                                        }else{
                                            V += value + "&";
                                        }
                                    }
                                }else{
                                    //System.out.println(owlClassExpression);
                                    String value = owlClassExpression.asOWLClass().getIRI().getShortForm();
                                    V += "#" + value;
                                }
                                // 保留结果
                                result.add(V);
                            }
                        }
                    }
                }
            }
        }else {
            System.out.println(P+"#没有该属性");
        }
        return result;
    }

    /*
     * 针对提供的属性P-值V,对其进行查询
     */
    public ArrayList<String> queryPV(String P, String V) throws Exception{
        // 定义对象IRI
        IRI PropIri = IRI.create(base+P);
        ArrayList<String> result = new ArrayList<String>();
        OWLProperty owlProperty = null;
        if (ontology.containsDataPropertyInSignature(PropIri)){
            owlProperty = factory.getOWLDataProperty(PropIri);
        }else if (ontology.containsObjectPropertyInSignature(PropIri)){
            owlProperty = factory.getOWLObjectProperty(PropIri);
        }

        //
        if(ontology.containsDataPropertyInSignature(PropIri) ||
                ontology.containsObjectPropertyInSignature(PropIri)){
            Set<OWLAxiom> owlAxioms = ontology.getAxioms();
            for (OWLAxiom owlAxiom : owlAxioms){
                if (owlAxiom.getAxiomType().getName().equals("SubClassOf")){
                    OWLSubClassOfAxiom ax = (OWLSubClassOfAxiom)owlAxiom;
                    OWLClassExpression clsEx = ax.getSuperClass();
                    String value = "";
                    if (clsEx.isAnonymous()) {
                        String type = clsEx.getClassExpressionType().getName();
                        if (type.equalsIgnoreCase("DataSomeValuesFrom")){
                            OWLDataSomeValuesFrom someValuesFrom = (OWLDataSomeValuesFrom) clsEx;
                            OWLDataProperty owlDataProperty = someValuesFrom.getProperty().asOWLDataProperty();
                            if (owlDataProperty.equals(owlProperty)){
                                OWLDataRange range = someValuesFrom.getFiller();
                                if (range.getDataRangeType().getName().equalsIgnoreCase("DataIntersectionOf")) {
                                    OWLDataIntersectionOf intersectionOf = (OWLDataIntersectionOf) range;
                                    Set<OWLDataRange> ranges = intersectionOf.getOperands();
                                    int i = 0;
                                    String tmp = "";
                                    for (OWLDataRange ra : ranges) {
                                        OWLDatatypeRestriction r = (OWLDatatypeRestriction) ra;
                                        Set<OWLFacetRestriction> facetRestrictions = r.getFacetRestrictions();
                                        for (OWLFacetRestriction f : facetRestrictions) {
                                            if (i == 0) {
                                                tmp += ("#" + f.getFacetValue().parseDouble());
                                            } else {
                                                tmp += ("-" + f.getFacetValue().parseDouble());
                                            }
                                        }
                                        i++;
                                    }
                                    if (tmp.equals(V)){
                                        value = ax.getSubClass().asOWLClass().getIRI().getShortForm()+ "#" + P + "#" + V;
                                        // 保留结果
                                        result.add(value);
                                    }
                                }
                            }

                        }else if (type.equalsIgnoreCase("DataHasValue")){
                            OWLDataHasValue dataHasValue = (OWLDataHasValue) clsEx;

                            OWLDataProperty owlDataProperty = dataHasValue.getProperty().asOWLDataProperty();
                            if(owlDataProperty.equals(owlProperty)){
                                String tmp = "";
                                OWLLiteral literal = dataHasValue.getFiller();
                                if (literal.getDatatype().isDouble()) {
                                    tmp = Double.toString(literal.parseDouble());

                                } else if (literal.getDatatype().isBoolean()) {
                                    tmp = Boolean.toString(literal.parseBoolean());

                                } else if (literal.getDatatype().isString()) {
                                    tmp = literal.getLiteral();
                                }
                                if (tmp.equals(V)){
                                    value = ax.getSubClass().asOWLClass().getIRI().getShortForm()+ "#" + P + "#" + V;
                                    // 保留结果
                                    result.add(value);
                                }
                            }

                        }else if (type.equalsIgnoreCase("ObjectHasValue")){
                            OWLObjectHasValue objectHasValue = (OWLObjectHasValue) clsEx;
                            OWLObjectProperty owlObjectProperty = objectHasValue.getProperty().asOWLObjectProperty();
                            if (owlObjectProperty.equals(owlProperty)){
                                OWLIndividual owlIndividual = objectHasValue.getFiller();
                                if (owlIndividual.asOWLNamedIndividual().getIRI().getShortForm().equals(V)){
                                    value = ax.getSubClass().asOWLClass().getIRI().getShortForm()+ "#" + P + "#" +V;
                                    result.add(value);
                                }
                            }

                        }else if (type.equalsIgnoreCase("ObjectSomeValuesFrom")){
                            OWLObjectSomeValuesFrom someValuesFrom = (OWLObjectSomeValuesFrom) clsEx;
                            OWLObjectProperty owlObjectProperty = someValuesFrom.getProperty().asOWLObjectProperty();
                            if(owlObjectProperty.equals(owlProperty)){
                                OWLClassExpression owlClassExpression = someValuesFrom.getFiller();
                                if(owlClassExpression.asOWLClass().getIRI().getShortForm().equals(V)){
                                    value = ax.getSubClass().asOWLClass().getIRI().getShortForm()+ "#" + P + "#" +V;
                                    result.add(value);
                                }
                            }

                        }else if (type.equalsIgnoreCase("ObjectAllValuesFrom")){





                            OWLObjectAllValuesFrom allValuesFrom = (OWLObjectAllValuesFrom) clsEx;
                            OWLObjectProperty owlObjectProperty = allValuesFrom.getProperty().asOWLObjectProperty();
                            if(owlObjectProperty.equals(owlProperty)){

                                OWLClassExpression owlClassExpression = allValuesFrom.getFiller();
                                if(owlClassExpression.asOWLClass().getIRI().getShortForm().equals(V)){
                                    value = ax.getSubClass().asOWLClass().getIRI().getShortForm()+ "#" + P + "#" +V;
                                    result.add(value);
                                }
                            }
                        }
                    }
                }
            }
        }else {
            System.out.println(P+"#没有该属性");
        }
        return result;
    }

    /*
     * 针对提供的E-P,对其进行查询
     */
    public ArrayList<String> queryEP(String E, String P) throws Exception{
        // 定义对象IRI
        IRI ClsIri = IRI.create(base+E);
        IRI PropIri = IRI.create(base+P);
        ArrayList<String> result = new ArrayList<String>();
        OWLProperty owlProperty = null;
        if (ontology.containsDataPropertyInSignature(PropIri)){
            owlProperty = factory.getOWLDataProperty(PropIri);
        }else if (ontology.containsObjectPropertyInSignature(PropIri)){
            owlProperty = factory.getOWLObjectProperty(PropIri);
        }

        if(ontology.containsClassInSignature(ClsIri)){
            OWLClass owlClass = factory.getOWLClass(ClsIri);
            if (!ontology.getSubClassAxiomsForSubClass(owlClass).isEmpty()) {
                for (OWLSubClassOfAxiom ax : ontology.getSubClassAxiomsForSubClass(owlClass)) {
                    OWLClassExpression clsEx = ax.getSuperClass();
                    // 添加概念
                    String V = E;
                    if (clsEx.isAnonymous()) {
                        String type = clsEx.getClassExpressionType().getName();
                        if (type.equalsIgnoreCase("DataSomeValuesFrom")){
                            OWLDataSomeValuesFrom someValuesFrom = (OWLDataSomeValuesFrom) clsEx;
                            OWLDataProperty owlDataProperty = someValuesFrom.getProperty().asOWLDataProperty();
                            if (owlDataProperty.equals(owlProperty)){
                                // 添加属性
                                V += "#" + P;
                                OWLDataRange range = someValuesFrom.getFiller();
                                if (range.getDataRangeType().getName().equalsIgnoreCase("DataIntersectionOf")) {
                                    OWLDataIntersectionOf intersectionOf = (OWLDataIntersectionOf) range;
                                    Set<OWLDataRange> ranges = intersectionOf.getOperands();
                                    int i = 0;
                                    for (OWLDataRange ra : ranges) {
                                        OWLDatatypeRestriction r = (OWLDatatypeRestriction) ra;
                                        Set<OWLFacetRestriction> facetRestrictions = r.getFacetRestrictions();
                                        for (OWLFacetRestriction f : facetRestrictions) {
                                            if (i == 0) {
                                                V += ("#" + f.getFacetValue().parseDouble());
                                            } else {
                                                V += ("-" + f.getFacetValue().parseDouble());
                                            }
                                        }
                                        i++;
                                    }
                                }
                                // 保留结果
                                result.add(V);
                            }

                        }else if (type.equalsIgnoreCase("DataHasValue")){
                            OWLDataHasValue dataHasValue = (OWLDataHasValue) clsEx;

                            OWLDataProperty owlDataProperty = dataHasValue.getProperty().asOWLDataProperty();
                            if(owlDataProperty.equals(owlProperty)){
                                // 添加属性
                                V += "#"+owlDataProperty.getIRI().getShortForm();

                                OWLLiteral literal = dataHasValue.getFiller();
                                if (literal.getDatatype().isDouble()) {
                                    V += "#" + Double.toString(literal.parseDouble());

                                } else if (literal.getDatatype().isBoolean()) {
                                    V += "#" + Boolean.toString(literal.parseBoolean());

                                } else if (literal.getDatatype().isString()) {
                                    V += "#" + literal.getLiteral();
                                }
                                // 保留结果
                                result.add(V);
                            }

                        }else if (type.equalsIgnoreCase("ObjectHasValue")){
                            OWLObjectHasValue objectHasValue = (OWLObjectHasValue) clsEx;
                            OWLObjectProperty owlObjectProperty = objectHasValue.getProperty().asOWLObjectProperty();
                            if (owlObjectProperty.equals(owlProperty)){
                                // 添加属性
                                V += "#"+owlObjectProperty.getIRI().getShortForm();

                                OWLIndividual owlIndividual = objectHasValue.getFiller();
                                String value = owlIndividual.asOWLNamedIndividual().getIRI().getShortForm();
                                V += "#" + value;
                                // 保留结果
                                result.add(V);
                            }

                        }else if (type.equalsIgnoreCase("ObjectSomeValuesFrom")){
                            OWLObjectSomeValuesFrom someValuesFrom = (OWLObjectSomeValuesFrom) clsEx;
                            OWLObjectProperty owlObjectProperty = someValuesFrom.getProperty().asOWLObjectProperty();
                            if(owlObjectProperty.equals(owlProperty)){
                                // 添加属性
                                V += "#"+owlObjectProperty.getIRI().getShortForm();
                                OWLClassExpression owlClassExpression = someValuesFrom.getFiller();
                                if(owlClassExpression.getClassExpressionType().getName().equals("ObjectIntersectionOf")){
                                    Set<OWLClass> set = owlClassExpression.getClassesInSignature();
                                    //System.out.println(set);
                                    V += "#";
                                    int imp = 0;
                                    for (OWLClass s : set){
                                        imp += 1;
                                        String value = s.getIRI().getShortForm();
                                        if (imp == set.size()){
                                            V += value;
                                        }else{
                                            V += value + "&";
                                        }
                                    }
                                }else{
                                    //System.out.println(owlClassExpression);
                                    String value = owlClassExpression.asOWLClass().getIRI().getShortForm();
                                    V += "#" + value;
                                }

                                // 保留结果
                                result.add(V);
                            }

                        }else if (type.equalsIgnoreCase("ObjectAllValuesFrom")){
                            OWLObjectAllValuesFrom allValuesFrom = (OWLObjectAllValuesFrom) clsEx;
                            OWLObjectProperty owlObjectProperty = allValuesFrom.getProperty().asOWLObjectProperty();

                            if(owlObjectProperty.equals(owlProperty)){
                                // 添加属性
                                V += "#"+owlObjectProperty.getIRI().getShortForm();
                                OWLClassExpression owlClassExpression = allValuesFrom.getFiller();
                                String value = owlClassExpression.asOWLClass().getIRI().getShortForm();
                                V += "#" + value;
                                // 保留结果
                                result.add(V);
                            }
                        }
                    }
                }
            }
        }else {
            System.out.println(E+"#该类不存在");
        }
        return result;
    }
    public static void main(String[] args)throws Exception{

        queryOwl d = new queryOwl("OWLAPI/datas/qieyinChild.owl");
        ArrayList<String> re = d.queryEP("鸭子","触觉评价");
        for(String s : re){
            System.out.println(s);
        }
    }

}
