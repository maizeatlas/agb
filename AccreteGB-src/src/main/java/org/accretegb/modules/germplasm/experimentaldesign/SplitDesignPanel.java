package org.accretegb.modules.germplasm.experimentaldesign;
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

import org.accretegb.modules.main.LoginScreen;

import java.awt.*;

/**
 * @author nkumar
 * do the layout of the complete randomized design panel
 */
public class SplitDesignPanel extends JPanel {

    private static final long serialVersionUID = 1L;
    private JTextField reps;
    private JTextField seedCount;
    @SuppressWarnings("rawtypes")
    private JComboBox methods;

    /**
     * initializes the panel for complete randomized design panel
     */
    public void initialize() {
        setLayout(new MigLayout("insets 5 0 5 5, gapx 0"));
        add(new JLabel("Split Design"), "span, grow, wrap");
        add(new JLabel("Replications"), "pushx");
        getReps().setColumns(11);
        add(getReps(), "w 100%,growx, wrap");
        add(new JLabel("Seed"), "pushx");
        getSeedCount().setColumns(11);
        add(getSeedCount(), "w 100%,growx, wrap");
        add(new JLabel("Method"), "pushx");
        add(getMethods(), "w 100%,wrap");
    }

    public JTextField getReps() {
        return reps;
    }

    public void setReps(JTextField reps) {
        this.reps = reps;
    }

    public JTextField getSeedCount() {
        return seedCount;
    }

    public void setSeedCount(JTextField seedCount) {
        this.seedCount = seedCount;
    }

    @SuppressWarnings("rawtypes")
    public JComboBox getMethods() {
        return methods;
    }

    @SuppressWarnings("rawtypes")
    public void setMethods(JComboBox methods) {
        this.methods = methods;
    }

}