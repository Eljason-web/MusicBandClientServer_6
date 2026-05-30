package org.example.server;

import org.example.common.command.Command;
import org.example.common.command.Response;
import org.example.server.handler.CommandProcessor;
import org.example.server.network.ConnectionReceiver;
import org.example.server.network.ResponseSender;
import org.example.server.service.CollectionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class ServerMain {
    private static final Logger logger = LoggerFactory.getLogger(ServerMain.class);

    private static final int PORT = 12345;
    private static final String DATA_FILE = "bands.json";

    public static void main(String[] args) {
        logger.info(" Music Band Server Starting...");
        logger.info("Port: {}", PORT);
        logger.info("Data File: {}", DATA_FILE);
        logger.info("Mode: Single-thread, UDP, Non-blocking");

        CollectionManager collectionManager = null;
        ConnectionReceiver receiver = null;
        ResponseSender sender;
        CommandProcessor processor;

        try {
            collectionManager = new CollectionManager();
            receiver = new ConnectionReceiver(PORT);
            sender = new ResponseSender(receiver.getChannel());
            processor = new CommandProcessor(collectionManager);
            collectionManager.loadCollection(DATA_FILE);

            logger.info(" Server Ready! Waiting for client connections...");

            while (true) {
                ConnectionReceiver.ReceivedCommand received = receiver.receiveCommand();

                if (received == null) {
                    Thread.sleep(50);
                    continue;
                }

                logger.info(" Receiver request: {} from client",
                        received.command().getCommandType());

                Command command = received.command();
                var clientAddress = received.clientAddress();


                Response response = processor.processCommand(command);

                sender.sendResponse(response, clientAddress);
                logger.info(" Sent response to client");

                if ("save".equals(command.getCommandType()) && response.isSuccess()) {
                    logger.info(" Collection saved to: {}", DATA_FILE);
                }
            }

        } catch (IOException e) {
            logger.error("❌ Network Error: {}", e.getMessage(), e);
            logger.error(" Hint: Is Port {} already in use?", PORT);

        } catch (ClassNotFoundException e) {
            logger.error("❌ Deserialization Error: {}", e.getMessage(), e);
            logger.error("Hint: Are Command/Response classes Serializable?");

        } catch (InterruptedException e) {
            logger.warn(" Server interrupted");
            Thread.currentThread().interrupt();

        } finally {
            logger.info(" Saving collection before shutdown...");

            if (collectionManager != null) {
                collectionManager.saveCollection(DATA_FILE);
            }

            if (receiver != null) {
                try {
                    receiver.close();
                } catch (IOException e) {
                    logger.warn(" Error closing receiver: {}", e.getMessage());
                }
            }

            logger.info(" Server terminated. Goodbye!");
        }
    }
}
