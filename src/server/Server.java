package server;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.ResourceBundle;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

public class Server extends Application implements Runnable, Initializable {

	@FXML
	private Button startServer;

	@FXML
	private Button stopServer;

	@FXML
	TextArea textArea;

	@FXML
	TextField guestTimeLimit;

	@FXML
	private TextField maximumGuestsField;

	@FXML
	private TextField portField;

	int serverPort = 8000;
	ServerSocket serverSocket;
	Thread thread;
	Socket socket;
	PrintWriter writer;

	// Clients list
	ArrayList<Client> clients;
	// General messages List
	ArrayList<String> roomMessages;
	// maximum Guests in room
	int maximumGuests = 2;

	public static void main(String[] args) {
		launch(args);
	}

	@Override
	public void start(Stage stage) throws Exception {

		// BorderPane
		BorderPane borderPane = FXMLLoader.load(getClass().getResource("/resources/fxml/Server.fxml"));

		stage.setTitle("Server");
		stage.setScene(new Scene(borderPane));
		stage.setX(0);
		stage.setY(0);
		stage.setOnCloseRequest(c -> {
			stopServer();
			System.exit(0);
		});
		stage.show();

		 LocalDate localDate = LocalDate.now();
		 LocalTime localTime = LocalTime.now();
		 System.out.println("Date: " + localDate + " time: " + localTime);
	}

	@Override
	public void run() {

		while (thread != null) {
			try {

				// Accepting the Server Connections
				socket = serverSocket.accept();

				// Create a Separate Thread for that each client
				new ServerClient(this, socket);
				Platform.runLater(() -> {
					textArea.appendText("\nNew Client Connected....");
				});

				Thread.sleep(150);
			} catch (InterruptedException | IOException ex) {
				stopServer();
			}
		}
		Platform.runLater(() -> {
			textArea.setText("Server has stopped!");
		});

	}

	private void startServer() {

		// Initialize the Server Socket
		try {

			serverSocket = new ServerSocket(serverPort);
			System.out.println(serverSocket.getInetAddress().getCanonicalHostName());
		} catch (IOException e) {
			e.printStackTrace();
			textArea.setText(e.getMessage());
			return;
		}

		// Initialize the ArrayLists
		clients = new ArrayList<>();
		roomMessages = new ArrayList<>();

		// Initialize the thread
		thread = new Thread(this);
		thread.start();

		// Configure the Buttons
		startServer.setDisable(true);
		textArea.setText("Waiting for clients to connect....");
	}

	private void stopServer() {

		if (serverSocket != null) {
			// Disconnect All Clients
			if (clients != null)
				clients.stream().forEach(client -> sendMessageToClient(client.getSocket(), "DISC"));

			thread = null;

			// Close Server Socket
			try {
				serverSocket.close();
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				// serverSocket = null;
				clients = null;
				roomMessages = null;
			}

			// Configure the Buttons
			startServer.setDisable(false);
		}
	}

	public boolean addUser(Socket socket, String userName, String password, String genre) {

		// No Duplicate names Allowed
		if (clients.stream().anyMatch(client -> client.getUserName().equals(userName))) {
			sendMessageToClient(socket, "DISC" + "Dublicate Name Found!Try Other UserName");
			return false;
		}

		// (Guest+guest.size()>maximumGuest)=DISCONNECT
		if (genre.equals("GUEST")
				&& clients.stream().filter(client -> client.getRole().equals("GUEST")).count() >= maximumGuests) {
			sendMessageToClient(socket, "DISC" + "Maximum Guests allowed:(" + maximumGuests + ")");
			return false;
			// Resident
		} else {

		}

		// If not he is the first Client in this Room
		if (clients.size() != 0) {
			String roomList = "";

			// Prepare the ROOM List
			for (Client client : clients)
				roomList += client.getUserName() + "><:><" + client.getRole() + ";";

			textArea.appendText("\nROOMLS" + roomList);

			// Send a Room List To New Client
			sendMessageToClient(socket, "ROOMLS" + roomList);

			// Send the New Client Detail into All Other Clients
			clients.stream().forEach(client -> {
				sendMessageToClient(client.getSocket(), "ADD" + userName + "><:><" + genre);
				sendMessageToClient(client.getSocket(), "(" + userName + ")" + "has joined the room...");
			});

		}
		// Add a user in to array list
		clients.add(new Client(socket, userName, password, genre));
		return true;

	}

	public String listUsers(Socket socket, String userName) {
		String roomList = "";

		// Prepare the ROOM List
		for (Client client : clients)
			if (!client.getUserName().equals(userName))
				roomList += client.getUserName() + ";";

		return roomList;
	}

	String info = null;

