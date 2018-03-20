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
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.*;

import org.accretegb.modules.constants.ColumnConstants;
import org.accretegb.modules.util.ThreadPool;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.TreeSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Future;

public class CheckBoxIndexColumnTable extends JTable {

    private static final long serialVersionUID = 1L;

    private MyItemListener itemListener;
    private CheckBoxHeader rendererComponent;
    private List<String> columnNames;
    private boolean checkBoxHeader;
    private boolean isSingleSelection;
    private List<Integer> editableColumns;
    private List<String> showColumns = new ArrayList<String>();
    private boolean hasSynced = false;
	private List<Integer> changedRowIds = new ArrayList<Integer>();
	private Future selectAllThread;
	private Future clearAllThread;
	private Set<Integer> checkedRows = new TreeSet<Integer>();
    
	public void initialize() {    	
        int numberOfRows = 0;
        Object rows[][] = new Object[numberOfRows][getColumnNames().size()];
        for (int rowCounter = 0; rowCounter < numberOfRows; rowCounter++) {
            rows[rowCounter] = new Object[getColumnNames().size()];
            for (int columnCounter = 0; columnCounter < getColumnNames().size(); columnCounter++) {
                if (columnCounter == 0) {
                    rows[rowCounter][columnCounter] = new Boolean(false);
                } else {
                    rows[rowCounter][columnCounter] = new String();
                }
            }
        } 
        final List<String> NumbericColumns = Arrays.asList(new String[]{ColumnConstants.TAG_ID, ColumnConstants.STOCK_ID, ColumnConstants.PACKET_ID, ColumnConstants.MATE_LINK,ColumnConstants.TOTAL_PACKETS,ColumnConstants.PACKET_NO,ColumnConstants.WEIGHT,ColumnConstants.NUMBER_OF_SEEDS});
        final TableModel model = getTableModel(rows, (String[]) getColumnNames().toArray(new String[getColumnNames().size()])); 
		setModel(model);  
		TableRowSorter<TableModel> sorter = new TableRowSorter<TableModel>(model){
            @Override
            public Comparator<?> getComparator(final int column) {
                Comparator c = new Comparator() {
                    public int compare(Object o1, Object o2) {
                    	if(NumbericColumns.contains(model.getColumnName(column))){
                             if (o1 instanceof String && o2 instanceof String) {
                                 return 0;
                             } else if (!(o1 instanceof String) && o2 instanceof String) {
                                 return Double.compare(Double.parseDouble(String.valueOf(o1)), 0);
                             } else if (o1 instanceof String && !(o2 instanceof String)) {
                                 return Double.compare(0,Double.parseDouble(String.valueOf(o2)));
                             }else {
                                return ((Comparable<Double>) Double.parseDouble(String.valueOf(o1))).compareTo(Double.parseDouble(String.valueOf(o2)));
                             }
                         }else{
                        	 return ((Comparable<String>)String.valueOf(o1)).compareTo(String.valueOf(o2));
                         }
                    	}
                       
                };
                return c;
            }
		};
        setRowSorter(sorter);
		setShowColumns();
	    TableColumn tc = getColumnModel().getColumn(0);
	    tc.setCellEditor(getDefaultEditor(Boolean.class));
	    tc.setCellRenderer(getDefaultRenderer(Boolean.class));
	    tc.setWidth(45);
	    tc.setPreferredWidth(45);
	    tc.setMaxWidth(45);
	    tc.setMinWidth(45);
	    getRendererComponent().setText(ColumnConstants.SELECT);
	    if (getCheckBoxHeader()) {
	        tc.setHeaderRenderer(getRendererComponent());
	    }            
        setDragEnabled(false);
        setCellSelectionEnabled(true);
        setGridColor(Color.LIGHT_GRAY);
        addMouseListener(new MySelectionMouseListener());
        
        addKeyListener(new KeyAdapter() {
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
        
        addFocusListener(new FocusAdapter() {
		    @Override
		    public void focusLost(FocusEvent e) {
		        TableCellEditor tce = getCellEditor();
		        if(tce != null)
		        {
		        	setHasSynced(false);
		        	int row= getEditingRow();
		        	if(!changedRowIds.contains(row) && row != -1)
		        	{
		        		changedRowIds.add(row);
		        	}
		        }
		    }
		});
        this.getModel().addTableModelListener(new TableModelListener(){
			public void tableChanged(TableModelEvent e) {
				// TODO Auto-generated method stub
				switch (e.getType()) {
				case TableModelEvent.INSERT:
					setHasSynced(false);
					break;
				case TableModelEvent.DELETE:
					setHasSynced(false);
					break;
				}
			}
        });
    }
    
    public void initilizeByCopy(){
	    TableColumn tc = getColumnModel().getColumn(0);
	    tc.setCellEditor(getDefaultEditor(Boolean.class));
	    tc.setCellRenderer(getDefaultRenderer(Boolean.class));
	    tc.setWidth(45);
	    tc.setPreferredWidth(45);
	    tc.setMaxWidth(45);
	    tc.setMinWidth(45);
	    getRendererComponent().setText(ColumnConstants.SELECT);
	    if (getCheckBoxHeader()) {
	        tc.setHeaderRenderer(getRendererComponent());
	    }            
	    setDragEnabled(false);
	    setCellSelectionEnabled(true);
	    setGridColor(Color.LIGHT_GRAY);
        addFocusListener(new FocusAdapter() {
		    @Override
		    public void focusLost(FocusEvent e) {
		        TableCellEditor tce = getCellEditor();
		        if(tce != null)
		        {
		        	setHasSynced(false);
		        	int row= getEditingRow();
		        	if(!changedRowIds.contains(row) && row != -1)
		        	{
		        		changedRowIds.add(row);
		        	}
		        }
		    }
		});
        addMouseListener(new MySelectionMouseListener());
        addKeyListener(new KeyAdapter() {
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
    
    

    /**
     * Show certain columns of a table
     */
    private void setShowColumns(){
		if(!showColumns.isEmpty())
		{
			for (int columnCounter = 0; columnCounter < getColumnCount(); columnCounter++) {
				if (!showColumns.contains(getColumnName(columnCounter))) {
					hideColumn(getColumnName(columnCounter));
				}
			}
		}
		if(getColumnNames().contains(ColumnConstants.ROW_NUM)){
			hideColumn(ColumnConstants.ROW_NUM);
		}
		
	}
   

    public void changeSelection(int rowIndex, int columnIndex, boolean toggle, boolean extend) {

		boolean rowShouldBeRemoved = false;
		int rowsBefore = getSelectedRowCount();
		if(!extend) {
		for(int row:getSelectedRows())
			if(row == rowIndex) {
				rowShouldBeRemoved = true;
				break;
			}
		}
		super.changeSelection(rowIndex, columnIndex, toggle, extend);
		int rowsAfter = getSelectedRowCount();
		if(!extend && rowShouldBeRemoved && rowsBefore == rowsAfter) {
			super.changeSelection(rowIndex, columnIndex, toggle, extend);
		}
	}
    
    

	public TableModel getTableModel(final Object row[][], String[] columnNames ){
		return new DefaultTableModel(row, columnNames) {
			private static final long serialVersionUID = 1L;
			@SuppressWarnings({ "rawtypes", "unchecked" })
			public Class getColumnClass(int column) {
				Class returnValue;
				try {					
					returnValue = getValueAt(0, column).getClass();					
					
				} catch (Exception e) {
					returnValue = Object.class;
				}
				return returnValue;
			}
			public boolean isCellEditable(int row, int col) {
				if(col == 0)
					return true;
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

    public class CheckBoxHeader extends JCheckBox implements TableCellRenderer, MouseListener {

        private static final long serialVersionUID = 1L;

        protected int column;

        protected boolean mousePressed = false;

        public ItemListener it1;

        public CheckBoxHeader(ItemListener itemListener) {

            setRendererComponent(this);
            this.it1 = itemListener;
            getRendererComponent().addItemListener(it1);
        }

        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                boolean hasFocus, int row, int column) {
            if (table != null) {

                JTableHeader header = table.getTableHeader();

                if (header != null) {

                    getRendererComponent().setForeground(header.getForeground());
                    getRendererComponent().setBackground(header.getBackground());
                    getRendererComponent().setFont(header.getFont());
                    getRendererComponent().setBorderPainted(true);
                    header.addMouseListener(getRendererComponent());
                }

            }

            setColumn(column);
            setBorder(UIManager.getBorder("TableHeader.cellBorder"));

            return getRendererComponent();
        }

        protected void setColumn(int column) {
            this.column = column;
        }

        public int getColumn() {
            return column;
        }

        protected void handleClickEvent(MouseEvent e) {

            if (mousePressed) {

                mousePressed = false;
                JTableHeader header = (JTableHeader) (e.getSource());
                JTable tableView = header.getTable();
                TableColumnModel columnModel = tableView.getColumnModel();
                int viewColumn = columnModel.getColumnIndexAtX(e.getX());
                int column = tableView.convertColumnIndexToModel(viewColumn);
                if (viewColumn == this.column && e.getClickCount() == 1 && column != -1) {
                    doClick();
                }

            }

        }

        public void mouseClicked(MouseEvent e) {
            handleClickEvent(e);
            ((JTableHeader) e.getSource()).repaint();
        }

        public void mousePressed(MouseEvent e) {
            mousePressed = true;
        }

        public void mouseReleased(MouseEvent e) {

        }

        public void mouseEntered(MouseEvent e) {

        }

        public void mouseExited(MouseEvent e) {

        }

    }

   

  public class MySelectionMouseListener extends MouseAdapter {
	  
	  	int pressedRow = -1;
	  	public void mousePressed(final MouseEvent mouseEvent) {
	  		if(selectAllThread!=null && !selectAllThread.isDone())
        	{
        		selectAllThread.cancel(true);
        	} 
	  		pressedRow = rowAtPoint(mouseEvent.getPoint());
	  	}
	  
        public void mouseReleased(final MouseEvent mouseEvent) {
            
        	      	
        	Runnable multiSelectionTask = new Runnable(){
				public void run() {
					int col = columnAtPoint(mouseEvent.getPoint());
					int releasedRow = rowAtPoint(mouseEvent.getPoint());
					if(getSelectionModel().getSelectionMode() == ListSelectionModel.MULTIPLE_INTERVAL_SELECTION){
						//drag or use shift to select
						if(pressedRow != releasedRow || (mouseEvent.getModifiers() & ActionEvent.SHIFT_MASK ) == ActionEvent.SHIFT_MASK ){
							for(int row : getSelectedRows()){
								checkedRows.add(row);
				        		setValueAt(true, row, 0);
							}
						}else{
							//click on checkbox
							if(col == 0){
								int row = rowAtPoint(mouseEvent.getPoint());
								boolean checked =  (Boolean) getValueAt(row,0);
								if(!checked){	
									checkedRows.remove(row);
									removeRowSelectionInterval(row,row);	
								}else{
									checkedRows.add(row);
								}
							// click on other columns
							}else{
								int row = rowAtPoint(mouseEvent.getPoint());
								boolean checked =  (Boolean) getValueAt(row,0);
								if(!checked){
									checkedRows.add(row);
									setValueAt(true, row, 0);							
								}else{
									setValueAt(false, row, 0);
									checkedRows.remove(row);
									removeRowSelectionInterval(row,row);	
								}							
							}
							for(int r: checkedRows){
								addRowSelectionInterval(r,r);
		        		    }
						}
					}else{
						for(int row : checkedRows){
							setValueAt(false,row,0);
						}
						checkedRows.clear();
						int row = getSelectedRow();
						checkedRows.add(row);
						addRowSelectionInterval(row,row);
						setValueAt(true,row,0);
						
					}
					
					
					repaint();				
				}        	
        	};
        	ThreadPool.getAGBThreadPool().executeTask(multiSelectionTask);
        }
 }
        
  
    
    @Override
    public Component prepareRenderer(TableCellRenderer renderer,int row,int column)
    {
       Component component=super.prepareRenderer(renderer,row, column);        
       if (hasSynced) {
			component.setBackground(new Color(255, 255, 224));	
			if(getValueAt(row,0)!=null && (Boolean)getValueAt(row,0))//  
			{
				component.setForeground(Color.BLACK);
	    	 
			}else{   
	    	    component.setForeground(Color.LIGHT_GRAY);
	    	  
			}
		}else{
			if(getValueAt(row,0)!=null && (Boolean)getValueAt(row,0))//  
		    {
		    	  component.setForeground(Color.BLACK);
		    	  component.setBackground(new Color(184, 207, 229));   	  
		     }else{   
		    	  component.setForeground(Color.BLACK);
		    	  component.setBackground(Color.WHITE);
		     }
		}
		return component;
    }
    

    @Override
    public void clearSelection() {
    	super.clearSelection();
    	  Runnable task = new Runnable() {
              public void run() {	
              	if(selectAllThread != null && !selectAllThread.isDone())
              	{
              		selectAllThread.cancel(true);
              	}
              	checkedRows.clear();           	
	            for (int rowCounter = 0; rowCounter < getRowCount(); rowCounter++) {	
	            	removeRowSelectionInterval(rowCounter, rowCounter); 
	            	setValueAt(false,rowCounter,0);
	            	if (Thread.interrupted()) {
         			   System.out.println("clear all has been interupted");
         			   return;
         			}
	            }
              	
  	            
              }
          };    	
        clearAllThread = ThreadPool.getAGBThreadPool().submitTask(task); // initilize selected row		
    }

    @Override
    public void selectAll() {    	
        super.selectAll(); 
        if(getSelectionModel().getSelectionMode() == ListSelectionModel.MULTIPLE_INTERVAL_SELECTION){
        	Runnable task = new Runnable() {
                public void run() {           	
                	
                	if(clearAllThread != null && !clearAllThread.isDone())
                	{
                		clearAllThread.cancel(true);
                	}
                	
            		for (int rowCounter = 0; rowCounter < getRowCount(); rowCounter++) {
            			setValueAt(true,rowCounter,0);
            			addRowSelectionInterval(rowCounter, rowCounter);
            			checkedRows.add(rowCounter);        			
            			if (Thread.interrupted()) {
            			   System.out.println("select all has been interupted");
            			   return;
            			}
            		}
                	          	
                }
            };
            selectAllThread = ThreadPool.getAGBThreadPool().submitTask(task);
        }      
	
    }

    @Override
    public void addRowSelectionInterval(int index0, int index1) {
        super.addRowSelectionInterval(index0, index1);
    }

    @Override
    public void removeRowSelectionInterval(int index0, int index1) {
        super.removeRowSelectionInterval(index0, index1);
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

    public MyItemListener getItemListener() {
        return itemListener;
    }

    public void setItemListener(MyItemListener itemListener) {
        this.itemListener = itemListener;
    }

    public CheckBoxHeader getRendererComponent() {
        return rendererComponent;
    }

    public void setRendererComponent(CheckBoxHeader rendererComponent) {
        this.rendererComponent = rendererComponent;
    }

    public List<String> getColumnNames() {
        return columnNames;
    }

    public void setColumnNames(List<String> columnNames) {
        this.columnNames = columnNames;
    }

    public List<Integer> getEditableColumns() {
        return editableColumns;
    }

    public void setEditableColumns(List<Integer> editableColumns) {
        this.editableColumns = editableColumns;
    }
    
    public List<String> getShowColumns() {
		return showColumns;
	}
    
	public CheckBoxIndexColumnTable() {
		itemListener = new MyItemListener();
		rendererComponent = new CheckBoxHeader(itemListener); 
	}
	
	public void setShowColumns(List<String> showColumns) {
		this.showColumns = showColumns;
	}
	public boolean hasSynced() {
		return hasSynced;
	}

	public void setHasSynced(boolean synced) {
		this.hasSynced = synced;
	}

	public List<Integer> getChangedRowIds() {
		return changedRowIds;
	}

	public void setChangedRowIds(List<Integer> changedRowIds) {
		this.changedRowIds = changedRowIds;
	}
	
	public Future getSelectAllThread() {
		return selectAllThread;
	}
	
  public Set<Integer> getCheckedRows() {
		return checkedRows;
	}

	public void setCheckedRows(Set<Integer> checkedRows) {
		this.checkedRows = checkedRows;
	}


}