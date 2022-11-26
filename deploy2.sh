REPOSITORY=/home/ec2-user/app
cd $REPOSITORY

source .env

APP_NAME=soccer

echo "=> docker login..."
docker login -u $DOCKER_USER_NAME -p $DOCKER_PASSWORD

echo "=> docker pull image..."
docker pull $REGISTRY_URL:$TAG

echo "=> docker stop..."
docker stop -t 10 $APP_NAME

echo "=> Remove previous container..."
docker rm -f $APP_NAME

# Run container
echo "=> Run container..."
docker run -d -p 8080:8080 -e PROFILE=dev $REGISTRY_URL:$TAG --name $APP_NAME

# remove image
echo "=> Remove previous image..."
docker rmi -f $APP_NAME
