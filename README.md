# 知識グラフ構築

## 準備

### コンテナ構築

Dockerの動作確認

```bash
docker run --rm curlimages/curl curl -s http://example.net
```

コンテナを構築できること，コンテナからのWebにアクセスできることを確認してから先に進む。

参考資料：https://neo4j.com/docs/operations-manual/current/docker/

ユーザ名：neo4j，パスワード：yoloのコンテナを構築する。`graph-data-science`は6章で使う。

```bash
docker run \
    --name neo4j \
    -p 7474:7474 -p 7687:7687 \
    -e NEO4J_AUTH=neo4j/yolo \
    -e NEO4JLABS_PLUGINS='["graph-data-science"]' \
    -e NEO4J_ACCEPT_LICENSE_AGREEMENT=yes \
    neo4j:4.4
```

データを永続化したい場合は，フォルダdataを作って，`-v "$(pwd)/data":/data`などとするのだろうが，まずは，永続化しないで試す。

補足：コンテナの管理はDocker desktopのGUIでもできるが，CUIなら次のとおり。

- 終了する場合：Ctrl-C
- 再開する場合：`docker start neo4j`
- 削除する場合：`docker rm -f neo4j`

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

## 3 グラフデータベース

ブラウザで次を実行する。

```cypher
MERGE (london:Place {city: 'London', country: 'UK'})
MERGE (fred:Person {name: 'Fred'})
MERGE (fred) -[:LIVES_IN]-> (london)
MERGE (karl:Person {name: 'Karl'})
MERGE (karl) -[:LIVES_IN]-> (london)
MERGE (berlin:Place {city: 'Berlin', country: 'DE'})
MERGE (rosa:Person {name: 'Rosa'})
MERGE (rosa) -[:LIVES_IN]-> (berlin)
MERGE (fred) -[:FRIEND]-> (karl)
MERGE (karl) -[:FRIEND]-> (fred)
MERGE (rosa) -[:FRIEND]-> (karl)
MERGE (karl) -[:FRIEND]-> (rosa)
```

結果の確認

```cypher
MATCH (n) RETURN (n)
```

補足：削除は次のとおり。

```cypher
MATCH () -[r:LIVES_IN]-> () DELETE r;
MATCH () -[r:FRIEND]-> () DELETE r;
MATCH (n) DELETE n;
MATCH (n) DETACH DELETE n;
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

ターミナルでCypher Shellを起動する。（ブラウザでも実行してもよい。）

```bash
cypher-shell -uneo4j -pyolo
#ファイルに書いておいて
#cypher-shell -uneo4j -pyolo < ファイル
#として実行することもできる。
```

リスト4-6, 4-8, 4-10, 4-12を修正して実行する（複数まとめる場合は「`;`」が必要）。

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

:exit
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

[Javaのドライバ](java)

## 6 データサイエンスによる知識グラフ拡充

次を実行する（まとめて実行するなら「`;`」が必要）。

```
CALL gds.graph.project.cypher(
 'gds-example-graph',
 'MATCH (p:Person)
  RETURN id(p) AS id',
 "MATCH (p1:Person)-[:FRIEND]->(p2:Person)
  RETURN id(p1) AS source, id(p2) AS target, 'FRIEND' AS type"
);
  
CALL gds.betweenness.write(
   'gds-example-graph', {
       writeProperty: 'betweennessCentrality'
   }
);
```

次の結果のUIの「Text」をクリックすると，図6-2のようになる（図6-2とは異なるが，betweennessCentralityの値は同じ）。

```cypher
MATCH (n) RETURN (n)
```

プロパティの削除

```cypher
MATCH (n:Person)
REMOVE n.betweennessCentrality
```

グラフの削除

```
CALL gds.graph.drop('gds-example-graph')
```