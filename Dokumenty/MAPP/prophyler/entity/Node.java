/*
 * Node.java
 *
 * Created on July 22, 2003, 5:10 PM
 */

package prophyler.entity;

import java.sql.*;
import java.util.*;
import prophyler.tool.*;
import prophyler.exception.*;

/** This class represents a sequence that is part of a ProPhylER analysis, also known as a node.
 * It contains an aligned sequence string with gaps inserted, and is associated with a particular
 * weight in relation to other nodes in the same edit set.
 * @author  rkwok
 */
public class Node {
    //private PConnection pconn;
    //private StatementUtil stmtUtil;

    private String ID;
    private String sequence = null;
    private int sequenceLength = -1;
    private double weight = -1;



    /** Creates a new instance of Node.  Use this constructor when initializing a Node
     * with data from a FASTA file.
     * @param ID The ID number of the node as a String
     * @param sequence The aligned (gapped) sequence string
     * @param weight The weight of the sequence (must be <1)
     */
    public Node(String ID, String sequence, double weight) {
        this.ID = ID;
        this.sequence = sequence;
        sequenceLength = sequence.length();
        this.weight = weight;
    }

    /** Returns the ID number of the node.
     */
    public String getID() {
        return ID;
    }

    /** get the sequence string.
     * @return The sequence string
     * @throws ProphylerException If the sequence string cannot be obtained
     */
    public String getSequence() throws ProphylerException {

        return sequence;
    }

    /** Returns the length of the sequence (including gaps).
     */
    public int getSequenceLength() throws ProphylerException {
        if (sequenceLength == -1) {
            if (sequence == null) {
                getSequence();
            }

            sequenceLength = sequence.length();
        }

        return sequenceLength;
    }

    /** get the weight of that node relative to other nodes in the
     * edit set.
     * @return The weight of the node
     * @throws ProphylerException If the weight cannot be obtained
     */
    public double getWeight() throws ProphylerException {

        return weight;
    }
}
