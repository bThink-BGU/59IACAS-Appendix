package il.ac.bgu.cs.bp.iacas18.events;

import il.ac.bgu.cs.bp.bpjs.model.BEvent;
import java.util.Objects;

/**
 * Telemetry event for the ADCS system.
 */
public class ADCSTelemetry extends BEvent {
    
    public enum ADCSMode {
        Detumbling, PayloadPointing, SunPointing
    }
    
    public enum AngularRate {
        High, Low
    }
    
    public final String currentADCSMode;
    public final String angularRate;

    public ADCSTelemetry(ADCSMode currentADCSMode, AngularRate angularRate) {
        super("ADCSTelemetry");
        this.currentADCSMode = currentADCSMode.name();
        this.angularRate = angularRate.name();
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 29 * hash + Objects.hashCode(this.currentADCSMode);
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
        final ADCSTelemetry other = (ADCSTelemetry) obj;
        if (!Objects.equals(this.currentADCSMode, other.currentADCSMode)) {
            return false;
        }
        return Objects.equals(this.angularRate, other.angularRate);
    }

    @Override
    public String toString() {
        return "[ADCSTelemetry currentADCSMode:" + currentADCSMode + ", angularRate:" + angularRate + ']';
    }
    
    
    
}
