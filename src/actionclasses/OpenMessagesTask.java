package actionclasses;

import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import data.GlobalDataContainer;
import data.MailFolder;
import gui.FrameManager;
import gui.mainframe.MessagesPanel;
import gui.mainframe.OpenedMessagePanel;
import gui.mainframe.TreeClickListener;

public class OpenMessagesTask implements Task {
	private MessagesPanel messagesPanel;
	private TreeClickListener treeListener;

	public OpenMessagesTask(TreeClickListener treeListener,
			MessagesPanel messagesPanel) {
		this.messagesPanel = messagesPanel;
		this.treeListener = treeListener;
	}

	@Override
	public void perform() {
		String userName= treeListener.getUserName();
		String folderName =treeListener.getFolderName();
		MailFolder folder = new MailFolder(userName, folderName);
		GlobalDataContainer.getAccountByName(userName).addFolder(folder);
		System.out.println(userName);
		System.out.println(folderName);
		messagesPanel = new MessagesPanel(GlobalDataContainer.getAccountByName(userName).getFolderByName(folderName));
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
	}

}
