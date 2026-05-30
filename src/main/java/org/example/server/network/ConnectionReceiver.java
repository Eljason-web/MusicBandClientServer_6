package org.example.server.network;

import org.example.common.command.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;

public class ConnectionReceiver {

    private  static final Logger logger = LoggerFactory.getLogger(ConnectionReceiver.class);

    private final DatagramChannel channel;

    public ConnectionReceiver(int port) throws IOException {
        this.channel = DatagramChannel.open();
        this.channel.configureBlocking(false);
        this.channel.bind(new InetSocketAddress(port));

        logger.info(" Connection Receiver listening on port {}", port);

    }

    public ReceivedCommand receiveCommand() throws IOException, ClassNotFoundException{
        ByteBuffer buffer = ByteBuffer.allocate(65535);

        SocketAddress clientAddress = channel.receive(buffer);

        if(clientAddress == null){
            return null;
        }

        buffer.flip();

        byte[] data = new byte[buffer.remaining()];
        buffer.get(data);

        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(data);
        ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream);
        Command command = (Command) objectInputStream.readObject();

        logger.info(" Received command: {}", command.getCommandType());

        return  new ReceivedCommand(command, clientAddress);
    }

    public void close() throws IOException {
        channel.close();
        System.out.println(" Connection Receiver closed");
    }

    public DatagramChannel getChannel(){
        return channel;
    }

    public record ReceivedCommand(Command command, SocketAddress clientAddress) {
    }

}
