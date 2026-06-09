package org.example.server;

import org.example.common.command.Response;
import org.example.server.handler.CommandProcessor;
import org.example.server.network.ConnectionReceiver;
import org.example.server.network.ResponseSender;
import org.example.server.service.CollectionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
@SuppressWarnings("BusyWait")

public class ServerMain {
    private static final Logger logger = LoggerFactory.getLogger(ServerMain.class);

    private static final int PORT = 12345;
    private static final String DATA_FILE = "bands.json";

    public static void main(String[] args) {
        logger.info(" Music Band Server Starting...");
        logger.info("Port: {}", PORT);
        logger.info("Data File: {}", DATA_FILE);
        logger.info("Mode: Single-thread, UDP, Non-blocking");

        CollectionManager collectionManager;
        ConnectionReceiver receiver = null;
        ResponseSender sender;
        CommandProcessor processor;

        try {
            collectionManager = new CollectionManager();
            receiver = new ConnectionReceiver(PORT);
            sender = new ResponseSender(receiver.getChannel());
            processor = new CommandProcessor(collectionManager);
            collectionManager.loadCollection(DATA_FILE);

            final CollectionManager managerRef = collectionManager;
            final ConnectionReceiver receiverRef = receiver;
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                logger.info(" Saving collection before shutdown...");
                managerRef.saveCollection(DATA_FILE);
                try {
                    receiverRef.close();
                } catch (IOException e) {
                    logger.warn("Error closing receiver: {}", e.getMessage());
                }
                logger.info(" Server terminated. Goodbye!");
            }));
            logger.info(" Server Ready! Waiting for client connections...");

            while (!Thread.currentThread().isInterrupted()) {
                ConnectionReceiver.ReceivedCommand received = receiver.receiveCommand();

                if (received == null) {
                    try {
                        Thread.sleep(50);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                    continue;
                }

                logger.info(" Receiver request: {} from client", received.command().getCommandType());

                Response response = processor.processCommand(received.command());
                sender.sendResponse(response, received.clientAddress());
                logger.info(" Sent response to client");
            }

        } catch (IOException e) {
            logger.error("❌ Network Error: {}", e.getMessage(), e);
            logger.error(" Hint: Is Port {} already in use?", PORT);

        } catch (ClassNotFoundException e) {
            logger.error("❌ Deserialization Error: {}", e.getMessage(), e);
            logger.error("Hint: Are Command/Response classes Serializable?");

        } finally {
            if (receiver != null) {
                try {
                    receiver.close();
                } catch (IOException e) {
                    logger.warn(" Error closing receiver: {}", e.getMessage());
                }
            }
        }
    }
}
