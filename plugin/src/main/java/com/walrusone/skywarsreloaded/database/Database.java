package com.walrusone.skywarsreloaded.database;

import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import com.walrusone.skywarsreloaded.SkyWarsReloaded;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

public class Database {
	
	    private final String connectionUri;
	    private final String username;
	    private final String password;
	    private HikariDataSource dataSource;

	    public Database() throws ClassNotFoundException, SQLException {
	        final String hostname = SkyWarsReloaded.get().getConfig().getString("sqldatabase.hostname");
	        final int port = SkyWarsReloaded.get().getConfig().getInt("sqldatabase.port");
	        final String database = SkyWarsReloaded.get().getConfig().getString("sqldatabase.database");

	        if (hostname == null || hostname.isEmpty() || database == null || database.isEmpty() || port == 0) {
	            SkyWarsReloaded.get().getLogger().warning("sqldatabase config missing, check hostname/port/database");
	            throw new SQLException("sqldatabase config missing");
	        }

			connectionUri = String.format("jdbc:mysql://%s:%d/%s?useUnicode=true&characterEncoding=UTF-8&serverTimezone=UTC&useSSL=false&allowPublicKeyRetrieval=true&connectTimeout=5000&socketTimeout=10000", hostname, port, database);
	        username = SkyWarsReloaded.get().getConfig().getString("sqldatabase.username");
	        password = SkyWarsReloaded.get().getConfig().getString("sqldatabase.password");

	        try {
	            Class.forName("com.mysql.cj.jdbc.Driver");
	            HikariConfig config = new HikariConfig();
	            config.setJdbcUrl(connectionUri);
	            config.setUsername(username);
	            config.setPassword(password);
	            config.setDriverClassName("com.mysql.cj.jdbc.Driver");
	            config.setMaximumPoolSize(16);
	            config.setMinimumIdle(5);
	            config.setConnectionTimeout(5000);
	            config.setMaxLifetime(1800000);
	            dataSource = new HikariDataSource(config);
	            checkConnection();

	        } catch (RuntimeException runtimeException) {
	            close();
	            throw runtimeException;
	        }
	    }

	    Connection getConnection() throws SQLException {
	        HikariDataSource ds = dataSource;
	        if (ds == null || ds.isClosed()) {
	            throw new SQLException("pool closed");
	        }
	        return ds.getConnection();
	    }

	    public void close() {
	        HikariDataSource ds = dataSource;
	        dataSource = null;
	        if (ds != null) {
	            try {
	                ds.close();
	            } catch (Exception ignored) {
	            }
	        }
	    }

	    boolean checkConnection() {
	        if (dataSource == null || dataSource.isClosed()) {
	            SkyWarsReloaded.get().getLogger().warning("Database connection failed, sqldatabase config may be invalid: pool closed");
	            return true;
	        }
	        try (Connection connection = dataSource.getConnection()) {
	            if (connection.isValid(2)) {
	                return false;
	            }
	        } catch (SQLException sqlException) {
	            sqlException.printStackTrace();
	            SkyWarsReloaded.get().getLogger().warning("Database connection failed, sqldatabase config may be invalid: " + sqlException.getMessage());
	            return true;
	        }
	        SkyWarsReloaded.get().getLogger().warning("Database connection failed, sqldatabase config may be invalid: connection invalid");
	        return true;
	    }

	    @SuppressWarnings("UnstableApiUsage")
		public void createTables() throws IOException, SQLException {
	        URL resource = Resources.getResource(SkyWarsReloaded.class, "/tables.sql");
	        String[] databaseStructure = Resources.toString(resource, Charsets.UTF_8).split(";");

	        if (databaseStructure.length == 0) {
	            return;
	        }

	        Statement statement = null;
	        Connection connection = null;

	        try {
	            connection = dataSource.getConnection();
	            connection.setAutoCommit(false);
	            statement = connection.createStatement();

	            for (String query : databaseStructure) {
	                query = query.trim();

	                if (query.isEmpty()) {
	                    continue;
	                }

	                statement.execute(query);
	            }

	            connection.commit();

	        } finally {
	            if (connection != null) {
	                try {
	                    connection.setAutoCommit(true);
	                } catch (SQLException ignored) {
	                }
	            }

	            if (statement != null && !statement.isClosed()) {
	                statement.close();
	            }

	            if (connection != null) {
	                connection.close();
	            }
	        }
	    }
	    
	    boolean doesPlayerExist(String fId) throws SQLException {
	        if (checkConnection()) {
	            return false;
	        }

	        int count = 0;
	        Connection connection = null;
	        PreparedStatement preparedStatement = null;
	        ResultSet resultSet = null;

	        try {
	            connection = dataSource.getConnection();
	            String query = "SELECT Count(`player_id`) FROM `sw_player` WHERE `uuid` = ? LIMIT 1;";
	            preparedStatement = connection.prepareStatement(query);
	            preparedStatement.setString(1, fId);
	            resultSet = preparedStatement.executeQuery();

	            if (resultSet.next()) {
	                count = resultSet.getInt(1);
	            }

	        } catch (final SQLException sqlException) {
	            sqlException.printStackTrace();
	            throw sqlException;

	        } finally {
	            if (resultSet != null) {
	                try {
	                    resultSet.close();
	                } catch (final SQLException ignored) {
	                }
	            }

	            if (preparedStatement != null) {
	                try {
	                    preparedStatement.close();
	                } catch (final SQLException ignored) {
	                }
	            }

	            if (connection != null) {
	                try {
	                    connection.close();
	                } catch (final SQLException ignored) {
	                }
	            }
	        }

	        return count > 0;
	    }

	    void createNewPlayer(String fId, String name) throws SQLException {
	        if (checkConnection()) {
	            return;
	        }

	        Connection connection = null;
	        PreparedStatement preparedStatement = null;

	        try {
	            connection = dataSource.getConnection();
	            String query = "INSERT INTO `sw_player` (`player_id`, `uuid`, `player_name`, `wins`, `losses`, `kills`, `deaths`, `elo`, `xp`, `runcount`, " +
							"`pareffect`, `proeffect`, `glasscolor`, `killsound`, `winsound`, `taunt`) VALUES (NULL, ?, ?, 0, 0, 0, 0, 1500, 0, 0, ?, ?, ?, ?, ?, ?);";
	            preparedStatement = connection.prepareStatement(query);
	            preparedStatement.setString(1, fId);
	            preparedStatement.setString(2, name);
	            preparedStatement.setString(3, "none");
	            preparedStatement.setString(4, "none");
	            preparedStatement.setString(5, "none");
	            preparedStatement.setString(6, "none");
	            preparedStatement.setString(7, "none");
	            preparedStatement.setString(8, "none");
	            
	            preparedStatement.executeUpdate();

	        } catch (final SQLException sqlException) {
	            sqlException.printStackTrace();
	            throw sqlException;

	        } finally {
	            if (preparedStatement != null) {
	                try {
	                    preparedStatement.close();
	                } catch (final SQLException ignored) {
	                }
	            }

	            if (connection != null) {
	                try {
	                    connection.close();
	                } catch (final SQLException ignored) {
	                }
	            }
	        }
	    }
	    
}
