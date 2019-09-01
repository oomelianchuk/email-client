package protokol;

import java.util.Properties;

import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Store;
import javax.swing.JOptionPane;

import data.AccountData;
import gui.FrameManager;

public class ConnectionCreatorImpl implements ConnectionCreator {
	private Properties smtpProps;

	@Override
	public
	/**
	 * Method to create pop/imap session
	 * 
	 * @param protokol imap or pop
	 * @param host     host of this mail service
	 * @param ssl      if ssl is needed for connection (needed when port!= 110 or
	 *                 143
	 * @param userAuth user name to log in on this mail service (usually user email)
	 * @param password password for this mail service
	 * @return store for created connection or null if connection was not successful
	 */
	Store createReadSession(String protokol, AccountData data) {

		// data initialization
		String protocolKeyWord = protokol.replaceAll("\\d", "");
		String port = data.get(protocolKeyWord + "Port");
		String host = data.get(protocolKeyWord + "Server");
		String userAuth = data.get("userAuth");
		String password = data.get("password");
		String ssl = data.get("ssl" + protocolKeyWord.substring(0, 1).toUpperCase() + protocolKeyWord.substring(1));
		String tls = data.get("tls" + protocolKeyWord.substring(0, 1).toUpperCase() + protocolKeyWord.substring(1));

		// configure properties
		Properties props = new Properties();
		props.setProperty("mail." + protokol + ".ssl.enable", ssl);
		props.setProperty("mail." + protokol + ".tls.enable", tls);
		if (data.get(protocolKeyWord + "Port") != null) {
			props.setProperty("mail." + protokol + ".port", port);
		}

		// create session
		Session session = Session.getInstance(props);
		Store store;
		try {
			store = session.getStore(protokol);
			FrameManager.logger.info("staring " + protokol + " connection for " + data.getUserName());
			store.connect(host, userAuth, password);
			FrameManager.logger.info(protokol + " connected for " + data.getUserName());
		} catch (Exception e) {
			FrameManager.logger.error(protokol + " connection failed for " + data.getUserName() + " : " + e.toString());
			return null;
		}
		return store;
	}

	@Override

	/**
	 * Method to create smtp session. Should be used for checking smtp connection,
	 * saves user authentication data in smtpAuthenticator, doesn't return any
	 * session object because smtp sessions usually exist for short time
	 * 
	 * @param host     host of this mail service
	 * @param ssl      if ssl is needed for connection (needed when port!= 25
	 * @param userAuth user name to log in on this mail service (usually user email)
	 * @param password password for this mail service
	 * @return store for created connection or null if connection was not successful
	 */
	public Session createSendSession(AccountData data) {

		// data initialization
		String port = data.get("smtpPort");
		String host = data.get("smtpServer");
		String userAuth = data.get("userAuth");
		String password = data.get("password");
		String ssl = data.get("sslSmtp");
		String tls = data.get("tlsSmtp");

		// configure properties
		smtpProps = new Properties();
		smtpProps.setProperty("mail.smtp.ssl.enable", ssl);
		smtpProps.setProperty("mail.smtp.tls.enable", tls);
		smtpProps.put("mail.smtp.host", host);
		smtpProps.put("mail.smtp.auth", "true");
		smtpProps.put("mail.stmp.port", port);
		smtpProps.setProperty("mail.user", userAuth);

		// create session
		SmtpAuthenticator smtpAuthenticator = new SmtpAuthenticator(userAuth, password);
		Session session = Session.getInstance(smtpProps, smtpAuthenticator);
		try {
			FrameManager.logger.info("staring smtp connection for " + data.getUserName());
			session.getTransport().close();
			FrameManager.logger.info("smtp connected for " + data.getUserName());
			return session;
		} catch (MessagingException e) {
			JOptionPane.showMessageDialog(FrameManager.mainFrame,
					"smtp session failed, please check your data or try later", "SMTP Conneciton failed",
					JOptionPane.ERROR_MESSAGE);
			FrameManager.logger.error("smtp connection failed for " + data.getUserName() + " : " + e.toString());
			e.printStackTrace();
			return null;
		}
	}
}
