package com.app.permissiondemo

import android.Manifest
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.ImageView
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.appcompat.app.AppCompatActivity


class MainActivity : AppCompatActivity(), PermissionResultInterface {

    var permissionsList: ArrayList<String>? = null
    var permissionsStr = arrayOf(
        Manifest.permission.CAMERA,
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.READ_EXTERNAL_STORAGE
    )

    var cameraResultLauncher = registerForActivityResult(
        StartActivityForResult()
    ) { result: ActivityResult ->
        if (result.resultCode == RESULT_OK) {
            // There are no request codes
            val img = findViewById<ImageView>(R.id.img)
            img.setImageBitmap(result.data!!.extras!!["data"] as Bitmap?)
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        permissionsList = ArrayList()
        permissionsList!!.addAll(permissionsStr)
        val btn = findViewById<Button>(R.id.btn)
        val permissionHelper = PermissionHelper(this, permissionsStr, this, 100)
        btn.setOnClickListener {
            permissionHelper.askForPermissions(permissionsList!!)
        }
    }


    override fun permissionGranted(code: Int) {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        cameraResultLauncher.launch(takePictureIntent)
    }
}