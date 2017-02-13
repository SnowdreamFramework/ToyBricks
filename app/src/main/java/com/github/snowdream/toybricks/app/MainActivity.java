package com.github.snowdream.toybricks.app;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;
import com.github.snowdream.toybricks.ToyBricks;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        IText text = ToyBricks.getImplementation(IText.class);

        ((TextView)findViewById(R.id.text)).setText(text.getText());
    }
}
