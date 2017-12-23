package wuxian.me.slightsniffer;
import org.apache.spark.SparkConf
import org.apache.spark.mllib.stat.test.{BinarySample, StreamingTest}
import org.apache.spark.streaming.{Seconds, StreamingContext}
import org.apache.spark.util.Utils



/**
 * Created by wuxian on 23/12/2017.
 */
object DirectoryInputDStreamTest {

    def main(args: Array[String]) {

        val dataDir = "/Users/wuxian/Desktop"
        val batchDuration = Seconds(1)


        val conf = new SparkConf().setMaster("local").setAppName("StreamingTestExample")
        val ssc = new StreamingContext(conf, batchDuration)
        //ssc.checkpoint {val dir = Utils.createTempDir() dir.toString}

        var sc = new DirectoryStreamingContext(ssc)
        sc.createDirectoryStream(dataDir).map(name => {println(name)
            name})

        ssc.start()
        ssc.awaitTermination()
    }

}