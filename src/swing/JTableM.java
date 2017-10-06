/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package swing;

import java.awt.Color;
import java.awt.Component;
import java.io.File;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import statics.HelpA;

/**
 *
 * @author KOCMOC
 */
public class JTableM extends JTable {

    public synchronized void build_table_common(ResultSet rs, String q) {
        //
        if (rs == null) {
            return;
        }
        //
        HelpA.setTrackingToolTip(this, q);
        //
        try {
            String[] headers = HelpA.getHeaders(rs);
            Object[][] content = HelpA.getContent(rs);
            this.setModel(new DefaultTableModel(content, headers));
        } catch (SQLException ex) {
            Logger.getLogger(HelpA.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public String jTableToCSV(JTable table, boolean writeToFile) {
        //
        String csv = "";
        //
        for (Object colName : getVisibleColumnsNames(this)) {
            csv += colName + ";";
        }
        //
        csv += "\n";
        //
        //
        for (int x = 0; x < this.getRowCount(); x++) {
            for (Object rowValue : getLineValuesVisibleColsOnly(this, x)) {
                csv += rowValue + ";";
            }
            csv += "\n";
        }
        //
        String path = HelpA.get_desktop_path() + "\\"
                + HelpA.get_proper_date_time_same_format_on_all_computers_err_output() + ".csv";
        //
        if (writeToFile) {
            try {
                HelpA.writeToFile(path, csv);
//                JOptionPane.showMessageDialog(null, "Export file ready, the file is in: " + path);
                HelpA.run_application_with_associated_application(new File(path));
            } catch (IOException ex) {
                Logger.getLogger(JTableM.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        //
        return csv;
    }

    public String jTableToCSV(JTable table, boolean writeToFile, String[] columnsToInclude) {
        //
        String csv = "";
        //
        for (Object colName : getVisibleColumnsNames_B(table, columnsToInclude)) {
            csv += colName + ";";
        }
        //
        csv += "\n";
        //
        //
        for (int x = 0; x < table.getRowCount(); x++) {
            for (Object rowValue : getLineValuesVisibleColsOnly_B(table, x, columnsToInclude)) {
                csv += rowValue + ";";
            }
            csv += "\n";
        }
        //
        String path = HelpA.get_desktop_path() + "\\"
                + HelpA.get_proper_date_time_same_format_on_all_computers_err_output() + ".csv";
        //
        if (writeToFile) {
            try {
                HelpA.writeToFile(path, csv);
//                JOptionPane.showMessageDialog(null, "Export file ready, the file is in: " + path);
                HelpA.run_application_with_associated_application(new File(path));
            } catch (IOException ex) {
                Logger.getLogger(JTableM.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        //
        return csv;
    }

    private boolean exists_(String col, String[] columns) {
        for (String colName : columns) {
            if (colName.equals(col)) {
                return true;
            }
        }
        return false;
    }

    public static void disableColumnDragging(JTable table) {
        table.getTableHeader().setReorderingAllowed(false);
    }

    public ArrayList getLineValuesVisibleColsOnly_B(JTable table, int rowNr, String[] columnsToInclude) {
        ArrayList rowValues = new ArrayList();
        for (int x = 0; x < table.getColumnCount(); x++) {
            if (columnIsVisible(table, x)) {
                String value = "" + table.getValueAt(rowNr, x);
                if (exists_(getColumnNameByIndex(table, x), columnsToInclude)) {
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
    public static ArrayList getLineValuesVisibleColsOnly(JTable table, int rowNr) {
        ArrayList rowValues = new ArrayList();
        for (int x = 0; x < table.getColumnCount(); x++) {
            if (columnIsVisible(table, x)) {
                String value = "" + table.getValueAt(rowNr, x);
                rowValues.add(value);
            }
        }
        return rowValues;
    }

    public ArrayList getVisibleColumnsNames_B(JTable table, String[] columnsToInclude) {
        ArrayList columnNames = new ArrayList();
        for (int i = 0; i < table.getColumnCount(); i++) {
            if (columnIsVisible(table, i) && exists_(getColumnNameByIndex(table, i), columnsToInclude)) {
                columnNames.add(getColumnNameByIndex(table, i));
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

    public ArrayList getVisibleColumnsNames(JTable table) {
        ArrayList columnNames = new ArrayList();
        for (int i = 0; i < this.getColumnCount(); i++) {
            if (columnIsVisible(this, i)) {
                columnNames.add(getColumnNameByIndex(this, i));
            }
        }
        return columnNames;
    }

    public static ArrayList getVisibleColumnsIndexes(JTable table) {
        ArrayList indexes = new ArrayList();
        for (int i = 0; i < table.getColumnCount(); i++) {
            if (columnIsVisible(table, i)) {
                indexes.add(i);
            }
        }
        return indexes;
    }

    public static int getVisibleColumnsCount(JTable table) {
        int count = 0;
        for (int i = 0; i < table.getColumnCount(); i++) {
            if (columnIsVisible(table, i)) {
                count++;
            }
        }
        return count++;
    }

    public static boolean columnIsVisible(JTable table, int column) {
        int width = table.getColumnModel().getColumn(column).getWidth();
        return width == 0 ? false : true;
    }

    public static String getColumnNameByIndex(JTable table, int column) {
        JTableHeader th = table.getTableHeader();
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
    public static void changeTableHeaderTitleOfOneColumn(JTable table, String oldName, String newTitle) {
        JTableHeader th = table.getTableHeader();
        TableColumnModel tcm = th.getColumnModel();
        TableColumn tc = tcm.getColumn(getColByName(table, oldName));
        tc.setHeaderValue(newTitle);
        th.repaint();
    }

    //
    public static void paintTableHeaderBorderOneColumn(JTable table, int column, final Color borederColor) {
        JTableHeader th = table.getTableHeader();
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

    public static void resetTableHeaderPainting(JTable table, int column) {
        //
        if (column == -1) {
            return;
        }
        //
        JTableHeader th = table.getTableHeader();
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

    public static void removeRowJTable(JTable table, int rowToRemove) {
        DefaultTableModel dm = (DefaultTableModel) table.getModel();
        dm.removeRow(rowToRemove);
    }

    public static void addRowJTable(JTable table) {
        DefaultTableModel model = (DefaultTableModel) table.getModel();
        model.addRow(new Object[]{});
    }

    public static int getRowByValue(JTable table, String col_name, String row_value) {
        for (int i = 0; i < table.getColumnCount(); ++i) {
            if (table.getColumnName(i).equals(col_name)) {
                for (int y = 0; y < table.getRowCount(); ++y) {
                    String curr_row_value = "" + table.getValueAt(y, i);
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

    public static void setValueGivenRow(JTable table, int row, String colName, Object value) {
        table.setValueAt(value, row, getColByName(table, colName));
    }

    public static boolean getIfAnyRowChosen(JTable table) {
        if (table.getSelectedRow() == -1) {
            return false;
        } else {
            return true;
        }
    }

    public static String getValueGivenRow(JTable table, int row, String colName) {
        return "" + table.getValueAt(row, getColByName(table, colName));
    }

    public static String getValueSelectedRow(JTable table, String colName) {
        int selected_row = table.getSelectedRow();
        //
        try {
            return "" + table.getValueAt(selected_row, getColByName(table, colName));
        } catch (Exception ex) {
//            Logger.getLogger(HelpA.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }

    public static int getColByName(JTable table, String name) {
        for (int i = 0; i < table.getColumnCount(); ++i) {
            if (table.getColumnName(i).equals(name)) {
                return i;
            }
        }
        return -1;
    }

    public static boolean hideColumnByName(JTable table, String name) {
        for (int i = 0; i < table.getColumnCount(); ++i) {
            if (table.getColumnName(i).equals(name)) {
                table.getColumnModel().getColumn(i).setMinWidth(0);
                table.getColumnModel().getColumn(i).setMaxWidth(0);
                table.getColumnModel().getColumn(i).setWidth(0);
                return true;
            }
        }
        return false;
    }

    public static int moveRowToEnd(JTable table, int currRow) {
        DefaultTableModel dtm = (DefaultTableModel) table.getModel();
        dtm.moveRow(currRow, currRow, table.getRowCount() - 1);
        return table.getRowCount() - 1;
    }

    public static void moveRowTo(JTable table, int rowToMove, int rowToMoveTo) {
        DefaultTableModel dtm = (DefaultTableModel) table.getModel();
        dtm.moveRow(rowToMove, rowToMove, rowToMoveTo);
    }

    public static void moveRowTo(JTable table, String colName, String rowValue, int rowToMoveTo) {
        DefaultTableModel dtm = (DefaultTableModel) table.getModel();
        int rowToMove = getRowByValue(table, colName, rowValue);
        dtm.moveRow(rowToMove, rowToMove, rowToMoveTo);
    }

    public static void selectNextRow(JTable table) {
        try {
            table.setRowSelectionInterval(table.getSelectedRow() + 1, table.getSelectedRow() + 1);
        } catch (Exception ex) {
        }
    }

    public static void selectPrevRow(JTable table) {
        try {
            table.setRowSelectionInterval(table.getSelectedRow() - 1, table.getSelectedRow() - 1);
        } catch (Exception ex) {
        }
    }

    public static void setSelectedRow(JTable table, int rowNr) {
        table.setRowSelectionInterval(rowNr, rowNr);
    }

    public static void markFirstRowJtable(JTable table) {
        markGivenRow(table, 0);
    }

    public static void markLastRowJtable(JTable table) {
        markGivenRow(table, table.getRowCount() - 1);
    }

    public static void markGivenRow(JTable table, int row) {
        try {
            table.changeSelection(row, 0, false, false);
        } catch (Exception ex) {
        }
    }

    public static int getNextRow(JTable table, int previousRow) {
        int nextRow = previousRow++;
        if (nextRow < table.getRowCount()) {
            return nextRow;
        } else {
            return 0;
        }
    }

    public static boolean isEmtyJTable(JTable table) {
        if (table.getRowCount() == 0) {
            return true;
        } else {
            return false;
        }
    }

    public static void stopEditJTable(JTable table) {
        table.editCellAt(0, 0);
    }
}
