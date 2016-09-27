package uk.me.drdools.contact.examples;


import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.util.Date;
import uk.me.drdools.contact.ContactEngine;
import uk.me.drdools.contact.ContactEntity;
import uk.me.drdools.contact.ContactMessageListener;


/**
 *
 * @author dools
 */
public class ContactEngineExample
{
    public static final String DISC_MCAST_GRP = "225.4.5.6";

    public static final int DISC_MCAST_PORT = 5050;


    private static void createEntities(ContactEngine engine, int num) throws Exception
    {
/*
        for(int i=1; i<=num; i++)
        {
            ContactEntityID service1_id = new ContactEntityID("urn:andium:test:s"+i);
            ContactEntity service1 = engine.getContactEntity(service1_id, 8080+i, 5683+i, "Service "+i);
            engine.add(service1);
        }*/
        String service1_id = "urn:andium:eid:95d7347d-9560-4e2e-bd43-9f90c8b2e67f";
        ContactEntity service1 = new ContactEntity(service1_id, "Edge Service");
        service1.setService("sock", 8080);
        service1.setService("coap", 5683);
        engine.add(service1);
    }

    private static void sendAllAdverts(ContactEngine engine) throws Exception
    {
        System.out.print("Sending adverts for all Entities...");
        long start = System.currentTimeMillis();
        engine.sendAdverts();
        long end = System.currentTimeMillis();
        System.out.println("Done (took "+(end-start)+" ms)");
    }

    private static void sendSearch(ContactEngine engine, String searchID) throws Exception
    {
        System.out.print("Sending Search request...");
        long start = System.currentTimeMillis();
        engine.sendSearch(searchID);
        long end = System.currentTimeMillis();
        System.out.println("Done (took "+(end-start)+" ms)");
    }

    private static void sendProbe(ContactEngine engine) throws Exception
    {
        System.out.print("Sending Enumerate request...");
        long start = System.currentTimeMillis();
        engine.sendEnumerate();
        long end = System.currentTimeMillis();
        System.out.println("Done (took "+(end-start)+" ms)");
    }

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
                System.out.println("\tservices:");
                for(String sName: entity.serviceNames())
                {
                    System.out.printf("\t\t%s = %d\n", sName, entity.getService(sName).getPort());
                }

                System.out.println("************************************************************************************");
            }
        });


        try
        {
            NetworkInterface ni = NetworkInterface.getByName("en1");

            InetSocketAddress destGroup = new InetSocketAddress(DISC_MCAST_GRP, DISC_MCAST_PORT);
            System.out.print("Starting ContactEngine (joining Multicast group)...");
            start = System.currentTimeMillis();

            // the NetworkInterface is optional, but allows for more  control
            engine.start(ni, destGroup);
            end = System.currentTimeMillis();
            System.out.println("Done (took "+(end-start)+" ms)");
        }
        catch (IOException ex)
        {
            ex.printStackTrace();
        }



        try
        {
            // publish entities
            //createEntities(engine, 5);
            //sendAllAdverts(engine);


            Thread.sleep(2000);
            System.out.println("=================================");


            // send probe
            sendProbe(engine);


            Thread.sleep(2000);
            System.out.println("=================================");

            //ContactEntityID searchID = new ContactEntityID("urn:andium:test:s3");
            String searchID = "urn:andium:eid:95d7347d-9560-4e2e-bd43-9f90c8b2e67f";
            //sendSearch(engine, searchID);


            //Thread.sleep(2000);
            System.in.read();

            // cleanup before exit
            engine.stop();
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }

    }
}
