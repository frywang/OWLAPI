package workOwl;

import updateOwl.AddConceptEntityPropertyTripple;
import updateOwl.AddNestedTripple;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;

/**
 * 调用updateOwl中的addOwl_two，进行批量添加限定型三元组
 * 批量添加的结果文件中每一行的格式为[概念#属性#值]
 * 
 * Test#包含#鼻子and颜色值-黑色
Test_1#包含#蛋and颜色值-白色or棕色&粉色
Test#包含#蛋and颜色值-浅棕色or白色
Test#食物对象#老虎and住址值-巴基斯坦
 儿子#性别值#男性and父母对象-我 
 */
public class AddNestedTri {
    public static void main(String[] args) throws Exception{
//        AddNestedTripple d = new AddNestedTripple("./datas/qieyinChild.owl");
        AddNestedTripple d = new AddNestedTripple("/home/fry/Documents/svn/ai/trunk/ontology/qieyinChild.owl");
        String outo = "./datas/add/nestedTuple_add";
        FileInputStream outo01 = new FileInputStream(new File(outo));
        BufferedReader br=new BufferedReader(new InputStreamReader(outo01));
        String tempstr01 = null;
        while((tempstr01=br.readLine())!=null){
            String[] s = tempstr01.split("#");

            d.addThreeTuple(s[0],s[1],s[2]);
        }
        d.saveOnt("datas/qieyinChild.owl");
    }
}
