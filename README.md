# Petstore

## Prereqs

Before running the service, you must have the following installed and on your path:

* Java 17
* Docker
* psql (for initializing the database)
  * Note: mac users, see [this link](https://www.timescale.com/blog/how-to-install-psql-on-mac-ubuntu-debian-windows/)

## Running the service

The service is meant to be run inside a Docker container in the cloud. Spring gives us tools that make this really easy to do.

However, it's a bit trickier to get it to run locally correctly. Several steps have to be taken to make this work.

```shell
# Create a docker network, which will allow the database
# and the service containers to talk to each other
$ docker network create petstore-net

# Make sure you have the latest Postgres 14 image
$ docker pull postgres:14

# Create the postgres DB instance
$ docker run \
    --name petstore-db \
    --net petstore-net \
    -e POSTGRES_PASSWORD=acoolpassword \
    -p 5432:5432 \
    -d \
    postgres:14

# Create the database
$ psql -h 127.0.0.1 -p 5432 -U postgres -c "CREATE DATABASE petstore;"
<Enter your password at the prompt; in this case it will be "acoolpassword">

# Run tests
$ ./gradlew clean build

# Create the service's docker image
# This will create a Docker image called pets-service:0.0.1-SNAPSHOT
$ ./gradlew clean bootBuildImage

# Run the service
$ docker run \
    --name pets-service \
    --net petstore-net \
    -e DATASOURCE_URL='r2dbc:pool:postgresql://petstore-db:5432/petstore' \
    -e DATASOURCE_USERNAME='postgres' \
    -e DATASOURCE_PASSWORD='acoolpassword' \
    -p 8080:8080 \
    pets-service:0.0.1-SNAPSHOT
```
