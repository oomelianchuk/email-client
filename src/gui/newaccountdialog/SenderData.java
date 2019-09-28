package gui.newaccountdialog;

import java.awt.Color;
import java.awt.Dimension;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.BevelBorder;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;

import data.AccountData;
import gui.FrameManager;

/**
 * Top part of NewAccounyDialog
 *
 */
public class SenderData extends JPanel {
	private JTextField email = new JTextField();
	private JTextField name = new JTextField();

	public SenderData() {
		this.setBorder(new CompoundBorder(new EmptyBorder(10, 10, 10, 10),
				new CompoundBorder(new BevelBorder(BevelBorder.LOWERED),
						new CompoundBorder(new EmptyBorder(10, 10, 10, 10), new TitledBorder(FrameManager.getLanguageProperty("senderData.title"))))));
		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

		JPanel emailPanel = new JPanel();
		emailPanel.setLayout(new BoxLayout(emailPanel, BoxLayout.X_AXIS));
		emailPanel.setBorder(new EmptyBorder(30, 10, 0, 10));
		this.add(emailPanel);
		JLabel emailLable = new JLabel(FrameManager.getLanguageProperty("senderData.email"));
		emailLable.setBorder(new EmptyBorder(0, 0, 10, 10));
		emailPanel.add(emailLable);

		email.setMaximumSize(new Dimension(500, 25));
		email.setMinimumSize(new Dimension(250, 25));
		emailPanel.add(email);

		JPanel namePanel = new JPanel();
		namePanel.setLayout(new BoxLayout(namePanel, BoxLayout.X_AXIS));
		namePanel.setBorder(new EmptyBorder(5, 10, 10, 10));
		this.add(namePanel);
		JLabel nameLable = new JLabel(FrameManager.getLanguageProperty("senderData.name"));
		nameLable.setBorder(new EmptyBorder(10, 0, 10, 50));
		namePanel.add(nameLable);

		name.setMaximumSize(new Dimension(500, 25));
		name.setMinimumSize(new Dimension(250, 25));
		namePanel.add(name);
	}

	public AccountData getAccountDaten() {
		AccountData accountDaten = new AccountData();
		if (email.getText().equals("")) {
			email.setBorder(BorderFactory.createLineBorder(Color.RED, 2));
			accountDaten = null;
		} else {
			accountDaten.setEmail(email.getText());
			accountDaten.setUserName(name.getText());
		}
		return accountDaten;
	}
}
