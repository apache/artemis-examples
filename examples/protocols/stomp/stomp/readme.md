# Stomp Example

If you have not already done so, [prepare the broker distribution](../../../../README.md#getting-started) before running the example.

To run the example, simply type **mvn verify** from this directory, or **mvn -PnoServer verify** if you want to start and create the broker manually.

This example shows you how to configure Apache Artemis to send and receive Stomp messages.

The client will open a socket to send one Stomp message (using TCP directly). The client will then consume a message from a queue and check it is the message sent with Stomp.