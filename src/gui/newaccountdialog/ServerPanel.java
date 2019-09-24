package gui.newaccountdialog;

import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JPanel;
import javax.swing.JTextField;

import gui.FrameManager;

/**
 * Panel for host addresses in NewAccountDialog
 */
public class ServerPanel extends JPanel {
	JTextField popServer;

	public ServerPanel() {
		super();
		popServer = new JTextField(FrameManager.getLanguageProperty("serverPanel.domain"));
		popServer.setMaximumSize(new Dimension(500, 25));
		popServer.setPreferredSize(new Dimension(250, 25));
		popServer.setMinimumSize(new Dimension(250, 25));
		this.add(popServer);
		popServer.setEditable(false);
		popServer.addMouseListener(new MouseAdapter() {

			@Override
			public void mouseClicked(MouseEvent e) {
				popServer.setText("");
			}
		});
	}

	public JTextField getPopServer() {
		return popServer;
	}
}
