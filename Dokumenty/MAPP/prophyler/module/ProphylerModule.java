/*
 * ProphylerModule.java
 * Created on July 11, 2003, 12:44 PM
 */

package prophyler.module;


/** This is an abstract class that provides a framework for ProPhylER modules.  It
 * provides the methods login() and register(), which log the user into the database
 * and register a module session, respectively.  The subclass can call these methods
 * whenever it is appropriate in their module (they do not get called automatically
 * by ProphylerModule); usually this should happen in the init() method.  The
 * subclass fills in the init(), process(), and cleanUpResources() methods.<p>
 * ProphylerModule will call init() and process(), then call cleanUpResources() and
 * cleanUpDatabase() at the end of the program or in the event of an error.  If the
 * user specifies the -h option, it will call the showHelp() method (also to be filled
 * in by the subclass).<p>
 *
 */

public abstract class ProphylerModule {


    /** Creates a new instance of ProphylerModule.
     */
    public ProphylerModule() {
    }

    /** Coordinates initialization, processing, and clean-up.  Shows help message
     * to the user if -h option is specified.  Otherwise attempts to run init()
     * and process().  At the end of the program (regardless of whether an exception
     * was thrown or not), cleanUpResources() and cleanUpDatabase() are called.
     */
    protected void run(String[] args) {
        if ((args.length == 1) && (args[0]).equals("-h")) {
            showHelp();
            System.exit(0);
        }

        try {
            init(args);
            process();
        }
        catch (Exception e) {
            System.out.println("Critical error; will attempt to clean up");
            e.printStackTrace();
        }
        finally {
            cleanUpResources();
        }
    }

    /** Message to be printed to the screen when user asks for help with -h option.
     */
    protected void showHelp() {

    }

    /** Any initialization that needs to be done before process() is called.
     */
    protected abstract void init(String[] args) throws Exception;

    /** The guts of the module.
     */
    protected abstract void process() throws Exception;

    /** Clean-up operations that should be performed at the end of the program run, or in the
     * event of a critical error.
     */
    protected abstract void cleanUpResources();

}
