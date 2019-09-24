package actionclasses;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JProgressBar;

import org.apache.commons.io.FileUtils;

import data.AccountData;
import data.GlobalDataContainer;
import data.MailFolder;
import filewriters.XMLFileManager;
import gui.AskPasswordFrame;
import gui.FrameManager;
import gui.mainframe.MainFrame;
import protokol.ConnectionManager;
import protokol.MessageContainer;

/**
 * class to load all for program needed data and display progress in progress
 * bar
 */
public class Loader implements BackgroundAction {
	private MainFrame mainFrame;
	private JLabel label;
	private JProgressBar progressBar;

	public Loader(MainFrame mainFrame) {
		this.mainFrame = mainFrame;
	}

	public void action(JProgressBar progressBar, JLabel label) {
		FrameManager.logger.info("start loading accounts");
		this.label = label;
		this.progressBar = progressBar;
		this.label.setText(FrameManager.getLanguageProperty("loader.label.configuring"));
		// first get short information from xml file
		XMLFileManager xml = new XMLFileManager(FrameManager.getProgramSetting("pathToAccountSettings"));
		ArrayList<AccountData> accountDatas = (ArrayList<AccountData>) xml.getAccountDaten();
		FrameManager.logger.info("data has been read");
		// than, if there are some account
		if (accountDatas.size() > 0) {
			ConnectionManager connectionManager;
			int numberOfAccounts = accountDatas.size();
			int valueOfAccount = this.progressBar.getMaximum() / numberOfAccounts;
			int valueOfProtocol = valueOfAccount / 3;
			// for each account
			for (AccountData data : accountDatas) {
				FrameManager.logger
						.info(FrameManager.getLanguageProperty("loader.label.accountSetup") + data.getUserName());
				boolean foldersChecked = false;
				boolean mailChecked = false;
				// check connections
				connectionManager = new ConnectionManager(data);
				boolean passwordSaved = data.getPassword() != null;
				FrameManager.logger.info("password saved " + passwordSaved);

				// imap connection
				if (data.getImapServer() != null) {
					this.label.setText(
							FrameManager.getLanguageProperty("loader.label.checkImap") + data.getUserName() + "...");
					boolean connectedImap = false;
					// if password already in temporary program memory, we don't need to ask it
					// again
					if (!passwordSaved) {
						askForPassword(data, connectionManager, "imap");
						passwordSaved = true;
					}
					// if password was already saved, just check connection
					else {
						connectedImap = connectionManager.checkImapConnection(data);
					}

					// now program can update user's mail data e.g. load new mail or check for new
					// folders
					if (connectedImap) {
						// inform user that connection succeed
						this.label.setText(FrameManager.getLanguageProperty("loader.label.imapconnected"));
						this.progressBar.setValue(this.progressBar.getValue() + valueOfProtocol / 3);

						// start folders checking
						this.label.setText(
								FrameManager.getLanguageProperty("loader.label.foldersUpdate") + data.getUserName());
						updateFolders(data, connectionManager, "imap");
						foldersChecked = true;
						this.label.setText(FrameManager.getLanguageProperty("loader.label.foldersUpdated"));

						this.progressBar.setValue(this.progressBar.getValue() + valueOfProtocol / 3);
						this.label.setText(FrameManager.getLanguageProperty("loader.label.checkForNewMail"));

						// update mail
						updateMail(data, connectionManager, "imap");
						mailChecked = true;

						this.label.setText(FrameManager.getLanguageProperty("loader.label.mailChecked"));
						this.progressBar.setValue(this.progressBar.getValue() + valueOfProtocol / 3);

					} else {
						// inform user that smtp connection failed
						this.label.setText(FrameManager.getLanguageProperty("error.imapFailed"));
						JOptionPane.showMessageDialog(null, FrameManager.getLanguageProperty("error.imapFailed"),
								FrameManager.getLanguageProperty("error.title.connectionFailed"),
								JOptionPane.ERROR_MESSAGE);
						this.progressBar.setValue(this.progressBar.getValue() + valueOfProtocol);
					}
				} else {
					// if there is no imap connection data just miss this part and set progress bar
					// filled as this part was already done
					this.progressBar.setValue(this.progressBar.getValue() + valueOfProtocol);
				}
				if (data.getPopServer() != null) {
					this.label.setText(
							FrameManager.getLanguageProperty("loader.label.checkPop") + data.getUserName() + "...");
					boolean connectedPop = false;

					// ask for password
					if (!passwordSaved) {
						askForPassword(data, connectionManager, "imap");
						passwordSaved = true;
					} else {
						connectedPop = connectionManager.checkPopConnection(data);
					}
					// update data if not updated
					if (connectedPop) {
						this.label.setText(FrameManager.getLanguageProperty("loader.label.popConnected"));
						this.progressBar.setValue(this.progressBar.getValue() + valueOfProtocol / 3);

						if (!foldersChecked) {
							this.label.setText(FrameManager.getLanguageProperty("loader.label.foldersUpdated"));
							updateFolders(data, connectionManager, "pop");
							this.progressBar.setValue(this.progressBar.getValue() + valueOfProtocol / 3);
							this.label.setText(FrameManager.getLanguageProperty("loader.label.checkForNewMail"));
						} else {
							this.progressBar.setValue(this.progressBar.getValue() + valueOfProtocol / 3);
						}
						if (!mailChecked) {
							this.label.setText(FrameManager.getLanguageProperty("loader.label.checkForNewMail"));
							// update mail
							updateMail(data, connectionManager, "pop");
							mailChecked = true;
							this.label.setText(FrameManager.getLanguageProperty("loader.label.mailChecked"));
							this.progressBar.setValue(this.progressBar.getValue() + valueOfProtocol / 3);

						} else {
							this.progressBar.setValue(this.progressBar.getValue() + valueOfProtocol / 3);
						}
					} else {
						this.label.setText(FrameManager.getLanguageProperty("error.popFailed"));
						JOptionPane.showMessageDialog(null, FrameManager.getLanguageProperty("error.popFailed"),
								FrameManager.getLanguageProperty("error.title.connectionFailed"),
								JOptionPane.ERROR_MESSAGE);
						this.progressBar.setValue(this.progressBar.getValue() + valueOfProtocol);
					}
				} else {
					this.progressBar.setValue(this.progressBar.getValue() + valueOfAccount / 3);
				}
				if (data.getSmtpServer() != null) {
					this.label.setText(FrameManager.getLanguageProperty("loader.label.checkSmtp") + data.getUserName());
					boolean connectedSmtp = false;
					if (!passwordSaved) {
						askForPassword(data, connectionManager, "smtp");
						passwordSaved = true;
					} else {
						connectedSmtp = connectionManager.checkSmtpConnection(data);
					}
					if (connectedSmtp) {
						this.progressBar.setValue(this.progressBar.getValue() + valueOfProtocol);
						this.label.setText(FrameManager.getLanguageProperty("loader.label.smtpConnected"));
					} else {
						this.label.setText(FrameManager.getLanguageProperty("error.smtpFailed"));
						JOptionPane.showMessageDialog(null, FrameManager.getLanguageProperty("error.smtpFailed"),
								FrameManager.getLanguageProperty("error.title.connectionFailed"),
								JOptionPane.ERROR_MESSAGE);
						this.progressBar.setValue(this.progressBar.getValue() + valueOfProtocol);
					}
				} else {
					this.progressBar.setValue(this.progressBar.getValue() + valueOfProtocol);
				}
				data.setLastUpdateDate(new Date());
				GlobalDataContainer.addAccount(data);
				GlobalDataContainer.addConnection(data.getUserName(), connectionManager);
				mainFrame.addNewAccount(data);
				FrameManager.logger.info("configuration for account " + data.getUserName() + " finished");
			}
			this.progressBar.setValue(this.progressBar.getMaximum());
		}

	}

