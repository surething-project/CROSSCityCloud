------------------------------
-- Route of Alameda - Técnico Lisboa
------------------------------

INSERT INTO route (id, position, image_url, main_locale)
VALUES ('route_1',
        1,
        'https://3.bp.blogspot.com/-gxWfAA_EdNI/Un0o1LzFOwI/AAAAAAAAJmo/KfsQMYxvHCo/s1600/1-4277-Marcas.jpg',
        'pt');

INSERT INTO route_lang (route_id, lang, name, description)
VALUES ('route_1',
        'pt',
        'Alameda - Técnico Lisboa',
        'Situado em Lisboa, numa zona central da cidade, o campus proporciona um acesso privilegiado aos principais pontos de interesse científico, cultural e de lazer da cidade. O sistema de transportes públicos permite uma fácil mobilidade para todos os pontos da cidade.'),
       ('route_1',
        'en',
        'Alameda - Técnico Lisboa',
        'Located in Lisbon, in a central area of the city, the campus provides privileged access to the main points of scientific, cultural and leisure interest in the city. The public transport system allows easy mobility to all parts of the city.');

INSERT INTO poi (id, world_coord, web_url, image_url, main_locale)
VALUES ('poi_1_1',
        '(38.73661764318405, -9.137217959885179)',
        'https://aeist.pt/',
        'https://lh5.googleusercontent.com/p/AF1QipPv9C1ZWF3OeOWExXRO0yHe49UdIY0Q4gFpUbOs=w750-h401-p-k-no',
        'pt'),
       ('poi_1_2',
        '(38.74251285902285, -9.138224647304572)',
        'https://tecnico.ulisboa.pt/pt/tag/museu-faraday/',
        'https://tecnico.ulisboa.pt/files/2017/02/inauguracao-do-museu-faraday-no-tecnico-1140x641.jpg',
        'pt'),
       ('poi_1_3',
        '(38.73700329289798, -9.139338003537375)',
        'https://bist.tecnico.ulisboa.pt/apresentacao/',
        'https://fenix.tecnico.ulisboa.pt/downloadFile/3779580646309/Biblioteca.Imagem_1629.JPG',
        'pt');

INSERT INTO poi_lang (poi_id, lang, name, description)
VALUES ('poi_1_1',
        'pt',
        'AEIST - Associação dos Estudantes do Instituto Superior Técnico',
        'A AEIST é a associação que representa os 12 000 estudantes do Instituto Superior Técnico e defende as suas causas e os seus direitos. A AEIST é uma instituição que marcou a sociedade portuguesa ao longo dos seus 106 anos de história, nomeadamente pelo seu papel na luta contra o fascismo, possuindo atualmente um papel predominante na definição do ensino universitário português e europeu.'),
       ('poi_1_1',
        'en',
        'AEIST - Student Association of Instituto Superior Técnico',
        'AEIST is the association that represents the 12,000 students of Instituto Superior Técnico and defends their causes and their rights. AEIST is an institution that has marked Portuguese society throughout its 106 years of history, namely for its role in the fight against fascism, currently having a predominant role in the definition of Portuguese and European university education.'),
       ('poi_1_2',
        'pt',
        'Museu Faraday',
        'O Museu Faraday, que se encontra em fase de organização no contexto da Área Científica de Eletrónica do Departamento de Engenharia Eletrotécnica e de Computadores (DEEC), reúne um importante conjunto de c. 600 instrumentos e equipamentos científicos históricos, sobretudo dos séculos XIX e XX.'),
       ('poi_1_2',
        'en',
        'Faraday Museum',
        'The Faraday Museum, which is being organized in the context of the Scientific Area of Electronics of the Department of Electrical and Computer Engineering (DEEC), brings together an important set of c. 600 historical scientific instruments and equipment, mainly from the 19th and 20th centuries.'),
       ('poi_1_3',
        'pt',
        'BIST - Biblioteca do Instituto Superior Técnico',
        'A BIST caracteriza-se por ser uma Biblioteca do Ensino Superior, especializada nas áreas de Engenharia, Ciência e Tecnologia, sendo possuidora de um património documental  que, enriquecido ao longo dos anos, se assume como um dos mais importantes ao nível nacional.'),
       ('poi_1_3',
        'en',
        'BIST - Library of Instituto Superior Técnico',
        'BIST is characterized by being a Higher Education Library, specialized in the areas of Engineering, Science and Technology, possessing a documentary heritage that, enriched over the years, assumes itself as one of the most important at the national level.');

INSERT INTO waypoint (id, poi_id, stay_for, confidence_threshold)
VALUES ('waypoint_1_1', 'poi_1_1', '20 seconds', 75),
       ('waypoint_1_2', 'poi_1_2', '20 seconds', 75),
       ('waypoint_1_3', 'poi_1_3', '20 seconds', 75);

INSERT INTO route_waypoint (route_id, waypoint_id, position)
VALUES ('route_1', 'waypoint_1_1', 1),
       ('route_1', 'waypoint_1_2', 2),
       ('route_1', 'waypoint_1_3', 3);

-- Wi-Fi Networks

INSERT INTO wifiap (bssid, type, trigger, totp_secret, totp_regexp)
VALUES ('03:00:00:00:03:00', 'untrusted', true, NULL, NULL),
       ('03:00:00:00:03:01', 'untrusted', false, NULL, NULL),
       ('03:00:00:00:03:02', 'untrusted', false, NULL, NULL),
       ('03:00:00:00:03:03', 'untrusted', false, NULL, NULL),
       ('03:00:00:00:03:04', 'untrusted', false, NULL, NULL),
       ('03:01:00:00:03:00', 'totp', true,
        'KAZGMUTLNFTFKVCNG5RW2ODQJA2TEZCUMVDVGNBVLFMVCUTWGJREM4KLO5ZVGWDLKZ2DE6KELJZVSN3TKFLWW2KBPFFDKUCTOBYG4WQ=',
        '^CROSS-([0-9]*?)$');

INSERT INTO poi_wifiap (poi_id, wifiap_bssid)
VALUES ('poi_1_1', '03:00:00:00:03:00'),
       ('poi_1_1', '03:00:00:00:03:01'),
       ('poi_1_1', '03:00:00:00:03:02'),
       ('poi_1_1', '03:00:00:00:03:03'),
       ('poi_1_1', '03:00:00:00:03:04'),
       ('poi_1_1', '03:01:00:00:03:00');

INSERT INTO wifiap (bssid, type, trigger, totp_secret, totp_regexp)
VALUES ('03:00:00:00:04:00', 'untrusted', true, NULL, NULL),
       ('03:00:00:00:04:01', 'untrusted', false, NULL, NULL),
       ('03:00:00:00:04:02', 'untrusted', false, NULL, NULL),
       ('03:00:00:00:04:03', 'untrusted', false, NULL, NULL),
       ('03:00:00:00:04:04', 'untrusted', false, NULL, NULL);

INSERT INTO poi_wifiap (poi_id, wifiap_bssid)
VALUES ('poi_1_2', '03:00:00:00:04:00'),
       ('poi_1_2', '03:00:00:00:04:01'),
       ('poi_1_2', '03:00:00:00:04:02'),
       ('poi_1_2', '03:00:00:00:04:03'),
       ('poi_1_2', '03:00:00:00:04:04');

