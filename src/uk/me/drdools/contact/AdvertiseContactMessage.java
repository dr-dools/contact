package uk.me.drdools.contact;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 *
 * @author dools
 */
public class AdvertiseContactMessage extends ContactMessage
{
    private final ContactEntity entity;

    public AdvertiseContactMessage(ContactEntity entity)
    {
        super(MESSAGE_TYPE.ADVERTISE);
        this.entity = entity;
    }

    public static AdvertiseContactMessage fromJson(JSONObject root, InetAddress addr) throws Exception
    {
        JSONObject entity = root.getJSONObject("entity");

        // EID
        String eid = entity.getString("entityID");

        // fname
        String fname = entity.getString("friendlyName");

        // create object
        ContactEntity ce = new ContactEntity(eid, fname, addr);

        // get services
        JSONObject services = entity.getJSONObject("services");
        int port;
        for(String sName : services.keySet())
        {
            port = services.getInt(sName);
            ce.setService(sName, port);
        }

        return new AdvertiseContactMessage(ce);
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
        engine.contactEntityFound(entity, super.getTimestamp());
    }

    public ContactEntity getEntity()
    {
        return entity;
    }

    @Override
    public String toString()
    {
        return "AdvertiseContactMessage[entity=" + entity + ']';
    }

}
