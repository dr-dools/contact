package uk.me.drdools.contact;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;


/**
 *
 * @author dools
 */
class DatagramReceiver implements Runnable
{
    private final MulticastSocket sock;

    private final ContactEngine engine;

    private boolean running = false;

    public DatagramReceiver(MulticastSocket socket, ContactEngine engine)
    {
        this.sock = socket;
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

        ByteBuffer buff = ByteBuffer.allocate(512);
        buff.order(ByteOrder.LITTLE_ENDIAN);
        DatagramPacket recv = new DatagramPacket(buff.array(), buff.capacity());


        while(this.running)
        {
            try
            {
                this.sock.receive(recv);

                try
                {
                    InetSocketAddress src = (InetSocketAddress)recv.getSocketAddress();

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
