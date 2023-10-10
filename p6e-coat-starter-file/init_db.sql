/*
 Navicat Premium Data Transfer

 Source Server Type    : PostgreSQL
 Source Catalog        : lds
 Source Schema         : public

 Target Server Type    : PostgreSQL
 Target Server Version : 120013 (120013)
 File Encoding         : 65001

 Date: 05/05/2023 14:46:58
*/


-- ----------------------------
-- Table structure for p6e_file_upload
-- ----------------------------
DROP TABLE IF EXISTS "public"."p6e_file_upload";
CREATE TABLE "public"."p6e_file_upload" (
  "id" int4 NOT NULL DEFAULT nextval('p6e_file_upload_seq_id'::regclass),
  "name" varchar(200) COLLATE "pg_catalog"."default" NOT NULL,
  "size" int8 NOT NULL DEFAULT 0,
  "source" varchar(50) COLLATE "pg_catalog"."default" NOT NULL,
  "storage_location" varchar(300) COLLATE "pg_catalog"."default" NOT NULL,
  "owner" varchar(50) COLLATE "pg_catalog"."default" NOT NULL,
  "create_date" timestamptz(0) NOT NULL,
  "update_date" timestamptz(0) NOT NULL,
  "rubbish" int2 NOT NULL DEFAULT 0,
  "operator" varchar(50) COLLATE "pg_catalog"."default" NOT NULL DEFAULT 'sys'::character varying,
  "lock" int2 NOT NULL DEFAULT 0,
  "version" int2 NOT NULL DEFAULT 0
)
;

-- ----------------------------
-- Records of p6e_file_upload
-- ----------------------------
INSERT INTO "public"."p6e_file_upload" VALUES (10026, '下载 (2).mp4', 3117079, 'SLICE_UPLOAD', '2023/05/05/e0a6a3136cab4f3785f0bdd7c7590f7cmibrna', 'sys', '2023-05-05 14:20:13+08', '2023-05-05 14:35:01+08', 0, 'sys', -1, 11);
INSERT INTO "public"."p6e_file_upload" VALUES (10028, 'hksi-bicycle-resource-1.0.0.jar', 92615271, 'SLICE_UPLOAD', '2023/05/05/9872147187534575806392dbf1742cd6zvqpzh', 'sys', '2023-05-05 14:35:53+08', '2023-05-05 14:36:02+08', 0, 'sys', -1, 37);
INSERT INTO "public"."p6e_file_upload" VALUES (10024, '微信图片_20230426230816.jpg', 0, 'SLICE_UPLOAD', '2023/05/05/2c1ddd9320fa48149629094c92a9c560xxhdun', 'sys', '2023-05-05 14:08:04+08', '2023-05-05 14:08:05+08', 0, 'sys', -1, 3);
INSERT INTO "public"."p6e_file_upload" VALUES (10025, '微信图片_20230426230816.jpg', 0, 'SLICE_UPLOAD', '2023/05/05/b4dd5c7b79d04d168cc942e666b9fddekc6zqk', 'sys', '2023-05-05 14:08:38+08', '2023-05-05 14:08:39+08', 0, 'sys', -1, 3);
INSERT INTO "public"."p6e_file_upload" VALUES (10022, '微信图片_20230426230816.jpg', 0, 'SLICE_UPLOAD', '2023/05/05/fa39056bfaef48dbbe558844b2a37a695uw5jq', 'sys', '2023-05-05 14:05:32+08', '2023-05-05 14:05:33+08', 0, 'sys', -1, 3);
INSERT INTO "public"."p6e_file_upload" VALUES (10023, '2b935b1ef78fb1f466a1519b0aa42adf.mp4', 0, 'SLICE_UPLOAD', '2023/05/05/9ad390521ddf493e9985c8b8afe95573uagcqc', 'sys', '2023-05-05 14:06:38+08', '2023-05-05 14:06:39+08', 0, 'sys', -1, 3);
INSERT INTO "public"."p6e_file_upload" VALUES (10027, 'WeChat_20230427092633.mp4', 0, 'SLICE_UPLOAD', '2023/05/05/2acf99829180499c94f10fd2101148f2dfb8ic', 'sys', '2023-05-05 14:21:58+08', '2023-05-05 14:21:58+08', 0, 'sys', 0, 3);

-- ----------------------------
-- Table structure for p6e_file_upload_chunk
-- ----------------------------
DROP TABLE IF EXISTS "public"."p6e_file_upload_chunk";
CREATE TABLE "public"."p6e_file_upload_chunk" (
  "id" int4 NOT NULL DEFAULT nextval('p6e_file_upload_chunk_seq_id'::regclass),
  "fid" int4 NOT NULL,
  "name" varchar(200) COLLATE "pg_catalog"."default" NOT NULL,
  "size" int8 NOT NULL DEFAULT 0,
  "date" timestamptz(0) NOT NULL,
  "operator" varchar(50) COLLATE "pg_catalog"."default" NOT NULL DEFAULT 'sys'::character varying
)
;

