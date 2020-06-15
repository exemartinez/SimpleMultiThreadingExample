# Simple Multi-Threading case problem

This is a multi-message and multiprocessing example of how to deal with several accesses to the same objects in the fashion of an 'food assembly line'. We choose the abstraction of a "Food Factory", but it is more like a simulation of how to deal with multiple-callers asking an application to deal with the processing required in a microservices kind of application. Where you need to assemble the data (the complete transaction or the final dataset), using different services and different data blocks in order to do it so.

> We took the gaunlet, and we took the analogy by heart.

## The analogy

We have to provide a **simulation** of a 'factory' that processes food products in different assembly lines. We need to automate the cooking portion of the assembly line for the products. The factory produces various food products, which are created in assembly lines. There is a common part to many of them, and that is the cooking stages for which some ovens are used. 
The process to cook the different products (in the “cooking stage”) in a given point of the assembly line involves getting the products from the line, to put them in the oven for a specific amount of time, in the order they arrive. An intermediate store is used for the products that arrive, if there is no space left in the oven, which has a finite size. If there is no more room in the ovens or the stores when extracting it from the assembly line, the originating assembly line halts. After each product is cooked, we have to extract it and return it to the originating line (this is because multiple lines arrive at this automated stage).

We have to develop an application that simulates how the cooking the factory's cooking stage should be managed.

## Considerations:

- This complete implementation and documents are based on a MuleSoft challenge which specification is [here](https://github.com/exemartinez/SimpleMultiThreadingExample/blob/master/documentation/MuleSoft_Specification.pdf)
- We have N Stores and M Ovens to process the products
- There is no particular restriction of the oven to use or the store to put the products in (e.g.: different products can share the same store/oven at the same time)
- All the processing from the different lines should respect FIFO ordering for the cooking to simplify the solution. That is, the order in which the elements are picked from a line, should be the same after the cooking stages, when these are put back.
- There is no control over the rate at which the lines will make a product available to be taken.
- We have assumed that what is intended to be built is a *simulator* of a food factory kitchen, not a system that controls a regular Kitchen.
- Store interface and Oven interface has certain oddities in their design; we have assumed they were intentional and worked with them into the whole finished design.
- We understood that the interfaces implementation of all their methods was optional.

## Documentation

The whole specification could be found [here](https://github.com/exemartinez/SimpleMultiThreadingExample/blob/master/documentation/Food%20Factory%20Architecture%20Model.pdf). However, most of the information can be retrieved from the code itseld. That document it's just a destillation of all the drafts we went throught during the architectural model ideation and understanding of the problem. We do believe it could be worth to understand the whole design more clearly; specially the **diagrams**.

## Well known issues 

This application is not complete. It was developed within a span of time and a deadline for a technical screening; we tried, however to deliver the highest amount of functionality within the highest degree of quality for the time being. So, we picked up our fights and we leaved some 'well known issues' in the code, as they're:

- The stop() procedure should be improved. It is faulty and ends up introducing oddities during the final stages of the threads. This can be improved with: a "stop" in the products' generation made by the assembly lines, and a validation over the *waitingProducts* queue that made sure that it has been completely emptied (and that the *finishedProducts* queue is holding all the products). Besides, if something went 'missing', it should be logged.
- We used the standard output as a logging device; this is wrong, but simple to implement. Given the current context we just wanted to deliver the main functionality and avoid overloading the design with non-functional requirements of sorts.
- The size of the Ovens and Stores were changed to Integer; this was a mistake during the specification reading and we know Double is kind of harder to handle in a multhreading environment (there is no AtomicDouble, for example). However, we decided to move on the ball to the goal line nevertheless.
- The companion documentation most valuable asset is their **diagrams**, the text is more or less what you will find in the code comments.
- We should have provided a standalone application; we took the easy lane of using Junit for testing it. The reasons has to be with the debugging and the need to focus the total coding effort to the **core** most valuable functionality development.
- We developed a 'pet project', we could've reached the same functionality using a miriad of technologies, like log4j, Kafka, serverless standalone REST API simulating the every involved worker and then deploy them all in Heroku dockerized, and then a Javascript UI and on and on...we wanted to keep it simple. Our main aim was to understand the problem and **deliver**.

