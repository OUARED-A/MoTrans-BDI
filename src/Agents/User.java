/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package Agents;

public class User {

	private String login=null;
	private char[] password=null;

	public User(String login,char[]password){
		this.login=login;
		this.password=password;
	}

	public String getLogin(){
		return this.login;
	}

	public char[] getPassword(){
		return this.password;
	}
}
