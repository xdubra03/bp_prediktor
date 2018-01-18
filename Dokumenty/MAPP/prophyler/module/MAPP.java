/*
 * MAPP.java
 * Created on June 30, 2003, 1:05 PM
 */

package prophyler.module;

import java.io.*;
import java.lang.*;
import java.util.*;
import java.math.*;
import java.text.*;

import Jama.Matrix; // a matrix library
import JSci.maths.statistics.FDistribution; // a statistics library

import prophyler.tool.*;
import prophyler.entity.*;
import prophyler.exception.*;

/* This module reads a protein sequence alignment and uses an algorithm to predict the
 * relative deleteriousness of all possible point mutations in each position of the alignment.
 * It bases these predictions on a set of preselected scales (properties) for which each amino
 * acid has a numeric value.  A separate set of predictions is generated for each alignment.
 * The program can accept an alignment from a FASTA file.
 * The algorithm also requires that a tree with branch lengths for each sequence in the alignment is provided.<p>
 * Open <a href="http://mendel.stanford.edu/ProPhylER/Documentation/cSNPer.html">this</a> link
 * in a new window for a description of the algorithm.<p>
 * java -jar MAPP.jar <followed by command-line arguments>
 * </pre>
 * Type the above command followed by "-h" to see the list of available command-line options.<p>
 *
 * <p>Copyright: Copyright (c) 2005  Eric Stone and Arend Sidow, Stanford University </p>
 *
 * <p>University: Stanford University</p>

 * @author  rkwok
 */
public class MAPP extends ProphylerModule {

    boolean fastaProvided = false; // option to provide sequences in a FASTA file instead of reading from the database
    String fastaFile = ""; // path to the FASTA file
    String ptreeFile = ""; // file containing parenthesis tree (to support FASTA file)
    boolean writeToFile = false; // option to write prediction results to file
    String outputFilePath = ""; // path to write output file
    boolean printToScreen = false; // option to print results to screen

    // Output file
    File outputFile;
    FileWriter writer;

    final double[][] masterPropertyTable = {

                                           {0.7667, 0.607, -0.0529, -1.2721,
                                           -0.91777, 0.67374}, {1.001, 0.6887,
                                           -0.0529, -0.79141, 0.34486, -0.80639},
                                           { -1.0077, -1.6003, -2.1704, -0.7286,
                                           0.32539, 1.4671}, { -1.0077, -1.3959,
                                           -2.1704, -0.069091, -1.6297, 0.69506},
                                           {1.1015, 1.0362, -0.0529, 1.175,
                                           -0.13071, -1.0111}, {0.0301, 0.4844,
                                           -0.0529, -1.9606, 1.1375, 0.77611},
                                           { -0.9073, -0.3331, 1.0058, 0.28844,
                                           0.13628, -0.068461}, {1.6706, 0.9136,
                                           -0.0529, 0.61457, -0.52007, -1.4462},
                                           { -1.1416, -1.5185, 2.0646, 0.66047,
                                           -0.3838, 0.4434}, {1.4362, 0.8522,
                                           -0.0529, 0.61457, -0.72031, -0.56752},
                                           {0.8001, 0.9749, -0.0529, 0.52277,
                                           -0.85937, -0.46942}, { -1.0077,
                                           -0.701, -0.0529, -0.65612, 0.42551,
                                           1.1728}, { -0.3716, 0.2391, -0.0529,
                                           -0.68995, 3.3151, 2.0899}, { -1.0077,
                                           -0.5579, -0.0529, 0.061361, -0.4422,
                                           0.54577}, { -1.3425, -2.2338, 2.0646,
                                           0.77643, -0.69528, 0.23012},
                                           { -0.1038, 0.4026, -0.0529, -1.2625,
                                           0.4867, 0.27704}, { -0.0703, 0.5252,
                                           -0.0529, -0.60781, 0.36433, -0.58885},
                                           {1.5702, 0.8114, -0.0529, -0.030439,
                                           -0.13071, -1.604}, { -0.1373, 0.6683,
                                           -0.0529, 2.0906, -0.17521, -0.71255},
                                           { -0.2712, 0.1369, -0.0529, 1.2644,
                                           0.069528, -1.0964}

    }; // 2-D array for holding amino acid property values


