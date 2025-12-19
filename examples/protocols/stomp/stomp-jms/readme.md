# Stomp 1.2 Example

If you have not already done so, [prepare the broker distribution](../../../../README.md#getting-started) before running the example.

To run the example, simply type **mvn verify** from this directory, or **mvn -PnoServer verify** if you want to start and create the broker manually.

This example shows you how to configure Apache Artemis to send and receive Stomp messages using Stomp 1.2 protocol via the JMS API using the [stomp-jms](https://github.com/fusesource/stompjms) client.