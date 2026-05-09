mysql -u root -proot -e "DROP DATABASE IF EXISTS smart_campus; CREATE DATABASE smart_campus;"
mysql -u root -proot smart_campus < sql\schema.sql
mysql -u root -proot smart_campus < sql\seed.sql
