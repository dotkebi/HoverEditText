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
import android.text.Editable;
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
import android.widget.Toast;

/**
 * @author dotkebi@gmail.com on 2015. 11. 18..
 */
public class HoverViewContainer extends RelativeLayout implements View.OnFocusChangeListener {
    private static final String Tag = "HoverEditText";

    private Context context;

    private int hoverViewLayoutResId;
    //private int hoverViewBackgroundResId;

    //private EditText editText;
    private View hoverContainer;

    private WindowManager windowManager;
    private WindowManager.LayoutParams stickyParams;

    private int hoverBoardHeight;
    private int softKeyHeight;

    private boolean keyboards;

    private RootViewChangeListener rootViewChangeListener;
    public void setRootViewChangeListener(RootViewChangeListener rootViewChangeListener) {
        this.rootViewChangeListener = rootViewChangeListener;
    }

    public HoverViewContainer(Context context) {
        super(context);
        this.context = context;
    }

    public HoverViewContainer(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        if (!isInEditMode()) {
            init(attrs, 0, 0);
        }

    }

    public HoverViewContainer(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        if (!isInEditMode()) {
            init(attrs, defStyleAttr);
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public HoverViewContainer(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        this.context = context;
        if (!isInEditMode()) {
            init(attrs, defStyleAttr, defStyleRes);
        }
    }

    private void init(AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        TypedArray a = context.getTheme().obtainStyledAttributes(
                attrs, R.styleable.HoverViewContainer, defStyleAttr, defStyleRes
        );

        try {
            //hoverBoardHeight = (int) a.getDimension(R.styleable.HoverViewContainer_hoverBoardHeight, 0);
            hoverViewLayoutResId = a.getResourceId(R.styleable.HoverViewContainer_hoverBoardLayout, 0);
            //hoverViewBackgroundResId = a.getResourceId(R.styleable.HoverViewContainer_hoverBoardBackground, 0);
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

        //setBackgroundResource(hoverViewBackgroundResId);

        //editText = createEditText(attrs);
        //editText.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
       /* this.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                //if (v == editText) {
                    showKeyboard();
                    return true;
                //}
                //return false;
            }
        });
        this.setOnFocusChangeListener(new OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    hideKeyboard();
                }
            }
        });*/
        //this.addView(editText);
        //((Activity) context).getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        checkKeyboardHeight(this);

        hoverContainer = createHoverBoard();

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
        if (windowManager == null && hoverContainer != null) {
            windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
            windowManager.addView(hoverContainer, stickyParams);
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (windowManager != null && hoverContainer != null) {
            windowManager.removeView(hoverContainer);
            windowManager = null;
        }
    }

    @Override
    public boolean dispatchKeyEventPreIme(@NonNull KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_BACK
                && getKeyDispatcherState() != null) {
            //Log.w(Tag, "back!");
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
        //editText.requestFocus();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.showSoftInput(HoverViewContainer.this, InputMethodManager.SHOW_FORCED);
                toggleHoverBoard(true);
            }
        }, 200);
    }


    private void hideKeyboard() {
        //this.clearFocus();
        keyboards = false;
        toggleHoverBoard(false);
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(HoverViewContainer.this.getWindowToken(), 0);
    }

    private void toggleHoverBoard(boolean flag) {
        if (hoverContainer != null) {
            hoverContainer.setVisibility((flag) ? VISIBLE : GONE);
        }
        if (rootViewChangeListener != null) {
            rootViewChangeListener.rootViewChangeListener(flag);
        }
    }

    /*private EditText createEditText(AttributeSet attrs) {
        EditText editText = (EditText) View.inflate(context, R.layout.hover_edit_text_layout, null);
        for (int i = 0; i < attrs.getAttributeCount(); i++) {
            if (attrs.getAttributeName(i).equals("")) {


            }
        }
        return editText;
        //return new EditText(context);
    }*/

    private View createHoverBoard() {
        if (hoverContainer == null && hoverViewLayoutResId != 0) {
            hoverContainer = View.inflate(context, hoverViewLayoutResId, null);
        }
        if (hoverContainer != null) {
            hoverContainer.setVisibility(GONE);
            getHoverboardHeight();
        }
        return hoverContainer;
    }

    private void getHoverboardHeight() {
        if (hoverContainer == null) {
            return;
        }
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

    private void checkKeyboardHeight(final View view) {
        //final ViewGroup parentLayout = this;
        view.getViewTreeObserver().addOnGlobalLayoutListener(
                new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        if (hoverContainer == null) {
                            return;
                        }
                        Rect r = new Rect();
                        view.getWindowVisibleDisplayFrame(r);

                        int screenHeight = view.getRootView().getHeight();
                        int difference = screenHeight - (r.bottom - r.top);
                        stickyParams.y = difference - hoverBoardHeight / 2 - softKeyHeight;
                        windowManager.updateViewLayout(hoverContainer, stickyParams);
                    }
                });
    }

    /**********************************************************************************************
     * public methods
     **********************************************************************************************/

    /**
     * setView
     * @param view view
     */
    public void setView(View view) {
        hoverViewLayoutResId = 0;
        hoverContainer = view;
        //getHoverboardHeight();
    }

    /**
     * setView
     * @param layoutResId layour resource id
     */
    public void setView(int layoutResId) {
        hoverViewLayoutResId = layoutResId;
        hoverContainer = null;
        createHoverBoard();
    }

    /**
     * get inflated view of hoverBoard
     * @return view of hoverBoard
     */
    public View getView() {
        return hoverContainer;
    }

    /**
     * set root view for detecting showing keyboard
     * @param view root view
     */
    public void setRootView(View view) {
        if (view != null) {
            checkKeyboardHeight(view);
        }
    }

    public void disappearHoverWithKeyboard() {
        toggleHoverBoard(false);
    }


    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        View view = this.getFocusedChild();
        if (view != null) {
            Log.w(Tag, view.toString());
        }
        if (ev.getAction() == MotionEvent.ACTION_DOWN && !keyboards) {
            toggleHoverBoard(true);
            //showKeyboard();
        }
        //Toast.makeText(context, "intercepter", Toast.LENGTH_SHORT).show();
        return super.onInterceptTouchEvent(ev);
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        if (!hasFocus()) {
            toggleHoverBoard(false);
            //hideKeyboard();
        }
    }

}
