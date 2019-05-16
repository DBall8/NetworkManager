package networkManager.protocols;

public class MessageConversionError extends Exception{
    protected String errorMessage;
    protected byte[] bytes;

    public MessageConversionError(String errorMessage)
    {
        this.errorMessage = errorMessage;
    }

    public MessageConversionError(String errorMessage, byte[] bytes)
    {
        this.errorMessage = errorMessage;
        this.bytes = bytes;
    }

    public void printError()
    {
        System.err.println(errorMessage);
        if(bytes.length > 0)
        {
            System.err.format("Message:\n");
            for(byte b: bytes)
            {
                System.err.format("\t%x\n", b);
            }
        }
    }
}
