package de.applicatum.shoprouter.ui.fragments;


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
import android.widget.SpinnerAdapter;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.applicatum.shoprouter.R;
import de.applicatum.shoprouter.model.Products.ProductsController;
import de.applicatum.shoprouter.model.Products.ShoppingList;
import de.applicatum.shoprouter.model.Shops.Shop;
import de.applicatum.shoprouter.ui.MainActivity;
import de.applicatum.shoprouter.ui.view.ShopDetailView;
import de.applicatum.shoprouter.utils.AppLog;

public class ShopNavigationFragment extends Fragment implements ShopDetailView.OnMeasureListener{

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
        shopView.setListener(this);
        setSpinner();
        return mRootView;
    }

    public void setShop(Shop shop){
        this.shop = shop;
    }

    @OnClick(R.id.buttonOptions)
    public void onClick() {
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
}