	private void updateFolders(AccountData data, ConnectionManager connectionManager, String protocol) {
		FrameManager.logger.info("update folders");
		// load folders from server
		ArrayList<String> newFolders = connectionManager.getFolderNames(protocol, data);
		if (data.getFolders() == null) {
			data.setFolders(new ArrayList<MailFolder>());
		}

		// place to store folders that are present on server and in user account
		ArrayList<String> newDataFolder = new ArrayList<String>();
		// for each folder in account check if it is present on server
		for (String folderName : data.getFolderNames()) {

			// if there are any folder in account, that are absent on server
			if (!newFolders.contains(folderName)) {
				FrameManager.logger.info(folderName + FrameManager.getLanguageProperty("loader.label.folderNotExist"));
				this.label.setText(folderName + " doesn't exist and should be deleted");
				// TODO: remove path pattern
				String dir = FrameManager.getProgramSetting("pathToUserFolders")
						.replaceAll("\\{userName\\}", data.getUserName())
						.replaceAll("\\{folderName\\}", folderName.replaceAll("\\[", "").replaceAll("\\]", ""));
				// delete this folder with all mail
				try {
					if (new File(dir).exists()) {
						FrameManager.logger.info("delete " + dir);
						FileUtils.deleteDirectory(new File(dir));
					}
				} catch (IOException e) {
					FrameManager.logger.error("deleting directory : " + e.toString());
					e.printStackTrace();
				}
			}
			// add folder to temporary array (so if there
			else {
				newDataFolder.add(folderName);
			}
		}

		// set updated folders (if folder is absent on server it should stay in program
		// memory)
		data.setFolderNames(newDataFolder);

		// for each folder from server check if it is present in account
		for (String folder : newFolders) {

			// if folder is absent in account, add it
			if (!data.hasFolder(folder)) {
				FrameManager.logger.info(folder + "  was apsent and should be created");
				this.label.setText(folder + FrameManager.getLanguageProperty("loader.label.folderCreate"));
				// and load all messages for this folder
				ArrayList<MessageContainer> messages = connectionManager.downloadMail(new JProgressBar(), 0, protocol,
						data.getUserName(), folder, folder.replaceAll("\\[", "").replaceAll("\\]", ""));
				data.addFolder(new MailFolder(data.getUserName(), folder, messages));
			}
		}
	}

