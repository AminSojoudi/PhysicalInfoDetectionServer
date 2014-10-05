package hpcrc.aut.physicalinfodetection;

import backtype.storm.task.OutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseRichBolt;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Tuple;
import backtype.storm.tuple.Values;

import java.util.List;
import java.util.Map;

/**
 * Created by Amin on 10/3/14.
 */
public class RemoveDistant extends BaseRichBolt {

    private OutputCollector collector;
    private List<Joint> Joints;
    private Transform baseTransform;

    @Override
    public void prepare(Map stormConf, TopologyContext context, OutputCollector collector) {
        this.collector = collector;
    }

    @Override
    public void execute(Tuple input) {
        Joints = (List<Joint>) input.getValueByField("Joints");

        // find hip center
        for (Joint joint : Joints)
        {
            if (joint.jointName == "HipCenter")
            {
                baseTransform = joint.transform;
            }
        }
        if (baseTransform == null)
        {
            // error
            System.out.println("ERROR : Base Transform Not Found!!!!!");
        }

        // transfer to hip center position
        for (Joint joint : Joints)
        {
            joint.transform.X = joint.transform.X - baseTransform.X;
            joint.transform.Y = joint.transform.Y - baseTransform.Y;
            joint.transform.Z = joint.transform.Z - baseTransform.Z;
        }

        this.collector.emit(new Values(input.getStringByField("UserID") , Joints , input.getStringByField("Time")));

    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {
        declarer.declare(new Fields("UserID" , "Joints" , "Time"));
    }
}
