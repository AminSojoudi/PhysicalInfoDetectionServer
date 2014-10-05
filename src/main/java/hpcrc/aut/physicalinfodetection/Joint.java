package hpcrc.aut.physicalinfodetection;

/**
 * Created by Amin on 10/4/14.
 */
public class Joint {
    public Transform transform;
    public String jointName;

    public Joint(String _name , Transform _transform)
    {
        jointName = _name;
        transform = _transform;
    }
}
