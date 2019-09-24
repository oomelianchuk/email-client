package filewriters;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import gui.FrameManager;

public class LoggerConfigurator {
	public void setUpLoggerForUser(String userName) {
		File propertiesFile = new File(FrameManager.getProgramSetting("pathToLoggingSettings"));
		BufferedWriter out;
		try {
			out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(propertiesFile, true)));
			out.write(generateLoggerConfiguration(userName));
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void deleteLoggerForUser(String userName) {
		File propertiesFile = new File(FrameManager.getProgramSetting("pathToLoggingSettings"));
		try {
			BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(propertiesFile)));
			String text = "";
			String line = in.readLine();
			String[] userLogger = generateLoggerConfiguration(userName).split("\r\n");
			int index = 0;
			if (userLogger[0].length() == 0) {
				index = 1;
			}
			while (line != null) {
				if (line.equals(userLogger[index])) {
					for (int i = 0; i < userLogger.length & line != null; i++) {
						line = in.readLine();
					}
					continue;
				}
				text += line + "\n";
				line = in.readLine();
			}
			BufferedWriter out = new BufferedWriter(
					new OutputStreamWriter(new FileOutputStream(propertiesFile, false)));
			out.write(text);
			out.close();
			in.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private String generateLoggerConfiguration(String userName) {
		return "\r\nappender." + userName.replaceAll(" ", "") + ".type = File\r\n" + "appender."
				+ userName.replaceAll(" ", "") + ".name = " + userName.replaceAll(" ", "").toUpperCase() + "\r\n"
				+ "appender." + userName.replaceAll(" ", "") + ".append = false\r\n" + "appender."
				+ userName.replaceAll(" ", "") + ".filter.threshold.type = ThresholdFilter\r\n" + "appender."
				+ userName.replaceAll(" ", "") + ".filter.threshold.level = info\r\n" + "appender."
				+ userName.replaceAll(" ", "") + ".fileName = target/log/${sys:currentDate}/"
				+ userName.replaceAll(" ", "") + ".log\r\n" + "appender." + userName.replaceAll(" ", "")
				+ ".layout.type = PatternLayout\r\n" + "appender." + userName.replaceAll(" ", "")
				+ ".layout.pattern = %p-%d{HH:mm:ss}:%class:%line%n%m%n%n\r\n" + "\r\n" + "logger."
				+ userName.replaceAll(" ", "") + ".name = actionclasses.MailLoader-" + userName.replaceAll(" ", "")
				+ "\r\n" + "logger." + userName.replaceAll(" ", "") + ".additivity = false\r\n" + "logger."
				+ userName.replaceAll(" ", "") + ".appenderRef.console.ref = "
				+ userName.replaceAll(" ", "").toUpperCase() + "\r\n";
	}
}
