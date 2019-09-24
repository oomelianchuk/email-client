package gui.mainframe;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import gui.FrameManager;

/**
 * Context menu on main frame account tree. It should contain buttons to
 * manipulate with user accounts e.g. to delete/change them
 *
 */
public class ContextMenu extends JPopupMenu {
	JMenuItem deleteUser;
	JMenuItem renameUser;
	JMenuItem changePass;

	public ContextMenu() {
		deleteUser = new JMenuItem(FrameManager.getLanguageProperty("contextMenu.delete"));
		add(deleteUser);
		renameUser = new JMenuItem(FrameManager.getLanguageProperty("contextMenu.rename"));
		add(renameUser);
		changePass = new JMenuItem(FrameManager.getLanguageProperty("contextMenu.change"));
		add(changePass);
	}
}