    int totalNumProperties; // number of properties in master table

    HashMap propertyMap;
    boolean useScales = false;
    NumberFormat formatter = new DecimalFormat("0.###E0");

    // Properties requested specifically by the user
    int[] requestedProperties; // list of column indexes of specific properties
    double[][] requestedPropertyTable; // 2-D array for holding only the requested property values (subset of masterPropertyTable)
    int numRequestedProperties = 0; // number of properties requested specifically by the user

    // Matrix indicating how each property is correlated with other properties
    double[][] propertyCorrelationTable;

    // Information about the edit set being evaluated
    int numSequences; // number of sequences in the alignment
    int sequenceLength; // length of the leading sequence

    // Results generated by the analysis (regenerated for each edit set)
    double[] colScores; // raw "distance" score for each column
    double[] colProbs; // p-values of the above
    double[] logColProbs; // natural log of the above
    double[] smoothedLogColProbs; // smoothed values of the above
    double[] smoothedNormLogColProbs; // normalized values of the above
    double[][] meanPropTable; // mean values for each property
    double[][] propVarianceTable; // variance for each property
    double[][] colPropProbTable;
    double[][] cSNPScoreTable; // raw "distance" score for each SNP
    double[][] cSNPProbTable; // p-values of the above
    ArrayList goodAAs; // list of amino acids considered tolerable for a particular position
    ArrayList badAAs; // list of amino acids considered deleterious for a particular position

    // Some constants
    final double VARIANCE_CORRECTION_FACTOR = 0; // Fudge factor used to correct variance in 100% conserved columns
    final double SUBSTITUTION_RATE_THRESHOLD = 1.0; // Minimum substitution rate required to process edit set
    final double SOFT_COUNT_FACTOR = 0.01; // Fudge factor to adjust weights
    final double P_VALUE_CUTOFF = 0.01; // Used to determine cut-offs for cSNP impact significance
    final double GAP_WEIGHT_THRESHOLD = 0.5; // Used to flag columns with high gap weight
    //final int SMOOTHING_WINDOW = 9; // Size of window for smoothing log of column p-values
    //final int MIDPOINT = SMOOTHING_WINDOW / 2; // The "midpoint" of the smoothing window, rounded down

    // A char array of the 20 amino acids
    final char[] aminoAcidList = {'A', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'K',
                                 'L', 'M', 'N', 'P', 'Q', 'R', 'S', 'T', 'V',
                                 'W', 'Y'};
    int numAminoAcids = 20;


    /** Creates a new instance of MAPP. */
    public MAPP() {
    }

    /** Creates an instance of MAPP and calls run().
     */
    public static void main(String[] args) {
        MAPP predictor = new MAPP();
        predictor.run(args);
    }

    /** Prints explanation of command-line argument use.
     */
    protected void showHelp() {
        System.out.println("Usage: ");
        System.out.println("java -jar MAPP.jar -f <alignment> -t <tree>");
        System.out.println();
        System.out.println("Options:");
        System.out.println();
        System.out.println("-f  <alignment_file> (required)");
        System.out.println("        path to text file containing sequence alignment in Fasta format");
        System.out.println();
        System.out.println("-t <tree_file> (required)");
        System.out.println( "       path to text file containing parenthesis tree");
        System.out.println();
        System.out.println("-s <column numbers of scales to use, separated by colons>");
        System.out.println("     defaults to all available scales");
        System.out.println();
        System.out.println("-o    <output file>");
        System.out.println("      if not specified, output goes to stdout");
        System.out.println();
        System.out.println("-h ");
        System.out.println("      display help");
    }

    /** Performs all the initialization events specific to cSNPImpactPreditor.  Processes
     * command-line arguments, reads the input property file, and calculates property
     * correlations.  If the user has selected to read the alignment data from the database,
     * it logs the user into the treedb database and registers a module session.
     */
    protected void init(String[] args) throws ProphylerException {
        processCommandLineArgs(args);
        readPropertyFile(); //matrix is hard-coded
        generateRequestedPropertyTable();
        calculatePropertyCorrelations();
        loadPropertyNames();
    }

