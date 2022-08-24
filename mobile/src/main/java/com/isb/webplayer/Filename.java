package com.isb.webplayer;

public class Filename {

private String fullPath;
    private char pathSeparator, extensionSeparator;

    public Filename(String str, String sep, char ext) {
        fullPath = str;
        pathSeparator = sep.charAt(0);
        extensionSeparator = ext;
    }
    public Filename(String str, char sep, char ext) {
        fullPath = str;
        pathSeparator = sep;
        extensionSeparator = ext;
    }

    public String extension() {
        int dot = fullPath.lastIndexOf(extensionSeparator);
        return fullPath.substring(dot + 1);
    }

    public String unzipfolder() {
        int dot = fullPath.lastIndexOf(extensionSeparator);
        return fullPath.substring(0,dot);
    }

    public String filename() { // gets filename without extension
        int dot = fullPath.lastIndexOf(extensionSeparator);
        int sep = fullPath.lastIndexOf(pathSeparator);
        return fullPath.substring(sep + 1, dot);
    }


    public String path() {
        int sep = fullPath.lastIndexOf(pathSeparator);
        return fullPath.substring(0, sep);
    }
}