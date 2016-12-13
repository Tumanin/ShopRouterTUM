package de.applicatum.shoprouter.ui.fragments;


import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.applicatum.shoprouter.Application;
import de.applicatum.shoprouter.R;
import de.applicatum.shoprouter.model.Shops.CashDesk;
import de.applicatum.shoprouter.model.Shops.EnterExit;
import de.applicatum.shoprouter.model.Shops.Shelf;
import de.applicatum.shoprouter.model.Shops.Shop;
import de.applicatum.shoprouter.model.Shops.ShopRoomObject;
import de.applicatum.shoprouter.ui.MainActivity;
import de.applicatum.shoprouter.ui.fragments.dialogs.NewShopSelectProductsDialogFragment;
import de.applicatum.shoprouter.ui.view.ShopDetailView;
import de.applicatum.shoprouter.ui.view.ShopObjectPreview;
import de.applicatum.shoprouter.utils.AppLog;
import me.zhanghai.android.materialratingbar.MaterialRatingBar;

public class ShopEditFragment extends Fragment implements ShopDetailView.OnMeasureListener{

    public static final String TAG = "ShopEditFragment";
    @BindView(R.id.shopObjectPreview)
    ShopObjectPreview shopObjectPreview;
    @BindView(R.id.textShopObject)
    TextView textShopObject;
    @BindView(R.id.buttonPreview)
    LinearLayout buttonPreview;
    @BindView(R.id.buttonDone)
    TextView buttonDone;
    @BindView(R.id.editLayout)
    RelativeLayout editLayout;
    @BindView(R.id.shopView)
    ShopDetailView shopView;

    private Shop shop;
    private ShopRoomObject actualObject;

    private MainActivity activity;
    private View view;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        activity = (MainActivity) getActivity();
        view = inflater.inflate(R.layout.fragment_shop_edit, container, false);

        ButterKnife.bind(this, view);

        actualObject = new EnterExit(0, 0);
        setPreview(actualObject);

        shopView.setApplication((Application) activity.getApplication());
        if(shop.getWidth()<1 || shop.getHeight()<1){
            startDimensDialog();
        } else{
            shopView.setMeasureListener(this);
        }
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        activity.setTitle("Edit "+shop.getName());
    }

    public void setShop(Shop shop) {
        this.shop = shop;
    }

    @OnClick({R.id.buttonPreview, R.id.buttonDone})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.buttonPreview:
                startSelectObjectDialog();
                break;
            case R.id.buttonDone:

                final AlertDialog.Builder alert = new AlertDialog.Builder(activity);
                alert.setTitle("Geschätzte Genauigkeit");

                LayoutInflater inflater = activity.getLayoutInflater();
                final View dialogView = inflater.inflate(R.layout.view_dialog_rate, null);
                final MaterialRatingBar ratingBar = (MaterialRatingBar) dialogView.findViewById(R.id.ratingBar);
                alert.setView(dialogView);
                alert.setCancelable(true);
                alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {

                        try {
                            int rating = ratingBar.getProgress();
                            AppLog.d(TAG, "buttonDone", "ratingBar: "+rating);

                            shop.setRating(rating);
                            shop.save((Application)activity.getApplication());

                            GlobalMapFragment fragment = new GlobalMapFragment();
                            activity.startFragment(fragment, false, 0);
                        } catch (NumberFormatException e) {
                            e.printStackTrace();
                            return;
                        }

                    }
                });

                alert.show();


                break;
        }
    }

    private void startDimensDialog(){
        final AlertDialog.Builder alert = new AlertDialog.Builder(activity);
        alert.setTitle("Gesamtflächengröße");

        LayoutInflater inflater = activity.getLayoutInflater();
        final View view = inflater.inflate(R.layout.view_dialog_dimens, null);
        final EditText editTextWidth = (EditText) view.findViewById(R.id.editTextWidth);
        final EditText editTextHeight = (EditText) view.findViewById(R.id.editTextHeight);
        alert.setView(view);
        alert.setCancelable(false);
        alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {

                try {
                    int width = Integer.parseInt(editTextWidth.getText().toString());
                    int height = Integer.parseInt(editTextHeight.getText().toString());
                    if (width < 1 || height < 1)
                        return;
                    shop.setWidthHeight(width, height);
                    shopView.setData(shop, true);
                    dialog.dismiss();
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                    return;
                }

            }
        });

        alert.show();
    }

    private void startSelectObjectDialog(){
        final AlertDialog.Builder alert = new AlertDialog.Builder(activity);
        alert.setTitle("Wähle Objekt");

//        LayoutInflater inflater = activity.getLayoutInflater();
//        final View view = inflater.inflate(R.layout.view_dialog_shop_object, null);
//
//        View enterExitLayout = view.findViewById(R.id.enterExitLayout);
//        View cashdeskLayout = view.findViewById(R.id.cashdeskLayout);
//        View shelfLayout = view.findViewById(R.id.shelfLayout);
//
//        enterExitLayout.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                EnterExit enterExit = new EnterExit(0, 0);
//                setPreview(enterExit);
//                alert.
//            }
//        });
//        alert.setView(view);
        int checkedItem = 0;
        if(actualObject instanceof EnterExit) checkedItem = 1;
        if(actualObject instanceof CashDesk) checkedItem = 2;
        else if(actualObject instanceof Shelf) checkedItem = 3;
        alert.setSingleChoiceItems(R.array.shop_objects, checkedItem, new DialogInterface.OnClickListener(){

            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which){
                    case 0:
                        setPreview(null);
                        dialog.dismiss();
                        break;
                    case 1:
                        setPreview(new EnterExit(0, 0));
                        dialog.dismiss();
                        break;
                    case 2:
                        setPreview(new CashDesk(0, 0));
                        dialog.dismiss();
                        break;
                    case 3:
                        Shelf shelf = new Shelf(0, 0);
                        setPreview(shelf);
                        startShelfDialog(shelf);
                        dialog.dismiss();
                        break;
                }
            }
        });

        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                dialog.dismiss();
            }
        });

        alert.show();
    }

    private void startShelfDialog(Shelf shelf){
        FragmentManager fm = activity.getSupportFragmentManager();
        NewShopSelectProductsDialogFragment df = new NewShopSelectProductsDialogFragment();
        df.setParentFragment(this);
        df.setShelf(shelf);
        Bundle args = new Bundle();
        df.setArguments(args);
        df.show(fm, "NewShopSelectProductsDialogFragment");
    }

    public void setPreview(ShopRoomObject object){
        if (object != null) {
            actualObject = object;
            if(object instanceof EnterExit){
                textShopObject.setText("Eingang/Ausgang");
            } else if(object instanceof CashDesk){
                textShopObject.setText("Kassen");
            } else{
                textShopObject.setText("Regal");
            }
            shopObjectPreview.setData(actualObject);
            shopView.setObject(actualObject);
        } else {
            actualObject = null;
            textShopObject.setText("Leere Fläche");
            shopObjectPreview.setData(null);
            shopView.setObject(null);
        }
    }

    @Override
    public void onMeasureDone() {
        AppLog.d(TAG, "onMeasureDone", "start");
        shopView.setData(shop, true);
    }
}
