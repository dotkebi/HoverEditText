package com.github.dotkebi.hoveredittext;

import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

/**
 * @author dotkebi on 2015. 11. 18..
 */
public class HoverEditText extends RelativeLayout {
    private static final String Tag = "HoverEditText";

    private Context context;

    private EditText editText;
    private LinearLayout hoverContainer;

    private boolean keyboards;

    public HoverEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }


    private void init(Context context, AttributeSet attrs) {
        this.context = context;

        keyboards = false;
        /*int[] attrsArray = new int[] {
                android.R.attr.id
                , android.R.attr.background
                , android.R.attr.layout_width
                , android.R.attr.layout_height
        };*/

        int hoverHeight;
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.HoverEditText);
        if (typedArray != null) {
            //hoverHeight = typedArray.getDimensionPixelOffset(R.styleable.HoverEditText_hoverBoardHeight, );

            typedArray.recycle();
        }
        editText = createEditText(attrs);
        editText.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (v == editText) {
                    showKeyboard();
                    return true;
                }
                return false;
            }
        });
        editText.setOnFocusChangeListener(new OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    hideKeyboard();
                }
            }
        });
        this.addView(editText);

        hoverContainer = createHoverBoard();
        hoverContainer.setVisibility(GONE);

        LayoutParams params = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);

        hoverContainer.setLayoutParams(params);
        this.addView(hoverContainer);
        ((Activity) context).getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
    }


    @Override
    public boolean dispatchKeyEventPreIme(@NonNull KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_BACK
                && getKeyDispatcherState() != null) {
            Log.w(Tag, "back!");
            if (event.getAction() == KeyEvent.ACTION_DOWN
                    //|| event.getAction() == KeyEvent.ACTION_UP
                    ) {
                hideKeyboard();
                return true;
            }
        }
        return super.dispatchKeyEventPreIme(event);
    }

    private void showKeyboard() {
        if (keyboards) {
            return;
        }
        Log.w(Tag, "show keyboard");
        keyboards = true;
        editText.requestFocus();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.showSoftInput(editText, InputMethodManager.SHOW_FORCED);
                toggleHoverBoard(true);
            }
        }, 200);
    }

    private void hideKeyboard() {
        editText.clearFocus();
        Log.w(Tag, "hide keyboard");
        toggleHoverBoard(false);
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);
        keyboards = false;
    }

    private void toggleHoverBoard(boolean flag) {
        hoverContainer.setVisibility((flag) ? VISIBLE : GONE);
    }

    private EditText createEditText(AttributeSet attrs) {
        return new EditText(context, attrs);
    }

    private LinearLayout createHoverBoard() {
        LinearLayout container = (LinearLayout) View.inflate(context, R.layout.hover_board, null);
        //container.generateLayoutParams()


        return container;
    }

}
