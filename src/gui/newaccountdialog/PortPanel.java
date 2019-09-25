package gui.newaccountdialog;

import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import gui.FrameManager;

/**
 * Panel for ports in NewAccountDialog
 */
public class PortPanel extends JPanel {
	private JLabel separator;
	private JTextField portTextField;
	private JCheckBox ssl;
	private JCheckBox tls;

	public PortPanel(String protocol) {
		super();
		this.setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
		this.setBorder(new EmptyBorder(0, 0, 11, 90));
		separator = new JLabel(":");
		separator.setBorder(new EmptyBorder(0, 0, 0, 10));
		portTextField = new JTextField(FrameManager.getLanguageProperty("serverPanel.port"));
		portTextField.setHorizontalAlignment(SwingConstants.LEFT);
		portTextField.setMaximumSize(new Dimension(45, 25));
		portTextField.setPreferredSize(new Dimension(45, 25));
		portTextField.setMinimumSize(new Dimension(45, 25));
		portTextField.setEditable(false);
		ssl = new JCheckBox(FrameManager.getLanguageProperty("portPanel.ssl"));
		ssl.setBorder(new EmptyBorder(0, 10, 0, 10));
		ssl.setSelected(true);
		tls = new JCheckBox(FrameManager.getLanguageProperty("portPanel.tls"));
		tls.setBorder(new EmptyBorder(0, 0, 0, 10));
		portTextField.addMouseListener(new MouseAdapter() {

			@Override
			public void mouseClicked(MouseEvent e) {
				portTextField.setText("");
			}
		});
		portTextField.getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void insertUpdate(DocumentEvent e) {

				if (protocol.equals("POP")) {
					if (portTextField.getText().equals("995")) {
						ssl.setSelected(true);
					} else {
						ssl.setSelected(false);
					}
				} else if (protocol.equals("IMAP")) {
					if (portTextField.getText().equals("993")) {
						ssl.setSelected(true);
					} else {
						ssl.setSelected(false);
					}
				} else {
					if (portTextField.getText().equals("465")) {
						ssl.setSelected(true);
					} else if (portTextField.getText().equals("587")) {
						ssl.setSelected(false);
						tls.setSelected(true);
					} else {
						ssl.setSelected(false);
						tls.setSelected(false);
					}
				}
			}

			public void removeUpdate(DocumentEvent e) {
			}

			public void changedUpdate(DocumentEvent e) {
			}
		});

		this.add(separator);
		this.add(portTextField);
		this.add(ssl);
		this.add(tls);
	}

	public JLabel getSeparator() {
		return separator;
	}

	public JTextField getPortTextField() {
		return portTextField;
	}

	public JCheckBox getPortSsl() {
		return ssl;
	}

	public JCheckBox getPortTls() {
		return tls;
	}
}
