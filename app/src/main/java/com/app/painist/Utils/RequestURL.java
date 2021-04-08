package com.app.painist.Utils;

public class RequestURL {
    public final static String main = "http://101.76.217.74:8000/user/";

    public final static String login = main + "login/";
    public final static String register = main + "register/";

    public final static String history = main + "history/";
    public final static String favorite = main + "favorite/";
    public final static String recommend = main + "recommend/";

    public final static String uploadImage = main + "upload/picture/";
    public final static String uploadImageInfo = main + "upload/picture_info/";

    public final static String uploadAudio = main + "upload/video/";
    public final static String checkAudio = main + "check/video/";

    // OMR Algorithm: Asking for progress
    public final static String getOMRProgress = main + "ask_progress/";

    // Debug only
    public final static String debugTest = main + "practice/progress/";
}
