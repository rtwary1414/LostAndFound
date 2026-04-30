package com.example.lostandfound;

import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class CreateAdvertActivity extends AppCompatActivity {

    // The below variables represent the screen elements.
    EditText etName, etDescription;
    Spinner spinnerCategory;
    ImageView imgPreview;
    Button btnChooseImage, btnSave;
    TextView btnBack;

    // This app stores the image URI, not the actual image file. That keeps
    // the database lightweight.
    DatabaseHelper dbHelper;
    Uri selectedImageUri = null;

    String[] categories = {
            "Electronics",
            "Wallets",
            "Pets",
            "Keys",
            "Documents",
            "Bags",
            "Other"
    };

    // This block handles the result after the user chooses an image.
    ActivityResultLauncher<Intent> imagePickerLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() == RESULT_OK && result.getData() != null) {

                            selectedImageUri = result.getData().getData();

                            if (selectedImageUri != null) {
                                final int takeFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION;

                                try {
                                    getContentResolver().takePersistableUriPermission(
                                            selectedImageUri,
                                            takeFlags
                                    );
                                } catch (SecurityException e) {
                                    e.printStackTrace();
                                }

                                imgPreview.setImageURI(selectedImageUri);
                            }
                        }
                    }
            );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_advert);

        btnBack = findViewById(R.id.btnBack);
        etName = findViewById(R.id.etName);
        spinnerCategory = findViewById(R.id.spinnerCategory);
        imgPreview = findViewById(R.id.imgPreview);
        btnChooseImage = findViewById(R.id.btnChooseImage);
        etDescription = findViewById(R.id.etDescription);
        btnSave = findViewById(R.id.btnSave);

        btnBack.setOnClickListener(v -> finish());

        dbHelper = new DatabaseHelper(this);

        // This connects the category array to the spinner dropdown.
        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                categories
        );
        spinnerCategory.setAdapter(categoryAdapter);


        btnChooseImage.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.setType("image/*");
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);

            imagePickerLauncher.launch(intent);
        });

        btnSave.setOnClickListener(v -> {

            String name = etName.getText().toString().trim();
            String desc = etDescription.getText().toString().trim();
            String category = spinnerCategory.getSelectedItem().toString();

            if (name.isEmpty() || desc.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            if (selectedImageUri == null) {
                Toast.makeText(this, "Please choose an image", Toast.LENGTH_SHORT).show();
                return;
            }

            String currentDate = new SimpleDateFormat(
                    "dd/MM/yyyy HH:mm",
                    Locale.getDefault()
            ).format(new Date());

            // opens the DB for writing
            SQLiteDatabase db = dbHelper.getWritableDatabase();

            // stores column-value pairs
            ContentValues values = new ContentValues();
            values.put(DatabaseHelper.COL_NAME, name);
            values.put(DatabaseHelper.COL_DESC, desc);
            values.put(DatabaseHelper.COL_DATE, currentDate);
            values.put(DatabaseHelper.COL_CATEGORY, category);
            values.put(DatabaseHelper.COL_IMAGE, selectedImageUri.toString());

            long result = db.insert(DatabaseHelper.TABLE_NAME, null, values);

            if (result != -1) {
                Toast.makeText(this, "Saved Successfully", Toast.LENGTH_SHORT).show();

                etName.setText("");
                etDescription.setText("");
                spinnerCategory.setSelection(0);
                imgPreview.setImageDrawable(null);
                selectedImageUri = null;
            } else {
                Toast.makeText(this, "Error saving data", Toast.LENGTH_SHORT).show();
            }
        });
    }
}