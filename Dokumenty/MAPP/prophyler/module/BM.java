package prophyler.module;

import java.io.*;
import java.util.*;
import java.math.*;


/* This program calcuates the weights given a tree with branch lengths.
* <p>Copyright: Copyright (c) 2005  Eric Stone and Arend Sidow, Stanford University </p>
* <p>University: Stanford University</p>
*/



public class BM {

    private String parentreeFile = "";
    //private String weightFile = "";

    public BM(String paren) {
        this.parentreeFile = paren;
        //this.weightFile = weightFile;
    }


    /* This method accepts a parenthesis tree and returns the
     * String of the output file with the BM weights.
     */
    public HashMap runBM() {

        // read in parenthesis tree
        String pstring = "";
        HashMap hm = new HashMap();

        try {

            //PrintWriter pw = new PrintWriter(new FileOutputStream(weightFile));

            FileInputStream fis = new FileInputStream(parentreeFile);
            InputStreamReader isr = new InputStreamReader(fis);
            BufferedReader in = new BufferedReader(isr);
            String line = "";

            boolean start = false;
            int leftparen = 0;
            int rightparen = 0;


            while ((line = in.readLine()) != null) {

               if (line.startsWith("(") && line.endsWith(";")) {  //semphy type output
                   pstring += line.trim();
                   break;
               }else{                                              //clustalw output
                   if (line.startsWith("(")) {
                       start = true;
                   }
                   if(start){
                       line.replaceAll("\r","");                  //replace carraige return with nothing
                       line.replaceAll("\n","");                  //replace new-line with nothing
                       pstring += line.trim();

                   }
               }
            }
            fis.close();
            isr.close();
            in.close();
           // System.out.println("In BM ptree is :  "+ pstring);
           // pstring += "\n";

           //check if number of left parenthese ((( match the number of ))) right parentheses
           char[] chars = pstring.toCharArray();
            for (int x = 0; x < chars.length; x++) {
                if ( chars[x] == '(' )  leftparen++;
                if ( chars[x] == ')' )  rightparen++;
            }

           if(leftparen != rightparen){
               System.err.println(
                  "Left and Right number of parentheses do not match in the provided parenthesis tree :" + pstring);
                     System.exit(1);
           }

           //Start BM processing
            GeneTree gt = new GeneTree();
            boolean chk = gt.GenerateGeneTree(pstring);
            if (!chk) {
                System.err.println(
                        "Could not construct a tree from provided parenthesis tree :" + pstring);
                System.exit(1);
            }

            if (gt.hasLengths) {
                gt.MidPointTree();
            } else {
                System.err.println("Provided tree has missing lengths");
                System.exit(1);
            }

            int parent_nodes[] = new int[gt.AllNodes.size() + 1];
            double distances[] = new double[gt.AllNodes.size() + 1];
            parent_nodes[0] = -1;
            distances[0] = -1.0;
            for (int i = 0; i < gt.AllNodes.size(); ++i) {
                GTNode g = (GTNode) gt.AllNodes.elementAt(i);
                if (g.parent != null) {
                    parent_nodes[g.id] = g.parent.id;
                    distances[g.id] = g.length_to_parent;
                } else {
                    parent_nodes[g.id] = 0;
                    distances[g.id] = 0.0;
                }
            }

            BranchManager bm = new BranchManager(parent_nodes, distances);
            double weights[] = bm.calculateWeights();

            for (int i = 0; i < gt.AllNodes.size(); ++i) {
                GTNode g = (GTNode) gt.AllNodes.elementAt(i);
                if (g.isLeaf) {
                    BigDecimal bd = new BigDecimal(weights[g.id]);
                    bd = bd.setScale(4, BigDecimal.ROUND_HALF_UP);
                    String shortname = g.name.split("\\s")[0];
                    hm.put(new String(shortname), new Double(bd.doubleValue()));
                    //System.out.println(shortname + "\t" + bd.toString());
                    //pw.println(shortname + "\t" + bd.toString());
                    //pw.flush();
                } else {
                    break;
                }
            }
            //pw.close();
        }  catch (Exception e) {
            System.err.println("Unable to read input file: " + parentreeFile);
            System.exit(1);
        }
        return hm;
    }
}
