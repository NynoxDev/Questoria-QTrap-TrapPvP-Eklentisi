Questoria QTrap - TrapPvP Eklentisi​
Desteklenen Sürümler:
Paper / Purpur 1.21.x

Amacı:
QTrap, TrapPvP sunucuları için geliştirilmiş kapsamlı bir trap yönetim eklentisidir. Oyuncuların trap satın almasını, trap bölgelerini yönetmesini, üyeler eklemesini, trap yükseltmesini ve pazar sistemi üzerinden trap ticareti yapmasını sağlar. Sistem; performans, özelleştirilebilirlik ve yönetim kolaylığı düşünülerek hazırlanmıştır.
Özellikleri:

-> Trap Satın Alma Sistemi:
Oyuncular belirlenen trap bölgelerini ekonomi sistemi üzerinden satın alabilir.

-> Chunk Tabanlı Trap Sistemi:
Trap bölgeleri chunk mantığıyla çalışır ve birden fazla chunk desteği sunar.

-> Otomatik Koruma Sistemi:
Blok kırma, blok koyma, sandık açma ve etkileşim izinleri otomatik olarak yönetilir.

-> GUI Yönetim Sistemi:
Trap bilgileri, üyeler, banka, yükseltme ve pazar işlemleri menü üzerinden kontrol edilir.

-> Trap Can Sistemi:
Her trap için özel can değeri bulunur ve config üzerinden düzenlenebilir.

-> Trap Dağılma Sistemi:
Trap canı sıfıra düştüğünde bölge otomatik olarak tekrar satın alınabilir hale gelir.

-> Trap Yükseltme Sistemi:
Seviye arttıkça üye limiti, can değeri, banka limiti ve chunk limiti genişler.

-> Trap Banka Sistemi:
Trap üyeleri yetkilerine göre ortak bankaya para yatırabilir veya çekebilir.

-> Trap Market Sistemi:
Oyuncular sahip oldukları trapleri satışa çıkarabilir.

-> Trap Pazarı Sistemi:
Satıştaki trapler GUI üzerinden listelenir ve diğer oyuncular tarafından satın alınabilir.

-> Üye Yönetim Sistemi:
Trap sahibi üyeleri davet edebilir, çıkarabilir ve rollerini düzenleyebilir.

-> Rol Yetki Sistemi:
Sahip, yönetici, moderatör ve üye rolleri için ayrı izinler tanımlanabilir.

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

-> Güvenilir Oyuncu Sistemi:
Üye olmayan oyunculara geçici veya kalıcı etkileşim izni verilebilir.

-> Admin Yönetim Sistemi:
Yetkililer oyun içinden trap oluşturabilir, silebilir ve düzenleyebilir.

-> Veritabanı Desteği:
SQLite ve MySQL desteği ile veriler güvenli şekilde saklanır.

-> PlaceholderAPI Desteği:
Trap bilgileri scoreboard, tab ve benzeri sistemlerde kullanılabilir.

-> Vault Desteği:
Ekonomi işlemleri Vault üzerinden sorunsuz şekilde yürütülür.

-> DecentHolograms Desteği:
Trap önlerinde bilgi hologramları gösterilebilir.

-> Performans Odaklı Yapı:
Chunk kontrolleri optimize edilmiştir, veritabanı işlemleri async çalışır.

Komutlar:
/trap
/trap al
/trap menu
/trap bilgi
/trap liste
/trap davet <oyuncu>
/trap kabul
/trap reddet
/trap at <oyuncu>
/trap rol <oyuncu> <rol>
/trap banka
/trap para yatır <miktar>
/trap para çek <miktar>
/trap yükselt
/trap satış <fiyat>
/trap satışiptal
/trap pazar
/trap spawn
/trap setspawn
/trap ziyaret <trap>
/trap sohbet
/trap pvp
/trap fly

Admin Komutları:

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