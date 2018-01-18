/*
 * FASTAReader.java
 *
 * Created on November 12, 2003, 3:44 PM
 */

package prophyler.tool;

import prophyler.entity.*;
import prophyler.exception.*;
import prophyler.tool.*;
import java.util.*;
import java.io.*;

/** Reads a FASTA file and weights file.  Returns a list of Node instances,
 * each of which has an ID, sequence, and weight associated with it.
 * @author  rkwok
 */
public class FASTAReader {
    private String fastaFile = "";
    //private String weightFile = "";
    private HashMap weightMap;

    /** Creates a new instance of FASTAReader.
     * @param fastaFile Path to a FASTA file in the following format:<p>
     * <pre>
     * >ID
     * sequence
     * >ID
     * sequence
     * ...
     * </pre>
     * @param weightFile Path to a weights file in the following format:<p>
     * <pre>
     * ID	weight
     * ID	weight
     * ...
     * </pre>
     * where the IDs match the IDs in the FASTA and the weights add up to 1.0.
     */
    /*
    public FASTAReader(String fastaFile, String weightFile) {
        this.fastaFile = fastaFile;
        this.weightFile = weightFile;
    }
    */

    public FASTAReader(String fastaFile, HashMap weights) {
        this.fastaFile = fastaFile;
        this.weightMap = weights;
    }

    /** Reads the FASTA and weights files into a list of Nodes.  Each Node
     * has an ID, sequence, and weight associated with it.
     */
    public ArrayList getNodes() throws ProphylerException {

        //readWeightFile();
        readWeightMap();

        // open the FASTA
        SimpleFileReader reader = new SimpleFileReader(fastaFile);
        String line = reader.readLine();

        ArrayList nodeList = new ArrayList();
        int index;
        String ID = "";
        String defline = "";
        String sequence = "";
        double weight;

        while (line != null) {
            // ignore comment lines beginning with ;
            if (line.startsWith(";")) {
                line = reader.readLine();
            }
            // read defline
            else if (line.startsWith(">")) {
                index = line.lastIndexOf(">");
                defline = line.substring(index + 1); // get ID after >  - KK and before first space!
                ID = defline.split("\\s")[0];
                line = reader.readLine();
            }
            // read sequence (can be spread over multiple lines)
            else {
                while ((line != null) && (!line.startsWith(">"))) {
                    sequence = sequence + line;
                    line = reader.readLine();
                }

                // if a weight can't be found for that ID, throw an exception
                if (!weightMap.containsKey(ID)) {
                    throw new ProphylerException(
                            "No weight provided for this ID: " + ID);
                }

                weight = ((Double) weightMap.get(ID)).doubleValue();
                Node node = new Node(ID, sequence, weight); // create a new Node
                nodeList.add(node); // add it to the list
                sequence = "";
            }
        }

        return nodeList;
    }


    /* Reads the weight HashMap, where the ID is the key and the
     * weight is the value.
     */
    private void readWeightMap() throws ProphylerException {

       double totalWeight = 0.0;
       Double weight;
       String ID;

       Iterator i = weightMap.keySet().iterator();
       while(i.hasNext()){
           ID = (String) i.next();
           weight = (Double) weightMap.get(ID);
            totalWeight = totalWeight + weight.doubleValue();
       }

        // If the weights don't add up to 1.0, readjust them by dividing them by the total weight
        if (totalWeight != 1.0) {
            Iterator keyIter = weightMap.keySet().iterator();
            while (keyIter.hasNext()) {
                ID = (String) keyIter.next();
                weight = (Double) weightMap.get(ID);
                weight = new Double(weight.doubleValue() / totalWeight);
                weightMap.put(ID, weight);
            }
        }
    }

}
