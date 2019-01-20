package il.ac.bgu.cs.bp.iacas18;

import il.ac.bgu.cs.bp.bpjs.execution.BProgramRunner;
import il.ac.bgu.cs.bp.bpjs.execution.listeners.BProgramRunnerListenerAdapter;
import il.ac.bgu.cs.bp.bpjs.execution.listeners.PrintBProgramRunnerListener;
import il.ac.bgu.cs.bp.bpjs.model.BEvent;
import il.ac.bgu.cs.bp.bpjs.model.BProgram;
import il.ac.bgu.cs.bp.bpjs.model.ResourceBProgram;
import il.ac.bgu.cs.bp.iacas18.events.ADCSTelemetry;
import il.ac.bgu.cs.bp.iacas18.events.EPSTelemetry;
import il.ac.bgu.cs.bp.iacas18.events.StaticEvents;
import il.ac.bgu.cs.bp.iacas18.gui.MainWindowCtrl;
import java.lang.reflect.InvocationTargetException;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;
import static javax.swing.SwingUtilities.invokeAndWait;
import static javax.swing.SwingUtilities.invokeLater;

/**
 * Runs the satellite BP logic, and pushes external events to simulate external
 * systems.
 *
 * @author michael
 */
public class SimSat {

    private static final int TELEMETRY_INTERVAL = 100; // msec
    private static final int CLOCK_INTERVAL = 50; // msec
    
    static MainWindowCtrl guir;
    static BProgramRunner rnr;
    static Timer externalEventsTimer;
    static ResourceBProgram bprog;
    static final AtomicBoolean isGo = new AtomicBoolean(true);
    static final AtomicBoolean isActivePass = new AtomicBoolean(false);
    
    public static void main(String[] args) throws InterruptedException, InvocationTargetException {
        invokeAndWait(()->setupUI());
    }
    
    private static class StopException extends RuntimeException {}
    
    private static void setupUI() {
        
        guir = new MainWindowCtrl();
        
        guir.pnl.btnStartStop.addActionListener(e -> {
            switch (guir.simStatus) {
                case Off:
                    guir.pnl.btnStartStop.setText("Stop");
                    guir.pnl.eventlog.clear();
                    guir.simStatus = MainWindowCtrl.SimulationStatus.Running;
                    bprog = new ResourceBProgram("SimSat.js");
                    bprog.setWaitForExternalEvents(true);
                    isGo.set(true);
                    rnr = new BProgramRunner(bprog);
                    addRnrListeners();
                    new Thread(()->rnr.run()).start();
                    startExternalEvents();
                    break;
//                case Error:
//                    // Start the simulation
//
//                    // TODO kick the simulation off
//                    break;

                case Running:
                    if (rnr != null) {
                        isGo.set(false);
                        externalEventsTimer.cancel();
                    }
                    guir.pnl.btnStartStop.setText("Start");
                    guir.simStatus = MainWindowCtrl.SimulationStatus.Off;
                    rnr = null;
                    break;

//                case Starting:
//                    guir.simStatus = MainWindowCtrl.SimulationStatus.Running;
//
//                    break;
            }
            guir.pnl.stsSimulationStatus.setValue(guir.simStatus);
        });

        guir.pnl.btnPassStart.addActionListener(e -> {
            bprog.enqueueExternalEvent(StaticEvents.ActivePass);
            guir.pnl.btnPassStart.setEnabled(false);
            guir.pnl.btnPassEnd.setEnabled(true);
        });

        guir.pnl.btnPassEnd.addActionListener(e -> {
            if (rnr != null) {
                rnr.getBProgram().enqueueExternalEvent(StaticEvents.PassDone);
                guir.pnl.btnPassStart.setEnabled(true);
                guir.pnl.btnPassEnd.setEnabled(false);
            }
        });
        
        guir.pnl.btnAngRateHigh.addActionListener(e -> {
               guir.pnl.stsAngularRate.setValue(ADCSTelemetry.AngularRate.High);
               System.out.println("ADCS at High Angular Rate");
               guir.angularRate = ADCSTelemetry.AngularRate.High;
               guir.pnl.btnAngRateHigh.setEnabled(false);
               guir.pnl.btnAngRateLow.setEnabled(true);
               
        });
        
        guir.pnl.btnAngRateLow.addActionListener(e -> {
               guir.pnl.stsAngularRate.setValue(ADCSTelemetry.AngularRate.Low);
               System.out.println("ADCS at Low Angular Rate");
               guir.angularRate = ADCSTelemetry.AngularRate.Low;
               guir.pnl.btnAngRateHigh.setEnabled(true);
               guir.pnl.btnAngRateLow.setEnabled(false);
        });
    }
    
