package uk.me.drdools.contact;

import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.util.Date;
import org.json.JSONObject;
import org.json.JSONTokener;

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

    public static ContactMessage fromBytes(byte[] buff, int length, InetAddress addr) throws Exception
    {
        JSONTokener tokener = new JSONTokener(new String(buff, 0, length));
        JSONObject root = new JSONObject(tokener);


        // get message type
        MESSAGE_TYPE type = MESSAGE_TYPE.valueOf(root.getString("type"));

        switch(type)
        {
            case ADVERTISE:
                return AdvertiseContactMessage.fromJson(root, addr);

            case ENUMERATE:
                return new EnumerateContactMessage();

            case SEARCH:
                return SearchContactMessage.fromBytes(root, addr);

            default:
                throw new Exception("Unknown ContactMessage Type");
        }
    }

    public abstract int getBytes(ByteBuffer buff);


    public abstract void onReceive(ContactEngine engine);


    public Date getTimestamp()
    {
        return this.timestamp;
    }


    public MESSAGE_TYPE getType()
    {
        return mType;
    }


    @Override
    public String toString()
    {
        return "ContactMessage[mType=" + mType + ']';
    }

}
