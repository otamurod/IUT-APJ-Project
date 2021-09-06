package server;

import java.net.Socket;
import java.time.LocalDate;
import java.time.LocalTime;

public class Client {

	private Socket socket;
	private String userName;
	private String password;
	private String role;
	private LocalDate lastLoggedInDate;
	private LocalTime lastLoggedInHour;

	public Client(Socket socket, String userName, String password, String role) {
		setSocket(socket);
		setUserName(userName);
		setPassword(password);
		setRole(role);
		setLastLoggedInDate(LocalDate.now());
		setLastLoggedInHour(LocalTime.now());
	}

	public Client(Socket socket, String userName, String role) {
		setSocket(socket);
		setUserName(userName);
		setRole(role);
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public Socket getSocket() {
		return socket;
	}

	public void setSocket(Socket socket) {
		this.socket = socket;
	}

	@Override
	public String toString() {
		return userName;
	}

	public String getRole() {
		return role;
	}

	public void setRole(String role) {
		this.role = role;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public LocalTime getLastLoggedInHour() {
		return lastLoggedInHour;
	}

	public void setLastLoggedInHour(LocalTime lastLoggedInHour) {
		this.lastLoggedInHour = lastLoggedInHour;
	}

	public LocalDate getLastLoggedInDate() {
		return lastLoggedInDate;
	}

	public void setLastLoggedInDate(LocalDate lastLoggedInDate) {
		this.lastLoggedInDate = lastLoggedInDate;
	}

}
