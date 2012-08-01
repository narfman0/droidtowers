/*
 * Copyright (c) 2012. HappyDroids LLC, All rights reserved.
 */

package com.happydroids.droidtowers.gui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.ui.tablelayout.Table;
import com.badlogic.gdx.utils.Scaling;
import com.google.common.collect.Lists;
import com.happydroids.droidtowers.entities.Janitor;
import com.happydroids.droidtowers.entities.Maid;
import com.happydroids.droidtowers.entities.SecurityGuard;
import com.happydroids.droidtowers.types.CommercialType;
import com.happydroids.droidtowers.types.GridObjectType;
import com.happydroids.droidtowers.types.RoomType;
import com.happydroids.droidtowers.utils.StringUtils;

import java.util.List;

import static com.happydroids.droidtowers.platform.Display.scale;
import static com.happydroids.droidtowers.types.ProviderType.COMMERCIAL;
import static com.happydroids.droidtowers.types.ProviderType.HOUSING;

class GridObjectPurchaseItem extends Table {
  private final TextButton buyButton;
  private final GridObjectType gridObjectType;

  public GridObjectPurchaseItem(final GridObjectType gridObjectType) {
    this.gridObjectType = gridObjectType;

    Image gridObjectImage = new Image(gridObjectType.getTextureRegion(0), Scaling.fit, Align.LEFT | Align.TOP);
    Label nameLabel = FontManager.RobotoBold18.makeLabel(gridObjectType.getName());
    Label priceLabel = FontManager.RobotoBold18.makeLabel(StringUtils.currencyFormat(gridObjectType.getCoins()), Color.WHITE, Align.RIGHT);
    buyButton = FontManager.RobotoBold18.makeTextButton(gridObjectType.isLocked() ? "Info" : "Buy");

    defaults().top().left().space(scale(8));

    Table left = newTable();
    left.width(scale(200));
    left.row().fillX();
    left.add(nameLabel).expandX();
    left.row().fillX();
    left.add(gridObjectImage).height(scale(40)).expand();


    Table center = newTable();
    center.row().fillX();
    if (gridObjectType.hasDescription()) {
      Label label = FontManager.Roboto18.makeLabel(gridObjectType.getDescription());
      label.setWrap(true);
      center.add(label).expandX();
    }

    if (gridObjectType.hasStatsLine()) {
      center.row().fill();
      center.add(makeGridObjectInfo()).expand().bottom();
    }

    Table right = newTable();
    right.row().width(scale(100));
    right.add(priceLabel).right().width(scale(100));
    right.row().fillX();
    right.add(buyButton).right();

    row().fill();
    add(left).width(scale(200));
    add(center).expand();
    add(right);
  }

  private Actor makeGridObjectInfo() {
    Label descriptionLabel = FontManager.Default.makeLabel("");
    String description = gridObjectType.getStatsLine();

    int maxIncome = 0;
    if (gridObjectType.provides(HOUSING)) {
      maxIncome = ((RoomType) gridObjectType).getPopulationMax() * gridObjectType.getCoinsEarned();
      description = description.replace("{maxResidents}", "" + ((RoomType) gridObjectType).getPopulationMax());
    } else if (gridObjectType.provides(COMMERCIAL)) {
      maxIncome = ((RoomType) gridObjectType).getPopulationMax() * gridObjectType.getCoinsEarned();
      description = description.replace("{maxEmployees}", "" + ((CommercialType) gridObjectType).getJobsProvided());
    }

    description = description.replace("{maxIncome}", StringUtils.currencyFormat(maxIncome));

    if (description.contains("{servicedBy}")) {
      List<String> servicedBy = Lists.newArrayList();
      if (gridObjectType.provides(Janitor.JANITOR_SERVICES_PROVIDER_TYPES)) {
        servicedBy.add("Janitors");
      }

      if (gridObjectType.provides(Maid.MAID_SERVICES_PROVIDER_TYPES)) {
        servicedBy.add("Maids");
      }

      if (gridObjectType.provides(SecurityGuard.SECURITY_GUARD_SERVICE_PROVIDER_TYPES)) {
        servicedBy.add("Security Guards");
      }

      description = description.replace("{servicedBy}", StringUtils.join(servicedBy, ", "));
    }

    descriptionLabel.setText(description);
    descriptionLabel.setWrap(true);

//    c.debug();
    return descriptionLabel;
  }

  public void setBuyClickListener(ClickListener clickListener) {
    if (gridObjectType.isLocked()) {
      buyButton.setClickListener(new GridObjectTypeLockedClickListener(gridObjectType));
    } else {
      buyButton.setClickListener(clickListener);
    }
  }
}
