# README

### Run dev env with redis
- Start redis stack with docker compose
  `docker compose -f docker-comp-redis-stack.yml up -d`
- Run Spring Boot app form ide (or with ./gradlew bootRun)

### Redis Insight
- Open in web browser `localhost:5540`
- Add connection to existing database: 'redis-dev:6379'

### Useful commands

redis and redis insight with docker compose
```
# docker
docker compose -f docker-comp-redis-stack.yml up -d
docker ps -a
docker compose -f docker-comp-redis-stack.yml stop/start/down

# Redis CLI prompt
docker exec -it container-name redis-cli
some redis cli commands:
keys '*'
set key1 'key1 value'
get key1
ttl key1
flushall
exit

# Redis CLI direct
docker exec container-name redis-cli monitor
docker exec container-name redis-cli ping
docker exec container-name redis-cli dbsize

```