/* global bp, EPSTelem, EPSTelemetry */

/**
 * Raise a false assrtion when the satellite is in
 * SetADCSModePayloadPointing and not in SetEPSModeGood
 */

var USE_PAYLOAD = bp.Event("SetADCSModePayloadPointing");

bp.registerBThread("NeverPointingOnLow", function () {
    var relevantEvents = [EPSTelem, USE_PAYLOAD];
    /* Init */
    var canPoint;
    var evt = bp.sync({waitFor: relevantEvents});
    if (EPSTelem.contains(evt)) {
        canPoint = evt.mode.equals(EPSTelemetry.EPSMode.Good);
    }

    /* ongoing verification */
    while (true) {
        var pointingRequested = false;
        var evt = bp.sync({waitFor: relevantEvents});
        if (EPSTelem.contains(evt)) {
            canPoint = evt.mode.equals(EPSTelemetry.EPSMode.Good);
        } else if (USE_PAYLOAD.equals(evt)) {
            pointingRequested = true;
        }

        bp.ASSERT(!(pointingRequested && !canPoint),
                "Satellite is using the payload while it should conserve energy");
    }
});

