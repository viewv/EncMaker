package top.viewv.encmaker.controller;

import io.github.palexdev.materialfx.controls.MFXButton;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import org.cryptomator.frontend.fuse.mount.FuseMountException;
import org.cryptomator.frontend.fuse.mount.Mount;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import top.viewv.encmaker.model.filesystem.EncBoxFileSystemProvider;
import top.viewv.encmaker.model.location.LocationEntity;
import top.viewv.encmaker.model.location.LocationSerialize;
import top.viewv.encmaker.model.util.Entity;
import top.viewv.encmaker.model.util.Serialize;

import java.awt.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.nio.file.FileSystem;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.spi.FileSystemProvider;
import java.util.*;

import static top.viewv.encmaker.model.util.MountFileSystem.mount;

public class MainController implements Initializable {
    public VBox itemContainer;
    public AnchorPane mainPane;
    public MFXButton lockButton;
    public Label stateLabel;
    public Label pathLabel;
    public Label volumeNameLabel;
    public MFXButton openButton;
    public AnchorPane toolPane;
    public MFXButton addVolumeButton;
    public MFXButton delButton;

    private LocationEntity locationEntity;

    private boolean locked;
    private String currentName;
    private String currentPath;

    private ItemController itemController;

    private static final Logger LOG = LoggerFactory.getLogger(MainController.class);

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        toolPane.setDisable(true);
        toolPane.setVisible(false);

