package org.accretegb.modules.germplasm.outsideseed;

import net.miginfocom.swing.MigLayout;

import org.accretegb.modules.MainLayout;
import org.accretegb.modules.config.AccreteGBBeanFactory;
import org.accretegb.modules.customswingcomponent.CheckBoxIndexColumnTable;
import org.accretegb.modules.customswingcomponent.Utils;
import org.accretegb.modules.hibernate.*;
import org.accretegb.modules.hibernate.dao.ClassificationDAO;
import org.accretegb.modules.hibernate.dao.PassportDAO;
import org.accretegb.modules.hibernate.dao.StockDAO;
import org.accretegb.modules.hibernate.dao.StockGenerationDAO;
import org.accretegb.modules.hibernate.dao.TaxonomyDAO;
import org.accretegb.modules.tab.TabComponent;
import org.accretegb.modules.tab.TabComponentPanel;
import org.accretegb.modules.tab.TabManager;
import org.accretegb.modules.util.LoggerUtils;
import org.apache.commons.lang.StringUtils;
import org.hibernate.*;
import org.hibernate.criterion.Restrictions;
import org.accretegb.modules.constants.ColumnConstants;
import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;

import static org.accretegb.modules.config.AccreteGBBeanFactory.getContext;
import static org.accretegb.modules.customswingcomponent.Utils.setTableSize;

public class StockVerificationPanel extends TabComponentPanel {

    private static final long serialVersionUID = 1L;

    private CheckBoxIndexColumnTable stockVerificationTable;
    private Source source;
    private StringBuilder errorBlock;
    private int successful;

    private JProgressBar progressBar;

    private Calendar calendar;

    public CheckBoxIndexColumnTable getStockVerificationTable() {
        return stockVerificationTable;
    }

    public void setStockVerificationTable(CheckBoxIndexColumnTable stockVerificationTable) {
        this.stockVerificationTable = stockVerificationTable;
    }

    public Source getSource() {
        return source;
    }

    public void setSource(Source source) {
        this.source = source;
    }

    public StringBuilder getErrorBlock() {
        return errorBlock;
    }

    public void setErrorBlock(StringBuilder errorBlock) {
        this.errorBlock = errorBlock;
    }

    public void initialize() {
        setLayout(new MigLayout("insets 20, gapy 10"));
        add(getPrevPanelButton(), "pushx");
        add(getNextPanelButton(), "wrap");
        add(getTablePanel(), "h 100%, grow, span, wrap");
        progressBar = new JProgressBar(0, 100);
        progressBar.setValue(0);
        progressBar.setVisible(false);
        add(progressBar, "grow, span, push, hidemode 0");
        errorBlock = new StringBuilder();
        addNavigationButtonListeners();
    }

