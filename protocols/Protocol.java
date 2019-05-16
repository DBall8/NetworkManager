package networkManager.protocols;

import networkManager.Buffer;
import networkManager.Connection;
import networkManager.NetworkConnection;
import networkManager.Server;
import networkManager.callback.ByteReceivedCallback;

import java.util.HashMap;

public abstract class Protocol<T>{

    private static final byte[] DEFAULT_END_MSG = new byte[]{ (byte)0xff, (byte)0xff};

    private NetworkConnection connection;

    private HashMap<String, Buffer> buffers = new HashMap<>();
    private int maxMessageLength;
    private byte[] messageTerminator = DEFAULT_END_MSG;

    public Protocol(NetworkConnection connection, int maxMessageLength)
    {
        this.connection = connection;
        this.maxMessageLength = maxMessageLength;

        connection.setOnByteReceivedCallback(new ByteReceivedCallback() {
            @Override
            public void handleByteReceived(String senderIp, byte byteReceived) {
                onByteReceived(senderIp, byteReceived);
            }
        });
    }

    public synchronized void onByteReceived(String senderIp, byte receivedByte)
    {
        Buffer buffer;
        if(buffers.containsKey(senderIp))
        {
            buffer = buffers.get(senderIp);
        }
        else
        {
            buffer = new Buffer(maxMessageLength);
            buffers.put(senderIp, buffer);
        }

        buffer.put(receivedByte);

        if(checkForMessageTerminator(buffer) || buffer.isFull())
        {
            // End of message, copy over buffer to newest message
            int messageLength = buffer.getLevel() - messageTerminator.length;
            byte[] newestMessage = new byte[messageLength];
            for(int i=0; i<messageLength; i++)
            {
                newestMessage[i] = buffer.get(i);
            }
            buffer.reset();

            try {
                handleMessageReceived(senderIp, convertBytesToMessage(newestMessage));
            }
            catch(MessageConversionError e)
            {
                System.err.println("Error in received message:");
                e.printError();
            }
        }
    }

    private boolean checkForMessageTerminator(Buffer buffer)
    {
        int level = buffer.getLevel();
        int terminatorLength = messageTerminator.length;

        if(buffer.getLevel() < terminatorLength) return false;

        int startIndex = level-terminatorLength;
        for(int i=0; (i + startIndex)<level; i++)
        {
            if(buffer.get(i + startIndex) != messageTerminator[i])
            {
                return false;
            }
        }
        return true;
    }

    public void sendMessage(T message)
    {
        try {
            // Convert message to byte array
            byte[] messageAsBytes = convertMessageToBytes(message);

            // Copy message terminator onto the end of the array
            byte[] terminatedMessage = new byte[messageAsBytes.length + messageTerminator.length];
            for(int i=0; i<messageAsBytes.length; i++)
            {
                terminatedMessage[i] = messageAsBytes[i];
            }
            for(int i=0; i<messageTerminator.length; i++)
            {
                terminatedMessage[messageAsBytes.length + i] = messageTerminator[i];
            }

            connection.sendMessage(terminatedMessage);
        }
        catch (MessageConversionError e)
        {
            System.err.println("Sending message failed: ");
            e.printError();
        }
    }

    protected abstract byte[] convertMessageToBytes(T message) throws MessageConversionError;
    protected abstract T convertBytesToMessage(byte[] bytes) throws MessageConversionError;

    protected abstract void handleMessageReceived(String ip, T message);
}
