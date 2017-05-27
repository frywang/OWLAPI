package workOwl;

import updateOwl.DeleteConceptEntityPropertyTripple;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;

/**
 * Created by qieyin on 2017/1/23.
 * 删除只能删除概念，属性，概念属性组，不能删除实例。
 * 制造物
   人#保护对象
   Test
   a
   养殖对象
   重庆
 */
public class DeleteCEPT {

    public static void main(String[] args) throws Exception{
        DeleteConceptEntityPropertyTripple d = new DeleteConceptEntityPropertyTripple("/home/fry/Documents/svn/ai/trunk/ontology/qieyinChild.owl");
        String outo = "./datas/concept_delete";
        FileInputStream outo01 = new FileInputStream(new File(outo));
        BufferedReader br=new BufferedReader(new InputStreamReader(outo01));
        String tempstr01 = null;
        while((tempstr01=br.readLine())!=null){
            if (tempstr01.contains("#")){
                String[] s = tempstr01.split("#");
                d.delEP(s[0],s[1]);
            }else{
                d.delE(tempstr01);
                d.delP(tempstr01);
                d.delI(tempstr01);
            }
        }
        d.saveOnt("./datas/qieyinChild.owl");
    }


}
