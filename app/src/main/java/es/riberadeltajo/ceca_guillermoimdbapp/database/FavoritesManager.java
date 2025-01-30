package es.riberadeltajo.ceca_guillermoimdbapp.database;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.security.AccessControlContext;
import java.util.ArrayList;
import java.util.List;

import es.riberadeltajo.ceca_guillermoimdbapp.models.Movie;

public class FavoritesManager {
    private final FavoritesDatabaseHelper databaseHelper;
    private SQLiteDatabase db;

    public FavoritesManager(Context context) {
        databaseHelper = new FavoritesDatabaseHelper(context);
    }

    public void addFavorite(Movie movie, String userId) {
        SQLiteDatabase db = databaseHelper.getWritableDatabase();

        if (isMovieInFavorites(movie, userId)) {
            db.close();
            return;
        }

        ContentValues values = new ContentValues();
        values.put(FavoritesDatabaseHelper.COLUMN_ID, movie.getId());
        values.put(FavoritesDatabaseHelper.COLUMN_TITLE, movie.getTitle());
        values.put(FavoritesDatabaseHelper.COLUMN_RELEASE_DATE, movie.getReleaseDate());
        values.put(FavoritesDatabaseHelper.COLUMN_RATING, movie.getRating());
        values.put(FavoritesDatabaseHelper.COLUMN_POSTER_PATH, movie.getPosterPath());
        values.put(FavoritesDatabaseHelper.COLUMN_USER_ID, userId);  // Asociar la película al usuario

        db.insert(FavoritesDatabaseHelper.TABLE_FAVORITES, null, values);
        db.close();
    }


    @SuppressLint("Range")
    public List<Movie> getFavorites(String userId) {
        List<Movie> favoriteMovies = new ArrayList<>();
        db = databaseHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + FavoritesDatabaseHelper.TABLE_FAVORITES + " WHERE " +
                FavoritesDatabaseHelper.COLUMN_USER_ID + " = ?", new String[]{userId});

        if (cursor != null && cursor.moveToFirst()) {
            do {
                Movie movie = new Movie();
                movie.setId(cursor.getString(cursor.getColumnIndex("id")));
                movie.setTitle(cursor.getString(cursor.getColumnIndex("title")));
                movie.setPosterPath(cursor.getString(cursor.getColumnIndex("poster_path")));
                favoriteMovies.add(movie);
            } while (cursor.moveToNext());
        }

        if (cursor != null) {
            cursor.close();
        }

        return favoriteMovies;
    }

    public void removeFavorite(Movie movie, String userId) {
        db.delete(FavoritesDatabaseHelper.TABLE_FAVORITES,
                FavoritesDatabaseHelper.COLUMN_ID + " = ? AND " + FavoritesDatabaseHelper.COLUMN_USER_ID + " = ?",
                new String[]{movie.getId(), userId});
    }

    public boolean isMovieInFavorites(Movie movie, String userId) {
        SQLiteDatabase db = databaseHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + FavoritesDatabaseHelper.TABLE_FAVORITES +
                        " WHERE " + FavoritesDatabaseHelper.COLUMN_ID + " = ? AND " +
                        FavoritesDatabaseHelper.COLUMN_USER_ID + " = ?",
                new String[]{movie.getId(), userId});

        boolean exists = cursor.getCount() > 0;
        cursor.close();
        return exists;
    }

}
