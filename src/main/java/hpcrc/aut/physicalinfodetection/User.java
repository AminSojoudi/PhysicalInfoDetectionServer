package hpcrc.aut.physicalinfodetection;

import com.google.gson.Gson;

import java.io.Serializable;

/**
 * Created by Amin on 10/7/14.
 */
public class User implements Serializable{

    public String UserID;
    public PhysicalInfo PhysiclInfo;


    public User(String _userID , PhysicalInfo _physicalinfo)
    {
        this.UserID = _userID;
        this.PhysiclInfo = _physicalinfo;
    }

    @Override
    public String toString() {
        Gson gson = new Gson();
        return gson.toJson(this);
    }
}
