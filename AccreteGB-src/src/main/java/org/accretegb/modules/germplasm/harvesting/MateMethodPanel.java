package org.accretegb.modules.germplasm.harvesting;

import java.awt.Component;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import net.miginfocom.swing.MigLayout;

import org.accretegb.modules.config.AccreteGBContext;
import org.accretegb.modules.customswingcomponent.CustomCalendar;
import org.accretegb.modules.hibernate.Mate;
import org.accretegb.modules.hibernate.MateMethodConnect;
import org.accretegb.modules.hibernate.dao.MateMethodDAO;
import org.accretegb.modules.hibernate.MateMethod;
import org.accretegb.modules.util.LoggerUtils;
import org.apache.commons.lang.StringUtils;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Transaction;
import org.hibernate.criterion.Restrictions;

public class MateMethodPanel extends JPanel {

	JTextField method;
	JTextField user;
	CustomCalendar date;
	JTextArea desc;
	JLabel methodLabel;
	JLabel userLabel;
	JLabel dateLabel;
	JLabel descLabel;
	private FieldGenerated fieldGenerated;

	public MateMethodPanel(FieldGenerated fieldGenerated) {
		this.fieldGenerated = fieldGenerated;
		initialize();
	}

	private void initialize() {
		initializeFields();
		showForm();
	}
	
	private void initializeFields() {
		setLayout(new MigLayout("insets 0, gap 0"));
		method = new JTextField(17);
		user = new JTextField(17);
		desc = new JTextArea(5, 17);
		date = new CustomCalendar();
		methodLabel = new JLabel("<HTML>Method<FONT COLOR = Red>*</FONT> :</HTML>");
		userLabel = new JLabel("User: ");
		descLabel = new JLabel("Description: ");
		dateLabel = new JLabel("Date: ");
		add(methodLabel);
		add(method, "wrap");
		add(userLabel);
		add(user, "wrap");
		add(dateLabel);
		add(date.getCustomDateCalendar(), "wrap");
		add(descLabel);
		JScrollPane scroll = new JScrollPane(desc, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		add(scroll, "wrap, gaptop 2");
	}

	private void showForm() {
		boolean valid = false;
		while (!valid) {
			int option = JOptionPane.showConfirmDialog(fieldGenerated, this, "Enter New Plan Information ", JOptionPane.OK_CANCEL_OPTION);
			if(option == JOptionPane.OK_OPTION) {
				if(StringUtils.isNotBlank(method.getText())) {
					valid = true;
					addMateMethodToDatabase(method.getText());
					fieldGenerated.refreshMethods();
				}
				else
					JOptionPane.showConfirmDialog((Component) AccreteGBContext.getContext().getBean("plantingChildPanel0"), "<HTML><FONT COLOR = Red>* marked fields are mandatory.</FONT></HTML>", "Error!", JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE);       
			}
			else break;
		}
	}
	
	private void addMateMethodToDatabase(String method) {			
		if(method != null) {			
			if(MateMethodDAO.getInstance().findMateMethod(method) == null) {
				String description = desc.getText();
				String userString = user.getText();
				Date dateDefined = date.getCustomDateCalendar().getDate();
				MateMethodDAO.getInstance().insert(method, description, userString, dateDefined);					
			} else {
				JOptionPane.showConfirmDialog(fieldGenerated, "<HTML><FONT COLOR = Red>Record already exists.</FONT></HTML>", "Error!", JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE); 
			}
		}
	}
		
	

}
