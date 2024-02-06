package com.example.auto_accounting;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class AddTsFragment extends Fragment {

    private AutoCompleteTextView autoCompleteTypeTS, autoCompleteBrand, autoCompleteModel, autoCompleteFuel, autoCompleteYear;

    private DBHelper dbHelper;
    private List<String> allModels;
    private ArrayAdapter<String> modelAdapter;
    private static final String ARG_VEHICLE_ID = "vehicleId";
    private static final String ARG_BRAND = "brand";
    private static final String ARG_MODEL = "model";
    private static final String ARG_YEAR = "year";
    private static final String ARG_TRANSPORT_TYPE = "transportType";
    private static final String ARG_VEHICLES_FUEL_TYPE = "vehicles_fuel_type";


    public AddTsFragment() {
    }

    public static AddTsFragment newInstance(long vehicleId, String transportType, String brand, String model, String vehicles_fuel_type, String year) {
        AddTsFragment fragment = new AddTsFragment();
        Bundle args = new Bundle();
        args.putLong(ARG_VEHICLE_ID, vehicleId);
        args.putString(ARG_TRANSPORT_TYPE, transportType);
        args.putString(ARG_BRAND, brand);
        args.putString(ARG_MODEL, model);
        args.putString(ARG_VEHICLES_FUEL_TYPE, vehicles_fuel_type);
        args.putString(ARG_YEAR, year);

        Log.d("AddTsFragment", "newInstance: vehicleId = " + vehicleId);

        fragment.setArguments(args);
        return fragment;
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_ts, container, false);

        autoCompleteTypeTS = view.findViewById(R.id.typeTS);
        autoCompleteBrand = view.findViewById(R.id.brand);
        autoCompleteModel = view.findViewById(R.id.model);
        autoCompleteFuel = view.findViewById(R.id.fuel);
        autoCompleteYear = view.findViewById(R.id.year);
        Button buttonAddCar = view.findViewById(R.id.buttonAddCar);

        dbHelper = new DBHelper(requireContext());

        // Используйте ресурсы для заполнения автозаполнения
        setupAutoCompleteAdapter(autoCompleteTypeTS, Arrays.asList(getResources().getStringArray(R.array.transport_types)));

        autoCompleteTypeTS.setOnItemClickListener((parent, view1, position, id) -> {
            String selectedTransportType = (String) parent.getItemAtPosition(position);
            updateBrandsAndModelsBasedOnTransportType(selectedTransportType);
        });
        setupAutoCompleteAdapter(autoCompleteBrand, Arrays.asList(getResources().getStringArray(R.array.brands)));
        setupAutoCompleteAdapter(autoCompleteModel, Arrays.asList(getResources().getStringArray(R.array.models)));
        setupAutoCompleteAdapter(autoCompleteFuel, Arrays.asList(getResources().getStringArray(R.array.fuels)));
        setupAutoCompleteAdapter(autoCompleteYear, Arrays.asList(getResources().getStringArray(R.array.years)));

        // адаптеры для марок и моделей
        List<String> allBrands = dbHelper.getAllBrands();
        allModels = dbHelper.getAllModels();

        ArrayAdapter<String> brandAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, allBrands);
        modelAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, allModels);

        autoCompleteBrand.setAdapter(brandAdapter);
        autoCompleteModel.setAdapter(modelAdapter);

        autoCompleteBrand.setOnItemClickListener((parent, view1, position, id) -> {
            String selectedBrand = (String) parent.getItemAtPosition(position);
            updateModelsBasedOnBrand(selectedBrand);
        });
        buttonAddCar.setOnClickListener(v -> {
            // получение данных из элементов интерфейса
            String transportTypeText = autoCompleteTypeTS.getText().toString();
            String brandText = autoCompleteBrand.getText().toString();
            String modelText = autoCompleteModel.getText().toString();
            String vehicles_fuel_typeText = autoCompleteFuel.getText().toString();
            String yearText = autoCompleteYear.getText().toString();

            // проверка на наличие значений brand и model
            if (TextUtils.isEmpty(brandText) || TextUtils.isEmpty(modelText)) {
                Toast.makeText(requireContext(), "Пожалуйста, выберите марку и модель", Toast.LENGTH_SHORT).show();
                return;
            }

            // добавление данных в базу данных
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put(DBHelper.COLUMN_TRANSPORT_TYPE, transportTypeText);
            values.put(DBHelper.COLUMN_BRAND, brandText);
            values.put(DBHelper.COLUMN_MODEL, modelText);
            values.put(DBHelper.COLUMN_VEHICLES_FUEL_TYPE, vehicles_fuel_typeText);
            values.put(DBHelper.COLUMN_YEAR, yearText);

            long newRowId = db.insert(DBHelper.TABLE_VEHICLES, null, values);


            // Проверяем, была ли успешной вставка
            if (newRowId != -1) {
                // Вставка прошла успешно
                Toast.makeText(requireContext(), "Автомобиль добавлен успешно", Toast.LENGTH_SHORT).show();

                long lastInsertedVehicleId = newRowId;

                // Сохраните последний вставленный идентификатор транспортного средства в общих настройках
                SharedPreferences preferences = requireContext().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = preferences.edit();
                editor.putLong("lastInsertedVehicleId", lastInsertedVehicleId);
                editor.apply();

                // Вызов метода для обновления списка транспортных средств в NavigationView
                ((MainActivity) requireActivity()).updateTransportMenuInNavigationView();


                // Передайте данные в CustomFragment
                Bundle bundle = new Bundle();
                bundle.putLong("vehicleId", lastInsertedVehicleId);
                bundle.putString("transportType", transportTypeText);
                bundle.putString("brand", brandText);
                bundle.putString("model", modelText);
                bundle.putString("vehicles_fuel_type", vehicles_fuel_typeText);
                bundle.putString("year", yearText);

                // Передайте lastInsertedVehicleId в CustomFragment
                CustomFragment customFragment = new CustomFragment();
                customFragment.setArguments(bundle);

                // Замена фрагмента на CustomFragment
                MainActivity mainActivity = (MainActivity) requireActivity();
                mainActivity.replaceFragment(customFragment, true, true);
            } else {
                Toast.makeText(requireContext(), "Ошибка при добавлении автомобиля", Toast.LENGTH_SHORT).show();
            }
        });
        return view;
    }

    private void setupAutoCompleteAdapter(AutoCompleteTextView autoCompleteTextView, List<String> data) {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, data);
        autoCompleteTextView.setAdapter(adapter);

        // максимальное количество отображаемых элементов
        autoCompleteTextView.setThreshold(5);
        autoCompleteTextView.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                // все значения
                autoCompleteTextView.showDropDown();
            }
        });
    }

    private void updateModelsBasedOnBrand(String selectedBrand) {
        List<String> filteredModels;

        // проверка типа транспортного средства
        if ("Машина".equals(autoCompleteTypeTS.getText().toString())) {
            filteredModels = getModelsForBrand(selectedBrand);
        } else {
            // Мотоцикл - вызываем метод для мотоциклов
            filteredModels = getModelsForBrandForMotorcycles(selectedBrand);
        }

        // обновляется адаптер моделей отфильтрованными моделями
        modelAdapter.clear();
        modelAdapter.addAll(filteredModels);
        modelAdapter.notifyDataSetChanged();
    }
    private void updateBrandsAndModelsBasedOnTransportType(String selectedTransportType) {
        List<String> filteredBrands;
        List<String> filteredModels;

        if ("Машина".equals(selectedTransportType)) {
            filteredBrands = Arrays.asList(getResources().getStringArray(R.array.car_brands));
            filteredModels = Arrays.asList(getResources().getStringArray(R.array.car_models));
        } else {
            filteredBrands = Arrays.asList(getResources().getStringArray(R.array.motorcycle_brands));
            filteredModels = Arrays.asList(getResources().getStringArray(R.array.motorcycle_models));
        }

        // обновляется адаптер брендов отфильтрованными брендами
        ArrayAdapter<String> brandAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, filteredBrands);
        autoCompleteBrand.setAdapter(brandAdapter);

        // обновляется адаптер моделей отфильтрованными моделями
        modelAdapter.clear();
        modelAdapter.addAll(filteredModels);
        modelAdapter.notifyDataSetChanged();
    }
    // метод для получения моделей, связанных с конкретной маркой
    private List<String> getModelsForBrand(String brand) {
        List<String> filteredModels = new ArrayList<>();

        // прямое отображение между марками и моделями
        switch (brand) {
            case "BMW":
                filteredModels.add("M5");
                filteredModels.add("X6");
                break;
            case "TOYOTA":
                filteredModels.add("Allex");
                filteredModels.add("Corolla");
                break;
            case "LADA":
                filteredModels.add("Granta");
                filteredModels.add("Vesta");
                break;
            case "SKODA":
                filteredModels.add("Citigo");
                filteredModels.add("Forman");
                break;
            case "OPEL":
                filteredModels.add("Adam");
                filteredModels.add("Vita");
                break;
            default:
                filteredModels.addAll(allModels);
        }
        return filteredModels;
    }
    private List<String> getModelsForBrandForMotorcycles(String brand) {
        List<String> filteredModels = new ArrayList<>();

        // прямое отображение между марками мотоциклов и моделями
        switch (brand) {
            case "Harley-Davidson":
                filteredModels.add("Sportster");
                filteredModels.add("Softail");
                break;
            case "Honda":
                filteredModels.add("CBR");
                filteredModels.add("CBF");
                break;
            case "Yamaha":
                filteredModels.add("YZF");
                filteredModels.add("MT");
                break;

            default:
                filteredModels.addAll(allModels);
        }
        return filteredModels;
    }

}

