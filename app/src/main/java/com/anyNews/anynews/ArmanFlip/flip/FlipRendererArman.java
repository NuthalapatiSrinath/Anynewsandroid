package com.anyNews.anynews.ArmanFlip.flip;

import android.opengl.GLSurfaceView;
import android.opengl.GLU;
import android.view.View;


import com.anyNews.anynews.ArmanFlip.utils.AphidLog;
import com.anyNews.anynews.ArmanFlip.utils.TextureUtilsArman;

import java.util.LinkedList;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import static javax.microedition.khronos.opengles.GL10.GL_AMBIENT;
import static javax.microedition.khronos.opengles.GL10.GL_COLOR_BUFFER_BIT;
import static javax.microedition.khronos.opengles.GL10.GL_DEPTH_BUFFER_BIT;
import static javax.microedition.khronos.opengles.GL10.GL_DEPTH_TEST;
import static javax.microedition.khronos.opengles.GL10.GL_LEQUAL;
import static javax.microedition.khronos.opengles.GL10.GL_LIGHT0;
import static javax.microedition.khronos.opengles.GL10.GL_LIGHTING;
import static javax.microedition.khronos.opengles.GL10.GL_MODELVIEW;
import static javax.microedition.khronos.opengles.GL10.GL_NICEST;
import static javax.microedition.khronos.opengles.GL10.GL_PERSPECTIVE_CORRECTION_HINT;
import static javax.microedition.khronos.opengles.GL10.GL_POSITION;
import static javax.microedition.khronos.opengles.GL10.GL_PROJECTION;
import static javax.microedition.khronos.opengles.GL10.GL_SMOOTH;

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
public class FlipRendererArman implements GLSurfaceView.Renderer {

  private FlipViewControllerArman flipViewControllerArman;

  private FlipCardsArman cards;

  private boolean created = false;

  private final LinkedList<TextureArman> postDestroyTextureArmen = new LinkedList<TextureArman>();

  public FlipRendererArman(FlipViewControllerArman flipViewControllerArman, FlipCardsArman cards) {
    this.flipViewControllerArman = flipViewControllerArman;
    this.cards = cards;
  }

  @Override
  public void onSurfaceCreated(GL10 gl, EGLConfig config) {
    gl.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
    gl.glShadeModel(GL_SMOOTH);
    gl.glClearDepthf(1.0f);
    gl.glEnable(GL_DEPTH_TEST);
    gl.glDepthFunc(GL_LEQUAL);
    gl.glHint(GL_PERSPECTIVE_CORRECTION_HINT, GL_NICEST);

    created = true;

    cards.invalidateTexture();
    flipViewControllerArman.sendMessage(FlipViewControllerArman.MSG_SURFACE_CREATED);


  }

  public static float[] light0Position = {0, 0, 100f, 0f};

  @Override
  public void onSurfaceChanged(GL10 gl, int width, int height) {
    gl.glViewport(0, 0, width, height);

    gl.glMatrixMode(GL_PROJECTION);
    gl.glLoadIdentity();

    float fovy = 20f;
    float eyeZ = height / 2f / (float) Math.tan(TextureUtilsArman.d2r(fovy / 2));

    GLU.gluPerspective(gl, fovy, (float) width / (float) height, 0.5f,
                       eyeZ + height / 2); //set zFar be larger than eyeZ to fix issue #5

    gl.glMatrixMode(GL_MODELVIEW);
    gl.glLoadIdentity();

    GLU.gluLookAt(gl,
                  width / 2f, height / 2f, eyeZ,
                  width / 2f, height / 2f, 0.0f,
                  0.0f, 1.0f, 0.0f
    );

    gl.glEnable(GL_LIGHTING);
    gl.glEnable(GL_LIGHT0);

    float lightAmbient[] = new float[]{3.5f, 3.5f, 3.5f, 1f};
    gl.glLightfv(GL_LIGHT0, GL_AMBIENT, lightAmbient, 0);

    light0Position = new float[]{0, 0, eyeZ, 0f};
    gl.glLightfv(GL_LIGHT0, GL_POSITION, light0Position, 0);

    if (AphidLog.ENABLE_DEBUG) {
      AphidLog.d("onSurfaceChanged: %d, %d", width, height);
    }
  }

  @Override
  public void onDrawFrame(GL10 gl) {
    if (cards.isVisible() && cards.isFirstDrawFinished())
      gl.glClearColor(1f, 1f, 1f, 1f);
    else
      gl.glClearColor(0f, 0f, 0f, 0f);
    gl.glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

    synchronized (postDestroyTextureArmen) {
      for (TextureArman textureArman : postDestroyTextureArmen) {
        textureArman.destroy(gl);
      }
      postDestroyTextureArmen.clear();
    }

    cards.draw(this, gl);
  }

  public void postDestroyTexture(TextureArman textureArman) {
    synchronized (postDestroyTextureArmen) {
      postDestroyTextureArmen.add(textureArman);
    }
  }

  public void updateTexture(int frontIndex, View frontView, int backIndex, View backView) {
    if (created) {
      cards.reloadTexture(frontIndex, frontView, backIndex, backView);
      flipViewControllerArman.getSurfaceView().requestRender();
    }
  }

  public static void checkError(GL10 gl) {
    if (AphidLog.ENABLE_DEBUG) {
      int error = gl.glGetError();
      if (error != 0) {
        throw new RuntimeException(GLU.gluErrorString(error));
      }
    }
  }
}
