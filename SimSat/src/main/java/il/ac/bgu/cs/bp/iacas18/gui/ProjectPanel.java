package il.ac.bgu.cs.bp.iacas18.gui;

import java.awt.BorderLayout;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;

/**
 * A panel with the UI for selecting files to be run
 * in the model.
 * 
 * @author michael
 */
public class ProjectPanel extends JPanel {
    
   
    JFileChooser jfcProjectFolder = new JFileChooser();
    FileTableModel tableModel = new FileTableModel();
    JTable table = new JTable(tableModel);
    Path currentPath = (new File("")).toPath().getParent();
    JButton btnSelectDir = new JButton("...");
    
    public ProjectPanel() {
        setLayout( new BorderLayout() );
        add( btnSelectDir, BorderLayout.NORTH );
        table.setCellSelectionEnabled(false);
        table.setRowSelectionAllowed(false);
        jfcProjectFolder.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        
        JScrollPane scr = new JScrollPane(table, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        add( scr, BorderLayout.CENTER );
        
        btnSelectDir.addActionListener(e -> {
            if ( currentPath != null ) {
                jfcProjectFolder.setCurrentDirectory(currentPath.toFile());
            }
            int res = jfcProjectFolder.showDialog(null, "Choose Folder");
            if ( res == JFileChooser.APPROVE_OPTION ) {
                setDirectory(jfcProjectFolder.getCurrentDirectory().toPath());
                reload();
            }
            
        });
    }
    
    public void setDirectory( Path aDirectory ) {
        currentPath = aDirectory;
        tableModel.setCurrentDirectory(currentPath);
    }
    
    public void reload() {
        try {
            tableModel.reloadDirectory();
            btnSelectDir.setText(currentPath.toString());
        } catch (IOException ex) {
            Logger.getLogger(ProjectPanel.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}

