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

import net.miginfocom.swing.MigLayout;

import org.accretegb.modules.util.LoggerUtils;
import org.accretegb.modules.util.ThreadPool;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.RowSorterEvent;
import javax.swing.event.RowSorterListener;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

import org.accretegb.modules.constants.ColumnConstants;
import org.accretegb.modules.customswingcomponent.Utils;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetAdapter;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.TooManyListenersException;
import java.util.logging.Level;

/**
 * @author nkumar
 * @author chinmay
 * @author Ningjing
 * This class contains a genric table panel
 * which has a jtable inside it with functionalities like
 * add a row, edit a row, delete a row, move
 * up, move dow, clear row, etc
 */
public class TableToolBoxPanel extends JPanel {

	private static final long serialVersionUID = 1L;

	private JButton addButton;
	private JButton deleteButton;
	private JButton moveUpButton;
	private JButton moveDownButton;
	private JButton editButton;
	private JButton uploadButton;
	private JButton clearButton;
	private JButton refreshButton;
	private JButton expandButton;
	private JButton checkDuplicates;
	private JLabel numberOfRows;

	private JPanel selectionPanel;
	private JLabel all;
	private JLabel none;
	private JPanel topHorizonPanel;
	private JPanel bottomHorizonPanel;

	private ImageIcon deleteIcon;
	private ImageIcon addIcon;
	private ImageIcon moveUpIcon;
	private ImageIcon moveDownIcon;
	private ImageIcon deleteColorIcon;
	private ImageIcon addColorIcon;
	private ImageIcon editIcon;
	private ImageIcon editColorIcon;
	private ImageIcon uploadIcon;
	private ImageIcon uploadColorIcon;
	private ImageIcon clearIcon;
	private ImageIcon clearColorIcon;
	private ImageIcon refreshIcon;
	private ImageIcon refreshColorIcon;
	private ImageIcon downIcon;
	private ImageIcon searchIcon;
	private ImageIcon expandIcon;
	private ImageIcon expandColorIcon;
	
	private List<String> horizontalList;
	private List<String> verticalList;
	
	private String searchvalue;
	private JTextField searchTextField;
	private JButton searchButton;
	private JLabel filterDonors;
	private JLabel searchFieldsByCriteria[];
	
	private JButton columnSelector;
	private JComboBox tableSubset;
	private List<String> rangeFilterColumnList;
	private boolean isDraggable;	
	private CheckBoxIndexColumnTable table;


	private enum Buttons {
		ADD, DELETE, EDIT, UPLOAD, CLEAR, MOVEUP, MOVEDOWN, SEARCH, GAP, 
		CHECKDUPLICATES,SELECTION, REFRESH, COLUMNSELECTOR, SUBSET,NEWLINE,EXPAND, COUNTER
	}


	public void initializeIcons() {
		ClassLoader loader = TableToolBoxPanel.class.getClassLoader();
		deleteIcon = new ImageIcon(loader.getResource("images/delete.png"));		
		addIcon = new ImageIcon(loader.getResource("images/add.png"));		
		moveUpIcon = new ImageIcon(loader.getResource("images/up.png"));
		moveDownIcon = new ImageIcon(loader.getResource("images/down.png"));
		deleteColorIcon = new ImageIcon(loader.getResource("images/deleteColor.png"));
		addColorIcon = new ImageIcon(loader.getResource("images/addColor.png"));	
		editIcon = new ImageIcon(loader.getResource("images/edit.png"));
		editColorIcon = new ImageIcon(loader.getResource("images/editColor.png"));
		uploadIcon = new ImageIcon(loader.getResource("images/upload.png"));	
		uploadColorIcon = new ImageIcon(loader.getResource("images/uploadColor.png"));	
		clearIcon = new ImageIcon(loader.getResource("images/clear.png"));	
		clearColorIcon = new ImageIcon(loader.getResource("images/clearColor.png"));
		refreshIcon = new ImageIcon(loader.getResource("images/reload.png"));
		refreshColorIcon = new ImageIcon(loader.getResource("images/reloadColor.png"));
		downIcon = new ImageIcon(loader.getResource("images/down.png"));		
		searchIcon = new ImageIcon(loader.getResource("images/Search.png"));
		expandIcon = new ImageIcon(loader.getResource("images/expandColor.png"));
		expandColorIcon = new ImageIcon(loader.getResource("images/expandColor.png"));
	}
	

    /**
     * initialize the tablePanel
     */
    public void initialize() {
        initializeIcons();
        setLayout(new MigLayout("insets 5 5 5 5, gapx 5"));
        add(getVerticalToolBox(), "growy, spany, h 100%");
        add(getHorizontalToolBox(), "growx, spanx, w 100%, wrap");
        add(getTablePanel(), "w 100%, h 100%, grow, span, wrap");
        add(getButtomHorizontalPanel(),"growx, spanx, w 100%, wrap");
    }

    /**
     * sets panel with jtable inside a scrollpane
     * @return the panel which has jtable
     */
    private JPanel getTablePanel() {
        JPanel tablePanel = new JPanel();
        tablePanel.setLayout(new GridBagLayout());
        tablePanel.setLayout(new BorderLayout());
        getTable().setMinimumSize(new Dimension(5, 2));
        getTable().getTableHeader().setReorderingAllowed(false);
        //getTable().setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
        getTable().setDragEnabled(isDraggable());
        getTable().setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        tablePanel.add(new JScrollPane(getTable(),JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
        		JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED), BorderLayout.CENTER);
        getTable().addComponentListener(new ComponentAdapter() {
		    @Override
		    public void componentResized(final ComponentEvent e) {
		    	if (getTable().getPreferredSize().width <=  getTable().getParent().getWidth()) {
		        	getTable().setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
		        } else {
		        	getTable().setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		        	for (int i = 0; i <  getTable().getColumnCount(); i++) {
		                adjustColumnSizes( getTable(), i, 2);
		            }
		        }		    		
		    }
		});
        getTable().getParent().addComponentListener(new ComponentAdapter(){
        	 public void componentResized(final ComponentEvent e) {
        		 if(getTable().getSize().width < getTable().getParent().size().width){
		        		getTable().setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
		          }
        	 }
        });
        setActionListenerTable();       
        return tablePanel;
    }
    
    
    
