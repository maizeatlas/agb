package org.accretegb.modules.customswingcomponent;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.apache.commons.lang.StringUtils;

import java.awt.*;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
 
public class TextField extends JTextField {
 
    private Font originalFont;
    private Color originalForeground = Color.BLACK;
    private Color placeholderForeground = new Color(160, 160, 160);
    private boolean textWrittenIn;
    private String placeHolder;
 
    public TextField(int columns) {
        super(columns);
    }
 
    public Color getPlaceholderForeground() {
        return placeholderForeground;
    }
 
    public void setPlaceholderForeground(Color placeholderForeground) {
        this.placeholderForeground = placeholderForeground;
    }
 
    public boolean isTextWrittenIn() {
        return textWrittenIn;
    }
 
    public void setTextWrittenIn(boolean textWrittenIn) {
        this.textWrittenIn = textWrittenIn;
    }
 
    public void setPlaceholder(final String text) {
 
        this.customizeText(text);
        placeHolder = text;
        this.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) {
                warn();
            }
 
            public void removeUpdate(DocumentEvent e) {
                warn();
            }
 
            public void changedUpdate(DocumentEvent e) {
                warn();
            }
 
            public void warn() {
            	if (getActualText().trim().length() != 0) {
                    setFont(originalFont);
                    setForeground(originalForeground);
                    setTextWrittenIn(true);
                } 
            }
        });
 
        this.addFocusListener(new FocusListener() {
            public void focusGained(FocusEvent e) {
                if (!isTextWrittenIn()) {
                    setText("");
                } 
            }
            public void focusLost(FocusEvent e) {
                if (getText().trim().length() == 0) {
                    customizeText(text);
                }
            } 
        }); 
    }
 
    public String getPlaceholder(){
    	return placeHolder;
    }
    public void customizeText(String text) {
        setText(text);
        setForeground(getPlaceholderForeground());
        setTextWrittenIn(false);
    }
    
    private String getActualText() {
    	return super.getText();
    }
    
    @Override
    public String getText() {
    	if(isTextWrittenIn()) {
    		return super.getText();
    	} else {
    		return StringUtils.EMPTY;
    	}
    }
   
 
}