    /* Processes all the command-line options and sets internal variables accordingly.
     * Throws an exception if the user specifies an invalid argument or an invalid
     * combination of arguments.
     */
    private void processCommandLineArgs(String[] args) throws
            ProphylerException {

        if (args.length < 4) {

            System.out.println("Usage: ");
            System.out.println("java -jar MAPP.jar -f <alignment> -t <tree>");
            System.exit(0);
        }
        for (int i = 0; i < args.length; i++) {

            if (args[i].equals("-f")) {
                fastaProvided = true;
                fastaFile = args[++i];
            }

            else if (args[i].equals("-t")) {
                ptreeFile = args[++i];
            } else if (args[i].equals("-s")) {
                String properties = args[++i];
                useScales = true;
                parseScales(properties);
            } else if (args[i].equals("-o")) {
                writeToFile = true;
                outputFilePath = args[++i];
                outputFile = new File(outputFilePath);
                if (outputFile.exists()) {
                    System.out.println(
                            "Output file already exists: " + outputFilePath);
                    System.exit(0);
                }

                try {
                    writer = new FileWriter(outputFile, true);
                } catch (IOException e) {
                    throw new ProphylerException(
                            "Unable to open output file at " + outputFilePath,
                            e);
                }
            } else {
                throw new ProphylerException(
                        "Invalid command-line arguments specified");
            }
        }

        if (fastaProvided && (ptreeFile == "")) {
            throw new ProphylerException("Must specify paren tree file");
        }

        if (!fastaProvided && (ptreeFile != "")) {
            throw new ProphylerException(
                    "Cannot specify paren tree file unless FASTA option is also specified");
        }
        if (!writeToFile) {
            printToScreen = true;
        }
    }

    /* Parses the -scales command-line argument to get a list of requested properties.  The
     * parsed numbers represent the column indexes of the properties to be used from the
     * master property table.
     */
    private void parseScales(String properties) throws ProphylerException {
        String[] propertyArray = properties.split(":"); // property indexes are delimited by colons
        String propertyString;
        int property;

        numRequestedProperties = propertyArray.length;
        requestedProperties = new int[numRequestedProperties];

        for (int i = 0; i < propertyArray.length; i++) {
            propertyString = propertyArray[i];

            try {
                // the user is specifying an index that starts at 1 instead of 0, so we have to subtract 1
                property = (new Integer(propertyString).intValue()) - 1;
            } catch (NumberFormatException e) {
                throw new ProphylerException(
                        "Unable to process -s <scales> argument; invalid number or delimiter specified");
            }

            requestedProperties[i] = property;
        }
    }

    /* Reads the input file of property values into an array.  Expects each row to correspond
     * to an amino acid, each column to correspond to a property, and column values to be
     * delimited by tabs or whitespace (no column or row headings).
     */

    private void readPropertyFile() throws ProphylerException {
        //masterPropertyTable = TextUtil.getNumArray(masterPropertyFile, false);  // a utility to read a table from a file
        double[] sampleRow = masterPropertyTable[0];
        totalNumProperties = sampleRow.length; // get the number of columns; this is equal to the number of properties
    }


    /* Creates a table of only the properties to be used for this analysis.
     */
    private void generateRequestedPropertyTable() throws ProphylerException {
        if (numRequestedProperties == 0) { // if the user didn't request any specific properties...
            requestedPropertyTable = masterPropertyTable; // use all of the properties available in the master file
            numRequestedProperties = totalNumProperties;
        } else { // if they did request specific properties...
            requestedPropertyTable = new double[numAminoAcids][
                                     numRequestedProperties];

            int property;
            double[] propertyValues;

            // go through the master property table and only pull out the requested property values
            for (int i = 0; i < numRequestedProperties; i++) {
                property = requestedProperties[i];

                if (property >= totalNumProperties) {
                    throw new ProphylerException("Invalid property number " +
                                                 (property + 1) +
                                                 " requested; only 1 through " +
                                                 (totalNumProperties) +
                                                 " available");
                }

                for (int aa = 0; aa < numAminoAcids; aa++) {
                    requestedPropertyTable[aa][i] = masterPropertyTable[aa][
                            property];
                }
            }
        }
    }

