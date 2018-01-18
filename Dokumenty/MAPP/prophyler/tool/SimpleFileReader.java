/*
 * SimpleFileReader.java
 *
 * Created on September 11, 2003, 5:29 PM
 */

package prophyler.tool;

import java.io.*;
import prophyler.exception.*;

/** A class that simplifies file-reading operations.
 * @author  rkwok
 */
public class SimpleFileReader {
    String filePath;
    BufferedReader reader;
    
    /** Creates a new instance of SimpleFileReader.
     */
    public SimpleFileReader(String filePath) throws ProphylerException {
        try {
            reader = new BufferedReader(new FileReader(filePath));
        }
        catch (IOException e) {
            throw new ProphylerException("IO exception while opening file " + filePath, e);
        }
    }
    
    /** Returns the next non-empty line in the file, trimmed of whitespace.
     */
    public String readLine() throws ProphylerException {
        String line;
        
        try {
            if (reader == null) {
                System.out.println("Reader is null");
            }
            line = reader.readLine();
            if ((line != null) && (line.trim() != "")) { 
                return line;
            }
            else {
                return null;
            }
        }
        catch (IOException e) {
            throw new ProphylerException("IO exception while reading from file " + filePath, e);
        }
    }
    
    /** Closes the file reader.
     */
    public void close() throws ProphylerException {
        try {
            reader.close();
        }
        catch (IOException e) {
            throw new ProphylerException("Error while closing file " + filePath, e);
        }
    }
}
