# ChatApp2

ChatApp2 は、リアルタイム通信とセキュリティを重視して開発した Web チャットアプリです。  
1 対 1 の個人チャットと、複数人のグループチャットに対応しており、Slack や LINE のようなアプリケーションを目指しています。

---

## 使用技術スタック

| 技術           | 内容                                   |
| -------------- | -------------------------------------- |
| バックエンド   | Java + Spring Boot                     |
| フロントエンド | Thymeleaf（HTML テンプレートエンジン） |
| 通信方式       | WebSocket（リアルタイム通信）          |
| 認証方式       | JWT（JSON Web Token）                  |
| データベース   | MySQL（JPA によるデータ管理）          |

---

## 主な機能

### ユーザー認証

- 登録・ログイン機能
- JWT によるセキュアな認証
- Spring Security によるアクセス制御

### フレンド機能

- フレンドの追加・削除
- フレンドリストの表示
- オンライン状態のリアルタイム更新

### チャット機能

- 個人チャット（1 対 1）
- グループチャット（複数人）
- メッセージのリアルタイム反映（WebSocket）
- 履歴の保存と表示

---

## 特にこだわったポイント

### セキュリティ

- Spring Security によるアクセス制御
- JWT（トークン）での安全な認証
- 未認証ユーザーのチャットルームアクセスを制限

### リアルタイム通信

- WebSocket によりページリロード不要でメッセージが反映
- オンライン状態もリアルタイム更新

---
