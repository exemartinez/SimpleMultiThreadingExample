import com.foodfactory.exceptions.KitchenRequiredException;
import com.foodfactory.view.AssemblyLineServer;
import com.foodfactory.builders.KitchenBuilder;
import com.foodfactory.controllers.Kitchen;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * A simulation of the following use case:
 *
 * "We have to provide a solution for a factory that processes food products in different assembly lines. We need to automate the cooking portion of the assembly line for the products. The factory produces various food products, which are created in assembly lines. There is a common part to many of them, and that is the cooking stages for which some ovens are used.
 * The process to cook the different products (in the “cooking stage”) in a given point of the assembly line involves getting the products from the line, to put them in the oven for a specific amount of time, in the order they arrive. An intermediate store is used for the products that arrive, if there is no space left in the oven, which has a finite size. If there is no more room in the ovens or the stores when extracting it from the assembly line, the originating assembly line halts. After each product is cooked, we have to extract it and return it to the originating line (this is because multiple lines arrive at this automated stage).
 * We have to develop an application that controls the cooking stage of the factory."
 *
 * @author hernan ezequiel martinez
 */
public class Main {

    private static AssemblyLineServer assemblyLineServer = null;

    /**
     * This whole method, just builds the kitchen and starts to "hear" for requests.
     * This stays looping. If this were an "actual" app, I'll put this hearing requests in a socket.
     * Instead of it, we will work with a Junit that will hit some shared objects and notify it of a new assembly line.
     *
     * @param args - not used.
     */
    public static void main(String[] args) {

        KitchenBuilder kitchenBuilder = KitchenBuilder.getInstance();
        Kitchen kitchen = kitchenBuilder.buildKitchenStructure();

        try {

            assemblyLineServer = new AssemblyLineServer(kitchen); // This starts the Kitchen...

            Executor executor = Executors.newSingleThreadExecutor();
            executor.execute(()-> startAseemblyLineServer()); // This starts thw Assembly Lines simulated production.

        } catch (KitchenRequiredException e) {
            e.printStackTrace();
        }

    }

    /**
     * Starts a looping thread that heards for request from different assembly lines.
     */
    private static void startAseemblyLineServer() {
        assemblyLineServer.waitForNewAssemblyLines();
    }

    public static AssemblyLineServer getAssemblyLineServer() {
        return assemblyLineServer;
    }

}
