package il.ac.bgu.cs.bp.iacas18.gui;

import il.ac.bgu.cs.bp.bpjs.model.BEvent;
import il.ac.bgu.cs.bp.iacas18.events.EPSTelemetry;
import il.ac.bgu.cs.bp.iacas18.events.ADCSTelemetry;
import il.ac.bgu.cs.bp.iacas18.events.LocationTelemetry;
import il.ac.bgu.cs.bp.iacas18.events.StaticEvent;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.HashMap;
import java.util.Map;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSlider;
import javax.swing.ListCellRenderer;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import org.panelmatic.PanelBuilder.HeaderLevel;
import org.panelmatic.PanelMatic;
import org.panelmatic.componentbehavior.Modifiers;
import static org.panelmatic.util.Groupings.lineGroup;

/**
 *
 * @author michael
 */
public class SimSatPanel extends JPanel {

    public JButton btnStartStop, btnPassStart, btnPassEnd,
            btnAngRateHigh, btnAngRateLow, btnSaveLog;
    public JSlider sldBatteryLevel;
    public JCheckBox chkAutoBatteryLevel;
    public JLabel lblTime;
    public JLabel lblVBatt;
    public EnumStatusDisplay<EPSTelemetry.EPSMode> stsEpsMode;
    public EnumStatusDisplay<ADCSTelemetry.ADCSMode> stsAdcsMode;
    public EnumStatusDisplay<ADCSTelemetry.AngularRate> stsAngularRate;
    public EnumStatusDisplay<MainWindowCtrl.SimulationStatus> stsSimulationStatus;

    public JComboBox<ADCSTelemetry.AngularRate> cmbAngularRate;
    JList logList;
    public final DefaultListModel<BEvent> eventlog = new DefaultListModel<>();

    public SimSatPanel() {
        
        sldBatteryLevel = new JSlider(JSlider.HORIZONTAL, 0, 100, 50);
        sldBatteryLevel.setPaintLabels(true);
        sldBatteryLevel.setPaintTicks(true);
        sldBatteryLevel.setEnabled(false);
        chkAutoBatteryLevel = new JCheckBox("Auto", true);
        JLabel lblBattery = new JLabel("  0");
        lblBattery.setFont( new Font(Font.MONOSPACED, Font.PLAIN, 12));
        
        JPanel pnlBattery = new JPanel(new BorderLayout());
        pnlBattery.add(chkAutoBatteryLevel, BorderLayout.WEST);
        pnlBattery.add(sldBatteryLevel, BorderLayout.CENTER);
        pnlBattery.add(lblBattery, BorderLayout.EAST);
        lblBattery.setText("0");
        
        btnStartStop = new JButton("Start");
        btnPassStart = new JButton("Start");
        btnPassEnd = new JButton("End");
        btnAngRateHigh = new JButton("Set to High");
        btnAngRateLow = new JButton("Set to Low");
        btnSaveLog = new JButton("Save...");
        
        lblTime = new JLabel("0");
        lblVBatt = new JLabel("-");
        stsEpsMode = new EnumStatusDisplay(EPSTelemetry.EPSMode.class);
        stsAdcsMode = new EnumStatusDisplay(ADCSTelemetry.ADCSMode.class);
        stsAngularRate = new EnumStatusDisplay(ADCSTelemetry.AngularRate.class);
        stsSimulationStatus = new EnumStatusDisplay<>(MainWindowCtrl.SimulationStatus.class);
        logList = new JList(eventlog);
        logList.setCellRenderer(new EventRenderer());
       

        JComponent controls = PanelMatic.begin()
                .addHeader(HeaderLevel.H3, "Control")
                .add("Simulation", btnStartStop)
                .add("Pass", lineGroup(btnPassStart, btnPassEnd))
                .add("Angular Velocity", lineGroup(btnAngRateHigh, btnAngRateLow))
                .add("Battery Level", pnlBattery )
                .add(new JSeparator())
                .addHeader(HeaderLevel.H3, "Status")
                .add("Simulation Status", stsSimulationStatus)
                .add("Simulation Time", dataLabel(lblTime))
                .add(new JSeparator())
                .add("Battery Level", dataLabel(lblVBatt))
                .add("Mode", stsEpsMode)
                .add(new JSeparator())
                .add("Mode", stsAdcsMode)
                .add("Angular Rate", stsAngularRate)
                .addFlexibleSpace()
                .get();

        JComponent logPanel = PanelMatic.begin()
                .addHeader(HeaderLevel.H3, "Event Log", btnSaveLog)
                .add(new JScrollPane(logList, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER),
                        Modifiers.GROW)
                .get();

        JPanel top = new JPanel();
        Border margins = new EmptyBorder(2, 5, 2, 5);
        controls.setBorder(margins);
        logPanel.setBorder(margins);
        top.setLayout(new GridLayout(1, 2));
        top.add(controls);
        top.add(logPanel);
        
        setLayout(new BorderLayout());
        add(PanelMatic.begin()
            .addHeader(HeaderLevel.H1, "Satellite Simulator, BP Group@BGU")
            .add( new JSeparator() )
            .add( top, Modifiers.GROW )
            .get(), BorderLayout.CENTER);
        
        
        sldBatteryLevel.addChangeListener(c->{
            String n = Integer.toString(sldBatteryLevel.getValue());
             n = ("   "+n);
            lblBattery.setText( n.substring(n.length()-3, n.length()) );
        });
        chkAutoBatteryLevel.addChangeListener(lc->{
            sldBatteryLevel.setEnabled( ! chkAutoBatteryLevel.isSelected() );
        });
    }
    
