package android.support.v4.media.session;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.AudioAttributes;
import android.media.MediaDescription;
import android.media.MediaMetadata;
import android.media.Rating;
import android.media.VolumeProvider;
import android.media.session.MediaSession;
import android.media.session.PlaybackState;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.os.ResultReceiver;
import android.util.Log;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/* loaded from: classes.dex */
class MediaSessionCompatApi21 {
    static final String TAG = "MediaSessionCompatApi21";

    interface Callback {
        void onCommand(String str, Bundle bundle, ResultReceiver resultReceiver);

        void onCustomAction(String str, Bundle bundle);

        void onFastForward();

        boolean onMediaButtonEvent(Intent intent);

        void onPause();

        void onPlay();

        void onPlayFromMediaId(String str, Bundle bundle);

        void onPlayFromSearch(String str, Bundle bundle);

        void onRewind();

        void onSeekTo(long j);

        void onSetRating(Object obj);

        void onSetRating(Object obj, Bundle bundle);

        void onSkipToNext();

        void onSkipToPrevious();

        void onSkipToQueueItem(long j);

        void onStop();
    }

    public static Object createSession(Context context, String tag) {
        return new MediaSession(context, tag);
    }

    public static Object verifySession(Object mediaSession) {
        if (mediaSession instanceof MediaSession) {
            return mediaSession;
        }
        throw new IllegalArgumentException("mediaSession is not a valid MediaSession object");
    }

    public static Object verifyToken(Object token) {
        if (token instanceof MediaSession.Token) {
            return token;
        }
        throw new IllegalArgumentException("token is not a valid MediaSession.Token object");
    }

    public static Object createCallback(Callback callback) {
        return new CallbackProxy(callback);
    }

    public static void setCallback(Object sessionObj, Object callbackObj, Handler handler) {
        ((MediaSession) sessionObj).setCallback((MediaSession.Callback) callbackObj, handler);
    }

    public static void setFlags(Object sessionObj, int flags) {
        ((MediaSession) sessionObj).setFlags(flags);
    }

    public static void setPlaybackToLocal(Object sessionObj, int stream) {
        AudioAttributes.Builder bob = new AudioAttributes.Builder();
        bob.setLegacyStreamType(stream);
        ((MediaSession) sessionObj).setPlaybackToLocal(bob.build());
    }

    public static void setPlaybackToRemote(Object sessionObj, Object volumeProviderObj) {
        ((MediaSession) sessionObj).setPlaybackToRemote((VolumeProvider) volumeProviderObj);
    }

    public static void setActive(Object sessionObj, boolean active) {
        ((MediaSession) sessionObj).setActive(active);
    }

    public static boolean isActive(Object sessionObj) {
        return ((MediaSession) sessionObj).isActive();
    }

    public static void sendSessionEvent(Object sessionObj, String event, Bundle extras) {
        ((MediaSession) sessionObj).sendSessionEvent(event, extras);
    }

    public static void release(Object sessionObj) {
        ((MediaSession) sessionObj).release();
    }

    public static Parcelable getSessionToken(Object sessionObj) {
        return ((MediaSession) sessionObj).getSessionToken();
    }

    public static void setPlaybackState(Object sessionObj, Object stateObj) {
        ((MediaSession) sessionObj).setPlaybackState((PlaybackState) stateObj);
    }

    public static void setMetadata(Object sessionObj, Object metadataObj) {
        ((MediaSession) sessionObj).setMetadata((MediaMetadata) metadataObj);
    }

    public static void setSessionActivity(Object sessionObj, PendingIntent pi) {
        ((MediaSession) sessionObj).setSessionActivity(pi);
    }

    public static void setMediaButtonReceiver(Object sessionObj, PendingIntent pi) {
        ((MediaSession) sessionObj).setMediaButtonReceiver(pi);
    }

    public static void setQueue(Object sessionObj, List<Object> queueObjs) {
        if (queueObjs == null) {
            ((MediaSession) sessionObj).setQueue(null);
            return;
        }
        ArrayList<MediaSession.QueueItem> queue = new ArrayList<>();
        for (Object itemObj : queueObjs) {
            queue.add((MediaSession.QueueItem) itemObj);
        }
        ((MediaSession) sessionObj).setQueue(queue);
    }

