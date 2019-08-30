package gui.mainframe;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

/**
 * Information part of OpenedMessagePanel. Here will be displayed fields for
 * subject, sender, recipient and date
 */
public class InfoOpenedMessagePanel extends JPanel {
	private JTextField subject;
	private JTextField sender;
	private JTextField to;
	private JLabel date;

	public InfoOpenedMessagePanel(boolean isEnabled) {
		super();
		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		JPanel subjectPanel = new JPanel();
		subjectPanel.setLayout(new BoxLayout(subjectPanel, BoxLayout.X_AXIS));
		JLabel subjectLabel = new JLabel("Subject:");
		subjectLabel.setPreferredSize(new Dimension(60, 20));
		subjectLabel.setBorder(new EmptyBorder(0, 10, 5, 0));

		subject = new JTextField();
		subject.setEnabled(isEnabled);
		subject.setDisabledTextColor(Color.gray);
		subjectPanel.add(subjectLabel);
		subjectPanel.add(subject);
		subjectPanel.setBorder(new EmptyBorder(15, 10, 10, 10));
		this.add(subjectPanel);
		JPanel senderPanel = new JPanel();
		senderPanel.setLayout(new BoxLayout(senderPanel, BoxLayout.X_AXIS));
		JLabel senderLabel = new JLabel("From:");
		senderLabel.setPreferredSize(new Dimension(60, 20));
		senderLabel.setBorder(new EmptyBorder(0, 10, 5, 0));
		sender = new JTextField();
		sender.setDisabledTextColor(Color.gray);
		sender.setEnabled(isEnabled);
		senderPanel.add(senderLabel);
		senderPanel.add(sender);
		senderPanel.setBorder(new EmptyBorder(0, 10, 10, 10));
		this.add(senderPanel);
		JPanel toPanel = new JPanel();
		toPanel.setLayout(new BoxLayout(toPanel, BoxLayout.X_AXIS));
		JLabel toLabel = new JLabel("To:");
		toLabel.setPreferredSize(new Dimension(60, 20));
		toLabel.setBorder(new EmptyBorder(0, 10, 5, 0));
		to = new JTextField();
		to.setDisabledTextColor(Color.gray);
		to.setEnabled(isEnabled);
		toPanel.add(toLabel);
		toPanel.add(to);
		toPanel.setBorder(new EmptyBorder(0, 10, 2, 10));
		this.add(toPanel);
		JPanel datePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		date = new JLabel();
		date.setBorder(new EmptyBorder(0, 10, 0, 10));
		datePanel.add(date);
		this.add(datePanel);
	}

	public String getSubject() {
		return subject.getText();
	}

	public String getSender() {
		return sender.getText();
	}

	public String getTo() {
		return to.getText();
	}

	public void setSubject(String subject) {
		this.subject.setText(subject);

	}

	public void setSender(String sender) {
		this.sender.setText(sender);
	}

	public void setTo(String to) {
		this.to.setText(to);
	}

	public void setDate(String date) {
		this.date.setText(date);
	}
}
