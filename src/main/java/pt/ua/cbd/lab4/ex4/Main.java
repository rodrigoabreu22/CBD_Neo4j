package pt.ua.cbd.lab4.ex4;

import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.Driver;
import org.neo4j.driver.GraphDatabase;
import org.neo4j.driver.Session;

public class Main {

    public static void main(String... args) {
        Driver driver;
        String file = "resources/top_500_songs.csv";
        String address = "bolt://localhost:7687";
        String user = "neo4j";
        String password = "password";

        driver = GraphDatabase.driver(address, AuthTokens.basic(user, password));
        Session session = driver.session();
        insertData(session, file);
    }

    public static void insertData(Session session, String filename) {
        //dataset link: https://www.kaggle.com/datasets/omarhanyy/500-greatest-songs-of-all-time

        //Entities:
        //Person {name}
        //Song {title, description}

        //Relations:
        //relation types: WROTE, CREATED, PRODUCED
        //(:Person)-[:WROTE]->(:Song)
        //(:Person)-[:RELEASED]->(:Song) attributes {release_date}
        //(:Person)-[:PRODUCED]->(:Song)

        // Start the query to load CSV
        String query = "LOAD CSV WITH HEADERS FROM 'file:///" + filename + "' AS row\n";

        // Create Song nodes
        query += "MERGE (s:Song {title: row.title, description: row.description})\n";

        // Create Person nodes for artists and establish RELEASED relationships
        query += "FOREACH(artistName IN split(row.artist, ',') | " +
                "MERGE (artist:Person {name: trim(artistName)}) " +
                "MERGE (artist)-[:RELEASED {released: row.released}]->(s))\n";

        // Create Person nodes for producers and establish PRODUCED relationships
        query += "FOREACH(producerName IN split(row.producer, ',') | " +
                "MERGE (producer:Person {name: trim(producerName)}) " +
                "MERGE (producer)-[:PRODUCED]->(s))\n";

        // Create Person nodes for writers and establish WROTE relationships
        query += "FOREACH(writerName IN split(row.writers, ',') | " +
                "MERGE (writer:Person {name: trim(writerName)}) " +
                "MERGE (writer)-[:WROTE]->(s))";

        // Execute the query
        try {
            session.run(query);
            System.out.println("Data inserted successfully from: " + filename);
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Failed to insert data from: " + filename);
        }
    }

}