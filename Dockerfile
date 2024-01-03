FROM openjdk:17
COPY ./src/main/java/api/response/apiResponse/ /tmp
WORKDIR /tmp
ENTRYPOINT ["java","ApiResponseApplication"]
