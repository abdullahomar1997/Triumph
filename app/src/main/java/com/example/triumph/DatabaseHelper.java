package com.example.triumph;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import androidx.annotation.Nullable;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String TAG = "DatabaseHelper";

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "game9";

    private static final String OngoingGame = "OngoingGame";
    private static final String Streak = "Streak";

    private static final String gameStatus = "GameStatus";
    private static final String players = "Players";
    private static final String gameinfo = "Gameinfo";
    private static final String highest = "Highest";
    private static final String current = "Current";
    private static final String triumph = "Triumph";

    public DatabaseHelper(@Nullable Context context) {
        super(context, DATABASE_NAME, null,DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createOngoingGameTable = "CREATE TABLE " + OngoingGame + " (ID INTEGER PRIMARY KEY AUTOINCREMENT, " + players +  " TEXT, " + gameinfo + " TEXT, " + gameStatus+" TEXT)";
        String createStreakTable = "CREATE TABLE " + Streak + " (ID INTEGER PRIMARY KEY AUTOINCREMENT, " + highest + " TEXT, " + current + " TEXT, " + triumph + " TEXT)";

        db.execSQL(createOngoingGameTable);
        db.execSQL(createStreakTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        db.execSQL("DROP TABLE IF EXISTS " + OngoingGame);
        db.execSQL("DROP TABLE IF EXISTS " + Streak);

        onCreate(db);
    }

    public void saveGameData(Game game, String status){

        String player1 = game.players.get(0).toString().replaceAll("\\[", "").replaceAll("\\]","");
        String player2 = game.players.get(1).toString().replaceAll("\\[", "").replaceAll("\\]","");

        String games = game.toString().replaceAll("\\[", "").replaceAll("\\]","");

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(players, player1+";"+player2);
        contentValues.put(gameinfo,games);
        contentValues.put(gameStatus,status);

        Log.d(TAG,"SOGD Saving Ongoing Game Data .......... ");
        Log.d(TAG,"SOGD for Game Status " + status);
        Log.d(TAG,"SOGD Saved Player 1  " + player1);
        Log.d(TAG,"SOGD Saved Player 2  " + player2);
        Log.d(TAG,"SOGD Game Info  " + games);

        long result = db.insert(OngoingGame,null,contentValues);

        if(result == 0){
            Log.d(TAG,"SOGD --------- Saving Ongoing Game Data PASSED -------- ");
        }
        else{
            Log.d(TAG,"SOGD --------- Saving Ongoing Game Data FAILED -------- ");
        }
    }

    public Cursor getGameData(){

        String query= "SELECT * FROM " + OngoingGame;

        SQLiteDatabase db = this.getWritableDatabase();

        return db.rawQuery(query,null);
    }

    public void saveStreakData(Game game,int Highest,int Current){

        if(game.players.get(0).playerScore > 60){
            Current+=1;
        }

        else{
            Current = 0;
        }

        if(Current > Highest){
            Highest = Current;
        }

        game.switchTriumphCard();

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(highest, String.valueOf(Highest));
        contentValues.put(current,String.valueOf(Current));
        contentValues.put(triumph,game.triumphCard);

        Log.d(TAG," adding Streak data to database ............. ");

        long result = db.insert(Streak,null,contentValues);

        if(result == 0){

            Log.d(TAG,"SSD --------- Saving Streak Data PASSED -------- ");
            Log.d(TAG,"SSD The Current Streak is " + Current );
            Log.d(TAG,"SSD The Highest Streak is " + Highest );
            Log.d(TAG,"SSD Triumph card has been changed to  " + game.triumphCard);

        }
        else{
            Log.d(TAG,"SSD --------- Saving Streak Data FAILED -------- ");
        }
    }

    public Cursor getStreakData(){

        String query= "SELECT * FROM " + Streak;

        SQLiteDatabase db = this.getWritableDatabase();

        return db.rawQuery(query,null);
    }
}