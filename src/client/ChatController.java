package client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.TreeMap;

import org.controlsfx.control.Notifications;
import org.fxmisc.richtext.InlineCssTextArea;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.ImageCursor;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.util.Callback;

public class ChatController extends BorderPane implements Initializable {

	Timer timer;
	public ChatClient chatClient;
	ArrayList<PrivateChatController> privateChatControllers = new ArrayList<>();

	@FXML
	private ListView<String> listView;

	@FXML
	private Label usersInRoom;

	@FXML
	private Button sentPrivateMessage;

	@FXML
	private Label topLabel;

	@FXML
	private Button sentMessage;

	@FXML
	private TextField messageField;

	@FXML
	private InlineCssTextArea messagesArea;

	@FXML
	private GridPane bottomGridPane;

	Scene scene;

	static Map<String, String> map = new TreeMap<String, String>();
	String[] colors = { "ORANGE", "CYAN", "MAGENTA", "GREEN", "BLUE", "BROWN", "ORANGE", "CYAN", "MAGENTA", "GREEN",
			"BLUE", "BROWN", "ORANGE", "CYAN", "MAGENTA", "BLACK", "GREEN", "BLUE", "BROWN", "ORANGE", "CYAN",
			"MAGENTA", "BLACK", "GREEN", "BLUE", "BROWN", "ORANGE", "CYAN", "MAGENTA", "BLACK", "GREEN", "BLUE",
			"BROWN", "ORANGE", "CYAN", "MAGENTA", "BLACK", "GREEN", "BLUE", "BROWN", "ORANGE", "CYAN", "MAGENTA",
			"BLACK", "GREEN", "BLUE", "BROWN", "ORANGE", "CYAN", "MAGENTA", "BLACK", "GREEN", "BLUE", "BROWN" };

