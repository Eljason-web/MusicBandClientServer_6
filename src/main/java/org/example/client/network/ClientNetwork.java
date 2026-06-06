package org.example.client.network;

import org.example.common.command.Command;
import org.example.common.command.Response;

import java.io.*;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketTimeoutException;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;

public class ClientNetwork {

    private final DatagramChannel channel;
    private final InetAddress serverAddress;
    private final int serverPort;
    private static final int TIMEOUT_MS = 15000;
    private static final int MAX_BUFFER_SIZE = 65535;

    public ClientNetwork(String serverHost, int serverPort) throws IOException {

        this.serverAddress = InetAddress.getByName(serverHost);
        this.serverPort = serverPort;
        this.channel = DatagramChannel.open();
        this.channel.bind(null);
        System.out.println(" Client connected to " + serverHost + ":" + serverPort);
    }

    public Response sendCommand (Command command){
        try {
            boolean isSuccess = true;
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
            objectOutputStream.writeObject(command);
            ByteBuffer buffer = ByteBuffer.wrap(byteArrayOutputStream.toByteArray());
            channel.send(buffer, new InetSocketAddress(serverAddress, serverPort));

            channel.configureBlocking(true);
            channel.socket().setSoTimeout(TIMEOUT_MS);

            StringBuilder fullMessage = new StringBuilder();
            int expectedChunks = 1;
            int receivedChunks = 0;


            while (receivedChunks < expectedChunks) {
                ByteBuffer receiveBuffer = ByteBuffer.allocate(MAX_BUFFER_SIZE);
                channel.receive(receiveBuffer);
                receiveBuffer.flip();

                byte[] data = new byte[receiveBuffer.remaining()];
                receiveBuffer.get(data);

                ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(data);
                ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream);
                Response response = (Response) objectInputStream.readObject();

                String message = response.getMessage();
                if (message != null && message.startsWith("CHUNK:")) {
                    int pipeIndex = message.indexOf('|');
                    String header = message.substring(0, pipeIndex);
                    String payload = message.substring(pipeIndex + 1);

                    String[] parts = header.split(":")[1].split("/");
                    expectedChunks = Integer.parseInt(parts[1]);
                    receivedChunks = Integer.parseInt(parts[0]);

                    fullMessage.append(payload);
                    if (receivedChunks == 1) {
                        isSuccess = response.isSuccess();
                    }
                } else {
                    fullMessage.append(message != null ? message : "");
                    receivedChunks = 1;
                    expectedChunks = 1;
                }
            }

            return new Response(isSuccess, fullMessage.toString());

        } catch (SocketTimeoutException e) {
            return new Response(false, " Error: Server timeout during chunk reception");
        } catch (IOException | ClassNotFoundException e) {
            return new Response(false, " Network error: " + e.getMessage());
        } finally {
            try {
                channel.configureBlocking(false);
            } catch (IOException ignored){}
        }
    }

    public void close() throws IOException {
        if (channel != null && channel.isOpen()) {
            channel.close();
        }
        System.out.println(" Client network closed");
    }

}
