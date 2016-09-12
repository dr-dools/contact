package uk.me.drdools.contact;

import java.nio.ByteBuffer;


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

    public static EnumerateContactMessage fromBytes() throws Exception
    {
        return new EnumerateContactMessage();
    }

    @Override
    public void getBytes(ByteBuffer buff)
    {
        // message type
        buff.putInt(this.getmType().ordinal());
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
        {

        }
    }

    @Override
    public String toString()
    {
        return "EnumerateContactMessage[]";
    }
}
