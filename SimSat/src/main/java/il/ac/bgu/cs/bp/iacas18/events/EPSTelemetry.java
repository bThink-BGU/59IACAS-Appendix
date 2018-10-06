package il.ac.bgu.cs.bp.iacas18.events;

import il.ac.bgu.cs.bp.bpjs.model.BEvent;
import java.util.Objects;

/**
 * Telemetry event for the Electronic Power Supply system.
 */
public class EPSTelemetry extends BEvent {
    
    public enum EPSMode {
        Good, Low, Critical
    }
    
    public final int vBatt;
    public final String currentEPSMode;

    public EPSTelemetry(int vBatt, EPSMode currentEPSMode) {
        super("EPSTelemetry");
        this.vBatt = vBatt;
        this.currentEPSMode = currentEPSMode.name();
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 97 * hash + this.vBatt;
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final EPSTelemetry other = (EPSTelemetry) obj;
        if (this.vBatt != other.vBatt) {
            return false;
        }
        return Objects.equals(this.currentEPSMode, other.currentEPSMode);
    }

    @Override
    public String toString() {
        return "[EPSTelemetry vBatt:" + vBatt + ", currentEPSMode:" + currentEPSMode + ']';
    }
    
    
    
}
