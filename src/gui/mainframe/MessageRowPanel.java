package gui.mainframe;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

import javax.mail.MessagingException;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.SwingWorker;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

import data.AccountData;
import data.GlobalDataContainer;
import gui.FrameManager;
import protokol.ConnectionManager;
import protokol.MessageContainer;

/**
 * This class represents message line on message panel. This should have access
 * to message it represents, so it can display short information about it, make
 * some operations e.g. move message to another folder, forward it etc. and give
 * needed information to objects, which are responsible for displaying message
 */
public class MessageRowPanel extends JPanel {
	// standard color to display seen message
	// represented message
	private MessageContainer message;
	private final Color SEEN_MESSAGE_COLOR = new Color(176, 224, 230);
	private final Color UNSEEN_MESSAGE_COLOR = new Color(30, 144, 255);
	private Color currentColor = UNSEEN_MESSAGE_COLOR;
	JCheckBox checked;

	public MessageRowPanel(MessageContainer message, Color lineBorderColor) {
		this.message = message;
		String subject = message.getSubject();
		String sender = message.getFrom();
		DateFormat format = new SimpleDateFormat(FrameManager.getLanguageProperty("dateFormat"));
		String recieveDate = format.format(message.getReceivedDate());
		if (message.isSeen()) {
			currentColor = SEEN_MESSAGE_COLOR;
		}
		this.setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
		this.setBorder(new LineBorder(lineBorderColor));

		// checkox
		checked = new JCheckBox();
		checked.setBorder(new EmptyBorder(10, 10, 10, 10));

		// subject label
		JLabel subjectLabel = new JLabel(subject);
		subjectLabel.setMaximumSize(new Dimension(200, 20));
		subjectLabel.setPreferredSize(new Dimension(150, 20));
		subjectLabel.setBorder(new EmptyBorder(10, 30, 10, 10));

		// sender label
		JLabel senderLabel = new JLabel(sender);
		senderLabel.setMaximumSize(new Dimension(500, 20));
		senderLabel.setPreferredSize(new Dimension(150, 20));
		senderLabel.setBorder(new EmptyBorder(10, 50, 10, 10));

		// date label
		JLabel dateLabel = new JLabel(recieveDate);
		dateLabel.setBorder(new EmptyBorder(10, 10, 10, 30));
		dateLabel.setMaximumSize(new Dimension(500, 20));
		dateLabel.setPreferredSize(new Dimension(150, 20));

		// make message row be always displayed till the end of the frame
		Double size = Toolkit.getDefaultToolkit().getScreenSize().getWidth();
		this.setPreferredSize(new Dimension(size.intValue(), 20));
		this.setMaximumSize(new Dimension(size.intValue(), 20));
		this.setMinimumSize(new Dimension(size.intValue(), 20));
		this.add(checked);
		this.add(subjectLabel);
		this.add(senderLabel);
		this.add(dateLabel);
	}

	/**
	 * This method will be called from control panel. It hides current message row,
	 * force message removing on mail server (by calling connection object) and on
	 * hard disk
	 * 
	 * @param folder -- name of folder to move message to
	 */
	public void moveToFolder(String folder) {
		Thread hdd=new Thread(new Runnable() {
			
			@Override
			public void run() {
				message.moveToFolder(folder);
			}
		});
		Thread server = new Thread(new Runnable() {
			
			@Override
			public void run() {
				try {
					GlobalDataContainer.getConnectionByAccount(message.getAccountName()).moveMessageToFolder(folder, message);
				} catch (MessagingException e) {
					e.printStackTrace();
				}
			}
		});
		hdd.start();
		server.start();
		try {
			hdd.join();
			server.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		this.setVisible(false);
	}

	/**
	 * This method will be called form MessagePanel so it can handle click on
	 * message row and that set it unchecked
	 */
	public void setUnchecked() {
		checked.setSelected(false);
	}

	/**
	 * This method will be called form MessagePanel to set all messages checked
	 */
	public void setChecked() {
		checked.setSelected(true);
	}

	/**
	 * This method should be used for reply message displaying
	 */
	public void displayReplyMessage() {
		checked.setSelected(true);

		// get user email
		String userEmail =GlobalDataContainer.getAccountByName(message.getAccountName()).getEmail();

		// get all email on which the original message was sent and set them as
		// recipients
		String recipient = (message.getFrom() + message.getTo().replaceAll(userEmail, ""))
				.replaceAll("(\\w+\\s+)*\\<", "").replaceAll("\\>", "");

		// create reply message display object
		OpenedMessagePanel oPanel = new OpenedMessagePanel(message.getAccountName(), userEmail, recipient,
				message.getSubject(), message.getMessageText());

		// display reply message under message list
		if (FrameManager.mainFrame.getRightPart().getClass().equals(MessagesPanel.class)) {
			MessagesPanel messagesPanel = (MessagesPanel) FrameManager.mainFrame.getRightPart();
			messagesPanel.addOpenedMessagePanel(oPanel);
		} else if (FrameManager.mainFrame.getRightPart().getClass().equals(JSplitPane.class)) {
			JSplitPane splitPanel = (JSplitPane) FrameManager.mainFrame.getRightPart();
			splitPanel.setTopComponent(splitPanel.getTopComponent());
			splitPanel.setDividerLocation(150);
			splitPanel.setBottomComponent(oPanel);
		}
	}

	/**
	 * This method should be used for simple or forward message displaying
	 * 
	 * @param forwarding boolean if method should display simple message or forward
	 */
	public void displayMessage(boolean forwarding) {
		checked.setSelected(true);
		OpenedMessagePanel openedMessage = new OpenedMessagePanel(message, forwarding);
		if (!forwarding) {
			openedMessage.setActionOnReplyButton(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					displayReplyMessage();
				}
			});
		}
		if (FrameManager.mainFrame.getRightPart().getClass().equals(MessagesPanel.class)) {
			MessagesPanel messagesPanel = (MessagesPanel) FrameManager.mainFrame.getRightPart();
			messagesPanel.addOpenedMessagePanel(openedMessage);
		} else if (FrameManager.mainFrame.getRightPart().getClass().equals(JSplitPane.class)) {
			JSplitPane splitPanel = (JSplitPane) FrameManager.mainFrame.getRightPart();
			splitPanel.setTopComponent(splitPanel.getTopComponent());
			splitPanel.setDividerLocation(150);
			splitPanel.setBottomComponent(openedMessage);
		}
		// if message hasn't been already seen, set it as seen
		if (!message.isSeen()) {
			// change color to seen message color
			currentColor = SEEN_MESSAGE_COLOR;
			repaint();

			// /change information on message object
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
						e.printStackTrace();
					}
					ConnectionManager connectionManager =GlobalDataContainer.getConnectionByAccount(message.getAccountName());
					connectionManager.setMessageAsSeen(message.getFolderName(), message);
					return null;
				}
			}.execute();
		}
	}

	public void delete() throws IOException {
		this.setVisible(false);
		// delegate object destroying to object itself
		this.message.delete();
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		Graphics2D g2d = (Graphics2D) g;
		g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
		int w = getWidth();
		int h = getHeight();
		Color color1 = Color.white;
		GradientPaint gp = new GradientPaint(0, 0, color1, 0, h, currentColor);
		g2d.setPaint(gp);
		g2d.fillRect(0, 0, w, h);
	}

	@Override
	public String toString() {
		return "MessageRowPanel [message=" + message + "]";
	}

}
