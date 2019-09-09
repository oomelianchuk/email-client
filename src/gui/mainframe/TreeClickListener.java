package gui.mainframe;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;

import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;

import data.AccountData;
import data.GlobalDataContainer;
import data.MailFolder;
import filewriters.XMLFileManager;
import gui.FrameManager;
import protokol.ConnectionManager;

class TreeClickListener extends MouseAdapter {
	private JTree tree;
	private DefaultTreeModel dm;
	private MessagesPanel messagesPanel;

	public TreeClickListener(JTree tree, DefaultTreeModel dm) {
		this.tree = tree;
		this.dm = dm;
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

	private void openContextMenu(MouseEvent e) {
		ContextMenu menu = new ContextMenu();
		menu.deleteUser.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				deleteUser();
			}
		});
		menu.renameUser.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				renameUser();
			}
		});
		menu.changePass.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				changePassword();
			}
		});
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
				if (!path[path.length - 1].toString().equals("Compose")) {
					String userName= path[1].toString();
					String folderName=path[2].toString();
					MailFolder folder =new MailFolder("src/"+userName+"/"+ folderName);
					GlobalDataContainer.getAccountByName(userName).addFolder(folder);
					System.out.println(folder.getMessages().size());
					messagesPanel = new MessagesPanel(folder);
					messagesPanel.loadMessages();
					if (FrameManager.mainFrame.getRightPart().getClass().equals(OpenedMessagePanel.class)) {
						JSplitPane pane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
						JScrollPane scrollPane = new JScrollPane(messagesPanel);
						pane.setTopComponent(scrollPane);
						pane.setBottomComponent((OpenedMessagePanel) FrameManager.mainFrame.getRightPart());
						pane.setDividerLocation(300);
						FrameManager.mainFrame.setRightPart(pane);
						FrameManager.mainFrame.repaint();
					} else {
						FrameManager.mainFrame.setRightPartScrollPane(messagesPanel);
					}
				} else {
					AccountData accountToCompare = new AccountData();
					accountToCompare.set("userName", path[1].toString());
					OpenedMessagePanel oPanel = new OpenedMessagePanel(path[1].toString(),
							FrameManager.accounts.get(FrameManager.accounts.indexOf(accountToCompare)).getEmail(), "",
							"", "");
					if (messagesPanel != null) {
						messagesPanel.addOpenedMessagePanel(oPanel);
					} else {
						FrameManager.mainFrame.setRightPart(oPanel);
					}
					FrameManager.mainFrame.repaint();
				}

			}
		} catch (NullPointerException nullPointer) {

		}
	}

	private void deleteUser() {
		String userName = tree.getLastSelectedPathComponent().toString();
		dm.removeNodeFromParent((DefaultMutableTreeNode) tree.getLastSelectedPathComponent());
		new FrameManager().deleteAccount(userName);
	}

	private void renameUser() {
		String userName = tree.getLastSelectedPathComponent().toString();
		String newUserName = JOptionPane.showInputDialog(FrameManager.mainFrame, "enter new username",
				"changing username", JOptionPane.QUESTION_MESSAGE);
		if (newUserName != null) {
			((DefaultMutableTreeNode) tree.getLastSelectedPathComponent()).setUserObject(newUserName);
			((DefaultTreeModel) tree.getModel())
					.nodeChanged((DefaultMutableTreeNode) tree.getLastSelectedPathComponent());

			AccountData accountToCompare = new AccountData();
			accountToCompare.set("userName", userName);
			FrameManager.accounts.get(FrameManager.accounts.indexOf(accountToCompare)).set("userName", newUserName);
			ConnectionManager connection = FrameManager.connections.get(userName);
			FrameManager.connections.remove(userName);
			FrameManager.connections.put(newUserName, connection);
			new XMLFileManager("src/accounts.xml").renameAccount(userName, newUserName);
			new File("src/" + userName).renameTo(new File("src/" + newUserName));
		}
	}

	private void changePassword() {
		String userName = tree.getLastSelectedPathComponent().toString();
		String newPassword = JOptionPane.showInputDialog(FrameManager.mainFrame, "enter new password",
				"changing password", JOptionPane.QUESTION_MESSAGE);
		if (newPassword != null) {
			AccountData accountToCompare = new AccountData();
			accountToCompare.set("userName", userName);
			FrameManager.accounts.get(FrameManager.accounts.indexOf(accountToCompare)).set("password", newPassword);
		}
		JOptionPane.showMessageDialog(FrameManager.mainFrame,
				"Password changed, to make changes be used in program, restart it", "Password Changed",
				JOptionPane.PLAIN_MESSAGE);
	}
}
