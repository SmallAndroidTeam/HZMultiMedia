package of.media.hz.musicFragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import of.media.hz.R;

/**
 * Created by MR.XIE on 2018/11/6.
 */
public class LocalRadioFragment extends Fragment{
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.local_radio_fragment_item, container,false);
        return view;
    }
}

