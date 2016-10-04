package uk.me.drdools.contact;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;


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

        byte[] buff = new byte[512];
        DatagramPacket recv = new DatagramPacket(buff, 512);

        while(this.running)
        {
            try
            {
                this.sock.receive(recv);

                try
                {
                    InetSocketAddress src = (InetSocketAddress)recv.getSocketAddress();

                    System.out.println("Rx: "+new String(buff, 0, recv.getLength()));

                    ContactMessage msg = ContactMessage.fromBytes(buff, recv.getLength(), src.getAddress());
                    ContactMessageHandler handleTask = new ContactMessageHandler(src, msg, engine);
                    engine.execute(handleTask);
                }
                catch(Exception e)
                {
                    e.printStackTrace();
                }
            }
            catch(IOException ex)
            {
                running = false;
            }

        }
    }

}
