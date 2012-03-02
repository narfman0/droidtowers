package com.unhappyrobot.entities;

import aurelienribon.tweenengine.BaseTween;
import aurelienribon.tweenengine.Tween;
import aurelienribon.tweenengine.TweenCallback;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.Vector2;
import com.google.common.eventbus.Subscribe;
import com.unhappyrobot.TowerConsts;
import com.unhappyrobot.WeatherService;
import com.unhappyrobot.events.GameGridResizeEvent;
import com.unhappyrobot.events.WeatherStateChangeEvent;
import com.unhappyrobot.grid.GameGrid;
import com.unhappyrobot.tween.GameObjectAccessor;
import com.unhappyrobot.tween.TweenSystem;
import com.unhappyrobot.utils.Random;

import java.util.ArrayList;
import java.util.List;

import static com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;

public class CloudLayer extends GameLayer {
  public static final int PADDING = 20;
  public static final int CLOUD_SPAWN_DELAY = 2;
  public static final double CLOUD_SPAWN_MIN = 0.6;
  public static final double CLOUD_SPAWN_MAX = 0.98;
  public static final int MAX_ACTIVE_CLOUDS = 40;
  private final TextureAtlas textureAtlas;
  private float timeSinceSpawn;
  private Vector2 worldSize;
  private List<GameObject> cloudsToRemove;
  private final int numberOfCloudTypes;

  public CloudLayer() {
    super();

    textureAtlas = new TextureAtlas(Gdx.files.internal("backgrounds/clouds.txt"));
    numberOfCloudTypes = textureAtlas.getRegions().size();
    cloudsToRemove = new ArrayList<GameObject>(5);

    GameGrid.events().register(this);
    WeatherService.events().register(this);
  }

  @Override
  public void update(float timeDelta) {
    super.update(timeDelta);

    removeDeadClouds();
    spawnCloudWhenItsTime(timeDelta);
  }

  private void spawnCloudWhenItsTime(float timeDelta) {
    timeSinceSpawn += timeDelta;

    if (timeSinceSpawn > CLOUD_SPAWN_DELAY && gameObjects.size() < MAX_ACTIVE_CLOUDS) {
      timeSinceSpawn = 0;

      spawnCloudNow();
    }
  }

  private void spawnCloudNow() {
    AtlasRegion cloudRegion = textureAtlas.findRegion("cloud", Random.randomInt(1, numberOfCloudTypes));

    float scale = Math.max(0.4f, Random.randomFloat());
    float cloudX = (cloudRegion.getRegionWidth() * scale) + PADDING;

    GameObject cloud = new GameObject(cloudRegion);

    switch (WeatherService.instance().currentState()) {
      case RAINING:
        cloud.setColor(Color.DARK_GRAY);
        break;

      case SUNNY:
        cloud.setColor(Color.WHITE);
        break;
    }

    cloud.setPosition(-cloudX, Random.randomInt(worldSize.y * CLOUD_SPAWN_MIN, worldSize.y * CLOUD_SPAWN_MAX));
    cloud.setVelocity(Random.randomInt(5, 25), 0);

    if (cloud.getX() > 0) {
      cloud.setOpacity(0);
      Tween.to(cloud, GameObjectAccessor.OPACITY, 2000).target(1.0f).start(TweenSystem.getTweenManager());
    }

    addChild(cloud);
  }

  private void removeDeadClouds() {
    if (cloudsToRemove.size() > 0) {
      gameObjects.removeAll(cloudsToRemove);
      cloudsToRemove.clear();
    }

    for (final GameObject cloud : gameObjects) {
      if (cloud.getX() >= worldSize.x + PADDING) {
        Tween.to(cloud, GameObjectAccessor.OPACITY, 2000).target(0f).addCallback(TweenCallback.EventType.COMPLETE, new TweenCallback() {
          public void onEvent(EventType eventType, BaseTween source) {
            markForRemoval(cloud);
          }
        }).start(TweenSystem.getTweenManager());
      }
    }
  }

  private void markForRemoval(GameObject cloud) {
    cloudsToRemove.add(cloud);
  }

  @Subscribe
  public void GameGrid_onResize(GameGridResizeEvent event) {
    worldSize = event.gameGrid.getWorldSize();

    removeAllChildren();
    cloudsToRemove.clear();

    for (int i = 0; i < MAX_ACTIVE_CLOUDS; i++) {
      spawnCloudNow();
    }

    for (GameObject cloud : gameObjects) {
      cloud.setX(Random.randomInt(0, worldSize.x));
    }
  }

  @Subscribe
  public void WeatherService_onWeatherChange(WeatherStateChangeEvent event) {
    Color cloudColor = null;

    switch (WeatherService.instance().currentState()) {
      case RAINING:
        cloudColor = Color.DARK_GRAY;
        break;

      case SUNNY:
        cloudColor = Color.WHITE;
        break;
    }

    if (cloudColor != null) {
      for (GameObject cloud : gameObjects) {
        Tween.to(cloud, GameObjectAccessor.COLOR, TowerConsts.WEATHER_SERVICE_STATE_CHANGE_DURATION)
                .target(cloudColor.r, cloudColor.g, cloudColor.b, cloudColor.a)
                .start(TweenSystem.getTweenManager());
      }
    }
  }
}