	public ChatController() {

		FXMLLoader loader = new FXMLLoader(getClass().getResource("/resources/fxml/ChatRoom.fxml"));
		loader.setController(this);
		loader.setRoot(this);
		scene = new Scene(this);

		try {
			loader.load();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public void initClient(String host, int port, String userName, String password, Role role) {
		chatClient = new ChatClient(host, port, userName, password, role);
	}

	//Update the ChatScene with the ClientInfo

	public void initChatScene() {

		// Clear
		privateChatControllers.clear();
		messageField.clear();
		messagesArea.clear();

		// Resident
		if (chatClient.getRole() == Role.RESIDENT) {
			// TopLabel
			topLabel.setText(chatClient.getUserName() + "   ADMIN");
			// SentPrivateMessage
			sentPrivateMessage.setOnAction(ac -> {

				if (listView.getItems().size() != 0 && !listView.getSelectionModel().isEmpty()) {
					String selected = listView.getSelectionModel().getSelectedItem();

					if (privateChatControllers.stream().anyMatch(window -> {
						if (window.receiver.equals(selected)) {
							window.show();
							return true;
						}
						return false;
					})) {
						System.out.println("Already exists!!!");
					} else {
						PrivateChatController window = new PrivateChatController(chatClient.getUserName(), selected);
						privateChatControllers.add(window);
						window.show();
					}

				}

			});
			sentPrivateMessage.setDisable(false);
			timer.setVisible(false);

			// Guest
		} else {
			// TopLabel
			topLabel.setText(chatClient.getUserName() + "   GUEST");
			// SendPrivateMessage
			sentPrivateMessage.setDisable(true);
			timer.setVisible(true);

		}

		// MessageField
		messageField.setOnAction(ac -> {
			String text = messageField.getText();
			if (!text.isEmpty()) {
				if (text.equals("QUIT"))
					chatClient.sentMessage("QUIT");
				else if (text.equals("HELLO"))
					chatClient.sentMessage("HELLO");
				else if (text.equals("TOTAL"))
					chatClient.sentMessage("TOTAL");
				else if (text.equals("PROOMLS"))
					chatClient.sentMessage("PROOMLS");
				else if (text.equals("INFO"))
					chatClient.sentMessage("INFO");
				else {

					// Print my Message to Me
					int lengthBefore = messagesArea.getText().length();
					messagesArea.appendText((text.isEmpty() ? "" : "\n") + " Me" + ": " + text);
					messagesArea.setStyle(lengthBefore, (lengthBefore + 6),
							"-fx-font-weight:bold; -fx-font-size:14px; -fx-fill:maroon;");

					// Send it
					chatClient.sentMessage("GMESS" + text);
				}
				messageField.clear();
			}
		});

		messageField.setCursor(new ImageCursor(new Image(getClass().getResourceAsStream("/resources/images/pencil.png")), 0, 32));

		listView.setCellFactory(new Callback<ListView<String>, ListCell<String>>() {
			@Override
			public ListCell<String> call(ListView<String> list) {
				return new CostumeCell();
			}
		});

		// SentMessage
		sentMessage.setOnAction(messageField.getOnAction());

	}

	private static class CostumeCell extends ListCell<String> {
		@Override
		public void updateItem(String item, boolean empty) {
			super.updateItem(item, empty);
			// If item is empty
			if (empty) {
				setGraphic(null);
				setText(null);
			} else {
				setContentDisplay(ContentDisplay.RIGHT);
				ImageView imageView = new ImageView(
						new Image(getClass().getResourceAsStream("/resources/images/" + map.get(item) + ".png")));
				setGraphic(imageView);
				setText(item);
			}
		}
	}

	// Connect the Client to Server
	public boolean connectToServer() {
		return chatClient.connectToServer();
	}

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		System.out.println("ChatScene Initialized");

		// TopLabel
		topLabel.setStyle(
				"-fx-background-color:#212F3C; -fx-background-radius:0px; -fx-font-size:18px; -fx-font-weight:700; -fx-text-fill:#f4f6f7;");

		timer = new Timer(85, 32);
		bottomGridPane.add(timer, 2, 0);
	}

	// ChatClient Class
	public class ChatClient implements Runnable {

		Socket socket;
		Thread thread;
		BufferedReader fromServer;
		PrintWriter toServer;
		private String userName;
		private String password;
		private Role role;
		String host;
		int port;

		public ChatClient(String host, int port, String userName, String password, Role role) {
			this.host = host;
			this.port = port;
			this.userName = userName;
			this.password = password;
			this.role = role;

		}

		//Connects to the Server

		public boolean connectToServer() {

			//Initialize the Socket
			try {

				socket = new Socket(host, port);

				toServer = new PrintWriter(socket.getOutputStream(), true);
				fromServer = new BufferedReader(new InputStreamReader(socket.getInputStream()));

				// Start the Thread
				thread = new Thread(this);
				thread.start();

				sentMessage("LOGIN" + userName + "><:><" + password + "><:><" + role);
				sentMessage("TOTAL");
				sentMessage("GETALLMESS");
			} catch (UnknownHostException e) {
				Platform.runLater(() -> {
					Notifications.create().title("Unknown Host").text("Can't find that host:\n[" + e.getMessage() + "]")
							.showError();
				});
				return false;
			} catch (IOException e) {
				Platform.runLater(() -> {
					Notifications.create().title(e.getMessage()).text("Can't connect to server:\n[" + host + "]")
							.showError();
				});
				e.printStackTrace();
				return false;
			}
			return true;

		}

		//Sends the specific message to server
		public void sentMessage(String message) {
			toServer.println(message);
		}

		public String getUserName() {
			return userName;
		}

		public String getPassword() {
			return password;
		}

		public Role getRole() {
			return role;
		}

		private void disconnectFromServer() {
			thread = null;

			// Close toServer
			toServer.close();

			// Close fromServer
			try {
				fromServer.close();
			} catch (IOException e) {
				e.printStackTrace();
			}

			// Close the socket
			try {
				socket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		String message;

		// Implements the Thread
		@Override
		public void run() {

			// Read Server Messages

			while (thread != null) {
				try {
					message = fromServer.readLine();

					 System.out.println(message);
					// ----------------------- LIST WITH ALL CLIENTS IN ROOM
					if (message.startsWith("ROOMLS")) {
						String message = this.message;
						// Clear the previous List
						Platform.runLater(() -> {
							map.clear();
							listView.getItems().clear();
							Arrays.stream(message.substring(6).split(";")).forEach(s -> {
								String[] z = s.split("><:><");
								map.put(z[0], z[1]);
							});
							listView.getItems().addAll(map.keySet());
							usersInRoom.setText("Users in Room: " + listView.getItems().size() + 1);
						});
						// ----------------------- ADD THIS CLIENT IN LIST
					} else if (message.startsWith("ADD")) {
						String[] array = this.message.substring(3).split("><:><");
						Platform.runLater(() -> {
							map.put(array[0], array[1]);
							listView.getItems().add(array[0]);
							usersInRoom.setText("Users in Room: " + (map.size() + 1));
						});

						// ----------------------- REMOVE THIS CLIENT FROM LIST
					} else if (message.startsWith("REMOVE")) {
						String message = this.message.substring(6);
						Platform.runLater(() -> {
							map.remove(message);
							listView.getItems().remove(message);
							usersInRoom.setText("Users in Room: " + (map.size() + 1));
						});
						// ----------------------- GOT TOTAL CLIENTS IN ROOM
					} else if (message.startsWith("TOTAL")) {
						String message = this.message;
						Platform.runLater(() -> {
							usersInRoom.setText("Users in Room: " + message.substring(5));
						});

						// ----------------------- GOT Total Clients in Room
					} else if (message.startsWith("GMESS")) {
						String[] array = message.substring(5).split("><:><");
						int lengthBefore = messagesArea.getText().length();

						//
						Platform.runLater(() -> {
							messagesArea.appendText(
									(messagesArea.getText().isEmpty() ? "" : "\n") + " " + array[0] + ": " + array[1]);
							messagesArea.setStyle(lengthBefore, (lengthBefore + array[0].length() + 3),
									"-fx-font-weight:bold; -fx-font-size:14px; -fx-fill:"
											+ colors[new ArrayList<String>(map.keySet()).indexOf(array[0])] + ";");
						});

						// ----------------------- GOT Private Message
					} else if (message.startsWith("PMESS")) {
						String[] array = message.substring(5).split("><:><");

						if (privateChatControllers.stream().anyMatch(window -> {
							if (window.receiver.equals(array[0])) {
								window.update(" " + array[0] + ": " + array[1]);
								return true;
							}
							return false;
						})) {

						} else {
							Platform.runLater(() -> {
								PrivateChatController window = new PrivateChatController(userName, array[0]);
								privateChatControllers.add(window);
								window.update(" " + array[0] + ": " + array[1]);
							});
						}

						// DISCONNECTED FROM SERVER
					} else if (message.startsWith("DISC")) {
						String message = this.message;
						disconnectFromServer();
						Platform.runLater(() -> {
							LoginController.mediaPlayer.play();
							Client.stage.setScene(Client.loginController.getScene());
							privateChatControllers.forEach(window -> window.stage.close());
							timer.stopClock();
							Notifications.create().title("Server Disconnection")
									.text("You have been disconnected from the Server.\n" + message.substring(4))
									.showWarning();
						});

						// ----------------------- GOT A TIMELIMIT by Server
					} else if (message.startsWith("TIMELIMIT")) {
						timer.startClock(Integer.parseInt(message.substring(9)));

						// ------------------ GOT INFORMATIONS ABOUT THIS CLIENT
					} else if (message.startsWith("INFO")) {
						String[] infos = message.substring(4).split("><:><");
						Platform.runLater(() -> {
							messagesArea.appendText((messagesArea.getText().isEmpty() ? "" : "\n") + " LoggedInDate: "
									+ infos[0] + "\n LoggedInTime: " + infos[1]);
						});
					} else {
						String message = this.message;
						Platform.runLater(() -> {
							messagesArea.appendText((messagesArea.getText().isEmpty() ? "" : "\n") + message);
						});
					}

				} catch (IOException e) {
					e.printStackTrace();
					thread = null;
				}
			}
		}

	}
}
