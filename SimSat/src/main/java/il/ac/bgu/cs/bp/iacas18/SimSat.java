package il.ac.bgu.cs.bp.iacas18;

import il.ac.bgu.cs.bp.bpjs.execution.BProgramRunner;
import il.ac.bgu.cs.bp.bpjs.execution.listeners.BProgramRunnerListenerAdapter;
import il.ac.bgu.cs.bp.bpjs.execution.listeners.PrintBProgramRunnerListener;
import il.ac.bgu.cs.bp.bpjs.model.BEvent;
import il.ac.bgu.cs.bp.bpjs.model.BProgram;
import il.ac.bgu.cs.bp.bpjs.model.ResourceBProgram;
import il.ac.bgu.cs.bp.iacas18.events.ADCSTelemetry;
import il.ac.bgu.cs.bp.iacas18.events.EPSTelemetry;
import il.ac.bgu.cs.bp.iacas18.events.LocationTelemetry;
import il.ac.bgu.cs.bp.iacas18.events.StaticEvent;
import il.ac.bgu.cs.bp.iacas18.gui.MainWindowCtrl;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.Timer;
import java.util.TimerTask;
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
    
    static MainWindowCtrl windowCtrl;
    static BProgramRunner rnr;
    static Timer externalEventsTimer;
    static ResourceBProgram bprog;
    
    public static void main(String[] args) throws InterruptedException, InvocationTargetException {
        invokeAndWait(()->setupUI());
    }
    
    private static void setupUI() {
        
        windowCtrl = new MainWindowCtrl();
        
        windowCtrl.pnl.btnStartStop.addActionListener(e -> {
            switch (windowCtrl.simStatus) {
                case Off:
                    windowCtrl.pnl.btnStartStop.setText("Stop");
                    windowCtrl.pnl.eventlog.clear();
                    windowCtrl.simStatus = MainWindowCtrl.SimulationStatus.Running;
                    bprog = new ResourceBProgram("SimSat.js");
                    bprog.setWaitForExternalEvents(true);
                    rnr = new BProgramRunner(bprog);
                    addRnrListeners();
                    new Thread(()->rnr.run()).start();
                    startExternalEvents();
                    break;

                case Running:
                    if (rnr != null) {
                        rnr.halt();
                        externalEventsTimer.cancel();
                    }
                    windowCtrl.pnl.btnStartStop.setText("Start");
                    windowCtrl.simStatus = MainWindowCtrl.SimulationStatus.Off;
                    rnr = null;
                    break;

            }
            windowCtrl.pnl.stsSimulationStatus.setValue(windowCtrl.simStatus);
        });

        windowCtrl.pnl.btnAngRateHigh.addActionListener(e -> {
               windowCtrl.pnl.stsAngularRate.setValue(ADCSTelemetry.AngularRate.High);
               System.out.println("ADCS at High Angular Rate");
               windowCtrl.angularRate = ADCSTelemetry.AngularRate.High;
               windowCtrl.pnl.btnAngRateHigh.setEnabled(false);
               windowCtrl.pnl.btnAngRateLow.setEnabled(true);
               
        });
        
        windowCtrl.pnl.btnAngRateLow.addActionListener(e -> {
               windowCtrl.pnl.stsAngularRate.setValue(ADCSTelemetry.AngularRate.Low);
               System.out.println("ADCS at Low Angular Rate");
               windowCtrl.angularRate = ADCSTelemetry.AngularRate.Low;
               windowCtrl.pnl.btnAngRateHigh.setEnabled(true);
               windowCtrl.pnl.btnAngRateLow.setEnabled(false);
        });
        
        windowCtrl.pnl.projectPanel.setDirectory(new File(".").getAbsoluteFile().toPath());
        windowCtrl.pnl.projectPanel.reload();
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
                invokeLater(()->windowCtrl.pnl.addToLog(theEvent));
                
                if (theEvent.equals(StaticEvent.SetEPSModeCritical)) {
                    System.out.println("EPS Set To Critical");
                    invokeLater(()->{
                        windowCtrl.EpsMode = EPSTelemetry.EPSMode.Critical;
                        windowCtrl.pnl.stsEpsMode.setValue(EPSTelemetry.EPSMode.Critical);
                    });
                }
                if (theEvent.equals(StaticEvent.SetEPSModeLow)) {
                    System.out.println("EPS Set To Low");
                    invokeLater(()->{
                        windowCtrl.EpsMode = EPSTelemetry.EPSMode.Low;
                        windowCtrl.pnl.stsEpsMode.setValue(EPSTelemetry.EPSMode.Low);
                    });
                }
                if (theEvent.equals(StaticEvent.SetEPSModeGood)) {
                    System.out.println("EPS Set To Good");
                    invokeLater(()->{
                        windowCtrl.EpsMode = EPSTelemetry.EPSMode.Good;
                        windowCtrl.pnl.stsEpsMode.setValue(EPSTelemetry.EPSMode.Good);
                    });
                }
                if (theEvent.equals(StaticEvent.SetADCSModeDetumbling)) {
                    System.out.println("ADCS Set To Detumbling");
                    invokeLater(()->{
                        windowCtrl.AdcSMode = ADCSTelemetry.ADCSMode.Detumbling;
                        windowCtrl.pnl.stsAdcsMode.setValue(ADCSTelemetry.ADCSMode.Detumbling);
                    });
                }
                if (theEvent.equals(StaticEvent.SetADCSModeSunPointing)) {
                    System.out.println("ADCS Set To SunPointing");
                    invokeLater(()->{
                        windowCtrl.AdcSMode = ADCSTelemetry.ADCSMode.SunPointing;
                        windowCtrl.pnl.stsAdcsMode.setValue(ADCSTelemetry.ADCSMode.SunPointing);
                    });
                }
                if (theEvent.equals(StaticEvent.SetADCSModePayloadPointing)) {
                    System.out.println("ADCS Set To PayloadPointing");
                    invokeLater(()->{
                        windowCtrl.AdcSMode = ADCSTelemetry.ADCSMode.PayloadPointing;
                        windowCtrl.pnl.stsAdcsMode.setValue(ADCSTelemetry.ADCSMode.PayloadPointing);
                    });
                }
                if (theEvent.equals(StaticEvent.PassDone)) {
                    System.out.println("PassDone requested");
                    invokeLater(()->{
                        windowCtrl.pnl.btnPassStart.setEnabled(true);
                        windowCtrl.pnl.btnPassEnd.setEnabled(false);
                    });
                }
            }
        });
    }
    
    static void startExternalEvents() {
        externalEventsTimer = new Timer("Telemetries", false);
        
        TimerTask pushTelemetries = new TimerTask(){
            long j = 0;
            @Override
            public void run() {
                int v = windowCtrl.batteryLevelInput.get();
                if ( windowCtrl.autoBatteryLevel.get() ) {
                    v = 30 + (int) (Math.round(50 * Math.abs(Math.sin(Math.PI * j / 360))));
                }
                final int updatedBatteryLevel = v;
                invokeLater(()->{
                    windowCtrl.pnl.setBatteryLevel(updatedBatteryLevel);
                    windowCtrl.pnl.lblVBatt.setText(Integer.toString(updatedBatteryLevel));
                 });
                j++;
                bprog.enqueueExternalEvent(new EPSTelemetry(v, windowCtrl.EpsMode));
                bprog.enqueueExternalEvent(new ADCSTelemetry(windowCtrl.AdcSMode, windowCtrl.angularRate));
                bprog.enqueueExternalEvent( new LocationTelemetry(windowCtrl.overTarget.get()));
            }
        };
        
        externalEventsTimer.scheduleAtFixedRate(pushTelemetries, 0, TELEMETRY_INTERVAL);
        
        TimerTask clock = new TimerTask(){
            int clockTick = 1;
            
            @Override
            public void run() {
                invokeLater(()->windowCtrl.pnl.lblTime.setText(Integer.toString(clockTick)));
                clockTick++;
            }
        };
        externalEventsTimer.scheduleAtFixedRate(clock, 0, CLOCK_INTERVAL);
    }
    
}
