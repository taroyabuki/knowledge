# 知識グラフ構築

## 準備

### コンテナ構築

Dockerの動作確認

```bash
docker run --rm curlimages/curl curl -s http://example.net
```

コンテナを構築できること，コンテナからのWebにアクセスできることを確認してから先に進む。

参考資料：https://neo4j.com/docs/operations-manual/current/docker/

ユーザ名：neo4j，パスワード：yoloのコンテナを構築する。

```bash
docker run -p 7474:7474 -p 7687:7687 -d -e NEO4J_AUTH=neo4j/yolo --name neo4j neo4j:4.4
```bash

データを永続化したい場合は，フォルダdataを作って，`-v "$(pwd)/data":/data`などとするのだろうが，まずは，永続化しないて試す。

補足：やり直す場合は次のとおり。Docker desktopのGUIで削除してもよい。

```bash
docker rm -f neo4j
```

コンテナを構築したら，http://localhost:7474/ にアクセスする（ユーザ名：neo4j，パスワード：yolo）。

### コンテナへの接続 

VS Codeで，拡張機能Remote Developmentをインストールする（Dev containerだけでもいいかも）。

VS Codeで，コンテナneo4jにアタッチする。

Ctrl+@でターミナルを開く。以下，コマンドはこのターミナルで実行する。

### サンプルコードのダウンロード

Gitをインストールする。

```
apt update && apt install git -y
```

サンプルコードをダウンロードする。

```
git clone https://github.com/sakusaku-rich/book-building-knowledge-graphs-ja.git
```

## 4 知識グラフデータの読み込み

### 4. 2 LOAD CSV

CSVファイルを/var/lib/neo4j/import/に置く。（教科書に書いてない？）

```bash
cd
cp book-building-knowledge-graphs-ja/example/chapter4/4-5.csv /var/lib/neo4j/import/places.csv
cp book-building-knowledge-graphs-ja/example/chapter4/4-7.csv /var/lib/neo4j/import/people.csv
cp book-building-knowledge-graphs-ja/example/chapter4/4-9.csv /var/lib/neo4j/import/friend_rels.csv
cp book-building-knowledge-graphs-ja/example/chapter4/4-11.csv /var/lib/neo4j/import/lives_in.csv
```

ターミナルでCypherを実行する（リスト4-6, 4-8, 4-10, 4-12を修正）。ブラウザで実行してもよい。複数まとめる場合は「`;`」が必要。

```cypher
LOAD CSV WITH HEADERS FROM 'file:///places.csv' AS line
MERGE (:Place { country: line.country, city: line.city });

LOAD CSV WITH HEADERS FROM 'file:///people.csv' AS line
MERGE (p:Person { name: line.name })
SET p.age = line.age
SET p.gender = line.gender;

LOAD CSV WITH HEADERS FROM 'file:///friend_rels.csv' AS line
MATCH (p1:Person {name: line.from})
MATCH (p2:Person {name: line.to})
MERGE (p1)-[:FRIEND]->(p2);

LOAD CSV WITH HEADERS FROM 'file:///lives_in.csv' AS line
MATCH (person:Person {name: line.from})
MATCH (place:Place {city: line.to})
MERGE (person)-[r:LIVES_IN]->(place)
SET r.since = line.since;
```

ブラウザで結果を確認する。

```cypher
MATCH (n) OPTIONAL MATCH (n)-[r]-()
RETURN n,r
```

補足：削除するなら次のとおり。

```
MATCH () -[r:LIVES_IN]-> () DELETE r;

MATCH () -[r:FRIEND]-> () DELETE r;

MATCH (n) DELETE n;

MATCH (n) DETACH DELETE n;
```

### 4.3 neo4j-admin

4.3節は，データベースを止める必要があるが，`neo4j stop`とするとコンテナが終了してしまう。Dockerではできない？　そもそも，Community Editionではデータベースを止められない？

試す場合：

```bash
cd book-building-knowledge-graphs-ja/example

sed -i 's/bin\///' chapter4/4-22.sh
sed -i 's/import\//chapter4\//g' chapter4/4-22.sh
sh chapter4/4-22.sh
```

## 5 知識グラフの組み込み

### Javaのドライバ

あえてJavaを試す必要はないが，動かしてみるのも経験にはなるか。

```bash
apt install maven -y
```

プロジェクトを作る。

```bash
mvn archetype:generate -DgroupId=com.example -DartifactId=neo4j-example -DarchetypeArtifactId=maven-archetype-quickstart -DinteractiveMode=false
```

この段階で，フォルダ構造は次のようになっている。

```
.
├── book-building-knowledge-graphs-ja ← サンプルコード
│   └── example
│       ├── chapter3
│       ├── chapter4
│       ├── chapter5
│       └── ...
└── neo4j-example ← ここで作業する。pom.xmlを編集する。
    ├── src
    │   ├── main
    │   │   └── java
    │   │       └── com
    │   │           └── example ← JavaDriverExample.javaを置く
    │   └── test
    │       └── java
    │           └── com
    │               └── example
    └── target
        ├── ...
```

neo4j-example/pom.xmlを次のように修正する。

```xml
<project xmlns="http://maven.apache.org/POM/4.0.0"
  xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/maven-v4_0_0.xsd">
  <modelVersion>4.0.0</modelVersion>
  <groupId>com.example</groupId>
  <artifactId>neo4j-example</artifactId>
  <packaging>jar</packaging>
  <version>1.0-SNAPSHOT</version>
  <properties>
    <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
    <maven.compiler.source>11</maven.compiler.source>
    <maven.compiler.target>11</maven.compiler.target>
  </properties>
  <name>neo4j-example</name>
  <url>http://maven.apache.org</url>
  <dependencies>
    <dependency>
      <groupId>org.neo4j.driver</groupId>
      <artifactId>neo4j-java-driver</artifactId>
      <version>4.4.0</version>
    </dependency>
    <dependency>
      <groupId>junit</groupId>
      <artifactId>junit</artifactId>
      <version>3.8.1</version>
      <scope>test</scope>
    </dependency>
  </dependencies>
</project>
```

#### hello, world

プロジェクト内に移動して，ビルド，実行。

```bash
cd cd neo4j-example
mvn clean package
mvn exec:java -Dexec.mainClass="com.example.App"
```

「Hello World!」と表示されればよい。

#### リスト5-1

リスト5-1（book-building-knowledge-graphs-ja/example/chapter5/5-1.java）には次の問題がある。

- 構文エラーがある。（「`- >`」は正しくは「`->`」）
- メソッド`findFriends`が終了した時点でセッションが終了するため，戻り値の`result`は使えない。

AIにコードとエラーメッセージを与えて解決した結果は次のとおり。これをneo4j

```java
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
```

ビルド，実行して，Rosaの友人つまりKarlが表示されればよい。

```bash
mvn clean package
mvn exec:java -Dexec.mainClass="com.example.JavaDriverExample"
```
