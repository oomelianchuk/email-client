package managers;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import javax.mail.MessagingException;
import javax.swing.JOptionPane;
import javax.swing.SwingWorker;

import org.apache.commons.io.FileUtils;

import data.AccountData;
import data.GlobalDataContainer;
import data.MailFolder;
import filewriters.XMLFileManager;
import gui.FrameManager;
import protokol.ConnectionManager;
import protokol.MessageContainer;

public class MessagesManager {

	public void moveMessageToFolder(MessageContainer message, String folder) {
		// get path to message folder
		String messagePath = message.getPath().replaceAll("/messageBody\\d+.txt", "");
		String[] oldMessageBodyPathSplited = message.getPath().split("/");
		String messageBodyName = oldMessageBodyPathSplited[oldMessageBodyPathSplited.length - 1];
		String userName = message.getAccountName();
		String oldFolder = message.getFolderName();
		// call connection manages to move folder on mail server
		try {
			FrameManager.connections.get(userName).moveMessageToFolder(folder, message);
			// remove message from temporary program memory (if it present)
			// TODO: move this piece to method
			// "getAccountByName"/"getFolderForAccount/getConnectionForAccount in
			// FrameManager /
			AccountData accountToCompare = new AccountData();
			accountToCompare.set("userName", userName);
			GlobalDataContainer.getAccountByName(userName).getFolderByName(folder).getMessages()
			.remove(message);
			// change message path
			String newMessagePath = messagePath.replaceAll("/" + oldFolder + "/", "/" + folder + "/");

			// if there is no real folder (on hdd) for new folder, create it
			try {
				Files.createDirectories(Paths.get(newMessagePath.replaceAll("\\]", "").replaceAll("\\[", "")));
			} catch (IOException e) {
				e.printStackTrace();
				JOptionPane.showMessageDialog(FrameManager.mainFrame, "Error: new message folder couldn't be created",
						"Error occureed", JOptionPane.ERROR_MESSAGE);
			}

			// move all files from old hdd folder to new
			for (File oldFiles : new File(message.getPath()).listFiles()) {
				oldFiles.renameTo(new File(oldFiles.getAbsolutePath().replaceAll(
						message.getFolderName(),
						folder.replaceAll("\\]", "").replaceAll("\\[", ""))));
			}
			// delete old hdd message folder
			try {
				FileUtils.deleteDirectory(new File(messagePath.replaceAll("\\]", "").replaceAll("\\[", "")));
			} catch (IOException e) {
				e.printStackTrace();
				JOptionPane.showMessageDialog(FrameManager.mainFrame, "Error: old message folder couldn't be deleted",
						"Error occureed", JOptionPane.ERROR_MESSAGE);
			}

			// change message path for message
			message.setPath(newMessagePath + messageBodyName);

		} catch (MessagingException e1) {
			e1.printStackTrace();
			JOptionPane.showMessageDialog(FrameManager.mainFrame, "Not possible to move message to this folder",
					"Error", JOptionPane.ERROR_MESSAGE);
		}
	}

	public void setAsSeen(MessageContainer message) {

		// set message on hdd and on mail server as seen
		// setting message as seen on mail server takes relative much time, so it will
		// be done in swing worker not't to make program view hang
		new SwingWorker<Void, Void>() {
			@Override
			protected Void doInBackground() {
				message.setSeen(true);
				try {
					message.serialize();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
/*				new XMLFileManager(message.getPath().replaceAll("/messageBody\\d+.txt", "").replaceAll("\\]", "")
						.replaceAll("\\[", "") + "/mainMessage.xml").setMessageAsSeen();*/
				ConnectionManager connectionManager = FrameManager.connections.get((message.getAccountName()));
				connectionManager.setMessageAsSeen(message.getFolderName(), message);
				return null;
			}
		}.execute();
	}
}
