package com.github.dotkebi.hoveringboardedittext;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.os.Build;
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
import android.widget.RelativeLayout;

/**
 * @author dotkebi@gmail.com on 2015. 11. 18..
 */
public class HoverEditText extends RelativeLayout {
    private static final String Tag = "HoverEditText";

    private Context context;

    private int hoverViewLayoutResId;

    private EditText editText;
    private View hoverContainer;

    private WindowManager windowManager;
    private WindowManager.LayoutParams stickyParams;

    private int hoverBoardHeight;
    private int softKeyHeight;

    private boolean keyboards;

    public HoverEditText(Context context) {
        super(context);
        this.context = context;
    }

    public HoverEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        init(attrs, 0, 0);
    }

    public HoverEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        init(attrs, defStyleAttr);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public HoverEditText(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        this.context = context;
        init(attrs, defStyleAttr, defStyleRes);
    }

    private void init(AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        TypedArray a = context.getTheme().obtainStyledAttributes(
                attrs, R.styleable.HoverEditText, defStyleAttr, defStyleRes
        );

        try {
            hoverBoardHeight = (int) a.getDimension(R.styleable.HoverEditText_hoverBoardHeight, 0);
            hoverViewLayoutResId = a.getResourceId(R.styleable.HoverEditText_hoverBoardLayout, 0);
        } finally {
            a.recycle();
        }
        init(attrs);
    }


    private void init(AttributeSet attrs, int defStyleAttr) {
        init(attrs, defStyleAttr, 0);
    }

    private void init(AttributeSet attrs) {
        keyboards = false;

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
        getHoverboardHeight();

        stickyParams = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_APPLICATION,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);
        stickyParams.gravity = Gravity.START | Gravity.BOTTOM;
        stickyParams.y = 300;
        //stickyParams.softInputMode = WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE;

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

    /*private LinearLayout createHoverBoard() {
        LinearLayout container = (LinearLayout) View.inflate(context, R.layout.hover_board, null);

        final TextView textView = (TextView) container.findViewById(R.id.btn);
        textView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                editText.append(textView.getText().toString());
            }
        });
        return container;
    }*/

    private View createHoverBoard() {
        if (hoverContainer == null && hoverViewLayoutResId != 0) {
            hoverContainer = View.inflate(context, hoverViewLayoutResId, null);
        }
        if (hoverContainer != null) {
            hoverContainer.setVisibility(GONE);
        }
        return hoverContainer;
    }

    private void getHoverboardHeight() {
        ViewTreeObserver viewTreeObserver = hoverContainer.getViewTreeObserver();
        viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                hoverBoardHeight = hoverContainer.getMeasuredHeight();
                if (hoverBoardHeight > 0) {
                    hoverContainer.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                }
            }
        });
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
                        stickyParams.y = difference - hoverBoardHeight / 2 - softKeyHeight;
                        windowManager.updateViewLayout(hoverContainer, stickyParams);
                    }
                });
    }

    /**********************************************************************************************
     * public methods
     **********************************************************************************************/
    public void setView(View view) {
        hoverViewLayoutResId = 0;
        hoverContainer = view;
        //getHoverboardHeight();
    }

    public void setView(int layoutResId) {
        hoverViewLayoutResId = layoutResId;
        hoverContainer = null;
        createHoverBoard();
    }

}
