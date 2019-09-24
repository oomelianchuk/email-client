
package gui.mainframe;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.border.EmptyBorder;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

import data.AccountData;
import data.MailFolder;
import filewriters.XMLFileManager;
import gui.FrameManager;

public class MainFrame extends JFrame {
	private JPanel htmlPane;
	private JTree tree;
	private DefaultTreeModel dm;
	private JPanel contentPane;
	private HashMap<String, Component> allPanels;
	private JSplitPane splitPane;

	public MainFrame() {
		super(FrameManager.getLanguageProperty("header.emailClient"));
		allPanels = new HashMap<String, Component>();
		dm = new DefaultTreeModel(new DefaultMutableTreeNode(FrameManager.getLanguageProperty("node.accounts")));
		tree = new JTree(dm);
		tree.addMouseListener(new TreeClickListener(tree, dm));

		// Create the scroll pane and add the tree to it.
		JScrollPane treeView = new JScrollPane(tree);
		treeView.setBorder(new EmptyBorder(0, 10, 0, 10));
		treeView.setMinimumSize(new Dimension(100, 50));

		// Create the HTML viewing pane.
		htmlPane = new JPanel();
		// htmlPane.setEditable(false);
		JScrollPane htmlView = new JScrollPane(htmlPane);

		// Add the scroll panes to a split pane.
		splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		splitPane.setLeftComponent(treeView);
		splitPane.setRightComponent(htmlView);
		treeView.setMinimumSize(new Dimension(100, 50));
		htmlView.setMinimumSize(new Dimension(100, 50));

		splitPane.setDividerLocation(200);
		splitPane.setPreferredSize(new Dimension(500, 300));

		// Add the split pane to this panel.
		contentPane = new JPanel();
		contentPane.setLayout(new BorderLayout());
		// top menu
		JMenuBar menuBar = new JMenuBar();
		menuBar.setBounds(0, 0, 434, 29);

		// program settings
		JMenu settingsMenu = new JMenu(FrameManager.getLanguageProperty("menu.settings"));
		menuBar.add(settingsMenu);
		// add new account button
		JMenuItem newAccountMenu = new JMenuItem(FrameManager.getLanguageProperty("menuItem.newAccount"));
		settingsMenu.add(newAccountMenu);
		newAccountMenu.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent evt) {
				new FrameManager().showPopUP();
			}

		});
		// button to change look and feel
		JMenu changeTheme = new JMenu(FrameManager.getLanguageProperty("menu.theme"));
		settingsMenu.add(changeTheme);
		JMenuItem systemTheme = new JMenuItem(FrameManager.getLanguageProperty("menu.theme.system"));
		changeTheme.add(systemTheme);
		systemTheme.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					// change look and feel
					UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
					SwingUtilities.updateComponentTreeUI(MainFrame.this);
					// save theme settings
					new XMLFileManager(FrameManager.getProgramSetting("pathToAccountSettings")).changeLookAndFeel("system");
				} catch (ClassNotFoundException | InstantiationException | IllegalAccessException
						| UnsupportedLookAndFeelException e1) {
					e1.printStackTrace();
					JOptionPane.showMessageDialog(FrameManager.mainFrame,
							FrameManager.getLanguageProperty("error.unsupportedTheme"),
							FrameManager.getLanguageProperty("error.title.unsupportedTheme"),
							JOptionPane.ERROR_MESSAGE);
				}
			}
		});
		JMenuItem crossplatformTheme = new JMenuItem(FrameManager.getLanguageProperty("menu.theme.cross"));
		changeTheme.add(crossplatformTheme);
		crossplatformTheme.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					// change look and feel
					UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
					SwingUtilities.updateComponentTreeUI(MainFrame.this);
					// save theme settings
					new XMLFileManager(FrameManager.getProgramSetting("pathToAccountSettings")).changeLookAndFeel("crossplatform");
				} catch (ClassNotFoundException | InstantiationException | IllegalAccessException
						| UnsupportedLookAndFeelException e1) {
					e1.printStackTrace();
					JOptionPane.showMessageDialog(FrameManager.mainFrame,
							FrameManager.getLanguageProperty("error.unsupportedTheme"),
							FrameManager.getLanguageProperty("error.title.unsupportedTheme"),
							JOptionPane.ERROR_MESSAGE);
				}
			}
		});
		contentPane.add(menuBar, BorderLayout.NORTH);
		contentPane.add(splitPane, BorderLayout.CENTER);
		add(contentPane);
	}

	// used for progress bar displaying
	public void addNewPanel(String name, JPanel panel, final String borderLayoutLocationConst) {
		contentPane.add(panel, borderLayoutLocationConst);
		allPanels.put(name, panel);
	}

	// used for progress bar displaying
	public JPanel getPanel(String name) {
		return (JPanel) allPanels.get(name);
	}

	public void setRightPart(JSplitPane splitPane) {
		allPanels.remove("rightPartPanel");
		this.splitPane.setDividerLocation(200);
		this.splitPane.setRightComponent(splitPane);

	}

	// as it's not possible to get contained component from scroll pane, it should't
	// be returned as right part, so the original panel,that will be wrapped in
	// scroll pane will be saved separately
	public void setRightPartScrollPane(Component panel) {
		this.splitPane.setRightComponent(new JScrollPane(panel));
		this.splitPane.setDividerLocation(200);
		allPanels.put("rightPartPanel", panel);
	}

	public void setRightPart(JPanel panel) {
		this.splitPane.setRightComponent(panel);
	}

	// and will be returned here
	public Component getRightPart() {
		if (allPanels.get("rightPartPanel") == null) {
			return this.splitPane.getRightComponent();
		} else {
			return allPanels.get("rightPartPanel");
		}
	}

	public void addNewAccount(AccountData data) {
		// add new account node with all folders
		DefaultMutableTreeNode root = (DefaultMutableTreeNode) tree.getModel().getRoot();
		DefaultMutableTreeNode newChild = new DefaultMutableTreeNode(data.getUserName());
		newChild.add(new DefaultMutableTreeNode(FrameManager.getLanguageProperty("node.compose")));
		ArrayList<MailFolder> folders = data.getFolders();
		if (folders.get(0)!= null) {
			for (MailFolder folder : folders) {
				newChild.add(new DefaultMutableTreeNode(folder.getName()));
			}
		}else {
			for(String folderName:data.getFolderNames()) {
				newChild.add(new DefaultMutableTreeNode(folderName));
			}
		}
		dm.insertNodeInto(newChild, root, root.getChildCount());
		tree.expandPath(new TreePath(dm.getPathToRoot(newChild.getParent())));
	}
}