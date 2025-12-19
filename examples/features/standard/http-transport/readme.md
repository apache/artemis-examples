# JMS HTTP Example

If you have not already done so, [prepare the broker distribution](../../../../README.md#getting-started) before running the example.

To run the example, simply type **mvn verify** from this directory, or **mvn -PnoServer verify** if you want to start and create the broker manually.

This example shows you how to configure Apache Artemis to use the HTTP protocol as its transport layer.

Apache Artemis supports a variety of network protocols to be its underlying transport without any specific code change.

This example is taken from the queue example without any code change. By changing the client's URL in `jndi.properties` one can get Apache Artemis working with the HTTP transport.