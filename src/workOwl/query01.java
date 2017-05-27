package workOwl;

import updateOwl.AddConceptEntityPropertyTripple;
import updateOwl.queryOwl;

import java.io.*;
import java.util.ArrayList;

/**
 * 调用updateOwl中的queryOwl，进行查询
 * query文件为查询项
 * answer文件为查询结果
 */
public class query01 {

    public static void main(String[] args) throws Exception{

        String s = "./datas/answer";
        FileWriter fw = new FileWriter(s);
        BufferedWriter writer  = new BufferedWriter(fw);



        queryOwl d = new queryOwl("./datas/qieyinChild.owl");
        String outo = "./datas/query/query";
        FileInputStream outo01 = new FileInputStream(new File(outo));
        BufferedReader br=new BufferedReader(new InputStreamReader(outo01));
        String tempstr01 = null;
        while((tempstr01=br.readLine())!=null){
            ArrayList<String> re = d.queryP(tempstr01);
            for (String r01 : re){
                writer.write(r01 + "\n");

            }
        }
        writer.close();
    }
}
