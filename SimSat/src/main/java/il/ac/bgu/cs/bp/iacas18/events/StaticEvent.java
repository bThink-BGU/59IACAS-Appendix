package il.ac.bgu.cs.bp.iacas18.events;

import il.ac.bgu.cs.bp.bpjs.model.BEvent;

@SuppressWarnings({ "serial" })
public class StaticEvent extends BEvent {
    private StaticEvent(String name ) {
        super(name);
    }
    
    public static StaticEvent ActivePass = new StaticEvent("ActivePass");
    public static StaticEvent PassDone = new StaticEvent("PassDone");
    public static StaticEvent Tick = new StaticEvent("Tick");
    public static StaticEvent SetEPSModeGood = new StaticEvent("SetEPSModeGood");
    public static StaticEvent SetEPSModeLow = new StaticEvent("SetEPSModeLow");
    public static StaticEvent SetEPSModeCritical = new StaticEvent("SetEPSModeCritical");
    public static StaticEvent SetADCSModeDetumbling = new StaticEvent("SetADCSModeDetumbling");
    public static StaticEvent SetADCSModeSunPointing = new StaticEvent("SetADCSModeSunPointing");
    public static StaticEvent SetADCSModePayloadPointing = new StaticEvent("SetADCSModePayloadPointing");

}