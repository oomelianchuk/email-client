package actionclasses;

import java.util.ArrayList;
import java.util.Date;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JProgressBar;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import data.AccountData;
import data.MailFolder;
import gui.FrameManager;
import protokol.ConnectionManager;
import protokol.MessageContainer;

/**
 * Class to load mail after account creation as well as new mail after program
 * start
 *
 */
public class MailLoader implements BackgroundAction, Runnable {
	private AccountData data;
	private String protocol;
	private ConnectionManager connectionManager;
	private boolean active = true;
	private Thread thread;
	private final Logger LOGGER;

	public MailLoader(ConnectionManager connectionManager, AccountData data, String protocol) {
		this.data = data;
		this.protocol = protocol;
		this.connectionManager = connectionManager;
		LOGGER = LogManager.getLogger(MailLoader.class.getName() + "-" + data.getUserName().replaceAll(" ", ""));
	}

	/**
	 * to load all mail after account creation
	 */
	@Override
	public void action(JProgressBar progressBar, JLabel label) {
		FrameManager.LOGGER.info("start loading mail");
		label.setText(FrameManager.getLanguageProperty("mailLoader.label.mailLoad"));
		int folderValue = progressBar.getMaximum() / data.getFolders().size();
		for (MailFolder folder : data.getFolders()) {
			label.setText(FrameManager.getLanguageProperty("mailLoader.label.folderLoad") + folder.getName());
			connectionManager.downloadMail(progressBar, folderValue, protocol, data.getUserName(), folder.getName(),
					folder.getName().replaceAll("\\[", "").replaceAll("\\]", ""));

			// progressBar.setValue(progressBar.getValue() + folderValue);
		}
		progressBar.setValue(progressBar.getMaximum());
		data.setLastUpdateDate(new Date());
	}

	/**
	 * to load new mails after program start
	 */
	public void action() {
		LOGGER.info("loading messages in background");
		boolean newMessage = false;
		ArrayList<MessageContainer> messages = new ArrayList<MessageContainer>();
		for (int i = 0; i < data.getFolders().size() && data.getFolders().get(i) != null; i++) {
			messages = connectionManager.downloadMailAfterDate(protocol, data.getUserName(),
					data.getFolders().get(i).getName().replaceAll("\\[", "").replaceAll("\\]", ""),
					data.getLastUpdateData() == null ? new Date() : data.getLastUpdateData(), LOGGER);
			// if new message is loaded for any folder, check other folders for new messages
			// (gmail receive messages in different folders at the same time)
			if (!messages.isEmpty()) {
				newMessage = true;
				messages = new ArrayList<MessageContainer>();
				for (int j = 0; j < data.getFolders().size(); j++) {
					messages = connectionManager.downloadMailAfterDate(protocol, data.getUserName(),
							data.getFolders().get(j).getName().replaceAll("\\[", "").replaceAll("\\]", ""),
							data.getLastUpdateData() == null ? new Date() : data.getLastUpdateData(), LOGGER);
					data.getFolders().get(j).getMessages().addAll(messages);
				}
				break;
			}
		}
		if (newMessage) {
			JOptionPane.showMessageDialog(FrameManager.mainFrame,
					FrameManager.getLanguageProperty("popup.newMessage").replaceAll("\\{userName\\}",
							data.getUserName()),
					FrameManager.getLanguageProperty("popup.title.newMessage"), JOptionPane.PLAIN_MESSAGE);
			data.setLastUpdateDate(new Date());
			LOGGER.info("new messages loaded");
		}
	}

	@Override
	public void run() {
		while (active) {
			action();
		}
	}

	public void runAsThread() {
		active = true;
		thread = new Thread(this);
		thread.start();
	}

	public void join() {
		FrameManager.LOGGER.info("joining thread");
		LOGGER.info("joining thread");
		active = false;
		try {
			thread.join();
		} catch (InterruptedException e) {
			LOGGER.error("thread interrupted : " + e.toString());
		}
	}
}
