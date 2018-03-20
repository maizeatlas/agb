package org.accretegb.modules.germplasm.inventory;

import static org.accretegb.modules.config.AccreteGBBeanFactory.getContext;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import org.accretegb.modules.constants.ColumnConstants;
import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingWorker;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

import net.miginfocom.swing.MigLayout;

import org.accretegb.modules.MainLayout;
import org.accretegb.modules.config.AccreteGBBeanFactory;
import org.accretegb.modules.config.AccreteGBLogger;
import org.accretegb.modules.customswingcomponent.CheckBoxIndexColumnTable;
import org.accretegb.modules.customswingcomponent.TableToolBoxPanel;
import org.accretegb.modules.customswingcomponent.TextField;
import org.accretegb.modules.customswingcomponent.Utils;
import org.accretegb.modules.germplasm.planting.SyncTags;
import org.accretegb.modules.germplasm.planting.TagGenerator;
import org.accretegb.modules.germplasm.stocksinfo.CreateStocksInfoPanel;
import org.accretegb.modules.germplasm.stocksinfo.StocksInfoPanel;
import org.accretegb.modules.hibernate.Source;
import org.accretegb.modules.hibernate.Location;
import org.accretegb.modules.hibernate.StockPacketContainer;
import org.accretegb.modules.hibernate.Stock;
import org.accretegb.modules.hibernate.StockPacket;
import org.accretegb.modules.hibernate.ContainerLocation;
import org.accretegb.modules.hibernate.HibernateSessionFactory;
import org.accretegb.modules.hibernate.dao.StockPacketContainerDAO;
import org.accretegb.modules.hibernate.dao.StockDAO;
import org.accretegb.modules.hibernate.dao.StockPacketDAO;
import org.accretegb.modules.tab.TabComponentPanel;
import org.accretegb.modules.util.ThreadPool;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.accretegb.modules.hibernate.dao.ContainerLocationDAO;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.context.support.GenericXmlApplicationContext;

/**
 * create, modify or delete packets data by uploading csv file or searching stocks
 * @author tnj
 *
 */
public class PacketsInventoryPanel extends JPanel {

	private static final long serialVersionUID = 1L;
	private JButton uploadButton;
	private JButton searchButton;
	private JTextField filePath;
	private JLabel selectStocksPackets;
	private JComboBox<String> currentInventoryAddress;
	private TableToolBoxPanel stockInventoryTablePanel;
	private CheckBoxIndexColumnTable stockInventoryTable;
	private JProgressBar progress;
	private int stockInventoryTableRowID = 0;
	ArrayList<String> skipped = new ArrayList<String>();
	

	public void initialize() {
		setLayout(new MigLayout("insets 20, gap 0"));
		stockInventoryTable = stockInventoryTablePanel.getTable();
		add(getUploadSearchPanel(), "gaptop 20, growx, spanx, wrap");
		add(stockInventoryTablePanel,"gaptop 5, w 100%, h 100%, grow, span, wrap");
		addStockInventoryTableListeners();
		add(progress, "w 100%, hidemode 3, wrap");
		populateShelves();
		Utils.removeAllRowsFromTable((DefaultTableModel) stockInventoryTable.getModel());
	}
	public void populateShelves(){
		// set Shelf column as JCombobox
		int shelfIndext = stockInventoryTable.getColumn(ColumnConstants.SHELF).getModelIndex();
		TableColumn tc = stockInventoryTable.getColumnModel().getColumn(shelfIndext);
		String[] shelves = getShelvesOption();
		tc.setCellEditor(new MyComboBoxEditor(shelves));
		tc.setCellRenderer(new MyComboBoxRenderer(shelves));			
	}

	/**
	 * MyComboBoxRenderer, MyComboBoxEditor are both for Jtable to have JCombobox cell
	 * @author tnj
	 */
	private class MyComboBoxRenderer extends JComboBox<String> implements TableCellRenderer {
		public MyComboBoxRenderer(String[] items) {
			super(items);
		}

		public Component getTableCellRendererComponent(JTable table,
				Object value, boolean isSelected, boolean hasFocus, int row,int column) {
			setSelectedItem(value);
			return this;
		}
	}

	private class MyComboBoxEditor extends DefaultCellEditor {
		public MyComboBoxEditor(String[] items) {
			super(new JComboBox(items));
		}
	}

	/**
	 * Add inventory table listeners including delete,add and refresh
	 */
	private void addStockInventoryTableListeners() {
		stockInventoryTablePanel.getDeleteButton().setEnabled(false);
		stockInventoryTablePanel.getAddButton().setEnabled(false);
		stockInventoryTablePanel.getAddButton().setEnabled(false);
		stockInventoryTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		stockInventoryTable.putClientProperty("terminateEditOnFocusLost", true);
		stockInventoryTable.getTableHeader().setReorderingAllowed(false);
		stockInventoryTablePanel.getNone().addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent mouseEvent) {
				setTableToolButtons();
				stockInventoryTable.clearSelection();
			}

