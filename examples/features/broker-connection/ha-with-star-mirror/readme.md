# High Availability with Star Mirror and Distributed Locks

This example demonstrates how to achieve high availability (HA) using star topology mirroring combined with distributed locks from the Lock Coordinator feature. The distributed locks ensure that only one broker accepts client connections at a time, providing automatic failover without split-brain scenarios across three brokers.

## Overview

This example configures three brokers (server0, server1, and server2) that:
- Mirror all messaging operations to each other using broker connections in a star topology
- Share a distributed file-based lock to coordinate which broker accepts client connections
- Automatically failover client connections when the active broker fails
- Use lock coordinator on broker connections to ensure mirroring only happens when the broker is active

## How It Works

### Star Topology Mirroring Configuration

All three brokers are configured with mirroring to the other two brokers using AMQP broker connections. The key feature is using the **lock-coordinator attribute on broker connections**, which ensures that mirroring only happens when the broker holding the lock is active.

**server0/broker.xml:**
```xml
<broker-connections>
   <amqp-connection uri="tcp://localhost:61001" name="mirrorB" retry-interval="2000" lock-coordinator="clients-lock">
      <mirror sync="true"/>
   </amqp-connection>
   <amqp-connection uri="tcp://localhost:61002" name="mirrorC" retry-interval="2000" lock-coordinator="clients-lock">
      <mirror sync="true"/>
   </amqp-connection>
</broker-connections>
```

**Note:** The first server (server0) is configured with `sync="true"` to demonstrate how synchronous mirroring would work in an HA scenario like this.

**server1/broker.xml:**
```xml
<broker-connections>
   <amqp-connection uri="tcp://localhost:61000" name="mirrorA" retry-interval="2000" lock-coordinator="clients-lock">
      <mirror sync="false"/>
   </amqp-connection>
   <amqp-connection uri="tcp://localhost:61002" name="mirrorC" retry-interval="2000" lock-coordinator="clients-lock">
      <mirror sync="false"/>
   </amqp-connection>
</broker-connections>
```

**server2/broker.xml:**
```xml
<broker-connections>
   <amqp-connection uri="tcp://localhost:61000" name="mirrorA" retry-interval="2000" lock-coordinator="clients-lock">
      <mirror sync="false"/>
   </amqp-connection>
   <amqp-connection uri="tcp://localhost:61001" name="mirrorB" retry-interval="2000" lock-coordinator="clients-lock">
      <mirror sync="false"/>
   </amqp-connection>
</broker-connections>
```

This ensures that messages, queues, and other operations are replicated across all three brokers in a star topology.

### Lock Coordinator for HA

The key feature of this example is the use of **distributed locks** to control which broker accepts client connections. All three brokers are configured with a lock coordinator on their client acceptors:

```xml
<lock-coordinators>
   <lock-coordinator name="clients-lock">
      <class-name>org.apache.activemq.artemis.lockmanager.file.FileBasedLockManager</class-name>
      <lock-id>star-mirror-cluster-clients</lock-id>
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
- Only the broker holding the lock actively mirrors to other brokers (because of lock-coordinator on broker connections)
- If the active broker fails, the lock is automatically released and acquired by another broker
- The backup broker immediately starts accepting connections when it acquires the lock
- The shared lock file prevents split-brain scenarios

### Client Failover

Clients connect using a failover URL that includes all three broker addresses:

```java
ConnectionFactory factory = new org.apache.qpid.jms.JmsConnectionFactory(
   "failover:(amqp://localhost:61616,amqp://localhost:61617,amqp://localhost:61618)?failover.maxReconnectAttempts=-1");
```

When the active broker (holding the lock) fails:
1. The lock is automatically released
2. One of the backup brokers acquires the lock and starts accepting connections
3. The client automatically reconnects to the now-active broker
4. All messages are available due to star mirroring

## Setup

### Create the Servers

You have the following options to run this example:

### Non-interactive way

You can run this example in a non-interactive way, with HAWithStarMirrorExample starting, stopping the servers, and consume messages from your servers during failover.

```shell
mvn verify -Pautomated
```

### Manual Execution

Start each server individually in separate terminals:

```shell
# Install servers
mvn install -Pcreate

