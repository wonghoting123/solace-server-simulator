package com.solace.simulator.service;

import com.solace.simulator.model.HandshakeRequest;
import com.solace.simulator.model.HandshakeSimulation;
import com.solace.simulator.model.SourceSystem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import javax.jms.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.*;

/**
 * Service to manage handshake simulations between AP & BCS
 */
@Service
public class HandshakeService {

    @Autowired
    private SolaceJmsService solaceJmsService;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    private final Map<String, HandshakeSimulation> activeSimulations = new ConcurrentHashMap<>();
    private final Map<String, ScheduledFuture<?>> scheduledTasks = new ConcurrentHashMap<>();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(4);
    private final Map<String, MessageConsumer> queueConsumers = new ConcurrentHashMap<>();

    /**
     * Start a handshake simulation for a source system
     */
    public synchronized HandshakeSimulation startSimulation(HandshakeRequest request) throws Exception {
        // Parse source system
        SourceSystem sourceSystem;
        try {
            sourceSystem = SourceSystem.valueOf(request.getSourceSystem());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid source system: " + request.getSourceSystem());
        }

        // Check if simulation already running for this source system
        String simulationKey = sourceSystem.name();
        if (activeSimulations.containsKey(simulationKey)) {
            throw new java.lang.IllegalStateException("Handshake simulation already running for " + sourceSystem.getDisplayName());
        }

        // Validate business date format
        try {
            LocalDate.parse(request.getBusinessDate(), DateTimeFormatter.ISO_LOCAL_DATE);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid business date format. Expected: YYYY-MM-DD");
        }

        // Check if connected to Solace
        if (!solaceJmsService.isConnected()) {
            throw new java.lang.IllegalStateException("Not connected to Solace broker");
        }

        // Create simulation
        String simulationId = UUID.randomUUID().toString();
        HandshakeSimulation simulation = new HandshakeSimulation(simulationId, sourceSystem, request.getBusinessDate());
        activeSimulations.put(simulationKey, simulation);

        // Start message flow 1: Listen for incoming message 322 and reply with 321
        startMessageFlow1(sourceSystem, request.getBusinessDate(), simulationKey);

        // Start message flow 2: Send message 322 and wait for reply, repeat every 5 seconds
        ScheduledFuture<?> future = scheduler.scheduleWithFixedDelay(
                () -> executeMessageFlow2(sourceSystem, request.getBusinessDate(), simulationKey),
                0, 5, TimeUnit.SECONDS
        );
        scheduledTasks.put(simulationKey, future);

        // Notify clients via WebSocket
        notifyClients();

        return simulation;
    }

    /**
     * Stop a handshake simulation
     */
    public synchronized void stopSimulation(String sourceSystemName) throws Exception {
        SourceSystem sourceSystem;
        try {
            sourceSystem = SourceSystem.valueOf(sourceSystemName);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid source system: " + sourceSystemName);
        }

        String simulationKey = sourceSystem.name();
        
        // Stop scheduled task
        ScheduledFuture<?> future = scheduledTasks.remove(simulationKey);
        if (future != null) {
            future.cancel(false);
        }

        // Stop queue consumer
        MessageConsumer consumer = queueConsumers.remove(simulationKey);
        if (consumer != null) {
            try {
                consumer.close();
            } catch (Exception e) {
                // Ignore close errors
            }
        }

        // Remove simulation
        HandshakeSimulation simulation = activeSimulations.remove(simulationKey);
        if (simulation != null) {
            simulation.setStatus("STOPPED");
        }

        // Notify clients via WebSocket
        notifyClients();
    }

    /**
     * Get list of active simulations
     */
    public List<HandshakeSimulation> getActiveSimulations() {
        return new ArrayList<>(activeSimulations.values());
    }

