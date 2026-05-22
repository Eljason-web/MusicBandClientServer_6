package org.example.server;

import org.example.common.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;

public class ResponseSender {
    private static final Logger logger = LoggerFactory.getLogger(ResponseSender.class);
    private final DatagramChannel channel;

    public ResponseSender(DatagramChannel channel) {
        this.channel = channel;
    }

    public void sendResponse(Response response, SocketAddress clientAddress) throws IOException {
        logger.debug(" SenderResponse() called");
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
        objectOutputStream.writeObject(response);
        objectOutputStream.flush();

        byte[] data = byteArrayOutputStream.toByteArray();
        ByteBuffer buffer = ByteBuffer.wrap(data);

        channel.send(buffer, clientAddress);
        logger.debug(" channel.send() completed to {}", clientAddress);

        logger.info(" Sent response to client");
    }

    public void close() throws IOException {
        System.out.println(" Response Sender closed");
    }
}
