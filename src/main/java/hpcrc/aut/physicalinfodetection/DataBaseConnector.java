package hpcrc.aut.physicalinfodetection;

import com.mongodb.*;
import com.mongodb.util.JSON;

import java.net.UnknownHostException;

/**
 * Created by Amin on 9/30/14.
 */
public class DataBaseConnector {

    public static DataBaseConnector _Instance;

    private static final String DATA_BASE_NAME = "testdb";

    private static final String COLLECTION_NAME = "mainData";
    private static final String TEMP_COLLECTION_NAME = "tempData";
    private static final int DATA_BASE_PORT = 27017;

    public MongoClient mongo;
    public DB dataBase;
    public DBCollection OriginalTable;
    public DBCollection TempTable;


    public DataBaseConnector()
    {
        _Instance = this;
        ConnectToDB();
    }


    private void ConnectToDB()
    {
        try {
            mongo = new MongoClient( "localhost" , DATA_BASE_PORT );
            dataBase = mongo.getDB(DATA_BASE_NAME);
            OriginalTable = dataBase.getCollection(COLLECTION_NAME);
            TempTable = dataBase.getCollection(TEMP_COLLECTION_NAME);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }

    public void SaveToDB(String Json , DBCollectionType type)
    {
        try {
            DBObject dbObject = (DBObject) JSON
                    .parse(Json);
            if (type == DBCollectionType.MainData)
            {
                OriginalTable.insert(dbObject);
            }
            else if(type == DBCollectionType.TemporaryData)
            {
                TempTable.insert(dbObject);
            }

        }
        catch (Exception ex)
        {
            System.out.println(ex.toString());
        }
    }



    public String queryDataBase(String query)
    {
        DBObject doc = OriginalTable.findOne();
        System.out.println("From DataBase : " + doc);
        return doc.toString();
    }

}

