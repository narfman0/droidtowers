/*
 * Copyright (c) 2012. HappyDroids LLC, All rights reserved.
 */

package com.happydroids.droidtowers.gui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.ClickListener;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.tablelayout.Table;
import com.esotericsoftware.tablelayout.Cell;
import com.happydroids.droidtowers.input.InputCallback;
import com.happydroids.droidtowers.input.InputSystem;

import static com.happydroids.droidtowers.platform.Display.scale;

public class TowerWindow {
  private static final int[] DIALOG_CLOSE_KEYCODES = new int[]{InputSystem.Keys.ESCAPE, InputSystem.Keys.BACK};
  private InputCallback closeDialogCallback;
  private Runnable dismissCallback;
  private final String title;
  protected final Stage stage;
  protected final Skin skin;
  private static NinePatch background;
  private static Pixmap pixmap;
  private Texture texture;
  private final Table content;
  private Table window;

  public TowerWindow(String title, Stage stage, Skin skin) {
    if (pixmap == null) {
      pixmap = new Pixmap(16, 16, Pixmap.Format.RGB888);


      pixmap.setColor(makeRGB(23, 22, 23));
      pixmap.fill();

      pixmap.setColor(makeRGB(36, 33, 38));
      pixmap.drawPixel(0, 0);
      pixmap.drawPixel(1, 0);

      pixmap.setColor(makeRGB(49, 45, 52));
      pixmap.drawPixel(0, 12);
      pixmap.drawPixel(1, 12);

      pixmap.setColor(makeRGB(57, 67, 70));
      pixmap.drawPixel(0, 14);
      pixmap.drawPixel(1, 14);

      texture = new Texture(pixmap, false);
      texture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
      background = new NinePatch(texture);
    }

    this.title = title;
    this.stage = stage;
    this.skin = skin;


    window = new Table();
    window.touchable = true;
    window.setClickListener(new ClickListener() {
      public void click(Actor actor, float x, float y) {

      }
    });

    window.setBackground(background);
    window.size((int) stage.width(), (int) stage.height());

    window.add(FontManager.Roboto32.makeLabel(title)).center().left().expand().padLeft(scale(18));
    window.row();
    window.add(new HorizontalRule(scale(2))).expandX();
    window.row().fill().height((int) (stage.height() - scale(46))).padLeft(scale(24)).padRight(scale(24));

    content = new Table();
    content.row().expandX();
    window.add(content).fill();
  }

  private Color makeRGB(int r, int g, int b) {
    return new Color(r / 255f, g / 255f, b / 255f, 1f);
  }

  public Cell add(Actor actor) {
    return content.add(actor);
  }

  public Cell row() {
    return content.row();
  }

  public TowerWindow show() {
    closeDialogCallback = new InputCallback() {
      public boolean run(float timeDelta) {
        TowerWindow.this.dismiss();
        return true;
      }
    };

    InputSystem.instance().bind(DIALOG_CLOSE_KEYCODES, closeDialogCallback);
    window.invalidate();
    window.pack();
    stage.addActor(window);

    return this;
  }

  public void dismiss() {
    if (closeDialogCallback != null) {
      InputSystem.instance().unbind(DIALOG_CLOSE_KEYCODES, closeDialogCallback);
      closeDialogCallback = null;
    }

    if (dismissCallback != null) {
      dismissCallback.run();
    }

    stage.setScrollFocus(null);
    stage.setKeyboardFocus(null);

    window.markToRemove(true);
  }

  public void setDismissCallback(Runnable dismissCallback) {
    this.dismissCallback = dismissCallback;
  }

  protected void debug() {
    content.debug();
  }

  protected void clear() {
    content.clear();
  }
}
