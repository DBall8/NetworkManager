package networkManager;

import networkManager.callback.ByteReceivedCallback;

import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;

public class Connection implements NetworkConnection{

    static final boolean DEBUG = false;

    private String ip;
    private int port;
    private Socket socket;
    private Thread listener;

    private OutputStream output; // for writing
    private InputStream input; // for reading

    private ByteReceivedCallback byteReceivedCallback;

    boolean closed = false;

    public Connection(String ip, int port)
    {
        this.ip = ip;
        this.port = port;
    }

    Connection(Socket socket)
    {
        this.socket = socket;
        this.ip = socket.getInetAddress().getHostAddress();

        try {
            output = socket.getOutputStream();
            input = socket.getInputStream();
            System.out.println("Connection with client at " + ip + " established.");

            // once streams are set up, start listening
            listen();

        } catch(IOException e){
            System.err.println("Could not create connection with client.");
            e.printStackTrace();
        }
    }

    public void connect()
    {
        try {
            this.socket = new Socket(ip, port);
            output = socket.getOutputStream();
            input = socket.getInputStream();
            System.out.println("Connection with client at " + socket.getInetAddress() + " established.");

            // once streams are set up, start listening
            listen();

        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        catch(IOException e){
            System.err.println("Could not create connection with client.");
            e.printStackTrace();
        }
    }

    private void listen()
    {
        listener = new Thread(new Runnable() {
            @Override
            public void run() {
                int receivedByte;
                try {
                    while ((receivedByte = input.read()) != -1) {
                        if (byteReceivedCallback != null)
                        {
                            byteReceivedCallback.handleByteReceived(ip, (byte)receivedByte);
                        }
                        if(DEBUG) System.out.println(receivedByte);
                    }

                    // End of stream received, connection closing
                    close();
                }
                catch (IOException e)
                {
                    if(listener.isInterrupted())
                    {
                        if(DEBUG) System.out.println("CLOSING LISTENER");
                        return;
                    }
                    System.err.println("Problem reading message from client:");
                    e.printStackTrace();
                }
            }
        });

        listener.setDaemon(true);
        listener.start();
    }

    public void sendMessage(byte[] message)
    {
        if(output != null)
        {
            try {
                output.write(message);

                if(DEBUG)
                {
                    System.out.println("SENDING");
                }
            } catch (IOException e) {
                System.err.println("Could not send byte:");
                e.printStackTrace();
            }
        }
    }

    public void close()
    {
        closed = true;
        listener.interrupt();
        try
        {
            output.flush();
            output.close();
            input.close();
            socket.close();
        } catch (IOException e) {
            System.err.println("Failed to close connection:");
            e.printStackTrace();
        }
        if(DEBUG) System.out.println("CONNECTION CLOSED");
    }

    public void setOnByteReceivedCallback(ByteReceivedCallback byteReceivedCallback) {
        this.byteReceivedCallback = byteReceivedCallback;
    }

    public String getIp(){ return ip; }
}
