package de.applicatum.shoprouter.model.Shops;

import java.util.ArrayList;

import de.applicatum.shoprouter.Application;
import de.applicatum.shoprouter.model.Products.ShoppingListItem;

/**
 * Created by Alexx on 12.12.16.
 */

public class ShoppingListPoint extends ShopRoomObject {

    ArrayList<ShoppingListItem> items;

    public ShoppingListPoint(int x, int y) {
        super(x, y);
        items = new ArrayList<>();
    }

    public ArrayList<ShoppingListItem> getItems(){
        return items;
    }

    public void addItem(ShoppingListItem item){
        items.add(item);
    }

    @Override
    public void save(Application application) {

    }

    @Override
    public void delete(Application application) {

    }
}