    private void addNavigationButtonListeners() {
        getNextPanelButton().setText("Submit");
        this.getNextPanelButton().addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                int result = JOptionPane.showConfirmDialog(StockVerificationPanel.this,
                        "Are you sure you want to submit to Database?\nOnce you do, you cannot make any changes.",
                        "Confirm data before submit", JOptionPane.YES_NO_OPTION);

                if (result == JOptionPane.YES_OPTION)
                    updateDatabase();
            }
        });
        getPrevPanelButton().addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                prevButtonActionPerformed();
            }
        });
    }

    protected void updateDatabase() {

        progressBar.setStringPainted(true);
        progressBar.setVisible(true);
        ((JPanel) progressBar.getParent()).revalidate();
        progressBar.getParent().repaint();
        final Thread inserts = new Thread(new Runnable() {

            public void run() {

                int totalRows = stockVerificationTable.getRowCount();
                for (int rowCounter = 0; rowCounter < totalRows; rowCounter++) {
                    progressBar.setValue((rowCounter - 1) * 100 / totalRows);

                    try {
                        Classification classification = null;
                        Taxonomy taxonomy = null;
                        StockGeneration generation = null;
                        Passport passport = null;

                        String generationValue = String.valueOf( stockVerificationTable.getValueAt(rowCounter,
                                stockVerificationTable.getColumn(ColumnConstants.GENERATION).getModelIndex())).trim();
                        String cycleValue = String.valueOf( stockVerificationTable.getValueAt(rowCounter,
                                stockVerificationTable.getColumn(ColumnConstants.CYCLE).getModelIndex()));
                        String generationComments = String.valueOf(stockVerificationTable.getValueAt(rowCounter,
                                stockVerificationTable.getColumn("Generation Comment").getModelIndex()));
                        
                        generation = StockGenerationDAO.getInstance().findStockGeneration(generationValue, cycleValue);
                        
                        if (generation == null && !generationValue.equals("")) {
                        	generation = StockGenerationDAO.getInstance().insert(generationValue, cycleValue, generationComments);
                        }


                        if (String.valueOf(stockVerificationTable.getValueAt(rowCounter,"classification id")).equals("null")
                        		||String.valueOf(stockVerificationTable.getValueAt(rowCounter,"classification id")).equals(""))
                        {
                        	classification = null;
                        }
                        else 
                        {
                        	Integer classificationId = Integer.parseInt(String.valueOf(stockVerificationTable.getValueAt(rowCounter,"classification id")) );
                            
                        	classification = ClassificationDAO.getInstance().findById(classificationId);
                              
                        }

                        if (String.valueOf(stockVerificationTable.getValueAt(rowCounter,"taxonomy id")).equals("null")
                        		||String.valueOf(stockVerificationTable.getValueAt(rowCounter,"taxonomy id")).equals(""))
                        {
                        	taxonomy = null;
                        }
                        else 
                        {
                            Integer taxonomy_id = Integer.parseInt(String.valueOf(stockVerificationTable.getValueAt(rowCounter,"taxonomy id")));
                            taxonomy = TaxonomyDAO.getInstance().findById(taxonomy_id);                          	
                        }

                        String accessionName = StringUtils.defaultIfBlank((String) (stockVerificationTable.getValueAt(
                                rowCounter, stockVerificationTable.getColumn(ColumnConstants.ACCESSION).getModelIndex())), "NA");
                        String accessionIdentifier = StringUtils.defaultIfBlank((String) (stockVerificationTable.getValueAt(
                                rowCounter, stockVerificationTable.getColumn("Accession Identifier").getModelIndex())), "NA");
                        String passportComments = StringUtils.defaultIfBlank((String) (stockVerificationTable.getValueAt(
                                rowCounter, stockVerificationTable.getColumn("Passport Comment").getModelIndex())), null);
                        // String sourceVal = (String)stockVerificationTable.getValueAt(rowCounter,
                        // stockVerificationTable.getColumn("Source").getModelIndex());
                        String pedigree = StringUtils.defaultIfBlank((String) stockVerificationTable.getValueAt(
                                rowCounter, stockVerificationTable.getColumn(ColumnConstants.PEDIGREE).getModelIndex()), "NA");
                        
                        passport = PassportDAO.getInstance().insert(source, classification, taxonomy, accessionName, accessionIdentifier, pedigree, passportComments);
                       
                        
                        Stock stock = new Stock(generation, passport, null, 
                        		String.valueOf(stockVerificationTable.getValueAt(rowCounter, stockVerificationTable
                                        .getColumn(ColumnConstants.STOCK_NAME).getModelIndex())),
                        		calendar.getTime(), null, null, null, null, null, null);                
                        
                        StockDAO.getInstance().insert(stock);                       
                        successful++;
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        if (LoggerUtils.isLogEnabled())
                            LoggerUtils.log(
                                    Level.INFO,
                                    "Failed to add stock"
                                            + (String) stockVerificationTable.getValueAt(rowCounter,
                                                    stockVerificationTable.getColumn(ColumnConstants.STOCK_NAME).getModelIndex())
                                            + "\n");
                        errorBlock.append("Failed to add stock"
                                + (String) stockVerificationTable.getValueAt(rowCounter, stockVerificationTable
                                        .getColumn(ColumnConstants.STOCK_NAME).getModelIndex()) + "\n");
                    }
                }
            }
        });

        inserts.start();
        new Thread(new Runnable() {
            public void run() {
                try {
                    inserts.join();
                } catch (InterruptedException e) {
                    if (LoggerUtils.isLogEnabled()) {
                        LoggerUtils.log(Level.INFO, e.toString());
                    }
                }
                navigateToNextScreen();
            }
        }).start();
    }

    protected void navigateToNextScreen() {
        MainLayout mainLayout = (MainLayout) getContext().getBean("mainLayoutBean");
        TabManager tabManager = mainLayout.getTabManager();
        TabComponent tabComponent = tabManager.getTabComponent(getTabComponentPanel());
        tabComponent.setCurrentComponentPanel(getTabComponentPanel());
        TabComponentPanel nextTabComponentPanel = tabComponent.getNextComponentPanel();
        ((OutsideSeedReportPanel) nextTabComponentPanel).setSource(getSource());
        ((OutsideSeedReportPanel) nextTabComponentPanel).setFinalTagsTable(stockVerificationTable);
        ((OutsideSeedReportPanel) nextTabComponentPanel).setErrorBlock(getErrorBlock());
        ((OutsideSeedReportPanel) nextTabComponentPanel).setSuccessful(successful);
        ((OutsideSeedReportPanel) nextTabComponentPanel).initializeReportPanel();
        tabManager.setReplaceComponentPanel(getTabComponentPanel(), nextTabComponentPanel);
    }

    public void populateStockVerificationTable(CheckBoxIndexColumnTable stockDetailsTable, Calendar calendar) {

        this.calendar = calendar;
        DecimalFormat df = new DecimalFormat("00");

        SessionFactory sessionFactory = HibernateSessionFactory.getSessionFactory();
        Session session = sessionFactory.openSession();
        Query LocationQuery = session.createSQLQuery("SELECT zipcode FROM location l "
                + "JOIN source s ON(l.location_id = s.location_id)" + "WHERE source_id = "
                + source.getSourceId());

        String zipcode = LocationQuery.list().get(0).toString();

        String stockPrefix = df.format(calendar.get(Calendar.MONTH) + 1) + df.format(calendar.get(Calendar.DATE))
                + df.format(calendar.get(Calendar.YEAR) % 100) + '.' + zipcode + '.' + getShortName().toUpperCase()
                + '.';
        df = new DecimalFormat("00000");

        int tagsCounter = 1;
        try {
            Query selectQuery = session.createSQLQuery("SELECT MAX(stock_name) " + "FROM stock "
                    + "WHERE stock_name LIKE '" + stockPrefix + "%'");

            List<String> maxCounter = selectQuery.list();
            if (maxCounter.get(0) != null) {
                tagsCounter += Integer.parseInt(maxCounter.get(0).substring(19));
            }

        } catch (HibernateException ex) {
            if (LoggerUtils.isLogEnabled()) {
                LoggerUtils.log(Level.INFO, ex.toString());
            }
        } finally {
            session.close();
        }
        Utils.removeAllRowsFromTable((DefaultTableModel) stockVerificationTable.getModel());
        // rows without accesion or pedigree won't go to database
        for (int rowCounter = 0; rowCounter < stockDetailsTable.getRowCount(); rowCounter++) {
            if (!StringUtils.isEmpty(String.valueOf(stockDetailsTable.getValueAt(rowCounter, stockDetailsTable
                    .getColumn(ColumnConstants.ACCESSION).getModelIndex())))
                    || !StringUtils.isEmpty(String.valueOf(stockDetailsTable.getValueAt(rowCounter, stockDetailsTable
                            .getColumn(ColumnConstants.PEDIGREE).getModelIndex())))) {
                Object rowData[] = new Object[stockVerificationTable.getColumnCount()];
                rowData[0] = false;
                rowData[1] = stockPrefix + df.format(tagsCounter);
                for (int j = 2; j < stockVerificationTable.getColumnCount(); j++)
                    rowData[j] = stockDetailsTable.getValueAt(rowCounter, j);
                ((DefaultTableModel) stockVerificationTable.getModel()).addRow(rowData);
                tagsCounter++;
            }
        }
        stockVerificationTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {

            public void valueChanged(ListSelectionEvent arg0) {
                stockVerificationTable.clearSelection();
            }
        });
        stockVerificationTable.getTableHeader().setReorderingAllowed(false);
        stockVerificationTable.getColumnModel().getColumn(0).setMinWidth(0);
        stockVerificationTable.hideColumn("classification id");
        stockVerificationTable.hideColumn("taxonomy id");

    }

    private Component getTablePanel() {
        JPanel tablePanel = new JPanel();
        tablePanel.setLayout(new BorderLayout());
        getStockVerificationTable().setMinimumSize(new Dimension(5, 2));
        setTableSize(getStockVerificationTable(), 1.0f, 20);
        getStockVerificationTable().setPreferredScrollableViewportSize(getStockVerificationTable().getPreferredSize());
        tablePanel.add(getStockVerificationTable().getTableHeader(), BorderLayout.NORTH);
        JScrollPane jscrollpane = new JScrollPane(getStockVerificationTable());
        tablePanel.add(jscrollpane, BorderLayout.CENTER);
        return tablePanel;
    }

    public String getShortName() {
        String nameParts[] = source.getPersonName().split(" ");
        String firstname = StringUtils.EMPTY;
        String lastname = StringUtils.EMPTY;

        firstname = nameParts[0].substring(0, 1);
        lastname = nameParts[1].substring(0, Math.min(4, nameParts[1].length()));
        for (int count = nameParts[1].length(); count < 4; count++)
            lastname += '$';
        return firstname + lastname;
    }
}
