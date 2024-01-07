package dev.buchstabet.messagebroadcast;

import dev.buchstabet.messagebroadcast.messages.MessageHolder;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class MessageUpdateTask implements Runnable
{

  private final MessageHolder messageHolder;

  @Override
  public void run()
  {
    MessageHolder.load().whenComplete((holder, throwable) -> {
      if (throwable == null) {
        throwable.printStackTrace();
        return;
      }

      messageHolder.clear();
      messageHolder.addAll(holder);
    });
  }

}
