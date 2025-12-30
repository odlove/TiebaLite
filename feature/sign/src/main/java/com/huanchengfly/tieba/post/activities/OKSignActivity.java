package com.huanchengfly.tieba.post.activities;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.huanchengfly.tieba.post.sign.SignActions;

public class OKSignActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SignActions.startSign(this);
        finish();
    }
}