INSERT INTO wifiap (bssid, type, trigger, totp_secret, totp_regexp)
VALUES ('03:00:00:00:05:00', 'untrusted', true, NULL, NULL),
       ('03:00:00:00:05:01', 'untrusted', false, NULL, NULL),
       ('03:00:00:00:05:02', 'untrusted', false, NULL, NULL),
       ('03:00:00:00:05:03', 'untrusted', false, NULL, NULL),
       ('03:00:00:00:05:04', 'untrusted', false, NULL, NULL);

INSERT INTO poi_wifiap (poi_id, wifiap_bssid)
VALUES ('poi_1_3', '03:00:00:00:05:00'),
       ('poi_1_3', '03:00:00:00:05:01'),
       ('poi_1_3', '03:00:00:00:05:02'),
       ('poi_1_3', '03:00:00:00:05:03'),
       ('poi_1_3', '03:00:00:00:05:04');

------------------------------
-- Route of my house
------------------------------

INSERT INTO route (id, position, image_url, main_locale)
VALUES ('route_2',
        4,
        'https://wp105917.wpdns.ca/wp-content/uploads/2021/01/Green-Cedar-Home-Banner-1-1.jpg',
        'en');

INSERT INTO route_lang (route_id, lang, name, description)
VALUES ('route_2',
        'pt',
        'Lar Doce Lar',
        'Uma casa, ou domicílio, é um espaço usado como residência permanente ou semipermanente para um ou vários humanos. É um espaço totalmente ou semi-abrigado e pode ter tanto aspectos interiores como exteriores. As casas oferecem espaços abrigados, por exemplo, quartos.'),
       ('route_2',
        'en',
        'Home Sweet Home',
        'A home, or domicile, is a space used as a permanent or semi-permanent residence for one or many humans. It is a fully or semi sheltered space and can have both interior and exterior aspects to it. Homes provide sheltered spaces for instance rooms.');

INSERT INTO poi (id, world_coord, web_url, image_url, main_locale)
VALUES ('poi_2_1',
        '(32.23772, 161.06769)',
        'https://en.wikipedia.org/wiki/Bedroom',
        'https://www.bocadolobo.com/en/inspiration-and-ideas/wp-content/uploads/2020/08/feature-image-81.jpg',
        'en'),
       ('poi_2_2',
        '(32.23772, 161.06769)',
        'https://en.wikipedia.org/wiki/Living_room',
        'https://images.adsttc.com/media/images/61f0/610a/7e09/6801/6484/ffc6/slideshow/casa-do-hugo-furo-mariana-sanches.jpg?1643143442',
        'en');

INSERT INTO poi_lang (poi_id, lang, name, description)
VALUES ('poi_2_1',
        'pt',
        'Quarto',
        'Um quarto ou quarto de dormir é um quarto situado dentro de uma unidade residencial ou de alojamento caracterizado pelo seu uso para dormir. Um quarto típico ocidental contém como mobília do quarto uma ou duas camas.'),
       ('poi_2_1',
        'en',
        'Bedroom',
        'A bedroom or bedchamber is a room situated within a residential or accommodation unit characterised by its usage for sleeping. A typical western bedroom contains as bedroom furniture one or two beds.'),
       ('poi_2_2',
        'pt',
        'Sala de estar',
        'Esta sala às vezes é chamada de sala da frente quando está perto da entrada principal na frente da casa. Em casas grandes e formais, uma sala de estar geralmente é uma pequena área de estar privada adjacente a um quarto, como a Sala de Estar da Rainha e a Sala de Estar Lincoln da Casa Branca.'),
       ('poi_2_2',
        'en',
        'Living room',
        'Such a room is sometimes called a front room when it is near the main entrance at the front of the house. In large, formal homes, a sitting room is often a small private living area adjacent to a bedroom, such as the Queens'' Sitting Room and the Lincoln Sitting Room of the White House.');

INSERT INTO waypoint (id, poi_id, stay_for, confidence_threshold)
VALUES ('waypoint_2_1', 'poi_2_1', '20 seconds', 75),
       ('waypoint_2_2', 'poi_2_2', '20 seconds', 75);

INSERT INTO route_waypoint (route_id, waypoint_id, position)
VALUES ('route_2', 'waypoint_2_1', 1),
       ('route_2', 'waypoint_2_2', 2);

-- Wi-Fi Networks

INSERT INTO wifiap (bssid, type, trigger, totp_secret, totp_regexp)
VALUES ('84:94:8c:ef:6e:78', 'untrusted', true, NULL, NULL),
       ('3c:a8:2a:c7:b5:ea', 'untrusted', true, NULL, NULL),
       ('ca:02:10:5c:72:1a', 'untrusted', true, NULL, NULL),
       ('a4:b1:e9:f3:ef:29', 'untrusted', true, NULL, NULL),
       ('86:b8:b8:34:af:00', 'untrusted', true, NULL, NULL),
       ('a6:b1:e9:f3:ef:2a', 'untrusted', true, NULL, NULL),
       ('20:3d:b2:9a:d5:d0', 'untrusted', true, NULL, NULL),
       ('f4:30:b9:4b:b3:78', 'untrusted', false, NULL, NULL),
       ('08:b0:55:11:22:b1', 'untrusted', false, NULL, NULL),
       ('04:9f:ca:ae:25:9c', 'untrusted', false, NULL, NULL),
       ('84:94:8c:ee:de:18', 'untrusted', false, NULL, NULL),
       ('54:13:10:a8:79:10', 'untrusted', false, NULL, NULL),
       ('e8:82:5b:b0:56:19', 'untrusted', false, NULL, NULL),
       ('00:05:ca:b5:d2:28', 'untrusted', false, NULL, NULL),
       ('cc:19:a8:01:b5:60', 'untrusted', true, NULL, NULL),
       ('cc:19:a8:01:b5:62', 'untrusted', true, NULL, NULL),
       ('f0:f2:49:d8:fb:c8', 'untrusted', true, NULL, NULL),
       ('a8:4e:3f:33:a7:48', 'untrusted', true, NULL, NULL);

INSERT INTO poi_wifiap (poi_id, wifiap_bssid)
VALUES ('poi_2_1', '84:94:8c:ef:6e:78'),
       ('poi_2_1', '3c:a8:2a:c7:b5:ea'),
       ('poi_2_1', 'ca:02:10:5c:72:1a'),
       ('poi_2_1', 'a4:b1:e9:f3:ef:29'),
       ('poi_2_1', '86:b8:b8:34:af:00'),
       ('poi_2_1', 'a6:b1:e9:f3:ef:2a'),
       ('poi_2_1', '20:3d:b2:9a:d5:d0'),
       ('poi_2_1', 'f4:30:b9:4b:b3:78'),
       ('poi_2_1', '08:b0:55:11:22:b1'),
       ('poi_2_1', '04:9f:ca:ae:25:9c'),
       ('poi_2_1', '84:94:8c:ee:de:18'),
       ('poi_2_1', '54:13:10:a8:79:10'),
       ('poi_2_1', 'e8:82:5b:b0:56:19'),
       ('poi_2_1', '00:05:ca:b5:d2:28'),
       ('poi_2_1', 'cc:19:a8:01:b5:60'),
       ('poi_2_1', 'cc:19:a8:01:b5:62'),
       ('poi_2_1', 'f0:f2:49:d8:fb:c8'),
       ('poi_2_1', 'a8:4e:3f:33:a7:48');