    /**
     * Message Flow 1: Listen for message 322 from queue and reply with message 321
     */
    private void startMessageFlow1(SourceSystem sourceSystem, String businessDate, String simulationKey) throws Exception {
        Session session = solaceJmsService.getSession();
        javax.jms.Queue queue = session.createQueue(sourceSystem.getIncomingQueue());
        MessageConsumer consumer = session.createConsumer(queue);

        consumer.setMessageListener(message -> {
            try {
                if (message instanceof BytesMessage) {
                    BytesMessage bytesMessage = (BytesMessage) message;
                    byte[] receivedBytes = new byte[(int) bytesMessage.getBodyLength()];
                    bytesMessage.readBytes(receivedBytes);

                    // Check if this is a handshake request (message code 322)
                    if (receivedBytes.length >= 2) {
                        int messageCode = ((receivedBytes[1] & 0xFF) << 8) | (receivedBytes[0] & 0xFF);
                        
                        if (messageCode == 322) {
                            // Send reply with message code 321
                            byte[] replyBytes = createHandshakeReply(sourceSystem);
                            sendHandshakeReply(sourceSystem, replyBytes);
                            
                            // Log to console
                            System.out.println("Message Flow 1: Received 322 from " + sourceSystem.getDisplayName() + 
                                             ", sent 321 reply");
                        }
                    }
                }
            } catch (Exception e) {
                System.err.println("Error in message flow 1: " + e.getMessage());
                e.printStackTrace();
            }
        });

        queueConsumers.put(simulationKey, consumer);
    }

