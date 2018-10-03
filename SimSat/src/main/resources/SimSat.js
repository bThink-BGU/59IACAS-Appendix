/* global bp */

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

///////////////
// b-threads

bp.registerBThread("EPS - Turn ON/OFF logic", function () {

    /* Init */
    var ePSTelem = bp.sync({waitFor: [EPSTelem]});
    if (ePSTelem.vBatt >= good_Min) {
        bp.sync({request: bp.Event("SetEPSMode-Good")});
    } else if (ePSTelem.vBatt >= low_Min) {
        bp.sync({request: bp.Event("SetEPSMode-Low")});
    } else {
        bp.sync({request: bp.Event("SetEPSMode-Critical")});
    }

    /* ongoing */
    while (true) {
        ePSTelem = bp.sync({waitFor: [EPSTelem]});

        if (ePSTelem.currentEPSMode == "Good") {
            if (ePSTelem.vBatt < good_Min) {
                bp.sync({request: bp.Event("SetEPSMode-Low")});
            }
        }

        if (ePSTelem.currentEPSMode == "Low") {
            if (ePSTelem.vBatt > low_Max) {
                bp.sync({request: bp.Event("SetEPSMode-Good")});
            }
            if (ePSTelem.vBatt < low_Min) {
                bp.sync({request: bp.Event("SetEPSMode-Critical")});
            }
        }

        if (ePSTelem.currentEPSMode == "Critical") {
            if (ePSTelem.vBatt > critical_Max) {
                bp.sync({request: bp.Event("SetEPSMode-Low")});
            }
        }
    }
});

bp.registerBThread("ADCS Mode Switch logic", function () {

    /* Init Deployment*/

    var activePass = false;
    bp.sync({request: bp.Event("SetADCSMode-Detumbling")});

    /* ongoing */
    while (true) {
        aDCSEvent = bp.sync({waitFor: [ADCSTelem, bp.Event("ActivePass"), bp.Event("PassDone")]});
        bp.sync({request: bp.Event("SetEPSMode-Low")});
        if (aDCSEvent.equals(bp.Event("ActivePayloadPass"))) {
            activePass = true;
        } else if (aDCSEvent.equals(bp.Event("PayloadPassDone!"))) {
            activePass = false;
        } else {
            if (aDCSEvent.currentADCSMode == "Detumbling") {
                if (aDCSEvent.lowAngularVel && activePass) {
                    bp.sync({request: bp.Event("SetADCSMode-PayloadPointing")});
                } else if (aDCSEvent.lowAngularVel) {
                    bp.sync({request: bp.Event("SetADCSMode-SunPointing")});
                }
            }

            if (aDCSEvent.currentADCSMode == "SunPointing") {
                if (aDCSEvent.lowAngularVel && activePass) {
                    bp.sync({request: bp.Event("SetADCSMode-PayloadPointing")});
                } else if (aDCSEvent.highAngularVel) {
                    bp.sync({request: bp.Event("SetADCSMode-Detumbling")});
                }
            }

            if (aDCSEvent.currentADCSMode == "PayloadPointing") {
                if (aDCSEvent.lowAngularVel && !activePass) {
                    bp.sync({request: bp.Event("SetADCSMode-SunPointing")});
                } else if (aDCSEvent.highAngularVel) {
                    bp.sync({request: bp.Event("SetADCSMode-Detumbling")});
                }
            }
        }
    }
});

bp.registerBThread("EPS & ADCS Integrator", function () {
    var ePSTelem = bp.sync({waitFor: EPSTelem});
    while (ePSTelem.currentEPSMode == "Low" || ePSTelem.currentEPSMode == "Critical") {
        bp.sync({
            waitFor: EPSTelem,
              block: bp.Event("SetADCSMode-PayloadPointing")
        });
    }
});

