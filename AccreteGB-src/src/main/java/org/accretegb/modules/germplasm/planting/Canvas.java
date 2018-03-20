package org.accretegb.modules.germplasm.planting;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;

import org.accretegb.modules.customswingcomponent.GraphicPanel;
import org.accretegb.modules.customswingcomponent.Utils;
import org.accretegb.modules.germplasm.planting.MapView.StartCorner;

public class Canvas extends GraphicPanel{
	private List<PlantingRow> stockList;
	private static final int rulerMargin = 15;
	private MapView mapView;
	private TableView tableView;
	private List<PlotRectangle> plots = new ArrayList<PlotRectangle>();
	private HashMap<Integer,Integer> stockIndex_plotIndex = new HashMap<Integer,Integer>();
	private ArrayList<Integer> selectedStocksIndex = new ArrayList<Integer>();
	private List<PlotRectangle> selectedPlots = new ArrayList<PlotRectangle>();
	private boolean mouseHeld = false;
	private int pressedStockIndex = -1;
	private int rowSize ;
	private int colSize ;
	private int vGap , hGap;
	private int boxHeight ;
	private int boxWidth ;
	private Font font = null;
	private HashMap<String, List<Integer>> rep_stockIndex = new HashMap<String, List<Integer>>();
	private boolean designSelected = false;
	private boolean movingDone = true;
	private boolean delete = false;
	public int StartRow = 0;
	public void initialize() {		
		generateMapPanel();	
		super.initialize();
	}
	
	private void generateMapPanel() {
		canvas = new JPanel() {
			@Override
			public void paint(Graphics g){
				super.paint(g);  
				createPlots(g);
				drawMap(g);
				xAxisNorth.repaint();
				yAxisWest.repaint();
				xAxisSouth.repaint();
				yAxisEast.repaint();
			}
		};
		
		xAxisNorth = new JPanel() {
			@Override
			public void paintComponent(Graphics g) {
				super.paintComponent(g);  
				createXAxis(g);				
			}	
		};
		
		
		yAxisWest = new JPanel() {
			@Override
			public void paintComponent(Graphics g) {
				super.paintComponent(g);  
				createYAxis(g);
				
			}	
		};
		
		xAxisSouth = new JPanel() {
			@Override
			public void paintComponent(Graphics g) {
				super.paintComponent(g);  
				createXAxis(g);				
			}	
		};
		
		
		yAxisEast = new JPanel() {
			@Override
			public void paintComponent(Graphics g) {
				super.paintComponent(g);  
				createYAxis(g);
				
			}	
		};
		addMouseListeners();
		addMotionListeners();
		addParentSizeListener();
		xAxisNorth.setBorder(BorderFactory.createLineBorder(Color.black));
		yAxisWest.setBorder(BorderFactory.createLineBorder(Color.black));
		xAxisSouth.setBorder(BorderFactory.createLineBorder(Color.black));
		yAxisEast.setBorder(BorderFactory.createLineBorder(Color.black));
	}
	private void addParentSizeListener() {
		canvas.addComponentListener(new ComponentAdapter(){
       	 public void componentResized(final ComponentEvent e) {
    		 repaint();
    	 }
    });
		
	}
	
