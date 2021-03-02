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

    protected MusicXmlParser musicXmlParser;
    protected ScoreRenderer scoreRenderer;

    public ScoreParser() {
        // 初始化parser
        try {
            musicXmlParser = new MusicXmlParser();
        } catch (ParserConfigurationException e) {
            Log.d("Error", "Configuration Exception");
            e.printStackTrace();
            return;
        }
        scoreRenderer = new ScoreRenderer();

        musicXmlParser.addParserListener(scoreRenderer);
    }

//    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
//    public void parse() {
//        // 临时使用本地文件做读取样例，TODO: 替换为后端传来的MUSICXML文件
//
//        String xmlString = "";
//        try {
//            InputStream inputStream = attachedActivity.getApplicationContext().getAssets().open("data/test.musicxml");
//            StringWriter writer = new StringWriter();
//            xmlString = IOUtils.toString(inputStream, StandardCharsets.UTF_8);
//
//        } catch (IOException e) {
//            Log.d("Error", "Path Error");
//            e.printStackTrace();
//        }
//
//        try {
//            Log.d("Debug:", "XML Start Rendering");
//            musicXmlParser.parse(xmlString);
//        } catch (IOException e) {
//            Log.d("Error", "FileIO Exception");
//            e.printStackTrace();
//            return;
//        } catch (nu.xom.ValidityException e) {
//            Log.d("Error", "Validity Exception");
//            e.printStackTrace();
//            return;
//        } catch (ParsingException e) {
//            Log.d("Error", "Parsing Exception");
//            e.printStackTrace();
//            return;
//        }
//
//        Log.d("Debug:", "Finished!");
//    }
}