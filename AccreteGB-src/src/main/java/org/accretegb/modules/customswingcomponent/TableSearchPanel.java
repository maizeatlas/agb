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

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.TableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import static org.accretegb.modules.customswingcomponent.Utils.setTableSize;

/**
 * @author nkumar
 * @author chinmay
 * This is a panel which provides a table inside with
 * column searchable facility inside it.
 */
public class TableSearchPanel extends JPanel {

    private static final long serialVersionUID = 1L;
    private ImageIcon searchIcon;
    private ImageIcon downIcon;

    private String searchvalue;
    private JTextField searchTextField;
    private JButton searchButton;
    private JLabel filterDonors;
    private JLabel searchFieldsByCriteria[];
    private CheckBoxIndexColumnTable table;

    /**
     * initializes and set up the panel layout
     */
    public void initialize() {
        initializeButtons();
        initSearchFieldsByCriteria();
        setLayout(new MigLayout("insets 5 5 5 5, gapx 0"));
        add(getSearchPanel(), "wrap");
        add(getTablePanel(), "w 100%, h 100%, grow, span, wrap");
    }

    /**
     * initializes the search criteria labels
     */
    private void initSearchFieldsByCriteria() {
        JLabel searchFieldsByCriteria[] = new JLabel[getTable().getColumnCount()];
        for (int columnCounter = 0; columnCounter < getTable().getColumnCount(); columnCounter++) {
            searchFieldsByCriteria[columnCounter] = new JLabel("Search by " + getTable().getColumnName(columnCounter));
        }
    }

    /**
     * initializes the button images
     */
    private void initializeButtons() {
        downIcon = new ImageIcon(TableToolBoxPanel.class.getClassLoader().getResource("images/down.png"));
        searchIcon = new ImageIcon(TableToolBoxPanel.class.getClassLoader().getResource("images/Search.png"));
    }

    /**
     * sets layout of panel with table inside it
     * @return jpanel with table inside it
     */
    private JPanel getTablePanel() {
        JPanel tablePanel = new JPanel();
        tablePanel.setLayout(new BorderLayout());
        getTable().setMinimumSize(new Dimension(5, 2));
        setTableSize(getTable(), 1.0f, 20);
        getTable().setPreferredScrollableViewportSize(getTable().getPreferredSize());
        tablePanel.add(getTable().getTableHeader(), BorderLayout.NORTH);
        final JScrollPane jscrollpane = new JScrollPane(getTable());
        tablePanel.add(jscrollpane, BorderLayout.CENTER);
        return tablePanel;
    }

    /**
     * initializes the panel with searchable items
     * @return panel which has search dropdown
     */
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
        getSearchTextField().getDocument().addDocumentListener(new DocumentListener() {
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


    /**
     * does filtering based on input entered by the user
     */
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

    public CheckBoxIndexColumnTable getTable() {
        return table;
    }

    public void setTable(CheckBoxIndexColumnTable table) {
        this.table = table;
    }

    public ImageIcon getSearchIcon() {
        return searchIcon;
    }

    public void setSearchIcon(ImageIcon searchIcon) {
        this.searchIcon = searchIcon;
    }

    public String getSearchvalue() {
        return searchvalue;
    }

    public void setSearchvalue(String searchvalue) {
        this.searchvalue = searchvalue;
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

}