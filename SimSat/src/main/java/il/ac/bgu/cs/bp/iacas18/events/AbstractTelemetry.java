package il.ac.bgu.cs.bp.iacas18.events;

import il.ac.bgu.cs.bp.bpjs.model.BEvent;
import java.util.Objects;

/**
 * Base class for telemetry events.
 * 
 * @author michael
 */
public abstract class AbstractTelemetry extends BEvent {
    
    public final boolean isActivePass;
    
    public AbstractTelemetry(String eventName, boolean isActivePass) {
        super(eventName);
        this.isActivePass = isActivePass;
    }
    
    public boolean equalsAsTelemetry( AbstractTelemetry that ){
        return this.getName().equals(that.getName())
               && this.isActivePass == that.isActivePass
                && Objects.equals(this.getData(), that.getData());
    }
}
