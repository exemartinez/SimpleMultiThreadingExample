package com.foodfactory.view;

import com.foodfactory.exceptions.KitchenRequiredException;
import com.foodfactory.model.AssemblyLine;
import com.foodfactory.controllers.Kitchen;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Receives all the requests from an external source.
 * For this implementation, we will use a unit test.
 * For future use, we will go for the implementation of
 * a REST api (Throught Spring MVC) or hearing in a socket directly.
 *
 * Its major aim is to build the kitchen, start it up.
 * And hear for new assembly lines to appear, dynamically over an external source.
 * Every need assembly line will generate its own products.
 */
public class AssemblyLineServer {

    private final List<AssemblyLine> assemblyLines; //This is not how the request will be implemented in a real app!
    private LocalDateTime startDateTime;
    private final Kitchen kitchen;
    private volatile boolean endProgram = false;

    /**
     * Defines the major controllers to operate the simulation.
     * It starts the kitchen and defines the assembly lines.
     */
    public AssemblyLineServer(Kitchen kitchen) throws KitchenRequiredException {

        if(kitchen == null){
            throw new KitchenRequiredException();
        }

        this.kitchen = kitchen;
        this.assemblyLines = new CopyOnWriteArrayList<>();
        kitchen.setAssemblyLines(assemblyLines); // We do this to give the Kitchen visibility over what happens on the AssemblyLines, at this time it will be EMPTY.

        kitchen.start();

    }

    public void addAssemblyLine() {
        AssemblyLine newAssemblyLine = new AssemblyLine(this.assemblyLines.size());
        newAssemblyLine.start();

        this.assemblyLines.add(newAssemblyLine);
    }

    /**
     * Loops for requests over the creation of new assembly lines
     * (Threads), and starts all the simulation.
     */
    public void waitForNewAssemblyLines() {
        this.startDateTime = LocalDateTime.now();

        // Hearing the creation of the new product assembly lines
        while (!endProgram) {
            Thread.onSpinWait();
        }

        // Stop all the Assembly lines, one by one!
        this.getAssemblyLines().forEach(AssemblyLine::stop);

        //Stops the kitchen
        this.getKitchen().stop();
    }

    /**
     * Just returns the amount of seconds since the current object started to
     * hear requests.
     * @return
     */
    public Long getActiveSecondsSinceStart(){
        return Duration.between(startDateTime,LocalDateTime.now()).toSeconds();
    }

    public boolean isEndProgram() {
        return endProgram;
    }

    public void setEndProgram(boolean endProgram) {
        this.endProgram = endProgram;
        this.startDateTime = null;
    }

    /**
     * We go AssemblyLine by AssemblyLine Asking for the number of
     * Elements in its "waiting" and "finished" lines.
     */
    public void printStatusAllAssemblyLines() {
        this.assemblyLines.forEach(AssemblyLine::printStatus); // We do not needed to use, neither implement "countCookedItems" (see design docs).
    }

    public Kitchen getKitchen() {
        return kitchen;
    }

    public List<AssemblyLine> getAssemblyLines() {
        return assemblyLines;
    }

    /**
     * We kill the process in cold blood; losing state and data.
     */
    public void kill() {
        assemblyLines.forEach(AssemblyLine::kill);
        kitchen.kill();
    }

    /**
     * This prints in the standard output the products already processed
     * and finished. We use this only in the context of this simulation.
     */
    public void printFinishedProductsInOrder() {
        assemblyLines.forEach(AssemblyLine::printAllFinishedProductsInOrder); // TODO replace it with an appropiate way of logging.
    }
}
