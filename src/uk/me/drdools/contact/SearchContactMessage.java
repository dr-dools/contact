package uk.me.drdools.contact;

import java.net.InetAddress;
import java.nio.ByteBuffer;

/**
 *
 * @author dools
 */
public class SearchContactMessage extends ContactMessage
{
    private final ContactEntityID eid;

    public SearchContactMessage(ContactEntityID entityID)
    {
        super(MESSAGE_TYPE.SEARCH);
        this.eid = entityID;
    }

    public static SearchContactMessage fromBytes(ByteBuffer buff, InetAddress addr) throws Exception
    {
        // EID
        int tmp = buff.getInt();
        byte[] bytes = new byte[tmp];
        buff.get(bytes, 0, tmp);

        ContactEntityID eid = new ContactEntityID(new String(bytes));

        return new SearchContactMessage(eid);
    }

    @Override
    public void getBytes(ByteBuffer buff)
    {
        // message type
        buff.putInt(this.getmType().ordinal());

        // target EID
        String tmp = this.eid.toString();
        buff.putInt(tmp.length());
        buff.put(tmp.getBytes());
    }


    @Override
    public void onReceive(ContactEngine engine)
    {
        System.out.println("*** Rx: Search request (eid="+eid+")");
        try {
            engine.sendAdvert(this.eid);
        } catch (Exception ex) {

        }
    }

    public ContactEntityID getEntityID()
    {
        return this.eid;
    }

    @Override
    public String toString()
    {
        return "SearchContactMessage[entityID=" + eid + ']';
    }

}
