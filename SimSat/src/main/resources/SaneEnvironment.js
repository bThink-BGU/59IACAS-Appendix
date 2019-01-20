/* global bp, EPSTelemetry, ADCSTelemetry, Packages, SetEvent, StaticEvents */
importClass(Packages.il.ac.bgu.cs.bp.iacas18.events.EPSTelemetry);
importClass(Packages.il.ac.bgu.cs.bp.iacas18.events.ADCSTelemetry);
importClass(Packages.il.ac.bgu.cs.bp.iacas18.events.StaticEvents);

/*
 * This file contains b-threads for simulating the Java wrapper program. These 
 * b-threads simulate a sensible environment, where the system responds to the
 * 
 */

var setEvents = bp.EventSet("setEvents", function (e) {
    return e instanceof StaticEvents && e.name.startsWith("Set");
});

var passEvents = bp.EventSet("passEvents", function (e) {
    return e.name.contains("Pass");
});

var ENV_SET_ANGULAR_RATE_LOW = bp.Event("EnvSetAngularRateLow");
var ENV_SET_ANGULAR_RATE_HIGH = bp.Event("EnvSetAngularRateHigh");
var ENV_SETS_SET = bp.EventSet("EnvSetEvents", function(evt) {
    var evts = [ENV_SET_ANGULAR_RATE_LOW, ENV_SET_ANGULAR_RATE_HIGH];
    for ( var idx in evts ){
        if ( evt.equals(evts[idx])) return true;
    }
    return false;
});

// The set of event the environment has to respond to.
var RELEVANT_EVENTS = [passEvents, setEvents, ENV_SETS_SET];

function envReadjust( stepCount ){
    // let the system adjust a bit
    for ( var i=0; i<stepCount; i++ ) {
        bp.sync({waitFor:[EPSTelem, ADCSTelem]});
    }
}

/**
 * Updates the angular speed based on the ADCS SET events.
 */
bp.registerBThread("AngularSpeedAdjuster", function(){
    while ( true ) {
        bp.sync({waitFor:StaticEvents.SetADCSModeDetumbling});
        bp.sync({request:ENV_SET_ANGULAR_RATE_LOW});
        envReadjust(20);
    }
});

/**
 * requests external events that effect how the sattelite should behave.
 */
bp.registerBThread("ExternalEventInitiator", function(){
    while ( true ) {
        bp.sync({request:[
                ENV_SET_ANGULAR_RATE_HIGH,
                StaticEvents.ActivePass,
                StaticEvents.PassDone
        ]});
        envReadjust(20);
    }
});

/**
 * Maintains state, and pushes telemetries.
 */
bp.registerBThread("Environment", function () {
    var status = {
        vBatt: 0,
        isActivePass: false,
        epsMode: EPSTelemetry.EPSMode.Good,
        adcsMode: ADCSTelemetry.ADCSMode.Detumbling,
        angularRate: ADCSTelemetry.AngularRate.High
    };

    var batDir = 1;
    respondToModelEvents(bp.sync({waitFor:RELEVANT_EVENTS}), status);
    while (true) {
        status.vBatt = (status.vBatt + batDir);
        if (status.vBatt === 0)
            batDir = 1;
        else if (status.vBatt === 100)
            batDir = -1;

        // push eps
        var evt;
        var epsPushed = false;
        var adcsPushed = false;
        while ( ! epsPushed ) {
            evt = bp.sync({
                request:EPSTelemetry(status.vBatt, status.epsMode, status.isActivePass),
                waitFor:RELEVANT_EVENTS
            });
            epsPushed = !respondToModelEvents(evt, status);
        }
        while ( ! adcsPushed ) {
            evt = bp.sync({
                request:ADCSTelemetry(status.adcsMode, status.angularRate, status.isActivePass),
                waitFor:RELEVANT_EVENTS
            });
            adcsPushed = !respondToModelEvents(evt, status);
        }
    }
});

/**
 * If the event is a model-oriented event, handles it and returns `true`. 
 * Otherwise, returns `false`.
 * @param {type} anEvent the event to respond to
 * @param {type} aStatus Environment status to alter based on the event
 * @returns {Boolean} true iff aStatus was updated.
 */
function respondToModelEvents(anEvent, aStatus) {
    if (setEvents.contains(anEvent)) {
        handleSetEvents(anEvent, aStatus);
        return true;
    } else if (passEvents.contains(anEvent)) {
        handlePassEvents(anEvent, aStatus);
        return true;
    }
    return handleEnvSetEvent(anEvent, aStatus);
}

function handlePassEvents(anEvent, aStatus) {
    switch (anEvent) {
        case StaticEvents.ActivePass:
            aStatus.isActivePass = true;
            break;
        case StaticEvents.PassDone:
            aStatus.isActivePass = false;
            break;
    }
}

function handleSetEvents(anEvent, aStatus) {
    switch (anEvent) {
        case StaticEvents.SetEPSModeGood:
            aStatus.epsMode = EPSTelemetry.EPSMode.Good;
            break;

        case StaticEvents.SetEPSModeLow:
            aStatus.epsMode = EPSTelemetry.EPSMode.Low;
            break;

        case StaticEvents.SetEPSModeCritical:
            aStatus.epsMode = EPSTelemetry.EPSMode.Critical;
            break;

        case StaticEvents.SetADCSModeDetumbling:
            aStatus.adcsMode = ADCSTelemetry.ADCSMode.Detumbling;
            break;

        case StaticEvents.SetADCSModeSunPointing:
            aStatus.adcsMode = ADCSTelemetry.ADCSMode.SunPointing;
            break;

        case StaticEvents.SetADCSModePayloadPointing:
            aStatus.adcsMode = ADCSTelemetry.ADCSMode.PayloadPointing;
            break;

    }
}

function handleEnvSetEvent( anEvent, aStatus ) {
    
    if ( ENV_SET_ANGULAR_RATE_LOW.contains(anEvent) ){
        aStatus.angularRate = ADCSTelemetry.AngularRate.Low;
        return true;
    }

    if ( ENV_SET_ANGULAR_RATE_HIGH.contains(anEvent) ){
        aStatus.angularRate = ADCSTelemetry.AngularRate.High;
        return true;
    }

    return false;
}