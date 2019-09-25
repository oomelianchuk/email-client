package protokol;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

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
import javax.swing.JOptionPane;
import javax.swing.JProgressBar;

import org.apache.logging.log4j.Logger;

import data.AccountData;
import gui.FrameManager;

public class MessageManagerImpl implements MessageManager {
	private boolean onUpdate = false;

	/**
	 * Methods to move message from one mail folder to another mail folder. Only for
	 * server folder applicable
	 * 
	 * @param newFolderName    folder to which message should be removed
	 * @param messageContainer message that should be removed
	 * @throws MessagingException if it's not possible to move message to this
	 *                            folder
	 */
	@Override
	public void moveMessageToFolder(Store session, String newFolderName, MessageContainer messageContainer)
			throws MessagingException {
		// open current messages' folder
		Folder folder = session.getDefaultFolder().getFolder(messageContainer.getFolderName());
		FrameManager.LOGGER.info("moving message to folder " + folder);
		openFolder(folder, Folder.READ_WRITE, messageContainer.getAccountName());

		// search for specified message
		Message[] ourMessage = new MessageSearcher().findMessage(folder, messageContainer);
		System.out.println(ourMessage[0].getSubject());
		// open folder to move message in
		Folder newFolder = session.getDefaultFolder().getFolder(newFolderName);
		openFolder(newFolder, Folder.READ_WRITE, messageContainer.getAccountName());
		FrameManager.LOGGER.info("copy messages from " + folder + " to " + newFolder);

		// copy message
		folder.copyMessages(ourMessage, newFolder);
		FrameManager.LOGGER.info("closing " + folder);

		// close all folders
		folder.close();
		FrameManager.LOGGER.info(folder + " closed");
		FrameManager.LOGGER.info("closing " + newFolder);
		newFolder.close();
		FrameManager.LOGGER.info(newFolder + " closed");
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
	@Override
	public ArrayList<String> getFolderNames(Store session, AccountData data) {
		// receiving folders
		FrameManager.LOGGER.info("receive folders");
		Folder[] folders = new Folder[0];
		try {
			folders = session.getDefaultFolder().list("*");
		} catch (MessagingException e) {
			FrameManager.LOGGER.error("while receiveing folders : " + e.toString());
		}
		ArrayList<String> folderNames = new ArrayList<String>();

		// convert folders to in app common format
		for (Folder folder : folders) {
			// not accessible folder
			if (!folder.getFullName().equals("[Gmail]")) {
				FrameManager.LOGGER.info("folder " + folder.getFullName() + " received");
				folderNames.add(folder.getName());
			}
		}
		FrameManager.LOGGER.info("all folders received");
		return folderNames;
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
	@Override
	public void downloadAttachment(Store session, String path, String folderName, MessageContainer messageContainer,
			String attachmentName) {
		FrameManager.LOGGER.info("starting attachment download to " + path);
		try {
			Folder folder = session.getDefaultFolder().getFolder(folderName);
			openFolder(folder, Folder.READ_ONLY, messageContainer.getAccountName());
			Message[] ourMessage = new MessageSearcher().findMessage(folder, messageContainer);
			saveAttachmentFile(ourMessage[0], path, attachmentName);
			folder.close();
		} catch (MessagingException e) {
			JOptionPane.showMessageDialog(FrameManager.mainFrame,
					FrameManager.getLanguageProperty("error.attachmentLoadingFailed"),
					FrameManager.getLanguageProperty("error.title.attachmentLoadingFailed"), JOptionPane.ERROR_MESSAGE);
			FrameManager.LOGGER.error("opening/closing folder/finding message : " + e.toString());
		}
	}

	private void saveAttachmentFile(Message message, String path, String attachmentName) {
		FrameManager.LOGGER.info("download attachment " + attachmentName);
		try {
			Multipart multipart = (Multipart) message.getContent();
			for (int i = 0; i < multipart.getCount(); i++) {
				BodyPart bodyPart = multipart.getBodyPart(i);
				if (!Part.ATTACHMENT.equalsIgnoreCase(bodyPart.getDisposition())) {
					continue; // dealing with attachments only
				}
				if (bodyPart.getFileName() != null && bodyPart.getFileName().contains(attachmentName)) {
					FrameManager.LOGGER.info("attachment found, strarting download");
					InputStream is = bodyPart.getInputStream();
					File f = new File(path + "/" + bodyPart.getFileName());
					FileOutputStream fos = new FileOutputStream(f);
					byte[] buf = new byte[4096];
					int bytesRead;
					while ((bytesRead = is.read(buf)) != -1) {
						fos.write(buf, 0, bytesRead);
					}
					fos.close();
				}
			}
		} catch (IOException | MessagingException e) {
			JOptionPane.showMessageDialog(FrameManager.mainFrame,
					FrameManager.getLanguageProperty("error.attachmentLoadingFailed"),
					FrameManager.getLanguageProperty("error.title.attachmentLoadingFailed"), JOptionPane.ERROR_MESSAGE);
			FrameManager.LOGGER.error("opening/closing folder/finding message : " + e.toString());
		}
	}

	/**
	 * Removes message from mail folder (not for hdd operations)
	 * 
	 * @param folderName       folder, where message is placed
	 * @param messageContainer short info about message
	 * 
	 */
	@Override
	public void deleteMessage(Store session, String folderName, MessageContainer messageContainer) {
		FrameManager.LOGGER.info("delete " + messageContainer + " from folder " + folderName + " on server");
		try {
			Folder folder = session.getDefaultFolder().getFolder(folderName);
			openFolder(folder, Folder.READ_WRITE, messageContainer.getAccountName());
			Message[] ourMessage = new MessageSearcher().findMessage(folder, messageContainer);
			ourMessage[0].setFlag(Flags.Flag.DELETED, true);
			folder.close();
		} catch (MessagingException e) {
			JOptionPane.showMessageDialog(FrameManager.mainFrame, "Message can not be deleted now, please try later",
					"Delete Error", JOptionPane.ERROR_MESSAGE);
			FrameManager.LOGGER.error("opening/closing folder/finding message : " + e.toString());
		}
	}

	/**
	 * Sets message as seen in mail folder
	 * 
	 * @param folderName       folder where message is placed
	 * @param messageContainer short information about message
	 */
	@Override
	public void setMessageAsSeen(Store session, String folderName, MessageContainer messageContainer) {
		FrameManager.LOGGER
				.info("set message  " + messageContainer + " from folder " + folderName + " as seen on server");
		try {
			Folder folder;
			while (onUpdate) {
			}
			folder = session.getDefaultFolder().getFolder(folderName);
			openFolder(folder, Folder.READ_WRITE, messageContainer.getAccountName());
			Message[] ourMessage = new MessageSearcher().findMessage(folder, messageContainer);
			ourMessage[0].setFlag(Flags.Flag.SEEN, true);
			folder.close();
		} catch (MessagingException e) {
			JOptionPane.showMessageDialog(FrameManager.mainFrame, "Connection failed", "Connection failed",
					JOptionPane.ERROR_MESSAGE);
			FrameManager.LOGGER.error("opening/closing folder/finding message : " + e.toString());
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
	@Override
	public void forward(Session smtpSession, Store session, MessageContainer messageContainer,
			MessageContainer commentMessage, String text) {
		FrameManager.LOGGER.info("forward message  " + messageContainer);
		try {
			FrameManager.LOGGER.info("finding the message on server");
			Folder folder = null;
			String[] splitedMessageBodyPath = messageContainer.getPath().split("/");

			String folderName = splitedMessageBodyPath[splitedMessageBodyPath.length - 3];
			if (splitedMessageBodyPath.length > 6) {
				folderName = splitedMessageBodyPath[splitedMessageBodyPath.length - 4] + "/"
						+ splitedMessageBodyPath[splitedMessageBodyPath.length - 3];
			}
			folder = session.getDefaultFolder().getFolder(folderName);

			openFolder(folder, Folder.READ_ONLY, folderName);
			Message ourMessage = new MessageSearcher().findMessage(folder, messageContainer)[0];
			FrameManager.LOGGER.info("message found");
			FrameManager.LOGGER.info("construct new message");
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

			Message forward = new MimeMessage(smtpSession);
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
				FrameManager.LOGGER.info("adding attachments from comment message");
				// Create your new message part
				if (commentMessage.getAttachments() != null) {
					for (String filename : commentMessage.getAttachments()) {
						addAttachmentFile(multipart, filename);
					}
				}
			}
			if (messageContainer.getAttachments() == null || messageContainer.getAttachments().isEmpty()) {
				FrameManager.LOGGER.info("adding attachments from forwarding message");
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
			FrameManager.LOGGER.info("sending");
			Transport.send(forward);
			FrameManager.LOGGER.info("sent");
			FrameManager.LOGGER.info("closing transport");
			smtpSession.getTransport().close();
			FrameManager.LOGGER.info("transport closed");
			FrameManager.LOGGER.info("closing folder");
			folder.close();
			FrameManager.LOGGER.info("folder closed");
		} catch (MessagingException | IOException e) {
			FrameManager.LOGGER.error("forwarding message : " + e.toString());
		}

	}

	/**
	 * Method to send message
	 * 
	 * @param messageContainer message to send
	 * @param text             message text
	 */
	@Override
	public void send(Session smtpSession, MessageContainer messageContainer, String text) {
		FrameManager.LOGGER.info("send message " + messageContainer);
		// Create the message part
		try {
			FrameManager.LOGGER.info("constructing message");
			Message message = new MimeMessage(smtpSession);
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
				FrameManager.LOGGER.info("add attachments");
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
			FrameManager.LOGGER.info("sending message");
			Transport.send(message);
			FrameManager.LOGGER.info("message sent");
			FrameManager.LOGGER.info("closing transport");
			smtpSession.getTransport().close();
			FrameManager.LOGGER.info("transport closed");
		} catch (MessagingException e) {
			FrameManager.LOGGER.error("sending message : " + e.toString());
		}

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
	@Override
	public ArrayList<MessageContainer> downloadMailAfterDate(Store session, String userName, String folderName,
			Date date, Logger logger) {
		if (logger == null) {
			logger = FrameManager.LOGGER;
		}
		// logger.info("download messages in folder " + folderName + " after date " +
		// date.toString());
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		calendar.add(Calendar.DATE, -1);
		Date yesterday = calendar.getTime();
		ArrayList<MessageContainer> newMessages = new ArrayList<MessageContainer>();
		try {
			// logger.info("search for messages after " + yesterday);
			Folder folder = session.getDefaultFolder().getFolder(folderName);

			openFolder(folder, Folder.READ_ONLY, userName, logger);
			Message[] messages = new MessageSearcher().findMessagesAfterDate(date, folder, userName, logger);
			for (Message message : messages) {
				newMessages.add(new MessageContainer(message, userName, folderName));
			}
			if (!newMessages.isEmpty()) {
				// logger.info("new messages were loaded in folder " + folderName);
			} else {
				// logger.info("no new messages found in folder" + folderName);

			}
			// logger.info("closing " + folder.getFullName() + " folder");
			folder.close();
			// logger.info("folder " + folder.getFullName() + " closed");
		} catch (MessagingException | IOException e) {
			logger.error("loading messages after date : " + e.toString());
		}
		return newMessages;
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
	@Override
	public ArrayList<MessageContainer> downloadAllMail(Store session, JProgressBar progressBar, int folderValue,
			String protocol, String userName, String folderName) {
		FrameManager.LOGGER.info("download all mail");
		try {
			Folder folder;
			folder = session.getDefaultFolder().getFolder(folderName);
			openFolder(folder, Folder.READ_ONLY, userName);
			ArrayList<MessageContainer> messages = new ArrayList<MessageContainer>();
			double messageValue = ((Integer) folderValue).doubleValue() / folder.getMessages().length;
			double summ = 0.0;
			for (Message message : folder.getMessages()) {
//				messages.add(
//						downloadMessage(new MessageContainer(message, userName, folderName), userName, folderName));
				messages.add(new MessageContainer(message, userName, folderName));
				summ += messageValue;
				if (summ >= 1.0) {
					progressBar.setValue(progressBar.getValue() + ((Double) summ).intValue());
					summ = 0.0;
				}
			}
			folder.close();
			return messages;
		} catch (MessagingException | IOException e) {
			FrameManager.LOGGER.error("loading mail : " + e.toString());
		}
		return null;
	}

	private void openFolder(Folder folder, int premission, String userName) {
		openFolder(folder, premission, userName, FrameManager.LOGGER);
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
				// logger.info("open folder " + folder.getFullName() + " for user " + userName);
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
	 * Method to attach attachment to message
	 * 
	 * @param multipart message content
	 * @param filename  path to file that should be attached
	 * @throws MessagingException
	 */
	private void addAttachmentFile(Multipart multipart, String filename) {
		FrameManager.LOGGER.info("add attachment " + filename + " to multipart message");
		BodyPart messageBodyPart = new MimeBodyPart();
		DataSource source = new FileDataSource(filename);
		try {
			messageBodyPart.setDataHandler(new DataHandler(source));
			String[] filenameSplited = filename.split("\\\\");
			messageBodyPart.setFileName(filenameSplited[filenameSplited.length - 1]);
			multipart.addBodyPart(messageBodyPart);
		} catch (MessagingException e) {
			FrameManager.LOGGER.error("adding attachment : " + e.toString());
		}
	}

	/**
	 * find message by short information about it (subject and received date)
	 * 
	 * @param folder           mail folder where searched message is placed
	 * @param messageContainer container for short info about message
	 * @return found message
	 * @throws MessagingException if folder is not accessible
	 */

}
