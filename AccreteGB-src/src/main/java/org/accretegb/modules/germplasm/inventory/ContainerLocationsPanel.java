package org.accretegb.modules.germplasm.inventory;

import static org.accretegb.modules.customswingcomponent.Utils.removeAllRowsFromTable;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.logging.Level;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import net.miginfocom.swing.MigLayout;

import org.accretegb.modules.constants.ColumnConstants;
import org.accretegb.modules.customswingcomponent.CheckBoxIndexColumnTable;
import org.accretegb.modules.customswingcomponent.TableToolBoxPanel;
import org.accretegb.modules.customswingcomponent.Utils;
import org.accretegb.modules.hibernate.Location;
import org.accretegb.modules.hibernate.StockPacketContainer;
import org.accretegb.modules.hibernate.ContainerLocation;
import org.accretegb.modules.hibernate.dao.LocationDAO;
import org.accretegb.modules.hibernate.dao.StockPacketContainerDAO;
import org.accretegb.modules.hibernate.dao.ContainerLocationDAO;

/**
 * @author tnj
 *  This class is used to manage fixed storage units. Users use this
 *  panel to register storage location, rooms and shelves into database.
 *  Users can also describe shelves location by tiers.
 *  Shelfids are supposed to be unique.
 */
public class ContainerLocationsPanel extends JPanel {
	private static final long serialVersionUID = 1L;
	private JButton registerButton;
	private TableToolBoxPanel containerLocationTablePanel;
	private CheckBoxIndexColumnTable containerLocationTable;
	private TableToolBoxPanel arrangeShelvesTablePanel;
	private CheckBoxIndexColumnTable arrangeShelvesTable;
	private JButton clearRegisterPanelTextFieldsButton;
	private HashMap<String,List<Object[]>> roomShelves;
	
	public void initialize() {
		setLayout(new MigLayout("insets 15, gap 0"));
		containerLocationTable = containerLocationTablePanel.getTable();
		arrangeShelvesTable = arrangeShelvesTablePanel.getTable();

		add(getRegisterPanel(), "cell 0 0, h 35%");
		add(getReviewPanel(), "cell 1 0, h 35%,growy, w 100%,wrap");
		add(getArrangePanel(), ",gaptop 10, h 65%, spanx,growx");
		
		populateContainerLocationTable();
		populateArrangeShelvesTable();
		
	}

