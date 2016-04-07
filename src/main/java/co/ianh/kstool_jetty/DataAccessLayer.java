package co.ianh.kstool_jetty;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Created by henderson_i on 4/4/16.
 */
public class DataAccessLayer {

    private static Connection c = makeConnection();
    private static final String DB_URL = "jdbc:sqlite:test.db";

    // Set up DB connection
    private static Connection makeConnection() {
        Connection c = null;
        try {
            c = DriverManager.getConnection(DB_URL);
            c.setAutoCommit(false);
        } catch ( Exception e ) {
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
            System.exit(0);
        }
        System.out.println("Opened DB Successfully!");
        return c;
    }

    public static void initTables() {
        Statement stmt = null;
        try {
            stmt = c.createStatement();

            // Table of user data
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS users (id INTEGER PRIMARY KEY, name VARCHAR(255) UNIQUE, password VARCHAR(255), salt VARCHAR(255))");

            // Tables of all unique kanji, words and a junction table
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS kanji (id INTEGER PRIMARY KEY, kanji TEXT UNIQUE)");
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS words (id INTEGER PRIMARY KEY, word TEXT UNIQUE)");
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS kanji_words (kanji TEXT, word_id INTEGER, FOREIGN KEY(kanji) REFERENCES kanji(kanji), FOREIGN KEY(word_id) REFERENCES words(id), CONSTRAINT unq UNIQUE (kanji, word_id))");

            // Tables of seen words/kanji on a per-user basis
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS seen_words (user_id INTEGER, word_id INTEGER, FOREIGN KEY(user_id) REFERENCES users(id), FOREIGN KEY(word_id) REFERENCES words(id))");
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS seen_kanji (user_id INTEGER, kanji TEXT, FOREIGN KEY(user_id) REFERENCES users(id), FOREIGN KEY(kanji) REFERENCES kanji(kanji))");

            // Queue of items to study for each user
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS study_queue (user_id INTEGER, queue TEXT, FOREIGN KEY(user_id) REFERENCES users(id))");

            stmt.close();
            c.commit();
        } catch ( Exception e ) {
//            c.rollback();
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
            System.exit(0);
        }
        System.out.println("Created DB tables!");
    }

}
