package il.ac.bgu.cs.bp.iacas18.gui;

import java.awt.Color;
import java.awt.GridLayout;
import java.util.EnumMap;
import java.util.Map;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import javax.swing.border.BevelBorder;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;

/**
 *
 * @author michael
 */
public class EnumStatusDisplay<T extends Enum> extends JComponent {
    
    private final Class<T> presentedEnum;
    
    private T currentValue;
    
    private final Map<T,JLabel> labels;
    
    public EnumStatusDisplay( Class<T> enumClz ) {
        presentedEnum = enumClz;
        labels = new EnumMap<>(enumClz);
        setup();
        updateStyles();
    }
    
    public void setValue( T newValue ) {
        currentValue = newValue;
        updateStyles();
    }
    
    private void setup() {
        T[] enumConstants = presentedEnum.getEnumConstants();
        GridLayout gridLayout = new GridLayout(1, enumConstants.length);
        gridLayout.setHgap(4);
        setLayout(gridLayout);
        
        Border brd = new BevelBorder(BevelBorder.LOWERED);
        brd = new CompoundBorder(brd, new EmptyBorder(4,4,4,4));
        
        for ( T e : enumConstants ) {
            JLabel lbl = new JLabel( e.name() );
            lbl.setOpaque(true);
            
            lbl.setHorizontalAlignment(SwingConstants.CENTER);
            labels.put(e, lbl);
            lbl.setBorder(brd);
            add(lbl);
        }
    }
    
    private void updateStyles() {
        labels.entrySet().forEach( es -> updateStyle(es.getValue(), es.getKey()==currentValue));
    }
    
    private void updateStyle( JLabel lbl, boolean isSelected ) {
        lbl.setBackground( isSelected ? new Color(220, 255, 230) : new Color(100,120,100) );
        lbl.setForeground( isSelected ? new Color(0, 0, 0) : new Color(190,200,190) );
    }
}
