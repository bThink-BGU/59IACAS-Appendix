/* global bp */
importPackage(Packages.il.ac.bgu.cs.bp.iacas18.events);

///////////////
// Constants

var EPSTelem = bp.EventSet("EPSTelem", function (e) {
    return e instanceof EPSTelemetry;
});

var ADCSTelem = bp.EventSet("ADCSTelem", function (e) {
    return e instanceof ADCSTelemetry;
});

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
bp.registerBThread("EPS - Turn ON/OFF logic", function () {

    /* Init */
    var ePSTelem = bp.sync({waitFor: EPSTelem});
    if (ePSTelem.vBatt >= GOOD_MIN) {
        bp.sync({waitFor: EPSTelem, 
            request: bp.Event("SetEPSModeGood")});
    } else if (ePSTelem.vBatt >= LOW_MIN) {
        bp.sync({waitFor: EPSTelem, 
            request: bp.Event("SetEPSModeLow")});
    } else {
        bp.sync({waitFor: EPSTelem, 
            request: bp.Event("SetEPSModeCritical")});
    }

    /* ongoing */
    while (true) {
        // TODO: this is really a state machine. Can we make is more elegant?
        //       Maybe some cool syntax generating this data structure.
        ePSTelem = bp.sync({waitFor: EPSTelem});

        if (ePSTelem.currentEPSMode == "Good") {
            if (ePSTelem.vBatt < GOOD_MIN) {
                bp.sync({
                    waitFor: EPSTelem, 
                    request: bp.Event("SetEPSModeLow")});
            }
        }

        if (ePSTelem.currentEPSMode == "Low") {
            if (ePSTelem.vBatt > LOW_MAX) {
                bp.sync({
                    waitFor: EPSTelem, 
                    request: bp.Event("SetEPSModeGood")});
            }
            if (ePSTelem.vBatt < LOW_MIN) {
                bp.sync({
                    waitFor: EPSTelem, 
                    request: bp.Event("SetEPSModeCritical")});
            }
        }

        if (ePSTelem.currentEPSMode == "Critical") {
            if (ePSTelem.vBatt > CRITICAL_MAX) {
                bp.sync({request: bp.Event("SetEPSModeLow")});
            }
        }
    }
});

/**
 * TODO: This seems like a complex scenario. Can we break it to:
 * * during a pass, point to target
 * * when there's low energy, point to the sun
 * * 
 */
bp.registerBThread("ADCS Mode Switch logic", function () {
    var activePass = false;
    /* Init Deployment*/
    bp.sync({request: bp.Event("SetADCSModeDetumbling")});

    /* ongoing */
    while (true) {
        var aDCSEvent = bp.sync({waitFor: [ADCSTelem, bp.Event("ActivePass"), bp.Event("PassDone")]});

        if (aDCSEvent.equals(bp.Event("ActivePass"))) {
            activePass = true;
            
        } else if (aDCSEvent.equals(bp.Event("PassDone"))) {
            activePass = false;
            
        } else {
            if (aDCSEvent.currentADCSMode == "Detumbling") {
                if (aDCSEvent.angularRate == "Low" && activePass) {
                    bp.sync({
                        waitFor: ADCSTelem, 
                        request: bp.Event("SetADCSModePayloadPointing")});
                    
                } else if (aDCSEvent.angularRate == "Low") {
                    bp.sync({
                        waitFor: ADCSTelem, 
                        request: bp.Event("SetADCSModeSunPointing")});
                }
            }

            if (aDCSEvent.currentADCSMode == "SunPointing") {
                if (aDCSEvent.angularRate == "Low" && activePass) {
                    bp.sync({
                        waitFor: ADCSTelem, 
                        request: bp.Event("SetADCSModePayloadPointing")});
                } else if (aDCSEvent.angularRate == "High") {
                    bp.sync({
                        waitFor: ADCSTelem, 
                        request: bp.Event("SetADCSModeDetumbling")});
                }
            }

            if (aDCSEvent.currentADCSMode == "PayloadPointing") {
                if (aDCSEvent.angularRate == "Low" && !activePass) {
                    bp.sync({
                        waitFor: ADCSTelem, 
                        request: bp.Event("SetADCSModeSunPointing")});
                } else if (aDCSEvent.angularRate == "High") {
                    bp.sync({
                        waitFor: ADCSTelem, 
                        request: bp.Event("SetADCSModeDetumbling")});
                }
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
    // FIXME: Need to maintain the "activePass" flag here. Or use a
    // BP/LSC-like idiom to unask this question.
    var activePass = false;
    while (true) {
        var ePSTelem2 = bp.sync({waitFor: EPSTelem});
        while (ePSTelem2.currentEPSMode == "Low" || 
               ePSTelem2.currentEPSMode == "Critical") {
            if (activePass == true) {
                var evt = bp.sync({
                    waitFor: ADCSTelem, 
                    request: bp.Event("PassDone"),
                    block: bp.Event("SetADCSModePayloadPointing")
                });
                if (evt.name.equals("PassDone")) {
                    activePass = false;
                }
            }
            var aDCSEvent2 = bp.sync({
                waitFor: ADCSTelem,
                block: bp.Event("SetADCSModePayloadPointing")
            });
            if (aDCSEvent2.currentADCSMode == "PayloadPointing") {
                bp.sync({
                    waitFor: ADCSTelem,
                    request: bp.Event("SetADCSModeSunPointing"),
                    block: bp.Event("SetADCSModePayloadPointing")
                });
            }
            var ePSTelem2 = bp.sync({
                waitFor: EPSTelem,
                block: bp.Event("SetADCSModePayloadPointing")
            });
        }
    }
});

