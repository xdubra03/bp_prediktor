/*
 * MathException.java
 *
 * Created on July 1, 2003, 11:35 AM
 */

package prophyler.exception;

/** This exception is thrown under certain conditions by the MathUtil class.
 * @author  rkwok
 */
public class MathException extends ProphylerException {
    
    /** Creates a new instance of MathException with the given message.
     */
    public MathException(String message) {
        super(message);
    }
    
    /** Creates a new instance of MathException and appends the given message to the message of the
     * originalException.
     */
    public MathException(String message, Exception originalException) {
        super(message, originalException);
    }
}
