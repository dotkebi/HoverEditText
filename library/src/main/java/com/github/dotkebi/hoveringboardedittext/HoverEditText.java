package com.github.dotkebi.hoveringboardedittext;

import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.text.InputType;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

/**
 * @author dotkebi@gmail.com on 2015. 11. 18..
 */
public class HoverEditText extends RelativeLayout {
    private static final String Tag = "HoverEditText";

    private Context context;

    private EditText editText;
    private LinearLayout hoverContainer;

    private WindowManager windowManager;
    private WindowManager.LayoutParams stickyParams;

    private int actionBarHeight;
    private int softKeyHeight;

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

        /*int hoverHeight;
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.HoverEditText);
        if (typedArray != null) {
            hoverHeight = typedArray.getDimensionPixelOffset(R.styleable.HoverEditText_hoverBoardHeight, );
            typedArray.recycle();
        }*/
        editText = createEditText(attrs);
        editText.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
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
        //((Activity) context).getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        checkKeyboardHeight(this);

        hoverContainer = createHoverBoard();
        hoverContainer.setVisibility(GONE);

        stickyParams = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_APPLICATION,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);
        stickyParams.gravity = Gravity.START | Gravity.BOTTOM;
        stickyParams.y = 300;
        //stickyParams.softInputMode = WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE;

        final TypedArray styledAttributes = getContext().getTheme().obtainStyledAttributes(
                new int[] { android.R.attr.actionBarSize });
        actionBarHeight = (int) styledAttributes.getDimension(0, 0);

        DisplayMetrics metrics = new DisplayMetrics();

        WindowManager windowManager = ((Activity) context).getWindowManager();
        windowManager.getDefaultDisplay().getMetrics(metrics);
        int usableHeight = metrics.heightPixels;

        windowManager.getDefaultDisplay().getRealMetrics(metrics);
        int realHeight = metrics.heightPixels;
        softKeyHeight = realHeight - usableHeight;
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (windowManager == null) {
            windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
            windowManager.addView(hoverContainer, stickyParams);
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (windowManager != null) {
            windowManager.removeView(hoverContainer);
            windowManager = null;
        }
    }

    @Override
    public boolean dispatchKeyEventPreIme(@NonNull KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_BACK
                && getKeyDispatcherState() != null) {
            Log.w(Tag, "back!");
            if (event.getAction() == KeyEvent.ACTION_DOWN) {
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

        final TextView textView = (TextView) container.findViewById(R.id.btn);
        textView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                editText.append(textView.getText().toString());
            }
        });
        return container;
    }

    private void checkKeyboardHeight(final View parentLayout) {
        parentLayout.getViewTreeObserver().addOnGlobalLayoutListener(
                new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        Rect r = new Rect();
                        parentLayout.getWindowVisibleDisplayFrame(r);

                        int screenHeight = parentLayout.getRootView().getHeight();
                        int difference = screenHeight - (r.bottom - r.top);
                        //if (difference > 100) {
                            stickyParams.y = difference - actionBarHeight / 2 - softKeyHeight;
                            //stickyParams.y = (screenHeight - actionBarHeight) / 2 - difference;
                            windowManager.updateViewLayout(hoverContainer, stickyParams);
                            //Log.w(Tag, "keyboard height " + stickyParams.y);
                        //}
                    }
                });

    }

}
