package gui.mainframe;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingWorker;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

import data.AccountData;
import data.GlobalDataContainer;
import gui.FrameManager;
import protokol.MessageContainer;

/**
 * This class is responsible for displaying all kinds of opened messages
 * (simple, forward, reply and new)
 */
public class OpenedMessagePanel extends JPanel {
	private JPanel attachAndHtml;
	private JTextArea messageText;
	private JPanel html;
	private JPanel attach;
	private JButton replyButton;
	private JPanel attachmentsPanel = new JPanel();
	private JButton showHTMLButton;
	private InfoOpenedMessagePanel info;
	private JScrollPane scrollMessage;

	// constructor for simple and forward displaying
	public OpenedMessagePanel(MessageContainer message, boolean forwarding) {
		super();
		this.setLayout(new BorderLayout());
		// configure info panel
		info = new InfoOpenedMessagePanel(forwarding);
		if (forwarding) {
			info.setSender(GlobalDataContainer.getAccountByName(message.getAccountName()).getEmail());
			info.setSubject("Fwd: " + message.getSubject());
		} else {
			info.setSender(message.getFrom());
			info.setTo(message.getTo());
			info.setSubject(message.getSubject());
			info.setDate(new SimpleDateFormat("dd.MM.yyyy HH:mm:ss").format(message.getReceivedDate()));
		}

		// used for forwarding
		// in comment message will be mostly stored attachments, so they can be append
		// to message before sending
		MessageContainer commentMessage = new MessageContainer(info.getSender(), info.getTo(), info.getSubject(),
				new Date(), false, "");

		// buttons to interact with attachments and html files
		attachAndHtml = new JPanel();
		attachAndHtml.setLayout(new BorderLayout());
		attach = new JPanel();
		attach.setLayout(new BoxLayout(attach, BoxLayout.X_AXIS));
		attach.setBorder(new EmptyBorder(10, 10, 10, 10));

		configureShowAttachmentPanel(message);
		configureShowHtmlPanel(message);

		//
		if (forwarding) {
			configureForwardingPanel(message, commentMessage);
		} else {
			// if message is not forwarding message it should have reply button
			configureReplyPanel();
		}

		messageText = new JTextArea(forwarding ? "" : message.getMessageText());
		messageText.setFont(new Font("TimesRoman", Font.PLAIN, 14));
		messageText.setEditable(forwarding);
		scrollMessage = new JScrollPane(messageText);
		messageText.setBorder(new CompoundBorder(new LineBorder(Color.GRAY), new EmptyBorder(10, 10, 10, 10)));

		this.add(info, BorderLayout.NORTH);
		this.add(scrollMessage, BorderLayout.CENTER);
		this.add(new JPanel(), BorderLayout.WEST);
		this.add(new JPanel(), BorderLayout.EAST);
		this.add(new JPanel(), BorderLayout.SOUTH);

	}

