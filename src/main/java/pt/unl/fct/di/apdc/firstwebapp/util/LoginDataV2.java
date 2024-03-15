package pt.unl.fct.di.apdc.firstwebapp.util;

public class LoginDataV2 {
	
	public String username;
	public String password;
	public String confirmation;
	public String email;
	public String name;
	
	public LoginDataV2() {}
	
	public LoginDataV2(String username, String password, String confirmation, String email, String name) {
		this.username = username;
		this.password = password;
		this.confirmation = confirmation;
		this.email = email;
		this.name = name;
	}

}
