package networkManager.protocols;

import networkManager.Buffer;

import java.util.HashMap;

public class BasicServerProtocol implements Protocol{

    private static final byte END_MSG = (byte)0xff;

    private int maxMessageLength;

    private HashMap<String, Buffer> buffers = new HashMap<>();
    private HashMap<String, byte[]> latestMessages = new HashMap<>();

    public BasicServerProtocol(int maxMessageLength)
    {
        this.maxMessageLength = maxMessageLength;
    }

    @Override
    public void handleByteReceived(String senderIp, byte receivedByte)
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

        if(receivedByte == END_MSG || buffer.isFull())
        {
            // End of message, copy over buffer to newest message
            byte[] newestMessage = new byte[buffer.getLevel()];
            for(int i=0; i<buffer.getLevel(); i++)
            {
                newestMessage[i] = buffer.get(i);
            }
            buffer.reset();

            if(latestMessages.containsKey(senderIp))
            {
                latestMessages.remove(senderIp);
            }
            latestMessages.put(senderIp, newestMessage);
        }
        else {
            buffer.put(receivedByte);
        }
    }

    @Override
    public byte[] prepareMessage(byte[] message) {
        byte[] preparedMessage = new byte[message.length + 1];
        for(int i=0; i< message.length; i++)
        {
            preparedMessage[i] = message[i];
        }
        preparedMessage[message.length] = END_MSG;
        return preparedMessage;
    }
}
