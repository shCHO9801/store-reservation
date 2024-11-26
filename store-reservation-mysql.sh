docker run -d \
--name store-mysql \
-e MYSQL_ROOT_PASSWORD="store" \
-e MYSQL_USER="store" \
-e MYSQL_PASSWORD="store" \
-e MYSQL_DATABASE="store" \
-p 3306:3306 \
mysql:latest