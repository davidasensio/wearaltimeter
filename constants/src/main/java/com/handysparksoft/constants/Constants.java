package com.handysparksoft.constants;

public class Constants {
    public static String PATH_ACTION_MESSAGE = "action/";
    public static String PATH_DATA_MESSAGE = "message/";

    public static String ACTION_START_METERING = "startMetering";
    public static String ACTION_STOP_METERING = "stopMetering";

    public interface SERVICE_ACTION {
        int FOREGROUND_ALTIMETER_SERVICE_NOTIFICATION_ID = 101;
        String MAIN_ACTION = "com.handysparksoft.constants.action.main";
        String PREV_ACTION = "com.handysparksoft.constants.action.prev";
        String PLAY_ACTION = "com.handysparksoft.constants.action.play";
        String NEXT_ACTION = "com.handysparksoft.constants.action.next";
        String STARTFOREGROUND_ACTION = "com.handysparksoft.constants.action.startforeground";
        String STOPFOREGROUND_ACTION = "com.handysparksoft.constants.action.stopforeground";
    }
}