    /* Uses matrix calculations to figure out the correlations between the requested properties.
     */
    private void calculatePropertyCorrelations() throws ProphylerException {
        // Convert the property table to a matrix
        Matrix propertyMatrix = new Matrix(requestedPropertyTable);
        Matrix transPropertyMatrix = propertyMatrix.transpose();

        // Calculate the correlation between the properties using matrix algebra formula
        double numAminoAcidsMinus1 = numAminoAcids - 1.0;
        propertyCorrelationTable = transPropertyMatrix.times(propertyMatrix).
                                   times(1.0 / numAminoAcidsMinus1).getArray();
    }

    /* To be able to print the names of the property, load the names
     */
    private void loadPropertyNames() throws ProphylerException {

        propertyMap = new HashMap();
        propertyMap.put("0", "Hydropathy");
        propertyMap.put("1", "Polarity");
        propertyMap.put("2", "Charge");
        propertyMap.put("3", "Volume");
        propertyMap.put("4", "Free energy alpha");
        propertyMap.put("5", "Free energy beta");

    }


    /** Decides how to kick off the query processing.  If the user has provided a FASTA file,
     * it processes the FASTA file.  If the user has provided a single query ID using the
     * -qsid flag, it calls processQuery() on that ID.  Otherwise, it calls
     * processAllQueries() to automatically get all the valid queries from the
     * database.
     */
    protected void process() throws ProphylerException {
        if (fastaProvided) {
            processFASTA(fastaFile);
        }
    }


    /* If the user has provided a FASTA, initialize the EditSet by giving it
     * the FASTA, then process it.
     */
    private void processFASTA(String fastaFile) throws ProphylerException {

        //generate the weightFile and pass the fileName

        BM bm = new BM(ptreeFile);
        HashMap bmwt = bm.runBM();
        //EditSet editSet = new EditSet(fastaFile, weightFilePath);
        EditSet editSet = new EditSet(fastaFile, bmwt);
        processEditSet(editSet);
    }

    /* Gets the sequence data associated with each edit set, calculates the scores, and writes the
     * output.
     */
    private void processEditSet(EditSet editSet) throws ProphylerException {

        // Get data about the edit set
        ArrayList columns = editSet.getColumns();
        sequenceLength = editSet.getLeadingSequenceLength();
        numSequences = editSet.getNumSequences();

        // Initialize the results data structures and run the analysis
        initializeOutputTables();
        evaluateColumns(columns);
        calculatePValues();
        //massagePValues();
        determineCutoffs();

        // Write the output to the screen, output file
        writeOutput(editSet);
    }

    /* Resets the necessary results tables.
     */
    private void initializeOutputTables() {
        cSNPScoreTable = new double[sequenceLength][numAminoAcids];
        colScores = new double[sequenceLength];

        //logColProbs = new double[sequenceLength];
        //smoothedLogColProbs = new double[sequenceLength];
        //smoothedNormLogColProbs = new double[sequenceLength];

        meanPropTable = new double[sequenceLength][numRequestedProperties];
        propVarianceTable = new double[sequenceLength][numRequestedProperties];

        goodAAs = new ArrayList();
        badAAs = new ArrayList();
    }

    /* Iterates through each column in the alignment and evaluates it.
     */
    private void evaluateColumns(ArrayList columns) throws ProphylerException {
        Column column;

        for (int i = 0; i < columns.size(); i++) {
            column = (Column) columns.get(i);
            evaluateColumn(column);
        }
    }

    /* Evaluates the composition of the column and calculates scores.
     */
    private void evaluateColumn(Column column) throws ProphylerException {
        int position = column.getPosition();

        // Evaluate the column and make weight adjustments as necessary
        double gapWeight = column.getGapWeight();
        double[] adjustedWeights = column.adjustWeightsForGaps();
        double[] aaComposition = column.getAminoAcidComposition(true, true,
                SOFT_COUNT_FACTOR);

        // Calculate the scores for the column
        calculateScoresForColumn(aaComposition, position);
    }

