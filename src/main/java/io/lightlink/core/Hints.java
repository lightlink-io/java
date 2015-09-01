package io.lightlink.core;

import java.util.List;
import java.util.Map;

public class Hints {

    private boolean autoDetectDroppedClient;
    private int[] progressive;
    private boolean antiXSS;

    private Hints() {
    }


    public boolean isAutoDetectDroppedClient() {
        return autoDetectDroppedClient;
    }

    public int[] getProgressiveBlockSizes() {
        return progressive;
    }

    public static Hints fromParams(Map<String, Object> params) {
        Hints hints = new Hints();
        Object progressiveObj = params.get("!progressive");

        if (progressiveObj != null) {

            if (progressiveObj instanceof Boolean && (Boolean) progressiveObj) {
                hints.progressive = new int[]{100, 1000, 10000};

            } else if (progressiveObj instanceof Number) {
                hints.progressive = new int[]{((Number) progressiveObj).intValue()};

            } else if (progressiveObj instanceof List) {
                List progressiveList = (List) progressiveObj;
                hints.progressive = new int[progressiveList.size()];
                for (int i = 0; i < progressiveList.size(); i++) {
                    hints.progressive[i] = ((Number) progressiveList.get(i)).intValue();
                }
            } else if (progressiveObj instanceof Number[]) {
                Number[] progressiveArray = (Number[]) progressiveObj;
                hints.progressive = new int[progressiveArray.length];
                for (int i = 0; i < progressiveArray.length; i++) {
                    hints.progressive[i] = progressiveArray[i].intValue();
                }
            } else if (progressiveObj instanceof int[]) {
                hints.progressive = (int[]) progressiveObj;
            }
        }

        Object autoDetectDroppedClient = params.get("!autoDetectDroppedClient");
        // true by default
        if (autoDetectDroppedClient != null && autoDetectDroppedClient instanceof Boolean
                && !((Boolean) autoDetectDroppedClient)) {
            hints.autoDetectDroppedClient = false;
        }


        Object antiXSS = params.get("!antiXSS");
        // true by default
        if (antiXSS != null && antiXSS instanceof Boolean
                && !((Boolean) antiXSS)) {
            hints.antiXSS = false;
        }

        return hints;
    }

    public boolean isAntiXSS() {
        return antiXSS;
    }
}
