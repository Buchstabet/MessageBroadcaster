package dev.buchstabet.messagebroadcast.messages;

import dev.buchstabet.messagebroadcast.utils.ColorUtil;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@AllArgsConstructor
public class Message
{

  private final UUID uuid;
  @Setter private int sortId;
  @Setter private String content;
  private final String author, permission;
  private final long createdAt;

  public String getStyledContent()
  {
    return ColorUtil.colorize(getContent()).replace("%n", "\n");
  }
}
