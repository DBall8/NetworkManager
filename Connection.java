package networkManager;

import networkManager.protocols.Protocol;

import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;

public class Connection {

    static final boolean DEBUG = true;

    private String ip;
    private int port;
    private Socket socket;
    private Thread listener;

    private OutputStream output; // for writing
    private InputStream input; // for reading

    private Protocol protocol;

    boolean closed = false;

    public Connection(String ip, int port, Protocol protocol)
    {
        this.protocol = protocol;
        this.ip = ip;
        this.port = port;
    }

    Connection(Socket socket, Protocol protocol)
    {
        this.socket = socket;
        this.ip = socket.getInetAddress().getHostAddress();
        this.protocol = protocol;

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
                int receievedByte;
                try {
                    while ((receievedByte = input.read()) != -1) {
                        protocol.handleByteReceived(ip, (byte)receievedByte);
                        if(DEBUG) System.out.println(receievedByte);
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
}
