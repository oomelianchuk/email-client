package data;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import javax.swing.JOptionPane;

import gui.FrameManager;

public class AccountData implements Serializable {
	private String email;
	private String userName;
	private String userAuth;
	private String password;
	private String popServer;
	private boolean sslPop;
	private boolean tlsPop;
	private String imapServer;
	private boolean sslImap;
	private boolean tlsImap;
	private String smtpServer;
	private boolean sslSmtp;
	private boolean tlsSmtp;
	private String popPort;
	private String imapPort;
	private String smtpPort;
	private boolean savePass;
	private ArrayList<String> folderNames = new ArrayList<String>();
	private HashMap<String, MailFolder> folders = new HashMap<String, MailFolder>();
	private Date lastUpdateData;
	private boolean runInBackground;

	public static ArrayList<AccountData> deserializeAccounts() {
		ArrayList<AccountData> accountDatas = new ArrayList<AccountData>();
		File folder = new File("src/accounts");
		if (folder.listFiles() != null) {
			for (File messageFolder : folder.listFiles()) {
				accountDatas.add(deserializeAccount(messageFolder));
			}
		}
		return accountDatas;
	}

	public static AccountData deserializeAccount(File fileName) {
		try {
			if (fileName.exists()) {
				ObjectInputStream in = new ObjectInputStream(
						new FileInputStream(fileName.getAbsoluteFile() + "/accounts.out"));
				AccountData messageInfo = (AccountData) in.readObject();
				in.close();
				return messageInfo;
			}
		} catch (IOException | ClassNotFoundException e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(null, FrameManager.getLanguageProperty("error.messageLoadFailedForFolder"),
					FrameManager.getLanguageProperty("error.title.messageLoadFailedForFolder"),
					JOptionPane.ERROR_MESSAGE);
		}
		return new AccountData();
	}

	public Date getLastUpdateData() {
		return lastUpdateData;
	}

	public void setLastUpdateDate(Date lastUpdateData) {
		this.lastUpdateData = lastUpdateData;
	}

	public boolean isSslPop() {
		return sslPop;
	}

	public void setSslPop(boolean sslPop) {
		this.sslPop = sslPop;
	}

	public boolean isTlsPop() {
		return tlsPop;
	}

	public void setTlsPop(boolean tlsPop) {
		this.tlsPop = tlsPop;
	}

	public boolean isSslImap() {
		return sslImap;
	}

	public void setSslImap(boolean sslImap) {
		this.sslImap = sslImap;
	}

	public boolean isTlsImap() {
		return tlsImap;
	}

	public void setTlsImap(boolean tlsImap) {
		this.tlsImap = tlsImap;
	}

	public boolean isSslSmtp() {
		return sslSmtp;
	}

	public void setSslSmtp(boolean sslSmtp) {
		this.sslSmtp = sslSmtp;
	}

	public boolean isTlsSmtp() {
		return tlsSmtp;
	}

	public void setTlsSmtp(boolean tlsSmtp) {
		this.tlsSmtp = tlsSmtp;
	}

	public boolean isRunInBackground() {
		return runInBackground;
	}

