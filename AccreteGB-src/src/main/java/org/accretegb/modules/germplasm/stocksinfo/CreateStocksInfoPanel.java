package org.accretegb.modules.germplasm.stocksinfo;

import static org.accretegb.modules.config.AccreteGBBeanFactory.getContext;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JTextField;

import org.accretegb.modules.constants.ColumnConstants;
import org.accretegb.modules.constants.TableColumnList;
import org.accretegb.modules.customswingcomponent.CheckBoxIndexColumnTable;
import org.accretegb.modules.customswingcomponent.TableToolBoxPanel;
import org.accretegb.modules.customswingcomponent.TextField;
import org.accretegb.modules.tab.TabComponentPanel;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.context.support.GenericXmlApplicationContext;

public class CreateStocksInfoPanel {
	
	public static StocksInfoPanel createStockInfoPanel(String str) {
		List<String> columnNames  = TableColumnList.STOCK_SELECTION_TABLE_COLUMN_LIST;
		List<String> showColumns = TableColumnList.STOCK_SELECTION_SHOW_COLUMN_LIST;
		List<String> horizontalList = new ArrayList<String>();
		horizontalList.add("selection");
		horizontalList.add("search");
		horizontalList.add("expand");
		horizontalList.add("columnSelector");
		horizontalList.add("counter");
		List<String> verticalList = new ArrayList<String>();
		verticalList.add("delete");
		verticalList.add("moveup");
		verticalList.add("movedown");
		List<String> horizontalListSaveTable = new ArrayList<String>();
        horizontalListSaveTable.add("selection");
        horizontalListSaveTable.add("checkDuplicates");
        horizontalListSaveTable.add("gap");
        horizontalListSaveTable.add("search");            
        horizontalListSaveTable.add("gap");
        horizontalListSaveTable.add("expand");
        horizontalListSaveTable.add("columnSelector");
        horizontalListSaveTable.add("counter");
        
		BeanDefinitionBuilder searchResultsTableStockInfoDefinitionBuilder = BeanDefinitionBuilder.genericBeanDefinition(CheckBoxIndexColumnTable.class)
				.addPropertyValue("columnNames", columnNames)
				.addPropertyValue("showColumns", showColumns)
				.addPropertyValue("checkBoxHeader", new Boolean(false))
				.setInitMethodName("initialize");
		((GenericXmlApplicationContext) getContext()).registerBeanDefinition("searchResultsTableStockInfo"+str, searchResultsTableStockInfoDefinitionBuilder.getBeanDefinition());

		BeanDefinitionBuilder searchResultsPanelDefinitionBuilder = BeanDefinitionBuilder.genericBeanDefinition(TableToolBoxPanel.class)
				.addPropertyValue("horizontalList", horizontalList)
				.addPropertyValue("table", getContext().getBean("searchResultsTableStockInfo"+str))
				.setInitMethodName("initialize");
		((GenericXmlApplicationContext) getContext()).registerBeanDefinition("searchResultsPanel"+str, searchResultsPanelDefinitionBuilder.getBeanDefinition());

		BeanDefinitionBuilder saveTableStockInfoDefinitionBuilder = BeanDefinitionBuilder.genericBeanDefinition(CheckBoxIndexColumnTable.class)
				.addPropertyValue("columnNames", columnNames)
				.addPropertyValue("showColumns", showColumns)
				.addPropertyValue("checkBoxHeader", new Boolean(false))
				.setInitMethodName("initialize");
		((GenericXmlApplicationContext) getContext()).registerBeanDefinition("saveTableStockInfo"+str, saveTableStockInfoDefinitionBuilder.getBeanDefinition());

		BeanDefinitionBuilder saveTableStockInfoPanelDefinitionBuilder = BeanDefinitionBuilder.genericBeanDefinition(TableToolBoxPanel.class)
				.addPropertyValue("verticalList", verticalList)
				.addPropertyValue("horizontalList", horizontalListSaveTable)
				.addPropertyValue("table", getContext().getBean("saveTableStockInfo"+str))
				.setInitMethodName("initialize");
		((GenericXmlApplicationContext) getContext()).registerBeanDefinition("saveTableStockInfoPanel"+str, saveTableStockInfoPanelDefinitionBuilder.getBeanDefinition());
		BeanDefinitionBuilder stocksInfoPanel0DefinitionBuilder = BeanDefinitionBuilder.genericBeanDefinition(TabComponentPanel.class)
				.addPropertyValue("panelIndex", new Integer(0))
				.setInitMethodName("initialize");

		((GenericXmlApplicationContext) getContext()).registerBeanDefinition("stocksInfoPanel0"+str, stocksInfoPanel0DefinitionBuilder.getBeanDefinition());

		BeanDefinitionBuilder stocksInfoDefinitionBuilder = BeanDefinitionBuilder.genericBeanDefinition(StocksInfoPanel.class)
				.addPropertyValue("stockname", new JTextField(23))
				.addPropertyValue("accession", new JTextField(23))
				.addPropertyValue("pedigree", new JTextField(23))
				.addPropertyValue("zipcode", new TextField(23))	
				.addPropertyValue("personName", new JTextField(23))
				.addPropertyValue("fromDate", new JTextField(23))
				.addPropertyValue("toDate", new JTextField(23))
				.addPropertyValue("generation", new JTextField(23))
				.addPropertyValue("cycle", new JTextField(23))
				.addPropertyValue("classificationCodeComboBox", new JComboBox())
				.addPropertyValue("selectedRows", new ArrayList<String>())
				.addPropertyValue("population", new JTextField(23))
				.addPropertyValue("buttonClear", new JButton("Clear Input"))
				.addPropertyValue("buttonSubmit", new JButton("Search"))
				.addPropertyValue("multiplier", new JTextField(3))
				.addPropertyValue("buttonSelect", new JButton("Add to Cart"))
				.addPropertyValue("buttonSave", new JButton("Save"))
				.addPropertyValue("searchResultsPanel",getContext().getBean("searchResultsPanel"+str))
				.addPropertyValue("saveTablePanel",getContext().getBean("saveTableStockInfoPanel"+str))
				.addPropertyValue("popup", true)
				.setParentName("stocksInfoPanel0"+str)
				.setInitMethodName("initialize");
		((GenericXmlApplicationContext) getContext()).registerBeanDefinition("Stock Selection - "+str, stocksInfoDefinitionBuilder.getBeanDefinition());

		StocksInfoPanel stockInfoPanel = (StocksInfoPanel) getContext().getBean("Stock Selection - "+str);
		return stockInfoPanel;
		
	}

}
