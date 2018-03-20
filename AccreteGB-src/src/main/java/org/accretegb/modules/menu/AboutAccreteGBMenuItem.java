package org.accretegb.modules.menu;

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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * @author nkumar
 * This class is used for a about AcreteGB
 * Menu Item
 */
public class AboutAccreteGBMenuItem extends MenuItem {

    private static final long serialVersionUID = 1L;

    public AboutAccreteGBMenuItem(String label) {
        super(label);
        this.addActionListener(new AboutAccreteGBButtonActionListener());
    }

    /**
     * @author nkumar
     * This class the is action Listener for the About Menu Button in the Menu
     */
    private class AboutAccreteGBButtonActionListener implements ActionListener {

        private static final String ABOUT_TITLE = "About AccreteGB";
        private static final String ABOUT_MESSAGE = "<html><p>Overview: Accrete Genetics & Breeding (AGB) is an open source laboratory information management "
        		+ "system with an accompanying multi-user interface for plant genetics & breeding programs. AGB supports the complete workflow for managing genetics "
        		+ "and breeding projects (from selecting stocks for a given planting to inventorying seed stocks from a harvest). AGB uses a modular design. "
        		+ "A project manager module provides an active memory of the workflow for different projects and allows collaborators to share/transfer responsibility "
        		+ "in executing breeding and trialing activities across seasons and locations. Projects and data are preserved as users advance through modules to choose "
        		+ "germplasm, create experimental designs, organize and plant genetic/breeding stocks and produce row or plant tags, create data collection files "
        		+ "and upload phenotype or weather data, track tissue samples, record mating types and pedigrees when harvesting, inventory seed stocks created, "
        		+ "and more. AGB does not handle sequence or genotype data or perform data analysis. There are plans to add a module for users to export datasets "
        		+ "in formats for downstream analysis with other tools.<br><br>AGB was designed based on a maize genetics and breeding program "
        		+ "but should be compatible with other programs.<br><br>Programing design: This system is fully integrated in the Eclipse programming environment "
        		+ "and is platform-independent. AGB is programmed in Java Swing using the Spring framework for control of software interface components "
        		+ "and the Hibernate framework for mapping data from two relational databases, AGB-Manager and AGB-Data. AGB also connects to R providing extensibility "
        		+ "for AGB through the he vast collection of packages available via R. Currently, the R API is only used for the experimental design module, calling "
        		+ "functions from the agricolae package for randomizing trials.<br><br>Developers: Ningjing Tian, Naveen Kumar, Matthew Saponaro, Chinmay Pednekar, "
        		+ "Teclemariam Weldekidan, Randy Wisser<p></html>";
        /**
         * show the user a new JFrame with details.
         * @param e - action Event
         */
        public void actionPerformed(ActionEvent e) {
            JFrame aboutBox = new JFrame();
            aboutBox.setTitle(ABOUT_TITLE);
            aboutBox.setSize(600, 450);
            aboutBox.setLocationByPlatform(true);
            aboutBox.setContentPane(new JLabel(ABOUT_MESSAGE));
            aboutBox.setResizable(false);
            aboutBox.setVisible(true);
        }

    }

}
