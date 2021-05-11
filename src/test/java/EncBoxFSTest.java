import org.cryptomator.frontend.fuse.mount.*;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import top.viewv.encmaker.model.filesystem.EncBoxFileSystemProvider;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.spi.FileSystemProvider;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class EncBoxFSTest {

    private static final Logger LOG = LoggerFactory.getLogger(EncBoxFSTest.class);

    private static void mount(Path pathToMirror, Path mountPoint) {
        Mounter mounter = FuseMountFactory.getMounter();

        EnvironmentVariables envVars = EnvironmentVariables.create()
                .withFlags(mounter.defaultMountFlags())
                .withMountPoint(mountPoint)
                .withFileNameTranscoder(mounter.defaultFileNameTranscoder())
                .build();

        CountDownLatch barrier = new CountDownLatch(1);
        Consumer<Throwable> onFuseMainExit = throwable -> barrier.countDown();
        try (Mount mnt = mounter.mount(pathToMirror, envVars, onFuseMainExit, false)) {
            LOG.info("Mounted successfully. Enter anything to stop the server...");
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
            }
            System.in.read();
            //Thread.sleep(15000);
            try {
                mnt.unmount();
            } catch (FuseMountException e) {
                LOG.info("Unable to perform regular unmount.", e);
                LOG.info("Forcing unmount...");
                mnt.unmountForced();
            }
            LOG.info("Unmounted successfully. Exiting...");
        } catch (FuseMountException | IOException e) {
            LOG.error("Mount failed", e);
        }

        try {
            if (!barrier.await(5000, TimeUnit.MILLISECONDS)) {
                LOG.error("Wait on onFuseExit action to finish exceeded timeout. Exiting ...");
            } else {
                LOG.info("onExit action executed.");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            LOG.error("Main thread interrupted. Exiting without waiting for onFuseExit action");
        }
    }

    @Test
    public void encBoxFuseTest() throws IOException {
        String valutpath = "/Users/viewv/Develop/encfs";

        URI uri = new File(valutpath).toURI();
        System.out.println(uri);

        FileSystemProvider provider = new EncBoxFileSystemProvider();

        Map<String, String> env = new HashMap<>();
        env.put(EncBoxFileSystemProvider.CIPHER_ALGORITHM, "AES");
        env.put(EncBoxFileSystemProvider.CIPHER_ALGORITHM_MODE, "CTR");
        env.put(EncBoxFileSystemProvider.CIPHER_ALGORITHM_PADDING, "NoPadding");
        env.put(EncBoxFileSystemProvider.SECRET_KEY, "1234567890abc");
        env.put(EncBoxFileSystemProvider.REVERSE_MODE, "false");
        env.put(EncBoxFileSystemProvider.FILESYSTEM_ROOT_URI, uri.toString());

        FileSystem fileSystem = provider.newFileSystem(URI.create("enc:///"), env);

        Path p = fileSystem.getPath("/Users/viewv/Develop/encfs");
        Path m = Paths.get("/Volumes/" + UUID.randomUUID().toString());

        mount(p, m);
    }
}
