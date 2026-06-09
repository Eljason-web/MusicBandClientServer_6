package org.example.client;

import org.example.client.network.ClientNetwork;
import org.example.client.ui.QueryReader;
import org.example.client.ui.ResponseHandler; 
import org.example.common.command.Command;
import org.example.common.command.Response;

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
        ResponseHandler responseHandler;



        try {
            network = new ClientNetwork(SERVER_HOST, SERVER_PORT);
            queryReader = new QueryReader();
            responseHandler = new ResponseHandler();

            System.out.println(" Connected to server!");
            System.out.println("Type 'help' for commands. Type 'exit' to quit.");
            System.out.println();

            while (true){
                Command command= queryReader.readCommand();
                if (command == null) continue;
                if ("exit".equals(command.getCommandType()))
                    break;
                Response response = network.sendCommand(command);
                responseHandler.handleResponse(response);
            }

        } catch (IOException mainError) {
            System.err.println(" Client Error: " + mainError.getMessage());
            System.err.println("Hint: Is the server running on " + SERVER_HOST + ":" + SERVER_PORT + "?");
        } finally {
            try {
                if (network != null) network.close();
            }catch (IOException networkError) {
                System.err.println("Error closing network: " + networkError.getMessage());
            }
            if (queryReader != null) queryReader.close();
        }
    }
}
