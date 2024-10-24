# 4 知識グラフデータの読み込み

### 4. 2 LOAD CSV

CSVファイルを/var/lib/neo4j/import/に置く。（教科書に書いてない？）

```bash
cd
cp book-building-knowledge-graphs-ja/example/chapter4/*.csv /var/lib/neo4j/import/
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
LOAD CSV WITH HEADERS FROM 'file:///4-5.csv' AS line
MERGE (:Place { country: line.country, city: line.city });

LOAD CSV WITH HEADERS FROM 'file:///4-7.csv' AS line
MERGE (p:Person { name: line.name })
SET p.age = line.age
SET p.gender = line.gender;

LOAD CSV WITH HEADERS FROM 'file:///4-9.csv' AS line
MATCH (p1:Person {name: line.from})
MATCH (p2:Person {name: line.to})
MERGE (p1)-[:FRIEND]->(p2);

LOAD CSV WITH HEADERS FROM 'file:///4-11.csv' AS line
MATCH (person:Person {name: line.from})
MATCH (place:Place {city: line.to})
MERGE (person)-[r:LIVES_IN]->(place)
SET r.since = line.since;
```

`:exit`で終了する。

### 4.3 neo4j-admin

4.3節は，データベースを止める必要があるが，`neo4j stop`とするとコンテナが終了してしまう。Dockerではできない？　そもそも，Community Editionではデータベースを止められない？

試す場合：

```bash
cd book-building-knowledge-graphs-ja/example

sed -i 's/bin\///' chapter4/4-22.sh
sed -i 's/import\//chapter4\//g' chapter4/4-22.sh
sh chapter4/4-22.sh
```
