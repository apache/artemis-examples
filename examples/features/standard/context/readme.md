# JMS Context Example

If you have not already done so, [prepare the broker distribution](../../../../README.md#getting-started) before running the example.

To run the example, simply type **mvn verify** from this directory, or **mvn -PnoServer verify** if you want to start and create the broker manually.

This example shows you how to send and receive a message to a JMS Queue using Apache Artemis by using a JMSContext.

A JMSContext is part of JMS 2.0 and combines the JMS Connection and Session Objects into a simple interface.