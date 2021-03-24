package com.app.painist;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

/*
 * 传输数据格式 (JSON)
 * 发送请求：
 * {
 *     type: "history"/"favorite"/"recommend" （表示请求曲目类型：历史曲谱、我的收藏、猜你想练）
 *     id: [number] （表示请求的曲目在前端第几个显示，同时也表示前端请求发送的顺序）
 *     code: [number] （前端生成的随机编码，不同请求的code数字唯一）
 * }
 *
 * 接收请求：
 * {
 *     code: [number] （表示响应前端对应的code）
 *     image: {
 *         url: [string] （表示传回image的对应url）
 *         name: [string] （表示传回image的文件名）
 *     }
 *     attribute: {
 *         name: [string] （表示曲谱名）
 *         proficiency: 0-100 （表示熟练程度）
 *         difficulty: 1/2/3/4/5 （表示难度）
 *         last_practice: [string(time)] （表示最后一次练习的时间）
 *         recommend_attr: {    （表示推荐属性，如果不是推荐乐谱直接填null）
 *             from: [string] （表示因为哪首曲子推荐而来，填曲目名称）
 *             weigh: [number] 0-100 （表示推荐程度）
 *         }
 *     }
 * }
 */

public class Scorelist extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
}