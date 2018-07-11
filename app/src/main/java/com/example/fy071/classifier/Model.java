package com.example.fy071.classifier;


import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

import java.io.File;

public class Model implements Parcelable {
    public static final String INVALID_ID = "null";

    public String name;
    public File file;

    public String[] labels;
    public String[] trueLabels;

    public File[] rawImages;
    public File[] jpgImages;
    public File meanImage;

    protected Model(Parcel in) {
        name = in.readString();
        file = new File(in.readString());

        final String[] rawPaths = new String[in.readInt()];
        in.readStringArray(rawPaths);
        rawImages = fromPaths(rawPaths);

        final String[] jpgPaths = new String[in.readInt()];
        in.readStringArray(jpgPaths);
        jpgImages = fromPaths(jpgPaths);

        meanImage = new File(in.readString());

        labels = new String[in.readInt()];
        in.readStringArray(labels);

        trueLabels = new String[in.readInt()];
        in.readStringArray(trueLabels);
    }

    public Model() {
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(file.getAbsolutePath());
        dest.writeInt(rawImages.length);
        dest.writeStringArray(toPaths(rawImages));
        dest.writeInt(jpgImages.length);
        dest.writeStringArray(toPaths(jpgImages));
        dest.writeString(meanImage.getAbsolutePath());
        dest.writeInt(labels.length);
        dest.writeStringArray(labels);
        dest.writeInt(trueLabels.length);
        dest.writeStringArray(trueLabels);
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
