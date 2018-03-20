package org.accretegb.modules.germplasm.planting;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.TreeSet;

import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.TransferHandler;
import javax.swing.table.DefaultTableModel;

import org.accretegb.modules.constants.ColumnConstants;
import org.accretegb.modules.customswingcomponent.PlotIndexColumnTable;


public class TableTransferHandler extends TransferHandler implements Transferable{
	
	  private TreeSet<Integer> selectedRows;
	  private int insertIndex = -1;
	  private static final DataFlavor flavors[] = { new DataFlavor(ArrayList.class, "Table Rows")};
	  private ArrayList<Object[]> tableRows = new ArrayList<Object[]>();
	  private boolean fromDesign = false;
	  private JPanel panel;
	  
	  public TableTransferHandler(JPanel panel){
		  if(panel instanceof TableView){
			  this.panel = panel;
		  }else{
			  this.panel=null;
		  }
	  }
	  
	  public DataFlavor[] getTransferDataFlavors() {
			// TODO Auto-generated method stub
			return flavors;
	  }

	  public boolean isDataFlavorSupported(DataFlavor flavor) {
			// TODO Auto-generated method stub
			return false;
	  }

	  public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
			// TODO Auto-generated method stub
			return tableRows;
	  }
	  public boolean canImport(JComponent comp, DataFlavor[] flavor) {
		  if (!(comp instanceof JTable)) {
		      return false;
		    }

		    return true;
	  }
	  public int getSourceActions(JComponent c) {
		    return TransferHandler.MOVE;
	  }
	  protected Transferable createTransferable(JComponent c) {
		  if(c instanceof PlotIndexColumnTable)
		  {
			  PlotIndexColumnTable table = (PlotIndexColumnTable) c;
			  int columnCount = table.getColumnCount();			   
			  TreeSet<Integer> selectedRowsWithPlants = new TreeSet<Integer>();
			  Object currentRep = table.getValueAt(table.getSelectedRows()[0], table.getIndexOf(ColumnConstants.REP));
			  if(currentRep != null){
				  fromDesign = true;
			  }			  
			  
			  for(int row :table.getSelectedRows()){
				  String type = (String) table.getValueAt(row, table.getIndexOf(ColumnConstants.TYPES));
				  if(type.equals("Row")){
					  int count = (Integer) table.getValueAt(row, table.getIndexOf(ColumnConstants.COUNT));
					  selectedRowsWithPlants.add(row);
					  if(count > 0){
						  for(int i = 1; i <= count ; ++i)
						  {
							  selectedRowsWithPlants.add(row+i);
						  }
					  }  
				  }else{
					  JOptionPane.showMessageDialog(null, "Select row with type \"Row\" to move.");
					  return null;
				  }				  
			  }
			  selectedRows = selectedRowsWithPlants;
			  for(int row : selectedRows){
				  Object[] selectedRow = new Object[columnCount];
				  for(int col=0; col < columnCount ; ++col){
					  selectedRow[col] = table.getValueAt(row, col);	
				  }
				  tableRows.add(selectedRow);				  
			  }			 
			  return this;
		  }
		  return null;
	  }	  
	  
	  public boolean importData( TransferHandler.TransferSupport info) {
		    JComponent c = (JComponent) info.getComponent();
		    Transferable t = info.getTransferable();
		    if(c instanceof JTable){
		        final PlotIndexColumnTable table = (PlotIndexColumnTable) c;
		    	try {		    		
					ArrayList<Object[]> draggedRows = (ArrayList<Object[]>) t.getTransferData(flavors[0]);
					JTable.DropLocation dl = (JTable.DropLocation)info.getDropLocation();
					
			    	int rowIndex = dl.getRow();	
			    	insertIndex = rowIndex;
			    	if(insertIndex < 0){
			    		System.out.println("invalid location");
			    		draggedRows = null;
			    		return false;
			    	}			    			    	
			    	//dropindex is in the middle of selected rows
			    	if (draggedRows != null && insertIndex >= selectedRows.first()
			    			&& insertIndex <= selectedRows.last()) {
			    		draggedRows = null;
			    		Thread thread;
			    		try{
			    			thread = new Thread() {
				    		    public void run() {
				    		    	String firstRow = (String) table.getValueAt(selectedRows.first(), table.getIndexOf(ColumnConstants.ROW));
				    		    	String lastRow = (String) table.getValueAt(selectedRows.last(), table.getIndexOf(ColumnConstants.ROW));
				    		    	if(firstRow.equals(lastRow)){
				    		    		JOptionPane.showMessageDialog(null, "Invalid movement");
				    		    	}else{
				    		    		JOptionPane.showMessageDialog(null, "All the selected rows can only be moved to same direction");	
				    		    	}				    		    	
				    		    }  
				    		};
				    		thread.start();
			    		}catch(Exception e){
			    			
			    		}
			    	    return false;
			    	}
			    	int above = insertIndex-1;
	    			int below = insertIndex;
			    	if(fromDesign){
			    		// allow to insert at the beginning and end
			    		if(insertIndex != 0 && insertIndex != table.getRowCount()-1 ){			    			
			    			Object aboveRep = table.getValueAt(above, table.getIndexOf(ColumnConstants.REP));
			    			Object belowRep = table.getValueAt(below, table.getIndexOf(ColumnConstants.REP));
			    			if(aboveRep != null  &&  belowRep !=null && 
			    					String.valueOf(aboveRep).equals(String.valueOf(belowRep))){
			    				//drop at the middle of a block
			    				draggedRows = null;
					    		try{
					    			Thread thread = new Thread() {
						    		    public void run() {
						    		    	JOptionPane.showMessageDialog(null, "Rows with different rep values can not be nested");
						    		    }  
						    		};
						    		thread.start();
					    		}catch(Exception e){
					    			
					    		}
			    				
			    				return false;
			    			}			    			
			    		}
			    	}
			    	//not allow to drop at the middle of plants
			    	if(insertIndex != 0 && insertIndex != table.getRowCount()-1 ){		
			    		Object aboveRownum = table.getValueAt(above, table.getIndexOf(ColumnConstants.ROW));
		    			Object belowRownum = table.getValueAt(below, table.getIndexOf(ColumnConstants.ROW));
		    			if(String.valueOf(aboveRownum).equals(String.valueOf(belowRownum))){
		    				draggedRows = null;
		    				try{
				    			Thread thread = new Thread() {
					    		    public void run() {
					    		    	JOptionPane.showMessageDialog(null, "Rows and their plants can not be saperated");
					    		    }  
					    		};
					    		thread.start();
				    		}catch(Exception e){
				    			
				    		}		    				
		    				return false;
		    			}
			    	}
			    	
	    			DefaultTableModel model = (DefaultTableModel) table.getModel();
			    	for(Object[] draggedRow : draggedRows){
			    		int convertedIndex = table.convertRowIndexToModel(rowIndex);
			    		model.insertRow(convertedIndex,draggedRow);
			    		String type = (String) draggedRow[table.getIndexOf(ColumnConstants.TYPES)];
			    		 if(type.equals("Row")){
			    			 table.addRowSelectionInterval(rowIndex, rowIndex);  
			    		 }
			    		rowIndex++;
			    	}
			    	
			    	return true;
			    	
				} catch (UnsupportedFlavorException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					System.out.println(e.getMessage());
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					System.out.println(e.getMessage());
				}
		    }
		    return false;
	  }

	  protected void exportDone(JComponent c, Transferable data, int action) {
	    cleanup(c, action==MOVE);
	  }

	  protected void cleanup(JComponent c, boolean remove) {
	    if (remove && selectedRows != null) {
	    	DefaultTableModel model = (DefaultTableModel) ((PlotIndexColumnTable) c).getModel();
	    	int index = -1;
	    	int count = selectedRows.size();
	    	//rows were move up in model 
	    	if(((PlotIndexColumnTable) c).convertRowIndexToModel(insertIndex) < ((PlotIndexColumnTable) c).convertRowIndexToModel(selectedRows.first())){
	    		index = 0;
	    		for(int deleteRow : selectedRows){
	    			deleteRow = ((PlotIndexColumnTable) c).convertRowIndexToModel(deleteRow + count - index);
		    		model.removeRow(deleteRow);
		    		index++;
		    	}	
	    	}else{
	    		index = 0;
	    		for(int deleteRow : selectedRows){
	    			deleteRow = ((PlotIndexColumnTable) c).convertRowIndexToModel(deleteRow -index);
	    			model.removeRow(deleteRow);
		    		index++;
		    	}
	    	}
	    	
	    }
	    tableRows.clear();
	    insertIndex = -1;
	    if(panel instanceof TableView){
	    	((TableView) panel).updateWhenRowCountChanged();
	    	((TableView) panel).setTableChanged(true);
	    }
	  }
	}