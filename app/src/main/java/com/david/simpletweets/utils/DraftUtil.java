package com.david.simpletweets.utils;

import android.content.Context;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * Created by David on 3/26/2017.
 */

public class DraftUtil {

    Context context;
    String savedDraft;

    public DraftUtil(Context context) {
        this.context = context;
        readFromDisc();
    }

    public void save(String draft) {
        savedDraft = draft;
        writeToDisc();
    }

    public String load() {
        return savedDraft;
    }

    public void discard() {
        savedDraft = "";
        delete();
    }

    private void writeToDisc() {
        File filesDir = context.getFilesDir();
        File file = new File(filesDir, "draft.txt");
        try {
            FileUtils.write(file, savedDraft);
        } catch (IOException e) {

        }
    }

    private void readFromDisc() {
        File filesDir = context.getFilesDir();
        File file = new File(filesDir, "draft.txt");
        try {
            savedDraft = FileUtils.readFileToString(file);
        } catch (FileNotFoundException e) {
            savedDraft = "";
        } catch (IOException e) {

        }
    }

    private void delete() {
        File filesDir = context.getFilesDir();
        File file = new File(filesDir, "draft.txt");
        FileUtils.deleteQuietly(file);
    }
}
