package com.pinpoint.appointment.permissionUtils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.provider.Settings;
import androidx.core.app.ActivityCompat;

import java.util.ArrayList;
import java.util.List;

/*
 * This class use to check runtime permission for marshmallow and higher version.
 * If your app needs a dangerous permission that was listed in the app manifest,
 * it must ask the user to grant the permission. Android provides several methods you can use
 * to request a permission. Calling these methods brings up a standard Android dialog,
 * which you cannot customize.
 *
 * refer below link for more details
 * @link:https://developer.android.com/training/permissions/requesting.html
 *
 * TODO steps to access method
 *
 * 1. Import permissionUtils package or Required class (PermissionClass.java)
 *
 * 2. Access "checkPermission" method
 *    @Note:- In this method
 *            check permission granted or not
 *
 *              case 1: Granted : do your stuff
 *              case 2: Not granted : than it will show dialog to user to do action
 *
 *    copy below code where you want to check permission
 *
 *    PermissionClass.checkPermission(this, PermissionClass.REQUEST_CODE_RUNTIME_PERMISSION,
 *      Arrays.asList(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE,
 *      Manifest.permission.CAMERA))
 *
 * 3. Implement override method "onRequestPermissionsResult()"
 *
 *    3.1) Implementation in activity
 *
 *         implement override method in activity where you want to check and get granted permission list
 *           @Note:- In this method we can get granted permission result
 *            You can do relevant action in this method
 *
 *            Refer below link for further details
 *            @link : https://developer.android.com/training/permissions/requesting.html
 *            Refer "Handle the permissions request response" point
 *       e.g.
 *       @Override
 *       public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
 *           super.onRequestPermissionsResult(requestCode, permissions, grantResults);
 *          List<String> shouldPermit = new ArrayList<>();
 *
 *          if (requestCode == PermissionClass.REQUEST_CODE_RUNTIME_PERMISSION) {
 *
 *                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
 *
 *                for (int i = 0; i < grantResults.length; i++) {
 *                    //  permissions[i] = Manifest.permission.CAMERA; //for specific permission check
 *                     grantResults[i] = PackageManager.PERMISSION_DENIED;
 *                    shouldPermit.add(permissions[i]);
 *                           }
 *              }
 *          }
 *      }
 *
 *   3.2) Implementation in fragment
 *
 *      3.2.1) copy below code in your parent activity where fragment loads.
 *
 *           @Override
 *          protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 *              super.onActivityResult(requestCode, resultCode, data);
 *                  for (Fragment fragment : getSupportFragmentManager().getFragments()) {
 *                   fragment.onActivityResult(requestCode, resultCode, data);
 *                      }
 *                  }
 *
 *          @Override
 *          public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
 *               super.onRequestPermissionsResult(requestCode, permissions, grantResults);
 *                   for (Fragment fragment : getSupportFragmentManager().getFragments()) {
 *                       fragment.onRequestPermissionsResult(requestCode, permissions, grantResults);
 *                      }
 *                  }
 *
 *     3.2.2) Implement override method  onRequestPermissionsResult() in fragment
 *
 *            @Note:- In this method we can get granted permission result
 *            You can do relevant action in this method
 *
 *            Refer below link for further details
 *            @link : https://developer.android.com/training/permissions/requesting.html
 *            Refer "Handle the permissions request response" point
 *       e.g.
 *       @Override
 *       public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
 *           super.onRequestPermissionsResult(requestCode, permissions, grantResults);
 *          List<String> shouldPermit = new ArrayList<>();
 *
 *          if (requestCode == PermissionClass.REQUEST_CODE_RUNTIME_PERMISSION) {
 *
 *                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
 *
 *                for (int i = 0; i < grantResults.length; i++) {
 *                    //  permissions[i] = Manifest.permission.CAMERA; //for specific permission check
 *                     grantResults[i] = PackageManager.PERMISSION_DENIED;
 *                    shouldPermit.add(permissions[i]);
 *                           }
 *              }
 *          }
 *      }
 *
 * 4. Access "verifyPermission" method to check or any further action.
 *
 *    Copy below code to override method "onRequestPermissionsResult()"
 *    if (PermissionUtil.verifyPermissions(grantResults)) {
 *          // permission granted
 *          // perform relevant action
 *    }else{
 *          // permission denied
 *          // perform relevant action
 *    }
 *    case 1: we can recall the dialog if user denied to accept the permission
 *    case 2: Perform relevant action
 *
 * 5. Manage Never ask Again action
 *    user denied the permission and checked the "never show again" option.
 *    you can show a dialog explaining the situation and that the user has
 *    to go to the app settings and allow the permission otherwise your feature
 *    will not be available.
 *
 *    use shouldShowRequestPermissionRationale(permission); to check that user checked the
 *    "never show again" option or not
 *    it return false when user checked the "never show again" option.
 *
 *   use this method in onRequestPermissionsResult() to get status
 *
 *
 */

