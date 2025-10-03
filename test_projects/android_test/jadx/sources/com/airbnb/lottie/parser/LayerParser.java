package com.airbnb.lottie.parser;

import android.graphics.Color;
import android.graphics.Rect;
import com.airbnb.lottie.LottieComposition;
import com.airbnb.lottie.model.animatable.AnimatableFloatValue;
import com.airbnb.lottie.model.animatable.AnimatableTextFrame;
import com.airbnb.lottie.model.animatable.AnimatableTextProperties;
import com.airbnb.lottie.model.animatable.AnimatableTransform;
import com.airbnb.lottie.model.content.ContentModel;
import com.airbnb.lottie.model.content.Mask;
import com.airbnb.lottie.model.layer.Layer;
import com.airbnb.lottie.parser.moshi.JsonReader;
import com.airbnb.lottie.utils.Utils;
import com.airbnb.lottie.value.Keyframe;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/* loaded from: classes.dex */
public class LayerParser {
    private static final JsonReader.Options NAMES = JsonReader.Options.m16of("nm", "ind", "refId", "ty", "parent", "sw", "sh", "sc", "ks", "tt", "masksProperties", "shapes", "t", "ef", "sr", "st", "w", "h", "ip", "op", "tm", "cl", "hd");
    private static final JsonReader.Options TEXT_NAMES = JsonReader.Options.m16of("d", "a");
    private static final JsonReader.Options EFFECTS_NAMES = JsonReader.Options.m16of("nm");

    private LayerParser() {
    }

    public static Layer parse(LottieComposition composition) {
        Rect bounds = composition.getBounds();
        return new Layer(Collections.emptyList(), composition, "__container", -1L, Layer.LayerType.PRE_COMP, -1L, null, Collections.emptyList(), new AnimatableTransform(), 0, 0, 0, 0.0f, 0.0f, bounds.width(), bounds.height(), null, null, Collections.emptyList(), Layer.MatteType.NONE, null, false);
    }

