import java.net.*;
import java.nio.file.*;
import java.io.*;

class Server
{
    private ServerSocket serverSocket;
    
    private String hostIP = "10.249.62.183";
    
    //move to thread
    private Socket clientSocket;
    private PrintWriter out;
    private BufferedReader in;
    private String code = "1234";
    private String scriptPath = "./scripts/script.py";
    private String testerPath = "./scripts/tester.py";
    private String testDataPath = "./scripts/data.txt";

    public static void main(String args[]) throws IOException
    {
        Server server = new Server();
        try
        {
            server.startServer(6666);
            server.start(new Message("1234", "", "", ""));
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

    public void start(Message m) throws IOException
    {
        Shell.command("docker run -d -e HOST_IP=\""+hostIP+"\" -e VERIF_CODE=\""+ m.getID() +"\" --name " + m.getID() + " docker-runner",
            System.getProperty("user.dir"));

        String recvdCode = "";

        while (recvdCode != code)
        {
            //wait for client connection
            clientSocket = serverSocket.accept();
            System.out.println("Waiting for connection");
            System.out.println("Client " + clientSocket.getRemoteSocketAddress() + " connected");

            //establish input and output streams
            out = new PrintWriter(clientSocket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

            //receive client code
            System.out.println("Waiting for client verification code");
            recvdCode = in.readLine();
            System.out.println("Received verification code " + recvdCode);

            //handle if received code is invalid
            if (recvdCode != code)
            {
                out.println("invalid code");
                clientSocket.close();
                System.out.println("Invalid verification code");
            }
            
        }
        System.out.println("Valid verification code");
        out.println("session valid");

        handleFileSend(testerPath);
        handleFileSend(scriptPath);
        handleFileSend(testDataPath);

        String output = waitForOutput();
        System.out.println(output);

        Shell.command("docker stop dr-1", System.getProperty("user.dir"));
        Shell.command("docker rm dr-1", System.getProperty("user.dir"));
        stop();
    }

    private String waitForOutput()
    {
        StringBuilder sb = new StringBuilder();
        if(waitForString("completed"))
        {
            try
            {
                String output = in.readLine();
                while (!output.equals("output transfer complete"))
                {
                    sb.append(output);
                    sb.append(System.lineSeparator());
                    output = in.readLine();
                }
            }
            catch (IOException e)
            {
                System.out.println(e);
            }
        }
        return sb.toString();
    }

    private boolean waitForString(String expected)
    {
        boolean result = false;
        try
        {
            String actual = in.readLine();
            if (actual.equals(expected))
            {
                result = true;
            }
        }
        catch (IOException e)
        {
            System.out.println("An error ocurred");
            System.out.println(e);
        }
        return result;
    }

    private void handleFileSend(String path)
    {
        String script = readFileContents(path);

        Path p = Paths.get(path);

        out.println("sending " + p.getFileName());

        waitForString("ready");

        out.println(script);
        if(!waitForString("received"))
        {
            try
            {
                stop();
                System.out.println("An error in the client-server handshake has ocurred "+
                "and the server has been terminated");
            }
            catch(IOException e)
            {
                System.out.println(e);
            }
        }
    }

    private String readFileContents(String path)
    {
        StringBuilder sb = new StringBuilder();
        //try-with means buffered reader br autocloses when complete
        try (BufferedReader br = new BufferedReader(new FileReader(path)))
        {
            String line = br.readLine();
            while (line != null)
            {
                sb.append(line);
                sb.append(System.lineSeparator());
                line = br.readLine();
            }
        }
        catch(FileNotFoundException e)
        {
            System.out.println("Referenced script file not found");
            System.out.println(e);
        }
        catch(IOException e)
        {
            System.out.println(e);
        }
        return sb.toString();
    }

    public void stop() throws IOException
    {
        in.close();
        out.close();
        clientSocket.close();
        serverSocket.close();
    }
}