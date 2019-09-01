package protokol;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
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

import data.AccountData;
import data.MailFolder;
import gui.FrameManager;

public class MessageContainer implements Comparable<MessageContainer> {
	private String path;
	private String from;
	private String to;
	private String subject;
	private Date recievedDate;
	private String pathToMessageBody;
	private ArrayList<String> htmlFiles = new ArrayList<String>();
	private ArrayList<String> attachments = new ArrayList<String>();
	private StringBuffer messageText;
	private boolean hasAttachment;
	private boolean seen;

	public boolean isHasAttachment() {
		return hasAttachment;
	}

	public boolean isSeen() {
		return seen;
	}

	public void setHasAttachment(boolean hasAttachment) {
		this.hasAttachment = hasAttachment;
	}

	public MessageContainer(Message message, String path) throws IOException, MessagingException {
		this(message);
		this.path = path;
		messageText = new StringBuffer();
		htmlFiles = new ArrayList<String>();
		attachments = new ArrayList<String>();
		this.pathToMessageBody = path + "/messageBody" + message.getMessageNumber() + ".txt";
		if (message.getContent() instanceof Multipart) {
			Multipart content = (Multipart) message.getContent();
			multiMessage(content);
		} else {
			String charset = message.getContentType();
			writeFile(message.getContent().toString(), charset,
					pathToMessageBody.replaceAll("\\]", "").replaceAll("\\[", ""));
		}
	}

	public MessageContainer(Message message) throws MessagingException {
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
		this.pathToMessageBody = path;
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
			hasAttachment = true;
			attachments.add(path + "/" + part.getFileName());
		} else if (part.isMimeType("text/html")) {
			String charset = part.getContentType();
			htmlFiles.add(path + "/email" + htmlFiles.size() + ".html");
			writeFile(part.getContent().toString(), charset, htmlFiles.get(htmlFiles.size() - 1));
		} else if (part.getContent() instanceof Multipart) {
			multiMessage((Multipart) part.getContent());
		} else if (part.isMimeType("message/rfc822")) {
			messageText.append("<Forwarded Message>:");
			parseMessagePart((Part) part.getContent());
		} else {
			String charset = part.getContentType();
			if (part.getContent().getClass().equals(String.class)) {
				writeFile(part.getContent().toString(), charset, pathToMessageBody);
			}
		}
	}

	private void writeFile(String text, String charset, String file) {
		Charset cha = StandardCharsets.UTF_8;
		if (charset.contains("iso-8859-1")) {
			cha = StandardCharsets.ISO_8859_1;

		} else if (charset.contains("utf-8")) {
			cha = StandardCharsets.UTF_8;

		} else if (charset.contains("us-ascii")) {
			cha = StandardCharsets.US_ASCII;

		} else if (charset.contains("utf-16")) {
			cha = StandardCharsets.UTF_16;

		} else if (charset.contains("utf-16be")) {
			cha = StandardCharsets.UTF_16BE;

		} else if (charset.contains("utf-16le")) {
			cha = StandardCharsets.UTF_16LE;
		}
		try {
			BufferedWriter out = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(file.replaceAll("\\]", "").replaceAll("\\[", ""), false), cha));
			out.write(text);
			out.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public void delete() throws IOException {
		AccountData accountToCompare = new AccountData();
		accountToCompare.set("userName", pathToMessageBody.split("/")[1]);
		int index = FrameManager.accounts.indexOf(accountToCompare);
		AccountData account = FrameManager.accounts.get(index);
		MailFolder folderToCompare = new MailFolder(pathToMessageBody.split("/")[3]);
		index = account.getFolders().indexOf(folderToCompare);
		MailFolder folder = account.getFolders().get(index);
		folder.getMessages().remove(this);
		FrameManager.connections.get(pathToMessageBody.split("/")[1]).deleteMessage(pathToMessageBody.split("/")[3],
				this);
		FileUtils.deleteDirectory(new File(
				pathToMessageBody.replaceAll("/messageBody\\d+.txt", "").replaceAll("\\]", "").replaceAll("\\[", "")));

	}

	public void setPathToMessageBody(String pathToMessageBody) {
		this.pathToMessageBody = pathToMessageBody;
	}

	public String getMessageText() {
		File file = new File(pathToMessageBody.replaceAll("\\]", "").replaceAll("\\[", ""));
		FileInputStream fis;
		StringBuffer text = new StringBuffer();
		try {
			fis = new FileInputStream(file);
			BufferedReader in = new BufferedReader(new InputStreamReader(fis));
			String line = in.readLine();
			while (line != null) {
				text.append(line + "\n");
				line = in.readLine();
			}
			in.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return text.toString();
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

	public String getPathToMessageBody() {
		return pathToMessageBody;
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

	@Override
	public int compareTo(MessageContainer o) {
		return o.getReceivedDate().compareTo(this.getReceivedDate());
	}

	@Override
	public boolean equals(Object o) {
		MessageContainer m = (MessageContainer) o;
		return m.pathToMessageBody.equals(this.pathToMessageBody);
	}

	@Override
	public String toString() {
		return "MessageContainer [path=" + path + ", from=" + from + ", to=" + to + ", subject=" + subject
				+ ", recievedDate=" + recievedDate + ", pathToMessageBody=" + pathToMessageBody + ", htmlFiles="
				+ htmlFiles + ", attachments=" + attachments + ", messageText=" + messageText + ", hasAttachment="
				+ hasAttachment + ", seen=" + seen + "]";
	}

}
