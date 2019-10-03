package org.accretegb.modules.phenotype;

import static org.accretegb.modules.customswingcomponent.Utils.removeAllRowsFromTable;
import static org.accretegb.modules.customswingcomponent.Utils.saveTableToFile;

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.Map.Entry;

import javax.swing.BorderFactory;
import javax.swing.DefaultCellEditor;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.RowFilter;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.event.MouseInputAdapter;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableRowSorter;
import javax.swing.text.JTextComponent;

import org.accretegb.modules.constants.ColumnConstants;
import org.accretegb.modules.constants.PhenotypeConstants;
import org.accretegb.modules.customswingcomponent.CheckBoxIndexColumnTable;
import org.accretegb.modules.customswingcomponent.ImageButton;
import org.accretegb.modules.customswingcomponent.TableToolBoxPanel;
import org.accretegb.modules.customswingcomponent.TextField;
import org.accretegb.modules.customswingcomponent.Utils;
import org.accretegb.modules.hibernate.MeasurementParameter;
import org.accretegb.modules.hibernate.MeasurementUnit;
import org.accretegb.modules.hibernate.dao.MeasurementValueDAO;
import org.accretegb.modules.hibernate.dao.MeasurementParameterDAO;
import org.accretegb.modules.hibernate.dao.ObservationUnitDAO;
import org.accretegb.modules.sampling.SampleSelectionPanel;
import org.accretegb.modules.util.ChangeMonitor;
import org.accretegb.modules.hibernate.dao.MeasurementUnitDAO;
import org.jdesktop.swingx.autocomplete.AutoCompleteDecorator;

import net.miginfocom.swing.MigLayout;

/**
 * Choose descriptors and parameters to be exported for observation_unit_ids
 * @author Ningjing
 *
 */
public class PhenotypeExportPanel extends JPanel {
	private JButton searchTagsButton;
	private JButton exportButton;
	private TableToolBoxPanel phenotypeTagsTablePanel;
	private JList descriptorsOptions;
	private JList descriptorsSelected;
	private JList parameterOptions;
	private JList parameterSelected;
	private Vector tableData;
	private TextField commentField;
	private String currentSubsetName;
	private JComboBox  uniTypeInfo;
	private JCheckBox codeAsCol = new JCheckBox();
	private Boolean isFirstSync = true;
	private SampleSelectionPanel sampleSelectionPanel;
	private JProgressBar progressBar = new JProgressBar();
	private LinkedHashMap<String, Object[][]> subsetTableMap = new LinkedHashMap<String, Object[][]>();
	private LinkedHashMap<String, String> subsetCommentMap = new LinkedHashMap<String, String>();
	private LinkedHashMap<String, List<List<String>>> subsetJlistMap = new LinkedHashMap<String, List<List<String>>>();
	private LinkedHashMap<String, List<String>> parameterInfoMap;
	private int projectID = -1;

	public void initialize() {
		initializeOptionsForDescriptors();
		initializeOptionsForParameters();
		setLayout(new MigLayout("insets 10, gap 5"));
		JPanel subsetPanel = phenotypeTagsTablePanel.getBottomHorizonPanel();
		subsetPanel.add(addStockSetComment(),"push, gapleft 10%, w 60%, al right");
		subsetPanel.add(createSubsetButton()," push, al right, wrap");
		phenotypeTagsTablePanel.getTable().setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		add(phenotypeTagsTablePanel," w 100%, h 50%,  wrap");

		JPanel selectionPanel = new JPanel(new MigLayout("insets 0, gap 15"));
		selectionPanel.add(addSelectDescriptorPanel(),"w 50%, h 100%");
		selectionPanel.add(addSelectParametersPanel(),"w 50%, h 100%");
		add(selectionPanel,"gapleft 4, w 100%, h 50%, wrap");

		JPanel exportPanel = new JPanel(new MigLayout("insets 0 0 0 0"));
		exportPanel.add(new JLabel("Export Parameter Code:"),"push, al right");
		exportPanel.add(codeAsCol);
		exportPanel.add(getExportButton());
		add(exportPanel," w 100%,wrap");
		add(progressBar, "gapleft 5, gapright 5, hidemode 3, span, grow, wrap");
		progressBar.setVisible(false);
		progressBar.setStringPainted(true);
		populateTable();
		initializeSubsetComboBox();
		initializeUnitTypeJCombobox();
		exportButtonListener();
		removeAllRowsFromTable((DefaultTableModel)getPhenotypeTagsTablePanel().getTable().getModel());

	}

	private JPanel addStockSetComment(){
		JPanel panel = new JPanel(new MigLayout("insets 0 0 0 0"));
		JLabel label = new JLabel("Stock Set Comment:");
		TextField commentField = new TextField(30); 
		commentField.setPlaceholder("All");
		commentField.setForeground(new Color(160, 160, 160));
		commentField.addFocusListener(new FocusListener(){

			public void focusGained(FocusEvent e) {
				// TODO Auto-generated method stub

			}

			public void focusLost(FocusEvent e) {
				getSubsetCommentMap().put(getCurrentSubsetName(), getCommentField().getText().trim());				
				sampleSelectionPanel.getSubsetCommentMap().put(getCurrentSubsetName(), getCommentField().getText().trim());
			}

		});
		setCommentField(commentField);
		panel.add(label);
		panel.add(commentField,"w 100%");
		return panel;
	}

	private void initializeUnitTypeJCombobox(){
		List<MeasurementUnit> existUnitypes = MeasurementUnitDAO.getInstance().findAll();
		String unitsInfo[] = new String[existUnitypes.size()];
		int i = 0;
		for(MeasurementUnit uniType :existUnitypes ){   	
			unitsInfo[i]=uniType.getUnitType();
			i++;
		}       
		final JComboBox<String> unitssComboBox = new JComboBox<String>(unitsInfo);
		unitssComboBox.setEditable(true);    
		AutoCompleteDecorator.decorate(unitssComboBox);
		unitssComboBox.setPreferredSize(new Dimension(200, 0));
		setUniTypeInfo(unitssComboBox);
	}