			public void mouseReleased(MouseEvent mouseEvent) {
				setTableToolButtons();
			}
		});
		stockInventoryTablePanel.getAll().addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent mouseEvent) {
				setTableToolButtons();
			}

			public void mouseReleased(MouseEvent mouseEvent) {
				setTableToolButtons();
			}
		});

		stockInventoryTable.addMouseListener(new MouseAdapter() {
			private int mousePressed;

			@Override
			public void mouseClicked(MouseEvent arg0) {
				mousePressed = stockInventoryTable.rowAtPoint(arg0.getPoint());
				setTableToolButtons();
			}

			public void mousePressed(MouseEvent arg0) {
				mousePressed = stockInventoryTable.rowAtPoint(arg0.getPoint());
			}

			public void mouseReleased(MouseEvent arg0) {
				int mouseReleased = stockInventoryTable.rowAtPoint(arg0.getPoint());
				if (mouseReleased != mousePressed)
					setTableToolButtons();
			}

		});
		stockInventoryTable.getModel().addTableModelListener(new TableModelListener() {
			public void tableChanged(TableModelEvent e) {
				stockInventoryTablePanel.getNumberOfRows().setText(String.valueOf(stockInventoryTablePanel.getTable().getRowCount()));			
			}});
		addDeleteButtonListener();
		addAddButtonListener();
		addSyncButtonListener();
	}

	/**
	 * clear table selection and Enable/disable table tool panel buttons' functionalities
	 */
	private void clearStockInventorySelection() {
		stockInventoryTable.clearSelection();
		setTableToolButtons();
	}

	/**
	 * Enable/disable buttons' functionalities based on certain situations
	 */
	private void setTableToolButtons() {
		boolean validForDelete = stockInventoryTable.getSelectedRowCount() > 0;
		boolean validForRefresh = stockInventoryTable.getRowCount() > 0;
		boolean validForAdd = stockInventoryTable.getSelectedColumnCount() == 1;
		stockInventoryTablePanel.getAddButton().setEnabled(validForAdd);
		stockInventoryTablePanel.getDeleteButton().setEnabled(validForDelete);
		stockInventoryTablePanel.getRefreshButton().setEnabled(validForRefresh);
	}

	/**
	 * Add button adds a packet for selected stock. Stock name keeps same.
	 */
	private void addAddButtonListener() {
		stockInventoryTablePanel.getAddButton().setToolTipText(
				"Add a new packet for selected stock");
		// add new packet for a selected stock
		stockInventoryTablePanel.getAddButton().addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				int selrow = stockInventoryTable.getSelectedRow();
				DefaultTableModel dataModel = (DefaultTableModel) stockInventoryTable.getModel();
				Object rowData[] = new Object[stockInventoryTable.getColumnCount()];
				rowData[0] = new Boolean(false);
				rowData[1] = Integer.toString(stockInventoryTableRowID++);
				rowData[2] = stockInventoryTable.getValueAt(selrow, 2);
				rowData[3] = stockInventoryTable.getValueAt(selrow, 3);
				rowData[4] = "NULL";
				rowData[5] = "NULL";
				rowData[6] = "NULL";
				rowData[7] = "NULL";
				rowData[8] = "NULL";
				rowData[9] = "NULL";
				rowData[10] = "NULL";
				rowData[11] = "padd";
				dataModel.insertRow((selrow + 1), rowData);
				stockInventoryTable.setModel(dataModel);
			}
		});
	}

	/**
	 * Delete button deletes selected rows 
	 */
	private void addDeleteButtonListener() {
		stockInventoryTablePanel.setActionListenerDeleteButton();
	}

	/**
	 * populate table that used to edit packets info by selecting stocks via search button
	 * @param stocksSearchResultsTable
	 * @param selectedRows
	 * @return
	 */
	boolean populateStockInventoryTableFromSearch(CheckBoxIndexColumnTable stocksSearchResultsTable,int[] selectedRows) {
		DefaultTableModel model = (DefaultTableModel) stockInventoryTable.getModel();
		boolean filterDuplicate = false;
		for (int rowCounter = 0; rowCounter < selectedRows.length; rowCounter++) {
			boolean noDuplicate = true;
			Object rowData[] = new Object[stockInventoryTable.getColumnCount()];
			Object newStockid = stocksSearchResultsTable.getValueAt(selectedRows[rowCounter],stocksSearchResultsTable.getColumn(ColumnConstants.STOCK_ID).getModelIndex());
			Object newPacket_no = StockPacketDAO.getInstance().findByStock((Integer)newStockid).size();
			//Object newPacket_no = stocksSearchResultsTable.getValueAt(selectedRows[rowCounter],stocksSearchResultsTable.getColumn(ColumnConstants.PACKET_NO).getModelIndex()).toString();		
			for (int row = 0; row < getStockInventoryTablePanel().getTable().getRowCount(); row++) {
				Object stockid = getStockInventoryTablePanel().getTable().getValueAt(row,getStockInventoryTablePanel().getTable().getColumn(ColumnConstants.STOCK_ID).getModelIndex());
				Object packet_no = getStockInventoryTablePanel().getTable().getValueAt(row,getStockInventoryTablePanel().getTable().getColumn(ColumnConstants.PACKET_NO_INVENTORY).getModelIndex());
				if (packet_no == null)
				{
					packet_no = "NULL";
				}
				if (stockid.equals(newStockid) && packet_no.equals(newPacket_no)) {
					noDuplicate = false;
					filterDuplicate = true;
					break;
				}
			}
			if (noDuplicate) {
				rowData[0] = new Boolean(false);
				rowData[1] = Integer.toString(stockInventoryTableRowID++);
				rowData[2] = newStockid;
				rowData[3] = stocksSearchResultsTable.getValueAt(selectedRows[rowCounter], stocksSearchResultsTable.getColumn(ColumnConstants.STOCK_NAME).getModelIndex());
				rowData[4] = newPacket_no;
				String weight = stocksSearchResultsTable.getValueAt(selectedRows[rowCounter],stocksSearchResultsTable.getColumn(ColumnConstants.WEIGHT).getModelIndex()).toString();
				rowData[5] = weight;
				String no_seed = stocksSearchResultsTable.getValueAt(selectedRows[rowCounter],stocksSearchResultsTable.getColumn(ColumnConstants.NUMBER_OF_SEEDS).getModelIndex()).toString();
				rowData[6] = no_seed;
				String shelf = stocksSearchResultsTable.getValueAt(selectedRows[rowCounter],stocksSearchResultsTable.getColumn(ColumnConstants.SHELF).getModelIndex()).toString();
				if (shelf.equals("NULL")) {
					rowData[7] = "select";
				} else {
					rowData[7] = shelf;
				}
				String unit = stocksSearchResultsTable.getValueAt(selectedRows[rowCounter],stocksSearchResultsTable.getColumn(ColumnConstants.UNIT).getModelIndex()).toString();
				rowData[8] = unit;
				rowData[9] = "NULL";

				if (newPacket_no.equals("NULL")) {
					rowData[10] = "NULL";
				} else {
					StockPacket stock_packet = getStockPacket(Integer.parseInt(String.valueOf(newStockid)),Integer.parseInt(String.valueOf(newPacket_no)));
					if( stock_packet != null){
						rowData[10] = Integer.toString(stock_packet.getStockPacketId());
					}
				}

				if (newPacket_no.equals("NULL")) {
					rowData[11] = "padd";
				} else {
					rowData[11] = "pedit";
				}
				model.addRow(rowData);
				stockInventoryTable.setModel(model);
			}
		}
		return filterDuplicate;
	}


	/**
	 * Sycn database with updated packets info or storage unit unit info
	 */
	private void addSyncButtonListener() {
		progress = new JProgressBar(0, 100);
		progress.setStringPainted(true);
		progress.setVisible(false);
		stockInventoryTablePanel.getRefreshButton().setToolTipText("sync with database");
		stockInventoryTablePanel.getRefreshButton().addActionListener( new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				boolean noEmptyStockNameAndPacketNo = true;
				boolean noSameStockNameAndPacketNo = true;

				for (int rowCounter = 0; rowCounter < stockInventoryTable.getRowCount(); ++rowCounter) {
					String packetNo = (String) stockInventoryTable.getValueAt(rowCounter, stockInventoryTable.getColumn(ColumnConstants.PACKET_NO_INVENTORY).getModelIndex());
					String stockName = (String) stockInventoryTable.getValueAt(rowCounter, stockInventoryTable.getColumn(ColumnConstants.STOCK_NAME).getModelIndex());
					if ( !stockName.equals("NULL") && packetNo.equals("NULL")) {
						noEmptyStockNameAndPacketNo = false;
						break;
					}						
				}
				for (int rowCounter = 0; rowCounter < stockInventoryTable.getRowCount(); ++rowCounter) {
					String packetNoBeCheck = (String) stockInventoryTable.getValueAt(rowCounter, stockInventoryTable.getColumn(ColumnConstants.PACKET_NO_INVENTORY).getModelIndex());
					String stockNameBeCheck = (String) stockInventoryTable.getValueAt(rowCounter, stockInventoryTable.getColumn(ColumnConstants.STOCK_NAME).getModelIndex());
					for (int rowCheck = rowCounter+1; rowCheck < stockInventoryTable.getRowCount(); ++rowCheck) {
						String packetNoByCheck = (String) stockInventoryTable.getValueAt(rowCheck, stockInventoryTable.getColumn(ColumnConstants.PACKET_NO_INVENTORY).getModelIndex());
						String stockNameByCheck = (String) stockInventoryTable.getValueAt(rowCheck, stockInventoryTable.getColumn(ColumnConstants.STOCK_NAME).getModelIndex());
						if(packetNoBeCheck.equals(packetNoByCheck) && stockNameBeCheck.equals(stockNameByCheck) && Utils.isValidInteger(packetNoBeCheck)){
							noSameStockNameAndPacketNo = false;
							break;
						}
					}
				}
				if (noEmptyStockNameAndPacketNo && noSameStockNameAndPacketNo ) {
					new SyncInventory(stockInventoryTable,  progress).execute();

				} else {
					if(!noSameStockNameAndPacketNo)
					{
						JOptionPane.showConfirmDialog((Component) AccreteGBBeanFactory
								.getContext()
								.getBean("stockInventoryTableToolPanel"),
								"Cannot create packets with same stockname and packet number",
								"Error", JOptionPane.DEFAULT_OPTION,
								JOptionPane.ERROR_MESSAGE);

					}else{
						JOptionPane.showConfirmDialog((Component) AccreteGBBeanFactory
								.getContext()
								.getBean("stockInventoryTableToolPanel"),
								"To create a packet, packet number can't be null",
								"Error", JOptionPane.DEFAULT_OPTION,
								JOptionPane.ERROR_MESSAGE);
					}
				}
			}
		});
	}


	/**
	 * get existing container_location entry by shelf id
	 * @param shelfID
	 * @return
	 */
	private ContainerLocation getContainerLocationByShelfID(String shelfID) {
		ContainerLocation existingContainerLocation = null;
		List containerLocations = ContainerLocationDAO.getInstance().findByShelf(shelfID);
		while (containerLocations.size() > 0) {
			existingContainerLocation = (ContainerLocation) containerLocations.get(0);
			containerLocations.remove(0);
		}
		return existingContainerLocation;
	}

	/**
	 * manage stock_packet_container.
	 * Search existing stock_packet_container entry by unitid. If not exist, create new one.
	 * @param unit
	 * @param ContainerLocation
	 * @return
	 */
	private StockPacketContainer managePacketLoc(String unit, ContainerLocation containerLocation) {
		StockPacketContainer existingPacketLoc = null;
		List<StockPacketContainer> stockPacketContainers = StockPacketContainerDAO.getInstance().findByBox(unit); 
		while (stockPacketContainers.size() > 0) {
			existingPacketLoc = stockPacketContainers.get(0);
			StockPacketContainerDAO.getInstance().update(existingPacketLoc, containerLocation);
			stockPacketContainers.remove(0);
		}
		// if there is no existing packet loc, create one which can one be of shelf, unit or both.
		// only shelf: packets are put on shlef without a unit
		// only unit : packets are put in unit that is not on any shelf
		if (existingPacketLoc == null && (containerLocation != null || !unit.equals("NULL"))) {
			StockPacketContainer packetloc = StockPacketContainerDAO.getInstance().insert(containerLocation, null, unit.equals("NULL") ? null : unit.toLowerCase(), null, null);
			existingPacketLoc = packetloc;
		}

		return existingPacketLoc;
	}

	/**
	 * gets container_location entry by unitid, further gets shelf id
	 * @param unit
	 * @return
	 */
	private String getShelfIdFormBox(String unit) { // unitids are unique
		String shelf = "select";
		List result = ContainerLocationDAO.getInstance().findByBox(unit);
		if (!result.isEmpty()) {
			ContainerLocation divstorageunit = (ContainerLocation) ((Object[]) result.get(0))[1];
			shelf = divstorageunit.getShelf();
		}
		return shelf;
	}

	/**
	 * gets exsiting stock entry by stockname
	 * @param stockname
	 * @return
	 */
	private Stock getStock(String stockname) {
		Stock existingStock = null;
		List<Stock> stocks = StockDAO.getInstance().findStockByName(stockname);
		while (stocks.size() > 0) {
			existingStock = stocks.get(0);
			stocks.remove(0);
		}
		return existingStock;
	}

	/**
	 * get existing packet loc info (shelf id and unit id) by packet id
	 * @param packetid
	 * @return
	 */
	private ArrayList<String> getStockPacketContainersFromPacketID(int packetid) {
		ArrayList<String> stockPacketContainers = new ArrayList<String>();
		List result = StockPacketContainerDAO.getInstance().findByPacketID(packetid);
		if (!result.isEmpty()) {
			ContainerLocation containerLocation = (ContainerLocation) ((Object[]) result.get(0))[1];
			StockPacketContainer stockPacketContainer = (StockPacketContainer) ((Object[]) result.get(0))[2];
			stockPacketContainers.add(containerLocation.getShelf());
			stockPacketContainers.add(stockPacketContainer.getUnit());
		}
		return stockPacketContainers;
	}

	/**
	 * gets existing stock_packet entry by stock id and packet_no
	 * @param newStockid
	 * @param packetNo
	 * @return
	 */
	private StockPacket getStockPacket(int newStockid, int packetNo) {
		StockPacket stockPacket = null;
		List<StockPacket> stockPackets = StockPacketDAO.getInstance().findByStockandPacket(newStockid, packetNo);
		while (stockPackets.size() > 0) {
			stockPacket = stockPackets.get(0);
			stockPackets.remove(0);
		}
		return stockPacket;
	}


	/**
	 * Panel that has upload, search buttons, and a JCombobox that shows current storage unit location and name
	 * @return
	 */
	private JPanel getUploadSearchPanel() {
		JPanel registerUploadSearchPanel = new JPanel(new MigLayout("insets 0, gap 5"));
		filePath.setColumns(20);
		uploadButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				
				skipped = new ArrayList<String>();
				uploadButtonActionPerformed();
				
			}
		});
		searchButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				searchButtonActionPerformed();
			}
		});
		populateCurrentInventoryAddress();
		JPanel selectStockPacketsPane = new JPanel(new MigLayout("insets 0,gapx 0"));
		//currentInventoryAddress.setPrototypeDisplayValue("                             ");
		selectStockPacketsPane.add(currentInventoryAddress, "gapleft 7");
		selectStockPacketsPane.add(uploadButton, "gapleft 3, split 2");
		selectStockPacketsPane.add(searchButton);
		registerUploadSearchPanel.add(selectStockPacketsPane,"spanx, pushy, wrap");
		return registerUploadSearchPanel;
	}

	/**
	 * get existing shelves ids for populating shelf column
	 * @return
	 */
	public String[] getShelvesOption() {
		String[] shelves = null;
		List<ContainerLocation> results = ContainerLocationDAO.getInstance().findAllContainerLocations();
		shelves = new String[results.size() + 1];
		shelves[0] = ("select");
		int index = 1;
		for (ContainerLocation containerLocation : results) {
			shelves[index] = containerLocation.getShelf();
			index++;
		}
		return shelves;
	}

	/**
	 * populate JCombobox that shows current storage units location and name
	 */
	public void populateCurrentInventoryAddress() {
		if (currentInventoryAddress.getItemCount() == 0) {
			currentInventoryAddress.addItem("Please register storage units");
		} else {
			currentInventoryAddress.removeAllItems();
			currentInventoryAddress.addItem("Storage Units List");
			List location = ContainerLocationDAO.getInstance().findByLocation();
			if (!location.isEmpty()) {
				for (int i = 0; i < location.size(); i++) {
					Location divLocation = (Location) ((Object[]) location.get(i))[0];
					ContainerLocation divstorageunit = (ContainerLocation) ((Object[]) location.get(i))[1];
					StringBuilder LocationInfo = new StringBuilder();
					LocationInfo.append(divLocation.getZipcode());
					LocationInfo.append(",");
					LocationInfo.append(divLocation.getLocationName());
					LocationInfo.append(",");
					LocationInfo.append(divLocation.getCity());
					LocationInfo.append(",");
					LocationInfo.append(divLocation.getStateProvince());
					LocationInfo.append(",");
					LocationInfo.append(divLocation.getCountry());
					String containerLocations = LocationInfo.toString() + ","+ divstorageunit.getBuilding() + ","+ divstorageunit.getRoom();
					currentInventoryAddress.addItem(containerLocations);
				}
			}
			/* read only list
			currentInventoryAddress.addItemListener(new ItemListener() {
				public void itemStateChanged(ItemEvent event) {
					if (event.getStateChange() == ItemEvent.SELECTED) {
						currentInventoryAddress.setSelectedIndex(0);
					}
				}
			});*/
		}
	}


	/**
	 * choose file to upload, read lines and call populateStockInventoryTableFromUpload to populate table
	 */
	public void uploadButtonActionPerformed() {
		try {
			final JFileChooser fc = new JFileChooser();
			fc.setFileFilter(new FileNameExtensionFilter("Comma separated files(.csv)", "csv"));
			int status = fc.showOpenDialog(this);
			if (fc.getSelectedFile() == null)
			{
				return;
			}
			String filename = fc.getSelectedFile().toString();
			filePath.setText(filename);
			if (status == JFileChooser.APPROVE_OPTION) {
				getUploadButton().setText("Loading...");
				getUploadButton().setEnabled(false);
				Utils.removeAllRowsFromTable((DefaultTableModel) stockInventoryTable.getModel());
				stockInventoryTableRowID = 0;
				if (!(filePath.getText().trim().endsWith(".csv"))) {
					JLabel errorFields = new JLabel("<HTML><FONT COLOR = Blue>"
							+ "File should have a .csv extension only"
							+ ".</FONT></HTML>");
					JOptionPane.showMessageDialog((Component) AccreteGBBeanFactory.getContext().getBean("stockInventoryTableToolPanel"),errorFields);
				} else {
					stockInventoryTablePanel.getRefreshButton().setEnabled(false);
					new ReadFile(stockInventoryTable,  progress).execute();					
				}
			}
		} catch (Exception E) {
			E.printStackTrace();
		}
	}
	
	private class ReadFile extends SwingWorker<Void, Void> {
		private CheckBoxIndexColumnTable table;
		
		public ReadFile(CheckBoxIndexColumnTable table, JProgressBar progress) {
			this.table = table;
		}
		
		@Override
		protected Void doInBackground() throws Exception {
			String filepath = filePath.getText();
			BufferedReader br = new BufferedReader(new FileReader(new File(filepath)));
			String line = br.readLine().trim();
			String unitORshelfId = "-1";
			String operation = "oper";
			String packet_name = "";
			while (line != null) {
				line = line.trim();
				if (line.substring(0, 1).equals("p") || line.substring(0, 1).equals("b")) { // padd,pmove,pdelete,badd,bmove,bdelete
					operation = line;
					line = br.readLine();
				}
				if (operation.substring(0, 1).equals("p")) {
					if(operation.equals("pdelete")){
						populateStockInventoryTableFromUpload(operation, "NULL", line, "");
						line = br.readLine();
					}else{
						if (!line.contains(".")) {
							unitORshelfId = line;
							line = br.readLine();
						} else {
							packet_name = line;
							line = br.readLine();
							if(line != null){
								line = line.trim();
							}
							if(line != null && !line.substring(0, 1).equals("p") && !line.substring(0, 1).equals("b")
									&& Utils.isDouble(line)){
								// line is weight
								populateStockInventoryTableFromUpload(operation, unitORshelfId, packet_name, line);	
								line = br.readLine();
							}else{
								populateStockInventoryTableFromUpload(operation, unitORshelfId, packet_name, "");
							}		
						}
					}
				} else if (operation.substring(0, 1).equals("b")) {
					if(operation.equals("bdelete")){
						populateStockInventoryTableFromUpload(operation, "select", line, "");
						line = br.readLine();
					}else{
						if (line.contains("-")) {
							unitORshelfId = line;
							line = br.readLine();
						} else {
							populateStockInventoryTableFromUpload(operation, unitORshelfId, line, "");
							line = br.readLine();
						}
					}
				} else {
					String packetID = line;
					String weight = br.readLine();
					if (weight != null) {
						weight = weight.trim();
					}
					populateStockInventoryTableFromUpload("+Weight","", packetID, weight);
					line = br.readLine();
				}
			}
			br.close();
			return null;
		}
		/**
		 * populate one table row with packets related info by uploading .csv file
		 * @param operation: operations that indicated in .csv, including padd, pmove, pdelete, badd,bmove,bdelte
		 * @param unitORshelfId: unitid if operation edits packets' location. shelf id if operation edits unites' location
		 * @param packetORbox: packet is operation  edits packets' location. unit if operation edits unites' location
		 * @param weight: "NULL" if operation edits location
		 */
		protected void populateStockInventoryTableFromUpload(String operation, String unitORshelfId, String packetORbox, String weight) {
			final DefaultTableModel model = (DefaultTableModel) this.table.getModel();
			final Object rowData[] = new Object[this.table.getColumnCount()];
			//System.out.println(operation + " > " + unitORshelfId + " > " + packetORbox + " > " + weight);
			// operations on packets' loc with or without weight
			if (operation.substring(0, 1).equals("p")) 
			{   
				String[] tmp = packetORbox.split("#");
				String stockname = tmp[0];
				String packetNo = tmp[1];
				Stock stock = getStock(stockname);
				if(stock == null){
					skipped.add(packetORbox);
					return;
				}
				int newStockid = stock.getStockId();
				StockPacket stockPacket = getStockPacket(newStockid,Integer.parseInt(packetNo));
				int packetId = stockPacket == null ? -1 : stockPacket.getStockPacketId();
				weight = weight==""?"NULL":weight ;
				String no_seed = "NULL";
				String comment = "NULL";
				operation = operation.equals("pdelete")  || operation.equals("pmove")? operation : "pedit";
				rowData[0] = new Boolean(false);
				rowData[1] = Integer.toString(stockInventoryTableRowID++);
				rowData[2] = newStockid;
				rowData[3] = stockname;
				rowData[4] = packetNo;
				rowData[5] = weight;
				rowData[6] = no_seed;
				rowData[7] = "select";
				rowData[8] = unitORshelfId;
				rowData[9] = comment;
				rowData[10] = packetId == -1 ? "NULL" : Integer.toString(packetId);
				rowData[11] = operation;
			}
			// operations on unites' loc
			else if (operation.substring(0, 1).equals("b")) 
			{
				
				rowData[0] = new Boolean(false);
				rowData[1] = Integer.toString(stockInventoryTableRowID++);
				rowData[2] = "NULL";
				rowData[3] = "NULL";
				rowData[4] = "NULL";
				rowData[5] = "NULL";
				rowData[6] = "NULL";
				rowData[7] = unitORshelfId;
				rowData[8] = packetORbox;
				rowData[9] = "NULL";
				rowData[10] = "NULL";
				rowData[11] = operation;
			} 
			// operations are to add packets' weight
			else if (operation.equals("+Weight")) {
				String[] tmp = packetORbox.split("#");
				String stockname = tmp[0];
				String packetNo = tmp[1];
				Stock stock = getStock(stockname);
				if(stock == null){
					skipped.add(packetORbox);
					return;
				}
				int newStockid = stock.getStockId();
				StockPacket stockPacket = getStockPacket(newStockid,Integer.parseInt(packetNo));
				int packetId = stockPacket == null ? -1 : stockPacket.getStockPacketId();
				String no_seed = "NULL";
				String comment = "NULL";
				rowData[0] = new Boolean(false);
				rowData[1] = Integer.toString(stockInventoryTableRowID++);
				//rowData[2] = newStockid;
				rowData[3] = stockname;
				rowData[4] = packetNo;
				rowData[5] = weight;
				rowData[6] = no_seed;
				rowData[7] = "select";
				rowData[8] = "NULL";
				rowData[9] = comment;
				rowData[10] = packetId == -1 ? "NULL" : Integer.toString(packetId);
				rowData[11] = operation;
			}

			model.addRow(rowData);
		}
		
		@Override
		public void done() {
			stockInventoryTable.repaint();
			stockInventoryTablePanel.getRefreshButton().setEnabled(true);
			clearStockInventorySelection();
			getUploadButton().setEnabled(true);
			getUploadButton().setText("Upload");
			if(skipped.size() > 0){
				String skippedmessage = "";
				for(String packet : skipped){
					skippedmessage = skippedmessage +  packet+ "\n";
				}
				JTextArea textArea = new JTextArea(skippedmessage);
				JScrollPane scrollPane = new JScrollPane(textArea);  
				textArea.setLineWrap(true);  
				textArea.setWrapStyleWord(true); 
				scrollPane.setPreferredSize( new Dimension( 300, 500 ) );
				JOptionPane.showMessageDialog(null, scrollPane, "Packet Stocks Not Found", 
						JOptionPane.YES_NO_OPTION);
			}
		}
	}
	
	private class SyncInventory extends SwingWorker<Void, Void> {

		private CheckBoxIndexColumnTable table;
		private JProgressBar progress;
		private long initialTime;

		public SyncInventory(CheckBoxIndexColumnTable table, JProgressBar progress) {
			this.table = table;
			this.progress = progress;
		}

		@Override
		protected Void doInBackground() throws Exception {
			initialTime = System.currentTimeMillis();
			progress.setVisible(true);
			progress.setValue(0);
			long duration = System.currentTimeMillis() - initialTime;
			for (int rowCounter = 0; rowCounter < table.getRowCount(); ++rowCounter) {
				String[] values = {
						String.valueOf(table.getValueAt(rowCounter,table.getIndexOf(ColumnConstants.STOCK_NAME))),
						String.valueOf(table.getValueAt(rowCounter,table.getIndexOf(ColumnConstants.PACKET_NO_INVENTORY))),
						String.valueOf(table.getValueAt(rowCounter,table.getIndexOf(ColumnConstants.WEIGHT))),
						String.valueOf(table.getValueAt(rowCounter,table.getIndexOf(ColumnConstants.NUMBER_OF_SEEDS))),
						String.valueOf(table.getValueAt(rowCounter,table.getIndexOf(ColumnConstants.SHELF))),
						String.valueOf(table.getValueAt(rowCounter,table.getIndexOf(ColumnConstants.UNIT))),
						String.valueOf(table.getValueAt(rowCounter,table.getIndexOf(ColumnConstants.PACKET_COMMENT))),
						String.valueOf(table.getValueAt(rowCounter,table.getIndexOf(ColumnConstants.PACKET_ID))),
						String.valueOf(table.getValueAt(rowCounter,table.getIndexOf(ColumnConstants.OPERATION))) 
				};

				// operations on packets <create/ delete/ update>
				if (!values[0].equalsIgnoreCase("NULL")) { 
					Stock stock = getStock(values[0]);					
					StockPacket stockPacket  = new StockPacket();
					ContainerLocation containerLocation = getContainerLocationByShelfID(values[4]);
					StockPacketContainer sc = managePacketLoc(values[5], containerLocation);
					stockPacket.setStock(stock);
					stockPacket.setStockPacketContainer(sc);
					stockPacket.setPacketNo(Integer.parseInt(values[1]));
					stockPacket.setWeight(values[2].trim().equalsIgnoreCase("NULL") ? null : Double .parseDouble(values[2]));
					stockPacket.setNoSeed(values[3].trim().equalsIgnoreCase("NULL") ? null : Integer.parseInt(values[3]));
					stockPacket.setStockPacketDate(new Date());
					stockPacket.setStockPacketComments(values[6].equalsIgnoreCase("NULL") ? null : values[6]);
					if (values[7].equalsIgnoreCase("NULL")) { 
						StockPacket existingStockPacket = getStockPacket(stock.getStockId(),Integer.parseInt(values[1]));
						if (existingStockPacket != null) {
							values[7] = String.valueOf(existingStockPacket.getStockPacketId());
						}

					}
					// create new packet when stock packet id is null
					if (values[7].equalsIgnoreCase("NULL")) { 
						// if stock packet id is null but operation is delete, it means that user is making mistake, no existing packet can be deleted. Operation gets ignored
						if (!values[8].equals("pdelete")) { 
							int packetid= StockPacketDAO.getInstance().insert(stockPacket).getStockPacketId();
							table.setValueAt(Integer.toString(packetid), rowCounter, table.getColumn(ColumnConstants.PACKET_ID).getModelIndex());
						}
					} else {
						int stockPacketID = Integer.parseInt(values[7]);											
						//delete packet
						if (values[8].equals("pdelete")) { 
							StockPacketDAO.getInstance().delete(stockPacketID);
						}
						//update packet
						else {
							StockPacketDAO.getInstance().updateStockPacket(stockPacket, stockPacketID);
						}
					}
				}
				// operations on packetloc (boxes)
				else {
					ContainerLocation containerLocation = getContainerLocationByShelfID(values[4]);
					managePacketLoc(values[5], containerLocation);
					if (values[8].equals("bdelete")) {
						StockPacketContainerDAO.getInstance().deleteByUnit(values[5]);
					}
				}
				progress.setValue((int) ((rowCounter * 1.0 / table.getRowCount()) * 100));
			}
			return null;
		}

		@Override
		public void done() {
			long duration = System.currentTimeMillis() - initialTime;
			System.out.println("Time: " + duration / 1000);
			progress.setVisible(false);
			stockInventoryTable.setHasSynced(true);
			stockInventoryTable.repaint();
			clearStockInventorySelection();
		}

	}

	/**
	 * pop up stocks selection panel
	 */
	public void searchButtonActionPerformed() {
		StocksInfoPanel stockInfoPanel = CreateStocksInfoPanel.createStockInfoPanel("inventory popup");
		JPanel popupPanel = new JPanel(new MigLayout("insets 0, gapx 5"));
		popupPanel.add(stockInfoPanel, "w 100%, h 100%");
		int option = JOptionPane.showConfirmDialog(
				(Component) AccreteGBBeanFactory.getContext().getBean("stockInventoryTableToolPanel"),
				stockInfoPanel,
				"Search Stock Packets", JOptionPane.OK_CANCEL_OPTION,
				JOptionPane.PLAIN_MESSAGE);
		if (option == JOptionPane.OK_OPTION) {

			boolean filterDuplicate = populateStockInventoryTableFromSearch(
					stockInfoPanel.getSearchResultsPanel().getTable(),
					stockInfoPanel.getSearchResultsPanel().getTable().getSelectedRows());
			if (filterDuplicate)
				JOptionPane.showConfirmDialog(
						(Component) AccreteGBBeanFactory.getContext().getBean("stockInventoryTableToolPanel"),
						"Duplicate selections have been filtered out", "",
						JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE);
		}
	}



	public JButton getUploadButton() {
		return uploadButton;
	}

	public void setUploadButton(JButton uploadButton) {
		this.uploadButton = uploadButton;
	}

	public JButton getSearchButton() {
		return searchButton;
	}

	public void setSearchButton(JButton searchButton) {
		this.searchButton = searchButton;
	}

	public JTextField getFilePath() {
		return filePath;
	}

	public void setFilePath(JTextField filePath) {
		this.filePath = filePath;
	}

	public JLabel getSelectStocksPackets() {
		return selectStocksPackets;
	}

	public void setSelectStocksPackets(JLabel selectStocksPackets) {
		this.selectStocksPackets = selectStocksPackets;
	}

	public JComboBox<String> getCurrentInventoryAddress() {
		return currentInventoryAddress;
	}

	public void setCurrentInventoryAddress(
			JComboBox<String> currentInventoryAddress) {
		this.currentInventoryAddress = currentInventoryAddress;
	}

	public TableToolBoxPanel getStockInventoryTablePanel() {
		return stockInventoryTablePanel;
	}

	public void setStockInventoryTablePanel(
			TableToolBoxPanel stockInventoryTablePanel) {
		this.stockInventoryTablePanel = stockInventoryTablePanel;
	}


}
