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

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.tree.TreeNode;

import org.accretegb.modules.constants.ColumnConstants;
import org.accretegb.modules.hibernate.HibernateSessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.internal.SessionFactoryImpl;

import java.awt.*;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * @author nkumar
 * This class contains methods which assists in
 * custom table Component.
 */
public class Utils {

    /**
     * sets the size of the table
     * @param table - table whose size to be set
     * @param percentage - percentage of current size
     * @param rows - rows in the table
     */
    static public void setTableSize(JTable table, float percentage, int rows) {
        if (table == null) {
            return;
        }
        Integer height = rows * table.getRowHeight();
        Float width = new Float(table.getPreferredSize().width * percentage);
        table.setPreferredScrollableViewportSize(new Dimension(width.intValue(), height));
    }

    /**
     * adds a boolean column in row object at index 0
     * @param row - the row array object
     * @return returns a new Object
     */
    static Object[] addBooleanIndexColumn(Object[] row) {
        if(row == null) {
            return row;
        }
        Object[] modifiedRow = new Object[row.length + 1];
        modifiedRow[0] = new Boolean(false);
        for (int Counter = 0; Counter < row.length; Counter++) {
            modifiedRow[Counter + 1] = row[Counter];
        }
        return modifiedRow;
    }

    /**
     * adds a row object to the table
     * @param row -  the row array object
     * @param model - - the model in which rows are to be added
     * @param rowIndex - rowIndex in which row is to be set or added
     * @param isCheckBox - checks if boolean indexcolumn is to be added
     */
    static public void addRowToTable(Object[] row, DefaultTableModel model, int rowIndex, boolean isCheckBox) {
        if (isCheckBox) {
            row = addBooleanIndexColumn(row);
        }
        List<String> NumbericColumns = Arrays.asList(new String[]{ColumnConstants.TAG_ID, ColumnConstants.STOCK_ID, ColumnConstants.PACKET_ID, ColumnConstants.MATE_LINK,ColumnConstants.TOTAL_PACKETS,ColumnConstants.PACKET_NO,ColumnConstants.WEIGHT,ColumnConstants.NUMBER_OF_SEEDS});
        for (int columnIndex = 0, size = row.length; columnIndex < size; columnIndex++) {
             if (row[columnIndex] == null) {
                if(NumbericColumns.contains(model.getColumnName(columnIndex)))
            		row[columnIndex] = new Integer(0);
                else{
            		row[columnIndex] = new String("NULL");
                }
            }

            if (model.getRowCount() > rowIndex) {
                model.setValueAt(row[columnIndex], rowIndex, columnIndex);
            } else {
                model.addRow(row);
            }
        }
    }

    /**
     * remove all existing rows from the table
     * @param model - the model from rows are removed
     */
    static public void removeAllRowsFromTable(DefaultTableModel model) {
        while (model.getRowCount() > 0) {
            model.removeRow(0);
        }
    }

    /**
     * checks if String is valid integer or not
     * @param value - the String value
     * @return - true if String can be converted to integer
     */
    static public boolean isValidInteger(String value) {
        try {
            Integer.parseInt(value.trim());
        } catch (Exception ex) {
            return false;
        }
        return true;
    }

    /**
     * checks if the date is valid or not
     * @param date - date in String format
     * @return true - if date is a valid date
     */
    static public boolean isValidDate(String date) {
        try {
            @SuppressWarnings("unused")
            Date dateTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSSSS").parse(date.trim() + " 00:00:00.000000");
        } catch (Exception ex) {
            return false;
        }
        return true;
    }

