package Sem2FP;

import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.*;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import java.io.IOException;

public class Main extends Application {
    private Server server;
    private GridPane userMenuPane = new GridPane();

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {
        stage.setTitle("Client2P");
        try {
            setUpUserMenu(stage);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setUpUserMenu(Stage stage) throws IOException {
        //gridpane style
        userMenuPane.setStyle("-fx-background-color: linear-gradient(to right,#22e0f3,#f980fd)");
        userMenuPane.setAlignment(Pos.CENTER);
        userMenuPane.setVgap(30);

        TextField nameInput = new TextField("Enter your name");
        nameInput.setAlignment(Pos.TOP_CENTER);

        Button acceptButton = new Button("Start!");

        //text if an error occurs
        final Text err = new Text();
        err.setFill(Color.RED);

        //setting title style
        Font font = Font.loadFont(getClass().getResource("Fonts/Baloo2-ExtraBold.ttf").toString(), 120);
        Text title = new Text("Pool 2P");
        title.setFont(font);
        title.setEffect(new DropShadow());
        title.setFill(Color.YELLOW);
        title.setStroke(Color.BLACK);
        title.setStrokeWidth(3);
        ((DropShadow) title.getEffect()).setOffsetX(-10);
        ((DropShadow) title.getEffect()).setOffsetY(10);

        //adding all elements to the pane
        userMenuPane.add(new Text("                     "), 0, 0);
        userMenuPane.add(title, 1, 0);
        userMenuPane.add(nameInput, 1, 1);
        userMenuPane.add(acceptButton, 2, 1);
        userMenuPane.add(err, 1, 2);

        //set the stage
        Scene userMenu = new Scene(userMenuPane, 1600, 900);
        stage.setScene(userMenu);
        stage.show();

        acceptButton.setOnMouseClicked((event) -> {
            if (nameInput.getText().isEmpty()) {
                //if user hasnt entered anything in textfield
                err.setText("err: please enter name");
            } else {
                try {
                    server = new Server("localhost", 69);
                    //create game if connection to server is created without trowing exceptions  and call its start method
                    new Game(server, nameInput.getText(), stage).start();
                } catch (IOException e) {
                    //this exception will be thrown if cant connect to server
                    err.setText("err: server is down or already hosting maximum number of games");
                } catch (Exception e2) {
                    //if an error occurs while trying to create game
                    err.setText("err: failed to create game");
                }
            }
        });
    }
}


