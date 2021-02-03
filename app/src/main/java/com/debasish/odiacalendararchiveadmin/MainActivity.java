package com.debasish.odiacalendararchiveadmin;

import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.debasish.odiacalendararchiveadmin.model.MapMonth;
import com.debasish.odiacalendararchiveadmin.model.Upload;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Collections;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    private static final int PICK_IMAGE_REQUEST = 1;
    MapMonth mapMonth = new MapMonth();
    private EditText editTextYear;
    private ImageView imageView;
    private ProgressBar progressBar;
    private Button buttonUpload;
    private Uri imageUri;
    private String month;
    private int year;
    private StorageReference storageReference;
    private DatabaseReference databaseReference;
    private StorageTask uploadTask;
    private ArrayList<String> listYear;
    private TextView textViewShowUploads;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button buttonChooseImage = findViewById(R.id.button_choose_image);
        buttonUpload = findViewById(R.id.button_upload);
        textViewShowUploads = findViewById(R.id.text_view_show_uploads);
        editTextYear = findViewById(R.id.edit_text_year);
        imageView = findViewById(R.id.image_view);
        progressBar = findViewById(R.id.progress_bar);
        Spinner spinner = findViewById(R.id.spinner_month);

        storageReference = FirebaseStorage.getInstance().getReference("calendar"); //save in path calendar
        databaseReference = FirebaseDatabase.getInstance().getReference("calendar");
        getListOfYear(); //fetch the years from firebase storage and store it into a list

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.month, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(this);

        buttonChooseImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openFileChooser();
            }
        });

        buttonUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadFile();
            }
        });
    }

    private void getListOfYear() {
        listYear = new ArrayList<>();
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                    String yearKey = postSnapshot.getKey();
                    listYear.add(yearKey.substring(0, 4)); //save the first 4 letters of the month
                }
                Collections.sort(listYear, Collections.reverseOrder()); //sort the list with in reverse order
                //if data is present goto ImagesActivity
                textViewShowUploads.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        openImagesActivity();
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MainActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void openFileChooser() {
        //intent for selecting an image using android file chooser
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK
                && data != null & data.getData() != null) {
            imageUri = data.getData();

            Picasso.get().load(imageUri).into(imageView);
        }
    }

    private String getFileExtension(Uri uri) {
        //get the extension of the file name that will be uploaded
        ContentResolver cR = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(cR.getType(uri));
    }

    private void uploadFile() {
        if (imageUri != null) {   //when an image is selected
            try {
                //get year from the edit text box
                year = Integer.parseInt(editTextYear.getText().toString());

                //when both cases are false that is month selected is Jan-Dec and year is between 1900-2099
                //upload the file
                if (month.equals("Select Month") || verifyYear(year)) {
                    Toast.makeText(MainActivity.this, "Give valid Month And Year", Toast.LENGTH_SHORT).show();
                } else {
                    buttonUpload.setEnabled(false);
                    String fileExtension = getFileExtension(imageUri);
                    String fileName = year + "_" + mapMonth.getMonth(month) + "." + fileExtension;  //eg: 2020_01.jpg
                    StorageReference fileRef = storageReference.child(year + "/" + fileName);   //save under path eg: 2020/

                    uploadTask = fileRef.putFile(imageUri)
                            .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                    //wait 1 sec before resetting the progressbar back to 0
                                    Handler handler = new Handler();
                                    handler.postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            progressBar.setProgress(0);
                                        }
                                    }, 1000);

                                    Toast.makeText(MainActivity.this, "Upload Successfull", Toast.LENGTH_LONG).show();

                                    fileRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                        @Override
                                        public void onSuccess(Uri uri) {
                                            Upload upload = new Upload(fileName, month, year, uri.toString());  //save filename, month, year and url to a uploads variable
                                            //String uploadId = databaseReference.push().getKey();
                                            databaseReference.child(year + "_" + mapMonth.getMonth(month)).setValue(upload);    //under child eg: 2020_01, save all the values of the upload variable to the database
                                        }
                                    });
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            })
                            .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onProgress(@NonNull UploadTask.TaskSnapshot taskSnapshot) {
                                    double progress = (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount());
                                    progressBar.setProgress((int) progress);
                                }
                            }).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                                    //enable the upload button after successful file upload
                                    buttonUpload.setEnabled(true);
                                }
                            });
                }
            } catch (Exception e) {
                Toast.makeText(MainActivity.this, "Give valid Month and Year", Toast.LENGTH_SHORT).show();
            }


        } else {
            Toast.makeText(this, "No File Selected", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean verifyYear(int year) {
        //if year is between 1900-2099 return false else return true
        return year / 100 != 19 && year / 100 != 20;
    }

    private void openImagesActivity() {
        ArrayList<String> newList = new ArrayList<>();
        //create a new array list and the save the years from the previous array list with no repetitions
        for (String string : listYear) {
            if (!newList.contains(string)) {
                newList.add(string);
            }
        }
        Intent intent = new Intent(this, ImagesActivity.class);
        //pass the intent as a string array list
        intent.putStringArrayListExtra("ListOfYear", newList);
        startActivity(intent);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        //get the selected month from spinner
        month = parent.getItemAtPosition(position).toString();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //create menu on the support action bar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.pin_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        //add a change pin button on the support action bar
        if (item.getItemId() == R.id.change_pin) {
            Intent intent = new Intent(this, ChangePinActivity.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        ActivityCompat.finishAffinity(MainActivity.this);
    }
}