    /**
     * Message Flow 2: Send message 322 and wait for reply 321
     */
    private void executeMessageFlow2(SourceSystem sourceSystem, String businessDate, String simulationKey) {
        try {
            // Check if simulation is still active
            if (!activeSimulations.containsKey(simulationKey)) {
                return;
            }

            // Create and send handshake request (message 322)
            byte[] requestBytes = createHandshakeRequest(sourceSystem, businessDate);
            sendHandshakeRequest(sourceSystem, requestBytes);

            System.out.println("Message Flow 2: Sent 322 to " + sourceSystem.getDisplayName());
            
            // Note: Reply listening is handled by the queue consumer in message flow 1
            // In a real implementation, you would correlate request/reply here
            
        } catch (Exception e) {
            System.err.println("Error in message flow 2: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Create handshake request message (322)
     * Format: Message code (2), Source system (1), Destination system (1), Business date (4), Checksum (1)
     */
    private byte[] createHandshakeRequest(SourceSystem sourceSystem, String businessDate) {
        byte[] message = new byte[9];
        
        // Message code 322 (0x0142) - little endian
        message[0] = 0x42;
        message[1] = 0x01;
        
        // Source system number (BCS system)
        message[2] = (byte) sourceSystem.getSystemNumber();
        
        // Destination system number (AGP system)
        message[3] = (byte) sourceSystem.getDestinationSystemNumber();
        
        // Business date (4 bytes: day, month, year low, year high)
        LocalDate date = LocalDate.parse(businessDate, DateTimeFormatter.ISO_LOCAL_DATE);
        message[4] = (byte) date.getDayOfMonth();
        message[5] = (byte) date.getMonthValue();
        int year = date.getYear();
        message[6] = (byte) (year & 0xFF);
        message[7] = (byte) ((year >> 8) & 0xFF);
        
        // Calculate checksum (XOR of bytes 0-7)
        byte checksum = 0;
        for (int i = 0; i < 8; i++) {
            checksum ^= message[i];
        }
        message[8] = checksum;
        
        return message;
    }

    /**
     * Create handshake reply message (321)
     * Format: Message code (2), Source system (1), Destination system (1), Checksum (1)
     */
    private byte[] createHandshakeReply(SourceSystem sourceSystem) {
        byte[] message = new byte[5];
        
        // Message code 321 (0x0141) - little endian
        message[0] = 0x41;
        message[1] = 0x01;
        
        // Source system number (BCS system)
        message[2] = (byte) sourceSystem.getSystemNumber();
        
        // Destination system number (AGP system)
        message[3] = (byte) sourceSystem.getDestinationSystemNumber();
        
        // Calculate checksum (XOR of bytes 0-3)
        byte checksum = 0;
        for (int i = 0; i < 4; i++) {
            checksum ^= message[i];
        }
        message[4] = checksum;
        
        return message;
    }

    /**
     * Send handshake request to topic
     */
    private void sendHandshakeRequest(SourceSystem sourceSystem, byte[] messageBytes) throws Exception {
        Session session = solaceJmsService.getSession();
        Topic topic = session.createTopic(sourceSystem.getOutgoingTopic());
        MessageProducer producer = session.createProducer(topic);
        
        BytesMessage message = session.createBytesMessage();
        message.writeBytes(messageBytes);
        
        // Set headers
        message.setIntProperty("destination_system_number", sourceSystem.getDestinationSystemNumber());
        message.setStringProperty("sender_hostname", "BADEV23");
        message.setBooleanProperty("Solace_JMS_Prop_IS_Reply_Message", false);
        message.setStringProperty("sender_systemcode", "BCSBA");
        message.setStringProperty("event_prefix_topic", 
            "hk/g/inct/ipc/01/upd/bcs_ba/brc_opr/" + sourceSystem.getDestinationSystemNumber() + "/" + sourceSystem.getSystemNumber());
        message.setBooleanProperty("JMS_Solace_MsgDiscardIndication", false);
        message.setBooleanProperty("JMS_Solace_DeliverToOne", false);
        message.setIntProperty("JMSXDeliveryCount", 1);
        message.setIntProperty("message_code", 322);
        message.setBooleanProperty("JMS_Solace_ElidingEligible", false);
        message.setBooleanProperty("JMS_Solace_DeadMsgQueueEligible", false);
        message.setLongProperty("SenderTimeStamp", System.currentTimeMillis());
        message.setIntProperty("source_system_number", sourceSystem.getSystemNumber());
        message.setStringProperty("sender_uniqueid", UUID.randomUUID().toString());
        
        producer.send(message);
        producer.close();
    }

    /**
     * Send handshake reply to topic
     */
    private void sendHandshakeReply(SourceSystem sourceSystem, byte[] messageBytes) throws Exception {
        Session session = solaceJmsService.getSession();
        Topic topic = session.createTopic(sourceSystem.getOutgoingTopic());
        MessageProducer producer = session.createProducer(topic);
        
        BytesMessage message = session.createBytesMessage();
        message.writeBytes(messageBytes);
        
        // Set headers
        message.setIntProperty("destination_system_number", sourceSystem.getDestinationSystemNumber());
        message.setStringProperty("sender_hostname", "BADEV23");
        message.setBooleanProperty("Solace_JMS_Prop_IS_Reply_Message", false);
        message.setStringProperty("sender_systemcode", "BCSBA");
        message.setStringProperty("event_prefix_topic", 
            "hk/g/inct/ipc/01/upd/bcs_ba/brc_opr/" + sourceSystem.getDestinationSystemNumber() + "/" + sourceSystem.getSystemNumber());
        message.setBooleanProperty("JMS_Solace_MsgDiscardIndication", false);
        message.setBooleanProperty("JMS_Solace_DeliverToOne", false);
        message.setIntProperty("JMSXDeliveryCount", 1);
        message.setIntProperty("message_code", 321);
        message.setBooleanProperty("JMS_Solace_ElidingEligible", false);
        message.setBooleanProperty("JMS_Solace_DeadMsgQueueEligible", false);
        message.setLongProperty("SenderTimeStamp", System.currentTimeMillis());
        message.setIntProperty("source_system_number", sourceSystem.getSystemNumber());
        message.setStringProperty("sender_uniqueid", UUID.randomUUID().toString());
        
        producer.send(message);
        producer.close();
    }

    /**
     * Notify WebSocket clients about simulation updates
     */
    private void notifyClients() {
        messagingTemplate.convertAndSend("/topic/handshake", getActiveSimulations());
    }

    /**
     * Cleanup on shutdown
     */
    public void shutdown() {
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
        }
        
        // Close all consumers
        for (MessageConsumer consumer : queueConsumers.values()) {
            try {
                consumer.close();
            } catch (Exception e) {
                // Ignore
            }
        }
    }
}
