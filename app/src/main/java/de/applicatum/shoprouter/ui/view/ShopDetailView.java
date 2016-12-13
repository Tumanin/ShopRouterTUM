package de.applicatum.shoprouter.ui.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

import de.applicatum.shoprouter.Application;
import de.applicatum.shoprouter.model.Products.ProductGroup;
import de.applicatum.shoprouter.model.Products.ProductsController;
import de.applicatum.shoprouter.model.Products.ShoppingList;
import de.applicatum.shoprouter.model.Products.ShoppingListItem;
import de.applicatum.shoprouter.model.Shops.CashDesk;
import de.applicatum.shoprouter.model.Shops.EnterExit;
import de.applicatum.shoprouter.model.Shops.Shelf;
import de.applicatum.shoprouter.model.Shops.Shop;
import de.applicatum.shoprouter.R;
import de.applicatum.shoprouter.model.Shops.ShopRoomObject;
import de.applicatum.shoprouter.model.Shops.ShoppingListPoint;
import de.applicatum.shoprouter.utils.AppLog;

public class ShopDetailView extends View {

    private static String TAG = "ShopDetailView";

    private boolean editMode = false;

    private Shop shop;
    private ShopRoomObject object;
    private OnMeasureListener measureListener;
    private OnPointTouchListener touchListener;
    private ShoppingList shoppingList;

    private Context context;
    private Application application;

    private Paint paintGrid;
    private Paint paintAreas;
    private Paint paintCashdesk;
    private Paint paintEnter;
    private Paint paintEmptyShelf;
    private Paint paintBread;
    private Paint paintFridge;
    private Paint paintMeat;
    private Paint paintMilk;
    private Paint paintVeget;
    private Paint paintDrink;
    private Paint paintDelicates;
    private Paint paintPoint;
    private Paint paintText;

    private List<Rect> gridAreas;

    private long viewWidth;
    private long viewHeight;
    private int blockWidth;
    private int blockHeight;

    public interface OnMeasureListener{
        void onMeasureDone();
    }

    public interface OnPointTouchListener{
        void onPointTouched(ArrayList<ShoppingListItem> items);
    }

    private void allocatePaints(Context context){

        paintGrid = new Paint();
        paintGrid.setStrokeWidth(2);
        paintGrid.setStyle(Paint.Style.STROKE);
        paintGrid.setColor(ContextCompat.getColor(context, R.color.normal_grey));

        paintAreas = new Paint();
        paintAreas.setStrokeWidth(2);
        paintAreas.setStyle(Paint.Style.STROKE);
        paintAreas.setColor(ContextCompat.getColor(context, R.color.normal_grey));

        paintCashdesk = new Paint();
        paintCashdesk.setStyle(Paint.Style.FILL);
        paintCashdesk.setColor(ContextCompat.getColor(context, R.color.color_cashdesk));

        paintEnter = new Paint();
        paintEnter.setStyle(Paint.Style.FILL);
        paintEnter.setColor(ContextCompat.getColor(context, R.color.color_enter_exit));

        paintEmptyShelf = new Paint();
        paintEmptyShelf.setStyle(Paint.Style.FILL);
        paintEmptyShelf.setColor(ContextCompat.getColor(context, R.color.color_empty_shelf));

        paintBread = new Paint();
        paintBread.setStyle(Paint.Style.FILL);
        paintBread.setColor(ContextCompat.getColor(context, R.color.color_bread));

        paintFridge = new Paint();
        paintFridge.setStyle(Paint.Style.FILL);
        paintFridge.setColor(ContextCompat.getColor(context, R.color.color_fridge));

        paintMeat = new Paint();
        paintMeat.setStyle(Paint.Style.FILL);
        paintMeat.setColor(ContextCompat.getColor(context, R.color.color_meat));

        paintMilk = new Paint();
        paintMilk.setStyle(Paint.Style.FILL);
        paintMilk.setColor(ContextCompat.getColor(context, R.color.color_milk));

        paintVeget = new Paint();
        paintVeget.setStyle(Paint.Style.FILL);
        paintVeget.setColor(ContextCompat.getColor(context, R.color.color_veget));

        paintDrink = new Paint();
        paintDrink.setStyle(Paint.Style.FILL);
        paintDrink.setColor(ContextCompat.getColor(context, R.color.color_drink));

        paintDelicates = new Paint();
        paintDelicates.setStyle(Paint.Style.FILL);
        paintDelicates.setColor(ContextCompat.getColor(context, R.color.color_delicates));

        paintPoint = new Paint();
        paintPoint.setStrokeWidth(8);
        paintPoint.setStyle(Paint.Style.STROKE);
        paintPoint.setColor(ContextCompat.getColor(context, R.color.red));

        paintText = new Paint();
        paintText.setStyle(Paint.Style.FILL);
        paintText.setColor(ContextCompat.getColor(context, R.color.dark_grey));
        paintText.setTextSize(20);
        paintText.setAntiAlias(true);

        gridAreas = new ArrayList<>();
    }

