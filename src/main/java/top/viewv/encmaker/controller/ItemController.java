package top.viewv.encmaker.controller;

import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import org.cryptomator.frontend.fuse.mount.Mount;

import java.net.URL;
import java.util.Objects;
import java.util.ResourceBundle;

public class ItemController implements Initializable {
    public AnchorPane itemPane;
    public Label volumeName;
    public Label volumePath;
    public ImageView imageLock;

    private MainController mainController;
    private boolean locked = true;
    private Image lockedImage;
    private Image unlockedImage;

    private Mount mnt;
    private String uuid;

    private Node node;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        this.lockedImage = new Image(Objects.requireNonNull(Objects.requireNonNull(getClass()).getClassLoader()
                .getResource("image/lock.png")).toString());
        this.unlockedImage = new Image(Objects.requireNonNull(Objects.requireNonNull(getClass()).getClassLoader()
                .getResource("image/unlock.png")).toString());
    }

    public void onClickItem() {
        mainController.setMainState(locked, volumeName.getText(), volumePath.getText(), this);
    }

    public void setMnt(Mount mnt) {
        this.mnt = mnt;
    }

    public Mount getMnt() {
        return mnt;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getUuid() {
        return uuid;
    }

    public void setNode(Node node) {
        this.node = node;
    }

    public Node getNode() {
        return node;
    }

    public void setVolumeName(String volumeName) {
        this.volumeName.setText(volumeName);
    }

    public void setVolumePath(String volumePath) {
        this.volumePath.setText(volumePath);
    }

    public void setMainController(MainController controller) {
        this.mainController = controller;
    }

    public void setLocked(boolean state) {
        this.locked = state;
        if (state) {
            this.imageLock.setImage(this.lockedImage);
        } else {
            this.imageLock.setImage(this.unlockedImage);
        }
    }
}
