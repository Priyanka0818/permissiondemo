package com.app.permissiondemo

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

/**
 * Created by Priyanka.
 */
class PermissionHelper(
    val activity: AppCompatActivity, permissionsStr: Array<String>,
    permissionResultInterface: PermissionResultInterface, permissionCode: Int
) {
    var permissionsCount = 0
    var alertDialog: AlertDialog? = null
    var permissionsList: ArrayList<String>? = null
    val settingActivityResultLauncher =
        activity.registerForActivityResult(
            StartActivityForResult()
        ) { result: ActivityResult ->
            permissionsList = ArrayList()
            permissionsCount = 0
            for (i in permissionsStr.indices) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (activity.shouldShowRequestPermissionRationale(permissionsStr[i])) {
                        permissionsList!!.add(permissionsStr[i])
                    } else if (!hasPermission(activity, permissionsStr[i])) {
                        permissionsCount++
                    }
                }
            }
            if (permissionsList!!.size > 0) {
                Toast.makeText(activity, "Permission not granted", Toast.LENGTH_SHORT).show()
            } else if (permissionsCount > 0) {
                Toast.makeText(activity, "Permission not granted", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(activity, "Permission granted", Toast.LENGTH_SHORT).show()
            }
        }
    var permissionsLauncher = activity.registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        val list: ArrayList<Boolean> = ArrayList(result?.values!!)
        permissionsList = ArrayList()
        permissionsCount = 0
        for (i in 0 until list.size) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (activity.shouldShowRequestPermissionRationale(permissionsStr[i])) {
                    permissionsList!!.add(permissionsStr[i])
                } else if (!hasPermission(activity, permissionsStr[i])) {
                    permissionsCount++
                }
            } else {
                permissionResultInterface.permissionGranted(permissionCode)
            }
        }
        if (permissionsList!!.size > 0) {
            //Some permissions are denied and can be asked again.
            askForPermissions(permissionsList!!)
        } else if (permissionsCount > 0) {
            //Show alert dialog
            showPermissionDialog()
        } else {
            permissionResultInterface.permissionGranted(permissionCode)
        }
    }

    fun askForPermissions(permissionsList: ArrayList<String>) {
        val newPermissionStr = arrayOf(permissionsList.size.toString())
        for (i in newPermissionStr.indices) {
            newPermissionStr[i] = permissionsList[i]
        }
        if (newPermissionStr.isNotEmpty()) {
            permissionsLauncher.launch(newPermissionStr)
        } else {
            /* User has pressed 'Deny & Don't ask again' so we have to show the enable permissions dialog
        which will lead them to app details page to enable permissions from there. */
            showPermissionDialog()
        }
    }

    private fun showPermissionDialog() {
        val builder = AlertDialog.Builder(activity)
        builder.setTitle("Permission required")
            .setMessage("Some permissions are need to be allowed to use this app without any problems.")
            .setPositiveButton(
                "Settings"
            ) { _, _ ->
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                val uri = Uri.fromParts("package", activity.packageName, null)
                intent.data = uri
                settingActivityResultLauncher.launch(intent)
                alertDialog!!.dismiss()
            }
        if (alertDialog == null) {
            alertDialog = builder.create()
        }
        if (!alertDialog?.isShowing!!) {
            alertDialog!!.show()
        }
    }

    private fun hasPermission(context: Context, permissionStr: String): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            permissionStr
        ) == PackageManager.PERMISSION_GRANTED
    }
}