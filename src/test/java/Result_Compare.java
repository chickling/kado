import java.io.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Objects;

/**
 * Created by gl08 on 2016/1/18.
 */
public class Result_Compare {

    public boolean compareCSV(ArrayList<String> presto,ArrayList<String> hive) throws IOException {
        if (presto.size()!=hive.size())
            return false;

        for (int index =0 ; index< presto.size() ; index++) {
            System.out.println("File Name : " + presto.get(index) + "  -----  " + hive.get(index));
//            FileInputStream pfis=new FileInputStream(new File(presto.get(index)));
//            FileInputStream hfis=new FileInputStream(new File(hive.get(index)));

            FileReader pfr = new FileReader(new File(presto.get(index)));
            FileReader hfr = new FileReader(new File(hive.get(index)));


//            BufferedReader pbr=new BufferedReader(new InputStreamReader(pfis));
//            BufferedReader hbr=new BufferedReader(new InputStreamReader(hfis));
            HashSet<String> prestoSet = new HashSet<>();
            HashSet<String> hiveSet = new HashSet<>();
            BufferedReader pbr = new BufferedReader(pfr);
            BufferedReader hbr = new BufferedReader(hfr);
            int rowCount = 1;
            String pline = pbr.readLine();
//            String pline ="";
//            String hline = hbr.readLine();
            String hline = "";
            int scan=0;
            while ((pline = pbr.readLine()) != null ) {
                scan++;
//                pline = pline.replace("\\N","null").trim();
                pline = pline.replaceAll("\"", "").trim().replaceAll("\\\\","");
                if (pline.endsWith(","))
                    pline+="null";
                pline=pline.replace("null","NULL");
//                pline=pline.split(",")[0];
                if (prestoSet.contains(pline))
                    System.out.println("already in Set : "+pline);
                prestoSet.add(pline);

            }
            prestoSet.remove("");
            while ( (hline = hbr.readLine()) != null){
                hline = hline.replace("\\N","null").trim();
//                hline = hline.replaceAll("\"", "").trim();
//                hline = hline.replaceAll("\u0001", ",").trim();
                if (hline.endsWith(","))
                    hline+="null";
                hline=hline.replace("null","NULL");
//                hline=hline.split(",")[0];
                hiveSet.add(hline);
            }
            hiveSet.remove("");

            System.out.println("Presto Rows : "+prestoSet.size());
            System.out.println("Hive Rows : " + hiveSet.size());


            if (!Objects.equals(hiveSet,prestoSet))
                System.err.println("Not same!!!!");
            if (!prestoSet.containsAll(hiveSet)){
                prestoSet.removeAll(hiveSet);

                System.out.println("Different Rows : " +prestoSet.size());
                System.out.println(prestoSet);
//                return  false;;
            }else
                System.out.println("All Columns : "+prestoSet.size()+" --Compare Success--");

        }
        return true;

    }


    public static void main(String[] args) throws IOException {
        ArrayList<String> prestoDir=new ArrayList<>();
        ArrayList<String> hiveDir=new ArrayList<>();

        File presto=new File("src/test/resources/presto");
        File hive=new File("src/test/resources/hive");

        for (File tmp:presto.listFiles()){
            if (null!=tmp){
                prestoDir.add(tmp.getAbsolutePath());
            }
        }
        for (File tmp:hive.listFiles()){
            if (null!=tmp){
                hiveDir.add(tmp.getAbsolutePath());
            }
        }

        Result_Compare compare=new Result_Compare();
        boolean  consistency=compare.compareCSV(prestoDir, hiveDir);

        if (consistency)
            System.out.println("ALL  Compare Success!!!!");
        else
            System.out.println("Compare Failed !!!!");
    }
}
