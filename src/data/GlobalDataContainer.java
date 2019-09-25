package data;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import gui.FrameManager;
import gui.mainframe.MainFrame;
import gui.newaccountdialog.NewAccountDialog;
import protokol.ConnectionManager;

public class GlobalDataContainer {
	static {
		SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy/hh-mm-ss");
		System.setProperty("currentDate", dateFormat.format(new Date()));
	}
	private static MainFrame mainFrame;
	private static NewAccountDialog popUP;
	private static ArrayList<AccountData> accounts = new ArrayList<AccountData>();
	private static HashMap<String, ConnectionManager> connections = new HashMap<String, ConnectionManager>();
	public static final Logger LOGGER = LogManager.getLogger(FrameManager.class);

	public static AccountData getAccountByName(String userName) {
		AccountData accountToCompare = new AccountData();
		accountToCompare.set("userName", userName);
		return accounts.get(accounts.indexOf(accountToCompare));
	}

	public static ArrayList<AccountData> getAccounts() {
		return accounts;
	}

	public static void setAccounts(ArrayList<AccountData> accounts) {
		GlobalDataContainer.accounts = accounts;
	}

	public static void addAccount(AccountData account) {
		accounts.add(account);
	}

	public static ConnectionManager getConnectionByAccount(String accountName) {
		return connections.get(accountName);
	}

	public static void addConnection(String userName, ConnectionManager connection) {
		connections.put(userName, connection);
	}
	public static void deleteConnection(String userName) {
		connections.remove(userName);
	}
}
