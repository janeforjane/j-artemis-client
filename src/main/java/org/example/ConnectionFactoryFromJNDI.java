package org.example;

import javax.jms.*;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.util.concurrent.TimeUnit;

public class ConnectionFactoryFromJNDI {

    public static void main(String[] args) throws NamingException, JMSException, InterruptedException {


        // Step 1. Get an initial context for looking up JNDI from server 0
        InitialContext initialContext = new InitialContext();

        // Step 2. Look-up the JMS Queue object from JNDI
        Queue queue = (Queue) initialContext.lookup("queue/exampleQueue");// jndi.properties

        // Step 3. Look-up a JMS Connection Factory object from JNDI on server 0
        ConnectionFactory connectionFactory = (ConnectionFactory) initialContext.lookup("ConnectionFactory"); // jndi.properties

        // Step 4. We create 1 JMS connections from the same connection factory.
        // Wait a little while to make sure broadcasts from all nodes have reached the client
        Thread.sleep(5000);
        Connection connectionA = connectionFactory.createConnection();

        // Step 5. We create JMS Sessions
        Session sessionA = connectionA.createSession(false, Session.AUTO_ACKNOWLEDGE);

        // Step 6. We create JMS MessageProducer objects on the sessions
        MessageProducer producerA = sessionA.createProducer(queue);

        // Step 7. We send some messages on each producer
        final int numMessages = 100;

        for (int i = 0; i < numMessages; i++) {
            TimeUnit.SECONDS.sleep(3);
            TextMessage messageA = sessionA.createTextMessage("A:This is text message " + i);
            producerA.send(messageA);
            System.out.println("Sent message: " + messageA.getText());

        }

        // Step 8. We start the connection to consume messages
        connectionA.start();


    }
}
