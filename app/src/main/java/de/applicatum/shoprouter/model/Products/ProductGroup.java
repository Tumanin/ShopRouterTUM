package de.applicatum.shoprouter.model.Products;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.couchbase.lite.CouchbaseLiteException;
import com.couchbase.lite.Document;

import de.applicatum.shoprouter.Application;

public class ProductGroup extends ProductObject {

    public static final String TAG = "ProductGroup";

    private boolean productGroup = true;

    private ArrayList<ProductObject> productObjects;

    public ProductGroup(String name) {
        super(name);
        productGroup = true;
        productObjects = new ArrayList<>();
    }

    public boolean isProductGroup() {
        return productGroup;
    }

    public void setProductGroup() {
        this.productGroup = true;
    }

    public ArrayList<ProductObject> getProductObjects() {
        return productObjects;
    }

    public ArrayList<Product> getProducts() {
        ArrayList<Product> products = new ArrayList<>();
        for(ProductObject productObject : productObjects){
            if(productObject instanceof Product) products.add((Product)productObject);
        }
        return products;
    }

    public Product getProduct(String name){
        ArrayList<Product> products = new ArrayList<>();
        for(ProductObject productObject : productObjects){
            if(productObject instanceof Product) products.add((Product)productObject);
        }
        for(Product product : products){
            if(product.getName().equals(name)) return product;
        }
        return null;
    }

    public void addProductObject(ProductObject productObject){
        productObjects.add(productObject);
    }

    public void removeProductObject(ProductObject productObject){
        productObjects.remove(productObject);
    }

    public void save(Application application){

        Map<String, Object> properties = new HashMap<>();

        properties.put("type", "product");
        properties.put("name", getName());
        properties.put("description", getDescription());
        properties.put("productGroup", true);

        ArrayList<String> children = new ArrayList<>();
        if(productObjects != null){
            for(ProductObject productObject : productObjects){
                children.add(productObject.getId());
            }
        }
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
