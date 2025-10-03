package com.airbnb.lottie.parser;

/* loaded from: classes.dex */
class MaskParser {
    private MaskParser() {
    }

    /* JADX WARN: Can't fix incorrect switch cases order, some code will duplicate */
    /* JADX WARN: Code restructure failed: missing block: B:49:0x0091, code lost:
    
        if (r5.equals("a") != false) goto L42;
     */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct add '--show-bad-code' argument
    */
    static com.airbnb.lottie.model.content.Mask parse(com.airbnb.lottie.parser.moshi.JsonReader r12, com.airbnb.lottie.LottieComposition r13) throws java.io.IOException {
        /*
            r0 = 0
            r1 = 0
            r2 = 0
            r3 = 0
            r12.beginObject()
        L7:
            boolean r4 = r12.hasNext()
            if (r4 == 0) goto Lc8
            java.lang.String r4 = r12.nextName()
            int r5 = r4.hashCode()
            r6 = 0
            r7 = 3
            r8 = 1
            r9 = 2
            r10 = -1
            switch(r5) {
                case 111: goto L3c;
                case 3588: goto L32;
                case 104433: goto L28;
                case 3357091: goto L1e;
                default: goto L1d;
            }
        L1d:
            goto L46
        L1e:
            java.lang.String r5 = "mode"
            boolean r5 = r4.equals(r5)
            if (r5 == 0) goto L1d
            r5 = r6
            goto L47
        L28:
            java.lang.String r5 = "inv"
            boolean r5 = r4.equals(r5)
            if (r5 == 0) goto L1d
            r5 = r7
            goto L47
        L32:
            java.lang.String r5 = "pt"
            boolean r5 = r4.equals(r5)
            if (r5 == 0) goto L1d
            r5 = r8
            goto L47
        L3c:
            java.lang.String r5 = "o"
            boolean r5 = r4.equals(r5)
            if (r5 == 0) goto L1d
            r5 = r9
            goto L47
        L46:
            r5 = r10
        L47:
            switch(r5) {
                case 0: goto L61;
                case 1: goto L5b;
                case 2: goto L55;
                case 3: goto L4f;
                default: goto L4a;
            }
        L4a:
            r12.skipValue()
            goto Lc6
        L4f:
            boolean r3 = r12.nextBoolean()
            goto Lc6
        L55:
            com.airbnb.lottie.model.animatable.AnimatableIntegerValue r2 = com.airbnb.lottie.parser.AnimatableValueParser.parseInteger(r12, r13)
            goto Lc6
        L5b:
            com.airbnb.lottie.model.animatable.AnimatableShapeValue r1 = com.airbnb.lottie.parser.AnimatableValueParser.parseShapeData(r12, r13)
            goto Lc6
        L61:
            java.lang.String r5 = r12.nextString()
            int r11 = r5.hashCode()
            switch(r11) {
                case 97: goto L8b;
                case 105: goto L81;
                case 110: goto L77;
                case 115: goto L6d;
                default: goto L6c;
            }
        L6c:
            goto L94
        L6d:
            java.lang.String r6 = "s"
            boolean r5 = r5.equals(r6)
            if (r5 == 0) goto L6c
            r6 = r8
            goto L95
        L77:
            java.lang.String r6 = "n"
            boolean r5 = r5.equals(r6)
            if (r5 == 0) goto L6c
            r6 = r9
            goto L95
        L81:
            java.lang.String r6 = "i"
            boolean r5 = r5.equals(r6)
            if (r5 == 0) goto L6c
            r6 = r7
            goto L95
        L8b:
            java.lang.String r7 = "a"
            boolean r5 = r5.equals(r7)
            if (r5 == 0) goto L6c
            goto L95
        L94:
            r6 = r10
        L95:
            switch(r6) {
                case 0: goto Lc2;
                case 1: goto Lbf;
                case 2: goto Lbc;
                case 3: goto Lb4;
                default: goto L98;
            }
        L98:
            java.lang.StringBuilder r5 = new java.lang.StringBuilder
            r5.<init>()
            java.lang.String r6 = "Unknown mask mode "
            r5.append(r6)
            r5.append(r4)
            java.lang.String r6 = ". Defaulting to Add."
            r5.append(r6)
            java.lang.String r5 = r5.toString()
            com.airbnb.lottie.utils.Logger.warning(r5)
            com.airbnb.lottie.model.content.Mask$MaskMode r0 = com.airbnb.lottie.model.content.Mask.MaskMode.MASK_MODE_ADD
            goto Lc5
        Lb4:
            java.lang.String r5 = "Animation contains intersect masks. They are not supported but will be treated like add masks."
            r13.addWarning(r5)
            com.airbnb.lottie.model.content.Mask$MaskMode r0 = com.airbnb.lottie.model.content.Mask.MaskMode.MASK_MODE_INTERSECT
            goto Lc5
        Lbc:
            com.airbnb.lottie.model.content.Mask$MaskMode r0 = com.airbnb.lottie.model.content.Mask.MaskMode.MASK_MODE_NONE
            goto Lc5
        Lbf:
            com.airbnb.lottie.model.content.Mask$MaskMode r0 = com.airbnb.lottie.model.content.Mask.MaskMode.MASK_MODE_SUBTRACT
            goto Lc5
        Lc2:
            com.airbnb.lottie.model.content.Mask$MaskMode r0 = com.airbnb.lottie.model.content.Mask.MaskMode.MASK_MODE_ADD
        Lc5:
        Lc6:
            goto L7
        Lc8:
            r12.endObject()
            com.airbnb.lottie.model.content.Mask r4 = new com.airbnb.lottie.model.content.Mask
            r4.<init>(r0, r1, r2, r3)
            return r4
        */
        throw new UnsupportedOperationException("Method not decompiled: com.airbnb.lottie.parser.MaskParser.parse(com.airbnb.lottie.parser.moshi.JsonReader, com.airbnb.lottie.LottieComposition):com.airbnb.lottie.model.content.Mask");
    }
}
