package dev.buchstabet.messagebroadcast;

import dev.buchstabet.messagebroadcast.messages.MessageHolder;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;

@RequiredArgsConstructor
public class Broadcaster implements Runnable
{

  private int id = 0;
  private final MessageHolder messages;

  @Override
  public void run()
  {
    if (messages.isEmpty())
      return;

    if (messages.size() <= id)
      id = 0;

    messages.findById(id).ifPresent(
        message -> Bukkit.broadcast(message.getStyledContent(), message.getPermission()));
    id++;
  }
}
