# Embedded Broker with Programmatic Config Example

If you have not already done so, [prepare the broker distribution](../../../../README.md#getting-started) before running the example.

To run the example, simply type **mvn verify** from this directory.

This examples shows how to setup and run an embedded broker using Apache Artemis.

Apache Artemis was designed using POJOs (Plain Old Java Objects) which means embedding Apache Artemis in your own application is as simple as instantiating a few objects.

This example does not use any configuration files. The broker is configured using POJOs and can be easily ported to any dependency injection framework.