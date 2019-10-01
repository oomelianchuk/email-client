package actionlisteners;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;

import javax.swing.JOptionPane;
import javax.swing.tree.DefaultMutableTreeNode;

import org.apache.commons.io.FileUtils;

import data.AccountData;
import data.GlobalDataContainer;
import filewriters.LoggerConfigurator;
import gui.FrameManager;
import gui.mainframe.TreeClickListener;

public class DeleteAccountTask implements ActionListener {
	private TreeClickListener treeClickListener;

	public DeleteAccountTask(TreeClickListener treeClickListener) {
		super();
		this.treeClickListener = treeClickListener;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		String userName = treeClickListener.getUserName();
		FrameManager.LOGGER.info("deleting account " + userName);
		// delete account node in xml file
		try {
			FrameManager.LOGGER.info("delete directory "
					+ FrameManager.getProgramSetting("pathToUser").replaceAll("\\{userName\\}", userName));
			FileUtils.deleteDirectory(
					new File(FrameManager.getProgramSetting("pathToUser").replaceAll("\\{userName\\}", userName)));
			FrameManager.LOGGER.info("joing thread");
			// end last update circle and kill check mail thread for this account
			if (GlobalDataContainer.threads.get(userName) != null) {
				GlobalDataContainer.threads.get(userName).join();
			}
			FrameManager.LOGGER.info("thread joint");
			FrameManager.LOGGER.info("close connections");
			// close all connections for this account
			GlobalDataContainer.getConnectionByAccount(userName).closeAllSessions();
			FrameManager.LOGGER.info("remove from temporary program memory");
			// remove account from temporary program memory
			for (AccountData account : GlobalDataContainer.getAccounts()) {
				if (account.getUserName().equals(userName)) {
					GlobalDataContainer.getAccounts().remove(account);
					break;
				}
			}
			// show message to inform user that account is deleted
			JOptionPane.showMessageDialog(FrameManager.mainFrame, "account " + userName + " deleted", "Account Deleted",
					JOptionPane.PLAIN_MESSAGE);
		} catch (IOException e1) {
			JOptionPane.showMessageDialog(FrameManager.mainFrame, "Not possible to delete account, try later",
					"Account Delete Error", JOptionPane.ERROR_MESSAGE);
			FrameManager.LOGGER.error("while deleting account: " + e1.toString());
		}
		new LoggerConfigurator().deleteLoggerForUser(userName);
		treeClickListener.deleteLastSelectedNode();
	}

}