	private void createPlots(Graphics g) {
		int totalStocks = stockList.size();
		rowSize = Integer.parseInt(mapView.getNumRows().getText());
		colSize = Integer.parseInt(mapView.getNumCols().getText());
		vGap = (int) (canvas.getHeight()*getScale()/(5.0*rowSize));
		hGap = (int) (canvas.getWidth()*getScale()/(colSize*10.0));
		boxHeight = (int) ((canvas.getHeight()*getScale() - vGap)/rowSize);
		boxWidth = (int) ((canvas.getWidth()*getScale() - hGap)/colSize);
		if(!mouseHeld){
			plots.clear();
			stockIndex_plotIndex.clear();	
			rep_stockIndex.clear();
			int plotIndex = 0;
			for(int coorX=1; coorX<=colSize; coorX++){
				for(int coorY=1; coorY<=rowSize; coorY++) {
					int x = 0;
					int y = 0;
					switch(mapView.getStartCorner()){
						case BOTTOM_LEFT:
							x = getNewX() + rulerMargin + (hGap + (coorX-1)*boxWidth); 
							y = getNewY() + rulerMargin + (vGap + (rowSize-coorY)*boxHeight);
							break;
							
						case TOP_LEFT:
							x = getNewX() + rulerMargin + (hGap + (coorX-1)*boxWidth); 
							y = getNewY() + rulerMargin + (vGap + (coorY-1)*boxHeight);
							break;
						case TOP_RIGHT:
							x = getNewX() + rulerMargin + (hGap + (colSize-coorX)*boxWidth); 
							y = getNewY() + rulerMargin + (vGap + (coorY-1)*boxHeight);
							break;
							
						case BOTTOM_RIGHT:
							x = getNewX() + rulerMargin + (hGap + (colSize-coorX)*boxWidth); 
							y = getNewY() + rulerMargin + (vGap + (rowSize-coorY)*boxHeight);
							break;
					}
					int stockIndex  = getStockIndexByColRow(coorX,coorY);
					if(stockIndex < totalStocks && stockIndex >=0)
					{
						PlotRectangle plot =  new PlotRectangle(x,y, boxWidth-hGap, boxHeight-vGap,stockList.get(stockIndex), stockIndex,this.StartRow);				
						plots.add(plot);
						PlantingRow plotStock  = stockList.get(stockIndex);
						plotStock.setCoordinates(coorX, coorY);
						if(plotStock.getRep()!=null && !plotStock.getRep().equals("null")){
							if(!rep_stockIndex.containsKey(plotStock.getRep())){
								rep_stockIndex.put(plotStock.getRep(), new ArrayList<Integer>());
							}
							rep_stockIndex.get(plotStock.getRep()).add(stockIndex);
						}
						stockIndex_plotIndex.put(stockIndex,plotIndex);
						
					}else{
						PlotRectangle plot =  new PlotRectangle(x,y, boxWidth-hGap, boxHeight-vGap,null,-1,-1);
						plots.add(plot);
					}
					
					plotIndex++;
				}
			}		
		}
		
		// set up font		
		font = new Font(g.getFont().getFontName(), Font.PLAIN, 10); 
    	g.setFont(font);
    	g.setColor(Color.black);
    	double ratio = (double)g.getFontMetrics().stringWidth("99") / ((double)boxHeight);   				    	
		if( ratio  > 1)
    	{				
			font = new Font(g.getFont().getFontName(), Font.PLAIN, (int)(10 / (ratio+1)));	
			g.setFont(font);			    			 
    	}
		
		if(mapView.exportingImage){
			g.setColor(Color.WHITE);
			g.fillRect(0, 0, getNewX() +rulerMargin*2 + (colSize+2) * boxWidth, rowSize * boxHeight+rulerMargin+boxHeight);
			g.setColor(Color.black);
			if(mapView.getNumCols().getText()!=null){
				int colSize = Integer.parseInt(mapView.getNumCols().getText());
				if(mapView.getStartCorner().equals(StartCorner.BOTTOM_LEFT) || mapView.getStartCorner().equals(StartCorner.TOP_LEFT)){
					for(int i = 0; i < colSize; i++)
					{
						g.drawString(""+(i+1), getNewX() +rulerMargin+boxWidth/2 + i*boxWidth, 15);
						g.drawString(""+(i+1), getNewX() +rulerMargin+boxWidth/2 + i*boxWidth, rowSize * boxHeight+rulerMargin+boxHeight);
					}	
				}else{
					for(int i = colSize-1; i >= 0; i--)
					{
						g.drawString(""+(i+1), getNewX() +rulerMargin+boxWidth/2 + (colSize-i-1)*boxWidth, 15);
						g.drawString(""+(i+1), getNewX() +rulerMargin+boxWidth/2 + (colSize-i-1)*boxWidth, rowSize * boxHeight+rulerMargin+boxHeight);
					}
					
				}
				
				
			}
			
			if(mapView.getNumRows().getText()!=null){
				if(mapView.getStartCorner().equals(StartCorner.BOTTOM_LEFT) || mapView.getStartCorner().equals(StartCorner.BOTTOM_RIGHT))
				{
					for(int j = rowSize-1; j >= 0; j--)
					{
						g.drawString(""+(j+1),0,getNewY() + rulerMargin+boxHeight/2 + (rowSize-j-1)*boxHeight + g.getFontMetrics().getAscent()/2);
						g.drawString(""+(j+1),getNewX()+rulerMargin*2+(colSize)*boxWidth,getNewY() + rulerMargin+boxHeight/2 + (rowSize-j-1)*boxHeight + g.getFontMetrics().getAscent()/2);		
					}	
				}else{

					for(int j = 0; j<rowSize; j++)
					{
						g.drawString(""+(j+1), 0, getNewY() + rulerMargin+boxHeight/2 + j*boxHeight + + g.getFontMetrics().getAscent()/2);
						g.drawString(""+(j+1),getNewX()+rulerMargin*2+(colSize)*boxWidth, getNewY() + rulerMargin+boxHeight/2 + j*boxHeight + g.getFontMetrics().getAscent()/2);
					}
				}
			}
			
		}
		//tableView.populateTableFromList(stockList);	
	}
	
