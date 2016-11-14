package de.applicatum.shoprouter.ui.fragments;


import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.applicatum.shoprouter.Application;
import de.applicatum.shoprouter.R;
import de.applicatum.shoprouter.model.Products.ProductsController;
import de.applicatum.shoprouter.model.Products.ShoppingList;
import de.applicatum.shoprouter.model.Products.ShoppingListItem;
import de.applicatum.shoprouter.ui.ShoppingListDetailsActivity;
import de.applicatum.shoprouter.ui.adapters.ShoppingListDetailsAdapter;
import de.applicatum.shoprouter.utils.AppLog;

public class ShoppingListDetailsFragment extends Fragment {

    public static final String TAG = "ShoppingListDetailsFragment";
    @BindView(R.id.buttonAdd)
    Button buttonAdd;
    @BindView(R.id.recyclerView)
    RecyclerView recyclerView;

    private String shoppingListId;
    private View rootView;
    private ShoppingListDetailsActivity mActivity;
    private ArrayList<ShoppingListItem> items;

    ShoppingListDetailsAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    public ShoppingListDetailsFragment() {
    }

    public void setShoppingListId(String shoppingListId) {
        this.shoppingListId = shoppingListId;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        AppLog.d(TAG, "onCreateView", "start");
        mActivity = (ShoppingListDetailsActivity) getActivity();
        rootView = inflater.inflate(R.layout.fragment_shopping_list_details, container, false);
        ButterKnife.bind(this, rootView);
        AppLog.d(TAG, "onCreateView", "inflate done");
        items = ProductsController.getInstance().findShoppingList(shoppingListId).getItems();

        mLayoutManager = new LinearLayoutManager(mActivity);
        AppLog.d(TAG, "onCreateView", "LinearLayoutManager done");
        recyclerView.setLayoutManager(mLayoutManager);
        mAdapter = new ShoppingListDetailsAdapter(items);
        AppLog.d(TAG, "onCreateView", "ShoppingListDetailsAdapter done");
        recyclerView.setAdapter(mAdapter);
        AppLog.d(TAG, "onCreateView", "end");

        setUpItemTouchHelper();
        setUpAnimationDecoratorHelper();

        return rootView;
    }


    @OnClick(R.id.buttonAdd)
    public void onClick() {
        ProductListFragment fragment = new ProductListFragment();
        fragment.setFromShoppingList(true);
        fragment.setShoppingList(ProductsController.getInstance().findShoppingList(shoppingListId));
        mActivity.startFragment(fragment, true, 0);
    }

    @Override
    public void onResume() {
        super.onResume();
        AppLog.d(TAG, "onResume", "items size: "+items.size());
        mActivity.setTitle(ProductsController.getInstance().findShoppingList(shoppingListId).getName());
        mAdapter.notifyDataSetChanged();
    }

    private void deleteShoppingListItem(ShoppingListItem shoppingListItem){
        ProductsController.getInstance().findShoppingList(shoppingListId).removeItem(shoppingListItem);
        ProductsController.getInstance().findShoppingList(shoppingListId).save((Application)mActivity.getApplication());
    }

