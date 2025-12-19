# Management Example

If you have not already done so, [prepare the broker distribution](../../../../README.md#getting-started) before running the example.

To run the example, simply type **mvn verify** from this directory.

This example shows how to manage Apache Artemis using JMS Messages to invoke management operations on the server.

To manage Apache Artemis using JMX, see the "jmx" example.

## Example configuration

Apache Artemis can be managed by sending JMS messages with specific properties to its _management_ queue.

By default, the management name is called `activemq.management` but this can be configured in broker.xml like so:

    <management-address>activemq.management</management-address>

The management queue requires a "special" user permission `manage` to be able to receive management messages. This is also configured in broker.xml:

    <security-setting match="activemq.management">
       <permission type="manage" roles="guest" />
    </security-setting>