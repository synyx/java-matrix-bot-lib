# Java Matrix Bot Lib

A java library for communicating with a [Matrix](https://matrix.org) server, meant to aid in creating a chatbot.

## Usage

Currently requires at least JDK 21.
If you are using Maven, add the following repository to your `pom.xml`:

```xml

<repositories>
    <repository>
        <id>synyx-public</id>
        <url>https://nexus.synyx.de/repository/public-releases/</url>
    </repository>
</repositories>
```

then you can add `java-matrix-bot-lib` as a dependency:

```xml

<dependency>
    <groupId>org.synyx</groupId>
    <artifactId>java-matrix-bot-lib</artifactId>
    <version>VERSIONHERE</version>
</dependency>
```

## Example

```java
public class MyMatrixBot implements MatrixEventConsumer {

  private MatrixClient matrixClient;

  public MyMatrixBot() {

    MatrixClient matrixClient = MatrixClient.create("https://matrix.example.com", "username", "password");
    matrixClient.setPersistedState(matrixStatePersistence);
  }

  public void startBot() {
    // Blocks the thread
    matrixClient.syncContinuous();
  }

  public void stopBot() {

    matrixClient.requestStopOfSync();
  }

  @Override
  public void onMessage(MatrixState state, MatrixRoom room, MatrixMessage message) {
    // ...
  }
}
```
