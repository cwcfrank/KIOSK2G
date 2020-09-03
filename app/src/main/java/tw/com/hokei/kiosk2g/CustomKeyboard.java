package tw.com.hokei.kiosk2g;

import android.app.Activity;
import android.content.Context;
import android.inputmethodservice.InputMethodService;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.KeyboardView;
import android.media.AudioManager;
import android.os.Build;
import android.text.Editable;
import android.text.InputType;
import android.util.Log;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputConnection;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

public class CustomKeyboard extends InputMethodService implements KeyboardView.OnKeyboardActionListener {

    private Context mContext;
    private KeyboardView kv;
    private Keyboard keyboard;
    private Keyboard numberKeyboard;
    private EditText edittext;
    private Activity mHostActivity;
    private EditText editText;
    private boolean isCaps = false;
    private OnKeyboardEventCallback onKeyboardShowCallback;
    private static boolean isKeyboardShow = false;
    private int layoutIdForChk;
    private boolean customKeyboardChk = true;
    private OnKeyPressListener onKeyPressListener;

    @Override
    public void onPress(int i) {

    }

    @Override
    public void onRelease(int i) {

    }

    @Override
    public void onKey(int primaryCode, int[] ints) {
        if(onKeyPressListener != null){
            onKeyPressListener.onKeyPress(primaryCode, ints);
        }
        Log.i("test", Integer.toString(primaryCode));
        if(primaryCode!=keyboard.KEYCODE_DONE) {
            InputConnection ic = getCurrentInputConnection();
            View focusCurrent = mHostActivity.getCurrentFocus();
            EditText edittext = (EditText) focusCurrent;
            Editable editable = null;
            int selectionStart = 0;
            int selectionEnd = 0;

            // Apply the key to the edittext


            if (edittext.isFocused()) {
                editable = edittext.getText();
                selectionStart = edittext.getSelectionStart();
                selectionEnd = edittext.getSelectionEnd();
            }
            if (primaryCode == keyboard.KEYCODE_CANCEL) {
                hideCustomKeyboard();

            } else if (primaryCode == keyboard.KEYCODE_DELETE) {
                if (editable.length() > 0) {
                    if (selectionStart >= 0) {
                        if (selectionStart == selectionEnd && selectionStart != 0) {
                            editable.delete(selectionStart - 1, selectionEnd);
                        } else {
                            editable.delete(selectionStart, selectionEnd);
                        }
                    }
                }
            }  else if (primaryCode == -10) {
                if (editable.length() > 0) {
                    editable.clear();
                }
            } else if (primaryCode == keyboard.KEYCODE_SHIFT) {
                isCaps = !isCaps;
                keyboard.setShifted(isCaps);
                kv.invalidateAllKeys();
            } else {

                //editable.insert(selectionStart, Character.toString((char) primaryCode));
                char code = (char) primaryCode;
                if (Character.isLetter(code) && isCaps) {
                    code = Character.toUpperCase(code);
                }
                if (selectionStart != selectionEnd) {
                    editable.delete(selectionStart, selectionEnd);
                    Log.i("selectionStart", Integer.toString(selectionStart));
                    editable.insert(selectionStart, Character.toString(code));
                } else {
                    editable.insert(selectionStart, Character.toString(code));

                }
            }
        }else{
            hideCustomKeyboard();
        }
    }


    private void playClick(int i) {
        AudioManager am = (AudioManager) getSystemService(AUDIO_SERVICE);
        switch (i) {
            case 32:
                am.playSoundEffect(AudioManager.FX_KEYPRESS_SPACEBAR);
                break;
            case Keyboard.KEYCODE_DONE:
            case 10:
                am.playSoundEffect(AudioManager.FX_KEYPRESS_RETURN);
                break;
            case Keyboard.KEYCODE_DELETE:
                am.playSoundEffect(AudioManager.FX_KEYPRESS_DELETE);
                break;
            default:
                am.playSoundEffect(AudioManager.FX_KEYPRESS_STANDARD);
        }
    }


    @Override
    public void onText(CharSequence charSequence) {

    }

    @Override
    public void swipeLeft() {

    }

    @Override
    public void swipeRight() {

    }

    @Override
    public void swipeDown() {

    }

    @Override
    public void swipeUp() {

    }

    public CustomKeyboard(Activity activity, KeyboardView kv, OnKeyboardEventCallback mOnKeyboardShowCallback) {
        this.onKeyboardShowCallback = mOnKeyboardShowCallback;
        mHostActivity = activity;
        this.kv = kv;
    }

