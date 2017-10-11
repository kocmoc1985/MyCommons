/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package swing;

import Exceptions.TableNameNotSpecifiedException;
import java.awt.Color;
import java.awt.Component;
import java.awt.Desktop;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.event.ChangeEvent;
import javax.swing.event.TableColumnModelListener;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

/**
 *
 * @author KOCMOC
 */
public class JTableM extends JTable implements TableColumnModelListener {

    private String TABLE_NAME;
    private boolean SAVE_COL_WIDTHS;
    private ArrayList COL_WIDTH_LIST_SAVE;
    private String COL_WIDTH_LIST_FILE_NAME;
    private final static int NOT_LESS_THEN = 100;
    private boolean SAVE_ALLOWED = false;
    private int INITIAL_TIMEOUT = 200;
    private ArrayList<JTable> SYNC_TABLES_LIST;

    /**
     * The basic one
     *
     * @param tableName
     * @param saveColWidths
     */
    public JTableM(String tableName, boolean saveColWidths) {
        basic(tableName, saveColWidths);
    }

    public void addSyncTable(JTable table) {
        this.SYNC_TABLES_LIST.add(table);
    }

    private void basic(String tableName, boolean saveColWidths) {
        //
        this.TABLE_NAME = tableName;
        //
        this.SAVE_COL_WIDTHS = saveColWidths;
        //
        COL_WIDTH_LIST_FILE_NAME = "col_widths_save__" + TABLE_NAME;
        //
        try {
            colWidthsRestoreInit();
        } catch (TableNameNotSpecifiedException ex) {
            Logger.getLogger(JTableM.class.getName()).log(Level.SEVERE, null, ex);
        }
        //
    }

    public String getTABLE_NAME() {
        return TABLE_NAME;
    }

    //==========================================================================
    private void colWidthsRestoreInit() throws TableNameNotSpecifiedException {
        //
        if (TABLE_NAME == null || TABLE_NAME.isEmpty()) {
            throw new TableNameNotSpecifiedException();
        }
        //
        if (SAVE_COL_WIDTHS) {
            Thread x = new Thread(new ColumnCountWatcher());
            x.start();
        }
    }

    private void restore() {
        COL_WIDTH_LIST_SAVE = restoreSaveListFromObject(COL_WIDTH_LIST_FILE_NAME);
        restoreColumnWidths(COL_WIDTH_LIST_SAVE);
    }

    @Override
    public void columnMarginChanged(ChangeEvent ce) {
        //
        super.columnMarginChanged(ce);
        //
        if (SYNC_TABLES_LIST == null) {
            SYNC_TABLES_LIST = new ArrayList<JTable>();
        }
        //
        if (SYNC_TABLES_LIST.isEmpty() == false) {
            for (JTable table : SYNC_TABLES_LIST) {
                synchColumnWidths(table);
            }
        }
        //
        if (SAVE_COL_WIDTHS == false) {
            return;
        }
        //
        if (COL_WIDTH_LIST_SAVE == null) {
            COL_WIDTH_LIST_SAVE = new ArrayList();
        }
        //
//        System.out.println("COL MARGIN CHANGED: ");
        //
        DefaultTableColumnModel dtcm = (DefaultTableColumnModel) ce.getSource();
        JTable parentTable = null;
        //
        Object[] signers = dtcm.getColumnModelListeners();
        //
        for (Object object : signers) {
            if (object instanceof JTable && parentTable == null) {
                parentTable = (JTable) object;
            }
        }
        //
        //
        if (parentTable instanceof JTableM && SAVE_ALLOWED) {
            COL_WIDTH_LIST_SAVE = saveColumnWidths();
        }
        //
    }

    class ColumnCountWatcher implements Runnable {

        private boolean run = true;

        @Override
        public void run() {
            while (run) {
                //
                wait_(INITIAL_TIMEOUT);
                //
                if (getColumnCount() > 0 && getColumnWidthByIndex(1) > NOT_LESS_THEN) {
                    run = false;
                    restore();
                    SAVE_ALLOWED = true;
                }
            }
        }

