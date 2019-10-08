package dy.mongo;

import com.alibaba.fastjson.JSONObject;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.BulkWriteOptions;
import com.mongodb.client.model.InsertOneModel;
import com.mongodb.util.JSON;
import dy.log.AppLogger;
import dy.type.DeviceLog;
import dy.type.DeviceMetaData;
import lombok.Data;
import org.bson.Document;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@Component("mongoutil")
@Scope("prototype")
@Data
public class MongoUtil {

    @Value("${data.mongo.dataDbName}")
    private String dataDbName;

    @Value("${data.mongo.logDbName}")
    private String logDbName;

    @Value("${data.mongo.host}")
    private String mongoHost;

    @Value("${data.mongo.port}")
    private int mongoPort;

    @Autowired
    AppLogger appLogger;

    public void insertMongo(List<DeviceMetaData> listDeviceMetaData, List<DeviceLog> ListDeviceLog){
        MongoClient mongoClient = null;
        BulkWriteOptions bulkWriteOptions = new BulkWriteOptions();
        List<InsertOneModel<Document>> insertList = new ArrayList<InsertOneModel<Document>>();
        bulkWriteOptions.ordered(false);
        try {
            MongoClientOptions.Builder builder = new MongoClientOptions.Builder();
            builder.connectTimeout(3000);
            //builder.threadsAllowedToBlockForConnectionMultiplier(50);
            MongoClientOptions mongoClientOptions = builder.build();
            mongoClient = new MongoClient(new ServerAddress(mongoHost,mongoPort),mongoClientOptions);
            if(listDeviceMetaData.size()>0){
            Map<String,List<DeviceMetaData>> groupMetadataMap = listDeviceMetaData.stream().collect(Collectors.groupingBy(
                    data->new DateTime(data.getCreatedate()).toString("yyyyMM")
            ));
            //插入设备数据
            for(Map.Entry<String, List<DeviceMetaData>> entry:groupMetadataMap.entrySet()){
                MongoDatabase database = mongoClient.getDatabase(entry.getKey());
                MongoCollection<Document> deviceDataCollection = database.getCollection(dataDbName);
                for(DeviceMetaData deviceMetaData : entry.getValue()){
                    insertList.add(new InsertOneModel<Document>(Document.parse(JSONObject.toJSONString(deviceMetaData))));
                }
                deviceDataCollection.bulkWrite(insertList,bulkWriteOptions);
                insertList.clear();
            }
            }
            //插入设备日志
            if(ListDeviceLog.size() > 0){
                Map<String,List<DeviceLog>> groupLogMap = ListDeviceLog.stream().collect(Collectors.groupingBy(
                        data->new DateTime(data.getCreatedate()).toString("yyyyMM")
                ));
                for(Map.Entry<String, List<DeviceLog>> entry:groupLogMap.entrySet()){
                    MongoDatabase database = mongoClient.getDatabase(entry.getKey());
                    MongoCollection<Document> deviceDataCollection = database.getCollection(logDbName);
                    for(DeviceLog deviceLog : entry.getValue()){
                        insertList.add(new InsertOneModel<Document>(Document.parse(JSONObject.toJSONString(deviceLog))));
                    }
                    deviceDataCollection.bulkWrite(insertList,bulkWriteOptions);
                    insertList.clear();
                }
            }

        }catch (Exception ex){
            appLogger.getLogger().error(ex.getMessage());
            ex.printStackTrace();
        }finally {
            mongoClient.close();
        }
    }

}
