package networkManager.protocols;

public interface Protocol {

    /**
     * Whenever a connection receives a byte, this method receives it for handling
     * @param senderIp  IP that sent the byte
     * @param receivedByte  the byte received
     */
    void handleByteReceived(String senderIp, byte receivedByte);

    /**
     * Before a message a sent, it is passed to this method
     * @param message The message put out for delivery
     * @return  The actual message to send
     */
    byte[] prepareMessage(byte[] message);
}