INSERT INTO poi_wifiap (poi_id, wifiap_bssid)
VALUES ('poi_2_2', '84:94:8c:ef:6e:78'),
       ('poi_2_2', '3c:a8:2a:c7:b5:ea'),
       ('poi_2_2', 'ca:02:10:5c:72:1a'),
       ('poi_2_2', 'a4:b1:e9:f3:ef:29'),
       ('poi_2_2', '86:b8:b8:34:af:00'),
       ('poi_2_2', 'a6:b1:e9:f3:ef:2a'),
       ('poi_2_2', '20:3d:b2:9a:d5:d0'),
       ('poi_2_2', 'f4:30:b9:4b:b3:78'),
       ('poi_2_2', '08:b0:55:11:22:b1'),
       ('poi_2_2', '04:9f:ca:ae:25:9c'),
       ('poi_2_2', '84:94:8c:ee:de:18'),
       ('poi_2_2', '54:13:10:a8:79:10'),
       ('poi_2_2', 'e8:82:5b:b0:56:19'),
       ('poi_2_2', '00:05:ca:b5:d2:28'),
       ('poi_2_2', 'cc:19:a8:01:b5:60'),
       ('poi_2_2', 'cc:19:a8:01:b5:62'),
       ('poi_2_2', 'f0:f2:49:d8:fb:c8'),
       ('poi_2_2', 'a8:4e:3f:33:a7:48');

------------------------------
-- Route of IST - Taguspark
------------------------------

INSERT INTO route (id, position, image_url, main_locale)
VALUES ('route_3',
        2,
        'https://fenix.tecnico.ulisboa.pt/downloadFile/845043405435819/Tagus%20Novo%20Med..jpg',
        'en');

INSERT INTO route_lang (route_id, lang, name, description)
VALUES ('route_3',
        'pt',
        'IST - Taguspark',
        'O campus do IST no Taguspark, concluído em 2009, situa-se na região metropolitana de Lisboa, concelho de Oeiras e está inserido no maior parque de C&T nacional: o Taguspark. O campus representa assim a concretização de uma visão do ensino da engenharia, ciência e tecnologia que nasceu em Portugal no final do século passado: a ligação entre a Universidade e as Empresas.'),
       ('route_3',
        'en',
        'IST - Taguspark',
        'The IST campus in Taguspark, completed in 2009, is located in the metropolitan region of Lisbon, in the municipality of Oeiras and is part of the largest national S&T park: Taguspark. The campus thus represents the realization of a vision of the teaching of engineering, science and technology that was born in Portugal at the end of the last century: the link between the University and Companies.');

INSERT INTO poi (id, world_coord, web_url, image_url, main_locale)
VALUES ('poi_3_1',
        '(38.73715291107833, -9.302635744572406)',
        'https://en.wikipedia.org/wiki/Office',
        'https://as2.ftcdn.net/v2/jpg/02/40/20/89/1000_F_240208923_VW0K7K2CKSBORxHaq0FyDyjt4qJLuCjG.jpg',
        'en'),
       ('poi_3_2',
        '(38.73715291107833, -9.302635744572406)',
        'https://www.nasa.gov/',
        'https://i.pinimg.com/originals/8c/27/19/8c27193fdf569ffb89f1279730fc7224.jpg',
        'en'),
       ('poi_3_3',
        '(38.73715291107833, -9.302635744572406)',
        'https://en.wikipedia.org/wiki/Snack_bar',
        'https://media.timeout.com/images/105208214/image.jpg',
        'en');

INSERT INTO poi_lang (poi_id, lang, name, description)
VALUES ('poi_3_1',
        'pt',
        'Escritório',
        'Um escritório é um espaço onde os funcionários de uma organização realizam trabalhos administrativos para apoiar e realizar objetos e objetivos da organização. A palavra "cargo" também pode denotar um cargo dentro de uma organização com funções específicas associadas a ele (ver diretor, titular de cargo, funcionário); o último é de fato um uso anterior.'),
       ('poi_3_1',
        'en',
        'Office',
        'An office is a space where an organization''s employees perform administrative work in order to support and realize objects and goals of the organization. The word "office" may also denote a position within an organization with specific duties attached to it (see officer, office-holder, official); the latter is in fact an earlier usage.'),
       ('poi_3_2',
        'pt',
        'NASA IST',
        'A Administração Nacional de Aeronáutica e Espaço é uma agência independente do governo federal dos EUA responsável pelo programa espacial civil, bem como pela aeronáutica e pesquisa espacial. A NASA foi criada em 1958, sucedendo o National Advisory Committee for Aeronautics.'),
       ('poi_3_2',
        'en',
        'NASA IST',
        'The National Aeronautics and Space Administration is an independent agency of the U.S. federal government responsible for the civilian space program, as well as aeronautics and space research. NASA was established in 1958, succeeding the National Advisory Committee for Aeronautics.'),
       ('poi_3_3',
        'pt',
        'Bar amarelo',
        'Uma lanchonete geralmente se refere a um balcão de comida barata que faz parte de uma estrutura permanente onde são vendidos salgadinhos e refeições leves.'),
       ('poi_3_3',
        'en',
        'Yellow bar',
        'A snack bar usually refers to an inexpensive food counter that is part of a permanent structure where snack foods and light meals are sold.');

INSERT INTO waypoint (id, poi_id, stay_for, confidence_threshold)
VALUES ('waypoint_3_1', 'poi_3_1', '20 seconds', 75),
       ('waypoint_3_2', 'poi_3_2', '20 seconds', 75),
       ('waypoint_3_3', 'poi_3_3', '20 seconds', 75);

INSERT INTO route_waypoint (route_id, waypoint_id, position)
VALUES ('route_3', 'waypoint_3_1', 1),
       ('route_3', 'waypoint_3_2', 2),
       ('route_3', 'waypoint_3_3', 3);

-- Wi-Fi Networks

