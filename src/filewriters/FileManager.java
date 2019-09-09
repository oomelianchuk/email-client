package filewriters;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.swing.JOptionPane;

import data.MailFolder;
import gui.FrameManager;
import protokol.MessageContainer;

public class FileManager {
	private File mainDir;
	private String mainDirRelativPath;

	public FileManager(String folderPath) {
		mainDirRelativPath = folderPath;
		this.mainDir = new File(folderPath);
	}

	public ArrayList<MessageContainer> getMessages(String userName) {
		ArrayList<MessageContainer> messages = new ArrayList<MessageContainer>();
		FrameManager.logger.info("get messages from folder " + mainDir + " for user " + userName);
		MailFolder folder = new MailFolder(mainDir.getAbsolutePath());
		messages = folder.getMessages();
		/*
		 * if (mainDir.exists()) { File[] messageFolders = mainDir.listFiles(); for
		 * (File folder : messageFolders) { if (new File(folder.getAbsolutePath() +
		 * "/mainMessage.xml").exists()) { XMLFileManager xml = new
		 * XMLFileManager((mainDirRelativPath + "/" + folder.getName().replaceAll("\\]",
		 * "").replaceAll("\\[", "") + "/mainMessage.xml")); MessageContainer m =
		 * xml.parseMessage(); messages.add(m); } } Collections.sort(messages); }
		 */
		return messages;
	}

	public File downloadAttachment(Message message, String attachmentName) {
		try {
			FrameManager.logger.info("download attachment " + attachmentName);
			Multipart multipart = (Multipart) message.getContent();

			for (int i = 0; i < multipart.getCount(); i++) {
				BodyPart bodyPart = multipart.getBodyPart(i);
				if (!Part.ATTACHMENT.equalsIgnoreCase(bodyPart.getDisposition())) {
					continue; // dealing with attachments only
				}
				if (bodyPart.getFileName() != null && bodyPart.getFileName().contains(attachmentName)) {
					FrameManager.logger.info("attachment found, strarting download");
					InputStream is = bodyPart.getInputStream();
					File f = new File(mainDirRelativPath + "/" + bodyPart.getFileName());
					FileOutputStream fos = new FileOutputStream(f);
					byte[] buf = new byte[4096];
					int bytesRead;
					while ((bytesRead = is.read(buf)) != -1) {
						fos.write(buf, 0, bytesRead);
					}
					fos.close();
					return f;
				}
			}
		} catch (IOException | MessagingException e) {
			JOptionPane.showMessageDialog(FrameManager.mainFrame, "Error loading attachment",
					"Error loading attachment", JOptionPane.ERROR_MESSAGE);
			FrameManager.logger.error("while loading attachment : " + e.toString());
		}
		return null;

	}

}
