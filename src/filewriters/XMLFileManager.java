package filewriters;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.swing.JOptionPane;
import javax.swing.UIManager;
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

import org.apache.commons.io.FileUtils;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import data.AccountData;
import data.GlobalDataContainer;
import data.MailFolder;
import gui.FrameManager;
import protokol.MessageContainer;

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
			FrameManager.LOGGER.info("xml file created " + created);
			if (created) {
				createDocument(path);
			}
			document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(this.path);
		} catch (SAXException | IOException | ParserConfigurationException e) {
			e.printStackTrace();
		}
	}

	private void createDocument(String path) {
		try {
			BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(path, false)));
			out.write("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n");
			out.write("<Properties>\n");
			out.write("<Skin>" + UIManager.getCrossPlatformLookAndFeelClassName() + "</Skin>\n");
			out.write("<Accounts></Accounts>\n");
			out.write("</Properties>\n");
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	/**
	 * get name of preselected look and feel (yet only system or cross platform)
	 * 
	 * @return name of preselected look and feel
	 */
	public String getLookAndFeel() {
		try {
			FrameManager.LOGGER.info("reading look and feel selection");
			XPathFactory pathFactory = XPathFactory.newInstance();
			XPath xpath = pathFactory.newXPath();
			XPathExpression expr = xpath.compile("//Skin");
			return ((Node) expr.evaluate(document, XPathConstants.NODE)).getTextContent();
		} catch (XPathExpressionException e) {
			FrameManager.LOGGER.error("look and feel selection not found : " + e.toString());
		}
		return UIManager.getCrossPlatformLookAndFeelClassName();
	}

	/**
	 * sets name of preselected look and feel (yet only system or cross platform)
	 * 
	 * @param newLookAndFeel name of look and feel
	 */
	public void changeLookAndFeel(String newLookAndFeel) {
		FrameManager.LOGGER.info("change look and feel selection in accounts.xml");
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
			FrameManager.LOGGER.error("look and feel selection not found : " + e.toString());
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
		FrameManager.LOGGER.info("create " + protocol + " protocol node");
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
			FrameManager.LOGGER.error("while xml file writing : " + e.toString());
		}
	}
}
