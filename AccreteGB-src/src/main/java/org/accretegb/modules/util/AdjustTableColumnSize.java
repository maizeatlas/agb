package org.accretegb.modules.util;

import java.awt.Component;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

import javax.swing.JTable;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

public class AdjustTableColumnSize {
	
	public static void addTableResizeListener(final JTable table){
		table.getParent().addComponentListener(new ComponentAdapter() {
		    @Override
		    public void componentResized(final ComponentEvent e) {
		        if (table.getPreferredSize().width < table.getParent().getWidth()) {
		        	table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
		        } else {
		        	table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		        	for (int i = 0; i < table.getColumnCount(); i++) {
		                adjustColumnSize(table, i, 2);
		            }
		        }
		    }
		});
	}
	
	
	
	public static  void adjustColumnSize(JTable table, int column, int margin) {
        DefaultTableColumnModel colModel = (DefaultTableColumnModel) table.getColumnModel();
        TableColumn col = colModel.getColumn(column);
        int width;

        TableCellRenderer renderer = col.getHeaderRenderer();
        if (renderer == null) {
            renderer = table.getTableHeader().getDefaultRenderer();
        }
        Component comp = renderer.getTableCellRendererComponent(table, col.getHeaderValue(), false, false, 0, 0);
        width = comp.getPreferredSize().width;

        for (int r = 0; r < table.getRowCount(); r++) {
            renderer = table.getCellRenderer(r, column);
            comp = renderer.getTableCellRendererComponent(table, table.getValueAt(r, column), false, false, r, column);
            int currentWidth = comp.getPreferredSize().width;
            width = Math.max(width, currentWidth);
        }

        width += 2 * margin;

        col.setPreferredWidth(width);
        col.setWidth(width);
    }


}
