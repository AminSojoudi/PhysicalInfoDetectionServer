package hpcrc.aut.physicalinfodetection;

import backtype.storm.task.OutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.IRichBolt;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseRichBolt;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Tuple;

import java.util.List;
import java.util.Map;

/**
 * Created by Amin on 9/30/14.
 */
public class ReportBolt extends BaseRichBolt {
    private OutputCollector collector;

    @Override
    public void prepare(Map stormConf, TopologyContext context, OutputCollector collector) {
        this.collector = collector;
    }

    @Override
    public void execute(Tuple input) {

        boolean isClassified = input.getBooleanByField("isClassified");
        String UserID = input.getStringByField("UserID");

        if (isClassified)
        {
            String query = UserID;
            String PhysicalDataJson = DataBaseConnector._Instance.queryDataBase(query);
            //TODO Send Physical Data
        }
        else
        {
            //TODO Send not found
        }



    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {
        // this is final bolt
    }
}
