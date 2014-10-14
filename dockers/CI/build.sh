#Example: build.sh arbusta-server 1.1 

NEW_UUID=$(cat /dev/urandom | tr -dc 'a-zA-Z0-9' | fold -w 32 | head -n 1)
APP=$1
VERSION=$2
SESSION=$BUILD-$APP:$VERSION


echo "Building builder image $SESSION"
#TEST the application
sudo docker build . -t $SESSION

#BUILD the application
#sudo docker run $SESSION

#REMOVE the session
#sudo docker rmi $SESSION
