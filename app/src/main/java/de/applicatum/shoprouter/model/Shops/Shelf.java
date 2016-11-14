package de.applicatum.shoprouter.model.Shops;


import com.couchbase.lite.CouchbaseLiteException;
import com.couchbase.lite.Document;

import java.util.HashMap;
import java.util.Map;

import de.applicatum.shoprouter.Application;
import de.applicatum.shoprouter.model.Products.ProductGroup;
import de.applicatum.shoprouter.utils.AppLog;

public class Shelf extends ShopRoomObject {

    public static final String TAG = "Shelf";

    private ProductGroup productTopLeft;
    private ProductGroup productTopRight;
    private ProductGroup productDownLeft;
    private ProductGroup productDownRight;

    public Shelf(int x, int y) {
        super(x, y);
    }

    public ProductGroup getProductTopLeft() {
        return productTopLeft;
    }

    public void setProductTopLeft(ProductGroup productTopLeft) {
        this.productTopLeft = productTopLeft;
    }

    public ProductGroup getProductTopRight() {
        return productTopRight;
    }

    public void setProductTopRight(ProductGroup productTopRight) {
        this.productTopRight = productTopRight;
    }

    public ProductGroup getProductDownLeft() {
        return productDownLeft;
    }

    public void setProductDownLeft(ProductGroup productDownLeft) {
        this.productDownLeft = productDownLeft;
    }

    public ProductGroup getProductDownRight() {
        return productDownRight;
    }

    public void setProductDownRight(ProductGroup productDownRight) {
        this.productDownRight = productDownRight;
    }

    @Override
    public void save(Application application){

        AppLog.d(TAG, "save", "store Shelf");
        Map<String, Object> properties = new HashMap<>();

        properties.put("type", "shelf");
        properties.put("x", getX());
        properties.put("y", getY());
        if (productTopLeft != null) {
            properties.put("productTopLeft", productTopLeft.getName());
        } else {
            properties.put("productTopLeft", "null");
        }
        if (productTopRight != null) {
            properties.put("productTopRight", productTopRight.getName());
        } else {
            properties.put("productTopRight", "null");
        }
        if (productDownLeft != null) {
            properties.put("productDownLeft", productDownLeft.getName());
        } else {
            properties.put("productDownLeft", "null");
        }
        if (productDownRight != null) {
            properties.put("productDownRight", productDownRight.getName());
        } else {
            properties.put("productDownRight", "null");
        }

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
