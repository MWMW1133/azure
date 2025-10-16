package com.azure.util;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public final class SfuKey {
    private static final String SECRET = "CHANGE_ME_SUPER_SECRET"; // yml로 빼도 됨

    public static long roomForMeeting(long meetingId) { return meetingId; }
    public static String pinForMeeting(long meetingId) { return h("m:"+meetingId).substring(0,12); }

    private static String h(String s){
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(SECRET.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            return Base64.getUrlEncoder().withoutPadding()
                    .encodeToString(mac.doFinal(s.getBytes(StandardCharsets.UTF_8)));
        } catch(Exception e){ throw new RuntimeException(e); }
    }
    private SfuKey(){}
}