INSERT INTO wifiap (bssid, type, trigger, totp_secret, totp_regexp)
VALUES ('6c:99:89:98:f8:a1', 'untrusted', true, NULL, NULL),
       ('0c:68:03:4b:cc:c1', 'untrusted', true, NULL, NULL),
       ('6c:99:89:9c:f0:41', 'untrusted', true, NULL, NULL),
       ('1c:1d:86:2a:2b:d1', 'untrusted', true, NULL, NULL),
       ('70:4f:57:61:bd:fb', 'untrusted', true, NULL, NULL),
       ('6c:99:89:b1:1b:20', 'untrusted', true, NULL, NULL),
       ('70:4f:57:61:bd:fa', 'untrusted', true, NULL, NULL),
       ('0c:68:03:4b:cc:c0', 'untrusted', true, NULL, NULL),
       ('6c:99:89:9c:f0:40', 'untrusted', true, NULL, NULL),
       ('0c:68:03:49:cc:d0', 'untrusted', true, NULL, NULL),
       ('0c:68:03:49:cc:d1', 'untrusted', true, NULL, NULL),
       ('24:01:c7:6d:ae:a1', 'untrusted', true, NULL, NULL),
       ('6c:99:89:a9:5e:80', 'untrusted', true, NULL, NULL),
       ('0c:68:03:d4:70:41', 'untrusted', true, NULL, NULL),
       ('24:01:c7:6d:ae:a0', 'untrusted', true, NULL, NULL),
       ('00:24:97:9f:95:90', 'untrusted', true, NULL, NULL),
       ('6c:99:89:a9:63:f1', 'untrusted', true, NULL, NULL),
       ('6c:99:89:a0:fe:91', 'untrusted', true, NULL, NULL),
       ('00:24:97:9f:6f:50', 'untrusted', true, NULL, NULL),
       ('6c:99:89:a7:64:60', 'untrusted', true, NULL, NULL),
       ('a0:3d:6f:86:7d:e4', 'untrusted', true, NULL, NULL),
       ('0c:68:03:d4:70:40', 'untrusted', true, NULL, NULL),
       ('2e:97:b1:40:56:88', 'untrusted', true, NULL, NULL),
       ('a0:3d:6f:86:7d:e3', 'untrusted', true, NULL, NULL),
       ('5c:a4:8a:69:e4:a0', 'untrusted', true, NULL, NULL),
       ('6c:99:89:a9:5e:81', 'untrusted', true, NULL, NULL),
       ('a0:3d:6f:86:7d:e1', 'untrusted', true, NULL, NULL),
       ('12:13:31:cf:27:d5', 'untrusted', true, NULL, NULL),
       ('6c:99:89:a9:63:f0', 'untrusted', true, NULL, NULL),
       ('6c:99:89:a9:63:f2', 'untrusted', true, NULL, NULL),
       ('5c:a4:8a:69:e7:80', 'untrusted', true, NULL, NULL),
       ('6c:99:89:a0:fe:90', 'untrusted', true, NULL, NULL),
       ('6c:99:89:a7:64:62', 'untrusted', true, NULL, NULL),
       ('00:24:c4:2d:1e:00', 'untrusted', true, NULL, NULL),
       ('a0:3d:6f:86:7d:e0', 'untrusted', true, NULL, NULL),
       ('00:24:c4:2d:1e:01', 'untrusted', true, NULL, NULL),
       ('6c:99:89:a9:63:60', 'untrusted', true, NULL, NULL),
       ('00:24:97:fb:93:41', 'untrusted', true, NULL, NULL),
       ('6c:99:89:a9:5e:82', 'untrusted', true, NULL, NULL),
       ('a0:3d:6f:86:7d:e5', 'untrusted', true, NULL, NULL),
       ('00:24:97:fb:93:40', 'untrusted', true, NULL, NULL),
       ('a0:3d:6f:86:7d:e2', 'untrusted', true, NULL, NULL),
       ('6c:99:89:af:12:b0', 'untrusted', true, NULL, NULL),
       ('6c:99:89:b1:1a:71', 'untrusted', true, NULL, NULL),
       ('6c:99:89:9c:e6:60', 'untrusted', true, NULL, NULL),
       ('6c:99:89:b1:1a:72', 'untrusted', true, NULL, NULL),
       ('00:24:97:9f:95:70', 'untrusted', true, NULL, NULL),
       ('6c:99:89:af:13:b1', 'untrusted', true, NULL, NULL),
       ('6c:99:89:af:1a:f2', 'untrusted', true, NULL, NULL),
       ('6c:99:89:af:1a:f1', 'untrusted', true, NULL, NULL),
       ('6c:99:89:af:13:b0', 'untrusted', true, NULL, NULL),
       ('6c:99:89:a3:00:10', 'untrusted', true, NULL, NULL),
       ('6c:99:89:a1:00:90', 'untrusted', true, NULL, NULL),
       ('6c:99:89:af:1b:a0', 'untrusted', true, NULL, NULL),
       ('5c:a4:8a:67:ee:00', 'untrusted', true, NULL, NULL),
       ('6c:99:89:b1:13:30', 'untrusted', true, NULL, NULL),
       ('5c:a4:8a:67:ee:01', 'untrusted', true, NULL, NULL),
       ('6c:99:89:a1:00:91', 'untrusted', true, NULL, NULL),
       ('6c:99:89:98:ee:b0', 'untrusted', true, NULL, NULL),
       ('6c:99:89:b1:1a:70', 'untrusted', true, NULL, NULL),
       ('6c:99:89:af:1a:f0', 'untrusted', true, NULL, NULL),
       ('5c:a4:8a:67:f3:50', 'untrusted', true, NULL, NULL),
       ('5c:a4:8a:69:ed:b0', 'untrusted', true, NULL, NULL),
       ('6c:99:89:9c:e6:61', 'untrusted', true, NULL, NULL),
       ('6c:99:89:af:12:b1', 'untrusted', true, NULL, NULL),
       ('5c:a4:8a:69:ed:b1', 'untrusted', true, NULL, NULL),
       ('5c:a4:8a:67:f3:51', 'untrusted', true, NULL, NULL),
       ('5c:a4:8a:69:f3:01', 'untrusted', true, NULL, NULL);

INSERT INTO poi_wifiap (poi_id, wifiap_bssid)
VALUES ('poi_3_1', '6c:99:89:98:f8:a1'),
       ('poi_3_1', '0c:68:03:4b:cc:c1'),
       ('poi_3_1', '6c:99:89:9c:f0:41'),
       ('poi_3_1', '1c:1d:86:2a:2b:d1'),
       ('poi_3_1', '70:4f:57:61:bd:fb'),
       ('poi_3_1', '6c:99:89:b1:1b:20'),
       ('poi_3_1', '70:4f:57:61:bd:fa'),
       ('poi_3_1', '0c:68:03:4b:cc:c0'),
       ('poi_3_1', '6c:99:89:9c:f0:40'),
       ('poi_3_1', '0c:68:03:49:cc:d0'),
       ('poi_3_1', '0c:68:03:49:cc:d1');

