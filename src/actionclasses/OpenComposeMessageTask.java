package actionclasses;

import data.GlobalDataContainer;
import gui.FrameManager;
import gui.mainframe.MessagesPanel;
import gui.mainframe.OpenedMessagePanel;
import gui.mainframe.TreeClickListener;

public class OpenComposeMessageTask implements Task {
	private MessagesPanel messagesPanel;
	private TreeClickListener treeListener;

	public OpenComposeMessageTask(TreeClickListener treeListener,
			MessagesPanel messagesPanel) {
		this.messagesPanel = messagesPanel;
		this.treeListener = treeListener;
	}

	@Override
	public void perform() {
		String userName=treeListener.getUserName();
		OpenedMessagePanel oPanel = new OpenedMessagePanel(userName,
				GlobalDataContainer.getAccountByName(userName).getEmail(), "", "", "");
		if (messagesPanel != null) {
			messagesPanel.addOpenedMessagePanel(oPanel);
		} else {
			FrameManager.mainFrame.setRightPart(oPanel);
		}
		FrameManager.mainFrame.repaint();
	}

}
