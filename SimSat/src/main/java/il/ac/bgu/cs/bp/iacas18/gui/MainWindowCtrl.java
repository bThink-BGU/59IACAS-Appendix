package il.ac.bgu.cs.bp.iacas18.gui;

import il.ac.bgu.cs.bp.iacas18.events.*;
import javax.swing.JFrame;

/**
 *
 * @author michael
 */
public class MainWindowCtrl {

    public enum SimulationStatus {
//        Off, Starting, Running, Error;
        Off, Running;
    }

    public SimulationStatus simStatus = SimulationStatus.Off;
    public ADCSTelemetry.AngularRate angularRate;
    public ADCSTelemetry.ADCSMode AdcSMode;
    public ADCSTelemetry adcTel;
    public EPSTelemetry epsTel;
    public EPSTelemetry.EPSMode EpsMode;
    private boolean inPass = false;
    public JFrame frame;
    public SimSatPanel pnl;

    public MainWindowCtrl() {
        EpsMode = EPSTelemetry.EPSMode.Critical;
        AdcSMode = ADCSTelemetry.ADCSMode.Detumbling;

        frame = new JFrame("BPjs SimSat - IACAS18");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        pnl = new SimSatPanel();

        pnl.stsSimulationStatus.setValue(SimulationStatus.Off);
        addListeners();
        frame.getContentPane().add(pnl);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        pnl.btnPassEnd.setEnabled(false);
        angularRate = ADCSTelemetry.AngularRate.High;
        pnl.stsAngularRate.setValue(ADCSTelemetry.AngularRate.High);
        pnl.btnAngRateHigh.setEnabled(false);
    }

    private void addListeners() {

        pnl.btnAngRateHigh.addActionListener(e -> {
            angularRate = ADCSTelemetry.AngularRate.High;
        });
        pnl.btnAngRateLow.addActionListener(e -> {
            angularRate = ADCSTelemetry.AngularRate.Low;
        });

    }

}
