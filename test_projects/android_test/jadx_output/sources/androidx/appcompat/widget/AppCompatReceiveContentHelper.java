package androidx.appcompat.widget;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.ContextWrapper;
import android.os.Build;
import android.os.Bundle;
import android.text.Selection;
import android.text.Spannable;
import android.util.Log;
import android.view.DragEvent;
import android.view.View;
import android.view.inputmethod.InputContentInfo;
import android.widget.TextView;
import androidx.core.view.ContentInfoCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.inputmethod.InputConnectionCompat;
import androidx.core.view.inputmethod.InputContentInfoCompat;

/* loaded from: classes.dex */
final class AppCompatReceiveContentHelper {
    private static final String EXTRA_INPUT_CONTENT_INFO = "androidx.core.view.extra.INPUT_CONTENT_INFO";
    private static final String LOG_TAG = "ReceiveContent";

    private AppCompatReceiveContentHelper() {
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static boolean maybeHandleMenuActionViaPerformReceiveContent(TextView view, int menuItemId) {
        int i = 0;
        if ((menuItemId != 16908322 && menuItemId != 16908337) || ViewCompat.getOnReceiveContentMimeTypes(view) == null) {
            return false;
        }
        ClipboardManager cm = (ClipboardManager) view.getContext().getSystemService("clipboard");
        ClipData clip = cm == null ? null : cm.getPrimaryClip();
        if (clip != null && clip.getItemCount() > 0) {
            ContentInfoCompat.Builder builder = new ContentInfoCompat.Builder(clip, 1);
            if (menuItemId != 16908322) {
                i = 1;
            }
            ContentInfoCompat payload = builder.setFlags(i).build();
            ViewCompat.performReceiveContent(view, payload);
        }
        return true;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static boolean maybeHandleDragEventViaPerformReceiveContent(View view, DragEvent event) {
        if (Build.VERSION.SDK_INT < 24 || event.getLocalState() != null || ViewCompat.getOnReceiveContentMimeTypes(view) == null) {
            return false;
        }
        Activity activity = tryGetActivity(view);
        if (activity == null) {
            Log.i(LOG_TAG, "Can't handle drop: no activity: view=" + view);
            return false;
        }
        if (event.getAction() == 1) {
            return !(view instanceof TextView);
        }
        if (event.getAction() != 3) {
            return false;
        }
        if (view instanceof TextView) {
            return OnDropApi24Impl.onDropForTextView(event, (TextView) view, activity);
        }
        return OnDropApi24Impl.onDropForView(event, view, activity);
    }

    /* loaded from: classes.dex */
    private static final class OnDropApi24Impl {
        private OnDropApi24Impl() {
        }

        static boolean onDropForTextView(DragEvent event, TextView view, Activity activity) {
            activity.requestDragAndDropPermissions(event);
            int offset = view.getOffsetForPosition(event.getX(), event.getY());
            view.beginBatchEdit();
            try {
                Selection.setSelection((Spannable) view.getText(), offset);
                ContentInfoCompat payload = new ContentInfoCompat.Builder(event.getClipData(), 3).build();
                ViewCompat.performReceiveContent(view, payload);
                view.endBatchEdit();
                return true;
            } catch (Throwable th) {
                view.endBatchEdit();
                throw th;
            }
        }

        static boolean onDropForView(DragEvent event, View view, Activity activity) {
            activity.requestDragAndDropPermissions(event);
            ContentInfoCompat payload = new ContentInfoCompat.Builder(event.getClipData(), 3).build();
            ViewCompat.performReceiveContent(view, payload);
            return true;
        }
    }

    static Activity tryGetActivity(View view) {
        for (Context context = view.getContext(); context instanceof ContextWrapper; context = ((ContextWrapper) context).getBaseContext()) {
            if (context instanceof Activity) {
                return (Activity) context;
            }
        }
        return null;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static InputConnectionCompat.OnCommitContentListener createOnCommitContentListener(final View view) {
        return new InputConnectionCompat.OnCommitContentListener() { // from class: androidx.appcompat.widget.AppCompatReceiveContentHelper.1
            @Override // androidx.core.view.inputmethod.InputConnectionCompat.OnCommitContentListener
            public boolean onCommitContent(InputContentInfoCompat inputContentInfo, int flags, Bundle opts) {
                Bundle extras = opts;
                if (Build.VERSION.SDK_INT >= 25 && (flags & 1) != 0) {
                    try {
                        inputContentInfo.requestPermission();
                        InputContentInfo inputContentInfoFmk = (InputContentInfo) inputContentInfo.unwrap();
                        extras = opts == null ? new Bundle() : new Bundle(opts);
                        extras.putParcelable(AppCompatReceiveContentHelper.EXTRA_INPUT_CONTENT_INFO, inputContentInfoFmk);
                    } catch (Exception e) {
                        Log.w(AppCompatReceiveContentHelper.LOG_TAG, "Can't insert content from IME; requestPermission() failed", e);
                        return false;
                    }
                }
                ClipData clip = new ClipData(inputContentInfo.getDescription(), new ClipData.Item(inputContentInfo.getContentUri()));
                ContentInfoCompat payload = new ContentInfoCompat.Builder(clip, 2).setLinkUri(inputContentInfo.getLinkUri()).setExtras(extras).build();
                return ViewCompat.performReceiveContent(view, payload) == null;
            }
        };
    }
}
