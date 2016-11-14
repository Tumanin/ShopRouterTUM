package de.applicatum.shoprouter.ui.adapters;


import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import java.util.ArrayList;

import de.applicatum.shoprouter.R;
import de.applicatum.shoprouter.model.Products.ShoppingListItem;
import de.applicatum.shoprouter.utils.AppLog;

public class ShoppingListDetailsAdapter extends RecyclerView.Adapter<ShoppingListDetailsAdapter.ViewHolder>{

    public static final String TAG = "ShoppingListDetailsAdapter";

    private ArrayList<ShoppingListItem> mDataset;

    public static class ViewHolder extends RecyclerView.ViewHolder {

        public TextView mTextView;
        public CheckBox mCheckbox;
        public ViewHolder(View v) {
            super(v);
            mTextView = (TextView) v.findViewById(R.id.text);
            mCheckbox = (CheckBox) v.findViewById(R.id.checkbox);
        }
    }

    public ShoppingListDetailsAdapter(ArrayList<ShoppingListItem> mDataset) {
        this.mDataset = mDataset;
    }

    @Override
    public ShoppingListDetailsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                                int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.shopping_list_item_layout, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        holder.mTextView.setText(mDataset.get(position).getProduct().getName());
        holder.mCheckbox.setChecked(mDataset.get(position).isBought());
    }

    @Override
    public int getItemCount() {
        return mDataset.size();
    }

    public Object getItem(int position) {
        return mDataset.get(position);
    }

    public void remove(int position) {
        AppLog.d(TAG, "remove", "position: "+position);
        AppLog.d(TAG, "remove", "dataset before: "+mDataset.size());
        //mDataset.remove(position);
        AppLog.d(TAG, "remove", "dataset after: "+mDataset.size());
        //mDataset.clear();
        //mDataset.addAll(ProductsController.getInstance().getShoppingLists());
        //notifyItemRemoved(position);
        notifyDataSetChanged();
    }
}
