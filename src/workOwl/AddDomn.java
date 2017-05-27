package workOwl;

import updateOwl.AddDomain;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.HashMap;

/**
 * 调用updateOwl中的addDomian，进行批量添加
 * 批量添加的结果文件中每一行的格式为[属性#值or值]
 * domain_add文件，#左边是需要添加的属性或者属性所包含的关键词，#右边是需要添加的属性的定义域。
 * 例子：颜#动物or植物
		颜色原因#动物
		生活#植物
		吃方式#人
		评价#人
		飞行#地方and组织and生物
 */
public class AddDomn {
    public static void main(String[] args)throws Exception{
//        AddDomain d = new AddDomain("./datas/qieyinChild.owl");
        AddDomain d = new AddDomain("/home/fry/Documents/svn/ai/trunk/ontology/qieyinChild.owl");
        String outo = "./datas/add/domain_add";
        FileInputStream outo01 = new FileInputStream(new File(outo));
        BufferedReader br=new BufferedReader(new InputStreamReader(outo01));
        String tempstr01 = null;
        HashMap<String, String> map = new HashMap<String, String>();
        while((tempstr01=br.readLine())!=null) {
            String[] s = tempstr01.split("#");
            //
            String s1= s[1];
            for (int i = 2 ; i < s.length; i++){
                s1 += "or" + s[i];
            }
            map.put(s[0], s1);
        }

        String file01 = "./datas/allProperties";
        FileInputStream fileInputStream = new FileInputStream(new File(file01));
        BufferedReader br01=new BufferedReader(new InputStreamReader(fileInputStream ));
        String tempstr = null;
        while((tempstr=br01.readLine())!=null) {
            for (String s2 : map.keySet()){
                //System.out.println(s2);
                if (tempstr.contains(s2)){
                    //System.out.println(tempstr);
                    d.addProDomain(tempstr,map.get(s2));
                }
            }
        }
        d.saveOnt("datas/qieyinChild.owl");
    }


}
