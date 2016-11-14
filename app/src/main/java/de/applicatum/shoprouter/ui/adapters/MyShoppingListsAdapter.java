package de.applicatum.shoprouter.ui.adapters;


import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import java.util.ArrayList;

import de.applicatum.shoprouter.R;
import de.applicatum.shoprouter.model.Products.ProductsController;
import de.applicatum.shoprouter.model.Products.ShoppingList;
import de.applicatum.shoprouter.ui.MainActivity;
import de.applicatum.shoprouter.ui.ShoppingListDetailsActivity;
import de.applicatum.shoprouter.utils.AppLog;

public class MyShoppingListsAdapter extends RecyclerView.Adapter{

    public static final String TAG = "MyShoppingListsAdapter";

    private MainActivity activity;
    private ArrayList<ShoppingList> mDataset;

    public static class ViewHolder extends RecyclerView.ViewHolder {

        public TextView mTextView;
        public View v;

        public ViewHolder(View v) {
            super(v);
            this.v = v;
            mTextView = (TextView) v.findViewById(R.id.text);
        }
    }

    public MyShoppingListsAdapter(ArrayList<ShoppingList> mDataset, MainActivity activity) {
        this.mDataset = mDataset;
        this.activity = activity;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                   int viewType) {
        // create a new view
        AppLog.d(TAG, "onCreateViewHolder", "load layout");
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.shopping_lists_list_item_layout, parent, false);
        // set the view's size, margins, paddings and layout parameters
        //TextView textView = (TextView) v.findViewById(R.id.text);
        AppLog.d(TAG, "onCreateViewHolder", "load textview");
        //CheckBox checkBox = (CheckBox) v.findViewById(R.id.checkbox);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        AppLog.d(TAG, "onBindViewHolder", "create holder");
        final ViewHolder viewHolder = (ViewHolder) holder;
        AppLog.d(TAG, "onBindViewHolder", "set text");
        viewHolder.mTextView.setText(mDataset.get(position).getName());
        AppLog.d(TAG, "onBindViewHolder", "text is: "+ mDataset.get(position).getName());
        viewHolder.v.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(activity, ShoppingListDetailsActivity.class);
                intent.putExtra("shoppingListId", mDataset.get(position).getId());
                activity.startActivity(intent);
            }
        });
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
