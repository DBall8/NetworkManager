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

    private void printNewestMessage()
    {
        for(byte b: newestMessage)
        {
            System.out.printf("byte: %x\n", b);
        }
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
            System.out.println("MESSAGE RECEIVED:");
            printNewestMessage();
        }
        else {
            buffer.put(receivedByte);
        }
    }
}
