Questoria QTrap - TrapPvP Eklentisi​
Desteklenen Sürümler:
Paper / Purpur 1.21.x

Amacı:
QTrap, TrapPvP sunucuları için geliştirilmiş kapsamlı bir trap yönetim eklentisidir. Oyuncular trap satın alabilir, bölgelerini yönetebilir, üyeler ekleyebilir, trap seviyesini yükseltebilir ve pazar sistemi üzerinden trap ticareti yapabilir.

Özellikleri:

-> Trap Satın Alma Sistemi:
Oyuncular belirlenen trap bölgelerini ekonomi sistemi üzerinden satın alabilir.

-> Chunk Tabanlı Trap Sistemi:
Trap bölgeleri chunk mantığıyla çalışır ve birden fazla chunk desteği sunar.

-> Otomatik Koruma Sistemi:
Blok kırma, blok koyma, sandık açma ve etkileşim izinleri otomatik yönetilir.

-> GUI Yönetim Sistemi:
Trap bilgileri, üyeler, banka, yükseltme, pazar ve log işlemleri menü üzerinden kontrol edilir.

-> Trap Can Sistemi:
Her trap için özel can değeri bulunur ve config üzerinden düzenlenebilir.

-> Trap Dağılma Sistemi:
Trap canı sıfıra düştüğünde bölge tekrar satın alınabilir hale gelir.

-> Trap Yükseltme Sistemi:
Seviye arttıkça üye limiti, can değeri, banka limiti ve chunk limiti genişler.

-> Trap Banka Sistemi:
Trap üyeleri yetkilerine göre ortak bankaya para yatırabilir veya çekebilir.

-> Trap Pazarı Sistemi:
Satıştaki trapler GUI üzerinden listelenir ve diğer oyuncular tarafından satın alınabilir.

-> Pazar Filtreleme Sistemi:
Satıştaki trapler fiyat, seviye ve can değerine göre sıralanabilir.

-> Üye Yönetim Sistemi:
Trap sahibi üyeleri davet edebilir, çıkarabilir ve rollerini düzenleyebilir.

-> Rol Yetki Sistemi:
Sahip, yönetici, moderatör ve üye rolleri ayrı renk ve yetkilerle yönetilebilir.

-> Trap Davet Sistemi:
Oyunculara trap daveti gönderilebilir, davetler kabul veya reddedilebilir.

-> Trap Sohbet Sistemi:
Trap üyeleri kendi aralarında özel sohbet kanalını kullanabilir.

-> Trap Spawn Sistemi:
Her trap için özel spawn noktası ayarlanabilir.

-> Trap Ziyaret Sistemi:
Trap sahibi bölgesini ziyaretçilere açıp kapatabilir.

-> Trap PvP Ayar Sistemi:
Trap içinde PvP durumu isteğe göre aktif veya pasif yapılabilir.

-> Trap Fly Sistemi:
Oyuncular sadece kendi trap bölgelerinde uçuş kullanabilir.

-> Trap Sınır Gösterme Sistemi:
Oyuncular trap sınırlarını particle efekti ile görüntüleyebilir.

-> Actionbar Bilgi Sistemi:
Oyuncu trap içindeyken trap bilgileri actionbar üzerinden gösterilir.

-> Bossbar Can Sistemi:
Trap canı bossbar üzerinden gösterilir, can oranına göre renk değiştirir.

-> Trap Giriş/Çıkış Bildirimi:
Oyuncu trap alanına girince veya çıkınca title bildirimi gösterilir.

-> Görsel Efekt Sistemi:
Satın alma, yükseltme, hasar alma ve yetkisiz işlem durumlarında particle ve ses efektleri oynatılır.

-> Hologram Durum Sistemi:
Boş, sahipli ve satılık trapler hologram üzerinde farklı durum rengiyle gösterilir.

-> Aktivite Log Sistemi:
Satın alma, yükseltme, banka, üye, chunk ve ayar işlemleri kayıt altına alınır.

-> Aktivite Log GUI Sistemi:
Oyuncular trap geçmişini GUI üzerinden görüntüleyebilir.

-> Güvenilir Oyuncu Sistemi:
Üye olmayan oyunculara geçici veya kalıcı etkileşim izni verilebilir.

-> Admin Yönetim Sistemi:
Yetkililer oyun içinden trap oluşturabilir, silebilir ve düzenleyebilir.

-> Veritabanı Desteği:
SQLite ve MySQL desteği ile veriler güvenli şekilde saklanır.

-> PlaceholderAPI Desteği:
Trap bilgileri scoreboard, tab ve benzeri sistemlerde kullanılabilir.

-> Vault Desteği:
Ekonomi işlemleri Vault üzerinden yürütülür.

-> DecentHolograms Desteği:
Trap önlerinde bilgi hologramları gösterilebilir.

-> Mesaj Dosyası Sistemi:
Mesajlar ayrı messages.yml dosyası üzerinden düzenlenebilir.

-> Performans Odaklı Yapı:
Chunk kontrolleri optimize edilmiştir, veritabanı işlemleri async çalışır.

Komutlar:
/trap
/trap menu
/trap al
/trap bilgi
/trap liste
/trap pazar
/trap banka
/trap para yatır <miktar>
/trap para çek <miktar>
/trap yükselt
/trap satış <fiyat>
/trap satışiptal
/trap davet <oyuncu>
/trap kabul
/trap reddet
/trap at <oyuncu>
/trap rol <oyuncu> <rol>
/trap spawn
/trap setspawn
/trap ziyaret
/trap ziyaret <trap>
/trap sohbet
/trap pvp
/trap fly
/trap sınır
/trap sinir
/trap log
/trap kayıt
/trap kayit

Admin Komutları:

/qtrap
/qtrap reload
/qtrap create <id> <fiyat>
/qtrap delete <id>
/qtrap setprice <id> <fiyat>
/qtrap setowner <id> <oyuncu>
/qtrap addchunk <id>
/qtrap removechunk <id>
/qtrap sethealth <id> <miktar>
/qtrap setmaxhealth <id> <miktar>
/qtrap setlevel <id> <seviye>
/qtrap info <id>

Placeholderlar:

%qtrap_owner%
%qtrap_name%
%qtrap_level%
%qtrap_health%
%qtrap_max_health%
%qtrap_bank%
%qtrap_members%
%qtrap_max_members%
%qtrap_pvp%
%qtrap_visit%
%qtrap_price%


Bağımlılıklar:
Vault
PlaceholderAPI
DecentHolograms


Opsiyonel Destek:
LuckPerms
MySQL
SQLite


Loga Kaydedilen İşlemler:

Satın alma
Yükseltme
Banka para yatırma
Banka para çekme
Satışa çıkarma
Satış iptal
PvP açma/kapatma
Ziyaret açma/kapatma
Spawn ayarlama
Chunk ekleme
Chunk çıkarma
Üye katılması
Üye çıkarılması
Rol değiştirme
Trap hasarı
Trap dağılması