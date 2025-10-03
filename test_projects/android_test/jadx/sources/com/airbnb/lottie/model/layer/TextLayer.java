package com.airbnb.lottie.model.layer;

import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Typeface;
import androidx.collection.LongSparseArray;
import com.airbnb.lottie.LottieComposition;
import com.airbnb.lottie.LottieDrawable;
import com.airbnb.lottie.LottieProperty;
import com.airbnb.lottie.TextDelegate;
import com.airbnb.lottie.animation.content.ContentGroup;
import com.airbnb.lottie.animation.keyframe.BaseKeyframeAnimation;
import com.airbnb.lottie.animation.keyframe.TextKeyframeAnimation;
import com.airbnb.lottie.animation.keyframe.ValueCallbackKeyframeAnimation;
import com.airbnb.lottie.model.DocumentData;
import com.airbnb.lottie.model.Font;
import com.airbnb.lottie.model.FontCharacter;
import com.airbnb.lottie.model.animatable.AnimatableTextProperties;
import com.airbnb.lottie.model.content.ShapeGroup;
import com.airbnb.lottie.utils.Utils;
import com.airbnb.lottie.value.LottieValueCallback;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/* loaded from: classes.dex */
public class TextLayer extends BaseLayer {
    private final LongSparseArray<String> codePointCache;
    private BaseKeyframeAnimation<Integer, Integer> colorAnimation;
    private BaseKeyframeAnimation<Integer, Integer> colorCallbackAnimation;
    private final LottieComposition composition;
    private final Map<FontCharacter, List<ContentGroup>> contentsForCharacter;
    private final Paint fillPaint;
    private final LottieDrawable lottieDrawable;
    private final Matrix matrix;
    private final RectF rectF;
    private final StringBuilder stringBuilder;
    private BaseKeyframeAnimation<Integer, Integer> strokeColorAnimation;
    private BaseKeyframeAnimation<Integer, Integer> strokeColorCallbackAnimation;
    private final Paint strokePaint;
    private BaseKeyframeAnimation<Float, Float> strokeWidthAnimation;
    private BaseKeyframeAnimation<Float, Float> strokeWidthCallbackAnimation;
    private final TextKeyframeAnimation textAnimation;
    private BaseKeyframeAnimation<Float, Float> textSizeAnimation;
    private BaseKeyframeAnimation<Float, Float> textSizeCallbackAnimation;
    private BaseKeyframeAnimation<Float, Float> trackingAnimation;
    private BaseKeyframeAnimation<Float, Float> trackingCallbackAnimation;

    TextLayer(LottieDrawable lottieDrawable, Layer layerModel) {
        super(lottieDrawable, layerModel);
        this.stringBuilder = new StringBuilder(2);
        this.rectF = new RectF();
        this.matrix = new Matrix();
        int i = 1;
        this.fillPaint = new Paint(i) { // from class: com.airbnb.lottie.model.layer.TextLayer.1
            {
                setStyle(Paint.Style.FILL);
            }
        };
        this.strokePaint = new Paint(i) { // from class: com.airbnb.lottie.model.layer.TextLayer.2
            {
                setStyle(Paint.Style.STROKE);
            }
        };
        this.contentsForCharacter = new HashMap();
        this.codePointCache = new LongSparseArray<>();
        this.lottieDrawable = lottieDrawable;
        this.composition = layerModel.getComposition();
        TextKeyframeAnimation createAnimation = layerModel.getText().createAnimation();
        this.textAnimation = createAnimation;
        createAnimation.addUpdateListener(this);
        addAnimation(createAnimation);
        AnimatableTextProperties textProperties = layerModel.getTextProperties();
        if (textProperties != null && textProperties.color != null) {
            BaseKeyframeAnimation<Integer, Integer> createAnimation2 = textProperties.color.createAnimation();
            this.colorAnimation = createAnimation2;
            createAnimation2.addUpdateListener(this);
            addAnimation(this.colorAnimation);
        }
        if (textProperties != null && textProperties.stroke != null) {
            BaseKeyframeAnimation<Integer, Integer> createAnimation3 = textProperties.stroke.createAnimation();
            this.strokeColorAnimation = createAnimation3;
            createAnimation3.addUpdateListener(this);
            addAnimation(this.strokeColorAnimation);
        }
        if (textProperties != null && textProperties.strokeWidth != null) {
            BaseKeyframeAnimation<Float, Float> createAnimation4 = textProperties.strokeWidth.createAnimation();
            this.strokeWidthAnimation = createAnimation4;
            createAnimation4.addUpdateListener(this);
            addAnimation(this.strokeWidthAnimation);
        }
        if (textProperties != null && textProperties.tracking != null) {
            BaseKeyframeAnimation<Float, Float> createAnimation5 = textProperties.tracking.createAnimation();
            this.trackingAnimation = createAnimation5;
            createAnimation5.addUpdateListener(this);
            addAnimation(this.trackingAnimation);
        }
    }