	private void createXAxis(Graphics g){
		if(font == null)
		{
			Font font = new Font(g.getFont().getFontName(), Font.PLAIN, 8);
			g.setFont(font);
		}else{
			g.setFont(font);
		}		
		g.setColor(Color.WHITE);
		g.fillRect(0, 0, canvas.getParent().getWidth(), 20);
		g.setColor(Color.black);
		if(mapView.getNumCols().getText()!=null  && Utils.isInteger(mapView.getNumCols().getText())){
			int colSize = Integer.parseInt(mapView.getNumCols().getText());
			if(mapView.getStartCorner().equals(StartCorner.BOTTOM_LEFT) || mapView.getStartCorner().equals(StartCorner.TOP_LEFT))
			{
				for(int i = 0; i < colSize; i++)
				{
					g.drawString(""+(i+1), getNewX() +rulerMargin+boxWidth/2 + i*boxWidth+g.getFontMetrics().getMaxAdvance(), 10);
				}
			}else{
				for(int i = colSize-1; i >= 0; i--)
				{
					g.drawString(""+(i+1), getNewX() +rulerMargin+boxWidth/2 + (colSize-i-1)*boxWidth+g.getFontMetrics().getMaxAdvance(), 10);
				}
			}
			
		}		
	}
	
	private void createYAxis(Graphics g){
		if(font == null)
		{
			Font font = new Font(g.getFont().getFontName(), Font.PLAIN, 8);
			g.setFont(font);
		}else{
			g.setFont(font);
		}
		g.setColor(Color.WHITE);
		g.fillRect(0, 0, 20, canvas.getParent().getHeight());
		g.setColor(Color.BLACK);
		if(mapView.getNumRows().getText()!=null && Utils.isInteger(mapView.getNumRows().getText())){
			
			int rowSize = Integer.parseInt(mapView.getNumRows().getText());			
			if(mapView.getStartCorner().equals(StartCorner.BOTTOM_LEFT) || mapView.getStartCorner().equals(StartCorner.BOTTOM_RIGHT))
			{
				for(int j = rowSize-1; j >= 0; j--)
				{
					g.drawString(""+(j+1),3,getNewY() + rulerMargin+boxHeight/2 + (rowSize-j-1)*boxHeight + g.getFontMetrics().getAscent()/2);		
				}	
			}else{

				for(int j = 0; j<rowSize; j++)
				{
					g.drawString(""+(j+1), 3, getNewY() + rulerMargin+boxHeight/2 + j*boxHeight);
				}
			}
			
		}		
	}
	
