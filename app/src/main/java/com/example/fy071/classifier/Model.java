package com.example.fy071.classifier;


import android.os.Parcel;
import android.os.Parcelable;

import java.io.File;

public class Model implements Parcelable {
    public static final String INVALID_ID = "null";

    public String name;
    public File file;

    public File[] jpgImages;

    public String[] labels;
    public String[] groundTruths;

    public String inputLayer;
    public String outputLayer;

    protected Model(Parcel in) {
        name = in.readString();

        file = new File(in.readString());

        final String[] jpgPaths = new String[in.readInt()];
        in.readStringArray(jpgPaths);
        jpgImages = fromPaths(jpgPaths);

        labels = new String[in.readInt()];
        in.readStringArray(labels);

        groundTruths = new String[in.readInt()];
        in.readStringArray(groundTruths);

        inputLayer = in.readString();
        outputLayer = in.readString();
    }

    public Model() {
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);

        dest.writeString(file.getAbsolutePath());

        dest.writeInt(jpgImages.length);
        dest.writeStringArray(toPaths(jpgImages));

        dest.writeInt(labels.length);
        dest.writeStringArray(labels);

        dest.writeInt(groundTruths.length);
        dest.writeStringArray(groundTruths);

        dest.writeString(inputLayer);

        dest.writeString(outputLayer);
    }

    private File[] fromPaths(String[] paths) {
        final File[] files = new File[paths.length];
        for (int i = 0; i < paths.length; i++) {
            files[i] = new File(paths[i]);
        }
        return files;
    }

    private String[] toPaths(File[] files) {
        final String[] paths = new String[files.length];
        for (int i = 0; i < files.length; i++) {
            paths[i] = files[i].getAbsolutePath();
        }
        return paths;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Model> CREATOR = new Creator<Model>() {
        @Override
        public Model createFromParcel(Parcel in) {
            return new Model(in);
        }

        @Override
        public Model[] newArray(int size) {
            return new Model[size];
        }
    };

    @Override
    public String toString() {
        return name.toUpperCase();
    }
}
