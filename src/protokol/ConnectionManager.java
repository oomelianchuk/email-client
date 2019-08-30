package protokol;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.Address;
import javax.mail.BodyPart;
import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.search.ComparisonTerm;
import javax.mail.search.ReceivedDateTerm;
import javax.mail.search.SearchTerm;
import javax.swing.JOptionPane;
import javax.swing.JProgressBar;

import org.apache.logging.log4j.Logger;

import data.AccountData;
import data.MailFolder;
import filewriters.FileManager;
import filewriters.XMLFileManager;
import gui.FrameManager;

/**
 * This class is bride between gui realisation and mail server. It saves all
 * needed data for connection creation to make it easier to interact with mail
 * server from any part of gui realisation. For each account one connection
 * manager instance will be created and only this connection object should be
 * used for all mail server interactions for this account
 */
public class ConnectionManager {
	private AccountData data;
	private Store popSession;
	private Store imapSession;
	// saves user password
	private SmtpAuthenticator smtpAuthenticator;
	// saves properties for smtp session creation
	private Properties smtpProps;
	private boolean onUpdate = false;

	/**
	 * Wrapper method to initialize all needed for pop connection variables
	 * 
	 * @param data user's account data, which contains host and port data
	 * @return boolean var if pop connection was successful
	 */
	public boolean checkPopConnection(AccountData data) {
		this.data = data;
		String protokol = "pop3";
		String host = data.getPopServer();
		String ssl = "true";
		String tls = "false";
		if (data.getPopPort() != null) {
			ssl = data.get("sslPop");
			tls = data.get("tlsPop");
		}
		String userAuth = data.getUserAuth();
		String password = data.getPassword();
		popSession = createReadSession(protokol, host, data.getPopPort(), ssl, tls, userAuth, password);
		return popSession != null;
	}

