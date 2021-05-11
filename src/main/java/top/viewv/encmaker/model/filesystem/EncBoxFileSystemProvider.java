package top.viewv.encmaker.model.filesystem;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.io.IOException;
import java.net.URI;
import java.nio.channels.FileChannel;
import java.nio.channels.SeekableByteChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.FileAttributeView;
import java.nio.file.spi.FileSystemProvider;
import java.security.spec.KeySpec;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;


public class EncBoxFileSystemProvider extends FileSystemProvider {

    public static final String URISCHEME = "enc";
    public static final String URISCHEMESPECPART = "///";

    public static final String CIPHER_ALGORITHM = "CipherAlgorithm";
    public static final String CIPHER_ALGORITHM_MODE = "CipherAlgorithmMode";
    public static final String CIPHER_ALGORITHM_PADDING = "CipherAlgorithmPadding";
    public static final String SECRET_KEY = "SecretKey";
    public static final String FILESYSTEM_ROOT_URI = "FileSystemRootURI";
    public static final String REVERSE_MODE = "ReverseMode";

    private static volatile EncBoxFileSystem encFileSystem;

    private byte[] key;

    public EncBoxFileSystemProvider() {
    }

    @Override
    public String getScheme() {
        return URISCHEME;
    }

    private void assertUriScheme(URI uri) {
        if (!uri.getScheme().equalsIgnoreCase(URISCHEME))
            throw new IllegalArgumentException();
    }

    private void assertUri(URI uri) {
        assertUriScheme(uri);
        if (!uri.getSchemeSpecificPart().equals(URISCHEMESPECPART))
            throw new IllegalArgumentException();
    }

    @Override
    public FileSystem newFileSystem(URI uri, Map<String, ?> env)
            throws IOException {
        assertUri(uri);
        System.out.println("newFilesystem");
        synchronized (EncBoxFileSystemProvider.class) {
            if (encFileSystem != null)
                throw new FileSystemAlreadyExistsException();

            // check environment
            try {
                String cipherAlgorithm = (String) env.get(CIPHER_ALGORITHM);
                if (cipherAlgorithm == null)
                    throw new IllegalArgumentException(
                            "Missing filesystem variable '" + CIPHER_ALGORITHM
                                    + "'");

                String cipherAlgorithmMode = (String) env
                        .get(CIPHER_ALGORITHM_MODE);
                if (cipherAlgorithmMode == null)
                    throw new IllegalArgumentException(
                            "Missing filesystem variable '"
                                    + CIPHER_ALGORITHM_MODE + "'");

                String cipherAlgorithmPadding = (String) env
                        .get(CIPHER_ALGORITHM_PADDING);
                if (cipherAlgorithmPadding == null)
                    throw new IllegalArgumentException(
                            "Missing filesystem variable '"
                                    + CIPHER_ALGORITHM_PADDING + "'");

                String cipherTransformation = cipherAlgorithm + "/"
                        + cipherAlgorithmMode + "/" + cipherAlgorithmPadding;
                Cipher.getInstance(cipherTransformation);

                String password = (String) env.get(SECRET_KEY);

                SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");

                KeySpec spec = new PBEKeySpec(password.toCharArray(), "EncBox-FileSystem".getBytes(StandardCharsets.UTF_8), 65545, 256);
                SecretKey key = factory.generateSecret(spec);

                byte[] secretKey = key.getEncoded();
                this.key = secretKey;

                if (secretKey == null)
                    throw new IllegalArgumentException(
                            "Missing filesystem variable '" + SECRET_KEY + "'");

            } catch (Exception e) {
                throw new IOException(e);
            }

            EncBoxFileSystem result = new EncBoxFileSystem(this,
                    FileSystems.getDefault());
            encFileSystem = result;
            return result;
        }
    }

    @Override
    public FileSystem getFileSystem(URI uri) {
        System.out.println("getFilesystem");
        assertUri(uri);
        FileSystem result = encFileSystem;
        if (result == null)
            throw new FileSystemNotFoundException();
        return result;
    }

    @Override
    public Path getPath(URI uri) {
        System.out.println("getPath");
        assertUriScheme(uri);
        if (encFileSystem == null)
            throw new FileSystemNotFoundException();

        // avoid unterminated recursion / to be able to run Paths.get(URI)
        uri = URI.create(encFileSystem.getSubFileSystem().provider()
                .getScheme()
                + ":" + uri.getSchemeSpecificPart());
        return new EncBoxFileSystemPath(encFileSystem, encFileSystem
                .getSubFileSystem().provider().getPath(uri));
    }

    @Override
    public void setAttribute(Path file, String attribute, Object value,
                             LinkOption... options) throws IOException {
        Files.setAttribute(EncBoxFileSystem.dismantle(file), attribute,
                value, options);
    }

