package gui.newaccountdialog;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;

import data.AccountData;
import gui.FrameManager;

/**
 * Dialog message to create new user
 */
public class NewAccountDialog extends JFrame {
	AbsenderDaten absenderDaten;
	private JLabel errorMessage;
	private JTextField userName;
	private JTextField userPass;
	private ServerPanel popServerPanel;
	private ServerPanel imapServerPanel;
	private ServerPanel smtpServerPanel;
	private JCheckBox savePass;
	private PortPanel popPortPanel;
	private PortPanel imapPortPanel;
	private PortPanel smtpPortPanel;
	private JCheckBox runInBackground;

	public NewAccountDialog() {
		super("Account Erstellen");
		JPanel contentPanel = new JPanel();
		contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
		absenderDaten = new AbsenderDaten();

		// down part
		JPanel contentDownPart = new JPanel();
		contentDownPart.setLayout(new BoxLayout(contentDownPart, BoxLayout.Y_AXIS));
		JPanel errMessagePanel = new JPanel();
		errMessagePanel.setLayout(new BoxLayout(errMessagePanel, BoxLayout.X_AXIS));
		errMessagePanel.setBorder(new EmptyBorder(8, 0, 8, 8));
		errorMessage = new JLabel();
		errorMessage.setBorder(new EmptyBorder(10, 10, 10, 10));
		// errorMessage.setVisible(false);
		errMessagePanel.add(errorMessage);
		contentDownPart.add(errMessagePanel);
		// labels
		JPanel downPart = new JPanel();
		downPart.setLayout(new BoxLayout(downPart, BoxLayout.X_AXIS));
		downPart.setBorder(new EmptyBorder(10, 10, 10, 10));

		contentDownPart.add(downPart);
		JPanel labels = new JPanel();
		labels.setLayout(new BoxLayout(labels, BoxLayout.Y_AXIS));
		String[] labelNames = { "Benutzererkennung: ", "Passwort: ", "POP: ", "IMAP: ", "SMTP: " };
		for (String labelName : labelNames) {
			JLabel label = new JLabel(labelName);
			label.setBorder(new EmptyBorder(0, 0, 20, 0));
			label.setFont(new Font("TimesRoman", Font.PLAIN, 14));
			labels.add(label);
		}
		downPart.add(labels);

		// text fields
		JPanel textFileds = new JPanel();
		textFileds.setLayout(new BoxLayout(textFileds, BoxLayout.Y_AXIS));
		downPart.add(textFileds);

		JPanel userNamePanel = new JPanel();
		userName = new JTextField();
		userName.setMaximumSize(new Dimension(500, 25));
		userName.setPreferredSize(new Dimension(250, 25));
		userName.setMinimumSize(new Dimension(250, 25));
		userNamePanel.add(userName);

		JPanel userPassPanel = new JPanel();
		userPass = new JPasswordField();
		userPass.setMaximumSize(new Dimension(500, 25));
		userPass.setPreferredSize(new Dimension(250, 25));
		userPass.setMinimumSize(new Dimension(250, 25));
		userPassPanel.add(userPass);

		popServerPanel = new ServerPanel();
		imapServerPanel = new ServerPanel();
		smtpServerPanel = new ServerPanel();

		textFileds.add(userNamePanel);
		textFileds.add(userPassPanel);
		textFileds.add(popServerPanel);
		textFileds.add(imapServerPanel);
		textFileds.add(smtpServerPanel);

		// right panel
		JPanel restRight = new JPanel();
		restRight.setLayout(new BoxLayout(restRight, BoxLayout.Y_AXIS));

		downPart.add(restRight);
		// check boxes panel
		CheckBoxPanel checkBoxes = new CheckBoxPanel();
		checkBoxes.addCheckBox("POP");
		checkBoxes.addCheckBox("IMAP");
		checkBoxes.addCheckBox("SMTP");
		// save pass panel
		JPanel savePassPanel = new JPanel();
		savePassPanel.setLayout(new BoxLayout(savePassPanel, BoxLayout.X_AXIS));
		savePassPanel.setBorder(new EmptyBorder(0, 0, 20, 50));
		savePass = new JCheckBox("Passwort speichern");
		savePassPanel.add(savePass);
		savePass.setBorder(new EmptyBorder(0, 0, 0, 20));

		// ports
		popPortPanel = new PortPanel("pop");
		imapPortPanel = new PortPanel("imap");
		smtpPortPanel = new PortPanel("smtp");

		// Action on check boxes
		checkBoxes.addEnableActionOnCheckBox("POP", popPortPanel.getPortTextField(), popServerPanel.getPopServer());
		checkBoxes.addEnableActionOnCheckBox("IMAP", imapPortPanel.getPortTextField(), imapServerPanel.getPopServer());
		checkBoxes.addEnableActionOnCheckBox("SMTP", smtpPortPanel.getPortTextField(), smtpServerPanel.getPopServer());

		restRight.add(checkBoxes);
		restRight.add(savePassPanel);
		restRight.add(popPortPanel);
		restRight.add(imapPortPanel);
		restRight.add(smtpPortPanel);
		JPanel confirmPanel = new JPanel();
		confirmPanel.setLayout(new BoxLayout(confirmPanel, BoxLayout.X_AXIS));
		JButton sendKeyes = new JButton("OK");
		sendKeyes.setMaximumSize(new Dimension(400, 30));
		sendKeyes.setPreferredSize(new Dimension(400, 30));
		sendKeyes.setMinimumSize(new Dimension(400, 30));
		sendKeyes.setAlignmentX(CENTER_ALIGNMENT);
		sendKeyes.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				sendKeys();
			}
		});
		runInBackground = new JCheckBox("check for messages in background");
		runInBackground.setBorder(new EmptyBorder(0, 0, 0, 30));
		confirmPanel.setBorder(new EmptyBorder(0, 0, 10, 0));
		confirmPanel.add(runInBackground);
		confirmPanel.add(sendKeyes);
		contentPanel.add(absenderDaten);
		contentPanel.add(contentDownPart);
		contentPanel.add(confirmPanel);
		this.add(contentPanel);
	}

	private void sendKeys() {
		JTextField popPort = popPortPanel.getPortTextField();
		JTextField imapPort = imapPortPanel.getPortTextField();
		JTextField smtpPort = smtpPortPanel.getPortTextField();
		JTextField popServer = popServerPanel.getPopServer();
		JTextField imapServer = imapServerPanel.getPopServer();
		JTextField smtpServer = smtpServerPanel.getPopServer();
		popServer.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));
		imapServer.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));
		smtpServer.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));
		popPort.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));
		imapPort.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));
		smtpPort.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));
		boolean popEmpty = popServer.getText().equals("") | popServer.getText().equals("<domain>");
		boolean imapEmpty = imapServer.getText().equals("") | imapServer.getText().equals("<domain>");
		boolean smtpEmpty = smtpServer.getText().equals("") | smtpServer.getText().equals("<domain>");
		hideErrorMessage();
		if (popEmpty & imapEmpty & smtpEmpty) {
			popServer.setBorder(BorderFactory.createLineBorder(Color.RED, 2));
			imapServer.setBorder(BorderFactory.createLineBorder(Color.RED, 2));
			smtpServer.setBorder(BorderFactory.createLineBorder(Color.RED, 2));
			popPort.setBorder(BorderFactory.createLineBorder(Color.RED, 2));
			imapPort.setBorder(BorderFactory.createLineBorder(Color.RED, 2));
			smtpPort.setBorder(BorderFactory.createLineBorder(Color.RED, 2));
			showErrorMessage("Geben Sie bitte Daten für zumidest einen Protokol");
		} else if (!smtpEmpty & (smtpPort.getText().equals("") | smtpPort.getText().equals("<port>"))) {
			smtpPort.setBorder(BorderFactory.createLineBorder(Color.RED, 2));
			showErrorMessage("Geben Sie bitte Port für SMTP");
		} else {
			AccountData accountDaten = absenderDaten.getAccountDaten();
			if (accountDaten != null) {
				accountDaten.set("userAuth", userName.getText());
				accountDaten.set("password", userPass.getText());
				accountDaten.setSavePass(savePass.isSelected());
				if (!popEmpty) {
					accountDaten.set("popServer", popServer.getText());
					if (!(popPort.getText().equals("") | popPort.getText().equals("<port>"))) {
						accountDaten.set("popPort", popPort.getText());
						accountDaten.set("sslPop", Boolean.toString(popPortPanel.getPortSsl().isSelected()));
						accountDaten.set("tlsPop", Boolean.toString(popPortPanel.getPortTls().isSelected()));
					}
				}
				if (!imapEmpty) {
					accountDaten.set("imapServer", imapServer.getText());
					if (!(imapPort.getText().equals("") | imapPort.getText().equals("<port>"))) {
						accountDaten.set("imapPort", imapPort.getText());
						accountDaten.set("sslImap", Boolean.toString(imapPortPanel.getPortSsl().isSelected()));
						accountDaten.set("tlsImap", Boolean.toString(imapPortPanel.getPortTls().isSelected()));
					}
				}
				if (!smtpEmpty) {
					accountDaten.set("smtpServer", smtpServer.getText());
					accountDaten.set("smtpPort", smtpPort.getText());
					accountDaten.set("sslSmtp", Boolean.toString(smtpPortPanel.getPortSsl().isSelected()));
					accountDaten.set("tlsSmtp", Boolean.toString(smtpPortPanel.getPortTls().isSelected()));
				}
				accountDaten.set("runInBackground", Boolean.toString(runInBackground.isSelected()));
				new FrameManager().createAccount(accountDaten);
			}
		}
	}

	public void showErrorMessage(String message) {
		errorMessage.setBorder(
				new CompoundBorder(BorderFactory.createLineBorder(Color.RED, 2), new EmptyBorder(10, 10, 10, 10)));
		errorMessage.setText(message);
	}

	public void hideErrorMessage() {
		errorMessage.setBorder(new EmptyBorder(10, 10, 10, 10));
		errorMessage.setText("");
	}

	public void showFrame() {
		this.setMinimumSize(new Dimension(500, 500));
		this.setResizable(false);
		this.pack();
		this.setVisible(true);
	}
}
