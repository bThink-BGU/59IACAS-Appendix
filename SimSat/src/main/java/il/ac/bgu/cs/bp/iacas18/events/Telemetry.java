package il.ac.bgu.cs.bp.iacas18.events;

import il.ac.bgu.cs.bp.bpjs.model.BEvent;

/**
 * Base class for telemetries.
 * 
 * @author michael
 */
public abstract class Telemetry extends BEvent {

    public Telemetry(String aName) {
        super(aName);
    }

    public Telemetry(String aName, Object someData) {
        super(aName, someData);
    }
    
}
