package hpcrc.aut.physicalinfodetection;

import backtype.storm.task.OutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseRichBolt;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Tuple;
import backtype.storm.tuple.Values;
import com.google.gson.Gson;

import java.util.Map;

/**
 * Created by Amin on 10/4/14.
 */
public class DecodeMessage extends BaseRichBolt {

    private OutputCollector collector;

    private FrameData frame;
    private Gson gson;

    @Override
    public void prepare(Map stormConf, TopologyContext context, OutputCollector collector) {
        this.collector = collector;
        this.gson = new Gson();
    }

    @Override
    public void execute(Tuple input) {
        Fields values = input.getFields();

        byte[] array = (byte[]) input.getValueByField(values.get(0));
        String json = new String(array);

        frame =  gson.fromJson(json, FrameData.class);

        this.collector.emit(new Values(frame.UserID , frame.Joints , frame.Time));

    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {
        declarer.declare(new Fields("UserID" , "Joints" , "Time"));
    }
}