    /* Calculates mean property values, property value variance, cSNP scores, and overall column
     * score using matrix algebra.
     */
    private void calculateScoresForColumn(double[] aaComposition, int position) throws
            ProphylerException {
        // R is the property correlation matrix
        Matrix R = new Matrix(propertyCorrelationTable);
        Matrix invR = R.inverse();

        // A is the property table, standardized (20 x p)
        Matrix A = new Matrix(MathUtil.standardize(requestedPropertyTable,
                VARIANCE_CORRECTION_FACTOR));

        // W is the amino acid composition (20 x 20)
        Matrix W = MatrixUtil.getDiagonalMatrix(aaComposition);

        // X is the property table (20 x p)
        Matrix X = new Matrix(requestedPropertyTable);
        Matrix transX = X.transpose();

        // O is a unit matrix of 1's (20 x 1)
        Matrix O = new Matrix(numAminoAcids, 1, 1);
        Matrix transO = O.transpose();

        // Calculate the mean property values (1 x p)
        Matrix mean = calculateMeanPropertyValues(transO, W, X, position);
        Matrix transMean = mean.transpose();

        // Calculate the variance in property values
        double[] variance = calculatePropertyVariance(X, transX, W, O, transO,
                position);
        double[] invSqrtVariance = MathUtil.invSqrt(variance);
        Matrix invSqrtVarianceMatrix = MatrixUtil.getDiagonalMatrix(
                invSqrtVariance);

        // Update the variance results table for that position
        propVarianceTable[position] = variance;

        // Calculate the distance of each amino acid's property value from weighted mean
        Matrix M = X.minus(O.times(mean));
        Matrix transM = M.transpose();

        // Calculate the score for each possible SNP
        calculateSNPScores(M, transM, invSqrtVarianceMatrix, invR, position);

        // Calculate the score for a SNP with all property values = 0 (the overall column score)
        calculateColumnScore(position);
    }

    /* Calculates the mean property values in the column and writes them to one of the results
     * tables.
     */
    private Matrix calculateMeanPropertyValues(Matrix transO, Matrix W,
                                               Matrix X, int position) {
        // Calculate the mean
        Matrix mean = transO.times(W).times(X);

        // Update the mean results table for that position
        double[][] meanArray = mean.getArray();
        meanPropTable[position] = meanArray[0];

        return mean;
    }

    /* Calculates the variance in each property in the column and writes them to one of the results
     * tables.
     */
    private double[] calculatePropertyVariance(Matrix X, Matrix transX,
                                               Matrix W, Matrix O,
                                               Matrix transO, int position) throws
            ProphylerException {
        // Calculate the variance
        Matrix S = (transX.times(W).times(X)).minus(transX.times(W).times(O).
                times(transO).times(W).times(X)); // variances (diagonal) and covariances (off-diagonal)
        double[] variance = MatrixUtil.getDiagonal(S);

        return variance;
    }

    /* Calculates the cSNP scores and writes them to one of the results tables.
     */
    private void calculateSNPScores(Matrix M, Matrix transM,
                                    Matrix invSqrtVarianceMatrix, Matrix invR,
                                    int position) throws ProphylerException {
        // MM contains the final scores in its diagonal
        Matrix MM = M.times(invSqrtVarianceMatrix).times(invR).times(
                invSqrtVarianceMatrix).times(transM);

        // Get the final scores from the diagonal of MM
        double[] diagMM = MatrixUtil.getDiagonal(MM);

        // Update the cSNP score results table for that position
        cSNPScoreTable[position] = diagMM;
    }

    /* Calculates the overall score for the column and writes it to one of the results tables.
     * "Overall score" is defined as the score for an amino acid with all property values = 0.
     * Essentially it represents how tolerant the position is to substitution in general.
     */
    private void calculateColumnScore(int position) throws ProphylerException {
        // Get the SNP scores for that column
        double[] snpScores = cSNPScoreTable[position];

        // Calculate the score for the column by getting the median SNP score
        double colScore = MathUtil.getMedian(snpScores);

        // Update the column scores results table for that position
        colScores[position] = colScore;
    }