	public String getInfoForClient(String userName) {

		clients.stream().filter(client -> client.getUserName().equals(userName)).forEach(client -> {

			info = client.getLastLoggedInDate() + "><:><" + client.getLastLoggedInHour();
		});

		return info;
	}

	public String listMessages() {
		String messList = "";

		//Prepare the ROOM List
		for (String roomMessage : roomMessages)
			messList += roomMessage + "><++><";

		return messList;
	}

	public void removeUser(Socket socket, String userName) {

		// Remove this Client from All other Clients Details
		Iterator<Client> iterator = clients.iterator();
		while (iterator.hasNext()) {
			Client client = iterator.next();
			if (client.getUserName().equals(userName)) {
				iterator.remove();
				break;
			}
		}
		sendMessageToClient(socket, "DISC" + "BYE!");

		// Notify all the Clients about Removed Client
		clients.stream().forEach(client -> {
			sendMessageToClient(client.getSocket(), "REMOVE" + userName);
			sendMessageToClient(client.getSocket(), "(" + userName + ")" + " left the room...");
		});

		textArea.appendText("\nConnected Users Now are: (" + clients.size() + ")");
	}


	public void sentGeneralMessage(Socket clientSocket, String userName, String message) {

		// Add it to Array so It can be seen
		// From Future Clients that log in this room
		roomMessages.add(userName + "><:><" + message);

		textArea.appendText("\nGeneral message:(" + message + ")");

		// Notify all the Clients about the Message
		for (Client client : clients)
			if (!client.getUserName().equals(userName))
				sendMessageToClient(client.getSocket(), "GMESS" + userName + "><:><" + message);

	}

	public void sentPrivateMessage(Socket clientSocket, String fromClient, String message, String toClient) {

		textArea.appendText(
				"\nPrivate message(from->" + fromClient + " to->" + toClient + ") Message: (" + message + ")");

		// Notify all the Clients about the Message
		for (Client client : clients)
			if (client.getUserName().equals(toClient))
				sendMessageToClient(client.getSocket(), "PMESS" + fromClient + "><:><" + message + "><:><" + toClient);

	}

	public void sendMessageToClient(Socket clientSocket, String message) {
		if (clientSocket != null && !clientSocket.isClosed()) {
			try {
				writer = new PrintWriter(clientSocket.getOutputStream(), true);
				writer.println(message);
			} catch (IOException ex) {
				ex.printStackTrace();
				// writer.close();
			}
		}
	}

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		System.out.println("Server initialized");

		maximumGuestsField.disableProperty().bind(startServer.disabledProperty());
		portField.disableProperty().bind(startServer.disabledProperty());
		guestTimeLimit.disableProperty().bind(startServer.disabledProperty());
		stopServer.disableProperty().bind(startServer.disabledProperty().not());

		// StartServer
		startServer.setOnAction(a -> {
			startServer();
		});

		// StopServer
		stopServer.setOnAction(a -> {
			stopServer();
		});

		// infoArea
		textArea.setPrefWidth(300);
		textArea.setWrapText(true);
		textArea.setEditable(false);
		textArea.setText("Press to start the Server....");

		portField.textProperty().addListener((observable, oldValue, newValue) -> {
			if (!newValue.matches("\\d"))
				portField.setText(newValue.replaceAll("\\D", ""));
			if (portField.getText().length() > 5)
				portField.setText(newValue.substring(0, 5));

			if (portField.getText().isEmpty())
				portField.setText("4444");

			serverPort = Integer.parseInt(portField.getText());

		});

		maximumGuestsField.textProperty().addListener((observable, oldValue, newValue) -> {
			if (!newValue.matches("\\d"))
				maximumGuestsField.setText(newValue.replaceAll("\\D", ""));
			if (maximumGuestsField.getText().length() > 5)
				maximumGuestsField.setText(newValue.substring(0, 5));

			if (maximumGuestsField.getText().isEmpty())
				maximumGuestsField.setText("10");
			else if (maximumGuestsField.getText().equals("0"))
				maximumGuestsField.setText("1");

			maximumGuests = Integer.parseInt(maximumGuestsField.getText());

		});

		guestTimeLimit.textProperty().addListener((observable, oldValue, newValue) -> {
			if (!newValue.matches("\\d"))
				guestTimeLimit.setText(newValue.replaceAll("\\D", ""));
			if (guestTimeLimit.getText().length() > 10)
				guestTimeLimit.setText(newValue.substring(0, 10));

			if (guestTimeLimit.getText().isEmpty())
				guestTimeLimit.setText("60");
			else if (guestTimeLimit.getText().equals("0"))
				guestTimeLimit.setText("1");

		});
	}

}
