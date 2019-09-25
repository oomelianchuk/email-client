package gui;

import java.awt.BorderLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JProgressBar;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import org.apache.commons.io.FileUtils;

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
	private static HashMap<String, MailLoader> threads = new HashMap<String, MailLoader>();
	private static boolean debug = false;
	public static final Logger logger = LogManager.getLogger(FrameManager.class);
	private final static Properties languageProperties = new Properties();
	private final static Properties programSettings = new Properties();

	public static String getLanguageProperty(String propertyName) {
		return languageProperties.getProperty(propertyName);
	}

	public static String getProgramSetting(String propertyName) {
		return programSettings.getProperty(propertyName);
	}

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
			logger.info("delete directory "
					+ FrameManager.getProgramSetting("pathToUser").replaceAll("\\{userName\\}", userName));
			FileUtils.deleteDirectory(
					new File(FrameManager.getProgramSetting("pathToUser").replaceAll("\\{userName\\}", userName)));

			logger.info("delete account from xml");
			XMLFileManager xml = new XMLFileManager(FrameManager.getProgramSetting("pathToAccountSettings"));
			xml.deleteAccount(userName);

			logger.info("joing thread");
			// end last update circle and kill check mail thread for this account
			if (threads.get(userName) != null) {
				threads.get(userName).join();
			}
			logger.info("thread joint");
			logger.info("close connections");
			// close all connections for this account
			GlobalDataContainer.getConnectionByAccount(userName).closeAllSessions();
			logger.info("remove from temporary program memory");
			// remove account from temporary program memory
			for (AccountData account : GlobalDataContainer.getAccounts()) {
				if (account.getUserName().equals(userName)) {
					GlobalDataContainer.getAccounts().remove(account);
					break;
				}
			}
			// show message to inform user that account is deleted
			JOptionPane.showMessageDialog(FrameManager.mainFrame, "account " + userName + " deleted", "Account Deleted",
					JOptionPane.PLAIN_MESSAGE);
		} catch (IOException e1) {
			JOptionPane.showMessageDialog(FrameManager.mainFrame, "Not possible to delete account, try later",
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
				popUP.showErrorMessage(FrameManager.getLanguageProperty("error.popFailed"));
			}
		}
		if ((popIs == popShould) & imapShould) {
			imapIs = connectionManager.checkImapConnection(data);
			if (!imapIs) {
				popUP.showErrorMessage(FrameManager.getLanguageProperty("error.imapFailed"));
			}
		}
		if ((popIs == popShould) & (imapIs == imapShould) & smtpShould) {
			smtpIs = connectionManager.checkSmtpConnection(data);
			if (!smtpIs) {
				popUP.showErrorMessage(FrameManager.getLanguageProperty("error.smtpFailed"));
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
			XMLFileManager xml = new XMLFileManager(FrameManager.getProgramSetting("pathToAccountSettings"));
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
			GlobalDataContainer.addConnection(data.getUserName(), connectionManager);
			GlobalDataContainer.addAccount(data);
			// start background thread to check for new mail
			if (Boolean.parseBoolean(data.get("runInBackground"))) {
				MailLoader thread = new MailLoader(connectionManager, data,
						data.getImapServer() == null ? "pop" : "imap");
				thread.runAsThread();
				threads.put(data.getUserName(), thread);
			}
			new LoggerConfigurator().setUpLoggerForUser(data.getUserName());
			JOptionPane.showMessageDialog(FrameManager.mainFrame,
					FrameManager.getLanguageProperty("popup.accountCreated"),
					FrameManager.getLanguageProperty("popup.title.accountCreated"), JOptionPane.PLAIN_MESSAGE);
		}

	}

	public static boolean updateConnections(String userName) {
		logger.info("update connection for " + userName);
		threads.forEach((name, thread) -> thread.join());
		logger.info("all threads paused");
		AccountData data = GlobalDataContainer.getAccountByName(userName);
		boolean pop = true;
		boolean imap = true;
		if (data.getPopServer() != null) {
			logger.info("check pop connection");
			pop = GlobalDataContainer.getConnectionByAccount(userName).checkPopConnection(data);
		}
		if (data.getImapServer() != null) {
			logger.info("check imap connection");
			imap = GlobalDataContainer.getConnectionByAccount(userName).checkImapConnection(data);
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
		if (new XMLFileManager(FrameManager.getProgramSetting("pathToAccountSettings")).getLookAndFeel()
				.equals("system")) {
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

		try (InputStream input = new FileInputStream("src/en.properties")) {
			languageProperties.load(input);

		} catch (IOException ex) {
			ex.printStackTrace();
		}
		try (InputStream input = new FileInputStream("src/settings.properties")) {
			programSettings.load(input);

		} catch (IOException ex) {
			ex.printStackTrace();
		}
		logger.info("programm start");
		configureTheame();
		mainFrame = new MainFrame();
		loadAccounts();
		logger.info("strat displaying main frame");
		// show main frame
		mainFrame.pack();
		mainFrame.setVisible(true);
		// run background threads to check for new mail for all accounts
		for (AccountData data : GlobalDataContainer.getAccounts()) {
			// new LoggerConfigurator().setUpLoggerForUser(data.getUserName());
			if (Boolean.parseBoolean(data.get("runInBackground"))) {
				if (GlobalDataContainer.getConnectionByAccount(data.getUserName()) != null) {
					logger.info("starting background thread for " + data.getUserName());
					MailLoader thread = new MailLoader(GlobalDataContainer.getConnectionByAccount(data.getUserName()),
							data, data.getImapServer() == null ? "pop" : "imap");
					thread.runAsThread();
					threads.put(data.getUserName(), thread);
					logger.info("background thread started");
				}
			}
		}
		// after main frame is closed
		mainFrame.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				logger.info("closing program");
				e.getWindow().dispose();
				logger.info("window disposed");
				if (!GlobalDataContainer.getAccounts().isEmpty()) {
					// wait until last mail update circle ends and close threads
					for (AccountData account : GlobalDataContainer.getAccounts()) {
						// close all connections for account
						if (Boolean.parseBoolean(account.get("runInBackground"))
								&& threads.get(account.getUserName()) != null) {
							logger.info("closing thread for " + account.getUserName());
							threads.get(account.getUserName()).join();
							logger.info("thread closed");
						}

						// rewrite account data to save changed data
						if (GlobalDataContainer.getConnectionByAccount(account.getUserName()) != null) {
							logger.info("closing connections for " + account.getUserName());
							GlobalDataContainer.getConnectionByAccount(account.getUserName()).closeAllSessions();
							logger.info("connections closed");
						}
						logger.info("rewriting xml");
						new XMLFileManager(FrameManager.getProgramSetting("pathToAccountSettings"))
								.rewriteAccount(account);
						logger.info("xml rewrote");
					}
				}
				System.exit(0);
			}
		});
	}
}
