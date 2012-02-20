package com.unhappyrobot.gui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.ui.tablelayout.Table;
import com.unhappyrobot.GridPosition;
import com.unhappyrobot.GridPositionCache;
import com.unhappyrobot.Overlays;
import com.unhappyrobot.TowerGame;
import com.unhappyrobot.entities.GameGrid;
import com.unhappyrobot.input.GestureTool;
import com.unhappyrobot.input.InputCallback;
import com.unhappyrobot.input.InputSystem;
import com.unhappyrobot.math.GridPoint;
import com.unhappyrobot.types.CommercialTypeFactory;
import com.unhappyrobot.types.GridObjectTypeFactory;
import com.unhappyrobot.types.RoomTypeFactory;
import com.unhappyrobot.types.TransitTypeFactory;

public class HeadsUpDisplay extends WidgetGroup {
  public static final float ONE_MEGABYTE = 1048576.0f;
  private TextureAtlas hudAtlas;
  private Skin guiSkin;
  private OrthographicCamera camera;
  private GameGrid gameGrid;
  private LabelButton addRoomButton;
  private Menu addRoomMenu;
  private Label statusLabel;
  private float updateMoneyLabel;
  private static HeadsUpDisplay instance;
  private SpriteBatch spriteBatch;
  private BitmapFont defaultBitmapFont;
  private BitmapFont menloBitmapFont;
  private Toast toast;
  private LabelButton setOverlayButton;
  private Menu overlayMenu;
  private Table topBar;
  private ToolTip mouseToolTip;
  private final TextureAtlas radialMenuAtlas = new TextureAtlas(Gdx.files.internal("hud/test.txt"));
  private GridObjectPurchaseMenu purchaseDialog;
  private InputCallback closeDialogCallback = null;

  public static HeadsUpDisplay getInstance() {
    if (instance == null) {
      instance = new HeadsUpDisplay();
    }

    return instance;
  }

  public void initialize(final OrthographicCamera camera, final GameGrid gameGrid, final Stage stage, SpriteBatch spriteBatch) {
    this.stage = stage;
    this.spriteBatch = spriteBatch;
    this.camera = camera;
    this.gameGrid = gameGrid;
    this.guiSkin = new Skin(Gdx.files.internal("default-skin.ui"), Gdx.files.internal("default-skin.png"));

    ModalOverlay.initialize(this);

    menloBitmapFont = new BitmapFont(Gdx.files.internal("fonts/menlo_16.fnt"), false);
    defaultBitmapFont = new BitmapFont(Gdx.files.internal("default.fnt"), false);

    hudAtlas = new TextureAtlas(Gdx.files.internal("hud/buttons.txt"));

    StatusBarPanel statusBarPanel = new StatusBarPanel(guiSkin);
    statusBarPanel.x = 0;
    statusBarPanel.y = stage.height() - statusBarPanel.height;
    addActor(statusBarPanel);

    makeOverlayButton();

    mouseToolTip = new ToolTip();
    addActor(mouseToolTip);


    addActorAt(0, ModalOverlay.instance());


    final ImageButton toolButton = new ImageButton(hudAtlas.findRegion("tool-sprite"));
    toolButton.x = stage.width() - toolButton.width - 5;
    toolButton.y = 5;

    final RadialMenu toolMenu = new RadialMenu();
    toolMenu.arc = 30f;
    toolMenu.radius = 120f;

    ImageButton housingButton = new ImageButton(hudAtlas.findRegion("tool-housing"));
    housingButton.setClickListener(new ClickListener() {
      public void click(Actor actor, float x, float y) {
        toolMenu.hide();

        if (purchaseDialog == null) {
          makePurchaseDialog("Rooms", RoomTypeFactory.getInstance());
        } else {
          purchaseDialog.dismiss();
          purchaseDialog = null;
        }
      }
    });
    toolMenu.addActor(housingButton);

    ImageButton transitButton = new ImageButton(hudAtlas.findRegion("tool-transit"));
    transitButton.setClickListener(new ClickListener() {
      public void click(Actor actor, float x, float y) {
        toolMenu.hide();

        if (purchaseDialog == null) {
          makePurchaseDialog("Transit", TransitTypeFactory.getInstance());
        } else {
          purchaseDialog.dismiss();
          purchaseDialog = null;
        }
      }
    });
    toolMenu.addActor(transitButton);

    ImageButton commerceButton = new ImageButton(hudAtlas.findRegion("tool-commerce"));
    commerceButton.setClickListener(new ClickListener() {
      public void click(Actor actor, float x, float y) {
        toolMenu.hide();

        if (purchaseDialog == null) {
          makePurchaseDialog("Commerce", CommercialTypeFactory.getInstance());
        } else {
          purchaseDialog.dismiss();
          purchaseDialog = null;
        }
      }
    });
    toolMenu.addActor(commerceButton);

    toolButton.setClickListener(new ClickListener() {
      public void click(Actor actor, float x, float y) {
        if (InputSystem.getInstance().getCurrentTool() != GestureTool.PLACEMENT) {
          if (purchaseDialog != null) {
            purchaseDialog.dismiss();
            purchaseDialog = null;
          }
          InputSystem.getInstance().switchTool(GestureTool.PICKER, null);
        }

        if (!toolMenu.visible) {
          toolButton.add(toolMenu);
          toolMenu.x = -200f;
          toolMenu.y = -100f;
          toolMenu.show();
        } else {
          toolMenu.hide();
        }
      }
    });

    addActor(toolButton);

    AudioControl audioControl = new AudioControl(hudAtlas);
    addActor(audioControl);

    stage.addActor(this);
  }

