package gui;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.border.EmptyBorder;

public class AskPasswordFrame extends JFrame {
	private String password;
	private boolean savePass = false;

	public AskPasswordFrame(String userName) {
		super("Password dialog");
		JPanel contentPane = new JPanel();
		JLabel label = new JLabel("please enter your password for " + userName);
		label.setBorder(new EmptyBorder(10, 10, 10, 10));
		JPanel labelPanel = new JPanel();
		labelPanel.setLayout(new BoxLayout(labelPanel, BoxLayout.X_AXIS));
		labelPanel.add(label);
		JPasswordField passwordField = new JPasswordField();
		passwordField.setMaximumSize(new Dimension(250, 25));
		passwordField.setPreferredSize(new Dimension(250, 25));
		passwordField.setMinimumSize(new Dimension(250, 25));
		JPanel passwordPanel = new JPanel();
		passwordPanel.setLayout(new BoxLayout(passwordPanel, BoxLayout.X_AXIS));
		passwordPanel.add(passwordField);
		JButton ok = new JButton("Submit");
		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
		buttonPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
		buttonPanel.add(ok);
		JCheckBox savePassCheckbox = new JCheckBox("save password");
		ok.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				password = passwordField.getText();
				savePass = savePassCheckbox.isSelected();
				setVisible(false);
			}
		});
		savePassCheckbox.setBorder(new EmptyBorder(10, 10, 10, 10));
		buttonPanel.add(savePassCheckbox);
		contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.Y_AXIS));
		contentPane.add(labelPanel);
		contentPane.add(passwordPanel);
		contentPane.add(buttonPanel);
		this.setContentPane(contentPane);
		this.setPreferredSize(new Dimension(300, 150));
		this.pack();
		this.setVisible(true);
	}

	public void resetPassword() {
		password = null;
		setVisible(true);
	}

	public String getPassword() {
		while (password == null) {
		}
		return password;
	}

	public boolean getSavePass() {
		return savePass;
	}
}
