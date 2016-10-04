package uk.me.drdools.contact;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.net.NetworkInterface;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Date;
import java.util.Map;
import java.util.Random;
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

    private final Map<String, ContactEntity> contactEntities = new TreeMap();

    private ExecutorService messageHandler = null;

    private final Random rand = new Random();

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

    public void setMessageListener(ContactMessageListener listener)
    {
        this.listener = listener;
    }

    /*
    public void start(InetSocketAddress addr) throws IOException
    {
        start(null, addr);
    }*/

    public void start(NetworkInterface ni, InetSocketAddress addr) throws IOException
    {
        this.contactAddress = addr;

        /* Create socket */
        this.sock = new MulticastSocket(addr.getPort());
        this.sock.setReuseAddress(true);
        if(ni != null)
        {
            this.sock.setNetworkInterface(ni);
        }
        this.sock.setTimeToLive(10);

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
        ByteBuffer buff = ByteBuffer.allocate(128);
        buff.order(ByteOrder.LITTLE_ENDIAN);
        int size = tx.getBytes(buff);
        buff.flip();

        // construct and send packet
        DatagramPacket snd = new DatagramPacket(buff.array(), size);
        snd.setSocketAddress(this.contactAddress);

        this.sock.send(snd);
    }

    public void sendSearch(String eid) throws Exception
    {
        SearchContactMessage tx = new SearchContactMessage(eid);

        // get message bytes
        ByteBuffer buff = ByteBuffer.allocate(256);

        int size = tx.getBytes(buff);
        buff.flip();

        // construct and send packet
        DatagramPacket snd = new DatagramPacket(buff.array(), size);
        snd.setSocketAddress(this.contactAddress);
        this.sock.send(snd);
    }

    public void sendAdvert(String eid) throws Exception
    {
        synchronized(this.contactEntities)
        {
            ByteBuffer buff = ByteBuffer.allocate(512);

            ContactEntity entity = this.contactEntities.get(eid);
            if(entity != null)
            {
                ContactMessage tx = new AdvertiseContactMessage(entity);

                // get message bytes
                int size = tx.getBytes(buff);
                buff.flip();

                // construct and send packet
                DatagramPacket snd = new DatagramPacket(buff.array(), size);
                snd.setSocketAddress(this.contactAddress);
                this.sock.send(snd);

                buff.clear();
            }
        }

    }

    public void sendAdverts() throws Exception
    {
        int size;
        ContactMessage tx;

        ByteBuffer buff = ByteBuffer.allocate(512);

        DatagramPacket snd = new DatagramPacket(buff.array(), buff.capacity());
        snd.setSocketAddress(this.contactAddress);

        ContactEntity[] entities = this.getAdvertisedContactEntities();

        for(ContactEntity entity: entities)
        {
            try
            {
                Thread.sleep(this.getRandom(3000));
            }
            catch(Exception e)
            {}

            buff.clear();

            tx = new AdvertiseContactMessage(entity);

            // get message bytes
            size = tx.getBytes(buff);
            buff.flip();

            // construct and send packet
            snd.setLength(size);
            this.sock.send(snd);
        }
    }

    public void add(ContactEntity entity)
    {
        synchronized(contactEntities)
        {
            this.contactEntities.put(entity.getEntityID(), entity);
        }
    }

    public void remove(String eid)
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

    public boolean hasEntity(String eid)
    {
        synchronized(contactEntities)
        {
            return contactEntities.containsKey(eid);
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

    public int getRandom(int bounds)
    {
        return rand.nextInt(bounds);
    }
}
