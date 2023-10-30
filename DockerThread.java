import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class DockerThread extends Thread
{
    private Message message;
    private String hostIP;
    private ServerSocket serverSocket;
    private Socket clientSocket;
    private PrintWriter out;
    private BufferedReader in;

    private Server serverObj;

    public DockerThread(Message m, String ip, ServerSocket serv, Server callback)
    {
        message = m;
        hostIP = ip;
        serverSocket = serv;
        serverObj = callback;
    }

    public void run()
    {
        Shell.command("docker run -d -e HOST_IP=\""+hostIP+"\""
        +" -e VERIF_CODE=\""+ message.getID() +"\" --name " + message.getID() + " docker-runner",
            System.getProperty("user.dir"));

        String recvdCode = "";

        while (!recvdCode.equals(message.getID()))
        {
            //wait for client connection
            try
            {
                //i probably shouldn't have the client acceptance in the thread but oh well
                clientSocket = serverSocket.accept();
            }
            catch (IOException e)
            {
                System.out.println("Encountered an error");
                System.err.println(e);
                try
                {
                    close();
                }
                catch (Exception f)
                {
                    System.err.println(f);
                }
                //RETURN OUT OF THREAD
            }
            System.out.println("Waiting for connection");
            System.out.println("Client " + clientSocket.getRemoteSocketAddress() + " connected");

            //establish input and output streams
            try
            {
                out = new PrintWriter(clientSocket.getOutputStream(), true);
                in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            }
            catch (IOException e)
            {
                System.out.println("Failed to establish IO stream");
                System.err.println(e);
            }

            //receive client code
            System.out.println("Waiting for client verification code");
            try
            {
                recvdCode = in.readLine();
            }
            catch (IOException e)
            {
                System.err.println(e);
            }
            System.out.println("Received verification code " + recvdCode);

            //handle if received code is invalid
            if (!recvdCode.equals(message.getID()))
            {
                out.println("invalid code");
                try
                {
                    clientSocket.close();
                }
                catch (IOException e)
                {
                    System.err.println(e);
                }
                System.out.println("Invalid verification code");
            }
        }
        System.out.println("Valid verification code");
        out.println("session valid");

        handleFileSend(readFileContents("./scripts/script.py"), "script.py");
        handleFileSend(readFileContents("./scripts/tester.py"), "tester.py");
        handleFileSend(readFileContents("./scripts/data.txt"), "data.txt");
        
        String output = waitForOutput();

        Shell.command("docker stop " + message.getID(), System.getProperty("user.dir"));
        Shell.command("docker rm " + message.getID(), System.getProperty("user.dir"));
        serverObj.completeRun(output);
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
                System.err.println(e);
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
            System.err.println(e);
        }
        return result;
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
            System.err.println(e);
        }
        catch(IOException e)
        {
            System.err.println(e);
        }
        return sb.toString();
    }

    private void handleFileSend(String script, String filename)
    {
        out.println("sending " + filename);

        waitForString("ready");

        out.println(script);
        if(!waitForString("received"))
        {
            try
            {
                close();
                System.out.println("An error in the client-server handshake has ocurred "+
                "and the server has been terminated");
            }
            catch(IOException e)
            {
                System.err.println(e);
            }
        }
    }

    public void close() throws IOException
    {
        in.close();
        out.close();
        clientSocket.close();
    }
}
