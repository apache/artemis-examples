# JMS Completion Listener Example

If you have not already done so, [prepare the broker distribution](../../../../README.md#getting-started) before running the example.

To run the example, simply type **mvn verify** from this directory, or **mvn -PnoServer verify** if you want to start and create the broker manually.

This example shows you how to send a message asynchronously to Apache Artemis and use a CompletionListener to be notified of the Broker receiving it.