	/**
	 * Panel that is used to arrange/describe shelves by tiers
	 * @return
	 */
	public JPanel getArrangePanel() {
		final JPanel arrangePanel = new JPanel(new MigLayout("insets 0, gap 5"));
		arrangePanel.setBorder(BorderFactory.createTitledBorder("Arrange Shelves"));
		JPanel settingsPanel = new JPanel(new MigLayout("insets 5 5 5 5, gap 0"));
		
		
		String[] settings = { "Tier1_position:", " Tier2_position:", "Tier3_position:" };
		// tiers setting labels
		JLabel[] labels = new JLabel[settings.length];		
		// tiers setting text fields
		final JTextField[] settingInputs = new JTextField[settings.length];
		// tiers setting buttons
		JButton[] settingButtons = new JButton[settings.length];
		for (int settingIndex = 0; settingIndex < settings.length; ++settingIndex) {
			labels[settingIndex] = new JLabel(settings[settingIndex]);
			settingsPanel.add(labels[settingIndex], "gapleft 5");
			settingInputs[settingIndex] = new JTextField(5);
			settingsPanel.add(settingInputs[settingIndex], "gapleft 15");
			settingButtons[settingIndex] = new JButton("set");
			settingsPanel.add(settingButtons[settingIndex],"gapleft 3,gapright 15");
		}
		

		// listeners for 3 tiers setting buttons
		settingButtons[0].addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String tierSetting = settingInputs[0].getText();
				for (int rowCounter = 0; rowCounter < arrangeShelvesTable.getSelectedRowCount(); ++rowCounter) {
					arrangeShelvesTable.setValueAt(
							tierSetting,
							arrangeShelvesTable.getSelectedRows()[rowCounter],
							arrangeShelvesTable.getColumn(ColumnConstants.TIER1_POSITION).getModelIndex());
				}
			}
		});
		settingButtons[1].addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String tierSetting = settingInputs[1].getText();
				for (int rowCounter = 0; rowCounter < arrangeShelvesTable.getSelectedRowCount(); ++rowCounter) {
					arrangeShelvesTable.setValueAt(tierSetting,
							arrangeShelvesTable.getSelectedRows()[rowCounter],
							arrangeShelvesTable.getColumn(ColumnConstants.TIER2_POSITION).getModelIndex());
				}
			}
		});
		settingButtons[2].addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String tierSetting = settingInputs[2].getText();
				for (int rowCounter = 0; rowCounter < arrangeShelvesTable.getSelectedRowCount(); ++rowCounter) {
					arrangeShelvesTable.setValueAt(tierSetting,
							arrangeShelvesTable.getSelectedRows()[rowCounter],
							arrangeShelvesTable.getColumn(ColumnConstants.TIER3_POSITION).getModelIndex());
				}
			}
		});
		
		//rename shelves by (first char of room)-(tier1)-(tier2_position)-(tier3_position)
		JButton renameShelf = new JButton("rename");
		settingsPanel.add(renameShelf, "gapleft 3");
		renameShelf.setToolTipText("Rename shelf using room-tier1_position-tier2_position-tier3_position");
		
		// listener for renameShelf button
		renameShelf.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				for (int rowCounter = 0; rowCounter < arrangeShelvesTable.getSelectedRowCount(); ++rowCounter) {
					String value;
					String room = ((String) arrangeShelvesTable.getValueAt(
							arrangeShelvesTable.getSelectedRows()[rowCounter],
							arrangeShelvesTable.getColumn(ColumnConstants.ROOM).getModelIndex())).substring(0, 1);;
					String tier1 = (String) arrangeShelvesTable.getValueAt(
							arrangeShelvesTable.getSelectedRows()[rowCounter],
							arrangeShelvesTable.getColumn(ColumnConstants.TIER1_POSITION).getModelIndex());
					String tier2_position = (String) arrangeShelvesTable.getValueAt(
							arrangeShelvesTable.getSelectedRows()[rowCounter],
							arrangeShelvesTable.getColumn(ColumnConstants.TIER2_POSITION).getModelIndex());
					String tier3_position = (String) arrangeShelvesTable.getValueAt(
							arrangeShelvesTable.getSelectedRows()[rowCounter],
							arrangeShelvesTable.getColumn(ColumnConstants.TIER3_POSITION).getModelIndex());
					
					value = room + "-" + tier1 + "-" + tier2_position + "-" + tier3_position;
					
					arrangeShelvesTable.setValueAt(
							value, 
							arrangeShelvesTable.getSelectedRows()[rowCounter], 
							arrangeShelvesTable.getColumn(ColumnConstants.SHELF).getModelIndex());
				}
			}
		});
		arrangePanel.add(settingsPanel, "wrap");
		arrangePanel.add(getArrangeShelvesTablePanel(), "h 100%, w 100%, wrap");
		arrangeShelvesTablePanel.getDeleteButton().setEnabled(false);		
		arrangeShelvesTable.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				if (arrangeShelvesTable.getSelectedRowCount() > 0) {
					arrangeShelvesTablePanel.getDeleteButton().setEnabled(true);
				} else {
					arrangeShelvesTablePanel.getDeleteButton().setEnabled(false);
				}
			}
		});
		
		addArrageShelvesPanelDeleteButtonListener();
		addArrageShelvesPanelSyncButtonListener();
		return arrangePanel;

	}
	
	/**
	 * Confirm shelves arrangement into database using sync button
	 */
	private void addArrageShelvesPanelSyncButtonListener(){
		getArrangeShelvesTablePanel().getRefreshButton().setToolTipText("sync with database");
		getArrangeShelvesTablePanel().getRefreshButton().addActionListener(
				new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						// check if there are duplicate shelf ids 
						HashMap<Integer, String> rowIndex_shelfID = new HashMap<Integer, String>();
						List<Integer> duplicateShelfIdRows = new ArrayList<Integer>();
						for (int rowCounter = 0; rowCounter < arrangeShelvesTable.getRowCount(); ++rowCounter) {						
							String shelfID = (String) arrangeShelvesTable.getValueAt(rowCounter, arrangeShelvesTable.getColumn(ColumnConstants.SHELF).getModelIndex());
							if (!rowIndex_shelfID.containsValue(shelfID)) {
								rowIndex_shelfID.put(rowCounter, shelfID);
							} else {
								for (Map.Entry entry : rowIndex_shelfID.entrySet()) {
									if (shelfID.equals(entry.getValue())) {
										duplicateShelfIdRows.add((Integer) entry.getKey());
										break;
									}
								}
								duplicateShelfIdRows.add(rowCounter);
							}
						}
						// if shelf ids are unique, sync with database
						if (rowIndex_shelfID.size() == arrangeShelvesTable.getRowCount()) {
							List<String[]> updateContainerLocations = new ArrayList<String[]>();
							for (int rowCounter = 0; rowCounter < arrangeShelvesTable.getRowCount(); ++rowCounter) {
								String[] values = {
										Integer.toString((Integer) arrangeShelvesTable.getValueAt(rowCounter,arrangeShelvesTable.getColumn("Location_id").getModelIndex())),
										(String) arrangeShelvesTable.getValueAt(rowCounter,arrangeShelvesTable.getColumn("ContainerLocation_id").getModelIndex()),
										(String) arrangeShelvesTable.getValueAt(rowCounter,arrangeShelvesTable.getColumn(ColumnConstants.BUILDING).getModelIndex()),
										(String) arrangeShelvesTable.getValueAt(rowCounter,arrangeShelvesTable.getColumn(ColumnConstants.ROOM).getModelIndex()),
										(String) arrangeShelvesTable.getValueAt(rowCounter,arrangeShelvesTable.getColumn(ColumnConstants.TIER1_POSITION).getModelIndex()),
										(String) arrangeShelvesTable.getValueAt(rowCounter,arrangeShelvesTable.getColumn(ColumnConstants.TIER2_POSITION).getModelIndex()),
										(String) arrangeShelvesTable.getValueAt(rowCounter,arrangeShelvesTable.getColumn(ColumnConstants.TIER3_POSITION).getModelIndex()),
										(String) arrangeShelvesTable.getValueAt(rowCounter,arrangeShelvesTable.getColumn(ColumnConstants.SHELF).getModelIndex()) 
										};
								updateContainerLocations.add(values);
						    }
							//}
							ContainerLocationDAO.getInstance().updateListOfContainerLocation(updateContainerLocations);
							populateContainerLocationTable();
							arrangeShelvesTable.setChangedRowIds(new ArrayList<Integer>());
							containerLocationTable.setHasSynced(true);
							arrangeShelvesTable.clearSelection();
							arrangeShelvesTable.setHasSynced(true);
							arrangeShelvesTable.repaint();
						} else {
							arrangeShelvesTable.clearSelection();
							JOptionPane.showConfirmDialog(
									getArrangeShelvesTablePanel(),
									"Shelf IDs should be unique", "Error!",
									JOptionPane.DEFAULT_OPTION,
									JOptionPane.WARNING_MESSAGE);
						}
					}
					});
	}
	/**
	 * Delete button listener in shelves arrangement panel.
	 * If selected shelfid is being used, the deletion will be denied.
	 */
	private void addArrageShelvesPanelDeleteButtonListener(){
		getArrangeShelvesTablePanel().getDeleteButton().addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						boolean cannotRemove = false;
						for (int rowCounter = 0; rowCounter < arrangeShelvesTable.getSelectedRowCount(); rowCounter++) {
							int containerLocationid = Integer.parseInt((String) arrangeShelvesTable.getValueAt(
									arrangeShelvesTable.getSelectedRows()[rowCounter],
									arrangeShelvesTable.getColumn("ContainerLocation_id").getModelIndex()));							
							List<StockPacketContainer> results = StockPacketContainerDAO.getInstance().findByContainerLocationId(containerLocationid);
							if (results.size() > 0) {
								cannotRemove = true;
							} else {
								ContainerLocation storageunit = new ContainerLocation();
								storageunit.setContainerLocationId(containerLocationid);
								ContainerLocationDAO.getInstance().deleteContainerLocation(storageunit);
								((DefaultTableModel) arrangeShelvesTable.getModel()).removeRow(arrangeShelvesTable.getSelectedRows()[rowCounter]);
								containerLocationTable.setForeground(Color.BLACK);
								containerLocationTable.setHasSynced(true);
							}
						
						}
						if (cannotRemove) {
							JOptionPane.showConfirmDialog(
											getContainerLocationTablePanel(),
											"Cannot delete shelves associated with packets location",
											"Error!",
											JOptionPane.DEFAULT_OPTION,
											JOptionPane.WARNING_MESSAGE);
						}
					}
				});
	}

	
	
	
	
	/**
	 * panel that use to review / edit information after registering 
	 * storage units location, rooms and shelves.
	 * @return
	 */
	public JPanel getReviewPanel() {
		final JPanel reviewPanel = new JPanel(new MigLayout("insets 0, gap 5"));
		reviewPanel.setBorder(BorderFactory.createTitledBorder("Review"));
		reviewPanel.add(getContainerLocationTablePanel(), "h 100%,w 100%, wrap");
		containerLocationTablePanel.getEditButton().setEnabled(false);
		containerLocationTablePanel.getDeleteButton().setEnabled(false);
		
		containerLocationTable.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				if (containerLocationTable.getSelectedRowCount() > 0) {
					containerLocationTablePanel.getEditButton().setEnabled(true);
					containerLocationTablePanel.getDeleteButton().setEnabled(true);
					showRoomShelves();
				} else {
					containerLocationTablePanel.getEditButton().setEnabled(false);
					containerLocationTablePanel.getDeleteButton().setEnabled(false);
					List<Object[]> tableRows = getRoomShelves().get("All");
					populateArrangeShelvesTableFromObjects(tableRows);
				}
			}

			
		});
	
		getContainerLocationTablePanel().getEditButton().addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						// edit button reuse register panel
						JPanel editPopupPanel = getRegisterPanel();
						editPopupPanel.setBorder(BorderFactory.createEmptyBorder());
						// remove add, clear button
						editPopupPanel.remove(9);
						editPopupPanel.remove(8);
						int selRow = containerLocationTable.getSelectedRow();
						String values[] = {
								containerLocationTable.getValueAt(selRow, 2) == null ? "": containerLocationTable.getValueAt(selRow, 2).toString(),
								containerLocationTable.getValueAt(selRow, 3) == null ? "": containerLocationTable.getValueAt(selRow, 3).toString(),
								containerLocationTable.getValueAt(selRow, 4) == null ? "": containerLocationTable.getValueAt(selRow, 4).toString(),
								containerLocationTable.getValueAt(selRow, 5) == null ? "": containerLocationTable.getValueAt(selRow, 5).toString(),
						};
						String originalBuilding = values[1];
						String originalRoom = values[2];
						int originalShelfNumber = Integer.parseInt(values[3]);
						((JComboBox<String>) editPopupPanel.getComponent(1)).setSelectedItem(values[0]);
						((JTextField) editPopupPanel.getComponent(3)).setText(values[1]);
						((JTextField) editPopupPanel.getComponent(5)).setText(values[2]);
						((JTextField) editPopupPanel.getComponent(7)).setText(values[3]);
						int registerOption = JOptionPane.showConfirmDialog(
								reviewPanel, editPopupPanel,
								"Edit Storage Unit Information",
								JOptionPane.OK_CANCEL_OPTION,
								JOptionPane.PLAIN_MESSAGE);
						if (registerOption == JOptionPane.OK_OPTION) {
							boolean noEmptyInput = true;
							String[] editFields = { "", "", "", "" };
							editFields[0] = ((JComboBox<String>) editPopupPanel.getComponent(1)).getSelectedItem().toString();
							editFields[1] = ((JTextField) editPopupPanel.getComponent(3)).getText();
							editFields[2] = ((JTextField) editPopupPanel.getComponent(5)).getText();
							editFields[3] = ((JTextField) editPopupPanel.getComponent(7)).getText();

							// Location selection, room input or number of shelves input is empty
							if (editFields[0].equals("select")|| editFields[1].trim().equals("")|| editFields[2].trim().equals("")) {
								noEmptyInput = false;
							}

							if (noEmptyInput) {
								if (!Utils.isValidInteger(editFields[3].trim())) {
									JOptionPane.showConfirmDialog(
											editPopupPanel,
											"Number should be integer",
											"Error!",
											JOptionPane.DEFAULT_OPTION,
											JOptionPane.WARNING_MESSAGE);
								} else if(ContainerLocationDAO.getInstance().findByBuildingRoom(originalBuilding.trim(), originalRoom.trim()).size() >0 && originalShelfNumber > Integer.parseInt(editFields[3])){
									JOptionPane.showConfirmDialog(
											editPopupPanel,
											"Please use delete button in arrange shleves panel to delete specific shelf",
											"Error!",
											JOptionPane.DEFAULT_OPTION,
											JOptionPane.WARNING_MESSAGE);
								}else {
									for (int column = 2; column < containerLocationTable.getColumnCount(); ++column) {
										containerLocationTable.setValueAt(editFields[column - 2],selRow,column);
									}
									containerLocationTable.setHasSynced(false);
								}
							} else {
								JOptionPane.showConfirmDialog(
										editPopupPanel,
										"<HTML><FONT COLOR = Red>*</FONT> Marked fields are mandatory.</HTML>",
										"Error!",
										JOptionPane.DEFAULT_OPTION,
										JOptionPane.WARNING_MESSAGE);
							}
						}
					}
				});
		 addReviewPanelDeleteButtonListener();
		 addReviewPanelSyncButtonListener();
		return reviewPanel;
	}
	
	/**
	 * Delete button listener in storage units review panel.
	 */
	private void addReviewPanelDeleteButtonListener(){
		getContainerLocationTablePanel().getDeleteButton().addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						int selRow = containerLocationTable.getSelectedRow();					
						String building = containerLocationTable.getValueAt(selRow, 3) == null ? "": containerLocationTable.getValueAt(selRow, 3).toString();
						String room = containerLocationTable.getValueAt(selRow, 4) == null ? "": containerLocationTable.getValueAt(selRow, 4).toString();
						List<ContainerLocation> containerLocations = ContainerLocationDAO.getInstance().findByBuildingRoom(building.trim(),room.trim());
						if (containerLocations.size() == 0) {
							((DefaultTableModel) containerLocationTable.getModel()).removeRow(containerLocationTable.convertRowIndexToModel(selRow));
							containerLocationTable.clearSelection();
						} else {
							boolean removeRow = true;
							for (ContainerLocation containerLocation : containerLocations) {
								List<StockPacketContainer> results = StockPacketContainerDAO.getInstance().findByContainerLocationId(containerLocation.getContainerLocationId());
								if (results.size() > 0) {
									removeRow = false;
									break;
								} 
							}
							if (removeRow) {
								((DefaultTableModel) containerLocationTable.getModel()).removeRow(containerLocationTable.convertRowIndexToModel(selRow));
								removeRow = false;
								containerLocationTable.clearSelection();
								containerLocationTable.setForeground(Color.BLACK);
							    ContainerLocationDAO.getInstance().deleteListOfContainerLocation(containerLocations);					
							}else{
								JOptionPane.showConfirmDialog(
										getContainerLocationTablePanel(),
										"Cannot delete storage unit associated with packets location",
										"Error!",
										JOptionPane.DEFAULT_OPTION,
										JOptionPane.WARNING_MESSAGE);
							}
						}
					}
				});
	}
	
	/**
	 * Confirm storage units insertion/modification into database using sync button
	 */
	private void addReviewPanelSyncButtonListener(){
		getContainerLocationTablePanel().getRefreshButton().setToolTipText("sync with database");
		getContainerLocationTablePanel().getRefreshButton().addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				clearRegisterPanelTextFieldsButton.doClick();
				List<BigInteger> numberOfShelves = ContainerLocationDAO.getInstance().findNumberOfShelvesEachRoom();
				List<Integer> originalNumbers = new ArrayList<Integer>();
				for(BigInteger number : numberOfShelves){
					originalNumbers.add(number.intValue());
				}
				for (int rowCounter = 0; rowCounter < getContainerLocationTablePanel().getTable().getRowCount()-originalNumbers.size(); rowCounter++) {
					originalNumbers.add(0);					
				}
				for (int rowCounter = 0; rowCounter < getContainerLocationTablePanel().getTable().getRowCount(); rowCounter++) {
					String LocationValue = (String) getContainerLocationTablePanel().getTable().getValueAt(rowCounter,getContainerLocationTablePanel().getTable().getColumn("Location").getModelIndex());
					String buildingValue = (String) getContainerLocationTablePanel().getTable().getValueAt(rowCounter,getContainerLocationTablePanel().getTable().getColumn(ColumnConstants.BUILDING).getModelIndex());
					String roomValue = (String) getContainerLocationTablePanel().getTable().getValueAt(rowCounter,getContainerLocationTablePanel().getTable().getColumn(ColumnConstants.ROOM).getModelIndex());
					String shelvesValue = (String) getContainerLocationTablePanel().getTable().getValueAt(rowCounter,getContainerLocationTablePanel().getTable().getColumn("Number of Shelves").getModelIndex());			
					String[] divLocationValue = LocationValue.split("_");
					Location divLocation = manageLocation(divLocationValue);					
					int orginalNumberOfShelves = originalNumbers.get(rowCounter);
					int AddeNumberOfShelves = Integer.parseInt(shelvesValue) - orginalNumberOfShelves;
					for (int shelfCounter = 0; shelfCounter < AddeNumberOfShelves; ++shelfCounter) {
						String[] containerLocationValue = new String[3];
						containerLocationValue[0] = buildingValue;
						containerLocationValue[1] = roomValue;
						containerLocationValue[2] = roomValue.substring(0, 1) + "-" + Integer.toString(shelfCounter +orginalNumberOfShelves + 1);
						ContainerLocation containerLocation = manageContainerLocation(containerLocationValue,divLocation);				
					}					
				}
				populateArrangeShelvesTable();
				containerLocationTable.clearSelection();
				containerLocationTable.setHasSynced(true);
				containerLocationTable.repaint();
				
			}

		});
		
	}

	/**
	 * fetch all Location data from database, and showsin JCombobox
	 * @return
	 */
	public ArrayList<String> populateLocationChoices() {
		ArrayList<String> localities = new ArrayList<String>();
		List<Location> results = LocationDAO.getInstance().findAllLocalities();
		for (Location result : results) {
			StringBuilder oneLocationRow = new StringBuilder();
			String zipCode = result.getZipcode() == null ? "null" : result.getZipcode();
			oneLocationRow.append(zipCode);
			oneLocationRow.append("_");
			String LocationName = result.getLocationName() == null ? "null" : result.getLocationName();
			oneLocationRow.append(LocationName);		
			oneLocationRow.append("_");
			String city =  result.getCity() == null ? "null" : result.getCity();		
			oneLocationRow.append(city);
			oneLocationRow.append("_");
			String state = result.getStateProvince() == null ? "null" : result.getStateProvince();	
			oneLocationRow.append(state);
			oneLocationRow.append("_");
			String country = result.getCountry() == null ? "null" : result.getCountry();	
			oneLocationRow.append(country);
			localities.add(oneLocationRow.toString());
		}
		return localities;
	}

	/**
	 * Panel that used to choose Location, input
	 * room name and number of shelves.
	 * @return
	 */
	public JPanel getRegisterPanel() {
		final JPanel registerPanel = new JPanel(new MigLayout("insets 5 5 5 5, gapx 0"));
		final JComboBox<Object> Location = new JComboBox<Object>();
		final StringBuilder currentLocation = new StringBuilder();
		final Object[] newRow = new Object[containerLocationTable.getColumnCount()];
		
		final String labelNames[] = {
				"<HTML>Location" + "</FONT></HTML>",
				"<HTML>Building Name" + "</FONT></HTML>",
				"<HTML>Room Name" + "</FONT></HTML>",
				"<HTML>Number of Shelves" + "</FONT></HTML>", };
		JLabel[] labels = new JLabel[labelNames.length];
		final JTextField[] containerLocationInput = new JTextField[labels.length - 1];

		for (int count = 0; count < labelNames.length; count++) {
			labels[count] = new JLabel(labelNames[0]);
		}
		Location.setPrototypeDisplayValue("                         ");
		ArrayList<String> LocationChoices = populateLocationChoices();
		Location.addItem("select");
		Location.addItem("add new Location");
		for (String choice : LocationChoices) {
			Location.addItem(choice);
		}
		ItemListener itemListener = new ItemListener() {
			public void itemStateChanged(ItemEvent itemEvent) {
				if (itemEvent.getStateChange() == ItemEvent.SELECTED) {
					// option selected is "add new Location"
					if (Location.getSelectedItem().toString().equals("add new Location")) {
						JPanel addNewLocationPanel = new JPanel(new MigLayout("insets 0, gapx 0"));
						String labelNames[] = { ColumnConstants.ZIPCODE, ColumnConstants.LOCATION_NAME,ColumnConstants.CITY, ColumnConstants.STATE, ColumnConstants.COUNTRY, };
						String values[] = { "", "", "", "", "" };
						JLabel labels[] = new JLabel[labelNames.length];
						final JTextField textBoxes[] = new JTextField[labelNames.length];
						for (int labelIndex = 0; labelIndex < labels.length; labelIndex++) {
							labels[labelIndex] = new JLabel(labelNames[labelIndex]);
							addNewLocationPanel.add(labels[labelIndex],"gapleft 10, push");
							textBoxes[labelIndex] = new JTextField();
							textBoxes[labelIndex].setPreferredSize(new Dimension(200, 0));
							textBoxes[labelIndex].setText(values[labelIndex]);
							addNewLocationPanel.add(textBoxes[labelIndex],"gapRight 10, wrap");
						}
						textBoxes[0].getDocument().addDocumentListener(new DocumentListener() {
									public void changedUpdate(DocumentEvent arg0) {
										if (textBoxes[0].getText().length() == 5)
											updateBasedOnZipcode(textBoxes);
									}

									public void insertUpdate(DocumentEvent arg0) {
										if (textBoxes[0].getText().length() == 5)
											updateBasedOnZipcode(textBoxes);
									}

									public void removeUpdate(DocumentEvent arg0) {
										if (textBoxes[0].getText().length() == 5)
											updateBasedOnZipcode(textBoxes);
									}
								});

						int newLocationOption = JOptionPane.showConfirmDialog(
								registerPanel, addNewLocationPanel,
								"Enter New Location Information ",
								JOptionPane.DEFAULT_OPTION);
						if (newLocationOption == JOptionPane.OK_OPTION) {
							String newLocationChoice = textBoxes[0].getText()
									+ "_" + textBoxes[1].getText() + "_"
									+ textBoxes[2].getText() + "_"
									+ textBoxes[3].getText() + "_"
									+ textBoxes[4].getText();
							DefaultComboBoxModel model = (DefaultComboBoxModel) Location.getModel();
							if (model.getIndexOf(newLocationChoice) == -1) {
								Location.addItem(newLocationChoice);
								Location.setSelectedItem(newLocationChoice);
								currentLocation.append(newLocationChoice);
								newRow[2] = newLocationChoice;
							} else {
								Location.setSelectedItem("select");
								newRow[2] = "";
								JOptionPane.showConfirmDialog(registerPanel,
										"Duplicate Location Infomation", "",
										JOptionPane.DEFAULT_OPTION,
										JOptionPane.PLAIN_MESSAGE);
							}
						} else {
							Location.setSelectedItem("select");
						}
					} else if (!Location.getSelectedItem().toString().equals("select")) {
						currentLocation.append(Location.getSelectedItem().toString());
						newRow[2] = Location.getSelectedItem().toString();
					}
				}
			}
		};

		Location.addItemListener(itemListener);

		registerPanel.setBorder(BorderFactory.createTitledBorder("Register"));
		registerPanel.add(labels[0], "gapleft 5,gapright 65  ");
		registerPanel.add(Location, "wrap");

		for (int counter = 1; counter < labels.length; ++counter) {
			labels[counter] = new JLabel(labelNames[counter]);
			registerPanel.add(labels[counter], "gapleft 5, shrink 0");
			containerLocationInput[counter - 1] = new JTextField("");
			registerPanel.add(containerLocationInput[counter - 1],"align right, wrap,growx");
		}

		JButton addContainerLocationInfoButton = new JButton("Add");
		
		//populate table in review panel after clicking add button
		addContainerLocationInfoButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				boolean validInput = true;
				String[] textFieldInputs = { "", "", "" };

				if (Location.getSelectedItem().toString().equals("select")) {
					validInput = false;
				}
				for (int valueCounter = 0; valueCounter < containerLocationInput.length; valueCounter++) {
					textFieldInputs[valueCounter] = containerLocationInput[valueCounter].getText();
					if (containerLocationInput[valueCounter].getText().equals("")) {
						validInput = false;
					}
				}

				if (validInput) {
					if (!Utils.isValidInteger(textFieldInputs[2].trim())) {
						JOptionPane.showConfirmDialog(registerPanel,
								"Number should be integer", "Error!",
								JOptionPane.DEFAULT_OPTION,
								JOptionPane.WARNING_MESSAGE);
					} else {
						newRow[0] = new Boolean(false);
						newRow[1] = containerLocationTable.getRowCount();
						newRow[3] = containerLocationInput[0].getText().trim();
						newRow[4] = containerLocationInput[1].getText().trim();
						newRow[5] = containerLocationInput[2].getText().trim();
						boolean noDuplicate = true;
						for (int rowCounter = 0; rowCounter < getContainerLocationTablePanel().getTable().getRowCount(); rowCounter++) {
							Object Location = getContainerLocationTablePanel().getTable().getValueAt(
											rowCounter,
											getContainerLocationTablePanel().getTable().getColumn("Location").getModelIndex());
							Object building = getContainerLocationTablePanel().getTable().getValueAt(
											rowCounter,
											getContainerLocationTablePanel().getTable().getColumn(ColumnConstants.BUILDING).getModelIndex());
							Object room = getContainerLocationTablePanel().getTable().getValueAt(
											rowCounter,
											getContainerLocationTablePanel().getTable().getColumn(ColumnConstants.ROOM).getModelIndex());
							if (newRow[2].equals(Location) && newRow[3].equals(building)&& newRow[4].equals(room)) {
								noDuplicate = false;
								break;
							}
						}
						if (noDuplicate) {
							DefaultTableModel model = (DefaultTableModel) containerLocationTable.getModel();
							model.addRow(newRow);
							containerLocationTable.setModel(model);
							containerLocationInput[1].setText("");
							containerLocationInput[2].setText("");
						} else {
							JOptionPane.showConfirmDialog(registerPanel,
									"Same storage unit exsits", "Error",
									JOptionPane.DEFAULT_OPTION,
									JOptionPane.WARNING_MESSAGE);
						}
					}
				} else {
					JOptionPane.showConfirmDialog(
									registerPanel,
									"<HTML>ALL fields are mandatory.</HTML>",
									"Error!", JOptionPane.DEFAULT_OPTION,
									JOptionPane.WARNING_MESSAGE);
				}
			}
		});
		clearRegisterPanelTextFieldsButton = new JButton("Clear");
		clearRegisterPanelTextFieldsButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				Location.setSelectedItem("select");
				for (int valueCounter = 0; valueCounter < containerLocationInput.length; valueCounter++) {
					containerLocationInput[valueCounter].setText("");
				}

			}

		});
		registerPanel.add(clearRegisterPanelTextFieldsButton, "cell 1 4,gapleft push");
		registerPanel.add(addContainerLocationInfoButton, "cell 1 4,align right,wrap");

		return registerPanel;
	}

	/**
	 * automatically fill out text fields based on zipcode input
	 * @param textBoxes
	 */
	public void updateBasedOnZipcode(JTextField textBoxes[]) {

		List<Location> LocationList = LocationDAO.getInstance().findLocationByZipcode(textBoxes[0].getText());
		if (LocationList.size() > 0) {
			textBoxes[1].setText(LocationList.get(0).getLocationName());
			textBoxes[2].setText(LocationList.get(0).getCity());
			textBoxes[3].setText(LocationList.get(0).getStateProvince());
			textBoxes[4].setText(LocationList.get(0).getCountry());
		}
	}

	/**
	 * Manage location data insertion. Avoid duplicate insertion.
	 * @param values values of a location entry
	 * @return
	 */
	private Location manageLocation(String[] values) {
		Location existingLocation = null;
		List<Location> LocationIds = (List<Location>) LocationDAO.getInstance().findLocationByCityZipCodeName(values[2].equalsIgnoreCase("NULL") ? null:values[2].toLowerCase(),values[0], values[1].equalsIgnoreCase("NULL") ? null:values[1].toLowerCase());
		while (LocationIds.size() > 0) {
			existingLocation = LocationIds.get(0);
			LocationIds.remove(0);
		}
		if (existingLocation == null) {
			existingLocation = LocationDAO.getInstance().insertNewLocation(values[1].equalsIgnoreCase("NULL") ? null:values[1], 
					values[2].equalsIgnoreCase("NULL") ? null:values[2], 
					values[3].equalsIgnoreCase("NULL") ? null:values[3],
					values[4].equalsIgnoreCase("NULL") ? null:values[4],
					values[0].equalsIgnoreCase("NULL") ? null:values[0], null, null, null, null);
		}
		return existingLocation;
	}

	/**
	 * Manage container_location data insertion. Avoid duplicate insertion.
	 * @param values values of a container_location entry
	 * @param Location DivLocation
	 * @return
	 */
	private ContainerLocation manageContainerLocation(String[] values, Location Location) {

		ContainerLocation existingContainerLocation = null;
		List<ContainerLocation> containerLocations = ContainerLocationDAO.getInstance().findUniqueContainerLocation(values[0].toLowerCase(),values[1].toLowerCase(), values[2].toLowerCase());
		while (containerLocations.size() > 0) {
			existingContainerLocation = containerLocations.get(0);
			containerLocations.remove(0);
		}

		if (existingContainerLocation == null) {
			existingContainerLocation = ContainerLocationDAO.getInstance().insert(Location, values[0], values[1], null, null, null,values[2], null, null);
		}
		
		return existingContainerLocation;
	}

	/**
	 * Populate table that describes shelves by tiers from database
	 */
	private void populateArrangeShelvesTable() {
		Utils.removeAllRowsFromTable((DefaultTableModel) arrangeShelvesTable.getModel());
		DefaultTableModel model = (DefaultTableModel) arrangeShelvesTable.getModel();
		List<ContainerLocation> containerLocations = ContainerLocationDAO.getInstance().findAllContainerLocations();
		HashMap<String,List<Object[]>> roomShelves = new HashMap<String,List<Object[]>>();		
		if (!containerLocations.isEmpty()) {
			for (ContainerLocation divstorageunit : containerLocations) {			
				if(!roomShelves.containsKey(divstorageunit.getBuilding()+"_"+divstorageunit.getRoom()))
				{
					List<Object[]> shelves = new ArrayList<Object[]>();
					roomShelves.put(divstorageunit.getBuilding()+"_"+divstorageunit.getRoom(),shelves);
				}
			}
			List<Object[]> shelves = new ArrayList<Object[]>();
			roomShelves.put("All",shelves);
			for (ContainerLocation divstorageunit : containerLocations) {
				Object[] rowData = new Object[arrangeShelvesTable.getColumnCount()];
				rowData[0] = new Boolean(false);
				rowData[1] = arrangeShelvesTable.getRowCount();
				rowData[2] = divstorageunit.getLocation().getLocationId();
				rowData[3] = divstorageunit.getContainerLocationId().toString();
				rowData[4] = divstorageunit.getBuilding();
				rowData[5] = divstorageunit.getRoom();
				rowData[6] = divstorageunit.getTier1();
				rowData[7] = divstorageunit.getTier2();
				rowData[8] = divstorageunit.getTier3();
				rowData[9] = divstorageunit.getShelf();
				model.addRow(rowData);
				roomShelves.get("All").add(rowData);
				arrangeShelvesTable.setModel(model);
				Set<String> keys = roomShelves.keySet();
				for(String roomName : keys){
					if(roomName.equals(divstorageunit.getBuilding()+"_"+divstorageunit.getRoom())){
						roomShelves.get(roomName).add(rowData);
					}
				}
			}
		}
		setRoomShelves(roomShelves);
		arrangeShelvesTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
	}
	
	/**
	 * Table that displays Location, building name, room name, and number of shelves in each room
	 */
	private void populateContainerLocationTable() {
		Utils.removeAllRowsFromTable((DefaultTableModel) containerLocationTable.getModel());
		DefaultTableModel model = (DefaultTableModel) containerLocationTable.getModel();
		List location = ContainerLocationDAO.getInstance().findByLocation();
		List<BigInteger> numberOfShelves = ContainerLocationDAO.getInstance().findNumberOfShelvesEachRoom();
		if (!location.isEmpty()) {
			for (int i = 0; i < location.size(); i++) {
				Location divLocation = (Location) ((Object[]) location.get(i))[0];
				ContainerLocation divstorageunit = (ContainerLocation) ((Object[]) location.get(i))[1];
				Object[] rowData = new Object[containerLocationTable.getColumnCount()];
				rowData[0] = new Boolean(false);
				rowData[1] = containerLocationTable.getRowCount();
				StringBuilder LocationInfo = new StringBuilder();
				LocationInfo.append(divLocation.getZipcode());
				LocationInfo.append("_");
				LocationInfo.append(divLocation.getLocationName());
				LocationInfo.append("_");
				LocationInfo.append(divLocation.getCity());
				LocationInfo.append("_");
				LocationInfo.append(divLocation.getStateProvince());
				LocationInfo.append("_");
				LocationInfo.append(divLocation.getCountry());
				rowData[2] = LocationInfo.toString();
				rowData[3] = divstorageunit.getBuilding();
				rowData[4] = divstorageunit.getRoom();
				int number = numberOfShelves.get(i).intValue();
				rowData[5] = Integer.toString(number);
				model.addRow(rowData);
				containerLocationTable.setModel(model);
			}
		}
		
		containerLocationTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		containerLocationTable.setSingleSelection(true);
	}
	public void showRoomShelves() {
		int selRow = containerLocationTable.getSelectedRow();
		String buildingName = (String) containerLocationTable.getValueAt(selRow, containerLocationTable.getIndexOf(ColumnConstants.BUILDING));
		String roomName = (String) containerLocationTable.getValueAt(selRow, containerLocationTable.getIndexOf(ColumnConstants.ROOM));
		List<Object[]> tableRows = getRoomShelves().get(buildingName+"_"+roomName);
		populateArrangeShelvesTableFromObjects(tableRows);
	}
	public void populateArrangeShelvesTableFromObjects(List<Object[]> tableRows ) {
        DefaultTableModel tableModel = ((DefaultTableModel)arrangeShelvesTable.getModel());
        removeAllRowsFromTable(tableModel);	
    	for(Object[] row : tableRows){
    		tableModel.addRow(row);               
    	}
    	
    }

	public JButton getRegisterButton() {
		return registerButton;
	}

	public void setRegisterButton(JButton registerButton) {
		this.registerButton = registerButton;
	}

	public void setContainerLocationTablePanel(TableToolBoxPanel containerLocationTablePanel) {
		this.containerLocationTablePanel = containerLocationTablePanel;
	}

	public TableToolBoxPanel getContainerLocationTablePanel() {
		return containerLocationTablePanel;
	}

	public void setArrangeShelvesTablePanel(
			TableToolBoxPanel arrangeShelvesTablePanel) {
		this.arrangeShelvesTablePanel = arrangeShelvesTablePanel;
	}

	public TableToolBoxPanel getArrangeShelvesTablePanel() {
		return arrangeShelvesTablePanel;
	}
	
	public HashMap<String, List<Object[]>> getRoomShelves() {
		return roomShelves;
	}

	public void setRoomShelves(HashMap<String, List<Object[]>> roomShelves) {
		this.roomShelves = roomShelves;
	}
	
}
