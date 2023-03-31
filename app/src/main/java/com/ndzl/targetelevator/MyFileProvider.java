package com.ndzl.targetelevator;

import android.database.Cursor;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;

//https://developer.android.com/reference/androidx/core/content/FileProvider
//https://developer.android.com/training/secure-file-sharing/setup-sharing

public class MyFileProvider extends FileProvider {
    public MyFileProvider() {
        super(R.xml.provider_paths);
    }

    @NonNull
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        return super.query(uri, projection, selection, selectionArgs, sortOrder);
    }
    // The root path of this subdirectory is the same as the value returned by getCacheDir().



}
