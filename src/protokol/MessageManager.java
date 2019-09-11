package protokol;

import java.util.ArrayList;
import java.util.Date;

import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Store;
import javax.swing.JProgressBar;

import org.apache.logging.log4j.Logger;

import data.AccountData;

public interface MessageManager {
	void moveMessageToFolder(Store session, String newFolderName, MessageContainer messageContainer)
			throws MessagingException;

	ArrayList<String> getFolderNames(Store session, AccountData data);

	void downloadAttachment(Store session, String path, String folderName, MessageContainer messageContainer,
			String attachmentName);

	void deleteMessage(Store session, String folderName, MessageContainer messageContainer);

	void setMessageAsSeen(Store session, String folderName, MessageContainer messageContainer);

	void forward(Session smtpSession, Store session, MessageContainer messageContainer, MessageContainer commentMessage,
			String text);

	void send(Session smtpSession, MessageContainer messageContainer, String text);

	ArrayList<MessageContainer> downloadMailAfterDate(Store session, String userName, String folderName, Date date,
			Logger logger);

	ArrayList<MessageContainer> downloadAllMail(Store session, JProgressBar progressBar, int folderValue,
			String protocol, String userName, String folderName);
}