    /**
     * Add the listener that reports back from the running b-program to the GUI
     * and to System.out.
     */
    static void addRnrListeners(){
        rnr.addListener(new PrintBProgramRunnerListener());
        rnr.addListener(new BProgramRunnerListenerAdapter() {

            @Override
            public void eventSelected(BProgram bp, BEvent theEvent) {
                invokeLater(()->guir.pnl.addToLog(theEvent));
                
                if ( ! isGo.get() ) throw new StopException();
                
                if (theEvent.equals(StaticEvents.SetEPSModeCritical)) {
                    System.out.println("EPS Set To Critical");
                    invokeLater( ()->{
                        guir.EpsMode = EPSTelemetry.EPSMode.Critical;
                        guir.pnl.stsEpsMode.setValue(EPSTelemetry.EPSMode.Critical);
                    });
                }
                if (theEvent.equals(StaticEvents.SetEPSModeLow)) {
                    System.out.println("EPS Set To Low");
                    invokeLater( ()->{
                        guir.EpsMode = EPSTelemetry.EPSMode.Low;
                        guir.pnl.stsEpsMode.setValue(EPSTelemetry.EPSMode.Low);
                    });
                }
                if (theEvent.equals(StaticEvents.SetEPSModeGood)) {
                    System.out.println("EPS Set To Good");
                    invokeLater( ()->{
                        guir.EpsMode = EPSTelemetry.EPSMode.Good;
                        guir.pnl.stsEpsMode.setValue(EPSTelemetry.EPSMode.Good);
                    });
                }
                if (theEvent.equals(StaticEvents.SetADCSModeDetumbling)) {
                    System.out.println("ADCS Set To Detumbling");
                    invokeLater( ()->{
                        guir.AdcSMode = ADCSTelemetry.ADCSMode.Detumbling;
                        guir.pnl.stsAdcsMode.setValue(ADCSTelemetry.ADCSMode.Detumbling);
                    });
                }
                if (theEvent.equals(StaticEvents.SetADCSModeSunPointing)) {
                    System.out.println("ADCS Set To SunPointing");
                    invokeLater( ()->{
                        guir.AdcSMode = ADCSTelemetry.ADCSMode.SunPointing;
                        guir.pnl.stsAdcsMode.setValue(ADCSTelemetry.ADCSMode.SunPointing);
                    });
                }
                if (theEvent.equals(StaticEvents.SetADCSModePayloadPointing)) {
                    System.out.println("ADCS Set To PayloadPointing");
                    invokeLater( ()->{
                        guir.AdcSMode = ADCSTelemetry.ADCSMode.PayloadPointing;
                        guir.pnl.stsAdcsMode.setValue(ADCSTelemetry.ADCSMode.PayloadPointing);
                    });
                }
                if (theEvent.equals(StaticEvents.PassDone)) {
                    System.out.println("PassDone requested");
                    isActivePass.set(false);
                    invokeLater( ()->{
                        guir.pnl.btnPassStart.setEnabled(true);
                        guir.pnl.btnPassEnd.setEnabled(false);
                    });
                }
                if ( theEvent.equals(StaticEvents.ActivePass) ) {
                    isActivePass.set(true);
                }
            }
        });
    }
    
    static void startExternalEvents() {
        externalEventsTimer = new Timer("Telemetries", false);
        
        TimerTask pushTelemetries = new TimerTask(){
            long j = 0;
            int v = 50;
            @Override
            public void run() {
                    v = (int) (30 + Math.round(50 * Math.abs(Math.sin(Math.PI * j / 360))));
                    invokeLater(()->guir.pnl.lblVBatt.setText(Integer.toString(v)));
                    j++;
                    bprog.enqueueExternalEvent(new EPSTelemetry(v, guir.EpsMode, isActivePass.get()));
                    bprog.enqueueExternalEvent(new ADCSTelemetry(guir.AdcSMode, guir.angularRate, isActivePass.get()));
            }
        };
        
        externalEventsTimer.scheduleAtFixedRate(pushTelemetries, 0, TELEMETRY_INTERVAL);
        
        TimerTask clock = new TimerTask(){
            int clockTick = 1;
            
            @Override
            public void run() {
                invokeLater(()->guir.pnl.lblTime.setText(Integer.toString(clockTick)));
                clockTick++;
            }
        };
        externalEventsTimer.scheduleAtFixedRate(clock, 0, CLOCK_INTERVAL);
    }
    
}
