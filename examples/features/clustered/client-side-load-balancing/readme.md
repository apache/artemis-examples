# JMS Client-Side Load-Balancing Example

If you have not already done so, [prepare the broker distribution](../../../../README.md#getting-started) before running the example.

To run the example, simply type **mvn verify** from this directory, or **mvn -PnoServer verify** if you want to create and start the broker manually.

This example demonstrates how connections created from a single JMS Connection factory can be created to different nodes of the cluster. In other words it demonstrates how Apache Artemis does **client side load balancing** of connections across the cluster.

The particular load-balancing policy can be chosen to be random, round-robin or user-defined. Please see the user guide for more details of how to configure the specific load-balancing policy. In this example we will use the default round-robin load balancing policy.

The list of servers over which Apache Artemis will round-robin the connections can either be specified explicitly in the connection factory when instantiating it directly, when configuring it on the broker or configured to use UDP discovery to discover the list of servers over which to round-robin. This example will use UDP discovery to obtain the list.

This example starts three servers which all broadcast their location using UDP discovery. The UDP broadcast configuration can be seen in the `broker.xml` file.

A JMS ConnectionFactory is deployed on each broker specifying the discovery group that will be used by that connection factory.

For more information on Apache Artemis load balancing, and clustering in general, please see the clustering section of the user manual.