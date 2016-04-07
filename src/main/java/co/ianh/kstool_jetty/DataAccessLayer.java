package co.ianh.kstool_jetty;

import java.sql.*;
import java.util.HashMap;

/**
 * Created by henderson_i on 4/4/16.
 */
public class DataAccessLayer {

    private static Connection c = makeConnection();
    private static final String DB_URL = "jdbc:sqlite:test.db";
    private static HashMap<String, PreparedStatement> stmtCache = buildStatements();

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

    // Build basic schema
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

    // Create cache of prepared statements
    private static HashMap<String, PreparedStatement> buildStatements() {
        HashMap<String, PreparedStatement> stmtCache = null;

        try {
            stmtCache.put( "addKanji",
                    c.prepareStatement("INSERT OR IGNORE INTO kanji (kanji) VALUES (?)")
            );
            stmtCache.put( "addKanjiWords",
                    c.prepareStatement("INSERT OR IGNORE INTO kanji_words (kanji, word_id) VALUES (?, ?)")
            );
            stmtCache.put( "addKanjiWords_",
                    c.prepareStatement("INSERT OR IGNORE INTO kanji_words (kanji, word_id) SELECT ? words.id FROM words WHERE words.word = ?")
            );
            stmtCache.put( "addSeenWords",
                    c.prepareStatement("INSERT INTO seen_words (user_id, word_id) VALUES (?, ?)")
            );
            stmtCache.put( "addSeenWords_",
                    c.prepareStatement("INSERT INTO seen_words (user_id, word_id) SELECT ?, words.id FROM words WHERE words.word = ?")
            );
            stmtCache.put( "addSeenKanji",
                    c.prepareStatement("INSERT INTO seen_kanji (user_id, kanji) VALUES (?, ?)")
            );
            stmtCache.put( "addWordToWordsTable",
                    c.prepareStatement("INSERT OR IGNORE INTO words (word) VALUES (?)")
            );
            stmtCache.put( "getRelatedWords",
                    c.prepareStatement("SELECT kanji, word FROM kanji_words AS kw, words AS w, seen_words AS sw WHERE kw.kanji = ? AND kw.word_id = w.id AND kw.word_id = sw.word_id AND sw.user_id = ?")
            );
        } catch (Exception e) {
            e.printStackTrace();
        }

        return stmtCache;
    }


    public static int addKanji(String kanji) throws SQLException {
        PreparedStatement stmt = null;
        int updatedRows = 0;

        stmt = stmtCache.get("addKanji");
        stmt.setString(1, kanji);
        updatedRows = stmt.executeUpdate();

        return updatedRows;
    }
}
