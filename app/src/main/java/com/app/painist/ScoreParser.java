package com.app.painist;

import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

import org.apache.commons.io.IOUtils;
import org.jfugue.integration.MusicXmlParser;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;

import javax.xml.parsers.ParserConfigurationException;

import nu.xom.ParsingException;

/* class ScoreParser
 * 用于解码MusicXml文件，并向ScoreParserListener发送解码数据
 * 内部关联MusicXmlParser(Extends JFugue.Parser)，通过该函数的主要功能解码
 **************
 * by Criheacy
 * last-edit: 2021/3/2 20:44
 */
public class ScoreParser {

    protected Score score;
    protected MusicXmlParser musicXmlParser;
    protected ScoreParserListener scoreParserListener;

    public ScoreParser() {
        // 初始化parser
        try {
            musicXmlParser = new MusicXmlParser();
        } catch (ParserConfigurationException e) {
            Log.d("Error", "Configuration Exception");
            e.printStackTrace();
            return;
        }
        score = new Score();
        scoreParserListener = new ScoreParserListener();
        scoreParserListener.setScore(score);
        musicXmlParser.addParserListener(scoreParserListener);
    }

    public Score getScore() {
        return score;
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public void parse(String xmlString) {

        try {
            Log.d("Debug:", "XML Start Rendering");
            musicXmlParser.parse(xmlString);
        } catch (IOException e) {
            Log.d("Error", "FileIO Exception");
            e.printStackTrace();
            return;
        } catch (nu.xom.ValidityException e) {
            Log.d("Error", "Validity Exception");
            e.printStackTrace();
            return;
        } catch (ParsingException e) {
            Log.d("Error", "Parsing Exception");
            e.printStackTrace();
            return;
        }

        Log.d("Debug:", "Finished!");
    }
}