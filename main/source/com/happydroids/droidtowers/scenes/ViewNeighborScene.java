/*
 * Copyright (c) 2012. HappyDroids LLC, All rights reserved.
 */

package com.happydroids.droidtowers.scenes;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.input.GestureDetector;
import com.badlogic.gdx.math.Vector2;
import com.google.common.collect.Lists;
import com.happydroids.droidtowers.TowerConsts;
import com.happydroids.droidtowers.TowerGame;
import com.happydroids.droidtowers.WeatherService;
import com.happydroids.droidtowers.entities.CloudLayer;
import com.happydroids.droidtowers.entities.GameLayer;
import com.happydroids.droidtowers.events.RespondsToWorldSizeChange;
import com.happydroids.droidtowers.gamestate.GameSave;
import com.happydroids.droidtowers.gamestate.server.FriendCloudGameSave;
import com.happydroids.droidtowers.graphics.CityScapeLayer;
import com.happydroids.droidtowers.graphics.GroundLayer;
import com.happydroids.droidtowers.graphics.RainLayer;
import com.happydroids.droidtowers.graphics.SkyLayer;
import com.happydroids.droidtowers.grid.NeighborGameGrid;
import com.happydroids.droidtowers.gui.ViewNeighborHUD;
import com.happydroids.droidtowers.input.GestureDelegater;
import com.happydroids.droidtowers.input.GestureTool;
import com.happydroids.droidtowers.input.InputCallback;
import com.happydroids.droidtowers.input.InputSystem;
import com.happydroids.droidtowers.math.GridPoint;
import com.happydroids.droidtowers.types.TowerNameBillboard;
import com.happydroids.server.HappyDroidServiceCollection;

import java.util.List;

public class ViewNeighborScene extends Scene {
  private List<GameLayer> gameLayers;
  private GestureDelegater gestureDelegater;
  private GestureDetector gestureDetector;
  private ViewNeighborHUD neighborHUD;
  private GameLayer billboardLayer;

  @Override
  public void create(Object... args) {
    neighborHUD = new ViewNeighborHUD();
    neighborHUD.x = 0;
    neighborHUD.y = Gdx.graphics.getHeight();

    getStage().addActor(neighborHUD);

    InputSystem.instance().bind(TowerConsts.NEGATIVE_BUTTON_KEYS, goBackHomeCallback);

    WeatherService weatherService = new WeatherService();

    gameLayers = Lists.newArrayList();
    gameLayers.add(new SkyLayer(weatherService));
    gameLayers.add(new CityScapeLayer());
    gameLayers.add(new CloudLayer(weatherService));
    gameLayers.add(new RainLayer(weatherService));
    gameLayers.add(new GroundLayer());

    billboardLayer = new GameLayer();

    gestureDelegater = new GestureDelegater(camera, gameLayers, null, getCameraController());
    gestureDetector = new GestureDetector(20, 0.5f, 2, 0.15f, gestureDelegater);
    InputSystem.instance().addInputProcessor(gestureDetector, 100);
    InputSystem.instance().setGestureDelegator(gestureDelegater);
    InputSystem.instance().switchTool(GestureTool.PICKER, null);

    createNeighborTowers((HappyDroidServiceCollection<FriendCloudGameSave>) args[0]);
  }

  private void createNeighborTowers(HappyDroidServiceCollection<FriendCloudGameSave> collection) {
    Vector2 worldSize = new Vector2();
    int gridX = 0;
    for (final FriendCloudGameSave friendCloudGameSave : collection.getObjects()) {
      NeighborGameGrid neighborGameGrid = new NeighborGameGrid(getCamera(), new GridPoint(gridX, 0));
      neighborGameGrid.setGridScale(1f);
      GameSave gameSave = friendCloudGameSave.getGameSave();

      if (!gameSave.hasGridObjects()) {
        System.out.println("Skipping, no objects! " + friendCloudGameSave);
        continue;
      }

      gameSave.attachToGame(neighborGameGrid, camera, cameraController);
      neighborGameGrid.findLimits();
      gridX += (neighborGameGrid.getGridSize().x + 6) * TowerConsts.GRID_UNIT_SIZE;

      neighborGameGrid.setOwnerName(friendCloudGameSave.getOwner().getFirstName());
      neighborGameGrid.setClickListener(new Runnable() {
        public void run() {
          neighborHUD.showToast("HELLO FROM " + friendCloudGameSave.getOwner().getFirstName());
        }
      });


      TowerNameBillboard billboard = new TowerNameBillboard(neighborGameGrid);
      billboard.setPosition(neighborGameGrid.getWorldBounds().x - (2 * TowerConsts.GRID_UNIT_SIZE), TowerConsts.GROUND_HEIGHT);
      billboardLayer.addChild(billboard);

      worldSize.y = Math.max(worldSize.y, neighborGameGrid.getWorldSize().y);
      worldSize.x = neighborGameGrid.getWorldBounds().x + neighborGameGrid.getWorldBounds().width;

      gameLayers.add(neighborGameGrid.getRenderer());
      gameLayers.add(neighborGameGrid);
    }

    gameLayers.add(billboardLayer);

    for (GameLayer gameLayer : gameLayers) {
      if (gameLayer instanceof RespondsToWorldSizeChange) {
        ((RespondsToWorldSizeChange) gameLayer).updateWorldSize(worldSize);
      }
    }

    cameraController.updateCameraConstraints(worldSize);
    camera.position.set(worldSize.x / 2 - Gdx.graphics.getWidth() / 2, TowerConsts.GROUND_HEIGHT, 0f);
  }

  @Override
  public void pause() {
    InputSystem.instance().removeInputProcessor(gestureDetector);
    InputSystem.instance().setGestureDelegator(null);
  }

  @Override
  public void resume() {
    InputSystem.instance().addInputProcessor(gestureDetector, 100);
    InputSystem.instance().setGestureDelegator(gestureDelegater);
    InputSystem.instance().switchTool(GestureTool.PICKER, null);
  }

  @Override
  public void render(float deltaTime) {
    for (GameLayer gameLayer : gameLayers) {
      gameLayer.update(deltaTime);
      gameLayer.render(getSpriteBatch());
    }
  }

  @Override
  public void dispose() {
    InputSystem.instance().unbind(TowerConsts.NEGATIVE_BUTTON_KEYS, goBackHomeCallback);
  }

  private InputCallback goBackHomeCallback = new InputCallback() {
    @Override
    public boolean run(float timeDelta) {
      TowerGame.popScene();
      return true;
    }
  };
}