package workOwl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;

import updateOwl.AddConceptEntityPropertyTripple;
import updateOwl.AddEquivalent;
import updateOwl.AddEquivalentTest;

public class AddEquiv {

    public static void main(String[] args) throws Exception{
//    	 AddEquivalent d = new AddEquivalent("/home/fry/Documents/svn/ai/trunk/ontology/qieyinChild.owl");
//       	AddConceptEntityPropertyTripple d = new AddConceptEntityPropertyTripple("datas/qieyinChild.owl");  
    	AddEquivalentTest d = new AddEquivalentTest("/home/fry/Documents/svn/ai/trunk/ontology/qieyinChild.owl");  
//        添加概念
//        String outo = "./datas/add/concept_add";

//        添加实例
//        String outo = "./datas/add/individual_add";
        
//        添加属性
//        String outo = "./datas/add/property_add";

//        添加subclass三元组或者equivalent三元组
//        String outo = "datas/add/nestedTuple_add";
        String outo = "datas/add/unionpvTuple_add";
                
        FileInputStream outo01 = new FileInputStream(new File(outo));
        BufferedReader br=new BufferedReader(new InputStreamReader(outo01));
        String tempstr01 = null;
        while((tempstr01=br.readLine())!=null){
            String[] s = tempstr01.split("#");
            System.out.println(s);
            
//            添加概念；
//            d.addE(s[0], s[1]);
            
//            添加实例；
//            d.addI(s[0], s[1]);            
            
//            添加属性；
//            d.addP(s[0],s[1]);
            
//           添加equivalent三元组
//            d.addEquivalntThreeTuple(s[0],s[1],s[2]);
////          添加equivalent嵌套三元组         
//            d.addEquivalentNestedThreeTuple(s[0],s[1],s[2]);
//          添加equivalent的intersection三元组   
            d.addEquivalentNestedThreeTupleTest(s[0],s[1],s[2]);
            
        }
        d.saveOnt("datas/qieyinChild.owl");
    }
}