INSERT INTO poi_wifiap (poi_id, wifiap_bssid)
VALUES ('poi_3_2', '24:01:c7:6d:ae:a1'),
       ('poi_3_2', '6c:99:89:a9:5e:80'),
       ('poi_3_2', '0c:68:03:d4:70:41'),
       ('poi_3_2', '24:01:c7:6d:ae:a0'),
       ('poi_3_2', '00:24:97:9f:95:90'),
       ('poi_3_2', '6c:99:89:a9:63:f1'),
       ('poi_3_2', '6c:99:89:a0:fe:91'),
       ('poi_3_2', '00:24:97:9f:6f:50'),
       ('poi_3_2', '6c:99:89:a7:64:60'),
       ('poi_3_2', 'a0:3d:6f:86:7d:e4'),
       ('poi_3_2', '0c:68:03:d4:70:40'),
       ('poi_3_2', '2e:97:b1:40:56:88'),
       ('poi_3_2', 'a0:3d:6f:86:7d:e3'),
       ('poi_3_2', '5c:a4:8a:69:e4:a0'),
       ('poi_3_2', '6c:99:89:a9:5e:81'),
       ('poi_3_2', 'a0:3d:6f:86:7d:e1'),
       ('poi_3_2', '12:13:31:cf:27:d5'),
       ('poi_3_2', '6c:99:89:a9:63:f0'),
       ('poi_3_2', '6c:99:89:a9:63:f2'),
       ('poi_3_2', '5c:a4:8a:69:e7:80'),
       ('poi_3_2', '6c:99:89:a0:fe:90'),
       ('poi_3_2', '6c:99:89:a7:64:62'),
       ('poi_3_2', '00:24:c4:2d:1e:00'),
       ('poi_3_2', 'a0:3d:6f:86:7d:e0'),
       ('poi_3_2', '00:24:c4:2d:1e:01'),
       ('poi_3_2', '6c:99:89:a9:63:60'),
       ('poi_3_2', '00:24:97:fb:93:41'),
       ('poi_3_2', '6c:99:89:a9:5e:82'),
       ('poi_3_2', 'a0:3d:6f:86:7d:e5'),
       ('poi_3_2', '00:24:97:fb:93:40'),
       ('poi_3_2', 'a0:3d:6f:86:7d:e2');

INSERT INTO poi_wifiap (poi_id, wifiap_bssid)
VALUES ('poi_3_3', '6c:99:89:af:12:b0'),
       ('poi_3_3', '6c:99:89:b1:1a:71'),
       ('poi_3_3', '6c:99:89:9c:e6:60'),
       ('poi_3_3', '6c:99:89:b1:1a:72'),
       ('poi_3_3', '00:24:97:9f:95:70'),
       ('poi_3_3', '6c:99:89:af:13:b1'),
       ('poi_3_3', '6c:99:89:af:1a:f2'),
       ('poi_3_3', '6c:99:89:af:1a:f1'),
       ('poi_3_3', '6c:99:89:af:13:b0'),
       ('poi_3_3', '6c:99:89:a3:00:10'),
       ('poi_3_3', '6c:99:89:a1:00:90'),
       ('poi_3_3', '6c:99:89:af:1b:a0'),
       ('poi_3_3', '5c:a4:8a:67:ee:00'),
       ('poi_3_3', '6c:99:89:b1:13:30'),
       ('poi_3_3', '5c:a4:8a:67:ee:01'),
       ('poi_3_3', '6c:99:89:a1:00:91'),
       ('poi_3_3', '6c:99:89:98:ee:b0'),
       ('poi_3_3', '6c:99:89:b1:1a:70'),
       ('poi_3_3', '6c:99:89:af:1a:f0'),
       ('poi_3_3', '5c:a4:8a:67:f3:50'),
       ('poi_3_3', '5c:a4:8a:69:ed:b0'),
       ('poi_3_3', '6c:99:89:9c:e6:61'),
       ('poi_3_3', '6c:99:89:af:12:b1'),
       ('poi_3_3', '5c:a4:8a:69:ed:b1'),
       ('poi_3_3', '5c:a4:8a:67:f3:51'),
       ('poi_3_3', '5c:a4:8a:69:f3:01');

------------------------------
-- Route of CROSS Scavenger - Demo
------------------------------

INSERT INTO route (id, position, image_url, main_locale)
VALUES ('route_4',
        3,
        'https://www.planetware.com/photos-large/P/st-georges-castle.jpg',
        'pt');

INSERT INTO route_lang (route_id, lang, name, description)
VALUES ('route_4',
        'pt',
        'CROSS Scavenger - Demo',
        'Rota usada no contexto do CROSS Scavenger para motivos de demonstração.'),
       ('route_4',
        'en',
        'CROSS Scavenger - Demo',
        'Route used in the CROSS Scavenger context for demonstration purposes.');

INSERT INTO poi (id, world_coord, web_url, image_url, main_locale)
VALUES ('alvalade',
        '(38.754438597, -9.146783678)',
        'https://pt.wikipedia.org/wiki/Alvalade_(Lisboa)',
        'https://offloadmedia.feverup.com/lisboasecreta.co/wp-content/uploads/2020/10/19150815/%40Junta-de-Freguesia-de-Alvalade-1024x683.jpg',
        'pt'),
       ('comercio',
        '(38.709016121, -9.136898741)',
        'https://pt.wikipedia.org/wiki/Rua_do_Com%C3%A9rcio',
        'https://toponimialisboa.files.wordpress.com/2015/05/0-rua-do-comc3a9rcio.jpg',
        'pt'),
       ('gulbenkian',
        '(38.737831, -9.153553)',
        'https://gulbenkian.pt/',
        'https://gulbenkian.pt/wp-content/uploads/2017/07/Dia-Calouste-Gulbenkian.jpg',
        'pt'),
       ('jeronimos',
        '(38.697520754, -9.205973155)',
        'http://www.patrimoniocultural.gov.pt/pt/museus-e-monumentos/dgpc/m/mosteiro-dos-jeronimos/',
        'https://upload.wikimedia.org/wikipedia/commons/thumb/d/d6/The_Jer%C3%B3nimos_Monastery_or_Hieronymites_Monastery.png/265px-The_Jer%C3%B3nimos_Monastery_or_Hieronymites_Monastery.png',
        'pt'),
       ('oceanario',
        '(38.763543, -9.093752)',
        'https://www.oceanario.pt/',
        'https://dynamic-media-cdn.tripadvisor.com/media/photo-o/1a/9c/96/f3/caption.jpg?w=300&h=300&s=1',
        'pt'),
       ('sb',
        '(38.709743545, -9.133603441)',
        'https://pt.wikipedia.org/wiki/S%C3%A9_(Lisboa)',
        'https://cdn.olhares.com/client/files/foto/big/704/7045749.jpg',
        'pt');

INSERT INTO poi_lang (poi_id, lang, name, description)
VALUES ('alvalade',
        'pt',
        'Alvalade',
        'Alvalade é uma freguesia e distrito de Lisboa, capital de Portugal. Localizada no centro de Lisboa, Alvalade fica a sul de Lumiar e Olivais, a oeste de Marvila, a leste de São Domingos de Benfica e a norte das Avenidas Novas e Areeiro.'),
       ('alvalade',
        'en',
        'Alvalade',
        'Alvalade is a freguesia and district of Lisbon, the capital of Portugal. Located in central Lisbon, Alvalade is south of Lumiar and Olivais, west of Marvila, east of São Domingos de Benfica, and north of Avenidas Novas and Areeiro.'),
       ('comercio',
        'pt',
        'Comércio',
        'A rua Augusta liga a Praça do Comércio à Praça do Rossio.'),
       ('comercio',
        'en',
        'Comércio',
        'Rua Augusta connects Praça do Comércio to Praça do Rossio.'),
       ('gulbenkian',
        'pt',
        'Museu Calouste Gulbenkian',
        'A Fundação Calouste Gulbenkian, vulgarmente designada por Fundação Gulbenkian, é uma instituição portuguesa dedicada à promoção das artes, filantropia, ciência e educação.'),
       ('gulbenkian',
        'en',
        'Calouste Gulbenkian Museum',
        'The Calouste Gulbenkian Foundation, commonly referred to simply as the Gulbenkian Foundation, is a Portuguese institution dedicated to the promotion of the arts, philanthropy, science, and education.'),
       ('jeronimos',
        'pt',
        'Mosteiro dos Jerónimos',
        'O Mosteiro dos Jerónimos ou Mosteiro dos Jerónimos é um antigo mosteiro da Ordem de São Jerónimo perto do rio Tejo, na freguesia de Belém, no concelho de Lisboa, Portugal.'),
       ('jeronimos',
        'en',
        'Jerónimos Monastery',
        'The Jerónimos Monastery or Hieronymites Monastery is a former monastery of the Order of Saint Jerome near the Tagus river in the parish of Belém, in the Lisbon Municipality, Portugal.'),
       ('oceanario',
        'pt',
        'Oceanário de Lisboa',
        'O Oceanário de Lisboa é um aquário público de referência em Lisboa.'),
       ('oceanario',
        'en',
        'Lisbon Oceanarium',
        'The Lisbon Oceanarium is a reference public aquarium in Lisbon.'),
       ('sb',
        'pt',
        'Sé',
        'A Sé é uma parte da cidade e antiga freguesia portuguesa do concelho de Lisboa.'),
       ('sb',
        'en',
        'Sé',
        'The Sé is a part of the city and former Portuguese parish in the municipality of Lisbon.');

