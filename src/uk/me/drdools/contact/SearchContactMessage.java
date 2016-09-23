package uk.me.drdools.contact;

import java.net.InetAddress;
import java.nio.ByteBuffer;
import org.json.JSONObject;

/**
 *
 * @author dools
 */
public class SearchContactMessage extends ContactMessage
{
    private final String eid;

    public SearchContactMessage(String entityID)
    {
        super(MESSAGE_TYPE.SEARCH);
        this.eid = entityID;
    }

    public static SearchContactMessage fromBytes(JSONObject root, InetAddress addr) throws Exception
    {
        // EID
        String eid = root.getString("entityID");

        return new SearchContactMessage(eid);
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
        System.out.println("*** Rx: Search request (eid="+eid+")");
        try {
            engine.sendAdvert(this.eid);
        } catch (Exception ex) {

        }
    }

    public String getEntityID()
    {
        return this.eid;
    }

    @Override
    public String toString()
    {
        return "SearchContactMessage[entityID=" + eid + ']';
    }

}
