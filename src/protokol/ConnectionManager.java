package protokol;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;

import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Store;
import javax.swing.JProgressBar;

import org.apache.logging.log4j.Logger;

import data.AccountData;
import data.MailFolder;
import gui.FrameManager;

/**
 * This class is bridge between gui realization and mail server. It saves all
 * needed data for connection creation to make it easier to interact with mail
 * server from any part of gui realization. For each account one connection
 * manager instance will be created and only this connection object should be
 * used for all mail server interactions for this account
 */
public class ConnectionManager implements ConnectionCreator, MessageManager {
	private ConnectionCreator connectionCreator = new ConnectionCreatorImpl();
	private MessageManager messageManager = new MessageManagerImpl();
	private Store popSession;
	private Store imapSession;
	private AccountData data;

	public ConnectionManager(AccountData data) {
		this.data = data;
	}

	@Override
	public Store createReadSession(String protokol, AccountData data) {
		return connectionCreator.createReadSession(protokol, data);
	}

	@Override
	public Session createSendSession(AccountData data) {
		return connectionCreator.createSendSession(data);
	}

	/**
	 * Wrapper method to initialize all needed for pop connection variables
	 * 
	 * @param data user's account data, which contains host and port data
	 * @return boolean var if pop connection was successful
	 */
	public boolean checkPopConnection(AccountData data) {
		popSession = createReadSession("pop3", data);
		return popSession != null;
	}

	/**
	 * Wrapper method to initialize all needed for imap connection variables
	 * 
	 * @param data user's account data, which contains host and port data
	 * @return boolean var if imap connection was successful
	 */
	public boolean checkImapConnection(AccountData data) {
		imapSession = createReadSession("imap", data);
		return imapSession != null;
	}

	/**
	 * Wrapper method to initialize all needed for pop connection variables. Not
	 * like other methods with alike names, this method doesn't save any sessions
	 * because smtp session expire quickly
	 * 
	 * @param data user's account data, which contains host and port data
	 * @return boolean var if smtp connection was successful
	 */
	public boolean checkSmtpConnection(AccountData data) {
		return createSendSession(data) != null;
	}

	public void moveMessageToFolder(String newFolderName, MessageContainer messageContainer) throws MessagingException {
		if (imapSession != null) {
			moveMessageToFolder(imapSession, newFolderName, messageContainer);
		} else {
			moveMessageToFolder(popSession, newFolderName, messageContainer);
		}
	}

	@Override
	public void moveMessageToFolder(Store session, String newFolderName, MessageContainer messageContainer)
			throws MessagingException {
		messageManager.moveMessageToFolder(session, newFolderName, messageContainer);

	}

	public ArrayList<MailFolder> getFolders(String protocol, AccountData data) {
		if (protocol.equals("imap")) {
			return getFolders(imapSession, data);
		} else {
			return getFolders(popSession, data);
		}
	}

	@Override
	public ArrayList<MailFolder> getFolders(Store session, AccountData data) {
		return messageManager.getFolders(session, data);
	}

	public File downloadAttachment(String protocol, String path, String folderName, MessageContainer messageContainer,
			String attachmentName) {
		if (protocol.equals("imap")) {
			return messageManager.downloadAttachment(imapSession, path, folderName, messageContainer, attachmentName);
		} else {
			return messageManager.downloadAttachment(popSession, path, folderName, messageContainer, attachmentName);
		}
	}

	@Override
	public File downloadAttachment(Store session, String path, String folderName, MessageContainer messageContainer,
			String attachmentName) {
		return messageManager.downloadAttachment(session, path, folderName, messageContainer, attachmentName);
	}

	public void deleteMessage(String folderName, MessageContainer messageContainer) {
		if (imapSession != null) {
			deleteMessage(imapSession, folderName, messageContainer);
		} else {
			deleteMessage(popSession, folderName, messageContainer);
		}
	}

	@Override
	public void deleteMessage(Store session, String folderName, MessageContainer messageContainer) {
		messageManager.deleteMessage(session, folderName, messageContainer);
	}

	public void setMessageAsSeen(String folderName, MessageContainer messageContainer) {
		if (imapSession != null) {
			messageManager.setMessageAsSeen(imapSession, folderName, messageContainer);
		} else {
			messageManager.setMessageAsSeen(popSession, folderName, messageContainer);
		}
	}

	@Override
	public void setMessageAsSeen(Store session, String folderName, MessageContainer messageContainer) {
		messageManager.setMessageAsSeen(session, folderName, messageContainer);
	}

	public void forward(MessageContainer messageContainer, MessageContainer commentMessage, String text) {
		Session smtpSession = connectionCreator.createSendSession(data);
		if (imapSession != null) {
			forward(smtpSession, imapSession, messageContainer, commentMessage, text);
		} else {
			forward(smtpSession, popSession, messageContainer, commentMessage, text);
		}
	}

	@Override
	public void forward(Session smtpSession, Store session, MessageContainer messageContainer,
			MessageContainer commentMessage, String text) {
		messageManager.forward(smtpSession, session, messageContainer, commentMessage, text);
	}

	public void send(MessageContainer messageContainer, String text) {
		Session smtpSession = connectionCreator.createSendSession(data);
		send(smtpSession, messageContainer, text);
	}

	@Override
	public void send(Session smtpSession, MessageContainer messageContainer, String text) {
		messageManager.send(smtpSession, messageContainer, text);
	}

	public ArrayList<MessageContainer> downloadMailAfterDate(String protocol, String userName, String folderName,
			Date date, Logger logger) {
		if (protocol.equals("imap")) {
			return messageManager.downloadMailAfterDate(imapSession, userName, folderName, date, logger);
		} else {
			return messageManager.downloadMailAfterDate(popSession, userName, folderName, date, logger);
		}
	}

	@Override
	public ArrayList<MessageContainer> downloadMailAfterDate(Store session, String userName, String folderName,
			Date date, Logger logger) {
		return messageManager.downloadMailAfterDate(session, userName, folderName, date, logger);
	}

	public ArrayList<MessageContainer> downloadMail(JProgressBar progressBar, int folderValue, String protocol,
			String userName, String folderName, String path) {
		if (imapSession != null) {
			return downloadAllMail(imapSession, progressBar, folderValue, protocol, userName, folderName);
		} else {
			return downloadAllMail(popSession, progressBar, folderValue, protocol, userName, folderName);
		}
	}

	@Override
	public ArrayList<MessageContainer> downloadAllMail(Store session, JProgressBar progressBar, int folderValue,
			String protocol, String userName, String folderName) {
		return messageManager.downloadAllMail(session, progressBar, folderValue, protocol, userName, folderName);
	}

	/**
	 * closes all sessions, called on program end
	 */
	public void closeAllSessions() {
		FrameManager.logger.info("close all sessions");
		try {
			if (popSession != null) {
				popSession.close();
			}
			if (imapSession != null) {
				imapSession.close();
			}

		} catch (MessagingException e) {
			FrameManager.logger.error("closing sessions : " + e.toString());
		}
	}

}
