package of.media.hz.adapter;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;



import java.util.ArrayList;
import java.util.List;

import of.media.hz.R;

/**
 * 歌词适配器
 * Created by MR.XIE on 2018/11/10.
 */
public class LryicAdapter  extends BaseAdapter{

    private List<String> mLrcs = new ArrayList<String>(); // 存放歌词
    private  int index=-1;//显示歌词的下标


    public List<String> getmLrcs() {
        return mLrcs;
    }

    public void setmLrcs(List<String> mLrcs) {
        this.mLrcs.clear();
        this.mLrcs.addAll(mLrcs);
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    @Override
    public int getCount() {
        return mLrcs.size();
    }

    @Override
    public Object getItem(int i) {
        return mLrcs.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        final    ViewHolder viewHolder;
        if(view==null){
            view= LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.lyric_adapter_item,viewGroup,false);
            TextView oneLineLyric=view.findViewById(R.id.oneLineLyric);
            viewHolder=new ViewHolder(oneLineLyric);
            view.setTag(viewHolder);
        }else{
            viewHolder= (ViewHolder) view.getTag();
        }
        viewHolder.oneLineLyric.setText(mLrcs.get(i));
        if(index==-1){
            viewHolder.oneLineLyric.setTextColor(viewGroup.getContext().getResources().getColor(R.color.textNoSelect));
        }else if(i==index){
            viewHolder.oneLineLyric.setTextColor(viewGroup.getContext().getResources().getColor(R.color.textSelect));
        }else{
            viewHolder.oneLineLyric.setTextColor(viewGroup.getContext().getResources().getColor(R.color.textNoSelect));
        }
        return view;
    }
     class  ViewHolder{
        TextView oneLineLyric;
         public ViewHolder(TextView oneLineLyric) {
             this.oneLineLyric = oneLineLyric;
         }

     }
}
