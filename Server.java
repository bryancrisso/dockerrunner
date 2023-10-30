import java.net.*;
import java.io.*;

class Server
{
    private ServerSocket serverSocket;
    
    private String hostIP = "172.22.120.58";

    public static void main(String args[]) throws IOException
    {
        Server server = new Server();
        try
        {
            server.startServer(6666);
            DockerThread dthread = new DockerThread(
                new Message("dr-1", "", "", ""), 
                server.hostIP, server.serverSocket, server);
            dthread.start();
        }
        catch(IOException e)
        {
            server.stop();
            throw e;
        }
    }

    public void startServer(int port) throws IOException
    {
        serverSocket = new ServerSocket(port);
    }

    public void completeRun(String output) 
    {
        System.out.println(output);
        try
        {
            stop();
        }
        catch(IOException e)
        {
            System.err.println(e);
        }
    }

    public void stop() throws IOException
    {
        serverSocket.close();
    }
}