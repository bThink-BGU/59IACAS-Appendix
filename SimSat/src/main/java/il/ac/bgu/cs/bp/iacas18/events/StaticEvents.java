package il.ac.bgu.cs.bp.iacas18.events;

import il.ac.bgu.cs.bp.bpjs.model.BEvent;

/**
 * Storage class for static events that can be re-used.
 * 
 * @author michael
 */
@SuppressWarnings({ "serial" })
public abstract class StaticEvents {
    private StaticEvents() {/* prevent instantiation */}
    
    public static class SetEvent extends BEvent {
        private SetEvent(String name){
            super(name);
        }
    }
    
    public static BEvent   ActivePass = new BEvent("ActivePass");
    public static BEvent   PassDone   = new BEvent("PassDone");
    public static BEvent   Tick       = new BEvent("Tick");
    
    public static SetEvent SetEPSModeGood     = new SetEvent("SetEPSModeGood");
    public static SetEvent SetEPSModeLow      = new SetEvent("SetEPSModeLow");
    public static SetEvent SetEPSModeCritical = new SetEvent("SetEPSModeCritical");
    
    public static SetEvent SetADCSModeDetumbling      = new SetEvent("SetADCSModeDetumbling");
    public static SetEvent SetADCSModeSunPointing     = new SetEvent("SetADCSModeSunPointing");
    public static SetEvent SetADCSModePayloadPointing = new SetEvent("SetADCSModePayloadPointing");

}