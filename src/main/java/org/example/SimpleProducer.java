package org.example;

import org.apache.activemq.artemis.jms.client.ActiveMQConnectionFactory;

import javax.jms.*;

public class SimpleProducer {

    //simple queue

    public static void main(String[] args) throws JMSException {

        Connection connection = null;
        try {

            ConnectionFactory cf = new ActiveMQConnectionFactory();// default url: "tcp://localhost:61616"
            connection = cf.createConnection();

            Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            Queue queue = session.createQueue("myQueue");


            MessageProducer producer = session.createProducer(queue);
            TextMessage message = session.createTextMessage("This is a text message for myQueue");

            producer.send(message);

        } finally {
            if (connection != null) {
                connection.close();
            }
        }



    }
}
