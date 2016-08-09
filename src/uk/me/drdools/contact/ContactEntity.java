package uk.me.drdools.contact;

import java.net.InetSocketAddress;

/**
 *
 * @author dools
 */
public class ContactEntity
{
    private String fName = null;

    private final ContactEntityID eid;

    private final InetSocketAddress address;

    public ContactEntity(ContactEntityID eid, InetSocketAddress address, String fname) throws Exception
    {
        // validate EntityID
        if(eid == null) throw new Exception("Must be a valid ContactEntityID ("+eid+" is not valid)");

        // validate advertised socket address
        if(address == null) throw new Exception("Must be a valid InetSocketAddress ("+address+" is not valid)");

        // validate friendly name
        if((fname == null) || fname.length() < 5) throw new Exception("Friendly Name is not valid ("+fname+")");


        // set instance variables
        this.eid = eid;
        this.address = address;
        this.fName = fname;
    }

    @Override
    public String toString()
    {
        return "ContactEntity[id="+this.eid+", address="+this.address+", fName="+this.fName+")";
    }

    @Override
    public boolean equals(Object obj)
    {
        return this.eid.equals(obj);
    }

    public String getfName()
    {
        return fName;
    }

    public ContactEntityID getEid()
    {
        return eid;
    }

    public InetSocketAddress getAddress()
    {
        return address;
    }

    public void setfName(String fName)
    {
        this.fName = fName;
    }
}
