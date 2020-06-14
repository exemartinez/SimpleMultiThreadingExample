import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;

/**
 * Executes and test different the creation of new assembly lines and puts
 * the simulation in motion.
 */
class MainTest {

    @Test
    void mainAppStartUpAndStopTest() {

        try {

            //Starts the simulation as it's made from the command line.
            Main.main(null);
            assert(Main.getAssemblyLineServer()!=null );

            //Waits a few seconds...
            TimeUnit.SECONDS.sleep(5);

            //...then stops it.
            assert(Main.getAssemblyLineServer().getActiveSecondsSinceStart() > 4);
            Main.getAssemblyLineServer().setEndProgram(true);

        } catch (InterruptedException e) {
            e.printStackTrace();
            assert(false);
        }

        assert(Main.getAssemblyLineServer().isEndProgram());

    }

    @Test
    void runAssemblyLinesProductGeneratorTest() {

        try {
            //Starts the simulation as it's made from the command line.
            Main.main(null);

            //Add two Assembly Lines that generates the due products.
            Main.getAssemblyLineServer().addAssemblyLine(); // First Assembly Line

            //Waits a few seconds...
            TimeUnit.SECONDS.sleep(3);

            Main.getAssemblyLineServer().addAssemblyLine(); // Second Assembly Line

            // Allow it to run for a whole minute...
            TimeUnit.SECONDS.sleep(60);

            Main.getAssemblyLineServer().printStatusAllAssemblyLines();

            // Kill them all!
            Main.getAssemblyLineServer().setEndProgram(true); // TODO Implement a cascaded stop! this is too harsh as is!

            // Wait a few seconds for the process to stop...
            TimeUnit.SECONDS.sleep(5);
            Main.getAssemblyLineServer().kill();

        } catch (InterruptedException e) {
            e.printStackTrace();
            assert(false);
        }
    }
}