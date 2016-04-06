package co.ianh.kstool_jetty;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

/**
 * Created by henderson_i on 4/4/16.
 */
public class DataAccessLayer {

    private static Connection c = null;

    // Set up DB connection
    public static void makeConnection() {
        try {
            if (c == null || c.isClosed()) { // connection already established
                Class.forName("org.sqlite.JDBC");
                c = DriverManager.getConnection("jdbc:sqlite:test.db");
                c.setAutoCommit(false);
            }
        } catch ( Exception e ) {
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
            System.exit(0);
        }
        System.out.println("Opened DB Successfully!");
    }

    public static void initTables() {
        makeConnection();
        try {
            Statement stmt = null;
            stmt = c.createStatement();
            String sql = "CREATE TABLE IF NOT EXISTS users (id INTEGER PRIMARY KEY, name VARCHAR(255) UNIQUE, password VARCHAR(255), salt VARCHAR(255))";
            stmt.executeUpdate(sql);

            // Tables of all unique kanji, words and a junction table
            sql = "CREATE TABLE IF NOT EXISTS kanji (id INTEGER PRIMARY KEY, kanji TEXT UNIQUE)";
            stmt.executeUpdate(sql);
            sql = "CREATE TABLE IF NOT EXISTS words (id INTEGER PRIMARY KEY, word TEXT UNIQUE)";
            stmt.executeUpdate(sql);
            sql = "CREATE TABLE IF NOT EXISTS kanji_words (kanji TEXT, word_id INTEGER, FOREIGN KEY(kanji) REFERENCES kanji(kanji), FOREIGN KEY(word_id) REFERENCES words(id), CONSTRAINT unq UNIQUE (kanji, word_id))";
            stmt.executeUpdate(sql);

            // Tables of seen words/kanji on a per-user basis
            sql = "CREATE TABLE IF NOT EXISTS seen_words (user_id INTEGER, word_id INTEGER, FOREIGN KEY(user_id) REFERENCES users(id), FOREIGN KEY(word_id) REFERENCES words(id))";
            stmt.executeUpdate(sql);
            sql = "CREATE TABLE IF NOT EXISTS seen_kanji (user_id INTEGER, kanji TEXT, FOREIGN KEY(user_id) REFERENCES users(id), FOREIGN KEY(kanji) REFERENCES kanji(kanji))";
            stmt.executeUpdate(sql);

            // Queue of items to study for each user
            sql = "CREATE TABLE IF NOT EXISTS study_queue (user_id INTEGER, queue TEXT, FOREIGN KEY(user_id) REFERENCES users(id))";
            stmt.executeUpdate(sql);

            stmt.close();
            c.commit();
            c.close();
        } catch ( Exception e ) {
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
            System.exit(0);
        }
        System.out.println("Created DB tables!");
    }

}