    /* Creates a Fisher distribution and calculates p-values for the cSNP scores, column scores,
     * and column property scores.
     */
    private void calculatePValues() throws ProphylerException {
        // Calculate the "effective" number of sequences
        int e = numSequences;
        if (numSequences < (2 * numRequestedProperties)) {
            e = 2 * numRequestedProperties;
        }

        // Create a Fisher distribution
        FDistribution dist = new FDistribution(e - numRequestedProperties,
                                               numRequestedProperties);

        // Calculate a constant to be used in calculating p-values
        double constant = ((double) e *
                           ((double) e - (double) numRequestedProperties)) /
                          (((double) e + 1) * ((double) e - 1) *
                           ((double) numRequestedProperties));

        // Calculate p-values for all the scores
        cSNPProbTable = calculateSNPProbs(dist, constant);
        colPropProbTable = calculateColumnPropertyProbs(dist, constant);
        colProbs = calculateColumnProbs(dist, constant);
    }

    /* Calculates p-values for the cSNP scores at each column.
     */
    private double[][] calculateSNPProbs(FDistribution dist, double constant) throws
            ProphylerException {
        double currScore;
        double cumulative;
        double prob;

        // Reset the cSNP p-value results table
        cSNPProbTable = new double[sequenceLength][numAminoAcids];

        // For each cSNP score, calculate its p-value
        for (int i = 0; i < sequenceLength; i++) {
            for (int j = 0; j < numAminoAcids; j++) {
                currScore = cSNPScoreTable[i][j];
                cumulative = dist.cumulative(currScore * constant);
                prob = 1.0 - cumulative;
                cSNPProbTable[i][j] = prob;
            }
        }

        return cSNPProbTable;
    }

    /* Calculates scores for the significance of each property at each column, then calculates
     * the p-value for each score.
     */
    private double[][] calculateColumnPropertyProbs(FDistribution dist,
            double constant) throws ProphylerException {
        double mean;
        double variance;
        double score;
        double cumulative;
        double prob;

        // Reset the column property p-value table
        colPropProbTable = new double[sequenceLength][numRequestedProperties];

        // For each property and each column, calculate a score based on the mean and variance
        // Then calculate a p-value from the score
        for (int i = 0; i < sequenceLength; i++) {
            for (int j = 0; j < numRequestedProperties; j++) {
                mean = meanPropTable[i][j];
                variance = propVarianceTable[i][j];
                score = MathUtil.getSquaredValue(mean) / variance;
                cumulative = dist.cumulative(score * constant);
                prob = 1.0 - cumulative;
                colPropProbTable[i][j] = prob;
            }
        }

        return colPropProbTable;
    }

    /* Calculates the p-values of the overall column scores.
     */
    private double[] calculateColumnProbs(FDistribution dist, double constant) throws
            ProphylerException {
        double score;
        double cumulative;
        double prob;

        // Reset the column p-value table
        colProbs = new double[sequenceLength];

        // For each column, calculate a p-value from the score
        for (int i = 0; i < sequenceLength; i++) {
            score = colScores[i];
            cumulative = dist.cumulative(score * constant);
            prob = 1.0 - cumulative;
            colProbs[i] = prob;
        }

        return colProbs;
    }

    /* Takes the log, smooths, and normalizes the p-values.

    private void massagePValues() throws ProphylerException {
        double p;

        // Calculate log of all column p-values
        for (int i = 0; i < sequenceLength; i++) {
            p = colProbs[i];
            logColProbs[i] = Math.log(p);
        }

        // Smooth values by taking a moving average
        // Beginning and end of array will have null values since those values can't be smoothed
        smoothedLogColProbs = MathUtil.smooth(logColProbs, SMOOTHING_WINDOW);

        // Get a "sub-array" of the above to trim off the null values
        double[] subSmoothedLogColProbs = MathUtil.getSubArray(
                smoothedLogColProbs, MIDPOINT, sequenceLength - MIDPOINT - 1);

        // Normalize the values
        double[] subSmoothedNormLogColProbs = MathUtil.standardize(
                subSmoothedLogColProbs, VARIANCE_CORRECTION_FACTOR);

        // Stick the normalized values into a bigger array with null values at the beginning
        // and end (where smoothed values weren't available)
        for (int i = 0; i < sequenceLength; i++) {
            if ((i >= MIDPOINT) && (i < (sequenceLength - MIDPOINT))) {
                smoothedNormLogColProbs[i] = subSmoothedNormLogColProbs[i -
                                             MIDPOINT];
            }
        }
    }
   */

