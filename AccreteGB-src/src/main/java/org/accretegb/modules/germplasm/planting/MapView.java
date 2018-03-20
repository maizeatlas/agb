package org.accretegb.modules.germplasm.planting;

import net.miginfocom.swing.MigLayout;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.accretegb.modules.customswingcomponent.Utils;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;


public class MapView extends JPanel {

	private static final long serialVersionUID = 1L;
		
	private boolean autoUpdate = false;
	private Canvas canvas;
	private JTextField numRows;
	private JTextField numCols;
	private JButton set;
	private JCheckBox autoAspectRatio;
	private JToggleButton[] layoutButton;
	private int layoutOption;
	public enum StartCorner{TOP_LEFT, TOP_RIGHT, BOTTOM_LEFT, BOTTOM_RIGHT};
	private StartCorner startCorner;	
	private JButton exportImage;
	private TableView tableView;
	private JLabel needSync;
	private String[] searchBy = {"Accession", "Pedigree", "StockName"};
	private Color selectedColor;
	public boolean exportingImage = false;
	

	public JLabel getNeedSync() {
		return needSync;
	}

	public void setNeedSync(JLabel needSync) {
		this.needSync = needSync;
	}

	public JTextField getNumRows() {
		return numRows;
	}

	public void setNumRows(JTextField numRows) {
		this.numRows = numRows;
	}

	public JTextField getNumCols() {
		return numCols;
	}

	public void setNumCols(JTextField numCols) {
		this.numCols = numCols;
	}

	public int getLayoutOption() {
		return layoutOption;
	}

	public void setLayoutOption(int layoutOption) {
		this.layoutOption = layoutOption;
	}

	public StartCorner getStartCorner() {
		return startCorner;
	}

	public void setStartCorner(StartCorner startCorner) {
		this.startCorner = startCorner;
	}

	public Canvas getCanvas() {
		return canvas;
	}

	public void setCanvas(Canvas canvas) {
		this.canvas = canvas;
	}
	
	public TableView getTableView() {
		return tableView;
	}

	public void setTableView(TableView tableView) {
		this.tableView = tableView;
	}

	public void initialize() {
		setLayout(new MigLayout("insets 10, gap 5"));
		add(buttonsPanel(), "w 100%, wrap");
		canvas.setMapView(this);
		add(canvas, "w 100%, h 100%,  wrap");
		canvas.setToolTipText("<HTML>Plots from Stock Selection directly are colored with White"
								+ "<br>Plots from Experiments  are colored with Red"
								+ "<br>Plots with Filler as stockname are colored with Gray"
								+ "<br>Empty spots are colored with Black"
								+ "<br>Plots that are selected are colored with Blue</HTML>");
		getExportButton();
		
	}
	
	
	 
