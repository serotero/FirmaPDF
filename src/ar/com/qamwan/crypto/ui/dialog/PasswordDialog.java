package ar.com.qamwan.crypto.ui.dialog;

import java.awt.Button;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Label;
import java.awt.Panel;
import java.awt.TextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import ar.com.qamwan.crypto.ui.UserIntefaceHelper;

public class PasswordDialog extends Dialog implements ActionListener {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	boolean actionOK = false;
	private Button ok, can;	
	private TextField password = null;
	
	public PasswordDialog(Component parent) {
		super(UserIntefaceHelper.getParentFrame(parent), "KeyStore", true);
		createUI();
		setVisible(true);
	}

	void createUI() {
		setLayout(new FlowLayout());
		password = new TextField(15);
		password.setEchoChar('*');
		password.setText("");
		add(new Label("KeyStore Password :"));
		add(password);
		
		Panel p = new Panel();
		p.setLayout(new FlowLayout());
		p.add(ok = new Button("OK"));
		ok.addActionListener(this);
		p.add(can = new Button("Cancel"));
		can.addActionListener(this);
		add(p);
		pack();
		
		Dimension d = getToolkit().getScreenSize();
		int top = (d.width-p.getWidth())/2;
		int left = (d.height-p.getHeight())/2;
		System.out.println("top:" + top + ", left:" + left);
		setLocation(top,left);
		setLocationRelativeTo(null);
	}

	public void actionPerformed(ActionEvent ae) {
		if (ae.getSource() == ok) {
			actionOK = true;
			setVisible(false);
		} else if (ae.getSource() == can) {
			actionOK = false;
			setVisible(false);
		}
	}
	
	public String getPassword() {		
		return password.getText();
	}
	
	public boolean actionOk() {
		return actionOK;
	}
	
	public void setPassword(TextField password) {
		this.password = password;
	}
}
