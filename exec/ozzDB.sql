SET NAMES utf8mb4;

CREATE TABLE `board` (
	`board_id`	bigint	NOT NULL	AUTO_INCREMENT,
	`content`	varchar(200)	NULL,
	`created_date`	timestamp	NULL,
	`image_file_id`	bigint	NOT NULL,
	`users_id`	bigint	NOT NULL,
	`age`	int	NULL,
	`style`	bit	NULL,
	`numlikes`	int	NULL
);

CREATE TABLE `users` (
	`users_id`	bigint	NOT NULL	AUTO_INCREMENT,
	`email`	varchar(31)	NOT NULL	COMMENT '유저 이메일',
	`birth`	date	NOT NULL	COMMENT '유저 생일',
	`nickname`	varchar(15)	NULL	COMMENT '유저 닉네임',
	`profile_file_id`	bigint	NULL,
	`created_date`	timestamp	NULL,
	`is_guest`	bit(1)	NULL,
	`name`	varchar(255)	NOT NULL,
	`phone_number`	varchar(255)	NULL
);

INSERT INTO `users` (`email`, `birth`, `nickname`, `profile_file_id`, `created_date`, `is_guest`, `name`, `phone_number`)
VALUES ('demo@ozz.local', '2000-01-01', 'ozz-demo', NULL, CURRENT_TIMESTAMP, b'1', 'OZZ Demo', NULL);

CREATE TABLE `board_like` (
	`board_id`	bigint	NOT NULL,
	`users_id`	bigint	NOT NULL,
	`created_date`	timestamp	NULL
);

CREATE TABLE `files` (
	`file_id`	bigint	NOT NULL	AUTO_INCREMENT,
	`path`	varchar(255)	NULL,
	`name`	varchar(255)	NULL,
	`type`	varchar(50)	NULL,
	`upload_date`	timestamp	NULL
);

CREATE TABLE `coordinate` (
	`coordinate_id`	bigint	NOT NULL	AUTO_INCREMENT	COMMENT '코디 id',
	`style`	bit(10)	NULL,
	`name`	varchar(100)	NULL,
	`low_temper`	float	NULL,
	`high_temper`	float	NULL,
	`humidity`	tinyint	NULL,
	`weather`	tinyint	NULL,
	`created_date`	timestamp	NULL,
	`users_id`	bigint	NOT NULL,
	`image_file_id`	bigint	NOT NULL
);

CREATE TABLE `clothes` (
	`clothes_id`	bigint	NOT NULL	AUTO_INCREMENT,
	`image_file_id`	bigint	NOT NULL,
	`category_low_id`	tinyint	NOT NULL,
	`users_id`	bigint	NOT NULL,
	`color`	bit(32)	NULL,
	`name`	varchar(255)	NULL,
	`size`	tinyint	NULL,
	`fit`	tinyint	NULL,
	`texture`	bit(32)	NULL,
	`memo`	varchar(255)	NULL,
	`brand`	varchar(50)	NULL,
	`purchase_date`	timestamp	NULL,
	`purchase_site`	varchar(100)	NULL,
	`created_date`	timestamp	NULL,
	`style`	bit(10)	NULL,
	`season`	bit(4)	NULL
);

CREATE TABLE `category_high` (
	`category_high_id`	tinyint	NOT NULL	AUTO_INCREMENT,
	`name`	varchar(30)	NULL
);

CREATE TABLE `category_low` (
	`category_low_id`	tinyint	NOT NULL	AUTO_INCREMENT,
	`name`	varchar(30)	NULL,
	`category_high_id`	bigint	NOT NULL
);

CREATE TABLE `favorite` (
	`favorite_group_id`	bigint	NULL,
	`coordinate_id`	bigint	NOT NULL	COMMENT '코디 id'
);

CREATE TABLE `favorite_group` (
	`favorite_group_id`	bigint	NOT NULL	AUTO_INCREMENT,
	`users_id`	bigint	NOT NULL,
	`favorite_group_name`	varchar(255)	NOT NULL
);

CREATE TABLE `clothes_tag` (
	`clothes_tag_id`	bigint	NOT NULL	AUTO_INCREMENT,
	`board_id`	bigint	NOT NULL,
	`x_position`	float	NULL,
	`y_position`	float	NULL,
	`board_clothes_id`	bigint	NOT NULL
);

CREATE TABLE `coordinate_clothes` (
	`coordinate_id`	bigint	NOT NULL	COMMENT '코디 id',
	`clothes_id`	bigint	NOT NULL,
	`offset`	tinyint	NULL
);

CREATE TABLE `notification` (
	`notification_id`	bigint	NOT NULL	AUTO_INCREMENT,
	`content`	varchar(200)	NULL,
	`is_read`	tinyint	NULL,
	`created_date`	timestamp	NULL,
	`users_id`	bigint	NOT NULL,
	`board_id`	bigint	NOT NULL
);

CREATE TABLE `board_clothes` (
	`board_clothes_id`	bigint	NOT NULL	AUTO_INCREMENT,
	`image_file_id`	bigint	NOT NULL,
	`category_low_id`	tinyint	NOT NULL,
	`color`	bit(32)	NULL,
	`name`	varchar(255)	NULL,
	`size`	tinyint	NULL,
	`fit`	tinyint	NULL,
	`texture`	bit(20)	NULL,
	`memo`	varchar(255)	NULL,
	`brand`	varchar(50)	NULL,
	`style`	bit(10)	NULL,
	`season`	bit(4)	NULL
);

