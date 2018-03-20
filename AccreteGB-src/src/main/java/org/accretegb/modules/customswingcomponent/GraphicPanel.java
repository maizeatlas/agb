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

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.event.*;

public class GraphicPanel extends JPanel {
	private int zoomFactor;
	private int newX;
	private int newY;
	private double scale;
	protected JPanel canvas;
	protected JPanel xAxisNorth = new JPanel();
	protected JPanel yAxisWest = new JPanel();
	protected JPanel xAxisSouth = new JPanel();
	protected JPanel yAxisEast = new JPanel();
	private JScrollBar hBar;
	private JScrollBar vBar;
	private ImageIcon zoomInIcon;
	private ImageIcon zoomInColorIcon;
	private ImageIcon zoomOutIcon;
	private ImageIcon zoomOutColorIcon;
	protected JButton zoomInButton;
	protected JButton zoomOutButton;
	private JPanel expotPanel;

	
	public void initialize() {
		initializeIcons();
		initializeScrollBars();
		scale = 1;
		setLayout(new MigLayout("insets 0, gap 0"));
		add(getButtons(), "dock west");
		expotPanel = new JPanel();
		add(expotPanel,"w 100%, h 100%");
		expotPanel.setLayout(new MigLayout("insets 0, gap 0"));		
		expotPanel.add(canvas, "w 100%, h 100%");
		add(vBar, "dock east");
		add(hBar, "dock south");
		expotPanel.add(xAxisNorth, "dock south");
		expotPanel.add(xAxisSouth, "dock north");
		yAxisWest.setPreferredSize(new Dimension(50,canvas.getHeight()));
		yAxisEast.setPreferredSize(new Dimension(50,canvas.getHeight()));
		expotPanel.add(yAxisWest, "dock west");		
		expotPanel.add(yAxisEast, "dock east");		
	}
	private void initializeScrollBars() {
		hBar = new JScrollBar(JScrollBar.HORIZONTAL, 0, 1, 0, 1);
		vBar = new JScrollBar(JScrollBar.VERTICAL, 0, 1, 0, 1);
		hBar.addAdjustmentListener(new AdjustmentListener() {
			public void adjustmentValueChanged(AdjustmentEvent arg0) {
				newX = -arg0.getValue()*canvas.getWidth()/2;
				repaint();
			}			
		});
		vBar.addAdjustmentListener(new AdjustmentListener() {
			public void adjustmentValueChanged(AdjustmentEvent arg0) {
				newY = -arg0.getValue()*canvas.getHeight()/2;
				repaint();
			}			
		});
	}
	private void initializeIcons() {
		zoomInIcon = new ImageIcon(GraphicPanel.class.getClassLoader().getResource("images/zoomIn.png"));	
		zoomInColorIcon = new ImageIcon(GraphicPanel.class.getClassLoader().getResource("images/zoomInColor.png"));	
		zoomOutIcon = new ImageIcon(GraphicPanel.class.getClassLoader().getResource("images/zoomOut.png"));	
		zoomOutColorIcon = new ImageIcon(GraphicPanel.class.getClassLoader().getResource("images/zoomOutColor.png"));	
	}
	private JPanel getButtons() {
		JPanel buttonsPanel = new JPanel(new MigLayout("insets 5, gap 5"));
		zoomInButton = new JButton(zoomInIcon);
		zoomOutButton = new JButton(zoomOutIcon);
		zoomOutButton.setEnabled(false);
		zoomInButton.addMouseListener(new MouseAdapter(){
			public void mouseEntered(MouseEvent arg0) {
				if(getZoomInButton().isEnabled()) {
					getZoomInButton().setIcon(getZoomInColorIcon());
				}
			}
			public void mouseExited(MouseEvent arg0) {
				getZoomInButton().setIcon(getZoomInIcon());	
			}
		});
		zoomOutButton.addMouseListener(new MouseAdapter(){
			public void mouseEntered(MouseEvent arg0) {
				if(getZoomOutButton().isEnabled()) {
					getZoomOutButton().setIcon(getZoomOutColorIcon());
				}
			}
			public void mouseExited(MouseEvent arg0) {
				getZoomOutButton().setIcon(getZoomOutIcon());	
			}
		});
		zoomInButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				scale += 0.5;
				hBar.setMaximum(hBar.getMaximum()+1);
				vBar.setMaximum(vBar.getMaximum()+1);
				zoomInButton.setEnabled(hBar.getMaximum() != 23);					
				zoomOutButton.setEnabled(true);
				repaint();
			}			
		});
		zoomOutButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				scale -= 0.5;
				hBar.setMaximum(hBar.getMaximum()-1);
				vBar.setMaximum(vBar.getMaximum()-1);
				zoomOutButton.setEnabled(scale != 1);
				zoomInButton.setEnabled(true);
				repaint();
			}			
		});
		buttonsPanel.add(getZoomInButton(), "h 24:24:24, w 24:24:24, wrap");
		buttonsPanel.add(getZoomOutButton(), "h 24:24:24, w 24:24:24, wrap");
		return buttonsPanel;
	}
	
	public int getZoomFactor() {
		return zoomFactor;
	}
	public void setZoomFactor(int zoomFactor) {
		this.zoomFactor = zoomFactor;
	}
	public int getNewX() {
		return newX;
	}
	public void setNewX(int newX) {
		this.newX = newX;
	}
	public int getNewY() {
		return newY;
	}
	public void setNewY(int newY) {
		this.newY = newY;
	}
	public double getScale() {
		return scale;
	}
	public void setScale(double scale) {
		this.scale = scale;
	}
	public JPanel getCanvas() {
		return canvas;
	}
	public void setCanvas(JPanel canvas) {
		this.canvas = canvas;
	}
	public ImageIcon getZoomInIcon() {
		return zoomInIcon;
	}
	public void setZoomInIcon(ImageIcon zoomInIcon) {
		this.zoomInIcon = zoomInIcon;
	}
	public ImageIcon getZoomInColorIcon() {
		return zoomInColorIcon;
	}
	public void setZoomInColorIcon(ImageIcon zoomInColorIcon) {
		this.zoomInColorIcon = zoomInColorIcon;
	}
	public ImageIcon getZoomOutIcon() {
		return zoomOutIcon;
	}
	public void setZoomOutIcon(ImageIcon zoomOutIcon) {
		this.zoomOutIcon = zoomOutIcon;
	}
	public ImageIcon getZoomOutColorIcon() {
		return zoomOutColorIcon;
	}
	public void setZoomOutColorIcon(ImageIcon zoomOutColorIcon) {
		this.zoomOutColorIcon = zoomOutColorIcon;
	}
	public JButton getZoomInButton() {
		return zoomInButton;
	}
	public void setZoomInButton(JButton zoomInButton) {
		this.zoomInButton = zoomInButton;
	}
	public JButton getZoomOutButton() {
		return zoomOutButton;
	}
	public void setZoomOutButton(JButton zoomOutButton) {
		this.zoomOutButton = zoomOutButton;
	}
	public JScrollBar gethBar() {
		return hBar;
	}
	public void sethBar(JScrollBar hBar) {
		this.hBar = hBar;
	}
	public JScrollBar getvBar() {
		return vBar;
	}
	public void setvBar(JScrollBar vBar) {
		this.vBar = vBar;
	}
	public JPanel getExpotPanel() {
		return expotPanel;
	}
	public void setExpotPanel(JPanel expotPanel) {
		this.expotPanel = expotPanel;
	}
}
