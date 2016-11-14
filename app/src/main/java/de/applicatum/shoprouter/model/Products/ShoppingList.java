package de.applicatum.shoprouter.model.Products;


import com.couchbase.lite.CouchbaseLiteException;
import com.couchbase.lite.Document;
import com.couchbase.lite.UnsavedRevision;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import de.applicatum.shoprouter.Application;
import de.applicatum.shoprouter.utils.AppLog;

public class ShoppingList {

    public static final String TAG = "ShoppingList";

    private String id;
    private String name;
    private ArrayList<ShoppingListItem> items;

    public ShoppingList(String name) {
        this.name = name;
        items = new ArrayList<>();
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

    public ArrayList<ShoppingListItem> getItems() {
        return items;
    }

    public void addItem(ShoppingListItem item){
        items.add(item);
    }

    public void removeItem(ShoppingListItem item){
        items.remove(item);
    }
    public void removeItemAtPosition(int position){
        items.remove(position);
    }

    public ShoppingListItem getItemAtIndex(int index){
        return items.get(index);
    }

    public void save(Application application){

        Map<String, Object> properties = new HashMap<>();

        properties.put("type", "shoppingList");
        properties.put("name", getName());

        ArrayList<String> children = new ArrayList<>();
        if(items != null){
            for(ShoppingListItem shoppingListItem : items){
                children.add(shoppingListItem.getId());
            }
        }
        properties.put("children", children);
        Document document;
        if(getId()==null || getId().equals("")){
            AppLog.d(TAG, "save", "new document");
            document = application.getUserDatabase().createDocument();
            this.id = document.getId();
            try {
                document.putProperties(properties);
            } catch (CouchbaseLiteException e) {
                e.printStackTrace();
            }
        }else{
            document = application.getUserDatabase().getExistingDocument(getId());
            AppLog.d(TAG, "save", "old document");
            if(document == null){
                AppLog.d(TAG, "save", "new document, old id");
                document = application.getUserDatabase().getDocument(getId());
                try {
                    document.putProperties(properties);
                } catch (CouchbaseLiteException e) {
                    e.printStackTrace();
                }
            }else{
                final Map<String, Object> newProperties = new HashMap<>();
                newProperties.putAll(properties);
                try {
                    document.update(new Document.DocumentUpdater() {
                        @Override
                        public boolean update(UnsavedRevision newRevision) {
                            Map<String, Object> oldProperties = newRevision.getUserProperties();
                            oldProperties.putAll(newProperties);
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
}
