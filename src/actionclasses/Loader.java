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
import data.MailFolder;
import filewriters.XMLFileManager;
import gui.AskPasswordFrame;
import gui.FrameManager;
import gui.mainframe.MainFrame;
import protokol.ConnectionManager;

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
		this.label.setText("configuring");
		// first get short information from xml file
		XMLFileManager xml = new XMLFileManager("src/accounts.xml");
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
				FrameManager.logger.info("account " + data.getUserName() + " setup");
				boolean foldersChecked = false;
				boolean mailChecked = false;
				// check connections
				connectionManager = new ConnectionManager();
				boolean passwordSaved = data.getPassword() != null;
				FrameManager.logger.info("password saved " + passwordSaved);

				// imap connection
				if (data.getImapServer() != null) {
					this.label.setText("check imap connection for " + data.getUserName() + "...");
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
						this.label.setText("imap connected");
						this.progressBar.setValue(this.progressBar.getValue() + valueOfProtocol / 3);

						// start folders checking
						this.label.setText("folders update for " + data.getUserName());
						updateFolders(data, connectionManager, "imap");
						foldersChecked = true;
						this.label.setText("folders updated");

						this.progressBar.setValue(this.progressBar.getValue() + valueOfProtocol / 3);
						this.label.setText("checking for new mail...");

						// update mail
						updateMail(data, connectionManager, "imap");
						mailChecked = true;

						this.label.setText("mail updated");
						this.progressBar.setValue(this.progressBar.getValue() + valueOfProtocol / 3);

					} else {
						// inform user that smtp connection failed
						this.label.setText("imap connection failed");
						JOptionPane.showMessageDialog(null, "Imap Connection failed", "Connetion faild",
								JOptionPane.ERROR_MESSAGE);
						this.progressBar.setValue(this.progressBar.getValue() + valueOfProtocol);
					}
				} else {
					// if there is no imap connection data just miss this part and set progress bar
					// filled as this part was already done
					this.progressBar.setValue(this.progressBar.getValue() + valueOfProtocol);
				}
				if (data.getPopServer() != null) {
					this.label.setText("check pop connection for " + data.getUserName() + "...");
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
						this.label.setText("pop connected");
						this.progressBar.setValue(this.progressBar.getValue() + valueOfProtocol / 3);

						if (!foldersChecked) {
							updateFolders(data, connectionManager, "pop");
							this.label.setText("folders updated");
							this.progressBar.setValue(this.progressBar.getValue() + valueOfProtocol / 3);
						} else {
							this.progressBar.setValue(this.progressBar.getValue() + valueOfProtocol / 3);
						}
						if (!mailChecked) {
							this.label.setText("checking for mail update");
							updateMail(data, connectionManager, "pop");
							this.label.setText("mail updated");
							this.progressBar.setValue(this.progressBar.getValue() + valueOfProtocol / 3);

						} else {
							this.progressBar.setValue(this.progressBar.getValue() + valueOfProtocol / 3);
						}
					} else {
						this.label.setText("Pop connection failed");
						JOptionPane.showMessageDialog(null, "Pop Connection failed", "Connetion faild",
								JOptionPane.ERROR_MESSAGE);
						this.progressBar.setValue(this.progressBar.getValue() + valueOfProtocol);
					}
				} else {
					this.progressBar.setValue(this.progressBar.getValue() + valueOfAccount / 3);
				}
				if (data.getSmtpServer() != null) {
					this.label.setText("checking smtp connection");
					boolean connectedSmtp = false;
					if (!passwordSaved) {
						askForPassword(data, connectionManager, "smtp");
						passwordSaved = true;
					} else {
						connectedSmtp = connectionManager.checkSmtpConnection(data);
					}
					if (connectedSmtp) {
						this.progressBar.setValue(this.progressBar.getValue() + valueOfProtocol);
					} else {
						this.label.setText("smtp connection failed");
						JOptionPane.showMessageDialog(null, "Smtp Connection failed", "Connetion faild",
								JOptionPane.ERROR_MESSAGE);
						this.progressBar.setValue(this.progressBar.getValue() + valueOfProtocol);
					}
				} else {
					this.progressBar.setValue(this.progressBar.getValue() + valueOfProtocol);
				}
				data.setLastUpdateDate(new Date());
				FrameManager.accounts.add(data);
				FrameManager.connections.put(data.getUserName(), connectionManager);
				mainFrame.addNewAccount(data);
				FrameManager.logger.info("configuration for account " + data.getUserName() + " finished");
			}
			this.progressBar.setValue(this.progressBar.getMaximum());
		}

	}

	private void updateFolders(AccountData data, ConnectionManager connectionManager, String protocol) {
		FrameManager.logger.info("update folders");
		// load folders from server
		ArrayList<MailFolder> newFolders = connectionManager.getFolders(protocol, data);
		if (data.getFolders() == null) {
			data.setFolders(new ArrayList<MailFolder>());
		}

		// place to store folders that are present on server and in user account
		ArrayList<MailFolder> newDataFolder = new ArrayList<MailFolder>();

		// for each folder in account check if it is present on server
		for (MailFolder folder : data.getFolders()) {

			// if there are any folder in account, that are absent on server
			if (!newFolders.contains(folder)) {
				FrameManager.logger.info(folder.getName() + " doesn't exist and should be deleted");
				this.label.setText(folder.getName() + " doesn't exist and should be deleted");
				String dir = "src/" + data.getUserName() + "/folders/"
						+ folder.getName().replaceAll("[", "").replaceAll("]", "");
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
				newDataFolder.add(folder);
			}
		}

		// set updated folders (if folder is absent on server it should stay in program
		// memory)
		data.setFolders(newDataFolder);

		// for each folder from server check if it is present in account
		for (MailFolder folder : newFolders) {

			// if folder is absent in account, add it
			if (!data.getFolders().contains(folder)) {
				FrameManager.logger.info(folder.getName() + "  was apsent and should be created");
				data.addFolder(folder);
				this.label.setText(folder.getName() + " was apsent and should be created");

				// and load all messages for this folder
				folder.getMessages()
						.addAll(connectionManager.downloadMail(new JProgressBar(), 0, protocol, data.getUserName(),
								folder.getName(), folder.getName().replaceAll("\\[", "").replaceAll("\\]", "")));
			}
		}
	}

	private void updateMail(AccountData data, ConnectionManager connectionManager, String protocol) {
		for (MailFolder folder : data.getFolders()) {
			folder.getMessages()
					.addAll(connectionManager.downloadMailAfterDate(protocol, data.getUserName(), folder.getName(),
							folder.getName().replaceAll("\\[", "").replaceAll("\\]", ""), data.getLastUpdateData(),
							null));
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
