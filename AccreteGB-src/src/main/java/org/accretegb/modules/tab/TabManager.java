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
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.LinkedList;
import java.util.List;

/**
 * @author nkumar
 *
 */
public class TabManager {

    private static int TAB_HEIGHT = 22;

    private JTabbedPane tabbedPane;

    private List<TabComponent> tabComponents = new LinkedList<TabComponent>();

    public void initialize() {
        JTabbedPane pane = getTabbedPane();
        pane.setUI(new javax.swing.plaf.basic.BasicTabbedPaneUI() {
            @Override
            protected int calculateTabHeight(int tabPlacement, int tabIndex, int fontHeight) {
                return TAB_HEIGHT;
            }
        });
        pane.addMouseListener(new TabbedPaneListener());
    }

    public void refreshComponentPanel(TabComponentPanel currentPanel) {
        JTabbedPane pane = getTabbedPane();
        for (int counter = 0; counter < pane.getComponentCount(); counter++) {
            TabComponentPanel panel = (TabComponentPanel) pane.getComponentAt(counter);
            if (panel == currentPanel) {
                pane.getComponentAt(counter).invalidate();
                pane.getComponentAt(counter).validate();
                pane.getComponentAt(counter).repaint();
                return;
            }
        }
    }

    public void setReplaceComponentPanel(TabComponentPanel currentPanel, TabComponentPanel replacementPanel) {
        JTabbedPane pane = getTabbedPane();
        for (int counter = 0; counter < pane.getComponentCount(); counter++) {
            TabComponentPanel panel = (TabComponentPanel) pane.getComponentAt(counter);
            if (panel == currentPanel) {
                pane.setComponentAt(counter, replacementPanel);
                pane.getComponentAt(counter).invalidate();
                pane.getComponentAt(counter).validate();
                pane.getComponentAt(counter).repaint();
                return;
            }
        }
    }

    public TabComponent getTabComponent(TabComponentPanel panel) {
        List<TabComponent> tabComponents = getTabComponents();
        for (TabComponent tabComponent : tabComponents) {
            List<TabComponentPanel> tabComponentPanels = tabComponent.getComponentPanels();
            for (TabComponentPanel tabComponentPanel : tabComponentPanels) {
                if (panel == tabComponentPanel) {
                    return tabComponent;
                }
            }
        }
        return null;
    }

    private class TabbedPaneListener extends MouseAdapter {

        public void mousePressed(MouseEvent e) {
            maybeShowPopup(e);
        }

        public void mouseReleased(MouseEvent e) {
            maybeShowPopup(e);
        }
        public void maybeShowPopup(MouseEvent evt) {
            if (evt.isPopupTrigger()) {
                Component component = evt.getComponent();
                JPopupMenu popupMenu = new JPopupMenu();
                JMenuItem closeTabMenuItem = new JMenuItem("close tab");
                closeTabMenuItem.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        int selectedIndex = AccreteGBBeanFactory.getTabbedPane().getSelectedIndex();
                        if(selectedIndex >= 0) {
                            AccreteGBBeanFactory.getTabbedPane().remove(selectedIndex);
                        }
                    }
                });
                TabManager tabManager = AccreteGBBeanFactory.getTabManager();
                JMenuItem otherTabMenuItem = new JMenuItem("close other tabs");
                otherTabMenuItem.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        int selectedIndex = AccreteGBBeanFactory.getTabbedPane().getSelectedIndex();
                        int tabCount = AccreteGBBeanFactory.getTabbedPane().getTabCount();
                        System.out.println("selected Index"+selectedIndex+"\t tabCount "+tabCount);
                        if (selectedIndex >= 0) {
                            int maxIndex  = tabCount - 1;
                            //remove the right side of the current tab
                            while(maxIndex != selectedIndex && maxIndex >=0) {
                                AccreteGBBeanFactory.getTabbedPane().remove(maxIndex);
                                maxIndex--;
                            }
                            //remove all tabs from the left tabs
                            while (true) {
                                tabCount = AccreteGBBeanFactory.getTabbedPane().getTabCount();
                                if(tabCount<=1) {
                                    break;
                                }
                                AccreteGBBeanFactory.getTabbedPane().remove(0);
                            }
                        }
                    }
                });
                int tabCount = tabManager.getTabbedPane().getTabCount();
                if (tabCount <= 1) {
                    otherTabMenuItem.setEnabled(false);
                }
                popupMenu.add(closeTabMenuItem);
                popupMenu.add(otherTabMenuItem);
                popupMenu.show(component, evt.getX(), evt.getY());
            }
        }
    }

    public JTabbedPane getTabbedPane() {
        return tabbedPane;
    }

    public void setTabbedPane(JTabbedPane tabbedPane) {
        this.tabbedPane = tabbedPane;
    }

    public List<TabComponent> getTabComponents() {
        return tabComponents;
    }

    public void setTabComponents(List<TabComponent> tabComponents) {
        this.tabComponents = tabComponents;
    }

}