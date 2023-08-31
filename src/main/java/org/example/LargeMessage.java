package org.example;

import org.apache.activemq.artemis.jms.client.ActiveMQConnectionFactory;

import javax.jms.*;
import java.io.*;
import java.util.concurrent.TimeUnit;

public class LargeMessage {

//    private static final long FILE_SIZE = 2L * 1024 * 1024 * 1024; // 2 GiB message
    private static final long FILE_SIZE = 1024 * 1024; // 1 MB message

    public static void main(String[] args) throws JMSException, IOException, InterruptedException {

        Connection connection = null;
        File inputFile = null;
        File outputFile = null;

        try {

            ConnectionFactory cf = new ActiveMQConnectionFactory();

            connection = cf.createConnection();

            Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

            Queue queue = session.createQueue("myQueueForLargeMessages");

            System.out.println("Queue: " + queue.toString());
            System.out.println("Session: " + session);

            MessageProducer producer = session.createProducer(queue);

            // Create a huge file - this will form the body of the message we will send.

            System.out.println("Creating a file to send of size " + FILE_SIZE +
                    " bytes. This may take a little while... " +
                    "If this is too big for your disk you can easily change the FILE_SIZE in the example.");

            inputFile = new File("huge_message_to_send.dat");

            createFile(inputFile, FILE_SIZE);

            System.out.println("File created.");

            // Create a BytesMessage
            BytesMessage message = session.createBytesMessage();

            // We set the InputStream on the message. When sending the message will read the InputStream
            // until it gets EOF. In this case we point the InputStream at a file on disk, and it will suck up the entire
            // file, however we could use any InputStream not just a FileInputStream.
            FileInputStream fileInputStream = new FileInputStream(inputFile);
            BufferedInputStream bufferedInput = new BufferedInputStream(fileInputStream);

            message.setObjectProperty("JMS_AMQ_InputStream", bufferedInput);

            System.out.println("Sending the huge message.");

            // Send the Message
            producer.send(message);


            //SLEEP
            System.out.println("Sleeping begins");

            TimeUnit.SECONDS.sleep(60);

            System.out.println("1 minute over");

            TimeUnit.SECONDS.sleep(30);

            System.out.println("1,5 minute over");

            TimeUnit.SECONDS.sleep(30);

            System.out.println("Sleeping is over");


            //CONSUMING


            MessageConsumer messageConsumer = session.createConsumer(queue);

            connection.start();

            System.out.println("Receiving message.");

            // Step 12. Receive the message. When we receive the large message we initially just receive the message with
            // an empty body.
            BytesMessage messageReceived = (BytesMessage) messageConsumer.receive(120000);

            System.out.println("Received message with: " + messageReceived.getLongProperty("_AMQ_LARGE_SIZE") +
                    " bytes. Now streaming to file on disk.");

            // Step 13. We set an OutputStream on the message. This causes the message body to be written to the
            // OutputStream until there are no more bytes to be written.
            // You don't have to use a FileOutputStream, you can use any OutputStream.
            // You may choose to use the regular BytesMessage or
            // StreamMessage interface but this method is much faster for large messages.

            outputFile = new File("huge_message_received.dat");

            try (FileOutputStream fileOutputStream = new FileOutputStream(outputFile)) {
                BufferedOutputStream bufferedOutput = new BufferedOutputStream(fileOutputStream);

                // Step 14. This will save the stream and wait until the entire message is written before continuing.
                messageReceived.setObjectProperty("JMS_AMQ_SaveStream", bufferedOutput);
            }

            System.out.println("File streamed to disk. Size of received file on disk is " + outputFile.length());


        } finally {
            if (connection != null) {
                connection.close();
            }
        }


    }

    private static void createFile(final File file, final long fileSize) throws IOException {
        FileOutputStream fileOut = new FileOutputStream(file);
        try (BufferedOutputStream buffOut = new BufferedOutputStream(fileOut)) {
            byte[] outBuffer = new byte[1024 * 1024];
            for (long i = 0; i < fileSize; i += outBuffer.length) {
                buffOut.write(outBuffer);
            }
        }
    }
}
