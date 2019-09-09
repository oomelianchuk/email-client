package filewriters;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.swing.JOptionPane;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import data.AccountData;
import data.MailFolder;
import gui.FrameManager;

/**
 * This class helps to interact with xml files e.g. accounts.xml or
 * mainMessage.xml. It saves and gets data from these files
 */
public class XMLFileManager {
	private Document document;
	private String path;

	/**
	 * to create instance of this class it's needed to give it path to file, with
	 * which it will interact (only for already created xml files with root element)
	 * 
	 * @param path path to file
	 */
	public XMLFileManager(String path) {
		try {
			this.path = path;
			File newFile = new File(path);
			boolean created = newFile.createNewFile();
			FrameManager.logger.info("xml file created " + created);
			if (!created) {
				document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(this.path);
			} else {
				createDocument(path);
			}
		} catch (SAXException | IOException | ParserConfigurationException e) {
			e.printStackTrace();
		}
	}

	private void createDocument(String path) {
		try {
			BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(path, false)));
			out.write("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n");
			out.write("<Properties>\n");
			out.write("<Skin>cross-platform</Skin>\n");
			out.write("<Accounts>cross-platform</Accounts>\n");
			out.write("</Properties>\n");
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	/**
	 * gets last update date for account (to know mail after which data should be
	 * loaded)
	 * 
	 * @param userName account name
	 * @return data of last update for the account in string form
	 */
	public String getLastAttendanceDateForAccount(String userName) {
		FrameManager.logger.info("reading last attendance date");
		XPathFactory pathFactory = XPathFactory.newInstance();
		XPath xpath = pathFactory.newXPath();
		try {
			XPathExpression expr = xpath.compile("//Account[userName='" + userName + "'/Date");
			NodeList node = (NodeList) expr.evaluate(document, XPathConstants.NODESET);
			return node.item(0).getTextContent();
		} catch (XPathExpressionException e) {
			FrameManager.logger.error("last attendance date not found : " + e.toString());
			return null;
		}
	}
	/**
	 * get name of preselected look and feel (yet only system or cross platform)
	 * 
	 * @return name of preselected look and feel
	 */
	public String getLookAndFeel() {
		try {
			FrameManager.logger.info("reading look and feel selection");
			XPathFactory pathFactory = XPathFactory.newInstance();
			XPath xpath = pathFactory.newXPath();
			XPathExpression expr = xpath.compile("//Skin");
			return ((Node) expr.evaluate(document, XPathConstants.NODE)).getTextContent();
		} catch (XPathExpressionException e) {
			FrameManager.logger.error("look and feel selection not found : " + e.toString());
		}
		return "crossplatform";
	}

	/**
	 * sets name of preselected look and feel (yet only system or cross platform)
	 * 
	 * @param newLookAndFeel name of look and feel
	 */
	public void changeLookAndFeel(String newLookAndFeel) {
		FrameManager.logger.info("change look and feel selection in accounts.xml");
		XPathFactory pathFactory = XPathFactory.newInstance();
		XPath xpath = pathFactory.newXPath();
		XPathExpression expr;
		try {
			expr = xpath.compile("//Skin");
			Node skin = ((Node) expr.evaluate(document, XPathConstants.NODE));
			skin.setTextContent(newLookAndFeel);
			skin.setTextContent(newLookAndFeel);
			writeDocument(document);
		} catch (XPathExpressionException e) {
			FrameManager.logger.error("look and feel selection not found : " + e.toString());
		}
	}

	/**
	 * method to change account name
	 * 
	 * @param oldName old account name (to find needed node)
	 * @param newName new name to set
	 */
	public void renameAccount(String oldName, String newName) {
		try {
			FrameManager.logger.info("change account name in accounts.xml");
			XPathFactory pathFactory = XPathFactory.newInstance();
			XPath xpath = pathFactory.newXPath();
			XPathExpression expr = xpath.compile("//Account[userName='" + oldName + "']/userName");
			Node account = ((NodeList) expr.evaluate(document, XPathConstants.NODE)).item(0);
			account.setTextContent(newName);
			writeDocument(document);
		} catch (XPathExpressionException e) {
			FrameManager.logger.error("account name not found : " + e.toString());
		}
	}