	private JPanel addSelectDescriptorPanel(){
		JPanel panel = new JPanel(new MigLayout("insets 0 0 0 0"));

		JScrollPane scrollPaneOption = new JScrollPane();
		scrollPaneOption.add(getDescriptorsOptions());		
		scrollPaneOption.setViewportView(getDescriptorsOptions());		
		scrollPaneOption.setOpaque(false);
		scrollPaneOption.setBorder(BorderFactory.createTitledBorder("Descriptors"));

		JScrollPane scrollPaneSelect = new JScrollPane();
		scrollPaneSelect.add(getDescriptorsSelected());
		scrollPaneSelect.setOpaque(false);
		scrollPaneSelect.setViewportView(getDescriptorsSelected());		
		scrollPaneSelect.setBorder(BorderFactory.createTitledBorder("Selected"));			

		DefaultListModel listModel  = new DefaultListModel();
		getDescriptorsSelected().setModel(listModel);
		getDescriptorsSelected().setToolTipText("Drag item to reoder it");
		dragAndDropMouseAdaptor dragAndDropMouseAdaptor =new dragAndDropMouseAdaptor(getDescriptorsSelected());
		getDescriptorsSelected().addMouseListener(dragAndDropMouseAdaptor);
		getDescriptorsSelected().addMouseMotionListener(dragAndDropMouseAdaptor);

		JButton add = new JButton("+");
		add.addActionListener(new ActionListener(){

			public void actionPerformed(ActionEvent e) {
				List selected = getDescriptorsOptions().getSelectedValuesList();
				DefaultListModel listModel = (DefaultListModel)getDescriptorsSelected().getModel();
				List existing = new ArrayList<Object>();
				for(int i=0; i < listModel.getSize(); i++){
					Object o =  listModel.getElementAt(i);  
					existing.add(o);
				}			
				for(Object item : selected){
					if(!existing.contains(item)){
						listModel.addElement(item);
					}					
				}			
				getDescriptorsSelected().setModel(listModel);
				getDescriptorsOptions().clearSelection();
				updateSubsetJlistMap();
			}


		});

		JButton remove = new JButton("-");
		remove.addActionListener(new ActionListener(){

			public void actionPerformed(ActionEvent e) {
				Object[] selected = getDescriptorsSelected().getSelectedValues(); 
				DefaultListModel listModel = (DefaultListModel) getDescriptorsSelected().getModel();
				for(Object item : selected){
					listModel.removeElement(item);
				}
				updateSubsetJlistMap();
			}

		});
		panel.add(scrollPaneOption,"w 50%, h 100%, west");
		panel.add(add,   "cell 1 0,  al center, gaptop 120, hmax 20, wmax 30");
		panel.add(remove,"cell 1 1,  al center, gapbottom 100, hmax 20, wmax 30");
		panel.add(scrollPaneSelect,"gapleft 5, w 50%, h 100%, east");

		return panel;
	}

	private void updateSubsetJlistMap(){
		List<String> deslist = new ArrayList<String>();
		DefaultListModel deslistModel = (DefaultListModel)getDescriptorsSelected().getModel();
		for(int i=0; i < deslistModel.getSize(); i++){
			Object o =  deslistModel.getElementAt(i);  
			deslist.add(o.toString());
		}

		DefaultListModel paralistModel = (DefaultListModel)getParameterSelected().getModel();
		List<String> paralist = new ArrayList<String>();
		for(int i=0; i < paralistModel.getSize(); i++){
			Object o =  paralistModel.getElementAt(i);  
			paralist.add(o.toString());
		}

		List<List<String>> subsetJlist = new ArrayList<List<String>>();
		subsetJlist.add(deslist);
		subsetJlist.add(paralist);
		getSubsetJlistMap().put(getCurrentSubsetName(), subsetJlist);
		ChangeMonitor.markAsChanged(projectID);
	}

	private void initializeOptionsForDescriptors(){
		Object descriptors[] = {"tagname","stock_name","coordinate_x", "coordinate_y", "coordinate_z", "plot", "row","plant","purpose",
				"planting_date","harvest_date","delay","city","state_province","country","zipcode","field_name","field_number",
				"altitude","latitude","longitude", "accession_name","pedigree","generation","cycle","mating_type","mate_role",
				"mate_method_name","mate_method_desc",PhenotypeConstants.EXPERIMENT_INFO
		};
		DefaultListModel listModel = new DefaultListModel();
		for(Object option : descriptors){
			listModel.addElement(option);
		}
		JList list = new JList(listModel);
		setDescriptorsOptions(list);		
		list.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

	}


