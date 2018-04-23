package com.linkyview.controlpanel

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        cantroPanel.setMenuListener { if (it != -1) Log.e("flag--", "(:)-->>$it") }
    }
}
