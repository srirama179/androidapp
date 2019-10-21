package com.example.satayam.mypetplayer;
import android.content.Context;
import android.database.Cursor;
import com.google.android.exoplayer2.util.Log;
import java.util.HashMap;
import p32929.androideasysql_library.Column;
import p32929.androideasysql_library.EasyDB;

public class DBHelper {
    private DBHelper(){}
    public HashMap<String, String> worddefMap = new HashMap<String, String>();
    public HashMap<String, String> wordOriginalMap = new HashMap<String, String>();
    public static DBHelper dbhelper = null;
    private EasyDB vocab = null;
    private EasyDB mapper = null;
    public static DBHelper getDBHelper(){
        if(dbhelper == null)
            dbhelper = new DBHelper();
        return dbhelper;
    }

    public void initDB(Context context){
        vocab = EasyDB.init(context, "WordVocab") // "TEST" is the name of the DATABASE
                .setTableName("VOCAB TABLE")  // You can ignore this line if you want
                .addColumn(new Column("C1", new String[]{"text", "unique"}))
                .addColumn(new Column("C2", new String[]{"text", "not null"}))
                .doneTableColumn();
        mapper = EasyDB.init(context, "MapWords") // "TEST" is the name of the DATABASE
                .setTableName("MAPPER TABLE")  // You can ignore this line if you want
                .addColumn(new Column("C1", new String[]{"text", "unique"}))
                .addColumn(new Column("C2", new String[]{"text", "not null"}))
                .doneTableColumn();
    }


    public void addWordDefinition(String originalWord, String word, String definition){
        vocab.addData(1, word)
                .addData(2, definition)
                .doneDataAdding();
        mapper.addData(1, originalWord)
                .addData(2, word)
                .doneDataAdding();
        worddefMap.put(word, definition);
        if(originalWord.equals(word) == false)
            wordOriginalMap.put(originalWord, word);
    }

    public String getDefinition(String word){
        String definition = worddefMap.get(word);
        if(definition == null){
            String originalWord = wordOriginalMap.get(word);
            return worddefMap.get(originalWord);
        }
        return  definition;
    }

    public void getAllWordsAndDefinitions(){
        if(vocab != null) {
            Cursor res = vocab.getAllData();
            while (res.moveToNext()) {
                String word = res.getString(1);
                String definition = res.getString(2);
                worddefMap.put(word, definition);
                Log.d("DBHelper", word + "-" + definition);
            }
        }

        if(mapper != null){
            Cursor res = mapper.getAllData();
            while (res.moveToNext()) {
                String originalWord = res.getString(1);
                String word = res.getString(2);
                wordOriginalMap.put(originalWord, word);
                Log.d("DBHelper", originalWord + "-" + word);
            }
        }
    }
}