    /**
     * transfer selected rows from a table to other table
     * @param fromTable - from table in which data is to be taken
     * @param toTable - to table in which data is to be stored
     */
    static public void transferRows(CheckBoxIndexColumnTable fromTable, CheckBoxIndexColumnTable toTable) {
        if (fromTable == null || toTable == null) {
            return;
        }

        List<Object[]> selectedRows = new LinkedList<Object[]>();

        for (int rowCounter = 0; rowCounter < fromTable.getRowCount(); rowCounter++) {
            // check for selected Row
            if ((Boolean) fromTable.getModel().getValueAt(rowCounter, 0)) {
                Object row[] = new Object[fromTable.getColumnCount()];
                row[0] = new Boolean(false);
                for (int columnCounter = 1; columnCounter < fromTable.getColumnCount(); columnCounter++) {
                    row[columnCounter] = new String((String) fromTable.getModel().getValueAt(rowCounter, columnCounter));
                }
                selectedRows.add(row);
            }
        }

        for (int rowCounter = 0; rowCounter < toTable.getRowCount(); rowCounter++) {
            // break when there no selected row remaining
            if (selectedRows.size() == 0) {
                break;
            }
            boolean isBlankRow = true;
            for (int columnCounter = 1; columnCounter < toTable.getColumnCount(); columnCounter++) {
                String value = (String) toTable.getModel().getValueAt(rowCounter, columnCounter);
                if (!value.trim().equalsIgnoreCase("")) {
                    isBlankRow = false;
                    break;
                }
            }
            //Insert in the Blank Row
            if (isBlankRow) {
                Object[] row = selectedRows.get(0);
                for (int columnCounter = 0; columnCounter < toTable.getColumnCount(); columnCounter++) {
                    toTable.getModel().setValueAt(row[columnCounter], rowCounter, columnCounter);
                }
                selectedRows.remove(0);
            } else {
                break;
            }
        }
        // if still have selectedRows we need to insert them
        if (selectedRows.size() > 0) {
            DefaultTableModel model = (DefaultTableModel) toTable.getModel();
            for (Object[] row : selectedRows) {
                model.addRow(row);
            }
        }
    }

    /**
     * save a table to a file
     * @param table - name of the table
     * @param panel - jpanel to show information messages
     * @param fileName - name of the file
     */
    @SuppressWarnings("deprecation")
    static public void saveTableToFile(JTable table, JPanel panel, String fileName) {
        try {
            if (table == null || fileName == null) {
                return;
            }
            JFileChooser fileChooser = new JFileChooser();
            File file = new File(System.getProperty("java.io.tmpdir") + "/" + fileName);
            fileChooser.setSelectedFile(file);
            int approve = fileChooser.showSaveDialog(panel);
            if (approve != JFileChooser.APPROVE_OPTION) {
                return;
            }
            file = fileChooser.getSelectedFile();
            BufferedWriter writer = new BufferedWriter(new FileWriter(file));
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
                    }else if (table.getValueAt(rowCounter, columnCounter) instanceof java.util.Date){
                    	Date date = (Date) table.getValueAt(rowCounter, columnCounter);
                    	value = String.valueOf(date.getMonth()+1)+"/"+String.valueOf(date.getDate())+
                    			"/"+String.valueOf(date.getYear()+1900);
                    }else {
                        value = String.valueOf(table.getValueAt(rowCounter, columnCounter));
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
   
   static public Map<String,String> getAuthorizationStrs(){
	   Map<String,String> map = new HashMap<String,String>();
	   Configuration config = new Configuration();	
       config.configure("pmhibernate.cfg.xml");	
       SessionFactoryImpl sessionFactoryImpl = (SessionFactoryImpl)HibernateSessionFactory.getSessionFactory();
       Properties props = sessionFactoryImpl.getProperties();
       String username = props.get("hibernate.connection.username").toString();
       String password = props.get("hibernate.connection.password").toString();
       String url = props.get("hibernate.connection.url").toString();
       String server = url.split(":")[2].replace("//", "");
       String port = url.split(":")[3].substring(0, url.split(":")[3].indexOf("/"));
       if(password.equals(""))
       {
       	password="";
       }
       map.put("username",username);
       map.put("password",password);
       map.put("url",url);
       map.put("server",server);
       map.put("port",port);     
	return map;
   }
    
   static public boolean isDouble(String s) {
	    try { 
	        Double.parseDouble(s); 
	    } catch(NumberFormatException e) { 
	        return false; 
	    }
	    return true;
	}
   
   static public boolean isInteger(String s) {
	    try { 
	        Integer.parseInt(s); 
	    } catch(NumberFormatException e) { 
	        return false; 
	    }
	    return true;
	}


}
