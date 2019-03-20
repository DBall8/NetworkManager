package networkManager.protocols;

import networkManager.Buffer;

public class BasicClientProtocol implements Protocol{

    private static final byte END_MSG = (byte)0xff;

    private int maxMessageLength;

    private Buffer buffer;

    private byte[] newestMessage;

    public BasicClientProtocol(int maxMessageLength)
    {
        this.maxMessageLength = maxMessageLength;
        this.buffer = new Buffer(maxMessageLength);
    }

    @Override
    public synchronized void handleByteReceived(String senderIp, byte receivedByte)
    {
        if(receivedByte == END_MSG || buffer.isFull())
        {
            // End of message, copy over buffer to newest message
            newestMessage = new byte[buffer.getLevel()];
            for(int i=0; i<buffer.getLevel(); i++)
            {
                newestMessage[i] = buffer.get(i);
            }
            buffer.reset();
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
