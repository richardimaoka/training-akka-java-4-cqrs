### 事前準備:

MacBook前提。

- MySQL8.0.19をローカル開発環境にインストールしてください
  - `brew update`
  - `brew install mysql@8.0.19`
  - `mysql.Sever stop` //もし自分の環境で別のバージョンのMySQLが走っていたら
  - `/usr/local/opt/mysql@8.0/bin/mysql.Sever start`
- Mavenをインストールしてください
  - `brew install maven`

### 作業開始:

- このレポジトリをgit cloneしてください
  - `git clone git@github.com:mvrck-inc/training-akka-java-4-cqrs.git`

この課題はgit clone下ソースコードをそのまま使うことで、自分で新たにソースコードを書くことなく実行できるようになっています。
もちろん、自分で書き方を覚えたい方や、最後の発展的内容に取り組みたい方はご自分でぜひソースコードを書いてみてください。

---
- データベースのセットアップをしてください ([setup.sql](./dbsetup/setup.sql)) 
  - 参考: akka-persistence-jdbcプラグインのデフォルト・テーブル構成([リンク](https://github.com/akka/akka-persistence-jdbc/blob/v3.5.3/src/test/resources/schema/mysql/mysql-schema.sql))

`SELECT * FROM journal;`で以下のようなテーブルが出来ているのが確認できます。

| ordering | persistence_id | sequence_number | deleted | tags | message |
|----------|----------------|-----------------|---------|------|---------|

今回のトレーニングでは`snapshot`テーブルは利用しないので、そちらは無視します。

イベント・ソーシングで使うための上記2つのテーブルに加え、CQRSのRead側で使うための以下の2つのテーブルを定義します。

`SELECT * FROM ticket_stocks;`:

| ticket_id | quantity | 
|-----------|----------|


`SELECT * FROM orders;`:

| id | tikcet_id | user_id | quantity |
|----|-----------|---------|----------|

---
- アプリケーションを走らせてください
  - `mvn compile`
  - `mvn exec:java -Dexec.mainClass=org.mvrck.training.app.Main`
  - `mvn exec:java -Dexec.mainClass=org.mvrck.training.app.ReadSideMain`

`Main`がCQRSのCommand側のHTTP APIです。

```
[INFO] Scanning for projects...
[INFO]
[INFO] ----------------< org.mvrck.training:akka-java-4-cqrs >-----------------
[INFO] Building app 1.0-SNAPSHOT
[INFO] --------------------------------[ jar ]---------------------------------
[INFO]
[INFO] --- exec-maven-plugin:1.6.0:java (default-cli) @ akka-java-4-cqrs ---
SLF4J: A number (1) of logging calls during the initialization phase have been intercepted and are
SLF4J: now being replayed. These are subject to the filtering rules of the underlying logging system.
SLF4J: See also http://www.slf4j.org/codes.html#replay
Server online at http://localhost:8080/
Press RETURN to stop...
```

`ReadSideMain`はRead側ですが、HTTP APIは設けておらず、Read側はSQLを直接使って`ticket_stocks`テーブルと`orders`テーブルに対してクエリを走らせます。

```
Picked up JAVA_TOOL_OPTIONS: -Dfile.encoding=UTF-8
[INFO] Scanning for projects...
[INFO]
[INFO] ----------------< org.mvrck.training:akka-java-4-cqrs >-----------------
[INFO] Building app 1.0-SNAPSHOT
[INFO] --------------------------------[ jar ]---------------------------------
[INFO]
[INFO] --- exec-maven-plugin:1.6.0:java (default-cli) @ akka-java-4-cqrs ---
SLF4J: A number (1) of logging calls during the initialization phase have been intercepted and are
SLF4J: now being replayed. These are subject to the filtering rules of the underlying logging system.
SLF4J: See also http://www.slf4j.org/codes.html#replay
TicketStockCreated: 1, 5000
TicketStockCreated: 2, 2000
```

`TicketStockCreated`イベントが処理されている事がわかります。

---
- curlでデータを挿入してください
  - `curl -X POST -H "Content-Type: application/json" -d "{\"ticket_id\": 1, \"user_id\": 25, \"quantity\": 10}"  http://localhost:8080/orders`
  - クライアント側ログからレスポンスを確認してください
  - サーバー側ログを確認してください
  - データベースでjournalテーブル、ticket_stocksテーブルとordersテーブルを確認してください ([select.sql](./dbsetup/select.sql))

クライアント側ログにはJSONレスポンスが、

```
{"quantity":10,"success":true,"ticketId":1,"userId":25}
```

サーバー側ログにはReadSideで読み込まれたイベントが記録されます。

```
OrderProcessed:     1, 4990
OrderCreated:       90ba1022-7aba-425f-a7d5-769948bdb5d7, 1, 25, 10
```

データベースはそれぞれ以下のようになっています。

`SELECT * FROM journal;`:

| ordering | persistence_id                                  | sequence_number | deleted | tags         | message |
|----------|-------------------------------------------------|-----------------|---------|--------------|---------|
| 1        | TicketStockActor-1                              | 1               | 0       | ticket-stock	| ...     |
| 2        | TicketStockActor-2                              | 1               | 0       | ticket-stock	| ...
| 3        | TicketStockActor-1                              | 2               | 0       | ticket-stock	| ...
| 4        | OrderActor-90ba1022-7aba-425f-a7d5-769948bdb5d7 | 1               | 0       | order       	| ...

`SELECT * FROM ticket_stocks;`:

| ticket_id | quantity | 
|-----------|----------|
| 1         | 4990     |
| 2         | 2000     |

`SELECT * FROM orders;`:

| id                                   | tikcet_id | user_id | quantity |
|--------------------------------------|-----------|---------|----------|
| 90ba1022-7aba-425f-a7d5-769948bdb5d7 | 1         | 25      | 10       |

---
- wrkでベンチマークを走らせてください
  - `wrk -t2 -c4 -d5s -s wrk-scripts/order.lua http://localhost:8080/orders`
    - `-t2`: 2 threads
    - `-c4`: 4 http connections
    - `-d5`: 5 seconds of test duration
    - `wrk-scripts/order.lua` ([リンク](./wrk-scrips/order.lua))
    - クライアント側の実行結果を確認してください
    - データベースでjournalテーブル、ticket_stocksテーブルとordersテーブルを確認してください ([select.sql](./dbsetup/select.sql)) 

ベンチマークの結果はこの様になりました。トレーニング第3回のイベント・ソーシングのみの構成よりパフォーマンスが落ちています。

```
Running 5s test @ http://localhost:8080/orders
  2 threads and 4 connections
  Thread Stats   Avg      Stdev     Max   +/- Stdev
    Latency    47.17ms   16.19ms 143.14ms   79.82%
    Req/Sec    42.52     11.56    70.00     66.00%
  426 requests in 5.01s, 78.21KB read
Requests/sec:     84.97
Transfer/sec:     15.60KB
```

- Command側に加えてRead側処理もデータベースの負担になった
- 同一マシン上ReadSideプロセスを走らせたので、単純に追加プロセスのぶんCPUサイクルを奪い合った

のどちらかもしくは両方の影響だと思います。


---
- akka-persistenceのセットアップを確認してください
  - [application.conf](./src/main/resources/application.conf)
    - 参考 akka-persistence-jdbcプラグインのデフォルト設定([リンク](https://github.com/akka/akka-persistence-jdbc/blob/v3.5.3/src/test/resources/mysql-application.conf))
  - [pom.xml](./pom.xml)
  - TicketStockActor [tagsFor()](https://github.com/mvrck-inc/training-akka-java-4-cqrs/blob/master/src/main/java/org/mvrck/training/actor/TicketStockActor.java#L88)
  - OrderActor [tagsFor()](https://github.com/mvrck-inc/training-akka-java-4-cqrs/blob/master/src/main/java/org/mvrck/training/actor/OrderActor.java#L65)

---
- ReadSideのMainを[確認してください](./src/main/java/org/mvrck/training/app/ReadSide.java)
