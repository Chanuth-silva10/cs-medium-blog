package com.chanuth.mongo.dtos;

import com.chanuth.mongo.models.PlayerPosition;
import com.chanuth.mongo.models.Player;
import java.util.Date;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class CreatePlayerDto {
  private String name;

  private Date birthDate;

  private PlayerPosition position;

  private boolean isAvailable;

  public Player toPlayer() {
    return new Player().setName(name).setBirthDate(birthDate).setPosition(position).setAvailable(isAvailable);
  }
}
