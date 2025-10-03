package com.google.android.gms.common.server.response;

import com.google.android.gms.common.server.response.FastParser;
import java.io.BufferedReader;
import java.io.IOException;

/* compiled from: com.google.android.gms:play-services-base@@17.1.0 */
/* loaded from: classes.dex */
final class zae implements FastParser.zaa<String> {
    @Override // com.google.android.gms.common.server.response.FastParser.zaa
    public final /* synthetic */ String zah(FastParser fastParser, BufferedReader bufferedReader) throws FastParser.ParseException, IOException {
        String zac;
        zac = fastParser.zac(bufferedReader);
        return zac;
    }
}
