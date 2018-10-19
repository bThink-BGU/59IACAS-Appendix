/* global bp */
importPackage(Packages.il.ac.bgu.cs.bp.iacas18.events);

/*
 
 General ideas going fwd:

req 1: Point to sun when can (Default behavior)
req 2: Point payload when active pass
req 3: but not when battery low, in which case you point to the sun 
req 4: When you need to detumble, leave everything and de-tumble

Note two reasons for pointing at the sun - default and emergency. Might lead to
two sun-pointing internal events, that get translated to an actuation event by
a dedicated b-thread.
 
 */


///////////////
// Constants

var EPSTelems = bp.EventSet("EPSTelems", function (e) {
    return e instanceof EPSTelemetry;
});

var ADCSTelems = bp.EventSet("ADCSTelems", function (e) {
    return e instanceof ADCSTelemetry;
});

var locationTelems = bp.EventSet("locationTelems", function (e) {
    return e instanceof LocationTelemetry;
});

var EPSChanges = bp.EventSet("EPS Changes", function(e){
    return e.name.contains("SetEPSMode");
});

var ADCSNames = {
    payloadPointing: "SetADCSModePayloadPointing",
    sunPointing: "SetADCSModeSunPointing",
    detumbling: "SetADCSModeDetumbling"
};

var LOW_MAX = 70;
var GOOD_MIN = 60;
var CRITICAL_MAX = 50;
var LOW_MIN = 40;

///////////////
// b-threads

/**
 * EPS Logic. Requirement:
 * EPS Mode should reflect operational semantics* of battery voltage.
 * 
 * * "operational semantics": What does the voltage mean for the system.
 * 
 */
bp.registerBThread("EPS-logic", function () {

    //Init
    var ePSTelem = bp.sync({waitFor: EPSTelems});
    if (ePSTelem.vBatt >= GOOD_MIN) {
        bp.sync({waitFor: EPSTelems, 
                 request: bp.Event("SetEPSModeGood")});
    } else if (ePSTelem.vBatt >= LOW_MIN) {
        bp.sync({waitFor: EPSTelems, 
                 request: bp.Event("SetEPSModeLow")});
    } else {
        bp.sync({waitFor: EPSTelems, 
                 request: bp.Event("SetEPSModeCritical")});
    }

    // ongoing 
    while (true) {
        // TODO: this is really a state machine. Can we make is more elegant?
        //       Maybe some cool syntax generating this data structure.
        ePSTelem = bp.sync({waitFor: EPSTelems});
        
        switch ( String(ePSTelem.currentEPSMode)) { // Using the String converter because Rhino uses a ConString class here.
            case "Good":
                if (ePSTelem.vBatt < GOOD_MIN) {
                    bp.sync({waitFor: EPSTelems, 
                             request: bp.Event("SetEPSModeLow")});
                }
                break;
                
            case "Low":
                if (ePSTelem.vBatt > LOW_MAX) {
                    bp.sync({waitFor: EPSTelems, 
                             request: bp.Event("SetEPSModeGood")});
                } else if (ePSTelem.vBatt < LOW_MIN) {
                    bp.sync({waitFor: EPSTelems, 
                             request: bp.Event("SetEPSModeCritical")});
                };
                break;
                
            case "Critical":
                if (ePSTelem.vBatt > CRITICAL_MAX) {
                    bp.sync({request: bp.Event("SetEPSModeLow")});
                }
                break;
       }
    }
});

/**
 * Requirement: When in a pass, use payload.
 */
bp.registerBThread( "PointPayloadInPass", function(){
    var evt = bp.sync({waitFor:locationTelems});
    var pointingRequested = false;
    while ( true ) {
        if ( evt.isOverTarget ) {
           if ( !pointingRequested ) {
                evt = bp.sync({
                    request:bp.Event(ADCSNames.payloadPointing)
                });
                pointingRequested = true;
            }
        } else {
            pointingRequested = false;
        }
        evt = bp.sync({waitFor:locationTelems});
    }
});

/**
 * Request: On low battery, point to the sun, not to the target.
 */
bp.registerBThread( "EPSRestrictions", function(){
    var evt = bp.sync({waitFor:EPSChanges});
    var sunPointing = false;
    while ( true ) {
        if ( evt.name != "SetEPSModeGood" ) {
            evt = bp.sync({
               request: sunPointing ? null : bp.Event(ADCSNames.sunPointing),
               block: bp.Event(ADCSNames.payloadPointing)
            });
            sunPointing = true;
        } else {
            sunPointing = false;
        }
        evt = bp.sync({waitFor:EPSChanges});
    }
});

if ( false ) {
/**
 * TODO: This seems like a complex scenario. Can we break it to:
 * * during a pass, point to target
 * * when there's low energy, point to the sun
 * * 
 */
bp.registerBThread("ADCS Mode Switch logic", function() {
    var activePass = false;
    /* Init Deployment*/
    bp.sync({request: bp.Event("SetADCSModeDetumbling")});

    /* ongoing */
    while (true) {
        var telemEvent = bp.sync({waitFor: [ADCSTelems, locationTelems]});

        if ( locationTelems.contains(telemEvent) ) {
            activePass = telemEvent.isOverTarget;
            
        } else {
                
            switch ( String(telemEvent.currentADCSMode) ) {
                case "Detumbling":
                    if (telemEvent.angularRate == "Low") {
                        bp.sync({
                            waitFor: ADCSTelems, 
                            request: activePass ? bp.Event("SetADCSModePayloadPointing") : bp.Event("SetADCSModeSunPointing")});
                    }
                    break;
               
                case "SunPointing":
                     if (telemEvent.angularRate == "Low" && activePass) {
                        bp.sync({
                            waitFor: ADCSTelems, 
                            request: bp.Event("SetADCSModePayloadPointing")});
                    } else if (telemEvent.angularRate == "High") {
                        bp.sync({
                            waitFor: ADCSTelems, 
                            request: bp.Event("SetADCSModeDetumbling")});
                    }
                    break;
                    
                case "PayloadPointing":
                    if (telemEvent.angularRate == "Low" && !activePass) {
                        bp.sync({
                            waitFor: ADCSTelems, 
                            request: bp.Event("SetADCSModeSunPointing")});
                    } else if (telemEvent.angularRate == "High") {
                        bp.sync({
                            waitFor: ADCSTelems, 
                            request: bp.Event("SetADCSModeDetumbling")});
                    }
                    break;
            }
        }
    }
});

/**
 * EPS+ADCS integration
 * Requirement:
 * The system will first take care of its energy requirements, and only after
 *   that deal with its mission
 *   
 * corollary: Don't use the payload if you don't have the energy
 * 
 */
bp.registerBThread("EPS & ADCS Integrator", function () {
    while (true) {
        var ePSTelem2 = bp.sync({waitFor: EPSTelems});
        while (ePSTelem2.currentEPSMode == "Low" || 
               ePSTelem2.currentEPSMode == "Critical") {
            var aDCSEvent2 = bp.sync({
                waitFor: ADCSTelems,
                block: bp.Event("SetADCSModePayloadPointing")
            });
            if (aDCSEvent2.currentADCSMode == "PayloadPointing") {
                bp.sync({
                    waitFor: ADCSTelems,
                    request: bp.Event("SetADCSModeSunPointing"),
                    block: bp.Event("SetADCSModePayloadPointing")
                });
            }
            var ePSTelem2 = bp.sync({
                waitFor: EPSTelems,
                block: bp.Event("SetADCSModePayloadPointing")
            });
        }
    }
});

}