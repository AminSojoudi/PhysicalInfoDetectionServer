package hpcrc.aut.physicalinfodetection;

/**
 * Created by Amin on 10/4/14.
 */
public class Transform {
    public float X,Y,Z;

    public Transform(float _x , float _y , float _z)
    {
        X = _x;
        Y = _y;
        Z = _z;
    }

    public static boolean IsTransformsSame(Transform t1 , Transform t2)
    {
        boolean[] xyz = { false , false , false };

        if (App.XYZ_AXIS[0])
        {
            if (Math.abs(t1.X - t2.X) < App.POSITION_COMPARISON_THRESHOLD)
            {
                //these points are similar in X axis
                xyz[0] = true;
            }
        }
        if (App.XYZ_AXIS[1])
        {
            if (Math.abs(t1.Y - t2.Y) < App.POSITION_COMPARISON_THRESHOLD)
            {
                //these points are similar in Y axis
                xyz[1] = true;
            }
        }
        if (App.XYZ_AXIS[2])
        {
            if (Math.abs(t1.Z - t2.Z) < App.POSITION_COMPARISON_THRESHOLD)
            {
                //these points are similar in Z axis
                xyz[2] = true;
            }
        }

        if (xyz[0] == App.XYZ_AXIS[0] &&
                xyz[1] == App.XYZ_AXIS[1] &&
                xyz[2] == App.XYZ_AXIS[2])
        {
            // these points are similar based on the important angles in App.java
            return true;
        }
        //else

        return false;
    }

    public static double[] GetTransformsDifference(Transform t1 , Transform t2)
    {
        double[] XYZ = new double[3];
        XYZ[0] = Math.abs(t1.X - t2.X);
        XYZ[1] = Math.abs(t1.Y - t2.Y);
        XYZ[2] = Math.abs(t1.Z - t2.Z);
        return XYZ;
    }
}
