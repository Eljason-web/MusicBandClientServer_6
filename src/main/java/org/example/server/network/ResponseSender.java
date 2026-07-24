package org.example.server.network;

import org.example.common.command.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.util.List;

public class ResponseSender {
    private static final Logger logger = LoggerFactory.getLogger(ResponseSender.class);
    private final DatagramChannel channel;

    public ResponseSender(DatagramChannel channel) {
        this.channel = channel;
    }

    public void sendResponse(Response response, SocketAddress clientAddress)  {
        try {
            List<Response> chunksToSend = response.getAdditionalChunks();

            response.setAdditionalChunks(null);

            sendPacket(response, clientAddress);
            logger.info("Sent main response (Chunk 1 of {}). Total bands: {}",
                response.getTotalChunks(),
                response.getBands() != null ? response.getBands().size() : 0);

            if (chunksToSend != null) {
                logger.info("Starting to send {} additional chunks...", chunksToSend.size());

                for (int i = 0; i < chunksToSend.size(); i++) {
                    Response chunk = chunksToSend.get(i);
                    sendPacket(chunk, clientAddress);
                    logger.info(" Sent chunk {}/{}", i + 2, response.getTotalChunks());

                    try {
                        Thread.sleep(50);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                } logger.info("Finished sending all chunks to {}", clientAddress);
            }
        } catch (Exception e){
            logger.error("Failed to send response to {}: {}", clientAddress, e.getMessage());}
        }


    private void sendPacket(Response response, SocketAddress address) {
        try {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            ObjectOutputStream objectoutputstream = new ObjectOutputStream(byteArrayOutputStream);
            objectoutputstream.writeObject(response);
            objectoutputstream.flush();

            ByteBuffer buffer = ByteBuffer.wrap(byteArrayOutputStream.toByteArray());
            channel.send(buffer, address);
        } catch (IOException e) {
            logger.error("Failed to send packet to {}: {}", address, e.getMessage());
        }
    }
}