        refreshNode();
    }

    public void addVolume() throws FileNotFoundException {

        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Input your volume name");
        dialog.setHeaderText("Set your volume name");
        dialog.setContentText("Volume name:");

        Optional<String> result = dialog.showAndWait();
        String name;
        if (result.isPresent()) {
            name = result.get();
            DirectoryChooser chooser = new DirectoryChooser();
            File selectPath = chooser.showDialog(null);
            if (selectPath != null) {

                Entity entity = new Entity();

                String password;

                URI uri = new File(this.currentPath).toURI();
                System.out.println(uri);

                Dialog<String> passwodialog = new Dialog<>();
                passwodialog.setTitle("Set password");
                passwodialog.setHeaderText("Enter your volume password");

                ButtonType buttonType = new ButtonType("Set", ButtonBar.ButtonData.OK_DONE);
                passwodialog.getDialogPane().getButtonTypes().addAll(buttonType, ButtonType.CANCEL);

                GridPane gridPane = new GridPane();
                gridPane.setHgap(10);
                gridPane.setVgap(10);
                gridPane.setPadding(new Insets(20, 20, 10, 10));

                PasswordField pwd = new PasswordField();
                pwd.setPromptText("password");

                gridPane.add(new Label("Password:"), 0, 0);
                gridPane.add(pwd, 1, 0);

                passwodialog.getDialogPane().setContent(gridPane);

                passwodialog.setResultConverter(dialogButton -> {
                    if (dialogButton == buttonType) {
                        return pwd.getText();
                    }
                    return null;
                });

                Optional<String> passwordresult = passwodialog.showAndWait();

                if (passwordresult.isPresent()){
                    password = passwordresult.get();
                    entity.password = password;

                    Serialize.serialize(entity, selectPath.getAbsolutePath()+File.pathSeparator+"p.lk");

                    locationEntity.location.put(name, selectPath.getAbsolutePath());
                    LocationSerialize.serialize(locationEntity, "vault.ser");

                    refreshNode();
                    sendAlert(
                            Alert.AlertType.INFORMATION,
                            "Information",
                            "Successfully created" + name
                    );
                }else {
                    sendAlert(
                            Alert.AlertType.WARNING,
                            "Information",
                            "Password needed to set!"
                    );
                }
            } else {
                sendAlert(
                        Alert.AlertType.INFORMATION,
                        "Information",
                        "You must set your volume path!"
                );
            }
        } else {
            sendAlert(
                    Alert.AlertType.INFORMATION,
                    "Information",
                    "You must set your volume name!"
            );
        }
    }

    public void lock() throws IOException, FuseMountException {
        if (this.locked) {
            System.out.println("try to unlock");

            String password;

            URI uri = new File(this.currentPath).toURI();
            System.out.println(uri);

            Dialog<String> dialog = new Dialog<>();
            dialog.setTitle("Unlock");
            dialog.setHeaderText("Enter your volume password");

            ButtonType buttonType = new ButtonType("Unlock", ButtonBar.ButtonData.OK_DONE);
            dialog.getDialogPane().getButtonTypes().addAll(buttonType, ButtonType.CANCEL);

            GridPane gridPane = new GridPane();
            gridPane.setHgap(10);
            gridPane.setVgap(10);
            gridPane.setPadding(new Insets(20, 20, 10, 10));

            PasswordField pwd = new PasswordField();
            pwd.setPromptText("password");

            gridPane.add(new Label("Password:"), 0, 0);
            gridPane.add(pwd, 1, 0);

            dialog.getDialogPane().setContent(gridPane);

            dialog.setResultConverter(dialogButton -> {
                if (dialogButton == buttonType) {
                    return pwd.getText();
                }
                return null;
            });

            Optional<String> result = dialog.showAndWait();
            if (result.isPresent()) {
                password = result.get();

                if (password.length() != 0) {
                    File plk = new File(currentPath+File.pathSeparator+"p.lk");

                    if (plk.exists()){
                        Entity entity = Serialize.deserialize(currentPath+File.pathSeparator+"p.lk");
                        if (password.equals(entity.password)){
                            FileSystemProvider provider = new EncBoxFileSystemProvider();
                            Map<String, String> env = new HashMap<>();
                            env.put(EncBoxFileSystemProvider.CIPHER_ALGORITHM, "AES");
                            env.put(EncBoxFileSystemProvider.CIPHER_ALGORITHM_MODE, "CTR");
                            env.put(EncBoxFileSystemProvider.CIPHER_ALGORITHM_PADDING, "NoPadding");
                            env.put(EncBoxFileSystemProvider.SECRET_KEY, password);
                            env.put(EncBoxFileSystemProvider.REVERSE_MODE, "false");
                            env.put(EncBoxFileSystemProvider.FILESYSTEM_ROOT_URI, uri.toString());

                            FileSystem fileSystem = provider.newFileSystem(URI.create("enc:///"), env);

                            //TODO currently only macOS support
                            String uuid = UUID.randomUUID().toString();
                            Path p = fileSystem.getPath(currentPath);
                            Path m = Paths.get("/Volumes/" + uuid);

                            System.out.println("Start mount");

                            try {
                                Mount mnt = mount(p,m);

                                if (mnt != null) {
                                    itemController.setMnt(mnt);
                                    itemController.setUuid(uuid);
                                    itemController.setLocked(false);

                                    //Main UI change
                                    this.stateLabel.setText("UNLOCKED");
                                    this.openButton.setDisable(false);
                                    this.delButton.setDisable(true);
                                    this.lockButton.setText("Lock");

                                    //state change
                                    this.locked = false;

                                    reveal(mnt);

                                } else {
                                    sendAlert(Alert.AlertType.ERROR,
                                            "Error",
                                            "Mount failed!");
                                }

                            } catch (Exception e) {
                                e.printStackTrace();
                                sendAlert(Alert.AlertType.ERROR,
                                        "Error",
                                        "You must enter password!");
                            }
                        }else {
                            sendAlert(Alert.AlertType.ERROR,
                                    "Error",
                                    "Error password");
                        }
                    }else {
                        sendAlert(Alert.AlertType.ERROR,
                                "Error",
                                "Not a vault folder!");
                    }
                } else {
                    sendAlert(Alert.AlertType.ERROR,
                            "Error",
                            "Empty password!");
                }
            } else {
                sendAlert(Alert.AlertType.ERROR,
                        "Error",
                        "You must enter password!");
            }

        } else {
            System.out.println("try to lock");
            Mount mnt = itemController.getMnt();

            if (mnt != null){
                try {
                    mnt.unmount();
                } catch (FuseMountException e) {
                    LOG.info("Unable to perform regular unmount.", e);
                    LOG.info("Forcing unmount...");
                    mnt.unmountForced();
                }
                LOG.info("Unmounted successfully");

                itemController.setMnt(null);
                itemController.setUuid(null);
                itemController.setLocked(true);

                //Main UI change
                this.stateLabel.setText("LOCKED");
                this.openButton.setDisable(true);
                this.delButton.setDisable(false);
                this.lockButton.setText("Unlock");

                //state change
                this.locked = true;

                sendAlert(Alert.AlertType.INFORMATION,
                        "Successful!",
                        "You have unmount this volume");
            }
        }
    }

    public void open() {
        Mount mnt = itemController.getMnt();
        if (mnt != null){
            reveal(mnt);
        }
    }

    public void delete() {
        try {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirmation");
            alert.setHeaderText("Do you really want to delete?");
            alert.setContentText("Confirm?");

            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK){
                if (locationEntity.location.containsKey(currentName)){
                    locationEntity.location.remove(currentName);
                    LocationSerialize.serialize(locationEntity,"vault.ser");
                    itemContainer.getChildren().remove(itemController.getNode());

                    this.toolPane.setDisable(true);
                    this.toolPane.setVisible(false);
                }
            }
        }catch (Exception e){
            sendAlert(Alert.AlertType.ERROR,
                    "Oops",
                    "Unknown error");
        }
    }

    private void reveal(Mount mnt) {
        try {
            mnt.reveal(path -> {
                if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.OPEN)) {
                    Desktop.getDesktop().open(path.toFile());
                } else {
                    throw new UnsupportedOperationException("Desktop API to browse files not supported.");
                }
            });
        } catch (Exception e) {
            LOG.warn("Reveal failed.", e);
            sendAlert(Alert.AlertType.ERROR,
                    "Error",
                    "Reveal failed!");
        }
    }


    public void setMainState(boolean locked, String name, String path, ItemController controller) {
        this.locked = locked;
        this.currentName = name;
        this.currentPath = path;
        this.toolPane.setVisible(true);
        this.toolPane.setDisable(false);
        this.volumeNameLabel.setText(name);
        this.pathLabel.setText(path);

        this.itemController = controller;

        if (locked) {
            this.stateLabel.setText("LOCKED");
            this.lockButton.setText("Unlock");
            this.openButton.setDisable(true);
            this.delButton.setDisable(false);
        } else {
            this.stateLabel.setText("UNLOCKED");
            this.lockButton.setText("Lock");
            this.openButton.setDisable(false);
            this.delButton.setDisable(true);
        }
    }

    private void sendAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void refreshNode() {
        itemContainer.getChildren().clear();
        try {
            locationEntity = LocationSerialize.deserialize("vault.ser");
            Map<String, String> map = locationEntity.location;

            for (Map.Entry<String, String> entry : map.entrySet()) {
                Node node;
                FXMLLoader loader = new
                        FXMLLoader(Objects.requireNonNull(getClass()).getClassLoader()
                        .getResource("ui/item.fxml"));
                node = loader.load();
                ItemController itemController = loader.getController();

                String name = entry.getKey();
                String path = entry.getValue();
                itemController.setVolumeName(name);
                itemController.setVolumePath(path);
                itemController.setMainController(this);
                itemController.setLocked(true);
                itemController.setNode(node);

                itemContainer.getChildren().add(node);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
