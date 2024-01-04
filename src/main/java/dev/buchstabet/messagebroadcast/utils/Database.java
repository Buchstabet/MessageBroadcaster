package dev.buchstabet.messagebroadcast.utils;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public final class Database
{
  private final String hostname;
  private final int port, poolSize;
  private final String database;
  private final String username;
  private final String password;

  @Override
  public String toString()
  {
    return "Database{" +
        "hostname='" + hostname + '\'' +
        ", port=" + port +
        ", poolSize=" + poolSize +
        ", database='" + database + '\'' +
        ", username='" + username + '\'' +
        ", password='" + password + '\'' +
        '}';
  }

  public HikariDataSource start()
  {
    HikariConfig hikariConfig = new HikariConfig();
    
    hikariConfig.setJdbcUrl("jdbc:mysql://" + hostname + ":" + port + "/" + database +
                            "?useSSL=false&verifyServerCertificate=false&allowPublicKeyRetrieval=true&serverTimezone=Europe/Berlin");
    hikariConfig.setDriverClassName("com.mysql.cj.jdbc.Driver");
    hikariConfig.setUsername(username);
    hikariConfig.setPassword(password);
    hikariConfig.setConnectionTimeout(3000);
    hikariConfig.setMaximumPoolSize(poolSize);
    return new HikariDataSource(hikariConfig);
  }
}