    public void adjustColumnSizes(JTable table, int column, int margin) {
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
            try{
            	if(String.valueOf(table.getValueAt(r, column)).equalsIgnoreCase("null")){
            		//ignore NULL
            	}
            	else if(String.valueOf(table.getColumnModel().getColumn(column).getIdentifier()).contains(ColumnConstants.DATE)){          	            	
            		width = 100;
            	}else{
            		comp = renderer.getTableCellRendererComponent(table, table.getValueAt(r, column), false, false, r, column);                
            		int currentWidth = comp.getPreferredSize().width;
                    width = Math.max(width, currentWidth);
	            }
            }
           catch(Exception e){
        	   LoggerUtils.log(Level.INFO,"adjsut column exception " + String.valueOf(table.getColumnModel().getColumn(column).getIdentifier())
      			+ " " + table.getValueAt(r, column));
               	System.out.println("adjsut column exception " + String.valueOf(table.getColumnModel().getColumn(column).getIdentifier())
               			+ " " + table.getValueAt(r, column) + " " + e.getMessage());
           }
            
            
        }

        width += 2 * margin;

        col.setPreferredWidth(width);
        col.setWidth(width);
    }

    /**
     * this sets a vertical panel with buttons
     * @return - the panel with vertical buttons
     */
	private JPanel getVerticalToolBox() {

		JPanel buttonsPanel = new JPanel(new MigLayout("insets 5 0 0 0, gapy 5"));
		if(verticalList==null)
			return buttonsPanel;
		
		if(horizontalList != null)
			buttonsPanel.add(new JLabel(""), "h 24:24:24, w 24:24:24, wrap");
		
		for(String button:verticalList) {
			Buttons current = Buttons.valueOf(button.toUpperCase());
			switch(current) {
			case ADD: 
				addButton = new ImageButton(getAddIcon(), getAddColorIcon());
				setAddButton(addButton);
				addButton.setToolTipText("Add a row");
				buttonsPanel.add(getAddButton(), "h 24:24:24, w 24:24:24, wrap");
				break;
			case DELETE:
				deleteButton = new ImageButton(getDeleteIcon(), getDeleteColorIcon());
				setDeleteButton(deleteButton);
				deleteButton.setToolTipText("Delete selected row(s)");
				buttonsPanel.add(getDeleteButton(), "h 24:24:24, w 24:24:24, wrap");
				break;
			case EDIT:
				editButton = new ImageButton(editIcon, editColorIcon);
				editButton.setToolTipText("Edit a selected row");
				buttonsPanel.add(editButton, "h 24:24:24, w 24:24:24, wrap");
				break;
			case UPLOAD:
				uploadButton = new ImageButton(uploadIcon, uploadColorIcon);
				uploadButton.setToolTipText("Upload selected data to above table");
				buttonsPanel.add(uploadButton, "h 24:24:24, w 24:24:24, wrap");
				break;
			case CLEAR:
				clearButton = new ImageButton(clearIcon, clearColorIcon);
				clearButton.setToolTipText("Clear uploaded data");
				buttonsPanel.add(clearButton, "h 24:24:24, w 24:24:24, wrap");
				break;
			case REFRESH:
				refreshButton = new ImageButton(refreshIcon, refreshColorIcon);
				buttonsPanel.add(refreshButton, "h 24:24:24, w 24:24:24, wrap");
				break;				
			case MOVEUP:
				JButton moveupButton = new JButton(getMoveUpIcon());
				setMoveUpButton(moveupButton);
				setMouseListenerMoveUpButton();
				moveupButton.setToolTipText("Move selected row(s) up");
				buttonsPanel.add(getMoveUpButton(), "h 24:24:24, w 24:24:24, wrap");
				break;
			case MOVEDOWN:
				JButton movedownButton = new JButton(getMoveDownIcon());
				setMoveDownButton(movedownButton);
				setMouseListenerMoveDownButton();
				movedownButton.setToolTipText("Move selected row(s) down");
				buttonsPanel.add(getMoveDownButton(), "h 24:24:24, w 24:24:24, wrap");
				break;
			case SEARCH:
				searchvalue = new String();
				searchTextField = new JTextField();
				searchButton = new JButton();
				filterDonors = new JLabel();
				initSearchFieldsByCriteria();
				buttonsPanel.add(getSearchPanel(), "wrap");
				break;
	
			case GAP:
				buttonsPanel.add(new JLabel(""), "pushy, wrap");
				break;
			
			default: LoggerUtils.log(Level.INFO, "No tool called: " + button);
				break;
			}
		}
		return buttonsPanel;
	}
	
	/**
     * sets the horizontal tool box of buttons
     * @return - the panel with horizontal tool box of buttons
     */
	public JPanel getHorizontalToolBox() {

		JPanel buttonsPanel = new JPanel(new MigLayout("insets 0, gapx 5"));
		this.topHorizonPanel = buttonsPanel;
		if(horizontalList==null)
			return buttonsPanel;
		
		for(String button:horizontalList) {
			Buttons current = Buttons.valueOf(button.toUpperCase());
			switch(current) {
				case ADD: 
					addButton = new ImageButton(getAddIcon(), getAddColorIcon());
					setAddButton(addButton);
					addButton.setToolTipText("Add a row");
					buttonsPanel.add(getAddButton(), "h 24:24:24, w 24:24:24");
					break;
				case DELETE:
					deleteButton = new ImageButton(getDeleteIcon(), getDeleteColorIcon());
					setDeleteButton(deleteButton);
					deleteButton.setToolTipText("Delete selected row(s)");
					buttonsPanel.add(getDeleteButton(), "h 24:24:24, w 24:24:24");
					break;
				case EDIT:
					editButton = new ImageButton(editIcon, editColorIcon);
					editButton.setToolTipText("Edit a selected row");
					buttonsPanel.add(editButton, "h 24:24:24, w 24:24:24");
					break;
				case UPLOAD:
					uploadButton = new ImageButton(uploadIcon, uploadColorIcon);
					uploadButton.setToolTipText("Upload selected data to above table");
					buttonsPanel.add(uploadButton, "h 24:24:24, w 24:24:24");
					break;
				case CLEAR:
					clearButton = new ImageButton(clearIcon, clearColorIcon);
					clearButton.setToolTipText("Clear uploaded data");
					buttonsPanel.add(clearButton, "h 24:24:24, w 24:24:24");
					break;
				case REFRESH:
					refreshButton = new ImageButton(refreshIcon, refreshColorIcon);
					buttonsPanel.add(refreshButton, "h 24:24:24, w 24:24:24");
					break;		
				case MOVEUP:
					setMoveUpButton(new JButton(getMoveUpIcon()));
					setMouseListenerMoveUpButton();
					buttonsPanel.add(getMoveUpButton(), "h 24:24:24, w 24:24:24");
					break;
				case MOVEDOWN:
					setMoveDownButton(new JButton(getMoveDownIcon()));
					setMouseListenerMoveDownButton();
					buttonsPanel.add(getMoveDownButton(), "h 24:24:24, w 24:24:24");
					break;
				case SEARCH:
					searchvalue = new String();
					searchTextField = new JTextField();
					searchButton = new JButton();
					filterDonors = new JLabel();
					searchButton.setToolTipText("Select criteria to filter");
					initSearchFieldsByCriteria();
					buttonsPanel.add(getSearchPanel());
					break;
				case SELECTION:
					selectionPanel = new JPanel();
					generateSelectionPanel();
					buttonsPanel.add(getSelectionPanel());
					break;
	            case COLUMNSELECTOR:
				    columnSelector = new JButton("Show Columns");
					initializeColumnSelector();
					buttonsPanel.add(columnSelector);
	                break;
	            case CHECKDUPLICATES:
				    checkDuplicates = new JButton("Check Duplicates");
					initializeCheckDuplicates();
					buttonsPanel.add(checkDuplicates);
	                break;
				case SUBSET:
					tableSubset = new JComboBox();
					buttonsPanel.add(new JLabel("Subsets:"));
					buttonsPanel.add(tableSubset);
					break;
				case GAP:
					buttonsPanel.add(new JLabel(""), "pushx");
					break;
				case NEWLINE:
					buttonsPanel.add(new JLabel(""), "wrap");
					break;
				case EXPAND:
					expandButton = new ImageButton(expandIcon, expandColorIcon);
					buttonsPanel.add(expandButton, "h 24:24:24, w 24:24:24");
					expandButton.setToolTipText("Expand table");
					break;
				case COUNTER:
					break;
				default: 
					LoggerUtils.log(Level.INFO, "No tool called: " + button);
					break;
			}
		}
		return buttonsPanel;
	}
	
	/**
     * sets the horizontal buttom panel
     */
	private JPanel getButtomHorizontalPanel() {

		JPanel buttomPanel = new JPanel(new MigLayout("insets 0, gapx 5"));
		this.bottomHorizonPanel = buttomPanel;
		if(horizontalList==null)
			return buttomPanel;
		
		if(horizontalList.contains("counter")){			
			JLabel label = new JLabel("The current number of rows in this table:");
			buttomPanel.add(label, "al left");			
			numberOfRows = new JLabel("0");
			setNumberOfRows(numberOfRows);
			buttomPanel.add(getNumberOfRows());
		}
		return buttomPanel;
	}
	
	private void generateSelectionPanel() {
		all = new JLabel("<HTML><FONT color = #00A0FF>all</FONT></HTML>");		
		JLabel separator = new JLabel(" | ");		
		none = new JLabel("<HTML><FONT color = #00A0FF>none</FONT></HTML>");
		all.addMouseListener(new MouseAdapter(){
			@Override
			public void mouseEntered(MouseEvent arg0) {				
				all.setText("<HTML><FONT color = #00A0FF><U>all<U></FONT></HTML>");					
			}
			@Override
			public void mouseExited(MouseEvent arg0) {				
				all.setText("<HTML><FONT color = #00A0FF>all</FONT></HTML>");					
			}
			@Override
			public void mouseClicked(MouseEvent arg0) {		
					table.selectAll();
			}			
		});
		
		none.addMouseListener(new MouseAdapter(){
			@Override
			public void mouseEntered(MouseEvent arg0) {				
				none.setText("<HTML><FONT color = #00A0FF><U>none<U></FONT></HTML>");				
			}
			@Override
			public void mouseExited(MouseEvent arg0) {				
				none.setText("<HTML><FONT color = #00A0FF>none</FONT></HTML>");					
			}
			@Override
			public void mouseClicked(MouseEvent arg0) {		
				table.clearSelection();
			}			
		});
		selectionPanel.add(all);
		selectionPanel.add(separator);
		selectionPanel.add(none);
	}

	private void initSearchFieldsByCriteria() {
		JLabel searchFieldsByCriteria[] = new JLabel[getTable().getColumnCount()];
		for(int columnCounter=0; columnCounter < getTable().getColumnCount(); columnCounter++) {
			searchFieldsByCriteria[columnCounter] = new JLabel("Search by "+getTable().getColumnName(columnCounter));
		}
	}	
	
	private void updateRowCount(){
		if(getNumberOfRows() != null)
		{
			getNumberOfRows().setText(String.valueOf(getTable().getRowCount()));
		}
	}
	private JPanel getSearchPanel() {
		final JPopupMenu popup = new JPopupMenu();
		int columnCounter = 0;
		for (columnCounter = 0; columnCounter < getTable().getColumnCount(); columnCounter++) {
			final String columnName = new String(getTable().getColumnName(columnCounter));
			popup.add(new JMenuItem(new AbstractAction(columnName) {

				private static final long serialVersionUID = 1L;

				public void actionPerformed(ActionEvent e) {
					searchvalue = columnName;
					filterDonors.setText("Search by " + columnName);
					if(columnName.equals(ColumnConstants.WEIGHT) || columnName.equals(ColumnConstants.NUMBER_OF_SEEDS))
					{
						rangeFilter();
					}
					else{
					   newFilter();
					}

					updateRowCount();
				}
						
			}));
		}

		final int columnCount = columnCounter;
		popup.add(new JMenuItem(new AbstractAction("All") {

			public void actionPerformed(ActionEvent e) {

				searchvalue = "All";

				for (int i = 0; i < searchFieldsByCriteria.length; i++)
					searchFieldsByCriteria[2].setVisible(i == columnCount);
				filterDonors.setText("Search by All");
				newFilter();

			}

		}));

		filterDonors = new JLabel("Search by All");
		searchvalue = "All";

		getSearchTextField().setPreferredSize(new Dimension(180, 10));
		getSearchTextField().setText("");
		getSearchTextField().getDocument().addDocumentListener(
				new DocumentListener() {
					public void changedUpdate(DocumentEvent e) {
						if(searchvalue.equals(ColumnConstants.WEIGHT) || searchvalue.equals(ColumnConstants.NUMBER_OF_SEEDS))
						{
							rangeFilter();
						}else{
							newFilter();
						}
						updateRowCount();
					}

					public void insertUpdate(DocumentEvent e) {
						if(searchvalue.equals(ColumnConstants.WEIGHT) || searchvalue.equals(ColumnConstants.NUMBER_OF_SEEDS))
						{
							rangeFilter();
						}
						else{
							newFilter();
						}
						updateRowCount();
					}

					public void removeUpdate(DocumentEvent e) {
						if(searchvalue.equals(ColumnConstants.WEIGHT) || searchvalue.equals(ColumnConstants.NUMBER_OF_SEEDS))
						{
							rangeFilter();
						}else{
							newFilter();
						}
						updateRowCount();
					}
				});

		JPanel searchPanel = new JPanel(new MigLayout("insets 0, gapx 0"));
		JButton donorSearchImage = new JButton(searchIcon);
		donorSearchImage.setEnabled(false);
		donorSearchImage.setPreferredSize(new Dimension(20, 10));
		donorSearchImage.setBorder(new EmptyBorder(new Insets(1, 1, 1, 1)));
		getSearchButton().setIcon(downIcon);
		getSearchButton().setPreferredSize(new Dimension(20, 10));
		getSearchButton().addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				popup.show(getSearchButton(), 0, getSearchButton().getHeight());
			}
		});

		searchPanel.add(donorSearchImage, "gaptop 10");
		searchPanel.add(getSearchTextField(), "gaptop 10");
		searchPanel.add(getSearchButton(), "gaptop 10, w 24:24:24, h 20");
		searchPanel.add(getFilterDonors(), "gapleft 10");

		return searchPanel;
	}

	@SuppressWarnings("unchecked")
	public void newFilter() {
		RowFilter<TableModel, Object> rf = null;
		try {
			String searchedValue = getSearchTextField().getText();
			for (int columnCounter = 0; columnCounter < getTable().getColumnCount(); columnCounter++) {
				if (searchvalue.equals(getTable().getColumnName(columnCounter))) {
					rf = RowFilter.regexFilter("(?i)" + searchedValue, columnCounter);
				}
			}

			if (rf == null) {
				rf = RowFilter.regexFilter("(?i)" + searchedValue);
			}

		} catch (java.util.regex.PatternSyntaxException e) {
			return;
		}
		((DefaultRowSorter<TableModel, Integer>) getTable().getRowSorter()).setRowFilter(rf);
	}
	
	public void setActionListenerAddButton() {

		getAddButton().addActionListener(new ActionListener(){	
			public void actionPerformed(ActionEvent e) {	
				Object row[] = {false};
				DefaultTableModel model = (DefaultTableModel) getTable().getModel();

				for(int rowCounter=0;rowCounter<model.getRowCount();rowCounter++){
					model.setValueAt(false, rowCounter, 0);
				}
				model.addRow(row);	
				getDeleteButton().setEnabled(false);
				if(getMoveDownButton() != null)
					getMoveDownButton().setEnabled(false);
				if(getMoveUpButton() != null)
					getMoveUpButton().setEnabled(false);
				getTable().clearSelection();
			}	
		});
	}

	public void setActionListenerDeleteButton() {
		getDeleteButton().addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				Runnable deleteTask =
						new Runnable(){
							public void run() {
								if(getTable().getSelectAllThread() != null && !getTable().getSelectAllThread().isDone()){
									getTable().getSelectAllThread().cancel(true);
								}								
								int rows[] = getTable().getSelectedRows();
								DefaultTableModel model = (DefaultTableModel) getTable().getModel();
								if(rows.length == getTable().getRowCount()){
									model.setRowCount(0);
									getTable().getCheckedRows().clear();
								}else{
									for(int rowCounter=0;rowCounter<rows.length;rowCounter++){
										int deleteRow = rows[rowCounter]-rowCounter;	
										getTable().getCheckedRows().remove(deleteRow);
										model.removeRow(getTable().convertRowIndexToModel(deleteRow));
									}
									getTable().clearSelection();									
									if(getMoveDownButton()!=null)
									{
										getMoveDownButton().setEnabled(false);
									}
									if(getMoveUpButton()!=null)
									{
										getMoveUpButton().setEnabled(false);
									}
									getTable().clearSelection();
								 }
								updateRowCount();
							}						
						};
				ThreadPool.getAGBThreadPool().executeTask(deleteTask);
				}

		});
	}
	
	protected void setMouseListenerMoveUpButton() {
		getMoveUpButton().addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent arg0) {
				int rows[] = getTable().getSelectedRows();
				getTable().clearSelection();
				
				for(int row : rows) {
					for(int columnCounter = 0; columnCounter < getTable().getColumnCount(); columnCounter++) {
						Object temp = getTable().getValueAt(row, columnCounter);
						getTable().setValueAt(getTable().getValueAt(row-1, columnCounter), row, columnCounter);
						getTable().setValueAt(temp, row-1, columnCounter);
					}
					getTable().setValueAt(true, row-1, 0);
					getTable().addRowSelectionInterval(row-1, row-1);
				}
				
				rows = getTable().getSelectedRows();
				Arrays.sort(rows);
				if(rows[0] == 0) {
					getMoveUpButton().setEnabled(false);
				} else {
					getMoveUpButton().setEnabled(true);
				}
				if(rows[rows.length-1] == getTable().getRowCount()-1) {
					getMoveDownButton().setEnabled(false);
				} else {
					getMoveDownButton().setEnabled(true);
				}
			}
		});
	}
	
	protected void setMouseListenerMoveDownButton() {
		getMoveDownButton().addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent arg0) {
				int rows[] = getTable().getSelectedRows();
				getTable().clearSelection();		
				Arrays.sort(rows);
				for(int i = 0; i < rows.length/2; i++) {
					int temp = rows[i];
					rows[i] = rows[rows.length-1 - i];
					rows[rows.length-1 - i] = temp;
				}
				
				for(int row : rows)	{
					for(int columnCounter = 0; columnCounter < getTable().getColumnCount(); columnCounter++) {
						Object temp = getTable().getValueAt(row, columnCounter);
						getTable().setValueAt(getTable().getValueAt(row+1, columnCounter), row, columnCounter);
						getTable().setValueAt(temp, row+1, columnCounter);
					}
					getTable().setValueAt(true, row+1, 0);
					getTable().addRowSelectionInterval(row+1, row+1);
				}
				
				rows = getTable().getSelectedRows();
				Arrays.sort(rows);
				if(rows.length!=0) {
					if(rows[0] == 0) {
						getMoveUpButton().setEnabled(false);
					} else {
						getMoveUpButton().setEnabled(true);
					}
					if(rows[rows.length-1] == getTable().getRowCount()-1) {
						getMoveDownButton().setEnabled(false);
					} else {
						getMoveDownButton().setEnabled(true);
					}
				}
			}
			
		});
	}
	
	public void setActionListenerTable() {
		if(isDraggable()) {
			getTable().setDropTarget(new DropTarget() {
				
				int countOffsetFrom = -1;
				private static final long serialVersionUID = 1L;
				
				@Override
				public void dragEnter(DropTargetDragEvent dtde) {
					if(countOffsetFrom != -1) //if entering from outside of component. So this isn't a new drag event
						return;
					countOffsetFrom =  getTable().rowAtPoint(dtde.getLocation());
				}

				public void drop(DropTargetDropEvent dtde) {
					Transferable t = dtde.getTransferable();
					DataFlavor[] d = t.getTransferDataFlavors();
					String content = null;
					try {
						content = (String)t.getTransferData(d[0]);
					} catch (UnsupportedFlavorException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					}

					int countOffsetTo = getTable().rowAtPoint(dtde.getLocation());
					int selectedRows[] = getTable().getSelectedRows();
					int offset = countOffsetTo-countOffsetFrom;
					if(offset == 0 //no need to shift
							|| selectedRows[0]+offset < 0 //if upward shifting results in rows going out of bounds
							|| selectedRows[selectedRows.length-1]+offset >= getTable().getRowCount()) //downward shifting results rows going out of bounds
						return; //TODO maybe want to show error here?
					int dy = offset/Math.abs(offset);
					if(offset > 0) {
						Arrays.sort(selectedRows);
						for(int row = 0; row < selectedRows.length/2; row++) {
							int temp = selectedRows[row];
							selectedRows[row] = selectedRows[selectedRows.length-1 - row];
							selectedRows[selectedRows.length-1 - row] = temp;
						}
					}
					getTable().clearSelection();
					for(int row:selectedRows) {
						getTable().setValueAt(true, row+offset, 0);
						getTable().addRowSelectionInterval(row+offset, row+offset);
					}
					for(int turn=0; turn<Math.abs(offset);turn++) {
						for(int row=0; row<selectedRows.length; row++) {
							for(int columnCounter=1; columnCounter<getTable().getColumnCount(); columnCounter++) {
								Object temp = getTable().getValueAt(selectedRows[row], columnCounter);
								getTable().setValueAt(getTable().getValueAt(selectedRows[row]+dy, columnCounter),selectedRows[row], columnCounter);
								getTable().setValueAt(temp, selectedRows[row]+dy, columnCounter);	
							}
							selectedRows[row]+=dy;
						}
					}
					countOffsetFrom = -1;

					int []rows = getTable().getSelectedRows();
					Arrays.sort(rows);
					if(rows.length!=0) {
						if(rows[0] == 0) {
							getMoveUpButton().setEnabled(false);
						} else {
							getMoveUpButton().setEnabled(true);
						}
						if(rows[rows.length-1] == getTable().getRowCount()-1) {
							getMoveDownButton().setEnabled(false);
						} else {
							getMoveDownButton().setEnabled(true);
						}
					}

				}
			});

			try {
				getTable().getDropTarget().addDropTargetListener(new DropTargetAdapter(){
					@Override
					public void dragOver(DropTargetDragEvent event) {
						Rectangle visibleRectangle = getTable().getVisibleRect();
						Point location = event.getLocation();
						if(location.getY() > visibleRectangle.getY() + visibleRectangle.getHeight()*0.9) {
							visibleRectangle.translate(0, getTable().getRowHeight());
							getTable().scrollRectToVisible(visibleRectangle);
						}
						if(location.getY() < visibleRectangle.getY() + visibleRectangle.getHeight()*0.1) {
							visibleRectangle.translate(0, -getTable().getRowHeight());
							getTable().scrollRectToVisible(visibleRectangle);
						}
					}

					public void drop(DropTargetDropEvent dtde) {
						
					}
				});
			} catch (TooManyListenersException e) {
				if(LoggerUtils.isLogEnabled())
					LoggerUtils.log(Level.INFO, e.toString());
			}
		}
	}
	
	public void copyTable(CheckBoxIndexColumnTable sourceTable, final CheckBoxIndexColumnTable targetTable){
		DefaultTableModel tableModel = (DefaultTableModel) sourceTable.getModel();		
		List<String> showedColumns = sourceTable.getShowColumns();
		targetTable.setColumnNames(sourceTable.getColumnNames());	
		targetTable.setModel(tableModel);
		targetTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		targetTable.setRowSorter(sourceTable.getRowSorter());
		targetTable.initilizeByCopy();
		targetTable.setSelectionModel(sourceTable.getSelectionModel());			
		for(String col: sourceTable.getColumnNames()){
			if(showedColumns.contains(col) && !col.equals(ColumnConstants.ROW_NUM) ){
				targetTable.showColumn(col);
			}else{
				targetTable.hideColumn(col);
			}
		}
		
	}

	public JButton getAddButton() {
		return addButton;
	}

	public void setAddButton(JButton addButton) {
		this.addButton = addButton;
	}

	public JButton getDeleteButton() {
		return deleteButton;
	}

	public void setDeleteButton(JButton deleteButton) {
		this.deleteButton = deleteButton;
	}

	public JButton getRefreshButton() {
		return refreshButton;
	}

	public void setRefreshButton(JButton refreshButton) {
		this.refreshButton = refreshButton;
	}

	public JButton getMoveUpButton() {
		return moveUpButton;
	}

	public void setMoveUpButton(JButton moveUpButton) {
		this.moveUpButton = moveUpButton;
	}

	public JButton getMoveDownButton() {
		return moveDownButton;
	}

	public void setMoveDownButton(JButton moveDownButton) {
		this.moveDownButton = moveDownButton;
	}

	public ImageIcon getDeleteIcon() {
		return deleteIcon;
	}

	public void setDeleteIcon(ImageIcon deleteIcon) {
		this.deleteIcon = deleteIcon;
	}

	public ImageIcon getAddIcon() {
		return addIcon;
	}

	public void setAddIcon(ImageIcon addIcon) {
		this.addIcon = addIcon;
	}

	public ImageIcon getMoveUpIcon() {
		return moveUpIcon;
	}

	public void setMoveUpIcon(ImageIcon moveUpIcon) {
		this.moveUpIcon = moveUpIcon;
	}

	public ImageIcon getMoveDownIcon() {
		return moveDownIcon;
	}

	public void setMoveDownIcon(ImageIcon moveDownIcon) {
		this.moveDownIcon = moveDownIcon;
	}
	
	public JButton getEditButton() {
		return editButton;
	}

	public void setEditButton(JButton editButton) {
		this.editButton = editButton;
	}

	public JButton getUploadButton() {
		return uploadButton;
	}

	public void setUploadButton(JButton uploadButton) {
		this.uploadButton = uploadButton;
	}

	public JButton getClearButton() {
		return clearButton;
	}

	public void setClearButton(JButton clearButton) {
		this.clearButton = clearButton;
	}

	public ImageIcon getRefreshIcon() {
		return refreshIcon;
	}

	public void setRefreshIcon(ImageIcon refreshIcon) {
		this.refreshIcon = refreshIcon;
	}

	public ImageIcon getDeleteColorIcon() {
		return deleteColorIcon;
	}

	public void setDeleteColorIcon(ImageIcon deleteColorIcon) {
		this.deleteColorIcon = deleteColorIcon;
	}

	public ImageIcon getAddColorIcon() {
		return addColorIcon;
	}

	public void setAddColorIcon(ImageIcon addColorIcon) {
		this.addColorIcon = addColorIcon;
	}

	public ImageIcon getEditIcon() {
		return editIcon;
	}

	public void setEditIcon(ImageIcon editIcon) {
		this.editIcon = editIcon;
	}

	public ImageIcon getEditColorIcon() {
		return editColorIcon;
	}

	public void setEditColorIcon(ImageIcon editColorIcon) {
		this.editColorIcon = editColorIcon;
	}

	public ImageIcon getUploadIcon() {
		return uploadIcon;
	}

	public void setUploadIcon(ImageIcon uploadIcon) {
		this.uploadIcon = uploadIcon;
	}

	public ImageIcon getUploadColorIcon() {
		return uploadColorIcon;
	}

	public void setUploadColorIcon(ImageIcon uploadColorIcon) {
		this.uploadColorIcon = uploadColorIcon;
	}

	public ImageIcon getClearIcon() {
		return clearIcon;
	}

	public void setClearIcon(ImageIcon clearIcon) {
		this.clearIcon = clearIcon;
	}

	public ImageIcon getClearColorIcon() {
		return clearColorIcon;
	}

	public void setClearColorIcon(ImageIcon clearColorIcon) {
		this.clearColorIcon = clearColorIcon;
	}

	public ImageIcon getRefreshColorIcon() {
		return refreshColorIcon;
	}

	public void setRefreshColorIcon(ImageIcon refreshColorIcon) {
		this.refreshColorIcon = refreshColorIcon;
	}

	public ImageIcon getDownIcon() {
		return downIcon;
	}

	public void setDownIcon(ImageIcon downIcon) {
		this.downIcon = downIcon;
	}

	public ImageIcon getSearchIcon() {
		return searchIcon;
	}

	public void setSearchIcon(ImageIcon searchIcon) {
		this.searchIcon = searchIcon;
	}

	public CheckBoxIndexColumnTable getTable() {
		return table;
	}
	
	public void setTable(CheckBoxIndexColumnTable table) {
		this.table = table;
	}
	
	public List<String> getHorizontalList() {
		return horizontalList;
	}

	public void setHorizontalList(List<String> buttons) {
		this.horizontalList = buttons;
	}
	
	public List<String> getVerticalList() {
		return verticalList;
	}

	public void setVerticalList(List<String> buttons) {
		this.verticalList = buttons;
	}

	public String getSearchvalue() {
		return searchvalue;
	}

	public void setSearchvalue(String searchvalue) {
		this.searchvalue = searchvalue;
	}

	public JTextField getSearchTextField() {
		return searchTextField;
	}

	public void setSearchTextField(JTextField searchTextField) {
		this.searchTextField = searchTextField;
	}

	public JButton getSearchButton() {
		return searchButton;
	}

	public void setSearchButton(JButton searchButton) {
		this.searchButton = searchButton;
	}

	public JLabel getFilterDonors() {
		return filterDonors;
	}

	public void setFilterDonors(JLabel filterDonors) {
		this.filterDonors = filterDonors;
	}

	public JLabel[] getSearchFieldsByCriteria() {
		return searchFieldsByCriteria;
	}

	public void setSearchFieldsByCriteria(JLabel[] searchFieldsByCriteria) {
		this.searchFieldsByCriteria = searchFieldsByCriteria;
	}

	public JPanel getSelectionPanel() {
		return selectionPanel;
	}

	public void setSelectionPanel(JPanel selectionPanel) {
		this.selectionPanel = selectionPanel;
	}

	public JLabel getAll() {
		return all;
	}

	public void setAll(JLabel all) {
		this.all = all;
	}

	public JLabel getNone() {
		return none;
	}

	public void setNone(JLabel none) {
		this.none = none;
	}

	public boolean isDraggable() {
		return isDraggable;
	}

	public void setDraggable(boolean isDraggable) {
		this.isDraggable = isDraggable;
	}
    
    public List<String> getRangeFilterColumnList() {
		return rangeFilterColumnList;
	}

	public void setRangeFilterColumnList(List<String> rangeFilterColumnList) {
		this.rangeFilterColumnList = rangeFilterColumnList;
	}
	
    public JComboBox getTableSubset() {
		return tableSubset;
	}


	public void setTableSubset(JComboBox tableSubset) {
		this.tableSubset = tableSubset;
	}
	
	public JButton getExpandButton() {
		return expandButton;
	}


	public void setExpandButton(JButton expandButton) {
		this.expandButton = expandButton;
	}

	public JLabel getNumberOfRows() {
		return numberOfRows;
	}
	public JPanel getTopHorizonPanel() {
		return topHorizonPanel;
	}


	public JPanel getBottomHorizonPanel() {
		return bottomHorizonPanel;
	}

	public void setNumberOfRows(JLabel numberOfRows) {
		this.numberOfRows = numberOfRows;
	}
    
    /**
     * sets the selection panel labels such as all, None etc.
     */
    private void setSelectionPanel() {
        all = new JLabel("<HTML><FONT color = #00A0FF>all</FONT></HTML>");
        JLabel separator = new JLabel(" | ");
        none = new JLabel("<HTML><FONT color = #00A0FF>none</FONT></HTML>");
        all.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent arg0) {
                all.setText("<HTML><FONT color = #00A0FF><U>all<U></FONT></HTML>");
            }

            @Override
            public void mouseExited(MouseEvent arg0) {
                all.setText("<HTML><FONT color = #00A0FF>all</FONT></HTML>");
            }

            @Override
            public void mouseClicked(MouseEvent arg0) {
                table.selectAll();
            }
        });

        none.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent arg0) {
                none.setText("<HTML><FONT color = #00A0FF><U>none<U></FONT></HTML>");
            }

            @Override
            public void mouseExited(MouseEvent arg0) {
                none.setText("<HTML><FONT color = #00A0FF>none</FONT></HTML>");
            }

            @Override
            public void mouseClicked(MouseEvent arg0) {
                table.clearSelection();
            }
        });
        selectionPanel.add(all);
        selectionPanel.add(separator);
        selectionPanel.add(none);
    }
    
    /**
     * initialize function of check duplicates button
     */
    private void initializeCheckDuplicates(){
    	checkDuplicates.setToolTipText("Duplicates in the table will be selected.");
    	checkDuplicates.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				checkDuplicates.setText("Checking...");
				Runnable checkTask = new Runnable(){
					public void run() {
						ArrayList<String> stockList = new ArrayList<String>(); 
						int duplicate = 0;
						if(getTable().getIndexOf("Stock Name") >= 0){
							for(int row = 0; row < getTable().getRowCount(); ++row){
								String stockName = String.valueOf(getTable().getValueAt(row, getTable().getIndexOf("Stock Name")));
								if(stockList.contains(stockName)){
									getTable().setValueAt(true, row, 0);
									getTable().addRowSelectionInterval(row, row);
									duplicate++;
								}else{
									getTable().setValueAt(false, row, 0);
									getTable().removeRowSelectionInterval(row, row);
									stockList.add(stockName);	
								}
								
								
							}
							if(duplicate >= 1){								
								String duplicatestr = duplicate > 1? " duplicates are " : " duplicate is ";
								JOptionPane.showMessageDialog(null,  duplicate + duplicatestr + "highlighted.");	
								
								if(getDeleteButton()!=null)
								{
									getDeleteButton().setEnabled(true);
								}
							}else{
								JOptionPane.showMessageDialog(null,  "No duplicate found");
							}
							checkDuplicates.setText("Check Duplicates");
						}

					}
				};
				ThreadPool.getAGBThreadPool().executeTask(checkTask);
			}    		
    	});
    }

    /**
     * initialize function of selecting columns to show
     */
    private void initializeColumnSelector(){
    	columnSelector.addActionListener(new ActionListener() {			
			public void actionPerformed(ActionEvent e) {
				final JPopupMenu columnsPopupMenu = new JPopupMenu();
        		final JCheckBox[] columnNameCheckBox = new JCheckBox[table.getColumnNames().size()];
        		JPanel columnsPopupPanel = new JPanel(new MigLayout("insets 0, gap 0")); 
        	    JScrollPane scrollPane = new JScrollPane(columnsPopupPanel);	
        	    final List<String> showingColumns = new ArrayList<String>();
        	    for(int columnCounter =0; columnCounter< columnNameCheckBox.length;++columnCounter){
        	    	final String columnName = table.getColumnNames().get(columnCounter);
        	    	
        	    		columnNameCheckBox[columnCounter]= new JCheckBox(columnName);
        	    		if(table.getColumnModel().getColumn(columnCounter).getMaxWidth() > 0) {
	        	    		columnNameCheckBox[columnCounter].setSelected(true);
	        	    	}
	        	    	final int index = columnCounter;
	        	    	columnNameCheckBox[columnCounter].addActionListener(new ActionListener() {				
	        				public void actionPerformed(ActionEvent e) {
			        					if(!columnNameCheckBox[index].isSelected()) {
			        						table.hideColumn(columnName);
			        					} else {
			        						table.showColumn(columnName);
			        						for (int i = 0; i <  getTable().getColumnCount(); i++) {
			        			                adjustColumnSizes( getTable(), i, 2);
			        			            }
			        						table.revalidate();
			        						table.repaint();
			        						showingColumns.add(columnName);
			        					}
	        				}
	        			});
	        	    	if(!columnName.equals(ColumnConstants.ROW_NUM) )
	        	    	{
	        	    		columnsPopupPanel.add(columnNameCheckBox[columnCounter],"wrap");
	        	    	}
        	    	
        		}
        	    showingColumns.addAll(getTable().getShowColumns());
        	    columnsPopupMenu.add(scrollPane);

		        if (!columnsPopupMenu.isVisible()) {
		            Point p = columnSelector.getLocationOnScreen();
		            columnsPopupMenu.setInvoker(columnSelector);
		            columnsPopupMenu.setLocation((int) p.getX(),
		                    (int) p.getY() + columnSelector.getHeight());
		            columnsPopupMenu.setVisible(true);		     
		        }

			}
		});
	}

    @SuppressWarnings("unchecked")
    public void singleValueFilter() {
        RowFilter<TableModel, Object> rf = null;
        try {
            String searchedValue = getSearchTextField().getText();
            for (int columnCounter = 0; columnCounter < getTable().getColumnCount(); columnCounter++) {
                if (searchvalue.equals(getTable().getColumnName(columnCounter))) {
                    rf = RowFilter.regexFilter("(?i)" + searchedValue, columnCounter);
                }
            }

            if (rf == null) {
                rf = RowFilter.regexFilter("(?i)" + searchedValue);
            }

        } catch (java.util.regex.PatternSyntaxException e) {
            return;
        }
        ((DefaultRowSorter<TableModel, Integer>) getTable().getRowSorter()).setRowFilter(rf);
    }

   
	public void rangeFilter(){
		RowFilter<TableModel, Integer> filter = null;
		filter = new RowFilter<TableModel, Integer>() {
			public boolean include(RowFilter.Entry<? extends TableModel, ? extends Integer> entry) {
				String range = getSearchTextField().getText();
				double lowerBound = 0;
				double upperBound = -1;
				double showedValue = 0;				
				if (range.contains(",")) {
					String[] temp = range.split(",");
					if (temp.length > 1) {
						if (Utils.isDouble(temp[0].trim())
								&& Utils.isDouble(temp[1].trim())) {
							lowerBound = Double.parseDouble(temp[0].trim());
							upperBound = Double.parseDouble(temp[1].trim());
						}
					} else {
						if (Utils.isDouble(temp[0].trim())) {
							lowerBound = Double.parseDouble(temp[0].trim());
						}
					}
				}
				else {
					if (Utils.isDouble(range))
						lowerBound = Double.parseDouble(range.trim());
				}
				
				if (!entry.getStringValue(table.getColumn(searchvalue).getModelIndex()).equals("NULL"))
				{
					  showedValue = Double.parseDouble(entry.getStringValue(table.getColumn(searchvalue).getModelIndex()));
				}else{
					showedValue = -1;
				} // filter out NULL
				
				if (lowerBound >= 0 && upperBound < 0)
					return showedValue >= lowerBound;
				else if (lowerBound < 0 && upperBound >= 0)
					return showedValue <= upperBound;
				else
					return (showedValue >= lowerBound && showedValue <= upperBound);

			}	
		};
		((DefaultRowSorter<TableModel, Integer>) getTable().getRowSorter()).setRowFilter(filter);
	}

}