INSERT INTO waypoint (id, poi_id, stay_for, confidence_threshold)
VALUES ('waypoint_4_1', 'alvalade', '20 seconds', 75),
       ('waypoint_4_2', 'comercio', '20 seconds', 75),
       ('waypoint_4_3', 'gulbenkian', '20 seconds', 75),
       ('waypoint_4_4', 'jeronimos', '20 seconds', 75),
       ('waypoint_4_5', 'oceanario', '20 seconds', 75),
       ('waypoint_4_6', 'sb', '20 seconds', 75);

INSERT INTO route_waypoint (route_id, waypoint_id, position)
VALUES ('route_4', 'waypoint_4_1', 1),
       ('route_4', 'waypoint_4_2', 2),
       ('route_4', 'waypoint_4_3', 3),
       ('route_4', 'waypoint_4_4', 4),
       ('route_4', 'waypoint_4_5', 5),
       ('route_4', 'waypoint_4_6', 6);

-- Badges

INSERT INTO badge (id, position, image_url, main_locale)
VALUES ('10_endorsements', 2, 'https://web.tecnico.ulisboa.pt/ist190774/CROSS/images/10_endorsements.png', 'en'),
       ('100_endorsements', 3, 'https://web.tecnico.ulisboa.pt/ist190774/CROSS/images/100_endorsements.png', 'en'),
       ('1000_endorsements', 4, 'https://web.tecnico.ulisboa.pt/ist190774/CROSS/images/1000_endorsements.png', 'en'),
       ('3_visits', 5, 'https://web.tecnico.ulisboa.pt/ist190774/CROSS/images/3_visits.png', 'en'),
       ('15_visits', 6, 'https://web.tecnico.ulisboa.pt/ist190774/CROSS/images/15_visits.png', 'en'),
       ('30_visits', 7, 'https://web.tecnico.ulisboa.pt/ist190774/CROSS/images/30_visits.png', 'en'),
       ('1_routes', 1, 'https://web.tecnico.ulisboa.pt/ist190774/CROSS/images/1_routes.png', 'en');

INSERT INTO badge_lang (badge_id, lang, name, quest, achievement)
VALUES ('10_endorsements', 'pt', 'Endossante de Dezenas',
        'Receberá este selo assim que endossar 10 reivindicações de localização de pares, para isso, basta visitar os pontos de interesse com a estratégia de testemunho de pares ativa (pode ativá-la e desativá-la nas definições).',
        'Endossou 10 reivindicações de localização, ajudando assim a certificar 10 visitas dos seus pares, parabéns pela sua conquista!'),
       ('10_endorsements', 'en', 'Endorser of Tens',
        'You will earn this badge once you have endorsed 10 peer location claims, for that you just have to visit the points of interest with the peer witness strategy active (you can turn it on and off in the settings).',
        'You''ve endorsed 10 location claims, thereby helping to certify 10 visits from your peers, congratulations on your achievement!'),
       ('100_endorsements', 'pt', 'Endossante de Centenas',
        'Receberá este selo assim que endossar 100 reivindicações de localização de pares, para isso, basta visitar os pontos de interesse com a estratégia de testemunho de pares ativa (pode ativá-la e desativá-la nas definições).',
        'Endossou 100 reivindicações de localização, ajudando assim a certificar 100 visitas dos seus pares, parabéns pela sua conquista!'),
       ('100_endorsements', 'en', 'Endorser of Hundreds',
        'You will earn this badge once you have endorsed 100 peer location claims, for that you just have to visit the points of interest with the peer witness strategy active (you can turn it on and off in the settings).',
        'You''ve endorsed 100 location claims, thereby helping to certify 100 visits from your peers, congratulations on your achievement!'),
       ('1000_endorsements', 'pt', 'Endossante de Milhares',
        'Receberá este selo assim que endossar 1000 reivindicações de localização de pares, para isso, basta visitar os pontos de interesse com a estratégia de testemunho de pares ativa (pode ativá-la e desativá-la nas definições).',
        'Endossou 1000 reivindicações de localização, ajudando assim a certificar 1000 visitas dos seus pares, parabéns pela sua conquista!'),
       ('1000_endorsements', 'en', 'Endorser of Thousands',
        'You will earn this badge once you have endorsed 1000 peer location claims, for that you just have to visit the points of interest with the peer witness strategy active (you can turn it on and off in the settings).',
        'You''ve endorsed 1000 location claims, thereby helping to certify 1000 visits from your peers, congratulations on your achievement!'),
       ('3_visits', 'pt', 'Visitante',
        'Receberá este selo quando completar as suas 3 primeiras visitas.',
        'Parabéns! Já completou as suas primeiras 3 visitas, agora possui o título de Visitante!'),
       ('3_visits', 'en', 'Visitor',
        'You will receive this badge when you complete your first 3 visits.',
        'Congratulations! You have already completed your first 3 visits, you now possess the title of Visitor!'),
       ('15_visits', 'pt', 'Explorador',
        'Receberá este selo quando completar as suas 3 primeiras visitas.',
        'Parabéns! Já completou as suas primeiras 3 visitas, agora possui o título de Explorador!'),
       ('15_visits', 'en', 'Explorer',
        'You will receive this badge when you complete your first 15 visits.',
        'Congratulations! You have already completed your first 15 visits, you now possess the title of Explorer!'),
       ('30_visits', 'pt', 'Descobridor',
        'Receberá este selo quando completar as suas 30 primeiras visitas.',
        'Parabéns! Já completou as suas primeiras 30 visitas, agora possui o título de Descobridor!'),
       ('30_visits', 'en', 'Discoverer',
        'You will receive this badge when you complete your first 30 visits.',
        'Congratulations! You have already completed your first 30 visits, you now possess the title of Discoverer!'),
       ('1_routes', 'pt', 'Viajante',
        'Receberá este selo assim que concluir a sua primeira rota.',
        'Parabéns! Já completou a sua primeira rota, agora possui o título de Viajante!'),
       ('1_routes', 'en', 'Traveler',
        'You will receive this badge as soon as you complete your first route.',
        'Congratulations! You have already completed your first route, you now possess the title of Traveler!');