# Terminal 1 - Start server0
cd target/server0/bin
./artemis run

# Terminal 2 - qstat on server0 (for better visualization)
cd target/server0/bin
./artemis queue stat --sleep 1000

# Terminal 3 - Start server1
cd target/server1/bin
./artemis run

# Terminal 4 - qstat on server1 (for better visualization)
cd target/server1/bin
./artemis queue stat --sleep 1000

# Terminal 5 - Start server2
cd target/server2/bin
./artemis run

# Terminal 6 - qstat on server2 (for better visualization)
cd target/server2/bin
./artemis queue stat --sleep 1000

# Terminal 7 - Start producer
mvn install -Pproducer

# Terminal 8 - Start consumer
mvn install -Pconsumer
```

### Screen Automation (Linux only)

For a better visual experience, use GNU Screen to automatically layout the terminals.

**Requirements:** Install screen on your Linux machine:
```shell
sudo dnf install screen  # Fedora/RHEL
# or
sudo apt install screen  # Debian/Ubuntu
```

**Start the servers:**
```shell
./screen-servers.sh
```

This will display:
- Server0, Server1, and Server2 running in the left column
- Queue statistics for all three servers in the right column

![Servers Running](src/main/resources/images/servers-running.png)

**Start the clients:**
```shell
./screen-clients.sh
```

This will display:
- Consumer in the top pane
- Producer in the bottom pane

![Clients Running](src/main/resources/images/clients-running.png)

## Testing Failover

To observe the high availability in action:

1. Watch the producer sending messages and the consumer receiving them
2. Kill one of the servers (Ctrl+C in its terminal)
3. Observe that:
   - The client automatically reconnects to the surviving broker
   - Message flow continues without interruption
   - All messages are preserved due to mirroring
   - Interrupt consumers and see how message accumulation will affect mirroring and how messages are replayed after mirrors reconnect.
4. Restart the killed server and observe it reconnecting to each other.

## What to Observe

- **Lock Acquisition**: Only one broker will accept client connections at a time (the one holding the distributed lock)
- **Mirroring**: Messages sent to one broker are replicated to the others
- **Automatic Failover**: When the active broker fails, clients automatically reconnect to one of the backup brokers

## Example Flow

1. All three brokers start with star mirroring configured
2. One broker (typically server0) acquires the distributed lock and accepts client connections
3. The client connects and sends 30 messages to a queue
4. Server0 is killed (simulating a failure)
5. Server1 or Server2 automatically acquires the lock and starts accepting connections
6. The client reconnects to the active broker via failover
7. All 30 messages are consumed from the active broker (due to mirroring)

## Configuration Notes

The lock coordinator supports different lock types (file-based, ZooKeeper). This example uses file-based locks where all brokers must have access to a shared filesystem location.

The `check-period` parameter (in milliseconds) controls how frequently the lock holder verifies it still owns the lock, affecting how quickly failover occurs when a broker crashes.

**Important:** This example demonstrates the star topology with lock coordinator on broker connections. Unlike the dual mirror example, the lock-coordinator attribute is specified on the broker connections themselves, ensuring that mirroring only occurs when a broker holds the lock. This prevents multiple brokers from simultaneously trying to mirror to each other when they don't have the client lock.

## Change the configuration to ZooKeeper

If you want to try ZooKeeper, you need to change the lock-coordinator configuration. The class-name for the Lock Manager and the connect-string must be provided on all three servers.

```xml
<lock-coordinators>
   <lock-coordinator name="clients-lock">
      <class-name>org.apache.activemq.artemis.lockmanager.zookeeper.CuratorDistributedLockManager</class-name>
      <lock-id>star-mirror-cluster-clients</lock-id>
      <check-period>1000</check-period>

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
