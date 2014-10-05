package hpcrc.aut.physicalinfodetection;

import backtype.storm.Config;
import backtype.storm.LocalCluster;
import backtype.storm.StormSubmitter;
import backtype.storm.generated.AlreadyAliveException;
import backtype.storm.generated.InvalidTopologyException;
import backtype.storm.topology.TopologyBuilder;
import nl.minvenj.nfi.storm.kafka.KafkaSpout;

/**
 * Hello world!
 *
 */
public class App
{
    public static void main( String[] args )
    {
        DataBaseConnector dbConnector = new DataBaseConnector();

        TopologyBuilder builder = new TopologyBuilder();

        builder.setSpout("kafka", new KafkaSpout());
        builder.setBolt("bolt", new ReportBolt()).shuffleGrouping("kafka");

        Config config = new Config();
        // configure kafka spout (values are available as constants on ConfigUtils)
        config.put("kafka.spout.topic", "test");
        // kafka consumer configuration, see below
        config.put("kafka.zookeeper.connect", "localhost:2181");
        config.put("kafka.consumer.timeout.ms", 100);

        LocalCluster cluster = new LocalCluster();
        cluster.submitTopology("my-topology",config , builder.createTopology());
//        try {
//            StormSubmitter.submitTopology("PhysicalInfoDetection",
//                    config, builder.createTopology());
//        } catch (AlreadyAliveException e) {
//            e.printStackTrace();
//        } catch (InvalidTopologyException e) {
//            e.printStackTrace();
//        }
    }
}
