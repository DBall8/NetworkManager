package networkManager;

import networkManager.callback.Callback;
import networkManager.protocols.Protocol;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {

    private static final boolean DEBUG = false;

    private ServerSocket serverSocket;
    private Thread connectionListener;

    private Connection connections[];
    private int maxConnections;

    private Protocol protocol;
    private Callback<Connection> newConnectionCallback = null;

    public Server(int port, int maxConnections, Protocol protocol)
    {
        this.protocol = protocol;
        try {
            serverSocket = new ServerSocket(port);
            connections = new Connection[maxConnections];
            this.maxConnections = maxConnections;
        } catch (IOException e) {
            System.err.println("Could not create server:");
            e.printStackTrace();
        }
    }

    public void start()
    {
        connectionListener = new Thread(new Runnable() {
            @Override
            public void run() {
                System.out.println("Server Listening on" + serverSocket.getInetAddress() + ":" + serverSocket.getLocalPort());
                while(true)
                {
                    try {
                        Socket socket = serverSocket.accept();
                        Connection newConnection = new Connection(socket, protocol);
                        addConnection(newConnection);
                        if(newConnectionCallback != null) newConnectionCallback.run(newConnection);
                    } catch (IOException e) {
                        if(connectionListener.isInterrupted())
                        {
                            if(DEBUG) System.out.println("CLOSING LISTENER");
                            return;
                        }
                        e.printStackTrace();
                    }
                }
            }
        });
        connectionListener.setDaemon(true);
        connectionListener.start();
    }

    public void setNewConnectionCallback(Callback<Connection> connectionCallback)
    {
        this.newConnectionCallback = connectionCallback;
    }

    public void broadcast(byte[] message)
    {
        for(int i=0; i<maxConnections; i++)
        {
            Connection connection = connections[i];
            if(connection != null && !connection.closed)
            {
                connection.sendMessage(message);
            }
        }
    }

    private void addConnection(Connection newConnection)
    {
        for(int i=0; i<maxConnections; i++)
        {
            Connection connection = connections[i];
            if(connection == null || connection.closed)
            {
                connections[i] = newConnection;
                return;
            }
        }
    }

    public int getMaxConnections() { return maxConnections; }

    public void close()
    {
        connectionListener.interrupt();
        for(int i=0; i<maxConnections; i++)
        {
            Connection connection = connections[i];
            if(connection != null && !connection.closed)
            {
                connection.close();
                return;
            }
        }

        try {
            serverSocket.close();
        } catch (IOException e) {
            System.err.println("Failed to close socket");
            e.printStackTrace();
        }

        if(DEBUG) System.out.println("CONNECTION CLOSED");
    }
}
