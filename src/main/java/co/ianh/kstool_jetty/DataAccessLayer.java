package co.ianh.kstool_jetty;

import org.mindrot.jbcrypt.BCrypt;

import javax.json.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Stream;

/**
 * Created by henderson_i on 4/4/16.
 */
public class DataAccessLayer {

    private static final String DB_NAME = Config.filename;
    private static final String DB_URL = "jdbc:sqlite:" + DB_NAME;
    private static Connection c;
    private static HashMap<String, PreparedStatement> stmtCache;
    static JsonBuilderFactory factory = Json.createBuilderFactory(null);

    static {
        c = makeConnection();
        initTables();
        stmtCache = buildStatements();
    }

    // Set up DB connection
    private static Connection makeConnection() {
        Connection c = null;
        try {
            c = DriverManager.getConnection(DB_URL);
        } catch ( Exception e ) {
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
            System.exit(0);
        }
        System.out.println("Opened DB Successfully!");
        return c;
    }

    // Build basic schema
    private static void initTables() {
        Statement stmt = null;
        try {
            c.setAutoCommit(false);
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
        } finally {
            try {
                c.setAutoCommit(true);
            } catch (SQLException e) { e.printStackTrace(); }
        }
        System.out.println("Created DB tables!");
    }

    // Create cache of prepared statements
    private static HashMap<String, PreparedStatement> buildStatements() {
        HashMap<String, PreparedStatement> stmtCache = new HashMap<String, PreparedStatement>();

        try {
            stmtCache.put( "addKanji",
                    c.prepareStatement("INSERT OR IGNORE INTO kanji (kanji) VALUES (?)")
            );
            stmtCache.put( "addKanjiWords",
                    c.prepareStatement("INSERT OR IGNORE INTO kanji_words (kanji, word_id) VALUES (?, ?)")
            );
            stmtCache.put( "addKanjiWords_",
                    c.prepareStatement("INSERT OR IGNORE INTO kanji_words (kanji, word_id) SELECT ?, words.id FROM words WHERE words.word = ?")
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
            stmtCache.put( "getUserQueue",
                    c.prepareStatement("SELECT queue FROM study_queue WHERE user_id = ?")
            );
            stmtCache.put( "updateUserQueue",
                    c.prepareStatement("UPDATE study_queue SET queue = ? WHERE user_id = ?")
            );
            stmtCache.put( "addToUserQueue",
                    c.prepareStatement("INSERT INTO study_queue (user_id, queue) VALUES (?, ?)")
            );
            stmtCache.put( "addNewUserQueue",
                    c.prepareStatement("INSERT OR IGNORE INTO study_queue (user_id, queue) SELECT id, ? FROM users WHERE name=?") // first ? will be "[]"
            );
            stmtCache.put( "addNewUser",
                    c.prepareStatement("INSERT OR IGNORE INTO users (name, password, salt) VALUES (?, ?, ?)")
            );
            stmtCache.put( "getUserByName",
                    c.prepareStatement("SELECT id, name, salt FROM users WHERE name = ?")
            );
            stmtCache.put( "getUserByPassword",
                    c.prepareStatement("SELECT id, name FROM users WHERE id = ? AND password = ?")
            );
        } catch (Exception e) {
            e.printStackTrace();
        }

        return stmtCache;
    }


    public static int addKanji(String kanji) throws SQLException {
        int updatedRows = 0;

        PreparedStatement addKanji = stmtCache.get("addKanji");
        addKanji.setString(1, kanji);
        updatedRows = addKanji.executeUpdate();

        return updatedRows;
    }

    public static int addWords(int userId, JsonValue facts) {
        // Prepared statements
        PreparedStatement addWordToWordsTable = stmtCache.get("addWordToWordsTable");
        PreparedStatement addSeenWords_ = stmtCache.get("addSeenWords_");
        PreparedStatement addKanji = stmtCache.get("addKanji");
        PreparedStatement addKanjiWords_ = stmtCache.get("addKanjiWords_");
        PreparedStatement addSeenKanji = stmtCache.get("addSeenKanji");

        ArrayList<String> allSeenKanji = new ArrayList<String>();
        JsonArray factsList;

        // Cast facts to array if not one
        if (facts.getValueType().name() == "ARRAY") {
            factsList = (JsonArray) facts;
        } else if (facts.getValueType().name() == "STRING") {
            factsList = Json.createArrayBuilder()
                    .add(facts)
                    .build();
        } else {
            // Error ?
            return 0;
        }

        try {
            // BEGIN TRANSACTION
            c.setAutoCommit(false);

            factsList.forEach((word)-> {
                try {
                    String cleanWord = Utils.filterKanji(word.toString());
                    allSeenKanji.add(cleanWord); // save big list for later
                    List<String> kanjiArray = Arrays.asList(cleanWord.split("")); //TODO: ???

                    // 1) Add word to words table
                    addWordToWordsTable.setString(1, word.toString());
                    addWordToWordsTable.executeUpdate();
                    // 2) Add to seen words table for current user_id
                    addSeenWords_.setInt(1, userId);
                    addSeenWords_.setString(2, word.toString());
                    addSeenWords_.executeUpdate();

                    // 3. For each kanji in the word...
                    kanjiArray.forEach((kanji)-> {
                        try {
                            // 3a. Add kanji to 'kanji' table...
                            addKanji.setString(1, kanji);
                            addKanji.executeUpdate();
                            // 3b. Add relationship to kanji_words junction table.
                            addKanjiWords_.setString(1, kanji);
                            addKanjiWords_.setString(2, word.toString());
                            addKanjiWords_.executeUpdate();
                            // 3c. Add to seen tables for current user_id
                            addSeenKanji.setInt(1, userId);
                            addSeenKanji.setString(2, kanji);
                            addSeenKanji.executeUpdate();
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }
                    });
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            });

            // COMMIT
            c.commit();
            // 4. Add kanji to 'study_queue' table...
            enqueue(userId, allSeenKanji);

            c.setAutoCommit(true);

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return 1;

    }

    public static int enqueue(int userId, List kanjiArray) {
        PreparedStatement getUserQueue = stmtCache.get("getUserQueue");
        PreparedStatement updateUserQueue = stmtCache.get("updateUserQueue");
        PreparedStatement addToUserQueue = stmtCache.get("addToUserQueue");


        return 1;

    }

    public static boolean checkUserExists(String username) throws SQLException {

        // 1) First, we get users with provided name.
        PreparedStatement getUserByName = stmtCache.get("getUserByName");
        getUserByName.setString(1, username);
        ResultSet maybeUser =  getUserByName.executeQuery();

        return maybeUser.isBeforeFirst();
    }

    public static JsonObject checkUsernameAndPassword(String username, String plainPassword) {
        boolean userChecked = false;
        JsonObject result = null;
        try {
            // 1) First, we get users with provided name.
            PreparedStatement getUserByName = stmtCache.get("getUserByName");
            getUserByName.setString(1, username);
            ResultSet maybeUser =  getUserByName.executeQuery();

            if ( maybeUser.isBeforeFirst() ) { // Rows returned: https://docs.oracle.com/javase/8/docs/api/java/sql/ResultSet.html#isBeforeFirst--
                maybeUser.next(); // move cursor to first row

                // 2) If user in system, generate hash from provided password and retrieved salt.
                int userId = maybeUser.getInt("id");
                String salt = maybeUser.getString("salt");
                String hash = BCrypt.hashpw(plainPassword, salt);

                // 3) Check hash against password in DB.
                userChecked = checkHashedPassword(userId, hash);
            }

            if (userChecked) {
                result = factory.createObjectBuilder()
                        .add("id", maybeUser.getInt("id"))
                        .add("name", maybeUser.getString("name"))
                        .build();
            }

        } catch (SQLException e) { }


        return result;
    }

    private static boolean checkHashedPassword(int userId, String hashedPassword) throws SQLException {
        PreparedStatement getUserByPassword = null;
        getUserByPassword = stmtCache.get("getUserByPassword");
        getUserByPassword.setInt(1, userId);
        getUserByPassword.setString(2, hashedPassword);
        ResultSet maybeUser = getUserByPassword.executeQuery();

        return maybeUser.isBeforeFirst(); // rows exist -> true; no results -> false
    }

    public static int addUser(String username, String plainPassword) throws SQLException {
        // 1) Generate salt for password (default is 10 rounds)
        String salt = BCrypt.gensalt();
        // 2) Generate hash of password + salt
        String hash = BCrypt.hashpw(plainPassword, salt);
        // 3) Save name, hash, salt to DB & create initial entry in study_queue
        PreparedStatement addNewUser = stmtCache.get("addNewUser");
        PreparedStatement addNewUserQueue = stmtCache.get("addNewUserQueue");
        String q = "[]";
        int added = 0; // TODO: get new user data and return to caller

        c.setAutoCommit(false);

        addNewUser.setString(1, username);
        addNewUser.setString(2, hash);
        addNewUser.setString(3, salt);

        addNewUserQueue.setString(1, q);
        addNewUserQueue.setString(2, username);

        added += addNewUser.executeUpdate();
        added += addNewUserQueue.executeUpdate();
        c.commit();

        c.setAutoCommit(true);

        return added;
    }
}
