docker network create docker_batch_network

docker run -d --name docker_batch_db \
    --network docker_batch_network \
    -e MYSQL_DATABASE=test \
    -e MYSQL_USER=user1 \
    -e MYSQL_PASSWORD=0000 \
    -e MYSQL_ROOT_PASSWORD=0000 \
    -v C:/mysql_data:/var/lib/mysql \
    mysql

echo 'batch-core 라이브러리 검색 이후 배치에 필요한 기본 테이블 정보 세팅'

