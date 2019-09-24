package protokol;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;

import javax.mail.BodyPart;
import javax.mail.Flags.Flag;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.internet.InternetAddress;

import org.apache.commons.io.FileUtils;

import data.GlobalDataContainer;
import data.MailFolder;
import gui.FrameManager;

public class MessageContainer implements Comparable<MessageContainer>, Serializable {
	private String path;
	private String from;
	private String to;
	private String subject;
	private Date recievedDate;
	// private String pathToMessageBody;
	private ArrayList<String> htmlFiles = new ArrayList<String>();
	private ArrayList<String> attachments = new ArrayList<String>();
	private StringBuffer messageText;
	// private boolean hasAttachment;
	private boolean seen;

	public boolean isSeen() {
		return seen;
	}

	public MessageContainer(Message message, String userName, String folderName)
			throws IOException, MessagingException {
		this(message);
		generatePath(userName, folderName);
		if (message.getContent() instanceof Multipart) {
			Multipart content = (Multipart) message.getContent();
			multiMessage(content);
		} else {
			String charset = message.getContentType();
			getMessageText(message);
		}
		serialize();
	}

	private MessageContainer(Message message) throws MessagingException {
		messageText = new StringBuffer();
		htmlFiles = new ArrayList<String>();
		attachments = new ArrayList<String>();
		this.from = InternetAddress.toString(message.getFrom()) == null ? ""
				: InternetAddress.toString(message.getFrom());
		this.to = InternetAddress.toString(message.getAllRecipients()) == null ? ""
				: InternetAddress.toString(message.getAllRecipients());
		this.subject = message.getSubject() == null ? "" : message.getSubject();
		this.recievedDate = message.getReceivedDate() != null ? message.getReceivedDate()
				: message.getSentDate() != null ? message.getSentDate() : new Date(0);
		this.seen = message.getFlags().contains(Flag.SEEN);
	}

	public MessageContainer(String from, String to, String subject, Date recievedDate, boolean seen, String path) {
		this.from = from;
		this.to = to;
		this.subject = subject;
		this.recievedDate = recievedDate;
		this.seen = seen;
	}

	public void serialize() throws IOException {
		ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(path + "/message.out"));
		out.writeObject(this);
		out.close();
	}

	private void generatePath(String userName, String folderName) {
		String messageXMLfolderName = from + subject + recievedDate;
		if (messageXMLfolderName.equals("")) {
			messageXMLfolderName = "defaultFolder";
		} else {
			messageXMLfolderName = Integer.toString(messageXMLfolderName.hashCode());
		}
		this.path = FrameManager.getProgramSetting("pathToAccountSettings").replaceAll("\\{userName\\}", userName)
				.replaceAll("\\{folderName\\}", folderName.replaceAll("\\[", "").replaceAll("\\]", "")) + "/"
				+ messageXMLfolderName;
		try {
			Files.createDirectories(Paths.get(path));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void multiMessage(Multipart content) {
		try {
			for (int i = 0; i < content.getCount(); i++) {
				BodyPart part = content.getBodyPart(i);
				parseMessagePart(part);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void parseMessagePart(Part part) throws MessagingException, IOException {
		if (Part.ATTACHMENT.equalsIgnoreCase(part.getDisposition())) {
			// hasAttachment = true;
			attachments.add(path + "/" + part.getFileName());
		} else if (part.isMimeType("text/html")) {
			String charset = part.getContentType();
			htmlFiles.add(path + "/email" + htmlFiles.size() + ".html");
			writeHtmlFile(part, htmlFiles.get(htmlFiles.size() - 1));
		} else if (part.getContent() instanceof Multipart) {
			multiMessage((Multipart) part.getContent());
		} else if (part.isMimeType("message/rfc822")) {
			messageText.append("<Forwarded Message>:");
			parseMessagePart((Part) part.getContent());
		} else {
			String charset = part.getContentType();
			if (part.getContent().getClass().equals(String.class)) {
				getMessageText(part);
			}
		}
	}

	private void getMessageText(Part part) {
		try {

			BufferedReader in = new BufferedReader(new InputStreamReader(part.getInputStream(), "Windows-1251"));
			String line = in.readLine();
			while (line != null) {
				messageText.append(line + "\n");
				line = in.readLine();
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (MessagingException e) {
			e.printStackTrace();
		}

	}

	private void writeHtmlFile(Part part, String path) {
		try {
			BufferedReader in = new BufferedReader(new InputStreamReader(part.getInputStream(), "Windows-1251"));
			BufferedWriter out;
			out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(path, false), "Windows-1251"));

			String line = in.readLine();
			while (line != null) {
				out.write(line + "\n");
				line = in.readLine();
			}
			out.close();
		} catch (IOException | MessagingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

//TODO: separate
	public void delete() throws IOException {
		MailFolder folder = GlobalDataContainer.getAccountByName(getAccountName()).getFolderByName(getFolderName());
		folder.getMessages().remove(this);
		GlobalDataContainer.getConnectionByAccount(getAccountName()).deleteMessage(getFolderName(), this);
		FileUtils.deleteDirectory(new File(path));
	}

	public void moveToFolder(String folder) {
		try {
			delete();
			generatePath(getAccountName(), folder);
			GlobalDataContainer.getAccountByName(getAccountName()).getFolderByName(getFolderName()).addMessage(this);
			serialize();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public String getFolderName() {
		return path.split("/")[2];
	}

	public String getAccountName() {
		return path.split("/")[1];
	}

	public String getMessageText() {
		return messageText.toString();
	}

	public String getPath() {
		return path;
	}

	public String getFrom() {
		return from;
	}

	public String getTo() {
		return to;
	}

	public String getSubject() {
		return subject;
	}

	public ArrayList<String> getHtmlFiles() {
		return htmlFiles;
	}

	public ArrayList<String> getAttachments() {
		return attachments;
	}

	public void setAttachments(ArrayList<String> attachments) {
		this.attachments = attachments;
	}

	public void setHtmlFiles(ArrayList<String> htmlFiles) {
		this.htmlFiles = htmlFiles;
	}

	public void addAttachment(String fileName) {
		if (attachments == null) {
			attachments = new ArrayList<String>();
		}
		attachments.add(fileName);
	}

	public void setFrom(String from) {
		this.from = from;
	}

	public void setTo(String to) {
		this.to = to;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public void setSeen(boolean seen) {
		this.seen = seen;
	}

	public Date getReceivedDate() {
		return recievedDate;
	}

	public void setPath(String path) {
		this.path = path;
	}

	@Override
	public int compareTo(MessageContainer o) {
		return o.getReceivedDate().compareTo(this.getReceivedDate());
	}

	@Override
	public boolean equals(Object o) {
		MessageContainer m = (MessageContainer) o;
		return m.path.equals(this.path);
	}

	@Override
	public String toString() {
		return "MessageContainer {path='" + path + "', from='" + from + "', to='" + to + "', subject='" + subject
				+ "', recievedDate='" + recievedDate + "', htmlFiles='" + htmlFiles + "', attachments='" + attachments
				+ "', messageText='" + messageText + "', seen='" + seen + "'}";
	}

}
