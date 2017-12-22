package wuxian.me.slightsniffer;

import org.junit.Test;

import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;

import static org.junit.Assert.*;

/**
 * Created by wuxian on 22/12/2017.
 */
public class DirectoryWatcherTest {

    @Test
    public void testWatcher() {
        String path = "/Users/wuxian/Desktop";

        DirectoryWatcher.createWatcher(path, new WatcherCallback() {
                    @Override
                    public void execute(WatchEvent.Kind<?> kind, String path) {
                        System.out.println("kind: " + kind.name() + " ,path: " + path);
                    }
                }, StandardWatchEventKinds.ENTRY_CREATE,
                StandardWatchEventKinds.ENTRY_MODIFY,
                StandardWatchEventKinds.ENTRY_DELETE);

        while (true) {

        }
    }

}