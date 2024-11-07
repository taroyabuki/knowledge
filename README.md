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

**コンテナ内での作業だから，よくわからなくなったら中断して，コンテナを削除してやり直せばよい。**

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

上の作業でグラフができているなら飛ばしてよい→[データの読み込み](ch04.md)

## 5 知識グラフの組み込み

必須ではないが，試したから残しておく→[Javaのドライバ](java)

## [6 データサイエンスによる知識グラフ拡充](ch06.md)

## [7 グラフネイティブ機械学習](ch07.md)