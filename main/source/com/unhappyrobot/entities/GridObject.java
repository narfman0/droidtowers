package com.unhappyrobot.entities;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Vector2;
import com.unhappyrobot.math.Bounds2d;
import com.unhappyrobot.types.GridObjectType;

public abstract class GridObject {
  protected final GridObjectType gridObjectType;
  protected final GameGrid gameGrid;
  public Vector2 position;
  public Vector2 size;

  public GridObject(GridObjectType gridObjectType, GameGrid gameGrid) {
    this.gridObjectType = gridObjectType;
    this.gameGrid = gameGrid;
    this.position = new Vector2(0, 0);
    this.size = new Vector2(1, 1);
  }

  public boolean canShareSpace() {
    return true;
  }

  public Bounds2d getBounds() {
    return new Bounds2d(position, size);
  }

  public GridObjectType getGridObjectType() {
    return gridObjectType;
  }

  public abstract Sprite getSprite();

  public boolean canBeAt() {
    return gridObjectType.canBeAt(this);
  }

  public GameGrid getGameGrid() {
    return gameGrid;
  }
}
