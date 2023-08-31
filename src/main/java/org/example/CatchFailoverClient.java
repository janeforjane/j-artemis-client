package org.example;

import org.apache.activemq.artemis.api.core.TransportConfiguration;
import org.apache.activemq.artemis.api.core.client.ActiveMQClient;
import org.apache.activemq.artemis.api.core.client.ServerLocator;
import org.apache.activemq.artemis.api.jms.ActiveMQJMSClient;
import org.apache.activemq.artemis.core.remoting.impl.netty.NettyConnectorFactory;
import org.apache.activemq.artemis.jms.client.ActiveMQConnectionFactory;

import javax.jms.*;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

public class CatchFailoverClient {


    private static ConnectionFactory cf;

    private static ServerLocator locator;

    private static Connection connection;

    private static Queue queue;

    private static Session session;

    private static MessageProducer producer;

    private static TransportConfiguration config1;
    private static TransportConfiguration config2;


    public static void main(String[] args) throws InterruptedException, JMSException {


        prepareTransportConfigs();

        locator = ActiveMQClient.createServerLocatorWithHA(config1, config2);
        cf = new ActiveMQConnectionFactory(locator);//

        createJMSObjects(cf);


        final int numMessages = 15;

        for (int i = 0; i < numMessages; i++) {
            TimeUnit.SECONDS.sleep(3);

            try {
                sendMessage(i);
            } catch (Exception e) {
                System.out.println("Exception occurs. Try to recreate session");
                System.out.println("Exception occurs on " + i + " step");

                i = i - 1;
                connection.close();

                createJMSObjects(cf);
                System.out.println("Recreating of new jms objects completed");
            }

        }

    }

    private static void sendMessage(int i) throws Exception {
        TextMessage messageA = session.createTextMessage("A:This is text message " + i);
        producer.send(messageA);
        System.out.println("A: Sent message: " + messageA.getText());
    }

    private static void prepareTransportConfigs() {

        HashMap<String, Object> map1 = new HashMap<String, Object>();
        map1.put("protocols", "tcp");
        map1.put("host", "localhost");
        map1.put("port", 61616);
        config1 = new TransportConfiguration(NettyConnectorFactory.class.getName(), map1, "master");


        HashMap<String, Object> map2 = new HashMap<String, Object>();
        map2.put("protocols", "tcp");
        map2.put("host", "localhost");
        map2.put("port", 61617);
        config2 = new TransportConfiguration(NettyConnectorFactory.class.getName(), map2, "slave");
    }


    private static void createJMSObjects(ConnectionFactory cf) throws JMSException {


        queue = ActiveMQJMSClient.createQueue("myQueue");

        connection = cf.createConnection();

        connection.start();

        session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

        producer = session.createProducer(queue);

    }

}
