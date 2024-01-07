package dev.buchstabet.messagebroadcast;

import com.google.gson.Gson;
import com.zaxxer.hikari.HikariDataSource;
import dev.buchstabet.messagebroadcast.inventory.SortInventory;
import dev.buchstabet.messagebroadcast.messages.MessageHolder;
import dev.buchstabet.messagebroadcast.utils.Database;
import dev.buchstabet.messagebroadcast.utils.GsonManager;
import dev.buchstabet.messagebroadcast.utils.itembuilder.ItemBuilderManager;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.Callable;
import java.util.function.Consumer;
import lombok.Getter;
import lombok.SneakyThrows;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.java.JavaPlugin;

public class MessageBroadcast extends JavaPlugin
{

  public static final SimpleDateFormat SIMPLE_DATE_FORMAT = new SimpleDateFormat("dd.MM.yy HH:mm");

  private final Map<Class<?>, Object> instanceHolder = new HashMap<>();
  @Getter private static MessageBroadcast instance;

  @Override
  public void onLoad()
  {
    instance = this;
  }

  @Override
  public void onDisable()
  {

  }

  @Override
  public void onEnable()
  {
    ItemBuilderManager itemBuilderManager = new ItemBuilderManager();
    register(ItemBuilderManager.class, itemBuilderManager);
    getServer().getPluginManager().registerEvents(itemBuilderManager, this);
    getServer().getPluginManager().registerEvents(new SortInventory(), this);

    saveDefaultConfig();

    ConfigurationSection databaseConfig = getConfig().getConfigurationSection("database");
    if (databaseConfig == null) {
      return;
    }

    Database database = new Database(databaseConfig.getString("hostname"),
        databaseConfig.getInt("port"), 2, databaseConfig.getString("database"),
        databaseConfig.getString("username"), databaseConfig.getString("password"));
    HikariDataSource hikariDataSource = database.start();
    register(hikariDataSource);

    MessageHolder.load().whenComplete((holder, throwable) -> {
      if (throwable != null) {
        getServer().getPluginManager().disablePlugin(this);
        throwable.printStackTrace();
        return;
      }

      register(MessageHolder.class, holder);

      MessageBroadcastCommand broadcastCommand = new MessageBroadcastCommand();
      PluginCommand command = getCommand("messagebroadcast");
      command.setExecutor(broadcastCommand);
      command.setTabCompleter(broadcastCommand);

      int tickInterval = getConfig().getInt("tick_interval");
      int updateInterval = getConfig().getInt("update_interval");

      Bukkit.getScheduler().runTaskTimerAsynchronously(this, new Broadcaster(holder), tickInterval, tickInterval);
      Bukkit.getScheduler().runTaskTimerAsynchronously(this, new MessageUpdateTask(holder), updateInterval, updateInterval);
    });

  }

  @SneakyThrows
  public static <T extends Properties> T loadProperties(String path, Class<T> clazz)
  {
    if (path == null) {
      return null;
    }

    URL url = MessageBroadcast.class.getClassLoader().getResource(path);
    if (url == null) {
      return null;
    }

    URLConnection urlConnection = url.openConnection();
    urlConnection.setUseCaches(false);
    T properties = clazz.newInstance();
    try (InputStream in = urlConnection.getInputStream(); InputStreamReader inputStreamReader = new InputStreamReader(
        in, StandardCharsets.UTF_8)) {
      properties.load(inputStreamReader);
    }
    return properties;
  }

  public <T> T get(Class<T> clazz)
  {
    return find(clazz).orElse(null);
  }

  public <T> Optional<T> find(Class<T> clazz)
  {
    Object o = instanceHolder.get(clazz);
    if (clazz.isInstance(o)) {
      return Optional.of(clazz.cast(o));
    }
    return Optional.empty();
  }

  public void register(Class<?> clazz, Object t)
  {
    instanceHolder.putIfAbsent(clazz, t);
  }

  public void register(Object t)
  {
    register(t.getClass(), t);
  }

  public void unregister(Class<?> clazz)
  {
    instanceHolder.remove(clazz);
  }

  public <T> T getAndUnregister(Class<T> clazz)
  {
    return (T) instanceHolder.remove(clazz);
  }

  public <T> Optional<T> findAndUnregister(Class<T> clazz)
  {
    return Optional.ofNullable(getAndUnregister(clazz));
  }

  public <T> void overwrite(Class<T> clazz, T t)
  {
    instanceHolder.put(clazz, t);
  }

  public <T> void ifPresent(Class<T> clazz, Consumer<T> consumer)
  {
    find(clazz).ifPresent(consumer);
  }

  public boolean contains(Class<?> clazz)
  {
    return instanceHolder.containsKey(clazz);
  }

  @SneakyThrows
  public <T> T loadConfig(Class<T> clazz, File file, Callable<T> callable)
  {
    Gson gson = get(GsonManager.class).createBuilder().setPrettyPrinting().create();

    T t;
    if (file.createNewFile()) {
      t = callable.call();
      try (FileWriter fileWriter = new FileWriter(file, StandardCharsets.UTF_8)) {
        fileWriter.write(gson.toJson(t));
        fileWriter.flush();
      }
    } else {
      t = gson.fromJson(
          new String(Files.readAllBytes(Paths.get(file.toURI())), StandardCharsets.UTF_8), clazz);
    }
    if (t == null) {
      t = callable.call();
    }
    overwrite(clazz, t);
    return t;
  }
}
