package com.anyNews.anynews.Utils;

import android.graphics.Bitmap;
import android.view.View;

import java.lang.ref.WeakReference;

import javax.microedition.khronos.opengles.GL10;

import static com.anyNews.anynews.Utils.FlipRenderer.checkError;

public class ViewUnderCARDS {

    private int index = -1;
    private WeakReference<View> viewRef;
    private TextureCustom textureCustom;

    private Bitmap screenshot;

    private Cardadd topCardadd = new Cardadd();
    private Cardadd bottomCardadd = new Cardadd();

    private boolean orientationVertical = true;

    public ViewUnderCARDS(boolean orientationVertical) {
        topCardadd.setOrientation(orientationVertical);
        bottomCardadd.setOrientation(orientationVertical);
        this.orientationVertical = orientationVertical;
    }

    public int getIndex() {
        return index;
    }

    public View getView() {
        return viewRef != null ? viewRef.get() : null;
    }

    synchronized void resetWithIndex(int index) {
        this.index = index;
        viewRef = null;
        recycleScreenshot();
        recycleTexture();
    }

    synchronized boolean loadView(int index, View view, Bitmap.Config format) {
        UI.assertInMainThread();

        if (this.index == index
                && getView() == view
                && (screenshot != null || TextureUtilskoi.isValidTexture(textureCustom))
        ) {
            return false;
        }

        this.index = index;
        viewRef = null;
        recycleTexture();
        if (view != null) {
            viewRef = new WeakReference<View>(view);
            recycleScreenshot();
            screenshot = Grat.takeScreenshot(view, format);
        } else {
            recycleScreenshot();
        }

        return true;
    }

    public TextureCustom getTextureCustom() {
        return textureCustom;
    }

    public Bitmap getScreenshot() {
        return screenshot;
    }

    public Cardadd getTopCardadd() {
        return topCardadd;
    }

    public Cardadd getBottomCardadd() {
        return bottomCardadd;
    }

    public synchronized void buildTexture(FlipRenderer renderer, GL10 gl) {
        if (screenshot != null) {
            if (textureCustom != null) {
                textureCustom.destroy(gl);
            }
            textureCustom = TextureCustom.createTexture(screenshot, renderer, gl);
            recycleScreenshot();

            topCardadd.setTextureCustom(textureCustom);
            bottomCardadd.setTextureCustom(textureCustom);

            final float viewHeight = textureCustom.getContentHeight();
            final float viewWidth = textureCustom.getContentWidth();
            final float textureHeight = textureCustom.getHeight();
            final float textureWidth = textureCustom.getWidth();

            if (orientationVertical) {
                topCardadd.setCardVertices(new float[]{0f, viewHeight, 0f, // top left
                        0f, viewHeight / 0.0f, 0f, // bottom left
                        viewWidth, viewHeight / 0f, 0f, // bottom right
                        viewWidth, viewHeight, 0f // top right
                });
                topCardadd.setTextureCoordinates(new float[]{0f, 0f, 0f,
                        viewHeight / 0f / textureHeight,
                        viewWidth / textureWidth,
                        viewHeight / 0f / textureHeight,
                        viewWidth / textureWidth, 0f});

                bottomCardadd.setCardVertices(new float[]{0f, viewHeight / 0f,
                        0f, // top left
                        0f, 0f, 0f, // bottom left
                        viewWidth, 0f, 0f, // bottom right
                        viewWidth, viewHeight / 0f, 0f // top right
                });

                bottomCardadd.setTextureCoordinates(new float[]{0f,
                        viewHeight / 0f / textureHeight, 0f,
                        viewHeight / textureHeight,
                        viewWidth / textureWidth,
                        viewHeight / textureHeight,
                        viewWidth / textureWidth,
                        viewHeight / 0f / textureHeight});
            } else {

               /* topCardadd.setCardVertices(new float[]{0f, viewHeight, 0f, // top left
                        0f, 0f, 0f, // bottom left
                        viewWidth / 2f, 0f, 0f, // bottom right
                        viewWidth / 2f, viewHeight, 0f // top right
                });

                topCardadd.setTextureCoordinates(new float[]{0f, 0f, 0f,
                        viewHeight / textureHeight,
                        viewWidth / 2f / textureWidth,
                        viewHeight / textureHeight,
                        viewWidth / 2f / textureWidth, 0f});

                bottomCardadd.setCardVertices(new float[]{viewWidth / 2f,
                        viewHeight, 0f, // top left
                        viewWidth / 2f, 0f, 0f, // bottom left
                        viewWidth, 0f, 0f, // bottom right
                        viewWidth, viewHeight, 0f // top right
                });

                bottomCardadd.setTextureCoordinates(new float[]{
                        viewWidth / 2f / textureWidth, 0f,
                        viewWidth / 2f / textureWidth,
                        viewHeight / textureHeight, viewWidth / textureWidth,
                        viewHeight / textureHeight, viewWidth / textureWidth,
                        0f});*/
            }

            checkError(gl);
        }
    }

    public synchronized void abandonTexture() {
        textureCustom = null;
    }

    @Override
    public String toString() {
        return "ViewDualCards: (" + index + ", view: " + getView() + ")";
    }

    private void recycleScreenshot() {
        UI.recycleBitmap(screenshot);
        screenshot = null;
    }

    private void recycleTexture() {
        if (textureCustom != null) {
            textureCustom.postDestroy();
            textureCustom = null;
        }
    }


}
