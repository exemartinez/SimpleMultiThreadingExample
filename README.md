# Simple Multi-Threading case problem

This is a multi-message and multiprocessing example of how to deal with multiple processings in the fashion of an assembly line. We choose the abstraction of a "Food Factory", but it is more like a simulation of how to deal with multiple callers asking an application to deal with the processing required in a microservices kind of application. Where you need to assemble the data (the complete transaction or the final dataset), using different services and different data blocks in order to do it so.

## The analogy

We have to provide a *simulation* of a factory that processes food products in different assembly lines. We need to automate the cooking portion of the assembly line for the products. The factory produces various food products, which are created in assembly lines. There is a common part to many of them, and that is the cooking stages for which some ovens are used. 
The process to cook the different products (in the “cooking stage”) in a given point of the assembly line involves getting the products from the line, to put them in the oven for a specific amount of time, in the order they arrive. An intermediate store is used for the products that arrive, if there is no space left in the oven, which has a finite size. If there is no more room in the ovens or the stores when extracting it from the assembly line, the originating assembly line halts. After each product is cooked, we have to extract it and return it to the originating line (this is because multiple lines arrive at this automated stage).
We have to develop an application that controls the cooking stage of the factory.

Considerations:

- We have N Stores and M Ovens to process the products
- There is no particular restriction of the oven to use or the store to put the products in (e.g.: different products can share the same store/oven at the same time)
- All the processing from the different lines should respect FIFO ordering for the cooking to simplify the solution. That is, the order in which the elements are picked from a line, should be the same after the cooking stages, when these are put back.
- There is no control over the rate at which the lines will make a product available to be taken.
