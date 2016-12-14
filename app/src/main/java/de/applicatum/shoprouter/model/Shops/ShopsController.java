package de.applicatum.shoprouter.model.Shops;

import com.couchbase.lite.CouchbaseLiteException;
import com.couchbase.lite.Document;
import com.couchbase.lite.Query;
import com.couchbase.lite.QueryEnumerator;
import com.couchbase.lite.QueryRow;

import java.util.ArrayList;
import java.util.Iterator;

import de.applicatum.shoprouter.Application;
import de.applicatum.shoprouter.model.Products.ProductsController;
import de.applicatum.shoprouter.utils.AppLog;

public class ShopsController {

    public static final String TAG = "ShopsController";

    private static ShopsController controller;
    private ArrayList<Shop> shops;
    private ArrayList<ShopRoomObject> shopRoomObjects;

    private ShopsController() {
        shops = new ArrayList<>();
        shopRoomObjects = new ArrayList<>();
    }

    public static ShopsController getInstance(){
        if(controller == null) controller = new ShopsController();
        return controller;
    }

    public ArrayList<Shop> getShops(){
        return shops;
    }

    public void addShop(Shop shop){
        shops.add(shop);
    }

    public void removeShop(Shop shop){
        shops.remove(shop);
    }

    public Shop getShop(String id){
        for(Shop shop : shops){
            if(shop.getId().equals(id)) return shop;
        }
        return null;
    }

    public Shop getShopAtPosition(double lat, double lng){
        for(Shop shop : shops){
            if(shop.getLatitude() == lat && shop.getLongitude() == lng) return shop;
        }
        return null;
    }

    public void loadShops(Application application){
        Query queryShops = application.getShopsView().createQuery();

        try {
            QueryEnumerator resultShops = queryShops.run();
            for (Iterator<QueryRow> it = resultShops; it.hasNext(); ) {
                QueryRow row = it.next();
                //AppLog.d(TAG, "loadShoppingLists", "row.getKey(): "+row.getKey()+", row.getValue(): "+row.getValue());
                //Map<String, Object> shoppingListMap = (HashMap<String, Object>) row.getValue();
                Document document = row.getDocument();
                shops.add(makeShopFromDocument(document, application));
                //AppLog.d(TAG, "loadShoppingLists", "document name: "+document.getProperty("name")+", document type: "+document.getProperty("type"));
            }
        } catch (CouchbaseLiteException e) {
            e.printStackTrace();
        }

    }

    public void loadShopRoomObjects(Application application){
        Query queryShopObjects = application.getShopRoomObjectsView().createQuery();

        try {
            QueryEnumerator resultShopObjects = queryShopObjects.run();
            AppLog.d(TAG, "loadShopRoomObjects", "stored objects: "+resultShopObjects.getCount());
            for (Iterator<QueryRow> it = resultShopObjects; it.hasNext(); ) {
                QueryRow row = it.next();
                //AppLog.d(TAG, "loadShoppingLists", "row.getKey(): "+row.getKey()+", row.getValue(): "+row.getValue());
                //Map<String, Object> shoppingListMap = (HashMap<String, Object>) row.getValue();
                Document document = row.getDocument();
                shopRoomObjects.add(makeShopObjectFromDocument(document));
                //AppLog.d(TAG, "loadShoppingLists", "document name: "+document.getProperty("name")+", document type: "+document.getProperty("type"));
            }
        } catch (CouchbaseLiteException e) {
            e.printStackTrace();
        }

    }

    private Shop makeShopFromDocument(Document document, Application application){
        String name = (String)document.getProperty("name");
        double latitude = (double)document.getProperty("latitude");
        double longitude = (double)document.getProperty("longitude");
        int width = (int)document.getProperty("width");
        int height = (int) document.getProperty("height");
        int rating = (int) document.getProperty("rating");
        int ratingCount = (int) document.getProperty("ratingCount");
        String address = (String)document.getProperty("address");
        Shop shop = new Shop(name, latitude, longitude, address);
        shop.setId(document.getId());
        shop.setWidthHeight(width, height);
        shop.setRating(rating);
        shop.setRatingCount(ratingCount);
        ArrayList<String> children = (ArrayList<String>)document.getProperty("children");
        AppLog.d(TAG, "makeShopFromDocument", "shop name: "+name);
        AppLog.d(TAG, "makeShopFromDocument", "size of children: "+children.size());

        for(ShopRoomObject shopRoomObject : shopRoomObjects){
            //AppLog.d(TAG, "makeShopFromDocument", "shopRoomObject: "+shopRoomObject.getId());
            if (shopRoomObject.getId()!=null) {
                for(String child : children){
                    //AppLog.d(TAG, "makeShopFromDocument", "child: "+child);
                    if(child != null && child.equals(shopRoomObject.getId())){
                        //AppLog.d(TAG, "makeShopFromDocument", "found child");
                        shop.addShopRoomObject(shopRoomObject);
                    }
                }
            } else {
                AppLog.d(TAG, "makeShopFromDocument", "shopRoomObject.getId() is null");
            }
//            if(children.contains(shopRoomObject.getId())){
//
//            }
        }
//        Query queryShopObjects = application.getShopRoomObjectsView().createQuery();
//
//        try {
//            QueryEnumerator resultShopObjects = queryShopObjects.run();
//            AppLog.d(TAG, "makeShopFromDocument", "stored objects: "+resultShopObjects.getCount());
//            for (Iterator<QueryRow> it = resultShopObjects; it.hasNext(); ) {
//                QueryRow row = it.next();
//                //AppLog.d(TAG, "loadShoppingLists", "row.getKey(): "+row.getKey()+", row.getValue(): "+row.getValue());
//                //Map<String, Object> shoppingListMap = (HashMap<String, Object>) row.getValue();
//                Document objectDocument = row.getDocument();
//                for(String childId: children){
//                    if(document.getId().equals(childId)){
//
//                    }
//                }
//                //AppLog.d(TAG, "loadShoppingLists", "document name: "+document.getProperty("name")+", document type: "+document.getProperty("type"));
//            }
//        } catch (CouchbaseLiteException e) {
//            e.printStackTrace();
//        }


        return shop;
    }

    private ShopRoomObject makeShopObjectFromDocument(Document document){
        AppLog.d(TAG, "makeShopObjectFromDocument", "child class: " +document.getProperty("type"));
        switch ((String)document.getProperty("type")){
            case "enterexit":
                EnterExit enterExit = new EnterExit((int)document.getProperty("x"), (int)document.getProperty("y"));
                enterExit.setId(document.getId());
                return enterExit;
            case "cashdesk":
                CashDesk cashDesk = new CashDesk((int)document.getProperty("x"), (int)document.getProperty("y"));
                cashDesk.setId(document.getId());
                return cashDesk;
            case "shelf":
                Shelf shelf = new Shelf((int)document.getProperty("x"), (int)document.getProperty("y"));
                shelf.setId(document.getId());
                shelf.setProductTopLeft(ProductsController.getInstance().getProductList().findProductGroupWithName((String)document.getProperty("productTopLeft")));
                shelf.setProductTopRight(ProductsController.getInstance().getProductList().findProductGroupWithName((String)document.getProperty("productTopRight")));
                shelf.setProductDownLeft(ProductsController.getInstance().getProductList().findProductGroupWithName((String)document.getProperty("productDownLeft")));
                shelf.setProductDownRight(ProductsController.getInstance().getProductList().findProductGroupWithName((String)document.getProperty("productDownRight")));
                return shelf;
        }
        return null;
    }
}
