package uk.me.drdools.contact.examples;

import java.awt.AWTException;
import java.awt.HeadlessException;
import java.awt.Image;
import java.awt.Menu;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.URL;
import java.util.Date;
import java.util.Enumeration;
import java.util.Map;
import java.util.TreeMap;
import uk.me.drdools.contact.ContactEngine;
import uk.me.drdools.contact.ContactEntity;
import uk.me.drdools.contact.ContactMessageListener;
import static uk.me.drdools.contact.examples.ContactEngineExample.DISC_MCAST_GRP;
import static uk.me.drdools.contact.examples.ContactEngineExample.DISC_MCAST_PORT;

/**
 *
 * @author dools
 */
public class SystemTrayExample implements ContactMessageListener, ActionListener
{
    private final ContactEngine engine;

    private final PopupMenu popup = new PopupMenu();
    private final MenuItem status = new MenuItem("Status: Down");
    private final TrayIcon trayIcon;

    private final Map<String, EntityMenu> entities = new TreeMap();


    private class EntityMenu extends Menu
    {
        public ContactEntity entity;
        public Date timestamp;

        public final MenuItem eid;
        public final MenuItem tstamp;
        public final Menu services = new Menu("Services");

        public EntityMenu(String label, ContactEntity entity, Date timestamp) throws HeadlessException
        {
            super(label);
            this.entity = entity;
            this.timestamp = timestamp;

            eid = new MenuItem(entity.getEntityID());
            tstamp = new MenuItem(timestamp.toString());

            for(String sName: entity.serviceNames())
            {
                services.add(sName+" ("+entity.getService(sName)+")");
            }

            super.add(eid);
            super.add(tstamp);
            super.addSeparator();
            super.add(services);
        }
    }

    public SystemTrayExample() throws IOException
    {
        // start system tray
        if (SystemTray.isSupported())
        {
            // load an image
            URL url = SystemTrayExample.class.getResource("/andiumControl.png");
            Image image = Toolkit.getDefaultToolkit().getImage(url);

            // get the SystemTray instance
            SystemTray tray = SystemTray.getSystemTray();

            trayIcon = new TrayIcon(image, "Contact (0 entities)", popup);
            trayIcon.addActionListener(this);
            // ...
            // add the tray image
            try
            {
                tray.add(trayIcon);
            }
            catch (AWTException e)
            {
                System.err.println(e);
            }


            // create a popup menu
            popup.addSeparator();

            // add status menu option
            status.setActionCommand("Status");
            status.addActionListener(this);
            popup.add(status);

            // add menu option to send enum
            MenuItem enumItem = new MenuItem("Send Enumerate (probe)");
            enumItem.setActionCommand("Enum");
            enumItem.addActionListener(this);
            popup.add(enumItem);

            // create menu item for exit
            MenuItem defaultItem = new MenuItem("Exit Contact");
            defaultItem.setActionCommand("Exit");
            defaultItem.addActionListener(this);
            popup.add(defaultItem);
        }
        else
        {
            trayIcon = null;
        }


        NetworkInterface ni = NetworkInterface.getByName("en1");
        engine = ContactEngine.getSingleton();
        engine.setMessageListener(this);
        InetSocketAddress destGroup = new InetSocketAddress(DISC_MCAST_GRP, DISC_MCAST_PORT);
        engine.start(ni, destGroup);

        status.setLabel("Status: Up");
    }

    public static void main(String[] args)
    {
        try
        {
            SystemTrayExample eg = new SystemTrayExample();
        }
        catch (IOException ex)
        {
            ex.printStackTrace();
        }
    }

    @Override
    public void contactEntityFound(ContactEntity entity, Date timestamp)
    {
        // an Entity has been advertised using Contact!

        synchronized(entities)
        {
            EntityMenu entityItem = entities.get(entity.getEntityID());

            if(entityItem == null)
            {
                //System.out.println("Entity advertised... adding to popup: "+entity.getFriendlyName());
                entityItem = new EntityMenu(entity.getFriendlyName(), entity, timestamp);
                entityItem.setActionCommand("Entity");
                entityItem.addActionListener(this);
                popup.insert(entityItem, 0);
                entities.put(entity.getEntityID(), entityItem);
            }
            else
            {
                // update entry
                entityItem.entity = entity;
                entityItem.setLabel(entity.getFriendlyName());
                entityItem.timestamp = timestamp;
            }

            if(trayIcon != null)
            {
                trayIcon.setToolTip("Contact ("+entities.size()+" entities)");
            }
        }

    }

    @Override
    public void actionPerformed(ActionEvent e)
    {
        // something in the system tray was selected...
        try
        {
            switch(e.getActionCommand())
            {
                case "Exit":
                    // cleanup
                    engine.stop();
                    System.exit(0);
                    break;

                case "Status":
                    // so something
                    break;

                case "Enum":
                    engine.sendEnumerate();
                    break;

                case "Entity":
                    EntityMenu entityItem = (EntityMenu)e.getSource();
                    System.out.println(entityItem.entity.getService("coap"));
                    break;

                default:
                    System.out.println("User selected some shiz!");
            }
        }
        catch(Exception ex)
        {
            ex.printStackTrace();
        }
    }

}
