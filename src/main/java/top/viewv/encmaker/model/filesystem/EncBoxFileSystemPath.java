package top.viewv.encmaker.model.filesystem;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.*;
import java.util.Iterator;

class EncBoxFileSystemPath implements Path {
    private final FileSystem subFS;
    private final Path subFSPath;

    EncBoxFileSystemPath(FileSystem subFS, Path subFSPath) {
        this.subFS = subFS;
        this.subFSPath = subFSPath;
    }

    @Override
    public FileSystem getFileSystem() {
        return subFS;
    }

    @Override
    public Path getParent() {
        return mantle(subFSPath.getParent());
    }

    @Override
    public Path getRoot() {
        return mantle(subFSPath.getRoot());
    }

    @Override
    public Path getFileName() {
        return mantle(subFSPath.getFileName());
    }

    @Override
    public Path getName(int index) {
        return mantle(subFSPath.getName(index));
    }

    @Override
    public int getNameCount() {
        return subFSPath.getNameCount();
    }

    @Override
    public Path subpath(int beginIndex, int endIndex) {
        return mantle(subFSPath.subpath(beginIndex, endIndex));
    }

    @Override
    public boolean isAbsolute() {
        return subFSPath.isAbsolute();
    }

    @Override
    public boolean startsWith(Path other) {
        return subFSPath.startsWith(EncBoxFileSystem.dismantle(other));
    }

    @Override
    public boolean startsWith(String other) {
        return subFSPath.startsWith(other);
    }

    @Override
    public boolean endsWith(Path other) {
        return subFSPath.endsWith(EncBoxFileSystem.dismantle(other));
    }

    @Override
    public boolean endsWith(String other) {
        return subFSPath.endsWith(other);
    }

    @Override
    public Path normalize() {
        return mantle(subFSPath.normalize());
    }

    @Override
    public Path resolve(Path other) {
        return mantle(subFSPath.resolve(EncBoxFileSystem.dismantle(other)));
    }

    @Override
    public Path resolve(String other) {
        return mantle(subFSPath.resolve(other));
    }

    @Override
    public Path resolveSibling(Path other) {
        return mantle(subFSPath.resolveSibling(EncBoxFileSystem.dismantle(other)));
    }

    @Override
    public Path resolveSibling(String other) {
        return mantle(subFSPath.resolveSibling(other));
    }

    @Override
    public Path relativize(Path other) {
        return mantle(subFSPath.relativize(EncBoxFileSystem.dismantle(other)));
    }

    @Override
    public int compareTo(Path other) {
        return subFSPath.compareTo(EncBoxFileSystem.dismantle(other));
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof EncBoxFileSystemPath))
            return false;
        return subFSPath.equals(EncBoxFileSystem.dismantle((EncBoxFileSystemPath) other));
    }

    @Override
    public int hashCode() {
        return subFSPath.hashCode();
    }

    @Override
    public Path toAbsolutePath() {
        return mantle(subFSPath.toAbsolutePath());
    }

    @Override
    public Path toRealPath(LinkOption... options) throws IOException {
        return mantle(subFSPath.toRealPath(options));
    }

    @Override
    public File toFile() {
        return subFSPath.toFile();
    }

    @Override
    public URI toUri() {
        String ssp = subFSPath.toUri().getSchemeSpecificPart();
        return URI.create(subFS.provider().getScheme() + ":" + ssp);
    }

    @Override
    public Iterator<Path> iterator() {
        final Iterator<Path> itr = subFSPath.iterator();
        return new Iterator<>() {
            @Override
            public boolean hasNext() {
                return itr.hasNext();
            }

            @Override
            public Path next() {
                return mantle(itr.next());
            }

            @Override
            public void remove() {
                itr.remove();
            }
        };
    }

    @Override
    public WatchKey register(WatchService watcher,
                             WatchEvent.Kind<?>[] events, WatchEvent.Modifier... modifiers) {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    public WatchKey register(WatchService watcher,
                             WatchEvent.Kind<?>... events) {
        throw new UnsupportedOperationException("not implemented");
    }

    private Path mantle(Path path) {
        return (path != null) ? new EncBoxFileSystemPath(subFS, path)
                : null;
    }

    @Override
    public String toString() {
        return subFSPath.toString();
    }

    public Path getSubFSPath() {
        return subFSPath;
    }
}
