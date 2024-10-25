create database battleship;
use battleship;

create table `user`(
	ID int auto_increment primary key,
    `username` varchar(255) unique,
    `password` varchar(255),
    `score` int default 0
);

insert into `user`(`username`, `password`) values ('huynh', '123');
insert into `user`(`username`, `password`) values ('quan', '123');
insert into `user`(`username`, `password`) values ('quang', '123');