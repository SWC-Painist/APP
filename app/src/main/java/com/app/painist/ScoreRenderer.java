package com.app.painist;

import android.util.Log;

import org.jfugue.integration.MusicXmlParser;
import org.jfugue.integration.MusicXmlParserListener;
import org.jfugue.parser.Parser;
import org.jfugue.theory.Note;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

class ScoreRenderer extends MusicXmlParserListener {
    protected List<String> renderList;

    @Override
    public void beforeParsingStarts() {
        super.beforeParsingStarts();
        Log.d("Parser", "Start");
    }

    @Override
    public void afterParsingFinished() {
        super.afterParsingFinished();
        Log.d("Parser", "Finished");
    }

    @Override
    public void onNoteParsed(Note note) {
        super.onNoteParsed(note);
        Log.d("Parser", "Note:"+note.getOctave()+" "+note.getValue());
    }
}
