package workOwl;

import updateOwl.AddIndividualValueTripple;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;

/**
 * Created by qieyin on 2017/1/23.
 */
public class AddIndTripple {
    public static void main(String[] args) throws Exception{
        AddIndividualValueTripple d = new AddIndividualValueTripple("./datas/qieyinChild.owl");
        String outo = "./datas/add/individualValueTripple_add";
        FileInputStream outo01 = new FileInputStream(new File(outo));
        BufferedReader br=new BufferedReader(new InputStreamReader(outo01));
        String tempstr01 = null;
        while((tempstr01=br.readLine())!=null){
            String[] s = tempstr01.split("#");
            d.addThreeTuple(s[0],s[1],s[2]);
        }
        d.saveOnt("./datas/qwe.owl");
    }
}
