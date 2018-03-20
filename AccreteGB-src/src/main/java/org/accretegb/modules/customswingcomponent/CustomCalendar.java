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

import org.jdesktop.swingx.JXDatePicker;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Calendar;
import java.util.TimeZone;

import javax.swing.JFormattedTextField;

/**
 * @author nkumar
 * This has calendar inside it with a caldate.
 * The action listener sets the caldate based on selected date by the user.
 */
public class CustomCalendar {

    private Calendar calDate = Calendar.getInstance(TimeZone.getDefault());
    private JXDatePicker datePicker = new JXDatePicker();

    /**
     * adds listener to the calendar
     */
    public void initialize() {
    	datePicker.addActionListener(new ActionListener() {			
			public void actionPerformed(ActionEvent e) {
				calDate.setTime(datePicker.getDate());
			}
		});
    }

    public JXDatePicker getCustomDateCalendar() {
        return datePicker;
    }

    public void setCustomDateCalendar(JXDatePicker customDateCalendar) {
        this.datePicker = customDateCalendar;
    }
    
    public String getEditorText(){
    	JFormattedTextField editor = datePicker.getEditor();
    	return editor.getText().trim();
    }

    public Calendar getCalDate() {
    	
        return calDate;
    }

    public void setCalDate(Calendar calDate) {
        this.calDate = calDate;
    }

}