    @Override // com.airbnb.lottie.model.layer.BaseLayer, com.airbnb.lottie.animation.content.DrawingContent
    public void getBounds(RectF outBounds, Matrix parentMatrix, boolean applyParents) {
        super.getBounds(outBounds, parentMatrix, applyParents);
        outBounds.set(0.0f, 0.0f, this.composition.getBounds().width(), this.composition.getBounds().height());
    }

    @Override // com.airbnb.lottie.model.layer.BaseLayer
    void drawLayer(Canvas canvas, Matrix parentMatrix, int parentAlpha) {
        canvas.save();
        if (!this.lottieDrawable.useTextGlyphs()) {
            canvas.setMatrix(parentMatrix);
        }
        DocumentData documentData = this.textAnimation.getValue();
        Font font = this.composition.getFonts().get(documentData.fontName);
        if (font == null) {
            canvas.restore();
            return;
        }
        BaseKeyframeAnimation<Integer, Integer> baseKeyframeAnimation = this.colorCallbackAnimation;
        if (baseKeyframeAnimation != null) {
            this.fillPaint.setColor(baseKeyframeAnimation.getValue().intValue());
        } else {
            BaseKeyframeAnimation<Integer, Integer> baseKeyframeAnimation2 = this.colorAnimation;
            if (baseKeyframeAnimation2 != null) {
                this.fillPaint.setColor(baseKeyframeAnimation2.getValue().intValue());
            } else {
                this.fillPaint.setColor(documentData.color);
            }
        }
        BaseKeyframeAnimation<Integer, Integer> baseKeyframeAnimation3 = this.strokeColorCallbackAnimation;
        if (baseKeyframeAnimation3 != null) {
            this.strokePaint.setColor(baseKeyframeAnimation3.getValue().intValue());
        } else {
            BaseKeyframeAnimation<Integer, Integer> baseKeyframeAnimation4 = this.strokeColorAnimation;
            if (baseKeyframeAnimation4 != null) {
                this.strokePaint.setColor(baseKeyframeAnimation4.getValue().intValue());
            } else {
                this.strokePaint.setColor(documentData.strokeColor);
            }
        }
        int opacity = this.transform.getOpacity() == null ? 100 : this.transform.getOpacity().getValue().intValue();
        int alpha = (opacity * 255) / 100;
        this.fillPaint.setAlpha(alpha);
        this.strokePaint.setAlpha(alpha);
        BaseKeyframeAnimation<Float, Float> baseKeyframeAnimation5 = this.strokeWidthCallbackAnimation;
        if (baseKeyframeAnimation5 != null) {
            this.strokePaint.setStrokeWidth(baseKeyframeAnimation5.getValue().floatValue());
        } else {
            BaseKeyframeAnimation<Float, Float> baseKeyframeAnimation6 = this.strokeWidthAnimation;
            if (baseKeyframeAnimation6 != null) {
                this.strokePaint.setStrokeWidth(baseKeyframeAnimation6.getValue().floatValue());
            } else {
                float parentScale = Utils.getScale(parentMatrix);
                this.strokePaint.setStrokeWidth(documentData.strokeWidth * Utils.dpScale() * parentScale);
            }
        }
        if (this.lottieDrawable.useTextGlyphs()) {
            drawTextGlyphs(documentData, parentMatrix, font, canvas);
        } else {
            drawTextWithFont(documentData, font, parentMatrix, canvas);
        }
        canvas.restore();
    }

