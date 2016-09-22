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

    private final InetSocketAddress address2;

    public ContactEntity(ContactEntityID eid, InetSocketAddress address, InetSocketAddress address2, String fname) throws Exception
    {
        // validate EntityID
        if(eid == null) throw new Exception("Must be a valid ContactEntityID ("+eid+" is not valid)");

        // validate advertised socket address
        if((address == null) || (address2 == null)) throw new Exception("Must be a valid InetSocketAddress ("+address+" is not valid)");

        // validate friendly name
        if((fname == null) || fname.length() < 5) throw new Exception("Friendly Name is not valid ("+fname+")");


        // set instance variables
        this.eid = eid;
        this.address = address;
        this.address2 = address2;
        this.fName = fname;
    }

    @Override
    public String toString()
    {
        return "ContactEntity[id="+this.eid+", address1="+this.address+", address2="+this.address2+", fName="+this.fName+")";
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

    public InetSocketAddress getAddress2() {
        return address2;
    }



    public void setfName(String fName)
    {
        this.fName = fName;
    }
}
