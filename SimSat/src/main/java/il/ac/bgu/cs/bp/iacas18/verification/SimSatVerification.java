package il.ac.bgu.cs.bp.iacas18.verification;

import il.ac.bgu.cs.bp.bpjs.analysis.DfsBProgramVerifier;
import il.ac.bgu.cs.bp.bpjs.analysis.DfsTraversalNode;
import il.ac.bgu.cs.bp.bpjs.analysis.HashVisitedStateStore;
import il.ac.bgu.cs.bp.bpjs.analysis.VerificationResult;
import il.ac.bgu.cs.bp.bpjs.analysis.listeners.BriefPrintDfsVerifierListener;
import il.ac.bgu.cs.bp.bpjs.analysis.violations.Violation;
import il.ac.bgu.cs.bp.bpjs.execution.BProgramRunner;
import il.ac.bgu.cs.bp.bpjs.execution.listeners.PrintBProgramRunnerListener;
import il.ac.bgu.cs.bp.bpjs.model.BProgram;
import il.ac.bgu.cs.bp.bpjs.model.ResourceBProgram;
import il.ac.bgu.cs.bp.bpjs.model.eventselection.PrioritizedBThreadsEventSelectionStrategy;
import java.util.Arrays;
import java.util.List;

/**
 * Runs verification on SimSat.js
 * @author michael
 */
public class SimSatVerification {
    
    public static void main(String[] args) throws Exception {
        
        // Compose a greater b-program
        List<String> fullyRandom = Arrays.asList("SimSat.js", // model
            "SimSatRequirements.js", // requirement b-threads
            "FullyRandomEnvironment.js" // Simulated environment.
        );
       
        List<String> saneEnvironment = Arrays.asList("SimSat.js", // model
            "SimSatRequirements.js", // requirement b-threads
            "SaneEnvironment.js" // Simulated environment.
        );
        
        BProgram bprog = new ResourceBProgram(fullyRandom);
        
        // Make the environment run in the lowest priority, so that we get
        // supersteps in the model. This assumes that the model is much faster than
        // its environment, which is quite logical.
        PrioritizedBThreadsEventSelectionStrategy pbess = new PrioritizedBThreadsEventSelectionStrategy();
        bprog.setEventSelectionStrategy(pbess);
        int envPriority = PrioritizedBThreadsEventSelectionStrategy.DEFAULT_PRIORITY-10;
        pbess.setPriority("Environment", envPriority);
        pbess.setPriority("ExternalEventInitiator", envPriority);
        
        boolean run = false;
        
        if ( run ) {
            // sanity check
            BProgramRunner rnr = new BProgramRunner(bprog);
            rnr.addListener( new PrintBProgramRunnerListener() );
            rnr.run();
            
        } else {
            DfsBProgramVerifier vfr = new DfsBProgramVerifier();
            vfr.setMaxTraceLength(200);
            vfr.setProgressListener( new BriefPrintDfsVerifierListener() );
//            vfr.setVisitedNodeStore( new BThreadSnapshotVisitedStateStore() );
            vfr.setVisitedNodeStore( new HashVisitedStateStore() );
            
            VerificationResult result = vfr.verify(bprog);
            System.out.println("Verification done.");
            System.out.println("States scanned: " + result.getScannedStatesCount());
            System.out.printf("Time: %,d ms\n", result.getTimeMillies());
            if ( result.isViolationFound() ) {
                System.out.println("Violation found");
                Violation violation = result.getViolation().get();
                System.out.println(violation.decsribe());
                violation.getCounterExampleTrace().stream()
                    .map( DfsTraversalNode::getLastEvent )
                    .filter( e -> e != null )
                    .forEach( System.out::println );
            } else {
                System.out.println("No Violation found");
            }
        }
    }
    
}
