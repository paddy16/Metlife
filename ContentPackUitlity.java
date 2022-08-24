package com.ibm.tivoli.unity.util;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ContentPackUitlity {

	/**Creates a connection to a Derby database. If the database is already loaded and running then the connection is opened in
     * Network mode, else the connection is opened in embedded mode. The connection must be closed explicitly by the client code.
     * @return Connection object
     * @throws IOException
     * @throws UtilityException
     * @throws SQLException
     */
    public static Connection getDerbyConnection(final String unityDBuser,
    		final String unityDBpasswd,
    		final String unityDBhost,
    		final String derbyPort,
    		final String unitySchema,
    		final String dbAbsPath) throws SQLException {
        Connection conn = null;
        final String DERBY_NETWORK_URL = "jdbc:derby://%s:%s/%s";
        final String conUrl = String.format(DERBY_NETWORK_URL, unityDBhost, derbyPort, dbAbsPath);
        
        conn = DriverManager.getConnection(conUrl, unityDBuser, unityDBpasswd);
        conn.setSchema(unitySchema);
        return conn;
    }
}
