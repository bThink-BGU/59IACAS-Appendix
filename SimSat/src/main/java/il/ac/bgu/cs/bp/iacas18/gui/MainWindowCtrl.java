package il.ac.bgu.cs.bp.iacas18.gui;

import il.ac.bgu.cs.bp.iacas18.ADCSTelemetry;
import il.ac.bgu.cs.bp.iacas18.EPSTelemetry;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

/**
 *
 * @author michael
 */
public class MainWindowCtrl {

    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(()->{
            new MainWindowCtrl().go();
        });
    }
    
    public void go() {
        JFrame frame = new JFrame("BPjs SimSat - IACAS18");
        frame.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
        
        SimSatPanel pnl = new SimSatPanel();
        
        frame.getContentPane().add(pnl);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        
        pnl.stsAdcsMode.setValue( ADCSTelemetry.ADCSMode.PayloadPointing );
        pnl.stsAngularVel.setValue( ADCSTelemetry.AngularVelocity.High );
        pnl.stsEpsMode.setValue( EPSTelemetry.EPSMode.Low );
    }
    
}