	/**
	 * rewrite account data in accounts file to make sure all changes in it saved,
	 * called before program end
	 * 
	 * @param accountData data about account that should be "resaved"
	 */
	public void rewriteAccount(AccountData accountData) {
		try {
			FrameManager.logger.info("rewrite account data in accounts.xml");
			XPathFactory pathFactory = XPathFactory.newInstance();
			XPath xpath = pathFactory.newXPath();
			// find "root" element for account
			XPathExpression expr = xpath.compile("//Account[userName='" + accountData.getUserName() + "']");
			Node root = ((NodeList) expr.evaluate(document, XPathConstants.NODESET)).item(0);
			NodeList nodeList = root.getChildNodes();
			// go through all account child nodes
			for (int i = 0; i < nodeList.getLength(); i++) {
				Node node = nodeList.item(i);
				if (node.getNodeType() != Node.TEXT_NODE) {
					// if password wasn't saved but in this session user decided to save it
					if (accountData.isSavePass() && (node.getNodeName().equals("pop")
							|| node.getNodeName().equals("imap") || node.getNodeName().equals("smtp"))) {
						// create password element
						Element password = document.createElement("password");
						// set password as content for this element
						password.setTextContent(accountData.getPassword());
						// insert it as child node
						root.insertBefore(password, node);
						accountData.setSavePass(false);
					}
					// if node is folder or protocol tag then it doesn't contain any information by
					// itself but it has child nodes
					if (node.getNodeName().equals("folders") || node.getNodeName().equals("pop")
							|| node.getNodeName().equals("imap") || node.getNodeName().equals("smtp")) {

						if (node.getNodeName().equals("folders")) {
							// to rewrite folders old folder nodes should be deleted and than new created
							// because it's possible that number of folders has changed
							root.removeChild(node);
							if (accountData.getFolders() != null) {
								Element newFolersNode = document.createElement("folders");
								for (MailFolder folder : accountData.getFolders()) {
									Element folderNode = document.createElement("folder");
									folderNode.setTextContent(folder.getName());
									newFolersNode.appendChild(folderNode);
								}
								root.appendChild(newFolersNode);
							}
						} else {
							NodeList subNodes = node.getChildNodes();
							for (int j = 0; j < subNodes.getLength(); j++) {
								Node subNode = subNodes.item(j);
								if (!subNode.getNodeName().equals("folder") & subNode.getNodeType() != Node.TEXT_NODE) {
									if (accountData.get(subNode.getNodeName()) != null) {
										subNode.setTextContent(accountData.get(subNode.getNodeName()));
									} else {
										node.removeChild(subNode);
									}
								}
							}
						}
					} else {
						node.setTextContent(accountData.get(node.getNodeName()));
					}
				}
			}
			writeDocument(document);
		} catch (XPathExpressionException e) {
			FrameManager.logger.error("account not found : " + e.toString());
		}
	}

