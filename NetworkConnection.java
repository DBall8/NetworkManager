package networkManager;

import networkManager.callback.ByteReceivedCallback;

public interface NetworkConnection {

    public void sendMessage(byte[] bytes);
    public void setOnByteReceivedCallback(ByteReceivedCallback callback);
}
