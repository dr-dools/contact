package uk.me.drdools.contact;

import java.net.SocketAddress;

/**
 *
 * @author dools
 */
public class ContactMessageHandler implements Runnable
{
    private final SocketAddress src;

    private final ContactMessage msg;

    private final ContactEngine engine;

    public ContactMessageHandler(SocketAddress src, ContactMessage msg, ContactEngine engine)
    {
        this.src = src;
        this.msg = msg;
        this.engine = engine;
    }

    @Override
    public void run()
    {
        //System.out.println("*** Rx (from: "+src.toString()+"): "+msg);
        msg.onReceive(engine);
    }



}
