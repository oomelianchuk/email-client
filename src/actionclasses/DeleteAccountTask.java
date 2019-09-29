package actionclasses;

import javax.swing.tree.DefaultMutableTreeNode;

import gui.FrameManager;
import gui.mainframe.TreeClickListener;

public class DeleteAccountTask implements Task {
	private TreeClickListener treeClickListener;

	public DeleteAccountTask(TreeClickListener treeClickListener) {
		super();
		this.treeClickListener = treeClickListener;
	}

	@Override
	public void perform() {
		String userName=treeClickListener.getUserName();
		new FrameManager().deleteAccount(userName);
		treeClickListener.deleteLastSelectedNode();
	}

}
