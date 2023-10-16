import java.util.Hashtable;
import java.util.ArrayList;

public class MessageQueue
{
    //table of request ID : request
    Hashtable<String, Message> queue = new Hashtable<>();
    //list of ids
    ArrayList<String> ids = new ArrayList<>();

    public void addMessage(Message msg)
    {
        queue.put(msg.getID(), msg);
    }

    public void processMessage(String id)
    {
        queue.get(id).start();
    }

    public void completeMessage(String id, String output)
    {
        queue.get(id).end(output);
    }

    public void messageError(String id, String errorMsg)
    {
        queue.get(id).error(errorMsg);
    }

    public Message getMessageToProcess()
    {
        for (int i = 0; i < ids.size(); i++)
        {
            Message m = queue.get(ids.get(i));
            if (m.getStatus() == MessageStatus.STOPPED)
            {
                return m;
            }
        }
        return null;
    }

    public String getResult(String id)
    {
        Message m = queue.get(id);
        if (m.getStatus() == MessageStatus.COMPLETE || m.getStatus() == MessageStatus.ERROR)
        {
            return m.result();
        }
        else if (m.getStatus() == MessageStatus.PROCESSING)
        {
            return "Request is being processed";
        }
        else
        {
            return "Request is waiting to be processed";
        }
    }
}