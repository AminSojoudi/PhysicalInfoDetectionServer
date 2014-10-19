package hpcrc.aut.physicalinfodetection;

import java.util.Arrays;
import java.util.List;

/**
 * Created by Amin on 10/6/14.
 */
public class StatisticsTools {

    private double[] data;
    private double size;


    double getMean(double[] _data)
    {
        this.data = _data;
        size = _data.length;
        double sum = 0.0;
        for(double a : data)
            sum += a;
        return sum/size;
    }

    double getVariance(double[] _data)
    {
        this.data = _data;
        size = _data.length;
        double mean = getMean(data);
        double temp = 0;
        for(double a :data)
            temp += (mean-a)*(mean-a);
        return temp/size;
    }


    public double median(double[] _data)
    {
        this.data = _data;
        size = _data.length;
        double[] b = new double[data.length];
        System.arraycopy(data, 0, b, 0, b.length);
        Arrays.sort(b);

        if (data.length % 2 == 0)
        {
            return (b[(b.length / 2) - 1] + b[b.length / 2]) / 2.0;
        }
        else
        {
            return b[b.length / 2];
        }
    }

    public boolean checkIfJointIsIdle(List<FrameData> Frames , String jointName)
    {
        // xValues are not needed in my scenario

        double[] yValues = new double[Frames.size()];
        double[] zValues = new double[Frames.size()];

        for (int i = 0 ; i < Frames.size() ; i++)
        {
            yValues[i] = Frames.get(i).getTransformByJoint(jointName).Y;
            zValues[i] = Frames.get(i).getTransformByJoint(jointName).Z;
        }
        //System.out.println("Variance : " + Math.sqrt(getVariance(yValues)) + " , " + Math.sqrt(getVariance(zValues)));
        if (Math.sqrt(getVariance(yValues)) < App.IDLE_THRESHOLD && Math.sqrt(getVariance(zValues)) < App.IDLE_THRESHOLD)
        {
            return true;
        }
        else {
            return false;
        }
    }
}
