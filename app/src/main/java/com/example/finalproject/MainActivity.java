package com.example.finalproject;


import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import android.annotation.SuppressLint;

import android.content.Intent;

import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;

import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;


import com.example.finalproject.adapters.NotesAdapter;
import com.example.finalproject.entities.DBHelper;
import com.example.finalproject.entities.Note;
import com.example.finalproject.listeners.NotesListener;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements NotesListener {

        public static final int REQUEST_CODE_ADD_NOTE = 1;
        public static final int REQUEST_CODE_UPDATE = 2;
        public static final int REQUEST_CODE_SHOW_NOTES = 3;
        DBHelper db;
        private RecyclerView notesRecyclerView;
        private List<Note> noteList;
        private NotesAdapter notesAdapter;

        private int noteClickedPosition = -1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_main);
        db = new DBHelper(this);
        ImageView imageAddNoteMain = findViewById(R.id.imageAddNotesMain);

        imageAddNoteMain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

              startActivityForResult(
                      new Intent(getApplicationContext(),CreateNoteActivity.class),
                      REQUEST_CODE_ADD_NOTE
              );

            }
        });

        notesRecyclerView = findViewById(R.id.notesRecyclerView);
        notesRecyclerView.setLayoutManager(
                new StaggeredGridLayoutManager(2,StaggeredGridLayoutManager.VERTICAL)
        );

        noteList = new ArrayList<>();
        notesAdapter = new NotesAdapter(noteList,this);

        notesRecyclerView.setAdapter(notesAdapter);
        getNotes(REQUEST_CODE_SHOW_NOTES,false);
        EditText inputSearch= (EditText) findViewById(R.id.inputSearch);
        inputSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                   notesAdapter.CancelTimer();
            }

            @Override
            public void afterTextChanged(Editable s) {
                    if (noteList.size() != 0){
                        notesAdapter.searchNotes(s.toString());
                    }
            }
        });
    }

    @Override
    public void onNoteClicked(Note note, int position) {

       noteClickedPosition = position;
        Intent intent = new Intent(getApplicationContext(),CreateNoteActivity.class);
        intent.putExtra("isViewOrUpdate",true);
        intent.putExtra("note",note);
        startActivityForResult(intent,REQUEST_CODE_UPDATE);
    }

    private void getNotes(final int requestCode,final boolean isNoteDeleted){
        @SuppressLint("StaticFieldLeak")
        class GetNotesTask extends AsyncTask<Void, Void, List<Note>>{
            @Override
            protected List<Note> doInBackground(Void... voids) {
                return db.getAllNote();
            }

            @Override
            protected void onPostExecute(List<Note> notes) {
                super.onPostExecute(notes);

                if (requestCode == REQUEST_CODE_SHOW_NOTES){
                    noteList.addAll(notes);
                    notesAdapter.notifyDataSetChanged();
                }else if (requestCode == REQUEST_CODE_ADD_NOTE){
                    notes.add(0,notes.get(0));
                    notesAdapter.notifyItemInserted(0);
                    notesRecyclerView.smoothScrollToPosition(0);
                }else if (requestCode == REQUEST_CODE_UPDATE){
                    noteList.remove(noteClickedPosition);
                    if (isNoteDeleted){
                        notesAdapter.notifyItemRemoved(noteClickedPosition);
                    }else{
                        noteList.add(noteClickedPosition,notes.get(noteClickedPosition));
                        notesAdapter.notifyItemChanged(noteClickedPosition);
                    }
                }

//               if(noteList.size() == 0){
//                   noteList.addAll(notes);
//                   notesAdapter.notifyDataSetChanged();
//               }else{
//                   notes.add(0,notes.get(0));
//                   notesAdapter.notifyItemInserted(0);
//               }
//               notesRecyclerView.smoothScrollToPosition(0);

            }
        }
        new GetNotesTask().execute();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_ADD_NOTE && resultCode == RESULT_OK){
                getNotes(REQUEST_CODE_ADD_NOTE,false);
        }else if (requestCode == REQUEST_CODE_UPDATE && resultCode == RESULT_OK){
            if (data !=null){
                getNotes(REQUEST_CODE_UPDATE,data.getBooleanExtra("isNoteDeleted",false));
            }
        }
    }
}