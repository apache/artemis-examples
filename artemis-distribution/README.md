## Apache Artemis Distribution for Examples

This is a helper module which prepares an extracted Apache Artemis distribution for use by the various example modules as they are run.

Use it by executing a command such as:

```
mvn clean package
```

This will result in Maven unpacking the Apache Artemis binary archive in directory `./target/apache-artemis-<version>-bin`, from where individual examples will then reference it when creating their broker instances as they are being run.
