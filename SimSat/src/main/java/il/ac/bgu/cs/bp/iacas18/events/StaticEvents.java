package il.ac.bgu.cs.bp.iacas18.events;

import il.ac.bgu.cs.bp.bpjs.model.BEvent;
import java.io.ObjectStreamException;
import java.lang.reflect.Field;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Storage class for static events that can be re-used.
 * 
 * @author michael
 */
@SuppressWarnings({ "serial" })
public class StaticEvents extends BEvent {
    private StaticEvents(String name){
        super(name);
    }
    
    
    public static final StaticEvents Tick = new StaticEvents("Tick");
    
    public static final StaticEvents ActivePass = new StaticEvents("ActivePass");
    public static final StaticEvents PassDone   = new StaticEvents("PassDone");
    
    public static final StaticEvents SetEPSModeGood     = new StaticEvents("SetEPSModeGood");
    public static final StaticEvents SetEPSModeLow      = new StaticEvents("SetEPSModeLow");
    public static final StaticEvents SetEPSModeCritical = new StaticEvents("SetEPSModeCritical");
    
    public static final StaticEvents SetADCSModeDetumbling      = new StaticEvents("SetADCSModeDetumbling");
    public static final StaticEvents SetADCSModeSunPointing     = new StaticEvents("SetADCSModeSunPointing");
    public static final StaticEvents SetADCSModePayloadPointing = new StaticEvents("SetADCSModePayloadPointing");
    
    private static final Map<String, StaticEvents> INSTANCES = new TreeMap<>();
    
    // This nifty trick means there is only a single instance of each object in 
    // the system, even though we're using serialization.
    static {
        for ( Field fld : StaticEvents.class.getDeclaredFields() ) {
            if ( fld.getType().isAssignableFrom(StaticEvents.class) ) {
                try {
                    INSTANCES.put(fld.getName(), (StaticEvents) fld.get(null));
                } catch (IllegalArgumentException | IllegalAccessException ex) {
                    Logger.getLogger(StaticEvents.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        System.out.println("INSTANCES = " + INSTANCES);
    }
    
    Object readResolve() throws ObjectStreamException {
        StaticEvents resolvedEvent = INSTANCES.get(getName());
        if ( resolvedEvent == null ) {
            System.out.println("String '" + getName() + "' resolved to null");
        }
        return resolvedEvent;
    }
}