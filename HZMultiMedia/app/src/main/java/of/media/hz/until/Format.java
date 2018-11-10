package of.media.hz.until;

import java.text.SimpleDateFormat;

/**
 * Created by MR.XIE on 2018/11/7.
 * 转化格式
 */
public class Format {

    //毫秒转变为00:00格式
    public  static String changeToTime(int time){
        SimpleDateFormat simpleDateFormat=new SimpleDateFormat("mm:ss");
      return   simpleDateFormat.format(time);
    }
}
