/* global bp */

/**
 * Raise a false assrtion when the satellite is in
 * SetADCSModePayloadPointing and not in SetEPSModeGood
 */
bp.registerBThread("NeverPointingOnLow", function(){
  var relevantEvents = [EPSTelem, ADCSTelem];
  /* Init */
  var canPoint;
  var isPointing;
  var evt = bp.sync({waitFor:relevantEvents});
  if ( ADCSTelem.contains(evt) ) {
    isPointing = evt.mode.equals( ADCSMode.PayloadPointing );
  } else if ( EPSTelem.contains(evt) ) {
    canPoint = evt.mode.equals( EPSTelemetry.EPSMode.Good );
  }

  /* ongoing verification */
  while ( true ) {
    var evt = bp.sync({waitFor:relevantEvents});
    if ( ADCSTelem.contains(evt) ) {
      isPointing = evt.mode.equals( ADCSTelemetry.ADCSMode.PayloadPointing );
    } else if ( EPSTelem.contains(evt) ) {
      canPoint = evt.mode.equals( EPSTelemetry.EPSMode.Good );
    }
             
    bp.ASSERT(!(isPointing && !canPoint), 
             "Satellite is using the payload while it should conserve energy" );
  }
});

