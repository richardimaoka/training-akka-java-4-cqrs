JavaによるAkkaトレーニング第4回 

## アクターとデータベースのシステム(CQRS)

Akkaは状態を持つ非同期処理を実装するだけでなく、耐障害性をもったシステムを構築するのに有用なツールキットです。
[トレーニングの第3回](https://github.com/mvrck-inc/training-akka-java-1-preparation)ではイベント・ソーシングを用いてアクターとデータベースを接続しました。
今回はCQRS - Command Query Responsibility Separationパターンとイベント・ソーシングを組み合わせる方法を紹介します。

- [第1回のトレーニング: リレーショナル・データベースのトランザクションによる排他制御](https://github.com/mvrck-inc/training-akka-java-1-preparation)
- [第2回のトレーニング: アクターによる非同期処理](https://github.com/mvrck-inc/training-akka-java-2-actor)
- [第3回のトレーニング: アクターとデータベースのシステム(イベント・ソーシング)](https://github.com/mvrck-inc/training-akka-java-3-persistence)
- [第4回のトレーニング: アクターとデータベースのシステム(CQRS)](https://github.com/mvrck-inc/training-akka-java-4-cqrs)
- [第5回のトレーニング: クラスタリング](https://github.com/mvrck-inc/training-akka-java-5-clustering)

## 課題

この課題をこなすことがトレーニングのゴールです。
独力でも手を動かしながら進められるようようになっていますが、可能ならトレーナーと対話しながらすすめることでより効果的に学べます。


- [課題提出トレーニングのポリシー](https://github.com/mvrck-inc/training-akka-java-1-preparation/blob/master/POLICIES.md)

## この課題で身につく能力

- CQRSのRead Sideの実装方法がわかる

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
- データベースのセットアップをしてください ([setup.sql](./dbsetup/setup.sql)) 
  - 参考: akka-persistence-jdbcプラグインのデフォルト・テーブル構成([リンク](https://github.com/akka/akka-persistence-jdbc/blob/v3.5.3/src/test/resources/schema/mysql/mysql-schema.sql))
- アプリケーションを走らせてください
  - `mvn compile`
  - `mvn exec:java -Dexec.mainClass=org.mvrck.training.app.Main`
  - `mvn exec:java -Dexec.mainClass=org.mvrck.training.app.ReadSideMain`
- curlでデータを挿入してください
  - `curl -X POST -H "Content-Type: application/json" -d "{\"ticket_id\": 1, \"user_id\": 2, \"quantity\": 1}"  http://localhost:8080/orders`
  - クライアント側ログからレスポンスを確認してください
  - サーバー側ログを確認してください
  - データベースでjournalテーブル、ticket_stocksテーブルとordersテーブルを確認してください ([select.sql](./dbsetup/select.sql))
- wrkでベンチマークを走らせてください
  - `wrk -t2 -c4 -d5s -s wrk-scripts/order.lua http://localhost:8080/orders`
    - `-t2`: 2 threads
    - `-c4`: 4 http connections
    - `-d5`: 5 seconds of test duration
    - `wrk-scripts/order.lua` ([リンク](./wrk-scrips/order.lua))
    - クライアント側の実行結果を確認してください
    - データベースでjournalテーブル、ticket_stocksテーブルとordersテーブルを確認してください ([select.sql](./dbsetup/select.sql)) 
- akka-persistenceのセットアップを確認してください
  - [application.conf](./src/main/resources/application.conf)
    - 参考 akka-persistence-jdbcプラグインのデフォルト設定([リンク](https://github.com/akka/akka-persistence-jdbc/blob/v3.5.3/src/test/resources/mysql-application.conf))
  - [pom.xml](./pom.xml)
  - TicketStockActor [tagsFor()](https://github.com/mvrck-inc/training-akka-java-4-cqrs/blob/master/src/main/java/org/mvrck/training/actor/TicketStockActor.java#L88)
  - OrderActor [tagsFor()](https://github.com/mvrck-inc/training-akka-java-4-cqrs/blob/master/src/main/java/org/mvrck/training/actor/OrderActor.java#L65)
- ReadSideのMainを[確認してください](./src/main/java/org/mvrck/training/app/ReadSide.java)

## 説明

- [課題背景](./BACKGROUND.md)
- [課題手順の詳細](./INSTRUCTION.md)

## 参考文献・資料

- https://plantuml.com/