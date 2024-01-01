package dev.buchstabet.messagebroadcast.messages;

import java.util.UUID;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class Message
{

  private final String content, author, permission;
  private final long createdAt;
  private final MessageType type;

}
