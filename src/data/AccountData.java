package data;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class AccountData {
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
	private ArrayList<MailFolder> folders;
	private Date lastUpdateData;
	private boolean runInBackground;

	public Date getLastUpdateData() {
		return lastUpdateData;
	}

	public void setLastUpdateDate(Date lastUpdateData) {
		this.lastUpdateData = lastUpdateData;
	}

	public void set(String key, String value) {
		if (key.equals("email")) {
			this.email = value;
		} else if (key.equals("userName")) {
			this.userName = value;
		} else if (key.equals("userAuth")) {
			this.userAuth = value;
		} else if (key.equals("password")) {
			this.password = value;
		} else if (key.equals("popServer")) {
			this.popServer = value;
		} else if (key.equals("popPort")) {
			this.popPort = value;
		} else if (key.equals("imapServer")) {
			this.imapServer = value;
		} else if (key.equals("imapPort")) {
			this.imapPort = value;
		} else if (key.equals("smtpServer")) {
			this.smtpServer = value;
		} else if (key.equals("smtpPort")) {
			this.smtpPort = value;
		} else if (key.equals("lastUpdate")) {
			try {
				this.lastUpdateData = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss").parse(value);
			} catch (ParseException e) {
				e.printStackTrace();
			}
		} else if (key.equals("sslPop")) {
			this.sslPop = Boolean.parseBoolean(value);
		} else if (key.equals("sslImap")) {
			this.sslImap = Boolean.parseBoolean(value);
		} else if (key.equals("sslSmtp")) {
			this.sslSmtp = Boolean.parseBoolean(value);
		} else if (key.equals("tlsPop")) {
			this.tlsPop = Boolean.parseBoolean(value);
		} else if (key.equals("tlsImap")) {
			this.tlsImap = Boolean.parseBoolean(value);
		} else if (key.equals("tlsSmtp")) {
			this.tlsSmtp = Boolean.parseBoolean(value);
		} else if (key.equals("runInBackground")) {
			this.runInBackground = Boolean.parseBoolean(value);
		}
	}

	public String get(String key) {
		if (key.equals("email")) {
			return email;
		} else if (key.equals("userName")) {
			return userName;
		} else if (key.equals("userAuth")) {
			return userAuth;
		} else if (key.equals("password")) {
			return password;
		} else if (key.equals("popServer")) {
			return popServer;
		} else if (key.equals("popPort")) {
			return popPort;
		} else if (key.equals("imapServer")) {
			return imapServer;
		} else if (key.equals("imapPort")) {
			return imapPort;
		} else if (key.equals("smtpServer")) {
			return smtpServer;
		} else if (key.equals("smtpPort")) {
			return smtpPort;
		} else if (key.equals("lastUpdate")) {
			return new SimpleDateFormat("dd.MM.yyyy HH:mm:ss").format(lastUpdateData);
		} else if (key.equals("sslPop")) {
			return Boolean.toString(this.sslPop);
		} else if (key.equals("sslImap")) {
			return Boolean.toString(this.sslImap);
		} else if (key.equals("sslSmtp")) {
			return Boolean.toString(this.sslSmtp);
		} else if (key.equals("tlsPop")) {
			return Boolean.toString(this.sslPop);
		} else if (key.equals("tlsImap")) {
			return Boolean.toString(this.tlsImap);
		} else if (key.equals("tlsSmtp")) {
			return Boolean.toString(this.tlsSmtp);
		} else if (key.equals("runInBackground")) {
			return Boolean.toString(runInBackground);
		} else {
			return null;
		}
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
		folders.add(folder);
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
		return folders;
	}

	public void setFolders(ArrayList<MailFolder> folders) {
		this.folders = folders;
	}

	@Override
	public String toString() {
		return "AccountData [email=" + email + ", userName=" + userName + ", userAuth=" + userAuth + ", password="
				+ password + ", popServer=" + popServer + ", imapServer=" + imapServer + ", smtpServer=" + smtpServer
				+ ", popPort=" + popPort + ", imapPort=" + imapPort + ", smtpPort=" + smtpPort + ", savePass="
				+ savePass + ", folders=" + folders + "]";
	}

	@Override
	public boolean equals(Object o) {
		AccountData data = (AccountData) o;
		return data.getUserName().equals(this.getUserName());
	}
}
