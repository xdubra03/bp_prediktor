/*
 * TextUtil.java
 *
 * Created on July 10, 2003, 2:33 PM
 */

package prophyler.tool;

import java.io.*;
import java.util.*;
import Jama.*;
import prophyler.exception.*;
import prophyler.tool.*;

/** A class that performs some basic text operations.
 * @author  rkwok
 */

/* Compile command: javac -classpath (home)lib/Jama-1.0.1:(home) TextUtil.java */

public class TextUtil {
    
    /** Creates a new instance of TextUtil.
     */
    public TextUtil() {
    }
    
    /** Reads a file and returns an ArrayList of all lines in the file, ignoring trailing empty
     * lines.
     * @throws ProphylerException If the file cannot be found or an IO exception occurs.
     */
    public static ArrayList getLines(String inputFile) throws ProphylerException {
        String line;
        ArrayList lines = new ArrayList();
        
        try {
            BufferedReader reader = new BufferedReader(new FileReader(inputFile));
            
            while (((line = reader.readLine()) != null) && (line.trim() != "")) {
                lines.add(line.trim());
            }
            
            reader.close();
        }
        catch (FileNotFoundException e1) {
            throw new ProphylerException("Error: Cannot find input file at " + inputFile);
        }
        catch (IOException e2) {
            throw new ProphylerException("Error: IO exception while opening input file at " + inputFile);
        }
        
        return lines;
    }
    
    /** Reads a file containing a whitespace-delimited table of numbers and returns the corresponding
     * 2-D double array.  User can specify whether the returned table should be transposed from the
     * input table.
     */
    public static double[][] getNumArray(String inputFile, boolean transpose) throws ProphylerException {
        ArrayList lines = getLines(inputFile);
        String line = (String)lines.get(0);
        String[] splitLine = line.split("\\s+");
            
        int numRows = lines.size();
        int numColumns = splitLine.length;
            
        double[][] numArray = new double[numRows][numColumns];
        double parsedNum;
        int i = 0;
        
        while (i < lines.size()) {
            for (int j = 0; j < splitLine.length; j++) {               
                try {
                    parsedNum = new Double(splitLine[j]).doubleValue();
                }
                catch (NumberFormatException e) {
                    throw new ProphylerException("Cannot parse numbers from the line '" + line + "' in input file " + inputFile, e);
                }
                numArray[i][j] = parsedNum;
            }

            i++;
            
            if (i < lines.size()) {
                line = (String)lines.get(i);
                splitLine = line.split("\\s+");
            }
        }
        
        if (transpose) {
            Matrix matrix = new Matrix(numArray);
            Matrix transposedMatrix = matrix.transpose();
            numArray = transposedMatrix.getArray();
        }
        
        return numArray;
    }    
    
    /** Takes a 2-D array and prints a tab-delimited table to the screen.
     */
    public static void printTable(double[][] table) {
        double[] row;
        
        for (int i = 0; i < table.length; i++) {
            row = table[i];
            
            for (int j = 0; j < row.length; j++) {
                System.out.print(table[i][j] + "\t");
            }
            System.out.print("\n");
        }
    }        
    
    /** Takes a 1-D array of doubles and prints it to the screen with each entry on a new line.
     */
    public static void printArray(double[] table) {
        for (int i = 0; i < table.length; i++) {
            System.out.println(table[i]);
        }
    }            

    /** Takes a 1-D array of Strings and prints it to the screen with each entry on a new line.
     */    
    public static void printArray(String[] table) {
        for (int i = 0; i < table.length; i++) {
            System.out.println(table[i]);
        }
    }             
    
    /** Takes an ArrayList and prints it to the screen as a series of lines where each line
     * shows the index and its element (cast to a String), separated by a tab.
     */
    public static void printArrayList(ArrayList list) {
        String element;
        
        for (int i = 0; i < list.size(); i++) {
            element = (String)list.get(i);
            System.out.println(element);
        }
    }

    /** Takes an ArrayList and writes it to a file, with each ArrayList entry as a new line
      * in the file.
      */
    public static void printToFile(ArrayList list, String filePath) throws ProphylerException {
        try {
            File file = new File(filePath);
            FileWriter writer = new FileWriter(file, true);
            String line;

            if (!list.isEmpty()) {
                for (int i = 0; i < list.size(); i++) {
                    line = (String)list.get(i);
                    writer.write(line + "\n");
                }
            }
            
            writer.flush();
            writer.close();
        }
        catch (IOException e) {
            throw new ProphylerException("Error while attempting to write to file at " + filePath);
        }
    }

    /** Performs a diff between two files.  Uses the Unix command "comm -23" to identify lines that
      * are in file1 but not in file2.
      */
    public static ArrayList getMissingLines(String tempDir, String file1, String file2) throws ProphylerException {
        sort(file1);
        sort(file2);

        String missingLines = SystemUtil.execute("comm -23 " + file1 + " " + file2);
        String[] missingLinesList = missingLines.split("\n");
        
        ArrayList missingLinesArray = new ArrayList();
        String missingLine;
        for (int i = 0; i < missingLinesList.length; i++) {
            missingLine = missingLinesList[i];
            if (!(missingLine.trim()).equals("")) {
                missingLinesArray.add(missingLine);
            }
        }
        
        return missingLinesArray;
    }

    /** Given a file path, removes all duplicate lines and writes the resulting
      * non-redundant data to the same file path (removing the original).
      */
    public static void uniq(String filePath) throws ProphylerException {
        String tempFile = filePath + ".uniq";
        SystemUtil.execute("uniq " + filePath + " > " + tempFile);
        SystemUtil.execute("rm " + filePath);
        SystemUtil.execute("mv " + tempFile + " " + filePath);
    }

    /** Given a file path, sorts the lines in the file and writes the resulting
      * sorted data to the same file path (removing the original).
      */
    public static void sort(String filePath) throws ProphylerException {
        String tempFile = filePath + ".sorted";
        SystemUtil.execute("sort " + filePath + " > " + tempFile);
        SystemUtil.execute("rm " + filePath);
        SystemUtil.execute("mv " + tempFile + " " + filePath);
    }    
}
