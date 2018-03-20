package org.accretegb.modules.tab;

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

import org.accretegb.modules.config.AccreteGBBeanFactory;

import javax.swing.*;
import javax.swing.plaf.basic.BasicButtonUI;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.LinkedList;
import java.util.List;

import static org.accretegb.modules.config.AccreteGBBeanFactory.getContext;

/**
 * @author nkumar
 * This class have all properties such as TabComponentPanels,
 * title for showing in the tabbedPane
 * a jPanel so that we have close button in tabbed Pane
 * if a tabComponent isStatic i.e. if it opens from a toolBar
 */
public class TabComponent extends JPanel {

    private static final long serialVersionUID = 1L;

    private List<TabComponentPanel> componentPanels = new LinkedList<TabComponentPanel>();
    private TabComponentPanel currentComponentPanel;
    private String title;
    private boolean isStatic;

    public void initialize() {
        setCurrentComponentPanel(getComponentPanels().get(0));
        add(new JLabel(getTitle()));
        add( new TabButton());
        setOpaque(false);
    }

    /*
     * @returns the next panel in a TabComponent
     */
    public TabComponentPanel getNextComponentPanel() {
        if (getCurrentComponentPanel().getPanelIndex() == getComponentPanels().size() - 1) {
            return getCurrentComponentPanel();
        } else {
            return getComponentPanels().get(getCurrentComponentPanel().getPanelIndex() + 1);
        }
    }

    /*
     * @returns the previous panel in the TabComponent
     */
    public TabComponentPanel getPrevComponentPanel() {
        if (getCurrentComponentPanel().getPanelIndex() == 0) {
            return getCurrentComponentPanel();
        } else {
            return getComponentPanels().get(getCurrentComponentPanel().getPanelIndex() - 1);
        }
    }

    /*
     * @author : nkumar
     * This class is used for adding closing facility in the TabComponent in the tabbedPane
     * of MainLayout
     */
    private class TabButton extends JButton implements ActionListener {

        private static final long serialVersionUID = 1L;

        private static final String CLOSE_TAB = "Close this Tab";
        private static final int SIZE = 17;

        public TabButton() {
            setPreferredSize(new Dimension(SIZE, SIZE));
            setToolTipText(CLOSE_TAB);
            setUI(new BasicButtonUI());
            setContentAreaFilled(false);
            setFocusable(false);
            setBorder(BorderFactory.createEtchedBorder());
            setBorderPainted(false);
            addMouseListener(getButtonmouselistener());
            setRolloverEnabled(true);
            addActionListener(this);
        }

        public void actionPerformed(ActionEvent e) {
            JTabbedPane tabbedPane = AccreteGBBeanFactory.getTabbedPane();
            int i = tabbedPane.indexOfTabComponent(TabComponent.this);
            if (i != -1) {
                tabbedPane.remove(i);
            }
        }

        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            if (getModel().isPressed()) {
                g2.translate(1, 1);
            }
            g2.setStroke(new BasicStroke(2));
            g2.setColor(Color.BLACK);
            if (getModel().isRollover()) {
                g2.setColor(Color.MAGENTA);
            }
            int delta = 5;
            g2.drawLine(delta, delta, getWidth() - delta - 1, getHeight() - delta - 1);
            g2.drawLine(getWidth() - delta - 1, delta, delta, getHeight() - delta - 1);
            g2.dispose();
        }
    }

    /*
     * @author: nkumar
     * This class keeps the track on Mouse when Mouse enters the TabComponent i.e.
     * The tabbedPane header in the MainLayout
     */
    private final static MouseAdapter buttonMouseListener = new MouseAdapter() {
        /*
         * @param: e The MouseEvent
         */
        public void mouseEntered(MouseEvent e) {
            Component component = e.getComponent();
            if (component instanceof AbstractButton) {
                AbstractButton button = (AbstractButton) component;
                button.setBorderPainted(true);
            }
        }

        /*
         * @param: e the MouseEvent
         */
        public void mouseExited(MouseEvent e) {
            Component component = e.getComponent();
            if (component instanceof AbstractButton) {
                AbstractButton button = (AbstractButton) component;
                button.setBorderPainted(false);
            }
        }
    };

    public static MouseAdapter getButtonmouselistener() {
        return buttonMouseListener;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public List<TabComponentPanel> getComponentPanels() {
        return componentPanels;
    }

    public void setComponentPanels(List<TabComponentPanel> componentPanels) {
        this.componentPanels = componentPanels;
    }

    public TabComponentPanel getCurrentComponentPanel() {
        return currentComponentPanel;
    }

    public void setCurrentComponentPanel(TabComponentPanel currentComponentPanel) {
        this.currentComponentPanel = currentComponentPanel;
    }

    public boolean getIsStatic() {
        return isStatic;
    }

    public void setIsStatic(boolean isStatic) {
        this.isStatic = isStatic;
    }

}