	/**
	 * Method to receive account data by user name
	 * 
	 * @param userName user name for which account data should be found
	 * @return data for this account
	 * @throws XPathExpressionException
	 */
	public AccountData getAccountDataBy(String userName) throws XPathExpressionException {
		FrameManager.logger.info("read account data for " + userName + "  from accounts.xml");
		XPathFactory pathFactory = XPathFactory.newInstance();
		XPath xpath = pathFactory.newXPath();
		XPathExpression expr = xpath.compile("//Account[userName='" + userName + "']");

		NodeList nodeList = ((NodeList) expr.evaluate(document, XPathConstants.NODESET)).item(0).getChildNodes();
		AccountData accountData = new AccountData();
		for (int i = 0; i < nodeList.getLength(); i++) {
			Node node = nodeList.item(i);
			if (node.getNodeType() != Node.TEXT_NODE) {
				// these nodes have sub nodes
				if (node.getNodeName().equals("folders") || node.getNodeName().equals("pop")
						|| node.getNodeName().equals("imap") || node.getNodeName().equals("smtp")) {
					if (node.getNodeName().equals("folders")) {
						accountData.setFolders(new ArrayList<MailFolder>());
					}
					NodeList subNodes = node.getChildNodes();
					for (int j = 0; j < subNodes.getLength(); j++) {
						Node subNode = subNodes.item(j);
						if (subNode.getNodeName().equals("folder")) {
							accountData.addFolder(new MailFolder("src/" + userName + "/" + subNode.getTextContent()));
						}
						if (subNode.getNodeType() != Node.TEXT_NODE) {
							accountData.set(subNode.getNodeName(), subNode.getTextContent());
						}
					}
				} else {
					accountData.set(node.getNodeName(), node.getTextContent());
				}
			}
		}
		return accountData;
	}

	/**
	 * Method to delete account node by account name
	 * 
	 * @param userName
	 */
	public void deleteAccount(String userName) {
		FrameManager.logger.info("delete account data from accounts.xml");
		XPathFactory pathFactory = XPathFactory.newInstance();
		XPath xpath = pathFactory.newXPath();
		XPathExpression expr;
		try {
			expr = xpath.compile("//Account[userName='" + userName + "']");
			Node account = ((NodeList) expr.evaluate(document, XPathConstants.NODESET)).item(0);
			expr = xpath.compile("//Accounts");
			Node root = ((NodeList) expr.evaluate(document, XPathConstants.NODESET)).item(0);
			root.removeChild(account);
			writeDocument(document);
		} catch (XPathExpressionException e) {
			FrameManager.logger.error("account not found : " + e.toString());
		}
	}

	/**
	 * create account node
	 * 
	 * @param data information about new account
	 * @throws TransformerFactoryConfigurationError
	 * @throws DOMException
	 */
	public void addNewAccount(AccountData data) throws TransformerFactoryConfigurationError, DOMException {
		FrameManager.logger.info("write account data " + data + " in accounts.xml");
		XPathFactory pathFactory = XPathFactory.newInstance();
		XPath xpath = pathFactory.newXPath();
		XPathExpression expr;
		try {
			expr = xpath.compile("//Accounts");
			Node root = ((NodeList) expr.evaluate(document, XPathConstants.NODESET)).item(0);

			Element account = document.createElement("Account");
			Element lastUpdate = document.createElement("lastUpdate");
			lastUpdate.setTextContent(new SimpleDateFormat("dd.MM.yyyy HH:mm:ss").format(new Date()));
			Element userName = document.createElement("userName");
			userName.setTextContent(data.getUserName());
			Element email = document.createElement("email");
			email.setTextContent(data.getEmail());
			Element userAuth = document.createElement("userAuth");
			userAuth.setTextContent(data.getUserAuth());
			account.appendChild(lastUpdate);
			account.appendChild(userName);
			account.appendChild(email);
			account.appendChild(userAuth);
			if (data.isSavePass()) {
				Element password = document.createElement("password");
				password.setTextContent(data.getPassword());
				account.appendChild(password);

			}
			if (data.getPopServer() != null) {
				account.appendChild(createProtocolNode("pop", data.getPopServer(), data.getPopPort(),
						data.get("sslPop"), data.get("tlsPop")));
			}
			if (data.getImapServer() != null) {
				account.appendChild(createProtocolNode("imap", data.getImapServer(), data.getImapPort(),
						data.get("sslImap"), data.get("tlsImap")));
			}
			if (data.getSmtpServer() != null) {
				account.appendChild(createProtocolNode("smtp", data.getSmtpServer(), data.getSmtpPort(),
						data.get("sslSmtp"), data.get("tlsSmtp")));
			}
			Element runInBackground = document.createElement("runInBackground");
			runInBackground.setTextContent(data.get("runInBackground"));
			account.appendChild(runInBackground);
			Element folders = document.createElement("folders");
			ArrayList<MailFolder> folderContainers = data.getFolders();
			if (folders != null && !folderContainers.isEmpty()) {
				for (MailFolder folderContainer : folderContainers) {
					Element folder = document.createElement("folder");
					folder.setTextContent(folderContainer.getName());
					folders.appendChild(folder);
				}
				account.appendChild(folders);
			}
			root.appendChild(account);
			writeDocument(document);
		} catch (XPathExpressionException e) {
			FrameManager.logger.error("no root node : " + e.toString());
		}
	}

