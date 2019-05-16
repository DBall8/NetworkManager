package networkManager.callback;

public abstract class ByteReceivedCallback {
    public abstract void handleByteReceived(String senderIp, byte byteReceived);
}