	private void getExportButton(){
		exportImage = new JButton("Export MapView");
		add(exportImage,"pushx, al right");
		exportImage.addActionListener(new ActionListener(){

			public void actionPerformed(ActionEvent e) {
				
				JFileChooser fileChooser = new JFileChooser();
	            File file = new File(System.getProperty("java.io.tmpdir") + "/" + "MapView.png");
	            fileChooser.setSelectedFile(file);
	            int approve = fileChooser.showSaveDialog(null);
	            if (approve != JFileChooser.APPROVE_OPTION) {
	                return;
	            }

	            file = fileChooser.getSelectedFile();
	            BufferedImage  bi = new BufferedImage((int) (canvas.getCanvas().getWidth()*canvas.getScale()), (int) (canvas.getExpotPanel().getHeight()*canvas.getScale()), BufferedImage.TYPE_INT_ARGB);
	            Graphics  g = bi.createGraphics();
	            exportingImage = true;
	            canvas.getCanvas().paint(g);
	            exportingImage = false;
	            try {
					ImageIO.write(bi, "PNG",file);
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				
			}
			
		});
	
	}
	
	
	private JPanel buttonsPanel() {
		JPanel buttonsPanel = new JPanel(new MigLayout("insets 10, gap 5"));
		
		autoUpdate = true;
		numRows = new JTextField(5);
		numRows.setText("-1");
		numCols = new JTextField(5);
		numCols.setText("-1");
		set = new JButton("Set");
		autoUpdate = false;
		autoAspectRatio = new JCheckBox("Auto");
		autoAspectRatio.setSelected(false);
		needSync = new JLabel("<HTML><FONT color = #B80000>Changes not synced with database</FONT></HTML>");
		needSync.setVisible(false);
		buttonsPanel.add(new JLabel("Rows : "));
		buttonsPanel.add(numRows);
		buttonsPanel.add(new JLabel("Columns: "));
		buttonsPanel.add(numCols);
		buttonsPanel.add(set);
		buttonsPanel.add(autoAspectRatio, "pushx");		
		buttonsPanel.add(needSync);
		
		layoutButton = new JToggleButton[4];
		ButtonGroup layoutGroup = new ButtonGroup();
		buttonsPanel.add(new JLabel("Layout Option: "));
		for(int counter=0; counter < layoutButton.length; counter++) {
			layoutButton[counter] = new JToggleButton(new ImageIcon(MapView.class.getClassLoader().getResource("images/option"+counter+".png")));
			layoutGroup.add(layoutButton[counter]);
			buttonsPanel.add(layoutButton[counter], "h 36:36:36, w 36:36:36");
			final int option = counter;
			layoutButton[counter].setContentAreaFilled(false);
			layoutButton[counter].setOpaque(true);
			layoutButton[counter].addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent actionEvent) {
					canvas.cleanSelection();
					tableView.setTableChanged(true);
					needSync.setVisible(true);
					for(int counter=0; counter < layoutButton.length; counter++) {
						if(layoutButton[counter].isSelected())
							layoutButton[counter].setBackground(Color.GREEN);
						else
							layoutButton[counter].setBackground(new Color(238, 238, 238));
					}
					layoutOption = option;
					repaint();
				}			
			});
		}
		layoutButton[0].setSelected(true);
		layoutButton[0].setBackground(Color.GREEN);
		layoutOption = 0;
		
		JPanel startCornerPanel = new JPanel(new MigLayout("insets 0, gap 0"));
		final JToggleButton topLeft = new JToggleButton();
		final JToggleButton topRight = new JToggleButton();
		final JToggleButton bottomLeft = new JToggleButton();
		final JToggleButton bottomRight = new JToggleButton();
		
		topLeft.setContentAreaFilled(false);
		topLeft.setOpaque(true);
		topRight.setContentAreaFilled(false);
		topRight.setOpaque(true);
		bottomLeft.setContentAreaFilled(false);
		bottomLeft.setOpaque(true);
		bottomRight.setContentAreaFilled(false);
		bottomRight.setOpaque(true);
		ButtonGroup group2 = new ButtonGroup();
		
		bottomLeft.setSelected(true);
		bottomLeft.setBackground(Color.GREEN);
		startCorner = StartCorner.BOTTOM_LEFT;
		
		group2.add(topLeft); group2.add(topRight); group2.add(bottomLeft); group2.add(bottomRight);
		startCornerPanel.add(topLeft, "h 18:18:18, w 18:18:18"); startCornerPanel.add(topRight, "h 18:18:18, w 18:18:18, wrap"); 
		startCornerPanel.add(bottomLeft, "h 18:18:18, w 18:18:18"); startCornerPanel.add(bottomRight, "h 18:18:18, w 18:18:18, wrap"); 
		buttonsPanel.add(new JLabel("Start Corner: "), "gapleft 50");
		buttonsPanel.add(startCornerPanel, "wrap");
		
		topLeft.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				canvas.cleanSelection();
				tableView.setTableChanged(true);
				needSync.setVisible(true);
				startCorner = StartCorner.TOP_LEFT;
				topLeft.setBackground(Color.GREEN);
				topRight.setBackground(new Color(238, 238, 238));
				bottomLeft.setBackground(new Color(238, 238, 238));
				bottomRight.setBackground(new Color(238, 238, 238));
				canvas.cleanSelection();
				repaint();
			}			
		});
		topRight.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				canvas.cleanSelection();
				tableView.setTableChanged(true);
				needSync.setVisible(true);
				startCorner = StartCorner.TOP_RIGHT;
				topLeft.setBackground(new Color(238, 238, 238));
				topRight.setBackground(Color.GREEN);
				bottomLeft.setBackground(new Color(238, 238, 238));
				bottomRight.setBackground(new Color(238, 238, 238));
				canvas.cleanSelection();
				repaint();
			}			
		});
		bottomLeft.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				canvas.cleanSelection();
				tableView.setTableChanged(true);
				needSync.setVisible(true);
				startCorner = StartCorner.BOTTOM_LEFT;
				topLeft.setBackground(new Color(238, 238, 238));
				topRight.setBackground(new Color(238, 238, 238));
				bottomLeft.setBackground(Color.GREEN);
				bottomRight.setBackground(new Color(238, 238, 238));
				canvas.cleanSelection();
				repaint();
			}			
		});
		bottomRight.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				canvas.cleanSelection();
				tableView.setTableChanged(true);
				needSync.setVisible(true);
				startCorner = StartCorner.BOTTOM_RIGHT;
				topLeft.setBackground(new Color(238, 238, 238));
				topRight.setBackground(new Color(238, 238, 238));
				bottomLeft.setBackground(new Color(238, 238, 238));
				bottomRight.setBackground(Color.GREEN);
				canvas.cleanSelection();
				repaint();
			}			
		});
		
		buttonsPanel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
		set.addActionListener(new ActionListener(){

			public void actionPerformed(ActionEvent e) {
				canvas.cleanSelection();
				int rows = Utils.isInteger(numRows.getText())? Integer.parseInt(numRows.getText()) : 0;
				int cols = Utils.isInteger(numCols.getText())? Integer.parseInt(numCols.getText()) : 0;
				if(!autoAspectRatio.isSelected()) {
					if(rows*cols >= canvas.getStockList().size()) {
						
						repaint();
					}
					else
					{
						JOptionPane.showConfirmDialog(MapView.this, "Make sure all stocks are contained in the given field size", "Error!", JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE);			
						rows = cols = (int) Math.ceil(Math.sqrt(canvas.getStockList().size()));
						autoUpdate = true;
						numRows.setText(String.valueOf(rows));
						numCols.setText(String.valueOf(cols));
						autoUpdate = false;
					}
				}else{
					if(rows <= 0 && cols <= 0){
						JOptionPane.showConfirmDialog(MapView.this, "Invalid Input", "Error!", JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE);									
					}else{
						if(rows <= 0 ){
							rows = (canvas.getStockList().size() / cols) + 1;
							numRows.setText(String.valueOf(rows));							
						}else{
							cols = (canvas.getStockList().size() / rows) + 1;
							numCols.setText(String.valueOf(cols));
						}
					}
					repaint();
				}
				
			}
			
		});

		autoAspectRatio.setToolTipText("Check this, you only need to fill out either Columns or Rows");
		autoAspectRatio.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) 
			{
				canvas.cleanSelection();
				tableView.setTableChanged(true);
				needSync.setVisible(true);
				if(autoAspectRatio.isSelected()) {
					int totalStocks = canvas.getStockList().size();
					if(layoutOption == 2 || layoutOption == 3) {
						int rows = Integer.parseInt(numRows.getText());
						int cols = (int) Math.ceil(totalStocks*1.0/rows);
						autoUpdate = true;
						numCols.setText(String.valueOf(cols));
						autoUpdate = false;
					}
					else {
						int cols = Integer.parseInt(numCols.getText());
						int rows = (int) Math.ceil(totalStocks*1.0/cols);
						autoUpdate = true;
						numRows.setText(String.valueOf(rows));
						autoUpdate = false;
					}
				}
				repaint();
			}			
		});
		buttonsPanel.add(getSearchPanel(), "span");	
		
		return buttonsPanel;
	}
	
	private JPanel getSearchPanel() {
		final JPanel searchPanel = new JPanel(new MigLayout("insets 0, gap 0"));		
		final JTextField searchValue = new JTextField(15);
		final JLabel searchByLabel = new JLabel("Search By ");
		final JComboBox<String> options = new JComboBox<String>(searchBy);
		
		searchPanel.add(searchByLabel);
		searchPanel.add(options,"gapright 5");
		searchPanel.add(new JLabel("Search Value: "));
		searchPanel.add(searchValue);
		final JButton hightlight = new JButton("Select");		
		searchPanel.add(hightlight);
		hightlight.setToolTipText("select the plots");		
		hightlight.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				if(!searchValue.getText().trim().equals(""))
				{
					hightlight.setForeground(Color.RED);
					hightlight.setText("Selecting, please be patient");
					canvas.selectPlotsBySearch(String.valueOf(options.getSelectedItem()),searchValue.getText());
					canvas.repaint();
					hightlight.setForeground(Color.BLACK);
					hightlight.setText("Select");
				}
			}
		});
		final JButton colorButton = new JButton("Choose Color");
		searchPanel.add(colorButton,"gapleft 5");
		final JColorChooser colorChooser = new JColorChooser(Color.BLACK);
		colorChooser.setBorder(null);		
		colorButton.addActionListener(new ActionListener() {
		    public void actionPerformed(ActionEvent arg0) {
		    	toggleColorChooser();
		    }
		    
		    protected void toggleColorChooser() {
		    	int option = JOptionPane.showConfirmDialog(null, colorChooser, "Select Colors For Selected Plots", JOptionPane.OK_CANCEL_OPTION);
		        if(option == JOptionPane.OK_OPTION){
		        	System.out.println("select color : " + colorChooser.getSelectionModel().getSelectedColor());
		        	if(!colorChooser.getSelectionModel().getSelectedColor().equals(new Color(184, 207, 229)))
		        	{
		        		setSelectedColor(colorChooser.getSelectionModel().getSelectedColor());
		        		canvas.colorPlots();
		        		canvas.repaint();
		        	}
		        }
		    }
		});
		
		return searchPanel;
	}

	public void updateDimensions() {
		int rows = Integer.parseInt(numRows.getText());
		int cols = Integer.parseInt(numCols.getText());
		if(rows == -1 || cols == -1 || canvas.getStockList().size() > rows*cols) {
			rows = cols = (int) Math.ceil(Math.sqrt(canvas.getStockList().size()));
			autoUpdate = true;
			numRows.setText(String.valueOf(rows));
			numCols.setText(String.valueOf(cols));
			needSync.setVisible(false);
			autoUpdate = false;
		}			
	}
	
	public Color getSelectedColor() {
		return selectedColor;
	}

	public void setSelectedColor(Color selectedColor) {
		this.selectedColor = selectedColor;
	}
}