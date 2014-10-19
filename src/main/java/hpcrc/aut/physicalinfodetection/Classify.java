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
public class Classify extends BaseRichBolt {
    private OutputCollector collector;
    private Gson gson;
    private StatisticsTools statTools;

    @Override
    public void prepare(Map stormConf, TopologyContext context, OutputCollector collector) {
        this.collector = collector;
        this.gson = new Gson();
    }

    @Override
    public void execute(Tuple input) {
        // input = { "UserID" : "someString" , "LoopFound" : false or true }
        // time is frame number so don't worry about it :D because kinect will always capture data with 30fps

        String UserID = input.getStringByField("UserID");
        String SimilarUserID = UserID;

        boolean loopFound = input.getBooleanByField("LoopFound");

        boolean isClassified = false;

        BasicDBObject whereQuery = new BasicDBObject();
        whereQuery.put("UserID", UserID);

        BasicDBObject removeIdProjection = new BasicDBObject("_id", 0);

        DBCursor cursor = DataBaseConnector._Instance.queryDataBase(whereQuery , removeIdProjection , DBCollectionType.TempUsersData);

        while(cursor.hasNext()) {

            DBObject object = cursor.next();
            if(object.get("isClassified") != null) {
                isClassified = object.get("isClassified").toString().equals("true");
            }
            break;
        }

        if (loopFound && !isClassified)
        {

            // get all users from users table
            whereQuery = new BasicDBObject();
            removeIdProjection = new BasicDBObject("_id", 0);

            cursor = DataBaseConnector._Instance.queryDataBase(whereQuery  , removeIdProjection , DBCollectionType.Users);

            List<User> Users = new ArrayList<User>();

            while(cursor.hasNext()) {
                String userString = cursor.next().toString();
                User user = gson.fromJson(userString, User.class);
                Users.add(user);
            }
            //System.out.println("All users added to list");

            // get temp data for stream user id
            whereQuery = new BasicDBObject();
            whereQuery.put("UserID", UserID);
            removeIdProjection = new BasicDBObject("_id", 0);

            cursor = DataBaseConnector._Instance.queryDataBase(whereQuery , removeIdProjection , DBCollectionType.TemporaryData);

            cursor.sort(new BasicDBObject("Time" , 1));

            List<FrameData> newUserFrames = new ArrayList<FrameData>();



            while(cursor.hasNext()) {
                FrameData fd = gson.fromJson(cursor.next().toString(), FrameData.class);
                newUserFrames.add(fd);
            }
            //System.out.println("All newUser frames added to list");


            for (int i = 0; i < Users.size(); i++) {
                if (isClassified) {
                    break;
                }
                System.out.println("****************** Comparing " + UserID + " with " + Users.get(i).UserID + " ****************");

                //get user data from mainData
                whereQuery = new BasicDBObject();
                whereQuery.put("UserID", Users.get(i).UserID);

                removeIdProjection = new BasicDBObject("_id", 0);

                cursor = DataBaseConnector._Instance.queryDataBase(whereQuery, removeIdProjection , DBCollectionType.MainData);

                cursor.sort(new BasicDBObject("Time", 1));

                List<FrameData> Frames = new ArrayList<FrameData>();

                // add frames to Frames array
                while (cursor.hasNext()) {
                    FrameData fd = gson.fromJson(cursor.next().toString(), FrameData.class);
                    Frames.add(fd);
                }

                //System.out.println("All newUser frames added to list");

                // double the Frames array for better classification
                int FramesSize = Frames.size();
                for (int j = 0; j < FramesSize; j++) {
                    Frames.add(Frames.get(j));
                }
                //System.out.println("frames doubled for better classification");

                // compare with newUser
                //first joint
                double[] firstJointComparisonDataY = new double[newUserFrames.size()];
                double[] firstJointComparisonDataZ = new double[newUserFrames.size()];
                //second joint
                double[] secondJointComparisonDataY = new double[newUserFrames.size()];
                double[] secondointComparisonDataZ = new double[newUserFrames.size()];

                //create StatisticalTools
                statTools = new StatisticsTools();

                for (int j = 0; j < Frames.size() - newUserFrames.size(); j++) {
                    if (isClassified) {
                        break;
                    }
                    for (int k = 0; k < newUserFrames.size(); k++) {
                        //first joint
                        firstJointComparisonDataY[k] = Transform.GetTransformsDifference(newUserFrames.get(k).getTransformByJoint(App.FIRST_JOINT)
                                , Frames.get(j).getTransformByJoint(App.FIRST_JOINT))[0]; // x axis
                        firstJointComparisonDataZ[k] = Transform.GetTransformsDifference(newUserFrames.get(k).getTransformByJoint(App.FIRST_JOINT)
                                , Frames.get(j).getTransformByJoint(App.FIRST_JOINT))[1]; // y axis
                        //second joint
                        secondJointComparisonDataY[k] = Transform.GetTransformsDifference(newUserFrames.get(k).getTransformByJoint(App.SECOND_JOINT)
                                , Frames.get(j).getTransformByJoint(App.SECOND_JOINT))[0]; // x axis
                        secondointComparisonDataZ[k] = Transform.GetTransformsDifference(newUserFrames.get(k).getTransformByJoint(App.SECOND_JOINT)
                                , Frames.get(j).getTransformByJoint(App.SECOND_JOINT))[1]; // y axis
                    }
                    double[] comparisonData =
                            {
                                    statTools.getVariance(firstJointComparisonDataY) ,
                                    statTools.getVariance(firstJointComparisonDataZ) ,
                                    statTools.getVariance(secondJointComparisonDataY) ,
                                    statTools.getVariance(secondointComparisonDataZ)
                            };
                    System.out.println("Classification diff :" + statTools.getMean(comparisonData));
                    if (statTools.getMean(comparisonData) < App.CLASSIFICATION_THRESHOLD)
                    {
                        // found similar person :D
                        isClassified = true;
                        System.out.println("*************** is Classified :"+ isClassified +", for user : "+ UserID +" ********************");
                        BasicDBObject object = new BasicDBObject();
                        object.put("UserID" , UserID);
                        object.put("IsClassified" , isClassified);
                        object.put("ClassifiedWithUserID" , Users.get(i).UserID);
                        DataBaseConnector._Instance.SaveToDB(object, DBCollectionType.Logs);

                        whereQuery = new BasicDBObject();
                        object.put("UserID" , UserID);

                        object = new BasicDBObject();
                        object.append("$set", new BasicDBObject().append("isClassified", isClassified));
                        object.append("$set", new BasicDBObject().append("similarUserID", Users.get(i).UserID));
                        DataBaseConnector._Instance.Update(whereQuery , object , DBCollectionType.TempUsersData);

                        SimilarUserID = Users.get(i).UserID;

                        break;
                    }
                    else {
                        //System.out.println("mean is grater than CLASSIFICATION_THRESHOLD");
                    }

                    //System.out.println(j);
                }

            }

        }
        collector.emit(new Values(isClassified , UserID , SimilarUserID));
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {
        declarer.declare(new Fields("isClassified" , "UserID" , "SimilarUserID"));
    }
}
