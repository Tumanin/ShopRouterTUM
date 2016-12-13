package de.applicatum.shoprouter.ui.fragments;


import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.AppCompatSpinner;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.lang.reflect.Array;
import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.applicatum.shoprouter.Application;
import de.applicatum.shoprouter.R;
import de.applicatum.shoprouter.model.Products.ProductsController;
import de.applicatum.shoprouter.model.Products.ShoppingList;
import de.applicatum.shoprouter.model.Products.ShoppingListItem;
import de.applicatum.shoprouter.model.Shops.Shop;
import de.applicatum.shoprouter.ui.MainActivity;
import de.applicatum.shoprouter.ui.view.ShopDetailView;
import de.applicatum.shoprouter.utils.AppLog;
import me.zhanghai.android.materialratingbar.MaterialRatingBar;

public class ShopNavigationFragment extends Fragment implements ShopDetailView.OnMeasureListener, ShopDetailView.OnPointTouchListener{

    public static final String TAG = "ShopNavigationFragment";
    @BindView(R.id.spinnerShopList)
    AppCompatSpinner spinnerShopList;
    @BindView(R.id.buttonOptions)
    TextView buttonOptions;
    @BindView(R.id.navLayout)
    RelativeLayout navLayout;
    @BindView(R.id.shopView)
    ShopDetailView shopView;

    private MainActivity activity;
    private View mRootView;

    private Shop shop;

    private ShoppingList shoppingList;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        activity = (MainActivity) getActivity();
        mRootView = inflater.inflate(R.layout.fragment_shop_navigation, container, false);

        ButterKnife.bind(this, mRootView);

        if(shop != null){
            AppLog.d(TAG, "onCreateView", "shop name: "+shop.getName());
        }
        shopView.setMeasureListener(this);
        shopView.setTouchListener(this);
        setSpinner();
        return mRootView;
    }

    public void setShop(Shop shop){
        this.shop = shop;
    }

    @OnClick(R.id.buttonOptions)
    public void onClick() {
        final AlertDialog.Builder alert = new AlertDialog.Builder(activity);
        alert.setTitle("Options");
        alert.setCancelable(false);
        alert.setPositiveButton("Bewerte Genauigkeit", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {

                final AlertDialog.Builder alert = new AlertDialog.Builder(activity);
                alert.setTitle("Geschätzte Genauigkeit");

                LayoutInflater inflater = activity.getLayoutInflater();
                final View dialogView = inflater.inflate(R.layout.view_dialog_rate, null);
                final MaterialRatingBar ratingBar = (MaterialRatingBar) dialogView.findViewById(R.id.ratingBar);
                alert.setView(dialogView);
                alert.setCancelable(false);
                alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {

                        try {
                            int rating = ratingBar.getProgress();
                            AppLog.d(TAG, "buttonDone", "ratingBar: "+rating);

                            shop.setRating(rating);
                            shop.save((Application)activity.getApplication());

                            dialog.dismiss();
                        } catch (NumberFormatException e) {
                            e.printStackTrace();
                            return;
                        }

                    }
                });

                alert.show();
                dialog.dismiss();
            }
        });
        if (shop.getRating()<10) {
            alert.setNeutralButton("Bearbeiten", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    ShopEditFragment fragment = new ShopEditFragment();
                    fragment.setShop(shop);
                    activity.startFragment(fragment, false, 0);
                }
            });
        }
        alert.setNegativeButton("Zurück", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });

        alert.show();
    }

    @Override
    public void onResume() {
        super.onResume();
        AppLog.d(TAG, "onResume", "start");
        activity.setTitle(shop.getName());

    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        AppLog.d(TAG, "onViewCreated", "start");
        super.onViewCreated(view, savedInstanceState);

    }

    private void setSpinner(){

        ArrayAdapter<String> adapter = new ArrayAdapter<>(activity, android.R.layout.simple_list_item_1, ProductsController.getInstance().getShoppingListNamesArray());
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerShopList.setAdapter(adapter);

        spinnerShopList.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                shoppingList = ProductsController.getInstance().getShoppingLists().get(position);
                shopView.setShoppingList(shoppingList);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    @Override
    public void onMeasureDone() {
        AppLog.d(TAG, "onMeasureDone", "start");
        shopView.setData(shop, false);
    }

    @Override
    public void onPointTouched(ArrayList<ShoppingListItem> items) {
        String[] shoppingListTitles = new String[items.size()];
        for(int i=0; i<shoppingListTitles.length; i++){
            shoppingListTitles[i] = items.get(i).getProduct().getName();
        }
        AlertDialog.Builder alert = new AlertDialog.Builder(activity);
        alert.setTitle("Produkte");
        alert.setCancelable(true);
        alert.setItems(shoppingListTitles, null);
        alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        alert.show();
    }
}
