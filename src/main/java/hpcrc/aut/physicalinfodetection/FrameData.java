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


}
