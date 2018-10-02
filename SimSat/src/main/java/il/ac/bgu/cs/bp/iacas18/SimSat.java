package il.ac.bgu.cs.bp.iacas18;

import il.ac.bgu.cs.bp.bpjs.execution.BProgramRunner;
import il.ac.bgu.cs.bp.bpjs.execution.listeners.PrintBProgramRunnerListener;
import il.ac.bgu.cs.bp.bpjs.model.SingleResourceBProgram;


/**
 * Runs the satellite BP logic, and pushes external events to simulate external
 * systems.
 * 
 * @author michael
 */
public class SimSat {
    
    public static void main(String[] args) throws InterruptedException {
        final SingleResourceBProgram bprog = 
                   new SingleResourceBProgram("SimSat.js");
        
        BProgramRunner rnr = new BProgramRunner(bprog);

        // Print program events to the console
        rnr.addListener(new PrintBProgramRunnerListener() );
        
        // go!
        rnr.run();
    }
    
}
