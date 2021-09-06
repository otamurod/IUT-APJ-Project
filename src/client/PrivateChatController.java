package client;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class PrivateChatController extends BorderPane implements Initializable {

	@FXML
	private TextArea messagesArea;

	@FXML
	private TextField messageField;

	@FXML
	private Button sentMessage;

	@FXML
	private Label label;

	String senter;
	String receiver;
	Stage stage;

	public PrivateChatController(String senter, String receiver) {
		this.senter = senter;
		this.receiver = receiver;
		this.stage = new Stage();

		FXMLLoader loader = new FXMLLoader(getClass().getResource("/resources/fxml/PrivateChat.fxml"));
		loader.setController(this);
		loader.setRoot(this);

		try {
			loader.load();
		} catch (IOException e) {
			e.printStackTrace();
		}

		stage.setAlwaysOnTop(true);
		stage.initStyle(StageStyle.UNIFIED);
		stage.setScene(new Scene(this));
		stage.getScene().getStylesheets().add(getClass().getResource("/resources/styles/style.css").toExternalForm());
		stage.show();
	}

	//Updates the Text of MessagesArea

	public void update(String text) {
		Platform.runLater(() -> {
			messagesArea.appendText((messagesArea.getText().isEmpty() ? "" : "\n") + text);
			stage.show();
		});

	}

	//Shows the private chat Window

	public void show() {
		stage.show();
	}

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		System.out.println("Private Chat Window Initialized....");

		label.setText(senter + "-> private Chat With <-" + receiver);
		stage.setTitle(label.getText());

		messageField.setOnAction(ac -> {
			if (!messageField.getText().isEmpty()) {
				Client.chatController.chatClient
						.sentMessage("PMESS" + senter + "><:><" + messageField.getText() + "><:><" + receiver);
				update("Me" + ": " + messageField.getText());
				messageField.clear();
			}
		});

		sentMessage.setOnAction(messageField.getOnAction());
	}

}
