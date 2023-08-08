FROM ubuntu:20.04

# Install OpenJDK-17
RUN apt-get update && \
    apt-get install -y openjdk-17-jdk && \
    apt-get install -y ant && \
    apt-get clean;

# Fix certificate issues
RUN apt-get update && \
    apt-get install ca-certificates-java && \
    apt-get clean && \
    update-ca-certificates -f;

# Setup JAVA_HOME -- useful for docker commandline
ENV JAVA_HOME /usr/lib/jvm/java-17-openjdk-amd64/
RUN export JAVA_HOME

RUN apt-get -y install python3-pip
RUN pip3 install --upgrade pip

COPY . /code-submissions-clustering
WORKDIR /code-submissions-clustering

RUN ./gradlew --no-daemon -x test build

EXPOSE 50051

ENTRYPOINT ["./gradlew", "-x", "test", "--no-daemon", ":code-submissions-clustering-ij:ij-code-server", "-Pport=50051"]

# Default programming language
CMD ["-Planguage=PYTHON"]
