package com.debasish.odiacalendararchiveadmin;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.debasish.odiacalendararchiveadmin.model.Upload;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

public class ImagesActivity extends AppCompatActivity implements ImageAdapter.OnItemClickListener {
    private TextView textView;
    private ProgressBar progressCircle;
    private ArrayList<String> listYear;
    private Spinner spinner;
    private RecyclerView recyclerView;
    private List<Upload> uploads;
    private ImageAdapter adapter;
    private Upload selectedItem;

    private FirebaseStorage firebaseStorage;
    private DatabaseReference databaseReference;

    private ValueEventListener listener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_images);
        textView = findViewById(R.id.text_view_select_year);
        progressCircle = findViewById(R.id.progress_circle);
        spinner = findViewById(R.id.spinner_select_year);
        recyclerView = findViewById(R.id.recycler_view);
        getYearList();      //get list of year passed from the intent

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        uploads = new ArrayList<>();

        adapter = new ImageAdapter(uploads);
        recyclerView.setAdapter(adapter);
        adapter.setOnItemClickListener(ImagesActivity.this);

        firebaseStorage = FirebaseStorage.getInstance();

        populateSpinner();      //populate the spinner with year from array list
    }


    private void getYearList() {
        listYear = new ArrayList<String>();
        Intent intent = getIntent();
        //store the passed array list into another array list
        listYear = intent.getStringArrayListExtra("ListOfYear");
    }

    private void populateSpinner() {
        ArrayAdapter<String> yearAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, listYear);
        yearAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(yearAdapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            //what to display when a spinner item is selected
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String databaseYear = parent.getItemAtPosition(position).toString();
                //get the default selected year from spinner
                int year;
                try {
                    year = Integer.parseInt(databaseYear);
                } catch (Exception e) {
                    year = 2021;
                }
                databaseReference = FirebaseDatabase.getInstance().getReference("calendar");
                //from the calendar path select all the months those have the mentioned year as their child
                Query query = databaseReference.orderByChild("year").startAt(year).endAt(year);
                listener = query.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        //clear uploads so than double data is not shown in case of data change
                        uploads.clear();

                        for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                            Upload upload = postSnapshot.getValue(Upload.class);  //save all the values to upload variable with get methods from Upload class
                            //key is excluded in the Upload class so it will be fetched in the next statement
                            upload.setKey(postSnapshot.getKey()); //save key to upload variable
                            uploads.add(upload); //add upload variable with key to the uploads list
                        }

                        adapter.notifyDataSetChanged();

                        //disable progress circle when data is shown
                        progressCircle.setVisibility(View.INVISIBLE);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(ImagesActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                        progressCircle.setVisibility(View.INVISIBLE);
                    }
                });
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                Toast.makeText(ImagesActivity.this, "Nothing selected", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onShowDetails(int position) {
        selectedItem = uploads.get(position); //get item from uploads list at position
        //get filename month and year and show when show details is clicked
        String name = selectedItem.getName();
        String month = selectedItem.getMonth();
        int year = selectedItem.getYear();
        View view = LayoutInflater.from(ImagesActivity.this).inflate(R.layout.details_dialog_layout, null);
        TextView textViewName = view.findViewById(R.id.textViewName);
        TextView textViewMonth = view.findViewById(R.id.textViewMonth);
        TextView textViewYear = view.findViewById(R.id.textViewYear);
        textViewName.setText(getString(R.string.filename, name));
        textViewMonth.setText(getString(R.string.month, month));
        textViewYear.setText(getString(R.string.year, Integer.toString(year)));

        Dialog dialog = new AlertDialog.Builder(this)
                .setView(view)
                .create();
        dialog.show();
    }

    @Override
    public void onDeleteClick(int position) {
        selectedItem = uploads.get(position);
        String selectedKey = selectedItem.getKey();     //get key of the selected item

        //get the url from firebase storage using the selected item
        StorageReference imageRef = firebaseStorage.getReferenceFromUrl(selectedItem.getImageUrl());
        //using the url delete the item from firebase storage
        imageRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                //on successful deletion from firebase storage delete the child from firebase database using the key
                databaseReference.child(selectedKey).removeValue();
                Toast.makeText(ImagesActivity.this, "Item deleted", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(ImagesActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        databaseReference.removeEventListener(listener);
    }
}
