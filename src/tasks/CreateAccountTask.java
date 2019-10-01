package tasks;

import java.awt.BorderLayout;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JProgressBar;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

import actionclasses.MailLoader;
import backrgroundhelpers.ProgressBarInMainFrame;
import data.AccountData;
import data.GlobalDataContainer;
import data.MailFolder;
import filewriters.LoggerConfigurator;
import gui.FrameManager;
import gui.mainframe.ProgressBarPanel;
import protokol.ConnectionManager;
import protokol.MessageContainer;

public class CreateAccountTask implements Task {
	private AccountData data;

	public CreateAccountTask(AccountData data) {
		this.data = data;
	}

	@Override
	public void perform() {
		FrameManager.LOGGER.info("create account for " + data.toString());
		ConnectionManager connectionManager = new ConnectionManager(data);
		boolean popShould = data.getPopServer() != null;
		boolean imapShould = data.getImapServer() != null;
		boolean smtpShould = data.getSmtpServer() != null;
		boolean popIs = false;
		boolean imapIs = false;
		boolean smtpIs = false;
		// if there is data for connection protocol try to create connection
		// if any connection is not successful show error message
		if (popShould) {
			popIs = connectionManager.checkPopConnection(data);
			if (!popIs) {
				FrameManager.popUP.showErrorMessage(FrameManager.getLanguageProperty("error.popFailed"));
			}
		}
		if ((popIs == popShould) & imapShould) {
			imapIs = connectionManager.checkImapConnection(data);
			if (!imapIs) {
				FrameManager.popUP.showErrorMessage(FrameManager.getLanguageProperty("error.imapFailed"));
			}
		}
		if ((popIs == popShould) & (imapIs == imapShould) & smtpShould) {
			smtpIs = connectionManager.checkSmtpConnection(data);
			if (!smtpIs) {
				FrameManager.popUP.showErrorMessage(FrameManager.getLanguageProperty("error.smtpFailed"));
			}
		}
		// if all wished connections are successful
		if (popShould == popIs & imapShould == imapIs & smtpShould == smtpIs) {
			// close create account window
			FrameManager.popUP.dispose();
			// receive folders
			if (imapShould) {
				ArrayList<MailFolder> folders = new ArrayList<MailFolder>();
				connectionManager.getFolderNames("imap", data).forEach(folderName -> folders
						.add(new MailFolder(data.getUserName(), folderName, new ArrayList<MessageContainer>())));
				data.setFolders(folders);
			} else if (popShould) {
				ArrayList<MailFolder> folders = new ArrayList<MailFolder>();
				connectionManager.getFolderNames("pop", data).forEach(folderName -> folders
						.add(new MailFolder(data.getUserName(), folderName, new ArrayList<MessageContainer>())));
				data.setFolders(folders);
			}
			FrameManager.LOGGER.info("add account on view");
			// add account tree node on main frame
			FrameManager.mainFrame.addNewAccountNode(data);
			try {
				data.serialize();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			// start loading mail for account
			// create a progress bar to display the progress
			ProgressBarPanel progressBarPanel = new ProgressBarPanel();
			FrameManager.mainFrame.addNewPanel("progressBar", progressBarPanel, BorderLayout.SOUTH);
			// load by imap protocol
			// if no data for imap connection load by pop
			FrameManager.LOGGER.info("loading mail");
			if (imapShould) {
				if (!FrameManager.debug) {
					ProgressBarInMainFrame progressTermitated = new ProgressBarInMainFrame(
							new MailLoader(connectionManager, data, "imap"), false);
					progressTermitated.setFrame(FrameManager.mainFrame);
					progressTermitated.startInMainFrame();
				} else {
					new MailLoader(connectionManager, data, "imap").action(new JProgressBar(), new JLabel(""));
				}
			} else if (popShould) {
				ProgressBarInMainFrame progressTermitated;
				if (!FrameManager.debug) {
					progressTermitated = new ProgressBarInMainFrame(new MailLoader(connectionManager, data, "pop"),
							false);
					progressTermitated.setFrame(FrameManager.mainFrame);
					progressTermitated.startInMainFrame();
				} else {
					new MailLoader(connectionManager, data, "pop").action(new JProgressBar(), new JLabel(""));
				}
			}
			FrameManager.LOGGER.info("add info in temporary memory");
			// save connections and short info for this account in temp memory,
			// so that it is reachable from any point of program
			GlobalDataContainer.addConnection(data.getUserName(), connectionManager);
			GlobalDataContainer.addAccount(data);
			// start background thread to check for new mail
			if (data.isRunInBackground()) {
				MailLoader thread = new MailLoader(connectionManager, data,
						data.getImapServer() == null ? "pop" : "imap");
				thread.runAsThread();
				GlobalDataContainer.threads.put(data.getUserName(), thread);
			}
			new LoggerConfigurator().setUpLoggerForUser(data.getUserName());
			JOptionPane.showMessageDialog(FrameManager.mainFrame,
					FrameManager.getLanguageProperty("popup.accountCreated"),
					FrameManager.getLanguageProperty("popup.title.accountCreated"), JOptionPane.PLAIN_MESSAGE);
		}
	}

}
