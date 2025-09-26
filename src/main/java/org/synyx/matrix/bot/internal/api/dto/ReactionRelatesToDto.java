package org.synyx.matrix.bot.internal.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReactionRelatesToDto {

  @JsonProperty("event_id")
  private String eventId;

  @JsonProperty("key")
  private String key;

  @JsonProperty("rel_type")
  public String getRelType() {

    return "m.annotation";
  }
}
