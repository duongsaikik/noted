package com.example.finalproject.entities;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DBHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "Notes.db";
    private static final String TABLE_NAME = "Note";
    private static final String _ID = "id";
    private static final String TITLE_NOTE = "title";
    private static final String NOTE_DATE = "datenote";
    private static final String SUB_TITLE = "subtitle";
    private static final String CONTENT_NOTE = "content";
    private static final String _COLOR = "color";
    private static final String IMAGE = "image";
    private static final String WEB_LINK = "link";

    public DBHelper(Context context){
        super(context,DATABASE_NAME,null,1);

    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String query = "CREATE TABLE " + TABLE_NAME +
                " (" + _ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                TITLE_NOTE + " TEXT, " +
                NOTE_DATE + " TEXT, " +
                SUB_TITLE + " TEXT, " +
                CONTENT_NOTE + " TEXT, " +
                _COLOR + " TEXT, " +
                IMAGE + " TEXT, " +
                WEB_LINK + " TEXT);";
//        db.execSQL("create table Note (id integer primary key, title text, datenote text, subtitle text, content text, color text, link text)" );
            db.execSQL(query);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME );
        onCreate(db);
    }

    public boolean insertNote(Note note){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("title",note.getTitle());
        contentValues.put("datenote",note.getDatetime());
        contentValues.put("subtitle",note.getSubtitle());
        contentValues.put("content",note.getNoteText());
        contentValues.put("color",note.getColor());
        contentValues.put("image",note.getImage());
        contentValues.put("link",note.getWebLink());
        db.insert("Note",null,contentValues);
        return true;
    }
    public List getAllNote(){

        List<Note> arrayList = new ArrayList<Note>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res = db.rawQuery("select * from Note"  ,null);
        res.moveToFirst();
        while (res.isAfterLast() == false){
//            ContentValues contentValues = new ContentValues();
            Note note = new Note();
            note.setTitle(res.getString(res.getColumnIndexOrThrow(TITLE_NOTE)));
            note.setId(res.getString(res.getColumnIndexOrThrow(_ID)));
            note.setDatetime(res.getString(res.getColumnIndexOrThrow(NOTE_DATE)));
            note.setSubtitle(res.getString(res.getColumnIndexOrThrow(SUB_TITLE)));
            note.setNoteText(res.getString(res.getColumnIndexOrThrow(CONTENT_NOTE)));
            note.setColor(res.getString(res.getColumnIndexOrThrow(_COLOR)));
            note.setImage(res.getString(res.getColumnIndexOrThrow(IMAGE)));
            note.setWebLink(res.getString(res.getColumnIndexOrThrow(WEB_LINK)));
            arrayList.add(
                    note
            );
            res.moveToNext();
        }
        return arrayList;
    }
    public boolean updateNote (Note note){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(TITLE_NOTE,note.getTitle());
        contentValues.put(NOTE_DATE, new SimpleDateFormat("EEEE, dd MMMM yyyy HH:mm a", Locale.getDefault())
                .format(new Date()));
        contentValues.put(SUB_TITLE,note.getSubtitle());
        contentValues.put(CONTENT_NOTE,note.getNoteText());
        contentValues.put(_COLOR,note.getColor());
        contentValues.put(IMAGE,note.getImage());
        contentValues.put(WEB_LINK,note.getWebLink());
        db.update("Note",contentValues,"id = ?",new String[]{note.getId()});
        return true;
    }
    public boolean deleteNote(String id){
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete("Note","id = ?" , new String[]{id});
        return true;
    }
}
