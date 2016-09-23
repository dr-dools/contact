package uk.me.drdools.contact;

import java.io.IOException;
import java.io.Writer;
import java.nio.ByteBuffer;

/**
 *
 * @author dools
 */
public class ByteBufferWriter extends Writer
{
    private final ByteBuffer buff;

    private int size = 0;

    public ByteBufferWriter(ByteBuffer buff)
    {
        super();
        this.buff = buff;
    }

    @Override
    public void write(char[] cbuf, int off, int len) throws IOException
    {
        buff.put(new String(cbuf).getBytes(), off, len);
        size += len;
    }

    @Override
    public void flush() throws IOException
    {
        // ...
    }

    @Override
    public void close() throws IOException
    {
        // ...
    }

    public int getSize()
    {
        return size;
    }

}
