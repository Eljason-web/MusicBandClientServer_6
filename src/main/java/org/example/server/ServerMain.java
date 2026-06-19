package org.example.server;

import org.example.common.command.Response;
import org.example.server.database.DatabaseManager;
import org.example.server.database.UserDAO;
import org.example.server.handler.CommandProcessor;
import org.example.server.network.ConnectionReceiver;
import org.example.server.network.ResponseSender;
import org.example.server.service.CollectionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;

@SuppressWarnings("BusyWait")

public class ServerMain {
    private static final Logger logger = LoggerFactory.getLogger(ServerMain.class);

    private static final int PORT = 12345;

    public static void main(String[] args) {
        logger.info(" Music Band Server Starting...");
        logger.info("Port: {}", PORT);
        logger.info("Mode: Single-thread, UDP, Non-blocking");

        DatabaseManager.testConnection();
        UserDAO.registerUser("testuser", "password123");
        UserDAO.registerUser("admin", "admin123");

        boolean success = UserDAO.authenticateUser("testuser", "password123");
        System.out.println("Authentication test: " + (success ? " Pass" : " FAIL"));

        ExecutorService readerPool = Executors.newCachedThreadPool();

        ForkJoinPool processorPool = ForkJoinPool.commonPool();

        ExecutorService senderPool = Executors.newFixedThreadPool(4);

        CollectionManager collectionManager;
        ConnectionReceiver receiver = null;

        try {
            collectionManager = new CollectionManager();
            receiver = new ConnectionReceiver(PORT);
            final ResponseSender sender = new ResponseSender(receiver.getChannel());
            final CommandProcessor processor = new CommandProcessor(collectionManager);

            collectionManager.loadFromDatabase();

            final CollectionManager managerRef = collectionManager;
            final ConnectionReceiver receiverRef = receiver;

            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                logger.info(" shutting down server...");

                readerPool.shutdown();
                processorPool.shutdown();
                senderPool.shutdown();

                try {
                    managerRef.saveToDatabase();
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

                processorPool.submit(() -> {
                    Response response = processor.processCommand(received.command());

                    senderPool.submit(() -> {
                        sender.sendResponse(response, received.clientAddress());
                        logger.info(" Sent response to client");
                    });
                });
            }
        } catch (ClassNotFoundException e) {
            logger.error("❌ Deserialization Error: {}", e.getMessage(), e);
            logger.error("Hint: Are Command/Response classes Serializable?");

        } catch (IOException e) {
            logger.warn(" Network Error: {}", e.getMessage(), e);
            logger.error(" Hint: Is Port {} already in use?", PORT);

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
