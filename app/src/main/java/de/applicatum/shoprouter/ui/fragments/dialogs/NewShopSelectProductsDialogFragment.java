package de.applicatum.shoprouter.ui.fragments.dialogs;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Service;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.AppCompatSpinner;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.applicatum.shoprouter.R;
import de.applicatum.shoprouter.model.Products.ProductGroup;
import de.applicatum.shoprouter.model.Products.ProductsController;
import de.applicatum.shoprouter.model.Shops.Shelf;
import de.applicatum.shoprouter.ui.fragments.ShopEditFragment;

public class NewShopSelectProductsDialogFragment extends DialogFragment {

    private static String TAG = "NewShopSelectProductsDialogFragment";
    @BindView(R.id.leftTopSpinner)
    AppCompatSpinner leftTopSpinner;
    @BindView(R.id.leftTopColor)
    View leftTopColor;
    @BindView(R.id.rightTopColor)
    View rightTopColor;
    @BindView(R.id.rightTopSpinner)
    AppCompatSpinner rightTopSpinner;
    @BindView(R.id.leftDownSpinner)
    AppCompatSpinner leftDownSpinner;
    @BindView(R.id.leftDownColor)
    View leftDownColor;
    @BindView(R.id.rightDownColor)
    View rightDownColor;
    @BindView(R.id.rightDownSpinner)
    AppCompatSpinner rightDownSpinner;

    private AlertDialog mAlertDialog;
    private ShopEditFragment shopEditFragment;
    private Shelf shelf;
    private Activity activity;

    public NewShopSelectProductsDialogFragment() {
        super();
    }

    public void setParentFragment(ShopEditFragment shopEditFragment) {
        this.shopEditFragment = shopEditFragment;
    }

    public void setShelf(Shelf shelf) {
        this.shelf = shelf;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        activity = getActivity();
        LayoutInflater inflater = (LayoutInflater) activity.getSystemService(Service.LAYOUT_INFLATER_SERVICE);
        View layout = inflater.inflate(R.layout.view_dialog_shelf_config, null);
        ButterKnife.bind(this, layout);

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(activity);
        alertDialogBuilder.setView(layout);
        alertDialogBuilder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                shopEditFragment.setPreview(shelf);
                mAlertDialog.dismiss();
            }
        });
        alertDialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mAlertDialog.dismiss();
            }
        });
        mAlertDialog = alertDialogBuilder.create();

        setSpinners();
        return mAlertDialog;
    }

    private void setSpinners(){
        ArrayAdapter<String> leftTopAdapter = new ArrayAdapter<>(activity, android.R.layout.simple_list_item_1, getResources().getStringArray(R.array.product_groups));
        leftTopAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        leftTopSpinner.setAdapter(leftTopAdapter);
        leftDownSpinner.setAdapter(leftTopAdapter);
        rightTopSpinner.setAdapter(leftTopAdapter);
        rightDownSpinner.setAdapter(leftTopAdapter);

        final ArrayList<ProductGroup> productGroups = ProductsController.getInstance().getProductList().getProductGroups();
        leftTopSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                try {
                    shelf.setProductTopLeft(productGroups.get(position-1));
                } catch (ArrayIndexOutOfBoundsException e) {
                    e.printStackTrace();
                    shelf.setProductTopLeft(null);
                }
                leftTopColor.setBackgroundColor(getProductColor(position));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        rightTopSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                try {
                    shelf.setProductTopRight(productGroups.get(position-1));
                } catch (ArrayIndexOutOfBoundsException e) {
                    e.printStackTrace();
                    shelf.setProductTopRight(null);
                }
                rightTopColor.setBackgroundColor(getProductColor(position));
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        leftDownSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                try {
                    shelf.setProductDownLeft(productGroups.get(position-1));
                } catch (ArrayIndexOutOfBoundsException e) {
                    e.printStackTrace();
                    shelf.setProductDownLeft(null);
                }
                leftDownColor.setBackgroundColor(getProductColor(position));
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        rightDownSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                try {
                    shelf.setProductDownRight(productGroups.get(position-1));
                } catch (ArrayIndexOutOfBoundsException e) {
                    e.printStackTrace();
                    shelf.setProductDownRight(null);
                }
                rightDownColor.setBackgroundColor(getProductColor(position));
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private int getProductColor(int position){
        switch (position){
            case 0:
                return ContextCompat.getColor(activity, R.color.color_empty_shelf);
            case 1:
                return ContextCompat.getColor(activity, R.color.color_bread);
            case 2:
                return ContextCompat.getColor(activity, R.color.color_fridge);
            case 3:
                return ContextCompat.getColor(activity, R.color.color_meat);
            case 4:
                return ContextCompat.getColor(activity, R.color.color_milk);
            case 5:
                return ContextCompat.getColor(activity, R.color.color_veget);
            case 6:
                return ContextCompat.getColor(activity, R.color.color_drink);
            case 7:
                return ContextCompat.getColor(activity, R.color.color_delicates);
            default:
                return ContextCompat.getColor(activity, R.color.color_empty_shelf);
        }
    }
}