    private void drawTextGlyphs(DocumentData documentData, Matrix parentMatrix, Font font, Canvas canvas) {
        float textSize;
        BaseKeyframeAnimation<Float, Float> baseKeyframeAnimation = this.textSizeCallbackAnimation;
        if (baseKeyframeAnimation != null) {
            textSize = baseKeyframeAnimation.getValue().floatValue();
        } else {
            BaseKeyframeAnimation<Float, Float> baseKeyframeAnimation2 = this.textSizeAnimation;
            if (baseKeyframeAnimation2 != null) {
                textSize = baseKeyframeAnimation2.getValue().floatValue();
            } else {
                float textSize2 = documentData.size;
                textSize = textSize2;
            }
        }
        float fontScale = textSize / 100.0f;
        float parentScale = Utils.getScale(parentMatrix);
        String text = documentData.text;
        float lineHeight = documentData.lineHeight * Utils.dpScale();
        List<String> textLines = getTextLines(text);
        int textLineCount = textLines.size();
        int l = 0;
        while (l < textLineCount) {
            String textLine = textLines.get(l);
            float textLineWidth = getTextLineWidthForGlyphs(textLine, font, fontScale, parentScale);
            canvas.save();
            applyJustification(documentData.justification, canvas, textLineWidth);
            float multilineTranslateY = ((textLineCount - 1) * lineHeight) / 2.0f;
            float translateY = (l * lineHeight) - multilineTranslateY;
            canvas.translate(0.0f, translateY);
            drawGlyphTextLine(textLine, documentData, parentMatrix, font, canvas, parentScale, fontScale);
            canvas.restore();
            l++;
            textLineCount = textLineCount;
            textLines = textLines;
        }
    }

