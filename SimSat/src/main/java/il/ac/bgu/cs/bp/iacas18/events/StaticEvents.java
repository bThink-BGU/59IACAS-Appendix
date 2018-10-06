package il.ac.bgu.cs.bp.iacas18.events;

import il.ac.bgu.cs.bp.bpjs.model.BEvent;

@SuppressWarnings({ "serial" })
public class StaticEvents {
        public static BEvent ActivePass = new BEvent("ActivePass") {
        };
        public static BEvent PassDone = new BEvent("PassDone") {
        };
        public static BEvent Tick = new BEvent("Tick") {
        };
        public static BEvent SetEPSModeGood = new BEvent("SetEPSModeGood") {
        };
        public static BEvent SetEPSModeLow = new BEvent("SetEPSModeLow") {
        };
        public static BEvent SetEPSModeCritical = new BEvent("SetEPSModeCritical") {
        };
        public static BEvent SetADCSModeDetumbling = new BEvent("SetADCSModeDetumbling") {
        };
        public static BEvent SetADCSModeSunPointing = new BEvent("SetADCSModeSunPointing") {
        };
        public static BEvent SetADCSModePayloadPointing = new BEvent("SetADCSModePayloadPointing") {
        };

}