ALTER TABLE `board` ADD CONSTRAINT `PK_BOARD` PRIMARY KEY (
	`board_id`
);

ALTER TABLE `users` ADD CONSTRAINT `PK_USERS` PRIMARY KEY (
	`users_id`
);

ALTER TABLE `users` ADD CONSTRAINT `UK_USERS_EMAIL` UNIQUE (
	`email`
);

ALTER TABLE `board_like` ADD CONSTRAINT `PK_BOARD_LIKE` PRIMARY KEY (
	`board_id`,
	`users_id`
);

ALTER TABLE `files` ADD CONSTRAINT `PK_FILES` PRIMARY KEY (
	`file_id`
);

ALTER TABLE `coordinate` ADD CONSTRAINT `PK_COORDINATE` PRIMARY KEY (
	`coordinate_id`
);

ALTER TABLE `clothes` ADD CONSTRAINT `PK_CLOTHES` PRIMARY KEY (
	`clothes_id`
);

ALTER TABLE `category_high` ADD CONSTRAINT `PK_CATEGORY_HIGH` PRIMARY KEY (
	`category_high_id`
);

ALTER TABLE `category_low` ADD CONSTRAINT `PK_CATEGORY_LOW` PRIMARY KEY (
	`category_low_id`
);

ALTER TABLE `favorite` ADD CONSTRAINT `PK_FAVORITE` PRIMARY KEY (
	`favorite_group_id`,
	`coordinate_id`
);

ALTER TABLE `favorite_group` ADD CONSTRAINT `PK_FAVORITE_GROUP` PRIMARY KEY (
	`favorite_group_id`
);

ALTER TABLE `clothes_tag` ADD CONSTRAINT `PK_CLOTHES_TAG` PRIMARY KEY (
	`clothes_tag_id`
);

ALTER TABLE `coordinate_clothes` ADD CONSTRAINT `PK_COORDINATE_CLOTHES` PRIMARY KEY (
	`coordinate_id`,
	`clothes_id`
);

INSERT INTO `category_high` (`category_high_id`, `name`) VALUES
(1, '상의'),
(2, '하의'),
(3, '아우터'),
(4, '원피스'),
(5, '신발'),
(6, '악세서리'),
(7, '가방')
ON DUPLICATE KEY UPDATE `name` = VALUES(`name`);

INSERT INTO `category_low` (`category_low_id`, `name`, `category_high_id`) VALUES
(1, '탑', 1),
(2, '블라우스', 1),
(3, '티셔츠', 1),
(4, '니트웨어', 1),
(5, '셔츠', 1),
(6, '브라탑', 1),
(7, '후드티', 1),
(8, '청바지', 2),
(9, '팬츠', 2),
(10, '스커트', 2),
(11, '레깅스', 2),
(12, '조거팬츠', 2),
(13, '코트', 3),
(14, '재킷', 3),
(15, '점퍼', 3),
(16, '패딩', 3),
(17, '베스트', 3),
(18, '가디건', 3),
(19, '짚업', 3),
(20, '드레스', 4),
(21, '점프수트', 4),
(22, '운동화', 5),
(23, '구두', 5),
(24, '샌들', 5),
(25, '주얼리', 6),
(26, '기타', 6),
(27, '모자', 6),
(28, '가방', 7),
(29, '백팩', 7),
(30, '힙색', 7)
ON DUPLICATE KEY UPDATE
	`name` = VALUES(`name`),
	`category_high_id` = VALUES(`category_high_id`);

ALTER TABLE `notification` ADD CONSTRAINT `PK_NOTIFICATION` PRIMARY KEY (
	`notification_id`
);

ALTER TABLE `board_clothes` ADD CONSTRAINT `PK_BOARD_CLOTHES` PRIMARY KEY (
	`board_clothes_id`
);

ALTER TABLE `board_like` ADD CONSTRAINT `FK_board_TO_board_like_1` FOREIGN KEY (
	`board_id`
)
REFERENCES `board` (
	`board_id`
);

ALTER TABLE `board_like` ADD CONSTRAINT `FK_users_TO_board_like_1` FOREIGN KEY (
	`users_id`
)
REFERENCES `users` (
	`users_id`
);

ALTER TABLE `favorite` ADD CONSTRAINT `FK_favorite_group_TO_favorite_1` FOREIGN KEY (
	`favorite_group_id`
)
REFERENCES `favorite_group` (
	`favorite_group_id`
);

ALTER TABLE `favorite` ADD CONSTRAINT `FK_coordinate_TO_favorite_1` FOREIGN KEY (
	`coordinate_id`
)
REFERENCES `coordinate` (
	`coordinate_id`
);

ALTER TABLE `coordinate_clothes` ADD CONSTRAINT `FK_coordinate_TO_coordinate_clothes_1` FOREIGN KEY (
	`coordinate_id`
)
REFERENCES `coordinate` (
	`coordinate_id`
);

ALTER TABLE `coordinate_clothes` ADD CONSTRAINT `FK_clothes_TO_coordinate_clothes_1` FOREIGN KEY (
	`clothes_id`
)
REFERENCES `clothes` (
	`clothes_id`
);
