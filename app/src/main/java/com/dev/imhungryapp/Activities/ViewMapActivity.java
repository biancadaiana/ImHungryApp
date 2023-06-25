package com.dev.imhungryapp.Activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import android.os.Bundle;
import android.view.View;

import com.dev.imhungryapp.Fragments.MapFragment;
import com.dev.imhungryapp.R;

public class ViewMapActivity extends AppCompatActivity {

    DrawerLayout drawerLayout;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_map);

        drawerLayout = findViewById(R.id.drawer);

        Fragment fragment = new MapFragment();
        getSupportFragmentManager().beginTransaction().replace(R.id.frame_layout, fragment).commit();

    }


    public void ClickMenu(View view){
        MainActivity.openDrawer(drawerLayout);
    }
    public void ClickLogo(View view){
        MainActivity.closeDrawer(drawerLayout);
    }
    public void Clickhome(View view){
        MainActivity.redirectActivity(this, MainActivity.class);
    }
    public void ClickSettings(View view){
        MainActivity.redirectActivity(this, SettingsActivity.class);
    }
    public void ClickMap(View view){
        recreate();

    }
    public void ClickFavourites(View view){
        MainActivity.redirectActivity(this, FavouritesActivity.class);
    }
    public void ClickMycart(View view){
        MainActivity.redirectActivity(this, MyCartActivity.class);
    }
    public void ClickMyorders(View view){
        MainActivity.redirectActivity(this, MyOrdersActivity.class);
    }
    public void ClickLogout(View view){
        //MainActivity.redirectActivity(this, SigninActivity.class);
        MainActivity.logout(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        MainActivity.closeDrawer(drawerLayout);
    }
}