public class PermissionClass {

    /* variable declaration*/
    public static final int REQUEST_CODE_RUNTIME_PERMISSION = 151;
    public static final int REQUEST_CODE_RUNTIME_PERMISSION_DOCUMENT = 151;
    public static final int REQUEST_CODE_RUNTIME_PERMISSION_CAMERA = 152;
    public static final int REQUEST_CODE_RUNTIME_PERMISSION_STORAGE = 153;
    public static final int REQUEST_CODE_PERMISSION_SETTING = 155;

    /**
     * This method check that required permission user granted or denied.
     * if denied than it pass denied permission list to show pop up to allow the permission
     * <p>
     * case 1 : Granted : do your stuff
     * <br>case 2 : Not granted : than it will show dialog to user to accept permission</br>
     *
     * @param context        (Context)             : application context
     * @param requestCode    (int)             : request code to be identify the request
     *                       e.g. 151
     * @param permissionList (List<String>) : list of permission that need to be granted by user
     *                       <ul>
     *                       <li>single permission </li><code>Collections.singletonList(Manifest.permission.ACCESS_FINE_LOCATION)</code>
     *                       <li>multiple permission </li><code>Arrays.asList(Manifest.permission.READ_EXTERNAL_STORAGE,
     *                       Manifest.permission.WRITE_EXTERNAL_STORAGE,
     *                       Manifest.permission.CAMERA)</code>
     *                       </ul>
     *                       <p> for single permission
     *                       e.g. Collections.singletonList(Manifest.permission.ACCESS_FINE_LOCATION)
     *                       <p> for multiple permission
     *                       e.g. Arrays.asList(Manifest.permission.READ_EXTERNAL_STORAGE,
     *                       Manifest.permission.WRITE_EXTERNAL_STORAGE,
     *                       Manifest.permission.CAMERA)
     * @return (boolean) : it return true, if permission already granted Or
     * return false and show permission relevant dialog
     * @see android.Manifest.permission
     * <a href="https://developer.android.com/training/permissions/requesting.html#perm-check">Check For Permissions</a>
     */
    public static boolean checkPermission(Context context, int requestCode, List<String> permissionList) {

        List<String> deniedPermissionList = new ArrayList<>();

        for (String permission : permissionList) {
            if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                deniedPermissionList.add(permission);
            }
        }

        if (deniedPermissionList.size() > 0) {

            // parse list as an array
            // In requestPermission method we must pass array
            String[] deniedPermissionArray = new String[deniedPermissionList.size()];
            deniedPermissionArray = deniedPermissionList.toArray(deniedPermissionArray);

            makeRequest(context, requestCode, deniedPermissionArray);
            return false;
        } else {
            return true;
        }
    }

    /**
     * This method shows permission dialog whose permission is not granted.
     * Calling this method brings up a standard Android dialog,
     * which you cannot customize.
     *
     * @param context     (Context)     : application context
     * @param requestCode (int)     : request code to be identify the request e.g. 151
     * @param permission  (String[]) : list of permission that need to be granted by user
     * @see android.Manifest.permission
     * @see #REQUEST_CODE_RUNTIME_PERMISSION
     */
    private static void makeRequest(Context context, int requestCode, String[] permission) {

        ActivityCompat.requestPermissions((Activity) context,
                permission,
                requestCode);
    }

    /**
     * Check that all given permissions have been granted by verifying that each entry in the
     * given array is of the value {@link PackageManager#PERMISSION_GRANTED}.
     *
     * @param grantResults (int[]) : The grant results for the corresponding permissions
     *                     which is either {@link PackageManager#PERMISSION_GRANTED}
     *                     or {@link PackageManager#PERMISSION_DENIED}.
     * @return (boolean) : return true or false,if permission is granted or not, respectively
     * @see Activity#onRequestPermissionsResult(int, String[], int[])
     */
    public static boolean verifyPermission(int[] grantResults) {
        // At least one result must be checked.
        if (grantResults.length < 1) {
            return false;
        }

        // Verify that each required permission has been granted, otherwise return false.
        for (int result : grantResults) {
            if (result != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    /**
     * This method use to open setting bar where user can allow runtime permission.
     * it will use when user permanently denied to accept runtime permission.
     * <br>Use {@link Activity#shouldShowRequestPermissionRationale(String)} method in
     * {@link Activity#onRequestPermissionsResult(int, String[], int[])} method to get result
     * that user permanently denied to accept permission or not.
     * </br>
     * <p><strong> Note :</strong> activityContext is optional to pass as a parameter. you can also use
     * Application class context to get package name</p>
     *
     * @param activityContext (Activity) : activity context or application context
     */
    public static void openSettingBar(Activity activityContext) {

        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", activityContext.getPackageName(), null);
        intent.setData(uri);
        activityContext.startActivityForResult(intent, REQUEST_CODE_PERMISSION_SETTING);
    }

}
