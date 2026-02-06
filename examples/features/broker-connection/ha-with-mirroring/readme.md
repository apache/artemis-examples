# High Availability with Mirroring and Distributed Locks

To run the example, simply type **mvn verify** from this directory.

This example demonstrates how to achieve high availability (HA) using mirroring combined with distributed locks from the Lock Coordinator feature. The distributed locks ensure that only one broker accepts client connections at a time, providing automatic failover without split-brain scenarios.

## Overview

This example configures two brokers (server0 and server1) that:
- Mirror all messaging operations to each other using broker connections
- Share a distributed file-based lock to coordinate which broker accepts client connections
- Automatically failover client connections when the active broker fails

## How It Works

### Mirroring Configuration

Both brokers are configured with bidirectional mirroring using AMQP broker connections. Each broker mirrors its data to the other:

**server0/broker.xml:**
```xml
<broker-connections>
   <amqp-connection uri="tcp://localhost:61001" name="mirror" retry-interval="2000">
      <mirror sync="true"/>
   </amqp-connection>
</broker-connections>
```

**server1/broker.xml:**
```xml
<broker-connections>
   <amqp-connection uri="tcp://localhost:61000" name="mirror" retry-interval="2000">
      <mirror sync="false"/>
   </amqp-connection>
</broker-connections>
```

This ensures that messages, queues, and other operations are replicated across both brokers.

### Lock Coordinator for HA

The key feature of this example is the use of **distributed locks** to control which broker accepts client connections. Both brokers are configured with a lock coordinator on their client acceptors:

```xml
<lock-coordinators>
   <lock-coordinator name="clients-lock">
      <class-name>org.apache.activemq.artemis.lockmanager.file.FileBasedLockManager</class-name>
      <lock-id>mirror-cluster-clients</lock-id>
      <check-period>1000</check-period>
      <properties>
         <property key="locks-folder" value="/path/to/shared/locks"/>
      </properties>
   </lock-coordinator>
</lock-coordinators>

<acceptors>
   <acceptor name="forClients" lock-coordinator="clients-lock">tcp://localhost:61616</acceptor>
</acceptors>
```

The lock coordinator ensures that:
- Only the broker holding the lock accepts client connections on that acceptor
- If the active broker fails, the lock is automatically released and acquired by the other broker
- The backup broker immediately starts accepting connections when it acquires the lock
- The shared lock file prevents split-brain scenarios

### Client Failover

Clients connect using a failover URL that includes both broker addresses:

```java
ConnectionFactory factory = new org.apache.qpid.jms.JmsConnectionFactory(
   "failover:(amqp://localhost:61616,amqp://localhost:61617)?failover.maxReconnectAttempts=-1");
```

When the active broker (holding the lock) fails:
1. The lock is automatically released
2. The backup broker acquires the lock and starts accepting connections
3. The client automatically reconnects to the now-active broker
4. All messages are available due to mirroring

## Example Flow

1. Both brokers start with mirroring configured
2. One broker (typically server0) acquires the distributed lock and accepts client connections
3. The client connects and sends 30 messages to a queue
4. Server0 is killed (simulating a failure)
5. Server1 automatically acquires the lock and starts accepting connections
6. The client reconnects to server1 via failover
7. All 30 messages are consumed from server1 (due to mirroring)

## Configuration Notes

The lock coordinator supports different lock types (file-based, zookeeper). This example uses file-based locks where both brokers must have access to a shared filesystem location.

The `check-period` parameter (in milliseconds) controls how frequently the lock holder verifies it still owns the lock, affecting how quickly failover occurs when a broker crashes.

## Change the configuration to ZooKeeper

If you want to try ZooKeeper, you need to change the lock-coordinator configuration. The class-name for the Lock Manager and the connect-string must be provided on server0 and server1.

```xml
<lock-coordinators>
   <lock-coordinator name="clients-lock">
      <class-name>org.apache.activemq.artemis.lockmanager.zookeeper.CuratorDistributedLockManager</class-name>
      <lock-id>mirror-cluster-clients</lock-id>
      <check-period>1000</check-period> <!-- how often to check if the lock is still valid, in milliseconds -->

      <properties>
         <property key="connect-string" value="localhost:2181"/>
      </properties>
   </lock-coordinator>
</lock-coordinators>
```

And of course you need to have ZooKeeper running for the broker. You can do that with Podman or Docker:

```shell
# you can replace podman with docker if you prefer, using the same arguments...
podman run -d --name zookeeper-artemis-test -p 2181:2181 zookeeper:latest
```
