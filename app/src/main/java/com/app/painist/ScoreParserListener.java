package com.app.painist;

import android.util.Log;

import org.jfugue.integration.MusicXmlParserListener;
import org.jfugue.theory.Note;

import java.util.List;

/* class ScoreParserListener
 * 重写回调函数，用于监听ScoreParser发来的事件，并更新ScoreRenderer中的ScoreNoteList
 * 关联对应的ScoreRenderer类，调用其addNote接口实现旋律添加
 **************
 * by Criheacy
 * last-edit: 2021/3/2 20:47
 */
class ScoreParserListener extends MusicXmlParserListener {
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
