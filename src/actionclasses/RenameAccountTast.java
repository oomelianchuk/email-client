package actionclasses;

import java.io.File;
import java.io.IOException;

import javax.swing.JOptionPane;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

import data.GlobalDataContainer;
import gui.FrameManager;
import gui.mainframe.TreeClickListener;
import protokol.ConnectionManager;

public class RenameAccountTast implements Task {
	private TreeClickListener treeClickListener;

	public RenameAccountTast(TreeClickListener treeClickListener) {
		this.treeClickListener = treeClickListener;
	}

	@Override
	public void perform() {
		String userName = treeClickListener.getUserName();
		String newUserName = JOptionPane.showInputDialog(FrameManager.mainFrame,
				FrameManager.getLanguageProperty("renameUser.text"),
				FrameManager.getLanguageProperty("renameUser.header"), JOptionPane.QUESTION_MESSAGE);
		if (newUserName != null) {
			treeClickListener.changeTitleOfLastSelectedElement(newUserName);
			GlobalDataContainer.getAccountByName(userName).setUserName(newUserName);
			ConnectionManager connection = GlobalDataContainer.getConnectionByAccount(userName);
			GlobalDataContainer.deleteConnection(userName);
			GlobalDataContainer.addConnection(newUserName, connection);
			new File(FrameManager.getProgramSetting("pathToUser").replaceAll("\\{userName\\}", userName)).renameTo(
					new File(FrameManager.getProgramSetting("pathToUser").replaceAll("\\{userName\\}", newUserName)));
			try {
				GlobalDataContainer.getAccountByName(newUserName).serialize();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

}
