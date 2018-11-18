package of.media.hz.adapter;

import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.io.File;
import java.io.ObjectInput;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import of.media.hz.Music;
import of.media.hz.R;
import of.media.hz.services.MusicService;
import of.media.hz.until.MusicIconLoader;

/**
 * Created by MR.XIE on 2018/11/18.
 */
public class LocalMusicAdapter extends BaseAdapter {

    private  List<Music> musicList=new ArrayList<>();//歌曲列表
    private int playIndex=-1;//播放的音乐下标

    public void setMusicList(List<Music> musicList) {
        this.musicList = musicList;
    }

    public void setPlayIndex(int playIndex) {
        this.playIndex = playIndex;
    }

    public int getPlayIndex() {
        return playIndex;
    }

    public List<Music> getMusicList() {
        return musicList;
    }

    @Override
    public int getCount() {
        return musicList.size();
    }

    @Override
    public Object getItem(int i) {
        return musicList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
       final ViewHolder viewHolder;
       if(view==null){
         view= LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.local_music_adapter_item,viewGroup,false);
           ImageView localMusicAlbum=view.findViewById(R.id.localMusicAlbum);
           TextView localMusicNameAndAritist=view.findViewById(R.id.localMusicNameAndAritist);
           TextView localMusicSize=view.findViewById(R.id.localMusicSize);
           ImageView playingImage=view.findViewById(R.id.playingImage);
           viewHolder=new ViewHolder(localMusicAlbum,localMusicNameAndAritist,localMusicSize,playingImage);
           view.setTag(viewHolder);
       }else{
           viewHolder= (ViewHolder) view.getTag();
       }

         if(musicList.get(i).getImage()==null){
             viewHolder.localMusicAlbum.setImageResource(R.drawable.mp1);
         }else{
             if(Objects.requireNonNull(musicList.get(i).getImage().startsWith("http"))){//如果是网络图片
                 Glide.with(Objects.requireNonNull(viewGroup.getContext())).load(musicList.get(i).getImage()).into(viewHolder.localMusicAlbum);
             }else{
                 Bitmap bitmap= MusicIconLoader.getInstance().load(musicList.get(i).getImage());
                 if(bitmap!=null){
                     viewHolder.localMusicAlbum.setImageBitmap(bitmap);
                 }else{
                     viewHolder.localMusicAlbum.setImageResource(R.drawable.mp1);
                 }
             }
         }


           viewHolder.localMusicNameAndAritist.setText(musicList.get(i).getTitle()+"-"+musicList.get(i).getArtist());

          File musicFile=new File(musicList.get(i).getUri());
          if(musicFile.exists()){
              viewHolder.localMusicSize.setText(String.format("%.2f",1.0*musicFile.length()/1024/1024)+"M");
          }else{
              viewHolder.localMusicSize.setVisibility(View.INVISIBLE);
          }

          if(playIndex==-1||i!=playIndex){
              viewHolder.playingImage.setVisibility(View.INVISIBLE);
              viewHolder.localMusicNameAndAritist.setTextColor(viewGroup.getContext().getResources().getColor(R.color.localMusicTextNoSelect));
          }else{
              viewHolder.localMusicNameAndAritist.setTextColor(viewGroup.getContext().getResources().getColor(R.color.localMusicTextSelect));
              viewHolder.playingImage.setVisibility(View.VISIBLE);
          }
        return view;
    }
    class ViewHolder{
     ImageView localMusicAlbum;
     TextView localMusicNameAndAritist;
     TextView localMusicSize;
     ImageView playingImage;

        public ViewHolder(ImageView localMusicAlbum, TextView localMusicNameAndAritist, TextView localMusicSize, ImageView playingImage) {
            this.localMusicAlbum = localMusicAlbum;
            this.localMusicNameAndAritist = localMusicNameAndAritist;
            this.localMusicSize = localMusicSize;
            this.playingImage = playingImage;
        }
    }
}
