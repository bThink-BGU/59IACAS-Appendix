package il.ac.bgu.cs.bp.iacas18.gui;

import il.ac.bgu.cs.bp.iacas18.events.EPSTelemetry;
import il.ac.bgu.cs.bp.iacas18.events.ADCSTelemetry;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.TextArea;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
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
    public JLabel lblTime;
    public JLabel lblVBatt;
    public EnumStatusDisplay<EPSTelemetry.EPSMode> stsEpsMode;
    public EnumStatusDisplay<ADCSTelemetry.ADCSMode> stsAdcsMode;
    public EnumStatusDisplay<ADCSTelemetry.AngularRate> stsAngularRate;
    public EnumStatusDisplay<MainWindowCtrl.SimulationStatus> stsSimulationStatus;

    public JComboBox<ADCSTelemetry.AngularRate> cmbAngularRate;
    public TextArea ThelogText;
    JList logList;

    public SimSatPanel() {
        btnStartStop = new JButton("Start");
        btnPassStart = new JButton("Start");
        btnPassEnd = new JButton("End");
        btnAngRateHigh = new JButton("Set to High");
        btnAngRateLow = new JButton("Set to Low");
        lblTime = new JLabel("0");
        lblVBatt = new JLabel("-");
        stsEpsMode = new EnumStatusDisplay(EPSTelemetry.EPSMode.class);
        stsAdcsMode = new EnumStatusDisplay(ADCSTelemetry.ADCSMode.class);
        stsAngularRate = new EnumStatusDisplay(ADCSTelemetry.AngularRate.class);
        stsSimulationStatus = new EnumStatusDisplay<>(MainWindowCtrl.SimulationStatus.class);
        logList = new JList();
        ThelogText = new TextArea();

        JComponent controls = PanelMatic.begin()
                .addHeader(HeaderLevel.H3, "Control")
                .add("Simulation", btnStartStop)
                .add("Pass", lineGroup(btnPassStart, btnPassEnd))
                .add("Angular Velocity", lineGroup(btnAngRateHigh, btnAngRateLow))
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
                .addHeader(HeaderLevel.H3, "Event Log")
                .add(new JScrollPane(ThelogText, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER),
                        Modifiers.GROW)
                .get();

        JPanel top = new JPanel();
        Border margins = new EmptyBorder(2, 5, 2, 5);
        controls.setBorder(margins);
        logPanel.setBorder(margins);
        top.setLayout(new GridLayout(1, 2));
//        JSplitPane top = new JSplitPane();
        top.add(controls);
        top.add(logPanel);

        setLayout(new BorderLayout());
        add(top, BorderLayout.CENTER);
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
