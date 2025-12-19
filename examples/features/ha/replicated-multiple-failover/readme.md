# JMS Multiple Failover using Replication Example

If you have not already done so, [prepare the broker distribution](../../../../README.md#getting-started) before running the example.

To run the example, simply type **mvn verify** from this directory.

This example demonstrates three servers coupled as a live-backup-backup group for high availability (HA) using replication, and a client connection failing over from live to backup when the live broker is crashed and then to the second backup once the new live fails.

For more information on Apache Artemis failover and HA, and clustering in general, please see the clustering section of the user manual.