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

            if (finalResponse != null && finalResponse.getMessage() != null && finalResponse.getMessage().startsWith("CHUNK:")) {
                StringBuilder fullMessage = new StringBuilder();
                int expectedChunks;
                int receivedChunksCount = 0;
                boolean isSuccess = true;
                String[] chunkBuffer;

                String message1 = finalResponse.getMessage();
                int pipeIndex1 = message1.indexOf('|');
                String header1 = message1.substring(0, pipeIndex1);
                String payload1 = message1.substring(pipeIndex1 + 1);

                String[] parts1 = header1.split(":")[1].split("/");
                int currentChunkNum1 = Integer.parseInt(parts1[0]);

                expectedChunks = Integer.parseInt(parts1[1]);
                chunkBuffer = new String[expectedChunks];

                int index1 = currentChunkNum1 - 1;
                chunkBuffer[index1] = payload1;
                receivedChunksCount++;
                if (currentChunkNum1 == 1) {
                    isSuccess = finalResponse.isSuccess();
                }

                if (expectedChunks > 1) {
                    channel.socket().setSoTimeout(60000);

                    while (receivedChunksCount < expectedChunks) {
                        byte[] chunkBufferArray = new byte[MAX_BUFFER_SIZE];
                        java.net.DatagramPacket chunkPacket = new java.net.DatagramPacket(chunkBufferArray, chunkBufferArray.length);

                        try {
                            channel.socket().receive(chunkPacket);
                        } catch (SocketTimeoutException e) {
                            drainBuffer();
                        return new Response(false, " Error: Server timeout during chunk reception");
                        }

                        byte[] chunkData = new byte[chunkPacket.getLength()];
                        System.arraycopy(chunkPacket.getData(), chunkPacket.getOffset(), chunkData, 0, chunkPacket.getLength());

                        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(chunkData);
                        ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream);
                        Response chunkResponse = (Response) objectInputStream.readObject();

                        String message = chunkResponse.getMessage();
                        if (message != null && message.startsWith("CHUNK:")) {
                            int pipeIndex = message.indexOf('|');
                            String header = message.substring(0, pipeIndex);
                            String payload = message.substring(pipeIndex + 1);

                            String[] parts = header.split(":")[1].split("/");
                            int currentChunkNum = Integer.parseInt(parts[0]);

                            int index = currentChunkNum - 1;
                            if (index >= 0 && index < expectedChunks && chunkBuffer[index] == null) {
                                chunkBuffer[index] = payload;
                                receivedChunksCount++;
                            }
                        } else {
                            fullMessage.append(message != null ? message : "");
                            break;
                        }
                    }
                }

                for (String chunk : chunkBuffer) {
                    if (chunk != null) {
                        fullMessage.append(chunk);
                    }
                }
                return new Response(isSuccess, fullMessage.toString());
            }
            return finalResponse;
        } catch (IOException | ClassNotFoundException e) {
            return new Response(false, " Network error: " + e.getMessage());
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
