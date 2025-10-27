package com.solace.simulator.service;

import com.solace.simulator.model.ConnectionConfig;
import com.solace.simulator.model.MessageRequest;
import com.solace.simulator.model.ReceivedMessage;
import com.solacesystems.jms.SolConnectionFactory;
import com.solacesystems.jms.SolJmsUtility;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import javax.jms.*;
import java.util.*;

@Service
public class SolaceJmsService {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    private Connection connection;
    private Session session;
    private Map<String, MessageConsumer> consumers = new HashMap<>();
    private ConnectionConfig currentConfig;

    public void connect(ConnectionConfig config) throws Exception {
        disconnect();

        SolConnectionFactory connectionFactory = SolJmsUtility.createConnectionFactory();
        connectionFactory.setHost(config.getHost());
        connectionFactory.setVPN(config.getVpnName());
        connectionFactory.setUsername(config.getUsername());
        connectionFactory.setPassword(config.getPassword());

        connectionFactory.setSSLValidateCertificate(false);
        connectionFactory.setSSLHostnameVerificationEnabled(false);

        connection = connectionFactory.createConnection();
        session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        connection.start();
        
        currentConfig = config;
    }

    public void disconnect() throws Exception {
        for (MessageConsumer consumer : consumers.values()) {
            consumer.close();
        }
        consumers.clear();

        if (session != null) {
            session.close();
            session = null;
        }

        if (connection != null) {
            connection.close();
            connection = null;
        }
        
        currentConfig = null;
    }

    public boolean isConnected() {
        return connection != null && currentConfig != null;
    }

    public ConnectionConfig getCurrentConfig() {
        return currentConfig;
    }

    public void sendMessage(MessageRequest request) throws Exception {
        if (!isConnected()) {
            throw new java.lang.IllegalStateException("Not connected to Solace broker");
        }

        Destination destination;
        if ("QUEUE".equalsIgnoreCase(request.getDestinationType())) {
            destination = session.createQueue(request.getDestination());
        } else {
            destination = session.createTopic(request.getDestination());
        }

        MessageProducer producer = session.createProducer(destination);

        Message message;
        if ("BYTE".equalsIgnoreCase(request.getMessageType())) {
            BytesMessage bytesMessage = session.createBytesMessage();
            byte[] bytes = hexStringToByteArray(request.getContent());
            bytesMessage.writeBytes(bytes);
            message = bytesMessage;
        } else {
            TextMessage textMessage = session.createTextMessage(request.getContent());
            message = textMessage;
        }

        // Set JMSReplyTo if provided
        if (request.getReplyTo() != null && !request.getReplyTo().isEmpty()) {
            Destination replyTo;
            if (request.getReplyTo().startsWith("#Q/")) {
                replyTo = session.createQueue(request.getReplyTo().substring(3));
            } else {
                replyTo = session.createTopic(request.getReplyTo());
            }
            message.setJMSReplyTo(replyTo);
        }

        // Set custom headers
        if (request.getHeaders() != null) {
            for (Map.Entry<String, String> entry : request.getHeaders().entrySet()) {
                message.setStringProperty(entry.getKey(), entry.getValue());
            }
        }

        producer.send(message);
        producer.close();
    }

    public void subscribe(String destination, String destinationType) throws Exception {
        if (!isConnected()) {
            throw new java.lang.IllegalStateException("Not connected to Solace broker");
        }

        String key = destinationType + ":" + destination;
        if (consumers.containsKey(key)) {
            return; // Already subscribed
        }

        Destination dest;
        if ("QUEUE".equalsIgnoreCase(destinationType)) {
            dest = session.createQueue(destination);
        } else {
            dest = session.createTopic(destination);
        }

        MessageConsumer consumer = session.createConsumer(dest);
        consumer.setMessageListener(message -> {
            try {
                ReceivedMessage receivedMsg = new ReceivedMessage();
                receivedMsg.setDestination(destination);

                if (message instanceof TextMessage) {
                    TextMessage textMessage = (TextMessage) message;
                    receivedMsg.setMessageType("TEXT");
                    receivedMsg.setContent(textMessage.getText());
                    receivedMsg.setHexContent(stringToHex(textMessage.getText()));
                } else if (message instanceof BytesMessage) {
                    BytesMessage bytesMessage = (BytesMessage) message;
                    byte[] bytes = new byte[(int) bytesMessage.getBodyLength()];
                    bytesMessage.readBytes(bytes);
                    receivedMsg.setMessageType("BYTE");
                    receivedMsg.setHexContent(bytesToHex(bytes));
                    receivedMsg.setContent(new String(bytes));
                }

                // Extract headers
                Map<String, String> headers = new HashMap<>();
                Enumeration<?> propertyNames = message.getPropertyNames();
                while (propertyNames.hasMoreElements()) {
                    String name = (String) propertyNames.nextElement();
                    headers.put(name, message.getStringProperty(name));
                }
                receivedMsg.setHeaders(headers);

                // Send to WebSocket
                messagingTemplate.convertAndSend("/topic/messages", receivedMsg);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        consumers.put(key, consumer);
    }

    public void unsubscribe(String destination, String destinationType) throws Exception {
        String key = destinationType + ":" + destination;
        MessageConsumer consumer = consumers.remove(key);
        if (consumer != null) {
            consumer.close();
        }
    }

    private byte[] hexStringToByteArray(String hexString) {
        // Remove spaces and convert to bytes
        hexString = hexString.replaceAll("\\s+", "");
        int len = hexString.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(hexString.charAt(i), 16) << 4)
                    + Character.digit(hexString.charAt(i + 1), 16));
        }
        return data;
    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02X ", b));
        }
        return sb.toString().trim();
    }

    private String stringToHex(String str) {
        return bytesToHex(str.getBytes());
    }
}
