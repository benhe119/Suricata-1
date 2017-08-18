package minux.suricata;

import android.app.SearchManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends BaseActivity {
    private ViewPager viewPager;
    private SearchView searchView;
    private MenuItem menuItemOption;
    private DatabaseReference databaseReference;
    private ValueEventListener velState = new ValueEventListener() { // 현재 상태(state)를 파악하는 리스너
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            state = (String) dataSnapshot.getValue();
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    };
    private String state;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_main);
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        TabLayout tabLayout = (TabLayout) findViewById(R.id.tablayout);
        tabLayout.addTab(tabLayout.newTab().setText(R.string.detection));
        tabLayout.addTab(tabLayout.newTab().setText(R.string.system));
        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
        viewPager = (ViewPager) findViewById(R.id.viewpager);
        viewPager.setAdapter(new FragmentPagerAdapter(getSupportFragmentManager(), tabLayout.getTabCount()));
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));

        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().permitAll().build());

        databaseReference = FirebaseDatabase.getInstance().getReference();
        databaseReference.child("state").addValueEventListener(velState);
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        searchView = (SearchView) menu.findItem(R.id.menu_search).getActionView();
        searchView.setOnSearchClickListener(new SearchView.OnClickListener() {
            @Override
            public void onClick(View view) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                menuItemOption = menu.findItem(R.id.menu_option);
                menuItemOption.setVisible(false);
                searchView.setQueryHint("검색할 내용을 입력하세요.");
            }
        });
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() { // 엔터를 누르면 동작하는 리스너
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchView.clearFocus();
                searchView.setQuery("", false);
                searchView.setIconified(true);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
        searchView.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                getSupportActionBar().setDisplayHomeAsUpEnabled(false);
                invalidateOptionsMenu();
                return false;
            }
        });

        SearchManager searchManager = (SearchManager) getSystemService(SEARCH_SERVICE);
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                getSupportActionBar().setDisplayHomeAsUpEnabled(false);
                invalidateOptionsMenu();
                searchView.clearFocus();
                break;
            case R.id.menu_option_start:
                if (state.equals("ready")) {
                    state = "start";
                    databaseReference.child("state").setValue(state);
                } else {
                    Toast.makeText(MainActivity.this, "준비 상태가 아닙니다.", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.menu_option_stop:
                if (state.equals("detecting")) {
                    CharSequence listitem[] = new CharSequence[]{"탐지 중지", "서버 종료", "서버 재시작"};
                    new AlertDialog.Builder(this).setTitle("서버 동작 선택")
                            .setItems(listitem, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int which) {
                                    switch (which) {
                                        case 0:
                                            Toast.makeText(getApplicationContext(), "탐지를 중지합니다.", Toast.LENGTH_SHORT).show();
                                            state = "stop";
                                            break;
                                        case 1:
                                            Toast.makeText(getApplicationContext(), "서버를 종료합니다.", Toast.LENGTH_SHORT).show();
                                            state = "shutdown";
                                            break;
                                        case 2:
                                            Toast.makeText(getApplicationContext(), "서버를 재시작합니다.", Toast.LENGTH_SHORT).show();
                                            state = "reboot";
                                            break;
                                    }
                                    databaseReference.child("state").setValue(state);
                                }
                            }).show();
                } else {
                    Toast.makeText(MainActivity.this, "탐지중이 아닙니다.", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.menu_option_statistics:
                startActivity(new Intent(MainActivity.this, StatisticsActivity.class));
                break;
            case R.id.menu_option_setting:
                startActivity(new Intent(MainActivity.this, SettingActivity.class));
                break;
            case R.id.menu_option_logout:
                databaseReference.child("user").child(getUid()).child("logon").setValue("false");
                finish();
        }
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        databaseReference.child("state").addValueEventListener(velState);
    }

    @Override
    public void onBackPressed() {

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        databaseReference.child("state").removeEventListener(velState);
    }
}

class FragmentPagerAdapter extends FragmentStatePagerAdapter {
    private int NUM_ITEMS;

    FragmentPagerAdapter(FragmentManager fragmentManager, int NUM_ITEMS) {
        super(fragmentManager);
        this.NUM_ITEMS = NUM_ITEMS;
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return new DetectionFragment();
            case 1:
                return new SystemFragment();
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return NUM_ITEMS;
    }

    @Override
    public int getItemPosition(Object object) {
        return POSITION_NONE;
    }
}