package il.ac.bgu.cs.bp.iacas18.events;

import java.util.Objects;

/**
 * Telemetry event for the Electronic Power Supply system.
 */
public class EPSTelemetry extends AbstractTelemetry {
    
    public enum EPSMode {
        Good, Low, Critical
    }
    
    public final int vBatt;
    public final String currentEPSMode;
    public final EPSMode mode;

    public EPSTelemetry(int vBatt, EPSMode currentEPSMode, boolean isActivePass) {
        super("EPSTelemetry", isActivePass);
        this.vBatt = vBatt;
        this.currentEPSMode = currentEPSMode.name();
        mode = currentEPSMode;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 97 * hash + this.vBatt;
        hash = 29 * hash + this.currentEPSMode.hashCode();
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
        return Objects.equals(this.currentEPSMode, other.currentEPSMode)
                && equalsAsTelemetry(other);
    }

    @Override
    public String toString() {
        return "[EPSTelemetry vBatt:" + vBatt 
             + " currentEPSMode:" + currentEPSMode 
             + " activePass:" + isActivePass + ']';
    }
    
    
    
}
