package com.example.lostandfound;

import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class ItemDetailActivity extends AppCompatActivity {

    TextView btnBack, tvItemName, tvItemDescription, tvDate, tvCategory;
    ImageView imgItem;
    Button btnRemove;

    DatabaseHelper dbHelper;
    int itemId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_detail);

        btnBack = findViewById(R.id.btnBack);
        imgItem = findViewById(R.id.imgItem);
        tvItemName = findViewById(R.id.tvItemName);
        tvCategory = findViewById(R.id.tvCategory);
        tvItemDescription = findViewById(R.id.tvItemDescription);
        tvDate = findViewById(R.id.tvDate);
        btnRemove = findViewById(R.id.btnRemove);

        dbHelper = new DatabaseHelper(this);

        itemId = getIntent().getIntExtra("id", -1);
        String name = getIntent().getStringExtra("name");
        String description = getIntent().getStringExtra("description");
        String date = getIntent().getStringExtra("date");
        String category = getIntent().getStringExtra("category");
        String image = getIntent().getStringExtra("image");

        tvItemName.setText(name);
        tvItemDescription.setText(description);
        tvDate.setText("Posted: " + date);
        tvCategory.setText("Category: " + category);

        if (image != null && !image.isEmpty()) {
            imgItem.setImageURI(Uri.parse(image));
        }

        btnBack.setOnClickListener(v -> finish());

        btnRemove.setOnClickListener(v -> {
            if (itemId == -1) {
                Toast.makeText(this, "Item not found", Toast.LENGTH_SHORT).show();
                return;
            }

            SQLiteDatabase db = dbHelper.getWritableDatabase();

            int deletedRows = db.delete(
                    DatabaseHelper.TABLE_NAME,
                    DatabaseHelper.COL_ID + "=?",
                    new String[]{String.valueOf(itemId)}
            );

            if (deletedRows > 0) {
                Toast.makeText(this, "Advert removed", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(this, "Error removing advert", Toast.LENGTH_SHORT).show();
            }
        });
    }
}