  private void makePurchaseDialog(String title, GridObjectTypeFactory typeFactory) {
    purchaseDialog = new GridObjectPurchaseMenu(title, typeFactory);

    purchaseDialog.setDismissCallback(new Runnable() {
      public void run() {
        purchaseDialog = null;
      }
    });

    stage.addActor(purchaseDialog);

    purchaseDialog.centerOnStage().modal(true).show();
  }

  private void makeOverlayButton() {
    setOverlayButton = new LabelButton(guiSkin, "Overlays");
    setOverlayButton.setClickListener(new ClickListener() {
      boolean isShowing;

      public void click(Actor actor, float x, float y) {
        overlayMenu.show(setOverlayButton);
      }
    });

    overlayMenu = new Menu(guiSkin);
    overlayMenu.defaults();
    overlayMenu.top().left();

    for (final Overlays overlay : Overlays.values()) {
      final CheckBox checkBox = new CheckBox(overlay.toString(), guiSkin);
      checkBox.align(Align.LEFT);
      checkBox.getLabelCell().pad(4);
      checkBox.invalidate();
      checkBox.setClickListener(new ClickListener() {
        public void click(Actor actor, float x, float y) {
          if (checkBox.isChecked()) {
            TowerGame.getGameGridRenderer().addActiveOverlay(overlay);
          } else {
            TowerGame.getGameGridRenderer().removeActiveOverlay(overlay);
          }
        }
      });
      overlayMenu.row().left().pad(2, 6, 2, 6);
      overlayMenu.add(checkBox);
      Pixmap pixmap = new Pixmap(16, 16, Pixmap.Format.RGB565);
      pixmap.setColor(Color.GRAY);
      pixmap.fill();
      pixmap.setColor(overlay.getColor(1f));
      pixmap.fillRectangle(1, 1, 14, 14);

      Image image = new Image(new Texture(pixmap));
      overlayMenu.add(image);
    }

    overlayMenu.row().colspan(2).left().pad(6, 2, 2, 2);
    LabelButton clearAllButton = new LabelButton(guiSkin, "Clear All");
    clearAllButton.setClickListener(new ClickListener() {
      public void click(Actor actor, float x, float y) {
        TowerGame.getGameGridRenderer().clearOverlays();

        for (Actor child : overlayMenu.getActors()) {
          if (child instanceof CheckBox) {
            ((CheckBox) child).setChecked(false);
          }
        }
      }
    });

    overlayMenu.add(clearAllButton).fill();

    overlayMenu.pack();
  }

  public Skin getGuiSkin() {
    return guiSkin;
  }

  @Override
  public void draw(SpriteBatch batch, float parentAlpha) {
    super.draw(batch, parentAlpha);

    float javaHeapInBytes = Gdx.app.getJavaHeap() / ONE_MEGABYTE;
    float nativeHeapInBytes = Gdx.app.getNativeHeap() / ONE_MEGABYTE;

    String infoText = String.format("fps: %02d, camera(%.1f, %.1f, %.1f)\nmem: (java %.1f Mb, native %.1f Mb)", Gdx.graphics.getFramesPerSecond(), camera.position.x, camera.position.y, camera.zoom, javaHeapInBytes, nativeHeapInBytes);
    menloBitmapFont.drawMultiLine(batch, infoText, 5, 45);
  }

  @Override
  public boolean touchMoved(float x, float y) {
    Actor hit = hit(x, y);
    if (hit == null) {
      updateGridPointTooltip(x, y);
    } else {
      mouseToolTip.visible = false;
    }

    return super.touchMoved(x, y);
  }

  private void updateGridPointTooltip(float x, float y) {
    Vector3 worldPoint = camera.getPickRay(Gdx.input.getX(), Gdx.input.getY()).getEndPoint(1);

    GridPoint gridPoint = gameGrid.closestGridPoint(worldPoint.x, worldPoint.y);
    GridPosition gridPosition = GridPositionCache.instance().getPosition(gridPoint);
    if (gridPosition != null) {
      mouseToolTip.visible = true;
      mouseToolTip.setText(String.format("%s\nobjects: %s\nelevator: %s\nstairs: %s", gridPoint, gridPosition.size(), gridPosition.elevator != null, gridPosition.stair != null));
      mouseToolTip.x = x + 5;
      mouseToolTip.y = y + 5;
    } else {
      mouseToolTip.visible = false;
    }
  }

  public void showToast(String message) {
    if (toast == null) {
      toast = new Toast();
      addActor(toast);
    }

    toast.setMessage(message);
    toast.show();
  }

  public float getPrefWidth() {
    return 0;
  }

  public float getPrefHeight() {
    return 0;
  }
}
