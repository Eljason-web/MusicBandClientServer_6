package org.example.server.handler;

import org.example.common.command.Command;
import org.example.common.command.Response;
import org.example.server.database.UserDAO;
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

        String login = command.getLogin();
        String password = command.getPassword();

        if (login == null || password == null || login.isEmpty() || password.isEmpty()) {
            return new Response(false, " Authentication failed: Login and password are required.");
        }

        if (!UserDAO.authenticateUser(login, password)) {
            return new Response(false, " Authentication failed: Invalid login or password.");
        }

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