    private void drawGlyphTextLine(String text, DocumentData documentData, Matrix parentMatrix, Font font, Canvas canvas, float parentScale, float fontScale) {
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            int characterHash = FontCharacter.hashFor(c, font.getFamily(), font.getStyle());
            FontCharacter character = this.composition.getCharacters().get(characterHash);
            if (character != null) {
                drawCharacterAsGlyph(character, parentMatrix, fontScale, documentData, canvas);
                float tx = ((float) character.getWidth()) * fontScale * Utils.dpScale() * parentScale;
                float tracking = documentData.tracking / 10.0f;
                BaseKeyframeAnimation<Float, Float> baseKeyframeAnimation = this.trackingCallbackAnimation;
                if (baseKeyframeAnimation != null) {
                    tracking += baseKeyframeAnimation.getValue().floatValue();
                } else {
                    BaseKeyframeAnimation<Float, Float> baseKeyframeAnimation2 = this.trackingAnimation;
                    if (baseKeyframeAnimation2 != null) {
                        tracking += baseKeyframeAnimation2.getValue().floatValue();
                    }
                }
                canvas.translate(tx + (tracking * parentScale), 0.0f);
            }
        }
    }

    private void drawTextWithFont(DocumentData documentData, Font font, Matrix parentMatrix, Canvas canvas) {
        float textSize;
        float parentScale = Utils.getScale(parentMatrix);
        Typeface typeface = this.lottieDrawable.getTypeface(font.getFamily(), font.getStyle());
        if (typeface == null) {
            return;
        }
        String text = documentData.text;
        TextDelegate textDelegate = this.lottieDrawable.getTextDelegate();
        if (textDelegate != null) {
            text = textDelegate.getTextInternal(text);
        }
        this.fillPaint.setTypeface(typeface);
        BaseKeyframeAnimation<Float, Float> baseKeyframeAnimation = this.textSizeCallbackAnimation;
        if (baseKeyframeAnimation != null) {
            textSize = baseKeyframeAnimation.getValue().floatValue();
        } else {
            BaseKeyframeAnimation<Float, Float> baseKeyframeAnimation2 = this.textSizeAnimation;
            if (baseKeyframeAnimation2 != null) {
                textSize = baseKeyframeAnimation2.getValue().floatValue();
            } else {
                textSize = documentData.size;
            }
        }
        this.fillPaint.setTextSize(Utils.dpScale() * textSize);
        this.strokePaint.setTypeface(this.fillPaint.getTypeface());
        this.strokePaint.setTextSize(this.fillPaint.getTextSize());
        float lineHeight = documentData.lineHeight * Utils.dpScale();
        List<String> textLines = getTextLines(text);
        int textLineCount = textLines.size();
        int l = 0;
        while (l < textLineCount) {
            String textLine = textLines.get(l);
            float textLineWidth = this.strokePaint.measureText(textLine);
            applyJustification(documentData.justification, canvas, textLineWidth);
            float multilineTranslateY = ((textLineCount - 1) * lineHeight) / 2.0f;
            float translateY = (l * lineHeight) - multilineTranslateY;
            canvas.translate(0.0f, translateY);
            drawFontTextLine(textLine, documentData, canvas, parentScale);
            canvas.setMatrix(parentMatrix);
            l++;
            typeface = typeface;
        }
    }

    private List<String> getTextLines(String text) {
        String formattedText = text.replaceAll("\r\n", "\r").replaceAll("\n", "\r");
        String[] textLinesArray = formattedText.split("\r");
        return Arrays.asList(textLinesArray);
    }

    private void drawFontTextLine(String text, DocumentData documentData, Canvas canvas, float parentScale) {
        int i = 0;
        while (i < text.length()) {
            String charString = codePointToString(text, i);
            i += charString.length();
            drawCharacterFromFont(charString, documentData, canvas);
            float charWidth = this.fillPaint.measureText(charString, 0, 1);
            float tracking = documentData.tracking / 10.0f;
            BaseKeyframeAnimation<Float, Float> baseKeyframeAnimation = this.trackingCallbackAnimation;
            if (baseKeyframeAnimation != null) {
                tracking += baseKeyframeAnimation.getValue().floatValue();
            } else {
                BaseKeyframeAnimation<Float, Float> baseKeyframeAnimation2 = this.trackingAnimation;
                if (baseKeyframeAnimation2 != null) {
                    tracking += baseKeyframeAnimation2.getValue().floatValue();
                }
            }
            float tx = (tracking * parentScale) + charWidth;
            canvas.translate(tx, 0.0f);
        }
    }

    private float getTextLineWidthForGlyphs(String textLine, Font font, float fontScale, float parentScale) {
        float textLineWidth = 0.0f;
        for (int i = 0; i < textLine.length(); i++) {
            char c = textLine.charAt(i);
            int characterHash = FontCharacter.hashFor(c, font.getFamily(), font.getStyle());
            FontCharacter character = this.composition.getCharacters().get(characterHash);
            if (character != null) {
                textLineWidth = (float) (textLineWidth + (character.getWidth() * fontScale * Utils.dpScale() * parentScale));
            }
        }
        return textLineWidth;
    }

    /* renamed from: com.airbnb.lottie.model.layer.TextLayer$3 */
    static /* synthetic */ class C06823 {
        static final /* synthetic */ int[] $SwitchMap$com$airbnb$lottie$model$DocumentData$Justification;

        static {
            int[] iArr = new int[DocumentData.Justification.values().length];
            $SwitchMap$com$airbnb$lottie$model$DocumentData$Justification = iArr;
            try {
                iArr[DocumentData.Justification.LEFT_ALIGN.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                $SwitchMap$com$airbnb$lottie$model$DocumentData$Justification[DocumentData.Justification.RIGHT_ALIGN.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
            try {
                $SwitchMap$com$airbnb$lottie$model$DocumentData$Justification[DocumentData.Justification.CENTER.ordinal()] = 3;
            } catch (NoSuchFieldError e3) {
            }
        }
    }

    private void applyJustification(DocumentData.Justification justification, Canvas canvas, float textLineWidth) {
        switch (C06823.$SwitchMap$com$airbnb$lottie$model$DocumentData$Justification[justification.ordinal()]) {
            case 2:
                canvas.translate(-textLineWidth, 0.0f);
                break;
            case 3:
                canvas.translate((-textLineWidth) / 2.0f, 0.0f);
                break;
        }
    }

    private void drawCharacterAsGlyph(FontCharacter character, Matrix parentMatrix, float fontScale, DocumentData documentData, Canvas canvas) {
        List<ContentGroup> contentGroups = getContentsForCharacter(character);
        for (int j = 0; j < contentGroups.size(); j++) {
            Path path = contentGroups.get(j).getPath();
            path.computeBounds(this.rectF, false);
            this.matrix.set(parentMatrix);
            this.matrix.preTranslate(0.0f, (-documentData.baselineShift) * Utils.dpScale());
            this.matrix.preScale(fontScale, fontScale);
            path.transform(this.matrix);
            if (documentData.strokeOverFill) {
                drawGlyph(path, this.fillPaint, canvas);
                drawGlyph(path, this.strokePaint, canvas);
            } else {
                drawGlyph(path, this.strokePaint, canvas);
                drawGlyph(path, this.fillPaint, canvas);
            }
        }
    }

    private void drawGlyph(Path path, Paint paint, Canvas canvas) {
        if (paint.getColor() == 0) {
            return;
        }
        if (paint.getStyle() == Paint.Style.STROKE && paint.getStrokeWidth() == 0.0f) {
            return;
        }
        canvas.drawPath(path, paint);
    }

    private void drawCharacterFromFont(String character, DocumentData documentData, Canvas canvas) {
        if (documentData.strokeOverFill) {
            drawCharacter(character, this.fillPaint, canvas);
            drawCharacter(character, this.strokePaint, canvas);
        } else {
            drawCharacter(character, this.strokePaint, canvas);
            drawCharacter(character, this.fillPaint, canvas);
        }
    }

    private void drawCharacter(String character, Paint paint, Canvas canvas) {
        if (paint.getColor() == 0) {
            return;
        }
        if (paint.getStyle() == Paint.Style.STROKE && paint.getStrokeWidth() == 0.0f) {
            return;
        }
        canvas.drawText(character, 0, character.length(), 0.0f, 0.0f, paint);
    }

    private List<ContentGroup> getContentsForCharacter(FontCharacter character) {
        if (this.contentsForCharacter.containsKey(character)) {
            return this.contentsForCharacter.get(character);
        }
        List<ShapeGroup> shapes = character.getShapes();
        int size = shapes.size();
        List<ContentGroup> contents = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            ShapeGroup sg = shapes.get(i);
            contents.add(new ContentGroup(this.lottieDrawable, this, sg));
        }
        this.contentsForCharacter.put(character, contents);
        return contents;
    }

    private String codePointToString(String text, int startIndex) {
        int firstCodePoint = text.codePointAt(startIndex);
        int firstCodePointLength = Character.charCount(firstCodePoint);
        int key = firstCodePoint;
        int index = startIndex + firstCodePointLength;
        while (index < text.length()) {
            int nextCodePoint = text.codePointAt(index);
            if (!isModifier(nextCodePoint)) {
                break;
            }
            int nextCodePointLength = Character.charCount(nextCodePoint);
            index += nextCodePointLength;
            key = (key * 31) + nextCodePoint;
        }
        if (this.codePointCache.containsKey(key)) {
            return this.codePointCache.get(key);
        }
        this.stringBuilder.setLength(0);
        int i = startIndex;
        while (i < index) {
            int codePoint = text.codePointAt(i);
            this.stringBuilder.appendCodePoint(codePoint);
            i += Character.charCount(codePoint);
        }
        String str = this.stringBuilder.toString();
        this.codePointCache.put(key, str);
        return str;
    }

    private boolean isModifier(int codePoint) {
        return Character.getType(codePoint) == 16 || Character.getType(codePoint) == 27 || Character.getType(codePoint) == 6 || Character.getType(codePoint) == 28 || Character.getType(codePoint) == 19;
    }

    @Override // com.airbnb.lottie.model.layer.BaseLayer, com.airbnb.lottie.model.KeyPathElement
    public <T> void addValueCallback(T property, LottieValueCallback<T> callback) {
        super.addValueCallback(property, callback);
        if (property == LottieProperty.COLOR) {
            BaseKeyframeAnimation<Integer, Integer> baseKeyframeAnimation = this.colorCallbackAnimation;
            if (baseKeyframeAnimation != null) {
                removeAnimation(baseKeyframeAnimation);
            }
            if (callback == null) {
                this.colorCallbackAnimation = null;
                return;
            }
            ValueCallbackKeyframeAnimation valueCallbackKeyframeAnimation = new ValueCallbackKeyframeAnimation(callback);
            this.colorCallbackAnimation = valueCallbackKeyframeAnimation;
            valueCallbackKeyframeAnimation.addUpdateListener(this);
            addAnimation(this.colorCallbackAnimation);
            return;
        }
        if (property == LottieProperty.STROKE_COLOR) {
            BaseKeyframeAnimation<Integer, Integer> baseKeyframeAnimation2 = this.strokeColorCallbackAnimation;
            if (baseKeyframeAnimation2 != null) {
                removeAnimation(baseKeyframeAnimation2);
            }
            if (callback == null) {
                this.strokeColorCallbackAnimation = null;
                return;
            }
            ValueCallbackKeyframeAnimation valueCallbackKeyframeAnimation2 = new ValueCallbackKeyframeAnimation(callback);
            this.strokeColorCallbackAnimation = valueCallbackKeyframeAnimation2;
            valueCallbackKeyframeAnimation2.addUpdateListener(this);
            addAnimation(this.strokeColorCallbackAnimation);
            return;
        }
        if (property == LottieProperty.STROKE_WIDTH) {
            BaseKeyframeAnimation<Float, Float> baseKeyframeAnimation3 = this.strokeWidthCallbackAnimation;
            if (baseKeyframeAnimation3 != null) {
                removeAnimation(baseKeyframeAnimation3);
            }
            if (callback == null) {
                this.strokeWidthCallbackAnimation = null;
                return;
            }
            ValueCallbackKeyframeAnimation valueCallbackKeyframeAnimation3 = new ValueCallbackKeyframeAnimation(callback);
            this.strokeWidthCallbackAnimation = valueCallbackKeyframeAnimation3;
            valueCallbackKeyframeAnimation3.addUpdateListener(this);
            addAnimation(this.strokeWidthCallbackAnimation);
            return;
        }
        if (property == LottieProperty.TEXT_TRACKING) {
            BaseKeyframeAnimation<Float, Float> baseKeyframeAnimation4 = this.trackingCallbackAnimation;
            if (baseKeyframeAnimation4 != null) {
                removeAnimation(baseKeyframeAnimation4);
            }
            if (callback == null) {
                this.trackingCallbackAnimation = null;
                return;
            }
            ValueCallbackKeyframeAnimation valueCallbackKeyframeAnimation4 = new ValueCallbackKeyframeAnimation(callback);
            this.trackingCallbackAnimation = valueCallbackKeyframeAnimation4;
            valueCallbackKeyframeAnimation4.addUpdateListener(this);
            addAnimation(this.trackingCallbackAnimation);
            return;
        }
        if (property == LottieProperty.TEXT_SIZE) {
            BaseKeyframeAnimation<Float, Float> baseKeyframeAnimation5 = this.textSizeCallbackAnimation;
            if (baseKeyframeAnimation5 != null) {
                removeAnimation(baseKeyframeAnimation5);
            }
            if (callback == null) {
                this.textSizeCallbackAnimation = null;
                return;
            }
            ValueCallbackKeyframeAnimation valueCallbackKeyframeAnimation5 = new ValueCallbackKeyframeAnimation(callback);
            this.textSizeCallbackAnimation = valueCallbackKeyframeAnimation5;
            valueCallbackKeyframeAnimation5.addUpdateListener(this);
            addAnimation(this.textSizeCallbackAnimation);
        }
    }
}
