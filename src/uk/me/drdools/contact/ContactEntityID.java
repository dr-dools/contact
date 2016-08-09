package uk.me.drdools.contact;

/**
 *
 * @author dools
 */
public class ContactEntityID implements Comparable
{

    private final String id;


    public ContactEntityID(String id)
    {
        this.id = id;
    }


    @Override
    public String toString()
    {
        return this.id;
    }

    
    @Override
    public boolean equals(Object obj)
    {
        return this.id.equals(obj);
    }


    @Override
    public int compareTo(Object obj)
    {
        ContactEntityID another = (ContactEntityID)obj;
        return this.id.compareTo(another.id);
    }

}
