package de.applicatum.shoprouter.model.Products;


import java.util.ArrayList;
import java.util.Arrays;

import de.applicatum.shoprouter.utils.AppLog;

public class ProductList {

    public static final String TAG = "ProductList";

    private ArrayList<ProductGroup> productGroups;

    public ProductList() {
        productGroups = new ArrayList<>();
    }

    public ArrayList<ProductGroup> getProductGroups() {

        for(ProductGroup productGroup : productGroups){
            AppLog.d(TAG, "getProductGroups", "productGroup: "+ productGroup.getName());
        }
        return productGroups;
    }

    public void addProductGroup(ProductGroup productGroup){
        productGroups.add(productGroup);
    }

    public Product findProductWithName(String name){
        Product product;
        for(ProductGroup productGroup : productGroups){
            product = productGroup.getProduct(name);
            if(product != null) return product;
        }
        return null;
    }

    public ProductGroup findProductGroupWithName(String name){

        for(ProductGroup productGroup : productGroups){
            if(productGroup.getName().equals(name)) return productGroup;
        }
        return null;
    }
}
