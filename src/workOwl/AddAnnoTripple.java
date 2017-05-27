package workOwl;

import updateOwl.AddAnnotationTripple;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;

/**
 * Created by qieyin on 2017/1/23.
 */
public class AddAnnoTripple {

    /*
     * label 添加中的结果文件中，文件行格式为：“概念#注释类型#value”
     */

    public static void main(String[] args) throws Exception{
        AddAnnotationTripple d = new AddAnnotationTripple("./datas/qieyinChild.owl");
        String outo = "./datas/add/annotationTripple_add";
        FileInputStream outo01 = new FileInputStream(new File(outo));
        BufferedReader br=new BufferedReader(new InputStreamReader(outo01));
        String tempstr01 = null;
        while((tempstr01=br.readLine())!=null){
            String[] s = tempstr01.split("#");

            d.addELabel(s[0],s[1],s[2]);

        }
        d.saveOnt("./datas/qwe.owl");
    }
}