	/**
	 * Method to create mail protocol nodes <protocol> <protocolServer>
	 * protocol.host.com </protocolServer> <protocolPort> 111 </prorocolPort>
	 * </protocol>
	 * 
	 * @param protocol       imap/pop/smtp
	 * @param protokolServer
	 * @param protokolPort
	 * @return created protocol node
	 */
	private Element createProtocolNode(String protocol, String protokolServer, String protokolPort, String ssl,
			String tls) {
		FrameManager.logger.info("create " + protocol + " protocol node");
		Element protocolMainNode = document.createElement(protocol);
		Element protokolServerNode = document.createElement(protocol + "Server");
		protokolServerNode.setTextContent(protokolServer);
		protocolMainNode.appendChild(protokolServerNode);
		if (protokolPort != null) {
			Element protokolPortNode = document.createElement(protocol + "Port");
			protokolPortNode.setTextContent(protokolPort);
			protocolMainNode.appendChild(protokolPortNode);
			Element protokolSsl = document
					.createElement("ssl" + protocol.substring(0, 1).toUpperCase() + protocol.substring(1));
			protokolSsl.setTextContent(ssl);
			protocolMainNode.appendChild(protokolSsl);
			Element protokolTls = document
					.createElement("tls" + protocol.substring(0, 1).toUpperCase() + protocol.substring(1));
			protokolTls.setTextContent(tls);
			protocolMainNode.appendChild(protokolTls);
		}
		return protocolMainNode;
	}

	/**
	 * get information about all account (called on program start)
	 * 
	 * @return list of containers with account information
	 */
	public List<AccountData> getAccountDaten() {
		FrameManager.logger.info("read all accounts from accounts.xml");
		List<AccountData> accountDatenList = new ArrayList<AccountData>();
		XPathFactory pathFactory = XPathFactory.newInstance();
		XPath xpath = pathFactory.newXPath();

		XPathExpression expr;
		try {
			expr = xpath.compile("//userName");
			NodeList nodeList = (NodeList) expr.evaluate(document, XPathConstants.NODESET);
			for (int i = 0; i < nodeList.getLength(); i++) {
				accountDatenList.add(getAccountDataBy(nodeList.item(i).getTextContent()));
			}
		} catch (XPathExpressionException e) {
			JOptionPane.showMessageDialog(FrameManager.mainFrame, "Error by data load", "Error by data load",
					JOptionPane.ERROR_MESSAGE);
			FrameManager.logger.error("error reading account data : " + e.toString());
		}
		return accountDatenList;

	}

	/**
	 * Method to write abstract xml document to file
	 * 
	 * @param document
	 * @throws TransformerFactoryConfigurationError
	 */
	private void writeDocument(Document document) throws TransformerFactoryConfigurationError {
		try {
			Transformer tr = TransformerFactory.newInstance().newTransformer();
			DOMSource source = new DOMSource(document);
			FileOutputStream fos = new FileOutputStream(path);
			StreamResult result = new StreamResult(fos);
			tr.transform(source, result);
			fos.close();
		} catch (TransformerException | IOException e) {
			FrameManager.logger.error("while xml file writing : " + e.toString());
		}
	}
}
