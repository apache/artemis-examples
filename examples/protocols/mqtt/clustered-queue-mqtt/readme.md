# MQTT Clustered Subscription Example

If you have not already done so, [prepare the broker distribution](../../../../README.md#getting-started) before running the example.

To run the example, simply type **mvn verify** from this directory, or **mvn -PnoServer verify** if you want to start and create the broker manually.

This example demonstrates a queue deployed on two different nodes. The two nodes are configured to form a cluster.

We then create an MQTT subscriber on the queue on each node, and we create a producer on only one of the nodes.

We then send some messages via the producer, and we verify that **both** subscribers receive the sent messages.

For more information on Apache Artemis clustering please see the clustering section of the user manual.