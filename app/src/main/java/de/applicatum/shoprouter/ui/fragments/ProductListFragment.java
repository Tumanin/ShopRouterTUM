package de.applicatum.shoprouter.ui.fragments;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ExpandableListView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.applicatum.shoprouter.Application;
import de.applicatum.shoprouter.R;
import de.applicatum.shoprouter.model.Products.ProductsController;
import de.applicatum.shoprouter.model.Products.ShoppingList;
import de.applicatum.shoprouter.ui.MainActivity;
import de.applicatum.shoprouter.ui.ShoppingListDetailsActivity;
import de.applicatum.shoprouter.ui.adapters.ExpandableListAdapter;
import de.applicatum.shoprouter.utils.AppLog;

public class ProductListFragment extends Fragment {

    public static final String TAG = "ProductListFragment";

    private View rootView;
    private MainActivity mActivity;
    private ShoppingListDetailsActivity sActivity;
    ExpandableListAdapter listAdapter;

    private boolean fromShoppingList = false;
    private ShoppingList shoppingList;

    @BindView(R.id.lvExp)
    ExpandableListView expListView;
    @BindView(R.id.buttonDone)
    Button buttonDone;
    public ProductListFragment() {
    }

    public void setFromShoppingList(boolean fromShoppingList) {
        this.fromShoppingList = fromShoppingList;
    }

    public void setShoppingList(ShoppingList shoppingList) {
        this.shoppingList = shoppingList;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {


        rootView = inflater.inflate(R.layout.fragment_product_list, container, false);
        ButterKnife.bind(this, rootView);

        if (fromShoppingList) {
            AppLog.d(TAG, "onCreateView", "fromShoppingList");
            sActivity = (ShoppingListDetailsActivity) getActivity();
            buttonDone.setVisibility(View.VISIBLE);
            listAdapter = new ExpandableListAdapter((Application) sActivity.getApplication(), sActivity, ProductsController.getInstance().getProductList().getProductGroups(), shoppingList);
        } else {
            AppLog.d(TAG, "onCreateView", "shoppingList is null");
            mActivity = (MainActivity) getActivity();
            buttonDone.setVisibility(View.GONE);
            listAdapter = new ExpandableListAdapter((Application) mActivity.getApplication(), mActivity, ProductsController.getInstance().getProductList().getProductGroups(), null);
        }


        // setting list adapter
        expListView.setAdapter(listAdapter);

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();

        if (fromShoppingList) {
            sActivity.setTitle("Alle Produkte");
        } else {
            mActivity.setTitle("Alle Produkte");
        }
    }

    @OnClick(R.id.buttonDone)
    public void onClick() {
        sActivity.getSupportFragmentManager().popBackStack();
    }
}
