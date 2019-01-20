/* global bp, EPSTelemetry, ADCSTelemetry, Packages */
importPackage(Packages.il.ac.bgu.cs.bp.iacas18.events);
importClass(Packages.il.ac.bgu.cs.bp.iacas18.events.EPSTelemetry);

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
   
    // ongoing control loop
    while (true) {
        var ePSTelem = bp.sync({waitFor: EPSTelem});
        switch ( ePSTelem.mode ) {
            case EPSTelemetry.EPSMode.Good: if (ePSTelem.vBatt < GOOD_MIN) {
                bp.sync({waitFor: EPSTelem, 
                    request: bp.Event("SetEPSModeLow")});
                }
                break;
                
            case EPSTelemetry.EPSMode.Low: 
                if (ePSTelem.vBatt > LOW_MAX) {
                bp.sync({waitFor: EPSTelem, 
                    request: bp.Event("SetEPSModeGood")});
                }
                if (ePSTelem.vBatt < LOW_MIN) {
                    bp.sync({waitFor: EPSTelem, 
                        request: bp.Event("SetEPSModeCritical")});
                }
                break;
                
            case EPSTelemetry.EPSMode.Critical: 
                if (ePSTelem.vBatt > CRITICAL_MAX) {
                    bp.sync({request: bp.Event("SetEPSModeLow")});
                }
                break;
        }
    }
});

bp.registerBThread("ADCS Mode Switch logic", function () {

    /* Init Deployment*/
    bp.sync({request: bp.Event("SetADCSModeDetumbling")});

    /* ongoing */
    while (true) {
        var aDCSEvent = bp.sync({waitFor: ADCSTelem});
        
        switch ( aDCSEvent.mode ) {
            case ADCSTelemetry.ADCSMode.Detumbling:
                if (aDCSEvent.angularRate == "Low" && aDCSEvent.isActivePass) {
                    bp.sync({waitFor: ADCSTelem,
                             request: bp.Event("SetADCSModePayloadPointing")});
                } else if (aDCSEvent.angularRate == "Low") {
                    bp.sync({waitFor: ADCSTelem,
                             request: bp.Event("SetADCSModeSunPointing")});
                }
                break;
            case ADCSTelemetry.ADCSMode.SunPointing:
                if (aDCSEvent.angularRate == "Low" && aDCSEvent.isActivePass) {
                    bp.sync({waitFor: ADCSTelem,
                             request: bp.Event("SetADCSModePayloadPointing")});
                } else if (aDCSEvent.angularRate == "High") {
                    bp.sync({waitFor: ADCSTelem,
                        request: bp.Event("SetADCSModeDetumbling")});
                }
                break;
            case ADCSTelemetry.ADCSMode.PayloadPointing:
                if (aDCSEvent.angularRate == "Low" && !aDCSEvent.isActivePass) {
                    bp.sync({waitFor: ADCSTelem,
                        request: bp.Event("SetADCSModeSunPointing")});
                } else if (aDCSEvent.angularRate == "High") {
                    bp.sync({waitFor: ADCSTelem,
                        request: bp.Event("SetADCSModeDetumbling")});
                }
                break;
        }
    }
});