	/**
	 * Wrapper method to initialize all needed for imap connection variables
	 * 
	 * @param data user's account data, which contains host and port data
	 * @return boolean var if imap connection was successful
	 */
	public boolean checkImapConnection(AccountData data) {
		this.data = data;
		String protokol = "imap";
		String host = data.getImapServer();
		String ssl = "true";
		String tls = "false";
		if (data.getImapPort() != null) {
			ssl = data.get("sslImap");
			tls = data.get("tlsImap");
		}
		String userAuth = data.getUserAuth();
		String password = data.getPassword();
		imapSession = createReadSession(protokol, host, data.getPopPort(), ssl, tls, userAuth, password);
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
		this.data = data;
		String host = data.getSmtpServer();
		String port = data.getSmtpPort();
		String ssl = data.get("sslSmtp");
		String tls = data.get("tlsSmtp");
		String userAuth = data.getUserAuth();
		String password = data.getPassword();
		createSendSession(host, port, ssl, tls, userAuth, password);
		return smtpAuthenticator != null;
	}

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
	private Store createReadSession(String protokol, String host, String port, String ssl, String tls, String userAuth,
			String password) {
		Properties props = new Properties();
		props.setProperty("mail." + protokol + ".ssl.enable", ssl);
		props.setProperty("mail." + protokol + ".tls.enable", tls);
		if (port != null) {
			props.setProperty("mail." + protokol + ".port", port);
		}
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
	private SmtpAuthenticator createSendSession(String host, String port, String ssl, String tls, String userAuth,
			String password) {
		smtpProps = new Properties();
		smtpProps.setProperty("mail.smtp.ssl.enable", ssl);
		smtpProps.setProperty("mail.smtp.tls.enable", tls);
		smtpProps.put("mail.smtp.host", host);
		smtpProps.put("mail.smtp.auth", "true");
		smtpProps.put("mail.stmp.port", port);
		smtpProps.setProperty("mail.user", userAuth);
		smtpAuthenticator = new SmtpAuthenticator(userAuth, password);
		Session session = Session.getInstance(smtpProps, smtpAuthenticator);
		try {
			FrameManager.logger.info("staring smtp connection for " + data.getUserName());
			session.getTransport().close();
			FrameManager.logger.info("smtp connected for " + data.getUserName());
			return smtpAuthenticator;
		} catch (MessagingException e) {
			JOptionPane.showMessageDialog(FrameManager.mainFrame,
					"smtp session failed, please check your data or try later", "SMTP Conneciton failed",
					JOptionPane.ERROR_MESSAGE);
			FrameManager.logger.error("smtp connection failed for " + data.getUserName() + " : " + e.toString());
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Methods to move message from one mail folder to another mail folder. Only for
	 * server folder applicable
	 * 
	 * @param newFolderName    folder to which message should be removed
	 * @param messageContainer message that should be removed
	 * @throws MessagingException if it's not possible to move message to this
	 *                            folder
	 */
	public void moveMessageToFolder(String newFolderName, MessageContainer messageContainer) throws MessagingException {
		Folder folder = imapSession.getDefaultFolder().getFolder(messageContainer.getPathToMessageBody().split("/")[3]);
		FrameManager.logger.info("moving message to folder " + folder);
		openFolder(folder, Folder.READ_WRITE, messageContainer.getPathToMessageBody().split("/")[1]);
		SearchTerm searchCondition = new SearchTerm() {
			@Override
			public boolean match(Message message) {
				try {
					if (message.getSubject().equals(messageContainer.getSubject())
							& message.getReceivedDate().compareTo(messageContainer.getReceivedDate()) == 0) {
						return true;
					}
				} catch (MessagingException ex) {
					ex.printStackTrace();
				}
				return false;
			}
		};
		Message[] ourMessage = folder.search(searchCondition);
		Folder newFolder = imapSession.getDefaultFolder().getFolder(newFolderName);
		openFolder(newFolder, Folder.READ_WRITE, messageContainer.getPathToMessageBody().split("/")[1]);
		FrameManager.logger.info("copy messages from " + folder + " to " + newFolder);
		folder.copyMessages(ourMessage, newFolder);
		FrameManager.logger.info("closing " + folder);
		folder.close();
		FrameManager.logger.info(folder + " closed");
		FrameManager.logger.info("closing " + newFolder);
		newFolder.close();
		FrameManager.logger.info(newFolder + " closed");
	}

	/**
	 * Method to receive all mail folders of the account. This method is called
	 * after account creation (to receive folders) and program start (to check if
	 * all folders are loaded )
	 * 
	 * @param protocol imap/pop for which folders should be loaded (more folders for
	 *                 imap)
	 * @param data     info about account for which folders should be loaded
	 * @return list of folders
	 */
	public ArrayList<MailFolder> getFolders(String protocol, AccountData data) {
		FrameManager.logger.info("receive folders");
		Folder[] folders = new Folder[0];
		Store store = imapSession;
		if (protocol.equals("pop")) {
			store = popSession;
		}
		try {
			folders = store.getDefaultFolder().list("*");
		} catch (MessagingException e) {
			FrameManager.logger.error("while receiveing folders : " + e.toString());
		}
		ArrayList<MailFolder> folderNames = new ArrayList<MailFolder>();
		for (Folder folder : folders) {
			// not accessible folder
			if (!folder.getFullName().equals("[Gmail]")) {
				FrameManager.logger.info("folder " + folder.getFullName() + " received");
				folderNames.add(new MailFolder(folder.getFullName()));
			}
		}
		FrameManager.logger.info("all folders received");
		return folderNames;
	}

	/**
	 * find message by short information about it (subject and received date)
	 * 
	 * @param folder           mail folder where searched message is placed
	 * @param messageContainer container for short info about message
	 * @return found message
	 * @throws MessagingException if folder is not accessible
	 */
	private Message[] findMessage(Folder folder, MessageContainer messageContainer) throws MessagingException {
		FrameManager.logger.info("search for message " + messageContainer + " in folder " + folder.getFullName());
		SearchTerm searchCondition = new SearchTerm() {
			@Override
			public boolean match(Message message) {
				try {
					if (message.getSubject().equals(messageContainer.getSubject())
							& (message.getReceivedDate() != null
									&& message.getReceivedDate().compareTo(messageContainer.getReceivedDate()) == 0)
							|| (message.getSentDate() != null
									&& message.getSentDate().compareTo(messageContainer.getReceivedDate()) == 0)) {
						return true;
					}
				} catch (MessagingException ex) {
					FrameManager.logger.error("while searching message : " + ex.toString());
				} catch (NullPointerException e) {
					FrameManager.logger.error("while searching message : " + e.toString());
					return false;
				}
				return false;
			}
		};
		return folder.search(searchCondition);
	}

	/**
	 * Method to load attachment from message. This method finds Message (where
	 * contained InputStream to load attachment) and calls FileManager to load
	 * attachment on hdd
	 * 
	 * @param protocol         imap/pop
	 * @param path             path where to save loaded attachment
	 * @param folderName       folder, which contains message with attachment
	 * @param messageContainer short info about message
	 * @param attachmentName   name of attachment that shoud be loaded
	 * @return loaded file
	 */
	public File downloadAttachment(String protocol, String path, String folderName, MessageContainer messageContainer,
			String attachmentName) {
		FrameManager.logger.info("starting attachment download to " + path);
		if (protocol.equals("imap")) {
			try {
				Folder folder = imapSession.getDefaultFolder().getFolder(folderName);
				openFolder(folder, Folder.READ_ONLY, messageContainer.getPathToMessageBody().split("/")[1]);
				Message[] ourMessage = findMessage(folder, messageContainer);
				File result = new FileManager(path).downloadAttachment(ourMessage[0], attachmentName);
				folder.close();
				return result;
			} catch (MessagingException e) {
				JOptionPane.showMessageDialog(FrameManager.mainFrame, "Attachment loading failed",
						"Attachment loading failed", JOptionPane.ERROR_MESSAGE);
				FrameManager.logger.error("opening/closing folder/finding message : " + e.toString());
			}
		}
		return null;
	}

	/**
	 * Removes message from mail folder (not for hdd operations)
	 * 
	 * @param folderName       folder, where message is placed
	 * @param messageContainer short info about message
	 * 
	 */
	public void deleteMessage(String folderName, MessageContainer messageContainer) {
		FrameManager.logger.info("delete " + messageContainer + " from folder " + folderName + " on server");
		try {
			Folder folder = imapSession.getDefaultFolder().getFolder(folderName);
			openFolder(folder, Folder.READ_WRITE, messageContainer.getPathToMessageBody().split("/")[1]);
			Message[] ourMessage = findMessage(folder, messageContainer);
			ourMessage[0].setFlag(Flags.Flag.DELETED, true);
			folder.close();
		} catch (MessagingException e) {
			JOptionPane.showMessageDialog(FrameManager.mainFrame, "Message can not be deleted now, please try later",
					"Delete Error", JOptionPane.ERROR_MESSAGE);
			FrameManager.logger.error("opening/closing folder/finding message : " + e.toString());
		}
	}

	/**
	 * Sets message as seen in mail folder
	 * 
	 * @param folderName       folder where message is placed
	 * @param messageContainer short information about message
	 */
	public void setMessageAsSeen(String folderName, MessageContainer messageContainer) {
		FrameManager.logger
				.info("set message  " + messageContainer + " from folder " + folderName + " as seen on server");
		try {
			Folder folder;
			while (onUpdate) {
			}
			if (imapSession != null) {
				folder = imapSession.getDefaultFolder().getFolder(folderName);
			} else {
				folder = popSession.getDefaultFolder().getFolder(folderName);
			}
			openFolder(folder, Folder.READ_WRITE, messageContainer.getPathToMessageBody().split("/")[1]);
			Message[] ourMessage = findMessage(folder, messageContainer);
			ourMessage[0].setFlag(Flags.Flag.SEEN, true);
			folder.close();
		} catch (MessagingException e) {
			JOptionPane.showMessageDialog(FrameManager.mainFrame, "Connection failed", "Connection failed",
					JOptionPane.ERROR_MESSAGE);
			FrameManager.logger.error("opening/closing folder/finding message : " + e.toString());
		}
	}

	/**
	 * Method to forward old message
	 * 
	 * @param messageContainer old message
	 * @param commentMessage   message that contains comment to this message (also
	 *                         attachments)
	 * @param text             comment text
	 */
	public void forward(MessageContainer messageContainer, MessageContainer commentMessage, String text) {
		FrameManager.logger.info("forward message  " + messageContainer);
		try {
			FrameManager.logger.info("finding the message on server");
			Folder folder = null;
			String[] splitedMessageBodyPath = messageContainer.getPathToMessageBody().split("/");

			String folderName = splitedMessageBodyPath[splitedMessageBodyPath.length - 3];
			if (splitedMessageBodyPath.length > 6) {
				folderName = splitedMessageBodyPath[splitedMessageBodyPath.length - 4] + "/"
						+ splitedMessageBodyPath[splitedMessageBodyPath.length - 3];
			}
			if (imapSession != null) {
				folder = imapSession.getDefaultFolder().getFolder(folderName);
			} else {
				folder = popSession.getDefaultFolder().getFolder(folderName);
			}

			openFolder(folder, Folder.READ_ONLY, folderName);
			Message ourMessage = findMessage(folder, messageContainer)[0];
			FrameManager.logger.info("message found");
			FrameManager.logger.info("construct new message");
			// Fill in header
			String[] recipients = commentMessage.getTo().split(";");
			Address[] addresses = new Address[recipients.length];
			for (int i = 0; i < recipients.length; i++) {
				addresses[i] = new InternetAddress(recipients[i].replaceAll(" ", ""));
			}
			text += "\nForwarded message \n From: " + InternetAddress.toString(ourMessage.getFrom()) + "\nDate: "
					+ ourMessage.getReceivedDate() + "\nSubject: " + ourMessage.getSubject() + " \nTo: "
					+ InternetAddress.toString(ourMessage.getAllRecipients()) + "\n\n"
					+ messageContainer.getMessageText();
			// Create the message part
			FrameManager.logger
					.info("start smtp session with " + smtpProps.toString() + " and saved smtp authenticator");
			Session session = Session.getInstance(smtpProps, smtpAuthenticator);

			Message forward = new MimeMessage(session);
			forward.setRecipients(Message.RecipientType.TO, addresses);

			// Fill in header
			forward.setSubject(commentMessage.getSubject());
			forward.setFrom(new InternetAddress(commentMessage.getFrom()));

			// Create your new message part
			Multipart multipart = new MimeMultipart();
			BodyPart messageBodyPart = new MimeBodyPart();
			messageBodyPart.setText(text);
			multipart.addBodyPart(messageBodyPart);
			if (!commentMessage.getAttachments().isEmpty()) {
				FrameManager.logger.info("adding attachments from comment message");
				// Create your new message part
				if (commentMessage.getAttachments() != null) {
					for (String filename : commentMessage.getAttachments()) {
						addAttachmentFile(multipart, filename);
					}
				}
			}
			if (messageContainer.isHasAttachment()) {
				FrameManager.logger.info("adding attachments from forwarding message");
				for (int i = 0; i < ((Multipart) ourMessage.getContent()).getCount(); i++) {
					BodyPart bodyPart = ((Multipart) ourMessage.getContent()).getBodyPart(i);
					if (Part.ATTACHMENT.equalsIgnoreCase(bodyPart.getDisposition()) & bodyPart.getFileName() != null) {
						multipart.addBodyPart(bodyPart);
					}
				}
			}
			// Create a multi-part to combine the parts
			// Send message
			forward.setContent(multipart);
			FrameManager.logger.info("sending");
			Transport.send(forward);
			FrameManager.logger.info("sent");
			FrameManager.logger.info("closing transport");
			session.getTransport().close();
			FrameManager.logger.info("transport closed");
			FrameManager.logger.info("closing folder");
			folder.close();
			FrameManager.logger.info("folder closed");
		} catch (MessagingException | IOException e) {
			FrameManager.logger.error("forwarding message : " + e.toString());
		}
	}

	/**
	 * Method to send message
	 * 
	 * @param messageContainer message to send
	 * @param text             message text
	 */
	public void send(MessageContainer messageContainer, String text) {
		FrameManager.logger.info("send message " + messageContainer);
		// Create the message part
		try {
			FrameManager.logger
					.info("start smtp session with " + smtpProps.toString() + " and saved smtp authenticator");
			Session session = Session.getInstance(smtpProps, smtpAuthenticator);
			FrameManager.logger.info("constructing message");
			Message message = new MimeMessage(session);
			// fill header
			String[] recipients = messageContainer.getTo().split(";");
			Address[] addresses = new Address[recipients.length];
			for (int i = 0; i < recipients.length; i++) {
				addresses[i] = new InternetAddress(recipients[i].replaceAll(" ", ""));
			}

			message.setRecipients(Message.RecipientType.TO, addresses);
			message.setSubject(messageContainer.getSubject());
			message.setFrom(new InternetAddress(messageContainer.getFrom()));

			// add attachment
			if (!messageContainer.getAttachments().isEmpty()) {
				FrameManager.logger.info("add attachments");
				BodyPart messageBodyPartText = new MimeBodyPart();
				messageBodyPartText.setText(text);
				Multipart multipart = new MimeMultipart();
				multipart.addBodyPart(messageBodyPartText);
				// TODO: not needed?
//				if (messageContainer.getAttachments() != null) {
				for (String filename : messageContainer.getAttachments()) {
					addAttachmentFile(multipart, filename);
				}
//				}
				message.setContent(multipart);
			} else {
				message.setText(text);
			}

			// Send message
			FrameManager.logger.info("sending message");
			Transport.send(message);
			FrameManager.logger.info("message sent");
			FrameManager.logger.info("closing transport");
			session.getTransport().close();
			FrameManager.logger.info("transport closed");
		} catch (MessagingException e) {
			FrameManager.logger.error("sending message : " + e.toString());
		}
	}

	/**
	 * Method to attach attachment to message
	 * 
	 * @param multipart message content
	 * @param filename  path to file that should be attached
	 * @throws MessagingException
	 */
	private void addAttachmentFile(Multipart multipart, String filename) {
		FrameManager.logger.info("add attachment " + filename + " to multipart message");
		BodyPart messageBodyPart = new MimeBodyPart();
		DataSource source = new FileDataSource(filename);
		try {
			messageBodyPart.setDataHandler(new DataHandler(source));
			String[] filenameSplited = filename.split("\\\\");
			messageBodyPart.setFileName(filenameSplited[filenameSplited.length - 1]);
			multipart.addBodyPart(messageBodyPart);
		} catch (MessagingException e) {
			FrameManager.logger.error("adding attachment : " + e.toString());
		}
	}

	/**
	 * This method saves message in hdd
	 * 
	 * @param message    message that should be saved
	 * @param userName   user whom belongs this message
	 * @param folderName name of mail folder of this message
	 * @param path       where to save this message
	 * @return short info about message
	 */
	private MessageContainer downloadMessage(Message message, String userName, String folderName, String path) {
		FrameManager.logger.info("downloading message");
		try {
			FrameManager.logger.info("creating path to message");
			String messageXMLfolderName = "";
			String from = InternetAddress.toString(message.getFrom());
			if (from != null) {
				folderName = messageXMLfolderName + from;
			}
			String subject = message.getSubject();
			if (subject != null) {
				folderName = folderName + subject;
			}
			String recievedDate = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss")
					.format(message.getReceivedDate() != null ? message.getReceivedDate() : message.getSentDate());
			if (recievedDate == null) {
				recievedDate = "01.01.01 00:00:00";
			}
			folderName = folderName + recievedDate;
			if (from == null & subject == null & recievedDate == null) {
				messageXMLfolderName = "defaultFolder";
			} else {
				messageXMLfolderName = Integer.toString(folderName.hashCode());
			}

			// create folder to save message
			String filePath = "src/" + userName + "/folders/"
					+ message.getFolder().toString().replaceAll("\\]", "").replaceAll("\\[", "") + "/"
					+ messageXMLfolderName;
			FrameManager.logger.info("path to message " + filePath);
			FrameManager.logger.info("create directory");
			Files.createDirectories(Paths.get(filePath));
			FrameManager.logger.info("directory created");
			FrameManager.logger.info("creating xml file");
			// create xml file with short information about message
			File newFile = new File(filePath + "/mainMessage.xml");

			boolean created = newFile.createNewFile();
			FrameManager.logger.info("xml file created " + created);
			if (created) {
				BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(newFile, false)));
				out.write("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>");
				out.close();
			}

			XMLFileManager xmlWriter = new XMLFileManager(filePath + "/mainMessage.xml", "message");
			// take message apart (single out message text and html view ,determine if
			// message has attachment and if yes than saves it's file name
			MessageContainer messageContainer = new MessageContainer(message,
					"src/" + userName + "/folders/" + message.getFolder().toString() + "/" + messageXMLfolderName);
			// write short information in file
			xmlWriter.createMessageFile(messageContainer);
			return messageContainer;
		} catch (IOException | MessagingException e) {
			FrameManager.logger.error("downloading message " + e.toString());
		}
		return null;
	}

	/**
	 * This method loads messages after some date. Used to update mail.
	 * 
	 * @param protocol   imap/pop
	 * @param userName   name of user, for which message should be loaded
	 * @param folderName name of message folder
	 * @param path       where to save message
	 * @param date       messages after which date should be loaded
	 * @return
	 */
	public ArrayList<MessageContainer> downloadMailAfterDate(String protocol, String userName, String folderName,
			String path, Date date, Logger logger) {
		if (logger == null) {
			logger = FrameManager.logger;
		}
		logger.info("download messages in folder " + folderName + " after date " + date.toString());
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		calendar.add(Calendar.DATE, -1);
		Date yesterday = calendar.getTime();
		ArrayList<MessageContainer> messageInfos = new ArrayList<MessageContainer>();
		Message[] messages = new Message[0];
		Folder folder;
		try {
			logger.info("search for messages after " + yesterday);
			if (protocol.equals("pop")) {
				folder = popSession.getDefaultFolder().getFolder(folderName);

			} else {
				folder = imapSession.getDefaultFolder().getFolder(folderName);
			}
			// because standard javamail search-by-date method doesn't take time into
			// account, it will be first search for mail after the day before needed date
			SearchTerm newerThan = new ReceivedDateTerm(ComparisonTerm.GT, yesterday);
			openFolder(folder, Folder.READ_ONLY, userName, logger);
			messages = folder.search(newerThan);
			logger.info("search for messages after " + yesterday);
			logger.info("search for messages after " + date);
			// and than "manually" search for needed date and time
			for (Message message : messages) {
				if (message.getReceivedDate().compareTo(date) > 0) {
					MessageContainer messageContainer = downloadMessage(message, userName, folderName, path);
					if (messageContainer != null) {
						messageInfos.add(messageContainer);
					}
				}
			}
			if (!messageInfos.isEmpty()) {
				logger.info("new messages were loaded in folder " + folderName);
			} else {
				logger.info("no new messages found in folder" + folderName);

			}
			logger.info("closing " + folder.getFullName() + " folder");
			folder.close();
			logger.info("folder " + folder.getFullName() + " closed");
		} catch (MessagingException e) {
			logger.error("loading messages after date : " + e.toString());
		}
		return messageInfos;
	}

	private void openFolder(Folder folder, int premission, String userName) {
		openFolder(folder, premission, userName, FrameManager.logger);
	}

	private void openFolder(Folder folder, int premission, String userName, Logger logger) {
		if (!folder.isOpen()) {
			try {
				if (onUpdate) {
					logger.info("waiting for update finish on folder " + folder.getFullName());
					while (onUpdate) {
					}
					logger.info("update finished");
				}
				logger.info("open folder " + folder.getFullName() + " for user " + userName);
				folder.open(premission);
			} catch (MessagingException e) {
				try {
					logger.warn("error opeining folder (maybe to many operations at the same time) " + e.toString());
					logger.info("waite for 10000 ms");
					Thread.sleep(10000);
				} catch (InterruptedException e1) {
					logger.error("thread interrupted : " + e1.toString());
				}
				logger.info("set connections on update");
				onUpdate = true;
				boolean connected = FrameManager.updateConnections(userName);
				while (!connected) {
					logger.info("try to restore connections");
					try {
						logger.info("wait for 10000 ms");
						Thread.sleep(100000);
					} catch (InterruptedException e1) {
						logger.error("thread interrupted : " + e1.toString());
					}
					connected = FrameManager.updateConnections(userName);
				}
				logger.info("connections restored");
				try {
					logger.info("wait for 5000 ms");
					Thread.sleep(5000);
				} catch (InterruptedException e1) {
					logger.error("thread interrupted : " + e1.toString());
				}
				try {
					logger.info("try to open folder");
					folder.open(premission);
				} catch (MessagingException e1) {
					logger.error("failed to open folder : " + e1.toString());
				}
				onUpdate = false;
			}
		}
	}

	/**
	 * download all mail from mail folder (called only on account creation)
	 * 
	 * @param protocol   imap/pop
	 * @param userName   user for which mail folder should be loaded
	 * @param folderName name of folder that should be loaded
	 * @param path       path where to load this folder
	 * @return
	 */
	public ArrayList<MessageContainer> downloadMail(JProgressBar progressBar, int folderValue, String protocol,
			String userName, String folderName, String path) {
		FrameManager.logger.info("download all mail");
		try {
			Folder folder;
			if (protocol.equals("pop")) {
				folder = popSession.getDefaultFolder().getFolder(folderName);
			} else {
				folder = imapSession.getDefaultFolder().getFolder(folderName);
			}
			openFolder(folder, Folder.READ_ONLY, userName);
			ArrayList<MessageContainer> messages = new ArrayList<MessageContainer>();
			double messageValue = ((Integer) folderValue).doubleValue() / folder.getMessages().length;
			double summ = 0.0;
			for (Message message : folder.getMessages()) {
				messages.add(downloadMessage(message, userName, folderName, path));
				summ += messageValue;
				if (summ >= 1.0) {
					progressBar.setValue(progressBar.getValue() + ((Double) summ).intValue());
					summ = 0.0;
				}
			}
			folder.close();
			return messages;
		} catch (MessagingException e) {
			FrameManager.logger.error("loading mail : " + e.toString());
		}
		return null;
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
