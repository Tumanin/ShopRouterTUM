package de.applicatum.shoprouter.ui.view;


import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.View;

import de.applicatum.shoprouter.R;
import de.applicatum.shoprouter.model.Products.ProductGroup;
import de.applicatum.shoprouter.model.Products.ProductsController;
import de.applicatum.shoprouter.model.Shops.CashDesk;
import de.applicatum.shoprouter.model.Shops.EnterExit;
import de.applicatum.shoprouter.model.Shops.Shelf;
import de.applicatum.shoprouter.model.Shops.ShopRoomObject;

public class ShopObjectPreview extends View{

    private static String TAG = "ShopObjectPreview";

    private boolean editMode = false;

    private Context context;

    private ShopRoomObject object;

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
    private Paint paintBlank;

    private long viewWidth;
    private long viewHeight;
    private int blockWidth;
    private int blockHeight;

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
        paintPoint.setStyle(Paint.Style.FILL);
        paintPoint.setColor(ContextCompat.getColor(context, R.color.red));

        paintText = new Paint();
        paintText.setStyle(Paint.Style.FILL);
        paintText.setColor(ContextCompat.getColor(context, R.color.dark_grey));
        paintText.setTextSize(20);
        paintText.setAntiAlias(true);

        paintBlank = new Paint();
        paintBlank.setStyle(Paint.Style.FILL);
        paintBlank.setColor(ContextCompat.getColor(context, R.color.full_white));
    }

    public void setData(ShopRoomObject object){
        this.object = object;
        this.invalidate();
    }

    public ShopObjectPreview(Context context) {
        super(context);
        this.context = context;
        allocatePaints(context);
    }

    public ShopObjectPreview(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        allocatePaints(context);
    }

    public ShopObjectPreview(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        allocatePaints(context);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public ShopObjectPreview(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        this.context = context;
        allocatePaints(context);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        viewWidth = getMeasuredWidth();
        viewHeight = getMeasuredHeight();
    }

    @Override
    protected void onDraw(Canvas canvas) {

        if (object != null) {
            if(object instanceof EnterExit){
                canvas.drawRect(0, 0, viewWidth, viewHeight, paintEnter);

                Rect bounds = new Rect();
                paintText.getTextBounds("E", 0, 1, bounds);

                canvas.drawText("E", viewWidth/2 - bounds.width()/2, viewHeight/2 + bounds.height()/2, paintText);
            }else if(object instanceof CashDesk){
                canvas.drawRect(0, 0, viewWidth, viewHeight, paintCashdesk);

                Rect bounds = new Rect();
                paintText.getTextBounds("$", 0, 1, bounds);

                canvas.drawText("$", viewWidth/2 - bounds.width()/2, viewHeight/2 + bounds.height()/2, paintText);
            }else if(object instanceof Shelf){
                Shelf shelf = (Shelf) object;

                if(shelf.getProductTopLeft() != null){
                    canvas.drawRect(0, 0, viewWidth/2, viewHeight/2, getPaintOfProductGroup(shelf.getProductTopLeft()));
                }else{
                    canvas.drawRect(0, 0, viewWidth/2, viewHeight/2, paintEmptyShelf);
                }
                if(shelf.getProductTopRight() != null){
                    canvas.drawRect(viewWidth/2, 0, viewWidth, viewHeight/2, getPaintOfProductGroup(shelf.getProductTopRight()));
                }else{
                    canvas.drawRect(viewWidth/2, 0, viewWidth, viewHeight/2, paintEmptyShelf);
                }
                if(shelf.getProductDownLeft() != null){
                    canvas.drawRect(0, viewHeight/2, viewWidth/2, viewHeight, getPaintOfProductGroup(shelf.getProductDownLeft()));
                }else{
                    canvas.drawRect(0, viewHeight/2, viewWidth/2, viewHeight, paintEmptyShelf);
                }
                if(shelf.getProductDownRight() != null){
                    canvas.drawRect(viewWidth/2, viewHeight/2, viewWidth, viewHeight, getPaintOfProductGroup(shelf.getProductDownRight()));
                }else{
                    canvas.drawRect(viewWidth/2, viewHeight/2, viewWidth, viewHeight, paintEmptyShelf);
                }
            }else{
                canvas.drawRect(0, 0, viewWidth, viewHeight, paintEmptyShelf);
            }
        } else {
            canvas.drawRect(0, 0, viewWidth, viewHeight, paintBlank);
            canvas.drawLine(0, 0, viewWidth, viewHeight, paintText);
            canvas.drawLine(viewWidth, 0, 0, viewHeight, paintText);
        }
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
}
