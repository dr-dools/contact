package uk.me.drdools.contact;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;


/**
 *
 * @author dools
 */
class DatagramReceiver implements Runnable
{

    private final DatagramChannel dc;

    private final ContactEngine engine;

    private boolean running = false;

    public DatagramReceiver(DatagramChannel dc, ContactEngine engine)
    {
        this.dc = dc;
        this.engine = engine;
    }


    public boolean isRunning()
    {
        return running;
    }

    @Override
    public void run()
    {
        if(this.running) return;

        this.running = true;

        ByteBuffer buff = ByteBuffer.allocate(1024);

        while(this.running)
        {
            try
            {
                InetSocketAddress src = (InetSocketAddress)this.dc.receive(buff);

                buff.flip();

                try
                {
                    ContactMessage msg = ContactMessage.fromBytes(buff, src.getAddress());
                    ContactMessageHandler handleTask = new ContactMessageHandler(src, msg, engine);
                    engine.execute(handleTask);
                }
                catch(Exception e)
                {
                    e.printStackTrace();
                }

                buff.clear();
            }
            catch(IOException ex)
            {
                running = false;
            }

        }
    }

}