    public static void setQueueTitle(Object sessionObj, CharSequence title) {
        ((MediaSession) sessionObj).setQueueTitle(title);
    }

    public static void setExtras(Object sessionObj, Bundle extras) {
        ((MediaSession) sessionObj).setExtras(extras);
    }

    public static boolean hasCallback(Object sessionObj) {
        try {
            Field callbackField = sessionObj.getClass().getDeclaredField("mCallback");
            if (callbackField != null) {
                callbackField.setAccessible(true);
                if (callbackField.get(sessionObj) == null) {
                    return false;
                }
                return true;
            }
        } catch (IllegalAccessException | NoSuchFieldException e) {
            Log.w(TAG, "Failed to get mCallback object.");
        }
        return false;
    }

    static class CallbackProxy<T extends Callback> extends MediaSession.Callback {
        protected final T mCallback;

        public CallbackProxy(T callback) {
            this.mCallback = callback;
        }

        @Override // android.media.session.MediaSession.Callback
        public void onCommand(String command, Bundle args, ResultReceiver cb) {
            MediaSessionCompat.ensureClassLoader(args);
            this.mCallback.onCommand(command, args, cb);
        }

        @Override // android.media.session.MediaSession.Callback
        public boolean onMediaButtonEvent(Intent mediaButtonIntent) {
            return this.mCallback.onMediaButtonEvent(mediaButtonIntent) || super.onMediaButtonEvent(mediaButtonIntent);
        }

        @Override // android.media.session.MediaSession.Callback
        public void onPlay() {
            this.mCallback.onPlay();
        }

        @Override // android.media.session.MediaSession.Callback
        public void onPlayFromMediaId(String mediaId, Bundle extras) {
            MediaSessionCompat.ensureClassLoader(extras);
            this.mCallback.onPlayFromMediaId(mediaId, extras);
        }

        @Override // android.media.session.MediaSession.Callback
        public void onPlayFromSearch(String search, Bundle extras) {
            MediaSessionCompat.ensureClassLoader(extras);
            this.mCallback.onPlayFromSearch(search, extras);
        }

        @Override // android.media.session.MediaSession.Callback
        public void onSkipToQueueItem(long id) {
            this.mCallback.onSkipToQueueItem(id);
        }

        @Override // android.media.session.MediaSession.Callback
        public void onPause() {
            this.mCallback.onPause();
        }

        @Override // android.media.session.MediaSession.Callback
        public void onSkipToNext() {
            this.mCallback.onSkipToNext();
        }

        @Override // android.media.session.MediaSession.Callback
        public void onSkipToPrevious() {
            this.mCallback.onSkipToPrevious();
        }

        @Override // android.media.session.MediaSession.Callback
        public void onFastForward() {
            this.mCallback.onFastForward();
        }

        @Override // android.media.session.MediaSession.Callback
        public void onRewind() {
            this.mCallback.onRewind();
        }

        @Override // android.media.session.MediaSession.Callback
        public void onStop() {
            this.mCallback.onStop();
        }

        @Override // android.media.session.MediaSession.Callback
        public void onSeekTo(long pos) {
            this.mCallback.onSeekTo(pos);
        }

        @Override // android.media.session.MediaSession.Callback
        public void onSetRating(Rating rating) {
            this.mCallback.onSetRating(rating);
        }

        @Override // android.media.session.MediaSession.Callback
        public void onCustomAction(String action, Bundle extras) {
            MediaSessionCompat.ensureClassLoader(extras);
            this.mCallback.onCustomAction(action, extras);
        }
    }

    static class QueueItem {
        public static Object createItem(Object mediaDescription, long id) {
            return new MediaSession.QueueItem((MediaDescription) mediaDescription, id);
        }

        public static Object getDescription(Object queueItem) {
            return ((MediaSession.QueueItem) queueItem).getDescription();
        }

        public static long getQueueId(Object queueItem) {
            return ((MediaSession.QueueItem) queueItem).getQueueId();
        }

        private QueueItem() {
        }
    }

    private MediaSessionCompatApi21() {
    }
}
