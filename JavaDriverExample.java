package com.example;

import static org.neo4j.driver.Values.parameters;

import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.Driver;
import org.neo4j.driver.GraphDatabase;
import org.neo4j.driver.Session;
import org.neo4j.driver.Record;
import org.neo4j.driver.exceptions.Neo4jException;

import java.util.ArrayList;
import java.util.List;

public class JavaDriverExample implements AutoCloseable {

  private final Driver driver;

  public JavaDriverExample(String uri, String user, String password) {
    driver = GraphDatabase.driver(uri, AuthTokens.basic(user, password));
  }

  @Override
  public void close() {
    driver.close();
  }

  public List<String> findFriends(final String name) {
    List<String> friends = new ArrayList<>();

    try (Session session = driver.session()) {
      session.readTransaction(tx -> {
        var result = tx.run(
            "MATCH (a:Person)-[:FRIEND]->(b:Person) "
            + "WHERE a.name = $name "
            + "RETURN b.name",
            parameters("name", name)
        );

        // 結果をセッション内で処理してリストに格納
        while (result.hasNext()) {
          Record record = result.next();
          friends.add(record.get("b.name").asString());
        }

        return friends; // 結果をリターン
      });
    } catch (Neo4jException e) {
      System.err.println("Error querying the database: " + e.getMessage());
      throw e;
    }

    return friends;
  }

  public static void main(String... args) {
    try (JavaDriverExample example = new JavaDriverExample("bolt://localhost:7687", "neo4j", "yolo")) {
      List<String> friends = example.findFriends("Rosa");

      // 結果の表示
      for (String friend : friends) {
        System.out.println(friend);
      }
    } catch (Exception e) {
      System.err.println("Error running the example: " + e.getMessage());
    }
  }
}
