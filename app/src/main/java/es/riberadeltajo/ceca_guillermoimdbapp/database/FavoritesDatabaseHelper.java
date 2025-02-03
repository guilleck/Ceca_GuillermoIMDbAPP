package es.riberadeltajo.ceca_guillermoimdbapp.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.security.AccessControlContext;

public class FavoritesDatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "favorites.db";
    private static final int DATABASE_VERSION = 6;

    public static final String COLUMN_RATING = "rating";
    public static final String TABLE_FAVORITES = "favorites";
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_TITLE = "title";
    public static final String COLUMN_RELEASE_DATE = "release_date";
    public static final String COLUMN_POSTER_PATH = "poster_path";
    public static final String COLUMN_USER_ID = "user_id";

    public FavoritesDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_FAVORITES_TABLE = "CREATE TABLE " + TABLE_FAVORITES + " (" +
                COLUMN_ID + " TEXT, " +
                COLUMN_TITLE + " TEXT, " +
                COLUMN_RELEASE_DATE + " TEXT, " +
                COLUMN_RATING + " TEXT, " +
                COLUMN_POSTER_PATH + " TEXT, " +
                COLUMN_USER_ID + " TEXT, " +
                "PRIMARY KEY (" + COLUMN_ID + ", " + COLUMN_USER_ID + "))";  // Clave primaria combinada
        db.execSQL(CREATE_FAVORITES_TABLE);
    }


    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 3) {
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_FAVORITES);
            onCreate(db);
        }
    }

}
