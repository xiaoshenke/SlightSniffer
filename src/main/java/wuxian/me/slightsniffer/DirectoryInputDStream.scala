package wuxian.me.slightsniffer

import java.io.{File, IOException}
import java.nio.file.StandardWatchEventKinds
import java.nio.file.WatchEvent.Kind

import com.google.common.base.Charsets
import com.google.common.io.Files
import org.apache.spark.storage.StorageLevel
import org.apache.spark.streaming.StreamingContext
import org.apache.spark.streaming.receiver.Receiver
import org.apache.spark.streaming.dstream.ReceiverInputDStream
import wuxian.me.slightsniffer.util.{DirectoryWatcher, WatcherCallback}

import scala.reflect.{ClassTag, classTag}
import scala.util.control.NonFatal

/**
  * Created by wuxian on 22/12/2017.
  */
class DirectoryInputDStream[T: ClassTag](_ssc: StreamingContext,
                                         path: String,
                                         storageLevel: StorageLevel,
                                         streamType: Class[T])
  extends ReceiverInputDStream[T](_ssc) {
  override def getReceiver(): Receiver[T] = {
    new DirectoryReceiver[T](path, storageLevel, streamType)
  }
}

private class DirectoryReceiver[T: ClassTag](path: String,
                                             storageLevel: StorageLevel,
                                             streamType: Class[T])
  extends Receiver[T](storageLevel) {
  override def onStart(): Unit = {

    DirectoryWatcher.createWatcher(path, new WatcherCallback {
      override def execute(kind: Kind[_], filePath: String): Unit = {

        val file: File = new File(path)

        try
          val lines = Files.readLines(file, Charsets.UTF_8)
          var content: String = ""
          import scala.collection.JavaConversions._
          for (line <- lines) {
            content += line
          }
          store(content.asInstanceOf[T])
        catch {
          case e: IOException => {
          }
        }
      }
    }, StandardWatchEventKinds.ENTRY_CREATE)
  }

  override def onStop(): Unit = {}
}
