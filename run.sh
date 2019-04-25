if [ "$1" != "" ]
then
    echo "UserName : $1"
fi
wget "http://localhost:3333/herest/execute?userName=$1&projectName=$2&tags=$3"
