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

var low_Max = 70;
var good_Min = 60;
var critical_Max = 50;
var low_Min = 40;
var activePass = false;
///////////////
// b-threads

bp.registerBThread("EPS - Turn ON/OFF logic", function () {

    /* Init */
    var ePSTelem = bp.sync({waitFor: EPSTelem});
    if (ePSTelem.vBatt >= good_Min) {
        bp.sync({waitFor: EPSTelem, 
            request: bp.Event("SetEPSModeGood")});
    } else if (ePSTelem.vBatt >= low_Min) {
        bp.sync({waitFor: EPSTelem, 
            request: bp.Event("SetEPSModeLow")});
    } else {
        bp.sync({waitFor: EPSTelem, 
            request: bp.Event("SetEPSModeCritical")});
    }

    /* ongoing */
    while (true) {
        ePSTelem = bp.sync({waitFor: EPSTelem});

        if (ePSTelem.currentEPSMode == "Good") {
            if (ePSTelem.vBatt < good_Min) {
                bp.sync({waitFor: EPSTelem, 
                    request: bp.Event("SetEPSModeLow")});
            }
        }

        if (ePSTelem.currentEPSMode == "Low") {
            if (ePSTelem.vBatt > low_Max) {
                bp.sync({waitFor: EPSTelem, 
                    request: bp.Event("SetEPSModeGood")});
            }
            if (ePSTelem.vBatt < low_Min) {
                bp.sync({waitFor: EPSTelem, 
                    request: bp.Event("SetEPSModeCritical")});
            }
        }

        if (ePSTelem.currentEPSMode == "Critical") {
            if (ePSTelem.vBatt > critical_Max) {
                bp.sync({request: bp.Event("SetEPSModeLow")});
            }
        }
    }
});

bp.registerBThread("ADCS Mode Switch logic", function () {

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
                    bp.sync({waitFor: [ADCSTelem], 
                        request: bp.Event("SetADCSModePayloadPointing")});
                } else if (aDCSEvent.angularRate == "Low") {
                    bp.sync({waitFor: [ADCSTelem], 
                        request: bp.Event("SetADCSModeSunPointing")});
                }
            }

            if (aDCSEvent.currentADCSMode == "SunPointing") {
                if (aDCSEvent.angularRate == "Low" && activePass) {
                    bp.sync({waitFor: [ADCSTelem], 
                        request: bp.Event("SetADCSModePayloadPointing")});
                } else if (aDCSEvent.angularRate == "High") {
                    bp.sync({waitFor: [ADCSTelem], 
                        request: bp.Event("SetADCSModeDetumbling")});
                }
            }

            if (aDCSEvent.currentADCSMode == "PayloadPointing") {
                if (aDCSEvent.angularRate == "Low" && !activePass) {
                    bp.sync({waitFor: [ADCSTelem], 
                        request: bp.Event("SetADCSModeSunPointing")});
                } else if (aDCSEvent.angularRate == "High") {
                    bp.sync({waitFor: [ADCSTelem], 
                        request: bp.Event("SetADCSModeDetumbling")});
                }
            }
        }
    }
});

bp.registerBThread("EPS & ADCS Integrator", function () {
    while (true) {
        var ePSTelem2 = bp.sync({waitFor: EPSTelem});
        while (ePSTelem2.currentEPSMode == "Low" || 
               ePSTelem2.currentEPSMode == "Critical") {
            if (activePass = true)
                bp.sync({waitFor: [ADCSTelem], 
                    request: bp.Event("PassDone"),
                    block: bp.Event("SetADCSModePayloadPointing")
                });
            var aDCSEvent2 = bp.sync({waitFor: [ADCSTelem],
                block: bp.Event("SetADCSModePayloadPointing")});
            if (aDCSEvent2.currentADCSMode == "PayloadPointing") {
                bp.sync({waitFor: [ADCSTelem], 
                    request: bp.Event("SetADCSModeSunPointing"),
                    block: bp.Event("SetADCSModePayloadPointing")
                });
            }
            var ePSTelem2 = bp.sync({waitFor: EPSTelem,
                block: bp.Event("SetADCSModePayloadPointing")
            });
        }
    }
});

