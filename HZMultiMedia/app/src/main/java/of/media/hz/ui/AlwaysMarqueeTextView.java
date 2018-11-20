package of.media.hz.ui;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;

/**
 * Created by MR.XIE on 2018/8/16.
 */
public class AlwaysMarqueeTextView extends android.support.v7.widget.AppCompatTextView {
    public AlwaysMarqueeTextView(Context context) {
        super(context);
    }

    public AlwaysMarqueeTextView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public AlwaysMarqueeTextView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public boolean isFocused() {
        return true;
    }
}
