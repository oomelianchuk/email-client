package gui.mainframe;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
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
import data.GlobalDataContainer;
import data.MailFolder;
import filewriters.FileManager;
import gui.FrameManager;
import protokol.MessageContainer;

public class MessagesPanel extends JPanel {
	private ControlPanel controlPanel;
	private MessagesTopLine topLine;
	private int messageAmount;
	private MailFolder folder;

	public MessagesPanel(MailFolder folder) {
		super();
		this.folder = folder;
		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		controlPanel = new ControlPanel();
		// action to display new loaded emails
		controlPanel.addActionListenerOnRefreshButton(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {

				AccountData data = GlobalDataContainer.getAccountByName(folder.getAccountName());
				new SwingWorker<Void, Void>() {
					protected Void doInBackground() {
						new MailLoader(FrameManager.connections.get(folder.getAccountName()), data,
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
			public void actionPerformed(ActionEvent arg0) {
				moveMessageToFolder();
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

	private void moveMessageToFolder() {
		// get all folders of account
		AccountData account = GlobalDataContainer.getAccountByName(folder.getAccountName());
		// add all folders except current to array
		ArrayList<MailFolder> folders = new ArrayList<MailFolder>();
		account.getFolders().forEach(receivedFolder -> {
			if (!receivedFolder.equals(folder)) {
				folders.add(receivedFolder);
			}
		});
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
//TODO: move to GlobalDataContainer
	/**
	 * Finds account object in temporary memory
	 * 
	 * @return account object
	 */

	/*
	 * private AccountData getCurrentAccountData() { AccountData accountToCompare =
	 * new AccountData(); accountToCompare.set("userName", userName); int index =
	 * FrameManager.accounts.indexOf(accountToCompare); return
	 * FrameManager.accounts.get(index); }
	 */
//TODO: move to GlobalDataContainer
	/**
	 * Finds folder object in temporary memory
	 * 
	 * @return folder object
	 */
	/*
	 * private MailFolder getCurrentFolder() { AccountData data =
	 * getCurrentAccountData(); MailFolder folderToCompare = new MailFolder();
	 * folderToCompare.setName(folder); int index =
	 * data.getFolders().indexOf(folderToCompare); return
	 * data.getFolders().get(index); }
	 */

	public void loadMessages() {
		// MailFolder accountFolder = getCurrentFolder();
		int newMessagesNumber = folder.getMessages().size() - folder.numberOfMessagesOnStart;
		// get messages from current folder
		// there are contained only that messages that are loaded during this
		// session, that's why they are new and will be displayed with red border

		// this method will also be used to refresh folder, so to prevent load of
		// exactly the same list of messages, here will be checked if there are any new
		// messages loaded
		if (messageAmount == 0 | newMessagesNumber != 0) {
			System.out.println(newMessagesNumber);
			ArrayList<MessageContainer> messages;
			messages = folder.getMessages();
			if (!messages.isEmpty()) {
				removeAll();
				Collections.sort(folder.getMessages());
				for (int i = 0; i < newMessagesNumber; i++) {
					MessageRowPanel messageRow = new MessageRowPanel(folder.getMessages().get(i), Color.red);
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
					//if (!folder.getMessages().contains(messages.get(i)) | newMessagesNumber >= messages.size()) {
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
				//}
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
