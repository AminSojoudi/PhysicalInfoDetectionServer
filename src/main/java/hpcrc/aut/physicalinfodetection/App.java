package hpcrc.aut.physicalinfodetection;

import backtype.storm.Config;
import backtype.storm.LocalCluster;
import backtype.storm.StormSubmitter;
import backtype.storm.generated.AlreadyAliveException;
import backtype.storm.generated.InvalidTopologyException;
import backtype.storm.topology.TopologyBuilder;
import backtype.storm.tuple.Fields;
import nl.minvenj.nfi.storm.kafka.KafkaSpout;
import nl.minvenj.nfi.storm.kafka.util.ConfigUtils;

/**
 * Hello world!
 *
 */
public class App
{
    public static final double IDLE_THRESHOLD = 0.01;
    public static final double POSITION_COMPARISON_THRESHOLD = 0.005 ;
    public static final int MINIMUM_LOOP_FRAMES = 30;
    public static final String FIRST_JOINT = "AnkleLeft";
    public static final String SECOND_JOINT = "AnkleRight";
    public static final double CLASSIFICATION_THRESHOLD = 0.003;
    public static final boolean[] XYZ_AXIS = { true , true , false } ; // { x , y , z }


    private static final String KAFKA_SPOUT_ID = "kafka";
    private static final String DECODE_MESSAGE_ID = "decodeMessage";
    private static final String REMOVE_DISTANT_ID = "removeDistant";
    private static final String REMOVE_ROTATION_ID = "removeRotation";
    private static final String SAVE_TO_TEMP_DATA_ID = "saveToTempDB";
    private static final String FIND_LOOP_ID = "findLoop";
    private static final String CLASSIFY_ID = "classify";
    private static final String REPORT_ID = "report";



    public static void main( String[] args )
    {
        new DataBaseConnector();

        TopologyBuilder builder = new TopologyBuilder();

        builder.setSpout(KAFKA_SPOUT_ID, new KafkaSpout() , 2);
        builder.setBolt(DECODE_MESSAGE_ID , new DecodeMessage()).globalGrouping(KAFKA_SPOUT_ID);
        builder.setBolt(REMOVE_DISTANT_ID , new RemoveDistant()).globalGrouping(DECODE_MESSAGE_ID);
        builder.setBolt(SAVE_TO_TEMP_DATA_ID, new SaveTempData()).fieldsGrouping(REMOVE_DISTANT_ID, new Fields("UserID"));
        builder.setBolt(FIND_LOOP_ID , new FindLoop()).fieldsGrouping(SAVE_TO_TEMP_DATA_ID , new Fields("UserID"));
        builder.setBolt(CLASSIFY_ID , new Classify()).fieldsGrouping(FIND_LOOP_ID , new Fields("UserID"));
        builder.setBolt(REPORT_ID, new ReportBolt()).shuffleGrouping(CLASSIFY_ID);

        Config config = new Config();
        // configure kafka spout (values are available as constants on ConfigUtils)
        config.put("kafka.spout.topic", "hpcrc");
        // kafka consumer configuration, see below
        config.put("kafka.zookeeper.connect", "localhost:2181");
        config.put("kafka.consumer.timeout.ms", 10000);


        if (args.length != 0)
        {
            try {
                StormSubmitter.submitTopology("PhysicalInfoDetection",
                        config, builder.createTopology());
            } catch (AlreadyAliveException e) {
                e.printStackTrace();
            } catch (InvalidTopologyException e) {
                e.printStackTrace();
            }
        }
        else {
            LocalCluster cluster = new LocalCluster();
            cluster.submitTopology("PhysicalInfoDetection", config, builder.createTopology());
        }
    }
}
