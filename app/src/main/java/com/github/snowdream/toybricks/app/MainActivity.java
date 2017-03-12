package com.github.snowdream.toybricks.app;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
//import android.widget.TextView;
//import com.github.snowdream.kotlin.helloworld.IKotlinText;
//import com.github.snowdream.toybricks.ToyBricks;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //IText text = ToyBricks.getImplementation(IText.class);
        //IKotlinText ktext = ToyBricks.getImplementation(IKotlinText.class);
        //
        //StringBuilder builder = new StringBuilder();
        //builder.append(text.getText());
        //builder.append("\n\n\n");
        //builder.append(ktext.getText());

        //((TextView)findViewById(R.id.text)).setText(builder.toString());
    }
}