    /* Determines which amino acids fall above and below the p-value cut-off for each position.
     */
    private void determineCutoffs() {
        double prob;
        char aminoAcid;
        String goodAAsForPosition = ""; // all amino acids above the cutoff (as a string of chars)
        String badAAsForPosition = ""; // all amino acids below the cutoff (as a string of chars)

        // For each cSNP p-value at each position...
        for (int i = 0; i < sequenceLength; i++) {
            for (int j = 0; j < numAminoAcids; j++) {
                aminoAcid = aminoAcidList[j];
                prob = cSNPProbTable[i][j];

                // Check to see if the p-value is above the cut-off
                if (prob > P_VALUE_CUTOFF) {
                    // If so, it's good
                    goodAAsForPosition = goodAAsForPosition.concat(String.
                            valueOf(aminoAcid));
                } else {
                    // If not, it's bad
                    badAAsForPosition = badAAsForPosition.concat(String.valueOf(
                            aminoAcid));
                }
            }

            // Update the cutoff results lists with that data
            goodAAs.add(i, goodAAsForPosition);
            badAAs.add(i, badAAsForPosition);

            // Reset the strings for the next position
            goodAAsForPosition = "";
            badAAsForPosition = "";
        }
    }

    /* Generates the final output table and funnels to the output to
     * a file,  or to the screen as specified by the
     * user.
     */
    private void writeOutput(EditSet editSet) throws ProphylerException {

        // Generate the raw output table
        String[] outputTable = generateOutputTable(editSet);

        // Write to file if requested by user
        if (writeToFile) {
            writeOutputToFile(editSet, outputTable);
        }

        // Write to screen if requested by user
        if (printToScreen) {
            writeOutputToScreen(outputTable);
        }
    }

