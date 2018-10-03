package il.ac.bgu.cs.bp.iacas18.gui;

import il.ac.bgu.cs.bp.bpjs.execution.BProgramRunner;
import il.ac.bgu.cs.bp.bpjs.model.BEvent;
import il.ac.bgu.cs.bp.iacas18.ADCSTelemetry;
import java.awt.event.ActionEvent;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

/**
 *
 * @author michael
 */
public class MainWindowCtrl {
    
    public enum SimulationStatus {
        Off, Starting, Running, Error;
    }
    
    SimulationStatus simStatus = SimulationStatus.Off;
    ADCSTelemetry.AngularVelocity angularVel = ADCSTelemetry.AngularVelocity.High;
    private boolean inPass = false;
    SimSatPanel pnl;
    BProgramRunner rnr;
    
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(()->{
            new MainWindowCtrl().go();
        });
    }
    
    public void go() {
        JFrame frame = new JFrame("BPjs SimSat - IACAS18");
        frame.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
        
        pnl = new SimSatPanel();
        
        pnl.stsSimulationStatus.setValue(SimulationStatus.Off);
        addListeners();
        
        frame.getContentPane().add(pnl);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);   
    }
    
    private void addListeners() {
        pnl.btnStartStop.addActionListener((ActionEvent e) -> {
            simulationBtnClicked();
        });
        pnl.btnAngVelHigh.addActionListener(e->{
            angularVel = ADCSTelemetry.AngularVelocity.High;
        });
        pnl.btnAngVelLow.addActionListener(e->{
            angularVel = ADCSTelemetry.AngularVelocity.Low;
        });
        pnl.btnPassStart.addActionListener(e->{
            if ( rnr != null ) {
                rnr.getBProgram().enqueueExternalEvent(new BEvent("ActivePass"));
            }
        });
        pnl.btnPassEnd.addActionListener(e->{
            if ( rnr != null ) {
                rnr.getBProgram().enqueueExternalEvent(new BEvent("PassDone"));
            }
        });
    }
    
    private void simulationBtnClicked() {
        switch ( simStatus ) {
            case Off:
            case Error:
                // Start the simulation
                pnl.btnStartStop.setText("Stop");
                simStatus = SimulationStatus.Starting;
                // TODO kick the simulation off
                break;
                
            case Running:
                if ( rnr != null ) {
                    // TODO make runner stop
                }
                pnl.btnStartStop.setText("Start");
                simStatus = SimulationStatus.Off;
                break;
                
            case Starting:
                // ignore
                break;
        }
        pnl.stsSimulationStatus.setValue(simStatus);
    }
    
}
