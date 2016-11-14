package de.applicatum.shoprouter.ui;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import de.applicatum.shoprouter.R;
import de.applicatum.shoprouter.ui.fragments.ShoppingListDetailsFragment;


public class ShoppingListDetailsActivity extends AppCompatActivity {

    public static final String TAG = "ShoppingListDetailsActivity";
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shopping_list_details);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Bundle extras = getIntent().getExtras();

        try {
            ShoppingListDetailsFragment fragment = new ShoppingListDetailsFragment();
            fragment.setShoppingListId(extras.getString("shoppingListId"));
            startFragment(fragment, false, 0);
        } catch (NullPointerException e) {
            e.printStackTrace();
            finish();
        }
    }

    public void setTitle(String title){
        toolbar.setTitle(title);
    }

    public void startFragment(Fragment fragment, boolean addToBackStack, int appearance) {



        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        if(appearance == 0){
            //fragmentTransaction.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left);
        }
        if(appearance == 1){
            //fragmentTransaction.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left, android.R.anim.slide_in_left, android.R.anim.slide_out_right);
        }
        if(appearance == -1){
            //fragmentTransaction.setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right, R.anim.slide_in_right, R.anim.slide_out_left);
        }

        fragmentTransaction.replace(R.id.content_main, fragment);

        if(addToBackStack) {
            fragmentTransaction.addToBackStack("frame");
        }
        else {
            fragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        }

        fragmentTransaction.commit();
    }
}
