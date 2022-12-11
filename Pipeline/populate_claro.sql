INSERT INTO `gsdsupport.cross_claro.users`(id, name) VALUES ("alice", "Alice"),
    ("bob", "Bob"),
    ("charlie", "Charlie");

INSERT INTO `gsdsupport.cross_claro.locations`(id, name, coordinates) VALUES ("gulbenkian", "Gulbenkian", "(38.737153911, -9.153319476)"),
    ("alvalade", "Alvalade", "(38.754528179, -9.146722011)"),
    ("comercio", "Comercio", "(38.708882852, -9.136965745)"),
    ("jeronimos", "Jeronimos", "(38.697520754, -9.205973155)"),
    ("oceanario", "Oceanario", "(38.762656434, -9.094146880)"),
    ("sb", "SB", "(38.709705890, -9.133649172)");

INSERT INTO `gsdsupport.cross_claro.devices`(id, name, user_id) VALUES ("A", "Samsung Galaxy S9", "alice"),
    ("B", "Huawei Mate 10", "bob"),
    ("C", "LG V10 thinq", "charlie");