------------------------------
-- Dataset version of the data
------------------------------

INSERT INTO dataset (id, version)
VALUES ('latest', '2022-07-23T12:34:17.280Z');

-- Dummy Users

INSERT INTO cross_user (username, password_hash, password_salt, gems, joined)
VALUES ('alice',
        decode(
                '5A37255468F647A9CB33F7DB1326EB4669C80488E4342624EC36D3D6A66EFCFC2D97401AA6DDBB0F3E4EE47F1005E84FC58C3449CA393D07CDF812B96775DF97',
                'hex'),
        decode(
                '8C811A6568ACCF4FFE91D83AE1476711',
                'hex'),
        0, '2020-03-25T18:47:00.000Z'),
       ('bob',
        decode(
                '5A37255468F647A9CB33F7DB1326EB4669C80488E4342624EC36D3D6A66EFCFC2D97401AA6DDBB0F3E4EE47F1005E84FC58C3449CA393D07CDF812B96775DF97',
                'hex'),
        decode(
                '8C811A6568ACCF4FFE91D83AE1476711',
                'hex'),
        0, '2020-03-25T18:47:00.000Z'),
       ('charlie',
        decode(
                '5A37255468F647A9CB33F7DB1326EB4669C80488E4342624EC36D3D6A66EFCFC2D97401AA6DDBB0F3E4EE47F1005E84FC58C3449CA393D07CDF812B96775DF97',
                'hex'),
        decode(
                '8C811A6568ACCF4FFE91D83AE1476711',
                'hex'),
        0, '2020-03-25T18:47:00.000Z'),
       ('muggletortilla',
        decode(
                '5A37255468F647A9CB33F7DB1326EB4669C80488E4342624EC36D3D6A66EFCFC2D97401AA6DDBB0F3E4EE47F1005E84FC58C3449CA393D07CDF812B96775DF97',
                'hex'),
        decode(
                '8C811A6568ACCF4FFE91D83AE1476711',
                'hex'),
        500, '2020-03-25T18:47:00.000Z'),
       ('vellabubbly',
        decode(
                '5A37255468F647A9CB33F7DB1326EB4669C80488E4342624EC36D3D6A66EFCFC2D97401AA6DDBB0F3E4EE47F1005E84FC58C3449CA393D07CDF812B96775DF97',
                'hex'),
        decode(
                '8C811A6568ACCF4FFE91D83AE1476711',
                'hex'),
        500, '2020-03-25T18:47:00.000Z'),
       ('zonkohippie',
        decode(
                '5A37255468F647A9CB33F7DB1326EB4669C80488E4342624EC36D3D6A66EFCFC2D97401AA6DDBB0F3E4EE47F1005E84FC58C3449CA393D07CDF812B96775DF97',
                'hex'),
        decode(
                '8C811A6568ACCF4FFE91D83AE1476711',
                'hex'),
        500, '2020-03-25T18:47:00.000Z'),
       ('mcboonrobe',
        decode(
                '5A37255468F647A9CB33F7DB1326EB4669C80488E4342624EC36D3D6A66EFCFC2D97401AA6DDBB0F3E4EE47F1005E84FC58C3449CA393D07CDF812B96775DF97',
                'hex'),
        decode(
                '8C811A6568ACCF4FFE91D83AE1476711',
                'hex'),
        500, '2020-03-25T18:47:00.000Z'),
       ('griphookrowing',
        decode(
                '5A37255468F647A9CB33F7DB1326EB4669C80488E4342624EC36D3D6A66EFCFC2D97401AA6DDBB0F3E4EE47F1005E84FC58C3449CA393D07CDF812B96775DF97',
                'hex'),
        decode(
                '8C811A6568ACCF4FFE91D83AE1476711',
                'hex'),
        500, '2020-03-25T18:47:00.000Z'),
       ('pomfreylibrary',
        decode(
                '5A37255468F647A9CB33F7DB1326EB4669C80488E4342624EC36D3D6A66EFCFC2D97401AA6DDBB0F3E4EE47F1005E84FC58C3449CA393D07CDF812B96775DF97',
                'hex'),
        decode(
                '8C811A6568ACCF4FFE91D83AE1476711',
                'hex'),
        500, '2020-03-25T18:47:00.000Z');

-- Dummy Scores