	private void configureShowHtmlPanel(MessageContainer message) {
		// show htmp panel
		html = new JPanel();
		html.setLayout(new BoxLayout(html, BoxLayout.X_AXIS));
		html.setBorder(new EmptyBorder(10, 10, 10, 10));

		showHTMLButton = new JButton();
		showHTMLButton.setMaximumSize(new Dimension(150, 60));
		showHTMLButton.setLayout(new BorderLayout());
		JLabel northHtml = new JLabel("This message has");
		JLabel centerHtml = new JLabel("html view");
		JLabel southHtml = new JLabel("Show it!");

		showHTMLButton.add(BorderLayout.NORTH, northHtml);
		showHTMLButton.add(BorderLayout.CENTER, centerHtml);
		showHTMLButton.add(BorderLayout.SOUTH, southHtml);
		html.add(showHTMLButton);

		// only if message has html view this panel should be displayed -> attached to
		// main panel (attachAndHtml)
		if (!message.getHtmlFiles().isEmpty()) {
			attachAndHtml.add(html, BorderLayout.CENTER);
		}
		showHTMLButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				remove(scrollMessage);
				showHtmlpart(scrollMessage, message.getHtmlFiles().get(0).replaceAll("\\]", "").replaceAll("\\[", ""));

				repaint();
				FrameManager.mainFrame.setVisible(false);
				FrameManager.mainFrame.setVisible(true);
			}
		});
		info.add(attachAndHtml);
	}

	private void configureShowAttachmentPanel(MessageContainer message) {
		// show attachment panel
		JButton showAttachmentButton = new JButton();
		showAttachmentButton.setMaximumSize(new Dimension(150, 60));
		showAttachmentButton.setLayout(new BorderLayout());
		JLabel northAttach = new JLabel("This message has");
		JLabel centerAttach = new JLabel("attachment");
		JLabel southAttach = new JLabel("Show it!");

		showAttachmentButton.add(BorderLayout.NORTH, northAttach);
		showAttachmentButton.add(BorderLayout.CENTER, centerAttach);
		showAttachmentButton.add(BorderLayout.SOUTH, southAttach);
		showAttachmentButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				showAttachment(message);
			}
		});
		attach.add(showAttachmentButton);
		// only if message has attachment this panel should be displayed -> attached to
		// main panel (attachAndHtml)
		if (!(message.getAttachments() == null || message.getAttachments().isEmpty())) {
			attachAndHtml.add(attach, BorderLayout.WEST);
		}

	}

	private void configureForwardingPanel(MessageContainer message, MessageContainer commentMessage) {
		JPanel forwardPanel = new JPanel();
		forwardPanel.setLayout(new BorderLayout());
		forwardPanel.setBorder(new EmptyBorder(10, 10, 10, 60));

		// inform user that forwarded message is attached and he/she can write comment's
		// to it below
		JPanel infoForwardingPanel = new JPanel();
		infoForwardingPanel.setBorder(new EmptyBorder(10, 10, 10, 60));
		infoForwardingPanel.setLayout(new BoxLayout(infoForwardingPanel, BoxLayout.Y_AXIS));
		JLabel infoForwarding1 = new JLabel("your message is attached");
		JLabel infoForwarding2 = new JLabel("please write your comments below");
		infoForwardingPanel.add(infoForwarding1);
		infoForwardingPanel.add(infoForwarding2);
		forwardPanel.add(infoForwardingPanel, BorderLayout.WEST);

		// button to add attachment to forwarded message
		attach = new JPanel();
		attach.setLayout(new BoxLayout(attach, BoxLayout.X_AXIS));
		attach.setBorder(new EmptyBorder(10, 10, 10, 10));
		JButton addAttachmenttButton = new JButton("Add attachment");
		addAttachmenttButton.setMaximumSize(new Dimension(150, 60));
		addAttachmenttButton.setLayout(new BorderLayout());
		addAttachmenttButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				addAttachment(attach, commentMessage);
			}
		});
		attach.add(addAttachmenttButton);
		forwardPanel.add(attach);

		// send buttom
		JButton sendButton = new JButton("Send");
		sendButton.setMaximumSize(new Dimension(100, 60));
		forwardPanel.add(sendButton, BorderLayout.EAST);
		sendButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// check if displayed email is valid
				if (validateMatch(info.getTo())) {
					// get information from fields
					commentMessage.setFrom(info.getSender().trim());
					commentMessage.setTo(info.getTo().trim());
					commentMessage.setSubject(info.getSubject());
					new SwingWorker<Void, Void>() {
						protected Void doInBackground() {
							GlobalDataContainer.getConnectionByAccount(message.getAccountName()).forward(message,
									commentMessage, messageText.getText());
							// after message is send - info message "sent"
							JOptionPane.showMessageDialog(FrameManager.mainFrame, "your message sent", "Message sent",
									JOptionPane.PLAIN_MESSAGE);
							// hide opened message panel
							OpenedMessagePanel.this.setVisible(false);
							return null;
						}
					}.execute();

				}
			}
		});
		attachAndHtml.add(forwardPanel, BorderLayout.CENTER);
	}

	private void configureReplyPanel() {
		JPanel replyPanel = new JPanel();
		replyPanel.setLayout(new BoxLayout(replyPanel, BoxLayout.X_AXIS));
		replyPanel.setBorder(new EmptyBorder(10, 10, 10, 60));
		replyButton = new JButton("Reply");
		replyButton.setMaximumSize(new Dimension(100, 60));
		replyPanel.add(replyButton);
		attachAndHtml.add(replyPanel, BorderLayout.EAST);
	}

	public void setActionOnReplyButton(ActionListener actionListener) {
		replyButton.addActionListener(actionListener);
	}

	// reply and send message
	public OpenedMessagePanel(String userName, String from, String to, String subject, String text) {
		super();
		MessageContainer message = new MessageContainer(from, to, subject, new Date(), true, "");

		this.setLayout(new BorderLayout());
		InfoOpenedMessagePanel info = new InfoOpenedMessagePanel(true);
		info.setSender(from);
		AccountData accountToCompare = new AccountData();
		accountToCompare.set("userName", userName);
		info.setTo(to);
		info.setSubject(subject);
		JPanel attachAndSend = new JPanel();
		attachAndSend.setLayout(new BorderLayout());
		JPanel attach = new JPanel();
		attach.setLayout(new BoxLayout(attach, BoxLayout.X_AXIS));
		attach.setBorder(new EmptyBorder(10, 10, 10, 10));

		JButton addAttachmenttButton = new JButton("Add attachment");
		addAttachmenttButton.setMaximumSize(new Dimension(150, 60));
		addAttachmenttButton.setLayout(new BorderLayout());
		addAttachmenttButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				addAttachment(attachAndSend, message);
			}
		});
		attach.add(addAttachmenttButton);
		attachAndSend.add(attach, BorderLayout.WEST);
		JPanel sendPanel = new JPanel();
		sendPanel.setLayout(new BorderLayout());
		sendPanel.setBorder(new EmptyBorder(10, 10, 10, 60));
		JButton sendButton = new JButton("Send");
		sendButton.setMaximumSize(new Dimension(100, 60));
		sendPanel.add(sendButton, BorderLayout.EAST);
		sendButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// check if displayed email is valid
				if (validateMatch(info.getTo())) {
					message.setFrom(info.getSender().trim());
					message.setTo(info.getTo().trim());
					message.setSubject(info.getSubject());
					GlobalDataContainer.getConnectionByAccount(userName).send(message, messageText.getText());
					JOptionPane.showMessageDialog(FrameManager.mainFrame, "your message sent", "Message sent",
							JOptionPane.PLAIN_MESSAGE);
					OpenedMessagePanel.this.setVisible(false);
				}
			}
		});
		attachAndSend.add(sendPanel, BorderLayout.CENTER);
		info.add(attachAndSend);
		messageText = new JTextArea(text.equals("") ? "" : "Quote: " + text);
		messageText.setFont(new Font("TimesRoman", Font.PLAIN, 14));
		JScrollPane scrollMessage = new JScrollPane(messageText);
		messageText.setBorder(new CompoundBorder(new LineBorder(Color.GRAY), new EmptyBorder(10, 10, 10, 10)));
		this.add(info, BorderLayout.NORTH);
		this.add(scrollMessage, BorderLayout.CENTER);
		this.add(new JPanel(), BorderLayout.WEST);
		this.add(new JPanel(), BorderLayout.EAST);
		this.add(new JPanel(), BorderLayout.SOUTH);

	}

	private void addAttachment(JPanel attachAndSend, MessageContainer message) {
		JFileChooser fc = new JFileChooser();
		fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
		int returnVal = fc.showSaveDialog(null);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			String file = fc.getSelectedFile().getPath();
			message.addAttachment(file);
			attachmentsPanel.setLayout(new BoxLayout(attachmentsPanel, BoxLayout.Y_AXIS));
			JPanel attachmentPanel = new JPanel();
			attachmentPanel.setLayout(new BoxLayout(attachmentPanel, BoxLayout.X_AXIS));
			attachmentPanel.setBorder(new EmptyBorder(10, 50, 10, 10));
			JLabel attachment = new JLabel(fc.getSelectedFile().getAbsolutePath());
			attachment.setBorder(new EmptyBorder(0, 10, 0, 10));
			JButton remove = new JButton("remove");
			remove.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					message.getAttachments().remove(file);
					attachmentsPanel.remove(attachmentPanel);
				}
			});
			attachmentPanel.add(remove);
			attachmentPanel.add(attachment);
			attachmentsPanel.add(attachmentPanel);
			attachAndSend.add(attachmentsPanel, BorderLayout.EAST);
			repaint();
			FrameManager.mainFrame.repaint();
			FrameManager.mainFrame.setVisible(false);
			FrameManager.mainFrame.setVisible(true);
		}
	}

	private void showHtmlpart(JScrollPane scrollMessage, String path) {
		FileInputStream fstream;
		try {
			fstream = new FileInputStream(path);
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String strLine;
			String text = "";
			while ((strLine = br.readLine()) != null) {
				text = text + strLine + "\n";
			}
			JEditorPane jEditorPane = new JEditorPane("text/html", text);

			if (text.contains("schemas-microsoft-com")) {
				File file = new File(path);
				jEditorPane = new JEditorPane(file.toURI().toURL());
			}
			jEditorPane.setBorder(new EmptyBorder(10, 10, 10, 10));
			// make it read-only
			jEditorPane.setEditable(false);

			// create a scrollpane; modify its attributes as desired
			JScrollPane scrollHtmlPane = new JScrollPane(jEditorPane);

			this.add(scrollHtmlPane);
			attachAndHtml.remove(html);
			JPanel back = new JPanel();
			back.setLayout(new BoxLayout(back, BoxLayout.X_AXIS));
			back.setBorder(new EmptyBorder(10, 10, 10, 10));
			JButton backButton = new JButton("Back to text");
			back.add(backButton);
			attachAndHtml.add(back, BorderLayout.CENTER);
			backButton.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					remove(scrollHtmlPane);
					add(scrollMessage);
					backButton.setVisible(false);
					attachAndHtml.remove(back);
					attachAndHtml.add(html, BorderLayout.CENTER);
					repaint();
				}
			});
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}

	private void showAttachment(MessageContainer message) {
		JPanel attachmentsPanel = new JPanel();
		attachmentsPanel.setLayout(new BoxLayout(attachmentsPanel, BoxLayout.Y_AXIS));
		for (String attachmentPath : message.getAttachments()) {
			JPanel attachmentPanel = new JPanel();
			attachmentPanel.setLayout(new BoxLayout(attachmentPanel, BoxLayout.X_AXIS));
			attachmentPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
			String[] attachmentPathArray = attachmentPath.split("/");
			String fileName = attachmentPathArray[attachmentPathArray.length - 1];
			String userName = message.getAccountName();
			String folderName = message.getFolderName();

			
			JLabel attachment = new JLabel(fileName);
			attachment.setBorder(new EmptyBorder(0, 0, 0, 10));
			attachmentPanel.add(attachment);
			JButton download = new JButton("download");
			download.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					JFileChooser fc = new JFileChooser();
					fc.setCurrentDirectory(new File(attachmentPath.replace("/" + fileName, "")));
					fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
					int returnVal = fc.showSaveDialog(null);
					if (returnVal == JFileChooser.APPROVE_OPTION) {
						File yourFolder = fc.getSelectedFile();
						new SwingWorker<Void, Void>() {
							@Override
							protected Void doInBackground() {
								GlobalDataContainer.getConnectionByAccount(userName).downloadAttachment("imap",
										yourFolder.getAbsolutePath(), folderName, message, fileName);
								return null;
							}

							protected void done() {
								JOptionPane.showMessageDialog(null, "your file saved", "saving file",
										JOptionPane.PLAIN_MESSAGE);
							}
						}.execute();
					}
				}
			});
			attachmentPanel.add(download);
			attachmentsPanel.add(attachmentPanel);
		}
		attachAndHtml.remove(attach);
		attachAndHtml.add(attachmentsPanel, BorderLayout.WEST);
		FrameManager.mainFrame.setVisible(false);
		FrameManager.mainFrame.setVisible(true);
	}

	private boolean validateMatch(String email) {
		if (!email.matches("^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}+(;\\s?[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4})*$")) {
			JOptionPane.showMessageDialog(null, "no valid email", "Error", JOptionPane.ERROR_MESSAGE);
			return false;
		}
		return true;
	}
}
