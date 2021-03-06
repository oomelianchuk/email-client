package protokol;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.search.ComparisonTerm;
import javax.mail.search.ReceivedDateTerm;
import javax.mail.search.SearchTerm;

import org.apache.logging.log4j.Logger;

import gui.FrameManager;

public class MessageSearcher {
	public Message[] findMessage(Folder folder, MessageContainer messageContainer) throws MessagingException {
		FrameManager.LOGGER.info("search for message " + messageContainer + " in folder " + folder.getFullName());
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
					FrameManager.LOGGER.error("while searching message : " + ex.toString());
				} catch (NullPointerException e) {
					FrameManager.LOGGER.error("while searching message : " + e.toString());
					return false;
				}
				return false;
			}
		};
		return folder.search(searchCondition);
	}

	public Message[] findMessagesAfterDate(Date date, Folder folder, String userName, Logger logger)
			throws MessagingException {
		ArrayList<Message> result = new ArrayList<Message>();
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		calendar.add(Calendar.DATE, -1);
		Date yesterday = calendar.getTime();
		SearchTerm newerThan = new ReceivedDateTerm(ComparisonTerm.GT, yesterday);

		Message[] messages = folder.search(newerThan);
		//logger.info("search for messages after " + yesterday);
		//logger.info("search for messages after " + date);
		// and than "manually" search for needed date and time
		for (Message message : messages) {
			if (message.getReceivedDate().compareTo(date) > 0) {
				result.add(message);
			}
		}
		messages = new Message[result.toArray().length];
		for(int i=0; i< messages.length;i++) {
			messages[i]=(Message)result.toArray()[i];
		}
		return messages;
	}
}
