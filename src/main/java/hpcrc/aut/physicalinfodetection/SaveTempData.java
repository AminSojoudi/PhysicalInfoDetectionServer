package hpcrc.aut.physicalinfodetection;

import backtype.storm.task.OutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseRichBolt;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Tuple;
import backtype.storm.tuple.Values;
import com.google.gson.Gson;

import java.util.List;
import java.util.Map;

/**
 * Created by Amin on 10/4/14.
 */
public class SaveTempData extends BaseRichBolt {

    private OutputCollector collector;
    private Gson gson;

    @Override
    public void prepare(Map stormConf, TopologyContext context, OutputCollector collector) {
        this.collector = collector;
        this.gson = new Gson();
    }

    @Override
    public void execute(Tuple input) {

        FrameData FD = new FrameData(input.getStringByField("UserID"));
        FD.Time = input.getStringByField("Time");
        FD.Joints = (List<Joint>) input.getValueByField("Joints");

        String json = gson.toJson(FD);

        DataBaseConnector._Instance.SaveToDB(json , DBCollectionType.TemporaryData);

        collector.emit(new Values(input.getStringByField("UserID")));
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {
        declarer.declare(new Fields("UserID"));
    }
}
