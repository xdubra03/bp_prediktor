/*
 * EditSet.java
 *
 * Created on July 22, 2003, 11:51 AM
 */

package prophyler.entity;

import prophyler.tool.*;
import prophyler.exception.*;
import java.util.*;
import java.sql.*;
import java.lang.*;

/** This class represents an edit set.  It may be either a master set (containing all the sequences
 * in that query) of a subset (containing only a portion of the sequences).  An edit set contains
 * a sequence alignment where each sequence has an assigned weight.  It can be initialized
 * with data from the database or from a FASTA file.
 * @author  rkwok
 */
public class EditSet {


    private String ID;
    private ArrayList nodeList = null;
    private ArrayList columns = null;
    private int leadingSequenceLength = -1;
    private int numSequences = -1;


    /** Creates a new instance of EditSet, initialized with data from a FASTA and weight file.
     * @param fastaFile Path to a file containing a sequence alignment in FASTA format
     * @param weightFile Path to a file containing a mapping from sequence IDs to their weights

    public EditSet(String fastaFile, String weightFile) throws ProphylerException {
        FASTAReader reader = new FASTAReader(fastaFile, weightFile);
        nodeList = reader.getNodes();
    }
    */


    /** Creates a new instance of EditSet, initialized with data from a FASTA and weight hashmap
     * @param fastaFile Path to a file containing a sequence alignment in FASTA format
     * @HashMap - mapping from sequence IDs to their weights
     */
    public EditSet(String fastaFile, HashMap weights) throws ProphylerException {
        FASTAReader reader = new FASTAReader(fastaFile, weights);
        nodeList = reader.getNodes();
    }

    /** Returns the ID number of the edit set as a String.
     */
    public String getID() {
        return ID;
    }


    /**  gets a list of all nodes (aka sequences)
     */
    public ArrayList getNodeList() throws ProphylerException {

        return nodeList;
    }

    /** Returns an ArrayList of Column instances, one for each position in the alignment.
     * @throws ProphylerException If an error occurs while attempting to construct the columns
     */
    public ArrayList getColumns() throws ProphylerException {
        if (columns == null) {
            if (nodeList == null) {
                getNodeList();
            }

            if (leadingSequenceLength == -1) {
                getLeadingSequenceLength();
            }

            columns = new ArrayList();

            for (int i = 0; i < leadingSequenceLength; i++) {
                columns.add(new Column(nodeList, i));
            }
        }

        return columns;
    }

    /** Returns the number of sequences in the edit set.
     * @throws ProphylerException If an error occurs while attempting to get the size of the
     * node list
     */
    public int getNumSequences() throws ProphylerException {
        if (numSequences == -1) {
            if (nodeList == null) {
                getNodeList();
            }

            numSequences = nodeList.size();
        }

        return numSequences;
    }

    /** Returns the length of the leading sequence, where the leading sequence is arbitrarily chosen
     * as the first Node in the list.
     * @throws ProphylerException If an error occurs while attempting to get the length of the
     * sequence
     */
    public int getLeadingSequenceLength() throws ProphylerException {
        if (leadingSequenceLength == -1) {
            if (nodeList == null) {
                getNodeList();
            }

            Node firstNode = (Node)nodeList.get(0);
            leadingSequenceLength = firstNode.getSequenceLength();
        }

        return leadingSequenceLength;
    }


}
