package config.model;

public class ManagerUserConfig {

	private String user;
	private String passwd;
	
	public ManagerUserConfig(String user, String passwd) {
		this.user = user;
		this.passwd = passwd;
	}

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public String getPasswd() {
		return passwd;
	}

	public void setPasswd(String passwd) {
		this.passwd = passwd;
	}
}
