package il.ac.bgu.cs.bp.iacas18.gui;

import il.ac.bgu.cs.bp.iacas18.*;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import org.panelmatic.PanelBuilder;
import org.panelmatic.PanelBuilder.HeaderLevel;
import org.panelmatic.PanelMatic;
import org.panelmatic.componentbehavior.Modifiers;

/**
 *
 * @author michael
 */
public class SimSatPanel extends JPanel {
    
    JButton btnStartStop, btnPass, btnAngVelHigh, btnAngVelLow;
    JLabel lblTime;
    JLabel lblVBatt;
    EnumStatusDisplay stsEpsMode;
    EnumStatusDisplay stsAdcsMode;
    EnumStatusDisplay stsAngularVel;
    JComboBox<ADCSTelemetry.AngularVelocity> cmbAngularVelocity;
    JList logList;
    
    public SimSatPanel() {
        btnStartStop = new JButton("Start");
        btnPass = new JButton("Start");
        btnAngVelHigh = new JButton("Set to High");
        btnAngVelLow = new JButton("Set to Low");
        lblTime = new JLabel("0");
        lblVBatt = new JLabel("-");
        stsEpsMode = new EnumStatusDisplay(EPSTelemetry.EPSMode.class);
        stsAdcsMode = new EnumStatusDisplay(ADCSTelemetry.ADCSMode.class);
        stsAngularVel = new EnumStatusDisplay(ADCSTelemetry.AngularVelocity.class);
        logList = new JList();
        
        JComponent controls = PanelMatic.begin()
            .addHeader(HeaderLevel.H2, "Status")
            .add("Simulation", btnStartStop )
            .add("Sim time", lblTime)
            .addHeader(PanelBuilder.HeaderLevel.H3, "EPS")
            .add("Battery Level", lblVBatt)
            .add("Mode", stsEpsMode)
            .addHeader(PanelBuilder.HeaderLevel.H3, "ADCS")
            .add("Mode", stsAdcsMode)
            .add("Angular Level", stsAngularVel)
            .addHeader(HeaderLevel.H2,"Interaction")
            .add("Pass", btnPass)
            .add("Angular Velocity", PanelMatic.begin().add(btnAngVelHigh).add(btnAngVelLow).get())
            .addFlexibleSpace()
            .get();
        
        JComponent logPanel = PanelMatic.begin()
            .addHeader(HeaderLevel.H2, "Event Log")
            .add( 
                new JScrollPane(logList, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER),
                Modifiers.GROW )
            .get();
        setLayout( new BorderLayout() );
        
        JPanel top = new JPanel();
        Border margins = new EmptyBorder(2,5,2,5);
        controls.setBorder(margins);
        logPanel.setBorder(margins);

        top.setLayout( new GridLayout(1,2) );
//        JSplitPane top = new JSplitPane();
        top.add( controls );
        top.add( logPanel );
        
        add( top, BorderLayout.CENTER );
    }
    
    
}
