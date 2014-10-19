package hpcrc.aut.physicalinfodetection;

import java.util.List;

/**
 * Created by Amin on 10/4/14.
 */
public class FrameData {
    public String UserID;
    public List<Joint> Joints;
    public String Time;

    public FrameData(String _userID)
    {
        UserID = _userID;
    }

    public Transform getTransformByJoint(String JointName)
    {
        for (int i = 0 ; i < Joints.size() ; i++)
        {
            if (Joints.get(i).jointName.equals(JointName))
            {
                return Joints.get(i).transform;
            }
        }
        return null;
    }


}
