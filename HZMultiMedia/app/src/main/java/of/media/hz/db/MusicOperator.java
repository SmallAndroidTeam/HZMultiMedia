package of.media.hz.db;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import of.media.hz.Application.App;
import of.media.hz.info.MusicName;


public class MusicOperator {
    private   MusicOpenHelper dbHelper;
    private   SQLiteDatabase db;
    private static MusicOperator musicOperator;
  private String TAG="Music";

  public static MusicOperator getInstatce(){
      if(musicOperator==null){
       musicOperator=new MusicOperator(App.sContext);
      }
      return musicOperator;
  }

    public MusicOperator(Context context) {
        dbHelper = new MusicOpenHelper(context, "musicData", null, 1);
            db = dbHelper.getWritableDatabase();
    }

    // 添加联系人
    public void add(MusicName lxr) {
        Log.i(TAG, "add: name"+lxr.getName());
        Log.i(TAG, "add:artist"+lxr.getArtist());
        Log.i(TAG, "add path: "+lxr.getImage());
        Log.i(TAG, "add path: "+lxr.getUri());
        Log.i(TAG, "add path: "+lxr.getLrc_uri());
        db.execSQL("insert into musicData(name,artist,image,uri,Lrc_uri) values(?,?,?,?,?)",
                new Object[] { lxr.getName() ,lxr.getArtist(),lxr.getImage(),lxr.getUri(),lxr.getLrc_uri()});

    }


    // 删除联系人
    public void delete(String name) {
        db.execSQL("delete from musicData where name=?", new String[] { name });
    }

    // 查询联系人
    public MusicName queryOne(String name) {
        MusicName lxr = new MusicName();
        Cursor c = db.rawQuery("select * from musicData where name= ?", new String[] { name });
        while (c.moveToNext()) {
            lxr.setName(c.getString(0));
            lxr.setArtist(c.getString(1));
           lxr.setImage(c.getString(2));
           lxr.setUri(c.getString(3));
           lxr.setLrc_uri(c.getString(4));
        }
        c.close();
        return lxr;
    }

    public List<MusicName> queryAlllxr() {
        List<MusicName> lxrs = new ArrayList<MusicName>();
        Cursor c = db.rawQuery("select name from musicData", null);

        while (c.moveToNext()) {
            MusicName lxr = new MusicName();
            lxr.setName(c.getString(0));
            lxrs.add(lxr);
            Log.i(TAG, "queryAlllxr: "+c.getString(0));
        }
        c.close();
        return lxrs;

    }

    // 查询所有的联系人信息
    public List<MusicName> queryMany() {
        ArrayList<MusicName> lxrs = new ArrayList<MusicName>();
        Cursor c = db.rawQuery("select * from musicData", null);
        while (c.moveToNext()) {
            MusicName lxr = new MusicName();
            lxr.setName(c.getString(0));
            lxr.setArtist(c.getString(1));
            lxr.setImage(c.getString(2));
            lxr.setUri(c.getString(3));
            lxr.setLrc_uri(c.getString(4));
            lxrs.add(lxr);
        }
        c.close();
        return lxrs;
    }

    // 检验用户名是否已存在
    public  boolean CheckIsDataAlreadyInDBorNot(String value) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        String Query = "Select * from musicData where name =?";
        Cursor cursor = db.rawQuery(Query, new String[] { value });
        Log.i(TAG, "add: "+cursor.getCount());
        if (cursor.getCount() > 0) {
            cursor.close();
            return true;
        }
        cursor.close();
        return false;
    }

    // 判断信息是否已经存在
    public boolean Dataexist(String name1,String artist,String image,String uri,String Lrc_uri) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        String Query = "Select name from musicData where name =? and artist=? and image=?and uri=?and Lrc_uri ";
        Cursor cursor = db.rawQuery(Query, new String[] { name1,artist ,image,uri,Lrc_uri});
        if (cursor.getCount() > 0) {
            cursor.close();
            return true;
        }
        cursor.close();
        return false;
    }

}