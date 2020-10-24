package com.jeevan.whatsapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.jeevan.whatsapp.Activities.LoginActivity;
import com.jeevan.whatsapp.Activities.LoginPhoneNumberActivity;
import com.jeevan.whatsapp.Activities.RegisterAccount;
import com.jeevan.whatsapp.Activities.SettingActivity;
import com.jeevan.whatsapp.Data.UserProfile;
import com.jeevan.whatsapp.Fragments.ChatFragment;
import com.jeevan.whatsapp.Fragments.ContactFragment;
import com.jeevan.whatsapp.Fragments.GroupFragment;
import com.jeevan.whatsapp.Fragments.MainFragmentAdapter;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    /**
     * The pager widget, which handles animation and allows swiping horizontally to access previous
     * and next wizard steps.
     */
    private ViewPager2 viewPager;

    /**
     * The pager adapter, which provides the pages to the view pager widget.
     */
    private FragmentStateAdapter pagerAdapter;

    /**
     * Firebase Firestore setup
     */

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    private FirebaseUser currentUser = firebaseAuth.getCurrentUser();
    private FirebaseAuth.AuthStateListener authStateListener;

    /**
     * Firebase Firestore
     */


    /**
     * Tablayout in  main pager
     */
    private TabLayout tabLayout;

    //main app toolbar
    private Toolbar mToolbar;

    //Fragment list for slide main page fragments
    private List<Fragment> fragmentList;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initializeViewsVariables();
        setUpFragment();
        setUpToolBar();
        setUpAuthStateListener();
    }

    private void setUpAuthStateListener() {
        authStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if(firebaseAuth.getCurrentUser() == null)
                {
                    //no user is login
                    sendToLogin();
                    finish();
                }else if (firebaseAuth.getCurrentUser() != null)
                {
                    db.collection("Users").document(firebaseAuth.getCurrentUser().getUid())
                            .get()
                            .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                    if(task.isSuccessful())
                                    {
                                        DocumentSnapshot documentSnapshot = task.getResult();
                                        if(!documentSnapshot.exists())
                                        {
                                           createUserData();
                                        }
                                    }
                                }
                            });
                }
            }
        };
    }

    @Override
    protected void onStart() {
        super.onStart();
        firebaseAuth.addAuthStateListener(authStateListener);
    }

    private void initializeViewsVariables() {

    }

    private void createUserData()
    {
        db.collection("Users").document(firebaseAuth.getCurrentUser().getUid())
                .set(new UserProfile())
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "onSuccess: Success user id is created ");

                        /**
                         * send to setting actvity to fill user info if user if first time
                         * here signed in
                         */
                        sendToSettingActivity();
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d(TAG, "onFailure: Fail to create user id in Users document");
            }
        });
    }

    private void sendToSettingActivity()
    {
        startActivity(new Intent(MainActivity.this, SettingActivity.class));
    }

    private void setUpToolBar() {
        //main app bar layout
        mToolbar = findViewById(R.id.main_page_appbar);
        setSupportActionBar(mToolbar);
        mToolbar.setTitle("WhatsApp");

    }

    private void setUpFragment() {
        //Fragment setup
        fragmentList = new ArrayList<>();
        fragmentList.add(new ContactFragment());
        fragmentList.add(new ChatFragment());
        fragmentList.add(new GroupFragment());

        // Instantiate a ViewPager2 and a PagerAdapter.
        viewPager = findViewById(R.id.main_page_viewpager2);
        pagerAdapter = new MainFragmentAdapter(this,fragmentList);
        viewPager.setAdapter(pagerAdapter);

        //integrating the TabLayout and view Pager here
        tabLayout = findViewById(R.id.main_page_tab_layout);
        new TabLayoutMediator(tabLayout, viewPager , new TabLayoutMediator.TabConfigurationStrategy() {
            @Override
            public void onConfigureTab(@NonNull TabLayout.Tab tab, int position)
            {
                if(position == 0)
                {
                    tab.setText(R.string.contact_label);
                }else if(position == 1)
                {
                    tab.setText(R.string.chat_label);
                }else if(position == 2)
                {
                    tab.setText(R.string.group_label);
                }

            }
        }).attach();
    }

    private void updateUI(FirebaseUser user)
    {
        //TODO update UI
    }

    private void sendToLogin()
    {
        sendToNumberLogin();
    }

    private void sendToNumberLogin() {
        startActivity(new Intent(MainActivity.this, LoginPhoneNumberActivity.class));
    }

    private void sendToEmailLogin() {
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        startActivity(intent);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId())
        {
            case R.id.setting : settingActivity();
            break;
            case R.id.logout : logout();
            break;
            default:break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void logout() {
        Log.d(TAG, "logout: called ");
        firebaseAuth.signOut();
    }

    private void settingActivity() {
        startActivity(new Intent(MainActivity.this, SettingActivity.class));
    }

    //gradient background for status bar
    public void setGradientStatusBar(Activity activity)
    {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
        {
           Window window = activity.getWindow();
            Drawable background = activity.getResources().getDrawable(R.drawable.app_bar_gradient_theme);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(activity.getResources().getColor(android.R.color.transparent));
            window.setNavigationBarColor(activity.getResources().getColor(android.R.color.transparent));
            window.setBackgroundDrawable(background);
        }
    }

    @Override
    public void onBackPressed() {
        if (viewPager.getCurrentItem() == 0) {
            // If the user is currently looking at the first step, allow the system to handle the
            // Back button. This calls finish() on this activity and pops the back stack.
            super.onBackPressed();
        } else {
            // Otherwise, select the previous step.
            viewPager.setCurrentItem(viewPager.getCurrentItem() - 1);
        }
    }
}