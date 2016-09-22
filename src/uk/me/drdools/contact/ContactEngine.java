package uk.me.drdools.contact;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Date;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


/**
 *
 * @author dools
 */
public class ContactEngine implements ContactMessageListener
{
//---------------- Constants ---------------------------------------------------

    private static final int DEFAULT_EXECUTOR_THREADSIZE = 3;

//---------------- Class Variables ---------------------------------------------


    private static ContactEngine singleton = null;


//---------------- Instance Variables ------------------------------------------


    private ContactMessageListener listener = null;

    private InetSocketAddress contactAddress = null;

    // multicast datagram channel to use
    private MulticastSocket sock = null;

    private DatagramReceiver dr = null;

    private Thread th = null;

    private final Map<ContactEntityID, ContactEntity> contactEntities = new TreeMap();

    private ExecutorService messageHandler = null;


//---------------- Constructor -------------------------------------------------

    private ContactEngine()
    {

    }


//---------------- Factory Methods ---------------------------------------------


    public static ContactEngine getSingleton()
    {
        if(singleton == null)
        {
            singleton = new ContactEngine();
        }

        return singleton;
    }

//---------------- Overrides ---------------------------------------------------


    @Override
    public String toString()
    {
        return "ContactEngine[address=]";
    }


//---------------- Methods -----------------------------------------------------

    public ContactEntity getContactEntity(ContactEntityID eid, int port1, int port2, String fname) throws Exception
    {
        InetSocketAddress addr1 = new InetSocketAddress(this.contactAddress.getAddress(), port1);
        InetSocketAddress addr2 = new InetSocketAddress(this.contactAddress.getAddress(), port2);

        ContactEntity rtn = new ContactEntity(eid, addr1, addr2, fname);
        return rtn;
    }

    public void setMessageListener(ContactMessageListener listener)
    {
        this.listener = listener;
    }


    public void start(InetSocketAddress addr) throws IOException
    {
        this.contactAddress = addr;

        /* Create socket */
        this.sock = new MulticastSocket(addr.getPort());
        this.sock.setReuseAddress(true);

        this.sock.joinGroup(addr.getAddress());


        this.dr = new DatagramReceiver(sock, this);
        th = new Thread(this.dr, "ContactEngineDatagramRx");
        th.start();
    }


    public void stop() throws Exception
    {
        this.sock.close();
        if (this.messageHandler != null)
        {
            this.messageHandler.shutdown();
        }
    }

    public boolean isListening()
    {
        return false;
    }

    public void sendEnumerate() throws Exception
    {
        EnumerateContactMessage tx = new EnumerateContactMessage();

        // get message bytes
        ByteBuffer buff = ByteBuffer.allocate(4);
        buff.order(ByteOrder.LITTLE_ENDIAN);
        tx.getBytes(buff);
        buff.flip();

        // construct and send packet
        DatagramPacket snd = new DatagramPacket(buff.array(), buff.capacity());
        snd.setSocketAddress(this.contactAddress);
        this.sock.send(snd);
    }

    public void sendSearch(ContactEntityID eid) throws Exception
    {
        SearchContactMessage tx = new SearchContactMessage(eid);

        // get message bytes
        ByteBuffer buff = ByteBuffer.allocate(512);
        buff.order(ByteOrder.LITTLE_ENDIAN);
        tx.getBytes(buff);
        buff.flip();

        // construct and send packet
        DatagramPacket snd = new DatagramPacket(buff.array(), buff.capacity());
        snd.setSocketAddress(this.contactAddress);
        this.sock.send(snd);
    }

    public void sendAdvert(ContactEntityID eid) throws Exception
    {
        synchronized(this.contactEntities)
        {
            ContactEntity entity = this.contactEntities.get(eid);
            if(entity != null)
            {
                ContactMessage tx = new AdvertiseContactMessage(entity);

                // get message bytes
                ByteBuffer buff = ByteBuffer.allocate(512);
                buff.order(ByteOrder.LITTLE_ENDIAN);
                tx.getBytes(buff);
                buff.flip();

                // construct and send packet
                DatagramPacket snd = new DatagramPacket(buff.array(), buff.capacity());
                snd.setSocketAddress(this.contactAddress);
                this.sock.send(snd);
            }
        }

    }

    public void sendAdverts() throws Exception
    {
        ContactEntity[] entities = this.getAdvertisedContactEntities();

        for(ContactEntity entity: entities)
        {
            ContactMessage tx = new AdvertiseContactMessage(entity);

            // get message bytes
            ByteBuffer buff = ByteBuffer.allocate(512);
            buff.order(ByteOrder.LITTLE_ENDIAN);
            tx.getBytes(buff);
            buff.flip();

            // construct and send packet
            DatagramPacket snd = new DatagramPacket(buff.array(), buff.capacity());
            snd.setSocketAddress(this.contactAddress);
            this.sock.send(snd);
        }
    }

    public void add(ContactEntity entity)
    {
        synchronized(contactEntities)
        {
            this.contactEntities.put(entity.getEid(), entity);
        }
    }

    public void remove(ContactEntityID eid)
    {
        synchronized(contactEntities)
        {
            this.contactEntities.remove(eid);
        }
    }

    public void removeAll()
    {
        synchronized(contactEntities)
        {
            this.contactEntities.clear();
        }
    }

    public ContactEntity[] getAdvertisedContactEntities()
    {
        synchronized(contactEntities)
        {
            ContactEntity[] rtn = new ContactEntity[this.contactEntities.size()];
            this.contactEntities.values().toArray(rtn);
            return rtn;
        }
    }

    public String easterEgg()
    {
        return "this is for Ari!";
    }

    public void setMessageHandler(ExecutorService messageHandler)
    {
        if(this.messageHandler != null)
        {
            this.messageHandler.shutdown();
        }

        this.messageHandler = messageHandler;
    }


    protected void execute(Runnable task)
    {
        if(this.messageHandler == null)
        {
            this.messageHandler = Executors.newFixedThreadPool(DEFAULT_EXECUTOR_THREADSIZE);
        }

        this.messageHandler.execute(task);
    }

    @Override
    public void contactEntityFound(ContactEntity entity, Date timestamp)
    {
        if(this.listener != null)
        {
            this.listener.contactEntityFound(entity, timestamp);
        }
    }
}
