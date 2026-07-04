/*
Copyright 2012 Aphid Mobile

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at
 
	 http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

 */

package com.anyNews.anynews.myflip.flip;

import android.graphics.Bitmap;
import android.view.View;


import com.anyNews.anynews.myflip.utils.TextureUtilsArman;
import com.anyNews.anynews.myflip.utils.UIArman;

import java.lang.ref.WeakReference;

import javax.microedition.khronos.opengles.GL10;

import static com.anyNews.anynews.myflip.flip.FlipRendererArman.checkError;


public class ViewDualCardsArman {

  private int index = -1;
  private WeakReference<View> viewRef;
  private TextureArman textureArman;

  private Bitmap screenshot;

  private CardArman topCardArman = new CardArman();
  private CardArman bottomCardArman = new CardArman();

  private boolean orientationVertical = true;

  public ViewDualCardsArman(boolean orientationVertical) {
    topCardArman.setOrientation(orientationVertical);
    bottomCardArman.setOrientation(orientationVertical);
    this.orientationVertical = orientationVertical;
  }

  public int getIndex() {
    return index;
  }

  public View getView() {
    return viewRef != null ? viewRef.get() : null;
  }

  public synchronized void resetWithIndex(int index) {
    this.index = index;
    viewRef = null;
    recycleScreenshot();
    recycleTexture();
  }

  public synchronized boolean loadView(int index, View view, Bitmap.Config format) {
    UIArman.assertInMainThread();

    if (this.index == index
        && getView() == view
        && (screenshot != null || TextureUtilsArman.isValidTexture(textureArman))
        ) {
      return false;
    }

    this.index = index;
    viewRef = null;
    //To current page on 13-06-2021
    //recycleTexture();
    if (view != null) {
      //viewRef = new WeakReference<View>(view);
      //Current page on 13-06-2021
     // recycleScreenshot();
      screenshot = GrabItArman.takeScreenshot(view, format);
    } else {
      recycleScreenshot();
    }

    return true;
  }

  public TextureArman getTextureArman() {
    return textureArman;
  }

  public Bitmap getScreenshot() {
    return screenshot;
  }

  public CardArman getTopCardArman() {
    return topCardArman;
  }

  public CardArman getBottomCardArman() {
    return bottomCardArman;
  }

  public synchronized void buildTexture(FlipRendererArman renderer, GL10 gl) {
    if (screenshot != null) {
      if (textureArman != null) {
        textureArman.destroy(gl);
      }
      textureArman = TextureArman.createTexture(screenshot, renderer, gl);
      //recycleScreenshot();

      topCardArman.setTextureArman(textureArman);
      bottomCardArman.setTextureArman(textureArman);

      final float viewHeight = textureArman.getContentHeight();
      final float viewWidth = textureArman.getContentWidth();
      final float textureHeight = textureArman.getHeight();
      final float textureWidth = textureArman.getWidth();

      if (orientationVertical) {
        topCardArman.setCardVertices(new float[]{0f, viewHeight, 0f, // top left
                                            0f, viewHeight / 2.0f, 0f, // bottom left
                                            viewWidth, viewHeight / 2f, 0f, // bottom right
                                            viewWidth, viewHeight, 0f // top right
        });

        topCardArman.setTextureCoordinates(new float[]{0f, 0f, 0f,
                                                  viewHeight / 2f / textureHeight,
                                                  viewWidth / textureWidth,
                                                  viewHeight / 2f / textureHeight,
                                                  viewWidth / textureWidth, 0f});

        bottomCardArman.setCardVertices(new float[]{0f, viewHeight / 2f,
                                               0f, // top left
                                               0f, 0f, 0f, // bottom left
                                               viewWidth, 0f, 0f, // bottom right
                                               viewWidth, viewHeight / 2f, 0f // top right
        });

        bottomCardArman.setTextureCoordinates(new float[]{0f,
                                                     viewHeight / 2f / textureHeight, 0f,
                                                     viewHeight / textureHeight,
                                                     viewWidth / textureWidth,
                                                     viewHeight / textureHeight,
                                                     viewWidth / textureWidth,
                                                     viewHeight / 2f / textureHeight});
      } else {
        topCardArman.setCardVertices(new float[]{0f, viewHeight, 0f, // top left
                                            0f, 0f, 0f, // bottom left
                                            viewWidth / 2f, 0f, 0f, // bottom right
                                            viewWidth / 2f, viewHeight, 0f // top right
        });

        topCardArman.setTextureCoordinates(new float[]{0f, 0f, 0f,
                                                  viewHeight / textureHeight,
                                                  viewWidth / 2f / textureWidth,
                                                  viewHeight / textureHeight,
                                                  viewWidth / 2f / textureWidth, 0f});

        bottomCardArman.setCardVertices(new float[]{viewWidth / 2f,
                                               viewHeight, 0f, // top left
                                               viewWidth / 2f, 0f, 0f, // bottom left
                                               viewWidth, 0f, 0f, // bottom right
                                               viewWidth, viewHeight, 0f // top right
        });

        bottomCardArman.setTextureCoordinates(new float[]{
            viewWidth / 2f / textureWidth, 0f,
            viewWidth / 2f / textureWidth,
            viewHeight / textureHeight, viewWidth / textureWidth,
            viewHeight / textureHeight, viewWidth / textureWidth,
            0f});
      }

      checkError(gl);
    }
  }

  public synchronized void abandonTexture() {
    textureArman = null;
  }

  @Override
  public String toString() {
    return "ViewDualCards: (" + index + ", view: " + getView() + ")";
  }

  private void recycleScreenshot() {
    UIArman.recycleBitmap(screenshot);
    screenshot = null;
  }

  private void recycleTexture() {
    if (textureArman != null) {
      textureArman.postDestroy();
      textureArman = null;
    }
  }


}
