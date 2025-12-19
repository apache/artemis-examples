# JMS Replicated Failback Example

If you have not already done so, [prepare the broker distribution](../../../../README.md#getting-started) before running the example.

To run the example, simply type **mvn verify** from this directory.

This example demonstrates two servers coupled as a live-backup pair for high availability (HA) using replication and a client connection failing over from live to backup when the live broker is crashed and then back to the original live when it is restarted (i.e. "failback").

For more information on Apache Artemis failover and HA, and clustering in general, please see the clustering section of the user manual.