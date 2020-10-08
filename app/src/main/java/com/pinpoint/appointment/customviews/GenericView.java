package com.pinpoint.appointment.customviews;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.view.View;

/**
 * <h1>GenericView.java</h1>
 * <p>
 * The GenericView.java class implements Convenient methods of findViewById.
 * It will automatically binds Id with specified control.
 * Developer no need to bind control for which Id is used.
 *
 * @author Abbacus Technologies
 * @version 1.0
 * @since 2017-04-28
 */
public class GenericView {

    /**
     * Convenience method of findViewById
     */
    public static <T extends View> T findViewById(final View parent, final int id) {
        return (T) parent.findViewById(id);
    }

    /**
     * Convenience method of findViewById
     */
    public static <T extends View> T findViewById(final Activity activity, final int id) {
        return (T) activity.findViewById(id);
    }

    /**
     * Convenience method of findViewById
     */
    public static <T extends View> T findViewById(final Dialog dialog, final int id) {
        return (T) dialog.findViewById(id);
    }

    public static final String getString(final Context context, final int id) {
        return context.getResources().getString(id);
    }
}