    /* Generates the output table.
     */
    private String[] generateOutputTable(EditSet editSet) throws
            ProphylerException {
        String[] outputTable = new String[sequenceLength + 1];

        // Add the column headings for the column score, column p-value, alignment, and gap weight
        //String columnHeadings = "Position\tColumn Score\tColumn p-value\tLog Column p-value\tSmoothed Log Column p-value\tNormalized Smoothed Log Column p-value\tAlignment\tGap Weight\tOver Gap Weight Threshold";
        String columnHeadings = "Position\tColumn Score\tColumn p-value\tAlignment\tGap Weight\tOver Gap Weight Threshold";

        // Add the column headings for the column property p-values
        for (int i = 0; i < numRequestedProperties; i++) {
            //columnHeadings = columnHeadings + "\tProperty " + i;
            if (useScales) {
                columnHeadings = columnHeadings + "\t" +
                                 propertyMap.get(String.valueOf(
                                         requestedProperties[i]));
            } else {
                columnHeadings = columnHeadings + "\t" +
                                 propertyMap.get(String.valueOf(i));

            }
        }
        // Add the column headings for the MAPP scores
        for (int j = 0; j < numAminoAcids; j++) {
            columnHeadings = columnHeadings + "\t" + aminoAcidList[j];
        }

        // Add the column headings for the cSNP p-values
        for (int j = 0; j < numAminoAcids; j++) {
            columnHeadings = columnHeadings + "\t" + aminoAcidList[j];
        }

        // Add the column headings for the good and bad amino acids
        columnHeadings = columnHeadings + "\tGood Amino Acids\tBad Amino Acids";

        outputTable[0] = columnHeadings;

        ArrayList columns = editSet.getColumns();
        Column column;
        String columnData = "";
        boolean isGappy;

        // Iterate through the table rows...
        for (int k = 0; k < sequenceLength; k++) {
            //		 Position	sqrt of column score             column p-value       log column p-value

            //columnData = (k + 1) + "\t" + Math.sqrt(colScores[k]) + "\t" +
            //colProbs[k] + "\t" + logColProbs[k];

            column = (Column) columns.get(k);
            isGappy = column.getGapWeight() > GAP_WEIGHT_THRESHOLD;


            double colscore = Math.sqrt(colScores[k]);
            BigDecimal bd = new BigDecimal(colscore);
            bd = bd.setScale(2, BigDecimal.ROUND_HALF_UP);

            if(isGappy){
                columnData = (k + 1) + "\t" + "N/A" + "\t" + "N/A";
            }else{
                columnData = (k + 1) + "\t" + bd.toString() + "\t" +
                             formatter.format(colProbs[k]);
            }
            // Smoothed log column p-value (if available for that position)
            /*
             if ((k >= MIDPOINT) && (k < (sequenceLength - MIDPOINT))) {
                columnData = columnData + "\t" + smoothedLogColProbs[k];
                columnData = columnData + "\t" + smoothedNormLogColProbs[k];
                         } else {
                columnData = columnData + "\t\t";
                         }
             *///column = (Column) columns.get(k);

            columnData = columnData + "\t" + "'" +
                         String.valueOf(column.getAminoAcids()); // alignment
            columnData = columnData + "\t" + column.getGapWeight(); // gap weight

            // Over gap weight threshold?
            //if (column.getGapWeight() > GAP_WEIGHT_THRESHOLD) {
             if(isGappy){
                columnData = columnData + "\tY";
            } else {
                columnData = columnData + "\tN";
            }

            // Each property's p-value
            for (int m = 0; m < numRequestedProperties; m++) {
                if(isGappy) {
                    columnData = columnData + "\t" + "N/A";

                }else{
                    columnData = columnData + "\t" +
                                 formatter.format(colPropProbTable[k][m]);
                }
            }
            // MAPP scores for each amino acid
            for (int n = 0; n < numAminoAcids; n++) {
                double mappscore = Math.sqrt((cSNPScoreTable[k][n]));
                BigDecimal md = new BigDecimal(mappscore);
                 md = md.setScale(2, BigDecimal.ROUND_HALF_UP);
                //columnData = columnData + "\t" + formatter.format(Math.sqrt(cSNPScoreTable[k][n]));
                if(isGappy){
                     columnData = columnData + "\t" + "N/A";
                }else{
                    columnData = columnData + "\t" + md.toString();
                }
            }
            // cSNP p-values for each amino acid
            for (int n = 0; n < numAminoAcids; n++) {
                if(isGappy){
                    columnData = columnData + "\t" + "N/A";
                }else{
                    columnData = columnData + "\t" +
                                 formatter.format(cSNPProbTable[k][n]);
                }
            }

            // Lists of good and bad amino acids
            if(isGappy){
                columnData = columnData + "\t" + "N/A";
                columnData = columnData + "\t" + "N/A";
            }else{
                columnData = columnData + "\t" + goodAAs.get(k);
                columnData = columnData + "\t" + badAAs.get(k);
            }
            // End the row
            outputTable[k + 1] = columnData;
            columnData = "";
        }

        return outputTable;
    }


    /* Writes the final result table to an output file.
     */
    private void writeOutputToFile(EditSet editSet, String[] outputTable) throws
            ProphylerException {
        try {
            //if (!fastaProvided) {
            //writer.write("QSID " + editSet.getQSID() + ", edit set " +
            //editSet.getID() + "\n");
            //}

            for (int i = 0; i < outputTable.length; i++) {
                writer.write(outputTable[i] + "\n");
            }

            writer.write("\n\n");
            writer.flush();
        } catch (IOException e) {
            throw new ProphylerException(
                    "Error while attempting to write output to file", e);
        }
    }

    /** Writes the output to the screen.
     */
    private void writeOutputToScreen(String[] outputTable) throws
            ProphylerException {
        for (int i = 0; i < outputTable.length; i++) {
            System.out.println(outputTable[i]);
        }
    }

    /** No resource clean-up required for this module.
     */
    protected void cleanUpResources() {
        try {
            if (writer != null) {
                writer.close();
            }
        } catch (IOException e) {
            System.out.println("Error while trying to close output file");
            e.printStackTrace();
        }
    }
}
