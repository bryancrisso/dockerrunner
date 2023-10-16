public class Message
{
    private String id;
    private String testCode;
    private String scriptCode;
    private String testData;
    private String output;
    private MessageStatus status;
    private String errorMessage;

    public Message(String _id, String _testCode, String _scriptCode, String _testData)
    {
        id = _id;
        testCode = _testCode;
        scriptCode = _scriptCode;
        testData = _testData;
        status = MessageStatus.STOPPED;
    }

    public void start()
    {
        status = MessageStatus.PROCESSING;
    }

    public void end(String _output)
    {
        output = _output;
        status = MessageStatus.COMPLETE;
    }

    public void error(String _message)
    {
        status = MessageStatus.ERROR;
        errorMessage = _message;
    }

    public String result()
    {
        return status.toString() + " " + (status == MessageStatus.COMPLETE ? output : errorMessage);
    }

    public String getID()
    {
        return id;
    }

    public MessageStatus getStatus()
    {
        return status;
    }

    public String getData()
    {
        return testData;
    }

    public String getTestCode()
    {
        return testCode;
    }

    public String getScriptCode()
    {
        return scriptCode;
    }
}