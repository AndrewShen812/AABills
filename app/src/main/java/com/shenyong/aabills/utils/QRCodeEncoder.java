/*
 * Copyright (C) 2008 ZXing authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.shenyong.aabills.utils;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import com.sddy.utils.DimenUtils;

import java.util.EnumMap;
import java.util.Map;

public class QRCodeEncoder {

  private static final int WHITE = 0xFFFFFFFF;
  private static final int BLACK = 0xFF000000;

  public static Bitmap encodeAsBitmap(String contentsToEncode, int dimension, Bitmap centerIcon)
      throws WriterException {
    if (contentsToEncode == null) {
      return null;
    }
    Map<EncodeHintType, Object> hints = new EnumMap<>(EncodeHintType.class);
    // 影响扫码枪识别
//    hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
      // 影响生成二维码的复杂度
    hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
    hints.put(EncodeHintType.MARGIN, 0);
    BitMatrix result;
    try {
      result =
          new MultiFormatWriter()
              .encode(contentsToEncode, BarcodeFormat.QR_CODE, dimension, dimension, hints);
    } catch (IllegalArgumentException iae) {
      // Unsupported format
      return null;
    }
    int width = result.getWidth();
    int height = result.getHeight();
    int[] pixels = new int[width * height];
    for (int y = 0; y < height; y++) {
      int offset = y * width;
      for (int x = 0; x < width; x++) {
        pixels[offset + x] = result.get(x, y) ? BLACK : WHITE;
      }
    }
    Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
    bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
      Canvas canvas = new Canvas(bitmap);
      int size = DimenUtils.dp2px(60);
      int left = width / 2 - size / 2;
      int top = height / 2 - size / 2;
      Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
      paint.setFilterBitmap(true);
//      canvas.drawBitmap(centerIcon, null, new Rect(left, top, left + size, top + size), paint);
      canvas.drawBitmap(getCornerBitmap(centerIcon), null, new Rect(left, top, left + size, top + size), paint);
    return bitmap;
  }

  private static Bitmap getCornerBitmap(Bitmap orgBitmap) {
//      int border = DimenUtils.dp2px(4);
      int border = 0;
      Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
      paint.setFilterBitmap(true);
      Path p = new Path();
      int cornerR = border;

      Bitmap bitmap = Bitmap.createBitmap(orgBitmap.getWidth() + border * 2, orgBitmap.getHeight() + border * 2, Bitmap.Config.RGB_565);
      Canvas canvas = new Canvas(bitmap);
      p.reset();
      canvas.drawColor(Color.WHITE);
      paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
      paint.setColor(Color.WHITE);
      canvas.drawRoundRect(0, 0, bitmap.getWidth(), bitmap.getHeight(), cornerR * 2, cornerR * 2, paint);
      paint.setXfermode(null);
      canvas.drawBitmap(orgBitmap, border, border, paint);

      return bitmap;
  }
}
