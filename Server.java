import java.net.*;
import java.io.*;

class Server
{
    private ServerSocket serverSocket;
    private Socket clientSocket;
    private PrintWriter out;
    private BufferedReader in;
    private int code = 12345;

    public void start(int port) throws IOException
    {
        serverSocket = new ServerSocket(port);
        System.out.println("Waiting for connection");

        int recvdCode = 0;

        while (recvdCode != code)
        {
            //wait for client connection
            clientSocket = serverSocket.accept();
            System.out.println("Client " + clientSocket.getRemoteSocketAddress() + " connected");

            //establish input and output streams
            out = new PrintWriter(clientSocket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

            //receive client code
            System.out.println("Waiting for client verification code");
            recvdCode = Integer.parseInt(in.readLine());
            System.out.println("Received verification code " + recvdCode);

            //handle if received code is invalid
            if (recvdCode != code)
            {
                out.println("invalid code");
                clientSocket.close();
                System.out.println("Invalid verification code");
            }
            else
            {
                System.out.println("Valid verification code");
                out.println("session valid");
            }
        }
    }

    public void stop() throws IOException
    {
        in.close();
        out.close();
        clientSocket.close();
        serverSocket.close();
    }

    public static void main(String args[]) throws IOException
    {
        Server server = new Server();
        try
        {
            server.start(6666);
        }
        catch(IOException e)
        {
            throw e;
        }
    }
}