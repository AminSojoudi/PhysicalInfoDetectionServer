package hpcrc.aut.physicalinfodetection;

/**
 * Created by Amin on 10/7/14.
 */
public class PhysicalInfo {
    public int Hieght;
    public float Weight;
    public String Sex;
    public int Age;

    public PhysicalInfo(int _height , float _weight , String _sex , int _age)
    {
        this.Hieght = _height;
        this.Weight = _weight;
        this.Sex = _sex;
        this.Age = _age;
    }
}
