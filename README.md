#はじめに

[【gihyo.jp】MBaaS徹底入門 -- Kii Cloudでスマホアプリ開発](http://gihyo.jp/dev/serial/01/mbaas)の連載で作成したチャットアプリのソースコードです。  
今までSkypeやLINEのようなインスタントメッセンジャーアプリの作成にはサーバサイドの開発が必須であり、個人レベルの開発者が全てを実装するのには、とても高い障壁がありました。  
MBaaSを利用することにより、このようなアプリケーションをサーバサイドの開発無しに、作成することが可能になっています。  
このソースコードで簡単な[KiiCloud](https://developer.kii.com/?locale=jp)の使い方を学ぶことができます。  
アプリケーションを実際に動作させる為には[KiiCloud](https://developer.kii.com/?locale=jp)への登録**(無料)**が必要になります。

#KiiCloudについて

[KiiCloud](https://developer.kii.com/?locale=jp)は[Kii株式会社](http://jp.kii.com/)が提供しているMBaaS(Mobile Backend as a Service)です。  
主にモバイルアプリケーション向けにユーザ管理、データ管理、アクセス制御、プッシュ通知、データ分析などの様々な機能を提供しています。  
[KiiCloud](https://developer.kii.com/?locale=jp)を利用することにより、サーバサイドの開発無しにリッチなモバイルアプリケーションを開発することが可能になります。  
[KiiCloud](https://developer.kii.com/?locale=jp)は**無料**で始められますので、是非、MBaaSの力を体験してみて下さい。


#開発環境

Eclipseにプロジェクトをインポートしてビルドする場合、Android Support Library v7とGoogle Play Serviceが必要です。  
以下のプロジェクトをEclipseのワークスペースにインポートしてください。

    {SDK-DIR}/extras/android/support/v7/appcompat
    {SDK-DIR}/extras/google/google_play_services_froyo/libproject/google-play-services_lib

また、プッシュ通知を実装するために、GCM (Google Cloud Messaging)を使用していますが、この機能を使用するためにGoogleのアカウントが必要になります。  
エミュレータを使って動作確認する場合は、ターゲットを以下のようにGoogle APIsに設定してください。  
エミュレータ起動後に「設定->アカウント」からGoogleアカウントの設定を行ってください。  

依存しているライブラリのリビジョンは以下の通りです。  

- android-support-v4.jar (rev. 19.1)
- android-support-v7-appcompat.jar (rev. 19.1)
- google-play-services.jar (for froyo rev. 16)


<img src="screenshots/05.png">


#サポート

本ソースコードおよび、KiiCloudについてのご質問は[コミュニティサイト](http://community-jp.kii.com/)にてお願い致します。


#スクリーンショット

<table border="0">
  <tr>
    <td><img src="screenshots/01.png"></td>
    <td><img src="screenshots/02.png"></td>
  </tr>
  <tr>
    <td><img src="screenshots/03.png"></td>
    <td><img src="screenshots/04.png"></td>
  </tr>
</talbe>


