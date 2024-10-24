# 知識グラフ構築

## 準備

VS Codeをインストールする。

```bash
winget install Microsoft.VisualStudioCode
```

VS Codeの拡張機能Remote Developmentをインストールする（Dev containerだけでもいいかも）。これがあると，コンテナ内での作業をVS Codeで行える。

### コンテナ構築

Dockerをインストールする。参考：https://qiita.com/zembutsu/items/a98f6f25ef47c04893b3

コンテナを構築できること，コンテナからのWebにアクセスできることを確認する。

```bash
docker run --rm curlimages/curl curl -s http://example.net
```

**ここでエラーが出る場合は先に進めない。**

「docker neo4j」で検索すると，[neo4jのオフィシャルイメージ](https://hub.docker.com/_/neo4j/)が見つかる。この先ではこれを使う。参考資料：https://neo4j.com/docs/operations-manual/current/docker/

ユーザ名：neo4j，パスワード：yoloのコンテナを構築する。`graph-data-science`は6章で使う。

コンテナを構築する。

```bash
docker run --name neo4j -d -p 7474:7474 -p 7687:7687 -e NEO4J_AUTH=neo4j/yolo -e NEO4JLABS_PLUGINS="[\"graph-data-science\"]" -e NEO4J_ACCEPT_LICENSE_AGREEMENT=yes neo4j:4.4
```

データを永続化したい場合は，フォルダdataを作って，`-v "$(pwd)/data":/data`などとするのだろうが，まずは，永続化しないで試す。

補足：コンテナの管理はDocker desktopのGUIでもできるが，CUIなら次のとおり。

- 停止：`docker stop neo4j`
- 再開：`docker start neo4j`
- 削除：`docker rm -f neo4j`

コンテナを構築したら，http://localhost:7474/ にアクセスする（ユーザ名：neo4j，パスワード：yolo）。

### コンテナへの接続 

VS Codeで，コンテナneo4jにアタッチする。（やり方はウェブで探す。）

Ctrl+@でターミナルを開く。以下，コマンドはこのターミナルで実行する。

### サンプルコードのダウンロード

Gitをインストールする。

```bash
apt update && apt install git -y
```

サンプルコードをダウンロードする。

```bash
cd
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
MATCH (n)
DETACH DELETE n
```

## 4 知識グラフデータの読み込み

上の作業でグラフができているなら飛ばしてよい→[データの読み込み](load.md)

## 5 知識グラフの組み込み

必須ではないが，試したから残しておく→[Javaのドライバ](java)

## 6 データサイエンスによる知識グラフ拡充

### 6.3 Graph Data Scienceの活用

ブラウザで，次を実行する（まとめて実行するなら「`;`」が必要）。

```cypher
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

### 6.4 グラフデータサイエンスと実験

Pythonをインストールする。

```bash
apt install python3 python-is-python3 python3-pip -y
pip install GraphDataScience
```

データを用意する。

```bash
cd
git clone https://github.com/jbarrasa/gc-2022.git
```

想定するディレクトリ構造は次のとおり。

```
.
├── book-building-knowledge-graphs-ja
│   └── example
└── gc-2022
    ├── guides
    ├── interop
    ├── search
    └── validation
```

LOAD CSVの対象のファイル，nr-stations-all.csvとnr-station-links.csvを/var/lib/neo4j/importにコピーする。

```bash
cp gc-2022/interop/data/* /var/lib/neo4j/import/
```

book-building-knowledge-graphs-ja/example/chapter6/6-2.pyを修正する（ファイル名の前に`file:///`を付ける）。

```bash
cd ~/book-building-knowledge-graphs-ja/example/chapter6
sed -i 's@nr@file:///nr@' 6-2.py
```

リスト6-2から6-4を実行する。補足：6-3をやり直すときは，先に`CALL gds.graph.drop('trains')`を実行する。

```bash
python 6-2.py # データを読み込む。
python 6-3.py # 射影を作成する。
python 6-4.py # バーミンガム・ニューストリート駅とエディンバラ鋭気の最短経路を計算する。
```

実行結果は295.91。書籍（298.0）と異なるから，別の方法で計算してみる。

次のプロンプトで，Claudeにコードを書いてもらう。（略称はnr-stations-all.csvで調べた。）

```
次のような内容のnr-station-links.csvがある。

from,to,distance
AAP,BOP,0.71
AAP,HRN,0.93
AAP,NSG,1.46
AAT,ACN,6.48
...

このファイルを読み込んで，BHMとEDBの最短距離を求めるPythonのプログラム
```

できたコードが[6-4_check.py](6-4_check.py)，ファイル名だけ修正した。

```bash
pip install networkx pandas
wget https://raw.githubusercontent.com/taroyabuki/knowledge/refs/heads/main/6-4_check.py
python 6-4_check.py
```
結果はやはり295.91。

図6-4の再現方法がわからないから，次のプロンプトでClaudeに書いてもらう。

```
次のように読み込んでできるグラフの，BHMとEDBの最短経路を，Neo4j Browserで可視化するCypher

from graphdatascience import GraphDataScience

# データベースに接続
host = "bolt://127.0.0.1:7687"
user = "neo4j"
password = "yolo"
gds = GraphDataScience(host, auth=(user, password), database="neo4j")

# 駅の間の線路をリレーションとして読み込む
gds.run_cypher(
    """\
LOAD CSV WITH HEADERS FROM 'file:///nr-station-links.csv' AS track
MATCH (from:Station {crs: track.from})
MATCH (to:Station {crs: track.to})
MERGE (from)-[:TRACK {distance: round(toFloat(track.distance), 2 )}]->(to)
"""
)
gds.close()
```

生成されたCypherは次のとおり。

```cypher
// 最短経路を見つけて可視化
MATCH path = shortestPath(
  (start:Station {crs: 'BHM'})-[:TRACK*]-(end:Station {crs: 'EDB'})
)
WITH path, reduce(d = 0, r in relationships(path) | d + r.distance) as distance
RETURN path,
       distance as totalDistance
```