	private void drawMap(Graphics g) {
		for(PlotRectangle p : plots){
			p.paint(g);			
		}
		if(!movingDone){
			for(PlotRectangle p : selectedPlots){
				 p.paint(g);	
			}	
		}
		
			
	}
	
	
	public void cleanSelection(){
		for(PlotRectangle p : selectedPlots){
			p.stock.setSelectedInMapView(false);
		}
		selectedPlots.clear();				        				
		selectedStocksIndex.clear();
		designSelected = false;
	}
	
	private void addMouseListeners(){
		canvas.addMouseListener(new MouseAdapter() {  
			
		      @Override    
		      public void mousePressed(MouseEvent e) {  
		    	  if(SwingUtilities.isRightMouseButton(e)) {
		    		  	final Point mousePt = e.getPoint();
						JPopupMenu popup = new JPopupMenu();
						JMenuItem deleteStocks = new JMenuItem("Delete Stock");
						int toDelete = getStockIndexAtPoint(mousePt);
						if(toDelete >= 0 && toDelete < stockList.size() && stockList.get(toDelete).getRep() == null){
							deleteStocks.addActionListener(new ActionListener() {
								public void actionPerformed(ActionEvent e) {									 
							    	int toDelete = getStockIndexAtPoint(mousePt);
										stockList.remove(toDelete);
										repaint();
									}
													
							});
							popup.add(deleteStocks);
							popup.show(Canvas.this, e.getX(), e.getY());
						}
												
						return;
				 }
		         pressedStockIndex = getStockIndexAtPoint(e.getPoint());
		         if(selectedStocksIndex.size() == 0){
		        	 for(PlotRectangle p : selectedPlots){
			        	 p.stock.setSelectedInMapView(false);
			         } 
		        	 selectedPlots.clear();
		         }else{
		        	 selectedPlots.clear();
		        	 for(int stockIndex : selectedStocksIndex){
		        		 int plotIndex = stockIndex_plotIndex.get(stockIndex);
		        		 PlotRectangle plot = plots.get(plotIndex);
		        		 selectedPlots.add(plot);
		        	 }
		         }
		         mouseHeld = true;
		         
		      }  
		      public void mouseReleased(MouseEvent e) { 
		    	  Point mousePt = e.getPoint(); 
		    	  int releasedStockIndex = getStockIndexAtPoint(mousePt);
		    	  if(pressedStockIndex == releasedStockIndex && pressedStockIndex >=0 ){
		    		  // selecting		    		 				      
				      if(releasedStockIndex < stockList.size() && releasedStockIndex >= 0){
				    	    int plotIndex = stockIndex_plotIndex.get(releasedStockIndex);
				        	//System.out.println("plot - " + plotIndex + " " + plots.get(plotIndex).stock.getStockName());
				        	PlotRectangle plot = plots.get(plotIndex);
				        	if(plot.stock.isSelectedInMapView()){				        		
				        		if(plot.stock.getRep()!=null && !plot.stock.getRep().equals("null")){
				        			cleanSelection();
				        		}else{
				        			plot.stock.setSelectedInMapView(false);
				        			selectedStocksIndex.remove((Integer) releasedStockIndex);
				        		}
				        		
				        	}else{
				        		if(plot.stock.getRep()!=null && !plot.stock.getRep().equals("null")){
				        			cleanSelection();
				        			List<Integer> stockIndexesWithSameRep = rep_stockIndex.get(plot.stock.getRep());	
				        			Collections.sort(stockIndexesWithSameRep);
				        			for(int sindex : stockIndexesWithSameRep){
				        				PlotRectangle p = plots.get(stockIndex_plotIndex.get(sindex));
				        				p.stock.setSelectedInMapView(true);
				        				selectedStocksIndex.add(sindex);
					        			
				        			}
				        			designSelected = true;
				        		}else{
				        			if(designSelected){
				        				cleanSelection();
				        			}
				        			plot.stock.setSelectedInMapView(true);
					        		if(!selectedStocksIndex.contains(releasedStockIndex))
					        		{
					        			selectedStocksIndex.add(releasedStockIndex);
					        		}
				        			
				        		}				        		
				        	}		        		        
				        	repaint();
				        }  
		    		  
		    	  }else{
		    		  
		    		  // move selected stocks to the end of list;
		    		  if(releasedStockIndex >= stockList.size()-1){		    			  
		    			  for(int i : selectedStocksIndex){
		    				  //add to the end based on the order of selection
			    			  stockList.add(stockList.get(i));
			    		  }
		    			  
		    			  Collections.sort(selectedStocksIndex);
		    			  int deletedCount = 0;
		    			  for(int i : selectedStocksIndex){
		    				  //delete from original index
			    			  stockList.remove(i-deletedCount);
			    			  deletedCount++;
			    		  }
		    		   
		    		  }else{
		    			  int insertedIndex = -1;
		    			  if(releasedStockIndex >= 0  && releasedStockIndex < stockList.size()-1)
		    			  { 
		    				  if(designSelected && releasedStockIndex!=0){
		    					  int lastStockIndex = releasedStockIndex - 1;
		    					  int nextStockIndex = releasedStockIndex + 1;
		    					  if(stockList.get(lastStockIndex).getRep() !=null 
		    							  && stockList.get(nextStockIndex).getRep() !=null
		    							  && stockList.get(lastStockIndex).getRep().equals(stockList.get(nextStockIndex).getRep())){
		    						  cleanSelection();
		    						  JOptionPane.showMessageDialog(null, "No nested replication allowed");
		    						  return;
		    					  }		    					  
		    				  }
		    				  insertedIndex = releasedStockIndex;
		    				  ArrayList<PlantingRow> tempList = new  ArrayList<PlantingRow>();		    			  
			    			  for(int i : selectedStocksIndex){		    				  
			    				  //save to templeList based on the order of selection
			    				  tempList.add(stockList.get(i));
				    		  }
			    			  
			    			  Collections.sort(selectedStocksIndex);
			    			  int deletedCount = 0;
			    			  for(int i : selectedStocksIndex){		    				  
			    				  //remove from list
				    			  stockList.remove(i-deletedCount);
				    			  deletedCount++;
				    		  }
			    			  
			    			  int insertedCount = 0;
			    			  for(PlantingRow row : tempList){
			    				  //insert based on the order of selection
				    			  stockList.add(insertedIndex+insertedCount,row);
				    			  insertedCount++ ;
				    		  }
		    			  }
		    			  		    			 
		    			 
		    		  }		    			    
		    		  selectedStocksIndex.clear(); 
		    		  designSelected = false;
		    		  movingDone = true;
		    		  repaint();
		    	  }
	    		  mouseHeld = false;	    		  
		      }  
		    });    
	}
	private void addMotionListeners() {
		canvas.addMouseMotionListener(new MouseAdapter() {
			@Override
			public void mouseDragged(MouseEvent mouseEvent) {
				movingDone = false;
				if(pressedStockIndex != getStockIndexAtPoint(mouseEvent.getPoint())){				
				  int distant = 0;				  
				  for(PlotRectangle p : selectedPlots){
					  p.x = mouseEvent.getX()+distant;
					  p.y = mouseEvent.getY()+distant;
			    	  distant = distant + 10;			    	 
			    	  repaint();
				  }	
				}
		     }   
			
			@Override
			public void mouseMoved(MouseEvent mouseEvent) {
				int stockIndex = getStockIndexAtPoint(mouseEvent.getPoint());
				if(stockIndex < stockList.size() && stockIndex >= 0){
					canvas.setToolTipText(stockList.get(stockIndex).toString());
				}else{
					canvas.setToolTipText(null);
				}
			}
		});
	}
	