    public void setApplication(Application application) {
        this.application = application;
    }

    public void setData(Shop shop, boolean editMode){
        this.shop = shop;
        this.editMode = editMode;
        shop.clearPoints();
        AppLog.d(TAG, "setData", "viewWidth: "+viewWidth);
        AppLog.d(TAG, "setData", "viewHeight: "+viewHeight);
        blockWidth = (int)viewWidth/shop.getWidth();
        blockHeight = (int)viewHeight/shop.getHeight();
        AppLog.d(TAG, "setData", "shop.getWidth(): "+shop.getWidth());
        AppLog.d(TAG, "setData", "shop.getHeight(): "+shop.getHeight());
        AppLog.d(TAG, "setData", "blockWidth: "+blockWidth);
        AppLog.d(TAG, "setData", "blockHeight: "+blockHeight);
        this.invalidate();
    }

    public void setObject(ShopRoomObject object) {
        this.object = object;
    }

    public void setShoppingList(ShoppingList shoppingList){
        this.shoppingList = shoppingList;

        shop.clearPoints();
        if(shoppingList != null){
            for(ShoppingListItem item : shoppingList.getItems()){
                Shelf shelf = shop.getShelfForProduct(item.getProduct());

                if(shelf != null){
                    int x = shelf.getX();
                    int y = shelf.getY();
                    ProductGroup productGroup = ProductsController.getInstance().getProductList().getGroupForProduct(item.getProduct());
                    if(shelf.getProductTopLeft()==productGroup){
                        if(shelf.getProductTopRight()==productGroup){
                            AppLog.d(TAG, "setShoppingList", "top shelf, product: "+item.getProduct());
                            try {
                                if(shop.getShopRoomObjects()[y-1][x]==null || shop.getShopRoomObjects()[y-1][x] instanceof ShoppingListPoint){
                                    setPoint(x, y-1, item);
                                }else if(shop.getShopRoomObjects()[y][x-1]==null || shop.getShopRoomObjects()[y][x-1] instanceof ShoppingListPoint){
                                    setPoint(x-1, y, item);
                                }else if(shop.getShopRoomObjects()[y][x+1]==null || shop.getShopRoomObjects()[y][x+1] instanceof ShoppingListPoint){
                                    setPoint(x+1, y, item);
                                }else if(shop.getShopRoomObjects()[y-1][x-1]==null || shop.getShopRoomObjects()[y-1][x-1] instanceof ShoppingListPoint){
                                    setPoint(x-1, y-1, item);
                                }else if(shop.getShopRoomObjects()[y-1][x+1]==null || shop.getShopRoomObjects()[y-1][x+1] instanceof ShoppingListPoint){
                                    setPoint(x+1, y-1, item);
                                }
                            } catch (IndexOutOfBoundsException e) {
                                e.printStackTrace();
                            }
                        }else if(shelf.getProductDownLeft()==productGroup){
                            AppLog.d(TAG, "setShoppingList", "left shelf, product: "+item.getProduct());
                            try {
                                if(shop.getShopRoomObjects()[y][x-1]==null || shop.getShopRoomObjects()[y][x-1] instanceof ShoppingListPoint){
                                    setPoint(x-1, y, item);
                                }else if(shop.getShopRoomObjects()[y-1][x]==null || shop.getShopRoomObjects()[y-1][x] instanceof ShoppingListPoint){
                                    setPoint(x, y-1, item);
                                }else if(shop.getShopRoomObjects()[y+1][x]==null || shop.getShopRoomObjects()[y+1][x] instanceof ShoppingListPoint){
                                    setPoint(x, y+1, item);
                                }else if(shop.getShopRoomObjects()[y-1][x-1]==null || shop.getShopRoomObjects()[y-1][x-1] instanceof ShoppingListPoint){
                                    setPoint(x-1, y-1, item);
                                }else if(shop.getShopRoomObjects()[y+1][x-1]==null || shop.getShopRoomObjects()[y+1][x-1] instanceof ShoppingListPoint){
                                    setPoint(x-1, y+1, item);
                                }
                            } catch (IndexOutOfBoundsException e) {
                                e.printStackTrace();
                            }
                        }else {
                            try {
                                if(shop.getShopRoomObjects()[y][x-1]==null || shop.getShopRoomObjects()[y][x-1] instanceof ShoppingListPoint){
                                    setPoint(x-1, y, item);
                                }else if(shop.getShopRoomObjects()[y-1][x]==null || shop.getShopRoomObjects()[y-1][x] instanceof ShoppingListPoint){
                                    setPoint(x, y-1, item);
                                }else if(shop.getShopRoomObjects()[y-1][x-1]==null || shop.getShopRoomObjects()[y-1][x-1] instanceof ShoppingListPoint){
                                    setPoint(x-1, y-1, item);
                                }
                            } catch (IndexOutOfBoundsException e) {
                                e.printStackTrace();
                            }
                        }
                    }else if(shelf.getProductTopRight()==productGroup){
                        if(shelf.getProductDownRight()==productGroup){
                            AppLog.d(TAG, "setShoppingList", "right shelf, product: "+item.getProduct());
                            try {
                                if(shop.getShopRoomObjects()[y][x+1]==null || shop.getShopRoomObjects()[y][x+1] instanceof ShoppingListPoint){
                                    setPoint(x+1, y, item);
                                }else if(shop.getShopRoomObjects()[y-1][x]==null || shop.getShopRoomObjects()[y-1][x] instanceof ShoppingListPoint){
                                    setPoint(x, y-1, item);
                                }else if(shop.getShopRoomObjects()[y+1][x]==null || shop.getShopRoomObjects()[y+1][x] instanceof ShoppingListPoint){
                                    setPoint(x, y+1, item);
                                }else if(shop.getShopRoomObjects()[y-1][x+1]==null || shop.getShopRoomObjects()[y-1][x+1] instanceof ShoppingListPoint){
                                    setPoint(x+1, y-1, item);
                                }else if(shop.getShopRoomObjects()[y+1][x+1]==null || shop.getShopRoomObjects()[y+1][x+1] instanceof ShoppingListPoint){
                                    setPoint(x+1, y+1, item);
                                }
                            } catch (IndexOutOfBoundsException e) {
                                e.printStackTrace();
                            }
                        }else {
                            try {
                                if(shop.getShopRoomObjects()[y-1][x]==null || shop.getShopRoomObjects()[y-1][x] instanceof ShoppingListPoint){
                                    setPoint(x, y-1, item);
                                }else if(shop.getShopRoomObjects()[y][x+1]==null || shop.getShopRoomObjects()[y][x+1] instanceof ShoppingListPoint){
                                    setPoint(x+1, y, item);
                                }else if(shop.getShopRoomObjects()[y-1][x+1]==null || shop.getShopRoomObjects()[y-1][x+1] instanceof ShoppingListPoint){
                                    setPoint(x+1, y-1, item);
                                }
                            } catch (IndexOutOfBoundsException e) {
                                e.printStackTrace();
                            }
                        }
                    }else if(shelf.getProductDownLeft()==productGroup){
                        if(shelf.getProductDownRight()==productGroup){
                            AppLog.d(TAG, "setShoppingList", "down shelf, product: "+item.getProduct());
                            try {
                                if(shop.getShopRoomObjects()[y+1][x]==null || shop.getShopRoomObjects()[y+1][x] instanceof ShoppingListPoint){
                                    setPoint(x, y+1, item);
                                }else if(shop.getShopRoomObjects()[y][x-1]==null || shop.getShopRoomObjects()[y][x-1] instanceof ShoppingListPoint){
                                    setPoint(x-1, y, item);
                                }else if(shop.getShopRoomObjects()[y][x+1]==null || shop.getShopRoomObjects()[y][x+1] instanceof ShoppingListPoint){
                                    setPoint(x+1, y, item);
                                }else if(shop.getShopRoomObjects()[y+1][x-1]==null || shop.getShopRoomObjects()[y+1][x-1] instanceof ShoppingListPoint){
                                    setPoint(x-1, y+1, item);
                                }else if(shop.getShopRoomObjects()[y+1][x+1]==null || shop.getShopRoomObjects()[y+1][x+1] instanceof ShoppingListPoint){
                                    setPoint(x+1, y+1, item);
                                }
                            } catch (IndexOutOfBoundsException e) {
                                e.printStackTrace();
                            }
                        }else {
                            try {
                                if(shop.getShopRoomObjects()[y][x-1]==null || shop.getShopRoomObjects()[y][x-1] instanceof ShoppingListPoint){
                                    setPoint(x-1, y, item);
                                }else if(shop.getShopRoomObjects()[y+1][x]==null || shop.getShopRoomObjects()[y+1][x] instanceof ShoppingListPoint){
                                    setPoint(x, y+1, item);
                                }else if(shop.getShopRoomObjects()[y+1][x-1]==null || shop.getShopRoomObjects()[y+1][x-1] instanceof ShoppingListPoint){
                                    setPoint(x-1, y+1, item);
                                }
                            } catch (IndexOutOfBoundsException e) {
                                e.printStackTrace();
                            }
                        }
                    }else if(shelf.getProductDownRight()==productGroup){
                        try {
                            if(shop.getShopRoomObjects()[y][x+1]==null || shop.getShopRoomObjects()[y][x+1] instanceof ShoppingListPoint){
                                setPoint(x+1, y, item);
                            }else if(shop.getShopRoomObjects()[y+1][x]==null || shop.getShopRoomObjects()[y+1][x] instanceof ShoppingListPoint){
                                setPoint(x, y+1, item);
                            }else if(shop.getShopRoomObjects()[y+1][x+1]==null || shop.getShopRoomObjects()[y+1][x+1] instanceof ShoppingListPoint){
                                setPoint(x+1, y+1, item);
                            }
                        } catch (IndexOutOfBoundsException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }

        this.invalidate();
    }

    private void setPoint(int x, int y, ShoppingListItem item){
        ShoppingListPoint point;
        if(shop.getShopRoomObjects()[y][x] instanceof ShoppingListPoint){
            AppLog.d(TAG, "setPoint","point exists on: "+x+", "+y);
            point = (ShoppingListPoint) shop.getShopRoomObjects()[y][x];
        }else{
            AppLog.d(TAG, "setPoint","new point on: "+x+", "+y);
            point = new ShoppingListPoint(x, y);
        }
        AppLog.d(TAG, "setPoint","add item: "+item.getProduct().getName());
        point.addItem(item);
        AppLog.d(TAG, "setPoint","items count: "+point.getItems().size());
        shop.addShopRoomObject(point);
    }

    public void setMeasureListener(OnMeasureListener measureListener) {
        this.measureListener = measureListener;
    }

    public void setTouchListener(OnPointTouchListener touchListener) {
        this.touchListener = touchListener;
    }

    public ShopDetailView(Context context) {
        super(context);
        this.context = context;
        allocatePaints(context);
    }

    public ShopDetailView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        allocatePaints(context);
    }

    public ShopDetailView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        allocatePaints(context);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public ShopDetailView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        this.context = context;
        allocatePaints(context);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        AppLog.d(TAG, "onMeasure", "start");
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        viewWidth = getMeasuredWidth();
        viewHeight = getMeasuredHeight();
        if(measureListener != null) measureListener.onMeasureDone();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        AppLog.d(TAG, "onDraw", "start");

        if(shop == null) return;
        if(shop.getHeight()<1 || shop.getWidth()<1){
            return;
        }
        drawAreas(canvas);
    }

    private void drawAreas(Canvas canvas){
        gridAreas.clear();

        for(int i=0; i<shop.getHeight(); i++){
            for(int j=0; j<shop.getWidth(); j++){

                int startPointX = blockWidth*j;
                int startPointY = blockHeight*i;
                int endPointX = startPointX + blockWidth;
                int endPointY = startPointY + blockHeight;
                int middlePointX = startPointX+(endPointX - startPointX)/2;
                int middlePointY = startPointY+(endPointY - startPointY)/2;

                Rect rect = new Rect(startPointX, startPointY, endPointX, endPointY);
                gridAreas.add(rect);
                canvas.drawRect(rect, paintAreas);
                ShopRoomObject shopRoomObject = shop.getShopRoomObjects()[i][j];

                int minLength = blockWidth<blockHeight?blockWidth:blockHeight;
                if(shopRoomObject != null){

                    if(shopRoomObject instanceof EnterExit){
                        setTextSizeForWidth(paintText, minLength, "E");
                        canvas.drawRect(startPointX, startPointY, endPointX, endPointY, paintEnter);
                        canvas.drawText("E", startPointX, endPointY, paintText);
                    } else if(shopRoomObject instanceof CashDesk){
                        setTextSizeForWidth(paintText, minLength, "$");
                        canvas.drawRect(startPointX, startPointY, endPointX, endPointY, paintCashdesk);
                        canvas.drawText("$", startPointX, endPointY, paintText);
                    } else if(shopRoomObject instanceof Shelf){
                        Shelf shelf = (Shelf) shopRoomObject;



//                        AppLog.d(TAG, "onDraw", "startPointX: "+startPointX);
//                        AppLog.d(TAG, "onDraw", "startPointY: "+startPointY);
//                        AppLog.d(TAG, "onDraw", "endPointX: "+endPointX);
//                        AppLog.d(TAG, "onDraw", "endPointY: "+endPointY);
//                        AppLog.d(TAG, "onDraw", "middlePointX: "+middlePointX);
//                        AppLog.d(TAG, "onDraw", "middlePointY: "+middlePointY);

                        if(shelf.getProductTopLeft() != null){
                            canvas.drawRect(startPointX, startPointY, middlePointX, middlePointY, getPaintOfProductGroup(shelf.getProductTopLeft()));
                        }else{
                            canvas.drawRect(startPointX, startPointY, middlePointX, middlePointY, paintEmptyShelf);
                        }
                        if(shelf.getProductTopRight() != null){
                            canvas.drawRect(middlePointX, startPointY, endPointX, middlePointY, getPaintOfProductGroup(shelf.getProductTopRight()));
                        }else{
                            canvas.drawRect(middlePointX, startPointY, endPointX, middlePointY, paintEmptyShelf);
                        }
                        if(shelf.getProductDownLeft() != null){
                            canvas.drawRect(startPointX, middlePointY, middlePointX, endPointY, getPaintOfProductGroup(shelf.getProductDownLeft()));
                        }else{
                            canvas.drawRect(startPointX, middlePointY, middlePointX, endPointY, paintEmptyShelf);
                        }
                        if(shelf.getProductDownRight() != null){
                            canvas.drawRect(middlePointX, middlePointY, endPointX, endPointY, getPaintOfProductGroup(shelf.getProductDownRight()));
                        }else{
                            canvas.drawRect(middlePointX, middlePointY, endPointX, endPointY, paintEmptyShelf);
                        }
                    } else if(shopRoomObject instanceof ShoppingListPoint && !editMode){
                        int radius = (int) Math.min(blockHeight, blockWidth)/3;
                        canvas.drawCircle(middlePointX, middlePointY, radius, paintPoint);
                    }
                }
            }
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        int touchX = (int)event.getX();
        int touchY = (int)event.getY();
        switch(event.getAction()){
            case MotionEvent.ACTION_DOWN:

                for(Rect rect : gridAreas){
                    if(rect.contains(touchX,touchY)){
                        AppLog.d(TAG, "onTouchEvent", "area number: " + gridAreas.indexOf(rect));
                        int x = gridAreas.indexOf(rect)%shop.getWidth();
                        int y = gridAreas.indexOf(rect)/shop.getWidth();
                        AppLog.d(TAG, "onTouchEvent", "x: " + x);
                        AppLog.d(TAG, "onTouchEvent", "y: " + y);
                        if (editMode) {
                            if(object != null){

                                if(object instanceof EnterExit){
                                    EnterExit newObject = new EnterExit(x, y);
                                    newObject.save(application);
                                    AppLog.d(TAG, "onTouchEvent", "object id: "+newObject.getId());
                                    shop.addShopRoomObject(newObject);
                                }else if(object instanceof CashDesk){
                                    CashDesk newObject = new CashDesk(x, y);
                                    newObject.save(application);
                                    AppLog.d(TAG, "onTouchEvent", "object id: "+newObject.getId());
                                    shop.addShopRoomObject(newObject);
                                }else if(object instanceof Shelf){
                                    Shelf newObject = new Shelf(x, y);
                                    newObject.setProductTopLeft(((Shelf) object).getProductTopLeft());
                                    newObject.setProductTopRight(((Shelf) object).getProductTopRight());
                                    newObject.setProductDownLeft(((Shelf) object).getProductDownLeft());
                                    newObject.setProductDownRight(((Shelf) object).getProductDownRight());
                                    newObject.save(application);
                                    AppLog.d(TAG, "onTouchEvent", "object id: "+newObject.getId());
                                    shop.addShopRoomObject(newObject);
                                }
                                AppLog.d(TAG, "onTouchEvent", "object is not null");
                            }else{
                                AppLog.d(TAG, "onTouchEvent", "object is null");
                                ShopRoomObject shopRoomObject = shop.removeShopRoomObjectAtIndex(x, y);
                                if(shopRoomObject != null) shopRoomObject.delete(application);
                            }
                        } else {
                            if (shop.getShopRoomObjects()[y][x] != null && shop.getShopRoomObjects()[y][x] instanceof ShoppingListPoint) {
                                if(touchListener != null){
                                    ShoppingListPoint point = (ShoppingListPoint) shop.getShopRoomObjects()[y][x];
                                    AppLog.d(TAG, "onTouchEvent","items count: "+point.getItems().size());
                                    touchListener.onPointTouched(point.getItems());
                                }
                            }
                        }
                        this.invalidate();
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
                AppLog.d(TAG, "onTouchEvent", "Touching up!");
                break;
            case MotionEvent.ACTION_MOVE:
                AppLog.d(TAG, "onTouchEvent", "Sliding your finger around on the screen.");
                break;
        }
        return true;
    }

    private Paint getPaintOfProductGroup(ProductGroup productGroup){

        switch (ProductsController.getInstance().getProductList().getProductGroups().indexOf(productGroup)){
            case 0:
                return paintBread;
            case 1:
                return paintFridge;
            case 2:
                return paintMeat;
            case 3:
                return paintMilk;
            case 4:
                return paintVeget;
            case 5:
                return paintDrink;
            case 6:
                return paintDelicates;
            default:
                return paintEmptyShelf;
        }
    }

    /**
     * Sets the text size for a Paint object so a given string of text will be a
     * given width.
     *
     * @param paint
     *            the Paint to set the text size for
     * @param desiredWidth
     *            the desired width
     * @param text
     *            the text that should be that width
     */
    private static void setTextSizeForWidth(Paint paint, float desiredWidth,
                                            String text) {

        // Pick a reasonably large value for the test. Larger values produce
        // more accurate results, but may cause problems with hardware
        // acceleration.
        final float testTextSize = 20f;

        // Get the bounds of the text, using our testTextSize.
        paint.setTextSize(testTextSize);
        Rect bounds = new Rect();
        paint.getTextBounds(text, 0, text.length(), bounds);

        // Calculate the desired size as a proportion of our testTextSize.
        float desiredTextSize = testTextSize * desiredWidth / bounds.width();

        // Set the paint for that size.
        if (desiredTextSize < testTextSize) {
            paint.setTextSize(desiredTextSize);
        }
    }
}
