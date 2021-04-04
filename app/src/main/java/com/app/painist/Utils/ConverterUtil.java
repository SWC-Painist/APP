package com.app.painist.Utils;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Picture;
import android.graphics.drawable.PictureDrawable;
import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

import com.caverock.androidsvg.SVG;
import com.caverock.androidsvg.SVGParseException;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class ConverterUtil {

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public static Bitmap SVGString2Bitmap(String srcSVG) {
        InputStream svgStream = new ByteArrayInputStream(srcSVG.getBytes(StandardCharsets.UTF_8));
        SVG svg = null;
        try {
            svg = SVG.getFromInputStream(svgStream);
        } catch (SVGParseException e) {
            e.printStackTrace();
            Log.e("SVG", "SVG格式错误");
        }
        Log.d("Height", String.valueOf(svg.getDocumentHeight()));
        Log.d("DPI", String.valueOf(svg.getRenderDPI()));

        Bitmap bitmap = Bitmap.createBitmap((int) svg.getDocumentWidth(), (int) svg.getDocumentHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        svg.renderToCanvas(canvas);
        return bitmap;
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public static Bitmap SVGString2Bitmap(String srcSVG, int targetHeight) {
        InputStream svgStream = new ByteArrayInputStream(srcSVG.getBytes(StandardCharsets.UTF_8));
        SVG svg = null;
        try {
            svg = SVG.getFromInputStream(svgStream);
        } catch (SVGParseException e) {
            e.printStackTrace();
            Log.e("SVG", "SVG格式错误");
        }

        Bitmap bitmap = Bitmap.createBitmap((int) svg.getDocumentWidth(), (int) svg.getDocumentHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        svg.renderToCanvas(canvas);
        return bitmap;
    }

    public static Bitmap getResizedBitmap(Bitmap bm, int newWidth, int newHeight) {
        int width = bm.getWidth();
        int height = bm.getHeight();
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        // CREATE A MATRIX FOR THE MANIPULATION
        Matrix matrix = new Matrix();
        // RESIZE THE BIT MAP
        matrix.postScale(scaleWidth, scaleHeight);

        // "RECREATE" THE NEW BITMAP
        Bitmap resizedBitmap = Bitmap.createBitmap(
                bm, 0, 0, width, height, matrix, false);
        bm.recycle();
        return resizedBitmap;
    }
}
