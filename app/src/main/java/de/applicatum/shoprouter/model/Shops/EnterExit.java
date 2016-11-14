package de.applicatum.shoprouter.model.Shops;

import com.couchbase.lite.CouchbaseLiteException;
import com.couchbase.lite.Document;

import java.util.HashMap;
import java.util.Map;

import de.applicatum.shoprouter.Application;
import de.applicatum.shoprouter.utils.AppLog;

public class EnterExit extends ShopRoomObject {

    public static final String TAG = "EnterExit";

    public EnterExit(int x, int y) {
        super(x, y);
    }

    @Override
    public void save(Application application){

        AppLog.d(TAG, "save", "store EnterExit");
        Map<String, Object> properties = new HashMap<>();

        properties.put("type", "enterexit");
        properties.put("x", getX());
        properties.put("y", getY());

        Document document;
        if(getId()==null || getId().equals("")){
            document = application.getGlobalDatabase().createDocument();
            AppLog.d(TAG, "save", "set new Id: "+document.getId());
            setId(document.getId());
        }else{
            document = application.getGlobalDatabase().getExistingDocument(getId());
            AppLog.d(TAG, "save", "use old Id");
            if(document == null){
                document = application.getGlobalDatabase().getDocument(getId());
            }
        }
        try {
            document.putProperties(properties);
        } catch (CouchbaseLiteException e) {
            e.printStackTrace();

        }
    }

    @Override
    public void delete(Application application) {
        try {
            Document document = application.getGlobalDatabase().getDocument(getId());
            document.delete();
        } catch (CouchbaseLiteException e) {
            e.printStackTrace();
        }
    }
}
