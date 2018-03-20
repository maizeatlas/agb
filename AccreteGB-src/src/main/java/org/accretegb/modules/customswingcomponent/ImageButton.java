package org.accretegb.modules.customswingcomponent;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.ImageIcon;
import javax.swing.JButton;

public class ImageButton extends JButton {
	
	private ImageIcon image;
	private ImageIcon hoverImage;
	private MouseAdapter imageChangeAdapter;
	
	public ImageIcon getImage() {
		return image;
	}
	public void setImage(ImageIcon image) {
		this.image = image;
		setIcon(this.image);
	}
	public ImageIcon getHoverImage() {
		return hoverImage;
	}
	public void setHoverImage(ImageIcon hoverImage) {
		this.hoverImage = hoverImage;
		addListener();
	}
	public ImageButton(ImageIcon image) {
		super(image);
		this.image = image;
	}
	public ImageButton(String imageName) {
		image = new ImageIcon(this.getClass().getClassLoader().getResource("images/" + imageName));
		setImage(image);
	}
	public ImageButton(ImageIcon image, ImageIcon hoverImage) {
		super(image);
		this.image = image;
		setHoverImage(hoverImage);
	}
	public ImageButton(String imageName, String hoverImageName) {
		setImage(imageName);
		setHoverImage(hoverImageName);
	}
	public void setImage(String imageName) {
		setImage(new ImageIcon(this.getClass().getClassLoader().getResource("images/" + imageName)) );
	}
	public void setHoverImage(String hoverImageName) {
		setHoverImage(new ImageIcon(this.getClass().getClassLoader().getResource("images/" + hoverImageName)) );
	}
	private void addListener() {
		if(imageChangeAdapter == null) {
			imageChangeAdapter = new MouseAdapter() {
				public void mouseEntered(MouseEvent arg0) {
					if(isEnabled()) {
						setIcon(getHoverImage());
					}
				}
				public void mouseExited(MouseEvent arg0) {
					setIcon(getImage());	
				}
			};
			addMouseListener(imageChangeAdapter);
		}
	}
}