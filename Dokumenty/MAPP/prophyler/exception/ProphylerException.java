/*
 * ProphylerException.java
 *
 * Created on July 16, 2003, 1:39 PM
 */

package prophyler.exception;

/** An exception indicating that an error occurred during ProPhylER processing.
 * @author  rkwok
 */
public class ProphylerException extends Exception {
    private String message;
    private Exception originalException;
    
    /** Creates a new instance of ProphylerException.
     */
    public ProphylerException(String message) {
        this.message = message;
    }

    /** Creates a new instance of ProphylerException and appends the given message to the message of
     * the originalException.
     */
    public ProphylerException(String message, Exception originalException) {
        this.message = message + ":\n" + originalException.getMessage();
        this.originalException = originalException;
    }    
    
    /** Returns the exception message.
     */
    public String getMessage() {
        return message;
    }
    
    public Exception getOriginalException() {
     
        return originalException;
    }
}
