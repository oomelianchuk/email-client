package gui.mainframe;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collections;

import javax.swing.BoxLayout;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.SwingWorker;

import actionclasses.MailLoader;
import data.AccountData;
import data.MailFolder;
import filewriters.FileManager;
import gui.FrameManager;
import protokol.MessageContainer;

public class MessagesPanel extends JPanel {
	private ControlPanel controlPanel;
	private MessagesTopLine topLine;
	private int messageAmount;
	private String userName;
	private String folder;

	public MessagesPanel(String userName, String folder) {
		super();
		this.userName = userName;
		this.folder = folder;
		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		controlPanel = new ControlPanel();
		// action to display new loaded emails
		controlPanel.addActionListenerOnRefreshButton(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				AccountData accountToCompare = new AccountData();
				accountToCompare.set("userName", userName);
				AccountData data = FrameManager.accounts.get(FrameManager.accounts.indexOf(accountToCompare));
				new SwingWorker<Void, Void>() {
					protected Void doInBackground() {
						new MailLoader(FrameManager.connections.get(userName), data,
								data.getImapServer() != null ? "imap" : "pop").action();
						loadMessages();
						FrameManager.mainFrame.setVisible(false);
						FrameManager.mainFrame.setVisible(true);
						JOptionPane.showMessageDialog(FrameManager.mainFrame, "Refershed", "Refeshed",
								JOptionPane.PLAIN_MESSAGE);
						return null;
					}
				}.execute();
			}
		});
		// action to move checked messages to another mail folder
		controlPanel.addActionListenerOnMoveButton(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				moveMessage();
			}
		});
		this.add(controlPanel);
		// top line contains labels for message rows
		topLine = new MessagesTopLine();
		topLine.checkAll.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				setAllComponentsChecked();
			}
		});
		this.add(topLine);
		// to control (un-)checking messages better it will be first called this action
		// handler
		// this action handler will be called only after direct click on message row
		// (not on checkbox on it)
		// such click opens the message, so, in order that it's not possible to open few
		// messages at the same time, all other messages will be unchecked
		this.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {

				setAllComponentsUnchecked();
				callFurtherEvent(e);
			}
		});
	}

	private void callFurtherEvent(MouseEvent e) {
		// detect which message row received click
		Component component = this.getComponentAt(e.getX(), e.getY());
		if (component.getClass().equals(MessageRowPanel.class)) {
			MessageRowPanel message = (MessageRowPanel) component;
			// delegate displaying opened message to just clicked message row
			message.displayMessage(false);
			// add clicked message in check messages array to enable manipulations with it
			controlPanel.addCheckedMessage(message);
		}
	}

	private void moveMessage() {
		// get all folders of account
		AccountData accountToCompare = new AccountData();
		accountToCompare.set("userName", userName);
		int index = FrameManager.accounts.indexOf(accountToCompare);
		AccountData account = FrameManager.accounts.get(index);
		// add all folders except current to array
		ArrayList<String> folders = new ArrayList<String>();
		for (MailFolder folderConatiner : account.getFolders()) {
			if (!folderConatiner.getName().equals(folder)) {
				folders.add(folderConatiner.getName());
			}
		}
		// ask user to choose to which folder does he/she want to remove message
		String newFolder = (String) JOptionPane.showInputDialog(FrameManager.mainFrame,
				"Select folder to move message to", "Message moving", JOptionPane.QUESTION_MESSAGE, null,
				folders.toArray(), null);
		// if user has chosen a folder move messages to it
		if (newFolder != null) {
			// delegate this operation to control panel
			controlPanel.moveMessagesTo(newFolder);
		}
		JOptionPane.showMessageDialog(FrameManager.mainFrame, "your message moved", "Message moved",
				JOptionPane.PLAIN_MESSAGE);
	}

	private void setAllComponentsUnchecked() {
		for (Component comp : this.getComponents()) {
			if (comp.getClass().equals(MessageRowPanel.class)) {
				((MessageRowPanel) comp).setUnchecked();
				controlPanel.removeCheckedMessage(((MessageRowPanel) comp));
			}
		}
	}

	private void setAllComponentsChecked() {
		for (Component comp : this.getComponents()) {
			if (comp.getClass().equals(MessageRowPanel.class)) {
				((MessageRowPanel) comp).setChecked();
				controlPanel.removeCheckedMessage(((MessageRowPanel) comp));
			}
		}
	}

	@Override
	public void removeAll() {
		super.removeAll();
		this.add(topLine);
		this.add(controlPanel);
	}

	/**
	 * Finds account object in temporary memory
	 * 
	 * @return account object
	 */
	private AccountData getCurrentAccountData() {
		AccountData accountToCompare = new AccountData();
		accountToCompare.set("userName", userName);
		int index = FrameManager.accounts.indexOf(accountToCompare);
		return FrameManager.accounts.get(index);
	}

	/**
	 * Finds folder object in temporary memory
	 * 
	 * @return folder object
	 */
	private MailFolder getCurrentFolder() {
		AccountData data = getCurrentAccountData();
		MailFolder folderToCompare = new MailFolder(folder);
		int index = data.getFolders().indexOf(folderToCompare);
		return data.getFolders().get(index);
	}

	public void loadMessages() {
		MailFolder accountFolder = getCurrentFolder();
		int newMessagesNumber = accountFolder.getMessages().size();
		// get messages from current folder
		// there are contained only that messages that are loaded during this
		// session, that's why they are new and will be displayed with red border

		// this method will also be used to refresh folder, so to prevent load of
		// exactly the same list of messages, here will be checked if there are any new
		// messages loaded
		if (messageAmount == 0 | newMessagesNumber != 0) {
			ArrayList<MessageContainer> messages = new FileManager(
					"src/" + userName + "/folders/" + folder.replaceAll("\\]", "").replaceAll("\\[", ""))
							.getMessages(userName);
			if (!messages.isEmpty()) {
				removeAll();
				Collections.sort(accountFolder.getMessages());
				for (int i = 0; i < newMessagesNumber; i++) {
					MessageRowPanel messageRow = new MessageRowPanel(accountFolder.getMessages().get(i), Color.red);
					messageRow.checked.addActionListener(new ActionListener() {

						@Override
						public void actionPerformed(ActionEvent e) {
							if (messageRow.checked.isSelected()) {
								controlPanel.addCheckedMessage(messageRow);
							} else {
								controlPanel.removeCheckedMessage(messageRow);
							}
						}
					});
					this.add(messageRow);
				}
				// the rest of messages will be displayed with black border
				for (int i = newMessagesNumber; i < messages.size(); i++) {
					if (!accountFolder.getMessages().contains(messages.get(i)) | newMessagesNumber >= messages.size()) {
						MessageRowPanel messageRow = new MessageRowPanel(messages.get(i), Color.black);
						messageRow.checked.addActionListener(new ActionListener() {

							@Override
							public void actionPerformed(ActionEvent e) {
								if (messageRow.checked.isSelected()) {
									controlPanel.addCheckedMessage(messageRow);
								} else {
									controlPanel.removeCheckedMessage(messageRow);
								}
							}

						});
						this.add(messageRow);
					}
				}
				messageAmount = messages.size();
			}
		}
	}

	public void addOpenedMessagePanel(OpenedMessagePanel openedMessagePanel) {
		JSplitPane pane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		JScrollPane scrollPane = new JScrollPane(this);
		pane.setTopComponent(scrollPane);
		pane.setBottomComponent(openedMessagePanel);
		pane.setDividerLocation(300);
		FrameManager.mainFrame.setRightPart(pane);
		FrameManager.mainFrame.repaint();
	}
}