    public void setBatteryLevel( int i ) {
        sldBatteryLevel.setValue(i);
        lblVBatt.setText( Integer.toString(i) );
    }
    
    public int getBatteryLevel() {
        return sldBatteryLevel.getValue();
    }
    
    public void addToLog( BEvent be ) {
        eventlog.addElement(be);
        int size = eventlog.getSize();
        logList.scrollRectToVisible(logList.getCellBounds(size-1, size));
    }

    private JLabel dataLabel(JLabel in) {
        in.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 14));
        in.setOpaque(true);
        in.setBackground(Color.GRAY);
        in.setForeground(new Color(200, 255, 200));
        in.setHorizontalAlignment(SwingConstants.CENTER);
        in.setBorder(new EmptyBorder(2, 5, 2, 5));
        return in;
    }

}


class EventRenderer implements ListCellRenderer<BEvent> {
    
    JPanel pnl = new JPanel();
    JLabel eventNameLbl = new JLabel();
    JLabel eventDataLbl = new JLabel();
    Map<Class,Color> colors = new HashMap<>();
    Color defaultColor = new Color(255,255,100);
    
    public EventRenderer() {
        Font fnt = new Font(Font.MONOSPACED, Font.PLAIN, 12);
        eventNameLbl.setFont( fnt );
        eventDataLbl.setFont( fnt );
        eventNameLbl.setPreferredSize(new Dimension(100, 20));
        pnl.setLayout( new BorderLayout() );
        pnl.add( eventNameLbl, BorderLayout.WEST );
        pnl.add( eventDataLbl, BorderLayout.CENTER );
        pnl.setBorder( new EmptyBorder(2,4,2,4) );
        colors.put(ADCSTelemetry.class, new Color(220,220,230));
        colors.put(EPSTelemetry.class, new Color(210,210,220));
        colors.put(LocationTelemetry.class, new Color(200,200,220));
        colors.put(StaticEvent.class, new Color(210,255,210));
    }
    
    @Override
    public Component getListCellRendererComponent(JList list, BEvent value, int index, boolean isSelected, boolean cellHasFocus) {
        pnl.setBackground( isSelected ? list.getSelectionBackground() : colors.getOrDefault(value.getClass(), defaultColor) );
        eventNameLbl.setForeground( isSelected ? list.getSelectionForeground() : Color.black );
        eventDataLbl.setForeground( isSelected ? list.getSelectionForeground() : Color.gray );
        String eventString = value.toString();
        if ( eventString.startsWith("[") && eventString.endsWith("]")) {
            eventString = eventString.substring(1, eventString.length()-1);
        }
        String[] comps = eventString.split(" ", 2);
       
        eventNameLbl.setText(comps[0]);
        eventDataLbl.setText(comps[1]);
        pnl.setPreferredSize( new Dimension(list.getWidth(), 25));
        return pnl;
    }
    
}