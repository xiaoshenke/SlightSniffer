package wuxian.me.slightsniffer.util;

import java.nio.file.WatchEvent;

/**
 * Created by wuxian on 22/12/2017.
 */
public interface WatcherCallback {
    void execute(WatchEvent.Kind<?> kind, String path);
}
