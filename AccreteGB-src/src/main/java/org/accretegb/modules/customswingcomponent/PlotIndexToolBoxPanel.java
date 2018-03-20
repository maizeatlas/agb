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

import org.accretegb.modules.constants.ColumnConstants;
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
import javax.swing.table.TableModel;

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
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.TooManyListenersException;
import java.util.logging.Level;

public class PlotIndexToolBoxPanel extends JPanel {

    private static final long serialVersionUID = 1L;
    private JButton addButton;
    private JButton deleteButton;
    private JButton moveUpButton;
    private JButton moveDownButton;
    private JButton editButton;
    private JButton uploadButton;
    private JButton clearButton;
    private JPanel selectionPanel;
    private JLabel all;
    private JLabel none;
    private JLabel numberOfRows;

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
    private ImageIcon downIcon;
    private ImageIcon searchIcon;

    private List<String> horizontalList;
    private List<String> verticalList;

    private String searchvalue;
    private JTextField searchTextField;
    private JButton searchButton;
    private JPanel bottomHorizonPanel;
   
	private JLabel filterDonors;
    private JLabel searchFieldsByCriteria[];

    private JButton columnSelector;

    private boolean isDraggable;

    private enum Buttons {
        ADD, DELETE, EDIT, UPLOAD, CLEAR, MOVEUP, MOVEDOWN, SEARCH, GAP, SELECTION, COLUMNSELECTOR, NEWLINE, COUNTER
    };

