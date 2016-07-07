# Fix-Engine
A sample Fix Engine where a session is generated between a client and exchange. A Client sends an Order which is by default accepted and an Execution report is sent back by the Exchange.

You will need few libraries added to your project.
1. apache-mina.jar
2. quickfixj-core-1.5.2.jar
3. quickfixj-msg-fix42-1.3.3.jar
4. slf4j-api-1.6.0.jar
5. slf4j-simple-1.6.1.jar
6. sol-common-7.1.2.248.jar

Run the Acceptor first. It will act as the Exchange. Then run Initiator.
