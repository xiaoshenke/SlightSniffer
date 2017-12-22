package wuxian.me.slightsniffer;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DirectoryWatcher {
    private static final Logger LOGGER = LoggerFactory.getLogger(DirectoryWatcher.class);

    private WatchService watchService = null;
    private final Map<WatchKey, Path> directories = new HashMap<>();
    private static ExecutorService executorService = Executors.newCachedThreadPool();
    ;
    private List<WatchEvent.Kind<?>> watchedEvents;

    private DirectoryWatcher() {
        try {
            watchService = FileSystems.getDefault().newWatchService();
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    public static DirectoryWatcher createWatcher(String watchPath, WatcherCallback callback, WatchEvent.Kind<?>... events) {
        return createWatcher(watchPath, true, callback, events);
    }

    public static DirectoryWatcher createWatcher(String watchPath, boolean recursive, WatcherCallback callback, WatchEvent.Kind<?>... events) {
        if (watchPath == null || watchPath.length() == 0 || callback == null || events == null || events.length == 0) {
            throw new RuntimeException("invalid watcher!");
        }
        DirectoryWatcher watcher = new DirectoryWatcher();
        watcher.setCallback(callback);
        watcher.setWatchEvents(events);
        watcher.watchDirectory(watchPath, recursive);
        return watcher;
    }


    public void watchDirectory(String path, boolean recursive) {
        if (recursive) {
            registerDirectoryTree(Paths.get(path));
        } else {
            registerDirectory(Paths.get(path));
        }

        if (watcherFuture == null || (watcherFuture != null && watcherFuture.isDone())) {
            watcherFuture = executorService.submit(new Runnable() {
                @Override
                public void run() {
                    realWatch();  //这里是个while循环
                }
            });
        }
    }

    private WatcherCallback callback;

    public void setCallback(WatcherCallback callback) {
        this.callback = callback;
    }

    public void setWatchEvents(WatchEvent.Kind<?>... events) {
        this.watchedEvents = new ArrayList<>(events.length);
        for (WatchEvent.Kind<?> event : events) {
            if (!watchedEvents.contains(event)) {
                watchedEvents.add(event);
            }
        }
    }


    private Future watcherFuture;


    private void realWatch() {
        try {
            while (true) {
                final WatchKey key = watchService.take();  //blocking?
                if (key == null) {
                    continue;
                }
                for (WatchEvent<?> event : key.pollEvents()) {

                    final WatchEvent.Kind<?> kind = event.kind();
                    if (kind == StandardWatchEventKinds.OVERFLOW) {
                        continue;
                    }
                    WatchEvent<Path> watchEventPath = (WatchEvent<Path>) event;

                    final Path contextPath = watchEventPath.context();  //relative path

                    final Path directoryPath = directories.get(key);
                    final Path absolutePath = directoryPath.resolve(contextPath);

                    //判断事件类别
                    switch (kind.name()) {
                        case "ENTRY_CREATE":
                            if (Files.isDirectory(absolutePath, LinkOption.NOFOLLOW_LINKS)) {
                                registerDirectoryTree(absolutePath); //为新增的目录及其所有子目录注册监控事件
                            } else {
                                LOGGER.info("新增文件：" + absolutePath);
                            }
                            break;
                        case "ENTRY_DELETE":
                            //LOGGER.info("删除：" + absolutePath);
                            break;
                        case "ENTRY_MODIFY":
                            //LOGGER.info("修改：" + absolutePath);
                            break;
                    }
                    callback.execute(kind, absolutePath.toAbsolutePath().toString());
                }
                boolean valid = key.reset();
                if (!valid) {
                    if (directories.get(key) != null) {
                        directories.remove(key);
                    }
                }
            }
        } catch (InterruptedException ex) {

        } finally {
            try {
                watchService.close();
            } catch (IOException ex) {

            }
        }
    }


    public void close() {
        if (watcherFuture != null && !watcherFuture.isDone()) {
            watcherFuture.cancel(true);
        }
    }

    private void registerDirectoryTree(Path path) {
        try {
            Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs)
                        throws IOException {
                    registerDirectory(dir);
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException ex) {

        }
    }

    private void registerDirectory(Path path) {

        WatchEvent.Kind<?>[] events = new WatchEvent.Kind[watchedEvents.size()];
        for (int i = 0; i < watchedEvents.size(); i++) {
            events[i] = watchedEvents.get(i);
        }

        try {
            WatchKey key = path.register(watchService, events);
            directories.put(key, path);
        } catch (IOException ex) {

        }
    }

}
