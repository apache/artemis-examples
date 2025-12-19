# JMS XA Send Example

If you have not already done so, [prepare the broker distribution](../../../../README.md#getting-started) before running the example.

To run the example, simply type **mvn verify** from this directory, or **mvn -PnoServer verify** if you want to start and create the broker manually.

This example shows you how message sending behaves in an XA transaction in Apache Artemis. When a message is sent within the scope of an XA transaction, it will only reach the queue once the transaction is committed. If the transaction is rolled back the sent messages will be discarded by the server.

Apache Artemis is JTA aware, meaning you can use Apache Artemis in a XA transactional environment and participate in XA transactions. It provides the `javax.transaction.xa.XAResource` interface for that purpose. Users can get a XAConnectionFactory to create XAConnections and XASessions.

In this example we simulate a transaction manager to control the transactions. First we create an XASession and enlist it in a transaction through its XAResource. We then send two words, `hello` and `world`, with the session, let the transaction roll back. The messages are discarded and never be received. Next we start a new transaction with the same XAResource, but this time we commit the transaction. Both messages are received.