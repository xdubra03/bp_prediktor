/*
 * Column.java
 *
 * Created on August 26, 2003, 4:50 PM
 */

package prophyler.entity;

import java.util.*;
import java.sql.*;

import prophyler.tool.*;
import prophyler.exception.*;

/** This class represents a column of a sequence alignment; that is, the amino acids present at a
 * particular position in the alignment.
 * @author  rkwok
 */
public class Column {
    private ArrayList nodes;
    private int position;
    private int numSequences;
    private char[] aminoAcids;
    private double[] weights;
    private double[] adjustedWeights;
    private double[] aaComposition;
    private double gapWeight = -1;

    private final char[] aminoAcidList = { 'A', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'K', 'L', 'M', 'N', 'P', 'Q', 'R', 'S', 'T', 'V', 'W', 'Y' };
    private final int numAminoAcids = 20;

    /** Creates a new instance of Column.  The input ArrayList must contain Node instances.
     */
    public Column(ArrayList nodes, int position) {
        this.nodes = nodes;
        this.position = position;
        numSequences = nodes.size();
    }

    /** Returns an ArrayList of the Nodes in that alignment.
     */
    public ArrayList getNodes() {
        return nodes;
    }

    /** Returns the position of the column in the alignment.
     */
    public int getPosition() {
        return position;
    }

    /** Returns the number of sequences (aka Nodes) in the alignment.
     */
    public int getNumSequences() {
        return numSequences;
    }

    /** Creates a char array of the amino acids in the column.  It gets the char verbatim
     * from the sequence string, so gaps will be recorded as hyphens.
     * @return A char array of the amino acids at that position
     * @throws ProphylerException If the amino acid data cannot be determined
     */
    public char[] getAminoAcids() throws ProphylerException {
        if (aminoAcids == null) {
            aminoAcids = new char[numSequences];
            Node node;
            String sequence;

            for (int i = 0; i < numSequences; i++) {
                node = (Node)nodes.get(i);
                sequence = node.getSequence();
                aminoAcids[i] = sequence.charAt(position);
            }
        }

        return aminoAcids;
    }

    /** @return A double array containing the weight of each node in the alignment.
     * @throws ProphylerException If an error occurs while attempting to get the weight data
     */
    public double[] getWeights() throws ProphylerException {
        if (weights == null) {
            weights = new double[numSequences];
            Node node;
            double nodeWeight;

            for (int i = 0; i < numSequences; i++) {
                node = (Node)nodes.get(i);
                nodeWeight = node.getWeight();
                weights[i] = nodeWeight;
            }
        }

        return weights;
    }

    /** Determines what percentage of the amino acids in the column are actually gaps
     * (represented by hyphens).  It adds up the weights of the corresponding sequences
     * to determine the summed gap weight.
     * @return The gap weight as a double
     * @throws ProphylerException If an error occurs while attempting to calculate the gap weight
     */
    public double getGapWeight() throws ProphylerException {
        if (gapWeight == -1) {
            if (aminoAcids == null) {
                getAminoAcids();
            }
            if (weights == null) {
                getWeights();
            }

            gapWeight = 0;
            double nodeWeight;

            for (int i = 0; i < numSequences; i++) {
                if (aminoAcids[i] == '-') {
                    nodeWeight = weights[i];
                    gapWeight = gapWeight + nodeWeight;
                }
            }
        }

        return gapWeight;
    }

    /** Having determined the gap weight, generates an adjusted weight array where the
     * sequences with gaps are given weight 0 and the rest are given proportionally
     * inflated weights so that the sum is still 1.
     * @return An adjusted weight array, where each entry corresponds to the sequence with
     * the same index number in the array of nodes
     * @throws ProphylerException If the current weight for each node cannot be determined
     */
    public double[] adjustWeightsForGaps() throws ProphylerException {
        if (weights == null) {
            getWeights();
        }

        adjustedWeights = weights;

        if (gapWeight == -1) {
            getGapWeight();
        }

        double currWeight;
        double adjustedWeight;
        double remainingWeight = 1.0 - gapWeight;

        for (int i = 0; i < numSequences; i++) {
            currWeight = adjustedWeights[i];

            if (aminoAcids[i] == '-') {
                adjustedWeight = 0;
            }
            else {
                adjustedWeight = currWeight / remainingWeight;
            }
            adjustedWeights[i] = adjustedWeight;
        }

        return adjustedWeights;
    }

    /** For each possible amino acid, counts the number of times that amino acid appears in the
     * column, and adds up the weights of the nodes to get the "weight" of that amino acid in the
     * column.  User can specify whether to use adjusted weights or not (note that getAdjustedWeights()
     * must be called first).  User can also specify whether to adjust the final numbers for soft
     * counts.
     * @return An array of size 20 where each entry corresponds to an amino
     * acid, alphabetically ordered, and the value is the weight of that amino acid.
     * @throws ProphylerException If an error occurs while attempting to determine the amino
     * acid composition
     */
    public double[] getAminoAcidComposition(boolean useAdjustedWeights, boolean adjustForSoftCounts, double softCountFactor) throws ProphylerException {
        if (useAdjustedWeights && (adjustedWeights == null)) {
            throw new ProphylerException("Adjusted weights have not yet been initialized");
        }

        if (aminoAcids == null) {
            getAminoAcids();
        }
        if (weights == null) {
            getWeights();
        }

        aaComposition = new double[numAminoAcids];
        double seqWeight;
        char aminoAcid;
        int aminoAcidNum;
        double currWeight;

        for (int i = 0; i < numAminoAcids; i++) {
            aaComposition[i] = 0;
        }

        for (int j = 0; j < numSequences; j++) {
            if (useAdjustedWeights) {
                seqWeight = adjustedWeights[j];
            }
            else {
                seqWeight = weights[j];
            }

            aminoAcid = aminoAcids[j];

            if (aminoAcid != '-') {
                aminoAcidNum = new String(aminoAcidList).indexOf(aminoAcid);
                currWeight = aaComposition[aminoAcidNum];
                aaComposition[aminoAcidNum] = currWeight + seqWeight;
            }
        }

        double currAAWeight;

        if (adjustForSoftCounts) {
            for (int k = 0; k < numAminoAcids; k++) {
                currAAWeight = aaComposition[k];
                aaComposition[k] = ((1 - softCountFactor) * currAAWeight) + (softCountFactor / numAminoAcids);
            }
        }

        return aaComposition;
    }

    /** Given a position in the alignment and a property identifier, it gets the property value for
     * each amino acid in that column by consulting a static property table.
     * @return An array of property values for the amino acids in that column, obtained by
     * consulting a static table
     * @throws ProphylerException If an error occurs while attempting to read the property
     * values
     */
    public double[] getPropertyValues(double[][] propertyTable, int propertyRow) throws ProphylerException {
        if (aminoAcids == null) {
            getAminoAcids();
        }

        double[] propertyValueList = new double[numSequences];
        char aminoAcid;
        int aminoAcidID;
        double propertyValue;

        for (int i = 0; i < numSequences; i++) {
            aminoAcid = aminoAcids[i];
            if (aminoAcid == '-') {
                propertyValue = 0;
            }
            else {
                aminoAcidID = new String(aminoAcidList).indexOf(aminoAcid);  // gets the integer corresponding to the amino acid char
                propertyValue = propertyTable[propertyRow][aminoAcidID];
            }
            propertyValueList[i] = propertyValue;
        }

        return propertyValueList;
    }
}
