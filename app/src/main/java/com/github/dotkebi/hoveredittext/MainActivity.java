package com.github.dotkebi.hoveredittext;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.github.dotkebi.hoveringboardedittext.HoverEditText;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final HoverEditText hoverEditText = (HoverEditText) findViewById(R.id.hover);

        assert hoverEditText != null;
        final View hoverBoard = hoverEditText.getView();

        hoverBoard.findViewById(R.id.btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hoverEditText.append("1");
            }
        });
    }
}
