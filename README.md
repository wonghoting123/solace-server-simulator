# Solace Server Simulator

A Java web application built with Spring Boot and Gradle for sending and receiving messages to/from Solace topics and queues via JMS.

## Features

### 1. Connection Configuration
- Configure Solace broker connection through the web UI
- Set host, VPN name, username, and password dynamically
- No server-side configuration required

### 2. Message Sending
- Send messages to Solace topics or queues
- Support for both text and byte messages
- Byte messages use hexadecimal format for input (e.g., `48 65 6C 6C 6F` for "Hello")
- Set JMSReplyTo destination (topics or queues with `#Q/` prefix)
- Add custom header properties to messages

### 3. Message Receiving
- Subscribe to topics or queues
- Real-time message display using WebSocket
- View message content as both text and hexadecimal format
- Display message headers and timestamps
- Messages appear instantly in the UI

## Requirements

- Java 17 or higher
- Gradle 8.5+ (included via wrapper)
- Access to a Solace broker instance

## Building the Application

```bash
# Build the project
./gradlew clean build

# Skip tests during build
./gradlew clean build -x test
```

## Running the Application

```bash
# Run using Gradle
./gradlew bootRun

# Or run the JAR file directly
java -jar build/libs/solace-server-simulator-1.0.0.jar
```

The application will start on port 8080 by default.

## Accessing the Web UI

Open your browser and navigate to:
```
http://localhost:8080
```

## Usage Guide

### 1. Connect to Solace Broker

1. Enter your Solace broker details:
   - **Host**: e.g., `tcp://localhost:55555`
   - **VPN Name**: e.g., `default`
   - **Username**: e.g., `admin`
   - **Password**: e.g., `admin`
2. Click **Connect**

### 2. Send Messages

1. Enter the destination (topic or queue name)
2. Select destination type (Topic or Queue)
3. Choose message type:
   - **Text**: Regular text messages
   - **Byte (Hexadecimal)**: Enter hex values (e.g., `48656C6C6F` or `48 65 6C 6C 6F`)
4. (Optional) Set JMSReplyTo destination
5. (Optional) Add custom headers by entering key-value pairs
6. Enter message content
7. Click **Send Message**

### 3. Subscribe to Messages

1. Enter the destination (topic or queue name)
2. Select destination type (Topic or Queue)
3. Click **Subscribe**
4. Received messages will appear in real-time in the "Received Messages" section

### 4. View Received Messages

Each received message displays:
- Destination name
- Message type (TEXT or BYTE)
- Message content (as text)
- Hexadecimal representation
- Custom headers
- Timestamp

## Technology Stack

- **Spring Boot 3.2.0** - Application framework
- **Solace JMS 10.21.0** - JMS client for Solace
- **WebSocket (STOMP)** - Real-time message updates
- **Gradle 8.5** - Build tool
- **SockJS & STOMP.js** - WebSocket client libraries

## Project Structure

```
src/
├── main/
│   ├── java/com/solace/simulator/
│   │   ├── SolaceSimulatorApplication.java     # Main application class
│   │   ├── config/
│   │   │   └── WebSocketConfig.java           # WebSocket configuration
│   │   ├── controller/
│   │   │   └── SolaceController.java          # REST API endpoints
│   │   ├── model/
│   │   │   ├── ConnectionConfig.java          # Connection configuration model
│   │   │   ├── MessageRequest.java            # Message sending request model
│   │   │   ├── ReceivedMessage.java           # Received message model
│   │   │   └── SubscriptionRequest.java       # Subscription request model
│   │   └── service/
│   │       └── SolaceJmsService.java          # JMS service implementation
│   └── resources/
│       ├── application.properties              # Application configuration
│       └── static/
│           └── index.html                      # Web UI
```

## API Endpoints

- `POST /api/connect` - Connect to Solace broker
- `POST /api/disconnect` - Disconnect from Solace broker
- `GET /api/status` - Get connection status
- `POST /api/send` - Send a message
- `POST /api/subscribe` - Subscribe to a topic/queue
- `POST /api/unsubscribe` - Unsubscribe from a topic/queue

## WebSocket Endpoint

- `/ws` - WebSocket connection for real-time message updates
- `/topic/messages` - Topic for receiving messages in the UI

## Example Hexadecimal Conversions

- `Hello` = `48 65 6C 6C 6F`
- `World` = `57 6F 72 6C 64`
- `Test` = `54 65 73 74`

You can enter hex values with or without spaces. The application will parse them correctly.

## Notes

- Ensure your Solace broker is running and accessible
- The application supports both topics and queues
- For queue destinations in JMSReplyTo, use the `#Q/` prefix (e.g., `#Q/reply.queue`)
- All connections are managed through the UI - no server restart required for configuration changes
