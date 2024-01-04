package dev.buchstabet.messagebroadcast.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class GsonManager
{

  private final List<Consumer<GsonBuilder>> consumers = new ArrayList<>();

  public void registerTypeAdapter(Consumer<GsonBuilder> consumer)
  {
    consumers.add(consumer);
  }

  public Gson getGson()
  {
    return createBuilder().create();
  }

  public GsonBuilder createBuilder()
  {
    GsonBuilder gsonBuilder = new GsonBuilder();
    consumers.forEach(consumer -> consumer.accept(gsonBuilder));
    return gsonBuilder;
  }

}
