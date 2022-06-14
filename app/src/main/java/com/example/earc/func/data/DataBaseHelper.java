package com.example.earc.func.data;

import android.content.ContentValues;
import android.content.Context;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

import com.example.earc.R;
import com.github.mikephil.charting.data.Entry;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class DataBaseHelper extends SQLiteOpenHelper {
    private static final String NAME = "earc.db";

    private static final int VERSION = 1;

    private Context mContext;

    public DataBaseHelper(@Nullable Context context) {
        super(context, NAME, null, VERSION);
        this.mContext = context;
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        String createTableStatement = this.mContext.getString(R.string.create_table);

        sqLiteDatabase.execSQL(createTableStatement);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }

    public boolean addOne(DataModel dataModel) {
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();

        ContentValues contentValues = new ContentValues();

        contentValues.put(this.mContext.getString(R.string.decibel), dataModel.getDecibel());

        long insertSuccess = sqLiteDatabase.insert(this.mContext.getString(R.string.table_name), null, contentValues);

        sqLiteDatabase.close();

        return insertSuccess != -1;
    }

    public List<DataModel> getDataInSpecificTime(Date start, Date end) {
        List<DataModel> result = new ArrayList<>();

        String queryData = this.mContext.getString(R.string.query_data);

        String[] selectionArgs = {String.valueOf((int) (start.getTime() / 1000)), String.valueOf((int) (end.getTime() / 1000))};

        SQLiteDatabase sqLiteDatabase = this.getReadableDatabase();

        Cursor cursor = sqLiteDatabase.rawQuery(queryData, selectionArgs);

        if (cursor.moveToFirst()) {
            do {
                int timestamp = cursor.getInt(0);
                int decibel = cursor.getInt(1);

                result.add(new DataModel(timestamp, decibel));
            } while (cursor.moveToNext());
        }

        cursor.close();
        sqLiteDatabase.close();

        return result;
    }
}
