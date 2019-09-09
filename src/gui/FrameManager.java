package gui;

import java.awt.BorderLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JProgressBar;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import actionclasses.Loader;
import actionclasses.MailLoader;
import backrgroundhelpers.ProgressBarInMainFrame;
import backrgroundhelpers.ProgressBarInNewFrame;
import data.AccountData;
import data.GlobalDataContainer;
import data.MailFolder;
import filewriters.LoggerConfigurator;
import filewriters.XMLFileManager;
import gui.mainframe.MainFrame;
import gui.mainframe.ProgressBarPanel;
import gui.newaccountdialog.NewAccountDialog;
import protokol.ConnectionManager;
import protokol.MessageContainer;

public class FrameManager {
	static {
		SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy/hh-mm-ss");
		System.setProperty("currentDate", dateFormat.format(new Date()));
	}
	public static MainFrame mainFrame;
	public static NewAccountDialog popUP;
	public static ArrayList<AccountData> accounts = new ArrayList<AccountData>();
	public static HashMap<String, ConnectionManager> connections = new HashMap<String, ConnectionManager>();
	private static HashMap<String, MailLoader> threads = new HashMap<String, MailLoader>();
	private static boolean debug = false;
	public static final Logger logger = LogManager.getLogger(FrameManager.class);

	public void showPopUP() {
		popUP = new NewAccountDialog();
		popUP.showFrame();
	}

	public void openMessage(MessageContainer message) {

	}

	public void deleteAccount(String userName) {
		logger.info("deleting account " + userName);
		// delete account node in xml file
		try {
			logger.info("delete directory " + "src/" + userName);
			FileUtils.deleteDirectory(new File("src/" + userName));

			logger.info("delete account from xml");
			XMLFileManager xml = new XMLFileManager("src/accounts.xml");
			xml.deleteAccount(userName);

			logger.info("joing thread");
			// end last update circle and kill check mail thread for this account
			if (threads.get(userName) != null) {
				threads.get(userName).join();
			}
			logger.info("thread joint");
			logger.info("close connections");
			// close all connections for this account
			connections.get(userName).closeAllSessions();
			logger.info("remove from temporary program memory");
			// remove account from temporary program memory
			for (AccountData account : accounts) {
				if (account.getUserName().equals(userName)) {
					accounts.remove(account);
					break;
				}
			}
			// show message to inform user that account is deleted
			JOptionPane.showMessageDialog(FrameManager.mainFrame, "account " + userName + " deleted", "Account Deleted",
					JOptionPane.PLAIN_MESSAGE);
		} catch (IOException e1) {
			JOptionPane.showMessageDialog(FrameManager.mainFrame, "Not possible to selete account, try later",
					"Account Delete Error", JOptionPane.ERROR_MESSAGE);
			logger.error("while deleting account: " + e1.toString());
		}
		new LoggerConfigurator().deleteLoggerForUser(userName);
	}

