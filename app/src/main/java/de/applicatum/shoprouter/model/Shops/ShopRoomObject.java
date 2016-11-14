package de.applicatum.shoprouter.model.Shops;


import de.applicatum.shoprouter.Application;

public abstract class ShopRoomObject {

    public static final String TAG = "ShopRoomObject";

    private String id;
    //Coordinates in meters from top left connor of shop
    private int x;
    private int y;

    public ShopRoomObject(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public abstract void save(Application application);

    public abstract void delete(Application application);
}
