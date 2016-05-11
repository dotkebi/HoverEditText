package com.github.dotkebi.hoveredittext;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.github.dotkebi.hoveringboardedittext.HoverViewContainer;
import com.github.dotkebi.hoveringboardedittext.RootViewChangeListener;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final HoverViewContainer hoverViewContainer = (HoverViewContainer) findViewById(R.id.hover);
        final EditText editText = (EditText) findViewById(R.id.inside);
        assert hoverViewContainer != null;
        assert editText != null;

        hoverViewContainer.setRootViewChangeListener(new RootViewChangeListener() {
            @Override
            public void rootViewChangeListener(boolean visibility) {
                Toast.makeText(MainActivity.this
                        , visibility ? R.string.appear : R.string.disappear
                        , Toast.LENGTH_SHORT).show();
            }
        });

        final View hoverBoard = hoverViewContainer.getView();

        if (hoverBoard != null) {
            hoverBoard.findViewById(R.id.btn).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    editText.getText().clear();
                }
            });
            hoverBoard.findViewById(R.id.btn2).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    editText.setText("");
                }
            });
        }
    }
}
