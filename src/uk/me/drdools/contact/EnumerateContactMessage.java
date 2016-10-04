package uk.me.drdools.contact;

import java.nio.ByteBuffer;
import org.json.JSONObject;


/**
 *
 * @author dools
 */
public class EnumerateContactMessage extends ContactMessage
{
    protected EnumerateContactMessage()
    {
        super(ContactMessage.MESSAGE_TYPE.ENUMERATE);
    }

    @Override
    public int getBytes(ByteBuffer buff)
    {
        JSONObject root2 = new JSONObject(this);

        ByteBufferWriter writer = new ByteBufferWriter(buff);
        root2.write(writer);
        return writer.getSize();
    }

    @Override
    public void onReceive(ContactEngine engine)
    {
        System.out.println("*** Rx: Enumerate request... sending adverts");

        try
        {
            engine.sendAdverts();
        }
        catch(Exception e)
        {}
    }

    @Override
    public String toString()
    {
        return "EnumerateContactMessage[]";
    }
}
