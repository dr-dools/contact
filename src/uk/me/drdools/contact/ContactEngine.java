package uk.me.drdools.contact;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.StandardProtocolFamily;
import java.net.StandardSocketOptions;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
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
    private DatagramChannel channel = null;

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

    public ContactEntity getContactEntity(ContactEntityID eid, int port, String fname) throws Exception
    {
        InetSocketAddress isa = new InetSocketAddress(this.contactAddress.getAddress(), port);

        ContactEntity rtn = new ContactEntity(eid, isa, fname);
        return rtn;
    }

    public void setMessageListener(ContactMessageListener listener)
    {
        this.listener = listener;
    }


    public void start(NetworkInterface ni, InetSocketAddress addr) throws IOException
    {
        this.contactAddress = addr;

        // init the datagram channel for multicast
        this.channel = DatagramChannel.open(StandardProtocolFamily.INET);
        this.channel.setOption(StandardSocketOptions.SO_REUSEADDR, true);
        this.channel.bind(new InetSocketAddress(addr.getPort()));
        this.channel.setOption(StandardSocketOptions.IP_MULTICAST_IF, ni);
        this.channel.configureBlocking(true);

        // join the right group !
        this.channel.join(this.contactAddress.getAddress(), ni);

        this.dr = new DatagramReceiver(channel, this);
        th = new Thread(this.dr, "ContactEngineDatagramRx");
        th.start();
    }


    public void stop() throws Exception
    {
        try
        {
            this.channel.close();
            if (this.messageHandler != null)
            {
            	this.messageHandler.shutdown();
            }
        }
        catch (IOException e)
        {}
    }

    public boolean isListening()
    {
        return false;
    }

    public void sendEnumerate() throws Exception
    {
        EnumerateContactMessage tx = new EnumerateContactMessage();

        // send
        ByteBuffer bb = ByteBuffer.allocate(1024);
        tx.getBytes(bb);
        bb.flip();
        this.channel.send(bb, this.contactAddress);
    }

    public void sendSearch(ContactEntityID eid) throws Exception
    {
        SearchContactMessage tx = new SearchContactMessage(eid);

        // send
        ByteBuffer bb = ByteBuffer.allocate(1024);
        tx.getBytes(bb);
        bb.flip();
        this.channel.send(bb, this.contactAddress);
    }

    public void sendAdvert(ContactEntityID eid) throws Exception
    {
        synchronized(this.contactEntities)
        {
            ContactEntity entity = this.contactEntities.get(eid);
            if(entity != null)
            {
                // do advertise
                ByteBuffer bb = ByteBuffer.allocate(1024);

                ContactMessage tx = new AdvertiseContactMessage(entity);
                tx.getBytes(bb);
                bb.flip();
                this.channel.send(bb, this.contactAddress);
            }
        }

    }

    public void sendAdverts() throws Exception
    {
        ContactEntity[] entities = this.getAdvertisedContactEntities();

        for(ContactEntity entity: entities)
        {
            // do advertise
            ByteBuffer bb = ByteBuffer.allocate(1024);

            ContactMessage tx = new AdvertiseContactMessage(entity);
            tx.getBytes(bb);
            bb.flip();
            this.channel.send(bb, this.contactAddress);
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
    public void contactEntityFound(ContactEntity entity)
    {
        if(this.listener != null)
        {
            this.listener.contactEntityFound(entity);
        }
    }
}
