package updateOwl;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.formats.RDFXMLDocumentFormat;
import org.semanticweb.owlapi.model.AddAxiom;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLEquivalentClassesAxiom;
import org.semanticweb.owlapi.model.OWLObjectIntersectionOf;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;
import org.semanticweb.owlapi.model.OWLObjectRestriction;
import org.semanticweb.owlapi.model.OWLObjectUnionOf;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.PrefixManager;
import org.semanticweb.owlapi.util.DefaultPrefixManager;

public class AddEquivalentTest {
	private OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
	private String filePath;
	private final String base = "http://qieyin/ontologies/child#";
	private OWLOntology ontology;
	private OWLDataFactory factory;
	private PrefixManager pm;

	// 定义类的初始结构
	public AddEquivalentTest(String filePath) {
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

	public boolean checkOntInSignature(List<String> list) throws Exception {
		// 需要添加属性组到概念即是list.get(0)
		IRI ClsIri = IRI.create(base + list.get(0));
		OWLClass owlClass = factory.getOWLClass(ClsIri);
		if (!ontology.containsClassInSignature(ClsIri)) {
			System.out.println(list.get(0) + " 概念不存在，请确认");
			return false;
		} else {
			int k = list.size()-1;
			for (int i = 1; i < list.size(); i++) {
				String pv = list.get(i);
				String pvdiv[] = pv.split("-");
				IRI PropIri = IRI.create(base + pvdiv[0]);
				IRI ValueIri = IRI.create(base + pvdiv[1]);
				if ((!ontology.containsObjectPropertyInSignature(PropIri))
						|| (!ontology.containsClassInSignature(ValueIri))) {
					if (!ontology.containsObjectPropertyInSignature(PropIri)) {
						// 判断对象属性是否存在
						System.out.println(pvdiv[0] + "#对象属性不存在");
					}
					 if (!ontology.containsClassInSignature(ValueIri)) {
					 // 判断属性值概念是否存在
					 System.out.println(pvdiv[1] + "#属性值概念不存在");
					 }
					continue;
				}
				k--;
			}
			if (k == 0){
				System.out.println("ok,该数据到概念和属性在知识库中都已存在，可以导入。\n导入中...");
				return true;
			}else{
				System.out.println("数据中有概念或属性不存在");
				return false;

			}		
			}

		// 判断对象属性或概念是否存在
	}
	// 查看知识库是否包含需要添加属性组的概念
	

	public void addEquivalentNestedThreeTupleTest(List<String> list) throws Exception {
		//所有对象属性组成的set
		Set<OWLObjectProperty> allObjectProperty = new HashSet<OWLObjectProperty>();
		//set的元素是objectRestriction
		Set<OWLClassExpression> allObjectRestriction = new HashSet<OWLClassExpression>();
		// 需要添加属性组到概念即是list.get(0)
		IRI ClsIri = IRI.create(base + list.get(0));
		OWLClass owlClass = factory.getOWLClass(ClsIri);
		
		for (int i = 1; i < list.size(); i++) {
			String pv = list.get(i);
			String pvdiv[] = pv.split("-");
			IRI PropIri = IRI.create(base + pvdiv[0]);
			IRI ValueIri = IRI.create(base + pvdiv[1]);
			OWLObjectProperty owlObProperty = factory.getOWLObjectProperty(PropIri);
			allObjectProperty.add(owlObProperty);
			OWLClass owlValue = factory.getOWLClass(ValueIri);
			 //********非常重要，这是添加属性值的方法，对属性值进行限制
			OWLObjectRestriction addPValue = factory.getOWLObjectSomeValuesFrom(owlObProperty, owlValue);
			 //向set类型中添加属性组
			allObjectRestriction.add(addPValue);
			}		
		OWLClassExpression EqualToD = factory.getOWLObjectIntersectionOf(allObjectRestriction);
		// getOWLEquivalentClassesAxiom表示最终完整的表达式
		OWLEquivalentClassesAxiom ax = factory.getOWLEquivalentClassesAxiom(owlClass, EqualToD);
		
		//这个采用先删后添，如果已有同样的EquivalentClassesAxioms则保留并记录。
		if (!ontology.getEquivalentClassesAxioms(owlClass).isEmpty()) {
			if (checkForDuplicates(ontology,owlClass,ax) == true){
				System.out.println("***请确认后再添加");
			}else{
				// AddAxiom表示在本体上添加最终的表达式
				AddAxiom addAx = new AddAxiom(ontology, ax);
				System.out.println(allObjectRestriction.toString() + "\n#添加成功");
				// 提交最终添加的结果
				manager.applyChange(addAx);
			}
		}else{
			// AddAxiom表示在本体上添加最终的表达式
			AddAxiom addAx = new AddAxiom(ontology, ax);
			System.out.println(allObjectRestriction.toString() + "#\n添加成功");
			// 提交最终添加的结果
			manager.applyChange(addAx);
		}
	}

	//此函数用来判断该表达式之前是否添加
	public static boolean checkForDuplicates(OWLOntology ontology, OWLClass owlClass, OWLEquivalentClassesAxiom newax)
			throws Exception {
		//所有的EquivalentClassesAxioms表达式
		Set<OWLEquivalentClassesAxiom> allEquivalentClassesAxioms = new HashSet<OWLEquivalentClassesAxiom>();
		Set<OWLObjectProperty> newOWLObjectProperties = newax.getObjectPropertiesInSignature();
		Set<OWLObjectProperty> oldOWLObjectProperties = new HashSet<OWLObjectProperty>();
		Set<Set> alloldOWLObjectProperties = new HashSet<Set>();
		
		for (OWLEquivalentClassesAxiom oldax : ontology.getEquivalentClassesAxioms(owlClass)) {
			//
			oldOWLObjectProperties = oldax.getObjectPropertiesInSignature();
			alloldOWLObjectProperties.add(oldOWLObjectProperties);
			allEquivalentClassesAxioms.add(oldax);
		}
		// 查看原概念是否具有此属性组
		if(alloldOWLObjectProperties.contains(newOWLObjectProperties)){			
			if (allEquivalentClassesAxioms.contains(newax)){
			System.out.println("此表达式在知识库中已经存在");
			System.out.println(newax.toString());
			return true;
			}else{
			System.out.println("\n原表达式的属性都已存在，如下");	
			System.out.println("原表达式为"+oldOWLObjectProperties.toString());
			System.out.println("新表达式为"+newOWLObjectProperties.toString());
			return false;
			}
		}else{
			return false;
		}
	}

	public static void main(String[] args) throws Exception {
//		AddEquivalentTest d = new AddEquivalentTest("datas/qieyinChild.owl");
		AddEquivalentTest d = new AddEquivalentTest("datas/ontowl.owl");
		String allexpress = "外公#性别值-女性#配偶对象-儿子#父母对象-妹妹#颜色值-嫂子";
		String[] express = allexpress.split("#");

		// 将字符串数组转为list
		List<String> expressList = Arrays.asList(express);
		if (d.checkOntInSignature(expressList) == true){
			d.addEquivalentNestedThreeTupleTest(expressList);
		}else{
			System.out.println("修改准确后再进行添加");
		}
		
		// d.delEP("河马", "价格值");
		// d.addE("te","Test");
		// d.addP("花心值", "data");
		// d.addI("子墨","Test");
		// d.addEquivalntThreeTuple("Test","手颜色值","熊猫and河马or海牛");
		// d.addEquivalntThreeTuple("Test","手颜色值","熊猫and河马or海牛");
		// d.addEquivalentNestedThreeTuple("Test","气味值","牛and颜色值-红色&黑色or咬对象-猪or食物对象-河马");
		// d.addEquivalentNestedThreeTuple("儿子","性别值","男性and父母对象-我");
		// d.addEquivalentNestedThreeTupleTest(expressList);
		d.saveOnt("datas/ontowl.owl");
	}
}
