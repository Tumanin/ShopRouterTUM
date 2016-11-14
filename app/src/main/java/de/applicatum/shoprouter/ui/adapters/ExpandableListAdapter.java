package de.applicatum.shoprouter.ui.adapters;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import de.applicatum.shoprouter.Application;
import de.applicatum.shoprouter.R;
import de.applicatum.shoprouter.model.Products.Product;
import de.applicatum.shoprouter.model.Products.ProductGroup;
import de.applicatum.shoprouter.model.Products.ProductObject;
import de.applicatum.shoprouter.model.Products.ShoppingList;
import de.applicatum.shoprouter.model.Products.ShoppingListItem;
import de.applicatum.shoprouter.utils.AppLog;

public class ExpandableListAdapter extends BaseExpandableListAdapter {

    public static final String TAG = "ExpandableListAdapter";

    private Application application;
    Context context;
    private List<ProductGroup> _listDataHeader; // header titles
    // child data in format of header title, child title
    private HashMap<String, List<Product>> _listDataChild;
    ArrayList<String> checkedItems;
    private ShoppingList shoppingList;

    public ExpandableListAdapter(Application application, Context context, List<ProductGroup> listDataHeader, ShoppingList shoppingList) {
        this.application = application;
        this._listDataHeader = listDataHeader;
        this.shoppingList = shoppingList;
        this.context = context;
        HashMap<String, List<Product>> listChildData = new HashMap<>();
        checkedItems = new ArrayList<>();
        if (shoppingList != null) {
            for(ShoppingListItem item : shoppingList.getItems()){
                checkedItems.add(item.getProduct().getName());
            }
        }
        for(ProductGroup productGroup : _listDataHeader){
            listChildData.put(productGroup.getName(), productGroup.getProducts());
        }
        this._listDataChild = listChildData;
    }

    @Override
    public Object getChild(int groupPosition, int childPosititon) {
        return this._listDataChild.get(this._listDataHeader.get(groupPosition).getName())
                .get(childPosititon);
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public View getChildView(int groupPosition, final int childPosition,
                             boolean isLastChild, View convertView, ViewGroup parent) {
        View itemView = convertView;
        final Product product = ((Product) getChild(groupPosition, childPosition));

        final ViewHolder vh;
        if (itemView == null) {
            LayoutInflater infalInflater = (LayoutInflater) this.context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            itemView = infalInflater.inflate(R.layout.product_list_item, null);
            vh = new ViewHolder(itemView);
            itemView.setTag(vh);
        } else {
            vh = (ViewHolder)itemView.getTag();
        }


        vh.txtListChild.setText(product.getName());

        if(shoppingList != null){
            AppLog.d(TAG, "getChildView", "fromShoppingList");
            vh.checkBox.setVisibility(View.VISIBLE);
            vh.checkBox.setChecked(checkedItems.contains(product.getName()));
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(!vh.checkBox.isChecked()){
                        AppLog.d(TAG, "onCheckedChanged", "checked");
                        ShoppingListItem item = new ShoppingListItem(product);
                        item.save(application);
                        shoppingList.addItem(item);
                        shoppingList.save(application);
                        vh.checkBox.setChecked(true);
                        checkedItems.add(product.getName());
                        //vh.itemView.setTag("checked");
                    } else {
                        AppLog.d(TAG, "onCheckedChanged", "uncheked");
                        int position = -1;
                        for(int i = 0; i<shoppingList.getItems().size(); i++){
                            if(product.getName().equals(shoppingList.getItems().get(i).getProduct().getName())){
                                position = i;
                            }
                        }
                        if (position>=0) {
                            shoppingList.removeItemAtPosition(position);
                        }
                        vh.checkBox.setChecked(false);
                        checkedItems.remove(product.getName());
                        //vh.itemView.setTag("unchecked");
                    }
                }
            });
//            checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
//                @Override
//                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//                    AppLog.d(TAG, "onCheckedChanged", "product: "+product.getName());
//                    if (isChecked) {
//                        AppLog.d(TAG, "onCheckedChanged", "checked");
//                        ShoppingListItem item = new ShoppingListItem(product);
//                        item.save(application);
//                        shoppingList.addItem(item);
//                        shoppingList.save(application);
//                    } else {
//                        AppLog.d(TAG, "onCheckedChanged", "uncheked");
//                        int position = -1;
//                        for(int i = 0; i<shoppingList.getItems().size(); i++){
//                            if(product.getName().equals(shoppingList.getItems().get(i).getProduct().getName())){
//                                position = i;
//                            }
//                        }
//                        if (position>=0) {
//                            shoppingList.removeItemAtPosition(position);
//                        }
//                    }
//                }
//            });
        } else {
            AppLog.d(TAG, "getChildView", "shoppingList is null");
            vh.checkBox.setVisibility(View.INVISIBLE);
        }

        return itemView;
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return this._listDataChild.get(this._listDataHeader.get(groupPosition).getName())
                .size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return this._listDataHeader.get(groupPosition);
    }

    @Override
    public int getGroupCount() {
        return this._listDataHeader.size();
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded,
                             View convertView, ViewGroup parent) {
        String headerTitle = ((ProductGroup) getGroup(groupPosition)).getName();
        if (convertView == null) {
            LayoutInflater infalInflater = (LayoutInflater) this.context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = infalInflater.inflate(R.layout.product_list_group_item, null);
        }

        TextView lblListHeader = (TextView) convertView
                .findViewById(R.id.lblListHeader);
        lblListHeader.setTypeface(null, Typeface.BOLD);
        lblListHeader.setText(headerTitle);

        return convertView;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }

    private class ViewHolder{
        TextView txtListChild;
        CheckBox checkBox;
        //View itemView;

        ViewHolder(View itemView) {
            //this.itemView = itemView;
            txtListChild = (TextView) itemView
                    .findViewById(R.id.lblListItem);

            checkBox = (CheckBox) itemView.findViewById(R.id.checkbox);
        }
    }
}
