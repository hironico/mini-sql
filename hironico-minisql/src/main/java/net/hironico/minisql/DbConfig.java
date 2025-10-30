package net.hironico.minisql;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import net.hironico.common.utils.NicoCrypto;
import org.eclipse.persistence.config.PersistenceUnitProperties;

import javax.crypto.SecretKey;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Logger;
import java.net.InetAddress;

@JsonRootName("dbConfig")
@JacksonXmlRootElement(localName = "dbConfig")
@JsonIgnoreProperties(value = { "connection" }, ignoreUnknown = true)
// @XmlAccessorType(XmlAccessType.NONE)
public class DbConfig implements Cloneable {
    private static final Logger LOGGER = Logger.getLogger(DbConfig.class.getName());

    private static final String superSecret = "EkRg0PcE2yv80Zhal+xTsGLSsIyZhlkttEbd2bMNT3Q=";
    private static final SecretKey secretKey = NicoCrypto.generate(superSecret);
    private static final NicoCrypto crypto = new NicoCrypto();

    @JsonProperty("name")
    @JacksonXmlProperty(localName = "name", isAttribute = true)
    public String name = null;

    @JsonProperty("jdbcUrl")
    @JacksonXmlProperty(localName = "jdbc-url", isAttribute = true)
    public String jdbcUrl = null;

    @JsonProperty("driverClassName")
    @JacksonXmlProperty(localName = "driver-class-name", isAttribute = true)
    public String driverClassName = "oracle.jdbc.OracleDriver";

    @JsonProperty("user")
    @JacksonXmlProperty(localName = "user", isAttribute = true)
    public String user = null;

    @JsonProperty("password")
    @JacksonXmlProperty(localName = "password", isAttribute = true)
    public String password = null;

    @JsonProperty("batchStatementSeparator")
    @JacksonXmlProperty(localName = "batch-statement-separator", isAttribute = true)
    public String batchStatementSeparator = "GO";

    @JsonProperty("color")
    @JacksonXmlProperty(localName = "color", isAttribute = true)
    public String color;

    @JsonProperty("useQuotedIdentifiers")
    @JacksonXmlProperty(localName = "useQuotedIdentifiers", isAttribute = true)
    public Boolean useQuotedIdentifiers = Boolean.FALSE;

    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    /**
     * Test if connection parameters are valid while not throwing any exception.
     * @return true if can open a new connection with the current parameters.
     */
    public boolean test() {
        try {
            Connection _con = getConnection();
            _con.close();
            return true;
        } catch (Throwable t) {
            return false;
        }
    }
    public Connection getConnection() throws ClassNotFoundException, SQLException, InstantiationException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        LOGGER.finest("Get connection: Loading driver class: " + this.driverClassName);
        
        Properties props = new Properties();
        props.put("user", this.user);
        props.put("password", DbConfig.decryptPassword(this.password));

        if (this.jdbcUrl.contains("oracle")) {
            addOracleProperties(props);
        }
        
        // Try to load the driver using the custom driver class loader first (for dynamically loaded drivers)
        ClassLoader driverClassLoader = getDriverClassLoader();
        
        try {
            // Load driver class from custom class loader
            Class<?> driverClass = Class.forName(this.driverClassName, true, driverClassLoader);
            java.sql.Driver driver = (java.sql.Driver) driverClass.getConstructor().newInstance();
            
            LOGGER.finest("Loaded driver using custom driver class loader: " + this.driverClassName);
            
            // Bypass DriverManager and connect directly using the driver instance
            // This is necessary because DriverManager has security restrictions with custom class loaders
            Connection conn = driver.connect(this.jdbcUrl, props);
            
            if (conn == null) {
                throw new SQLException("Driver returned null connection for URL: " + this.jdbcUrl);
            }
            
            return conn;
            
        } catch (ClassNotFoundException e) {
            // Fall back to default class loader and DriverManager
            LOGGER.finest("Driver not found in custom class loader, trying system class loader");
            Class.forName(this.driverClassName).getConstructor().newInstance();
            return DriverManager.getConnection(this.jdbcUrl, props);
        }
    }

    /**
     * Get the driver class loader from DriverConfigPanel.
     * This allows loading drivers that were dynamically added through the UI.
     */
    private ClassLoader getDriverClassLoader() {
        try {
            Class<?> driverConfigPanelClass = Class.forName("net.hironico.minisql.ui.config.DriverConfigPanel");
            java.lang.reflect.Method method = driverConfigPanelClass.getDeclaredMethod("getDriverClassLoader");
            return (ClassLoader) method.invoke(null);
        } catch (Exception e) {
            LOGGER.finest("Could not get custom driver class loader, using system class loader");
            return ClassLoader.getSystemClassLoader();
        }
    }

    private void addOracleProperties(Properties props) {
        props.put("v$session.osuser", System.getProperty("user.name"));
        try {
            props.put("v$session.machine", InetAddress.getLocalHost().getCanonicalHostName());
        } catch (Exception ex) {
            // nop
        }
        props.put("v$session.program", "Hironico MiniSQL");
    }

    public static String encryptPassword(String clearPassword) {
        try {
            return crypto.encrypt(clearPassword, secretKey);
        } catch (Exception ex) {
            return clearPassword;
        }
    }

    public static String decryptPassword(String encryptedPassword) {
        try {
            return crypto.decrypt(encryptedPassword, secretKey);
        } catch (Exception ex) {
            return encryptedPassword;
        }
    }

    /**
     * Shortcut to toEntityManager(name, null). this method uses only the JDBC properties for user, password, driver, and URL
     * to create the entity manager factory from this DbConfig information.
     * @see #toEntityManagerFactory(String, Map)
     * @return @return Entity manager factory configured accordingly with this DbConfig or null if the peristence unit name is not found.
     */
    public EntityManagerFactory toEntityManagerFactory(String name) {
        return this.toEntityManagerFactory(name, null);
    }

    /**
     * Convert this DB config to an EntityManagerFactory used in JPA frameworks.
     * This method tries to look at the persistence.xml files to load the unit designated as 
     * the name given in parameter. Then the persistence unit is configured to use the connection
     * setup of this DbConfig.
     * @param name Name of the peristence unit to load and ocnfigure
     * @param additionalProperties additional properties to include in the configuration on top of the jdbc user, password, url and driver ones.
     * @return Entity manager factory configured accordingly with this DbConfig or null if the peristence unit name is not found.
     */
    public EntityManagerFactory toEntityManagerFactory(String name, Map<String, Object> additionalProperties) {
        Map<String, Object> props = new HashMap<>();
        // props.put(PersistenceUnitProperties.TRANSACTION_TYPE, PersistenceUnitTransactionType.RESOURCE_LOCAL);
        props.put(PersistenceUnitProperties.JDBC_USER, this.user);
        props.put(PersistenceUnitProperties.JDBC_PASSWORD, DbConfig.decryptPassword(this.password));
        props.put(PersistenceUnitProperties.JDBC_URL, this.jdbcUrl);
        props.put(PersistenceUnitProperties.JDBC_DRIVER, this.driverClassName);

        if (additionalProperties != null) {
            props.putAll(additionalProperties);
        }

        return Persistence.createEntityManagerFactory(name, props);
    }

    @Override
    public String toString() {
        return this.name == null ? "NULL" : this.name;
    }
}
