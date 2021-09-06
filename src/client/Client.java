package client;

import javafx.application.Application;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class Client extends Application {

	static Stage stage;
	public static ChatController chatController;
	static LoginController loginController;

	@Override
	public void start(Stage stage) throws Exception {
		Client.stage = stage;
		chatController = new ChatController();
		loginController = new LoginController();

		// Stage
		stage.setTitle("Messenger");
		stage.setMinWidth(600);
		stage.setMinHeight(450);
		stage.setMaxWidth(700);
		stage.setMaxHeight(500);
		stage.initStyle(StageStyle.UNIFIED);
		stage.setOnCloseRequest(c -> {
			System.exit(0);
		});

		// Initialize Scenes
		loginController.getScene().getStylesheets().add(getClass().getResource("/resources/styles/style.css").toExternalForm());
		chatController.getScene().getStylesheets().add(getClass().getResource("/resources/styles/style.css").toExternalForm());
		stage.setScene(loginController.getScene());
		stage.show();
	}

	public static void main(String[] args) {
		launch(args);
	}
}
