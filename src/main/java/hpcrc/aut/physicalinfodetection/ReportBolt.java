package hpcrc.aut.physicalinfodetection;

import backtype.storm.task.OutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.IRichBolt;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseRichBolt;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Tuple;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;

import java.util.ArrayList;
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
        String SimilarUserID = input.getStringByField("SimilarUserID");

        if (isClassified)
        {


            BasicDBObject whereQuery = new BasicDBObject();
            whereQuery.put("UserID", SimilarUserID);

            BasicDBObject removeIdProjection = new BasicDBObject("_id", 0);

            DBCursor cursor = DataBaseConnector._Instance.queryDataBase(whereQuery , removeIdProjection , DBCollectionType.Users);



            while(cursor.hasNext()) {
                System.out.println("New User Physical data found! . userID  " + UserID + " is similar to :" + cursor.next());
            }


            // remove temp data for current user
            BasicDBObject deleteQuery = new BasicDBObject();
            deleteQuery.put("UserID" , UserID);
            DataBaseConnector._Instance.deleteData(deleteQuery , DBCollectionType.TemporaryData);

        }
        else
        {
            // do nothing
        }
        //System.out.println("Report Bolt");


    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {
        // this is final bolt
    }
}