	private int getStockIndexAtPoint(Point p) {
		p.x = p.x - (getNewX()+rulerMargin);
		p.y = p.y - (getNewY()+rulerMargin);		
		int totalStocks = stockList.size();
		int rowSize = Integer.parseInt(mapView.getNumRows().getText());
		int colSize = Integer.parseInt(mapView.getNumCols().getText());
		int vGap = (int) (canvas.getHeight()*getScale()/(5.0*rowSize)), hGap = (int) (canvas.getWidth()*getScale()/(colSize*10.0));
		int boxHeight = (int) ((canvas.getHeight()*getScale() - vGap)/rowSize);
		int boxWidth = (int) ((canvas.getWidth()*getScale() - hGap)/colSize);
		
		
		int coorY = 0;
		int coorX = 0;
		if(mapView.getStartCorner().equals(StartCorner.BOTTOM_LEFT)){
			coorY = rowSize - (p.y-vGap)/boxHeight;
			coorX = (p.x-hGap)/boxWidth + 1;
			if(p.x > (coorX)*boxWidth || p.y > (rowSize-coorY+1)*boxHeight || p.x < hGap || p.y < vGap) {
				return -1;
			}
		}else if(mapView.getStartCorner().equals(StartCorner.TOP_LEFT)){
			coorY = (p.y-vGap)/boxHeight + 1;
			coorX = (p.x-hGap)/boxWidth + 1;
			if(p.x > (coorX)*boxWidth || p.y > coorY *boxHeight || p.x < hGap || p.y < vGap) {
				return -1;
			}
		}else if(mapView.getStartCorner().equals(StartCorner.BOTTOM_RIGHT)){
			coorY = rowSize - (p.y-vGap)/boxHeight;
			coorX = colSize - (p.x-hGap)/boxWidth;
			if(p.x > (colSize-coorX+1)*boxWidth || p.y > (rowSize-coorY+1)*boxHeight || p.x < hGap || p.y < vGap) {
				return -1;
			}
		}else if(mapView.getStartCorner().equals(StartCorner.TOP_RIGHT)){
			coorY = (p.y-vGap)/boxHeight + 1;
			coorX = colSize - (p.x-hGap)/boxWidth;
			if(p.x > (colSize-coorX+1)*boxWidth || p.y > coorY *boxHeight || p.x < hGap || p.y < vGap) {
				return -1;
			}
		}
		
		int number = getStockIndexByColRow(coorX, coorY);		
		if(number > totalStocks)		
			return -1;
		else
			return number;
	}
	
