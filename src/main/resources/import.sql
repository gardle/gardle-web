insert into authority (name) values ('ROLE_USER');
insert into authority (name) values ('ROLE_ADMIN');

insert into gardle_user(id, login, password_hash, first_name, last_name, email, activated, lang_key, created_by, last_modified_by, bank_accountiban, payment_account_id) values (nextval('user_sequence_generator'), 'admin','$2a$10$gSAhZrxMllrbgj/kkK9UceBPpChGWJA7SYIb1Mqo.n5aNLq1/oRrC','Administrator','Administrator','admin@localhost',true,'de','system','system','AT89370400440532013000', 'acct_1FyTQhBri4fBj7W5');
insert into user_authority (user_id, authority_name) values (currval('user_sequence_generator'), 'ROLE_USER');
insert into user_authority (user_id, authority_name) values (currval('user_sequence_generator'), 'ROLE_ADMIN');

insert into gardle_user(id, login, password_hash, first_name, last_name, email, activated, lang_key, created_by, last_modified_by, bank_accountiban, payment_account_id) values (nextval('user_sequence_generator'), 'user','$2a$10$VEjxo0jq2YG9Rbk2HmX9S.k1uZBGYUHdUcid3g/vfiEl7lwWgOH/K','User','User','user@localhost',true,'de','system','system','AT89370400440532013000','acct_1FyTQfK9e7avPhy1');
insert into user_authority (user_id, authority_name) values (currval('user_sequence_generator'), 'ROLE_USER');
