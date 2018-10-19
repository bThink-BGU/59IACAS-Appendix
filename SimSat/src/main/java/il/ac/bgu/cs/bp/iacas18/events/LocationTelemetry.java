package il.ac.bgu.cs.bp.iacas18.events;

/**
 *
 * @author michael
 */
public class LocationTelemetry extends Telemetry implements java.io.Serializable {
    
    public final boolean isOverTarget;

    public LocationTelemetry(boolean isOverTarget) {
        super("LocationTelemetry");
        this.isOverTarget = isOverTarget;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 17 * hash + (this.isOverTarget ? 1 : 0);
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
        final LocationTelemetry other = (LocationTelemetry) obj;
        return this.isOverTarget == other.isOverTarget;
    }

    @Override
    public String toString() {
        return "[LocationTelemetry isOverTarget:" + isOverTarget +"]";
    }
    
}