    private PlotIndexColumnTable table;

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
		downIcon = new ImageIcon(loader.getResource("images/down.png"));		
		searchIcon = new ImageIcon(loader.getResource("images/Search.png"));
    }

    public void initialize() {
        initializeIcons();
        setLayout(new MigLayout("insets 5 5 5 5, gapx 5"));
        // setBorder(BorderFactory.createLineBorder(Color.BLACK));
        add(getVerticalTools(), "growy, spany, h 100%");
        add(getHorizontalTools(), "growx, spanx, w 100%, wrap");
        add(getTablePanel(), "w 100%, h 100%, grow, span, wrap");
        add(getButtomHorizontalPanel(),"growx, spanx, w 100%, wrap");
    }

    private JPanel getTablePanel() {
    	 JPanel tablePanel = new JPanel();
         tablePanel.setLayout(new GridBagLayout());
         tablePanel.setLayout(new BorderLayout());
         getTable().setMinimumSize(new Dimension(5, 2));
         getTable().getTableHeader().setReorderingAllowed(false);
         getTable().setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
         getTable().setDragEnabled(isDraggable());
         getTable().setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
         tablePanel.add(new JScrollPane(getTable(),JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
         		JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED), BorderLayout.CENTER);
         getTable().addComponentListener(new ComponentAdapter() {
 		    @Override
 		    public void componentResized(final ComponentEvent e) {
	 		        if (getTable().getPreferredSize().width <  getTable().getParent().getWidth()) {
	 		        	getTable().setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
	 		        } else {
	 		        	 getTable().setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
	 		        	for (int i = 0; i <  getTable().getColumnCount(); i++) {
	 		                adjustColumnSizes( getTable(), i, 2);
	 		            }
	 		        }
		    		  
 		    }
 		}); 
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
            		comp = renderer.getTableCellRendererComponent(table, String.valueOf(table.getValueAt(r, column)), false, false, r, column);                
            		int currentWidth = comp.getPreferredSize().width;
                    width = Math.max(width, currentWidth);
	            }
            }
           catch(Exception e){
        	   
           }
        }

        width += 2 * margin;

        col.setPreferredWidth(width);
        col.setWidth(width);
    }
	
	private JPanel getVerticalTools() {

		JPanel buttonsPanel = new JPanel(new MigLayout("insets 0, gapy 5"));
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
			case MOVEUP:
				JButton moveupButton = new JButton(getMoveUpIcon());
				setMoveUpButton(moveupButton);
				moveupButton.setToolTipText("Move selected row(s) up");
				buttonsPanel.add(getMoveUpButton(), "h 24:24:24, w 24:24:24, wrap");
				break;
			case MOVEDOWN:
				JButton movedownButton = new JButton(getMoveDownIcon());
				setMoveDownButton(movedownButton);
				movedownButton.setToolTipText("Move selected row(s) down");
				buttonsPanel.add(getMoveDownButton(), "h 24:24:24, w 24:24:24, wrap");
				break;
			case SEARCH:
				searchvalue = new String();
				searchTextField = new JTextField();
				searchButton = new JButton();
				searchButton.setToolTipText("Select criteria to filter");
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
	
	private JPanel getHorizontalTools() {

		JPanel buttonsPanel = new JPanel(new MigLayout("insets 0, gapx 5"));
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
				case MOVEUP:
					setMoveUpButton(new JButton(getMoveUpIcon()));
					buttonsPanel.add(getMoveUpButton(), "h 24:24:24, w 24:24:24");
					break;
				case MOVEDOWN:
					setMoveDownButton(new JButton(getMoveDownIcon()));
					buttonsPanel.add(getMoveDownButton(), "h 24:24:24, w 24:24:24");
					break;
				case SEARCH:
					searchvalue = new String();
					searchTextField = new JTextField();
					searchButton = new JButton();
					searchButton.setToolTipText("Select criteria to filter");
					filterDonors = new JLabel();
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
				case GAP:
					buttonsPanel.add(new JLabel(""), "pushx");
					break;
				case NEWLINE:
					buttonsPanel.add(new JLabel(""), "wrap");
					break;
				case COUNTER:
					break;
				default: LoggerUtils.log(Level.INFO, "No tool called: " + button);
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
		        					}
        				}
        			});
        	    	if(!columnName.equals(ColumnConstants.ROW_NUM) )
        	    	{
        	    		columnsPopupPanel.add(columnNameCheckBox[columnCounter],"wrap");
        	    	}
    	    	        	    	
        		}
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
				    Runnable selectAll = new Runnable() {
			            public void run() {
							for(int rowCounter = 0; rowCounter < table.getRowCount(); rowCounter++) {					
								table.addRowSelectionInterval(rowCounter, rowCounter);
							}
							if (Thread.interrupted()) {
		            			   System.out.println("select all has been interupted");
		            			   return;
		            			}
						}
			        };
			       ThreadPool.getAGBThreadPool().executeTask(selectAll);
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
					newFilter();
				}

			}));
		}

		final int columnCount = columnCounter;
		popup.add(new JMenuItem(new AbstractAction("All") {

			private static final long serialVersionUID = 1L;

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
						newFilter();
					}

					public void insertUpdate(DocumentEvent e) {
						newFilter();
					}

					public void removeUpdate(DocumentEvent e) {
						newFilter();
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
				if(table.getRowCount() > 0 )
					row[0] = Integer.parseInt(String.valueOf(table.getValueAt(table.getRowCount()-1, 0))) + 1;
				else row[0] = 0;
				model.addRow(row);	
				getDeleteButton().setEnabled(false);
				getMoveDownButton().setEnabled(false);
				getMoveUpButton().setEnabled(false);
				getTable().clearSelection();
			}	
		});
	}

	
	protected void setMouseListenerMoveUpButton() {
		getMoveUpButton().addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent arg0) {
				int rows[] = getTable().getSelectedRows();
				getTable().clearSelection();
				
				for(int row : rows) {
					for(int columnCounter = 1; columnCounter < getTable().getColumnCount(); columnCounter++) {
						Object temp = getTable().getValueAt(row, columnCounter);
						getTable().setValueAt(getTable().getValueAt(row-1, columnCounter), row, columnCounter);
						getTable().setValueAt(temp, row-1, columnCounter);
					}
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
					for(int columnCounter = 1; columnCounter < getTable().getColumnCount(); columnCounter++) {
						Object temp = getTable().getValueAt(row, columnCounter);
						getTable().setValueAt(getTable().getValueAt(row+1, columnCounter), row, columnCounter);
						getTable().setValueAt(temp, row+1, columnCounter);
					}
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

    public void setClearButton(ImageButton clearButton) {
        this.clearButton = clearButton;
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

    public PlotIndexColumnTable getTable() {
        return table;
    }

    public void setTable(PlotIndexColumnTable table) {
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
    public JButton getColumnSelector() {
		return columnSelector;
	}

	public void setColumnSelector(JButton columnSelector) {
		this.columnSelector = columnSelector;
	}
	public JLabel getNumberOfRows() {
		return numberOfRows;
	}

	public void setNumberOfRows(JLabel numberOfRows) {
		this.numberOfRows = numberOfRows;
	}
	public JPanel getBottomHorizonPanel() {
			return bottomHorizonPanel;
	}

}