	public void setRunInBackground(boolean runInBackground) {
		this.runInBackground = runInBackground;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public void setUserAuth(String userAuth) {
		this.userAuth = userAuth;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public void setPopServer(String popServer) {
		this.popServer = popServer;
	}

	public void setImapServer(String imapServer) {
		this.imapServer = imapServer;
	}

	public void setSmtpServer(String smtpServer) {
		this.smtpServer = smtpServer;
	}

	public void setPopPort(String popPort) {
		this.popPort = popPort;
	}

	public void setImapPort(String imapPort) {
		this.imapPort = imapPort;
	}

	public void setSmtpPort(String smtpPort) {
		this.smtpPort = smtpPort;
	}

	public void setFolders(HashMap<String, MailFolder> folders) {
		this.folders = folders;
	}

	public void setLastUpdateData(Date lastUpdateData) {
		this.lastUpdateData = lastUpdateData;
	}

	public String getPort(String protocol) {
		if (protocol.equals("pop")) {
			return this.popPort;
		} else if (protocol.equals("imap")) {
			return this.imapPort;
		} else if (protocol.equals("smtp")) {
			return this.smtpPort;
		} else {
			return null;
		}
	}

	public String getServer(String protocol) {
		if (protocol.equals("pop")) {
			return this.popServer;
		} else if (protocol.equals("imap")) {
			return this.imapServer;
		} else if (protocol.equals("smtp")) {
			return this.smtpServer;
		} else {
			return null;
		}
	}

	public ArrayList<String> getFolderNames() {
		return new ArrayList<String>(folders.keySet());
	}

	public String getPopServer() {
		return popServer;
	}

	public String getImapServer() {
		return imapServer;
	}

	public String getSmtpServer() {
		return smtpServer;
	}

	public String getPopPort() {
		return popPort;
	}

	public String getImapPort() {
		return imapPort;
	}

	public String getSmtpPort() {
		return smtpPort;
	}

	public void addFolder(MailFolder folder) {
		if (folders.containsKey(folder.getName())) {
			if (folders.get(folder.getName()) != null) {
				folders.get(folder.getName()).addAllMessages(folder.getMessages());
			} else {
				folders.remove(folder.getName());
				folders.put(folder.getName(), folder);
			}
		} else {
			if (!folderNames.contains(folder.getName())) {
				folderNames.add(folder.getName());
			}
			folders.put(folder.getName(), folder);
		}
	}

	public MailFolder getFolderByName(String folderName) {
		return folders.get(folderName);
	}

	public String getEmail() {
		return email;
	}

	public String getUserName() {
		return userName;
	}

	public String getUserAuth() {
		return userAuth;
	}

	public String getPassword() {
		return password;

	}

	public boolean isSavePass() {
		return savePass;
	}

	public void setSavePass(boolean savePass) {
		this.savePass = savePass;
	}

	public ArrayList<MailFolder> getFolders() {
		return new ArrayList<MailFolder>(folders.values());
	}

	public void setFolders(ArrayList<MailFolder> folders) {
		folders.forEach(folder -> {
			this.folders.put(folder.getName(), folder);
			if (!folderNames.contains(folder.getName())) {
				folderNames.add(folder.getName());
			}
		});
	}

	public void setFolderNames(ArrayList<String> folderNames) {
		folderNames.forEach(folderName -> {
			addFolderName(folderName);
			if (!folderNames.contains(folderName)) {
				folderNames.add(folderName);
			}
		});
	}

	public void addFolderName(String folderName) {
		if (!folderNames.contains(folderName)) {
			folderNames.add(folderName);
		}
		folders.put(folderName, null);
	}

	public void serialize() throws IOException {
		Files.createDirectories(Paths.get("src/accounts/" + userName));
		ObjectOutputStream out = new ObjectOutputStream(
				new FileOutputStream("src/accounts/" + userName + "/accounts.out"));
		out.writeObject(this);
		out.close();
	}

	public void rename(String newName) {
		new File("src/accounts/" + userName + "/accounts.out").delete();
		this.userName = newName;
		try {
			serialize();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public String toString() {
		return "AccountData {email='" + email + "', userName='" + userName + "', userAuth='" + userAuth
				+ "', password='" + password + "', popServer='" + popServer + "', sslPop='" + sslPop + "', tlsPop='"
				+ tlsPop + "', imapServer='" + imapServer + "', sslImap='" + sslImap + "', tlsImap='" + tlsImap
				+ "', smtpServer='" + smtpServer + "', sslSmtp='" + sslSmtp + "', tlsSmtp='" + tlsSmtp + "', popPort='"
				+ popPort + "', imapPort='" + imapPort + "', smtpPort='" + smtpPort + "', folders='" + folders + "'}\n";
	}

	@Override
	public boolean equals(Object o) {
		AccountData data = (AccountData) o;
		return data.getUserName().equals(this.getUserName());
	}

	public boolean hasFolder(String folderName) {
		return folders.containsKey(folderName);
	}
}
