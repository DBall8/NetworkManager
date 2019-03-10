package networkManager.protocols;

public interface Protocol {

    void handleByteReceived(String senderIp, byte receivedByte);
}
