package tw.com.hokei.kiosk2g;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;

import pl.droidsonroids.gif.GifImageView;

import static android.view.KeyEvent.ACTION_DOWN;
import static android.view.MotionEvent.ACTION_UP;

public class GifImageButtonView extends GifImageView {
    private Drawable gifDrawable;
    private Drawable bkDrawable;

    public GifImageButtonView(Context context) {
        super(context);
    }

    public GifImageButtonView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public GifImageButtonView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        switch (event.getAction()) {
            case ACTION_UP :
                setImageDrawable(gifDrawable);
                setBackground(null);
                break;
            case ACTION_DOWN :
                setImageDrawable(null);
                setBackground(bkDrawable);
                break;
        }

        return super.onTouchEvent(event);
    }

    @Override
    public void setImageDrawable(@Nullable Drawable drawable) {
        super.setImageDrawable(drawable);
    }

    public void saveImageDrawable(@Nullable Drawable drawable) {
        gifDrawable = drawable;
    }

    public void saveImageBackroundDrawable(@Nullable Drawable drawable) {
        bkDrawable = drawable;
    }
}