    @Override
    public Map<String, Object> readAttributes(Path file, String attributes,
                                              LinkOption... options) throws IOException {
        return Files.readAttributes(EncBoxFileSystem.dismantle(file),
                attributes, options);
    }

    @Override
    public <V extends FileAttributeView> V getFileAttributeView(Path file,
                                                                Class<V> type, LinkOption... options) {
        return Files.getFileAttributeView(EncBoxFileSystem.dismantle(file),
                type, options);
    }

    @Override
    public <A extends BasicFileAttributes> A readAttributes(Path file,
                                                            Class<A> type, LinkOption... options) throws IOException {
        return Files.readAttributes(EncBoxFileSystem.dismantle(file), type,
                options);
    }

    @Override
    public void delete(Path file) throws IOException {
        System.out.println("delete");
        Files.delete(EncBoxFileSystem.dismantle(file));
    }

    @Override
    public void createSymbolicLink(Path link, Path target,
                                   FileAttribute<?>... attrs) throws IOException {
        System.out.println("crearsymblink");
        Files.createSymbolicLink(EncBoxFileSystem.dismantle(link),
                EncBoxFileSystem.dismantle(target), attrs);
    }

    @Override
    public void createLink(Path link, Path existing) throws IOException {
        System.out.println("createlink");
        Files.createLink(EncBoxFileSystem.dismantle(link),
                EncBoxFileSystem.dismantle(existing));
    }

    @Override
    public Path readSymbolicLink(Path link) throws IOException {
        System.out.println("readsymb");
        Path target = Files.readSymbolicLink(EncBoxFileSystem
                .dismantle(link));
        return new EncBoxFileSystemPath(encFileSystem, target);
    }

    @Override
    public void copy(Path source, Path target, CopyOption... options)
            throws IOException {
        System.out.println("copy");
        Files.copy(EncBoxFileSystem.dismantle(source),
                EncBoxFileSystem.dismantle(target), options);
    }

    @Override
    public void move(Path source, Path target, CopyOption... options)
            throws IOException {
        System.out.println("move");
        Files.move(EncBoxFileSystem.dismantle(source),
                EncBoxFileSystem.dismantle(target), options);
    }

    private DirectoryStream<Path> mantle(final DirectoryStream<Path> stream) {
        return new DirectoryStream<>() {
            @Override
            public Iterator<Path> iterator() {
                final Iterator<Path> itr = stream.iterator();
                return new Iterator<>() {
                    @Override
                    public boolean hasNext() {
                        return itr.hasNext();
                    }

                    @Override
                    public Path next() {
                        return new EncBoxFileSystemPath(encFileSystem,
                                itr.next());
                    }

                    @Override
                    public void remove() {
                        itr.remove();
                    }
                };
            }

            @Override
            public void close() throws IOException {
                stream.close();
            }
        };
    }

    @Override
    public DirectoryStream<Path> newDirectoryStream(Path dir,
                                                    DirectoryStream.Filter<? super Path> filter) throws IOException {
        System.out.println("newDirStream");
        return mantle(Files.newDirectoryStream(
                EncBoxFileSystem.dismantle(dir), filter));
    }

    @Override
    public void createDirectory(Path dir, FileAttribute<?>... attrs)
            throws IOException {
        System.out.println("CreatDir");
        Files.createDirectory(EncBoxFileSystem.dismantle(dir), attrs);
    }

    @Override
    public boolean isHidden(Path file) throws IOException {
        System.out.println("ifHidden");
        return Files.isHidden(EncBoxFileSystem.dismantle(file));
    }

    @Override
    public FileStore getFileStore(Path file) throws IOException {
        System.out.println("getFileStore");
        return Files.getFileStore(EncBoxFileSystem.dismantle(file));
    }

    @Override
    public boolean isSameFile(Path file, Path other) throws IOException {
        System.out.println("isSameFile");
        return Files.isSameFile(EncBoxFileSystem.dismantle(file),
                EncBoxFileSystem.dismantle(other));
    }

    @Override
    public void checkAccess(Path file, AccessMode... modes) throws IOException {
        System.out.println("checkAccess");
        if (modes.length == 0) {
            if (Files.exists(EncBoxFileSystem.dismantle(file)))
                return;
            else
                throw new NoSuchFileException(file.toString());
        }
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    public FileChannel newFileChannel(Path path, Set<? extends OpenOption> options, FileAttribute<?>... attrs) throws IOException {
        System.out.println("newFileChannel");
        System.out.println("Path: " + path);
        System.out.println(options);

        return new EncBoxFileChannel(
                EncBoxFileSystem.dismantle(path), key, options);
    }

    @Override
    public SeekableByteChannel newByteChannel(Path path, Set<? extends OpenOption> options, FileAttribute<?>... attrs) {
        return null;
    }
}
