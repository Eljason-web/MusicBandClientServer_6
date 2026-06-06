package org.example.server.network;

import org.example.common.command.Response;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.util.ArrayList;
import java.util.List;

public class ResponseSender {
    private final DatagramChannel channel;
    private static final int MAX_CHUNK_SIZE = 3000;

    public ResponseSender(DatagramChannel channel) {
        this.channel = channel;
    }

    public void sendResponse(Response response, SocketAddress clientAddress)  {
        String message = response.getMessage();

        if (message != null && message.length() > MAX_CHUNK_SIZE) {
            List<String> chunks = splitIntoChunks(message);
            for (int i = 0; i < chunks.size(); i++) {
                String header = String.format("CHUNK:%d/%d|", i + 1, chunks.size());
                Response chunkResp = new Response(response.isSuccess(), header + chunks.get(i));
                sendPacket(chunkResp, clientAddress);

                try {
                    Thread.sleep(1);
                } catch (InterruptedException ignored) {}
            }
        } else {
            sendPacket(response, clientAddress);
        }
    }

    private List<String> splitIntoChunks(String message) {
        List<String> chunks = new ArrayList<>();
        for (int i = 0; i < message.length(); i += ResponseSender.MAX_CHUNK_SIZE) {
            chunks.add(message.substring(i, Math.min(i + ResponseSender.MAX_CHUNK_SIZE, message.length())));
        }
        return chunks;
    }

    private void sendPacket(Response response, SocketAddress address) {
        try {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            ObjectOutputStream objectoutputstream = new ObjectOutputStream(byteArrayOutputStream);
            objectoutputstream.writeObject(response);
            ByteBuffer buffer = ByteBuffer.wrap(byteArrayOutputStream.toByteArray());
            channel.send(buffer, address);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
