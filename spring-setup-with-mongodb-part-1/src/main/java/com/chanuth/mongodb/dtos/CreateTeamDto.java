package com.chanuth.mongodb.dtos;

import com.chanuth.mongodb.models.Team;
import com.chanuth.mongodb.models.Address;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class CreateTeamDto {
  private String name;

  private String acronym;

  private Address address;

  public Team toTeam() {
    return new Team().setName(name).setAcronym(acronym).setAddress(address);
  }
}
