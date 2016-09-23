package uk.me.drdools.contact;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.Map;
import java.util.TreeMap;

/**
 *
 * @author dools
 */
public class ContactEntity
{
    private String fName = null;

    private final String eid;

    private final Map<String, Integer> services = new TreeMap();

    private InetAddress addr = null;

    public ContactEntity(String eid, String fname) throws Exception
    {
        this(eid, fname, Inet4Address.getLocalHost());
    }

    public ContactEntity(String eid, String fname, InetAddress addr) throws Exception
    {
        // validate EntityID
        if(eid == null) throw new Exception("Must be a valid ContactEntityID ("+eid+" is not valid)");

        // validate friendly name
        if((fname == null) || fname.length() < 5) throw new Exception("Friendly Name is not valid ("+fname+")");

        // validate inet address
        if(addr == null) throw new Exception("Address is not valid ("+addr+")");

        this.addr = addr;

        // set instance variables
        this.eid = eid;
        this.fName = fname;
    }

    @Override
    public String toString()
    {
        return "ContactEntity[id="+this.eid+", fName="+this.fName+" @ "+this.addr+"]";
    }

    @Override
    public boolean equals(Object obj)
    {
        return this.eid.equals(obj);
    }

    public String getFriendlyName()
    {
        return fName;
    }

    public void setFriendlyName(String fName)
    {
        this.fName = fName;
    }

    public String getEntityID()
    {
        return eid;
    }

    public void setService(String serviceName, int port)
    {
        synchronized(this.services)
        {
            this.services.put(serviceName, port);
        }
    }

    public InetSocketAddress getService(String serviceName)
    {
        int port = 0;
        synchronized(this.services)
        {
            port = this.services.get(serviceName);
        }

        return new InetSocketAddress(this.addr, port);
    }

    public String[] serviceNames()
    {
        synchronized(this.services)
        {
            return this.services.keySet().toArray(new String[0]);
        }
    }

    public Map<String, Integer> getServices()
    {
        return this.services;
    }
}
