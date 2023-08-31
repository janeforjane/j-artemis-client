package org.example;

import org.apache.activemq.artemis.jms.client.ActiveMQConnectionFactory;

import javax.jms.*;
import java.util.concurrent.TimeUnit;

public class ManyMessages {

    public static void main(String[] args) throws JMSException, InterruptedException {

        Connection connection = null;
        try {

            ConnectionFactory cf = new ActiveMQConnectionFactory();

            connection = cf.createConnection();

            Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

            Queue queue = session.createQueue("myQueue2");


            System.out.println("Queue: " + queue.toString());
            System.out.println("Session: " + session);

            //PRODUCING

            for (int i = 1; i < 100; i++) {

                TimeUnit.SECONDS.sleep(3);

                MessageProducer producer = session.createProducer(queue);
                TextMessage message = session.createTextMessage("This is a text message for myQueue number:" + i);

                producer.send(message);
            }


        } finally {
            if (connection != null) {
                connection.close();
            }
        }



    }
}