    private void setUpItemTouchHelper() {

        ItemTouchHelper.SimpleCallback simpleItemTouchCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {

            // we want to cache these and not allocate anything repeatedly in the onChildDraw method
            Drawable background;
            int textMargin;
            boolean initiated;

            private void init() {
                background = new ColorDrawable(Color.RED);
                textMargin = (int) mActivity.getResources().getDimension(R.dimen.activity_horizontal_margin);
                initiated = true;
            }

            // not important, we don't want drag & drop
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public int getSwipeDirs(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
                return super.getSwipeDirs(recyclerView, viewHolder);
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int swipeDir) {
                final int swipedPosition = viewHolder.getAdapterPosition();
                final ShoppingListDetailsAdapter adapter = (ShoppingListDetailsAdapter)recyclerView.getAdapter();
                final ShoppingListItem shoppingListItem = (ShoppingListItem) adapter.getItem(swipedPosition);
                final AlertDialog.Builder builder = new AlertDialog.Builder(
                        mActivity);
                builder.setMessage(
                        String.format(getResources().getString(
                                R.string.dialog_unlink_question), shoppingListItem.getProduct().getName()))
                        .setPositiveButton(
                                getResources().getString(R.string.dialog_btn_dialog_yes),
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        deleteShoppingListItem(shoppingListItem);
                                        adapter.remove(swipedPosition);
                                    }
                                })
                        .setNegativeButton(
                                getResources().getString(R.string.dialog_btn_dialog_no),
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        dialog.cancel();
                                        adapter.notifyDataSetChanged();
                                    }
                                });
                final AlertDialog alert = builder.create();
                alert.show();


            }

            @Override
            public void onChildDraw(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
                View itemView = viewHolder.itemView;

                // not sure why, but this method get's called for viewholder that are already swiped away
                if (viewHolder.getAdapterPosition() == -1) {
                    // not interested in those
                    return;
                }

                if (!initiated) {
                    init();
                }

                // draw red background
                background.setBounds(itemView.getRight() + (int) dX, itemView.getTop(), itemView.getRight(), itemView.getBottom());
                background.draw(c);

                // draw "disconnect"
                int itemHeight = itemView.getBottom() - itemView.getTop();

                Paint paintTextX = new Paint();
                paintTextX.setStyle(Paint.Style.FILL);
                paintTextX.setColor(ContextCompat.getColor(mActivity, R.color.full_white));
                //paintTextX.setTypeface(TypefaceCache.get(getActivity().getAssets(), "fonts/Lato-Regular.ttf"));

                paintTextX.setTextSize(32);
                paintTextX.setAntiAlias(true);
                String disconnect = getResources().getString(R.string.text_disconnect);
                Rect boundsX = new Rect();
                paintTextX.getTextBounds(disconnect, 0 ,disconnect.length(), boundsX);

                int textLeft = itemView.getRight() - textMargin - boundsX.width();
                int textTop = itemView.getTop() + itemHeight/2 + boundsX.height()/2;

                c.drawText(disconnect, textLeft, textTop, paintTextX);

                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            }

        };
        ItemTouchHelper mItemTouchHelper = new ItemTouchHelper(simpleItemTouchCallback);
        mItemTouchHelper.attachToRecyclerView(recyclerView);
    }

    private void setUpAnimationDecoratorHelper() {
        recyclerView.addItemDecoration(new RecyclerView.ItemDecoration() {

            // we want to cache this and not allocate anything repeatedly in the onDraw method
            Drawable background;
            Drawable mDivider;
            boolean initiated;

            private void init() {
                background = new ColorDrawable(Color.RED);
                mDivider = ContextCompat.getDrawable(mActivity, R.drawable.divider_item_decoration);
                initiated = true;
            }

            @Override
            public void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state) {

                if (!initiated) {
                    init();
                }

                // only if animation is in progress
                if (parent.getItemAnimator().isRunning()) {

                    // some items might be animating down and some items might be animating up to close the gap left by the removed item
                    // this is not exclusive, both movement can be happening at the same time
                    // to reproduce this leave just enough items so the first one and the last one would be just a little off screen
                    // then remove one from the middle

                    // find first child with translationY > 0
                    // and last one with translationY < 0
                    // we're after a rect that is not covered in recycler-view views at this point in time
                    View lastViewComingDown = null;
                    View firstViewComingUp = null;

                    // this is fixed
                    int left = 0;
                    int right = parent.getWidth();

                    // this we need to find out
                    int top = 0;
                    int bottom = 0;

                    // find relevant translating views
                    int childCount = parent.getLayoutManager().getChildCount();
                    for (int i = 0; i < childCount; i++) {
                        View child = parent.getLayoutManager().getChildAt(i);
                        if (child.getTranslationY() < 0) {
                            // view is coming down
                            lastViewComingDown = child;
                        } else if (child.getTranslationY() > 0) {
                            // view is coming up
                            if (firstViewComingUp == null) {
                                firstViewComingUp = child;
                            }
                        }
                    }

                    if (lastViewComingDown != null && firstViewComingUp != null) {
                        // views are coming down AND going up to fill the void
                        top = lastViewComingDown.getBottom() + (int) lastViewComingDown.getTranslationY();
                        bottom = firstViewComingUp.getTop() + (int) firstViewComingUp.getTranslationY();
                    } else if (lastViewComingDown != null) {
                        // views are going down to fill the void
                        top = lastViewComingDown.getBottom() + (int) lastViewComingDown.getTranslationY();
                        bottom = lastViewComingDown.getBottom();
                    } else if (firstViewComingUp != null) {
                        // views are coming up to fill the void
                        top = firstViewComingUp.getTop();
                        bottom = firstViewComingUp.getTop() + (int) firstViewComingUp.getTranslationY();
                    }

                    background.setBounds(left, top, right, bottom);
                    background.draw(c);



                }
                super.onDraw(c, parent, state);

                int dividerleft = parent.getPaddingLeft();
                int dividerright = parent.getWidth() - parent.getPaddingRight();
                int childCount = parent.getLayoutManager().getChildCount();
                for (int i = 0; i < childCount; i++) {
                    View child = parent.getLayoutManager().getChildAt(i);

                    RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child.getLayoutParams();

                    int dividertop = child.getBottom() + params.bottomMargin;
                    int dividerbottom = dividertop + mDivider.getIntrinsicHeight();

                    mDivider.setBounds(dividerleft, dividertop, dividerright, dividerbottom);
                    mDivider.draw(c);
                }
            }

        });
    }
}
