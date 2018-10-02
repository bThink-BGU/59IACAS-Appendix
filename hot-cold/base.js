var COLD = bp.Event("add-cold");
var HOT  = bp.Event("add-hot");

bp.registerBThread("add-hot", function(){
  bsync({request:HOT});
  bsync({request:HOT});
  bsync({request:HOT});
});
bp.registerBThread("add-cold", function(){
  bsync({request:COLD});
  bsync({request:COLD});
  bsync({request:COLD});
});
