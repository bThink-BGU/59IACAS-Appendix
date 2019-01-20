/* global bp, EPSTelemetry, ADCSTelemetry, StaticEvents, Packages */
importPackage(Packages.il.ac.bgu.cs.bp.iacas18.events);
importClass(Packages.il.ac.bgu.cs.bp.iacas18.events.EPSTelemetry);
importClass(Packages.il.ac.bgu.cs.bp.iacas18.events.StaticEvents);

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
                 request: StaticEvents.SetEPSModeGood});
    } else if (ePSTelem.vBatt >= LOW_MIN) {
        bp.sync({waitFor: EPSTelem, 
                 request: StaticEvents.SetEPSModeLow});
    } else {
        bp.sync({waitFor: EPSTelem, 
                 request: StaticEvents.SetEPSModeCritical});
    }
    delete ePSTelem;
   
    // ongoing control loop
    while (true) {
        var ePSTelem = bp.sync({waitFor: EPSTelem});
        switch ( ePSTelem.mode ) {
            case EPSTelemetry.EPSMode.Good: if (ePSTelem.vBatt < GOOD_MIN) {
                bp.sync({waitFor: EPSTelem, 
                    request: StaticEvents.SetEPSModeLow});
                }
                break;
                
            case EPSTelemetry.EPSMode.Low: 
                if (ePSTelem.vBatt > LOW_MAX) {
                bp.sync({waitFor: EPSTelem, 
                    request: StaticEvents.SetEPSModeGood});
                }
                if (ePSTelem.vBatt < LOW_MIN) {
                    bp.sync({waitFor: EPSTelem, 
                        request: StaticEvents.SetEPSModeCritical});
                }
                break;
                
            case EPSTelemetry.EPSMode.Critical: 
                if (ePSTelem.vBatt > CRITICAL_MAX) {
                    bp.sync({request: StaticEvents.SetEPSModeLow});
                }
                break;
        }
    }
});

bp.registerBThread("ADCS Mode Switch logic", function () {

    /* Init Deployment*/
    bp.sync({request: StaticEvents.SetADCSModeDetumbling});

    /* ongoing */
    while (true) {
        var aDCSEvent = bp.sync({waitFor: ADCSTelem});
        
        switch ( aDCSEvent.mode ) {
            case ADCSTelemetry.ADCSMode.Detumbling:
                if (aDCSEvent.angularRate == "Low" && aDCSEvent.isActivePass) {
                    bp.sync({waitFor: ADCSTelem,
                             request: StaticEvents.SetADCSModePayloadPointing});
                } else if (aDCSEvent.angularRate == "Low") {
                    bp.sync({waitFor: ADCSTelem,
                             request: StaticEvents.SetADCSModeSunPointing});
                }
                break;
            case ADCSTelemetry.ADCSMode.SunPointing:
                if (aDCSEvent.angularRate == "Low" && aDCSEvent.isActivePass) {
                    bp.sync({waitFor: ADCSTelem,
                             request: StaticEvents.SetADCSModePayloadPointing});
                } else if (aDCSEvent.angularRate == "High") {
                    bp.sync({waitFor: ADCSTelem,
                        request: StaticEvents.SetADCSModeDetumbling});
                }
                break;
            case ADCSTelemetry.ADCSMode.PayloadPointing:
                if (aDCSEvent.angularRate == "Low" && !aDCSEvent.isActivePass) {
                    bp.sync({waitFor: ADCSTelem,
                        request: StaticEvents.SetADCSModeSunPointing});
                } else if (aDCSEvent.angularRate == "High") {
                    bp.sync({waitFor: ADCSTelem,
                        request: StaticEvents.SetADCSModeDetumbling});
                }
                break;
        }
    }
});

bp.registerBThread("EPS & ADCS Integrator", function () {
    while (true) {
        var ePSTelem2 = bp.sync({waitFor: EPSTelem});
        while ( ePSTelem2.currentEPSMode == "Low" || 
                ePSTelem2.currentEPSMode == "Critical" ) {
            if ( ePSTelem2.isActivePass ) {
                bp.sync({waitFor: ADCSTelem,
                         request: StaticEvents.PassDone,
                           block: StaticEvents.SetADCSModePayloadPointing
                });
            }
            var aDCSEvent2 = bp.sync({waitFor: ADCSTelem,
                block: bp.Event("SetADCSModePayloadPointing")});
            if (aDCSEvent2.currentADCSMode == "PayloadPointing") {
                bp.sync({waitFor: ADCSTelem,
                         request: StaticEvents.SetADCSModeSunPointing,
                           block: StaticEvents.SetADCSModePayloadPointing
                });
            }
            var ePSTelem2 = bp.sync({waitFor: EPSTelem,
                block: StaticEvents.SetADCSModePayloadPointing
            });
        }
    }
});