-- ----------------------------
-- Records of p6e_file_upload_chunk
-- ----------------------------
INSERT INTO "public"."p6e_file_upload_chunk" VALUES (10142, 10022, '0_133784648ddf4555813ec2aa1d3635b0bb2tja', 4296092, '2023-05-05 14:05:33+08', 'sys');
INSERT INTO "public"."p6e_file_upload_chunk" VALUES (10143, 10023, '0_6c621412a2934d48909c64f4980de017k79hhk', 4621686, '2023-05-05 14:06:39+08', 'sys');
INSERT INTO "public"."p6e_file_upload_chunk" VALUES (10144, 10024, '0_3fbfbda65ba34b169633970e88354d31434uty', 4296092, '2023-05-05 14:08:05+08', 'sys');
INSERT INTO "public"."p6e_file_upload_chunk" VALUES (10145, 10025, '0_b66ed3aa9b3349f89e1c5ca16218e27094ltnc', 4296092, '2023-05-05 14:08:39+08', 'sys');
INSERT INTO "public"."p6e_file_upload_chunk" VALUES (10146, 10026, '0_4b097f2cf56542a19a0d7125049dcbd5o5taz3', 3117079, '2023-05-05 14:20:14+08', 'sys');
INSERT INTO "public"."p6e_file_upload_chunk" VALUES (10147, 10027, '0_8a1c91d7708e4ccf8d97a0d0cfb408aaqdfoxy', 3277003, '2023-05-05 14:21:58+08', 'sys');
INSERT INTO "public"."p6e_file_upload_chunk" VALUES (10148, 10028, '0_8190233c8b75403c8e7a26789ea29a20ajys18', 5242880, '2023-05-05 14:35:54+08', 'sys');
INSERT INTO "public"."p6e_file_upload_chunk" VALUES (10149, 10028, '1_bf4b20ba90474e83a0f75240963baf4301v4dd', 5242880, '2023-05-05 14:35:54+08', 'sys');
INSERT INTO "public"."p6e_file_upload_chunk" VALUES (10150, 10028, '2_cab5b1c058464e1d90a30c326198dadf8cms2l', 5242880, '2023-05-05 14:35:55+08', 'sys');
INSERT INTO "public"."p6e_file_upload_chunk" VALUES (10151, 10028, '3_b75ee5160e764a7792fae37605fcdf34qxs3r9', 5242880, '2023-05-05 14:35:55+08', 'sys');
INSERT INTO "public"."p6e_file_upload_chunk" VALUES (10152, 10028, '4_c2a0ab3b54834fe9bbde55be5c249d24zi5uw0', 5242880, '2023-05-05 14:35:56+08', 'sys');
INSERT INTO "public"."p6e_file_upload_chunk" VALUES (10153, 10028, '5_163d8eb451e7460684b8d8331246cc8elgy6u6', 5242880, '2023-05-05 14:35:56+08', 'sys');
INSERT INTO "public"."p6e_file_upload_chunk" VALUES (10154, 10028, '6_16f70cfa11d04e5b81506fcc8dc0f2b4nmqvjt', 5242880, '2023-05-05 14:35:57+08', 'sys');
INSERT INTO "public"."p6e_file_upload_chunk" VALUES (10155, 10028, '7_cecfb7c94ea24c7686dc80c6ef1e346elf5lz7', 5242880, '2023-05-05 14:35:57+08', 'sys');
INSERT INTO "public"."p6e_file_upload_chunk" VALUES (10156, 10028, '8_1f31f5ea887d42af90415d17f31c7bfdvk5vv3', 5242880, '2023-05-05 14:35:58+08', 'sys');
INSERT INTO "public"."p6e_file_upload_chunk" VALUES (10157, 10028, '9_47c55e7f8ca04ca0ade0909a1242b9a4dpsx3i', 5242880, '2023-05-05 14:35:58+08', 'sys');
INSERT INTO "public"."p6e_file_upload_chunk" VALUES (10158, 10028, '10_84d1729778284bd9bd91bfe0944d5e2e0ia6b1', 5242880, '2023-05-05 14:35:58+08', 'sys');
INSERT INTO "public"."p6e_file_upload_chunk" VALUES (10159, 10028, '11_4dd33afdbe8244cab404d6bc050053e55seb8d', 5242880, '2023-05-05 14:35:59+08', 'sys');
INSERT INTO "public"."p6e_file_upload_chunk" VALUES (10160, 10028, '12_501c9f862b3e43ee993a7d9c62ee0222nxym2d', 5242880, '2023-05-05 14:35:59+08', 'sys');
INSERT INTO "public"."p6e_file_upload_chunk" VALUES (10161, 10028, '13_37d6c535db4d478ea071293d39acea292yz2kq', 5242880, '2023-05-05 14:36:00+08', 'sys');
INSERT INTO "public"."p6e_file_upload_chunk" VALUES (10162, 10028, '14_eda464aa590a49ac8fe80f8b24feefa6dkq538', 5242880, '2023-05-05 14:36:00+08', 'sys');
INSERT INTO "public"."p6e_file_upload_chunk" VALUES (10163, 10028, '15_270945a58a1545d2a71c2d4ac06ca741ejka7r', 5242880, '2023-05-05 14:36:01+08', 'sys');
INSERT INTO "public"."p6e_file_upload_chunk" VALUES (10164, 10028, '16_091c7024cd2548a48808c2686850207fo5n7dv', 5242880, '2023-05-05 14:36:01+08', 'sys');
INSERT INTO "public"."p6e_file_upload_chunk" VALUES (10165, 10028, '17_a455473959a044f799d645af26a86311wox3qs', 3486311, '2023-05-05 14:36:02+08', 'sys');

-- ----------------------------
-- Primary Key structure for table p6e_file_upload
-- ----------------------------
ALTER TABLE "public"."p6e_file_upload" ADD CONSTRAINT "p6e_file_upload_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Primary Key structure for table p6e_file_upload_chunk
-- ----------------------------
ALTER TABLE "public"."p6e_file_upload_chunk" ADD CONSTRAINT "p6e_file_upload_chunk_pkey" PRIMARY KEY ("id");
