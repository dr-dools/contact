package uk.me.drdools.contact.examples;


import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Date;
import uk.me.drdools.contact.ContactEngine;
import uk.me.drdools.contact.ContactEntity;
import uk.me.drdools.contact.ContactEntityID;
import uk.me.drdools.contact.ContactMessageListener;

/**
 *
 * @author dools
 */
public class ContactEngineExample
{
    public static final String DISC_MCAST_GRP = "225.4.5.6";

    public static final int DISC_MCAST_PORT = 5050;

    public static void main(String[] args)
    {
        // get singleton instance
        System.out.print("Creating ContactEngine...");
        long start = System.currentTimeMillis();
        ContactEngine engine = ContactEngine.getSingleton();
        long end = System.currentTimeMillis();
        System.out.println("Done (took "+(end-start)+" ms)");


        // add message listener
        engine.setMessageListener(new ContactMessageListener()
        {
            @Override
            public void contactEntityFound(ContactEntity entity, Date timestamp)
            {
                System.out.printf("*** Contact Entity Found (@%s): %s\n", timestamp, entity.toString());
            }
        });


        try
        {
            InetSocketAddress destGroup = new InetSocketAddress(DISC_MCAST_GRP, DISC_MCAST_PORT);
            System.out.print("Starting ContactEngine (joining Multicast group)...");
            start = System.currentTimeMillis();
            engine.start(destGroup);
            end = System.currentTimeMillis();
            System.out.println("Done (took "+(end-start)+" ms)");
        }
        catch (IOException ex)
        {
            ex.printStackTrace();
        }


        // publish the entity for a service
        try
        {
            /*
            for(int i=0; i<5; i++)
            {
                ContactEntityID service1_id = new ContactEntityID("urn:andium:test:s"+i);
                ContactEntity service1 = engine.getContactEntity(service1_id, 8080+i, "Service "+i);
                engine.add(service1);
            }
            System.out.print("Sending adverts for all Entities...");
            start = System.currentTimeMillis();
            engine.sendAdverts();
            end = System.currentTimeMillis();
            System.out.println("Done (took "+(end-start)+" ms)");

            Thread.sleep(2000);

            System.out.println("=================================");*/


            // send probe
            System.out.print("Sending Enumerate request...");
            start = System.currentTimeMillis();
            engine.sendEnumerate();
            end = System.currentTimeMillis();
            System.out.println("Done (took "+(end-start)+" ms)");


            Thread.sleep(2000);
/*
            System.out.println("=================================");

            System.out.print("Sending Search request...");
            ContactEntityID searchID = new ContactEntityID("urn:andium:test:s3");
            start = System.currentTimeMillis();
            engine.sendSearch(searchID);
            end = System.currentTimeMillis();
            System.out.println("Done (took "+(end-start)+" ms)");*/
        }
        catch(Exception e)
        {
            System.out.println("Could not create ContactEntity to advertise:");
            e.printStackTrace();
            System.exit(1);
        }


        try
        {
            //Thread.sleep(2000);
            System.in.read();
        }
        catch (Exception ex)
        {
        }


        // cleanup before exit
        try
        {
            engine.stop();
        }
        catch (Exception ex)
        {
        }

    }
}
