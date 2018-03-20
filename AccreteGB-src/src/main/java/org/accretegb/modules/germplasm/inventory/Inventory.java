package org.accretegb.modules.germplasm.inventory;

import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import net.miginfocom.swing.MigLayout;

import org.accretegb.modules.germplasm.planting.TableView;
import org.accretegb.modules.tab.TabComponentPanel;

/**
 * @author tnj
 * This class is use to initialize tab panes, including storage units pane and 
 * packets inventory pane 
 */
public class Inventory extends TabComponentPanel {

	private static final long serialVersionUID = 1L;
	private PacketsInventoryPanel packetsInventoryPanel;
	private ContainerLocationsPanel containerLocationsPanel;
		
	public void initialize() {
		setLayout(new MigLayout("insets 10, gap 10"));
		
		final JTabbedPane tabbedPane = new JTabbedPane();
		tabbedPane.add(containerLocationsPanel);
		tabbedPane.setTitleAt(0, "Storage Units");
		
		tabbedPane.add(packetsInventoryPanel);
		tabbedPane.setTitleAt(1, "Packets Inventory");
		
		// when click tabs, refresh current inventory address info in packets inventory
		tabbedPane.addChangeListener(new ChangeListener(){
			public void stateChanged(ChangeEvent e) {
				if(containerLocationsPanel.getArrangeShelvesTablePanel().getTable().getRowCount() > 0){
					packetsInventoryPanel.populateCurrentInventoryAddress();
				}
				else{
					packetsInventoryPanel.getCurrentInventoryAddress().removeAllItems();
					packetsInventoryPanel.populateCurrentInventoryAddress();
				}
				packetsInventoryPanel.populateShelves();
				packetsInventoryPanel.getStockInventoryTablePanel().getTable().revalidate();
				packetsInventoryPanel.getStockInventoryTablePanel().getTable().repaint();
			}			
		});		
		add(tabbedPane, "w 100%, h 100%");
	}
	
	public PacketsInventoryPanel getPacketsInventoryPanel() {
		return packetsInventoryPanel;
	}

	public void setPacketsInventoryPanel(
			PacketsInventoryPanel packetsInventoryPanel) {
		this.packetsInventoryPanel = packetsInventoryPanel;
	}
	
	public ContainerLocationsPanel getcontainerLocationsPanel() {
		return containerLocationsPanel;
	}

	public void setcontainerLocationsPanel(
			ContainerLocationsPanel containerLocationsPanel) {
		this.containerLocationsPanel = containerLocationsPanel;
	}

}
