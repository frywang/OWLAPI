package workOwl;

import updateOwl.AddConceptEntityPropertyTripple;
import updateOwl.DeleteConceptEntityPropertyTripple;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;

/**
 * 调用updateOwl中的AddConceptEntityPropertyTripple，进行批量添加
 * 批量添加的add文件中每一行的格式为[概念#属性#值]
 * 
 * 	___添加三元组
 *  Test#原因#我是中国
	Test#颜色原因#true
	Test#生活原因#22.0
	Test#吃方式#23.0-28
	Test#评价#Test_1
	Test#飞行评价#a
	Test#优点评价#男人andTest_1and女人
	Test#住址评价#动物andTest_1or真菌
	Test#颜色评价#动物orTest_1and细菌
 */
public class AddCEPT {

    public static void main(String[] args) throws Exception{
    	AddConceptEntityPropertyTripple d = new AddConceptEntityPropertyTripple("/home/fry/Documents/svn/ai/trunk/ontology/qieyinChild.owl");
//       	AddConceptEntityPropertyTripple d = new AddConceptEntityPropertyTripple("datas/qieyinChild.owl");        
//        添加概念
//        String outo = "./datas/add/concept_add";

//        添加实例
//        String outo = "./datas/add/individual_add";
        
//        添加属性
//        String outo = "./datas/add/property_add";

//        添加subclass三元组
        String outo = "datas/add/objecAndDataValueTripple_add";
                
        FileInputStream outo01 = new FileInputStream(new File(outo));
        BufferedReader br=new BufferedReader(new InputStreamReader(outo01));
        String tempstr01 = null;
        while((tempstr01=br.readLine())!=null){
            String[] s = tempstr01.split("#");
            
//            添加概念；
//            d.addE(s[0], s[1]);
            
//            添加实例；           
//            d.addI(s[0], s[1]);            
            
//            添加属性；
//            d.addP(s[0],s[1]);
            
//            添加suclass三元组(单独三元组，如果出现多个三元组嵌套，则在addOWL_two里添加；
            d.addThreeTuple(s[0],s[1],s[2]);
            
    
          
        }
        d.saveOnt("datas/qieyinChild.owl");
        System.out.println("********添加完毕********");
    }
}