	private int getStockIndexByColRow(int coorX, int coorY){
		int rowSize = Integer.parseInt(mapView.getNumRows().getText());
		int colSize = Integer.parseInt(mapView.getNumCols().getText());
		int stockIndex = 0;
		if(mapView.getNumRows().getText()!=null){
			if(mapView.getStartCorner().equals(StartCorner.BOTTOM_LEFT) || mapView.getStartCorner().equals(StartCorner.BOTTOM_RIGHT))
			{
				switch(mapView.getLayoutOption()){
				//4 5 6
				//1 2 3
				case 0: 						 
					 double a = 1;
					 double b = (double)(colSize * (coorY-1))/((double)coorY);			 
					 stockIndex = (int) ((a * coorX ) + (b * coorY))-1;
					 break;
					 
				//8	7 6 5
			    //1 2 3 4
				case 1:
					 a = coorY%2 == 0 ? -1 : 1;
					 b = a == -1 ? ((double)(colSize * coorY - a))/((double)coorY) : ((double)(colSize * coorY - a*colSize))/((double)coorY); 
					 stockIndex = (int) ((a * coorX ) + (b * coorY))-1;			
					 break;	
					 
				//3 6 9
				//2 5 8
				//1 4 7
				case 2:
					b = 1;
					a = ((double)rowSize * (coorX -1))/((double) coorX);
					stockIndex = (int) ((a * coorX ) + (b * coorY))-1;	
					break;
					
				//3 4 7
				//2 5 8
				//1 6 9
				case 3:
					b = coorX%2 == 0 ? -1 : 1;
					a = b == -1?((double)(rowSize * coorX - b))/((double)coorX) : ((double)(rowSize * (coorX - 1)))/((double)coorX);
					stockIndex = (int) ((a * coorX ) + (b * coorY))-1;	
					break;			
				}
			}else if(mapView.getStartCorner().equals(StartCorner.TOP_LEFT) || mapView.getStartCorner().equals(StartCorner.TOP_RIGHT)){
				switch(mapView.getLayoutOption()) {				
				//1 2 3
				//4 5 6
				case 0: 
						stockIndex = (coorX - colSize) + (colSize * coorY) - 1;				
						break;						
			    //1 2 3 4
				//5	6 7 8
				case 1: 
						int a = coorY%2 == 0 ? -1 : 1;
						int bCoorY = a==-1? colSize * coorY + 1 : colSize * coorY - colSize;
						stockIndex = a*coorX + bCoorY - 1;
						break;				
				//1 4 7
				//2 5 8
				//3 6 9
				case 2: 
						stockIndex = rowSize*coorX-(rowSize - coorY) - 1 ;
						break;
				//1 6 7
				//2 5 8
				//3 4 9
				case 3: 
					 	int b = coorX%2 == 0 ? -1 : 1;
					 	int aCoorX = b==-1? rowSize * coorX +1 : rowSize * coorX - rowSize;
						stockIndex = aCoorX + b*coorY - 1;
						break;
				}				
			}
		}		
		
		return stockIndex;
		
	}
	