    public static Layer parse(JsonReader reader, LottieComposition composition) throws IOException {
        List<ContentModel> shapes;
        List<Mask> masks;
        List<ContentModel> shapes2;
        float inFrame = 0.0f;
        float outFrame = 0.0f;
        Layer.MatteType matteType = Layer.MatteType.NONE;
        List<Mask> masks2 = new ArrayList<>();
        List<ContentModel> shapes3 = new ArrayList<>();
        reader.beginObject();
        Layer.LayerType layerType = null;
        String refId = null;
        long layerId = 0;
        int solidWidth = 0;
        int solidHeight = 0;
        int solidColor = 0;
        int preCompWidth = 0;
        int preCompHeight = 0;
        long parentId = -1;
        float timeStretch = 1.0f;
        float startFrame = 0.0f;
        String cl = null;
        boolean hidden = false;
        Layer.MatteType matteType2 = matteType;
        AnimatableTransform transform = null;
        AnimatableTextFrame text = null;
        AnimatableTextProperties textProperties = null;
        AnimatableFloatValue timeRemapping = null;
        String layerName = "UNSET";
        while (reader.hasNext()) {
            switch (reader.selectName(NAMES)) {
                case 0:
                    layerName = reader.nextString();
                    continue;
                case 1:
                    long layerId2 = reader.nextInt();
                    layerId = layerId2;
                    continue;
                case 2:
                    refId = reader.nextString();
                    continue;
                case 3:
                    List<Mask> masks3 = masks2;
                    List<ContentModel> shapes4 = shapes3;
                    int layerTypeInt = reader.nextInt();
                    if (layerTypeInt < Layer.LayerType.UNKNOWN.ordinal()) {
                        layerType = Layer.LayerType.values()[layerTypeInt];
                        masks2 = masks3;
                        shapes3 = shapes4;
                        break;
                    } else {
                        layerType = Layer.LayerType.UNKNOWN;
                        masks2 = masks3;
                        shapes3 = shapes4;
                        continue;
                    }
                case 4:
                    long parentId2 = reader.nextInt();
                    parentId = parentId2;
                    continue;
                case 5:
                    int solidWidth2 = (int) (reader.nextInt() * Utils.dpScale());
                    solidWidth = solidWidth2;
                    continue;
                case 6:
                    int solidHeight2 = (int) (reader.nextInt() * Utils.dpScale());
                    solidHeight = solidHeight2;
                    continue;
                case 7:
                    solidColor = Color.parseColor(reader.nextString());
                    continue;
                case 8:
                    transform = AnimatableTransformParser.parse(reader, composition);
                    continue;
                case 9:
                    matteType2 = Layer.MatteType.values()[reader.nextInt()];
                    composition.incrementMatteOrMaskCount(1);
                    continue;
                case 10:
                    shapes = shapes3;
                    reader.beginArray();
                    while (reader.hasNext()) {
                        masks2.add(MaskParser.parse(reader, composition));
                    }
                    masks = masks2;
                    composition.incrementMatteOrMaskCount(masks.size());
                    reader.endArray();
                    break;
                case 11:
                    reader.beginArray();
                    while (reader.hasNext()) {
                        ContentModel shape = ContentModelParser.parse(reader, composition);
                        if (shape == null) {
                            shapes2 = shapes3;
                        } else {
                            shapes2 = shapes3;
                            shapes2.add(shape);
                        }
                        shapes3 = shapes2;
                    }
                    shapes = shapes3;
                    reader.endArray();
                    masks = masks2;
                    break;
                case 12:
                    reader.beginObject();
                    while (reader.hasNext()) {
                        switch (reader.selectName(TEXT_NAMES)) {
                            case 0:
                                text = AnimatableValueParser.parseDocumentData(reader, composition);
                                break;
                            case 1:
                                reader.beginArray();
                                if (reader.hasNext()) {
                                    textProperties = AnimatableTextPropertiesParser.parse(reader, composition);
                                }
                                while (reader.hasNext()) {
                                    reader.skipValue();
                                }
                                reader.endArray();
                                break;
                            default:
                                reader.skipName();
                                reader.skipValue();
                                break;
                        }
                    }
                    reader.endObject();
                    continue;
                case 13:
                    reader.beginArray();
                    List<String> effectNames = new ArrayList<>();
                    while (reader.hasNext()) {
                        reader.beginObject();
                        while (reader.hasNext()) {
                            switch (reader.selectName(EFFECTS_NAMES)) {
                                case 0:
                                    effectNames.add(reader.nextString());
                                    break;
                                default:
                                    reader.skipName();
                                    reader.skipValue();
                                    break;
                            }
                        }
                        reader.endObject();
                    }
                    reader.endArray();
                    composition.addWarning("Lottie doesn't support layer effects. If you are using them for  fills, strokes, trim paths etc. then try adding them directly as contents  in your shape. Found: " + effectNames);
                    masks = masks2;
                    shapes = shapes3;
                    break;
                case 14:
                    float timeStretch2 = (float) reader.nextDouble();
                    timeStretch = timeStretch2;
                    continue;
                case 15:
                    float startFrame2 = (float) reader.nextDouble();
                    startFrame = startFrame2;
                    continue;
                case 16:
                    int preCompHeight2 = reader.nextInt();
                    int preCompWidth2 = (int) (preCompHeight2 * Utils.dpScale());
                    preCompWidth = preCompWidth2;
                    continue;
                case 17:
                    int preCompHeight3 = (int) (reader.nextInt() * Utils.dpScale());
                    preCompHeight = preCompHeight3;
                    continue;
                case 18:
                    float inFrame2 = (float) reader.nextDouble();
                    inFrame = inFrame2;
                    continue;
                case 19:
                    float outFrame2 = (float) reader.nextDouble();
                    outFrame = outFrame2;
                    continue;
                case 20:
                    timeRemapping = AnimatableValueParser.parseFloat(reader, composition, false);
                    continue;
                case 21:
                    cl = reader.nextString();
                    continue;
                case 22:
                    hidden = reader.nextBoolean();
                    continue;
                default:
                    masks = masks2;
                    shapes = shapes3;
                    reader.skipName();
                    reader.skipValue();
                    break;
            }
            masks2 = masks;
            shapes3 = shapes;
        }
        List<Mask> masks4 = masks2;
        List<ContentModel> shapes5 = shapes3;
        reader.endObject();
        float inFrame3 = inFrame / timeStretch;
        float outFrame3 = outFrame / timeStretch;
        List<Keyframe<Float>> inOutKeyframes = new ArrayList<>();
        if (inFrame3 > 0.0f) {
            Keyframe<Float> preKeyframe = new Keyframe<>(composition, Float.valueOf(0.0f), Float.valueOf(0.0f), null, 0.0f, Float.valueOf(inFrame3));
            inOutKeyframes.add(preKeyframe);
        }
        float outFrame4 = outFrame3 > 0.0f ? outFrame3 : composition.getEndFrame();
        Keyframe<Float> visibleKeyframe = new Keyframe<>(composition, Float.valueOf(1.0f), Float.valueOf(1.0f), null, inFrame3, Float.valueOf(outFrame4));
        inOutKeyframes.add(visibleKeyframe);
        String cl2 = cl;
        String layerName2 = layerName;
        Keyframe<Float> outKeyframe = new Keyframe<>(composition, Float.valueOf(0.0f), Float.valueOf(0.0f), null, outFrame4, Float.valueOf(Float.MAX_VALUE));
        inOutKeyframes.add(outKeyframe);
        if (layerName2.endsWith(".ai") || "ai".equals(cl2)) {
            composition.addWarning("Convert your Illustrator layers to shape layers.");
        }
        return new Layer(shapes5, composition, layerName2, layerId, layerType, parentId, refId, masks4, transform, solidWidth, solidHeight, solidColor, timeStretch, startFrame, preCompWidth, preCompHeight, text, textProperties, inOutKeyframes, matteType2, timeRemapping, hidden);
    }
}
