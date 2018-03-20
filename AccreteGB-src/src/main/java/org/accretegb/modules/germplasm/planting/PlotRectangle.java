package org.accretegb.modules.germplasm.planting;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;

public class PlotRectangle extends Rectangle {
		int x,y; 		
		int width,height;
		PlantingRow stock;
		int indexInStockList = -1;
		boolean moving = false;
		
		public PlotRectangle(int x, int y, int width,int height, PlantingRow stock, int index, int startRow) {  
		    this.x = x;  
		    this.y = y;  
		    this.width = width;
		    this.height = height;
		    this.stock = stock;
		    this.indexInStockList = index + startRow;
		  }  
		
		
		public void paint(Graphics g) {
			if(stock != null){			    	
		    	if(!stock.getStockName().equals("Filler")){
		    		 g.setColor(stock.getPlotColor());		 
		    		 //g.setColor(Color.white);	
		    	}else{
		    		g.setColor(Color.gray);
		    	}
		    	if(stock.getRep()!=null && !stock.getRep().equals("null")){
		    		g.setColor(new Color(252, 218, 218));
		    	}
		    	if(stock.isSelectedInMapView()){
				  g.setColor(new Color(184, 207, 229));				  
				}	
		    	g.fillRect(x, y, width, height);
		    	g.setColor(Color.black);
		    	g.drawRect(x, y, width, height); 
		    	// make label stay in the rectangle 
		    	FontMetrics fm = g.getFontMetrics();		    	
		    	g.setColor(Color.black);
		    	//g.drawString(String.valueOf(Integer.parseInt(stock.getRow())), x+2, y+fm.getAscent());	
		    	g.drawString(String.valueOf(this.indexInStockList), x+2, y+fm.getAscent());
				    
		    }else{
		    	if(moving){
		    		g.setColor(new Color(184, 207, 229));
		    		g.fillRect(x, y, width, height);
			    	g.setColor(Color.black);
			    	g.drawRect(x, y, width, height);  
		    	}else{
		    		g.setColor(Color.black);
			    	g.fillRect(x, y, width, height);
			    	g.drawRect(x, y, width, height); 
		    	}
		    	
		    	
			    
		    }	
			
		   
		   
		}
		
}
