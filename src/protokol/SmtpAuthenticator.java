package protokol;

import javax.mail.Authenticator;
import javax.mail.PasswordAuthentication;

public class SmtpAuthenticator extends Authenticator {
	private String userAuth;
	private String password;

	public SmtpAuthenticator(String userAuth, String password) {
		this.userAuth = userAuth;
		this.password = password;
	}

	@Override
	public PasswordAuthentication getPasswordAuthentication() {
		return new PasswordAuthentication(userAuth, password);
	}

	public String getUserAuth() {
		return userAuth;
	}

	public String getPassword() {
		return password;
	}

}
