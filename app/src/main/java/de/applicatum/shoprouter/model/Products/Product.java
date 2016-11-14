package de.applicatum.shoprouter.model.Products;

import com.couchbase.lite.CouchbaseLiteException;
import com.couchbase.lite.Document;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import de.applicatum.shoprouter.Application;

public class Product extends ProductObject{

    public static final String TAG = "Product";

    private boolean productGroup = false;
    public Product(String name) {
        super(name);
        productGroup = false;
    }

    public boolean isProductGroup() {
        return productGroup;
    }

    public void setProductGroup() {
        this.productGroup = false;
    }

    public void save(Application application){

        Map<String, Object> properties = new HashMap<>();

        properties.put("type", "product");
        properties.put("name", getName());
        properties.put("description", getDescription());
        properties.put("productGroup", false);

        ArrayList<String> children = new ArrayList<>();
        properties.put("children", children);
        Document document;
        if(getId()==null || getId().equals("")){
            document = application.getGlobalDatabase().createDocument();
        }else{
            document = application.getGlobalDatabase().getExistingDocument(getId());

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
}
