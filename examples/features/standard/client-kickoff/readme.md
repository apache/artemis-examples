# Client Kickoff Example

If you have not already done so, [prepare the broker distribution](../../../../README.md#getting-started) before running the example.

To run the example, simply type **mvn verify** from this directory, or **mvn -PnoServer verify** if you want to start and create the broker manually.

This example shows how to kick off a client connected to Apache Artemis using [JMX](http://www.oracle.com/technetwork/java/javase/tech/javamanagement-140525.html)

The example will connect to Apache Artemis. Using JMX, we will list the remote addresses connected to the broker and close the corresponding connections. The client will be kicked off from Apache Artemis receiving an exception that its JMS connection was interrupted.
