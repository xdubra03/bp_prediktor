/*
 * SystemUtil.java
 *
 * Created on September 4, 2003, 4:30 PM
 */

package prophyler.tool;

import java.io.*;
import java.lang.*;
import java.util.*;
import prophyler.exception.*;

/** A utility class to facilitate execution of Java system calls.
 * @author  rkwok
 */
public class SystemUtil {
    
    /** Creates a new instance of SystemUtil.
     */
    public SystemUtil() {
    }
    
    /** A static method that takes a system call and executes it.  It prints the error stream and
     * the output stream to the screen as it comes in.  If the command completes successfully (i.e.
     * with exit value 0), it returns the output of the command as a String.  If an exit value
     * other than 0 is returned, or some other exception is thrown, it throws a ProphylerException.
     * Note that it does not throw an exception simply if some data is received through the error
     * stream, since some programs seem to be flaky about whether they write errors to stdout or
     * stderr.
     */
    public static String execute(String command) throws ProphylerException {
        StringBuffer output = new StringBuffer();
        StringBuffer errors = new StringBuffer();
        
        try {
            String[] shellCommand = {"/bin/csh", "-c", command};
            Process process = Runtime.getRuntime().exec(shellCommand);            

            InputStreamReader inputReader = new InputStreamReader(process.getInputStream());
            int inputByte = 0;
            while ((inputByte=inputReader.read()) != -1) {
                output.append((char)inputByte);
                //System.out.print((char)inputByte);
            }
            inputReader.close();  
            
            InputStreamReader errorReader = new InputStreamReader(process.getErrorStream());
            int errorByte;
            while ((errorByte=errorReader.read()) != -1) {
                errors.append((char)errorByte);
                //System.out.print((char)errorByte);
            }
            errorReader.close();  
            
            int exitValue = process.waitFor();
            
            if (exitValue != 0) {
                throw new ProphylerException("Bad exit value: " + exitValue);
            }
        }
        catch (SecurityException e1) {
            throw new ProphylerException("Security exception encountered while attempting to execute this command:\n" + command, e1);
        }
        catch (IOException e2) {
            throw new ProphylerException("IO exception encountered while attempting to execute this command:\n" + command, e2);
        }  
        catch (NullPointerException e3) {
            throw new ProphylerException("Null command passed to SystemUtil", e3);
        }
        catch (IllegalArgumentException e4) {
            throw new ProphylerException("Empty command passed to SystemUtil", e4);
        }
        catch (InterruptedException e5) {
            throw new ProphylerException("Thread interrupted while attempting to execute this command:\n" + command, e5);
        }
        
        return output.toString();
    }
    
    /** Returns the current date as a string in the form "YYYY-MM-DD".
     */
    public static String getDateString() {
        Calendar now = Calendar.getInstance();
        
        int year = now.get(Calendar.YEAR);
        int month = now.get(Calendar.MONTH);
        int day = now.get(Calendar.DAY_OF_MONTH);
        
        String dateString = year + "-" + (month+1) + "-" + day;
        
        return dateString;
    }
    
    /** Returns true if a file with the given prefix exists in the given directory, otherwise
     * returns false.
     */
    public static boolean fileExists(String dir, String filePrefix) throws ProphylerException {
        if (!dir.endsWith("/")) {
            dir = dir + "/";
        }

        String output = execute("ls " + dir + filePrefix + "*");
        return (!output.equals(""));
    }
    
    /** Returns true if the given folder exists, otherwise returns false.
     */
    public static boolean folderExists(String dir) throws ProphylerException {
        try {
            execute("cd " + dir);
            return true;
        }
        catch (ProphylerException e) {
            return false;
        }
    }    
    
    /** Returns true if the given folder is empty, otherwise returns false.
     */
    public static boolean folderIsEmpty(String dir) throws ProphylerException {
        if (!dir.endsWith("/")) {
            dir = dir + "/";
        }
        
        String output = execute("ls " + dir);
        return (output.equals(""));
    }
}