    public void setLayout(int layoutid) {
        layoutIdForChk = layoutid;
        keyboard = new Keyboard(mHostActivity, layoutid);
        numberKeyboard = new Keyboard(mHostActivity, R.xml.numberic_keyboard);
        kv.setKeyboard(keyboard);
        kv.setEnabled(true);
        kv.setPreviewEnabled(false);
        kv.setOnKeyboardActionListener(this);
        if (isKeyboardShow == false) {
            //kv.setTranslationY(1000f);
            kv.setVisibility(View.INVISIBLE);
        }
        mHostActivity.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        //hideSoftKeyboard();
    }

    public int getLayout() {
        return layoutIdForChk;
    }
    public boolean isCustomKeyboardVisible() {
        return kv.getVisibility() == View.VISIBLE;
    }

    public boolean isCustomKeyboardShow() {
        return isKeyboardShow;
    }

    public void showKeyboard(View v) {
        if (customKeyboardChk) {
            //show custom keyboard, hide soft keyboard
            showCustomKeyboard();
            if (v != null) {
                hideSoftKeyboard(v);
            }
        } else {
            //show soft keyboard, hide custom keyboard
            if (v != null) {
                hideCustomKeyboard();
                showSoftKeyboard(v);
            }
        }

    }

    public void showCustomKeyboard() {
        kv.setVisibility(View.VISIBLE);
        isKeyboardShow = true;
        kv.setEnabled(true);
    }

    public void hideCustomKeyboard() {
        isKeyboardShow = false;
        kv.setVisibility(View.GONE);
        kv.setEnabled(false);
        Log.d("0419","hideCustomKeyboard Call");
    }

    public void hideSoftKeyboard(View v) {
        //disable soft keyboard showing, while keeping the copy and paste and cursor function
        //mHostActivity.getWindow().setFlags(WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM,
        //      WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
        ((InputMethodManager) mHostActivity.getSystemService(Activity.INPUT_METHOD_SERVICE)).
                hideSoftInputFromWindow(v.getWindowToken(), 0);
        Log.d("HideSoftKeyboard","active");
    }

    public void showSoftKeyboard(View v) {
        ((InputMethodManager) mHostActivity.getSystemService(Activity.INPUT_METHOD_SERVICE))
                .showSoftInput(v, 0);
    }


    public void registerEditText(final int resid) {
        // Find the EditText 'resid'
        edittext = (EditText) mHostActivity.findViewById(resid);
        // Make the custom keyboard appear
        edittext.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            // NOTE By setting the on focus listener, we can show the custom keyboard when the edit box gets focus, but also hide it when the edit box loses focus
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    showKeyboard(v);
                    onKeyboardCallback(resid);

                } else {
                    if(!customKeyboardChk){
                        hideSoftKeyboard(v);
                    }

                }
            }
        });
        edittext.setOnClickListener(new View.OnClickListener() {
            // NOTE By setting the on click listener, we can show the custom keyboard again, by tapping on an edit box that already had focus (but that had the keyboard hidden).
            @Override
            public void onClick(View v) {
                showKeyboard(v);
                onKeyboardCallback(resid);
            }
        });

        edittext.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {

                return false;
            }
        });

        edittext.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                EditText edittext = (EditText) v;
                edittext.setCustomSelectionActionModeCallback(new ActionMode.Callback() {
                    @Override
                    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                        return false;
                    }

                    @Override
                    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                        return false;
                    }

                    @Override
                    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                        return false;
                    }

                    @Override
                    public void onDestroyActionMode(ActionMode mode) {

                    }
                });
                int inType = edittext.getInputType();       // Backup the input type

                edittext.onTouchEvent(event);               // Call n   ative handler
                edittext.setInputType(inType);              // Restore input type
                //edittext.setCursorVisible(true);
                edittext.requestFocus();
                return true; // Consume touch event
            }
        });
        // Disable spell check (hex strings look like words to Android)
        edittext.setInputType(edittext.getInputType() | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
    }

    public void onKeyboardCallback(int editTextId) {
        onKeyboardShowCallback.onKeyboardShow(editTextId);
    }

    public void setCustomKeyboardChk(boolean chk) {
        customKeyboardChk = chk;
    }

    public boolean customKeyboardChk() {
        if (customKeyboardChk) {
            return true;
        } else {
            return false;
        }

    }
    public interface OnKeyPressListener {
        void onKeyPress(int primaryCode, int[] ints);
    }
    public void setOnKeyPressListener(OnKeyPressListener listener){
        onKeyPressListener = listener;
    }
    private void hideNavBar(View v) {
        if (Build.VERSION.SDK_INT >= 19) {
            v.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        }
    }

}