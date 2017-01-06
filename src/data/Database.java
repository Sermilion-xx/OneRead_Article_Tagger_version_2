package data;


import org.javatuples.Pair;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

import java.io.ObjectStreamException;
import java.io.Serializable;
import java.sql.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.IntStream;

/**
 * Created by Sermilion on 07/08/2016.
 */
public final class Database implements Serializable{

    @Contract(pure = true) //to get same instance from after deserialization
    protected Object readResolve() throws ObjectStreamException {
        return getInstance();
    }

    private final static Logger LOGGER = Logger.getLogger(Database.class.getName());
    private static final String SQL_DRIVER = "org.postgresql.Driver";
    private static final String DATABASE_URL = "jdbc:postgresql://52.164.247.191:5432/oneread";
    private static final String USER = "rbatukaev";
    private static final String PASSWORD = "22AunaledE25!";

    //The Bill Pugh Singleton
    private static class SingletonHelper {
        private static final Database INSTANCE = new Database();
    }

    public static Database getInstance() {
        return SingletonHelper.INSTANCE;
    }

    private Connection connection;

    private Database() {
        if (this.connection == null) {
            try {
                Class.forName(SQL_DRIVER);
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
                return;
            }
            try {
                this.connection = DriverManager.getConnection(DATABASE_URL, USER, PASSWORD);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    @Nullable
    private static Statement createStatement() {
        try {
            return getInstance().connection.createStatement();
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public void closeConnection() {
        try {
            this.connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Nullable
    public ArrayList<Pair<Integer, String>> getArticlesAndIdsForTag(int tagId, int limit, String lang, int lastId) {
        ArrayList<Pair<Integer, String>> result = new ArrayList<>();
        Pair<Integer, String> article;
        String query = "SELECT a.id, a.description, a.text FROM articles a " +
                "INNER JOIN activities att ON a.id = att.article_id " +
                " WHERE att.model_type= 3 AND att.model_id=" + tagId + " AND a.id>" + lastId +
                " AND a.lang='" + lang + "' LIMIT " + limit + "";
        try (Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery(query)) {
            while (rs.next()) {
                String fulltext;
                int id = rs.getInt("id");
                String desc = rs.getString("description");
                String text = rs.getString("text");
                if (text.length() > desc.length()) {
                    if (text.contains(desc)) {
                        fulltext = text;
                    } else {
                        fulltext = text + " " + desc;
                    }
                } else {
                    fulltext = desc;
                }
                article = new Pair<>(id, fulltext);
                result.add(article);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            LOGGER.log(Level.INFO, "getArticlesAndIdsForTag: SQL exception was raised while performing SELECT: " + e.getMessage());
            return null;

        }
        return result;
    }

    @Nullable
    public ArrayList<String> getIdsNamesForTag(String idsStr) {
        String query = "SELECT name FROM tags WHERE id IN (" + idsStr + ")";
        try (Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery(query)) {
            ArrayList<String> ids = new ArrayList<>();
            while (rs.next()) {
                ids.add(rs.getString("name"));
            }
            return ids;
        } catch (SQLException e) {
            LOGGER.log(Level.INFO, "getIdsNamesForTag: SQL exception was raised while performing SELECT: " + e);
            return null;
        }
    }

    @Nullable
    public ArrayList<Integer> getIdsByTagsNames(String names) {
        ArrayList<Integer> tags = new ArrayList<>();
        String query = "SELECT id FROM tags WHERE name IN (" + names + ")";
        try (Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery(query)) {
            while (rs.next()) {
                tags.add(rs.getInt("id"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            LOGGER.log(Level.INFO, "getIdsByTags: SQL exception was raised while performing SELECT: " + e.getMessage());
            return null;
        }
        return tags;
    }

    @Nullable
    public HashMap<Integer, String> getIdsAndNamesWhereNumberOfTagsMoreThan(int threshold) {
        HashMap<Integer, String> idsAndTags = new HashMap<>();
        String query = "SELECT t.id, t.name, count(a.article_id) as article_counter " +
                "FROM tags t " +
                "INNER JOIN activities a ON t.id=a.model_id " +
                "WHERE t.id=a.model_id AND a.model_type=3 " +
                "GROUP BY t.id " +
                "HAVING count(a.article_id) > " + threshold + " " +
                "ORDER BY article_counter DESC";
        try (Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery(query)) {
            while (rs.next()) {
                int id = rs.getInt("id");
                String name = rs.getString("name");
                idsAndTags.put(id, name);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            LOGGER.log(Level.INFO, "getIdsAndNamesWhereNumberOfTagsMoreThan: SQL exception was raised while performing SELECT: " + e.getMessage());
            return null;
        }
        return idsAndTags;
    }

    @Nullable
    public Pair<List<String>, Integer> getArticlesForTag(int tag_id, int limit, String lang, int lastId) {

        Pair<List<String>, Integer> res;
        int localLastId = -1;
        String query = "SELECT a.id, a.description, a.text FROM articles a, activities at  " +
                "WHERE a.id = at.model_id AND at.model_type=3 " +
                "AND at.model_id=" + tag_id + " AND a.id> " + lastId + " " +
                "AND a.lang= '" + lang + "' LIMIT " + limit + "";
        try (Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery(query)) {
            List<String> result = new ArrayList<>();
//            "SELECT * FROM articles a INNER JOIN article_tags att ON a.id = att.article_id INNER JOIN blogs b ON b.id=a.blog_id" +
//                    " WHERE att.tag_id=" + tag_id + " AND a.id>" + lastId + " AND b.lang='" + lang + "' LIMIT " + limit + ""
            while (rs.next()) {
                String fulltext;
                String text = rs.getString(3);
                String desc = rs.getString(2);
                if (localLastId == -1) {
                    localLastId = rs.getInt(1);
                }
                if (text.length() > desc.length()) {
                    if (text.contains(desc)) {
                        fulltext = text;
                    } else {
                        fulltext = text + " " + desc;
                    }
                } else {
                    fulltext = desc;
                }
                result.add(fulltext);
            }
            res = new Pair<>(result, localLastId);
        } catch (SQLException e) {
            e.printStackTrace();
            LOGGER.log(Level.INFO, "getArticlesForTag: SQL exception was raised while performing SELECT: " + e.getMessage());
            return null;
        }
        return res;
    }

    /**
     * Retrieves all words for a chosen language
     *
     * @param lang - language of dictionary to retrieve
     * @return ArrayList of all the words of chosen language
     */
    @Nullable
    public ArrayList<String> getWordsDictionary(String lang) {
        ArrayList<String> wordDictionary = new ArrayList<>();
        try (Statement st = createStatement()) {
            ResultSet rs;
            if (!lang.equals("en"))
                rs = st.executeQuery("SELECT * FROM tags, tag_langs  WHERE tags.id = tag_langs.tag_id AND tag_langs.lang ='" + lang + "'");
            else
                rs = st.executeQuery("SELECT * FROM tags");
            int index = 0;
            while (rs.next()) {
                String term = rs.getString(2).replaceAll("[\\d#€¡¢∞§•ªº\"≠!@£$%^&*()_+=,.`~;:<>'\\|{}/]", "").replaceAll("-", " ").replaceAll(" ", "");
                if (term.length() > 0) {
                    wordDictionary.add(index, term);
                    index++;
                }
            }
            Collections.sort(wordDictionary);
            rs.close();
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("getWordsDictionary: SQL exception was raised while performing SELECT: " + e);
            return null;
        }
        return wordDictionary;
    }

    /**
     * Save tag to database if it does not exist
     *
     * @param tag  - to to save
     * @param lang
     * @return id of saved tag
     */
    public int addTag(String tag, String lang) {
        ResultSet rs;
        int key = -1;
        try {
            connection.setAutoCommit(false);
            Statement st1 = Database.createStatement();
            Statement st2 = Database.createStatement();
//            st1.executeUpdate("INSERT INTO tags (name) VALUES (?) ON CONFLICT (name) DO NOTHING RETURNING id");
            st1.executeUpdate("INSERT INTO tags (name) SELECT '" + tag + "' WHERE NOT EXISTS (SELECT '" + tag + "' FROM tags WHERE name = '" + tag + "')");
            rs = st1.getGeneratedKeys();
            if (rs.next()) {
                key = rs.getInt(1);
            }
            if (key == -1) {
                key = this.getIdByTag(tag);
            }
            st2.executeUpdate("INSERT INTO tag_langs (tag_id, lang) VALUES (" + key + ", '" + lang + "') ON CONFLICT DO NOTHING");
            st1.close();
            st2.close();
            rs.close();
            connection.commit();
            connection.setAutoCommit(true);
        } catch (SQLException e) {
            System.out.println("addTag: SQL exception was raised while performing INSERT: " + e);
            try {
                connection.rollback();
                System.out.println("addTag: JDBC Transaction rolled back successfully");
            } catch (SQLException e1) {
                System.out.println("addTag: SQLException in rollback" + e.getMessage());
            }
        }
        return key;
    }

    public void saveCentroid(String centroid, int mainTagId, boolean isMainTag, int divisor, String lang) {
        try {
            Statement st = Database.createStatement();
            if (st != null) {
                st.executeUpdate("INSERT INTO tag_centroid (tag, centroid, main, divisor, lang) " +
                        "VALUES (" + mainTagId + ",'" + centroid + "', " + isMainTag + ", " + divisor + ", '" + lang + "') " +
                        "ON CONFLICT (tag) DO UPDATE " +
                        "SET centroid = '" + centroid + "', divisor = " + divisor + "");
            }
            System.out.println("Centroid saved!");
        } catch (SQLException e) {
            e.printStackTrace();
            LOGGER.log(Level.INFO, "SQL exception INSERT - saveCentroid: " + e.getMessage());
        }
    }

    public void saveCentroidToDB(double[] centroid, int tagId, int divisor) {
        PostgreSQLDoubleArray psqlArray = new PostgreSQLDoubleArray(centroid);
        Database.getInstance().saveCentroid(psqlArray.toString(), tagId, false, divisor, "en");
    }

    @Nullable
    public Pair<Integer, double[]> getCentroidAndDivisor(int tag) {
        String centroidString = "";
        double[] results;
        int divisor = 0;
        try {
            Statement st = Database.createStatement();
            ResultSet rs = st.executeQuery("SELECT centroid, divisor FROM tag_centroid WHERE tag ='" + tag + "'");
            while (rs.next()) {
                centroidString = rs.getString(1);
                if (divisor != -1)
                    divisor = rs.getInt(2);
            }
            String[] temp = centroidString.split(",");
            results = new double[temp.length];

            for (int i = 0; i < temp.length; i++) {
                try {
                    results[i] = Double.parseDouble(temp[i]);
                } catch (NumberFormatException ignored) {
                }
            }
            Pair<Integer, double[]> res = new Pair<>(divisor, results);
            rs.close();
            st.close();
            return res;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Nullable
    public HashMap<Integer, Pair<String, double[]>> getAllCentroids() {
        HashMap<Integer, Pair<String, double[]>> centroids = new HashMap<>();
        try {
            Statement st = connection.createStatement();
            ResultSet rs = st.executeQuery("SELECT id, name, centroid FROM tags, tag_centroid WHERE tags.id=tag_centroid.tag");
            while (rs.next()) {
                int tag = rs.getInt("id");
                String name = rs.getString("name");
                String centroid = rs.getString("centroid");

                String[] temp = centroid.split(",");
                double[] results = new double[temp.length];

                for (int i = 0; i < temp.length; i++) {
                    try {
                        results[i] = Double.parseDouble(temp[i]);
                    } catch (NumberFormatException ignored) {
                    }
                }
                centroids.put(tag, new Pair<>(name, results));
            }
            rs.close();
            st.close();
            return centroids;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public ArrayList<Integer> addTags1(ArrayList<String> tagList, String lang) {
        String values;
        StringBuilder sb = new StringBuilder();
        IntStream.range(0, tagList.size()).forEach(idx -> sb.append("('" + tagList.get(idx) + "')"));
        values = sb.toString();
        ResultSet rs;
        ArrayList<Integer> ids = new ArrayList<>();
        try {
            Statement st = connection.createStatement();
            rs = st.executeQuery("with all_tags(name) as (values " + values + ")," +
                    "inserted (id,name) as (insert into tags (name) " +
                    "select name from all_tags on conflict do nothing returning id, name)" +
                    "select t.id, t.name, 0 as inserted from tags t " +
                    "join all_tags at on at.name = t.name union all " +
                    "select id, name, 1 as inserted from inserted");
            rs = st.getGeneratedKeys();
//            st2.executeUpdate("INSERT INTO tag_langs (tag_id, lang) VALUES ('"+tag+"', '"+lang+"'))");

            if (rs.next()) {
                ids.add(rs.getInt(1));
            }
        } catch (SQLException e) {
            System.out.println("SQL exception was raised while performing batch INSERT: " + e.getNextException());
            e.printStackTrace();
        }
        return ids;
    }


    public List<Integer> addWords(List<List<String>> allTermsForTag, String table, String lang) {
        final int batchSize = 1000;
        int count = 0;
        ResultSet rs;
        List<Integer> ids = new ArrayList<>();
        try {
            connection.setAutoCommit(false);
            PreparedStatement stmt = connection.prepareStatement("WITH new_values (name) as (values (?)), " +
                    "upsert as(update " + table + " m set name = nv.name FROM new_values nv WHERE m.name = nv.name RETURNING m.*) " +
                    "INSERT INTO " + table + " (name)SELECT name FROM new_values WHERE NOT EXISTS " +
                    "(SELECT 1 FROM upsert up WHERE up.name = new_values.name)", new String[]{"id"});

            for (List<String> list : allTermsForTag) {
                for (String term : list) {
                    stmt.setString(1, term.toLowerCase());
                    stmt.addBatch();
                    if (list.size() < 100 || ++count % batchSize == 0) {
                        stmt.executeBatch();
                    }
                }
            }
            stmt.executeBatch();
            rs = stmt.getGeneratedKeys();
            while (rs.next()) {
                ids.add(rs.getInt("id"));
            }
            stmt.close();

            PreparedStatement stmt2 = connection.prepareStatement("INSERT INTO tag_langs (tag_id, lang) VALUES (?, ?)");
            for (int a : ids) {
                stmt2.setInt(1, a);
                stmt2.setString(2, lang);
                stmt2.addBatch();
                if (++count % batchSize == 0) {
                    stmt2.executeBatch();
                }
            }
            stmt2.executeBatch();
            connection.commit();
            connection.setAutoCommit(true);

        } catch (SQLException e) {
            e.printStackTrace();
            try {
                connection.rollback();
                return null;
            } catch (SQLException e1) {
                e1.printStackTrace();
                return null;
            }
        }
        return ids;
    }

    public ArrayList<Integer> addTempWords(ArrayList<Pair> allTermsForTag) {
        ResultSet rs = null;
        ArrayList<Integer> ids = new ArrayList<Integer>();
        try {
            PreparedStatement stmt = connection.prepareStatement("WITH new_values (name, lang) AS (VALUES (?,?)), " +
                    "upsert AS(UPDATE tag_temp m SET name = nv.name, lang = nv.lang FROM new_values nv WHERE m.name = nv.name RETURNING m.*) " +
                    "INSERT INTO tag_temp (name, lang)SELECT name, lang FROM new_values WHERE NOT EXISTS " +
                    "(SELECT 1 FROM upsert up WHERE up.name = new_values.name)");
            for (Pair<ArrayList<String>, String> pair : allTermsForTag) {
                String lang = pair.getValue1();
                for (String term : pair.getValue0()) {
                    stmt.setString(1, term.toLowerCase());
                    stmt.setString(2, lang.toLowerCase());
                    stmt.addBatch();
                }
            }
            stmt.executeBatch();
            rs = stmt.getGeneratedKeys();
            while (rs.next()) {
                int id = rs.getInt(1);
                ids.add(id);
            }
            stmt.close();
        } catch (SQLException e) {
            System.out.println("SQL exception was raised while performing INSERT addTempWords: " + e.getNextException());
        }
        return ids;
    }

    /**
     * Method to delete all previously calculated centroids.
     */
    public void deleteCentroids() {
        try {
            Statement st = connection.createStatement();
            st.executeUpdate("DELETE FROM tag_centroid");
            st.close();
            System.out.println("Centroid deleted");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Method to add all new words to database form articles being process
     *
     * @param lang - language of articles (each word's language)
     */
    public void copyWordsFromTemp(String lang) {
        try {
            Statement st = connection.createStatement();
            connection.setAutoCommit(false);
            List<String> tempTerms = this.getAllTempTerms(lang);
            List<List<String>> list = new ArrayList<>();
            list.add(tempTerms);
            if (tempTerms.size() > 0) {
                this.addWords(list, "tags", lang);
                connection.setAutoCommit(false);
                st.executeUpdate("DELETE FROM temp_tags");
            }
            st.close();
            connection.commit();
            connection.setAutoCommit(true);
            System.out.println("Words are copied");
        } catch (SQLException e) {
            e.printStackTrace();
            try {
                connection.rollback();
            } catch (SQLException e1) {
                e.printStackTrace();
            }
        }
    }


    public List<String> getAllTempTerms(String lang) {
        List<String> allTerms = new ArrayList<>();
        try {
            Statement st = connection.createStatement();
            ResultSet rs;
            if (lang.equals("en"))
                rs = st.executeQuery("SELECT * FROM temp_tags WHERE lang ='" + lang + "'");
            else
                rs = st.executeQuery("SELECT * FROM temp_tags");
            int index = 0;
            while (rs.next()) {
                String term = rs.getString(2)
                        .replaceAll("[\\d#€¡¢∞§•ªº\"≠!@£$%^&*()_+=,.`~;:<>'\\|{}/]", "").replaceAll("-", " ").replaceAll(" ", "");
                if (term.length() > 0) {
                    allTerms.add(index, term);
                    index++;
                }
            }
            Collections.sort(allTerms);
            rs.close();
            st.close();
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
        return allTerms;
    }

    /**
     * @param tag for which to get number of articles
     * @return total count of articles for that tag
     */
    public int getIdsCountForTag(String tag) {
        int count = -1;
        try {
            int tag_id = this.getIdByTag(tag);
            Statement st = connection.createStatement();
            ResultSet rs = st.executeQuery(
                    "SELECT count(id) as count FROM articles a INNER JOIN activities att ON a.id = att.model_id AND att.model_type=3" +
                            "WHERE att.model_id=" + tag_id
            );
            while (rs.next()) {
                count = rs.getInt("count");
            }
        } catch (SQLException e) {
            System.out.println("getIdsCountForTag: SQL exception was raised while performing SELECT: " + e);
        }
        return count;
    }

    public int getIdByTag(String tag) {
        try {
            Statement st = connection.createStatement();
            ResultSet rs = st.executeQuery(
                    "SELECT id FROM tags WHERE name='" + tag + "'");
            if (rs.next()) {
                int id = rs.getInt(1);
                return id;
            }
            rs.close();
            st.close();
        } catch (SQLException e) {
            System.out.println("getIdByTag: SQL exception was raised while performing SELECT: " + e);
        }
        return -1;
    }

    @Nullable
    public List<Integer> getIdsByTags(String names) {
        List<Integer> tags = new ArrayList<>();
        String query = "SELECT id FROM tags WHERE name IN (" + names + ")";
        try (Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery(query)) {
            while (rs.next()) {
                tags.add(rs.getInt("id"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
        return tags;
    }
}
