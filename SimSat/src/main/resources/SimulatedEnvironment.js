/* global bp, EPSTelemetry, ADCSTelemetry, Packages */
importClass(Packages.il.ac.bgu.cs.bp.iacas18.events.EPSTelemetry);
importClass(Packages.il.ac.bgu.cs.bp.iacas18.events.ADCSTelemetry);

/*
 * This file contains b-threads for simulating the Java wrapper program. These
 * b-threads go through all possible combinations of inputs, to examine all the
 * states the controller b-program can find itself in.
 * 
 * This is a very wild environment that reqeusts all possible combinations, with
 * no logical assumptions at all. For example, it ignores model's requests to 
 * start or end active passes, and the battery voltage does not follow any pattern.
 */

// Generate all possible EPS Telemetries
var EPS_MODES = EPSTelemetry.EPSMode.values();
var possibleEPSes = [];
for ( var vBatt=0; vBatt <= 100; vBatt++ ) {
    for ( var modeIdx in EPS_MODES ) {
        for ( var activePass in [0,1] ) {
            possibleEPSes.push( EPSTelemetry(vBatt, EPS_MODES[modeIdx], (activePass===1)) );
        }
    }
}

// Generate all possible ADCS Telemetries
var ADCS_MODES = ADCSTelemetry.ADCSMode.values();
var ANGULAR_RATES = ADCSTelemetry.AngularRate.values();
var possibleADCSes = [];
for ( var adcsModeIdx in ADCS_MODES ) {
    for ( var angularRateIdx in ANGULAR_RATES ) {
        for ( var activePass in [0,1] ) {
            possibleADCSes.push( ADCSTelemetry(ADCS_MODES[adcsModeIdx], 
                                                ANGULAR_RATES[angularRateIdx],
                                                (activePass===1)) );
        }
    }
}

bp.log.info("possibleEPSes.length=" + possibleEPSes.length );
bp.log.info("possibleADCSes.length=" + possibleADCSes.length );


bp.registerBThread("Environment", function(){
    while ( true ) {
        bp.sync({request:possibleEPSes});
        bp.sync({request:possibleADCSes});
    }
});