	private JPanel addSelectParametersPanel(){
		JPanel panel = new JPanel(new MigLayout("insets 0 0 0 0"));

		JScrollPane scrollPaneOption = new JScrollPane();
		scrollPaneOption.add(getParameterOptions());		
		scrollPaneOption.setViewportView(getParameterOptions());	
		scrollPaneOption.setOpaque(false);
		scrollPaneOption.setBorder(BorderFactory.createTitledBorder("Parameter Names"));

		JScrollPane scrollPaneSelect = new JScrollPane();
		scrollPaneSelect.add(getParameterSelected());
		scrollPaneSelect.setOpaque(false);
		scrollPaneSelect.setViewportView(getParameterSelected());		
		scrollPaneSelect.setBorder(BorderFactory.createTitledBorder("Selected"));	

		DefaultListModel listModel  = new DefaultListModel();
		getParameterSelected().setModel(listModel);
		getParameterSelected().setToolTipText("Drag item to reoder it");
		dragAndDropMouseAdaptor dragAndDropMouseAdaptor =new dragAndDropMouseAdaptor(getParameterSelected());
		getParameterSelected().addMouseListener(dragAndDropMouseAdaptor);
		getParameterSelected().addMouseMotionListener(dragAndDropMouseAdaptor);
		getParameterOptions().setToolTipText("<html>Double left-click item to edit it.<br>Right-click item to delete it.</html>");

		JButton add = new JButton("+");
		add.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				List selected = getParameterOptions().getSelectedValuesList();
				DefaultListModel listModel = (DefaultListModel)getParameterSelected().getModel();
				List existing = new ArrayList<Object>();
				for(int i=0; i < listModel.getSize(); i++){
					Object o =  listModel.getElementAt(i);  
					existing.add(o);
				}			
				for(Object item : selected){
					if(!existing.contains(item)){
						if(!item.equals(PhenotypeConstants.ADD_NEW_PARAMETER))
						{
							listModel.addElement(item);
						}
					}					
				}			
				getParameterSelected().setModel(listModel);
				getParameterOptions().clearSelection();
				updateSubsetJlistMap();
			}

		});

		JButton remove = new JButton("-");
		remove.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				Object[] selected = getParameterSelected().getSelectedValues(); 
				DefaultListModel listModel = (DefaultListModel) getParameterSelected().getModel();
				for(Object item : selected){
					listModel.removeElement(item);
				}
				updateSubsetJlistMap();
			}	    	
		});
		panel.add(scrollPaneOption,"w 50%, h 100%, west");
		panel.add(add,   "cell 1 0,  al center, gaptop 120, hmax 20, wmax 30");
		panel.add(remove,"cell 1 1,  al center, gapbottom 100, hmax 20, wmax 30");
		panel.add(scrollPaneSelect,"gapleft 5, w 50%, h 100%, east");		
		return panel;
	}


	private void initializeOptionsForParameters(){
		List parameters = new ArrayList<String>();
		parameters.add(PhenotypeConstants.ADD_NEW_PARAMETER);
		List<MeasurementParameter> measurementParameters = MeasurementParameterDAO.getInstance().findAll();
		LinkedHashMap<String, List<String>> parameterInfomap = new LinkedHashMap<String, List<String>>();

		for(MeasurementParameter dmp : measurementParameters){
			parameters.add(dmp.getParameterName());
			List<String> parainfo = new ArrayList<String>();
			parainfo.add(dmp.getParameterName());
			parainfo.add(dmp.getParameterCode());
			parainfo.add(dmp.getMeasurementClassification());
			parainfo.add(dmp.getOntologyAccession());
			parainfo.add(dmp.getProtocol());
			parainfo.add(dmp.getFormat());
			parainfo.add(dmp.getDefaultValue());
			parainfo.add(dmp.getMinValue());
			parainfo.add(dmp.getMaxValue());
			parainfo.add(dmp.getCategories());
			parainfo.add(dmp.getIsVisible());
			if(dmp.getMeasurementUnit()!= null)
			{
				List<MeasurementUnit> unitOfMeasureList = MeasurementUnitDAO.getInstance().findByID(dmp.getMeasurementUnit().getMeasurementUnitId());
				parainfo.add(unitOfMeasureList.get(0).getUnitType());
			}
			else{
				parainfo.add(null);
			}
			parainfo.add(String.valueOf(dmp.getMeasurementParameterId()));
			parameterInfomap.put(dmp.getParameterName(), parainfo);

		}		
		setParameterInfoMap(parameterInfomap);				
		DefaultListModel listModel = new DefaultListModel();
		for(Object option : parameters){
			listModel.addElement(option);
		}
		JList list = new JList(listModel);
		setParameterOptions(list);		
		list.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

		getParameterOptions().addMouseListener(new MouseListener(){
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() == 2) {
					editParameters(e);

				}else if(SwingUtilities.isRightMouseButton(e) ){
					deleteParameter(e);
				}

			}

			public void editParameters(MouseEvent e){
				JList list = (JList) e.getSource();
				if(list.getSelectedValue()!=null)
				{	
					if(list.getSelectedValue().equals(PhenotypeConstants.ADD_NEW_PARAMETER)){					
						addNewParameterPanel(list);				
					}else{

						updateParameterPanel(list);
					}
				}
			}

			public void deleteParameter(MouseEvent e){
				JList list = (JList) e.getSource();
				int row = list.locationToIndex(e.getPoint());
				list.setSelectedIndex(row);
				if(!list.getSelectedValue().equals(PhenotypeConstants.ADD_NEW_PARAMETER)){
					int dialogResult = JOptionPane.showConfirmDialog(null, "Are you sure to delete parameter "+list.getSelectedValue(), "Delete Parameter",JOptionPane.YES_NO_OPTION);
					if(dialogResult==JOptionPane.YES_OPTION)
					{
						int deleteId = Integer.parseInt(getParameterInfoMap().get(list.getSelectedValue()).get(12));
						if(MeasurementValueDAO.getInstance().findByParameterId(deleteId)){
							JOptionPane.showConfirmDialog(null,
									"You can not delete parameter that is being used by other data", "",
									JOptionPane.DEFAULT_OPTION,
									JOptionPane.PLAIN_MESSAGE);
						}else{
							MeasurementParameterDAO.getInstance().delete(deleteId);
							((DefaultListModel)list.getModel()).removeElementAt(row);
						}

					}
				}
			}
			public void mousePressed(MouseEvent e) {
				// TODO Auto-generated method stub

			}

			public void mouseReleased(MouseEvent e) {
				// TODO Auto-generated method stub

			}

			public void mouseEntered(MouseEvent e) {
				// TODO Auto-generated method stub

			}

			public void mouseExited(MouseEvent e) {
				// TODO Auto-generated method stub

			}

		});			
	}

	private void addNewParameterPanel(JList list){
		String labelNames[] = { "<HTML>Parameter Name<FONT COLOR = Red>*</FONT></HTML>", "Parameter Code","Type","ToAccession","Protocol", 
				"Format", "DefaultValue","MinValue","MaxValue","Categories","<HTML>IsVisible<FONT COLOR = Red>*</FONT></HTML>","<HTML>Unit<FONT COLOR = Red>*</FONT></HTML>" };
		String values[] = { "", "", "", "", "" ,"", "", "", "", "" ,"",""};
		JLabel labels[] = new JLabel[labelNames.length];
		final JTextField textBoxes[] = new JTextField[labelNames.length-1];
		JComboBox units = getUniTypeInfo();

		boolean invalid = true;
		boolean notcancel = true;
		while(invalid && notcancel){
			JPanel addNewParemeterPanel = new JPanel(new MigLayout("insets 0, gapx 0"));
			for (int labelIndex = 0; labelIndex < labels.length; labelIndex++) {
				labels[labelIndex] = new JLabel(labelNames[labelIndex]);
				addNewParemeterPanel.add(labels[labelIndex],"gapleft 10, push");
				if(labelIndex!=labels.length-1)
				{
					textBoxes[labelIndex] = new JTextField();
					textBoxes[labelIndex].setPreferredSize(new Dimension(200, 0));
					textBoxes[labelIndex].setText(values[labelIndex]);
					addNewParemeterPanel.add(textBoxes[labelIndex],"growx, gapRight 10, wrap");

				}else{
					((JTextComponent) units.getEditor().getEditorComponent()).setText(values[labelIndex]);
					addNewParemeterPanel.add(units,"gapRight 10, wrap");									
				}


			}
			int newParameterOption = JOptionPane.showConfirmDialog(
					null, addNewParemeterPanel,
					"Enter New Parameter Information ",
					JOptionPane.OK_CANCEL_OPTION);
			if (newParameterOption == JOptionPane.CANCEL_OPTION) {
				notcancel = false;
				list.clearSelection();
			}
			if (newParameterOption == JOptionPane.OK_OPTION) {

				Boolean notNullValid = true;
				for (int labelIndex = 0; labelIndex < labels.length; labelIndex++) {
					if(labelIndex != labels.length-1)
					{
						values[labelIndex]= textBoxes[labelIndex].getText();

					}else{
						values[labelIndex] = (String) units.getEditor().getItem();

					}

				}

				if(!values[0].trim().equals("") && !values[10].trim().equals("")&&!values[11].trim().equals(""))
				{
					DefaultListModel listModel = (DefaultListModel)list.getModel();
					List existing = new ArrayList<String>();
					for(int i=1; i < listModel.getSize(); i++){
						Object o =  listModel.getElementAt(i);  
						existing.add(o.toString());
					}	
					if(!existing.contains(values[0]))
					{
						listModel.addElement(values[0]);
						List<String> newParaInfo = new ArrayList<String>();	
						for(String value : values){
							newParaInfo.add(value.trim().equals("") ? null : value.trim());
						}
						newParaInfo.add("0");//id									
						int newid = MeasurementParameterDAO.getInstance().insert(newParaInfo);
						newParaInfo.set(12, String.valueOf(newid));
						getParameterInfoMap().put(values[0],newParaInfo);
						invalid = false;
						list.clearSelection();
					}else{
						JOptionPane.showConfirmDialog(addNewParemeterPanel,
								"Duplicate Parameter Infomation", "",
								JOptionPane.DEFAULT_OPTION,
								JOptionPane.PLAIN_MESSAGE);
					}
				}else{
					JOptionPane.showConfirmDialog(
							addNewParemeterPanel,
							"<HTML><FONT COLOR = Red>*</FONT> marked fields are mandatory.</HTML>",
							"Error!", JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE);
				}					
			}
		}		
	}


	private void updateParameterPanel(JList list){
		String labelNames[] = { "<HTML>Parameter Name<FONT COLOR = Red>*</FONT></HTML>", "Parameter Code",ColumnConstants.TYPES,"ToAccession","Protocol", 
				"Format", "DefaultValue","MinValue","MaxValue","Categories","<HTML>IsVisible<FONT COLOR = Red>*</FONT></HTML>","<HTML>Unit<FONT COLOR = Red>*</FONT></HTML>" };
		JLabel labels[] = new JLabel[labelNames.length];
		List<String> selectedParamterInfo = getParameterInfoMap().get(list.getSelectedValue());
		final JTextField textBoxes[] = new JTextField[labelNames.length-1];
		JComboBox<String> units = getUniTypeInfo();
		String values[] = new String[labelNames.length];
		for (int labelIndex = 0; labelIndex < labels.length; labelIndex++) {
			values[labelIndex] = selectedParamterInfo.get(labelIndex);
		}

		boolean invalid = true;
		boolean notcancel = true;
		while(invalid && notcancel){
			JPanel addNewParemeterPanel = new JPanel(new MigLayout("insets 0, gapx 0"));
			for (int labelIndex = 0; labelIndex < labels.length; labelIndex++) {
				labels[labelIndex] = new JLabel(labelNames[labelIndex]);
				addNewParemeterPanel.add(labels[labelIndex],"gapleft 10, push");
				if(labelIndex!=(labels.length-1))
				{
					textBoxes[labelIndex] = new JTextField();
					textBoxes[labelIndex].setPreferredSize(new Dimension(200, 0));
					textBoxes[labelIndex].setText(values[labelIndex]);
					addNewParemeterPanel.add(textBoxes[labelIndex],"gapRight 10, wrap");
				}else{
					((JTextComponent) units.getEditor().getEditorComponent()).setText(values[labelIndex]==null?"":values[labelIndex]);
					addNewParemeterPanel.add(units,"gapRight 10, wrap");
				}								
			}
			int newParameterOption = JOptionPane.showConfirmDialog(
					null, addNewParemeterPanel,
					"Edit Parameter Information",
					JOptionPane.OK_CANCEL_OPTION);
			if (newParameterOption == JOptionPane.CANCEL_OPTION) {
				notcancel = false;
				list.clearSelection();
			}
			if (newParameterOption == JOptionPane.OK_OPTION) {
				for (int labelIndex = 0; labelIndex < labels.length; labelIndex++) {
					if(labelIndex != labels.length-1)
					{
						values[labelIndex]= textBoxes[labelIndex].getText();
					}else{
						values[labelIndex] = (String) units.getEditor().getItem();
					}
				}

				if(!values[0].trim().equals(""))
				{
					DefaultListModel listModel = (DefaultListModel)list.getModel();
					List existing = new ArrayList<String>();
					for(int i=1; i < listModel.getSize(); i++){
						Object o =  listModel.getElementAt(i);  
						existing.add(o.toString());
					}
					if(!existing.contains(values[0]) || 
							values[0].trim().equals(String.valueOf(list.getSelectedValue())))
					{
						List<String> editParaInfo = new ArrayList<String>();	
						for(String value : values){
							editParaInfo.add(value.trim().equals("")? null : value.trim());
						}
						editParaInfo.add(selectedParamterInfo.get(12));//id
						getParameterInfoMap().put((String) list.getSelectedValue(),editParaInfo);
						if(selectedParamterInfo.get(12).equals("0"))
						{
							MeasurementParameterDAO.getInstance().insert(editParaInfo);
						}else{
							MeasurementParameterDAO.getInstance().update(editParaInfo);
						}

						invalid = false;
						list.clearSelection();

					}else{
						JOptionPane.showConfirmDialog(addNewParemeterPanel,
								"Duplicate Parameter Infomation", "",
								JOptionPane.DEFAULT_OPTION,
								JOptionPane.PLAIN_MESSAGE);
					}					
				}						
			}
		}
	}

	private class dragAndDropMouseAdaptor extends MouseInputAdapter {
		private int pressIndex = 0;
		private int releaseIndex = 0;
		private JList myList;

		dragAndDropMouseAdaptor(JList list){
			this.myList = list;
		}

		@Override
		public void mousePressed(MouseEvent e) {
			pressIndex = myList.locationToIndex(e.getPoint());
		}

		@Override
		public void mouseReleased(MouseEvent e) {
			releaseIndex = myList.locationToIndex(e.getPoint());
			if (releaseIndex != pressIndex && releaseIndex != -1) {
				reorder();
			}
		}

		@Override
		public void mouseDragged(MouseEvent e) {
			mouseReleased(e);
			pressIndex = releaseIndex;      
		}

		private void reorder() {
			DefaultListModel model = (DefaultListModel) myList.getModel();
			Object dragee = model.elementAt(pressIndex);
			model.removeElementAt(pressIndex);
			model.insertElementAt(dragee, releaseIndex);
		}
	}

	public void initializeSubsetComboBox(){
		final JComboBox subset = getPhenotypeTagsTablePanel().getTableSubset();
		subset.addItem("All");	
		subset.setPrototypeDisplayValue("              ");
		setCurrentSubsetName("All");
		subset.addItemListener(new ItemListener(){
			public void itemStateChanged(ItemEvent e) {
				if(e.getStateChange() == ItemEvent.SELECTED)
				{
					String selected = subset.getSelectedItem().toString();
					if (selected.equals("All")) {
						populateTable();
					}else{
						populateSubset(selected);
					}					
					setCurrentSubsetName(selected);
					if(getSubsetCommentMap().get(selected) == null || getSubsetCommentMap().get(selected).equals("")){
						getCommentField().setPlaceholder(selected);
					}else{
						getCommentField().setPlaceholder(selected);
						getCommentField().setText(getSubsetCommentMap().get(selected));
						getCommentField().setForeground(Color.black);
					}
					List<List<String>> subsetJlist = getSubsetJlistMap().get(selected);
					DefaultListModel deslistModel = (DefaultListModel)getDescriptorsSelected().getModel();
					DefaultListModel paralistModel = (DefaultListModel)getParameterSelected().getModel();
					deslistModel.removeAllElements();
					paralistModel.removeAllElements();

					if(subsetJlist != null){						
						List<String> desJlist = subsetJlist.get(0);
						List<String> paraJlist = subsetJlist.get(1);					  					    
						for(String str :desJlist){
							deslistModel.addElement(str);
						}

						for(String str : paraJlist){
							paralistModel.addElement(str);
						}	
					}

				}
			}
		});
	}


	public void updateNumofItems(){
		getPhenotypeTagsTablePanel().getNumberOfRows().setText(String.valueOf(getPhenotypeTagsTablePanel().getTable().getRowCount()));
	}

	public void populateTable(){
		if(getTableData() != null){
			CheckBoxIndexColumnTable table = getPhenotypeTagsTablePanel().getTable();
			DefaultTableModel model = (DefaultTableModel) table.getModel();
			Utils.removeAllRowsFromTable((DefaultTableModel)model);
			int rows = getTableData().size();
			int cols = ((Vector)getTableData().elementAt(0)).size();
			for(int row = 0; row < rows; row ++){
				Object[] newRow = new Object[cols -1];
				for(int col = 0 ; col < (cols -1 ); ++col){
					if(col < 29){//remove "modified"
						newRow[col] = ((Vector)getTableData().elementAt(row)).elementAt(col);						
					}else{
						newRow[col] = ((Vector)getTableData().elementAt(row)).elementAt(col+1);	
					}
				}
				model.addRow(newRow);
			}
			table.setModel(model);
			updateNumofItems();
		}
	}

	public void populateSubset(String subsetName){
		Object[][] subsetData =	(Object[][]) getSubsetTableMap().get(subsetName);
		CheckBoxIndexColumnTable table = getPhenotypeTagsTablePanel().getTable();
		DefaultTableModel model = (DefaultTableModel) table.getModel();
		Utils.removeAllRowsFromTable((DefaultTableModel)model);
		int rows = subsetData.length;
		int cols = subsetData[0].length;
		for(int row = 0; row < rows; row ++){
			Object[] newRow = new Object[cols];
			for(int col = 0 ; col < cols; ++col){
				newRow[col] = subsetData[row][col];
			}
			model.addRow(newRow);
		}
		table.setModel(model);	
		updateNumofItems();
	}

	public JButton createSubsetButton(){
		JButton createSubset = new JButton("Create Subset");	
		createSubset.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				JComboBox subset = getPhenotypeTagsTablePanel().getTableSubset();
				String str=null;

				CheckBoxIndexColumnTable table = getPhenotypeTagsTablePanel().getTable();
				if(table.getSelectedRowCount() > 0)
				{
					do {
						str = JOptionPane.showInputDialog(null, "Enter Subset Name:", "Create New Subset", 1);
						if (str != null) {            		
							DefaultComboBoxModel model = (DefaultComboBoxModel) subset.getModel();
							if (model.getIndexOf(str) == -1) {
								subset.addItem(str);

							} else {
								str = null;
								JOptionPane.showConfirmDialog(null,
										"Duplicate Subset Name", "",
										JOptionPane.DEFAULT_OPTION,
										JOptionPane.PLAIN_MESSAGE);
							}
						}else {
							str = "";
						}
					}while (str == null);
					Object subsetData[][] = new Object[table.getSelectedRowCount()][table.getColumnCount()]; 
					for(int row = 0; row < table.getSelectedRowCount();++row){
						subsetData[row][0] = new Boolean(false);
						for(int col = 1; col < table.getColumnCount();++col){
							subsetData[row][col] = table.getValueAt(table.getSelectedRows()[row], col);
						}						
					}
					getSubsetTableMap().put(str, subsetData);
					subset.setSelectedItem(str);
					sampleSelectionPanel.getSubsetTableMap().put(str, subsetData);
					if(str != "All")
					{
						sampleSelectionPanel.getSampleSelectionTablePanel().getTableSubset().addItem(str);
					}
					ChangeMonitor.markAsChanged(projectID);
				}else{
					JOptionPane.showConfirmDialog(null,
							"Please select subset from the table", "",
							JOptionPane.DEFAULT_OPTION,
							JOptionPane.PLAIN_MESSAGE);
				}
			}

		});


		return createSubset;

	}

	private void exportButtonListener(){
		getExportButton().addActionListener(new ActionListener(){

			public void actionPerformed(ActionEvent e) {			 
				if(getPhenotypeTagsTablePanel().getTable().getRowCount() > 0)
				{
					new Export().execute();		
				}else{
					JOptionPane.showConfirmDialog(null,
							"Selected subset is empty !", "",
							JOptionPane.DEFAULT_OPTION,
							JOptionPane.PLAIN_MESSAGE);
				}	
			}

		});
	}
    
	private class Export extends SwingWorker<Void, Void> {

		@Override
		protected Void doInBackground() throws Exception {
			getPreviewPanel();
			return null;
		}
		
	}


	private void getPreviewPanel(){		
		List<String> columnames = new ArrayList<String>();	
		DefaultListModel descriptorlistModel = (DefaultListModel)getDescriptorsSelected().getModel();
		List<String> selectedDescriptors = new ArrayList<String>();
		for(int i=0; i < descriptorlistModel.getSize(); i++){
			Object o =  descriptorlistModel.getElementAt(i);  
			selectedDescriptors.add(o.toString());
		}

		for(String selected : selectedDescriptors){
			if(selected.equals(PhenotypeConstants.EXPERIMENT_INFO)){
				columnames.add("exp_name");
				columnames.add("exp_factor_name");
				columnames.add("exp_factor_type");
				columnames.add("exp_factor_value_level");
			}else{
				columnames.add(selected);
			}

		}
		List<ArrayList<Object>> tableData = new ArrayList<ArrayList<Object>>();
		LinkedHashMap<Integer, List<Object[]>> obsDataMap= new LinkedHashMap<Integer, List<Object[]>>();
		List<String> tempColumnames = new ArrayList<String>(columnames);	
		int numberOfFactor = 0;
		if(columnames.size()>0){					
			CheckBoxIndexColumnTable table = getPhenotypeTagsTablePanel().getTable();
			int tagIndex = table.getColumnModel().getColumnIndex(ColumnConstants.TAG_ID);
			progressBar.setVisible(true);
			for(int row = 0; row < table.getRowCount(); ++ row){
				int currentProgress = (row + 1) * 100 / table.getRowCount();
                progressBar.setValue(currentProgress);
				if(!Utils.isInteger(String.valueOf(table.getValueAt(row, tagIndex)))) {
					continue;// skip filler
				}
				int obsUnitId = (Integer)table.getValueAt(row, tagIndex);
				List<Object[]> results = ObservationUnitDAO.getInstance().getObsRelatedInfo(obsUnitId,tempColumnames);
				obsDataMap.put(obsUnitId, results);
				if(results.size() > numberOfFactor)
				{
					numberOfFactor = results.size();
				}
			}

			if(columnames.contains("exp_factor_name")){
				int indexOfExpFacValue = columnames.indexOf("exp_factor_value_level")+1;
				int firstFactorValue = indexOfExpFacValue;
				int firstFactorName  = columnames.indexOf("exp_factor_name");
				//final columns
				for(int num = 1; num < numberOfFactor; ++num){
					columnames.add(indexOfExpFacValue++,"exp_factor_name");					
					columnames.add(indexOfExpFacValue++,"exp_factor_type");				
					columnames.add(indexOfExpFacValue++,"exp_factor_value_level");				

				}

				ArrayList<Integer> keys = new ArrayList<Integer>(obsDataMap.keySet());
				for(Integer obs : keys){
					ArrayList<Object> rowdata = new ArrayList<Object>();
					rowdata.add(obs);
					for(int index = 0; index < tempColumnames.size(); ++index){
						if(index != firstFactorName){
							rowdata.add(obsDataMap.get(obs).get(0)[index]); 
						}else{
							for(int num = 0; num < obsDataMap.get(obs).size(); ++num){
								rowdata.add(obsDataMap.get(obs).get(num)[firstFactorName]);//exp_factor_name
								rowdata.add(obsDataMap.get(obs).get(num)[firstFactorName+1]);//exp_factor_type	
								rowdata.add(obsDataMap.get(obs).get(num)[firstFactorName+2]);//exp_factor_value_level
							}
							for(int num = 0; num < (numberOfFactor-obsDataMap.get(obs).size()); ++num){
								rowdata.add(null); //exp_factor_name
								rowdata.add(null); //exp_factor_type
								rowdata.add(null); //exp_factor_value_level
							}
							index = firstFactorValue-1;
						}
					}	
					tableData.add(rowdata);
				}	
			}else{
				ArrayList<Integer> keys = new ArrayList<Integer>(obsDataMap.keySet());

				for(Integer obs : keys){
					ArrayList<Object> rowdata = new ArrayList<Object>();
					rowdata.add(obs);
					for(int index = 0; index < obsDataMap.get(obs).get(0).length; ++index){
						rowdata.add(obsDataMap.get(obs).get(0)[index]); 
					}
					tableData.add(rowdata);
				}				
			}
			progressBar.setVisible(false);
		}

		DefaultListModel parameterlistModel = (DefaultListModel)getParameterSelected().getModel();
		List<String> selectedParameters = new ArrayList<String>();
		for(int i=0; i < parameterlistModel.getSize(); i++){
			Object o =  parameterlistModel.getElementAt(i);  
			selectedParameters.add(o.toString());
		}

		for(String selected : selectedParameters){
			Object code = getParameterInfoMap().get(selected).get(1) == null 
					|| getParameterInfoMap().get(selected).get(1).trim().equals("") ? null : getParameterInfoMap().get(selected).get(1);
			if(codeAsCol.isSelected() && code != null){
				columnames.add((String)code);
				columnames.add("tom_"+(String)code);
			}else{
				columnames.add(selected);
				columnames.add("tom_"+selected);
			}

		}
		columnames.add(0, "observation_unit_id");		
		getPreviewPanel(columnames,tableData);


	}



	private void getPreviewPanel(List<String> columnnames, List<ArrayList<Object>> tableData){
		JPanel previewPanel = new JPanel(new MigLayout("insets 0, gap 5"));
		ClassLoader loader = PhenotypeExportPanel.class.getClassLoader();
		ImageIcon deleteIcon = new ImageIcon(loader.getResource("images/delete.png"));		
		ImageIcon deleteColorIcon = new ImageIcon(loader.getResource("images/deleteColor.png"));
		ImageButton deleteButton = new ImageButton(deleteIcon,deleteColorIcon);
		deleteButton.setToolTipText("Select a column to delete");

		String firstRow = ">>>> Subset Name: "+ getCurrentSubsetName() + ". Subset Comment: "+getCommentField().getText()+" .";
		previewPanel.add(new JLabel(firstRow));		
		previewPanel.add(deleteButton, "h 24:24:24, w 24:24:24, push, al right, wrap");

		int numRows = 0 ;
		String[] colIndentifiers = new String[columnnames.size()];
		colIndentifiers = columnnames.toArray(colIndentifiers);		
		final MyTableModel model = new MyTableModel(numRows, colIndentifiers.length) ;
		model.setColumnIdentifiers(colIndentifiers);
		final JTable table= new JTable(model);
		table.setShowGrid(true);
		table.setGridColor(Color.LIGHT_GRAY);
		table.setEnabled(false);
		JScrollPane tableScrollPane = new JScrollPane(table, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
		table.getParent().addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(final ComponentEvent e) {
				if (table.getPreferredSize().width < table.getParent().getWidth()) {
					table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
				} else {
					table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
					for (int i = 0; i < table.getColumnCount(); i++) {
						adjustColumnSizes(table, i, 2);
					}
				}
			}
		});
		//hide rows when row index >5
		RowFilter<Object, Object> filter = new RowFilter<Object, Object>() {
			public boolean include(Entry entry) {
				return (Integer)entry.getIdentifier() < 10;
			}
		};
		TableRowSorter<MyTableModel> sorter = new TableRowSorter<MyTableModel>(model);
		sorter.setRowFilter(filter);
		table.setRowSorter(sorter);

		final JTableHeader header = table.getTableHeader();  
		table.setCellEditor(new CustomCellEditor());
		header.setReorderingAllowed(false);  
		header.addMouseListener(new MouseAdapter() {  
			public void mouseClicked(MouseEvent e) {  
				int col = header.columnAtPoint(e.getPoint());   
				if(header.getCursor().getType() == Cursor.E_RESIZE_CURSOR)  
					e.consume();  
				else {  
					table.setColumnSelectionAllowed(true);
					table.setRowSelectionAllowed(false);
					table.clearSelection();
					table.setColumnSelectionInterval(col,col);
				}  
			}  
		});  

		for(ArrayList<Object> rowdata : tableData){
			Object[] row = new Object[rowdata.size()];
			row = rowdata.toArray(row);
			model.addRow(row);
		}

		deleteButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				int cols[] = table.getSelectedColumns();
				for(int col : cols){
					if(col != 0)
					{
						model.removeColumn(col);
					}
				}
			}

		});
		previewPanel.setPreferredSize(new Dimension(600,500));
		previewPanel.add(tableScrollPane,"span, w 100%, h 100%");
		int option = JOptionPane.showConfirmDialog(
				null, previewPanel,"Preview of first 10 rows ",
				JOptionPane.OK_CANCEL_OPTION,
				JOptionPane.PLAIN_MESSAGE);	
		if(option ==  JOptionPane.OK_OPTION){
			saveTableToFile(table, null, "Phenotyping.csv",firstRow);
		}					

	}


	public class MyTableModel extends DefaultTableModel {

		MyTableModel(int numRows, int length){
			super();
		};
		public void removeColumn(int column) {
			columnIdentifiers.remove(column);
			for (Object row: dataVector) {
				((Vector) row).remove(column);
			}
			fireTableStructureChanged();
		}
	}



	public class CustomCellEditor extends DefaultCellEditor {

		public CustomCellEditor() {
			super(new JTextField());
			// TODO Auto-generated constructor stub
		}

		public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
			table.clearSelection();
			table.setColumnSelectionAllowed(true);
			table.setRowSelectionAllowed(false);
			return super.getTableCellEditorComponent(table, value, isSelected, row, column);
		}

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
			comp = renderer.getTableCellRendererComponent(table, table.getValueAt(r, column), false, false, r, column);
			int currentWidth = comp.getPreferredSize().width;
			width = Math.max(width, currentWidth);
		}

		width += 2 * margin;

		col.setPreferredWidth(width);
		col.setWidth(width);
	}



	private void saveTableToFile(JTable table, JPanel panel, String fileName, String firstRowComment) {
		try {
			if (table == null || fileName == null) {
				return;
			}
			table.setRowSorter(null);
			JFileChooser fileChooser = new JFileChooser();
			File file = new File(System.getProperty("java.io.tmpdir") + "/" + fileName);
			fileChooser.setSelectedFile(file);
			int approve = fileChooser.showSaveDialog(panel);
			if (approve != JFileChooser.APPROVE_OPTION) {
				return;
			}
			file = fileChooser.getSelectedFile();
			BufferedWriter writer = new BufferedWriter(new FileWriter(file));
			writer.write(firstRowComment);
			writer.newLine();
			//write the column names first
			for (int columnCounter = 0; columnCounter < table.getColumnCount(); columnCounter++) {
				writer.write(table.getColumnName(columnCounter));
				writer.write(",");
			}

			for (int rowCounter = 0; rowCounter < table.getRowCount(); rowCounter++) {
				writer.newLine();
				for (int columnCounter = 0; columnCounter < table.getColumnCount(); columnCounter++) {
					String value = "";
					if (table.getValueAt(rowCounter, columnCounter) instanceof java.sql.Timestamp) {
						java.sql.Timestamp timeStamp = (java.sql.Timestamp) table.getValueAt(rowCounter, columnCounter);
						value = (timeStamp.getYear() + 1900) + "-" + (timeStamp.getMonth() + 1) + "-"
								+ timeStamp.getDate();
					} else {
						value = String.valueOf(table.getValueAt(rowCounter, columnCounter)).equals("null")?"":String.valueOf(table.getValueAt(rowCounter, columnCounter));
					}
					writer.write(value);
					writer.write(",");
				}
			}
			writer.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}


	public void plantingResync(){
		setSubsetTableMap(new LinkedHashMap<String, Object[][]>());
		setSubsetCommentMap( new LinkedHashMap<String, String>() );
		for(int index = 1; index<getPhenotypeTagsTablePanel().getTableSubset().getItemCount();++index)
		{
			getPhenotypeTagsTablePanel().getTableSubset().removeItemAt(index);
		}

	}

	public void populateTableFromObjects(List<Object[]> exportTableRows) {
		DefaultTableModel tableModel = ((DefaultTableModel) getPhenotypeTagsTablePanel().getTable().getModel());
		removeAllRowsFromTable(tableModel);	
		for(Object[] row : exportTableRows){
			tableModel.addRow(row);               
		}
		updateNumofItems();
		Vector data = ((DefaultTableModel) getPhenotypeTagsTablePanel().getTable().getModel()).getDataVector();
		setTableData(data);	
	}




	public JButton getSearchTagsButton() {
		return searchTagsButton;
	}

	public void setSearchTagsButton(JButton searchTagsButton) {
		this.searchTagsButton = searchTagsButton;
	}

	public JList getDescriptorsOptions() {
		return descriptorsOptions;
	}


	public void setDescriptorsOptions(JList descriptorsOptions) {
		this.descriptorsOptions = descriptorsOptions;
	}


	public JList getDescriptorsSelected() {
		return descriptorsSelected;
	}


	public void setDescriptorsSelected(JList descriptorsSelected) {
		this.descriptorsSelected = descriptorsSelected;
	}


	public JList getParameterOptions() {
		return parameterOptions;
	}


	public void setParameterOptions(JList parameterOptions) {
		this.parameterOptions = parameterOptions;
	}


	public JList getParameterSelected() {
		return parameterSelected;
	}


	public void setParameterSelected(JList parameterSelected) {
		this.parameterSelected = parameterSelected;
	}

	public JButton getExportButton() {
		return exportButton;
	}

	public void setExportButton(JButton exportButton) {
		this.exportButton = exportButton;
	}

	public TableToolBoxPanel getPhenotypeTagsTablePanel() {
		return phenotypeTagsTablePanel;
	}

	public void setPhenotypeTagsTablePanel(TableToolBoxPanel phenotypeTagsTablePanel) {
		this.phenotypeTagsTablePanel = phenotypeTagsTablePanel;
	}

	public Vector getTableData() {
		return tableData;
	}

	public void setTableData(Vector tableData) {
		this.tableData = tableData;
	}

	public LinkedHashMap<String, Object[][]> getSubsetTableMap() {
		return subsetTableMap;
	}

	public void setSubsetTableMap(LinkedHashMap<String, Object[][]> subsetTableMap) {
		this.subsetTableMap = subsetTableMap;
	}

	public LinkedHashMap<String, String> getSubsetCommentMap() {
		return subsetCommentMap;
	}

	public void setSubsetCommentMap(LinkedHashMap<String, String> subsetCommentMap) {
		this.subsetCommentMap = subsetCommentMap;
	}
	public TextField getCommentField() {
		return commentField;
	}

	public void setCommentField(TextField commentField) {
		this.commentField = commentField;
	}

	public String getCurrentSubsetName() {
		return currentSubsetName;
	}

	public void setCurrentSubsetName(String currentSubsetName) {
		this.currentSubsetName = currentSubsetName;
	}
	public LinkedHashMap<String, List<String>> getParameterInfoMap() {
		return parameterInfoMap;
	}

	public void setParameterInfoMap(LinkedHashMap<String, List<String>> parameterInfoMap) {
		this.parameterInfoMap = parameterInfoMap;
	}
	public JComboBox<String> getUniTypeInfo() {
		return uniTypeInfo;
	}

	public void setUniTypeInfo(JComboBox<String> uniTypeInfo) {
		this.uniTypeInfo = uniTypeInfo;
	}

	public Boolean getIsFirstSync() {
		return isFirstSync;
	}

	public void setIsFirstSync(Boolean isFirstSync) {
		this.isFirstSync = isFirstSync;
	}

	public LinkedHashMap<String, List<List<String>>> getSubsetJlistMap() {
		return subsetJlistMap;
	}

	public void setSubsetJlistMap(
			LinkedHashMap<String, List<List<String>>> subsetJlistMap) {
		this.subsetJlistMap = subsetJlistMap;
	}
	public SampleSelectionPanel getSampleSelectionPanel() {
		return sampleSelectionPanel;
	}

	public void setSampleSelectionPanel(SampleSelectionPanel sampleSelectionPanel) {
		this.sampleSelectionPanel = sampleSelectionPanel;
	}

	public int getProjectID() {
		return projectID;
	}

	public void setProjectID(int projectID) {
		this.projectID = projectID;
	}




}
