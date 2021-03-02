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

/* class ScoreRenderer
 * 用于按照drawScore函数发来的要求绘制五线谱及谱上的音符
 * 谱的内容是ScoreParserListener通过addScoreNote添加的；当添加完成后可以根据需求渲染界面
 * 存放主要的渲染逻辑
 **************
 * by Criheacy
 * last-edit: 2021/3/2 20:50
 */

class ScoreRenderer {
    protected List<String> renderList;
    
}
