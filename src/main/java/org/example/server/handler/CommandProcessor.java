package org.example.server.handler;

import org.example.common.command.Command;
import org.example.common.command.Response;
import org.example.server.service.CollectionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class CommandProcessor {
    private static final Logger logger = LoggerFactory.getLogger(CommandProcessor.class);

    private final CollectionManager collectionManager;
    private final CommandRegistry commandRegistry;

    public CommandProcessor(CollectionManager collectionManager) {
        this.collectionManager = collectionManager;
        this.commandRegistry = new CommandRegistry();
    }

    public Response processCommand(Command command) {
        String commandType = command.getCommandType();
        logger.info("Processing command: {}", commandType);

        CommandRegistry.CommandHandler handler = commandRegistry.getHandler(commandType);

        if (handler == null) {
            return new Response(false, "Unknown command: '" + commandType + "'");
        }

        try {
            return handler.execute(collectionManager, command);
        } catch (Exception e) {
            logger.error("Error executing command '{}' : {}", commandType, e.getMessage(), e);
            return new Response(false, "Error: " + e.getMessage());
        }
    }
}