        private void wait_(int millis) {
            synchronized (this) {
                try {
                    wait(millis);
                } catch (InterruptedException ex) {
                    Logger.getLogger(ColumnCountWatcher.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }

    private ArrayList<Integer> saveColumnWidths() {
        //
        ArrayList<Integer> list = new ArrayList<>();
        //
        for (int i = 0; i < getColumnCount(); i++) {
            list.add(getColumnWidthByIndex(i));
        }
        //
        if (COL_WIDTH_LIST_SAVE.isEmpty()) {
            return list;
        }
        //
        if (verifyWidths(list)) {
            objectToFile(COL_WIDTH_LIST_FILE_NAME, list);
//            System.out.println("saved");
            return list;
        } else {
            return COL_WIDTH_LIST_SAVE;
        }
        //
    }

    private ArrayList restoreSaveListFromObject(String fileName) {
        try {
            Object obj = fileToObject(fileName);
            ArrayList colWidthList = (ArrayList) obj;
            return colWidthList;
        } catch (IOException | ClassNotFoundException ex) {
            return new ArrayList();
        }
    }

    private Object fileToObject(String path) throws IOException, ClassNotFoundException {
        FileInputStream fas = new FileInputStream(path);
        ObjectInputStream ois = new ObjectInputStream(fas);
        Object obj = ois.readObject();
        return obj;
    }

    private void restoreColumnWidths(ArrayList<Integer> list) {
        //
        if (list.isEmpty()) {
            return;
        }
        //
        for (int i = 0; i < getColumnCount(); i++) {
            setColumnWidthByIndex(i, list.get(i));
        }
    }

    private void objectToFile(String path, Object obj) {
        try {
            FileOutputStream fos = new FileOutputStream(path);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(obj);


        } catch (Exception ex) {
            Logger.getLogger(JTableM.class
                    .getName()).log(Level.SEVERE, null, ex);
        }
    }

    private boolean verifyWidths(ArrayList<Integer> list) {
        //
        int less = 0;
        //
        for (Integer i : list) {
            if (i < NOT_LESS_THEN) {
                less++;
            }
        }
        //
        if (less == 0) {
            return true;
        } else {
            return false;
        }
    }

    //==========================================================================
    public void synchColumnWidths(JTable tableToSyncWith) {
        for (int i = 0; i < getColumnCount(); i++) {
            int srcWidth = getColumnWidthByIndex(i);
//            int destWidth = getColumnWidthByIndex(tableToSyncWith, i);
//            System.out.println("src: " + srcWidth + "  dest: " + destWidth);
            tableToSyncWith.getColumnModel().getColumn(i).setPreferredWidth(srcWidth);
        }
    }

    //==========================================================================
    public synchronized void build_table_common(ResultSet rs, String q) {
        //
        if (rs == null) {
            return;
        }
        //
//        HelpA.setTrackingToolTip(this, q);
        //
        try {
            String[] headers = getHeaders(rs);
            Object[][] content = getContent(rs);
            this.setModel(new DefaultTableModel(content, headers));


        } catch (SQLException ex) {
            Logger.getLogger(JTableM.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public synchronized void build_table_common(ResultSet rs, String q, int indexFirst, int indexLast) {
        //
        if (rs == null) {
            return;
        }
        //
//        HelpA.setTrackingToolTip(this, q);
        //
        try {
            String[] headers = getHeaders(rs);
            Object[][] content = getContent(rs);
            this.setModel(new DefaultTableModel(content, headers));


        } catch (SQLException ex) {
            Logger.getLogger(JTableM.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static synchronized String[] getHeaders(ResultSet rs) throws SQLException {
        ResultSetMetaData meta; // Returns the number of columns
        String[] headers; // skapar en ny array att lagra titlar i
        meta = rs.getMetaData(); // Den parameter som skickas in "ResultSet rs" innehåller Sträng vid initialisering
        headers = new String[meta.getColumnCount()]; // ger arrayen "headers" initialisering och anger antalet positioner
        for (int i = 0; i < headers.length; i++) {
            headers[i] = meta.getColumnLabel(i + 1);
        }
        //
        return headers;
    }

    public static synchronized Object[][] getContent(ResultSet rs) throws SQLException {
        ResultSetMetaData rsmt;
        Object[][] content;
        int rows, columns;
        rsmt = rs.getMetaData(); // får in antalet columner
        rs.last(); // flyttar pekaren till sista positon
        rows = rs.getRow(); // retrieves the current antalet rows och lagrar det i variabeln "rows"
        columns = rsmt.getColumnCount(); // retrieves number of columns och lagrar det i "columns".
        content = new Object[rows][columns]; // ger arrayen content som är en "Object"
        // initialisering i den första demensionen är "rows" i den andra "columns"
        //
        for (int row = 0; row < rows; row++) {
            rs.absolute(row + 1); // Flytta till rätt rad i resultatmängden
            for (int col = 0; col < columns; col++) {
                Object obj = rs.getString(col + 1);
                content[row][col] = obj;
            }
        }
        //
        return content;
    }

    public static synchronized Object[][] getContent(ResultSet rs, int indexFirst, int indexLast) throws SQLException {
        ResultSetMetaData rsmt;
        Object[][] content;
        int rows, columns;
        rsmt = rs.getMetaData(); // får in antalet columner
        rs.last(); // flyttar pekaren till sista positon
        columns = rsmt.getColumnCount(); // retrieves number of columns och lagrar det i "columns".
        rows = (indexLast - indexFirst) + 1;
        content = new Object[rows][columns]; // ger arrayen content som är en "Object"
        // initialisering i den första demensionen är "rows" i den andra "columns"
        //
        int row_ = 0;
        for (int row = indexFirst; row <= indexLast; row++) {
            rs.absolute(row); // Flytta till rätt rad i resultatmängden
            for (int col = 0; col < columns; col++) {
//                System.out.println("Col: " + (col+1));
                Object obj = rs.getString(col + 1);
                content[row_][col] = obj;
            }
            row_++;
        }
        //
        return content;
    }

    public String jTableToHTML(String[] jtableColsToInclude) {
        //
        ArrayList<String> colNames;
        //
        if (jtableColsToInclude != null) {
            colNames = getVisibleColumnsNames_B(jtableColsToInclude);
        } else {
            colNames = getVisibleColumnsNames();
        }
        //
        //
        String html = "";
        //
        //
        html += "<table class='jtable'>";
        //
        //<TABLE HEADER>
        html += "<tr>";
        //
        for (int i = 0; i < colNames.size(); i++) {
            html += "<th>" + colNames.get(i) + "</th>";
        }
        //
        html += "</tr>";
        //</TABLE HEADER>
        //
        //<TABLE BODY>
        for (int x = 0; x < getRowCount(); x++) {
            //
            ArrayList rowValues;
            //
            if (jtableColsToInclude != null) {
                rowValues = getLineValuesVisibleColsOnly_B(x, jtableColsToInclude);
            } else {
                rowValues = getLineValuesVisibleColsOnly(x);
            }
            //
            //
            html += "<tr>";
            //
            for (int i = 0; i < rowValues.size(); i++) {
                html += "<td>" + rowValues.get(i) + "</td>";
            }
            //
            html += "</tr>";
            //
        }
        //</TABLE BODY>
        //
        html += "</table>";
        //
        //
        return html;
    }

    public String jTableToCSV(boolean writeToFile) {
        //
        String csv = "";
        //
        for (Object colName : getVisibleColumnsNames()) {
            csv += colName + ";";
        }
        //
        csv += "\n";
        //
        //
        for (int x = 0; x < this.getRowCount(); x++) {
            for (Object rowValue : getLineValuesVisibleColsOnly(x)) {
                csv += rowValue + ";";
            }
            csv += "\n";
        }
        //
        String path = get_desktop_path() + "\\" + getDate() + ".csv";
        //
        if (writeToFile) {
            try {
                writeToFile(path, csv);
//                JOptionPane.showMessageDialog(null, "Export file ready, the file is in: " + path);
                run_application_with_associated_application(new File(path));


            } catch (IOException ex) {
                Logger.getLogger(JTableM.class
                        .getName()).log(Level.SEVERE, null, ex);
            }
        }
        //
        return csv;
    }

    public String jTableToCSV(boolean writeToFile, String[] columnsToInclude) {
        //
        String csv = "";
        //
        for (Object colName : getVisibleColumnsNames_B(columnsToInclude)) {
            csv += colName + ";";
        }
        //
        csv += "\n";
        //
        //
        for (int x = 0; x < getRowCount(); x++) {
            for (Object rowValue : getLineValuesVisibleColsOnly_B(x, columnsToInclude)) {
                csv += rowValue + ";";
            }
            csv += "\n";
        }
        //
        String path = get_desktop_path() + "\\" + getDate() + ".csv";
        //
        if (writeToFile) {
            try {
                writeToFile(path, csv);
//                JOptionPane.showMessageDialog(null, "Export file ready, the file is in: " + path);
                run_application_with_associated_application(new File(path));


            } catch (IOException ex) {
                Logger.getLogger(JTableM.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        //
        return csv;
    }

    private String get_desktop_path() {
        return System.getProperty("user.home") + "\\" + "Desktop";
    }

    private String getDate() {
        DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH_mm");
        Calendar calendar = Calendar.getInstance();
        return formatter.format(calendar.getTime());
    }

    private void writeToFile(String fileName, String textToWrite) throws IOException {
        FileWriter fstream = new FileWriter(fileName, false);
        BufferedWriter out = new BufferedWriter(fstream);

        out.write(textToWrite);
        out.newLine();
        out.flush();
        out.close();
    }

    private static void run_application_with_associated_application(File file) throws IOException {
        Desktop.getDesktop().open(file);
    }

    private boolean exists_(String col, String[] columns) {
        for (String colName : columns) {
            if (colName.equals(col)) {
                return true;
            }
        }
        return false;
    }

    public int getColumnWidthByIndex(int colIndex) {
        return getColumnModel().getColumn(colIndex).getWidth();
    }

    public int getColumnWidthByIndex(JTable table, int colIndex) {
        return table.getColumnModel().getColumn(colIndex).getWidth();
    }

    /**
     *
     * @param colIndex - starts from 0
     * @param table
     * @param width
     */
    public void setColumnWidthByIndex(int colIndex, int width) {
        getColumnModel().getColumn(colIndex).setWidth(width);
    }

    /**
     * OBS! Some times setWidth() works, some times setPreferredWidth()
     *
     * @param table
     * @param colIndex
     * @param width
     */
    public void setColumnWidthByIndex(JTable table, int colIndex, int width) {
        table.getColumnModel().getColumn(colIndex).setPreferredWidth(width);
    }

    public void disableColumnDragging() {
        getTableHeader().setReorderingAllowed(false);
    }

    public ArrayList getLineValuesVisibleColsOnly_B(int rowNr, String[] columnsToInclude) {
        ArrayList rowValues = new ArrayList();
        for (int x = 0; x < getColumnCount(); x++) {
            if (columnIsVisible(x)) {
                String value = "" + getValueAt(rowNr, x);
                if (exists_(getColumnNameByIndex(x), columnsToInclude)) {
                    rowValues.add(value);
                }
            }
        }

        return rowValues;
    }

    /**
     * OBS! JTable row index start with 0
     *
     * @param table
     * @param rowNr
     * @return
     */
    public ArrayList getLineValuesVisibleColsOnly(int rowNr) {
        ArrayList rowValues = new ArrayList();
        for (int x = 0; x < getColumnCount(); x++) {
            if (columnIsVisible(x)) {
                String value = "" + getValueAt(rowNr, x);
                rowValues.add(value);
            }
        }
        return rowValues;
    }

    public ArrayList getVisibleColumnsNames_B(String[] columnsToInclude) {
        ArrayList columnNames = new ArrayList();
        for (int i = 0; i < getColumnCount(); i++) {
            if (columnIsVisible(i) && exists_(getColumnNameByIndex(i), columnsToInclude)) {
                columnNames.add(getColumnNameByIndex(i));
            }
        }

//        ArrayList visibleColumnsIndexes = getVisibleColumnsIndexes(table);
//
//        for (Object index : visibleColumnsIndexes) {
//            Integer ind = (Integer) index;
//            if (exists_(ind, columnsToInclude)) {
//                columnNames.add(getColumnNameByIndex(table, ind));
//            }
//        }

        return columnNames;
    }

    public ArrayList getVisibleColumnsNames() {
        ArrayList columnNames = new ArrayList();
        for (int i = 0; i < this.getColumnCount(); i++) {
            if (columnIsVisible(i)) {
                columnNames.add(getColumnNameByIndex(i));
            }
        }
        return columnNames;
    }

    public ArrayList getVisibleColumnsIndexes() {
        ArrayList indexes = new ArrayList();
        for (int i = 0; i < getColumnCount(); i++) {
            if (columnIsVisible(i)) {
                indexes.add(i);
            }
        }
        return indexes;
    }

    public int getVisibleColumnsCount(JTable table) {
        int count = 0;
        for (int i = 0; i < table.getColumnCount(); i++) {
            if (columnIsVisible(i)) {
                count++;
            }
        }
        return count++;
    }

    public boolean columnIsVisible(int column) {
        int width = getColumnModel().getColumn(column).getWidth();
        return width == 0 ? false : true;
    }

    public String getColumnNameByIndex(int column) {
        JTableHeader th = getTableHeader();
        TableColumnModel tcm = th.getColumnModel();
        TableColumn tc = tcm.getColumn(column);
        return (String) tc.getHeaderValue();
    }

    /**
     * OBS! Even if you change the header title of the column the "Real Name"
     * will be the same!!!!!
     *
     * @param table
     * @param column
     * @param newTitle
     */
    public static void changeTableHeaderTitleOfOneColumn(JTable table, int column, String newTitle) {
        JTableHeader th = table.getTableHeader();
        TableColumnModel tcm = th.getColumnModel();
        TableColumn tc = tcm.getColumn(column);
        tc.setHeaderValue(newTitle);
        th.repaint();
    }

    /**
     * OBS! Even if you change the header title of the column the "Real Name"
     * will be the same!!!!!
     *
     * @param table
     * @param oldName
     * @param newTitle
     */
    public void changeTableHeaderTitleOfOneColumn(String oldName, String newTitle) {
        JTableHeader th = getTableHeader();
        TableColumnModel tcm = th.getColumnModel();
        TableColumn tc = tcm.getColumn(getColByName(oldName));
        tc.setHeaderValue(newTitle);
        th.repaint();
    }

    //
    public void paintTableHeaderBorderOneColumn(int column, final Color borederColor) {
        JTableHeader th = getTableHeader();
        TableColumnModel tcm = th.getColumnModel();
        TableColumn tc = tcm.getColumn(column);
        //
        //
        TableCellRenderer renderer = new TableCellRenderer() {
            JLabel label = new JLabel();

            @Override
            public Component getTableCellRendererComponent(JTable table,
                    Object value, boolean isSelected, boolean hasFocus,
                    int row, int column) {
                label.setOpaque(true);
                label.setText("" + value);
                label.setBorder(BorderFactory.createLineBorder(borederColor));
                return label;
            }
        };
        //
        tc.setHeaderRenderer(renderer);
        th.repaint();
    }

    public void resetTableHeaderPainting(int column) {
        //
        if (column == -1) {
            return;
        }
        //
        JTableHeader th = getTableHeader();
        TableColumnModel tcm = th.getColumnModel();
        TableColumn tc = tcm.getColumn(column);
        //
        tc.setHeaderRenderer(null);
        th.repaint();
    }

    public static void clearAllRowsJTable(JTable table) {
        DefaultTableModel dm = (DefaultTableModel) table.getModel();
        //
        int rowCount = dm.getRowCount();
        //
        for (int i = rowCount - 1; i >= 0; i--) {
            dm.removeRow(i);
        }
    }

    public void removeRowJTable(int rowToRemove) {
        DefaultTableModel dm = (DefaultTableModel) getModel();
        dm.removeRow(rowToRemove);
    }

    public void addRowJTable() {
        DefaultTableModel model = (DefaultTableModel) getModel();
        model.addRow(new Object[]{});
    }

    public int getRowByValue(String col_name, String row_value) {
        for (int i = 0; i < getColumnCount(); ++i) {
            if (getColumnName(i).equals(col_name)) {
                for (int y = 0; y < getRowCount(); ++y) {
                    String curr_row_value = "" + getValueAt(y, i);
                    //
                    if (curr_row_value == null) {
                        continue;
                    }
                    //
                    if (curr_row_value.equals(row_value)) {
                        return y;
                    }
                }
            }
        }
        return -1;
    }

    public void setValueGivenRow(int row, String colName, Object value) {
        setValueAt(value, row, getColByName(colName));
    }

    public boolean getIfAnyRowChosen() {
        if (getSelectedRow() == -1) {
            return false;
        } else {
            return true;
        }
    }

    public String getValueGivenRow(int row, String colName) {
        return "" + getValueAt(row, getColByName(colName));
    }

    public String getValueSelectedRow(String colName) {
        int selected_row = getSelectedRow();
        //
        try {
            return "" + getValueAt(selected_row, getColByName(colName));
        } catch (Exception ex) {
            return null;
        }
    }

    public int getColByName(String name) {
        for (int i = 0; i < getColumnCount(); ++i) {
            if (getColumnName(i).equals(name)) {
                return i;
            }
        }
        return -1;
    }

    public boolean hideColumnByName(String name) {
        for (int i = 0; i < getColumnCount(); ++i) {
            if (getColumnName(i).equals(name)) {
                getColumnModel().getColumn(i).setMinWidth(0);
                getColumnModel().getColumn(i).setMaxWidth(0);
                getColumnModel().getColumn(i).setWidth(0);
                return true;
            }
        }
        return false;
    }

    public int moveRowToEnd(int currRow) {
        DefaultTableModel dtm = (DefaultTableModel) getModel();
        dtm.moveRow(currRow, currRow, getRowCount() - 1);
        return getRowCount() - 1;
    }

    public void moveRowTo(int rowToMove, int rowToMoveTo) {
        DefaultTableModel dtm = (DefaultTableModel) getModel();
        dtm.moveRow(rowToMove, rowToMove, rowToMoveTo);
    }

    public void moveRowTo(String colName, String rowValue, int rowToMoveTo) {
        DefaultTableModel dtm = (DefaultTableModel) getModel();
        int rowToMove = getRowByValue(colName, rowValue);
        dtm.moveRow(rowToMove, rowToMove, rowToMoveTo);
    }

    public void selectNextRow() {
        try {
            setRowSelectionInterval(getSelectedRow() + 1, getSelectedRow() + 1);
        } catch (Exception ex) {
        }
    }

    public void selectPrevRow() {
        try {
            setRowSelectionInterval(getSelectedRow() - 1, getSelectedRow() - 1);
        } catch (Exception ex) {
        }
    }

    public void setSelectedRow(int rowNr) {
        setRowSelectionInterval(rowNr, rowNr);
    }

    public void markFirstRowJtable() {
        markGivenRow(0);
    }

    public void markLastRowJtable(JTable table) {
        markGivenRow(getRowCount() - 1);
    }

    public void markGivenRow(int row) {
        try {
            changeSelection(row, 0, false, false);
        } catch (Exception ex) {
        }
    }

    public int getNextRow(int previousRow) {
        int nextRow = previousRow++;
        if (nextRow < getRowCount()) {
            return nextRow;
        } else {
            return 0;
        }
    }

    public boolean isEmtyJTable() {
        if (getRowCount() == 0) {
            return true;
        } else {
            return false;
        }
    }

    public void stopEditJTable() {
        editCellAt(0, 0);
    }
}
