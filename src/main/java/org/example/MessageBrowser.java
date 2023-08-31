package org.example;

import org.apache.activemq.artemis.jms.client.ActiveMQConnectionFactory;

import javax.jms.*;
import java.util.Enumeration;

public class MessageBrowser {

    public static void main(String[] args) throws JMSException {


        Connection connection = null;
        try {

            ConnectionFactory cf = new ActiveMQConnectionFactory();

            connection = cf.createConnection();

            Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

            Queue queue = session.createQueue("myQueue2");

            System.out.println("Queue: " + queue.toString());
            System.out.println("Session: " + session);

            //PRODUCING

            MessageProducer producer = session.createProducer(queue);
            TextMessage message1 = session.createTextMessage("This is a text message for myQueue number FOR BROWSER APP");

            producer.send(message1);

            //  Create the JMS QueueBrowser
            QueueBrowser browser = session.createBrowser(queue);

            // Browse the messages on the queue
            // Browsing a queue does not remove the messages from the queue
            Enumeration messageEnum = browser.getEnumeration();
            while (messageEnum.hasMoreElements()) {
                TextMessage message = (TextMessage) messageEnum.nextElement();
                System.out.println("Browsing: " + message.getText());
            }

            // Step 11. Close the browser
            browser.close();


        } finally {
            if (connection != null) {
                connection.close();
            }
        }


    }
}
