package com.example.finalproject;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.EdgeEffect;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.finalproject.entities.DBHelper;
import com.example.finalproject.entities.Note;
import com.google.android.material.bottomsheet.BottomSheetBehavior;

import java.io.FileDescriptor;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class CreateNoteActivity extends AppCompatActivity {
    DBHelper db;
    private EditText inputNoteTitle, inputNoteSubTitle, inputNoteText;
    private TextView textDateTime;
    private View viewSubtitleIndicator;
    private String selectNoteColor;
    private EditText textWebURL;

    private ImageView imageNote;
    private String selectImagePath;

    private Note alreadyAvailableNote;
    private ImageView imageDelete;
    private AlertDialog dialogDelete;

    private static final int REQUEST_CODE_STORAGE_PERMISSION = 1;
    private static final int  REQUEST_CODE_SELECT_IMAGE = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_create_note);
        db = new DBHelper(this);
        ImageView imageBack = findViewById(R.id.imageBack);
        imageBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        inputNoteTitle = findViewById(R.id.inputNoteTitle);
        inputNoteSubTitle = findViewById(R.id.inputNoteSubTitle);
        inputNoteText = findViewById(R.id.inputNote);
        textDateTime = findViewById(R.id.textDateTime);
        viewSubtitleIndicator = findViewById(R.id.viewSubtitleIndicator);
        textDateTime.setText(
                new SimpleDateFormat("EEEE, dd MMMM yyyy HH:mm a", Locale.getDefault())
                .format(new Date())
        );
        imageNote = findViewById(R.id.imageNote);
        ImageView imageView = findViewById(R.id.imageSave);
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

               saveNote();

            }
        });

        selectNoteColor = "#333333";
        selectImagePath = "";

        if(getIntent().getBooleanExtra("isViewOrUpdate",false)){
            alreadyAvailableNote = (Note) getIntent().getSerializableExtra("note");
            setViewOrUpdate();
        }
        if (alreadyAvailableNote != null){
            imageDelete = findViewById(R.id.imageDelete);
            imageDelete.setVisibility(View.VISIBLE);
            imageDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    showDeleteNoteDialog();

                }
            });
        }
        initMiscellaneous();
        setSubtitleIndicatorColor();
    }


    private void setViewOrUpdate(){
        inputNoteTitle.setText(alreadyAvailableNote.getTitle());
        inputNoteSubTitle.setText(alreadyAvailableNote.getSubtitle());
        inputNoteText.setText(alreadyAvailableNote.getNoteText());
        textDateTime.setText(alreadyAvailableNote.getDatetime());

        if (alreadyAvailableNote.getWebLink() != null && !alreadyAvailableNote.getWebLink().trim().isEmpty()){
            textWebURL.setText(alreadyAvailableNote.getWebLink());
        }
        if (alreadyAvailableNote.getImage() != null && !alreadyAvailableNote.getWebLink().trim().isEmpty()){

        }
    }

    private void saveNote(){
        if(inputNoteTitle.getText().toString().trim().isEmpty()){
            Toast.makeText(this,"Chủ đề không được trống",Toast.LENGTH_SHORT).show();
            return;
        }else if(inputNoteSubTitle.getText().toString().trim().isEmpty() && inputNoteText.getText().toString().trim().isEmpty()){
            Toast.makeText(this,"Nội dung không được trống",Toast.LENGTH_SHORT).show();
            return;
        }else{
            final Note note = new Note();
            note.setTitle(inputNoteTitle.getText().toString());
            note.setSubtitle(inputNoteSubTitle.getText().toString());
            note.setNoteText(inputNoteText.getText().toString());
            note.setDatetime(textDateTime.getText().toString());
            note.setColor(selectNoteColor);
            note.setImage(selectImagePath);

            if(alreadyAvailableNote != null){
                note.setId(alreadyAvailableNote.getId());

                if(db.updateNote(note)){
                    Intent intent = new Intent(CreateNoteActivity.this,MainActivity.class);
                    startActivity(intent);
                }else{
                    Toast.makeText(getApplicationContext(),"Sửa thất bại",Toast.LENGTH_SHORT).show();
                }
            }else{
                if(db.insertNote(note)){

                    Intent intent = new Intent(CreateNoteActivity.this,MainActivity.class);
                    startActivity(intent);

                }else{
                    Toast.makeText(getApplicationContext(),"Thêm thất bại",Toast.LENGTH_SHORT).show();
                }
            }



        }

    }

    private void showDeleteNoteDialog(){
        if (dialogDelete == null){
            AlertDialog.Builder builder = new AlertDialog.Builder(CreateNoteActivity.this);
            View view = LayoutInflater.from(this).inflate(
                    R.layout.layout_delete_note,
                    (ViewGroup) findViewById(R.id.layoutDeleteNoteContainer)
            );
            builder.setView(view);
            dialogDelete = builder.create();
            if (dialogDelete.getWindow() != null){
                dialogDelete.getWindow().setBackgroundDrawable(new ColorDrawable(0));
            }
            view.findViewById(R.id.textDeleteNote).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(db.deleteNote(alreadyAvailableNote.getId())){
                        Intent intent = new Intent();
                        intent.putExtra("isNoteDeleted",true);
                        setResult(RESULT_OK,intent);
                        finish();

                    }else{
                        Toast.makeText(getApplicationContext(),"Xoá thất bại",Toast.LENGTH_SHORT).show();
                    }
                }
            });
            view.findViewById(R.id.textCancel).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dialogDelete.dismiss();
                }
            });
        }
        dialogDelete.show();
    }

    private void initMiscellaneous(){
        final LinearLayout layoutMiscellaneous = findViewById(R.id.layoutMiscellaneous);
        final BottomSheetBehavior<LinearLayout> bottomSheetBehavior = BottomSheetBehavior.from(layoutMiscellaneous);
        layoutMiscellaneous.findViewById(R.id.textMiscellaneous).setOnClickListener(new View.OnClickListener() {
            @Override //animation of layoutColor
            public void onClick(View view) {
                if (bottomSheetBehavior.getState() != BottomSheetBehavior.STATE_EXPANDED){
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                }else{
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                }
            }
        });
        final ImageView imageView1 = layoutMiscellaneous.findViewById(R.id.imageColor1);
        final ImageView imageView2 = layoutMiscellaneous.findViewById(R.id.imageColor2);
        final ImageView imageView3 = layoutMiscellaneous.findViewById(R.id.imageColor3);
        final ImageView imageView4 = layoutMiscellaneous.findViewById(R.id.imageColor4);
        final ImageView imageView5 = layoutMiscellaneous.findViewById(R.id.imageColor5);
        //set state of color
        layoutMiscellaneous.findViewById(R.id.viewColor1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectNoteColor = "#333333";
                imageView1.setImageResource(R.drawable.ic_done);
                imageView2.setImageResource(0);
                imageView3.setImageResource(0);
                imageView4.setImageResource(0);
                imageView5.setImageResource(0);
                setSubtitleIndicatorColor();
            }
        });
        layoutMiscellaneous.findViewById(R.id.viewColor2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectNoteColor = "#FDBE3B";
                imageView2.setImageResource(R.drawable.ic_done);
                imageView1.setImageResource(0);
                imageView3.setImageResource(0);
                imageView4.setImageResource(0);
                imageView5.setImageResource(0);
                setSubtitleIndicatorColor();
            }
        });
        layoutMiscellaneous.findViewById(R.id.viewColor3).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectNoteColor = "#EE3A3A";
                imageView3.setImageResource(R.drawable.ic_done);
                imageView2.setImageResource(0);
                imageView1.setImageResource(0);
                imageView4.setImageResource(0);
                imageView5.setImageResource(0);
                setSubtitleIndicatorColor();
            }
        });
        layoutMiscellaneous.findViewById(R.id.viewColor4).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectNoteColor = "#438AE5";
                imageView4.setImageResource(R.drawable.ic_done);
                imageView2.setImageResource(0);
                imageView3.setImageResource(0);
                imageView1.setImageResource(0);
                imageView5.setImageResource(0);
                setSubtitleIndicatorColor();
            }
        });
        layoutMiscellaneous.findViewById(R.id.viewColor5).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectNoteColor = "#D73DDC";
                imageView5.setImageResource(R.drawable.ic_done);
                imageView2.setImageResource(0);
                imageView3.setImageResource(0);
                imageView4.setImageResource(0);
                imageView1.setImageResource(0);
                setSubtitleIndicatorColor();
            }
        });

        layoutMiscellaneous.findViewById(R.id.layoutAddImage).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                if (ContextCompat.checkSelfPermission(
                        getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED){
                    ActivityCompat.requestPermissions(
                            CreateNoteActivity.this,
                            new String[] {Manifest.permission.READ_EXTERNAL_STORAGE},
                            REQUEST_CODE_STORAGE_PERMISSION
                    );
                }else{
                    selectImage();
                }
            }
        });

        if (alreadyAvailableNote != null && alreadyAvailableNote.getColor() != null && !alreadyAvailableNote.getColor().trim().isEmpty()){
                switch (alreadyAvailableNote.getColor()){
                    case "#FDBE3B":
                        layoutMiscellaneous.findViewById(R.id.viewColor2).performClick();
                        break;
                    case "#EE3A3A":
                        layoutMiscellaneous.findViewById(R.id.viewColor3).performClick();
                        break;
                    case "#438AE5":
                        layoutMiscellaneous.findViewById(R.id.viewColor4).performClick();
                        break;
                    case "#D73DDC":
                        layoutMiscellaneous.findViewById(R.id.viewColor5).performClick();
                        break;
                    default:
                        layoutMiscellaneous.findViewById(R.id.viewColor1).performClick();
                }
        }


    }
    //set color of SubTitle in detail note
    private void setSubtitleIndicatorColor(){
        GradientDrawable gradientDrawable = (GradientDrawable) viewSubtitleIndicator.getBackground();
        gradientDrawable.setColor(Color.parseColor(selectNoteColor));
    }

    private void selectImage(){
//        Intent intent = new Intent();
//        intent.setType("image/*");
//        intent.setAction(Intent.ACTION_GET_CONTENT);
//
//            startActivityForResult(Intent.createChooser(intent, "Select Picture"),REQUEST_CODE_SELECT_IMAGE);
        Intent intent = new Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        if (intent.resolveActivity(getPackageManager()) != null){
            startActivityForResult(intent,REQUEST_CODE_SELECT_IMAGE);
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_STORAGE_PERMISSION && grantResults.length > 0){
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED){
                selectImage();
            }else{
                Toast.makeText(this, "Quyền truy cập bị từ chối", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_SELECT_IMAGE && resultCode == RESULT_OK){
            if (data != null){
                Uri selectImageUri = data.getData();

                if (selectImageUri != null){
                    try {
                        InputStream inputStream = getContentResolver().openInputStream(selectImageUri);
                        Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                        imageNote.setImageBitmap(bitmap);

                        imageNote.setVisibility(View.VISIBLE);

                        selectImagePath = getPathFromUri(selectImageUri);

                    }catch (Exception e){
                        Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }
    }
    private Bitmap getBitmapFromUri(Uri uri) throws IOException {
        ParcelFileDescriptor parcelFileDescriptor =
                getContentResolver().openFileDescriptor(uri, "r");
        FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();
        Bitmap image = BitmapFactory.decodeFileDescriptor(fileDescriptor);
        parcelFileDescriptor.close();
        return image;
    }
    private String getPathFromUri(Uri contentUri){
            String filePath;
            Cursor cursor = getContentResolver().query(contentUri,null,null,null,null);
            if (cursor == null){
                filePath = contentUri.getPath();

            }else{
                cursor.moveToFirst();
                int index = cursor.getColumnIndex("_data");
                filePath = cursor.getString(index);
                cursor.close();
            }
            return filePath;
    }
}