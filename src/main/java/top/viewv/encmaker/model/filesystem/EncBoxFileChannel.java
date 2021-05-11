package top.viewv.encmaker.model.filesystem;

import com.google.crypto.tink.Config;
import com.google.crypto.tink.Registry;
import com.google.crypto.tink.StreamingAead;
import com.google.crypto.tink.proto.AesGcmHkdfStreamingKey;
import com.google.crypto.tink.proto.AesGcmHkdfStreamingParams;
import com.google.crypto.tink.proto.HashType;
import com.google.crypto.tink.streamingaead.StreamingAeadConfig;
import com.google.protobuf.ByteString;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.*;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.security.GeneralSecurityException;
import java.util.Set;

import static java.nio.file.StandardOpenOption.READ;
import static java.nio.file.StandardOpenOption.WRITE;


public class EncBoxFileChannel extends FileChannel {

    private final FileChannel base;
    private final Path path;
    private final Set<? extends OpenOption> openOptions;
    private final StreamingAead cryptoPrimitive;
    private final Object positionLock = new Object();
    /**
     * The current position within the file, from a user perspective.
     */
    private long pos = 0;

    EncBoxFileChannel(final Path path, final byte[] encryptionKey, final Set<? extends OpenOption> openOptions) throws IOException {
        if (encryptionKey == null) {
            throw new IllegalStateException(String.format("Encryption key must not be null for file '%s'.", path));
        }
        this.openOptions = openOptions;
        this.path = path;
        this.base = FileChannel.open(path, openOptions);
        try {
            cryptoPrimitive = constructCryptoPrimitive(encryptionKey);
        } catch (GeneralSecurityException e) {
            throw new IOException(e);
        }
    }

    static StreamingAead constructCryptoPrimitive(byte[] keyBytes) throws GeneralSecurityException {
        Config.register(StreamingAeadConfig.LATEST);
        final AesGcmHkdfStreamingParams params = AesGcmHkdfStreamingParams.newBuilder()
                .setDerivedKeySize(32)
                .setCiphertextSegmentSize(128)
                .setHkdfHashType(HashType.SHA256)
                .build();
        AesGcmHkdfStreamingKey streamingKey = AesGcmHkdfStreamingKey.newBuilder()
                .setKeyValue(ByteString.copyFrom(keyBytes))
                .setParams(params)
                .build();
        return Registry.getPrimitive(StreamingAeadConfig.AES_GCM_HKDF_STREAMINGAEAD_TYPE_URL, streamingKey);
    }

    private SeekableByteChannel getInputChannel() throws IOException {
        if (Files.exists(path) && Files.size(path) > 0) {
            try {
                return cryptoPrimitive.newSeekableDecryptingChannel(FileChannel.open(path, READ), new byte[]{});
            } catch (GeneralSecurityException e) {
                throw new IOException(e);
            }
        }
        return null;
    }

    private WritableByteChannel getOutputChannel() throws IOException {
        try {
            return cryptoPrimitive.newEncryptingChannel(FileChannel.open(path, WRITE), new byte[]{});
        } catch (GeneralSecurityException e) {
            throw new IOException(e);
        }
    }

    @Override
    protected void implCloseChannel() throws IOException {
        if (base != null) {
            base.close();
        }
    }

    @Override
    public FileChannel position(long newPosition) {
        synchronized (positionLock) {
            this.pos = newPosition;
            return this;
        }
    }

    @Override
    public long position() {
        return pos;
    }

    @Override
    public int read(ByteBuffer dst) throws IOException {
        synchronized (positionLock) {
            int len = read(dst, pos);
            if (len > 0) {
                pos += len;
            }
            return len;
        }
    }

    @Override
    public long read(ByteBuffer[] byteBuffers, int i, int i1) {
        synchronized (positionLock) {
            long len = read(byteBuffers,i,i1);
            if (len > 0) {
                pos += len;
            }
            return len;
        }
    }

    @Override
    public synchronized int read(ByteBuffer dst, long position) throws IOException {
        if (position < 0) {
            throw new IllegalArgumentException("negative position");
        }
        try (final SeekableByteChannel inputChannel = getInputChannel()) {
            if (inputChannel != null) {
                if (position == inputChannel.size()) {
                    return -1;
                }
                inputChannel.position(position);
                return inputChannel.read(dst);
            }
        }
        return 0;
    }

    @Override
    public int write(ByteBuffer src) throws IOException {
        synchronized (positionLock) {
            int len = write(src, pos);
            if (len > 0) {
                pos += len;
            }
            return len;
        }
    }

    @Override
    public long write(ByteBuffer[] byteBuffers, int i, int i1) {
        synchronized (positionLock) {
            long len = write(byteBuffers,i,i1);
            if (len > 0) {
                pos += len;
            }
            return len;
        }
    }

    @Override
    public synchronized int write(ByteBuffer src, long position) throws IOException {
        if (!openOptions.contains(WRITE)) {
            throw new IllegalStateException("This encrypted FileChannel is readonly");
        }
        int bytesToWrite = src.remaining();
        final long newSize = Math.max(size(), position + bytesToWrite);
        final ByteBuffer tmp = ByteBuffer.allocate((int) newSize);
        read(tmp, 0);
        tmp.position((int) position);
        tmp.put(src.array(), 0, bytesToWrite);
        tmp.rewind();
        internalWrite(tmp);
        src.position(src.position() + bytesToWrite);

        return bytesToWrite;
    }

    private void internalWrite(ByteBuffer tmp) throws IOException {
        try (WritableByteChannel writableByteChannel = getOutputChannel()) {
            int bytesWritten = writableByteChannel.write(tmp);
            if (tmp.array().length != bytesWritten) {
                throw new IllegalStateException("failed to write bytes");
            }
        }
    }

    @Override
    public MappedByteBuffer map(MapMode mapMode, long l, long l1) {
        throw new UnsupportedOperationException();
    }

    @Override
    public FileLock lock(long l, long l1, boolean b) {
        throw new UnsupportedOperationException();
    }

    @Override
    public long size() throws IOException {
        try (final SeekableByteChannel inputChannel = getInputChannel()) {
            if (inputChannel != null) {
                return inputChannel.size();
            }
        }
        return 0;
    }

    @Override
    public FileChannel truncate(long size) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void force(boolean metaData) throws IOException {
        base.force(metaData);
    }

    @Override
    public long transferTo(long l, long l1, WritableByteChannel writableByteChannel) {
        throw new UnsupportedOperationException();
    }

    @Override
    public long transferFrom(ReadableByteChannel readableByteChannel, long l, long l1) {
        throw new UnsupportedOperationException();
    }

    @Override
    public FileLock tryLock(long position, long size, boolean shared)
            throws IOException {
        return base.tryLock(position, size, shared);
    }

    @Override
    public String toString() {
        return path.toString();
    }
}