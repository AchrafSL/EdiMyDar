package com.example.edimydar;

import android.app.Activity;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.edimydar.databinding.ActivityHomePageMainBinding;
import com.google.android.material.navigation.NavigationBarView;

public class HomePage_MAIN extends AppCompatActivity {
    ActivityHomePageMainBinding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        replaceFragment(new myDayFragment());

        binding = ActivityHomePageMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        binding.btmNavView.setItemIconTintList(null); // Disable icon tinting



        binding.btmNavView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();
                if (itemId == R.id.My_Day) {
                    replaceFragment(new myDayFragment());
                    return true;
                } else if (itemId == R.id.Tasks) {
                    replaceFragment(new TaskFragment());
                    return true;
                } else if (itemId == R.id.Ai_Help) {
                    replaceFragment(new AiFragment());
                    return true;
                }
                return false;
            }
        });
    }

    private void replaceFragment(Fragment fragment) {
        FragmentManager fragmentManager= getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frameLayout,fragment);
        fragmentTransaction.commit();
    }


}