INSERT INTO user_score (username, timestamp, awarded_score)
VALUES ('muggletortilla', '2022-03-07T18:23:27.361Z', 10),
       ('muggletortilla', '2022-03-07T18:23:27.361Z', 10),
       ('muggletortilla', '2022-03-07T18:23:27.361Z', 10),
       ('muggletortilla', '2022-03-07T18:23:27.361Z', 10),
       ('muggletortilla', '2022-03-07T18:23:27.361Z', 10),
       ('muggletortilla', '2022-03-07T18:23:27.361Z', 50),
       ('muggletortilla', '2022-07-11T18:23:27.361Z', 10),
       ('muggletortilla', '2022-07-11T18:23:27.361Z', 10),
       ('muggletortilla', '2022-07-11T18:23:27.361Z', 10),
       ('muggletortilla', '2022-07-11T18:23:27.361Z', 10),
       ('muggletortilla', '2022-07-11T18:23:27.361Z', 10),
       ('muggletortilla', '2022-07-11T18:23:27.361Z', 50),
       ('muggletortilla', '2022-07-17T18:23:27.361Z', 10),
       ('muggletortilla', '2022-07-17T18:23:27.361Z', 10),
       ('muggletortilla', '2022-07-17T18:23:27.361Z', 10),
       ('muggletortilla', '2022-07-17T18:23:27.361Z', 10),
       ('muggletortilla', '2022-07-17T18:23:27.361Z', 10),
       ('muggletortilla', '2022-07-17T18:23:27.361Z', 50),
       ('vellabubbly', '2022-03-07T18:23:27.361Z', 10),
       ('vellabubbly', '2022-03-07T18:23:27.361Z', 10),
       ('vellabubbly', '2022-03-07T18:23:27.361Z', 10),
       ('vellabubbly', '2022-03-07T18:23:27.361Z', 10),
       ('vellabubbly', '2022-03-07T18:23:27.361Z', 50),
       ('vellabubbly', '2022-03-07T18:23:27.361Z', 50),
       ('vellabubbly', '2022-07-11T18:23:27.361Z', 10),
       ('vellabubbly', '2022-07-11T18:23:27.361Z', 10),
       ('vellabubbly', '2022-07-11T18:23:27.361Z', 10),
       ('vellabubbly', '2022-07-11T18:23:27.361Z', 10),
       ('vellabubbly', '2022-07-11T18:23:27.361Z', 50),
       ('vellabubbly', '2022-07-11T18:23:27.361Z', 50),
       ('vellabubbly', '2022-07-17T18:23:27.361Z', 10),
       ('vellabubbly', '2022-07-17T18:23:27.361Z', 10),
       ('vellabubbly', '2022-07-17T18:23:27.361Z', 10),
       ('vellabubbly', '2022-07-17T18:23:27.361Z', 10),
       ('vellabubbly', '2022-07-17T18:23:27.361Z', 50),
       ('vellabubbly', '2022-07-17T18:23:27.361Z', 50),
       ('zonkohippie', '2022-03-07T18:23:27.361Z', 10),
       ('zonkohippie', '2022-03-07T18:23:27.361Z', 10),
       ('zonkohippie', '2022-03-07T18:23:27.361Z', 10),
       ('zonkohippie', '2022-03-07T18:23:27.361Z', 50),
       ('zonkohippie', '2022-03-07T18:23:27.361Z', 50),
       ('zonkohippie', '2022-03-07T18:23:27.361Z', 50),
       ('zonkohippie', '2022-07-11T18:23:27.361Z', 10),
       ('zonkohippie', '2022-07-11T18:23:27.361Z', 10),
       ('zonkohippie', '2022-07-11T18:23:27.361Z', 10),
       ('zonkohippie', '2022-07-11T18:23:27.361Z', 50),
       ('zonkohippie', '2022-07-11T18:23:27.361Z', 50),
       ('zonkohippie', '2022-07-11T18:23:27.361Z', 50),
       ('zonkohippie', '2022-07-17T18:23:27.361Z', 10),
       ('zonkohippie', '2022-07-17T18:23:27.361Z', 10),
       ('zonkohippie', '2022-07-17T18:23:27.361Z', 10),
       ('zonkohippie', '2022-07-17T18:23:27.361Z', 50),
       ('zonkohippie', '2022-07-17T18:23:27.361Z', 50),
       ('zonkohippie', '2022-07-17T18:23:27.361Z', 50),
       ('mcboonrobe', '2022-03-07T18:23:27.361Z', 10),
       ('mcboonrobe', '2022-03-07T18:23:27.361Z', 10),
       ('mcboonrobe', '2022-03-07T18:23:27.361Z', 50),
       ('mcboonrobe', '2022-03-07T18:23:27.361Z', 50),
       ('mcboonrobe', '2022-03-07T18:23:27.361Z', 50),
       ('mcboonrobe', '2022-03-07T18:23:27.361Z', 50),
       ('mcboonrobe', '2022-07-11T18:23:27.361Z', 10),
       ('mcboonrobe', '2022-07-11T18:23:27.361Z', 10),
       ('mcboonrobe', '2022-07-11T18:23:27.361Z', 50),
       ('mcboonrobe', '2022-07-11T18:23:27.361Z', 50),
       ('mcboonrobe', '2022-07-11T18:23:27.361Z', 50),
       ('mcboonrobe', '2022-07-11T18:23:27.361Z', 50),
       ('mcboonrobe', '2022-07-17T18:23:27.361Z', 10),
       ('mcboonrobe', '2022-07-17T18:23:27.361Z', 10),
       ('mcboonrobe', '2022-07-17T18:23:27.361Z', 50),
       ('mcboonrobe', '2022-07-17T18:23:27.361Z', 50),
       ('mcboonrobe', '2022-07-17T18:23:27.361Z', 50),
       ('mcboonrobe', '2022-07-17T18:23:27.361Z', 50),
       ('griphookrowing', '2022-03-07T18:23:27.361Z', 10),
       ('griphookrowing', '2022-03-07T18:23:27.361Z', 50),
       ('griphookrowing', '2022-03-07T18:23:27.361Z', 50),
       ('griphookrowing', '2022-03-07T18:23:27.361Z', 50),
       ('griphookrowing', '2022-03-07T18:23:27.361Z', 50),
       ('griphookrowing', '2022-03-07T18:23:27.361Z', 50),
       ('griphookrowing', '2022-07-11T18:23:27.361Z', 10),
       ('griphookrowing', '2022-07-11T18:23:27.361Z', 50),
       ('griphookrowing', '2022-07-11T18:23:27.361Z', 50),
       ('griphookrowing', '2022-07-11T18:23:27.361Z', 50),
       ('griphookrowing', '2022-07-11T18:23:27.361Z', 50),
       ('griphookrowing', '2022-07-11T18:23:27.361Z', 50),
       ('griphookrowing', '2022-07-17T18:23:27.361Z', 10),
       ('griphookrowing', '2022-07-17T18:23:27.361Z', 50),
       ('griphookrowing', '2022-07-17T18:23:27.361Z', 50),
       ('griphookrowing', '2022-07-17T18:23:27.361Z', 50),
       ('griphookrowing', '2022-07-17T18:23:27.361Z', 50),
       ('griphookrowing', '2022-07-17T18:23:27.361Z', 50),
       ('pomfreylibrary', '2022-03-07T18:23:27.361Z', 50),
       ('pomfreylibrary', '2022-03-07T18:23:27.361Z', 50),
       ('pomfreylibrary', '2022-03-07T18:23:27.361Z', 50),
       ('pomfreylibrary', '2022-03-07T18:23:27.361Z', 50),
       ('pomfreylibrary', '2022-03-07T18:23:27.361Z', 50),
       ('pomfreylibrary', '2022-03-07T18:23:27.361Z', 50),
       ('pomfreylibrary', '2022-07-11T18:23:27.361Z', 50),
       ('pomfreylibrary', '2022-07-11T18:23:27.361Z', 50),
       ('pomfreylibrary', '2022-07-11T18:23:27.361Z', 50),
       ('pomfreylibrary', '2022-07-11T18:23:27.361Z', 50),
       ('pomfreylibrary', '2022-07-11T18:23:27.361Z', 50),
       ('pomfreylibrary', '2022-07-11T18:23:27.361Z', 50),
       ('pomfreylibrary', '2022-07-17T18:23:27.361Z', 50),
       ('pomfreylibrary', '2022-07-17T18:23:27.361Z', 50),
       ('pomfreylibrary', '2022-07-17T18:23:27.361Z', 50),
       ('pomfreylibrary', '2022-07-17T18:23:27.361Z', 50),
       ('pomfreylibrary', '2022-07-17T18:23:27.361Z', 50),
       ('pomfreylibrary', '2022-07-17T18:23:27.361Z', 50),
       ('pomfreylibrary', '2022-07-23T18:23:27.361Z', 50);

-- Dummy Badges

INSERT INTO user_badge (username, badge_id)
VALUES ('vellabubbly', '3_visits'),
       ('zonkohippie', '3_visits'),
       ('zonkohippie', '10_endorsements'),
       ('mcboonrobe', '1_routes'),
       ('mcboonrobe', '3_visits'),
       ('mcboonrobe', '10_endorsements'),
       ('mcboonrobe', '100_endorsements'),
       ('griphookrowing', '1_routes'),
       ('griphookrowing', '3_visits'),
       ('griphookrowing', '15_visits'),
       ('griphookrowing', '10_endorsements'),
       ('griphookrowing', '100_endorsements'),
       ('griphookrowing', '1000_endorsements'),
       ('pomfreylibrary', '1_routes'),
       ('pomfreylibrary', '3_visits'),
       ('pomfreylibrary', '15_visits'),
       ('pomfreylibrary', '30_visits'),
       ('pomfreylibrary', '10_endorsements'),
       ('pomfreylibrary', '100_endorsements'),
       ('pomfreylibrary', '1000_endorsements');
