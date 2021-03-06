package actionlisteners;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JOptionPane;

import data.GlobalDataContainer;
import gui.FrameManager;
import gui.mainframe.TreeClickListener;

public class ChangePasswordTask implements ActionListener {
	private TreeClickListener treeClickListener;

	public ChangePasswordTask(TreeClickListener treeClickListener) {
		this.treeClickListener = treeClickListener;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		String userName = treeClickListener.getUserName();
		String newPassword = JOptionPane.showInputDialog(FrameManager.mainFrame,
				FrameManager.getLanguageProperty("changePassword.text"),
				FrameManager.getLanguageProperty("changePassword.header"), JOptionPane.QUESTION_MESSAGE);
		if (newPassword != null) {
			GlobalDataContainer.getAccountByName(userName).setPassword(newPassword);
		}
		JOptionPane.showMessageDialog(FrameManager.mainFrame, FrameManager.getLanguageProperty("popup.passwordChanged"),
				FrameManager.getLanguageProperty("popup.title.passwordChanged"), JOptionPane.PLAIN_MESSAGE);
	}

}
