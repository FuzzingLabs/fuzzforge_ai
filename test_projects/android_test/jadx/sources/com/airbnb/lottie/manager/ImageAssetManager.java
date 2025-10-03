package com.airbnb.lottie.manager;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.Base64;
import android.view.View;
import com.airbnb.lottie.ImageAssetDelegate;
import com.airbnb.lottie.LottieImageAsset;
import com.airbnb.lottie.utils.Logger;
import com.airbnb.lottie.utils.Utils;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/* loaded from: classes.dex */
public class ImageAssetManager {
    private static final Object bitmapHashLock = new Object();
    private final Context context;
    private ImageAssetDelegate delegate;
    private final Map<String, LottieImageAsset> imageAssets;
    private String imagesFolder;

    public ImageAssetManager(Drawable.Callback callback, String imagesFolder, ImageAssetDelegate delegate, Map<String, LottieImageAsset> imageAssets) {
        this.imagesFolder = imagesFolder;
        if (!TextUtils.isEmpty(imagesFolder)) {
            if (this.imagesFolder.charAt(r0.length() - 1) != '/') {
                this.imagesFolder += '/';
            }
        }
        if (!(callback instanceof View)) {
            Logger.warning("LottieDrawable must be inside of a view for images to work.");
            this.imageAssets = new HashMap();
            this.context = null;
        } else {
            this.context = ((View) callback).getContext();
            this.imageAssets = imageAssets;
            setDelegate(delegate);
        }
    }

    public void setDelegate(ImageAssetDelegate assetDelegate) {
        this.delegate = assetDelegate;
    }

    public Bitmap updateBitmap(String id, Bitmap bitmap) {
        if (bitmap == null) {
            LottieImageAsset asset = this.imageAssets.get(id);
            Bitmap ret = asset.getBitmap();
            asset.setBitmap(null);
            return ret;
        }
        Bitmap prevBitmap = this.imageAssets.get(id).getBitmap();
        putBitmap(id, bitmap);
        return prevBitmap;
    }

    public Bitmap bitmapForId(String id) {
        LottieImageAsset asset = this.imageAssets.get(id);
        if (asset == null) {
            return null;
        }
        Bitmap bitmap = asset.getBitmap();
        if (bitmap != null) {
            return bitmap;
        }
        ImageAssetDelegate imageAssetDelegate = this.delegate;
        if (imageAssetDelegate != null) {
            Bitmap bitmap2 = imageAssetDelegate.fetchBitmap(asset);
            if (bitmap2 != null) {
                putBitmap(id, bitmap2);
            }
            return bitmap2;
        }
        String filename = asset.getFileName();
        BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.inScaled = true;
        opts.inDensity = 160;
        if (filename.startsWith("data:") && filename.indexOf("base64,") > 0) {
            try {
                byte[] data = Base64.decode(filename.substring(filename.indexOf(44) + 1), 0);
                return putBitmap(id, BitmapFactory.decodeByteArray(data, 0, data.length, opts));
            } catch (IllegalArgumentException e) {
                Logger.warning("data URL did not have correct base64 format.", e);
                return null;
            }
        }
        try {
            if (TextUtils.isEmpty(this.imagesFolder)) {
                throw new IllegalStateException("You must set an images folder before loading an image. Set it with LottieComposition#setImagesFolder or LottieDrawable#setImagesFolder");
            }
            InputStream is = this.context.getAssets().open(this.imagesFolder + filename);
            return putBitmap(id, Utils.resizeBitmapIfNeeded(BitmapFactory.decodeStream(is, null, opts), asset.getWidth(), asset.getHeight()));
        } catch (IOException e2) {
            Logger.warning("Unable to open asset.", e2);
            return null;
        }
    }

    public boolean hasSameContext(Context context) {
        return (context == null && this.context == null) || this.context.equals(context);
    }

    private Bitmap putBitmap(String key, Bitmap bitmap) {
        synchronized (bitmapHashLock) {
            this.imageAssets.get(key).setBitmap(bitmap);
        }
        return bitmap;
    }
}
