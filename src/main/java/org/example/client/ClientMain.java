package org.example.client;

import org.example.common.Command;
import org.example.common.Response;

import java.io.IOException;

public class ClientMain {

    private static final String SERVER_HOST = "localhost";
    private static final int SERVER_PORT = 12345;

    public static void main(String[] args) {
        System.out.println(" Music Band Client Starting...");
        System.out.println("Server: " + SERVER_HOST + ":" + SERVER_PORT);
        System.out.println();

        ClientNetwork network = null;
        QueryReader queryReader = null;
        ResponseHandler responseHandler = null;

        try{
            network = new ClientNetwork(SERVER_HOST, SERVER_PORT);
            queryReader = new QueryReader();
            responseHandler = new ResponseHandler();

            System.out.println(" Connected to server!");
            System.out.println("Type 'help' for commands. Type 'exit' to quit.");
            System.out.println();

            while (true){
                Command command= queryReader.readCommand();

                if (command == null) {
                    continue;
                }

                if ("exit".equals(command.getCommandType())) {
                    System.out.println(" Goodbye!");
                    break;
                }

                Response response = network.sendCommand(command);
                responseHandler.handleResponse(response);
            }

        } catch (IOException e) {
            System.err.println(" Client Error: " + e.getMessage());
            System.err.println("Hint: Is the server running on " + SERVER_HOST + ":" + SERVER_PORT + "?");
        } finally {
            try {
                if (network != null) network.close();
                if (queryReader != null) queryReader.close();
            }catch (IOException e) {

            }
        }
    }
}
