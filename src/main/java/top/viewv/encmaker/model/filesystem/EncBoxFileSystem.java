package top.viewv.encmaker.model.filesystem;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.UserPrincipalLookupService;
import java.nio.file.spi.FileSystemProvider;
import java.util.Iterator;
import java.util.Set;

class EncBoxFileSystem extends FileSystem {
    private final FileSystemProvider provider;
    private final FileSystem subFileSystem;

    EncBoxFileSystem(FileSystemProvider provider, FileSystem subFileSystem) {
        this.provider = provider;
        this.subFileSystem = subFileSystem;
    }

    static Path dismantle(Path mantle) {
        if (mantle == null)
            throw new NullPointerException();
        if (!(mantle instanceof EncBoxFileSystemPath))
            throw new ProviderMismatchException();
        return ((EncBoxFileSystemPath) mantle).getSubFSPath();
    }

    @Override
    public FileSystemProvider provider() {
        return provider;
    }

    @Override
    public void close() throws IOException {
        subFileSystem.close();
    }

    @Override
    public boolean isOpen() {
        return subFileSystem.isOpen();
    }

    @Override
    public boolean isReadOnly() {
        return subFileSystem.isReadOnly();
    }

    @Override
    public String getSeparator() {
        return subFileSystem.getSeparator();
    }

    @Override
    public Iterable<Path> getRootDirectories() {
        final Iterable<Path> roots = subFileSystem.getRootDirectories();
        return () -> {
            final Iterator<Path> itr = roots.iterator();
            return new Iterator<>() {
                @Override
                public boolean hasNext() {
                    return itr.hasNext();
                }

                @Override
                public Path next() {
                    return new EncBoxFileSystemPath(subFileSystem,
                            itr.next());
                }

                @Override
                public void remove() {
                    itr.remove();
                }
            };
        };
    }

    @Override
    public Iterable<FileStore> getFileStores() {
        return subFileSystem.getFileStores();
    }

    @Override
    public Set<String> supportedFileAttributeViews() {
        return subFileSystem.supportedFileAttributeViews();
    }

    @Override
    public Path getPath(String first, String... more) {
        return new EncBoxFileSystemPath(this, subFileSystem.getPath(first,
                more));
    }

    @Override
    public PathMatcher getPathMatcher(String syntaxAndPattern) {
        final PathMatcher matcher = subFileSystem
                .getPathMatcher(syntaxAndPattern);
        return path -> matcher.matches(dismantle(path));
    }

    @Override
    public UserPrincipalLookupService getUserPrincipalLookupService() {
        return subFileSystem.getUserPrincipalLookupService();
    }

    @Override
    public WatchService newWatchService() {
        throw new UnsupportedOperationException();
    }

    public FileSystem getSubFileSystem() {
        return this.subFileSystem;
    }
}
