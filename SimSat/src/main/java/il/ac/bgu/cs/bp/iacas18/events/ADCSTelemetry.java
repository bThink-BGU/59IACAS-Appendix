package il.ac.bgu.cs.bp.iacas18.events;

import java.util.Objects;

/**
 * Telemetry event for the ADCS system.
 */
public class ADCSTelemetry extends AbstractTelemetry {
    
    public enum ADCSMode {
        Detumbling, PayloadPointing, SunPointing
    }
    
    public enum AngularRate {
        High, Low
    }
    
    public final String currentADCSMode;
    public final String angularRate;
    public final ADCSMode mode;

    public ADCSTelemetry(ADCSMode currentADCSMode, AngularRate angularRate, boolean isActivePass) {
        super("ADCSTelemetry", isActivePass);
        this.currentADCSMode = currentADCSMode.name();
        this.angularRate = angularRate.name();
        this.mode = currentADCSMode;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 29 * hash + Objects.hashCode(this.currentADCSMode);
        hash = 29 * hash + Objects.hashCode(this.angularRate);
        hash = 29 * hash + Objects.hashCode(this.isActivePass);
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
        return Objects.equals(this.angularRate, other.angularRate) 
            && equalsAsTelemetry(other);
    }

    @Override
    public String toString() {
        return "[ADCSTelemetry currentADCSMode:" + currentADCSMode + ", angularRate:" + angularRate + ']';
    }
    
    
    
}
