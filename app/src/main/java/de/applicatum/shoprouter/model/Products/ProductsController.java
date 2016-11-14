package de.applicatum.shoprouter.model.Products;


import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;

import de.applicatum.shoprouter.Application;
import de.applicatum.shoprouter.utils.AppLog;

import com.couchbase.lite.CouchbaseLiteException;
import com.couchbase.lite.Document;
import com.couchbase.lite.Query;
import com.couchbase.lite.QueryEnumerator;
import com.couchbase.lite.QueryRow;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ProductsController {

    public static final String TAG = "ProductsController";

    private static ProductsController controller;
    private ArrayList<ShoppingList> shoppingLists;
    private ProductList productList;

    private ProductsController() {
        shoppingLists = new ArrayList<>();
    }

    public static ProductsController getInstance(){
        if(controller == null) controller = new ProductsController();
        return controller;
    }

    public boolean checkShoppingListName(String name){
        for (ShoppingList shoppingList : shoppingLists){
            if(shoppingList.getName().equals(name)) return false;
        }
        return true;
    }

    public String[] getShoppingListNamesArray(){
        String[] names = new String[shoppingLists.size()];
        for(int i = 0; i<shoppingLists.size(); i++){
            names[i] = shoppingLists.get(i).getName();
        }
        return names;
    }

    public ArrayList<ShoppingList> getShoppingLists() {
        return shoppingLists;
    }

    public ProductList getProductList() {
        return productList;
    }

    public void addShoppingList(ShoppingList shoppingList){
        shoppingLists.add(shoppingList);
        //DBUtils.
    }

    public void removeShoppingList(ShoppingList shoppingList){
        shoppingLists.remove(shoppingList);
    }

    public ShoppingList getShoppingListAtIndex(int index){
        return shoppingLists.get(index);
    }

    public ShoppingList findShoppingList(String id){

        for (ShoppingList shoppingList : shoppingLists){
            if(shoppingList.getId().equals(id)) return shoppingList;
        }
        return null;
    }

    public void loadShoppingLists(Application application){
        Query queryShoppingLists = application.getShoppingListsView().createQuery();
        Query queryItems = application.getShoppingListItemsView().createQuery();
        ArrayList<ShoppingListItem> items = new ArrayList<>();
        try {
            QueryEnumerator resultItems = queryItems.run();
            for (Iterator<QueryRow> it = resultItems; it.hasNext(); ) {
                QueryRow row = it.next();
                Document document = row.getDocument();
                items.add(makeShoppingListItemFromDocument(document));
            }
        } catch (CouchbaseLiteException e) {
            e.printStackTrace();
        }
        shoppingLists.clear();
        try {
            QueryEnumerator resultShoppingLists = queryShoppingLists.run();
            for (Iterator<QueryRow> it = resultShoppingLists; it.hasNext(); ) {
                QueryRow row = it.next();
                //AppLog.d(TAG, "loadShoppingLists", "row.getKey(): "+row.getKey()+", row.getValue(): "+row.getValue());
                //Map<String, Object> shoppingListMap = (HashMap<String, Object>) row.getValue();
                Document document = row.getDocument();
                shoppingLists.add(makeShoppingListFromMap(document, items));
                //AppLog.d(TAG, "loadShoppingLists", "document name: "+document.getProperty("name")+", document type: "+document.getProperty("type"));
            }
        } catch (CouchbaseLiteException e) {
            e.printStackTrace();
        }

    }

    public void loadProductList(Application application){
        String json = null;
        productList = new ProductList();
        try {
            InputStream is = application.getAssets().open("productlist.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, "UTF-8");
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        try {
            JSONObject obj = new JSONObject(json);
            JSONArray productListChildrenArray = obj.getJSONArray("children");

            for (int i = 0; i < productListChildrenArray.length(); i++) {
                JSONObject productGroupObject = productListChildrenArray.getJSONObject(i);
                ProductGroup productGroup = new ProductGroup(productGroupObject.getString("name"));
                JSONArray productGroupChildrenArray = productGroupObject.getJSONArray("children");
                AppLog.d(TAG, "loadProductList", "product group: "+productGroup.getName());
                for (int j = 0; j < productGroupChildrenArray.length(); j++) {
                    JSONObject productObject = productGroupChildrenArray.getJSONObject(j);
                    Product product = new Product(productObject.getString("name"));
                    AppLog.d(TAG, "loadProductList", "product: "+product.getName());
                    productGroup.addProductObject(product);
                }
                productList.addProductGroup(productGroup);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private ShoppingList makeShoppingListFromMap(Document document, ArrayList<ShoppingListItem> items){
        ShoppingList shoppingList = new ShoppingList((String)document.getProperty("name"));
        shoppingList.setId(document.getId());
        ArrayList<String> children = (ArrayList<String>)document.getProperty("children");

        for(String childId: children){
            for(ShoppingListItem item : items){
                if(item.getId().equals(childId)) shoppingList.addItem(item);
            }
        }
        return shoppingList;
    }

    private ShoppingListItem makeShoppingListItemFromDocument(Document document){
        ShoppingListItem item = new ShoppingListItem(productList.findProductWithName((String)document.getProperty("product")));
        item.setId(document.getId());
        return item;
    }

    public void deleteShoppingList(Application application, ShoppingList shoppingList){

        AppLog.d(TAG, "deleteShoppingList", "try to delete: "+shoppingList.getName());
        try {
            application.getUserDatabase().getDocument(shoppingList.getId()).delete();
            shoppingLists.remove(shoppingList);
        } catch (CouchbaseLiteException e) {
            e.printStackTrace();
        }
        AppLog.d(TAG, "deleteShoppingList", "size of list: "+shoppingLists.size());
    }
}
