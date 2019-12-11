package dev.jorgecastillo.androidlintdocs;

import android.content.Context;
import android.content.res.ColorStateList;
import android.util.AttributeSet;

import androidx.appcompat.widget.AppCompatTextView;

import com.google.android.material.textfield.TextInputLayout;

public class SomeJavaTextView extends AppCompatTextView {

    public SomeJavaTextView(Context context) {
        this(context, null);
    }

    public SomeJavaTextView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SomeJavaTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        ColorStateList colors = getResources().getColorStateList(R.color.colorAccent);
        TextInputLayout input = new TextInputLayout(getContext());
        input.setError("");
    }
}
