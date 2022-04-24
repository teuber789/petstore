# Petstore

This project the beginnings of a RESTful service representing a pet store. It is written using Java 17 + Webflux + Postgres on R2DBC. Originally, it was based on Swagger's [Petstore API](https://editor.swagger.io), but has been modified to better follow RESTful best practices.

The project is only in its infancy; only the following have been completed:

- [x] Ability to create a category
- [x] Ability to retrieve a paginated list of categories

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

## A note about reactive pagination

There is [some discussion](https://stackoverflow.com/questions/46384618/how-apply-pagination-in-reactive-spring-data) in the community about whether it's a good idea to paginate APIs that are built on reactive streams. However, it is still necessary that reactive APIs have to return chunked pages of data from RESTful collections; otherwise, they would create serious performance blocks. I've chosen to implement pagination using a non-blocking list collection combined with Spring's Page object. This allows us to take full advantage of Webflux's performance improvements while also giving us the advantages of using traditional paging.

## Future work

This section describes future improvements that need to be made to the service

- [ ] CRUD endpoints for categories
- [ ] Implement field validation for input models
- [ ] Pet domain model
- [ ] CRUD endpoints for pets
- [ ] Search pets by status