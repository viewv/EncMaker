package top.viewv.encmaker;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import top.viewv.encmaker.model.location.LocationEntity;
import top.viewv.encmaker.model.location.LocationSerialize;

import java.io.File;
import java.util.Objects;

public class MainApp extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        try {
            FXMLLoader loader = new
                    FXMLLoader(Objects.requireNonNull(getClass()).getClassLoader()
                    .getResource("ui/main.fxml"));

            System.out.println("Start EncMaker");

            // Print some useful information
            String javaversion = System.getProperty("java.version");
            System.out.println("Java Version: " + javaversion);

            String javaFxversion = System.getProperty("javafx.version");
            System.out.println("JavaFx Version: " + javaFxversion);

            final String os = System.getProperty("os.name");
            System.out.println("Operate System: " + os);

            String currentPath = System.getProperty("user.dir");
            System.out.println("Current Path: " + currentPath);

            //check path storage
            File currentDir = new File("vault.ser");
            if (!currentDir.exists()){
                LocationEntity locationEntity = new LocationEntity();
                LocationSerialize.serialize(locationEntity,"vault.ser");
            }

            Parent root = loader.load();
            Scene scene = new Scene(root);

            String css = Objects.requireNonNull(
                    MainApp.class.getClassLoader().
                            getResource("style/main.css")).toExternalForm();
            scene.getStylesheets().add(css);

            primaryStage.setTitle("EncMaker");
            primaryStage.setScene(scene);
            primaryStage.show();

            //Set min-size
            primaryStage.setMinWidth(primaryStage.getWidth());
            primaryStage.setMinHeight(primaryStage.getHeight());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
