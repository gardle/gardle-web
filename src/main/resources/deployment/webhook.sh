#!/bin/sh
echo "TAG=$1" > /home/k004765/gardle/.env
cd /home/k004765/gardle/
echo "stopping stack ..."
docker stack rm gardle
sleep 10s # wait for the network to be completely removed (might fail otherwise)
#echo "removing container ..."
#docker-compose -f /home/k004765/gardle/gardle-stack.yml rm -f airgnb-app
echo "pulling new container ..."
# Pulls the docker image associated with the gardle service automatically
docker pull "gardle/gardle:${TAG}"
# docker-compose -f /home/k004765/gardle/gardle-stack.yml pull gardle-app
echo "starting new stack ..."
# docker stack does not load .env files like docker compose does
# Retry 4 times (sometimes the network is not created first and deployment fails)
for i in 1 2 3 4; do TAG=$1 docker stack deploy --compose-file /home/k004765/gardle/gardle-stack.yml gardle && break || sleep 15; done
echo "completed"
