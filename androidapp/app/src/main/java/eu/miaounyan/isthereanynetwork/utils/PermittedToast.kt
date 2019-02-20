package eu.miaounyan.isthereanynetwork.utils

import android.content.Context
import android.widget.Toast
import eu.miaounyan.isthereanynetwork.MainActivity.PERMISSIONS
import eu.miaounyan.isthereanynetwork.service.PermissionManager

class PermittedToast(context: Context) : Toast(context) {
    companion object {
        val permissionManager = PermissionManager()

        fun makeText(context: Context?, text: CharSequence, duration: Int): Toast? {
            if (context == null)
                return null

            // missing permissions, don't show Toasts that may overlay
            if (!permissionManager.checkPermissions(context, *PERMISSIONS))
                return null

            return Toast.makeText(context, text, duration)
        }
    }
}