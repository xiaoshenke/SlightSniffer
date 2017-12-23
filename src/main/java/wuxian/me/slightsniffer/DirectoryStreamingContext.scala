
package wuxian.me.slightsniffer

import org.apache.spark.streaming.StreamingContext
import org.apache.spark.storage.StorageLevel

/** 用作spark streaming的directory connector,
  * 目前用于爬虫解析出新的文件,因此只监听CREATE事件即可
  */
class DirectoryStreamingContext(@transient val ssc: StreamingContext) extends Serializable {

  /**
    * @param storageLevel the receiver' storage tragedy of received data, default as MEMORY_AND_DISK_2
    * @return a stream of (listname, value)
    */
  def createDirectoryStream(path: String
                            //, storageLevel: StorageLevel = StorageLevel.MEMORY_AND_DISK_2
                           )
  = {
    new DirectoryInputDStream(ssc, path, StorageLevel.MEMORY_AND_DISK_2, classOf[String])
  }

}