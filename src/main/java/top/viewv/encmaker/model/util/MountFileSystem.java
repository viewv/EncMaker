package top.viewv.encmaker.model.util;

import org.cryptomator.frontend.fuse.mount.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class MountFileSystem {

    private static final Logger LOG = LoggerFactory.getLogger(MountFileSystem.class);

    public static Mount mount(Path pathToMirror, Path mountPoint){
        Mounter mounter = FuseMountFactory.getMounter();

        EnvironmentVariables envVars = EnvironmentVariables.create()
                .withFlags(mounter.defaultMountFlags())
                .withMountPoint(mountPoint)
                .withFileNameTranscoder(mounter.defaultFileNameTranscoder())
                .build();

        CountDownLatch barrier = new CountDownLatch(1);
        Consumer<Throwable> onFuseMainExit = throwable -> barrier.countDown();

        try {
            Mount mnt = mounter.mount(pathToMirror,envVars,onFuseMainExit,false);
            LOG.info("Mounted successfully");
            return mnt;
        }catch (FuseMountException e){
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

        return null;
    }
}
