package de.applicatum.shoprouter.model.Shops;


import com.couchbase.lite.CouchbaseLiteException;
import com.couchbase.lite.Document;
import com.couchbase.lite.UnsavedRevision;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import de.applicatum.shoprouter.Application;
import de.applicatum.shoprouter.utils.AppLog;

public class Shop{

    public static final String TAG = "Shop";

    private String id;
    private String name;
    private double latitude;
    private double longitude;
    private String address;
    private int rating;
    private String creator;

    private int width;
    private int height;
    private ShopRoomObject[][] shopRoomObjects;
    private HashMap<String, String> discussion;

    public Shop(String name, double latitude, double longitude, String address) {
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
        this.address = address;
        this.discussion = new HashMap<>();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public String getCreator() {
        return creator;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }

    public int getWidth() {
        return width;
    }

    public void setWidthHeight(int width, int height) {
        this.width = width;
        this.height = height;
        shopRoomObjects = new ShopRoomObject[height][width];
    }

    public int getHeight() {
        return height;
    }

    public ShopRoomObject[][] getShopRoomObjects() {
        return shopRoomObjects;
    }

    public void addMessage(String author, String message){
        discussion.put(author, message);
    }

    public HashMap<String, String> getDiscussion(){
        return discussion;
    }

    public void addShopRoomObject(ShopRoomObject shopRoomObject){
        //shopRoomObjects.add(shopRoomObject);
        AppLog.d(TAG, "addShopRoomObject", "shopRoomObject argument: "+shopRoomObject.getId());
        shopRoomObjects[shopRoomObject.getY()][shopRoomObject.getX()] = shopRoomObject;
    }

    public void removeShopRoomObject(ShopRoomObject shopRoomObject){
        //shopRoomObjects.remove(shopRoomObject);
        shopRoomObjects[shopRoomObject.getY()][shopRoomObject.getX()] = null;
    }

    public ShopRoomObject removeShopRoomObjectAtIndex(int x, int y){
        //shopRoomObjects.remove(shopRoomObject);
        ShopRoomObject shopRoomObject = shopRoomObjects[y][x];
        shopRoomObjects[y][x] = null;
        return shopRoomObject;
    }

    public void save(Application application){

        final Map<String, Object> properties = new HashMap<>();

        properties.put("type", "shop");
        properties.put("name", getName());
        properties.put("address", getAddress());
        properties.put("latitude", getLatitude());
        properties.put("longitude", getLongitude());
        properties.put("rating", getRating());
        properties.put("creator", getCreator());
        properties.put("width", getWidth());
        properties.put("height", getHeight());

        ArrayList<String> children = new ArrayList<>();
        if(shopRoomObjects != null){
            for(int i=0; i<shopRoomObjects.length; i++){
                for(int j=0; j<shopRoomObjects[i].length; j++){
                    if(shopRoomObjects[i][j]!=null){
                        AppLog.d(TAG, "save", "object: "+shopRoomObjects[i][j].getId());
                        children.add(shopRoomObjects[i][j].getId());
                    }
                }
            }
        }
        AppLog.d(TAG, "save", "shop name: "+getName());
        AppLog.d(TAG, "save", "size of children: "+children.size());
        properties.put("children", children);
        properties.put("discussion", discussion);
        Document document;
        if(getId()==null || getId().equals("")){
            AppLog.d(TAG, "save", "getId()==null");
            document = application.getGlobalDatabase().createDocument();
            setId(document.getId());
        }else{
            document = application.getGlobalDatabase().getExistingDocument(getId());

            if(document == null){
                AppLog.d(TAG, "save", "document == null");
                document = application.getGlobalDatabase().getDocument(getId());

                try {
                    document.putProperties(properties);
                } catch (CouchbaseLiteException e) {
                    e.printStackTrace();

                }
            }else{
                AppLog.d(TAG, "save", "document exists");

                try {
                    document.update(new Document.DocumentUpdater() {
                        @Override
                        public boolean update(UnsavedRevision newRevision) {

                            Map<String, Object> oldProperties = newRevision.getUserProperties();
                            oldProperties.putAll(properties);
                            newRevision.setUserProperties(oldProperties);
                            return true;
                        }
                    });
                } catch (CouchbaseLiteException e) {
                    e.printStackTrace();

                }
            }
        }

    }

//    public <T> List<T> twoDArrayToList(T[][] twoDArray) {
//        List<T> list = new ArrayList<T>();
//        for (T[] array : twoDArray) {
//            list.addAll(Arrays.asList(array));
//        }
//        return list;
//    }
}