	public void createAccount(AccountData data) {
		logger.info("create account for " + data.toString());
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
				popUP.showErrorMessage("POP Connection failed");
			}
		}
		if ((popIs == popShould) & imapShould) {
			imapIs = connectionManager.checkImapConnection(data);
			if (!imapIs) {
				popUP.showErrorMessage("IMAP Connection failed");
			}
		}
		if ((popIs == popShould) & (imapIs == imapShould) & smtpShould) {
			smtpIs = connectionManager.checkSmtpConnection(data);
			if (!smtpIs) {
				popUP.showErrorMessage("SMTP Connection failed");
			}
		}
		// if all wished connections are successful
		if (popShould == popIs & imapShould == imapIs & smtpShould == smtpIs) {
			// close create account window
			popUP.dispose();
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
			logger.info("add account on view");
			// add account tree node on main frame
			mainFrame.addNewAccount(data);
			logger.info("write account in xml");
			// create account node in xml file
			XMLFileManager xml = new XMLFileManager("src/accounts.xml");
			xml.addNewAccount(data);
			// start loading mail for account
			// create a progress bar to display the progress
			ProgressBarPanel progressBarPanel = new ProgressBarPanel();
			mainFrame.addNewPanel("progressBar", progressBarPanel, BorderLayout.SOUTH);
			// load by imap protocol
			// if no data for imap connection load by pop
			logger.info("loading mail");
			if (imapShould) {
				if (!debug) {
					ProgressBarInMainFrame progressTermitated = new ProgressBarInMainFrame(
							new MailLoader(connectionManager, data, "imap"), false);
					progressTermitated.setFrame(mainFrame);
					progressTermitated.startInMainFrame();
				} else {
					new MailLoader(connectionManager, data, "imap").action(new JProgressBar(), new JLabel(""));
				}
			} else if (popShould) {
				ProgressBarInMainFrame progressTermitated;
				if (!debug) {
					progressTermitated = new ProgressBarInMainFrame(new MailLoader(connectionManager, data, "pop"),
							false);
					progressTermitated.setFrame(mainFrame);
					progressTermitated.startInMainFrame();
				} else {
					new MailLoader(connectionManager, data, "pop").action(new JProgressBar(), new JLabel(""));
				}
			}
			logger.info("add info in temporary memory");
			// save connections and short info for this account in temp memory,
			// so that it is reachable from any point of program
			connections.put(data.getUserName(), connectionManager);
			accounts.add(data);
			// start background thread to check for new mail
			if (Boolean.parseBoolean(data.get("runInBackground"))) {
				MailLoader thread = new MailLoader(connectionManager, data,
						data.getImapServer() == null ? "pop" : "imap");
				thread.runAsThread();
				threads.put(data.getUserName(), thread);
			}
			new LoggerConfigurator().setUpLoggerForUser(data.getUserName());
			JOptionPane.showMessageDialog(FrameManager.mainFrame,
					"account created, to make program deal with it properly, please restart the app", "Account Created",
					JOptionPane.PLAIN_MESSAGE);
		}

	}

	public static boolean updateConnections(String userName) {
		logger.info("update connection for " + userName);
		threads.forEach((name, thread) -> thread.join());
		logger.info("all threads paused");
		AccountData accountToCompare = new AccountData();
		accountToCompare.set("userName", userName);
		AccountData data = accounts.get(accounts.indexOf(accountToCompare));
		boolean pop = true;
		boolean imap = true;
		if (data.getPopServer() != null) {
			logger.info("check pop connection");
			pop = connections.get(userName).checkPopConnection(data);
		}
		if (data.getImapServer() != null) {
			logger.info("check imap connection");
			imap = connections.get(userName).checkImapConnection(data);
		}

		threads.forEach((name, thread) -> thread.runAsThread());
		if (pop & imap) {
			logger.info("connection updated");
		} else {
			if (!pop) {
				logger.error("pop connection update failed");
			}
			if (!imap) {
				logger.error("imap connection update failed");
			}
		}
		return pop & imap;
	}

	private static void configureTheame() {
		// read theme settings and set selected theme
		if (new XMLFileManager("src/accounts.xml").getLookAndFeel().equals("system")) {
			try {
				UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
				logger.info("system look and feel set");
			} catch (ClassNotFoundException | InstantiationException | IllegalAccessException
					| UnsupportedLookAndFeelException e1) {
				e1.printStackTrace();
				logger.error("while setting system look and feel: " + e1.toString());
			}
		} else {
			try {
				UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
				logger.info("cross-platform look and feel set");
			} catch (ClassNotFoundException | InstantiationException | IllegalAccessException
					| UnsupportedLookAndFeelException e1) {
				e1.printStackTrace();
				logger.error("while setting cross-platform look and feel: " + e1.toString());
			}
		}
	}

	public static void loadAccounts() {
		if (!debug) {
			logger.info("debug modus off");
			ProgressBarInNewFrame progressTermitated = new ProgressBarInNewFrame(new Loader(mainFrame), true);
			Thread t = new Thread(progressTermitated);
			t.start();
			try {
				t.join();
			} catch (InterruptedException e1) {
				e1.printStackTrace();
				logger.error("while joing thread: " + e1.toString());
			}
		} else {
			logger.info("debug modus on");
			new Loader(mainFrame).action(new JProgressBar(), new JLabel());
		}

	}

	public static void main(String[] args) {
		logger.info("programm start");
		configureTheame();
		mainFrame = new MainFrame();
		loadAccounts();
		logger.info("strat displaying main frame");
		// show main frame
		mainFrame.pack();
		mainFrame.setVisible(true);
		// run background threads to check for new mail for all accounts
		for (AccountData data : accounts) {
			// new LoggerConfigurator().setUpLoggerForUser(data.getUserName());
			if (Boolean.parseBoolean(data.get("runInBackground"))) {
				logger.info("starting background thread for " + data.getUserName());
				MailLoader thread = new MailLoader(connections.get(data.getUserName()), data,
						data.getImapServer() == null ? "pop" : "imap");
				thread.runAsThread();
				threads.put(data.getUserName(), thread);
				logger.info("background thread started");
			}
		}
		GlobalDataContainer.setAccounts(accounts);
		// after main frame is closed
		mainFrame.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				logger.info("closing program");
				e.getWindow().dispose();
				logger.info("window disposed");
				if (!accounts.isEmpty()) {
					// wait until last mail update circle ends and close threads
					for (AccountData account : accounts) {
						// close all connections for account
						if (Boolean.parseBoolean(account.get("runInBackground"))) {
							logger.info("closing thread for " + account.getUserName());
							threads.get(account.getUserName()).join();
							logger.info("thread closed");
						}
						logger.info("closing connections for " + account.getUserName());
						connections.get(account.getUserName()).closeAllSessions();
						logger.info("connections closed");
						logger.info("rewriting xml");
						// rewrite account data to save changed data
						new XMLFileManager("src/accounts.xml").rewriteAccount(account);
						logger.info("xml rewrote");
					}
				}
				System.exit(0);
			}
		});
	}
}
