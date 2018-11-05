package il.ac.bgu.cs.bp.iacas18.gui;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import javax.swing.table.AbstractTableModel;

/**
 * Table model for selecting which files are used in simulation/verification
 * 
 * @author michael
 */
public class FileTableModel extends AbstractTableModel {
    
    public static class FileTableRow {
        Path file;
        boolean isExecution;
        boolean isVerification;

        public FileTableRow(Path file, boolean inRun, boolean inVerification) {
            this.file = file;
            this.isExecution = inRun;
            this.isVerification = inVerification;
        }
        
    }
    
    private final List<FileTableRow> rows = new ArrayList<>();
    
    private Path currentDirectory;

    public Path getCurrentDirectory() {
        return currentDirectory;
    }

    public void setCurrentDirectory(Path currentDirectory) {
        this.currentDirectory = currentDirectory;
    }
    
    public void reloadDirectory() throws IOException {
        rows.clear();
        if ( currentDirectory != null ) {
            Files.list(currentDirectory)
                .filter( p -> p.getFileName().toString().toLowerCase().endsWith(".js"))
                .sorted((p1, p2)-> p1.getFileName().compareTo(p2.getFileName()))
                .forEach( p-> rows.add(new FileTableRow(p, true, false)) );
        }
        fireTableDataChanged();
    }
    
    
    @Override
    public int getRowCount() {
        return rows.size();
    }

    @Override
    public int getColumnCount() {
        return 3;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        FileTableRow row = rows.get(rowIndex);
        switch (columnIndex) {
            case 0: return row.file.getFileName().toString();
            case 1: return row.isExecution;
            case 2: return row.isVerification;
            default: throw new IllegalArgumentException("No value at " + rowIndex + "," + columnIndex);
        }
    }

    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        FileTableRow row = rows.get(rowIndex);
        if ( columnIndex==1 ) {
            row.isExecution = (Boolean)aValue;
        } else if ( columnIndex == 2 ) {
            row.isVerification = (Boolean)aValue;
        }
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return (columnIndex>0);
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        switch (columnIndex) {
            case 0: return String.class;
            case 1: return Boolean.class;
            case 2: return Boolean.class;
            default: return Object.class;
        }
    }

    @Override
    public String getColumnName(int columnIndex) {
        switch (columnIndex) {
            case 0: return "File";
            case 1: return "Execution";
            case 2: return "Verification";
            default: return "??";
        }
    }
    
    
    
}
