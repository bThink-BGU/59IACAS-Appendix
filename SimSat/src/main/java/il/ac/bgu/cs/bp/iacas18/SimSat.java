package il.ac.bgu.cs.bp.iacas18;

import il.ac.bgu.cs.bp.bpjs.execution.BProgramRunner;
import il.ac.bgu.cs.bp.bpjs.execution.listeners.BProgramRunnerListenerAdapter;
import il.ac.bgu.cs.bp.bpjs.execution.listeners.PrintBProgramRunnerListener;
import il.ac.bgu.cs.bp.bpjs.model.BEvent;
import il.ac.bgu.cs.bp.bpjs.model.BProgram;
import il.ac.bgu.cs.bp.bpjs.model.SingleResourceBProgram;
import il.ac.bgu.cs.bp.iacas18.events.ADCSTelemetry;
import il.ac.bgu.cs.bp.iacas18.events.EPSTelemetry;
import il.ac.bgu.cs.bp.iacas18.events.StaticEvents;
import il.ac.bgu.cs.bp.iacas18.gui.MainWindowCtrl;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Runs the satellite BP logic, and pushes external events to simulate external
 * systems.
 *
 * @author michael
 */
public class SimSat {

    public static MainWindowCtrl guir;

    public static void main(String[] args) throws InterruptedException {

        final SingleResourceBProgram bprog
                = new SingleResourceBProgram("SimSat.js");

        BProgramRunner rnr = new BProgramRunner(bprog);
        bprog.setDaemonMode(true);
        // Print program events to the console
        rnr.addListener(new PrintBProgramRunnerListener());
        guir = new MainWindowCtrl();

        Thread EPSTelemThread = new Thread(() -> {
            int j = 0;
            int v = 50;
            while (true) {
                try {
                    Thread.sleep(400);
                    v = (int) (30 + Math.round(50 * Math.abs(Math.sin(Math.PI * j / 360))));
                    guir.pnl.lblVBatt.setText(new String().valueOf(v));
                    j++;
                    bprog.enqueueExternalEvent(new EPSTelemetry(v, guir.EpsMode));
                    bprog.enqueueExternalEvent(new ADCSTelemetry(guir.AdcSMode, guir.angularRate));

                } catch (InterruptedException ex) {
                    Logger.getLogger(SimSat.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
        Thread tmrThread = new Thread(() -> {
            int i = 1;
            while (true) {
                try {
                    Thread.sleep(200);
                    guir.pnl.lblTime.setText(new String().valueOf(i));
                    i++;

                } catch (InterruptedException ex) {
                    Logger.getLogger(SimSat.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });

        guir.pnl.btnStartStop.addActionListener(e -> {
            switch (guir.simStatus) {
                case Off:
                    guir.pnl.btnStartStop.setText("Running");
                    guir.pnl.btnStartStop.setEnabled(false);
                    guir.simStatus = MainWindowCtrl.SimulationStatus.Running;
                    tmrThread.start();
                    EPSTelemThread.start();
                    break;
//                case Error:
//                    // Start the simulation
//
//                    // TODO kick the simulation off
//                    break;

                case Running:
                    if (rnr != null) {
                        // TODO make runner stop
                    }
                    guir.pnl.btnStartStop.setText("Start");
                    guir.simStatus = MainWindowCtrl.SimulationStatus.Off;
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
        
        
        rnr.addListener(new BProgramRunnerListenerAdapter() {

            @Override

            public void eventSelected(BProgram bp, BEvent theEvent) {
                guir.pnl.ThelogText.append(theEvent.toString() + "\n");

                if (theEvent.equals(StaticEvents.SetEPSModeCritical)) {
                    System.out.println("EPS Set To Critical");
                    guir.EpsMode = EPSTelemetry.EPSMode.Critical;
                    guir.pnl.stsEpsMode.setValue(EPSTelemetry.EPSMode.Critical);
                }
                if (theEvent.equals(StaticEvents.SetEPSModeLow)) {
                    System.out.println("EPS Set To Low");
                    guir.EpsMode = EPSTelemetry.EPSMode.Low;
                    guir.pnl.stsEpsMode.setValue(EPSTelemetry.EPSMode.Low);
                }
                if (theEvent.equals(StaticEvents.SetEPSModeGood)) {
                    System.out.println("EPS Set To Good");
                    guir.EpsMode = EPSTelemetry.EPSMode.Good;
                    guir.pnl.stsEpsMode.setValue(EPSTelemetry.EPSMode.Good);
                }
                if (theEvent.equals(StaticEvents.SetADCSModeDetumbling)) {
                    System.out.println("ADCS Set To Detumbling");
                    guir.AdcSMode = ADCSTelemetry.ADCSMode.Detumbling;
                    guir.pnl.stsAdcsMode.setValue(ADCSTelemetry.ADCSMode.Detumbling);
                }
                if (theEvent.equals(StaticEvents.SetADCSModeSunPointing)) {
                    System.out.println("ADCS Set To SunPointing");
                    guir.AdcSMode = ADCSTelemetry.ADCSMode.SunPointing;
                    guir.pnl.stsAdcsMode.setValue(ADCSTelemetry.ADCSMode.SunPointing);
                }
                if (theEvent.equals(StaticEvents.SetADCSModePayloadPointing)) {
                    System.out.println("ADCS Set To PayloadPointing");
                    guir.AdcSMode = ADCSTelemetry.ADCSMode.PayloadPointing;
                    guir.pnl.stsAdcsMode.setValue(ADCSTelemetry.ADCSMode.PayloadPointing);
                }
            }
        });
        rnr.run();
        // go!

    }
}