	private void updateMail(AccountData data, ConnectionManager connectionManager, String protocol) {
		for (String folderName : data.getFolderNames()) {
			// TODO: pass real logger
			data.addFolder(new MailFolder(data.getUserName(), folderName, connectionManager
					.downloadMailAfterDate(protocol, data.getUserName(), folderName, data.getLastUpdateData(), null)));

		}
	}

	private void askForPassword(AccountData data, ConnectionManager connectionManager, String protocol) {
		FrameManager.logger.info("ask for password");
		// if password not saved, ask for it
		AskPasswordFrame passwordFrame = new AskPasswordFrame(data.getUserName());
		String password = null;
		boolean savePassword = false;
		boolean connectedProtocol = false;
		// while imap connection won't succeed ask for password again (presumably
		// password is incorrect)
		while (!connectedProtocol) {
			password = passwordFrame.getPassword();
			data.set("password", password);
			savePassword = passwordFrame.getSavePass();
			if (protocol.equals("pop")) {
				connectedProtocol = connectionManager.checkPopConnection(data);
			} else {
				connectedProtocol = connectionManager.checkImapConnection(data);
			}
			if (!connectedProtocol) {
				passwordFrame.resetPassword();
			}
		}
		// if connection succeeded and user want to save the password, save it :)
		if (savePassword) {
			data.setSavePass(true);
		}
	}
}
