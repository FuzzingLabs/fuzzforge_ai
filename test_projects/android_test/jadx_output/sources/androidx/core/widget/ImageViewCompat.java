package androidx.core.widget;

import android.content.res.ColorStateList;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.widget.ImageView;

/* loaded from: classes.dex */
public class ImageViewCompat {
    /* JADX WARN: Multi-variable type inference failed */
    public static ColorStateList getImageTintList(ImageView imageView) {
        if (Build.VERSION.SDK_INT >= 21) {
            return imageView.getImageTintList();
        }
        if (imageView instanceof TintableImageSourceView) {
            return ((TintableImageSourceView) imageView).getSupportImageTintList();
        }
        return null;
    }

    /* JADX WARN: Multi-variable type inference failed */
    public static void setImageTintList(ImageView imageView, ColorStateList tintList) {
        Drawable imageViewDrawable;
        if (Build.VERSION.SDK_INT >= 21) {
            imageView.setImageTintList(tintList);
            if (Build.VERSION.SDK_INT == 21 && (imageViewDrawable = imageView.getDrawable()) != null && imageView.getImageTintList() != null) {
                if (imageViewDrawable.isStateful()) {
                    imageViewDrawable.setState(imageView.getDrawableState());
                }
                imageView.setImageDrawable(imageViewDrawable);
                return;
            }
            return;
        }
        if (imageView instanceof TintableImageSourceView) {
            ((TintableImageSourceView) imageView).setSupportImageTintList(tintList);
        }
    }

    /* JADX WARN: Multi-variable type inference failed */
    public static PorterDuff.Mode getImageTintMode(ImageView imageView) {
        if (Build.VERSION.SDK_INT >= 21) {
            return imageView.getImageTintMode();
        }
        if (imageView instanceof TintableImageSourceView) {
            return ((TintableImageSourceView) imageView).getSupportImageTintMode();
        }
        return null;
    }

    /* JADX WARN: Multi-variable type inference failed */
    public static void setImageTintMode(ImageView imageView, PorterDuff.Mode mode) {
        Drawable imageViewDrawable;
        if (Build.VERSION.SDK_INT >= 21) {
            imageView.setImageTintMode(mode);
            if (Build.VERSION.SDK_INT == 21 && (imageViewDrawable = imageView.getDrawable()) != null && imageView.getImageTintList() != null) {
                if (imageViewDrawable.isStateful()) {
                    imageViewDrawable.setState(imageView.getDrawableState());
                }
                imageView.setImageDrawable(imageViewDrawable);
                return;
            }
            return;
        }
        if (imageView instanceof TintableImageSourceView) {
            ((TintableImageSourceView) imageView).setSupportImageTintMode(mode);
        }
    }

    private ImageViewCompat() {
    }
}
