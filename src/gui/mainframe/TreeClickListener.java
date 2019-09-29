package gui.mainframe;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;

import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;

import actionclasses.ChangePasswordTask;
import actionclasses.DeleteAccountTask;
import actionclasses.OpenComposeMessageTask;
import actionclasses.OpenMessagesTask;
import actionclasses.RenameAccountTast;
import data.AccountData;
import data.GlobalDataContainer;
import data.MailFolder;
import filewriters.XMLFileManager;
import gui.FrameManager;
import protokol.ConnectionManager;

public class TreeClickListener extends MouseAdapter {
	private JTree tree;
	private DefaultTreeModel dm;
	private MessagesPanel messagesPanel;

	public TreeClickListener(JTree tree, DefaultTreeModel dm) {
		this.tree = tree;
		this.dm = dm;
	}

	public String getUserName() {
		DefaultMutableTreeNode folderNode = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
		TreeNode[] path = folderNode.getPath();
		return path[1].toString();
	}

	public String getFolderName() {
		DefaultMutableTreeNode folderNode = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
		TreeNode[] path = folderNode.getPath();
		if (path.length > 2) {
			return path[2].toString();
		}
		return "";
	}

	public void mouseReleased(MouseEvent e) {
		// on right button click open context menu
		if (e.getButton() == MouseEvent.BUTTON3) {
			openContextMenu(e);
			// on left click open messages panel
		} else if (e.getButton() == MouseEvent.BUTTON1) {
			openMessages(e);
		}
	}

	public void changeTitleOfLastSelectedElement(String newTitle) {
		((DefaultMutableTreeNode) tree.getLastSelectedPathComponent()).setUserObject(newTitle);
		((DefaultTreeModel) tree.getModel()).nodeChanged((DefaultMutableTreeNode) tree.getLastSelectedPathComponent());
	}

	public void deleteLastSelectedNode() {
		dm.removeNodeFromParent((DefaultMutableTreeNode) tree.getLastSelectedPathComponent());
	}

	private void openContextMenu(MouseEvent e) {
		ContextMenu menu = new ContextMenu(this);
		// if selected node is account node
		if (tree.getPathForLocation(e.getX(), e.getY()).getPath().length == 2) {
			// if it was selected before right click
			DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree.getPathForLocation(e.getX(), e.getY())
					.getPath()[1];
			if (tree.getLastSelectedPathComponent() != null && tree.getLastSelectedPathComponent().equals(node)) {
				menu.show(e.getComponent(), e.getX(), e.getY());
			}
		}
	}

	private void openMessages(MouseEvent e) {
		try {
			// if selected node is folder node
			if (tree.getPathForLocation(e.getX(), e.getY()).getPath().length > 2) {
				DefaultMutableTreeNode folderNode = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
				TreeNode[] path = folderNode.getPath();
				// if click was not on "compose" menu
				if (!path[path.length - 1].toString().equals(FrameManager.getLanguageProperty("node.compose"))) {
					new OpenMessagesTask(this, messagesPanel).perform();
				} else {
					new OpenComposeMessageTask(this, messagesPanel).perform();
				}

			}
		} catch (NullPointerException nullPointer) {

		}
	}
}
