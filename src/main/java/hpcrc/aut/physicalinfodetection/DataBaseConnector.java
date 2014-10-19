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
    private static final String USERS_COLLECTION_NAME = "Users";
    private static final String TEMP_USERS_DATA_COLLECTION_NAME = "tempUsersData";
    private static final String LOGS_COLLECTION_NAME = "logs";
    private static final int DATA_BASE_PORT = 27017;

    private MongoClient mongo;
    private DB dataBase;
    private DBCollection OriginalTable;
    private DBCollection TempTable;
    private DBCollection Users;
    private DBCollection tempUsersData;
    private DBCollection logsTable;


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
            Users = dataBase.getCollection(USERS_COLLECTION_NAME);
            tempUsersData = dataBase.getCollection(TEMP_USERS_DATA_COLLECTION_NAME);
            logsTable = dataBase.getCollection(LOGS_COLLECTION_NAME);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }

    public void SaveToDB(String Json , DBCollectionType type)
    {
        try {
            DBObject dbObject = (DBObject) JSON
                    .parse(Json);
            SaveToDB(dbObject , type);
        }
        catch (Exception ex)
        {
            System.out.println(ex.toString());
        }
    }

    public void SaveToDB(DBObject dbObject , DBCollectionType type)
    {
        try {
            DBCollection collection = getCollectionFromCollectionType(type);
            collection.insert(dbObject);
        }
        catch (Exception ex)
        {
            System.out.println(ex.toString());
        }
    }

    public void Update(DBObject searchQuery , DBObject updateQuery , DBCollectionType type)
    {
        DBCollection collection = getCollectionFromCollectionType(type);

        /// IMPORTANT : Example Usage of Update
        /// USE $set
//        BasicDBObject newDocument = new BasicDBObject();
//        newDocument.append("$set", new BasicDBObject().append("clients", 110));
//
//        BasicDBObject searchQuery = new BasicDBObject().append("hosting", "hostB");

        collection.update(searchQuery, updateQuery);

    }



    public DBCursor queryDataBase(BasicDBObject query , BasicDBObject fields , DBCollectionType type)
    {
        DBCursor cursor = null;
        DBCollection collection = getCollectionFromCollectionType(type);
        cursor = collection.find(query , fields);

        return cursor;
    }

    public void deleteData(DBObject query , DBCollectionType type)
    {
        DBCollection collection = getCollectionFromCollectionType(type);
        collection.remove(query);
    }

    private DBCollection getCollectionFromCollectionType(DBCollectionType type) {
        if (type == DBCollectionType.TemporaryData) {
            return TempTable;
        } else if (type == DBCollectionType.MainData) {
            return OriginalTable;
        } else if (type == DBCollectionType.Users) {
            return Users;
        } else if (type == DBCollectionType.TempUsersData) {
            return tempUsersData;
        } else if (type == DBCollectionType.Logs) {
            return logsTable;
        }
        return null;
    }

}

