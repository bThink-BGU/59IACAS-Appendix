package il.ac.bgu.cs.bp.iacas18.gui;

import il.ac.bgu.cs.bp.bpjs.model.BEvent;
import il.ac.bgu.cs.bp.iacas18.events.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import javax.swing.JFileChooser;
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
    public final AtomicInteger batteryLevelInput = new AtomicInteger();
    public final AtomicBoolean autoBatteryLevel = new AtomicBoolean(true);
    File previousDir = null;

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
        pnl.btnSaveLog.addActionListener( e->saveEventLog() );
        pnl.sldBatteryLevel.addChangeListener(e->{
            batteryLevelInput.set(pnl.getBatteryLevel());
        });
        pnl.chkAutoBatteryLevel.addChangeListener(e->{
            autoBatteryLevel.set(pnl.chkAutoBatteryLevel.isSelected());
        });
        
    }
    
    private boolean saveEventLog() {
       
        JFileChooser csr = new JFileChooser();
        if ( previousDir != null ) {
            csr.setCurrentDirectory(previousDir);
        }
        csr.showSaveDialog(null);
        if ( csr.getSelectedFile() != null ) {
            System.out.println(csr.getSelectedFile());
            previousDir = csr.getCurrentDirectory();
            final List<String> toWrite = new ArrayList<>(pnl.eventlog.size());
            Enumeration<BEvent> evts = pnl.eventlog.elements();
            
            while ( evts.hasMoreElements() ) {
                BEvent evt = evts.nextElement();
                toWrite.add( evt.toString() );
            }
            
            new Thread( ()->{
                try {
                    Files.write(csr.getSelectedFile().toPath(), toWrite);
                } catch (IOException iox) {
                    iox.printStackTrace(System.out);
                        
                }
            }).start();
        }
        return true;
    }

}
