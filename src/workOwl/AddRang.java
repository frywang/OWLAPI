package workOwl;

import updateOwl.AddRange;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.HashMap;

/**
 * Created by qieyin on 2017/1/23.
 * 
 */
public class AddRang {

    public static void main(String[] args)throws Exception{
        AddRange d = new AddRange("./datas/qieyinChild.owl");
        String outo = "./datas/add/range_add";
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
        d.saveOnt("./datas/qwe.owl");
    }

}
