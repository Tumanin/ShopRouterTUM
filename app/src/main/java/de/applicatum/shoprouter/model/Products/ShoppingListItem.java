package de.applicatum.shoprouter.model.Products;


import com.couchbase.lite.CouchbaseLiteException;
import com.couchbase.lite.Document;

import java.util.HashMap;
import java.util.Map;

import de.applicatum.shoprouter.Application;

public class ShoppingListItem {

    public static final String TAG = "ShoppingListItem";

    private String id;
    private Product product;
    private boolean bought = false;

    public ShoppingListItem(Product product) {
        this.product = product;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public boolean isBought() {
        return bought;
    }

    public void setBought(boolean bought) {
        this.bought = bought;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public void save(Application application){

        Map<String, Object> properties = new HashMap<>();

        properties.put("type", "shoppingListItem");
        properties.put("product", getProduct().getName());

        Document document;
        if(getId()==null || getId().equals("")){
            document = application.getUserDatabase().createDocument();
            this.id = document.getId();
        }else{
            document = application.getUserDatabase().getExistingDocument(getId());

            if(document == null){
                document = application.getUserDatabase().getDocument(getId());
            }
        }
        try {
            document.putProperties(properties);
        } catch (CouchbaseLiteException e) {
            e.printStackTrace();
        }
    }
}
