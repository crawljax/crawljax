package com.crawljax.stateabstractions.visual;

import org.bytedeco.javacpp.Loader;
import org.bytedeco.opencv.opencv_java;

public class OpenCVLoad {

    public static String location = "";

    public static boolean loaded = true;

    // static {
    // System.load(location);
    // System.out.println("OPENCV LOADED!!");
    // }
    static {
        Loader.load(opencv_java.class);
    }

    public static boolean load() {
        if (!loaded) {
            loaded = true;
        }

        return true;
    }
}
