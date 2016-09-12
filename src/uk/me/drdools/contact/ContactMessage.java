package uk.me.drdools.contact;

import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.util.Date;

/**
 *
 * @author dools
 */
public abstract class ContactMessage
{
    public enum MESSAGE_TYPE { ADVERTISE, SEARCH, ENUMERATE };

    private final MESSAGE_TYPE mType;
    
    private final Date timestamp;

    protected ContactMessage(MESSAGE_TYPE mType)
    {
        this.mType = mType;
        this.timestamp = new Date();
    }

    public static ContactMessage fromBytes(ByteBuffer buff, InetAddress addr) throws Exception
    {
        // message type
        int tmp = buff.getInt();
        MESSAGE_TYPE type = MESSAGE_TYPE.values()[tmp];

        switch(type)
        {
            case ADVERTISE:
                return AdvertiseContactMessage.fromBytes(buff, addr);

            case ENUMERATE:
                return EnumerateContactMessage.fromBytes();

            case SEARCH:
                return SearchContactMessage.fromBytes(buff, addr);

            default:
                throw new Exception("Unknown ContactMessage Type");
        }
    }

    public abstract void getBytes(ByteBuffer buff);
    

    public abstract void onReceive(ContactEngine engine);
    
    
    public Date getTimestamp()
    {
        return this.timestamp;
    }
    

    public MESSAGE_TYPE getmType()
    {
        return mType;
    }
    

    @Override
    public String toString()
    {
        return "ContactMessage[mType=" + mType + ']';
    }

}
