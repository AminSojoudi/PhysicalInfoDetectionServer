package hpcrc.aut.physicalinfodetection;

import backtype.storm.task.OutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseRichBolt;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Tuple;
import backtype.storm.tuple.Values;
import com.google.gson.Gson;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by Amin on 10/4/14.
 */
public class FindLoop extends BaseRichBolt {

    private OutputCollector collector;
    private Gson gson;

    @Override
    public void prepare(Map stormConf, TopologyContext context, OutputCollector collector) {
        this.collector = collector;
        gson = new Gson();
    }

    @Override
    public void execute(Tuple input) {
        // input = { "UserID" : "someString" }

        String UserID = input.getStringByField("UserID");
        boolean loopFound = false;
        boolean isClassified = false;

        BasicDBObject whereQuery = new BasicDBObject();
        whereQuery.put("UserID", UserID);

        BasicDBObject removeIdProjection = new BasicDBObject("_id", 0);

        DBCursor cursor = DataBaseConnector._Instance.queryDataBase(whereQuery , removeIdProjection , DBCollectionType.TempUsersData);

        while(cursor.hasNext()) {

            DBObject object = cursor.next();
            loopFound = object.get("LoopFound").toString().equals("true");
            if(object.get("isClassified") != null) {
                isClassified = object.get("isClassified").toString().equals("true");
            }
            break;
        }

        if (!loopFound && !isClassified) {
            System.out.println("Finding Loop for UserID : " + UserID);

            whereQuery = new BasicDBObject();
            whereQuery.put("UserID", UserID);

            removeIdProjection = new BasicDBObject("_id", 0);

            cursor = DataBaseConnector._Instance.queryDataBase(whereQuery, removeIdProjection, DBCollectionType.TemporaryData);

            cursor.sort(new BasicDBObject("Time", 1));

            List<FrameData> Frames = new ArrayList<FrameData>();


            while (cursor.hasNext()) {
                String frameJSON = cursor.next().toString();
                //System.out.println("TempFrames for user " + UserID + " :" + frameJSON);
                FrameData fd = gson.fromJson(frameJSON, FrameData.class);
                Frames.add(fd);
            }

            //check if user is walking or not
            StatisticsTools tools = new StatisticsTools();

            if (!tools.checkIfJointIsIdle(Frames, App.FIRST_JOINT)) {
                if (!tools.checkIfJointIsIdle(Frames, App.SECOND_JOINT)) {
                    List<FrameData> LoopList = new ArrayList<FrameData>();

                    for (int i = 0; i < Frames.size(); i++) {
                        //add new frame
                        LoopList.add(Frames.get(i));
                        for (int j = 0; j < LoopList.size(); j++) {
                            if (Math.abs(i - j) >= App.MINIMUM_LOOP_FRAMES) {
                                if (Transform.IsTransformsSame(Frames.get(i).getTransformByJoint(App.FIRST_JOINT), LoopList.get(j).getTransformByJoint(App.FIRST_JOINT))
                                        || Transform.IsTransformsSame(Frames.get(i).getTransformByJoint(App.SECOND_JOINT), LoopList.get(j).getTransformByJoint(App.SECOND_JOINT))) {
                                    // Loop Found
                                    loopFound = true;
                                    System.out.println("*************** Loop found :" + loopFound + ", for user : " + UserID + " ********************");
                                    System.out.println("*************** minFrame number :" + j + "maxFrame Number : " + i + "********************");
                                    System.out.println("*************** minFrame time :" + LoopList.get(j).Time + "maxFrame time : " + Frames.get(i).Time + "********************");
                                    BasicDBObject userTempData = new BasicDBObject();
                                    userTempData.put("UserID", UserID);
                                    userTempData.put("LoopFound", loopFound);

                                    DataBaseConnector._Instance.SaveToDB(userTempData.toString(), DBCollectionType.TempUsersData);

                                    //delete extra data , frames > i and frames < j
                                    BasicDBObject query = new BasicDBObject();
                                    query.put("Time", new BasicDBObject("$gt", Frames.get(i).Time));
                                    DataBaseConnector._Instance.deleteData(query , DBCollectionType.TemporaryData);

                                    query = new BasicDBObject();
                                    query.put("Time", new BasicDBObject("$lt", LoopList.get(j).Time));
                                    DataBaseConnector._Instance.deleteData(query , DBCollectionType.TemporaryData);
                                } else {
                                    //System.out.println("transforms are not same");
                                }
                            } else {
                                //System.out.println("lower than minimum loop frames");
                            }
                        }
                    }
                } else {
                    System.out.println("joint is idle");
                }
            } else {
                System.out.println("joint is idle");
            }

        }
        collector.emit(new Values(UserID , loopFound));
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {
        declarer.declare(new Fields("UserID" , "LoopFound"));
    }
}
