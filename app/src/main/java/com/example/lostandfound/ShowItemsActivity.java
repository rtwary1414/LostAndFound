package com.example.lostandfound;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

public class ShowItemsActivity extends AppCompatActivity {

    TextView btnBack;
    Spinner spinnerFilterCategory;
    ListView listViewItems;

    // It is used to connect to SQLite database
    DatabaseHelper dbHelper;

    // These store data fetched from database.
    ArrayList<String> itemList;
    ArrayList<Integer> itemIds;
    ArrayList<String> itemNames;
    ArrayList<String> itemDescriptions;
    ArrayList<String> itemDates;
    ArrayList<String> itemCategories;
    ArrayList<String> itemImages;

    // This connects itemList → ListView (UI binding)
    ArrayAdapter<String> listAdapter;

    // Below is the predefined categories for filter dropdown
    String[] filterCategories = {
            "All", "Electronics", "Wallets", "Pets", "Keys", "Documents", "Bags", "Other"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_items);

        btnBack = findViewById(R.id.btnBack);
        spinnerFilterCategory = findViewById(R.id.spinnerFilterCategory);
        listViewItems = findViewById(R.id.listViewItems);

        btnBack.setOnClickListener(v -> finish());

        // Initializing database connection
        dbHelper = new DatabaseHelper(this);

        // I have declared variables to store data fetched from the DB
        itemList = new ArrayList<>();
        itemIds = new ArrayList<>();
        itemNames = new ArrayList<>();
        itemDescriptions = new ArrayList<>();
        itemDates = new ArrayList<>();
        itemCategories = new ArrayList<>();
        itemImages = new ArrayList<>();

        // Displays category options in dropdown
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                filterCategories
        );
        spinnerFilterCategory.setAdapter(spinnerAdapter);

        // Whenever user selects category then it reloads the data.
        spinnerFilterCategory.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, android.view.View view, int position, long id) {
                loadItems(filterCategories[position]);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                loadItems("All");
            }
        });

        // When user clicks an item, it opens the detailed screen
        listViewItems.setOnItemClickListener((parent, view, position, id) -> {
            Intent intent = new Intent(ShowItemsActivity.this, ItemDetailActivity.class);
            intent.putExtra("id", itemIds.get(position));
            intent.putExtra("name", itemNames.get(position));
            intent.putExtra("description", itemDescriptions.get(position));
            intent.putExtra("date", itemDates.get(position));
            intent.putExtra("category", itemCategories.get(position));
            intent.putExtra("image", itemImages.get(position));
            startActivity(intent);
        });
    }

    // It reloads the data when the user comes back.
    @Override
    protected void onResume() {
        super.onResume();

        if (spinnerFilterCategory != null && spinnerFilterCategory.getSelectedItem() != null) {
            loadItems(spinnerFilterCategory.getSelectedItem().toString());
        }
    }

    private void loadItems(String selectedCategory) {
        // clearing the old data
        itemList.clear();
        itemIds.clear();
        itemNames.clear();
        itemDescriptions.clear();
        itemDates.clear();
        itemCategories.clear();
        itemImages.clear();

        // Opening the database in read mode
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor;

        if (selectedCategory.equals("All")) {
            cursor = db.rawQuery(
                    "SELECT * FROM " + DatabaseHelper.TABLE_NAME,
                    null
            );
        } else {
            cursor = db.rawQuery(
                    "SELECT * FROM " + DatabaseHelper.TABLE_NAME +
                            " WHERE " + DatabaseHelper.COL_CATEGORY + "=?",
                    new String[]{selectedCategory}
            );
        }

        // Loops through the DB rows
        while (cursor.moveToNext()) {
            int itemId = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_ID));
            String name = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_NAME));
            String description = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_DESC));
            String date = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_DATE));
            String category = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_CATEGORY));
            String image = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_IMAGE));

            itemIds.add(itemId);
            itemNames.add(name);
            itemDescriptions.add(description);
            itemDates.add(date);
            itemCategories.add(category);
            itemImages.add(image);

            itemList.add(
                    name +
                            "\nCategory: " + category +
                            "\n" + description +
                            "\nPosted: " + date
            );
        }

        cursor.close();

        if (itemList.isEmpty()) {
            Toast.makeText(this, "No items found", Toast.LENGTH_SHORT).show();
        }

        listAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_list_item_1,
                itemList
        );

        listViewItems.setAdapter(listAdapter);
    }
}