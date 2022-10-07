FROM ubuntu:20.04

# Install OpenJDK-8
RUN apt-get update && \
    apt-get install -y openjdk-11-jdk && \
    apt-get install -y ant && \
    apt-get clean;

# Fix certificate issues
RUN apt-get update && \
    apt-get install ca-certificates-java && \
    apt-get clean && \
    update-ca-certificates -f;

# Setup JAVA_HOME -- useful for docker commandline
ENV JAVA_HOME /usr/lib/jvm/java-11-openjdk-amd64/
RUN export JAVA_HOME

WORKDIR /

RUN apt-get -y install python3-pip
RUN pip3 install --upgrade pip

COPY scripts/requirements.txt scripts/requirements.txt
RUN pip3 install --no-cache-dir -r scripts/requirements.txt

COPY . clustering
RUN cd clustering && ./gradlew build

CMD ["bin/bash"]