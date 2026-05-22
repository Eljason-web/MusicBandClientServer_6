package org.example.client;

import org.example.common.Command;
import org.example.common.Response;

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
    private static final int TIMEOUT_MS = 5000;

    public ClientNetwork(String serverHost, int serverPort) throws IOException {

        this.serverAddress = InetAddress.getByName(serverHost);
        this.serverPort = serverPort;

        this.channel = DatagramChannel.open();
        this.channel.bind(null);
        System.out.println(" Client connected to " + serverHost + ":" + serverPort);
    }

    public Response sendCommand (Command command){
        try {

            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
            objectOutputStream.writeObject(command);
            objectOutputStream.flush();

            byte[] data = byteArrayOutputStream.toByteArray();
            ByteBuffer buffer = ByteBuffer.wrap(data);

            channel.send(buffer, new InetSocketAddress(serverAddress, serverPort));

            channel.configureBlocking(true);
            channel.socket().setSoTimeout(TIMEOUT_MS);

            ByteBuffer receiveBuffer = ByteBuffer.allocate(65535);
            channel.receive(receiveBuffer);
            receiveBuffer.flip();

            byte[] responseData = new byte[receiveBuffer.remaining()];
            receiveBuffer.get(responseData);

            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(responseData);
            ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream);

            return (Response) objectInputStream.readObject();

        } catch (SocketTimeoutException e) {

            return new Response(false, " Error: Server not available. Please try again later.");
        } catch (IOException | ClassNotFoundException e) {
            return new Response(false, " Network error: " + e.getMessage());

        } finally {
            try {
                channel.configureBlocking(false);
            } catch (IOException e){

            }
        }
    }

    public void close() throws IOException{
        channel.close();
        System.out.println(" Client network closed");
    }
}
