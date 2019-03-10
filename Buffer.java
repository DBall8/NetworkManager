package networkManager;

public class Buffer {

    private byte[] buffer;
    private int size;
    private int currentIndex;

    public Buffer(int size)
    {
        this.size = size;
        this.currentIndex = 0;
        buffer = new byte[size];
    }

    public boolean put(byte newByte)
    {
        if(currentIndex >= size)
        {
            // buffer full
            return false;
        }

        buffer[currentIndex] = newByte;
        currentIndex++;
        return true;
    }

    public byte get(int index)
    {
        if(index >= size) return 0;
        return buffer[index];
    }

    public void reset()
    {
        currentIndex = 0;
    }

    public int getLevel()
    {
        return currentIndex;
    }

    public boolean isFull()
    {
        return currentIndex >= size;
    }
}
