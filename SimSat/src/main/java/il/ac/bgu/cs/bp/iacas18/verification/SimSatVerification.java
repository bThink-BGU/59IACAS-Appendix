package il.ac.bgu.cs.bp.iacas18.verification;

import il.ac.bgu.cs.bp.bpjs.execution.BProgramRunner;
import il.ac.bgu.cs.bp.bpjs.execution.listeners.PrintBProgramRunnerListener;
import il.ac.bgu.cs.bp.bpjs.model.BProgram;
import il.ac.bgu.cs.bp.bpjs.model.ResourceBProgram;
import il.ac.bgu.cs.bp.bpjs.model.eventselection.PrioritizedBThreadsEventSelectionStrategy;

/**
 * Runs verification on SimSat.js
 * @author michael
 */
public class SimSatVerification {
    
    public static void main(String[] args) {
        
        // Compose a greater b-program
        BProgram bprog = new ResourceBProgram(
            "SimSat.js", // model
            "SimSatRequirements.js", // requirement b-threads
            "SimulatedEnvironment.js" // Simulated environment.
        );
        
        // Make the environment run in the lowest priority, so that we get
        // supersteps in the model. This assumes that the model is much faster than
        // its environment, which is quite logical.
        PrioritizedBThreadsEventSelectionStrategy pbess = new PrioritizedBThreadsEventSelectionStrategy();
        bprog.setEventSelectionStrategy(pbess);
        pbess.setPriority("Environment", PrioritizedBThreadsEventSelectionStrategy.DEFAULT_PRIORITY-10);
        
        // sanity check - un-comment to run and look at the log.
        BProgramRunner rnr = new BProgramRunner(bprog);
        rnr.addListener( new PrintBProgramRunnerListener() );
        rnr.run();
    }
    
}
