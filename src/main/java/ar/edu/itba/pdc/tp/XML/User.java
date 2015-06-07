package ar.edu.itba.pdc.tp.XML;

public class User {

	private String username;
	private String password;
	private String plainAuth;
	public User(String username, String password,String plainAuth) {
		super();
		this.username = username;
		this.password = password;
		this.plainAuth=plainAuth;
	}
	public String getUsername() {
		return username;
	}
	
	public String getPassword() {
		return password;
	}
	public String getPlainAuth() {
		return plainAuth;
	}
	
	
	
}
