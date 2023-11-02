package DateTime::Locale::Catalog;

use strict;
use warnings;

our $VERSION = '1.39';

1;

# ABSTRACT: Provides a catalog of all valid locale names

__END__

=pod

=encoding UTF-8

=head1 NAME

DateTime::Locale::Catalog - Provides a catalog of all valid locale names

=head1 VERSION

version 1.39

=head1 DESCRIPTION

This module contains a list of all known locales.

=head1 LOCALES

Any method taking locale code or name arguments should use one of the values
listed below. Codes and names are case sensitive. The code starts with the
ISO639-1 language code, and may also include information identifying any or all
of territory, script, or variant.

Always select the closest matching locale - for example, French Canadians would
choose C<fr-CA> over fr - and B<always> use locale codes in preference to
names; locale codes offer greater compatibility when using localized third
party modules.

The available locales are:

=begin :locales



=end :locales

 Locale code      Locale name (in English)                          Native locale name
 ================================================================================================================
 aa               Afar
 aa-DJ            Afar Djibouti
 aa-ER            Afar Eritrea
 aa-ET            Afar Ethiopia
 ab               Abkhazian                                         Аԥсшәа
 ab-GE            Abkhazian Georgia                                 Аԥсшәа
 af               Afrikaans                                         Afrikaans
 af-NA            Afrikaans Namibia                                 Afrikaans Namibië
 af-ZA            Afrikaans South Africa                            Afrikaans Suid-Afrika
 agq              Aghem                                             Aghem
 agq-CM           Aghem Cameroon                                    Aghem Kàmàlûŋ
 ak               Akan                                              Akan
 ak-GH            Akan Ghana                                        Akan Gaana
 am               Amharic                                           አማርኛ
 am-ET            Amharic Ethiopia                                  አማርኛ ኢትዮጵያ
 an               Aragonese                                         aragonés
 an-ES            Aragonese Spain                                   aragonés
 ann              Obolo                                             Obolo
 ann-NG           Obolo Nigeria                                     Obolo
 apc-SY           Syria                                             العامية
 ar               Arabic                                            العربية
 ar-001           Arabic world                                      العربية العالم
 ar-AE            Arabic United Arab Emirates                       العربية الإمارات العربية المتحدة
 ar-BH            Arabic Bahrain                                    العربية البحرين
 ar-DJ            Arabic Djibouti                                   العربية جيبوتي
 ar-DZ            Arabic Algeria                                    العربية الجزائر
 ar-EG            Arabic Egypt                                      العربية مصر
 ar-EH            Arabic Western Sahara                             العربية الصحراء الغربية
 ar-ER            Arabic Eritrea                                    العربية إريتريا
 ar-IL            Arabic Israel                                     العربية إسرائيل
 ar-IQ            Arabic Iraq                                       العربية العراق
 ar-JO            Arabic Jordan                                     العربية الأردن
 ar-KM            Arabic Comoros                                    العربية جزر القمر
 ar-KW            Arabic Kuwait                                     العربية الكويت
 ar-LB            Arabic Lebanon                                    العربية لبنان
 ar-LY            Arabic Libya                                      العربية ليبيا
 ar-MA            Arabic Morocco                                    العربية المغرب
 ar-MR            Arabic Mauritania                                 العربية موريتانيا
 ar-OM            Arabic Oman                                       العربية عُمان
 ar-PS            Arabic Palestinian Territories                    العربية الأراضي الفلسطينية
 ar-QA            Arabic Qatar                                      العربية قطر
 ar-SA            Arabic Saudi Arabia                               العربية المملكة العربية السعودية
 ar-SD            Arabic Sudan                                      العربية السودان
 ar-SO            Arabic Somalia                                    العربية الصومال
 ar-SS            Arabic South Sudan                                العربية جنوب السودان
 ar-SY            Arabic Syria                                      العربية سوريا
 ar-TD            Arabic Chad                                       العربية تشاد
 ar-TN            Arabic Tunisia                                    العربية تونس
 ar-YE            Arabic Yemen                                      العربية اليمن
 arn              Mapuche                                           Mapudungun
 arn-CL           Mapuche Chile                                     Mapudungun
 as               Assamese                                          অসমীয়া
 as-IN            Assamese India                                    অসমীয়া ভাৰত
 asa              Asu                                               Kipare
 asa-TZ           Asu Tanzania                                      Kipare Tadhania
 ast              Asturian                                          asturianu
 ast-ES           Asturian Spain                                    asturianu España
 az               Azerbaijani                                       azərbaycan
 az-Arab          Azerbaijani Arabic                                آذربایجان تۆرکجه‌سی عربجه
 az-Arab-IQ       Azerbaijani Iraq Arabic                           آذربایجان تۆرکجه‌سی عربجه
 az-Arab-IR       Azerbaijani Iran Arabic                           آذربایجان تۆرکجه‌سی عربجه
 az-Arab-TR       Azerbaijani Türkiye Arabic                        آذربایجان تۆرکجه‌سی عربجه
 az-Cyrl          Azerbaijani Cyrillic                              азәрбајҹан Кирил
 az-Cyrl-AZ       Azerbaijani Azerbaijan Cyrillic                   азәрбајҹан Азәрбајҹан Кирил
 az-Latn          Azerbaijani Latin                                 azərbaycan latın
 az-Latn-AZ       Azerbaijani Azerbaijan Latin                      azərbaycan Azərbaycan latın
 ba               Bashkir                                           башҡорт теле
 ba-RU            Bashkir Russia                                    башҡорт теле
 bal              Baluchi                                           بلۆچی
 bal-Arab         Baluchi Arabic                                    بلۆچی
 bal-Arab-PK      Baluchi Pakistan Arabic                           بلۆچی
 bal-Latn         Baluchi Latin                                     بلۆچی
 bal-Latn-PK      Baluchi Pakistan Latin                            بلۆچی
 bas              Basaa                                             Ɓàsàa
 bas-CM           Basaa Cameroon                                    Ɓàsàa Kàmɛ̀rûn
 be               Belarusian                                        беларуская
 be-BY            Belarusian Belarus                                беларуская Беларусь
 be-tarask        Belarusian Taraskievica orthography               беларуская
 bem              Bemba                                             Ichibemba
 bem-ZM           Bemba Zambia                                      Ichibemba Zambia
 bez              Bena                                              Hibena
 bez-TZ           Bena Tanzania                                     Hibena Hutanzania
 bg               Bulgarian                                         български
 bg-BG            Bulgarian Bulgaria                                български България
 bgc              Haryanvi                                          हरियाणवी
 bgc-IN           Haryanvi India                                    हरियाणवी भारत
 bgn              Western Balochi                                   بلوچی (رخشانی)
 bgn-AE           Western Balochi United Arab Emirates              بلوچی (رخشانی) متحدین عربین امارات
 bgn-AF           Western Balochi Afghanistan                       بلوچی (رخشانی)
 bgn-IR           Western Balochi Iran                              بلوچی (رخشانی)
 bgn-OM           Western Balochi Oman                              بلوچی (رخشانی) ئومان
 bgn-PK           Western Balochi Pakistan                          بلوچی (رخشانی)
 bho              Bhojpuri                                          भोजपुरी
 bho-IN           Bhojpuri India                                    भोजपुरी भारत
 blt              Tai Dam                                           ꪼꪕꪒꪾ
 blt-VN           Tai Dam Vietnam                                   ꪼꪕꪒꪾ
 bm               Bambara                                           bamanakan
 bm-ML            Bambara Mali                                      bamanakan Mali
 bm-Nkoo          Bambara N’Ko
 bm-Nkoo-ML       Bambara Mali N’Ko
 bn               Bangla                                            বাংলা
 bn-BD            Bangla Bangladesh                                 বাংলা বাংলাদেশ
 bn-IN            Bangla India                                      বাংলা ভারত
 bo               Tibetan                                           བོད་སྐད་
 bo-CN            Tibetan China                                     བོད་སྐད་ རྒྱ་ནག
 bo-IN            Tibetan India                                     བོད་སྐད་ རྒྱ་གར་
 br               Breton                                            brezhoneg
 br-FR            Breton France                                     brezhoneg Frañs
 brx              Bodo                                              बर’
 brx-IN           Bodo India                                        बर’ भारत
 bs               Bosnian                                           bosanski
 bs-Cyrl          Bosnian Cyrillic                                  босански ћирилица
 bs-Cyrl-BA       Bosnian Bosnia & Herzegovina Cyrillic             босански Босна и Херцеговина ћирилица
 bs-Latn          Bosnian Latin                                     bosanski latinica
 bs-Latn-BA       Bosnian Bosnia & Herzegovina Latin                bosanski Bosna i Hercegovina latinica
 bss              Akoose                                            Akoose
 bss-CM           Akoose Cameroon                                   Akoose Kamerûn
 byn              Blin
 byn-ER           Blin Eritrea
 ca               Catalan                                           català
 ca-AD            Catalan Andorra                                   català Andorra
 ca-ES            Catalan Spain                                     català Espanya
 ca-ES-valencia   Catalan Spain Valencian                           català Espanya valencià
 ca-FR            Catalan France                                    català França
 ca-IT            Catalan Italy                                     català Itàlia
 cad              Caddo
 cad-US           Caddo United States
 cch              Atsam                                             Atsam
 cch-NG           Atsam Nigeria                                     Atsam
 ccp              Chakma                                            𑄌𑄋𑄴𑄟𑄳𑄦
 ccp-BD           Chakma Bangladesh                                 𑄌𑄋𑄴𑄟𑄳𑄦 𑄝𑄁𑄣𑄘𑄬𑄌𑄴
 ccp-IN           Chakma India                                      𑄌𑄋𑄴𑄟𑄳𑄦 𑄞𑄢𑄧𑄖𑄴
 ce               Chechen                                           нохчийн
 ce-RU            Chechen Russia                                    нохчийн Росси
 ceb              Cebuano                                           Cebuano
 ceb-PH           Cebuano Philippines                               Cebuano Pilipinas
 cgg              Chiga                                             Rukiga
 cgg-UG           Chiga Uganda                                      Rukiga Uganda
 cho              Choctaw                                           Chahta
 cho-US           Choctaw United States                             Chahta United States
 chr              Cherokee                                          ᏣᎳᎩ
 chr-US           Cherokee United States                            ᏣᎳᎩ ᏌᏊ ᎢᏳᎾᎵᏍᏔᏅ ᏍᎦᏚᎩ
 cic              Chickasaw                                         Chikashshanompaʼ
 cic-US           Chickasaw United States                           Chikashshanompaʼ United States
 ckb              Central Kurdish                                   کوردیی ناوەندی
 ckb-IQ           Central Kurdish Iraq                              کوردیی ناوەندی عێراق
 ckb-IR           Central Kurdish Iran                              کوردیی ناوەندی ئێران
 co               Corsican                                          corsu
 co-FR            Corsican France                                   corsu
 cs               Czech                                             čeština
 cs-CZ            Czech Czechia                                     čeština Česko
 cu               Church Slavic
 cu-RU            Church Slavic Russia
 cv               Chuvash                                           чӑваш
 cv-RU            Chuvash Russia                                    чӑваш Раҫҫей
 cy               Welsh                                             Cymraeg
 cy-GB            Welsh United Kingdom                              Cymraeg Y Deyrnas Unedig
 da               Danish                                            dansk
 da-DK            Danish Denmark                                    dansk Danmark
 da-GL            Danish Greenland                                  dansk Grønland
 dav              Taita                                             Kitaita
 dav-KE           Taita Kenya                                       Kitaita Kenya
 de               German                                            Deutsch
 de-AT            German Austria                                    Deutsch Österreich
 de-BE            German Belgium                                    Deutsch Belgien
 de-CH            German Switzerland                                Deutsch Schweiz
 de-DE            German Germany                                    Deutsch Deutschland
 de-IT            German Italy                                      Deutsch Italien
 de-LI            German Liechtenstein                              Deutsch Liechtenstein
 de-LU            German Luxembourg                                 Deutsch Luxemburg
 dje              Zarma                                             Zarmaciine
 dje-NE           Zarma Niger                                       Zarmaciine Nižer
 doi              Dogri                                             डोगरी
 doi-IN           Dogri India                                       डोगरी भारत
 dsb              Lower Sorbian                                     dolnoserbšćina
 dsb-DE           Lower Sorbian Germany                             dolnoserbšćina Nimska
 dua              Duala                                             duálá
 dua-CM           Duala Cameroon                                    duálá Cameroun
 dv               Divehi
 dv-MV            Divehi Maldives
 dyo              Jola-Fonyi                                        joola
 dyo-SN           Jola-Fonyi Senegal                                joola Senegal
 dz               Dzongkha                                          རྫོང་ཁ
 dz-BT            Dzongkha Bhutan                                   རྫོང་ཁ འབྲུག
 ebu              Embu                                              Kĩembu
 ebu-KE           Embu Kenya                                        Kĩembu Kenya
 ee               Ewe                                               Eʋegbe
 ee-GH            Ewe Ghana                                         Eʋegbe Ghana nutome
 ee-TG            Ewe Togo                                          Eʋegbe Togo nutome
 el               Greek                                             Ελληνικά
 el-CY            Greek Cyprus                                      Ελληνικά Κύπρος
 el-GR            Greek Greece                                      Ελληνικά Ελλάδα
 el-polyton       Greek Polytonic                                   Ἑλληνικά Πολυτονικό
 en               English                                           English
 en-001           English world                                     English world
 en-150           English Europe                                    English Europe
 en-AE            English United Arab Emirates                      English United Arab Emirates
 en-AG            English Antigua & Barbuda                         English Antigua & Barbuda
 en-AI            English Anguilla                                  English Anguilla
 en-AS            English American Samoa                            English American Samoa
 en-AT            English Austria                                   English Austria
 en-AU            English Australia                                 English Australia
 en-BB            English Barbados                                  English Barbados
 en-BE            English Belgium                                   English Belgium
 en-BI            English Burundi                                   English Burundi
 en-BM            English Bermuda                                   English Bermuda
 en-BS            English Bahamas                                   English Bahamas
 en-BW            English Botswana                                  English Botswana
 en-BZ            English Belize                                    English Belize
 en-CA            English Canada                                    English Canada
 en-CC            English Cocos (Keeling) Islands                   English Cocos (Keeling) Islands
 en-CH            English Switzerland                               English Switzerland
 en-CK            English Cook Islands                              English Cook Islands
 en-CM            English Cameroon                                  English Cameroon
 en-CX            English Christmas Island                          English Christmas Island
 en-CY            English Cyprus                                    English Cyprus
 en-DE            English Germany                                   English Germany
 en-DG            English Diego Garcia                              English Diego Garcia
 en-DK            English Denmark                                   English Denmark
 en-DM            English Dominica                                  English Dominica
 en-Dsrt          English Deseret                                   𐐀𐑍𐑊𐐮𐑇 𐐔𐐯𐑆𐐲𐑉𐐯𐐻
 en-Dsrt-US       English United States Deseret                     𐐀𐑍𐑊𐐮𐑇 𐐏𐐭𐑌𐐴𐐻𐐲𐐼 𐐝𐐻𐐩𐐻𐑅 𐐔𐐯𐑆𐐲𐑉𐐯𐐻
 en-ER            English Eritrea                                   English Eritrea
 en-FI            English Finland                                   English Finland
 en-FJ            English Fiji                                      English Fiji
 en-FK            English Falkland Islands                          English Falkland Islands
 en-FM            English Micronesia                                English Micronesia
 en-GB            English United Kingdom                            English United Kingdom
 en-GD            English Grenada                                   English Grenada
 en-GG            English Guernsey                                  English Guernsey
 en-GH            English Ghana                                     English Ghana
 en-GI            English Gibraltar                                 English Gibraltar
 en-GM            English Gambia                                    English Gambia
 en-GU            English Guam                                      English Guam
 en-GY            English Guyana                                    English Guyana
 en-HK            English Hong Kong SAR China                       English Hong Kong SAR China
 en-IE            English Ireland                                   English Ireland
 en-IL            English Israel                                    English Israel
 en-IM            English Isle of Man                               English Isle of Man
 en-IN            English India                                     English India
 en-IO            English British Indian Ocean Territory            English British Indian Ocean Territory
 en-JE            English Jersey                                    English Jersey
 en-JM            English Jamaica                                   English Jamaica
 en-KE            English Kenya                                     English Kenya
 en-KI            English Kiribati                                  English Kiribati
 en-KN            English St. Kitts & Nevis                         English St Kitts & Nevis
 en-KY            English Cayman Islands                            English Cayman Islands
 en-LC            English St. Lucia                                 English St Lucia
 en-LR            English Liberia                                   English Liberia
 en-LS            English Lesotho                                   English Lesotho
 en-MG            English Madagascar                                English Madagascar
 en-MH            English Marshall Islands                          English Marshall Islands
 en-MO            English Macao SAR China                           English Macao SAR China
 en-MP            English Northern Mariana Islands                  English Northern Mariana Islands
 en-MS            English Montserrat                                English Montserrat
 en-MT            English Malta                                     English Malta
 en-MU            English Mauritius                                 English Mauritius
 en-MV            English Maldives                                  English Maldives
 en-MW            English Malawi                                    English Malawi
 en-MY            English Malaysia                                  English Malaysia
 en-NA            English Namibia                                   English Namibia
 en-NF            English Norfolk Island                            English Norfolk Island
 en-NG            English Nigeria                                   English Nigeria
 en-NL            English Netherlands                               English Netherlands
 en-NR            English Nauru                                     English Nauru
 en-NU            English Niue                                      English Niue
 en-NZ            English New Zealand                               English New Zealand
 en-PG            English Papua New Guinea                          English Papua New Guinea
 en-PH            English Philippines                               English Philippines
 en-PK            English Pakistan                                  English Pakistan
 en-PN            English Pitcairn Islands                          English Pitcairn Islands
 en-PR            English Puerto Rico                               English Puerto Rico
 en-PW            English Palau                                     English Palau
 en-RW            English Rwanda                                    English Rwanda
 en-SB            English Solomon Islands                           English Solomon Islands
 en-SC            English Seychelles                                English Seychelles
 en-SD            English Sudan                                     English Sudan
 en-SE            English Sweden                                    English Sweden
 en-SG            English Singapore                                 English Singapore
 en-SH            English St. Helena                                English St Helena
 en-SI            English Slovenia                                  English Slovenia
 en-SL            English Sierra Leone                              English Sierra Leone
 en-SS            English South Sudan                               English South Sudan
 en-SX            English Sint Maarten                              English Sint Maarten
 en-SZ            English Eswatini                                  English Eswatini
 en-Shaw          English Shavian
 en-Shaw-GB       English United Kingdom Shavian
 en-TC            English Turks & Caicos Islands                    English Turks & Caicos Islands
 en-TK            English Tokelau                                   English Tokelau
 en-TO            English Tonga                                     English Tonga
 en-TT            English Trinidad & Tobago                         English Trinidad & Tobago
 en-TV            English Tuvalu                                    English Tuvalu
 en-TZ            English Tanzania                                  English Tanzania
 en-UG            English Uganda                                    English Uganda
 en-UM            English U.S. Outlying Islands                     English U.S. Outlying Islands
 en-US            English United States                             English United States
 en-VC            English St. Vincent & Grenadines                  English St Vincent & the Grenadines
 en-VG            English British Virgin Islands                    English British Virgin Islands
 en-VI            English U.S. Virgin Islands                       English U.S. Virgin Islands
 en-VU            English Vanuatu                                   English Vanuatu
 en-WS            English Samoa                                     English Samoa
 en-ZA            English South Africa                              English South Africa
 en-ZM            English Zambia                                    English Zambia
 en-ZW            English Zimbabwe                                  English Zimbabwe
 eo               Esperanto                                         esperanto
 eo-001           Esperanto world                                   esperanto Mondo
 es               Spanish                                           español
 es-419           Spanish Latin America                             español Latinoamérica
 es-AR            Spanish Argentina                                 español Argentina
 es-BO            Spanish Bolivia                                   español Bolivia
 es-BR            Spanish Brazil                                    español Brasil
 es-BZ            Spanish Belize                                    español Belice
 es-CL            Spanish Chile                                     español Chile
 es-CO            Spanish Colombia                                  español Colombia
 es-CR            Spanish Costa Rica                                español Costa Rica
 es-CU            Spanish Cuba                                      español Cuba
 es-DO            Spanish Dominican Republic                        español República Dominicana
 es-EA            Spanish Ceuta & Melilla                           español Ceuta y Melilla
 es-EC            Spanish Ecuador                                   español Ecuador
 es-ES            Spanish Spain                                     español España
 es-GQ            Spanish Equatorial Guinea                         español Guinea Ecuatorial
 es-GT            Spanish Guatemala                                 español Guatemala
 es-HN            Spanish Honduras                                  español Honduras
 es-IC            Spanish Canary Islands                            español Canarias
 es-MX            Spanish Mexico                                    español México
 es-NI            Spanish Nicaragua                                 español Nicaragua
 es-PA            Spanish Panama                                    español Panamá
 es-PE            Spanish Peru                                      español Perú
 es-PH            Spanish Philippines                               español Filipinas
 es-PR            Spanish Puerto Rico                               español Puerto Rico
 es-PY            Spanish Paraguay                                  español Paraguay
 es-SV            Spanish El Salvador                               español El Salvador
 es-US            Spanish United States                             español Estados Unidos
 es-UY            Spanish Uruguay                                   español Uruguay
 es-VE            Spanish Venezuela                                 español Venezuela
 et               Estonian                                          eesti
 et-EE            Estonian Estonia                                  eesti Eesti
 eu               Basque                                            euskara
 eu-ES            Basque Spain                                      euskara Espainia
 ewo              Ewondo                                            ewondo
 ewo-CM           Ewondo Cameroon                                   ewondo Kamərún
 fa               Persian                                           فارسی
 fa-AF            Persian Afghanistan                               فارسی افغانستان
 fa-IR            Persian Iran                                      فارسی ایران
 ff               Fula                                              Pulaar
 ff-Adlm          Fula Adlam                                        𞤆𞤵𞤤𞤢𞤪 𞤀𞤁𞤂𞤢𞤃
 ff-Adlm-BF       Fula Burkina Faso Adlam                           𞤆𞤵𞤤𞤢𞤪 𞤄𞤵𞤪𞤳𞤭𞤲𞤢 𞤊𞤢𞤧𞤮𞥅 𞤀𞤁𞤂𞤢𞤃
 ff-Adlm-CM       Fula Cameroon Adlam                               𞤆𞤵𞤤𞤢𞤪 𞤑𞤢𞤥𞤢𞤪𞤵𞥅𞤲 𞤀𞤁𞤂𞤢𞤃
 ff-Adlm-GH       Fula Ghana Adlam                                  𞤆𞤵𞤤𞤢𞤪 𞤘𞤢𞤲𞤢 𞤀𞤁𞤂𞤢𞤃
 ff-Adlm-GM       Fula Gambia Adlam                                 𞤆𞤵𞤤𞤢𞤪 𞤘𞤢𞤥𞤦𞤭𞤴𞤢 𞤀𞤁𞤂𞤢𞤃
 ff-Adlm-GN       Fula Guinea Adlam                                 𞤆𞤵𞤤𞤢𞤪 𞤘𞤭𞤲𞤫 𞤀𞤁𞤂𞤢𞤃
 ff-Adlm-GW       Fula Guinea-Bissau Adlam                          𞤆𞤵𞤤𞤢𞤪 𞤘𞤭𞤲𞤫-𞤄𞤭𞤧𞤢𞤱𞤮𞥅 𞤀𞤁𞤂𞤢𞤃
 ff-Adlm-LR       Fula Liberia Adlam                                𞤆𞤵𞤤𞤢𞤪 𞤂𞤢𞤦𞤭𞤪𞤭𞤴𞤢𞥄 𞤀𞤁𞤂𞤢𞤃
 ff-Adlm-MR       Fula Mauritania Adlam                             𞤆𞤵𞤤𞤢𞤪 𞤃𞤮𞤪𞤼𞤢𞤲𞤭𞥅 𞤀𞤁𞤂𞤢𞤃
 ff-Adlm-NE       Fula Niger Adlam                                  𞤆𞤵𞤤𞤢𞤪 𞤐𞤭𞥅𞤶𞤫𞤪 𞤀𞤁𞤂𞤢𞤃
 ff-Adlm-NG       Fula Nigeria Adlam                                𞤆𞤵𞤤𞤢𞤪 𞤐𞤢𞤶𞤫𞤪𞤭𞤴𞤢𞥄 𞤀𞤁𞤂𞤢𞤃
 ff-Adlm-SL       Fula Sierra Leone Adlam                           𞤆𞤵𞤤𞤢𞤪 𞤅𞤢𞤪𞤢𞤤𞤮𞤲 𞤀𞤁𞤂𞤢𞤃
 ff-Adlm-SN       Fula Senegal Adlam                                𞤆𞤵𞤤𞤢𞤪 𞤅𞤫𞤲𞤫𞤺𞤢𞥄𞤤 𞤀𞤁𞤂𞤢𞤃
 ff-Latn          Fula Latin                                        Pulaar
 ff-Latn-BF       Fula Burkina Faso Latin                           Pulaar Burkibaa Faaso
 ff-Latn-CM       Fula Cameroon Latin                               Pulaar Kameruun
 ff-Latn-GH       Fula Ghana Latin                                  Pulaar Ganaa
 ff-Latn-GM       Fula Gambia Latin                                 Pulaar Gammbi
 ff-Latn-GN       Fula Guinea Latin                                 Pulaar Gine
 ff-Latn-GW       Fula Guinea-Bissau Latin                          Pulaar Gine-Bisaawo
 ff-Latn-LR       Fula Liberia Latin                                Pulaar Liberiyaa
 ff-Latn-MR       Fula Mauritania Latin                             Pulaar Muritani
 ff-Latn-NE       Fula Niger Latin                                  Pulaar Nijeer
 ff-Latn-NG       Fula Nigeria Latin                                Pulaar Nijeriyaa
 ff-Latn-SL       Fula Sierra Leone Latin                           Pulaar Seraa liyon
 ff-Latn-SN       Fula Senegal Latin                                Pulaar Senegaal
 fi               Finnish                                           suomi
 fi-FI            Finnish Finland                                   suomi Suomi
 fil              Filipino                                          Filipino
 fil-PH           Filipino Philippines                              Filipino Pilipinas
 fo               Faroese                                           føroyskt
 fo-DK            Faroese Denmark                                   føroyskt Danmark
 fo-FO            Faroese Faroe Islands                             føroyskt Føroyar
 fr               French                                            français
 fr-BE            French Belgium                                    français Belgique
 fr-BF            French Burkina Faso                               français Burkina Faso
 fr-BI            French Burundi                                    français Burundi
 fr-BJ            French Benin                                      français Bénin
 fr-BL            French St. Barthélemy                             français Saint-Barthélemy
 fr-CA            French Canada                                     français Canada
 fr-CD            French Congo - Kinshasa                           français Congo-Kinshasa
 fr-CF            French Central African Republic                   français République centrafricaine
 fr-CG            French Congo - Brazzaville                        français Congo-Brazzaville
 fr-CH            French Switzerland                                français Suisse
 fr-CI            French Côte d’Ivoire                              français Côte d’Ivoire
 fr-CM            French Cameroon                                   français Cameroun
 fr-DJ            French Djibouti                                   français Djibouti
 fr-DZ            French Algeria                                    français Algérie
 fr-FR            French France                                     français France
 fr-GA            French Gabon                                      français Gabon
 fr-GF            French French Guiana                              français Guyane française
 fr-GN            French Guinea                                     français Guinée
 fr-GP            French Guadeloupe                                 français Guadeloupe
 fr-GQ            French Equatorial Guinea                          français Guinée équatoriale
 fr-HT            French Haiti                                      français Haïti
 fr-KM            French Comoros                                    français Comores
 fr-LU            French Luxembourg                                 français Luxembourg
 fr-MA            French Morocco                                    français Maroc
 fr-MC            French Monaco                                     français Monaco
 fr-MF            French St. Martin                                 français Saint-Martin
 fr-MG            French Madagascar                                 français Madagascar
 fr-ML            French Mali                                       français Mali
 fr-MQ            French Martinique                                 français Martinique
 fr-MR            French Mauritania                                 français Mauritanie
 fr-MU            French Mauritius                                  français Maurice
 fr-NC            French New Caledonia                              français Nouvelle-Calédonie
 fr-NE            French Niger                                      français Niger
 fr-PF            French French Polynesia                           français Polynésie française
 fr-PM            French St. Pierre & Miquelon                      français Saint-Pierre-et-Miquelon
 fr-RE            French Réunion                                    français La Réunion
 fr-RW            French Rwanda                                     français Rwanda
 fr-SC            French Seychelles                                 français Seychelles
 fr-SN            French Senegal                                    français Sénégal
 fr-SY            French Syria                                      français Syrie
 fr-TD            French Chad                                       français Tchad
 fr-TG            French Togo                                       français Togo
 fr-TN            French Tunisia                                    français Tunisie
 fr-VU            French Vanuatu                                    français Vanuatu
 fr-WF            French Wallis & Futuna                            français Wallis-et-Futuna
 fr-YT            French Mayotte                                    français Mayotte
 frr              Northern Frisian                                  Nordfriisk
 frr-DE           Northern Frisian Germany                          Nordfriisk
 fur              Friulian                                          furlan
 fur-IT           Friulian Italy                                    furlan Italie
 fy               Western Frisian                                   Frysk
 fy-NL            Western Frisian Netherlands                       Frysk Nederlân
 ga               Irish                                             Gaeilge
 ga-GB            Irish United Kingdom                              Gaeilge an Ríocht Aontaithe
 ga-IE            Irish Ireland                                     Gaeilge Éire
 gaa              Ga                                                Gã
 gaa-GH           Ga Ghana                                          Gã
 gd               Scottish Gaelic                                   Gàidhlig
 gd-GB            Scottish Gaelic United Kingdom                    Gàidhlig An Rìoghachd Aonaichte
 gez              Geez
 gez-ER           Geez Eritrea
 gez-ET           Geez Ethiopia
 gl               Galician                                          galego
 gl-ES            Galician Spain                                    galego España
 gn               Guarani                                           avañe’ẽ
 gn-PY            Guarani Paraguay                                  avañe’ẽ
 gsw              Swiss German                                      Schwiizertüütsch
 gsw-CH           Swiss German Switzerland                          Schwiizertüütsch Schwiiz
 gsw-FR           Swiss German France                               Schwiizertüütsch Frankriich
 gsw-LI           Swiss German Liechtenstein                        Schwiizertüütsch Liächteschtäi
 gu               Gujarati                                          ગુજરાતી
 gu-IN            Gujarati India                                    ગુજરાતી ભારત
 guz              Gusii                                             Ekegusii
 guz-KE           Gusii Kenya                                       Ekegusii Kenya
 gv               Manx                                              Gaelg
 gv-IM            Manx Isle of Man                                  Gaelg Ellan Vannin
 ha               Hausa                                             Hausa
 ha-Arab          Hausa Arabic
 ha-Arab-NG       Hausa Nigeria Arabic
 ha-Arab-SD       Hausa Sudan Arabic
 ha-GH            Hausa Ghana                                       Hausa Gana
 ha-NE            Hausa Niger                                       Hausa Nijar
 ha-NG            Hausa Nigeria                                     Hausa Nijeriya
 haw              Hawaiian                                          ʻŌlelo Hawaiʻi
 haw-US           Hawaiian United States                            ʻŌlelo Hawaiʻi ʻAmelika Hui Pū ʻIa
 he               Hebrew                                            עברית
 he-IL            Hebrew Israel                                     עברית ישראל
 hi               Hindi                                             हिन्दी
 hi-IN            Hindi India                                       हिन्दी भारत
 hi-Latn          Hindi Latin                                       Hindi Latin
 hi-Latn-IN       Hindi India Latin                                 Hindi India Latin
 hnj              Hmong Njua                                        𞄀𞄄𞄰𞄩𞄍𞄜𞄰
 hnj-Hmnp         Hmong Njua Nyiakeng Puachue Hmong                 𞄀𞄄𞄰𞄩𞄍𞄜𞄰
 hnj-Hmnp-US      Hmong Njua United States Nyiakeng Puachue Hmong   𞄀𞄄𞄰𞄩𞄍𞄜𞄰 𞄒𞄫𞄱𞄔𞄩𞄴
 hr               Croatian                                          hrvatski
 hr-BA            Croatian Bosnia & Herzegovina                     hrvatski Bosna i Hercegovina
 hr-HR            Croatian Croatia                                  hrvatski Hrvatska
 hsb              Upper Sorbian                                     hornjoserbšćina
 hsb-DE           Upper Sorbian Germany                             hornjoserbšćina Němska
 hu               Hungarian                                         magyar
 hu-HU            Hungarian Hungary                                 magyar Magyarország
 hy               Armenian                                          հայերեն
 hy-AM            Armenian Armenia                                  հայերեն Հայաստան
 ia               Interlingua                                       interlingua
 ia-001           Interlingua world                                 interlingua Mundo
 id               Indonesian                                        Indonesia
 id-ID            Indonesian Indonesia                              Indonesia Indonesia
 ig               Igbo                                              Igbo
 ig-NG            Igbo Nigeria                                      Igbo Naịjịrịa
 ii               Sichuan Yi                                        ꆈꌠꉙ
 ii-CN            Sichuan Yi China                                  ꆈꌠꉙ ꍏꇩ
 io               Ido                                               Ido
 io-001           Ido world                                         Ido
 is               Icelandic                                         íslenska
 is-IS            Icelandic Iceland                                 íslenska Ísland
 it               Italian                                           italiano
 it-CH            Italian Switzerland                               italiano Svizzera
 it-IT            Italian Italy                                     italiano Italia
 it-SM            Italian San Marino                                italiano San Marino
 it-VA            Italian Vatican City                              italiano Città del Vaticano
 iu               Inuktitut
 iu-CA            Inuktitut Canada
 iu-Latn          Inuktitut Latin
 iu-Latn-CA       Inuktitut Canada Latin
 ja               Japanese                                          日本語
 ja-JP            Japanese Japan                                    日本語 日本
 jbo              Lojban                                            la .lojban.
 jbo-001          Lojban world                                      la .lojban.
 jgo              Ngomba                                            Ndaꞌa
 jgo-CM           Ngomba Cameroon                                   Ndaꞌa Kamɛlûn
 jmc              Machame                                           Kimachame
 jmc-TZ           Machame Tanzania                                  Kimachame Tanzania
 jv               Javanese                                          Jawa
 jv-ID            Javanese Indonesia                                Jawa Indonésia
 ka               Georgian                                          ქართული
 ka-GE            Georgian Georgia                                  ქართული საქართველო
 kab              Kabyle                                            Taqbaylit
 kab-DZ           Kabyle Algeria                                    Taqbaylit Lezzayer
 kaj              Jju                                               Kaje
 kaj-NG           Jju Nigeria                                       Kaje
 kam              Kamba                                             Kikamba
 kam-KE           Kamba Kenya                                       Kikamba Kenya
 kcg              Tyap                                              Katab
 kcg-NG           Tyap Nigeria                                      Katab
 kde              Makonde                                           Chimakonde
 kde-TZ           Makonde Tanzania                                  Chimakonde Tanzania
 kea              Kabuverdianu                                      kabuverdianu
 kea-CV           Kabuverdianu Cape Verde                           kabuverdianu Kabu Verdi
 ken              Kenyang                                           Kɛnyaŋ
 ken-CM           Kenyang Cameroon                                  Kɛnyaŋ
 kgp              Kaingang                                          kanhgág
 kgp-BR           Kaingang Brazil                                   kanhgág Mrasir
 khq              Koyra Chiini                                      Koyra ciini
 khq-ML           Koyra Chiini Mali                                 Koyra ciini Maali
 ki               Kikuyu                                            Gikuyu
 ki-KE            Kikuyu Kenya                                      Gikuyu Kenya
 kk               Kazakh                                            қазақ тілі
 kk-KZ            Kazakh Kazakhstan                                 қазақ тілі Қазақстан
 kkj              Kako                                              kakɔ
 kkj-CM           Kako Cameroon                                     kakɔ Kamɛrun
 kl               Kalaallisut                                       kalaallisut
 kl-GL            Kalaallisut Greenland                             kalaallisut Kalaallit Nunaat
 kln              Kalenjin                                          Kalenjin
 kln-KE           Kalenjin Kenya                                    Kalenjin Emetab Kenya
 km               Khmer                                             ខ្មែរ
 km-KH            Khmer Cambodia                                    ខ្មែរ កម្ពុជា
 kn               Kannada                                           ಕನ್ನಡ
 kn-IN            Kannada India                                     ಕನ್ನಡ ಭಾರತ
 ko               Korean                                            한국어
 ko-KP            Korean North Korea                                한국어 조선민주주의인민공화국
 ko-KR            Korean South Korea                                한국어 대한민국
 kok              Konkani                                           कोंकणी
 kok-IN           Konkani India                                     कोंकणी भारत
 kpe              Kpelle                                            Kpɛlɛɛ
 kpe-GN           Kpelle Guinea                                     Kpɛlɛɛ
 kpe-LR           Kpelle Liberia                                    Kpɛlɛɛ
 ks               Kashmiri                                          کٲشُر
 ks-Arab          Kashmiri Arabic                                   کٲشُر عربی
 ks-Arab-IN       Kashmiri India Arabic                             کٲشُر ہِندوستان عربی
 ks-Deva          Kashmiri Devanagari                               कॉशुर देवनागरी
 ks-Deva-IN       Kashmiri India Devanagari                         कॉशुर हिंदोस्तान देवनागरी
 ksb              Shambala                                          Kishambaa
 ksb-TZ           Shambala Tanzania                                 Kishambaa Tanzania
 ksf              Bafia                                             rikpa
 ksf-CM           Bafia Cameroon                                    rikpa kamɛrún
 ksh              Colognian                                         Kölsch
 ksh-DE           Colognian Germany                                 Kölsch Doütschland
 ku               Kurdish                                           kurdî
 ku-TR            Kurdish Türkiye                                   kurdî Tirkiye
 kw               Cornish                                           kernewek
 kw-GB            Cornish United Kingdom                            kernewek Rywvaneth Unys
 ky               Kyrgyz                                            кыргызча
 ky-KG            Kyrgyz Kyrgyzstan                                 кыргызча Кыргызстан
 la               Latin
 la-VA            Latin Vatican City
 lag              Langi                                             Kɨlaangi
 lag-TZ           Langi Tanzania                                    Kɨlaangi Taansanía
 lb               Luxembourgish                                     Lëtzebuergesch
 lb-LU            Luxembourgish Luxembourg                          Lëtzebuergesch Lëtzebuerg
 lg               Ganda                                             Luganda
 lg-UG            Ganda Uganda                                      Luganda Yuganda
 lij              Ligurian                                          ligure
 lij-IT           Ligurian Italy                                    ligure Italia
 lkt              Lakota                                            Lakȟólʼiyapi
 lkt-US           Lakota United States                              Lakȟólʼiyapi Mílahaŋska Tȟamákȟočhe
 lmo              Lombard
 lmo-IT           Lombard Italy
 ln               Lingala                                           lingála
 ln-AO            Lingala Angola                                    lingála Angóla
 ln-CD            Lingala Congo - Kinshasa                          lingála Republíki ya Kongó Demokratíki
 ln-CF            Lingala Central African Republic                  lingála Repibiki ya Afríka ya Káti
 ln-CG            Lingala Congo - Brazzaville                       lingála Kongo
 lo               Lao                                               ລາວ
 lo-LA            Lao Laos                                          ລາວ ລາວ
 lrc              Northern Luri                                     لۊری شومالی
 lrc-IQ           Northern Luri Iraq                                لۊری شومالی
 lrc-IR           Northern Luri Iran                                لۊری شومالی
 lt               Lithuanian                                        lietuvių
 lt-LT            Lithuanian Lithuania                              lietuvių Lietuva
 lu               Luba-Katanga                                      Tshiluba
 lu-CD            Luba-Katanga Congo - Kinshasa                     Tshiluba Ditunga wa Kongu
 luo              Luo                                               Dholuo
 luo-KE           Luo Kenya                                         Dholuo Kenya
 luy              Luyia                                             Luluhia
 luy-KE           Luyia Kenya                                       Luluhia Kenya
 lv               Latvian                                           latviešu
 lv-LV            Latvian Latvia                                    latviešu Latvija
 mai              Maithili                                          मैथिली
 mai-IN           Maithili India                                    मैथिली भारत
 mas              Masai                                             Maa
 mas-KE           Masai Kenya                                       Maa Kenya
 mas-TZ           Masai Tanzania                                    Maa Tansania
 mdf              Moksha                                            мокшень кяль
 mdf-RU           Moksha Russia                                     мокшень кяль
 mer              Meru                                              Kĩmĩrũ
 mer-KE           Meru Kenya                                        Kĩmĩrũ Kenya
 mfe              Morisyen                                          kreol morisien
 mfe-MU           Morisyen Mauritius                                kreol morisien Moris
 mg               Malagasy                                          Malagasy
 mg-MG            Malagasy Madagascar                               Malagasy Madagasikara
 mgh              Makhuwa-Meetto                                    Makua
 mgh-MZ           Makhuwa-Meetto Mozambique                         Makua Umozambiki
 mgo              Metaʼ                                             metaʼ
 mgo-CM           Metaʼ Cameroon                                    metaʼ Kamalun
 mi               Māori                                             Māori
 mi-NZ            Māori New Zealand                                 Māori Aotearoa
 mk               Macedonian                                        македонски
 mk-MK            Macedonian North Macedonia                        македонски Северна Македонија
 ml               Malayalam                                         മലയാളം
 ml-IN            Malayalam India                                   മലയാളം ഇന്ത്യ
 mn               Mongolian                                         монгол
 mn-MN            Mongolian Mongolia                                монгол Монгол
 mn-Mong          Mongolian Mongolian
 mn-Mong-CN       Mongolian China Mongolian
 mn-Mong-MN       Mongolian Mongolia Mongolian                      ᠮᠣᠩᠭᠣᠯ ᠮᠣᠩᠭᠣᠯ ᠮᠣᠩᠭᠣᠯ ᠪᠢᠴᠢᠭ᠌
 mni              Manipuri                                          মৈতৈলোন্
 mni-Beng         Manipuri Bangla                                   মৈতৈলোন্ বাংলা
 mni-Beng-IN      Manipuri India Bangla                             মৈতৈলোন্ ইন্দিয়া বাংলা
 mni-Mtei         Manipuri Meitei Mayek                             ꯃꯤꯇꯩꯂꯣꯟ ꯃꯤꯇꯩ ꯃꯌꯦꯛ
 mni-Mtei-IN      Manipuri India Meitei Mayek                       ꯃꯤꯇꯩꯂꯣꯟ ꯃꯤꯇꯩ ꯃꯌꯦꯛ
 moh              Mohawk                                            Kanienʼkéha
 moh-CA           Mohawk Canada                                     Kanienʼkéha
 mr               Marathi                                           मराठी
 mr-IN            Marathi India                                     मराठी भारत
 ms               Malay                                             Melayu
 ms-Arab          Malay Arabic
 ms-Arab-BN       Malay Brunei Arabic
 ms-Arab-MY       Malay Malaysia Arabic
 ms-BN            Malay Brunei                                      Melayu Brunei
 ms-ID            Malay Indonesia                                   Melayu Indonesia
 ms-MY            Malay Malaysia                                    Melayu Malaysia
 ms-SG            Malay Singapore                                   Melayu Singapura
 mt               Maltese                                           Malti
 mt-MT            Maltese Malta                                     Malti Malta
 mua              Mundang                                           MUNDAŊ
 mua-CM           Mundang Cameroon                                  MUNDAŊ kameruŋ
 mus              Muscogee                                          Mvskoke
 mus-US           Muscogee United States                            Mvskoke United States
 my               Burmese                                           မြန်မာ
 my-MM            Burmese Myanmar (Burma)                           မြန်မာ မြန်မာ
 myv              Erzya                                             эрзянь кель
 myv-RU           Erzya Russia                                      эрзянь кель
 mzn              Mazanderani                                       مازرونی
 mzn-IR           Mazanderani Iran                                  مازرونی ایران
 naq              Nama                                              Khoekhoegowab
 naq-NA           Nama Namibia                                      Khoekhoegowab Namibiab
 nb               Norwegian Bokmål                                  norsk bokmål
 nb-NO            Norwegian Bokmål Norway                           norsk bokmål Norge
 nb-SJ            Norwegian Bokmål Svalbard & Jan Mayen             norsk bokmål Svalbard og Jan Mayen
 nd               North Ndebele                                     isiNdebele
 nd-ZW            North Ndebele Zimbabwe                            isiNdebele Zimbabwe
 nds              Low German
 nds-DE           Low German Germany
 nds-NL           Low German Netherlands
 ne               Nepali                                            नेपाली
 ne-IN            Nepali India                                      नेपाली भारत
 ne-NP            Nepali Nepal                                      नेपाली नेपाल
 nl               Dutch                                             Nederlands
 nl-AW            Dutch Aruba                                       Nederlands Aruba
 nl-BE            Dutch Belgium                                     Nederlands België
 nl-BQ            Dutch Caribbean Netherlands                       Nederlands Caribisch Nederland
 nl-CW            Dutch Curaçao                                     Nederlands Curaçao
 nl-NL            Dutch Netherlands                                 Nederlands Nederland
 nl-SR            Dutch Suriname                                    Nederlands Suriname
 nl-SX            Dutch Sint Maarten                                Nederlands Sint-Maarten
 nmg              Kwasio
 nmg-CM           Kwasio Cameroon                                   Kamerun
 nn               Norwegian Nynorsk                                 norsk nynorsk
 nn-NO            Norwegian Nynorsk Norway                          norsk nynorsk Noreg
 nnh              Ngiemboon                                         Shwóŋò ngiembɔɔn
 nnh-CM           Ngiemboon Cameroon                                Shwóŋò ngiembɔɔn Kàmalûm
 no               Norwegian                                         norsk
 nqo              N’Ko                                              ߒߞߏ
 nqo-GN           N’Ko Guinea                                       ߒߞߏ ߖߌ߬ߣߍ߫
 nr               South Ndebele
 nr-ZA            South Ndebele South Africa
 nso              Northern Sotho
 nso-ZA           Northern Sotho South Africa                       Afrika Borwa
 nus              Nuer                                              Thok Nath
 nus-SS           Nuer South Sudan                                  Thok Nath
 nv               Navajo                                            Diné Bizaad
 nv-US            Navajo United States                              Diné Bizaad
 ny               Nyanja                                            Nyanja
 ny-MW            Nyanja Malawi                                     Nyanja
 nyn              Nyankole                                          Runyankore
 nyn-UG           Nyankole Uganda                                   Runyankore Uganda
 oc               Occitan
 oc-ES            Occitan Spain
 oc-FR            Occitan France
 om               Oromo                                             Oromoo
 om-ET            Oromo Ethiopia                                    Oromoo Itoophiyaa
 om-KE            Oromo Kenya                                       Oromoo Keeniyaa
 or               Odia                                              ଓଡ଼ିଆ
 or-IN            Odia India                                        ଓଡ଼ିଆ ଭାରତ
 os               Ossetic                                           ирон
 os-GE            Ossetic Georgia                                   ирон Гуырдзыстон
 os-RU            Ossetic Russia                                    ирон Уӕрӕсе
 osa              Osage                                             𐓏𐓘𐓻𐓘𐓻𐓟
 osa-US           Osage United States                               𐓏𐓘𐓻𐓘𐓻𐓟 United States
 pa               Punjabi                                           ਪੰਜਾਬੀ
 pa-Arab          Punjabi Arabic                                    پنجابی عربی
 pa-Arab-PK       Punjabi Pakistan Arabic                           پنجابی پاکستان عربی
 pa-Guru          Punjabi Gurmukhi                                  ਪੰਜਾਬੀ ਗੁਰਮੁਖੀ
 pa-Guru-IN       Punjabi India Gurmukhi                            ਪੰਜਾਬੀ ਭਾਰਤ ਗੁਰਮੁਖੀ
 pap              Papiamento                                        Papiamentu
 pap-AW           Papiamento Aruba                                  Papiamentu
 pap-CW           Papiamento Curaçao                                Papiamentu Kòrsou
 pcm              Nigerian Pidgin                                   Naijíriá Píjin
 pcm-NG           Nigerian Pidgin Nigeria                           Naijíriá Píjin Naijíria
 pis              Pijin                                             Pijin
 pis-SB           Pijin Solomon Islands                             Pijin Solomon Aelan
 pl               Polish                                            polski
 pl-PL            Polish Poland                                     polski Polska
 prg              Prussian                                          prūsiskan
 prg-001          Prussian world                                    prūsiskan
 ps               Pashto                                            پښتو
 ps-AF            Pashto Afghanistan                                پښتو افغانستان
 ps-PK            Pashto Pakistan                                   پښتو پاکستان
 pt               Portuguese                                        português
 pt-AO            Portuguese Angola                                 português Angola
 pt-BR            Portuguese Brazil                                 português Brasil
 pt-CH            Portuguese Switzerland                            português Suíça
 pt-CV            Portuguese Cape Verde                             português Cabo Verde
 pt-GQ            Portuguese Equatorial Guinea                      português Guiné Equatorial
 pt-GW            Portuguese Guinea-Bissau                          português Guiné-Bissau
 pt-LU            Portuguese Luxembourg                             português Luxemburgo
 pt-MO            Portuguese Macao SAR China                        português Macau, RAE da China
 pt-MZ            Portuguese Mozambique                             português Moçambique
 pt-PT            Portuguese Portugal                               português Portugal
 pt-ST            Portuguese São Tomé & Príncipe                    português São Tomé e Príncipe
 pt-TL            Portuguese Timor-Leste                            português Timor-Leste
 qu               Quechua                                           Runasimi
 qu-BO            Quechua Bolivia                                   Runasimi Bolivia
 qu-EC            Quechua Ecuador                                   Runasimi Ecuador
 qu-PE            Quechua Peru                                      Runasimi Perú
 quc              Kʼicheʼ                                           Kʼicheʼ
 quc-GT           Kʼicheʼ Guatemala                                 Kʼicheʼ
 raj              Rajasthani                                        राजस्थानी
 raj-IN           Rajasthani India                                  राजस्थानी भारत
 rhg              Rohingya                                          𐴌𐴗𐴥𐴝𐴙𐴚𐴒𐴙𐴝
 rhg-Rohg         Rohingya Hanifi                                   𐴌𐴗𐴥𐴝𐴙𐴚𐴒𐴙𐴝
 rhg-Rohg-BD      Rohingya Bangladesh Hanifi                        𐴌𐴗𐴥𐴝𐴙𐴚𐴒𐴙𐴝
 rhg-Rohg-MM      Rohingya Myanmar (Burma) Hanifi                   𐴌𐴗𐴥𐴝𐴙𐴚𐴒𐴙𐴝
 rif              Riffian                                           Tarifit
 rif-MA           Riffian Morocco                                   Tarifit
 rm               Romansh                                           rumantsch
 rm-CH            Romansh Switzerland                               rumantsch Svizra
 rn               Rundi                                             Ikirundi
 rn-BI            Rundi Burundi                                     Ikirundi Uburundi
 ro               Romanian                                          română
 ro-MD            Romanian Moldova                                  română Republica Moldova
 ro-RO            Romanian Romania                                  română România
 rof              Rombo                                             Kihorombo
 rof-TZ           Rombo Tanzania                                    Kihorombo Tanzania
 ru               Russian                                           русский
 ru-BY            Russian Belarus                                   русский Беларусь
 ru-KG            Russian Kyrgyzstan                                русский Киргизия
 ru-KZ            Russian Kazakhstan                                русский Казахстан
 ru-MD            Russian Moldova                                   русский Молдова
 ru-RU            Russian Russia                                    русский Россия
 ru-UA            Russian Ukraine                                   русский Украина
 rw               Kinyarwanda                                       Kinyarwanda
 rw-RW            Kinyarwanda Rwanda                                Kinyarwanda U Rwanda
 rwk              Rwa                                               Kiruwa
 rwk-TZ           Rwa Tanzania                                      Kiruwa Tanzania
 sa               Sanskrit                                          संस्कृत भाषा
 sa-IN            Sanskrit India                                    संस्कृत भाषा भारतः
 sah              Yakut                                             саха тыла
 sah-RU           Yakut Russia                                      саха тыла Арассыыйа
 saq              Samburu                                           Kisampur
 saq-KE           Samburu Kenya                                     Kisampur Kenya
 sat              Santali                                           ᱥᱟᱱᱛᱟᱲᱤ
 sat-Deva         Santali Devanagari                                सानताड़ी देवानागारी
 sat-Deva-IN      Santali India Devanagari                          सानताड़ी देवानागारी
 sat-Olck         Santali Ol Chiki                                  ᱥᱟᱱᱛᱟᱲᱤ ᱚᱞ ᱪᱤᱠᱤ
 sat-Olck-IN      Santali India Ol Chiki                            ᱥᱟᱱᱛᱟᱲᱤ ᱤᱱᱰᱤᱭᱟ ᱚᱞ ᱪᱤᱠᱤ
 sbp              Sangu                                             Ishisangu
 sbp-TZ           Sangu Tanzania                                    Ishisangu Tansaniya
 sc               Sardinian                                         sardu
 sc-IT            Sardinian Italy                                   sardu Itàlia
 scn              Sicilian                                          sicilianu
 scn-IT           Sicilian Italy                                    sicilianu
 sd               Sindhi                                            سنڌي
 sd-Arab          Sindhi Arabic                                     سنڌي عربي
 sd-Arab-PK       Sindhi Pakistan Arabic                            سنڌي پاڪستان عربي
 sd-Deva          Sindhi Devanagari                                 सिन्धी देवनागिरी
 sd-Deva-IN       Sindhi India Devanagari                           सिन्धी भारत देवनागिरी
 sdh              Southern Kurdish                                  کوردی خوارگ
 sdh-IQ           Southern Kurdish Iraq                             کوردی خوارگ
 sdh-IR           Southern Kurdish Iran                             کوردی خوارگ
 se               Northern Sami                                     davvisámegiella
 se-FI            Northern Sami Finland                             davvisámegiella Suopma
 se-NO            Northern Sami Norway                              davvisámegiella Norga
 se-SE            Northern Sami Sweden                              davvisámegiella Ruoŧŧa
 seh              Sena                                              sena
 seh-MZ           Sena Mozambique                                   sena Moçambique
 ses              Koyraboro Senni                                   Koyraboro senni
 ses-ML           Koyraboro Senni Mali                              Koyraboro senni Maali
 sg               Sango                                             Sängö
 sg-CF            Sango Central African Republic                    Sängö Ködörösêse tî Bêafrîka
 shi              Tachelhit                                         ⵜⴰⵛⵍⵃⵉⵜ
 shi-Latn         Tachelhit Latin                                   Tashelḥiyt
 shi-Latn-MA      Tachelhit Morocco Latin                           Tashelḥiyt lmɣrib
 shi-Tfng         Tachelhit Tifinagh                                ⵜⴰⵛⵍⵃⵉⵜ
 shi-Tfng-MA      Tachelhit Morocco Tifinagh                        ⵜⴰⵛⵍⵃⵉⵜ ⵍⵎⵖⵔⵉⴱ
 shn              Shan                                              တႆး
 shn-MM           Shan Myanmar (Burma)                              တႆး မျၢၼ်ႇမႃႇ (မိူင်းမၢၼ်ႈ)
 shn-TH           Shan Thailand                                     တႆး မိူင်းထႆး
 si               Sinhala                                           සිංහල
 si-LK            Sinhala Sri Lanka                                 සිංහල ශ්‍රී ලංකාව
 sid              Sidamo
 sid-ET           Sidamo Ethiopia
 sk               Slovak                                            slovenčina
 sk-SK            Slovak Slovakia                                   slovenčina Slovensko
 sl               Slovenian                                         slovenščina
 sl-SI            Slovenian Slovenia                                slovenščina Slovenija
 sma              Southern Sami                                     Åarjelsaemien gïele
 sma-NO           Southern Sami Norway                              Åarjelsaemien gïele
 sma-SE           Southern Sami Sweden                              Åarjelsaemien gïele
 smj              Lule Sami                                         julevsámegiella
 smj-NO           Lule Sami Norway                                  julevsámegiella
 smj-SE           Lule Sami Sweden                                  julevsámegiella
 smn              Inari Sami                                        anarâškielâ
 smn-FI           Inari Sami Finland                                anarâškielâ Suomâ
 sms              Skolt Sami                                        sääʹmǩiõll
 sms-FI           Skolt Sami Finland                                sääʹmǩiõll
 sn               Shona                                             chiShona
 sn-ZW            Shona Zimbabwe                                    chiShona Zimbabwe
 so               Somali                                            Soomaali
 so-DJ            Somali Djibouti                                   Soomaali Jabuuti
 so-ET            Somali Ethiopia                                   Soomaali Itoobiya
 so-KE            Somali Kenya                                      Soomaali Kenya
 so-SO            Somali Somalia                                    Soomaali Soomaaliya
 sq               Albanian                                          shqip
 sq-AL            Albanian Albania                                  shqip Shqipëri
 sq-MK            Albanian North Macedonia                          shqip Maqedonia e Veriut
 sq-XK            Albanian Kosovo                                   shqip Kosovë
 sr               Serbian                                           српски
 sr-Cyrl          Serbian Cyrillic                                  српски ћирилица
 sr-Cyrl-BA       Serbian Bosnia & Herzegovina Cyrillic             српски Босна и Херцеговина ћирилица
 sr-Cyrl-ME       Serbian Montenegro Cyrillic                       српски Црна Гора ћирилица
 sr-Cyrl-RS       Serbian Serbia Cyrillic                           српски Србија ћирилица
 sr-Cyrl-XK       Serbian Kosovo Cyrillic                           српски Косово ћирилица
 sr-Latn          Serbian Latin                                     srpski latinica
 sr-Latn-BA       Serbian Bosnia & Herzegovina Latin                srpski Bosna i Hercegovina latinica
 sr-Latn-ME       Serbian Montenegro Latin                          srpski Crna Gora latinica
 sr-Latn-RS       Serbian Serbia Latin                              srpski Srbija latinica
 sr-Latn-XK       Serbian Kosovo Latin                              srpski Kosovo latinica
 ss               Swati                                             siSwati
 ss-SZ            Swati Eswatini                                    siSwati eSwatini
 ss-ZA            Swati South Africa                                siSwati
 ssy              Saho                                              Saho
 ssy-ER           Saho Eritrea                                      Saho
 st               Southern Sotho                                    Sesotho
 st-LS            Southern Sotho Lesotho                            Sesotho
 st-ZA            Southern Sotho South Africa                       Sesotho
 su               Sundanese                                         Basa Sunda
 su-Latn          Sundanese Latin                                   Basa Sunda Latin
 su-Latn-ID       Sundanese Indonesia Latin                         Basa Sunda Indonesia Latin
 sv               Swedish                                           svenska
 sv-AX            Swedish Åland Islands                             svenska Åland
 sv-FI            Swedish Finland                                   svenska Finland
 sv-SE            Swedish Sweden                                    svenska Sverige
 sw               Swahili                                           Kiswahili
 sw-CD            Swahili Congo - Kinshasa                          Kiswahili Jamhuri ya Kidemokrasia ya Kongo
 sw-KE            Swahili Kenya                                     Kiswahili Kenya
 sw-TZ            Swahili Tanzania                                  Kiswahili Tanzania
 sw-UG            Swahili Uganda                                    Kiswahili Uganda
 syr              Syriac                                            ܣܘܪܝܝܐ
 syr-IQ           Syriac Iraq                                       ܣܘܪܝܝܐ ܥܝܪܩ
 syr-SY           Syriac Syria                                      ܣܘܪܝܝܐ ܣܘܪܝܐ
 szl              Silesian                                          ślōnski
 szl-PL           Silesian Poland                                   ślōnski
 ta               Tamil                                             தமிழ்
 ta-IN            Tamil India                                       தமிழ் இந்தியா
 ta-LK            Tamil Sri Lanka                                   தமிழ் இலங்கை
 ta-MY            Tamil Malaysia                                    தமிழ் மலேசியா
 ta-SG            Tamil Singapore                                   தமிழ் சிங்கப்பூர்
 te               Telugu                                            తెలుగు
 te-IN            Telugu India                                      తెలుగు భారతదేశం
 teo              Teso                                              Kiteso
 teo-KE           Teso Kenya                                        Kiteso Kenia
 teo-UG           Teso Uganda                                       Kiteso Uganda
 tg               Tajik                                             тоҷикӣ
 tg-TJ            Tajik Tajikistan                                  тоҷикӣ Тоҷикистон
 th               Thai                                              ไทย
 th-TH            Thai Thailand                                     ไทย ไทย
 ti               Tigrinya                                          ትግርኛ
 ti-ER            Tigrinya Eritrea                                  ትግርኛ ኤርትራ
 ti-ET            Tigrinya Ethiopia                                 ትግርኛ ኢትዮጵያ
 tig              Tigre
 tig-ER           Tigre Eritrea
 tk               Turkmen                                           türkmen dili
 tk-TM            Turkmen Turkmenistan                              türkmen dili Türkmenistan
 tn               Tswana
 tn-BW            Tswana Botswana
 tn-ZA            Tswana South Africa                               Aforika Borwa
 to               Tongan                                            lea fakatonga
 to-TO            Tongan Tonga                                      lea fakatonga Tonga
 tok              Toki Pona                                         Toki Pona
 tok-001          Toki Pona world                                   Toki Pona
 tpi              Tok Pisin                                         Tok Pisin
 tpi-PG           Tok Pisin Papua New Guinea                        Tok Pisin Papua Niugini
 tr               Turkish                                           Türkçe
 tr-CY            Turkish Cyprus                                    Türkçe Kıbrıs
 tr-TR            Turkish Türkiye                                   Türkçe Türkiye
 trv              Taroko                                            patas Taroko
 trv-TW           Taroko Taiwan                                     patas Taroko
 trw              Torwali                                           توروالی
 trw-PK           Torwali Pakistan                                  توروالی
 ts               Tsonga
 ts-ZA            Tsonga South Africa
 tt               Tatar                                             татар
 tt-RU            Tatar Russia                                      татар Россия
 twq              Tasawaq                                           Tasawaq senni
 twq-NE           Tasawaq Niger                                     Tasawaq senni Nižer
 tzm              Central Atlas Tamazight                           Tamaziɣt n laṭlaṣ
 tzm-MA           Central Atlas Tamazight Morocco                   Tamaziɣt n laṭlaṣ Meṛṛuk
 ug               Uyghur                                            ئۇيغۇرچە
 ug-CN            Uyghur China                                      ئۇيغۇرچە جۇڭگو
 uk               Ukrainian                                         українська
 uk-UA            Ukrainian Ukraine                                 українська Україна
 und              Unknown language
 ur               Urdu                                              اردو
 ur-IN            Urdu India                                        اردو بھارت
 ur-PK            Urdu Pakistan                                     اردو پاکستان
 uz               Uzbek                                             o‘zbek
 uz-Arab          Uzbek Arabic                                      اوزبیک عربی
 uz-Arab-AF       Uzbek Afghanistan Arabic                          اوزبیک افغانستان عربی
 uz-Cyrl          Uzbek Cyrillic                                    ўзбекча Кирил
 uz-Cyrl-UZ       Uzbek Uzbekistan Cyrillic                         ўзбекча Ўзбекистон Кирил
 uz-Latn          Uzbek Latin                                       o‘zbek lotin
 uz-Latn-UZ       Uzbek Uzbekistan Latin                            o‘zbek Oʻzbekiston lotin
 vai              Vai                                               ꕙꔤ
 vai-Latn         Vai Latin                                         Vai
 vai-Latn-LR      Vai Liberia Latin                                 Vai Laibhiya
 vai-Vaii         Vai Vai                                           ꕙꔤ
 vai-Vaii-LR      Vai Liberia Vai                                   ꕙꔤ ꕞꔤꔫꕩ
 ve               Venda
 ve-ZA            Venda South Africa
 vec              Venetian                                          Veneto
 vec-IT           Venetian Italy                                    Veneto
 vi               Vietnamese                                        Tiếng Việt
 vi-VN            Vietnamese Vietnam                                Tiếng Việt Việt Nam
 vo               Volapük
 vo-001           Volapük world
 vun              Vunjo                                             Kyivunjo
 vun-TZ           Vunjo Tanzania                                    Kyivunjo Tanzania
 wa               Walloon                                           walon
 wa-BE            Walloon Belgium                                   walon
 wae              Walser                                            Walser
 wae-CH           Walser Switzerland                                Walser Schwiz
 wal              Wolaytta
 wal-ET           Wolaytta Ethiopia
 wbp              Warlpiri                                          Warlpiri
 wbp-AU           Warlpiri Australia                                Warlpiri
 wo               Wolof                                             Wolof
 wo-SN            Wolof Senegal                                     Wolof Senegaal
 xh               Xhosa                                             IsiXhosa
 xh-ZA            Xhosa South Africa                                IsiXhosa EMzantsi Afrika
 xog              Soga                                              Olusoga
 xog-UG           Soga Uganda                                       Olusoga Yuganda
 yav              Yangben                                           nuasue
 yav-CM           Yangben Cameroon                                  nuasue Kemelún
 yi               Yiddish                                           ייִדיש
 yi-001           Yiddish world                                     ייִדיש וועלט
 yo               Yoruba                                            Èdè Yorùbá
 yo-BJ            Yoruba Benin                                      Èdè Yorùbá Bɛ̀nɛ̀
 yo-NG            Yoruba Nigeria                                    Èdè Yorùbá Nàìjíríà
 yrl              Nheengatu                                         nheẽgatu
 yrl-BR           Nheengatu Brazil                                  nheẽgatu Brasiu
 yrl-CO           Nheengatu Colombia                                ñengatú Kurũbiya
 yrl-VE           Nheengatu Venezuela                               ñengatú Wenesuera
 yue              Cantonese                                         粵語
 yue-Hans         Cantonese Simplified                              粤语 简体
 yue-Hans-CN      Cantonese China Simplified                        粤语 中华人民共和国 简体
 yue-Hant         Cantonese Traditional                             粵語 繁體
 yue-Hant-HK      Cantonese Hong Kong SAR China Traditional         粵語 中華人民共和國香港特別行政區 繁體
 zgh              Standard Moroccan Tamazight                       ⵜⴰⵎⴰⵣⵉⵖⵜ
 zgh-MA           Standard Moroccan Tamazight Morocco               ⵜⴰⵎⴰⵣⵉⵖⵜ ⵍⵎⵖⵔⵉⴱ
 zh               Chinese                                           中文
 zh-Hans          Chinese Simplified                                中文 简体
 zh-Hans-CN       Chinese China Simplified                          中文 中国 简体
 zh-Hans-HK       Chinese Hong Kong SAR China Simplified            中文 中国香港特别行政区 简体
 zh-Hans-MO       Chinese Macao SAR China Simplified                中文 中国澳门特别行政区 简体
 zh-Hans-SG       Chinese Singapore Simplified                      中文 新加坡 简体
 zh-Hant          Chinese Traditional                               中文 繁體
 zh-Hant-HK       Chinese Hong Kong SAR China Traditional           中文 中國香港特別行政區 繁體字
 zh-Hant-MO       Chinese Macao SAR China Traditional               中文 中國澳門特別行政區 繁體字
 zh-Hant-TW       Chinese Taiwan Traditional                        中文 台灣 繁體
 zu               Zulu                                              isiZulu
 zu-ZA            Zulu South Africa                                 isiZulu iNingizimu Afrika

=head1 SUPPORT

Bugs may be submitted at L<https://github.com/houseabsolute/DateTime-Locale/issues>.

There is a mailing list available for users of this distribution,
L<mailto:datetime@perl.org>.

=head1 SOURCE

The source code repository for DateTime-Locale can be found at L<https://github.com/houseabsolute/DateTime-Locale>.

=head1 AUTHOR

Dave Rolsky <autarch@urth.org>

=head1 COPYRIGHT AND LICENSE

This software is copyright (c) 2003 - 2023 by Dave Rolsky.

This is free software; you can redistribute it and/or modify it under
the same terms as the Perl 5 programming language system itself.

The full text of the license can be found in the
F<LICENSE> file included with this distribution.

=cut
