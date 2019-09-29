package gui.mainframe;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import actionclasses.ChangePasswordTask;
import actionclasses.DeleteAccountTask;
import actionclasses.RenameAccountTast;
import gui.FrameManager;

/**
 * Context menu on main frame account tree. It should contain buttons to
 * manipulate with user accounts e.g. to delete/change them
 *
 */
public class ContextMenu extends JPopupMenu {

	public ContextMenu(TreeClickListener treeClickListener) {
		JMenuItem deleteUser = new JMenuItem(FrameManager.getLanguageProperty("contextMenu.delete"));
		deleteUser.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				new DeleteAccountTask(treeClickListener).perform();
			}
		});
		add(deleteUser);
		JMenuItem renameUser = new JMenuItem(FrameManager.getLanguageProperty("contextMenu.rename"));
		renameUser.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				new RenameAccountTast(treeClickListener).perform();
			}
		});
		add(renameUser);


		JMenuItem changePass = new JMenuItem(FrameManager.getLanguageProperty("contextMenu.change"));
		changePass.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				new ChangePasswordTask(treeClickListener).perform();
			}
		});
		add(changePass);
	}
}
