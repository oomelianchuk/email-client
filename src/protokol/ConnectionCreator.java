package protokol;

import javax.mail.Session;
import javax.mail.Store;

import data.AccountData;

public interface ConnectionCreator {
	Store createReadSession(String protokol, AccountData data);

	Session createSendSession(AccountData data);

	boolean updateConnection(String userName);
}
