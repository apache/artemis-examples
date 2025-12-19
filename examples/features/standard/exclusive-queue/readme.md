# JMS Exclusive Queue Example

If you have not already done so, [prepare the broker distribution](../../../../README.md#getting-started) before running the example.

To run the example, simply type **mvn verify** from this directory, or **mvn -PnoServer verify** if you want to start and create the broker manually.

This example shows you how to configure Apache Artemis so all messages are delivered to the same consumer

## ExclusiveQueueExample.java

The broker is configured (using `address-settings`), so that the queue is exclusive and will deliver all messages to the same
consumer

```xml
<address-settings>
   <address-setting match="my.exclusive.queue">
      <default-exclusive-queue>true</default-exclusive-queue>
   </address-setting>
</address-settings>
```

## ExclusiveQueueClientSideExample.java

The JMS  Queue is auto created from the client code and uses the `exclusive` parameter.

```java
Queue queue = session.createQueue("client.side.exclusive.queue?exclusive=true");
```

This example also shows that all remaining messages are sent to another consumer when the first consumer (that was receiving
all messages), is closed.