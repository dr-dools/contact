package uk.me.drdools.contact;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;

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

    public static AdvertiseContactMessage fromBytes(ByteBuffer buff, InetAddress addr) throws Exception
    {
        // advertised port
        int port = buff.getInt();
        InetSocketAddress sAddr = new InetSocketAddress(addr, port);

        // EID
        int tmp = buff.getInt();
        byte[] bytes = new byte[tmp];
        buff.get(bytes, 0, tmp);

        ContactEntityID eid = new ContactEntityID(new String(bytes));

        // fname
        tmp = buff.getInt();
        bytes = new byte[tmp];
        buff.get(bytes, 0, tmp);
        String fname = new String(bytes);

        buff.clear();

        ContactEntity ce = new ContactEntity(eid, sAddr, fname);

        return new AdvertiseContactMessage(ce);
    }

    @Override
    public void getBytes(ByteBuffer buff)
    {
        // message type
        buff.putInt(this.getmType().ordinal());

        // advertised port
        buff.putInt(this.entity.getAddress().getPort());

        // advertised EID
        String tmp = this.entity.getEid().toString();
        buff.putInt(tmp.length());
        buff.put(tmp.getBytes());

        // advertised fname
        tmp = this.entity.getfName();
        buff.putInt(tmp.length());
        buff.put(tmp.getBytes());
    }


    @Override
    public void onReceive(ContactEngine engine)
    {
        engine.contactEntityFound(entity);
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
