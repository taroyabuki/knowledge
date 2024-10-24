# Javaのドライバ

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

neo4j-example/pom.xmlを[pom.xml](pom.xml)のように修正する。

```bash
cd neo4j-example
rm pom.xml
wget https://raw.githubusercontent.com/taroyabuki/knowledge/refs/heads/main/java/pom.xml
```

#### hello, world

ビルド，実行。

```bash
mvn clean package
mvn exec:java -Dexec.mainClass="com.example.App"
```

「Hello World!」と表示されればよい。

#### リスト5-1

[リスト5-1を修正したもの](JavaDriverExample.java)を実行する。


```bash
wget -O src/main/java/com/example/JavaDriverExample.java https://raw.githubusercontent.com/taroyabuki/knowledge/refs/heads/main/java/JavaDriverExample.java
mvn clean package
mvn exec:java -Dexec.mainClass="com.example.JavaDriverExample"
```

Rosaの友人つまりKarlが表示されれば成功。
