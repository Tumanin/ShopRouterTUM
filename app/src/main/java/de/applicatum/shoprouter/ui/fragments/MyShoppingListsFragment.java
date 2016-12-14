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
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import com.couchbase.lite.CouchbaseLiteException;
import com.couchbase.lite.Document;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.applicatum.shoprouter.Application;
import de.applicatum.shoprouter.R;
import de.applicatum.shoprouter.model.Products.ProductsController;
import de.applicatum.shoprouter.model.Products.ShoppingList;
import de.applicatum.shoprouter.ui.MainActivity;
import de.applicatum.shoprouter.ui.adapters.MyShoppingListsAdapter;
import de.applicatum.shoprouter.utils.AppLog;

public class MyShoppingListsFragment extends Fragment {

    public static final String TAG = "MyShoppingListsFragment";

    private View rootView;
    private MainActivity mActivity;

    @BindView(R.id.recycler)RecyclerView recyclerView;

    private MyShoppingListsAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    private ArrayList<ShoppingList> shoppingLists;

    public MyShoppingListsFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        mActivity = (MainActivity) getActivity();
        rootView = inflater.inflate(R.layout.fragment_my_shopping_lists, container, false);
        ButterKnife.bind(this, rootView);

        mLayoutManager = new LinearLayoutManager(mActivity);
        if (recyclerView != null) {
            recyclerView.setLayoutManager(mLayoutManager);

            shoppingLists = ProductsController.getInstance().getShoppingLists();
            mAdapter = new MyShoppingListsAdapter(shoppingLists, mActivity);
            recyclerView.setAdapter(mAdapter);

            setUpItemTouchHelper();
            setUpAnimationDecoratorHelper();
        } else {
            AppLog.d(TAG, "onCreateView", "recyclerView is null");
        }

        FloatingActionButton fab = (FloatingActionButton) rootView.findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
                displayCreateDialog();
            }
        });

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        mActivity.setTitle("Meine Einkaufslisten");
    }

    private void displayCreateDialog() {
        AlertDialog.Builder alert = new AlertDialog.Builder(mActivity);
        alert.setTitle("Neue Einkaufsliste");

        LayoutInflater inflater = mActivity.getLayoutInflater();
        final View view = inflater.inflate(R.layout.view_dialog_input, null);
        final EditText input = (EditText) view.findViewById(R.id.text);
        alert.setView(view);

        alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                try {
                    String title = input.getText().toString();
                    if (title.length() == 0)
                        return;
                    if(ProductsController.getInstance().checkShoppingListName(title))
                        create(title);
                    else Toast.makeText(mActivity, "Der Name existiert schon!", Toast.LENGTH_LONG).show();
                } catch (CouchbaseLiteException e) {
                    AppLog.d(TAG, "displayCreateDialog", "Cannot create");
                }
            }
        });

        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) { }
        });

        alert.show();
    }

    private void create(String title) throws CouchbaseLiteException {

        Application application = (Application) mActivity.getApplication();
        ShoppingList shoppingList = new ShoppingList(title);
        shoppingList.save(application);
        ProductsController.getInstance().addShoppingList(shoppingList);
        AppLog.d(TAG, "create", "notifyDataSetChanged");
        mAdapter.notifyDataSetChanged();
        AppLog.d(TAG, "create", "notifyDataSetChanged done");
    }

    private void deleteShoppingList(ShoppingList shoppingList){
        ProductsController.getInstance().deleteShoppingList((Application)mActivity.getApplication(), shoppingList);
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
                final MyShoppingListsAdapter adapter = (MyShoppingListsAdapter)recyclerView.getAdapter();
                final ShoppingList shoppingList = (ShoppingList) adapter.getItem(swipedPosition);
                final AlertDialog.Builder builder = new AlertDialog.Builder(
                        mActivity);
                builder.setMessage(
                        String.format(getResources().getString(
                                R.string.dialog_unlink_question), shoppingList.getName()))
                        .setPositiveButton(
                                getResources().getString(R.string.dialog_btn_dialog_yes),
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        deleteShoppingList(shoppingList);
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