	public void colorPlots(){
		for(PlotRectangle p : selectedPlots){
			if(p.stock !=null && (p.stock.getRep() == null || p.stock.getRep().equals("null"))){
				p.stock.setPlotColor(mapView.getSelectedColor());
				tableView.getTag_Color().put(p.stock.getTag(), mapView.getSelectedColor());
			}      	 
        }
		cleanSelection();
	}
	public void selectPlotsBySearch(String searchBy, String searchValue){
		cleanSelection();
		if(searchBy.equals("Accession")){
			int index = -1;
			for(PlotRectangle p : plots){
				index++;
				if(p.stock !=null && (p.stock.getRep() == null || p.stock.getRep().equals("null"))){
					if(p.stock.getAccession().contains(searchValue)){
						p.stock.setSelectedInMapView(true);
						selectedStocksIndex.add(p.indexInStockList);
						PlotRectangle copy = new PlotRectangle(p.x,p.y,p.width, p.height,null, -1,-1);
	        			copy.moving=true;
					}
				}
			}
		}else if(searchBy.equals("Pedigree")){
			int index = -1;
			for(PlotRectangle p : plots){
				index++;
				if(p.stock !=null && (p.stock.getRep() == null || p.stock.getRep().equals("null"))){
					if(p.stock.getPedigree().contains(searchValue)){
						p.stock.setSelectedInMapView(true);
						selectedStocksIndex.add(p.indexInStockList);
						PlotRectangle copy = new PlotRectangle(p.x,p.y,p.width, p.height,null, -1,-1);
	        			copy.moving=true;
					}
				}
			}
			
		}else if(searchBy.equals("StockName")){
			int index = -1;
			for(PlotRectangle p : plots){
				index++;
				if(p.stock !=null && (p.stock.getRep() == null || p.stock.getRep().equals("null"))){
					if(p.stock.getStockName().contains(searchValue)){
						p.stock.setSelectedInMapView(true);
						selectedStocksIndex.add(p.indexInStockList);					
						PlotRectangle copy = new PlotRectangle(p.x,p.y,p.width, p.height,null, -1,-1);
	        			copy.moving=true;
					}
				}
			}			
		}
	}
	public void assignDefaultCoord(){
		mapView.updateDimensions();
		int totalStocks = stockList.size();
		int rows = Integer.parseInt(mapView.getNumRows().getText());
		int cols = Integer.parseInt(mapView.getNumCols().getText());
		int  number = 0;
		for(int i=0; i<cols; i++){
			for(int j=0; j<rows; j++) {
				number = getStockIndexByColRow(i, j);
				if(number <= totalStocks) {
					stockList.get(number-1).setCoordinates(i, j);
				}
				
			}
		}
		
	} 
	
	public List<PlantingRow> getStockList() {
		return stockList;
	}
	public void setStockList(List<PlantingRow> stockList) {
		this.stockList = stockList;
	}
	public MapView getMapView() {
		return mapView;
	}
	public void setMapView(MapView mapView) {
		this.mapView = mapView;
	}
	public TableView getTableView() {
		return tableView;
	}
	public void setTableView(TableView tableView) {
		this.tableView = tableView;
	}
	

}
