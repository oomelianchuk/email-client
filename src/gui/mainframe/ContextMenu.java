package gui.mainframe;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

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
		deleteUser = new JMenuItem("Delete User");
		add(deleteUser);
		renameUser = new JMenuItem("Rename User");
		add(renameUser);
		changePass = new JMenuItem("Change Password");
		add(changePass);
	}
}
