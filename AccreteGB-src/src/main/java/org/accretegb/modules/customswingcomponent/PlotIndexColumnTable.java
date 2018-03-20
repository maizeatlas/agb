package org.accretegb.modules.customswingcomponent;

/*
 * Licensed to Openaccretegb-common under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. Openaccretegb-common licenses this
 * file to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import javax.swing.*;
import javax.swing.table.*;

import org.accretegb.modules.constants.ColumnConstants;
import org.accretegb.modules.util.ThreadPool;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.dnd.DragGestureEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;


public class PlotIndexColumnTable extends JTable {

	private static final long serialVersionUID = 1L;
	private MyItemListener itemListener;
	private List<String> columnNames;
	private boolean checkBoxHeader;
	private boolean isSingleSelection;
	private List<Integer> editableColumns;
	private List<String> selectedUniqueRowVars = new ArrayList<String>();
	private String flagColunName;
	private List<String> showColumns = new ArrayList<String>();



	public List<String> getSelectedUniqueRowVars() {
		return selectedUniqueRowVars;
	}

	public void setSelectedUniqueRowVars(List<String> selectedUniqueRowVars) {
		this.selectedUniqueRowVars = selectedUniqueRowVars;
	}
	
	public List<Integer> getEditableColumns() {
		return editableColumns;
	}

	public void setEditableColumns(List<Integer> editableColumns) {
		this.editableColumns = editableColumns;
	}

	public boolean isSingleSelection() {
		return isSingleSelection;
	}

	public void setSingleSelection(boolean isSingleSelection) {
		this.isSingleSelection = isSingleSelection;
	}

	public boolean getCheckBoxHeader() {
		return checkBoxHeader;
	}
	
	public void setCheckBoxHeader(boolean checkBoxHeader) {
		this.checkBoxHeader = checkBoxHeader;
	}
	public PlotIndexColumnTable() {
	}
	public MyItemListener getItemListener() {
		return itemListener;
	}

	public void setItemListener(MyItemListener itemListener) {
		this.itemListener = itemListener;
	}

	public List<String> getColumnNames() {
		return columnNames;
	}
	
	public void setColumnNames(List<String> columnNames) {
		this.columnNames = columnNames;
	}
	
	public String getFlagColunName() {
		return flagColunName;
	}

	public void setFlagColunName(String flagColunName) {
		this.flagColunName = flagColunName;
	}
	
	public List<String> getShowColumns() {
		return showColumns;
	}
    public void setShowColumns(List<String> showColumns) {
		this.showColumns = showColumns;
	}
    
	public void initialize() {
		int numberOfRows = 0;
		Object rows[][] = new Object[numberOfRows][getColumnNames().size()];
		for(int rowCounter=0; rowCounter < numberOfRows; rowCounter++) {
			rows[rowCounter] = new Object[getColumnNames().size()];
			for(int columnCounter=0; columnCounter<getColumnNames().size(); columnCounter++) {
				if(columnCounter == 0) {
					rows[rowCounter][columnCounter] = rowCounter+1;
				} else {
					rows[rowCounter][columnCounter] = new String();
				}
			}
		}
		
		setModel(getTableModel(rows, (String []) getColumnNames().toArray(new String[getColumnNames().size()])));
		TableColumn tc = getColumnModel().getColumn(0);
		tc.setCellRenderer(new RowHeaderRenderer(this));
		setDragEnabled(false);
		setCellSelectionEnabled(true);
		setGridColor(Color.LIGHT_GRAY);
        if (!isSingleSelection()) {
        	addMouseListener(new MyMultiSelectionMouseListener());
		    this.addKeyListener(new KeyAdapter() {
		        public void keyPressed(KeyEvent arg0) {
		            if (arg0.isControlDown() && arg0.getKeyCode() == KeyEvent.VK_A)
		                selectAll();
		        }
		    });
        }
        this.addKeyListener(new KeyAdapter() {
	        public void keyPressed(KeyEvent e) {	
	        	if ((e.getKeyCode() == KeyEvent.VK_COPY) ||
	        		        ((e.getKeyCode() == KeyEvent.VK_C) && e.isControlDown()) ||
	        		        ((e.getKeyCode() == KeyEvent.VK_C) && e.isMetaDown()))
		            {	
	        			 
		                 int row = getSelectedRow();
		                 int col = getSelectedColumn();
		                 if(row >= 0 && col >= 0){
		                	String value = String.valueOf(getValueAt(row,col));
		                	StringSelection selection = new StringSelection(value);
		                	Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
		                	clipboard.setContents(selection, selection); 
		                 }
		                 
		           }
	        	
	            
	        }
	    });
	}
	public void updateRowNums(){
		int rowNum = 0;
		for(int row = 0; row < getRowCount(); ++row){
			rowNum++;
			setValueAt(rowNum, row, getIndexOf(ColumnConstants.ROW_NUM));
		}
	}
	public TableModel getTableModel(Object row[][], String[] columnNames ){
		return new DefaultTableModel(row, columnNames) {
			private static final long serialVersionUID = 1L;
			@SuppressWarnings({ "rawtypes", "unchecked" })
			public Class getColumnClass(int column) {
				Class returnValue;
				try {
					returnValue = getValueAt(0, column).getClass();
				} catch(Exception e) {
					returnValue = Object.class;
				}
				return returnValue;
			}
			public boolean isCellEditable(int row, int col) {
				if(editableColumns!= null && editableColumns.contains(col))
					return true;
				return false;
			}
		};
	}

	public class MyItemListener implements ItemListener {
		
		public void itemStateChanged(ItemEvent e) {

			Object source = e.getSource();
			if (source instanceof AbstractButton == false)
				return;

			boolean checked = e.getStateChange() == ItemEvent.SELECTED;

			for (int x = 0, y = getRowCount(); x < y; x++) {
				setValueAt(new Boolean(checked), x, 0);
			}

		}

	}
	
	private static class RowHeaderRenderer extends JLabel implements TableCellRenderer {
		RowHeaderRenderer(JTable table) {
			JTableHeader header = table.getTableHeader();
			setOpaque(true);
			setBorder(UIManager.getBorder("TableHeader.cellBorder"));
			setHorizontalAlignment(CENTER);
			setForeground(header.getForeground());
			setBackground(header.getBackground());
			setFont(header.getFont());
		}

		public Component getTableCellRendererComponent(JTable table, Object value,
				boolean isSelected, boolean hasFocus, int row, int column) {
			setText((value == null) ? "" : value.toString());
			return this;
		}
	}

	public class MySingleSelectionMouseListener extends MouseAdapter {
		
		public void mouseClicked(MouseEvent mouseEvent) {

			int checkedCount = 0;
			getTableHeader().repaint();
			
			int row = rowAtPoint(mouseEvent.getPoint());
			String rowValue = "";
			if(getValueAt(row, 0) instanceof String){
				rowValue = String.valueOf(Integer.parseInt((String)getValueAt(row, 0)) - 1);
			}else{
				rowValue = String.valueOf((Integer)getValueAt(row, 0) - 1);
			}
			if(getSelectedUniqueRowVars().contains( rowValue)) {//Selected same row again
				clearSelection();
			}
			else {//selected different row
				getSelectedUniqueRowVars().clear();
				getSelectedUniqueRowVars().add( String.valueOf(  ((Integer)getValueAt(row, 0)) - 1 ) );
			}
			
		}

	}

	public class MyMultiSelectionMouseListener extends MouseAdapter {

        public void mouseReleased(MouseEvent mouseEvent) {
            int selectedRows[] = getSelectedRows();
            if(selectedRows.length > 1)
            {
            	getSelectedUniqueRowVars().clear();
            	for (int row : selectedRows) {
            		int rowindex = Integer.parseInt(String.valueOf(getValueAt(row, 0)));
                    getSelectedUniqueRowVars().add(String.valueOf(rowindex-1));
                }
            }            
            repaint();
        }
    }

	@Override
	public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
		
		Component component = super.prepareRenderer(renderer, row, column);
		if(column ==0) return component;
		if (getSelectedRows().length > 0) {
			if (isRowSelected(row)) {
				component.setForeground(Color.BLACK);
				component.setBackground(new Color(184, 207, 229));
			} else {
				component.setForeground(Color.LIGHT_GRAY);
				component.setBackground(Color.WHITE);
			}
		} else {
			component.setForeground(Color.BLACK);
			component.setBackground(Color.WHITE);
		}
		if(getFlagColunName()!= null ){
			Object value = getValueAt(row, getIndexOf(getFlagColunName()));
			if(value !=null && !value.equals("null")){
				component.setBackground(new Color(252, 218, 218));
			}
		}
		return component;
	}
	
	@Override
	public void clearSelection() {
		super.clearSelection();
		Runnable task = new Runnable() {
            public void run() {
	
				if(selectedUniqueRowVars!=null) {
					selectedUniqueRowVars.clear();
				}
				repaint();
			}
        };
        ThreadPool.getAGBThreadPool().executeTask(task);
		
	}
	
	@Override
	public void addRowSelectionInterval(int index0, int index1) {
		super.addRowSelectionInterval(index0, index1);
		int min = Math.min(index0, index1);
		index1 = Math.max(index0, index1);
		index0 = min;
		for(int rowCounter=index0; rowCounter<=index1; rowCounter++) {
			selectedUniqueRowVars.add(String.valueOf(getValueAt(rowCounter, 0)));
		}
	}
	
	@Override
	public void removeRowSelectionInterval(int index0, int index1) {
		super.removeRowSelectionInterval(index0, index1);
		int min = Math.min(index0, index1);
		index1 = Math.max(index0, index1);
		index0 = min;
		for(int rowCounter=index0; rowCounter<=index1; rowCounter++) {
			selectedUniqueRowVars.remove(String.valueOf(getValueAt(rowCounter, 0)));
		}
	}
	
	public void hideColumn(String columnName) {
        getColumn(columnName).setWidth(0);
        getColumn(columnName).setMinWidth(0);
        getColumn(columnName).setMaxWidth(0);
    }

    public void showColumn(String columnName) {
        getColumn(columnName).setMinWidth(15);
        getColumn(columnName).setMaxWidth(Integer.MAX_VALUE);
        repaint();
    }
	
	public Object getValueAt(int row, String columnIdentifier) {
		int column = getColumnModel().getColumnIndex(columnIdentifier);
		return getValueAt(row, column);
	}
	
	public int getIndexOf(String columnIdentifier) {
		return getColumnModel().getColumnIndex(columnIdentifier);
	}

	public void dragGestureRecognized(DragGestureEvent dge) {
		// TODO Auto-generated method stub
		
	}

}
