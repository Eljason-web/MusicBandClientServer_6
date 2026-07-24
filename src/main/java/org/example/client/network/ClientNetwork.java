package org.example.client.network;

import org.example.common.command.Command;
import org.example.common.command.Response;
import org.example.common.model.MusicBand;

import java.io.*;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketTimeoutException;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.util.List;
import java.util.ArrayList;

public class ClientNetwork {

    private final DatagramChannel channel;
    private final InetAddress serverAddress;
    private final int serverPort;
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
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
            objectOutputStream.writeObject(command);
            ByteBuffer buffer = ByteBuffer.wrap(byteArrayOutputStream.toByteArray());
            channel.send(buffer, new InetSocketAddress(serverAddress, serverPort));

            channel.socket().setSoTimeout(5000);

            int maxAttempts = 3;
            Response finalResponse = null;

            for (int attempt = 1; attempt <= maxAttempts; attempt++) {
                try {
                    byte[] bufferArray = new byte[MAX_BUFFER_SIZE];
                    java.net.DatagramPacket packet = new java.net.DatagramPacket(bufferArray, bufferArray.length);
                    channel.socket().receive(packet);

                    byte[] data = new byte[packet.getLength()];
                    System.arraycopy(packet.getData(), packet.getOffset(), data, 0, packet.getLength());

                    ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(data);
                    ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream);
                    finalResponse = (Response) objectInputStream.readObject();
                    break;

                } catch (SocketTimeoutException e) {
                    if (attempt == maxAttempts) {
                        drainBuffer();
                        return new Response(false, "  Server is unavailable. Try again later.");
                    }
                }
            }

            if (finalResponse != null) {
                int totalChunks = finalResponse.getTotalChunks();

                if (totalChunks > 1) {
                    List<MusicBand> allBands = new ArrayList<>();
                    if (finalResponse.getBands() != null) {
                        allBands.addAll(finalResponse.getBands());
                    }

                    channel.socket().setSoTimeout(30000);
                    int receivedChunks = 1;

                    while (receivedChunks < totalChunks) {
                        byte[] chunkBufferArray = new byte[MAX_BUFFER_SIZE];
                        java.net.DatagramPacket chunkPacket = new java.net.DatagramPacket(chunkBufferArray, chunkBufferArray.length);

                        try {
                            channel.socket().receive(chunkPacket);
                        } catch (SocketTimeoutException e) {
                            drainBuffer();
                            return new Response(false, "Server timeout during chunk reception");
                        }

                        byte[] chunkData = new byte[chunkPacket.getLength()];
                        System.arraycopy(chunkPacket.getData(), chunkPacket.getOffset(), chunkData, 0, chunkPacket.getLength());

                        try(ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(chunkData);
                            ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream)){

                            Response chunkResponse = (Response) objectInputStream.readObject();

                            if (chunkResponse.getBands() != null) {
                            allBands.addAll(chunkResponse.getBands());
                            }
                            receivedChunks++;
                    } catch (ClassNotFoundException e) {
                            return new Response(false, "Class not found during chunk reception: " + e.getMessage());
                    } catch (IOException e) {
                            return new Response(false, "IO error during chunk deserialization: " + e.getMessage());
                        }
                    }

                    finalResponse.setBand(allBands);
                    finalResponse.setMessage(finalResponse.getMessage() + "(Loaded all " + totalChunks + " chunks)");
                }
            }
            return finalResponse;

            } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    private  void drainBuffer() {
        try {
            channel.socket().setSoTimeout(500);
            byte[] drainArray = new byte[MAX_BUFFER_SIZE];
            java.net.DatagramPacket drainPacket = new java.net.DatagramPacket(drainArray, drainArray.length);
            while (true) {
                channel.socket().receive(drainPacket);
            }
        } catch (IOException ignored) {
        } finally {
            try {
                channel.socket().setSoTimeout(5000);
            } catch (Exception ignored) {
            }
        }
    }

    public void close() throws IOException {
        if (channel != null && channel.isOpen()) {
            channel.close();
        }
        System.out.println(" Client network closed");
    }
}
