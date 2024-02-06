package com.example.auto_accounting;

import androidx.annotation.NonNull;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.annotation.SuppressLint;

import android.app.PendingIntent;
import android.content.Context;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import androidx.appcompat.widget.Toolbar;
import com.google.android.material.bottomappbar.BottomAppBar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationBarView;
import com.google.android.material.navigation.NavigationView;

import java.util.List;



public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout drawerLayout;
    private BottomNavigationView bottomNavigationView;
    private FragmentManager fragmentManager;
    private Toolbar toolbar;
    private DBHelper dbHelper;

    @SuppressLint({"MissingInflatedId", "ResourceType"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        dbHelper = new DBHelper(this);

        FloatingActionButton fab = findViewById(R.id.fab);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawerLayout = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.open_nav, R.string.close_nav);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setBackground(null);
        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();
                if (itemId == R.id.note) {
                    replaceFragment(new NoteFragment(),true,true);
                } else if (itemId == R.id.refill) {
                    replaceFragment(new RefillFragment(),true,true);
                } else if (itemId == R.id.service) {
                    replaceFragment(new ServiceFragment(),true, true);
                } else if (itemId == R.id.other) {
                    replaceFragment(new OtherFragment(),true, true);
                }
                return true;
            }
        });
        fragmentManager = getSupportFragmentManager();
        replaceFragment(new AddTsFragment(),false,false);
        setBottomAppBarVisibility(false);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Создаем Intent для запуска StatisticsActivity
                Intent intent = new Intent(MainActivity.this, StatisticsActivity.class);

                // Запускаем StatisticsActivity
                startActivity(intent);
            }
        });

        // Получение последнего вставленного идентификатора транспортного средства из базы данных
        long lastInsertedVehicleId = dbHelper.getLastInsertedVehicleId();

        // Если идентификатор существует, откройте CustomFragment с этим идентификатором
        if (lastInsertedVehicleId != -1) {
            openCustomFragmentWithVehicleId(lastInsertedVehicleId);
        } else {
            // Иначе AddTsFragment
            fragmentManager = getSupportFragmentManager();
            replaceFragment(new AddTsFragment(), false, false);
            setBottomAppBarVisibility(false);
        }

    }
    @SuppressLint("Range")
    private void openCustomFragmentWithVehicleId(long vehicleId) {
        // Получаем данные из базы данных на основе vehicleId
        DBHelper dbHelper = new DBHelper(this);
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(
                DBHelper.TABLE_VEHICLES,
                new String[]{DBHelper.COLUMN_TRANSPORT_TYPE, DBHelper.COLUMN_BRAND, DBHelper.COLUMN_MODEL, DBHelper.COLUMN_VEHICLES_FUEL_TYPE, DBHelper.COLUMN_YEAR},
                DBHelper.COLUMN_VEHICLE_ID + " = ?",
                new String[]{String.valueOf(vehicleId)},
                null, null, null
        );

        String transportType = "";
        String brand = "";
        String model = "";
        String vehicles_fuel_type = "";
        String year = "";

        if (cursor != null && cursor.moveToFirst()) {
            transportType = cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_TRANSPORT_TYPE));
            brand = cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_BRAND));
            model = cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_MODEL));
            vehicles_fuel_type = cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_VEHICLES_FUEL_TYPE));
            year = cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_YEAR));
            cursor.close();
        }

        db.close();

        // Создаем Bundle с полученными данными
        Bundle bundle = createBundleFromVehicleId(vehicleId, transportType, brand, model, vehicles_fuel_type, year);

        // Передаем Bundle в фрагмент
        CustomFragment customFragment = CustomFragment.newInstance(vehicleId, transportType, brand, model, vehicles_fuel_type, year);
        customFragment.setArguments(bundle);

        // Заменяем фрагмент на CustomFragment
        replaceFragment(customFragment, true, true);

        // Обновляем меню навигации после открытия фрагмента
        updateTransportMenuInNavigationView();
    }

    // Вспомогательный метод для создания Bundle из данных о транспортном средстве
    private Bundle createBundleFromVehicleId(long vehicleId, String transportType, String brand, String model, String vehicles_fuel_type, String year) {
        Bundle bundle = new Bundle();
        bundle.putLong("vehicleId", vehicleId);
        bundle.putString("transportType", transportType);
        bundle.putString("brand", brand);
        bundle.putString("model", model);
        bundle.putString("vehicles_fuel_type", vehicles_fuel_type);
        bundle.putString("year", year);
        return bundle;
    }
    @Override
    protected void onDestroy() {
        // Закрываем базу данных при завершении активности
        dbHelper.close();
        super.onDestroy();
    }

    // Метод для замены текущего фрагмента
    public void replaceFragment(Fragment fragment, boolean showBottomAppBar, boolean showFab) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frame_layout, fragment);
        fragmentTransaction.commit();

        // Управление видимостью BottomAppBar и FloatingActionButton
        setBottomAppBarVisibility(showBottomAppBar);
        setFabVisibility(showFab);
    }


    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();

        // Проверка, является ли выбранный элемент транспортным средством
        if (item.getGroupId() == R.id.transportSubMenuGroup) {
            // Извлеките идентификатор ТС из атрибута MenuItem
            long vehicleId = item.getActionView().getId();

            // Передайте идентификатор ТС в ваш фрагмент
            openCustomFragmentWithVehicleId(vehicleId);
        } else if (itemId == R.id.add_vehicle) {
            // Откройте AddTsFragment
            replaceFragment(new AddTsFragment(), false, false);
        } else {
            Log.e("MainActivity", "transportSubMenuGroup ошибка");
        }

        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    // Метод для обновления списка транспортных средств в меню

    void updateTransportMenuInNavigationView() {
        NavigationView navigationView = findViewById(R.id.nav_view);
        Menu menu = navigationView.getMenu();
        SubMenu transportSubMenuGroup = menu.findItem(R.id.transportSubMenuGroup).getSubMenu();

        // Очистите все элементы меню в подменю
        transportSubMenuGroup.clear();

        // Получите данные о всех транспортных средствах из базы данных
        DBHelper dbHelper = new DBHelper(this);
        List<String> allTransportItems = dbHelper.getAllTransportItems();

        int itemId = Menu.FIRST;
        for (String transportItem : allTransportItems) {
            MenuItem menuItem = transportSubMenuGroup.add(R.id.transportSubMenuGroup, Menu.NONE, Menu.NONE, transportItem)
                    .setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem item) {
                            // Обработайте нажатие на пункт меню здесь
                            String selectedTransportItem = item.getTitle().toString();
                            String transport_type = dbHelper.getTransportTypeForTransportItem(selectedTransportItem);
                            String brand = dbHelper.getBrandForTransportItem(selectedTransportItem);
                            String model = dbHelper.getModelForTransportItem(selectedTransportItem);
                            String vehicles_fuel_type = dbHelper.getVehiclesFuelTypeForTransportItem(selectedTransportItem);
                            String year = dbHelper.getYearForTransportItem(selectedTransportItem);

                            long vehicleId = dbHelper.getVehicleIdForTransportItem(brand, model, transport_type, vehicles_fuel_type,year);

                            SharedPreferences preferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
                            SharedPreferences.Editor editor = preferences.edit();
                            editor.putLong("lastInsertedVehicleId", vehicleId);
                            editor.apply();
                            // Сохраните идентификатор ТС в атрибуте MenuItem
                            item.setActionView(new View(MainActivity.this));
                            item.getActionView().setId((int) vehicleId);

                            Bundle bundle = new Bundle();
                            bundle.putLong("vehicleId", vehicleId);
                            bundle.putString("transportType", transport_type);
                            bundle.putString("brand", brand);
                            bundle.putString("model", model);
                            bundle.putString("vehicles_fuel_type", vehicles_fuel_type);
                            bundle.putString("year", year);

                            CustomFragment customFragment = CustomFragment.newInstance(vehicleId,transport_type, brand, model, vehicles_fuel_type,year);
                            customFragment.setArguments(bundle);

                            replaceFragment(customFragment, true, true);
                            return true;
                        }
                    });

            // Сохраните идентификатор ТС в атрибуте MenuItem
            menuItem.setActionView(new View(MainActivity.this));
            menuItem.getActionView().setId(itemId);

            itemId++;
        }
    }
    @Override
    protected void onResume() {
        super.onResume();
        updateTransportMenuInNavigationView();
    }

    private void setBottomAppBarVisibility(boolean isVisible) {
        BottomAppBar bottomAppBar = findViewById(R.id.bottomAppBar);
        if (isVisible) {
            bottomAppBar.setVisibility(View.VISIBLE);
        } else {
            bottomAppBar.setVisibility(View.GONE);
        }
    }
    private void setFabVisibility(boolean isVisible) {
        FloatingActionButton fab = findViewById(R.id.fab);
        if (isVisible) {
            fab.setVisibility(View.VISIBLE);
        } else {
            fab.setVisibility(View.GONE);
        }
    }

}
