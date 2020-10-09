package com.photatos.dalin.mlkit.md.common;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;

import com.photatos.dalin.mlkit.md.common.GraphicOverlayLabel.Graphic;

/** Draw camera image to background. */
public class CameraImageGraphic extends Graphic {

    private final Bitmap bitmap;

    public CameraImageGraphic(GraphicOverlayLabel overlay, Bitmap bitmap) {
        super(overlay);
        this.bitmap = bitmap;
    }

    @Override
    public void draw(Canvas canvas) {
        canvas.drawBitmap(bitmap, null, new Rect(0, 0, canvas.getWidth(), canvas.getHeight()), null);
    }
}

