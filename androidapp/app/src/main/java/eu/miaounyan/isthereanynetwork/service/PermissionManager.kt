package eu.miaounyan.isthereanynetwork.service

import android.content.Context
import android.content.pm.PackageManager
import android.support.v4.content.ContextCompat

class PermissionManager() {

    fun checkPermission(context: Context, permission: String) : Boolean{
        val result = ContextCompat.checkSelfPermission(context, permission)
        return result == PackageManager.PERMISSION_GRANTED;
    }

    fun checkPermissions(context: Context, vararg permissions: String) : Boolean {
        return permissions.all { checkPermission(context, it) };
    }
}