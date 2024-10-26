package com.chanuth.mongodb.dtos;

import com.chanuth.mongodb.models.PlayerPosition;
import com.chanuth.mongodb.models.Player;
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
