package org.accretegb.modules.germplasm.planting;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
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
import org.accretegb.modules.hibernate.MateMethod;
import org.accretegb.modules.hibernate.dao.MateDAO;
import org.accretegb.modules.hibernate.dao.MateMethodConnectDAO;
import org.accretegb.modules.hibernate.dao.MateMethodDAO;
import org.accretegb.modules.util.LoggerUtils;
import org.apache.commons.lang.StringUtils;
import org.hibernate.HibernateException;

public class MatingPlanSelector extends JComboBox {

	private static final long serialVersionUID = 1L;
	private List<MateMethodConnect> matingPlanList;
	private JPanel parentPanel;
	
	@Override
	public Object getSelectedItem() {
		if(getSelectedIndex() == 0) {
			return null;
		}
		return (String)super.getSelectedItem();
	}
	
	public MateMethodConnect getSelectedMateConnect() {
		if(getSelectedIndex() == 0) {
			return null;
		}
		return  matingPlanList.get(getSelectedIndex()-1);
	}
	
	public MatingPlanSelector(JPanel parentPanel) {
		this.setParentPanel(parentPanel);
		initalize();
	}

	private void initalize() {
		populateMatingPlans();
		addListener();
	}

	private void addListener() {
		addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(getSelectedIndex() == getItemCount()-1) {
					setSelectedIndex(0);
					new NewMatingPlanPanel(MatingPlanSelector.this);
				}
			}
		});
	}

	public void populateMatingPlans() {

		matingPlanList =  MateMethodConnectDAO.getInstance().getAllMateConnects();
		String[] matingPlanValues = new String[matingPlanList.size()+2];
		matingPlanValues[0] = "Select mating plan";
		for(int counter=0; counter<matingPlanList.size(); counter++) {
			MateMethodConnect plan = matingPlanList.get(counter);
			String mateMethodConnectString = plan.getMate().getMatingType() + " - " + plan.getMate().getMateRole();
			if(plan.getMateMethod() != null) {
				mateMethodConnectString = plan.getMateMethod().getMateMethodName() + " (" + mateMethodConnectString + ")";
			}
			matingPlanValues[counter+1] = mateMethodConnectString;
		}
		matingPlanValues[matingPlanValues.length-1] = "Add new..";
		setModel(new DefaultComboBoxModel(matingPlanValues));

	}

	public JPanel getParentPanel() {
		return parentPanel;
	}

	public void setParentPanel(JPanel parentPanel) {
		this.parentPanel = parentPanel;
	}
}

class NewMatingPlanPanel extends JPanel {

	private static final long serialVersionUID = 1L;
	JTextField role;
	JTextField type;
	JTextField method;
	JTextField user;
	CustomCalendar date;
	JTextArea desc;
	JLabel roleLabel;
	JLabel typeLabel;
	JLabel methodLabel;
	JLabel userLabel;
	JLabel dateLabel;
	JLabel descLabel;
	MatingPlanSelector matingPlanSelector;
	
	public NewMatingPlanPanel(MatingPlanSelector matingPlanSelector) {
		this.matingPlanSelector = matingPlanSelector;
		initialize();
	}
	
	private void initialize() {
		initializeFields();
		showForm();
	}
	
	private void addMatingPlanToDatabase(String type, String role, String method) {
		type = validate(type);
		role = validate(role);
		method = validate(method);		
		try {
			List resultSet = MateDAO.getInstance().findMate(type, role) ;
			Mate mate;
			boolean alreadyExisted = true;
			if(resultSet.size() == 0) {				
				mate = MateDAO.getInstance().insert(type, role);
				alreadyExisted = false;			
			} else {
				mate = (Mate) resultSet.get(0);
			}

			MateMethod mateMethod = null;
			if(method != null) {
				mateMethod = MateMethodDAO.getInstance().findMateMethod(method);
				if( mateMethod == null) {
					String description = String.valueOf(desc.getText().trim()).equals("")?null:desc.getText().trim();
					String userString = String.valueOf(user.getText().trim()).equals("")?null:user.getText().trim();
					Date dateDefined = date.getCustomDateCalendar().getDate();
					mateMethod = MateMethodDAO.getInstance().insert(method, description, userString, dateDefined);
					alreadyExisted = false;
				} 
			}
			
			MateMethodConnectDAO.getInstance().insertIfNoExist(mate, mateMethod);			
			if(alreadyExisted) {
				JOptionPane.showConfirmDialog((Component) AccreteGBContext.getContext().getBean("plantingChildPanel0"), "<HTML><FONT COLOR = Red>Record already exists.</FONT></HTML>", "Error!", JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE); 
			}		
		} catch (HibernateException ex) {							
			if(LoggerUtils.isLogEnabled())
				LoggerUtils.log(Level.INFO, ex.toString());
			ex.printStackTrace();
		}
	}
	
	private void showForm() {
		boolean valid = false;
		while (!valid) {
			int option = JOptionPane.showConfirmDialog(matingPlanSelector.getParentPanel(), this, "Enter New Plan Information ", JOptionPane.OK_CANCEL_OPTION);
			if(option == JOptionPane.OK_OPTION) {
				if(StringUtils.isNotBlank(role.getText()) && StringUtils.isNotBlank(type.getText())) {
					valid = true;
					addMatingPlanToDatabase(type.getText(), role.getText(), method.getText());
					matingPlanSelector.populateMatingPlans();
				}
				else
					JOptionPane.showConfirmDialog((Component) AccreteGBContext.getContext().getBean("plantingChildPanel0"), "<HTML><FONT COLOR = Red>* marked fields are mandatory.</FONT></HTML>", "Error!", JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE);       
			}
			else break;
		}
	}

	private void initializeFields() {
		setLayout(new MigLayout("insets 0, gap 0"));
		role = new JTextField(17);
		type = new JTextField(17);
		method = new JTextField(17);
		user = new JTextField(17);
		desc = new JTextArea(5, 17);
		date = new CustomCalendar();
		roleLabel = new JLabel("<HTML>Role<FONT COLOR = Red>*</FONT> :</HTML>");
		typeLabel = new JLabel("<HTML>Type<FONT COLOR = Red>*</FONT> :</HTML>");
		methodLabel = new JLabel("Method: ");
		userLabel = new JLabel("User: ");
		descLabel = new JLabel("Description: ");
		dateLabel = new JLabel("Date: ");
		add(typeLabel);
		add(type, "wrap");
		add(roleLabel);
		add(role, "wrap");
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
	
	private String validate(String value) {
		if(StringUtils.isBlank(value)) {
			return null;
		}
		return value.toUpperCase();
	}
	
}