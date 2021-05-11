import org.cryptomator.frontend.fuse.mount.Mount;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import top.viewv.encmaker.model.filesystem.EncBoxFileSystemProvider;
import top.viewv.encmaker.model.util.MountFileSystem;

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

public class MountTest {

    private static final Logger LOG = LoggerFactory.getLogger(EncBoxFSTest.class);

//    @Test
//    public void mountnewtest() throws IOException {
//        String valutpath = "/Users/viewv/Develop/encfs";
//
//        URI uri = new File(valutpath).toURI();
//        System.out.println(uri);
//
//        FileSystemProvider provider = new EncBoxFileSystemProvider();
//
//        Map<String, String> env = new HashMap<>();
//        env.put(EncBoxFileSystemProvider.CIPHER_ALGORITHM, "AES");
//        env.put(EncBoxFileSystemProvider.CIPHER_ALGORITHM_MODE, "CTR");
//        env.put(EncBoxFileSystemProvider.CIPHER_ALGORITHM_PADDING, "NoPadding");
//        env.put(EncBoxFileSystemProvider.SECRET_KEY, "1234567890abc");
//        env.put(EncBoxFileSystemProvider.REVERSE_MODE, "false");
//        env.put(EncBoxFileSystemProvider.FILESYSTEM_ROOT_URI, uri.toString());
//
//        FileSystem fileSystem = provider.newFileSystem(URI.create("enc:///"), env);
//
//        Path p = fileSystem.getPath("/Users/viewv/Develop/encfs");
//        Path m = Paths.get("/Volumes/" + UUID.randomUUID().toString());
//
//        Mount mnt = MountFileSystem.mount(p,m);
//
//        try {
//            mnt.reveal(path -> {
//                if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.OPEN)) {
//                    Desktop.getDesktop().open(path.toFile());
//                } else {
//                    throw new UnsupportedOperationException("Desktop API to browse files not supported.");
//                }
//            });
//        } catch (Exception e) {
//            LOG.warn("Reveal failed.", e);
//        }
//    }
}
