package org.example.client;

import org.example.client.network.ClientNetwork;
import org.example.client.ui.QueryReader;
import org.example.client.ui.ResponseHandler; 
import org.example.common.command.Command;
import org.example.common.command.Response;

import java.io.IOException;
import java.util.Scanner;

public class ClientMain {

    private static final String SERVER_HOST = "localhost";
    private static final int SERVER_PORT = 12345;

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        boolean isAuthenticated = false;
        String login = "";
        String password = "";

        System.out.println(" Music Band Client Starting...");
        System.out.println("Server: " + SERVER_HOST + ":" + SERVER_PORT);
        System.out.println();


        ClientNetwork network = null;
        QueryReader queryReader = null;

        while (!isAuthenticated) {
            System.out.print("\nEnter your login: ");
            login = scanner.nextLine().trim();

            System.out.print("Enter your password: ");
            password = scanner.nextLine().trim();

            try {
                network = new ClientNetwork(SERVER_HOST, SERVER_PORT);

                Command loginCommand = new Command("help");
                loginCommand.setLogin(login);
                loginCommand.setPassword(password);

                Response authenticResponse = network.sendCommand(loginCommand);

                if (authenticResponse.isSuccess()) {
                    isAuthenticated = true;
                    System.out.println("✅ Connected to Server! Welcome, " + login + ".");

                } else {
                    System.out.println("❌ " + authenticResponse.getMessage());
                    System.out.println("Please try again.");
                    network.close();
                    network = null;
                }
            } catch (IOException e) {
                System.err.println("❌ Connection failed: " + e.getMessage());
                System.out.println("Please try again.\n");
                if (network != null) {
                    try {
                        network.close();
                    }catch (IOException exception) {
                        network = null;
                    }
                }
            }
        }

            try {
                queryReader = new QueryReader();
                ResponseHandler responseHandler = new ResponseHandler();

                System.out.println("Type 'help' for commands. Type 'exit' to quit.");
                System.out.println();

                while (true) {
                    Command command = queryReader.readCommand();
                    if (command == null) continue;
                    if ("exit".equals(command.getCommandType()))
                        break;

                    command.setLogin(login);
                    command.setPassword(password);

                    Response response = network.sendCommand(command);
                    responseHandler.handleResponse(response);
                }
            }catch (Exception mainError) {
                System.err.println(" Client Error: " + mainError.getMessage());
            } finally {
                try {
                    network.close();
                } catch (IOException networkError) {
                    System.err.println("Error closing network: " + networkError.getMessage());
                }
                if (queryReader != null) queryReader.close();
            }
        }
    }

